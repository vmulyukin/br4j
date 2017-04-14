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
/**
 * @author etarakanov
 * Date: 14.09.2015
 * Time: 17:25
 */
@Log4j
class BR4J00040380 extends AbstractExecute
{
    private static final String[] filesToDelete = ['conf' + File.separator + 'dbmi' + File.separator + 'dmsi' + File.separator + 'config.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'flags.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'mail' + File.separator + 'mailerConfig_ru.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'mail' + File.separator + 'notificationConfig.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'mail' + File.separator + 'notificationConfig_ru.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'medo' + File.separator + 'options.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'pagesVisibility.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'solr' + File.separator + 'solrModule.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'config.example.properties',
                                                   'conf' + File.separator + 'dbmi' + File.separator + 'gost' + File.separator + 'gost_statistic_config.example.properties']

    public void install()
    {
        log.info "BR4J00040380 is running... "
        for (String fileToDelete : filesToDelete)
        {
            fileToDelete = getTransformedPath(fileToDelete);
            log.info "Deleting file: " + fileToDelete
            if (!FileUtils.deleteFile(fileToDelete))
            {
                log.info "Failed to delte file: " + fileToDelete;
            }
            log.info "File: " + fileToDelete + "delete successfully"
        }
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
        new BR4J00040380().start()
    }
}
