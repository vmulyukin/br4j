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
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

@Log4j
public class Solr extends AbstractExecute
{
    private static String ROOT_LOCATION = "store.rootLocation";
    private static String NULL_URL_ROOT_LOCATION = "store.nullUrlRootLocation";
    private static String CACHE_ROOT_LOCATION = "store.cacheRootLocation";

    private Map<String, String> properties;
	
	public void install() {
		log.info "Solr is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'solr' + File.separator + 'solrModule.properties');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'solr' + File.separator + 'solrModule.properties.example');

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            FileUtils.copyFile(exampleProperties, fileProperties);
            if (!checkPropertiesValues(fileProperties)) {
                updateRequiredProperty(fileProperties);
            }
            checkPathLocation(fileProperties);
            map.putAll(properties);
            FileUtils.changeFilePermission(fileProperties, FileUtils.Permission.WRITE, false);
        }
        else if(FileUtils.checkFileExist(fileProperties, false))
        {
            map.put(CommonParameters.Solr.ROOT_LOCATION, PropertiesUtils.readPropertyFromFile(fileProperties, ROOT_LOCATION));
            map.put(CommonParameters.Solr.NULL_URL_ROOT_LOCATION, PropertiesUtils.readPropertyFromFile(fileProperties, NULL_URL_ROOT_LOCATION));
            map.put(CommonParameters.Solr.CACHE_ROOT_LOCATION, PropertiesUtils.readPropertyFromFile(fileProperties, CACHE_ROOT_LOCATION));
        }
    }

    private static boolean checkPathLocation(File fileProperties)
    {
        if(!FileUtils.checkDirectoryExist(new File(PropertiesUtils.readPropertyFromFile(fileProperties, ROOT_LOCATION)), false))
        {
            log.warn "Value of property: " + ROOT_LOCATION + " in file: " + fileProperties + " is INVALID";
            return false;
        }
        if(!FileUtils.checkDirectoryExist(new File(PropertiesUtils.readPropertyFromFile(fileProperties, CACHE_ROOT_LOCATION)), false))
        {
            log.warn "Value of property: " + CACHE_ROOT_LOCATION + " in file: " + fileProperties + " is INVALID";
            return false;
        }
        return true;
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(CommonParameters.Solr.ROOT_LOCATION,
                    getPropertyValue(CommonParameters.Solr.ROOT_LOCATION, null));
            properties.put(CommonParameters.Solr.NULL_URL_ROOT_LOCATION,
                    getPropertyValue(CommonParameters.Solr.NULL_URL_ROOT_LOCATION, null));
            properties.put(CommonParameters.Solr.CACHE_ROOT_LOCATION,
                    getPropertyValue(CommonParameters.Solr.CACHE_ROOT_LOCATION, null));
        }
        return properties
    }

    private boolean checkPropertiesValues (File propertiesFile)
    {
        if (PropertiesUtils.checkPropertyEquals(propertiesFile, collectProperties()).isEmpty()) {
            return true;
        }
        return false;
    }

    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        List<String> fileLines = FileUtils.readLines(propertiesFile);
        log.info("Updating property: " + ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, ROOT_LOCATION, getTransformedPath(collectProperties().get(CommonParameters.Solr.ROOT_LOCATION)),
                "Edited by Solr script at: " + new Date());
        log.info("Updating property: " + NULL_URL_ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, NULL_URL_ROOT_LOCATION, getTransformedPath(collectProperties().get(CommonParameters.Solr.NULL_URL_ROOT_LOCATION)),
                "Edited by Solr script at: " + new Date());
        log.info("Updating property: " + CACHE_ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, CACHE_ROOT_LOCATION, getTransformedPath(collectProperties().get(CommonParameters.Solr.CACHE_ROOT_LOCATION)),
                "Edited by Solr script at: " + new Date());
        FileUtils.storeLines(propertiesFile, fileLines);
        log.info("Updating file: " + propertiesFile + " finished");
    }

    private static String getTransformedPath (String path)
    {
        File checkedPath = new File(path);
        if (checkedPath.isAbsolute()){
            return path;
        } else {
            return getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH) + File.separator + path;
        }
    }

    public static void main(String[] args) {
            new Solr().start();
    }

}