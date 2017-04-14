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
package com.aplana.agent.plugin.sender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.aplana.agent.plugin.BaseTest;
/**
 * Unit test for SMTPSender plugin.
 * @author atsvetkov
 *
 */
public class SMTPSenderTest extends BaseTest {
    
    private static final String EMAIL_ADDRESS_PROPERTY = "EMAIL_ADDRESS";
    private static final String EMAIL_PROPERTY = "e-mail";
    static final String  CONF_DIRECTORY = "/test/conf/";
    static final String  DOCUMENT_FILE = "/document.xml";
    static final String  ATACHEMENT_FILE = "/attachement.xml";

    @Test
    public void testSendMessage() throws SendException {        
/*        List<File> supAttachements = new ArrayList<File>();
        String currentDir = System.getProperty("user.dir");
        supAttachements.add(new File(currentDir + CONF_DIRECTORY + ATACHEMENT_FILE));        
        
        com.aplana.agent.conf.model.Address address = new com.aplana.agent.conf.model.Address(); 
        Prop emailProp = new Prop();
        Properties testConfiguration = getTestConfiguration();
        emailProp.setKey(EMAIL_PROPERTY);
        emailProp.setContent(testConfiguration.getProperty(EMAIL_ADDRESS_PROPERTY));        
        address.getProp().add(emailProp);
        MessageToSend transportMessage = new MessageToSend(address, null, new File(currentDir + CONF_DIRECTORY + ATACHEMENT_FILE), supAttachements);
        SMTPSender sender = new SMTPSender(){
            @Override
            protected Properties getConfiguration() {
                return getTestConfiguration();
            }
        };
        sender.sendMessage(transportMessage);
*/                
    }    
}
