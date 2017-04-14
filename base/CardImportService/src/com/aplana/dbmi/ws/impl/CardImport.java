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
package com.aplana.dbmi.ws.impl;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.cardexchange.action.ImportCardXml;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.ws.CardImportService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.io.ByteArrayInputStream;

@WebService(endpointInterface = "com.aplana.dbmi.ws.CardImportService")
public class CardImport implements CardImportService {
	private Log logger = LogFactory.getLog(getClass());
	private static final ObjectId ATTR_FILE_ID = 
		ObjectId.predefined(CardLinkAttribute.class, "jbr.files");

	public long importCard(DataHandler card, String filename, DataHandler file) {
		try {
			InitialContext context = new InitialContext();
			DataServiceHome home = (DataServiceHome) PortableRemoteObject
					.narrow(context.lookup("ejb/dbmi"), DataServiceHome.class);
			DataService service = home.create();
			DataServiceBean serviceBean = new DataServiceBean();
			serviceBean.setService(service, service.authUser(new SystemUser(),
					"127.0.0.1"));
			ImportCardXml action = new ImportCardXml();
			action.setXmlData(card.getInputStream());
			ObjectId cardId = (ObjectId) serviceBean.doAction(action);
			if (filename != null && filename.trim().length() > 0) {
				logger.info("Uploading file: filename = '" + filename + "'");
				UploadFile upload = new UploadFile();
				upload.setCardId(cardId);
				upload.setFileName(filename);
				upload.setData(file.getInputStream());
				serviceBean.doAction(upload);
			} else {
				logger.info("Filename is not specified.");
			}
			return ((Long) cardId.getId()).longValue();
		} catch (Exception e) {
			logger.error("Exception caught: ", e);
			return -1;
		}
	}

	public String testService(String instring, String instring1) {
		String retVal = "Hello, " + instring + "! This is the second string: "
				+ instring1;
		logger.info(retVal);
		return retVal;
	}

	public Long importMaterialCard(String attrName, String attrVal) {
		try {
			InitialContext context = new InitialContext();
			DataServiceHome home = (DataServiceHome) PortableRemoteObject
					.narrow(context.lookup("ejb/dbmi"), DataServiceHome.class);
			DataService service = home.create();
			DataServiceBean serviceBean = new DataServiceBean();
			serviceBean.setService(service, service.authUser(new SystemUser(),
					"127.0.0.1"));
			// New card

			ObjectId templateId = new ObjectId(Template.class, new Long(284));
			logger.info("Template ID: " + templateId.getId().toString());
			CreateCard action = new CreateCard(templateId);

			Card card = (Card) serviceBean.doAction(action);

			if (card != null)
				logger.info("Card created from Template "
						+ templateId.getId().toString());
			ObjectId cardId = serviceBean.saveObject(card);
			card.setId((Long) cardId.getId());

			if (cardId == null)
				logger.info("No ID assigned!");
			logger.info("Card ID: " + cardId.getId().toString());

			if (attrName != null && attrName.trim().length() > 0) {
				// Set attributes
			} else {
				logger.info("Attribute is not specified.");
			}
			return ((Long) cardId.getId()).longValue();
			// return (new Long(111));

		} catch (Exception e) {
			logger.error("Exception caught: ", e);
			return new Long(-1);
		}
	}

	public Long importCardFromXML(String encoding, String xmlInString,
			String inFileName, String inFileBase64, String inAttachmentIds,
			String inFileNames) {
		String xmlString = null;
		Long retVal = new Long(-1);
		ObjectId cardId = null;
		DataServiceBean serviceBean = null;
		DataService service = null;
		Card srcCard = null;
		UploadFile upload = null;

		try {
			InitialContext context = new InitialContext();
			DataServiceHome home = (DataServiceHome) PortableRemoteObject
					.narrow(context.lookup("ejb/dbmi"), DataServiceHome.class);
			service = home.create();
			serviceBean = new DataServiceBean();
			serviceBean.setService(service, service.authUser(new SystemUser(),
					"127.0.0.1"));
			// New card

			xmlString = xmlInString;
			logger.info("Received XML in Base64: " + xmlString);
			byte[] xmlBytes = Base64.base64ToByteArray(xmlString);
			String tempXml = new String(xmlBytes);

			logger.info("Received XML from Base64: " + tempXml);

			ByteArrayInputStream inStream = new ByteArrayInputStream(xmlBytes);
			ImportCardXml action = new ImportCardXml();
			action.setXmlData(inStream);
			cardId = (ObjectId) serviceBean.doAction(action);
			if (inFileName != null) {
				logger.info("Received Filename: " + inFileName);
				if (inFileBase64 != null) {
					if (inFileBase64.equals(""))
						logger.warn("File body is empty");

					LockObject lockAction = new LockObject();
					lockAction.setId(cardId);
					serviceBean.doAction(lockAction);
					byte[] inFileBytes = Base64.base64ToByteArray(inFileBase64);
					ByteArrayInputStream bais = new ByteArrayInputStream(inFileBytes);

					upload = new UploadFile();
					upload.setCardId(cardId);
					upload.setFileName(inFileName);
					upload.setData(bais);
					serviceBean.doAction(upload);

					Card currCard = serviceBean.getById(cardId);
					serviceBean.saveObject(currCard);
					logger.info("Action executed!");
					UnlockObject unLockAction = new UnlockObject();
					unLockAction.setId(cardId);
					serviceBean.doAction(unLockAction);
				}else
					logger.warn("File body is NULL ! File is not uploaded !");
			}

			if ((inAttachmentIds != null)&&(!inAttachmentIds.equals(""))) {
				String[] attachIds = inAttachmentIds.split("#");
				String[] fileNames = inFileNames.split("#");
				String curAttachId = null;
				String curFileName = null;

				logger.info("Received AttachmentId: " + inAttachmentIds);
				ObjectId linkAttrObjId = new ObjectId(CardLinkAttribute.class,
						"DOCLINKS");
				srcCard = (Card) serviceBean.getById(cardId);

				LockObject lockAction = new LockObject();
				lockAction.setId(cardId);
				serviceBean.doAction(lockAction);

				CardLinkAttribute linkAttr = (CardLinkAttribute) srcCard
						.getAttributeById(linkAttrObjId);

				for (int i = 0; i < attachIds.length; i++) {
					curAttachId = (String) attachIds[i];
					curFileName = (String) fileNames[i];
					linkAttr.addLinkedId(new ObjectId(Card.class, new Long(
							curAttachId)));
					System.out.println("Filename is: " + curFileName);
				}

				serviceBean.saveObject(srcCard);
				UnlockObject unlockAction = new UnlockObject();
				unlockAction.setId(cardId);
				serviceBean.doAction(unlockAction);

			}

			retVal = ((Long) cardId.getId()).longValue();
		} catch (Exception e) {
			logger.error("Exception caught: ", e);
			// e.printStackTrace();
			retVal = new Long(-1);
		} finally {
			return retVal;
		}
	}
}