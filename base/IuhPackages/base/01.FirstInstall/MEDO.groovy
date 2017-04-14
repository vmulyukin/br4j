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
 * Time: 14:58
 */
@Log4j
public class MEDO extends AbstractExecute
{
    private static String IN_FOLDER = "InFolder";
    private static String IN_FOLDER_PROCESSED = "InFolderProcessed";
    private static String IN_FOLDER_DISCARDED = "InFolderDiscarded";
    private static String OUT_FOLDER_EXPORT = "outFolderExport";
    private static String TICKETS_PATH = "ticketsPath";
    private static String PROCESSED_TICKETS_PATH = "processedTicketsPath";

    private Map<String, String> properties;

    @Override
    public void install() {
        log.info "MEDO is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'medo' + File.separator + 'options.properties');
        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'medo' + File.separator + 'options.properties.example');

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

    public static void main(String[] args) {
        new MEDO().start()
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(OtherParameters.Medo.IN_FOLDER,
                    getPropertyValue(OtherParameters.Medo.IN_FOLDER, null));
            properties.put(OtherParameters.Medo.IN_FOLDER_PROCESSED,
                    getPropertyValue(OtherParameters.Medo.IN_FOLDER_PROCESSED, null));
            properties.put(OtherParameters.Medo.IN_FOLDER_DISCARDED,
                    getPropertyValue(OtherParameters.Medo.IN_FOLDER_DISCARDED, null));
            properties.put(OtherParameters.Medo.OUT_FOLDER_EXPORT,
                    getPropertyValue(OtherParameters.Medo.OUT_FOLDER_EXPORT, null));
            properties.put(OtherParameters.Medo.TICKETS_PATH,
                    getPropertyValue(OtherParameters.Medo.TICKETS_PATH, null));
            properties.put(OtherParameters.Medo.PROCESSED_TICKETS_PATH,
                    getPropertyValue(OtherParameters.Medo.PROCESSED_TICKETS_PATH, null));
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
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Medo.IN_FOLDER)),
                "Edited by MEDO script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_PROCESSED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_PROCESSED, getTransformedPath(collectProperties().get(OtherParameters.Medo.IN_FOLDER_PROCESSED)),
                "Edited by MEDO script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_DISCARDED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_DISCARDED, getTransformedPath(collectProperties().get(OtherParameters.Medo.IN_FOLDER_DISCARDED)),
                "Edited by MEDO script at: " + new Date());
        log.info("Updating property: " + OUT_FOLDER_EXPORT);
        PropertiesUtils.updateProperty(fileLines, OUT_FOLDER_EXPORT, getTransformedPath(collectProperties().get(OtherParameters.Medo.OUT_FOLDER_EXPORT)),
                "Edited by MEDO script at: " + new Date());
        log.info("Updating property: " + TICKETS_PATH);
        PropertiesUtils.updateProperty(fileLines, TICKETS_PATH, getTransformedPath(collectProperties().get(OtherParameters.Medo.TICKETS_PATH)),
                "Edited by MEDO script at: " + new Date());
        log.info("Updating property: " + PROCESSED_TICKETS_PATH);
        PropertiesUtils.updateProperty(fileLines, PROCESSED_TICKETS_PATH, getTransformedPath(collectProperties().get(OtherParameters.Medo.PROCESSED_TICKETS_PATH)),
                "Edited by MEDO script at: " + new Date());
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
}
