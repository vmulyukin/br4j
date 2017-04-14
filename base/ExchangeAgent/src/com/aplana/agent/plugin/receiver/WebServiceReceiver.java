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
package com.aplana.agent.plugin.receiver;

import com.aplana.agent.plugin.AbstractPlugin;
import com.aplana.agent.plugin.Plugin;
import com.aplana.agent.plugin.ReceivedMessage;
import com.aplana.ws.replication.Folder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static com.aplana.agent.util.FileUtility.*;

/**
 * This plugin is not "usual" receiver.
 * It will be called NOT from ReceiverExecutor but directly from WebServices
 * The main goal of this class - store received files in directory defined in bean.xml
 */
public class WebServiceReceiver //extends AbstractPlugin implements Plugin 
{

    public final static String WS_STORE_FOLDER = "storeFolder";

    protected final Logger logger =  Logger.getLogger(getClass());

/*    public void saveFolder(Folder folder) throws ReceiveException {
        if (folder == null) throw new IllegalArgumentException("folder is null");
        File wsStoreFolder = getWsStoreFolder();
        String messageFolderName = folder.getName();
        if (messageFolderName == null){
            logger.error("Folder name is null");
            throw new IllegalArgumentException("Folder name is null");
        }
        File messageFolder = checkDirOrMakeIt(wsStoreFolder + SEP + messageFolderName);
        lockFolder(messageFolder, "WebServiceReceiver");

        try {
            List<Folder.File> files = folder.getFiles();
            if (files != null) {
                for (Folder.File file : files) {
                    String fileName = file.getName();
                    if (fileName!=null){
                        ReceivedMessage.FileUnit fu = new ReceivedMessage.FileUnit(fileName, file.getContent());
                        writeFile(messageFolder, fu);
                    } else {
                        logger.error("File name is null in received message!");
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ReceiveException(e);
        }

        unlockFolder(messageFolder);
    }
*/
/*    private File getWsStoreFolder() {
        Properties parameters = getParameters();
        String wsStoreFolder = parameters.getProperty(WS_STORE_FOLDER);
        if (wsStoreFolder == null){
            throw new RuntimeException("WebServiceReceiver is not properly configured! Required parameter " + WS_STORE_FOLDER + " is null!" );
        }

        return checkDirOrMakeIt(wsStoreFolder);
    }
*/    
/*    public void postProcess(Address fromAddress) throws ReceiveException {

    }
*/
}
