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
/**
 * 
 */
package com.aplana.medo.cards;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

import java.util.*;

/**
 * @author PPanichev
 *
 */
public class ElementListMailing  extends ExportCardHandler {
    
    public static final ObjectId TEMPLATE_ELM_ID = ObjectId.predefined(
	    Template.class, "jbr.DistributionListElement");
    
    public static final ObjectId COUNT_ATTEMPT_DELIVERY = ObjectId.predefined(
	    IntegerAttribute.class, "countAttemptDELIVERY");
    public static final ObjectId LAST_ATTEMPT_DELIVERY = ObjectId.predefined(
	    IntegerAttribute.class, "lastAttemptDELIVERY");
    public static final ObjectId LAST_TIME_DELIVERY = ObjectId.predefined(
	    DateAttribute.class, "lastTimeDELIVERY");
    public static final ObjectId SENDING_INFO_ATTRIBUTE_ID = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.distributionItem.processing");
    
    public static final ObjectId MODE_MEDO = ObjectId.predefined(
	    ReferenceValue.class, "modeMEDO");
    public static final ObjectId MODE_DELIVERY = ObjectId.predefined(
	    ListAttribute.class, "jbr.distributionItem.method");
    
    public static final ObjectId PREPARE_DELIVERY = ObjectId.predefined(
	    CardState.class, "prepareDELIVERY");
    public static final ObjectId SUCCESSFULLY_DELIVERY = ObjectId.predefined(
	    CardState.class, "sent");
    public static final ObjectId ERROR_DELIVERY = ObjectId.predefined(
	    CardState.class, "jbr.distributionItem.notSent");
    
    public static final ObjectId PREPARE_SENT = ObjectId.predefined(
	    WorkflowMove.class, "prepare.sent");
    public static final ObjectId PREPARE_NOTSENT = ObjectId.predefined(
	    WorkflowMove.class, "prepare.notsent");
    
    public static final ObjectId BASE_DOC = ObjectId.predefined(BackLinkAttribute.class,"jbr.distributionItem.foundationDoc");
    
    private static Properties optionsSt = null;
    
    private String last_interval = null;	// �� ���������,
						// ���������
						// �������� ��
						// ��������� (24
						// ���� ��� itr = 5)   
    
    private String uid = null;
    private String id = null;
    private Integer countRepeat = null; // ����� ������� (5 ��������
					// �����. � ��������) - ��������

    private Integer lastRepeat = null;	// ����� ��������� �������
    private Date lastDate = null;	// ����� ��������� �������
    private String iterator = null;
    private CardLinkAttribute sendingInfoAttribute = null; // ���������� �� ��������
    private CardLinkAttribute recipientAttribute = null;
    private BackLinkAttribute foundationDocAttribute = null;

    public ElementListMailing(ObjectId card_id) throws DataException,
	    ServiceException {
	serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    if (optionsSt == null) throw new CardException("jbr.medo.card.elementlistmailing.notOptions");
	    // Generate UID
	    StringAttribute uidAttribute = (StringAttribute) card.getAttributeById(DistributionItemCardHandler.UUID_ATTRIBUTE_ID);
	    if (uidAttribute != null) {
	    	uid = uidAttribute.getValue();
	    } else {
	    	throw new CardException("jbr.medo.card.elementlistmailing.notUUIDAttribute");
	    }

	    if (uid == null || "".equals(uid)) {
			uid = UUID.randomUUID().toString();
			uidAttribute.setValue(uid);
			saveCard();
	    }

	    id = card.getId().getId().toString();
	    try {
	    	lastRepeat = ((IntegerAttribute) card.getAttributeById(LAST_ATTEMPT_DELIVERY)).getValue();
	    } finally {
			if (lastRepeat == null) {
			    lastRepeat = 0;
			}
	    }
	    try {
	    	countRepeat = ((IntegerAttribute) card.getAttributeById(COUNT_ATTEMPT_DELIVERY)).getValue();
	    } finally {
			if (countRepeat == null) {
			    countRepeat = 0;
			}
	    }
	    try {
	    	lastDate = ((DateAttribute) card.getAttributeById(LAST_TIME_DELIVERY)).getValue();
	    } finally {
			if (lastDate == null) {
			    lastDate = new GregorianCalendar().getTime();
			}
	    }
	    last_interval = optionsSt.getProperty("iteratorLAST", "24");
	    iterator = optionsSt.getProperty("iterator" + lastRepeat, last_interval); // �� ��������� ��������� �������� �� ���������
	    recipientAttribute = (CardLinkAttribute) card.getAttributeById(DistributionItemCardHandler.RECIPIENT_ATTRIBUTE_ID);
	    foundationDocAttribute = (BackLinkAttribute) card.getAttributeById(BASE_DOC);
	    sendingInfoAttribute = card.getCardLinkAttributeById(SENDING_INFO_ATTRIBUTE_ID);
	} else
	    throw new CardException("jbr.medo.card.elementlistmailing.notFound");
	logger.info("Create object ELM with current parameters: "
		+ getParameterValuesLog());
    }
    
    /* (non-Javadoc)
     * @see com.aplana.medo.cards.CardHandler#getCardId()
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
	throw new CardException("jbr.medo.card.elementlistmailing.notFound");
    }
    
    public Card getCard() throws CardException {
	if (card != null)
	return card;
	throw new CardException("jbr.medo.card.elementlistmailing.notFound");
    }
    
    public static void setOptions(Properties options) {
	optionsSt = options;
    }
    
    /*public void setCard(ObjectId card_id) throws DataException, ServiceException {
	card = (Card)serviceBean.getById(card_id);
    }*/
    
    /**
     * @return the uid
     */
    public String getUid() {
	return this.uid;
    }
    
    /**
     * @return the id
     */
    public String getId() {
	return id;
    }
    
    /**
     * @return ����� ��������
     */
    public Integer getLastRepeat() {
	return lastRepeat;
    }
    
    public void setLastRepeat(Integer lastRepeat) throws DataException, ServiceException {
	this.lastRepeat = lastRepeat;
	((IntegerAttribute) card
	.getAttributeById(LAST_ATTEMPT_DELIVERY)).setValue(this.lastRepeat);
	saveCard();
    }
    
    public void setLastRepeatAndLastDate(Integer lastRepeat, Date lastDate) throws DataException, ServiceException {
    	this.lastRepeat = lastRepeat;
    	this.lastDate = lastDate;
    	((IntegerAttribute) card
    	.getAttributeById(LAST_ATTEMPT_DELIVERY)).setValue(this.lastRepeat);
    	((DateAttribute) card.getAttributeById(LAST_TIME_DELIVERY))
    		.setValue(this.lastDate);
    	saveCard();
        }

    /**
     * @return ���������� ��������
     */
    public Integer getCountRepeat() {
	return countRepeat;
    }
    
    /**
     * @return ���� ���������� ��������������
     */
    public Date getLastDate() {
	return lastDate;
    }
    
    public void setLastDate(Date lastDate) throws DataException,
	    ServiceException {
	this.lastDate = lastDate;
	((DateAttribute) card.getAttributeById(LAST_TIME_DELIVERY))
		.setValue(this.lastDate);
	saveCard();
    }
    
    /**
     * @return ������ �������� ���������
     */
    public String getItrPeriod() {
	return iterator;
    }
    
    /**
     * @return ����������
     */
    public CardLinkAttribute getRecipientAttribute() {
	return recipientAttribute;
    }
    
    /**
     * @return ���������� �� ��������
     */
    public CardLinkAttribute getSendingInfoAttribute() {
	return sendingInfoAttribute;
    }
    
    public void addLinkedId(Long linkId) throws DataException, ServiceException {
	getSendingInfoAttribute().addLinkedId(linkId);
	saveCard();
    }
    
    /**
     * @return Foundation Document
     **/
    public BackLinkAttribute getFoundationDocAttribute() {
	return foundationDocAttribute;
    }
    
    /**
     * @set WorkflowMove in PREPARE_SENT
     **/
    public void byPrepareSent() throws DataException, ServiceException {
	setMoveCard(PREPARE_SENT);
    }
    
    /**
     * @set WorkflowMove in PREPARE_NOTSENT
     **/
    public void byPrepareNotSent() throws DataException, ServiceException {
	setMoveCard(PREPARE_NOTSENT);
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.aplana.medo.cards.ExportCardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("UUID='%s', ", uid));
	logBuilder.append(String.format("id='%s', ", id));
	logBuilder.append(String.format("countRepeat='%s', ", countRepeat));
	logBuilder.append(String.format("lastRepeat='%s'", lastRepeat));
	logBuilder.append(String.format("lastDate='%s'", lastDate));
	return logBuilder.toString();
    }
    
    public static Collection<Card> findCards() throws CardException {
	Collection<Card> cards = search();
	if (cards == null) {
	    cards = new ArrayList<Card>();
	}
	loggerSt.info(String.format("There was found %d cards", cards.size()));
	return cards;
    }

    @SuppressWarnings("unchecked")
    private static Collection<Card> search() throws CardException {
	
	serviceBeanStatic = getServiceBeanStatic();
	final Search search_state = new Search();
	final List<String> states = new ArrayList<String>(1);
	final ReferenceValue refVal = (ReferenceValue) DataObject
		.createFromId(MODE_MEDO);

	states.add(PREPARE_DELIVERY.getId().toString());
	search_state.setStates(states);
	search_state.setTemplates(Collections.singleton(DataObject.createFromId(TEMPLATE_ELM_ID)));
	Collection<ReferenceValue> medoValues = Collections
		.singletonList(refVal);
	search_state.addListAttribute(MODE_DELIVERY, medoValues);
	search_state.setByAttributes(true);

	final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
	search_state.setColumns(columns);
	// 1� ������� - LAST_ATTEMPT_DELIVERY
	final SearchResult.Column colAttempt = new SearchResult.Column();
	columns.add(colAttempt);
	colAttempt.setAttributeId(LAST_ATTEMPT_DELIVERY);
	// ***//
	// 2� ������� - COUNT_ATTEMPT_DELIVERY
	final SearchResult.Column colCount = new SearchResult.Column();
	columns.add(colCount);
	colCount.setAttributeId(COUNT_ATTEMPT_DELIVERY);
	// ***//
	// 3� ������� - LAST_TIME_DELIVERY
	final SearchResult.Column colLastTime = new SearchResult.Column();
	columns.add(colLastTime);
	colLastTime.setAttributeId(LAST_TIME_DELIVERY);
	// ***//
	try {
	    @SuppressWarnings("unchecked")
	    SearchResult cardsSR = (SearchResult)serviceBeanStatic
		    .doAction(search_state);
	    Collection<Card> cards = cardsSR.getCards();
	    return cards;
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.elementlistmailing.searchFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.elementlistmailing.searchFailed",
		    ex);
	}
    }
    
    private void setMoveCard(ObjectId workflowMove) throws DataException, ServiceException {
	LockObject lock = new LockObject(card.getId());
	serviceBean.doAction(lock);
	try {
	    final ChangeState move = new ChangeState();
	    serviceBean.saveObject(card);
		card = serviceBean.getById(card.getId());
	    move.setCard(card);
		move.setWorkflowMove((WorkflowMove) DataObject.createFromId(workflowMove));
	    serviceBean.doAction(move);
		card = serviceBean.getById(card.getId());
	} finally {
	    UnlockObject unlock = new UnlockObject(
		    card.getId());
	    serviceBean.doAction(unlock);
	}
    }

}
