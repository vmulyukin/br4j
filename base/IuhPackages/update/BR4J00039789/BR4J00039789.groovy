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
public class BR4J00039789 extends AbstractExecute
{
    Map<String, String> properties;
	
	public void install() {
        File fileProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'statistics' + File.separator + 'docs_statistic_config.properties');
		updateRequiredProperty(fileProperties);
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
        log.info("Updating property: startInterval");
        PropertiesUtils.updateProperty(fileLines, "startInterval", "YEAR",
                "Edited by DocsStatistic script at: " + new Date());
				
        log.info("Updating property: endInterval");
        PropertiesUtils.updateProperty(fileLines, "endInterval", "0",
                "Edited by DocsStatistic script at: " + new Date());
        log.info("Updating file: " + propertiesFile + " finished");
		FileUtils.storeLines(propertiesFile, fileLines);
    }

    public static void main(String[] args) {
            new BR4J00039789().start();
    }

}