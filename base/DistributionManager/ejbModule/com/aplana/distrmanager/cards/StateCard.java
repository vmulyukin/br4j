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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.WriteResultException;
import com.aplana.distrmanager.letter.types.Letter;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class StateCard {

	private static final ObjectId RESULT_PROCESSING = ObjectId.predefined(
			TextAttribute.class, "resultProcessing");
	private static final ObjectId LAST_TIME = ObjectId.predefined(
			DateAttribute.class, "lastTimeMEDO");
	private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.ProcessingDistribution");
	public static final ObjectId ITERATION_NUMBER = ObjectId.predefined(
			IntegerAttribute.class, "jbr.processingDestribution.count");
	private static final ObjectId TEMPLATE_FILE_ID = ObjectId.predefined(
			Template.class, "jbr.file");
	private static final ObjectId DOCLINKS_ATTRIBUTE_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	private static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
		    TextAttribute.class, "uid");

	private ObjectId cardId;
	private String resultProcessing;
	private Card card = null;
	private DataServiceFacade serviceBean = null;
	private Log logger = LogFactory.getLog(getClass());
	private Date lastTime;
	private String id = null;
	private CardLinkAttribute docLinks;
	private String uuid = null;
	private TextAttribute resultProcessingAttr;

	public StateCard(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	@SuppressWarnings("unused")
	private StateCard() {
	}
	
	public void init(Card card) throws DataException {
		if (card != null) {	
			if(this.card == null)
				this.card = card;
			TextAttribute uidAttribute = (TextAttribute) card
		    	.getAttributeById(UUID_ATTRIBUTE_ID);
	    if (uidAttribute != null)
	    	uuid = uidAttribute.getValue();
	    else
	    	throw new DataException("jbr.DistributionManager.card.statecard.uuid.notFound");
	    docLinks = card
			.getCardLinkAttributeById(DOCLINKS_ATTRIBUTE_ID);
	    resultProcessingAttr = (TextAttribute)card.getAttributeById(RESULT_PROCESSING);
	    if (resultProcessingAttr != null)
	    	resultProcessing = resultProcessingAttr.getValue();
	    else
	    	throw new DataException("jbr.DistributionManager.card.statecard.resultProcessing.notFound");
	    cardId = card.getId();
	    id = card.getId().getId().toString();
		} else
		    throw new DataException("jbr.DistributionManager.card.statecard.notFound");
		logger.info("Create object StateCard with current parameters: "
				+ getParameterValuesLog());
	}
	
	public void init(ObjectId cardSatateId) throws DataException, ServiceException {
		this.card = (Card) serviceBean.getById(cardSatateId);
		init(card);
	}
	
	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("UUID='%s', ", uuid));
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

	public ObjectId getCardId() {
		return cardId;
	}

	/*public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}*/

	public String getResultProcessing() {
		return this.resultProcessing;
	}

	public void setResultProcessing(String resultProcessing) throws WriteResultException {
		try {
			this.resultProcessing = resultProcessing;
			resultProcessingAttr.setValue(resultProcessing);
		} catch(Exception e) {
			throw new WriteResultException(e);
		}
	}

	public ObjectId createCard() throws DataException {
		logger.info(String.format("Trying to create StateDistributionManager card."));

		if (serviceBean == null)
			throw new DataException("DataServiceBean was not initialized");

		final CreateCard createCard = new CreateCard(TEMPLATE_ID);

		try {
			card = (Card) serviceBean.doAction(createCard);
			if (card == null) {
				throw new DataException(
						"Card was not created by unspecifed reason.");
			}
			final DateAttribute lastTimeAttr = (DateAttribute) card.getAttributeById(LAST_TIME);
			lastTimeAttr.setValue(this.lastTime);
			final TextAttribute resultProcessingAttr = (TextAttribute) card.getAttributeById(RESULT_PROCESSING);
			resultProcessingAttr.setValue(resultProcessing);
			final ObjectId cardId = saveCardDistributionManager();
			logger.info(String.format("Card with '%s' id was created", cardId.getId().toString()));
			this.cardId = cardId;
			return cardId;
		} catch (DataException ex) {
			throw new DataException("jbr.DistributionManager.card.statecard.dataexception", ex);
		} catch (ServiceException ex) {
			throw new DataException("jbr.DistributionManager.card.statecard.serviceexception", ex);
		} catch (Exception e) {
			throw new DataException("jbr.DistributionManager.card.statecard.exception", e);
		}

	}

	// ������ ��� ������ createCard
	private ObjectId saveCardDistributionManager() throws DataException, ServiceException
	{
		final ObjectId id_c = serviceBean.saveObject(card);
		final UnlockObject unlock = new UnlockObject(id_c);
		serviceBean.doAction(unlock);
		return id_c;
	}
	
	public ObjectId saveLetterXml(ExportCardToXml.Result outXmlResult) throws Exception {
		if (docLinks == null) {
			logger.error("Attribute " + DOCLINKS_ATTRIBUTE_ID
					+ " in the target card " + id
					+ " was not found");
			throw new DataException("jbr.DistributionManager.card.statecard.attributeNotFound");
		}
		
		// ������� �������� ��������
		Card cardFile = UtilsWorkingFiles.createFileCard(serviceBean, TEMPLATE_FILE_ID, Letter.RESULT_FILE_NAME);
		
		// ��������� � �������� �������� ����
		UtilsWorkingFiles.attachFile(cardFile, outXmlResult.getData(), Letter.RESULT_FILE_NAME, serviceBean);
		
		// ������������ �������� �������� ��������� � �������� �������
		docLinks.setLinkedCardLabelText(cardFile.getId(), Letter.RESULT_FILE_NAME);
		UtilsWorkingFiles.saveCard(card, serviceBean);
		return cardFile.getId();
	}

	public String getUuid() {
		return uuid;
	}

	public Card getCard() {
		return card;
	}
}
