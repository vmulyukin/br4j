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
 * Time: 17:31
 */
@Log4j
public class DMSI extends AbstractExecute
{
    private static String STANDART = "standart";
    private static String VERSION = "version";
    private static String SYS_ID = "sys_id";
    private static String SYSTEM = "system";
    private static String SYSTEM_DETAILS = "system_details";
    private static String DEFAULT_ORGANIZATION_ID = "default_organization_id";
    private static String IN_FOLDER = "inFolder";
    private static String IN_FOLDER_PROCESSED = "inFolderProcessed";
    private static String IN_FOLDER_DISCARDED = "inFolderDiscarded";

    private Map<String, String> properties;

    @Override
    void install() {
        log.info "DMSI is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'dmsi' + File.separator + 'config.properties');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'dmsi' + File.separator + 'config.properties.example');

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
            properties.put(OtherParameters.Dmsi.STANDART, getPropertyValue(OtherParameters.Dmsi.STANDART, null));
            properties.put(OtherParameters.Dmsi.VERSION, getPropertyValue(OtherParameters.Dmsi.VERSION, null));
            properties.put(OtherParameters.Dmsi.SYS_ID, getPropertyValue(OtherParameters.Dmsi.SYS_ID, null));
            properties.put(OtherParameters.Dmsi.SYSTEM, getPropertyValue(OtherParameters.Dmsi.SYSTEM, null));
            properties.put(OtherParameters.Dmsi.SYSTEM_DETAILS, getPropertyValue(OtherParameters.Dmsi.SYSTEM_DETAILS, null));
            properties.put(OtherParameters.Dmsi.DEFAULT_ORGANIZATION_ID, getPropertyValue(OtherParameters.Dmsi.DEFAULT_ORGANIZATION_ID, null));
            properties.put(OtherParameters.Dmsi.IN_FOLDER, getPropertyValue(OtherParameters.Dmsi.IN_FOLDER, null));
            properties.put(OtherParameters.Dmsi.IN_FOLDER_PROCESSED, getPropertyValue(OtherParameters.Dmsi.IN_FOLDER_PROCESSED, null));
            properties.put(OtherParameters.Dmsi.IN_FOLDER_DISCARDED, getPropertyValue(OtherParameters.Dmsi.IN_FOLDER_DISCARDED, null));
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
        log.info("Updating property: " + STANDART);
        PropertiesUtils.updateProperty(fileLines, STANDART, collectProperties().get(OtherParameters.Dmsi.STANDART),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + VERSION);
        PropertiesUtils.updateProperty(fileLines, VERSION, collectProperties().get(OtherParameters.Dmsi.VERSION),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + SYS_ID);
        PropertiesUtils.updateProperty(fileLines, SYS_ID, collectProperties().get(OtherParameters.Dmsi.SYS_ID),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + SYSTEM);
        PropertiesUtils.updateProperty(fileLines, SYSTEM, collectProperties().get(OtherParameters.Dmsi.SYSTEM),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + SYSTEM_DETAILS);
        PropertiesUtils.updateProperty(fileLines, SYSTEM_DETAILS, collectProperties().get(OtherParameters.Dmsi.SYSTEM_DETAILS),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + DEFAULT_ORGANIZATION_ID);
        PropertiesUtils.updateProperty(fileLines, DEFAULT_ORGANIZATION_ID, collectProperties().get(OtherParameters.Dmsi.DEFAULT_ORGANIZATION_ID),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Dmsi.IN_FOLDER)),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_PROCESSED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_PROCESSED, getTransformedPath(collectProperties().get(OtherParameters.Dmsi.IN_FOLDER_PROCESSED)),
                "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + IN_FOLDER_DISCARDED);
        PropertiesUtils.updateProperty(fileLines, IN_FOLDER_DISCARDED, getTransformedPath(collectProperties().get(OtherParameters.Dmsi.IN_FOLDER_DISCARDED)),
                "Edited by DMSI script at: " + new Date());
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
        new DMSI().start()
    }
}
