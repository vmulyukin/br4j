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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that operates with 'Notification' cards:
 * type of exact card is defined by #{@link NotificationType}.
 */
public class NotificationCardHandler extends CardHandler {

    protected final static ObjectId DOC_ACCEPTED_TEMPLATE_ID = ObjectId
	    .predefined(Template.class, "med.documentRegister");
    protected final static ObjectId DOC_REFUSED_TEMPLATE_ID = ObjectId
	    .predefined(Template.class, "med.registrDenied");
    protected final static ObjectId EXECUTOR_ASSIGNED_TEMPLATE_ID = ObjectId
	    .predefined(Template.class, "med.resolutionAccept");
    protected final static ObjectId REPORT_SENT_TEMPLATE_ID = ObjectId
	    .predefined(Template.class, "med.reportSent");

    protected final static ObjectId FOUNDATION_DOC_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class, "medo.doc.parent");
    protected final static ObjectId FOUNDATION_IN_DOC_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class, "medo.doc.parentForIn");

    public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "medo.notification.UID");

    public static final ObjectId TICKET_ATTRIBUTE_ID = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.incoming.notification.ticket");

    public enum NotificationType {
	ALL, DOC_ACCEPTED, DOC_REFUSED, EXECUTOR_ASSIGNED, REPORT_SENT
    };

    private UUID uid = null;

    private NotificationType notificationType = NotificationType.ALL;

    /**
     * Creates instance that allows to find cards by its ID.
     *
     * @param id -
     *                ID of card
     * @see CardHandler#CardHandler(Long)
     */
    public NotificationCardHandler(Long id) {
	super(id);
    }

    /**
     * Creates instance that allows to find cards by UID - attribute defined by
     * {@link #UUID_ATTRIBUTE_ID} object ID.
     *
     * @param uid -
     *                required UUID of card
     */
    public NotificationCardHandler(UUID uid) {
	this.uid = uid;
    }

    /**
     * Links ticket with card ID equals to <code>cardId</code> to current card
     * (defined by {@link #getCardId()}).
     *
     * @param cardId -
     *                ID of ticket card
     * @throws CardException
     * @see {@link #updateAttribute(ObjectId, Object, String)}
     */
    public void linkTicket(long cardId) throws CardException {
	logger.info("Trying to add ticket card");
	if (cardId < 0) {
	    logger.error("Error during linking ticket to notification card",
		    new IllegalArgumentException(
			    "Ticket card ID should be positive number"));
	    throw new CardException(
		    "jbr.medo.card.notification.ticketAppendFailed");
	}
	updateAttribute(TICKET_ATTRIBUTE_ID, cardId,
		"jbr.medo.card.notification.ticketAppendFailed");
    }

    /**
     * Links ticket with card ID equals to <code>ticketId</code> to foundation
     * doc of current card (defined by {@link #getCardId()}). Foundation card
     * is defined according to {@link #FOUNDATION_DOC_ATTRIBUTE_ID} backlink
     * attribute
     *
     * @param ticketId
     * @throws CardException
     * @see {@link #updateAttribute(ObjectId, Object, String)}
     */
    public void linkParentDocToTicket(long ticketId) throws CardException {
	DataServiceBean serviceBean = getServiceBean();
	try {
		Card notification = (Card) serviceBean.getById(new ObjectId(Card.class, getCardId()));
		CardLinkAttribute foundationDoc = notification.getCardLinkAttributeById(FOUNDATION_DOC_ATTRIBUTE_ID);
		List<ObjectId> foundationDocIds = foundationDoc.getIdsLinked();
		if (foundationDocIds.size() != 1) {
		    logger.error("Error during linking doc to ticket",
			    new IllegalArgumentException("There should be exactly one paren doc in notification"));
		    throw new CardException("jbr.medo.card.notification.ticketAppendToBaseDocFailed");
		}
		new TicketCardHandler(ticketId).updateAttribute(TicketCardHandler.TICKET_DOC_BASE_ATTRIBUTE_ID, foundationDocIds.get(0),
				"jbr.medo.card.notification.ticketAppendToBaseDocFailed");
	} catch (CardException ex) {
	    throw ex;
	} catch (DataException ex) {
	    throw new CardException(
		    "jbr.medo.card.notification.ticketAppendToBaseDocFailed",
		    ex);
	} catch (ServiceException ex) {
	    throw new CardException(
		    "jbr.medo.card.notification.ticketAppendToBaseDocFailed",
		    ex);
	}
    }

    public void linkIncomeDocument(long docId) throws CardException {
    	logger.info("Trying to link base doc to notification card");
    	if (docId < 0) {
    	    logger.error(
    		    "Error during linking base document to notification",
    		    new IllegalArgumentException(
    			    "Document card ID should be positive number"));
    	    throw new CardException();
    	}
    	updateAttribute(FOUNDATION_IN_DOC_ATTRIBUTE_ID, new ObjectId(Card.class, docId), "jbr.medo.card.incomeDocument.appendFailed");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
     */
    @Override
    protected String getParameterValuesLog() {
	StringBuilder logBuilder = new StringBuilder();
	logBuilder.append(super.getParameterValuesLog());
	logBuilder.append(String.format("uid='%s', ", uid));
	return logBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.medo.cards.CardHandler#search()
     */
    @Override
    protected List<ObjectId> search() throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	if (uid != null) {
	    Search uidSearch = new Search();
	    Collection<Template> templates = new ArrayList<Template>();
	    switch (notificationType) {
	    case ALL:
		templates.add((Template) DataObject
			.createFromId(DOC_ACCEPTED_TEMPLATE_ID));
		templates.add((Template) DataObject
			.createFromId(DOC_REFUSED_TEMPLATE_ID));
		templates.add((Template) DataObject
			.createFromId(EXECUTOR_ASSIGNED_TEMPLATE_ID));
		templates.add((Template) DataObject
			.createFromId(REPORT_SENT_TEMPLATE_ID));
		break;
	    case DOC_ACCEPTED:
		templates.add((Template) DataObject
			.createFromId(DOC_ACCEPTED_TEMPLATE_ID));
		break;
	    case DOC_REFUSED:
		templates.add((Template) DataObject
			.createFromId(DOC_REFUSED_TEMPLATE_ID));
		break;
	    case EXECUTOR_ASSIGNED:
		templates.add((Template) DataObject
			.createFromId(EXECUTOR_ASSIGNED_TEMPLATE_ID));
		break;
	    case REPORT_SENT:
		templates.add((Template) DataObject
			.createFromId(REPORT_SENT_TEMPLATE_ID));
		break;
	    }
	    uidSearch.setTemplates(templates);
	    uidSearch.addStringAttribute(UUID_ATTRIBUTE_ID);
	    uidSearch.setWords(uid.toString());
	    uidSearch.setByAttributes(true);
	    try {
		SearchResult result = (SearchResult) serviceBean
			.doAction(uidSearch);
		@SuppressWarnings("unchecked")
		List<Card> cards = result.getCards();
		List<ObjectId> cardIds = new ArrayList<ObjectId>();
		for (Card card : cards) {
		    cardIds.add(card.getId());
		}
		return cardIds;
	    } catch (DataException ex) {
		throw new CardException(
			"jbr.medo.card.notification.searchFailed", ex);
	    } catch (ServiceException ex) {
		throw new CardException(
			"jbr.medo.card.notification.searchFailed", ex);
	    }
	}
	return new ArrayList<ObjectId>();
    }

    /**
     * @return the notificationType
     */
    public NotificationType getNotificationType() {
	return this.notificationType;
    }

    /**
     * @param notificationType
     *                the notificationType to set
     */
    public void setNotificationType(NotificationType notificationType) {
	this.notificationType = notificationType;
    }

    /**
     * Saves current card without attributes modification. Can be used to force
     * card saving processors.
     *
     * @throws CardException
     */
    public void saveCard() throws CardException {
	updateAttributes(new HashMap<ObjectId, Object>(),
		"jbr.medo.card.notification.saveFailed");
    }
}
