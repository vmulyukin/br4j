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
package com.aplana.medo.cards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * @author PPanichev
 *
 */
public class InBox extends ExportCardHandler {
    
    public static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.incoming");
    
    public static final ObjectId REGISTRATION = ObjectId.predefined(
	    CardState.class, "registration");
    public static final ObjectId TRASH = ObjectId.predefined(
	    CardState.class, "trash");
    
    public static final ObjectId MODE_DELIVERY_MEDO = ObjectId.predefined(
	    ReferenceValue.class, "modeDeliveryMEDO");
    public static final ObjectId DELIVERY_ITEM_METHOD = ObjectId.predefined(
	    ListAttribute.class, "jbr.deliveryItem.method");
    
    private static final ObjectId ORIGINAL_SOURCE_ATTRIBUTE_ID = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.original.source");
    private static final ObjectId INCOMING_SENDER = ObjectId
    .predefined(CardLinkAttribute.class, "jbr.incoming.sender");
    
    public static final ObjectId REG_NUMBER = ObjectId.predefined(
	    StringAttribute.class, "regnumber");
    public static final ObjectId SHORT_DESCRIPTION = ObjectId.predefined(
	    TextAttribute.class, "jbr.document.title");
    public static final ObjectId DATE_CREATED = ObjectId.predefined(
	    DateAttribute.class, "created");
    public static final ObjectId DATE_REGISTRATION = ObjectId.predefined(
	    DateAttribute.class, "regdate");
    public static final ObjectId DOC_NUMBER = ObjectId.predefined(
	    StringAttribute.class, "jbr.incoming.outnumber");
    public static final ObjectId DATE_DOC = ObjectId.predefined(
	    DateAttribute.class, "jbr.incoming.outdate");
    public static final ObjectId MEDO_TYPE = ObjectId.predefined(
	    StringAttribute.class, "jbr.medo.type");
    
    private CardLinkAttribute original_source = null;
    private CardLinkAttribute sender = null;
    
    private String id = null;
    private TextAttribute theme = null;
    private StringAttribute regnumber = null;
    private DateAttribute date_registration = null;
    private StringAttribute docnumber = null;
    private DateAttribute date_doc = null;
    
    public InBox(ObjectId card_id) throws DataException,
    ServiceException {
	serviceBean = getServiceBean();
	card = (Card) serviceBean.getById(card_id);
	if (card != null) {
	    id = card.getId().getId().toString();
	    original_source = (CardLinkAttribute) card
		.getAttributeById(ORIGINAL_SOURCE_ATTRIBUTE_ID);
	    theme = (TextAttribute)card.getAttributeById(SHORT_DESCRIPTION);
	    if (theme.getValue() == null) {
		theme.setValue("");
	    }
	    sender = (CardLinkAttribute)card.getAttributeById(INCOMING_SENDER);
	    regnumber = (StringAttribute)card.getAttributeById(REG_NUMBER);
	    if (regnumber.getValue() == null) {
		    regnumber.setValue("0");
	    }
	    date_registration = (DateAttribute)card.getAttributeById(DATE_REGISTRATION);
	    if (date_registration.getValue() == null) {
		date_registration = (DateAttribute) card
		    .getAttributeById(DATE_CREATED);
		} 
	    docnumber = (StringAttribute)card.getAttributeById(DOC_NUMBER);
	    if (docnumber.getValue() == null) {
		docnumber.setValue("0");
	    }
	    date_doc = (DateAttribute)card.getAttributeById(DATE_DOC);
	    if (date_doc.getValue() == null) {
		date_doc = (DateAttribute) card
		    .getAttributeById(DATE_CREATED);
		} 
	} else
	    throw new CardException("jbr.medo.card.inbox.notFound");
	logger.info("Create object InBox with current parameters: "
		+ getParameterValuesLog());
    }

    /* (non-Javadoc)
     * @see com.aplana.medo.cards.ExportCardHandler#getCardId()
     */
    @Override
    public long getCardId() throws CardException {
	if (card != null ) return (Long)card.getId().getId();
	throw new CardException("jbr.medo.card.inbox.notFound");
    }
    
    /**
     * @return the id
     */
    public String getId() {
	return id;
    }
    
    public Card getCard() throws CardException {
	if (card != null)
	return card;
	throw new CardException("jbr.medo.card.inbox.notFound");
    }

    /* (non-Javadoc)
     * @see com.aplana.medo.cards.ExportCardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(String.format("id='%s', ", id));
	return logBuilder.toString();
    }
    
    /**
     * @return ��������������� ��������
     */
    public CardLinkAttribute getSourcesAttribute() {
	return original_source;
    }
    
    /**
     * @return the regnumber
     */
    public StringAttribute getRegNumber() {
        return this.regnumber;
    }
    
    /**
     * @return the date_registration
     */
    public DateAttribute getDateRegistration() {
        return this.date_registration;
    }
    
    /**
     * @return the docnumber
     */
    public StringAttribute getDocNumber() {
        return this.docnumber;
    }
    
    /**
     * @return the date_doc
     */
    public DateAttribute getDocDate() {
        return this.date_doc;
    }
    
    /**
     * @return the theme
     */
    public TextAttribute getTheme() {
        return this.theme;
    }
    
    /**
     * @return the sender
     */
    public CardLinkAttribute getSender() {
        return this.sender;
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
		.createFromId(MODE_DELIVERY_MEDO);

	states.add(REGISTRATION.getId().toString());
	states.add(TRASH.getId().toString());
	search_state.setStates(states);
	search_state.setTemplates(Collections.singleton(DataObject.createFromId(TEMPLATE_ID)));
	Collection<ReferenceValue> medoValues = Collections
		.singletonList(refVal);
	search_state.addListAttribute(DELIVERY_ITEM_METHOD, medoValues);
	search_state.addStringAttribute(MEDO_TYPE);
	search_state.setWords("�� ����������");
	search_state.setByAttributes(true);
	try {
	    @SuppressWarnings("unchecked")
	    SearchResult cardsSR = (SearchResult)serviceBeanStatic
		    .doAction(search_state);
	    Collection<Card> cards = cardsSR.getCards();
	    return cards;
	} catch (DataException ex) {
	    throw new CardException("jbr.medo.card.inbox.searchFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new CardException("jbr.medo.card.inbox.searchFailed",
		    ex);
	}
    }
    
    public void setAttributeCard(StringAttribute attribute, String value) throws DataException, ServiceException {
	LockObject lock = new LockObject(card.getId());
	serviceBean.doAction(lock);
	try {
	    attribute.setValue(value); 
	    serviceBean.saveObject(card);
	} finally {
	    UnlockObject unlock = new UnlockObject(
		    card.getId());
	    serviceBean.doAction(unlock);
	}
    }
    
}
