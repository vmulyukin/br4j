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
 * Date: 10.02.15
 * Time: 16:23
 */
@Log4j
public class GOST extends AbstractExecute
{
    private static String IN_FOLDER = "inFolder";
    private static String IN_FOLDER_PROCESSED = "inFolderProcessed";
    private static String IN_FOLDER_DISCARDED = "inFolderDiscarded";
    private static String OUT_FOLDER = "outFolder";

    private Map<String, String> properties;

    @Override
    void install() {
        log.info "GOST is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'config.properties');
        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'config.properties.example');

        File fileProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'gost_statistic_config.properties');
        File exampleProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'gost_statistic_config.properties.example');

        log.info "File: " + fileProperties2 + " will be created from copy: " + exampleProperties2;
        FileUtils.copyFile(exampleProperties2, fileProperties2);

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
            properties.put( OtherParameters.Gost.IN_FOLDER,
                    getPropertyValue( OtherParameters.Gost.IN_FOLDER, null));
            properties.put(OtherParameters.Gost.IN_FOLDER_PROCESSED,
                    getPropertyValue(OtherParameters.Gost.IN_FOLDER_PROCESSED, null));
            properties.put(OtherParameters.Gost.IN_FOLDER_DISCARDED,
                    getPropertyValue(OtherParameters.Gost.IN_FOLDER_DISCARDED, null));
            properties.put(OtherParameters.Gost.OUT_FOLDER,
                    getPropertyValue(OtherParameters.Gost.OUT_FOLDER, null));
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
        log.info("Updating property: " + IN_FOLDER);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER, getTransformedPath(collectProperties().get( OtherParameters.Gost.IN_FOLDER)),
                "Edited by GOST script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_PROCESSED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_PROCESSED, getTransformedPath(collectProperties().get(OtherParameters.Gost.IN_FOLDER_PROCESSED)),
                "Edited by GOST script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_DISCARDED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_DISCARDED, getTransformedPath(collectProperties().get(OtherParameters.Gost.IN_FOLDER_DISCARDED)),
                "Edited by GOST script at: " + new Date());
        log.info("Updating property: " + OUT_FOLDER);
        PropertiesUtils.updateProperty(fileLines, OUT_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Gost.OUT_FOLDER)),
                "Edited by GOST script at: " + new Date());
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
        new GOST().start()
    }
}
