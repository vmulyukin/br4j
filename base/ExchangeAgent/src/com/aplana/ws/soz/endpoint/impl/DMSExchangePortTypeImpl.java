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
package com.aplana.ws.soz.endpoint.impl;

import com.aplana.agent.plugin.PluginFactory;
import com.aplana.agent.plugin.receiver.WebServiceReceiver;
import com.aplana.ws.replication.Folder;
import com.aplana.ws.soz.model.attachement.AttachedFileType;
import com.aplana.ws.soz.model.attachement.PutDMSMessageRequest;
import com.aplana.ws.soz.model.attachement.PutDMSMessageResponse;
import com.aplana.ws.soz.model.attachement.ResultType;
import com.aplana.ws.soz.model.communication.Communication;
import com.aplana.ws.soz.service.DMSExchangePortType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Web Service implementation.
 *
 * @author atsvetkov
 */
@WebService(endpointInterface = "com.aplana.ws.soz.service.DMSExchangePortType", targetNamespace = "urn://x-artefacts-it-ru/dob/poltava/dmsx/1.0")
public class DMSExchangePortTypeImpl implements DMSExchangePortType {

    protected final Log logger = LogFactory.getLog(getClass());
    public static final String FILE_NAME = "document.xml";
    public static final String DEFAULT_FOLDER = "default";
    public static final String FILE_ENCODING = "WINDOWS-1251";
    public static final String SUCCESS_MESSAGE = "Web Service called successfully";
    public static final String ERROR_MESSAGE = "Web Service call fails: ";

    Marshaller marshaller;


    private enum ResultCode {
        SUCCESS("SUCCESS"),
        ERROR("ERROR");

        private String value;

        ResultCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private JAXBContext jaxbContext;

    //initialization block
    {
        try {
            jaxbContext = JAXBContext.newInstance("com.aplana.ws.soz.model.communication");
        } catch (JAXBException e) {
            logger.error("Error creating JAXBContext, " + e.getMessage());
        }
        try {
            marshaller = jaxbContext.createMarshaller();
        } catch (JAXBException e) {
            logger.error("Error creating Marshaller, " + e.getMessage());
        }
    }

    private final static String RECEIVE_PLUGIN = "sozWsReceiver";

    public PutDMSMessageResponse putDMSMessage(PutDMSMessageRequest request) {
/*        logger.debug("Calling web service DMSExchangePortType");

        if (!PluginFactory.containsBean(RECEIVE_PLUGIN)) {
            throw new IllegalStateException("This WS require " + RECEIVE_PLUGIN + "in beans.xml");
        }

        PutDMSMessageResponse response = new PutDMSMessageResponse();
        try {
            if (request != null && request.getCommunication() != null) {
                Folder folder = writeToBuffer(request);
                WebServiceReceiver receiver = (WebServiceReceiver) PluginFactory.getBean(RECEIVE_PLUGIN);
                receiver.saveFolder(folder);
            }
        } catch (Exception e) {
            logger.error("Exception occured calling ws DMSExchangePortType: " + e.getMessage());
            ResultType resulType = new ResultType();
            resulType.setResultCode(ResultCode.ERROR.getValue());
            resulType.setResultMessage(ERROR_MESSAGE + e.getMessage());
            response.setResult(resulType);
            return response;
        }

        logger.info("Web service DMSExchangePortType called successfully");
        ResultType resulType = new ResultType();
        resulType.setResultCode(ResultCode.SUCCESS.getValue());
        resulType.setResultMessage(SUCCESS_MESSAGE);
        response.setResult(resulType);
        return response;
*/    
    return null;
    }

    private Folder writeToBuffer(PutDMSMessageRequest request) throws IOException, JAXBException {
        Folder ret = new Folder();
        Communication communicationData = request.getCommunication();
        ret.setName(getFolderName(communicationData));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, FILE_ENCODING);
        marshaller.marshal(communicationData, outputStream);
        List<Folder.File> files = new ArrayList<Folder.File>();
        Folder.File document = new Folder.File(outputStream.toByteArray(), FILE_NAME);
        outputStream.close();

        files.add(document);
        List<AttachedFileType> attachedFile = request.getAttachedFile();
        if (attachedFile != null && attachedFile.size() > 0) {
            writeAttachments(files, attachedFile);
        }

        ret.setFiles(files);
        return ret;
    }

    private void writeAttachments(List<Folder.File> folder, List<AttachedFileType> attachedFiles) throws IOException {
        for (AttachedFileType attachedFile : attachedFiles) {
            String localName = attachedFile.getLocalName();
            if (localName != null) {
                Folder.File file = new Folder.File(attachedFile.getValue(), localName);
                folder.add(file);
            }
        }
    }

    /**
     * Gets the folder name where the obtained xml will be stored.
     * Returns //communucation/header/@uid in incoming xml.
     *
     * @param communicationData {@link Communication}
     * @return folder name
     */
    private String getFolderName(Communication communicationData) {
        if (communicationData.getHeader() != null
                && communicationData.getHeader().getUid() != null) {
            return communicationData.getHeader().getUid();
        }
        logger.warn("Notification does not have UID, the default folder is used: " + DEFAULT_FOLDER);
        return DEFAULT_FOLDER;
    }


}