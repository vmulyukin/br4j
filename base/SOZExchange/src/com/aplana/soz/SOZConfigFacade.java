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
package com.aplana.soz;

import com.aplana.dbmi.Portal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SOZConfigFacade {

    protected final static Log logger = LogFactory.getLog(SOZConfigFacade.class);

    private Properties properties = null;
    private static SOZConfigFacade instance;

    private SOZConfigFacade(boolean standalone) {
        properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream("/"+CONFIG_FILE_NAME);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        if (inputStream == null) {
            logger.error("Can't find " + CONFIG_FILE_NAME + " in CLASSPATH");
            throw new RuntimeException("Can't find " + CONFIG_FILE_NAME + " in CLASSPATH");
        }
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private SOZConfigFacade() {
        try {
            InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE_NAME);
            if (inputStream == null) {
                logger.error("Can't find " + CONFIG_FILE_NAME + " in CLASSPATH");
                throw new RuntimeException("Can't find " + CONFIG_FILE_NAME + " in CLASSPATH");
            }
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Can't find " + CONFIG_FILE_NAME + " in CLASSPATH");
            logger.error(e);
            throw new RuntimeException(e);
        }
    }


    public static synchronized SOZConfigFacade getInstance() {
        if (instance == null) {
            instance = new SOZConfigFacade();
        }
        return instance;
    }

    public final static String CONFIG_FILE_NAME = "dbmi/soz/config.properties" +
            "";
    public final static String EXPORT_FOLDER_PROPERTY = "soz.export.directory";
    public final static String RESULT_OK_EXPORT_FOLDER_PROPERTY = "soz.export.directory.result.ok";
    public final static String RESULT_FAIL_EXPORT_FOLDER_PROPERTY = "soz.export.directory.result.fail";
    public final static String SOZ_SERVER_URL = "soz.server.url";
    public final static String SOZ_SERVER_DMS_EXCHANGE_PORT = "soz.server.dms_exchange.port";
    public final static String SOZ_SERVER_DMS_EXCHANGE_SERVICE = "soz.server.dms_exchange.service";
    public final static String SOZ_EXPORT_PERIOD_SEC = "soz.export.period.sec";

    public String getExportDirectory() {
        return properties.getProperty(EXPORT_FOLDER_PROPERTY);
    }

    public String getExportResultOkDirectory() {
        return properties.getProperty(RESULT_OK_EXPORT_FOLDER_PROPERTY);
    }

    public String getExportResultFailDirectory() {
        return properties.getProperty(RESULT_FAIL_EXPORT_FOLDER_PROPERTY);
    }

    public String getServerURL() {
        return properties.getProperty(SOZ_SERVER_URL);
    }

    public String getDMSExchangePort() {
        return properties.getProperty(SOZ_SERVER_DMS_EXCHANGE_PORT);
    }

    public String getDMSExchangeService() {
        return properties.getProperty(SOZ_SERVER_DMS_EXCHANGE_SERVICE);
    }

    public int getPeriod() {
        String sec = properties.getProperty(SOZ_EXPORT_PERIOD_SEC, "300");
        int secInt = 300;
        try {
            secInt = Integer.parseInt(sec);
        } catch (NumberFormatException e) {
            logger.error("Wrong integer " + sec + ". Using default period" + secInt, e);
        }
        return secInt;
    }
}
