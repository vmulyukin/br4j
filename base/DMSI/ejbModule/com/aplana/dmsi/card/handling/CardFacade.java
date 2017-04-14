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
package com.aplana.dmsi.card.handling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.MaterialAttributeHandler.CardIdSource;
import com.aplana.dmsi.util.ServiceUtils;
import com.aplana.dmsi.value.controllers.ValueController;

public class CardFacade {

    private final static long FAKE_CARD_ID = 0;

	private DataServiceFacade serviceBean;
    private Card card;

    private Log logger = LogFactory.getLog(getClass());

    private Map<ObjectId, ObjectId> parents = new HashMap<ObjectId, ObjectId>();
    private Map<ObjectId, Object> attributeValues = new HashMap<ObjectId, Object>();
    private Material cardMaterial = new Material();
    private Map<ObjectId, AttributeHandlerExtension> specificAttributeHandlers = new HashMap<ObjectId, AttributeHandlerExtension>();
    private boolean isCardFull = true;

	public CardFacade(DataServiceFacade serviceBean, Card card) {
	this.serviceBean = serviceBean;
	this.card = card;
    }

	public CardFacade(DataServiceFacade serviceBean, long cardId) {
	this(serviceBean, new ObjectId(Card.class, cardId));
    }

	public CardFacade(DataServiceFacade serviceBean, ObjectId cardId) {
	this(serviceBean, (Card) DataObject.createFromId(cardId));
	this.isCardFull = false;
    }

	public CardFacade(DataServiceFacade serviceBean) {
	this(serviceBean, FAKE_CARD_ID);
    }

    public ObjectId getCardId() {
    validateCardId();
	return card.getId();
    }

    public void addAttributeValue(ObjectId attributeId, Object value) {
	addAttributeValue(attributeId, value, null);
    }

	public void addAttributeValue(ObjectId attributeId, Object value, ValueController valueController) {
	if (value instanceof Material) {
	    setMaterial((Material) value);
	}
	attributeValues.put(attributeId, value);
	setAttributeValueController(attributeId, valueController);
    }

	public void setAttributeValueController(ObjectId attributeId, ValueController valueController) {
	if (valueController != null) {
			specificAttributeHandlers.put(attributeId, new ComplexAttributeHandler(valueController));
	}
    }

    private void setMaterial(Material material) {
	if (material.getName() != null) {
	    cardMaterial.setName(material.getName());
	}
	if (material.getData() != null) {
	    cardMaterial.setData(material.getData());
	    cardMaterial.setLength(material.getLength());
	}
    }

    public void createCard(ObjectId templateId) throws DMSIException {
	CreateCard createCard = new CreateCard(templateId);
	try {
	    card = (Card) serviceBean.doAction(createCard);
	    applyChanges();
	    this.isCardFull = true;
	} catch (DataException ex) {
	    throw new DMSIException(ex.getMessage(), ex);
	}
    }

    public void updateCard() throws DMSIException {
    	validateCardId();
	try {
	    LockObject lock = new LockObject(card.getId());
	    serviceBean.doAction(lock);
	    reloadCard();
	    applyChanges();
	    this.isCardFull = true;
	} catch (DataException ex) {
	    throw new DMSIException(ex.getMessage(), ex);
	}
    }

    private void validateCardId() {
    	if (card.getId() == null || card.getId().getId().equals(FAKE_CARD_ID)) {
    		throw new IllegalStateException("Card is not defined: use createCard method or appropriate constructor");
    	}
    }

    private void reloadCard() throws DataException {
	card = (Card) serviceBean.getById(card.getId());
	this.isCardFull = true;
    }

	private void applyChanges() throws DataException, DMSIException {
	ObjectId cardId = null;
	try {
	    setAttributes();
	    cardId = serviceBean.saveObject(card);
	    card.setId(cardId);
	    uploadMaterial();
	    updateParentCards();
	} finally {
	    if (cardId != null) {
		UnlockObject unlock = new UnlockObject(cardId);
		serviceBean.doAction(unlock);
	    }
	}
    }

    private void setAttributes() throws DMSIException {
	for (Entry<ObjectId, Object> attribute : attributeValues.entrySet()) {
	    ObjectId attributeId = attribute.getKey();
	    Object attributeValue = attribute.getValue();
	    setAttributeValue(attributeId, attributeValue);
	}
    }

    private void uploadMaterial() {
	ServiceUtils.uploadMaterial(serviceBean, getCardId(), cardMaterial);
    }

    private void updateParentCards() throws DMSIException {
	if (parents.isEmpty())
	    return;

	StringBuilder failedUpdateIds = new StringBuilder();
	for (Entry<ObjectId, ObjectId> parent : parents.entrySet()) {
	    ObjectId cardId = parent.getKey();
	    ObjectId attrbuteId = parent.getValue();
	    try {
		updateParentCard(cardId, attrbuteId);
	    } catch (Exception ex) {
		if (failedUpdateIds.length() != 0) {
		    failedUpdateIds.append(", ");
		}
		failedUpdateIds.append(cardId);
		logger.error("Error during update parent card " + cardId, ex);
	    }
	}
	if (failedUpdateIds.length() > 0) {
			throw new DMSIException("Unable to update cards with following ids: " + failedUpdateIds.toString());
	}
    }

	private void updateParentCard(ObjectId parentCardId, ObjectId attributeId) throws DMSIException {
	CardFacade subCardFacade = new CardFacade(serviceBean, parentCardId);
	subCardFacade.addAttributeValue(attributeId, getCardId());
	subCardFacade.updateCard();
    }

	public ObjectId getCardState() throws DMSIException {
		if (this.card.getState() != null) {
			return this.card.getState();
		}
		try {
			if (!this.isCardFull) {
				reloadCard();
			}
			if (this.card.getState() != null) {
				return this.card.getState();
			}
			ObjectId stateValue = ((ReferenceValue) getAttributeValue(Card.ATTR_STATE)).getId();
			this.card.setState(new ObjectId(CardState.class, stateValue.getId()));
			return this.card.getState();
		} catch (DataException ex) {
			throw new DMSIException(ex.getMessage(), ex);
		}
	}

	public ObjectId getTemplate() throws DMSIException {
		if (this.card.getTemplate() != null) {
			return this.card.getTemplate();
		}
		try {
			if (!this.isCardFull) {
				reloadCard();
			}
			if (this.card.getTemplate() != null) {
				return this.card.getTemplate();
			}
			ObjectId templateValue = ((ReferenceValue) getAttributeValue(Card.ATTR_TEMPLATE)).getId();
			this.card.setTemplate(new ObjectId(Template.class, templateValue.getId()));
			return this.card.getTemplate();
		} catch (DataException ex) {
			throw new DMSIException(ex.getMessage(), ex);
		}
	}

	public Object getAttributeValue(ObjectId attributeId) throws DMSIException {
		if (Card.ATTR_ID.equals(attributeId)) {
			return getCardId().getId();
		}
		try {
			if (!this.isCardFull) {
				reloadCard();
			}
			AttributeHandler attributeHandler = getAttributeHandler(attributeId);
			Object value = attributeHandler.getAttributeValue();
			return value;
		} catch (DataException ex) {
			throw new DMSIException(ex.getMessage(), ex);
		}
	}

	public Object getAttributeName(ObjectId attributeId) throws DMSIException {
		AttributeHandler attributeHandler = getAttributeHandler(attributeId);
		return attributeHandler.getAttributeName();
	}

	private void setAttributeValue(ObjectId attributeId, Object value) throws DMSIException {
	if (value == null)
	    return;

	AttributeHandler attributeHandler = getAttributeHandler(attributeId);
	attributeHandler.setAttributeValue(value);
    }

    protected void addParent(ObjectId parent, ObjectId attributeId) {
	parents.put(parent, attributeId);
    }

    private AttributeHandler getAttributeHandler(ObjectId attrId) {
	AttributeHandler attributeHandler;
	Attribute attribute = getAttribute(attrId);
	if (attribute == null) {
	    logger.warn("Attribute is not found: " + attrId);
			attributeHandler = new StubAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_CARD_LINK.equals(attribute.getType())) {
			attributeHandler = new CardLinkAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_PERSON.equals(attribute.getType())) {
			attributeHandler = new PersonAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_TEXT.equals(attribute.getType())) {
			attributeHandler = new TextAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_STRING.equals(attribute.getType())) {
			attributeHandler = new StringAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_DATE.equals(attribute.getType())) {
			attributeHandler = new DateAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_LIST.equals(attribute.getType())) {
			attributeHandler = new ListAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_BACK_LINK.equals(attribute.getType())) {
			attributeHandler = new BackLinkAttributeHandler(this, attribute, serviceBean);
	} else if (Attribute.TYPE_INTEGER.equals(attribute.getType())) {
			attributeHandler = new IntegerAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_TREE.equals(attribute.getType())) {
			attributeHandler = new TreeAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(attribute.getType())) {
			attributeHandler = new TypedCardLinkAttributeHandler(attribute, serviceBean);
	} else if (Attribute.TYPE_MATERIAL.equals(attribute.getType())) {
	    attributeHandler = new MaterialAttributeHandler(new CardIdSource() {
			public ObjectId getCardId() {
				return CardFacade.this.getCardId();
				}
			}, attribute, serviceBean);
	} else if (Attribute.TYPE_HTML.equals(attribute.getType())) {
			attributeHandler = new HtmlAttributeHandler(attribute, serviceBean);
	} else {
			attributeHandler = new StubAttributeHandler(attribute, serviceBean);
	}

	if (specificAttributeHandlers.containsKey(attrId)) {
			AttributeHandlerExtension extendedAttributeHandler = specificAttributeHandlers.get(attrId);
	    extendedAttributeHandler.setAttributeHandler(attributeHandler);
	    attributeHandler = extendedAttributeHandler;
	}
	return attributeHandler;
    }

    private Attribute getAttribute(ObjectId attrId) {
	Attribute attribute = card.getAttributeById(attrId);
	if (attribute != null) {
	    return attribute;
	} else {
	    return null;
	}
    }
}
