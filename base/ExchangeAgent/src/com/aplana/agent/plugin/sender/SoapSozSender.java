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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.agent.TransportAgentConfiguration;
import com.aplana.ws.soz.model.attachement.AttachedFileType;
import com.aplana.ws.soz.model.attachement.PutDMSMessageRequest;
import com.aplana.ws.soz.model.attachement.PutDMSMessageResponse;
import com.aplana.ws.soz.model.communication.AssociatedFile;
import com.aplana.ws.soz.model.communication.Communication;
import com.aplana.ws.soz.model.communication.FileType;
import com.aplana.agent.conf.DocumentBodyReader;
import com.aplana.agent.plugin.AbstractPlugin;
import com.aplana.ws.soz.client.DMSExchangeService;
import com.aplana.ws.soz.service.DMSExchangePortType;

/**
 * 
 * @author atsvetkov
 *
 */
public class SoapSozSender //extends AbstractPlugin implements Sender 
{

    private static final String WEB_SERVICE_WSDL = "webService";
    private static final String COMMUNICATION_PACKAGE = "com.aplana.ws.soz.model.communication";
    public final static String NAMESPACE_URI = "urn://x-artefacts-it-ru/dob/poltava/dmsx/1.0";
    public final static String SUCCESS_RESULT_CODE = "SUCCESS";
    
    public final static String SOZ_WEB_SERVICE_WSDL = "soz.web.servive.wsdl";
    public final static String SOZ_WEB_SERVICE_DMS_EXCHANGE_PORT = "soz.server.dms_exchange.port";
    public final static String SOZ_WEB_SERVICE_DMS_EXCHANGE_SERVICE = "soz.server.dms_exchange.service";

    protected final Log logger = LogFactory.getLog(getClass());
    
    private static Unmarshaller unmarshaller;

/*    public SendStatus sendMessage(MessageToSend message) throws SendException {
        Properties configuration = getConfiguration();
        
        String url = getWebServiceWsdl(message);
        URL serverURL = null;
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException e) {
            logger.error(e);
            throw new SendException("\"" + url + "\" is not a valid URL!\n" + "Please configure property " + url + " in soz configuration file.");
        }

        String serviceName = configuration.getProperty(SOZ_WEB_SERVICE_DMS_EXCHANGE_SERVICE);
        String portName = configuration.getProperty(SOZ_WEB_SERVICE_DMS_EXCHANGE_PORT);
        PutDMSMessageResponse putDMSMessageResponse = null;
       
        try {
            DMSExchangeService service = new DMSExchangeService(serverURL, new QName(NAMESPACE_URI, serviceName));
            DMSExchangePortType port = service.getPort(new QName(NAMESPACE_URI, portName), DMSExchangePortType.class);

            Communication communication = getCommunication(message);

            PutDMSMessageRequest request = new PutDMSMessageRequest();
            request.setCommunication(communication);
                        
            addAttachedFiles(request, communication, getAttachmentsExceptCommunictaion(message));
            putDMSMessageResponse = port.putDMSMessage(request);

        } catch (WebServiceException wse) {
            throwSendRetryException(wse);
        } catch (IllegalArgumentException e) {
            throwSendRetryException(e);
        }

        if (putDMSMessageResponse != null && putDMSMessageResponse.getResult() != null
                && SUCCESS_RESULT_CODE.equals(putDMSMessageResponse.getResult().getResultCode())) {
            logger.info("SoapSozSender task is successfully finished");
            return SendStatus.SUCCESS;
        } else {
            logger.error(putDMSMessageResponse.getResult().getResultMessage());
            return SendStatus.FAILURE;
        }

    }

    private  List<File> getAttachmentsExceptCommunictaion(MessageToSend message) {
        List<File> attachments = new ArrayList<File>();
        for(File attachment : message.getAttachments()) {
            if(!DocumentBodyReader.DOCUMENT_FILENAME.equals(attachment.getName())){
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    private void throwSendRetryException(Exception e) throws SendException {
        //Exception is caused by the connection fail problem
        throw new SendException("Error calling web service: " + e.getMessage(), true);
    }
*/
    /**
     * Initializes and puts in cache the instance of {@link Unmarshaller}.
     * @return {@link Unmarshaller}
     * @throws SendException
     */
/*    private Unmarshaller getUnmarshaller() throws SendException {
        if (unmarshaller == null) {
            try {
                JAXBContext jc = JAXBContext.newInstance(COMMUNICATION_PACKAGE);
                unmarshaller = jc.createUnmarshaller();
            } catch (JAXBException e) {
                throw new SendException(e);
            }
        }
        return unmarshaller;
    }
*/    
    /**
     * Retrieves the {@link Communication} object from the main document (document.xml) in {@link MessageToSend}.
     * @param message
     * @return
     * @throws SendException
     */
/*    private Communication getCommunication(MessageToSend message) throws SendException {
        Communication communication;
        try {
            File communicationDocument = getCommunicationDocument(message);
            InputStream inputStream = new FileInputStream(communicationDocument);
            communication = (Communication) getUnmarshaller().unmarshal(inputStream);
            inputStream.close();
        } catch (IOException e) {
            throw new SendException("Can't read " + message.getDocument().getAbsolutePath(), e);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new SendException("Can't unmarshall " + message.getDocument().getAbsolutePath(), e);
        }
        return communication;
    }

    private File getCommunicationDocument(MessageToSend message) throws SendException {        
        if(DocumentBodyReader.DOCUMENT_FILENAME.equals(message.getDocument().getName())){
            return message.getDocument();
        }
        for(File attachement : message.getAttachments()){
            if(DocumentBodyReader.DOCUMENT_FILENAME.equals(attachement.getName())){
                return attachement;
            }
        }
        throw new SendException("No communication file is found in message: " + message);
    }
    
    private void addAttachedFiles(PutDMSMessageRequest request, Communication communication, List<File> attachements) throws SendException {
        
        Communication.Files files = communication.getFiles();
        if (files != null && files.getFile() != null) {
            List<AttachedFileType> attachedFiles = request.getAttachedFile();

            for (AssociatedFile associatedFile : files.getFile()) {
                String localFileName = associatedFile.getLocalName();
                BigInteger localId = associatedFile.getLocalId();
                FileType fileType = associatedFile.getType();

                if (localFileName != null) {
                    try {
                        File file = getFileByName(attachements, localFileName);
                        if (file == null) {
                            throw new SendException("No attachment with name: " + localFileName + " founfd in folder.");
                        }
                        if (file.exists() && file.isFile()) {
                            byte[] fileContent = FileUtils.readFileToByteArray(file);
                            AttachedFileType attachedFileType = new AttachedFileType();
                            attachedFileType.setLocalName(localFileName);
                            if (localId != null) {
                                attachedFileType.setLocalId(localId.toString());
                            }
                            if (fileType != null){
                                attachedFileType.setType(fileType.value());
                            }
                            attachedFileType.setValue(fileContent);
                            attachedFiles.add(attachedFileType);
                            logger.debug("File \"" + localFileName + "\" was attached to the request.");
                        } else if (!file.exists()) {
                            logger.error("File \"" + localFileName + "\" does not exists in document folder. Skip it.");
                        } else if (!file.isFile()) {
                            logger.error("\"" + localFileName + "\" is not a file. Skip it.");
                        }
                    } catch (IOException e) {
                        logger.error("Error reading attached file \"" + localFileName
                                + "\".", e);
                        throw new SendException("Error reading attached file \"" + localFileName
                                + "\"." + e.getMessage());
                    }
                }
            }
        }
    }
    
    private File getFileByName(List<File> files, String fileName){
        for(File file : files){
            if(fileName.equals(file.getName())){
                return file;
            }
        }
        return null;
    }
*/    
/*    protected Properties getConfiguration() {
        String resourceFile = getParameters().getProperty(PLUGIN_PROP_SOAP_CONF);
        if (resourceFile == null) {
            throw new IllegalStateException("Plugin " + this + " is not configured properly. Required property \"" +
                    PLUGIN_PROP_SOAP_CONF + "\" was not set.");
        }
        return TransportAgentConfiguration.getConfiguration(resourceFile);
    }
*/
    /**
     * Reads the web service wsdl from property {@link SoapSozSender#WEB_SERVICE_WSDL} in address, 
     * if this property is not defined then reads its value from property {@link SoapSozSender#SOZ_WEB_SERVICE_WSDL} in soz configuration (soz.properties).
     * @param message
     * @return
     * @throws SendException
     */
/*    public String getWebServiceWsdl(MessageToSend message) throws SendException {        
        String webServiceWsdl = getProperty(WEB_SERVICE_WSDL, message.getAddress().getProp());
        if (!StringUtils.isBlank(webServiceWsdl)){
            return webServiceWsdl;
        }

        if (getConfiguration().getProperty(SOZ_WEB_SERVICE_WSDL) != null) {
            webServiceWsdl = getConfiguration().getProperty(SOZ_WEB_SERVICE_WSDL);
        } else {
            throw new SendException("Web service is not defined for address" + message.getAddress());
        }
        return webServiceWsdl;
    }
*/}
