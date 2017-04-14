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
package org.aplana.br4j.dynamicaccess.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.EditConfigMainForm;
import org.aplana.br4j.dynamicaccess.xmldef.config.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class ConfigurationFacade {

    public final static String PARAMETER_xmlFileHome = "xmlFileHome";
    public final static String PARAMETER_jdbcSettings = "jdbcSettings";
    public final static String PARAMETER_profileAttributes = "profileAttributes";
    public final static String PARAMETER_profileCardAttributes = "profileCardAttributes";
    public final static String PARAMETER_personCardLinkAttributes = "personCardLinkAttributes";
    
    private static final String CONFIG_PATH = "/org/aplana/br4j/dynamicaccess/config/";
    
    private static final String CONFIG_PARAMS_FILE = CONFIG_PATH + "AccessRulesConfigParameters.xml";
    
    private static final String CONFIG_PROPERTIES = CONFIG_PATH + "config.properties";
    private static final String PARAM_APP_VERSION = "app.version";
    		
    private static final String JDBC_CONFIG_PATH = "./conf/jdbc-config.xml";
    //private static final String JDBC_CONFIG_PATH = "D:/BUILDS/FSIN/tool/DynamicAccessRule/conf/jdbc-config.xml";
    
    public static final String TAG_DATABASES = "databases";
    public static final String TAG_MAIN_DATABASE = "main";
    public static final String TAG_SECONDARY_DATABASES = "secondary";
    public static final String TAG_SECONDARY_DB = "database";
    public static final String TAG_PARAMETER = "parameter";
    public static final String ATTR_TITLE = "title";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_VALUE = "value";
    public static final String ATTR_ENABLE = "enable";
    
    
    private final static Log logger = LogFactory.getLog(ConfigurationFacade.class);
    
    
    private String appVersion;

    private static ConfigurationFacade ourInstance = new ConfigurationFacade();

    public static ConfigurationFacade getInstance() {
        return ourInstance;
    }

    private AccessRulesConfig config;

    private ConfigurationFacade() {
    	try {
            InputStream inputStream = this.getClass().getResourceAsStream(CONFIG_PARAMS_FILE);
            if (inputStream == null){
                System.err.println("Can't find AccessRulesConfigParameters.xml in JAR");
                throw new RuntimeException("Can't find AccessRulesConfigParameters.xml in JAR");
            }
            Reader reader = new InputStreamReader(inputStream);
            //config = AccessRulesConfig.unmarshal(reader);
            config = (AccessRulesConfig) AccessRulesConfig.unmarshal(reader);
        } catch (MarshalException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read configuration", e);
        } catch (ValidationException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read configuration", e);
        }
    }

    public JdbcSettings getJdbcSettings() throws IOException{
        JdbcSettings ret = new JdbcSettings();
        
		Document document = null;
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(JDBC_CONFIG_PATH);
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fin);
			if(document != null){
				Element root = document.getDocumentElement();
				NodeList mainDatabaseConf = root.getElementsByTagName(TAG_MAIN_DATABASE);
				for(int i = 0; i < mainDatabaseConf.getLength(); i++){
					ret.mainJdbcSetting = readJdbcSetting((Element) mainDatabaseConf.item(i));
				}
				NodeList secondaryDatabases = root.getElementsByTagName(TAG_SECONDARY_DB);
				for(int i = 0; i < secondaryDatabases.getLength(); i++){
					Element db = (Element) secondaryDatabases.item(i);
					String title = db.getAttribute(ATTR_TITLE);
					ret.addSecondaryJdbcSetting(title, readJdbcSetting(db));
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error initializing jdbc configuration", e);
		} finally {
			if(fin!=null)
				fin.close();
		}
		return ret;
    }
    
    private Map<String,Object> readJdbcSetting(Element el){
    	Map<String,Object> map = new HashMap<String, Object>();
    	NodeList nodeList = el.getElementsByTagName(TAG_PARAMETER);
    	for(int i = 0; i < nodeList.getLength(); i++){
    		Element parameter = (Element) nodeList.item(i);
    		map.put(parameter.getAttribute(ATTR_NAME), parameter.getAttribute(ATTR_VALUE));
    	}
    	map.put(ATTR_ENABLE, Boolean.TRUE);
    	return map;
    }
    
    public void loadAppVersion() throws IOException {
    	final Properties props = new Properties();
		InputStream in = null;
		try {
			in = this.getClass().getResourceAsStream(CONFIG_PROPERTIES);
			props.load(in);
			this.appVersion = props.getProperty(PARAM_APP_VERSION);
		} finally {
			if(in!=null)
				in.close();
		}
    }

    public String getXmlFileHome() {
        Parameter parameterByName = getParameterByName(PARAMETER_xmlFileHome);
        return parameterByName.getSimpleParameter().getValue().toString();
    }

    public List<String> getProfileAttributes(){
        return getAttributeList(PARAMETER_profileAttributes);
    }

    public List<String> getProfileCardAttributes(){
        return getAttributeList(PARAMETER_profileCardAttributes);
    }

    public List<String> getPersonCardLinkAttributes(){
        return getAttributeList(PARAMETER_personCardLinkAttributes);
    }

    private List<String> getAttributeList(String name) {
        List<String> ret = new ArrayList<String>();
        AttributeList attributeList = getParameterByName(name).getAttributeList();
        for (AttributeListItem alItem : attributeList.getAttributeListItem()){
            ret.add(alItem.getAttribute().getCode().toString());
        }
        return ret;
    }


    private Parameter getParameterByName(String name){
        AccessRulesConfigItem[] items = config.getAccessRulesConfigItem();
        for (AccessRulesConfigItem item : items) {
            if (name.equals(item.getParameter().getName())){
                return item.getParameter();
            }
        }
        throw new IllegalArgumentException("Unknown parameter " + name);
    }


    public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
    
    public String getAppVersion() {
    	return appVersion;
    }


	public class JdbcSettings{
		public Map<String,Object> mainJdbcSetting = new HashMap<String,Object>();
        
		public Map<String,Map<String,Object>> secondaryJdbcSettings = new HashMap<String,Map<String,Object>>();
        
        public void addSecondaryJdbcSetting(String title, Map<String,Object> jdbcSetting){
        	this.secondaryJdbcSettings.put(title, jdbcSetting);
        }
        
        public void removeSecondaryJdbcSetting(String title){
        	this.secondaryJdbcSettings.remove(title);
        }
        
        public void cleanSecondaryJdbcSettings(){
        	this.secondaryJdbcSettings.clear();
        }

        @Override
        public String toString() {
            return "JdbcSettings stub";
        }

		public JdbcSettings() {
			mainJdbcSetting.put(EditConfigMainForm.KEY_URL, "");
			mainJdbcSetting.put(EditConfigMainForm.KEY_USER, "");
			mainJdbcSetting.put(EditConfigMainForm.KEY_PASSWORD, "");
		}
    }

}
