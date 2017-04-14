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
 * Date: 27.03.2015
 * Time: 17:25
 */
@Log4j
public class CardSignature extends AbstractExecute
{
    public void install()
    {
        log.info "CardSignature is running... "
        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'certChecking.properties.example');
        File exampleProperties1 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'crypto.properties.example');
        File exampleProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'cryptoLayer.properties.example');
        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'certChecking.properties');
        File fileProperties1 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'crypto.properties');
        File fileProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator + 'signature' + File.separator + 'cryptoLayer.properties');
        log.info "File: " + fileProperties + " will be created from copy: " + exampleProperties;
        FileUtils.copyFile(exampleProperties, fileProperties);
        FileUtils.changeFilePermission(fileProperties, FileUtils.Permission.WRITE, false);
        log.info "File: " + fileProperties1 + " will be created from copy: " + exampleProperties1;
        FileUtils.copyFile(exampleProperties1, fileProperties1);
        FileUtils.changeFilePermission(fileProperties1, FileUtils.Permission.WRITE, false);
        log.info "File: " + fileProperties2 + " will be created from copy: " + exampleProperties2;
        FileUtils.copyFile(exampleProperties2, fileProperties2);
        FileUtils.changeFilePermission(fileProperties2, FileUtils.Permission.WRITE, false);
    }

    public static void main(String[] args) {
        new CardSignature().start()
    }
}