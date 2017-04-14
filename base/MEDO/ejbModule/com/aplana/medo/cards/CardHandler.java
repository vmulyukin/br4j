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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.medo.ServicesProvider;

/**
 * The base class for IEMDS import helpers. These classes performs required
 * business processing of cards in system.
 */
public abstract class CardHandler {

    protected Log logger = LogFactory.getLog(getClass());

    private Long id = null;

    public CardHandler() {
    }

    /**
     * Creates instance of class that allows to find cards directly by its id
     *
     * @param id -
     *                ID of card in system
     */
    public CardHandler(Long id) {
	this.id = id;
    }

    /**
     * Returns id of card according to current state of class.
     *
     * @return card id or -1 in case of error
     * @throws CardException
     */
    public long getCardId() throws CardException {
	if (id != null)
	    return id;
	return calculateCardId();
    }

    /**
     * <p>
     * Method is used in the {@link #getCardId()} and implements default
     * behavior of card id calculating. It is recommended to override this
     * method instead of {@link #getCardId()}.
     * </p>
     * <p>
     * The following behavior is implemented:
     * <ol>
     * <li>Searches cards according to current state</li>
     * <li>If there were found more than one cards returns ID of first of them</li>
     * </ol>
     * </p>
     *
     * @return ID of first of found cards or -1 if cards were not found
     * @throws CardException
     */
    protected long calculateCardId() throws CardException {
	List<ObjectId> cards = findCards();

	if (cards.size() > 1) {
	    logger
		    .warn("More than one card was found. First of them will be used");
	}
	if (!cards.isEmpty()) {
	    return (Long) cards.get(0).getId();
	}
	return -1;
    }

    /**
     * <p>
     * Returns list of <code>ObjectId</code> of cards found according to
     * current state of class. This method should not be overridden except
     * serious reason. Use {@link #search()} method for that goal.
     * </p>
     * <p>
     * If ID of card is defined ({@link #id}, only list with this id will be
     * returned.
     * </p>
     *
     * @return list of <code>ObjectId</code> of cards
     * @throws CardException
     */
    public List<ObjectId> findCards() throws CardException {
	logger.info("Trying to find cards according to current state: "
		+ getParameterValuesLog());

	if (getId() != null)
	    return Collections.singletonList(new ObjectId(Card.class, getId()));

	List<ObjectId> cards = search();

	if (cards == null) {
	    cards = new ArrayList<ObjectId>();
	}

	logger.info(String.format("There was found %d cards", cards.size()));
	return cards;
    }

    /**
     * Method that used in {@link #findCards()} and should implement search
     * mechanism.
     *
     * @return list of <code>ObjectId</code> of cards found according to
     *         current state of class
     * @throws CardException
     */
    protected abstract List<ObjectId> search() throws CardException;

    /**
     * Returns string contained description of current class state.
     *
     * @return parameter values log
     */
    protected String getParameterValuesLog() {
	return String.format("id='%s', ", getId());
    }

    /**
     * Returns {@link DataServiceBean} instance or throws {@link CardException}
     * if it is impossible.
     *
     * @return DataServiceBean
     * @throws CardException
     */
    protected DataServiceBean getServiceBean() throws CardException {
	DataServiceBean serviceBean;
	try {
	    serviceBean = ServicesProvider.serviceBeanInstance();
	} catch (ServiceException ex) {
	    throw new CardException();
	}

	if (serviceBean == null) {
	    logger.error("DataServiceBean was not initialized");
	    throw new CardException();
	}

	return serviceBean;
    }

    /**
     * Creates card of given template and fill it with given attribute values.
     * In case of some error localized message with <code>errorCode</code>
     * code will be thrown.
     *
     * @param templateId -
     *                template ID of creating card
     * @param attributeValues -
     *                table of values for attributes where key is ID of
     *                attribute and value is attribute value
     * @param errorCode -
     *                code of localized message
     * @return ID of created card
     * @throws CardException
     */
    protected long createCard(ObjectId templateId,
	    Map<ObjectId, Object> attributeValues, String errorCode)
	    throws CardException {
	DataServiceBean serviceBean = getServiceBean();

	CreateCard createCard = new CreateCard(templateId);
	Card card = null;
	try {
	    card = (Card) serviceBean.doAction(createCard);
	    if (card == null) {
		throw new CardException(errorCode);
	    }

	    for (Entry<ObjectId, Object> valueOfAttribute : attributeValues
		    .entrySet()) {
		Attribute attr = card.getAttributeById(valueOfAttribute
			.getKey());
		if (attr == null) {
		    logger
			    .error(String
				    .format(
					    "Attribute with given id (%s) was not found in card",
					    valueOfAttribute.getKey()));
		    throw new CardException("jbr.processor.nodestattr_2",
			    new Object[] { card.getId().getId(),
				    valueOfAttribute.getKey().getId() });
		}
		setAttributeValue(attr, valueOfAttribute.getValue());
	    }
	    ObjectId cardId = serviceBean.saveObject(card);
	    final UnlockObject unlock = new UnlockObject(cardId);
	    serviceBean.doAction(unlock);
	    logger.info(String.format("Card with '%d' id was created", cardId
		    .getId()));
	    return (Long) cardId.getId();
	} catch (CardException ex) {
	    throw ex;
	} catch (DataException ex) {
	    throw new CardException(errorCode, ex);
	} catch (ServiceException ex) {
	    throw new CardException(errorCode, ex);
	}
    }

    /**
     * Updates attribute of current card (ID of card is defined using
     * {@link #getCardId()} method). In case of some error localized message
     * with <code>errorCode</code> code will be thrown.
     *
     * @param attributeId -
     *                ID of updating attribute
     * @param value -
     *                value of updating attribute
     * @param errorCode -
     *                code of localized message
     * @return instance of created card
     * @see #updateAttributes(Map, String)
     * @throws CardException
     *
     */
    protected Card updateAttribute(ObjectId attributeId, Object value,
	    String errorCode) throws CardException {
	Map<ObjectId, Object> valuesOfAttributes = new HashMap<ObjectId, Object>();
	valuesOfAttributes.put(attributeId, value);
	return updateAttributes(valuesOfAttributes, errorCode);
    }

    /**
     * Updates attributes of current card (ID of card is defined using
     * {@link #getCardId()} method). In case of some error localized message
     * with <code>errorCode</code> code will be thrown.
     *
     * @param attributeValues -
     *                table of values for attributes where key is ID of
     *                attribute and value is attribute value
     * @param errorCode -
     *                code of localized message
     * @return instance of updated card
     * @throws CardException
     */
    protected Card updateAttributes(Map<ObjectId, Object> attributeValues,
	    String errorCode) throws CardException {
	DataServiceBean serviceBean = getServiceBean();
	long cardId = getCardId();

	if (cardId == -1) {
	    throw new CardException(errorCode);
	}

	ObjectId cardObjectId = new ObjectId(Card.class, cardId);

	try {
	    Card card = (Card) serviceBean.getById(cardObjectId);
	    for (Entry<ObjectId, Object> valueOfAttribute : attributeValues
		    .entrySet()) {
		Attribute attr = card.getAttributeById(valueOfAttribute
			.getKey());
		if (attr == null) {
		    throw new CardException("jbr.processor.nodestattr_2",
			    new Object[] { card.getId().getId(),
				    valueOfAttribute.getKey().getId() });
		}
		setAttributeValue(attr, valueOfAttribute.getValue());
	    }
	    LockObject lock = new LockObject(cardObjectId);
	    serviceBean.doAction(lock);
	    try {
		serviceBean.saveObject(card);
	    } finally {
		UnlockObject unlock = new UnlockObject(cardObjectId);
		serviceBean.doAction(unlock);
	    }
	    return card;
	} catch (CardException ex) {
	    throw ex;
	} catch (DataException ex) {
	    throw new CardException(errorCode, ex);
	} catch (ServiceException ex) {
	    throw new CardException(errorCode, ex);
	}
    }

    /**
     * Sets value of attribute taking into account its type.
     *
     * @param attribute -
     *                Attribute instance
     * @param value -
     *                value of attribute (actual type of parameter should be
     *                valid)
     * @throws CardException
     */
    protected void setAttributeValue(Attribute attribute, Object value)
	    throws CardException {
	if (Attribute.TYPE_CARD_LINK.equals(attribute.getType())) {
	    if (value instanceof ObjectId) {
		((CardLinkAttribute) attribute).addLinkedId((ObjectId) value);
	    } else if (value instanceof Long) {
		((CardLinkAttribute) attribute).addLinkedId((Long) value);
	    } else {
		logger.error(String.format(
			"Incorrect type '%s' of cardlink value: %s", value
				.getClass(), value));
		throw new CardException("jbr.card.configfail");
	    }

	} else if (Attribute.TYPE_TEXT.equals(attribute.getType())) {
	    if (value instanceof String)
		((TextAttribute) attribute).setValue((String) value);
	    else {
		logger.error(String.format(
			"Incorrect type '%s' of text value: %s", value
				.getClass(), value));
		throw new CardException("jbr.card.configfail");
	    }
	} else if (Attribute.TYPE_STRING.equals(attribute.getType())) {
	    if (value instanceof String)
		((StringAttribute) attribute).setValue((String) value);
	    else {
		logger.error(String.format(
			"Incorrect type '%s' of string value: %s", value
				.getClass(), value));
		throw new CardException("jbr.card.configfail");
	    }
	} else if (Attribute.TYPE_DATE.equals(attribute.getType())) {
	    if (value instanceof Date)
		((DateAttribute) attribute).setValue((Date) value);
	    else {
		logger.error(String.format(
			"Incorrect type '%s' of date value: %s", value
				.getClass(), value));
		throw new CardException("jbr.card.configfail");
	    }
	} else
	    throw new CardException("jbr.card.configfail",
		    new UnsupportedOperationException(String.format(
			    "Attribute of %s type is not supported", attribute
				    .getType())));
    }

    /**
     * @return the id
     */
    public Long getId() {
	return this.id;
    }

    /**
     * @param id
     *                the id to set
     */
    public void setId(Long id) {
	this.id = id;
    }

}
