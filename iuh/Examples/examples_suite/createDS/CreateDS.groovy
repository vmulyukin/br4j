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
import groovy.util.logging.Log4j
import ru.datateh.jbr.iuh.groovy.AbstractExecute
import ru.datateh.jbr.iuh.groovy.anno.Parameters
import ru.datateh.jbr.iuh.groovy.utils.FileUtils

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author etarakanov
 * Date: 18.03.15
 * Time: 17:39
 */
@Log4j
public class CreateDS extends AbstractExecute {

//  -->наименование параметров
    private static final IUH_DB_NAME = "br4j.db.name";
    private static final IUH_DB_HOST_NAME = "br4j.db.host.name";
    private static final IUH_DB_PORT_NUMBER = "br4j.db.port.number";

    private static final String IUH_DS_JNDI_NAME_DBMIDS = "br4j.dbmi.ds.jndi-name.DBMIDS";
    private static final String IUH_DS_JNDI_NAME_DBMIDS_EVENT = "br4j.dbmi_event.ds.jndi-name.DBMIDS_EVENT";
    private static final String IUH_DS_JNDI_NAME_PORTAL = "br4j.jboss.portal.ds.jndi-name.PortalDS";

    private static final String IUH_DBMI_DB_URL = "br4j.dbmi.db.url";
    private static final String IUH_DBMI_DB_DRIVER = "br4j.dbmi.db.driver";
    private static final String IUH_DBMI_DB_USER = "br4j.dbmi.db.user.name";
    private static final String IUH_DBMI_DB_PASSWORD = "br4j.dbmi.db.user.password";
    private static final String IUH_DBMI_DB_POOLSIZE_MIN = "br4j.dbmi.db.poolsize.min";
    private static final String IUH_DBMI_DB_POOLSIZE_MAX = "br4j.dbmi.db.poolsize.max";
    private static final String IUH_DBMI_DB_CHECK_SQL = "br4j.dbmi.db.check_sql";

    private static final String IUH_DBMI_EVENT_DB_URL = "br4j.dbmi_event.db.url";
    private static final String IUH_DBMI_EVENT_DB_DRIVER = "br4j.dbmi_event.db.driver";
    private static final String IUH_DBMI_EVENT_DB_USER = "br4j.dbmi_event.db.user.name";
    private static final String IUH_DBMI_EVENT_DB_PASSWORD = "br4j.dbmi_event.db.user.password";
    private static final String IUH_DBMI_EVENT_DB_POOLSIZE_MIN = "br4j.dbmi_event.db.poolsize.min";
    private static final String IUH_DBMI_EVENT_DB_POOLSIZE_MAX = "br4j.dbmi_event.db.poolsize.max";
    private static final String IUH_DBMI_EVENT_DB_CHECK_SQL = "br4j.dbmi_event.db.check_sql";

    private static final String IUH_PORTAL_DB_URL = "br4j.jboss.portal.db.url";
    private static final String IUH_PORTAL_DB_DRIVER = "br4j.jboss.portal.db.driver";
    private static final String IUH_PORTAL_DB_USER = "br4j.jboss.portal.db.user.name";
    private static final String IUH_PORTAL_DB_PASSWORD = "br4j.jboss.portal.db.user.password";
    private static final String IUH_PORTAL_DB_POOLSIZE_MIN = "br4j.jboss.portal.db.poolsize.min";
    private static final String IUH_PORTAL_DB_POOLSIZE_MAX = "br4j.jboss.portal.db.poolsize.max";
    private static final String IUH_PORTAL_DB_CHECK_SQL = "br4j.jboss.portal.db.check_sql";

    private static final String IUH_DS_TYPE_MAPPING = "br4j.db.type-mapping";
//  <--наименование параметров
//  значения параметров по умолчанию
    private static final IUH_DB_NAME_DEFVAL = "br4j";
    private static final IUH_DB_HOST_NAME_DEFVAL = "localhost";
    private static final IUH_DB_PORT_NUMBER_DEFVAL = "5432";

    private static final String IUH_DS_JNDI_NAME_DBMIDS_DEFVAL = "jdbc/DBMIDS";
    private static final String IUH_DS_JNDI_NAME_DBMIDS_EVENT_DEFVAL = "jdbc/DBMIDS_EVENT";
    private static final String IUH_DS_JNDI_NAME_PORTAL_DEFVAL = "PortalDS";

    private static final String IUH_DBMI_DB_DRIVER_DEFVAL = "org.postgresql.Driver";
    private static final String IUH_DBMI_DB_USER_DEFVAL = "dbmi";
    private static final String IUH_DBMI_DB_PASSWORD_DEFVAL = "dbmi";
    private static final String IUH_DBMI_DB_POOLSIZE_MIN_DEFVAL = "1";
    private static final String IUH_DBMI_DB_POOLSIZE_MAX_DEFVAL = "400";
    private static final String IUH_DBMI_DB_CHECK_SQL_DEFVAL = "SELECT 1";

    private static final String IUH_DBMI_EVENT_DB_DRIVER_DEFVAL = "org.postgresql.Driver";
    private static final String IUH_DBMI_EVENT_DB_USER_DEFVAL = "dbmi";
    private static final String IUH_DBMI_EVENT_DB_PASSWORD_DEFVAL = "dbmi";
    private static final String IUH_DBMI_EVENT_DB_POOLSIZE_MIN_DEFVAL = "1";
    private static final String IUH_DBMI_EVENT_DB_POOLSIZE_MAX_DEFVAL = "400";
    private static final String IUH_DBMI_EVENT_DB_CHECK_SQL_DEFVAL = "SELECT 1";

    private static final String IUH_PORTAL_DB_DRIVER_DEFVAL = "org.postgresql.Driver";
    private static final String IUH_PORTAL_DB_USER_DEFVAL = "portal";
    private static final String IUH_PORTAL_DB_PASSWORD_DEFVAL = "portal";
    private static final String IUH_PORTAL_DB_POOLSIZE_MIN_DEFVAL = "1";
    private static final String IUH_PORTAL_DB_POOLSIZE_MAX_DEFVAL = "400";
    private static final String IUH_PORTAL_DB_CHECK_SQL_DEFVAL = "SELECT 1";

    private static final String IUH_DS_TYPE_MAPPING_DEFVAL = "PostgreSQL 9.3";
//  <--значения параметров по умолчанию

    private Map<String, String> properties;
    private DataSources dataSources;


    public void install() {
        log.info "CreateDS is running... "

        File fileProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'deploy' + File.separator + 'DBMI-pg-xa-ds.xml');

        File exampleProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'deploy' + File.separator + 'DBMI-pg-xa-ds.xml.example');

        log.info "file " + exampleProperties + " exist = " + FileUtils.checkFileExist(exampleProperties, false)
        log.info "file " + fileProperties + " exist = " + FileUtils.checkFileExist(fileProperties, false)

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            FileUtils.copyFile(exampleProperties, fileProperties);
            if (!checkPropertiesValues(fileProperties)) {
                updateRequiredProperty(fileProperties);
            }
            map.putAll(properties);
            FileUtils.changeFilePermission(fileProperties, FileUtils.Permission.WRITE, false);
        }
        else if(FileUtils.checkFileExist(fileProperties, false))
        {
            map.putAll(extractPropertiesFromDataSources(getDataSourcesFromFile(fileProperties)));
        }

        log.info "CreateDS successfully finished... ";
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(IUH_DB_NAME, getPropertyValue(IUH_DB_NAME, IUH_DB_NAME_DEFVAL));
            properties.put(IUH_DB_HOST_NAME, getPropertyValue(IUH_DB_HOST_NAME, IUH_DB_HOST_NAME_DEFVAL));
            properties.put(IUH_DB_PORT_NUMBER, getPropertyValue(IUH_DB_PORT_NUMBER, IUH_DB_PORT_NUMBER_DEFVAL));

            properties.put(IUH_DS_JNDI_NAME_DBMIDS, getPropertyValue(IUH_DS_JNDI_NAME_DBMIDS, IUH_DS_JNDI_NAME_DBMIDS_DEFVAL));
            properties.put(IUH_DS_JNDI_NAME_DBMIDS_EVENT, getPropertyValue(IUH_DS_JNDI_NAME_DBMIDS_EVENT, IUH_DS_JNDI_NAME_DBMIDS_EVENT_DEFVAL));
            properties.put(IUH_DS_JNDI_NAME_PORTAL, getPropertyValue(IUH_DS_JNDI_NAME_PORTAL, IUH_DS_JNDI_NAME_PORTAL_DEFVAL));

            properties.put(IUH_DBMI_DB_URL, "jdbc:postgresql://${properties.get(IUH_DB_HOST_NAME)}:${properties.get(IUH_DB_PORT_NUMBER)}/${properties.get(IUH_DB_NAME)}");
            properties.put(IUH_DBMI_DB_DRIVER, getPropertyValue(IUH_DBMI_DB_DRIVER, IUH_DBMI_DB_DRIVER_DEFVAL));
            properties.put(IUH_DBMI_DB_USER, getPropertyValue(IUH_DBMI_DB_USER, IUH_DBMI_DB_USER_DEFVAL));
            properties.put(IUH_DBMI_DB_PASSWORD, getPropertyValue(IUH_DBMI_DB_PASSWORD, IUH_DBMI_DB_PASSWORD_DEFVAL));
            properties.put(IUH_DBMI_DB_POOLSIZE_MIN, getPropertyValue(IUH_DBMI_DB_POOLSIZE_MIN, IUH_DBMI_DB_POOLSIZE_MIN_DEFVAL));
            properties.put(IUH_DBMI_DB_POOLSIZE_MAX, getPropertyValue(IUH_DBMI_DB_POOLSIZE_MAX, IUH_DBMI_DB_POOLSIZE_MAX_DEFVAL));
            properties.put(IUH_DBMI_DB_CHECK_SQL, getPropertyValue(IUH_DBMI_DB_CHECK_SQL, IUH_DBMI_DB_CHECK_SQL_DEFVAL));

            properties.put(IUH_DBMI_EVENT_DB_URL, "jdbc:postgresql://${properties.get(IUH_DB_HOST_NAME)}:${properties.get(IUH_DB_PORT_NUMBER)}/${properties.get(IUH_DB_NAME)}");
            properties.put(IUH_DBMI_EVENT_DB_DRIVER, getPropertyValue(IUH_DBMI_EVENT_DB_DRIVER, IUH_DBMI_EVENT_DB_DRIVER_DEFVAL));
            properties.put(IUH_DBMI_EVENT_DB_USER, getPropertyValue(IUH_DBMI_EVENT_DB_USER, IUH_DBMI_EVENT_DB_USER_DEFVAL));
            properties.put(IUH_DBMI_EVENT_DB_PASSWORD, getPropertyValue(IUH_DBMI_EVENT_DB_PASSWORD, IUH_DBMI_EVENT_DB_PASSWORD_DEFVAL));
            properties.put(IUH_DBMI_EVENT_DB_POOLSIZE_MIN, getPropertyValue(IUH_DBMI_EVENT_DB_POOLSIZE_MIN, IUH_DBMI_EVENT_DB_POOLSIZE_MIN_DEFVAL));
            properties.put(IUH_DBMI_EVENT_DB_POOLSIZE_MAX, getPropertyValue(IUH_DBMI_EVENT_DB_POOLSIZE_MAX, IUH_DBMI_EVENT_DB_POOLSIZE_MAX_DEFVAL));
            properties.put(IUH_DBMI_EVENT_DB_CHECK_SQL, getPropertyValue(IUH_DBMI_EVENT_DB_CHECK_SQL, IUH_DBMI_EVENT_DB_CHECK_SQL_DEFVAL));

            properties.put(IUH_PORTAL_DB_URL, "jdbc:postgresql://${properties.get(IUH_DB_HOST_NAME)}:${properties.get(IUH_DB_PORT_NUMBER)}/${properties.get(IUH_DB_NAME)}");
            properties.put(IUH_PORTAL_DB_DRIVER, getPropertyValue(IUH_PORTAL_DB_DRIVER, IUH_PORTAL_DB_DRIVER_DEFVAL));
            properties.put(IUH_PORTAL_DB_USER, getPropertyValue(IUH_PORTAL_DB_USER, IUH_PORTAL_DB_USER_DEFVAL));
            properties.put(IUH_PORTAL_DB_PASSWORD, getPropertyValue(IUH_PORTAL_DB_PASSWORD, IUH_PORTAL_DB_PASSWORD_DEFVAL));
            properties.put(IUH_PORTAL_DB_POOLSIZE_MIN, getPropertyValue(IUH_PORTAL_DB_POOLSIZE_MIN, IUH_PORTAL_DB_POOLSIZE_MIN_DEFVAL));
            properties.put(IUH_PORTAL_DB_POOLSIZE_MAX, getPropertyValue(IUH_PORTAL_DB_POOLSIZE_MAX, IUH_PORTAL_DB_POOLSIZE_MAX_DEFVAL));
            properties.put(IUH_PORTAL_DB_CHECK_SQL, getPropertyValue(IUH_PORTAL_DB_CHECK_SQL, IUH_PORTAL_DB_CHECK_SQL_DEFVAL));

            properties.put(IUH_DS_TYPE_MAPPING, getPropertyValue(IUH_DS_TYPE_MAPPING, IUH_DS_TYPE_MAPPING_DEFVAL));
        }
        return properties
    }

    private static DataSources collectDataSources(Map<String, String> parameters)
    {
        DataSources result = new DataSources();
        List<DataSourceDS> dses = new ArrayList<DataSourceDS>();
        List<String> typeMapping = new ArrayList<String>();
        typeMapping.add(parameters.get(IUH_DS_TYPE_MAPPING));

        DataSourceDS dbmi = new DataSourceDS();
        dbmi.setJndiName(parameters.get(IUH_DS_JNDI_NAME_DBMIDS));
        dbmi.setUrl(parameters.get(IUH_DBMI_DB_URL));
        dbmi.setDriverClass(parameters.get(IUH_DBMI_DB_DRIVER));
        dbmi.setUserName(parameters.get(IUH_DBMI_DB_USER));
        dbmi.setPassword(parameters.get(IUH_DBMI_DB_PASSWORD));
        dbmi.setMinPoolSize(parameters.get(IUH_DBMI_DB_POOLSIZE_MIN));
        dbmi.setMaxPoolSize(parameters.get(IUH_DBMI_DB_POOLSIZE_MAX));
        dbmi.setCheckedSql(parameters.get(IUH_DBMI_DB_CHECK_SQL));
        dbmi.setTypeMappings(typeMapping);

        DataSourceDS dbmi_event = new DataSourceDS();
        dbmi_event.setJndiName(parameters.get(IUH_DS_JNDI_NAME_DBMIDS_EVENT));
        dbmi_event.setUrl(parameters.get(IUH_DBMI_EVENT_DB_URL));
        dbmi_event.setDriverClass(parameters.get(IUH_DBMI_EVENT_DB_DRIVER));
        dbmi_event.setUserName(parameters.get(IUH_DBMI_EVENT_DB_USER));
        dbmi_event.setPassword(parameters.get(IUH_DBMI_EVENT_DB_PASSWORD));
        dbmi_event.setMinPoolSize(parameters.get(IUH_DBMI_EVENT_DB_POOLSIZE_MIN));
        dbmi_event.setMaxPoolSize(parameters.get(IUH_DBMI_EVENT_DB_POOLSIZE_MAX));
        dbmi_event.setCheckedSql(parameters.get(IUH_DBMI_EVENT_DB_CHECK_SQL));
        dbmi_event.setTypeMappings(typeMapping);

        DataSourceDS portal = new DataSourceDS();
        portal.setJndiName(parameters.get(IUH_DS_JNDI_NAME_PORTAL));
        portal.setUrl(parameters.get(IUH_PORTAL_DB_URL));
        portal.setDriverClass(parameters.get(IUH_PORTAL_DB_DRIVER));
        portal.setUserName(parameters.get(IUH_PORTAL_DB_USER));
        portal.setPassword(parameters.get(IUH_PORTAL_DB_PASSWORD));
        portal.setMinPoolSize(parameters.get(IUH_PORTAL_DB_POOLSIZE_MIN));
        portal.setMaxPoolSize(parameters.get(IUH_PORTAL_DB_POOLSIZE_MAX));
        portal.setCheckedSql(parameters.get(IUH_PORTAL_DB_CHECK_SQL));
        portal.setTypeMappings(typeMapping);

        dses.add(dbmi);
        dses.add(dbmi_event);
        dses.add(portal);

        result.setDataSourceList(dses);

        log.trace(result.toString());

        return result;
    }

    private static DataSources getDataSourcesFromFile(File file)
    {
        JAXBContext jc = JAXBContext.newInstance(DataSources.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        DataSources ds = (DataSources) unmarshaller.unmarshal(file);
        log.trace(ds.toString());
        return ds;
    }

    private static Map<String, String> extractPropertiesFromDataSources (DataSources data)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (DataSourceDS ds : data.getDataSourceList())
        {
            if (ds.getJndiName().equals(IUH_DS_JNDI_NAME_DBMIDS_DEFVAL)) {
                String cleanURI = ds.getUrl().substring(5);
                URI uri = URI.create(cleanURI);
                result.put(IUH_DB_NAME, uri.getPath().substring(1));
                result.put(IUH_DB_HOST_NAME, uri.getHost());
                result.put(IUH_DB_PORT_NUMBER, Integer.toString(uri.getPort()));
                result.put(IUH_DS_JNDI_NAME_DBMIDS, ds.getJndiName());
                result.put(IUH_DBMI_DB_URL, ds.getUrl());
                result.put(IUH_DBMI_DB_DRIVER,  ds.getDriverClass());
                result.put(IUH_DBMI_DB_USER,  ds.getUserName());
                result.put(IUH_DBMI_DB_PASSWORD,  ds.getPassword());
                result.put(IUH_DBMI_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(IUH_DBMI_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(IUH_DBMI_DB_CHECK_SQL,  ds.getCheckedSql());
                result.put(IUH_DS_TYPE_MAPPING, ds.getTypeMappings() == null ? "" : ds.getTypeMappings().get(0));
            } else if (ds.getJndiName().equals(IUH_DS_JNDI_NAME_DBMIDS_EVENT_DEFVAL))
            {
                result.put(IUH_DS_JNDI_NAME_DBMIDS_EVENT, ds.getJndiName());
                result.put(IUH_DBMI_EVENT_DB_URL, ds.getUrl());
                result.put(IUH_DBMI_EVENT_DB_DRIVER,  ds.getDriverClass());
                result.put(IUH_DBMI_EVENT_DB_USER,  ds.getUserName());
                result.put(IUH_DBMI_EVENT_DB_PASSWORD,  ds.getPassword());
                result.put(IUH_DBMI_EVENT_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(IUH_DBMI_EVENT_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(IUH_DBMI_EVENT_DB_CHECK_SQL,  ds.getCheckedSql());
            } else if(ds.getJndiName().equals(IUH_DS_JNDI_NAME_PORTAL_DEFVAL))
            {
                result.put(IUH_DS_JNDI_NAME_PORTAL, ds.getJndiName());
                result.put(IUH_PORTAL_DB_URL, ds.getUrl());
                result.put(IUH_PORTAL_DB_DRIVER,  ds.getDriverClass());
                result.put(IUH_PORTAL_DB_USER,  ds.getUserName());
                result.put(IUH_PORTAL_DB_PASSWORD,  ds.getPassword());
                result.put(IUH_PORTAL_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(IUH_PORTAL_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(IUH_PORTAL_DB_CHECK_SQL,  ds.getCheckedSql());
            }
        }
        return result;
    }

    private boolean checkPropertiesValues(File file)
    {
        dataSources = collectDataSources(collectProperties());
        if (dataSources.equals(getDataSourcesFromFile(file)))
        {
            return true;
        }
        return false;
    }

    private void updateRequiredProperty(File file)
    {
        log.trace(dataSources.toString());
        JAXBContext jc = JAXBContext.newInstance(DataSources.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(dataSources, file);
    }

    public static void main(String[] args) {
        new CreateDS().start()
    }

    @XmlRootElement(name="datasources")
    @XmlAccessorType(XmlAccessType.NONE)
    private static class DataSources
    {
        @XmlElement(name="local-tx-datasource")
        private List<DataSourceDS> dataSourceList;

        List<DataSourceDS> getDataSourceList() {
            return dataSourceList;
        }

        void setDataSourceList(List<DataSourceDS> dataSourceList) {
            this.dataSourceList = dataSourceList;
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof DataSources)) return false

            DataSources that = (DataSources) o

            if (dataSourceList == that.dataSourceList) return true;

            if (dataSourceList.size() != that.getDataSourceList().size()) return false;

            int index = 0;
            for (DataSourceDS ds : dataSourceList)
            {
                if (!ds.equals(that.getDataSourceList()[index]))
                {
                    return false;
                }
                index++;
            }

            return true
        }

        int hashCode() {
            return dataSourceList.hashCode()
        }

        @Override
        public String toString() {
            return "DataSources{" +
                    "dataSourceList=" + dataSourceList +
                    '}';
        }
    }

    @XmlAccessorType(XmlAccessType.NONE)
    private static class DataSourceDS
    {
        @XmlElement(name="jndi-name")
        private String jndiName;

        @XmlElement(name="connection-url")
        private String url;

        @XmlElement(name="user-name")
        private String userName;

        @XmlElement(name="password")
        private String password;

        @XmlElement(name="driver-class")
        private String driverClass;

        @XmlElement(name="min-pool-size")
        private String minPoolSize;

        @XmlElement(name="max-pool-size")
        private String maxPoolSize;

        @XmlElement(name="check-valid-connection-sql")
        private String checkedSql;

        @XmlElementWrapper(name="metadata")
        @XmlElement(name="type-mapping")
        private List<String> typeMappings;

        String getJndiName() {
            return jndiName;
        }

        void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }

        String getUrl() {
            return url;
        }

        void setUrl(String url) {
            this.url = url;
        }

        String getUserName() {
            return userName;
        }

        void setUserName(String userName) {
            this.userName = userName;
        }

        String getPassword() {
            return password;
        }

        void setPassword(String password) {
            this.password = password;
        }

        String getDriverClass() {
            return driverClass;
        }

        void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
        }

        String getMinPoolSize() {
            return minPoolSize;
        }

        void setMinPoolSize(String minPoolSize) {
            this.minPoolSize = minPoolSize;
        }

        String getMaxPoolSize() {
            return maxPoolSize;
        }

        void setMaxPoolSize(String maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        String getCheckedSql() {
            return checkedSql;
        }

        void setCheckedSql(String checkedSql) {
            this.checkedSql = checkedSql;
        }

        List<String> getTypeMappings() {
            return typeMappings;
        }

        void setTypeMappings(List<String> typeMappings) {
            this.typeMappings = typeMappings;
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof DataSourceDS)) return false

            DataSourceDS that = (DataSourceDS) o

            if (checkedSql != that.checkedSql) return false
            if (driverClass != that.driverClass) return false
            if (jndiName != that.jndiName) return false
            if (maxPoolSize != that.maxPoolSize) return false
            if (minPoolSize != that.minPoolSize) return false
            if (password != that.password) return false
            if (url != that.url) return false
            if (userName != that.userName) return false

            return true
        }

        int hashCode() {
            int result
            result = jndiName.hashCode()
            result = 31 * result + url.hashCode()
            result = 31 * result + userName.hashCode()
            result = 31 * result + password.hashCode()
            result = 31 * result + driverClass.hashCode()
            result = 31 * result + minPoolSize.hashCode()
            result = 31 * result + maxPoolSize.hashCode()
            result = 31 * result + checkedSql.hashCode()
            return result
        }

        @Override
        public String toString() {
            return "DataSourceDS{" +
                    "jndiName='" + jndiName + '\'' +
                    ", url='" + url + '\'' +
                    ", userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    ", driverClass='" + driverClass + '\'' +
                    ", minPoolSize='" + minPoolSize + '\'' +
                    ", maxPoolSize='" + maxPoolSize + '\'' +
                    ", checkedSql='" + checkedSql + '\'' +
                    ", typeMappings=" + typeMappings +
                    '}';
        }
    }
}
