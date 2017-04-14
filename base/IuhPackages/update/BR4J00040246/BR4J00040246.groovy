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
import ru.datateh.jbr.iuh.utils.PropertiesUtils

@Log4j
public class BR4J00040246 extends AbstractExecute {

    public void install() {
        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'openoffice' + File.separator + 'pdfConvertor.properties');
        updateRequiredProperty(fileProperties);
    }

    /**
     * обновление свойств в файле
     * @param propertiesFile файл свойств
     */
    private static void updateRequiredProperty(File propertiesFile) {
        log.info("Updating file: " + propertiesFile);

        //находим значение параметра временной папки конвертирования
        String gsLogDir = getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH) + File.separator + "log/gsLogs";

        String newParamName = "convertor.log.dir";
        String newParamVal = PropertiesUtils.readPropertyFromFile(propertiesFile, newParamName);
        boolean added = false;
        if (newParamVal == null) {
            //добавляем параметр директории для логов
            log.info("Adding property: " + newParamName);
            PropertiesUtils.addPropertyAndSave(propertiesFile, newParamName, gsLogDir,
                    "Added by BR4J00040246 update script at: " + new Date());
            added = true;
        }
        if (!added) {
            log.info("\tNothing was added into " + propertiesFile);
        }
    }

    public static void main(String[] args) {
        new BR4J00040246().start();
    }

}