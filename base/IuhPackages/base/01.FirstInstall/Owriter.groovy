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
import ru.datateh.jbr.iuh.parameters.OtherParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

/**
 * @author etarakanov
 * Date: 27.03.2015
 * Time: 12:51
 */
@Log4j
public class Owriter extends AbstractExecute
{
    private static String LOCAL_SHARED_FOLDER = "owriter.local.shared.folder";
    private static String SERVICE_SHARED_FOLDER = "owriter.service.shared.folder";
    private static String SERVICE_BASE_URL = "owriter.service.base.url";

    private Map<String, String> properties;

    public void install() {
        log.info "Owriter is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'owriter' + File.separator + 'service.properties');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'owriter' + File.separator + 'service.properties.example');

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            FileUtils.copyFile(exampleProperties, fileProperties);
            if (!checkPropertiesValues(fileProperties)) {
                updateRequiredProperty(fileProperties);
            }
            FileUtils.changeFilePermission(fileProperties, FileUtils.Permission.WRITE, false);
        }
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(OtherParameters.Owriter.LOCAL_SHARED_FOLDER,
                    getPropertyValue(OtherParameters.Owriter.LOCAL_SHARED_FOLDER, null));
            properties.put(OtherParameters.Owriter.SERVICE_SHARED_FOLDER,
                    getPropertyValue(OtherParameters.Owriter.SERVICE_SHARED_FOLDER, null));
            properties.put(OtherParameters.Owriter.SERVICE_BASE_URL,
                    getPropertyValue(OtherParameters.Owriter.SERVICE_BASE_URL, null));
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
        log.info("Updating property: " + LOCAL_SHARED_FOLDER);
        PropertiesUtils.updateProperty(fileLines, LOCAL_SHARED_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Owriter.LOCAL_SHARED_FOLDER)),
                "Edited by Owriter script at: " + new Date());
        log.info("Updating property: " + SERVICE_SHARED_FOLDER);
        PropertiesUtils.updateProperty(fileLines, SERVICE_SHARED_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Owriter.SERVICE_SHARED_FOLDER)),
                "Edited by Owriter script at: " + new Date());
        log.info("Updating property: " + SERVICE_BASE_URL);
        PropertiesUtils.updateProperty(fileLines, SERVICE_BASE_URL, collectProperties().get(OtherParameters.Owriter.SERVICE_BASE_URL),
                "Edited by Owriter script at: " + new Date());
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
        new Owriter().start();
    }

}
