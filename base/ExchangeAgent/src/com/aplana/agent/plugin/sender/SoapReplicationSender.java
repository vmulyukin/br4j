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

import com.aplana.agent.conf.DocumentBodyReader;
import com.aplana.agent.plugin.AbstractPlugin;
import com.aplana.agent.util.FileUtility;
import com.aplana.ws.replication.Folder;
import com.aplana.ws.replication.Replication;
import com.aplana.ws.replication.Status;
import com.aplana.ws.replication.client.ReplicationProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Send replication packages to WS
 */
public class SoapReplicationSender //extends AbstractPlugin implements Sender 
{

    protected final Log logger = LogFactory.getLog(getClass());

/*    public SendStatus sendMessage(MessageToSend message) throws SendException {
        Address address = message.getAddress();
        Folder folder = prepareFolder(message);

        String task = address.getTask();
        logger.info(task + " started");

        Status status = null;
        try {
            ReplicationProxy proxy = new ReplicationProxy(getWsdlUrl(address.getProp()),
                    new QName(Replication.NAME_SPACE, getServiceName()));

            Replication replication = proxy.getReplicationEndPoint();
            status = replication.sendFolder(folder);
            
        } catch (WebServiceException wse) {
            throwSendRetryException(wse);
        } catch (IllegalArgumentException e) {
            throwSendRetryException(e);
        }
        
        if (status == Status.SUCCESS) {
            logger.info(task + " successfully finished");
            return SendStatus.SUCCESS;
        } else {
            logger.error(task + " finished with errors");
            logger.error("Error details : " + status.getDetails() + "Cause: " + status.getCause());
            return SendStatus.FAILURE;
        }
    }
*/

/*    private void throwSendRetryException(Exception e) throws SendException {
        throw new SendException("Error calling web service: " + e.getMessage(), true);
    }


    private Folder prepareFolder(MessageToSend message) throws SendException {
        Folder folder = new Folder();
        File replicationPackage = getReplicationPackageDocument(message);
        
        if (replicationPackage == null) throw new SendException("Document is null");
        folder.setName(replicationPackage.getParentFile().getName());
        List<Folder.File> files = new ArrayList<Folder.File>();
        try {
            Folder.File document = FileUtility.readFile(replicationPackage);
            files.add(document);
            List<File> attachments = getAttachmentsExceptReplicationPackage(message);
            for (File attach : attachments) {
                Folder.File attachment = FileUtility.readFile(attach);
                files.add(attachment);
            }
            folder.setFiles(files);
            return folder;

        } catch (IOException e) {
            logger.error(e);
            throw new SendException(e);
        }
    }

    private File getReplicationPackageDocument(MessageToSend message) throws SendException {        
        if(DocumentBodyReader.REPLICATION_PACKAGE_FILENAME.equals(message.getDocument().getName())){
            return message.getDocument();
        }
        for(File attachement : message.getAttachments()){
            if(DocumentBodyReader.REPLICATION_PACKAGE_FILENAME.equals(attachement.getName())){
                return attachement;
            }
        }
        throw new SendException("No replication package is found in message: " + message);
    }

    private  List<File> getAttachmentsExceptReplicationPackage(MessageToSend message) {
        List<File> attachments = new ArrayList<File>();
        for(File attachment : message.getAttachments()) {
            if(!DocumentBodyReader.REPLICATION_PACKAGE_FILENAME.equals(attachment.getName())){
                attachments.add(attachment);
            }
        }
        return attachments;
    }
*/    
/*    private URL getWsdlUrl(List<Prop> prop) {
        String wsdlUrl = getProperty("wsdlUrl", prop);
        if (wsdlUrl == null){
            throw new IllegalStateException("Specify WSDL URL in router table for this address");
        }
        try {
            return new URL (wsdlUrl);
        } catch (MalformedURLException e) {
            logger.error(e);
            throw new IllegalStateException("Malformed WSDL URL in router table for this address");
        }
    }

    private final static String SERVICE_NAME = "serviceName";

    private String getServiceName() {
        Object serviceName = getParameters().get(SERVICE_NAME);
        if (serviceName == null){
            throw new IllegalStateException("Specify web service name in bean.xml");
        }
        return (String) serviceName;
    }
*/
}
