/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.quartz.sheduler;

import com.aplana.dbmi.jobs.InterruptableEjbJob;
import com.aplana.quartz.utils.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.management.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author etarakanov
 *         Date: 12.05.2015
 *         Time: 16:00
 */
public class JbossShutdownInterceptor implements JbossShutdownInterceptorMBean {

    private Log logger = LogFactory.getLog(getClass());
    private static final String BUILD_INFO = "dbmi/build-info.properties";
    private static final String BR4J_CONFIG = "dbmi/br4j.properties";

    private static final String BUILD_NUMBER = "build.number";
    private static final String SHUTDOWN_FLAG = "jboss_shutdown";
    private static final String BUILD_NUMBER_REGEX = "build_number_regex";

    private static final String REGEX_DEFAULT = "\\d{2}\\.\\d{1,}\\.\\d{3}\\.\\d{2,}\\.\\d{3}\\..{3}\\.{0,1}\\w{0,3}";

    private static final String GET_DB_VERSION = "SELECT build \n" +
            "FROM dbm_changelog \n" +
            "WHERE build ~ ? \n" +
            "order by date_time desc limit 1";

    public JbossShutdownInterceptor(){
        logger.info("JbossShutdownInterceptor service was starting...");
    }


    @Override
    public void start()
    {
        logger.info("Jboss on start interceptor was started");

        Properties br4jProps = getPropertiesFromConfig(BR4J_CONFIG);
        boolean shutdownFlag = getShutdownFlag(br4jProps);
        String regex = getBuildNumberRegex(br4jProps);
        String appVersion = getAppVersion(getPropertiesFromConfig(BUILD_INFO), regex);
        String dbVersion = getDBVersion(regex);

        if (!appVersion.isEmpty() && !dbVersion.isEmpty())
        {
            logger.info("Application version: " + appVersion);
            logger.info("DB version: " + dbVersion);
            logger.debug("Regex for build number: " + regex);
            logger.debug("JBOSS shutdown flag: " + shutdownFlag);

            if (!appVersion.equalsIgnoreCase(dbVersion))
            {
                logger.error("Application version not correspond DB version. Using application may corrupt data");
                if (shutdownFlag)
                {
                    logger.fatal("Application will be undeployed");
                    shutdownMainDeployer();
                }
            }
        } else {
            logger.warn("Could not get information about Application or DB version. Check your DB connection or " + BUILD_INFO);
        }

    }

    @Override
    public void stop()
    {
        logger.info("Sheduled tasks will stopped");
        Scheduler quartz = getQuartz();
        if (quartz != null) {
            try {
                List<JobExecutionContext> jobs= quartz.getCurrentlyExecutingJobs();
                for (JobExecutionContext ctx : jobs) {
                    Job job=ctx.getJobInstance();
                    if (job instanceof InterruptableEjbJob) {
                        ((InterruptableEjbJob)job).setJobExecutionContext(ctx);
                        ((InterruptableEjbJob)job).interrupt();
                    }
                }
                quartz.shutdown(true);
            }
            catch (  Exception ex) {
                logger.warn("Could not interrupt all the current jobs properly", ex);
            }
        }
        else
        {
            logger.warn("Could not get Quarts service");
        }
    }

    private Scheduler getQuartz()
    {
        Context ctx;
        Scheduler quartz = null;
        try {
            ctx = new InitialContext();
            quartz = (Scheduler) ctx.lookup("Quartz");
        } catch (NamingException e) {
            logger.error("Error connecting Quartz service", e);
        }
        return quartz;
    }

    private void shutdownMainDeployer()
    {
        try {
            Context ctx = new InitialContext();
            MBeanServerConnection mconn = (MBeanServerConnection) ctx.lookup("jmx/invoker/RMIAdaptor");
            ObjectName name = new ObjectName("jboss.system:service=MainDeployer");
            mconn.invoke(name, "shutdown", null, null);

        } catch (NamingException e) {
            logger.error("Error while lookup: jmx/invoker/RMIAdaptor", e);
        } catch (MalformedObjectNameException e) {
            logger.error("Wrong format of ObjectName contstruct parameter  ", e);
        } catch (ReflectionException e) {
            logger.error("Wraps a java.lang.Exception thrown while trying to invoke the method.",e);
        } catch (MBeanException e) {
            logger.error("Wraps an exception thrown by the MBean's invoked method.",e);
        } catch (InstanceNotFoundException e) {
            logger.error("The MBean specified is not registered in the MBean server.",e);
        } catch (IOException e) {
            logger.error("A communication problem occurred when talking to the MBean server.", e);
        }
    }

    private String getDBVersion(String regex)
    {
        String result;
        Context context;
        try {
            context = new InitialContext();
            DataSource dataSource = (DataSource)context.lookup("java:/jdbc/DBMIDS");
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            result = (String) jdbc.queryForObject(GET_DB_VERSION, new String[] { regex }, String.class);
            return extractBuildNumber(result, regex);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return "";
    }

    private Properties getPropertiesFromConfig(String configName)
    {
        Configuration configuration = new Configuration(configName);
        configuration.loadConfig();
        return configuration.getConfig();
    }

    private boolean getShutdownFlag(Properties properties)
    {
        return Boolean.parseBoolean(properties.getProperty(SHUTDOWN_FLAG, "false"));
    }

    private String getBuildNumberRegex(Properties properties)
    {
        return properties.getProperty(BUILD_NUMBER_REGEX, REGEX_DEFAULT);
    }

    private String getAppVersion (Properties properties, String regex)
    {
        return extractBuildNumber(properties.getProperty(BUILD_NUMBER, null), regex);
    }

    private String extractBuildNumber (String versionInfo, String regex)
    {
        String result = "";
        if (versionInfo != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(versionInfo);
            if (matcher.find())
            {
                result = matcher.group();
            }
        }
        return result;
    }

}
