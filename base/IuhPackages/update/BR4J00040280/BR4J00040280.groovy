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
 * Date: 16.09.2015
 * Time: 16:57
 */
@Log4j
class BR4J00040280  extends AbstractExecute
{
    private static final String NOT_CONTROLL = "result_for_sign_notcontrol";
    private static final String NOT_CONTROLL_ASSIST= "result_for_sign_notcontrol_assist";

    public void install()
    {
        log.info "BR4J00040280 is running... "
        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'pagesVisibility.properties.example');
        File exampleProperties1 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'pagesVisibility.example.properties');

        if (FileUtils.checkFileExist(exampleProperties, false))
        {
            addProperty(exampleProperties);
        } else if (FileUtils.checkFileExist(exampleProperties1, false)) {
            FileUtils.copyFile(exampleProperties1, exampleProperties);
            addProperty(exampleProperties);
        }
        FileUtils.deleteFile(exampleProperties1.toString());
        log.info "BR4J00040280 is finished. "
    }

    private static void addProperty(File fileProperties)
    {
        if (!checkPropertiesValues(fileProperties)) {
            updateRequiredProperty(fileProperties);
        }
    }

    private static Map<String, String> collectProperties ()
    {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(NOT_CONTROLL, "false");
        properties.put(NOT_CONTROLL_ASSIST, "false");
        return properties
    }

    private static boolean checkPropertiesValues (File propertiesFile)
    {
        if (PropertiesUtils.checkPropertyEquals(propertiesFile, collectProperties()).isEmpty()) {
            return true;
        }
        return false;
    }

    private static void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        List<String> fileLines = FileUtils.readLines(propertiesFile);
        log.info("Updating property: " + NOT_CONTROLL);
        PropertiesUtils.updateProperty(fileLines, NOT_CONTROLL, "false", "Edited by DMSI script at: " + new Date());
        log.info("Updating property: " + NOT_CONTROLL_ASSIST);
        PropertiesUtils.updateProperty(fileLines, NOT_CONTROLL_ASSIST, "false", "Edited by DMSI script at: " + new Date());
        FileUtils.storeLines(propertiesFile, fileLines);
        log.info("Updating file: " + propertiesFile + " finished");
    }

    public static void main(String[] args) {
        new BR4J00040280().start()
    }
}
