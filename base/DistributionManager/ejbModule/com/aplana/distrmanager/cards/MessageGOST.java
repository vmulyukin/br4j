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
package com.aplana.distrmanager.cards;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CloneCard;
import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.action.file.CopyMaterial;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class MessageGOST {

	protected Log logger = LogFactory.getLog(getClass());

	public static final ObjectId TEMPLATE_MESSAGE_GOST = ObjectId.predefined(
			Template.class, "jbr.gost.msg");
	public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
		    TextAttribute.class, "uid");
	private static final ObjectId TEMPLATE_FILE_ID = ObjectId.predefined(
			Template.class, "jbr.file");
	private static final ObjectId DOCLINKS_ATTRIBUTE_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	private static final ObjectId MATERIAL_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.materialName");
	private static final ObjectId NOTICE_GOST_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.gost.msg.acks");
	private static final ObjectId UUID_SENDER_ID = ObjectId.predefined(
		    TextAttribute.class, "jbr.gost.msg.fromOrgUuid");
	private static final ObjectId NAME_SENDER_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.gost.msg.fromOrg");
	private static final ObjectId UUID_SYSTEM_SENDER_ID = ObjectId.predefined(
			TextAttribute.class, "jbr.gost.msg.fromSysUuid");
	private static final ObjectId NAME_SYSTEM_SENDER_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.gost.msg.fromSysName");
	private static final ObjectId RECIPIENT_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.gost.msg.toOrg");
	private static final ObjectId RECIPIENT_UID_ID = ObjectId.predefined(
			TextAttribute.class, "jbr.gost.msg.toOrgUuid");
	private static final ObjectId SYSTEM_SENDER_DETAILS_ID = ObjectId.predefined(
			TextAttribute.class, "jbr.gost.msg.fromSysDet");
	private static final ObjectId STANDART_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.gost.msg.standart");
	private static final ObjectId VERSION_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.gost.msg.version");

	private static final String RESULT_FILE_NAME = "document.xml";

	private DataServiceFacade serviceBean = null;
	private Card card = null;
	private String uid = null;
	private String uidSender = null;
	private String uidSystemSender = null;
	private String fullNameSender = null;
	private String nameSystemSender = null;
	private String recipient = null;
	private String recipientUid = null;
	private String sysSenderDetails = null;
	private String standart = null;
	private String version = null;
	private String id = null;
	private CardLinkAttribute docLinks;
	private CardLinkAttribute noticeGostLinks;
	private StringAttribute recipientAttr = null;
	private TextAttribute recipientUidAttr = null;
	private TextAttribute uidSenderAttr = null;
	private StringAttribute fullNameSenderAttr = null;
	private TextAttribute uidSystemSenderAttr = null;
	private StringAttribute nameSystemSenderAttr = null;
	private TextAttribute sysSenderDetailsAttr = null;
	private StringAttribute standartAttr = null;
	private StringAttribute versionAttr = null;

	public MessageGOST(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

	@SuppressWarnings("unused")
	private MessageGOST() {
	}

	public void init(ObjectId msgGOSTCardId) throws DataException {
		this.card = (Card) serviceBean.getById(msgGOSTCardId);
		if (card != null) {
			TextAttribute uidAttribute = (TextAttribute) card
		    	.getAttributeById(UUID_ATTRIBUTE_ID);
	    if (uidAttribute != null)
	    	uid = uidAttribute.getValue();
	    else
	    	throw new DataException("jbr.DistributionManager.cards.MessageGOST.notUUIDAttribute");

	    uidSenderAttr = (TextAttribute) card
		    	.getAttributeById(UUID_SENDER_ID);
	    if (null != uidSenderAttr) {
	    	uidSender = uidSenderAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notUUIDSenderAttr");

	    uidSystemSenderAttr = (TextAttribute) card
		    	.getAttributeById(UUID_SYSTEM_SENDER_ID);
	    if (null != uidSystemSenderAttr) {
	    	uidSystemSender = uidSystemSenderAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notUUIDSystemSenderAttr");

	    fullNameSenderAttr = (StringAttribute) card
	    		.getAttributeById(NAME_SENDER_ID);
	    if (null != fullNameSenderAttr) {
	    	fullNameSender = fullNameSenderAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notFullNameSenderAttr");

	    nameSystemSenderAttr = (StringAttribute) card
	    		.getAttributeById(NAME_SYSTEM_SENDER_ID);
	    if (null != nameSystemSenderAttr) {
	    	nameSystemSender = nameSystemSenderAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notNameSystemSenderAttr");

	    recipientAttr = (StringAttribute) card
	    		.getAttributeById(RECIPIENT_ID);
	    if (null != recipientAttr) {
	    	recipient = recipientAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notRecipientAttr");

	    recipientUidAttr = (TextAttribute)card.getAttributeById(RECIPIENT_UID_ID);
	    if (null != recipientUidAttr) {
	    	recipientUid = recipientUidAttr.getValue();
	    } else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notRecipientUidAttr");

	    sysSenderDetailsAttr = (TextAttribute) card
		    	.getAttributeById(SYSTEM_SENDER_DETAILS_ID);
	    if (null != sysSenderDetailsAttr) {
	    	sysSenderDetails = sysSenderDetailsAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notSysSenderDetailsAttr");

	    standartAttr = (StringAttribute) card
	    		.getAttributeById(STANDART_ID);
	    if (null != standartAttr) {
	    	standart = standartAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notStandartAttr");

	    versionAttr = (StringAttribute) card
	    		.getAttributeById(VERSION_ID);
	    if (null != standartAttr) {
	    	version = versionAttr.getValue();
	    }
	    else
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.notVersionAttr");

	    docLinks = card
			.getCardLinkAttributeById(DOCLINKS_ATTRIBUTE_ID);
	    if (null == docLinks)
	    	throw
	    		new DataException("jbr.DistributionManager.cards.MessageGOST.docLinks.isNull");
	    noticeGostLinks = card.getCardLinkAttributeById(NOTICE_GOST_ID);
	    if (null == noticeGostLinks)
	    	throw
    			new DataException("jbr.DistributionManager.cards.MessageGOST.noticeGostLinks.isNull");
	    id = card.getId().getId().toString();
		} else
		    throw new DataException("jbr.DistributionManager.cards.MessageGOST.notFound");
		logger.info("Create object MessageGOST with current parameters: "
				+ getParameterValuesLog());
	}

	public long getCardId() throws DataException {
    	if (card != null ) return (Long)card.getId().getId();
    	throw new DataException("jbr.DistributionManager.cards.elementlistmailing.notFound");
    }

	public String getUidSender() {
		return uidSender;
	}

	public void setUidSender(String uidSender) {
		uidSenderAttr.setValue(uidSender);
		this.uidSender = uidSender;
	}

	public String getUidSystemSender() {
		return uidSystemSender;
	}

	public void setUidSystemSender(String uidSystemSender) {
		uidSystemSenderAttr.setValue(uidSystemSender);
		this.uidSystemSender = uidSystemSender;
	}

	public String getFullNameSender() {
		return fullNameSender;
	}

	public void setFullNameSender(String fullNameSender) {
		fullNameSenderAttr.setValue(fullNameSender);
		this.fullNameSender = fullNameSender;
	}

	public String getNameSystemSender() {
		return nameSystemSender;
	}

	public void setNameSystemSender(String nameSystemSender) {
		nameSystemSenderAttr.setValue(nameSystemSender);
		this.nameSystemSender = nameSystemSender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		recipientAttr.setValue(recipient);
		this.recipient = recipient;
	}

	public String getRecipientUid() {
		return recipientUid;
	}

	public void setRecipientUid(String value) {
		recipientUidAttr.setValue(value);
		this.recipientUid = value;
	}

	public String getSysSenderDetails() {
		return sysSenderDetails;
	}

	public void setSysSenderDetails(String sysSenderDetails) {
		sysSenderDetailsAttr.setValue(sysSenderDetails);
		this.sysSenderDetails = sysSenderDetails;
	}

	public String getStandart() {
		return standart;
	}

	public void setStandart(String standart) {
		standartAttr.setValue(standart);
		this.standart = standart;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		versionAttr.setValue(version);
		this.version = version;
	}

	public String getUid() {
		return uid;
	}

	public void saveAttachments(ExportCardToXml.Result result) throws Exception {
		if (docLinks == null) {
			logger.error("Attribute " + DOCLINKS_ATTRIBUTE_ID
					+ " in the target card " + id
					+ " was not found");
			throw new DataException("jbr.DistributionManager.cards.MessageGOST.attributeNotFound");
		}

		// ������� �������� ��������
		Card cardFile = UtilsWorkingFiles.createFileCard(serviceBean, TEMPLATE_FILE_ID, RESULT_FILE_NAME);

		// ��������� � �������� �������� ����
		UtilsWorkingFiles.attachFile(cardFile, result.getData(), RESULT_FILE_NAME, serviceBean);

		// ������������ �������� �������� ��������� � �������� ��������� ����
		docLinks.setLinkedCardLabelText(cardFile.getId(), RESULT_FILE_NAME);

		// ��������� ��������� ��������, ���� ��� ����
		Map<ObjectId, String> attachments = result.getFiles();
		if (null != attachments && !attachments.isEmpty()) {

			// ���� ��� ������������
			CloneCard cloneAction = new CloneCard();
			cloneAction.getDisabledTypes().remove(Attribute.TYPE_MATERIAL);
			cloneAction.setTemplate(TEMPLATE_FILE_ID);

			// ���� ��� ����������� ���������
			CopyMaterial copyAction = new CopyMaterial();

			for (Map.Entry<ObjectId, String> attachment : attachments.entrySet()) {
				ObjectId cardId = attachment.getKey();
				String name = attachment.getValue();
				if (null == cardId) {
					throw new Exception(
								String.format("jbr.DistributionManager.cards.MessageGOST.saveAttachments.attacmentIsNull cardLink: %s; MessageGost: %s",
									docLinks.getId(),
									id
								)
							);
				}
				if(null == name || "".equals(name.trim())) {
					throw new Exception(
								String.format("jbr.DistributionManager.cards.MessageGOST.saveAttachments.attacmentNameIsEmpty cardLink: %s; MessageGost: %s",
									docLinks.getId(),
									id
								)
						);
				}
				cloneAction.setOrigId(cardId);

				// ���������
				Card cardCloneAttach = (Card)serviceBean.doAction(cloneAction);

				// ��������� ��������������� ��������
				UtilsWorkingFiles.saveCardCreated(cardCloneAttach, serviceBean);

				// ���������� ���� � ����� ��������
				copyAction.setFromCardId(cardId);
				copyAction.setToCardId(cardCloneAttach.getId());
				serviceBean.doAction(copyAction);

				// ��������������� �������� � ������������ � Result ������
				renameMaterial(cardCloneAttach, name);

				// ��������� ��������������� ��������
				UtilsWorkingFiles.saveCard(cardCloneAttach, serviceBean);
				docLinks.setLinkedCardLabelText(cardCloneAttach.getId(), name);
			}
		}
		saveCard();
		return;
	}

	public void saveCard() throws SaveCardException {
		UtilsWorkingFiles.saveCard(card, serviceBean);
	}

	//public static ObjectId findCard(String uidGOST, DataServiceFacade serviceBean) throws DataException {
		/*final Search searchMsgGOST = new Search();
		searchMsgGOST.setTemplates(Collections.singleton(DataObject.createFromId(TEMPLATE_MESSAGE_GOST)));
		searchMsgGOST.addStringAttribute(UUID_ATTRIBUTE_ID, uidGOST);
		searchMsgGOST.setByAttributes(true);*/
		/*try {
		    //@SuppressWarnings("unchecked")
		    //SearchResult cardsMsgGOST = (SearchResult)serviceBean.doAction(searchMsgGOST);
		    //Card card = (Card)cardsMsgGOST.getCards().get(0);
			GetCardIdByUUID cardMsgGOST = new GetCardIdByUUID();
			cardMsgGOST.setUuid(uidGOST);
			ObjectId cardId = (ObjectId)serviceBean.doAction(cardMsgGOST);
		    return cardId;
		} catch (Exception ex) {
		    throw new DataException("jbr.DistributionManager.cards.MessageGOST.searchFailed", ex);
		}
	}*/

	@SuppressWarnings("unchecked")
	public Map<ObjectId, String> getAttachments() throws Exception {
		Map<ObjectId, String> attachments = docLinks.getLabelLinkedMap();
		Iterator iter = attachments.entrySet().iterator();
		while(iter.hasNext()) {
			Object obj = iter.next();
			Map.Entry<ObjectId, String> attachment = (Map.Entry<ObjectId, String>)obj;
			ObjectId cardId = attachment.getKey();
			if (null == cardId) {
				throw new Exception(
							String.format("jbr.DistributionManager.cards.MessageGOST.getAttachments.attacmentIsNull cardLink: %s; MessageGost: %s",
										docLinks.getId(),
										cardId
									)
						);
			}
			Card card = (Card)serviceBean.getById(cardId);
			StringAttribute nameMaterial = (StringAttribute) card.getAttributeById(MATERIAL_NAME);
			String nameFile = nameMaterial.getValue();
			if(null == nameFile || "".equals(nameFile.trim())) {
				throw new Exception(
						String.format("jbr.DistributionManager.cards.MessageGOST.getAttachments.attacmentNameIsEmpty cardLink: %s; MessageGost: %s",
									docLinks.getId(),
									cardId
								)
						);
			}
			attachment.setValue(nameFile);
		}
		return attachments;
	}

	private void renameMaterial(Card card, String fileName) {
		StringAttribute nameMaterial = (StringAttribute) card.getAttributeById(MATERIAL_NAME);
		nameMaterial.setValue(fileName);
	}

	public void addNoticeGost(Long linkId) throws DataException, ServiceException {
		noticeGostLinks.addLinkedId(linkId);
		saveCard();
	    }

	public ObjectId getNoticeGostId() {
		ObjectId[] noticeGostArr = noticeGostLinks.getIdsArray();
		return (null != noticeGostArr)? noticeGostArr[0]:null;
	}

	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("UUID='%s', ", uid));
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

	public Card getCard() {
		return card;
	}

	public boolean isExistAttachments() {
		return !docLinks.isEmpty();
	}

	public boolean isExistNoticeGost() {
		Boolean result = Boolean.FALSE;
		if (null != noticeGostLinks.getIdsLinked() && !noticeGostLinks.getIdsLinked().isEmpty())
			result = Boolean.TRUE;
		return result;//!noticeGostLinks.isEmpty();
	}
}
