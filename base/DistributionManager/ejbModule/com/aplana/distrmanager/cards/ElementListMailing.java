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

import java.sql.Types;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.RemoveCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.distrmanager.exceptions.ChangeStateException;
import com.aplana.distrmanager.exceptions.DeleteMessageGostException;
import com.aplana.distrmanager.exceptions.FindMessageGostException;
import com.aplana.distrmanager.exceptions.SaveCardException;
import com.aplana.distrmanager.util.UtilsWorkingFiles;

public class ElementListMailing {

	protected Log logger = LogFactory.getLog(getClass());

	public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
		    StringAttribute.class, "jbr.distributionItem.uuid");
	public static final ObjectId SENDING_INFO_ATTRIBUTE_ID = ObjectId.predefined(
		    CardLinkAttribute.class, "jbr.distributionItem.processing");
	public static final ObjectId PREPARE_SENT = ObjectId.predefined(
		    WorkflowMove.class, "prepare.sent");
	public static final ObjectId PREPARE_NOTSENT = ObjectId.predefined(
		    WorkflowMove.class, "prepare.notsent");
	public static final ObjectId MESSAGE_GOST_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.distributionItem.msgGOST");
	public static final ObjectId LAST_ATEMPT_NUMBER = ObjectId.predefined(
			IntegerAttribute.class, "jbr.distributionItem.lastAttempt");
    public static final ObjectId RECIPIENT = ObjectId.predefined(
    		CardLinkAttribute.class, "jbr.distributionItem.recipient");
    public static final ObjectId MESSAGE_CREATION_TIME = ObjectId.predefined(
    	    DateAttribute.class, "ImportedDocument.message_creation_time");
    public static final ObjectId MODE_DELIVERY = ObjectId.predefined(
    	    ListAttribute.class, "jbr.distributionItem.method");
    public static final ObjectId MODE_MEDO = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.distributionItem.method.medo");
    public static final ObjectId MODE_DELO = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.distributionItem.method.delo");
    public static final ObjectId MODE_GOST = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.distributionItem.method.gost");
    public static final ObjectId TYPE_MESSAGE = ObjectId.predefined(
    	    ListAttribute.class, "jbr.gost.msg.type.sent");
    public static final ObjectId TYPE_MESSAGE_NOTICE = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.gost.msg.type.notification");
    public static final ObjectId TYPE_MESSAGE_DOCUMENT = ObjectId.predefined(
    	    ReferenceValue.class, "jbr.gost.msg.type.mainDoc");
    public static final ObjectId PROC_RESULT_ID = ObjectId.predefined(
		    TextAttribute.class, "jbr.importedDoc.result");

    public static final ReferenceValue MEDO_VALUE = (ReferenceValue) DataObject.createFromId(MODE_MEDO);
    public static final ReferenceValue DELO_VALUE = (ReferenceValue) DataObject.createFromId(MODE_DELO);
    public static final ReferenceValue GOST_VALUE = (ReferenceValue) DataObject.createFromId(MODE_GOST);

    public static final ReferenceValue NOTICE_VALUE = (ReferenceValue) DataObject.createFromId(TYPE_MESSAGE_NOTICE);
    public static final ReferenceValue DOCUMENT_VALUE = (ReferenceValue) DataObject.createFromId(TYPE_MESSAGE_DOCUMENT);

    private static final String CLEAR_ATTRIBUTE = "DELETE FROM attribute_value av \n" +
												  "WHERE av.card_id=:CARD_ID \n" +
												  "	AND av.attribute_code =:ATTR_ID \n";
    private static final String CLEAR_ATTRIBUTE_HIST = "DELETE FROM attribute_value_hist av \n" +
	  											  "WHERE av.card_id=:CARD_ID \n" +
	  											  "	AND av.attribute_code =:ATTR_ID \n";

    private DataServiceFacade serviceBean = null;
	private Card card = null;
	private CardLinkAttribute sendingInfoAttribute = null; // ���������� �� ��������
    private String uid = null;
    private String id = null;
    private String processingResult = null;
    private CardLinkAttribute msgGostLnk;
    private IntegerAttribute lastAttempt;
    private CardLinkAttribute recipientLnk;
    private DateAttribute messageCreationTime;

    private ReferenceValue deliveryValue;
    private ReferenceValue msgTypeValue;

    public ElementListMailing(DataServiceFacade serviceBean) {
		this.serviceBean = serviceBean;
	}

    @SuppressWarnings("unused")
	private ElementListMailing() {
    }

	public void init (Card cardElm) throws DataException, ServiceException {
		card = cardElm;
		if (card != null) {
		    StringAttribute uidAttribute = (StringAttribute) card
			    .getAttributeById(UUID_ATTRIBUTE_ID);
		    if (uidAttribute != null)
		    	uid = uidAttribute.getValue();
		    else
		    	throw new DataException("jbr.DistributionManager.cards.elementlistmailing.notUUIDAttribute");
		    id = card.getId().getId().toString();
			sendingInfoAttribute = card.getCardLinkAttributeById(SENDING_INFO_ATTRIBUTE_ID);
			if (sendingInfoAttribute == null)
				throw new DataException("jbr.DistributionManager.cards.elementlistmailing.sendingInfoAttribute.notFound");
			msgGostLnk = card.getCardLinkAttributeById(MESSAGE_GOST_ID);
			if (msgGostLnk == null)
				throw new DataException("jbr.DistributionManager.cards.elementlistmailing.msgGostLnk.notFound");
			recipientLnk = card.getCardLinkAttributeById(RECIPIENT);
			if (recipientLnk == null)
				throw new DataException("jbr.DistributionManager.cards.elementlistmailing.recipientLnk.notFound");
			lastAttempt = (IntegerAttribute)card.getAttributeById(LAST_ATEMPT_NUMBER);
			if (lastAttempt == null)
				throw new DataException("jbr.DistributionManager.cards.elementlistmailing.lastAttempt.notFound");
			messageCreationTime = (DateAttribute)card.getAttributeById(MESSAGE_CREATION_TIME);
			if (messageCreationTime == null)
				throw new DataException("jbr.DistributionManager.cards.elementlistmailing.messageCreationTime.notFound");
			ListAttribute modeDelivery = (ListAttribute)card.getAttributeById(MODE_DELIVERY);
			if (modeDelivery != null) {
				deliveryValue = modeDelivery.getValue();
			} else
				throw new DataException("jbr.DistributionManager.card.elementlistmailing.notModeDeliveryAttribute");
			ListAttribute msgType = (ListAttribute)card.getAttributeById(TYPE_MESSAGE);
			if (msgType != null) {
				msgTypeValue = msgType.getValue();
			} else
				throw new DataException("jbr.DistributionManager.card.elementlistmailing.notTypeMessageAttribute");
			TextAttribute processingResultAttr = (TextAttribute) card
					.getAttributeById(PROC_RESULT_ID);
			if (null != processingResultAttr)
				processingResult = processingResultAttr.getValue();
			else
				logger.warn("jbr.DistributionManager.card.elementlistmailing.notProcessingResultAttr");

		} else
		    throw new DataException("jbr.DistributionManager.cards.elementlistmailing.notFound");

		logger.info("Create object ELM with current parameters: "
			+ getParameterValuesLog());
	}

	/* (non-Javadoc)
     * @see com.aplana.DistributionManager.cards.CardHandler#getCardId()
     */
    public long getCardId() throws DataException {
    	if (card != null ) return (Long)card.getId().getId();
    	throw new DataException("jbr.DistributionManager.cards.elementlistmailing.notFound");
    }

    public Card getCard() throws DataException {
    	if (card != null)
    		return card;
    	throw new DataException("jbr.DistributionManager.cards.elementlistmailing.notFound");
    }

	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("UUID='%s', ", uid));
		logBuilder.append(String.format("id='%s', ", id));
		return logBuilder.toString();
	}

    /**
     * @return ���������� �� ��������
     */
    public CardLinkAttribute getSendingInfoAttribute() {
    	return sendingInfoAttribute;
    }

    public void addLinkedId(Long linkId, JdbcTemplate jdbc) throws DataException, ServiceException {
    	getSendingInfoAttribute().addLinkedId(linkId);
    	UtilsWorkingFiles.saveCardParent(card, serviceBean, sendingInfoAttribute, jdbc);
    }

	/**
     * @set WorkflowMove in PREPARE_SENT
     **/
    public void byPrepareSent(JdbcTemplate jdbc) throws ChangeStateException {
    	try {
			setMoveCard(PREPARE_SENT, jdbc);
		} catch (DataException e) {
			throw new ChangeStateException("jbr.DistributionManager.cards.elementlistmailing.DataException", e);
		} catch (ServiceException e) {
			throw new ChangeStateException("jbr.DistributionManager.cards.elementlistmailing.ServiceException", e);
		}
    }

    /**
     * @set WorkflowMove in PREPARE_NOTSENT
     **/
    public void byPrepareNotSent(JdbcTemplate jdbc) throws ChangeStateException {
    	try {
    		setMoveCard(PREPARE_NOTSENT, jdbc);
    	} catch (DataException e) {
			throw new ChangeStateException("jbr.DistributionManager.cards.elementlistmailing.DataException", e);
		} catch (ServiceException e) {
			throw new ChangeStateException("jbr.DistributionManager.cards.elementlistmailing.ServiceException", e);
		}
    }

    private void setMoveCard(ObjectId workflowMove, JdbcTemplate jdbc) throws DataException, ServiceException {
    	// �������� �������� ��� �������� ������������� ��������� ����� ��������� �������
    	//UtilsWorkingFiles.saveCardParent(card, serviceBean, jdbc);

    	// ��������� �������
    	try {
			serviceBean.doAction(new LockObject(card));
			try {
				final ChangeState move = new ChangeState(); // ��������
				// ��������-��������
				// ���
				// ��������
				move.setCard(card); // ���������� ��������,
				// ������� ����
				// ����������.
				move.setWorkflowMove((WorkflowMove) DataObject
						.createFromId(workflowMove));
				serviceBean.doAction(move);
			} finally {
				serviceBean.doAction(new UnlockObject(card));
			}
		} catch (Exception e) {
			throw new SaveCardException(
					"jbr.DistributionManager.cards.elementlistmailing.MoveCardELM",
					e);
		}

    	/*
		 * boolean wasLocked = false; // ���� �� ������������� UserData prevUser =
		 * serviceBean.getUser(); // ���������� �������� ����� if
		 * (card.isLocked()) { wasLocked = true; ObjectId lockerPerson =
		 * card.getLocker(); Person person =
		 * (Person)serviceBean.getById(lockerPerson); UserData userData = new
		 * UserData(); userData.setAddress("127.0.0.1");
		 * userData.setPerson(person); serviceBean.setUser(userData); } else {
		 * wasLocked = false; LockObject lock = new LockObject(card.getId());
		 * serviceBean.doAction(lock); } try { final ChangeState move = new
		 * ChangeState(); // �������� // ��������-�������� // ��� // ��������
		 * serviceBean.saveObject(card); move.setCard(card); // ����������
		 * ��������, // ������� ���� // ����������. move
		 * .setWorkflowMove((WorkflowMove) DataObject
		 * .createFromId(workflowMove)); serviceBean.doAction(move); } finally {
		 * if (!wasLocked) { UnlockObject unlock = new
		 * UnlockObject(card.getId()); serviceBean.doAction(unlock); } else {
		 * serviceBean.setUser(prevUser); // ���������� ��� ���� } }
		 */
    }

    public ObjectId findCardMessageGOST() throws FindMessageGostException {
    	if (msgGostLnk == null)
    		throw
    			new FindMessageGostException("jbr.DistributionManager.cards.elementlistmailing.messageGostLink.notFound");
    	ObjectId[] listMsgs = msgGostLnk.getIdsArray();
    	if (listMsgs == null || listMsgs.length == 0)
    		throw
    			new FindMessageGostException("jbr.DistributionManager.cards.elementlistmailing.messageGost.notFound");
    	return listMsgs[0];
    }

    public ObjectId getRecipientId() throws DataException {
    	if (recipientLnk == null)
    		throw new DataException("jbr.DistributionManager.cards.elementlistmailing.recipientLnk.notFound");
    	return (null == recipientLnk.getIdsArray())? null:recipientLnk.getIdsArray()[0];
    }

    public void deleteMsgGost(ObjectId msgGostId, NamedParameterJdbcTemplate paramJdbc, JdbcTemplate jdbc) throws DeleteMessageGostException {
    	try {
    		if (null == msgGostId)
    			throw new Exception("jbr.DistributionManager.cards.elementlistmailing.deleteMsgGost.messageGostId.isNull");


		serviceBean.doAction(new LockObject(card));
    		try {
    			// ���������� �� ��������� �������� ���� ���������
    	    	// � ������ ������, ��� ������� ���, ������ ��������� ��� - ����� 1 � 1.
    			msgGostLnk.clear(); // ������� ���� �� ��������� ����
    			// ��������� ��������� �������� � ��
    			final MapSqlParameterSource args = new MapSqlParameterSource();
    			args.addValue("CARD_ID", getCardId(), Types.NUMERIC);
    			args.addValue("ATTR_ID", MESSAGE_GOST_ID.getId(), Types.VARCHAR);
    			paramJdbc.update(CLEAR_ATTRIBUTE, args);
    			paramJdbc.update(CLEAR_ATTRIBUTE_HIST, args);
    			// ������ �������� ����� ��������� �� ��, ����� ����� ����������
    			card = (Card)serviceBean.getById(card.getId());
    			//serviceBean.saveObject(card);

    	    	// ������ ���� ��� ���������� ��������
    	    	//OverwriteCardAttributes overwriteAction = new OverwriteCardAttributes();
    	    	// ����� �� ������ ��� ������� ���������, �.�. ������� ��������,
    	    	// ���������� �������� �� �� �������� ��������.
    	    	//overwriteAction.setInsertOnly(false);
    	    	//overwriteAction.setCardId(card.getId());
    	    	//overwriteAction.setAttributes(Collections.singleton(msgGostLnk));
    	    	//serviceBean.doAction(overwriteAction);
    		} finally {
    			serviceBean.doAction(new UnlockObject(card));
    		}
    		// ��������� ���
    		UtilsWorkingFiles.saveCardParent(card, serviceBean, msgGostLnk, jdbc);

        	LockObject lockMsgGost = new LockObject(msgGostId);
	    	serviceBean.doAction(lockMsgGost); // ��������� ��������� ����
    		RemoveCard removeCard = new RemoveCard(); // ������ ���� ��� �������� ��������
    		removeCard.setCardId(msgGostId); // ����� �� ��������� ��������� ����
    		try {
    			serviceBean.doAction(removeCard); // ������� ��������� ����
    		} catch(Exception e) {
    			// � ������ ������ �������� ������������ ��������� ����
    			UnlockObject unlockMsgGost = new UnlockObject(msgGostId);
	    	    serviceBean.doAction(unlockMsgGost);
	    	    throw e;
    		}
    	} catch(Exception e) {
    		throw new DeleteMessageGostException(e);
    	}
    }

	@SuppressWarnings("unchecked")
	public Card findStateCardOnIntAttr(ObjectId attrId, int value) throws DataException, ServiceException {
		/*CardLinkAttribute a = (CardLinkAttribute) serviceBean.getById(SENDING_INFO_ATTRIBUTE_ID);
		ObjectId aId = a.getId();
		final Search searchByAttribute = new Search();
		searchByAttribute.addIntegerAttribute(attrId, value, value);
		searchByAttribute.addCardLinkAttribute(SENDING_INFO_ATTRIBUTE_ID, card.getId());
		searchByAttribute.setByAttributes(true);
		SearchResult stateCards = (SearchResult)serviceBean
	    	.doAction(searchByAttribute);
		List<Card> cards = stateCards.getCards();
		if (cards.size() > 1)
			logger.error(
					String.format("jbr.DistributionManager.cards.elementlistmailing.statecard.multipleFound ELM: {%s}",
							card.getId().getId()
					)
			);
		if (cards.size() == 0 ) {
			throw new DataException("jbr.DistributionManager.cards.elementlistmailing.statecard.notFound");
		}
		ObjectId cardId = cards.get(0).getId();
		Card cardState = (Card)serviceBean.getById(cardId); // ������, ����� ������� ������ ��������.*/
		if (sendingInfoAttribute == null)
    		throw
    			new DataException("jbr.DistributionManager.cards.elementlistmailing.stateCardLink.notFound");
    	ObjectId[] listStateCardId = sendingInfoAttribute.getIdsArray();
    	if (listStateCardId.length == 0)
    		throw
    			new DataException("jbr.DistributionManager.cards.elementlistmailing.stateCard.notFound");
    	// ���� ����� ��������, ��������������� ��������
    	int length = listStateCardId.length;
    	Card stateCard = null;
    	for (int i = length-1; i >= 0; i-- ){
    		// ������ � �������� �������, ��� ������� ����������� ���������� �� ��������� ��������,
    		// �� � ����� ������ ������� ������� �� ���������� � ��
    		ObjectId stateCardId = listStateCardId[i];
    		stateCard = (Card) serviceBean.getById(stateCardId);
    		IntegerAttribute iterationNumber = (IntegerAttribute)stateCard.getAttributeById(attrId);
    		if (null == iterationNumber)
    			continue;
    		if (iterationNumber.getValue() == value)
    			// ���
    			break;
    	}
    	if (null == stateCard)
    		throw
				new DataException("jbr.DistributionManager.cards.elementlistmailing.stateCard.in.CardLink.notFound");
		return stateCard;
	}

	public int getLastAttempt() {
		return lastAttempt.getValue();
	}

    /**
     * @return ������� �� ��� id (DataObject)
     */
	public DataObject getAttributeById(ObjectId idObject) {
		return card.getAttributeById(idObject);
	}

	/**
     * @return uuid (String)
     */
	public String getUid() {
		return uid;
	}

	public Date getMessageCreationTime() {
		return messageCreationTime.getValue();
	}

	public DateAttribute getMessageCreationTimeAttr() {
		return messageCreationTime;
	}

	public void setMessageCreationTime(Date messageCreationTime) {
		this.messageCreationTime.setValue(messageCreationTime);
	}

	public ReferenceValue getDeliveryValue() {
		return deliveryValue;
	}

	public ReferenceValue getMsgTypeValue() {
		return msgTypeValue;
	}

	 public String getProcessingResult() {
		return processingResult;
	}
}
