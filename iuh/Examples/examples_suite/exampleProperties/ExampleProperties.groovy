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
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

@Log4j
public class ExampleProperties extends AbstractExecute
{
    public static String IUH_ROOT_LOCATION = "br4j.dbmi.filestore.rootLocation";
    public static String IUH_NULL_URL_ROOT_LOCATION = "br4j.dbmi.filestore.nullUrlRootLocation";
    public static String IUH_CACHE_ROOT_LOCATION = "br4j.dbmi.filestore.cacheRootLocation";

    public static String IUH_ROOT_LOCATION_DEFVAL = "data/filestore/root";
    public static String IUH_NULL_URL_ROOT_LOCATION_DEFVAL = "data/filestore/null";
    public static String IUH_CACHE_ROOT_LOCATION_DEFVAL = "data/filestore/cache";

    public static String ROOT_LOCATION = "store.rootLocation";
    public static String NULL_URL_ROOT_LOCATION = "store.nullUrlRootLocation";
    public static String CACHE_ROOT_LOCATION = "store.cacheRootLocation";

    Map<String, String> properties;
	
	public void install() {
		log.info "Solr is running... "

        File fileProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'solr' + File.separator + 'solrModule.properties');

        File exampleProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'solr' + File.separator + 'solrModule.properties.example');

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            //копирует файл
            FileUtils.copyFile(exampleProperties, fileProperties);
            //проверяет корректность свойств в нем
            if (!checkPropertiesValues(fileProperties)) {
                //обновляет значения свойств
                updateRequiredProperty(fileProperties);
            }
            checkRootLocation(fileProperties);
            //сохранение добавление карты свойств в общую карту свойств
            map.putAll(properties);
        }
    }

    /**
     * проверка корректности путей root и cache для хранилища
     * @param fileProperties файл свойств
     * @return true, если пути существуют и являются папками
     */
    private boolean checkRootLocation(File fileProperties)
    {
        File rootLocation = new File(collectProperties().get(IUH_ROOT_LOCATION));
        File cacheRootLocation = new File(collectProperties().get(IUH_CACHE_ROOT_LOCATION));
        if(!rootLocation.isDirectory())
        {
            log.warn "Value of property: " + ROOT_LOCATION + " in file: " + fileProperties + " is INVALID";
            return false;
        }
        if(!cacheRootLocation.isDirectory())
        {
            log.warn "Value of property: " + CACHE_ROOT_LOCATION + " in file: " + fileProperties + " is INVALID";
            return false;
        }
        return true;
    }

    /**
     * инициализация карты свойств
     * @return карта свойств
     */
    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            //заполнение карты свойств
            properties.put(IUH_ROOT_LOCATION,
                    getPropertyValue(IUH_ROOT_LOCATION, map.get('br4j.jboss.configuration.path')
                            + File.separator + IUH_ROOT_LOCATION_DEFVAL));
            properties.put(IUH_NULL_URL_ROOT_LOCATION,
                    getPropertyValue(IUH_NULL_URL_ROOT_LOCATION, map.get('br4j.jboss.configuration.path')
                            + File.separator + IUH_NULL_URL_ROOT_LOCATION_DEFVAL));
            properties.put(IUH_CACHE_ROOT_LOCATION,
                    getPropertyValue(IUH_CACHE_ROOT_LOCATION, map.get('br4j.jboss.configuration.path')
                            + File.separator + IUH_CACHE_ROOT_LOCATION_DEFVAL));
        }
        return properties
    }

    /**
     * Проверка соответсвтвия значений свойств в файле, значениям в карте свойтсв
     * @param propertiesFile файл свойств
     * @return true, если значения свойств в файле идентичны значениям в карте свойств
     */
    private boolean checkPropertiesValues (File propertiesFile)
    {
        if (PropertiesUtils.checkPropertyEquals(propertiesFile, collectProperties()).isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * обновление свойств в файле
     * @param propertiesFile файл свойств
     */
    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        //читает список строк из файла свойств
        List<String> fileLines = FileUtils.readLines(propertiesFile);
        //обновляет значения свойств
        log.info("Updating property: " + ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, ROOT_LOCATION, collectProperties().get(IUH_ROOT_LOCATION),
                "Edited by Solr script at: " + new Date());
        log.info("Updating property: " + NULL_URL_ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, NULL_URL_ROOT_LOCATION, collectProperties().get(IUH_NULL_URL_ROOT_LOCATION),
                "Edited by Solr script at: " + new Date());
        log.info("Updating property: " + CACHE_ROOT_LOCATION);
        PropertiesUtils.updateProperty(fileLines, CACHE_ROOT_LOCATION, collectProperties().get(IUH_CACHE_ROOT_LOCATION),
                "Edited by Solr script at: " + new Date());
        //сохраняет список строк в файле свойств
        FileUtils.storeLines(propertiesFile, fileLines);
        log.info("Updating file: " + propertiesFile + " finished");
    }

    public static void main(String[] args) {
            new ExampleProperties().start();
    }

}