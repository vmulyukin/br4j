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
package com.aplana.ireferent.card.handling;

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
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.util.ServiceUtils;
import com.aplana.ireferent.value.controllers.ValueController;

public class CardFacade {

    private final static long FAKE_CARD_ID = 0;

    private DataServiceBean serviceBean;
    private Card card;

    private Log logger = LogFactory.getLog(getClass());

    private Map<ObjectId, ObjectId> parents = new HashMap<ObjectId, ObjectId>();
    private Map<ObjectId, Object> attributeValues = new HashMap<ObjectId, Object>();
    private Material cardMaterial = new Material();
    private Map<ObjectId, AttributeHandlerExtension> specificAttributeHandlers = new HashMap<ObjectId, AttributeHandlerExtension>();

    public CardFacade(DataServiceBean serviceBean, Card card) {
	this.serviceBean = serviceBean;
	this.card = card;
    }

    public CardFacade(DataServiceBean serviceBean, long cardId) {
	this(serviceBean, new ObjectId(Card.class, cardId));
    }

    public CardFacade(DataServiceBean serviceBean, ObjectId cardId) {
	this(serviceBean, (Card) DataObject.createFromId(cardId));
    }

    public CardFacade(DataServiceBean serviceBean) {
	this(serviceBean, FAKE_CARD_ID);
    }

    public ObjectId getCardId() {
	return card.getId();
    }

    public void addAttributeValue(ObjectId attributeId, Object value) {
	addAttributeValue(attributeId, value, null);
    }

    public void addAttributeValue(ObjectId attributeId, Object value,
	    ValueController valueController) {
	if (value instanceof Material) {
	    setMaterial((Material) value);
	}
	attributeValues.put(attributeId, value);
	setAttributeValueController(attributeId, valueController);
    }

    public void setAttributeValueController(ObjectId attributeId,
	    ValueController valueController) {
	if (valueController != null) {
	    specificAttributeHandlers.put(attributeId,
		    new ComplexAttributeHandler(valueController));
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

    public void createCard(ObjectId templateId) throws IReferentException {
	CreateCard createCard = new CreateCard(templateId);
	try {
	    card = (Card) serviceBean.doAction(createCard);
	    applyChanges();
	    // BR4J00036557 ��� ������ � ����������� 2-�� ������ ���������� ��������� ��������� ������ ���, 
	    // ����� ���������� ���������� ��� ��.
	    // TODO � ���������� ����� �������� ��� �������� ������� ����������.
	    resaveCard();
	} catch (ServiceException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	} catch (DataException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	}
    }

    public void updateCard() throws IReferentException {
	try {
	    LockObject lock = new LockObject(card.getId());
	    serviceBean.doAction(lock);
	    reloadCard();
	    applyChanges();
	} catch (DataException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	} catch (ServiceException ex) {
	    throw new IReferentException(ex.getMessage(), ex);
	}
    }

    public void resaveCard() throws DataException, ServiceException {
    	try {
    	    LockObject lock = new LockObject(card.getId());
    	    serviceBean.doAction(lock);
    	    reloadCard();
    	    serviceBean.saveObject(card);
		} finally {
			UnlockObject unlock = new UnlockObject(card.getId());
			serviceBean.doAction(unlock);
		}
    }

    private void reloadCard() throws DataException, ServiceException {
	card = (Card) serviceBean.getById(card.getId());
    }

    private void applyChanges() throws DataException, ServiceException,
	    IReferentException {
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

    private void setAttributes() throws IReferentException {
	for (Entry<ObjectId, Object> attribute : attributeValues.entrySet()) {
	    ObjectId attributeId = attribute.getKey();
	    Object attributeValue = attribute.getValue();
	    setAttributeValue(attributeId, attributeValue);
	}
    }

    private void uploadMaterial() {
	ServiceUtils.uploadMaterial(serviceBean, getCardId(), cardMaterial);
    }

    private void updateParentCards() throws IReferentException {
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
	    throw new IReferentException(
		    "Unable to update cards with following ids: "
			    + failedUpdateIds.toString());
	}
    }

    private void updateParentCard(ObjectId parentCardId, ObjectId attributeId)
	    throws IReferentException {
	CardFacade subCardFacade = new CardFacade(serviceBean, parentCardId);
	subCardFacade.addAttributeValue(attributeId, card.getId());
	subCardFacade.updateCard();
    }

    public Object getAttributeValue(ObjectId attributeId)
	    throws IReferentException {
	if (Card.ATTR_ID.equals(attributeId)) {
	    return card.getId().getId();
	}

	AttributeHandler attributeHandler = getAttributeHandler(attributeId);
	return attributeHandler.getAttributeValue();
    }

    private void setAttributeValue(ObjectId attributeId, Object value)
	    throws IReferentException {
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
	    attributeHandler = new StubAttributeHandler(attribute);
	} else if (Attribute.TYPE_CARD_LINK.equals(attribute.getType())) {
	    attributeHandler = new CardLinkAttributeHandler(attribute);
	} else if (Attribute.TYPE_PERSON.equals(attribute.getType())) {
	    attributeHandler = new PersonAttributeHandler(attribute,
		    serviceBean);
	} else if (Attribute.TYPE_TEXT.equals(attribute.getType())) {
	    attributeHandler = new TextAttributeHandler(attribute);
	} else if (Attribute.TYPE_STRING.equals(attribute.getType())) {
	    attributeHandler = new StringAttributeHandler(attribute);
	} else if (Attribute.TYPE_DATE.equals(attribute.getType())) {
	    attributeHandler = new DateAttributeHandler(attribute);
	} else if (Attribute.TYPE_LIST.equals(attribute.getType())) {
	    attributeHandler = new ListAttributeHandler(attribute);
	} else if (Attribute.TYPE_BACK_LINK.equals(attribute.getType())) {
	    attributeHandler = new BackLinkAttributeHandler(this, attribute,
		    serviceBean);
	} else if (Attribute.TYPE_INTEGER.equals(attribute.getType())) {
	    attributeHandler = new IntegerAttributeHandler(attribute);
	} else if (Attribute.TYPE_TREE.equals(attribute.getType())) {
	    attributeHandler = new TreeAttributeHandler(attribute);
	} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(attribute.getType())) {
	    attributeHandler = new TypedCardLinkAttributeHandler(attribute);
	} else if (Attribute.TYPE_MATERIAL.equals(attribute.getType())) {
	    attributeHandler = new MaterialAttributeHandler(getCardId(),
		    attribute, serviceBean);
	} else if (Attribute.TYPE_HTML.equals(attribute.getType())) {
	    attributeHandler = new HtmlAttributeHandler(attribute);
	} else {
	    attributeHandler = new StubAttributeHandler(attribute);
	}

	if (specificAttributeHandlers.containsKey(attrId)) {
	    AttributeHandlerExtension extendedAttributeHandler = specificAttributeHandlers
		    .get(attrId);
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
