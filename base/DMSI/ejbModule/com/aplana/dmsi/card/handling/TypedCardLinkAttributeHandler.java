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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.client.DataServiceFacade;

class TypedCardLinkAttributeHandler extends AttributeHandler {

    private Log logger = LogFactory.getLog(getClass());

    public TypedCardLinkAttributeHandler(Attribute attribute,
    		DataServiceFacade serviceBean) {
	super(attribute, serviceBean);
    }

    @Override
    public Object getAttributeValue() {
	TypedCardLinkAttribute typedAttribute = (TypedCardLinkAttribute) getAttribute();
	ObjectId[] cardIds = typedAttribute.getIdsArray();
	TypedObjectId[] typedCardIds = new TypedObjectId[cardIds.length];
	for (int i = 0; i < cardIds.length; ++i) {
	    ObjectId cardId = cardIds[i];
	    ObjectId cardType = typedAttribute.getCardType(cardId);
	    typedCardIds[i] = new TypedObjectId(cardId, cardType);
	}
	return typedCardIds;
    }

    @Override
    public void setAttributeValue(Object value) {
	TypedCardLinkAttribute typedAttribute = (TypedCardLinkAttribute) getAttribute();
	if (value instanceof ObjectId) {
	    addAttributeValue(typedAttribute, (ObjectId) value);
	} else if (value instanceof ObjectId[]) {
	    for (ObjectId typedObject : (ObjectId[]) value) {
		addAttributeValue(typedAttribute, typedObject);
	    }
	} else {
	    logger.error(String.format(
		    "Incorrect type '%s' of cardlink value: %s", value
			    .getClass(), value));
	}
    }

    private void addAttributeValue(TypedCardLinkAttribute typedAttribute,
	    ObjectId cardId) {
	if (cardId instanceof TypedObjectId) {
	    TypedObjectId typedCardId = (TypedObjectId) cardId;
	    ObjectId typeId = typedCardId.getTypeId();
	    typedAttribute.addType((Long) cardId.getId(), typeId == null ? null
		    : (Long) typeId.getId());
	    cardId = new ObjectId(cardId.getType(), cardId.getId());
	}
	typedAttribute.addLinkedId(cardId);
    }
}