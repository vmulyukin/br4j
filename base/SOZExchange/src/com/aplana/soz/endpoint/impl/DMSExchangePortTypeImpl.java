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
package com.aplana.soz.endpoint.impl;

import java.io.*;
import java.util.List;
import java.util.Properties;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.aplana.dbmi.Portal;
import com.aplana.soz.model.attachement.AttachedFileType;
import com.aplana.soz.model.communication.AssociatedFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.soz.model.attachement.PutDMSMessageRequest;
import com.aplana.soz.model.attachement.PutDMSMessageResponse;
import com.aplana.soz.model.attachement.ResultType;
import com.aplana.soz.model.communication.Communication;
import com.aplana.soz.service.DMSExchangePortType;
/**
 * Web Service implementation. 
 * @author atsvetkov
 *
 */
@WebService(endpointInterface = "com.aplana.soz.service.DMSExchangePortType", targetNamespace = "urn://x-artefacts-it-ru/dob/poltava/dmsx/1.0")
public class DMSExchangePortTypeImpl implements DMSExchangePortType {

	protected final Log logger = LogFactory.getLog(getClass());
	public static final String FILE_NAME = "document.xml";
	public static final String DEFAULT_FOLDER = "default";	
	public static final String FILE_ENCODING = "WINDOWS-1251";	
	public static final String SUCCESS_MESSAGE = "Web Service called successfully";	
	public static final String ERROR_MESSAGE = "Web Service call fails: ";	
	

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
			jaxbContext = JAXBContext.newInstance("com.aplana.soz.model.communication");
		} catch (JAXBException e) {
			logger.error("Error creating JAXBContext, " + e.getMessage());
		}
	}
	
	public PutDMSMessageResponse putDMSMessage(PutDMSMessageRequest request) {
		logger.debug("Calling web service DMSExchangePortType");
		PutDMSMessageResponse response = new PutDMSMessageResponse();
		try {
			if (request != null && request.getCommunication() != null) {
                writeToFile(request);
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
    }

    private void writeToFile(PutDMSMessageRequest request) throws FileNotFoundException, IOException, JAXBException {
        Communication communicationData = request.getCommunication();
		String folderName = getFolderName(communicationData);
		makeStorageFolder(folderName);
        String folderPath = getInFolder() + File.separator + folderName + File.separator;
        FileOutputStream outputStream = new FileOutputStream(folderPath + FILE_NAME);
		if (jaxbContext != null) {
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, FILE_ENCODING);
			marshaller.marshal(communicationData, outputStream);
			outputStream.close();
		}
        List<AttachedFileType> attachedFile = request.getAttachedFile();
        if (attachedFile != null && attachedFile.size() > 0) {
            writeAttachments(folderPath, attachedFile);
        }

	}

    private void writeAttachments(String folder, List<AttachedFileType> attachedFiles) throws IOException {
        for (AttachedFileType attachedFile : attachedFiles) {
            String localName = attachedFile.getLocalName();
            if (localName != null) {
                FileOutputStream outputStream = new FileOutputStream(folder + localName);
                outputStream.write(attachedFile.getValue());
                outputStream.close();
            }
        }
    }

	/**
	 * Gets the folder name where the obtained xml will be stored.
	 * Returns //communucation/header/@uid in incoming xml.
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

	
	private void makeStorageFolder(String uid) {
		String folderName = getInFolder() + File.separator + uid;
		File file = new File(folderName);
		if(!file.exists()){
			file.mkdirs();
		}
	}

    public final static String CONFIG_FILE_NAME = "dbmi/soz/config.properties";
    public final static String IN_FOLDER_PROPERTY = "soz.in.directory";

	private String getInFolder() {
        try {
            InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE_NAME);
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(IN_FOLDER_PROPERTY);
        } catch (IOException e) {
            logger.error("Can't read webservice configuration! Please check \"" + IN_FOLDER_PROPERTY
                    + "\" property in %JBOSS_HOME%/server/default/conf/" + CONFIG_FILE_NAME);
            logger.error(e);
            throw new RuntimeException(e);
        }
	}
	
}