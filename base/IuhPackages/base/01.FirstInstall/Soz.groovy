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
 * Time: 13:07
 */
@Log4j
public class Soz extends AbstractExecute
{
    private static String EXPORT_DIRECTORY = "soz.export.directory";
    private static String EXPORT_DIRECTORY_OK = "soz.export.directory.result.ok";
    private static String EXPORT_DIRECTORY_FAIL = "soz.export.directory.result.fail";
    private static String SERVER_URL = "soz.server.url";
    private static String IN_DERECTORY = "soz.in.directory";

    private Map<String, String> properties;

    @Override
    void install() {
        log.info "Soz is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'soz' + File.separator + 'config.properties');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'soz' + File.separator + 'config.properties.example');

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
            properties.put(OtherParameters.Soz.EXPORT_DIRECTORY, getPropertyValue(OtherParameters.Soz.EXPORT_DIRECTORY,  null));
            properties.put(OtherParameters.Soz.EXPORT_DIRECTORY_OK, getPropertyValue(OtherParameters.Soz.EXPORT_DIRECTORY_OK,  null));
            properties.put(OtherParameters.Soz.EXPORT_DIRECTORY_FAIL, getPropertyValue(OtherParameters.Soz.EXPORT_DIRECTORY_FAIL,  null));
            properties.put( OtherParameters.Soz.SERVER_URL, getPropertyValue( OtherParameters.Soz.SERVER_URL, null));
            properties.put(OtherParameters.Soz.IN_DERECTORY, getPropertyValue(OtherParameters.Soz.IN_DERECTORY,  null));
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
        log.info("Updating property: " + EXPORT_DIRECTORY);
        PropertiesUtils.updateProperty(fileLines, EXPORT_DIRECTORY, getTransformedPath(collectProperties().get(OtherParameters.Soz.EXPORT_DIRECTORY)),
                "Edited by Soz script at: " + new Date());
        log.info("Updating property: " + EXPORT_DIRECTORY_OK);
        PropertiesUtils.updateProperty(fileLines, EXPORT_DIRECTORY_OK, getTransformedPath(collectProperties().get(OtherParameters.Soz.EXPORT_DIRECTORY_OK)),
                "Edited by Soz script at: " + new Date());
        log.info("Updating property: " + EXPORT_DIRECTORY_FAIL);
        PropertiesUtils.updateProperty(fileLines, EXPORT_DIRECTORY_FAIL, getTransformedPath(collectProperties().get(OtherParameters.Soz.EXPORT_DIRECTORY_FAIL)),
                "Edited by Soz script at: " + new Date());
        log.info("Updating property: " + SERVER_URL);
        PropertiesUtils.updateProperty(fileLines, SERVER_URL, collectProperties().get(OtherParameters.Soz.SERVER_URL),
                "Edited by Soz script at: " + new Date());
        log.info("Updating property: " + IN_DERECTORY);
        PropertiesUtils.updateProperty(fileLines, IN_DERECTORY, getTransformedPath(collectProperties().get(OtherParameters.Soz.IN_DERECTORY)),
                "Edited by Soz script at: " + new Date());
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
        new Soz().start()
    }
}

