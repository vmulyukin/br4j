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
import ru.datateh.jbr.iuh.AbstractExecute
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*
/**
 * @author etarakanov
 * Date: 18.03.15
 * Time: 17:39
 */
@Log4j
public class CreateDS extends AbstractExecute {

//  значения параметров по умолчанию
    private static final String IUH_DS_JNDI_NAME_DBMIDS_DEFVAL = "jdbc/DBMIDS";
    private static final String IUH_DS_JNDI_NAME_DBMIDS_EVENT_DEFVAL = "jdbc/DBMIDS_EVENT";
    private static final String IUH_DS_JNDI_NAME_PORTAL_DEFVAL = "PortalDS";
//  <--значения параметров по умолчанию

    private Map<String, String> properties;
    private DataSources dataSources;


    public void install() {
        log.info "CreateDS is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'deploy' + File.separator + getPropertyValue(CommonParameters.DataSources.DS_FILE_NAME,"br4j-ds.xml"));

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'deploy' + File.separator + "${getPropertyValue(CommonParameters.DataSources.DS_FILE_NAME,"br4j-ds.xml")}.example");

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
        if (!FileUtils.deleteFile(exampleProperties.getPath()))
        {
            throw new HarnessException(new Message(MessageType.ERROR, "Cannot delete file: " + exampleProperties));
        }

        log.info "CreateDS successfully finished... ";
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(CommonParameters.DataSources.DB_NAME, getPropertyValue(CommonParameters.DataSources.DB_NAME, null));
            properties.put(CommonParameters.DataSources.DB_HOST_NAME, getPropertyValue(CommonParameters.DataSources.DB_HOST_NAME, null));
            properties.put(CommonParameters.DataSources.DB_PORT_NUMBER, getPropertyValue(CommonParameters.DataSources.DB_PORT_NUMBER, null));

            properties.put(CommonParameters.DataSources.JNDI_NAME_DBMIDS, getPropertyValue(CommonParameters.DataSources.JNDI_NAME_DBMIDS, null));
            properties.put(CommonParameters.DataSources.JNDI_NAME_DBMIDS_EVENT, getPropertyValue(CommonParameters.DataSources.JNDI_NAME_DBMIDS_EVENT, null));
            properties.put(CommonParameters.DataSources.JNDI_NAME_PORTAL, getPropertyValue(CommonParameters.DataSources.JNDI_NAME_PORTAL, null));

            properties.put(CommonParameters.DataSources.DBMI_DB_URL, "jdbc:postgresql://${properties.get(CommonParameters.DataSources.DB_HOST_NAME)}:" +
                    "${properties.get(CommonParameters.DataSources.DB_PORT_NUMBER)}/${properties.get(CommonParameters.DataSources.DB_NAME)}");
            properties.put(CommonParameters.DataSources.DBMI_DB_DRIVER, getPropertyValue(CommonParameters.DataSources.DBMI_DB_DRIVER, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_USER, getPropertyValue(CommonParameters.DataSources.DBMI_DB_USER, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_PASSWORD, getPropertyValue(CommonParameters.DataSources.DBMI_DB_PASSWORD, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MIN, getPropertyValue(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MIN, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MAX, getPropertyValue(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MAX, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_CHECK_SQL, getPropertyValue(CommonParameters.DataSources.DBMI_DB_CHECK_SQL, null));

            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_URL, "jdbc:postgresql://${properties.get(CommonParameters.DataSources.DB_HOST_NAME)}:" +
                    "${properties.get(CommonParameters.DataSources.DB_PORT_NUMBER)}/${properties.get(CommonParameters.DataSources.DB_NAME)}");
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_DRIVER, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_DRIVER, null));
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_USER, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_USER, null));
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_PASSWORD, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_PASSWORD, null));
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MIN, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MIN, null));
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MAX, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MAX, null));
            properties.put(CommonParameters.DataSources.DBMI_EVENT_DB_CHECK_SQL, getPropertyValue(CommonParameters.DataSources.DBMI_EVENT_DB_CHECK_SQL, null));

            properties.put(CommonParameters.DataSources.PORTAL_DB_URL, "jdbc:postgresql://${properties.get(CommonParameters.DataSources.DB_HOST_NAME)}:" +
                    "${properties.get(CommonParameters.DataSources.DB_PORT_NUMBER)}/${properties.get(CommonParameters.DataSources.DB_NAME)}");
            properties.put(CommonParameters.DataSources.PORTAL_DB_DRIVER, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_DRIVER, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_USER, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_USER, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_PASSWORD, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_PASSWORD, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MIN, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MIN, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MAX, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MAX, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_CHECK_SQL, getPropertyValue(CommonParameters.DataSources.PORTAL_DB_CHECK_SQL, null));

            properties.put(CommonParameters.DataSources.TYPE_MAPPING, getPropertyValue(CommonParameters.DataSources.TYPE_MAPPING, null));
        }
        return properties
    }

    private static DataSources collectDataSources(Map<String, String> parameters)
    {
        DataSources result = new DataSources();
        List<DataSourceDS> dses = new ArrayList<DataSourceDS>();
        List<String> typeMapping = new ArrayList<String>();
        typeMapping.add(parameters.get(CommonParameters.DataSources.TYPE_MAPPING));

        DataSourceDS dbmi = new DataSourceDS();
        dbmi.setJndiName(parameters.get(CommonParameters.DataSources.JNDI_NAME_DBMIDS));
        dbmi.setUrl(parameters.get(CommonParameters.DataSources.DBMI_DB_URL));
        dbmi.setDriverClass(parameters.get(CommonParameters.DataSources.DBMI_DB_DRIVER));
        dbmi.setUserName(parameters.get(CommonParameters.DataSources.DBMI_DB_USER));
        dbmi.setPassword(parameters.get(CommonParameters.DataSources.DBMI_DB_PASSWORD));
        dbmi.setMinPoolSize(parameters.get(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MIN));
        dbmi.setMaxPoolSize(parameters.get(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MAX));
        dbmi.setCheckedSql(parameters.get(CommonParameters.DataSources.DBMI_DB_CHECK_SQL));
        dbmi.setTypeMappings(typeMapping);

        DataSourceDS dbmi_event = new DataSourceDS();
        dbmi_event.setJndiName(parameters.get(CommonParameters.DataSources.JNDI_NAME_DBMIDS_EVENT));
        dbmi_event.setUrl(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_URL));
        dbmi_event.setDriverClass(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_DRIVER));
        dbmi_event.setUserName(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_USER));
        dbmi_event.setPassword(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_PASSWORD));
        dbmi_event.setMinPoolSize(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MIN));
        dbmi_event.setMaxPoolSize(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MAX));
        dbmi_event.setCheckedSql(parameters.get(CommonParameters.DataSources.DBMI_EVENT_DB_CHECK_SQL));
        dbmi_event.setTypeMappings(typeMapping);

        DataSourceDS portal = new DataSourceDS();
        portal.setJndiName(parameters.get(CommonParameters.DataSources.JNDI_NAME_PORTAL));
        portal.setUrl(parameters.get(CommonParameters.DataSources.PORTAL_DB_URL));
        portal.setDriverClass(parameters.get(CommonParameters.DataSources.PORTAL_DB_DRIVER));
        portal.setUserName(parameters.get(CommonParameters.DataSources.PORTAL_DB_USER));
        portal.setPassword(parameters.get(CommonParameters.DataSources.PORTAL_DB_PASSWORD));
        portal.setMinPoolSize(parameters.get(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MIN));
        portal.setMaxPoolSize(parameters.get(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MAX));
        portal.setCheckedSql(parameters.get(CommonParameters.DataSources.PORTAL_DB_CHECK_SQL));
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
                result.put(CommonParameters.DataSources.DB_NAME, uri.getPath().substring(1));
                result.put(CommonParameters.DataSources.DB_HOST_NAME, uri.getHost());
                result.put(CommonParameters.DataSources.DB_PORT_NUMBER, Integer.toString(uri.getPort()));
                result.put(CommonParameters.DataSources.JNDI_NAME_DBMIDS, ds.getJndiName());
                result.put(CommonParameters.DataSources.DBMI_DB_URL, ds.getUrl());
                result.put(CommonParameters.DataSources.DBMI_DB_DRIVER,  ds.getDriverClass());
                result.put(CommonParameters.DataSources.DBMI_DB_USER,  ds.getUserName());
                result.put(CommonParameters.DataSources.DBMI_DB_PASSWORD,  ds.getPassword());
                result.put(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(CommonParameters.DataSources.DBMI_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(CommonParameters.DataSources.DBMI_DB_CHECK_SQL,  ds.getCheckedSql());
                result.put(CommonParameters.DataSources.TYPE_MAPPING, ds.getTypeMappings() == null ? "" : ds.getTypeMappings().get(0));
            } else if (ds.getJndiName().equals(IUH_DS_JNDI_NAME_DBMIDS_EVENT_DEFVAL))
            {
                result.put(CommonParameters.DataSources.JNDI_NAME_DBMIDS_EVENT, ds.getJndiName());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_URL, ds.getUrl());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_DRIVER,  ds.getDriverClass());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_USER,  ds.getUserName());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_PASSWORD,  ds.getPassword());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(CommonParameters.DataSources.DBMI_EVENT_DB_CHECK_SQL,  ds.getCheckedSql());
            } else if(ds.getJndiName().equals(IUH_DS_JNDI_NAME_PORTAL_DEFVAL))
            {
                result.put(CommonParameters.DataSources.JNDI_NAME_PORTAL, ds.getJndiName());
                result.put(CommonParameters.DataSources.PORTAL_DB_URL, ds.getUrl());
                result.put(CommonParameters.DataSources.PORTAL_DB_DRIVER,  ds.getDriverClass());
                result.put(CommonParameters.DataSources.PORTAL_DB_USER,  ds.getUserName());
                result.put(CommonParameters.DataSources.PORTAL_DB_PASSWORD,  ds.getPassword());
                result.put(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MIN,  ds.getMinPoolSize());
                result.put(CommonParameters.DataSources.PORTAL_DB_POOLSIZE_MAX,  ds.getMaxPoolSize());
                result.put(CommonParameters.DataSources.PORTAL_DB_CHECK_SQL,  ds.getCheckedSql());
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
