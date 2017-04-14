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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * One of <code>CardHandler</code> that operates with 'Distribution item'
 * cards.
 */
public class DistributionItemCardHandler extends CardHandler {

    private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
	    Template.class, "jbr.DistributionListElement");

    public static final ObjectId UUID_ATTRIBUTE_ID = ObjectId.predefined(
	    StringAttribute.class, "jbr.distributionItem.uuid");

    protected static final ObjectId SEND_INFO_ATTRIBUTE_ID = ObjectId
	    .predefined(CardLinkAttribute.class,
		    "jbr.distributionItem.sendInfo");
    protected static final ObjectId METHOD_ATTRIBUTE_ID = ObjectId.predefined(
	    ListAttribute.class, "jbr.distributionItem.method");
    public static final ObjectId RECIPIENT_ATTRIBUTE_ID = ObjectId.predefined(
	    CardLinkAttribute.class, "jbr.distributionItem.recipient");
    protected static final ObjectId FOUNDATION_DOC_ATTRIBUTE_ID = ObjectId
	    .predefined(BackLinkAttribute.class,
		    "jbr.distributionItem.foundationDoc");

    protected static final ObjectId MEDO_METHOD_ID = ObjectId.predefined(
	    ReferenceValue.class, "jbr.distributionItem.method.medo");

    private enum ClassState {
	UNDEFINED, BY_UID, BY_OUTCOME_CARD
    };

    private ClassState classState = ClassState.UNDEFINED;

    private UUID uid = null;

    private Long outcomeCardId = null;
    private Long recipientId = null;

    public DistributionItemCardHandler(long cardId) {
	super(cardId);
    }

    /**
     * Creates instance of class that allows to search cards that linked to
     * given 'Outcome' card and has given recipient
     *
     * @param outcomeId -
     *                id of 'Outcome' card
     * @param recipientId -
     *                id of recipient
     */
    public DistributionItemCardHandler(Long outcomeId, Long recipientId) {
	setOutcomeCard(outcomeId);
	setRecipientId(recipientId);
    }

    /**
     * Creates instance of class that allows to search cards by UUID.
     *
     * @param uid -
     *                UID of card
     */
    public DistributionItemCardHandler(UUID uid) {
	setUid(uid);
    }

    /**
     * <p>
     * Method is used in the {@link #getCardId()} and implements behavior of
     * card id calculating.
     * </p>
     * <p>
     * The following behavior is implemented:
     * <ol>
     * <li>Searches cards according to current state</li>
     * <li>If there were found more than one cards returns ID of last of them</li>
     * </ol>
     * </p>
     *
     * @return ID of last of found cards or -1 if cards were not found
     * @throws CardException
     */
    @Override
    protected long calculateCardId() throws CardException {
	List<ObjectId> cards = findCards();

	if (cards.size() > 1) {
	    logger.warn("There was found more than one card");
	}

	if (!cards.isEmpty()) {
	    return (Long) cards.get(cards.size() - 1).getId();
	}

	return -1;
    }

    /**
     * Links notification with card ID equals to <code>notificationId</code>
     * to current card (defined by {@link #getCardId()}).
     *
     * @param notificationId -
     *                ID of notification card
     * @throws CardException
     * @see {@link #updateAttribute(ObjectId, Object, String)}
     */
    public void appendNotification(long notificationId) throws CardException {
	logger.info("Trying to add notification card");
	if (notificationId < 0) {
	    logger.error(
		    "Error during linking notification to distribution item",
		    new IllegalArgumentException(
			    "Notification card ID should be positive number"));
	    throw new CardException();
	}
	updateAttribute(SEND_INFO_ATTRIBUTE_ID, notificationId,
		"jbr.medo.card.distributionItem.appendFailed");
    }

    /**
     * Links ticket with card ID equals to <code>ticketId</code> to foundation
     * doc of current card (defined by {@link #getCardId()}). Foundation card
     * is defined according to {@link #FOUNDATION_DOC_ATTRIBUTE_ID} backlink
     * attribute
     *
     * @param ticketId -
     *                ID of ticket card
     * @throws CardException
     * @see {@link #updateAttribute(ObjectId, Object, String)}
     */
    public void appendTicketToParentDoc(long ticketId) throws CardException {
	if (ticketId < 0) {
	    logger.error("Error during linking ticket to parent doc",
		    new IllegalArgumentException(
			    "Ticket card ID should be positive number"));
	    throw new CardException();
	}
	DataServiceBean serviceBean = getServiceBean();
	ListProject listProjectAction = new ListProject(new ObjectId(
		Card.class, getCardId()));
	listProjectAction.setAttribute(FOUNDATION_DOC_ATTRIBUTE_ID);
	try {
	    SearchResult result = (SearchResult) serviceBean
		    .doAction(listProjectAction);
	    @SuppressWarnings("unchecked")
	    List<Card> cards = result.getCards();
	    if (cards.size() != 1) {
		    logger.error("Error during linking doc to ticket",
			    new IllegalArgumentException("There should be exactly one paren doc in notification"));
		    throw new CardException("jbr.medo.card.distributionItem.ticketAppendFailed");
		}

		new TicketCardHandler(ticketId)
			.updateAttribute(TicketCardHandler.TICKET_DOC_BASE_ATTRIBUTE_ID,
				cards.get(0).getId(),
				"jbr.medo.card.distributionItem.ticketAppendFailed");

	} catch (CardException ex) {
	    throw ex;
	} catch (DataException ex) {
	    throw new CardException(
		    "jbr.medo.card.distributionItem.ticketAppendFailed", ex);
	} catch (ServiceException ex) {
	    throw new CardException(
		    "jbr.medo.card.distributionItem.ticketAppendFailed", ex);
	}
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
	logBuilder.append(String.format("UUID='%s', ", uid));
	logBuilder.append(String.format("outcomeCardId='%s', ", outcomeCardId));
	logBuilder.append(String.format("recipientId='%s'", recipientId));
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

	switch (classState) {
	case UNDEFINED:
	    throw new CardException(
		    "Search source is not defined. Use one of setters");
	case BY_UID:
	    StrictSearch uidSearch = new StrictSearch();
	    uidSearch.setTemplates(Collections.singleton(DataObject
		    .createFromId(TEMPLATE_ID)));
	    uidSearch.addStringAttribute(UUID_ATTRIBUTE_ID, uid.toString(), false);
	    try {
		@SuppressWarnings("unchecked")
		List<ObjectId> cards = (List<ObjectId>) serviceBean
			.doAction(uidSearch);
		return cards;
	    } catch (DataException ex) {
		throw new CardException(
			"jbr.medo.card.distributionItem.searchFailed", ex);
	    } catch (ServiceException ex) {
		throw new CardException(
			"jbr.medo.card.distributionItem.searchFailed", ex);
	    }
	case BY_OUTCOME_CARD:
	    // FIXME: (N.Zhegalin) Probably search should be realized by Search
	    try {
		List<ObjectId> result = new ArrayList<ObjectId>();
		Card outcomeCard = (Card) serviceBean.getById(new ObjectId(
			Card.class, outcomeCardId));
		CardLinkAttribute attr = (CardLinkAttribute) outcomeCard
			.getAttributeById(OutcomeCardHandler.DISTRIBUTION_LIST_ATTRIBUTE_ID);
		ObjectId[] distributionList = attr.getIdsArray();
		if (distributionList == null)
		    return result;
		for (ObjectId distributionItemId : distributionList) {
		    Card distributionItem = (Card) serviceBean
			    .getById(distributionItemId);
		    ListAttribute methodAttribute = (ListAttribute) distributionItem
			    .getAttributeById(METHOD_ATTRIBUTE_ID);
		    ReferenceValue reference = methodAttribute.getValue();
		    if (reference == null
			    || !reference.getId().equals(MEDO_METHOD_ID)) {
			continue;
		    }
		    CardLinkAttribute recipient = (CardLinkAttribute) distributionItem
			    .getAttributeById(RECIPIENT_ATTRIBUTE_ID);
		    ObjectId[] recipIds = recipient.getIdsArray();
		    if (recipIds.length > 0) {
			if (recipIds[0].getId().equals(recipientId)) {
			    result.add(distributionItemId);
			}
		    }
		}
		return result;
	    } catch (DataException ex) {
		throw new CardException(
			"jbr.medo.card.distributionItem.searchFailed", ex);
	    } catch (ServiceException ex) {
		throw new CardException(
			"jbr.medo.card.distributionItem.searchFailed", ex);
	    }
	}
	return new ArrayList<ObjectId>();
    }

    private void resetState() {
	this.uid = null;
	setId(null);
	this.outcomeCardId = null;
	classState = ClassState.UNDEFINED;
    }

    /**
     * @return the uid
     */
    public UUID getUid() {
	return this.uid;
    }

    /**
     * @param uid
     *                the uid to set
     */
    public void setUid(UUID uid) {
	resetState();
	this.uid = uid;
	classState = ClassState.BY_UID;
    }

    /**
     * @return the outcomeCardId
     */
    public Long getOutcomeCard() {
	return this.outcomeCardId;
    }

    /**
     * @param outcomeCardId
     *                the outcomeCardId to set
     */
    public void setOutcomeCard(Long outcomeCard) {
	Long tempRecipientId = this.recipientId;
	resetState();
	this.outcomeCardId = outcomeCard;
	this.recipientId = tempRecipientId;
	classState = ClassState.BY_OUTCOME_CARD;
    }

    /**
     * @return the recipientId
     */
    public Long getRecipientId() {
	return this.recipientId;
    }

    /**
     * @param recipientId
     *                the recipientId to set
     */
    public void setRecipientId(Long recipientId) {
	Long tempOutcomeCardId = this.outcomeCardId;
	resetState();
	this.outcomeCardId = tempOutcomeCardId;
	this.recipientId = recipientId;
	classState = ClassState.BY_OUTCOME_CARD;
    }

}
