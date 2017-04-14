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
/**
 * @author etarakanov
 * Date: 06.02.15
 * Time: 15:47
 */

@Log4j
public class OpenOffice extends AbstractExecute
{
    private static String CONVERTER_TEMP = "convertor.temp.dir";
    private static String CONVERTER_LOG_DIR = "convertor.log.dir";
    private static String OPEN_OFFICE_TEMP = "openoffice.temp.dir";
    private static String CONVERTER_CACHE_PARAMETER = "convertor.cache.storage";

    private Map<String, String> properties;

    public void install() {
        log.info "OpenOffice is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'openoffice' + File.separator + 'pdfConvertor.properties');
        File exampleProperties = new File(getParam('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'openoffice' + File.separator + 'pdfConvertor.properties.example');

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
            setParam(CommonParameters.OpenOffice.CONVERTER_TEMP, PropertiesUtils.readPropertyFromFile(fileProperties, CONVERTER_TEMP));
            setParam(CommonParameters.OpenOffice.CONVERTER_LOG_DIR, PropertiesUtils.readPropertyFromFile(fileProperties, CONVERTER_LOG_DIR));
            setParam(CommonParameters.OpenOffice.OPEN_OFFICE_TEMP, PropertiesUtils.readPropertyFromFile(fileProperties, OPEN_OFFICE_TEMP));
        }
    }

    private static boolean checkPathLocation(File fileProperties)
    {
        if(!FileUtils.checkDirectoryExist(new File(PropertiesUtils.readPropertyFromFile(fileProperties, CONVERTER_CACHE_PARAMETER)), false))
        {
            log.warn "Value of property: " + CONVERTER_CACHE_PARAMETER + " in file: " + fileProperties + " is INVALID";
            return false;
        }
        return true;
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(CommonParameters.OpenOffice.CONVERTER_TEMP, getPropertyValue(CommonParameters.OpenOffice.CONVERTER_TEMP, null));
            properties.put(CommonParameters.OpenOffice.CONVERTER_LOG_DIR, getPropertyValue(CommonParameters.OpenOffice.CONVERTER_LOG_DIR, null));
            properties.put(CommonParameters.OpenOffice.OPEN_OFFICE_TEMP, getPropertyValue(CommonParameters.OpenOffice.OPEN_OFFICE_TEMP, null));
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
        log.info("Updating property: " + CONVERTER_TEMP);
        PropertiesUtils.updateProperty(fileLines, CONVERTER_TEMP, getTransformedPath(collectProperties().get(CommonParameters.OpenOffice.CONVERTER_TEMP)),
                "Edited by OpenOffice script at: " + new Date());
        log.info("Updating property: " + CONVERTER_LOG_DIR);
        PropertiesUtils.updateProperty(fileLines, CONVERTER_LOG_DIR, getTransformedPath(collectProperties().get(CommonParameters.OpenOffice.CONVERTER_LOG_DIR)),
                "Edited by OpenOffice script at: " + new Date());
        log.info("Updating property: " + OPEN_OFFICE_TEMP);
        PropertiesUtils.updateProperty(fileLines, OPEN_OFFICE_TEMP, getTransformedPath(collectProperties().get(CommonParameters.OpenOffice.OPEN_OFFICE_TEMP)),
                "Edited by OpenOffice script at: " + new Date());
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
        new OpenOffice().start()
    }

}
