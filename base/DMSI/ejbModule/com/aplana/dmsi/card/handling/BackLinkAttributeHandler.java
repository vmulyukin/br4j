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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.util.ServiceUtils;

class BackLinkAttributeHandler extends AttributeHandler {

    private final CardFacade cardFacade;
    private Log logger = LogFactory.getLog(getClass());

    public BackLinkAttributeHandler(CardFacade cardFacade, Attribute attribute,
    		DataServiceFacade serviceBean) {
	super(attribute, serviceBean);
	this.cardFacade = cardFacade;
    }

    @Override
    public Object getAttributeValue() {
		ObjectId linkSourceId = ((BackLinkAttribute) getAttribute())
			.getLinkSource();
		Collection<ObjectId> linkSourceIds = ((BackLinkAttribute) getAttribute()).getLinkSources();
	Collection<Card> linkedCards = ServiceUtils.getBackLinkedCards(
			getServiceBean(), this.cardFacade.getCardId(), getAttribute()
			.getId(), linkSourceId);
		// ���� ������������ �������� �� �������, �� �������� �� ����� ����� �� ������ ���������� �� ������
		if (linkedCards==null||linkedCards.isEmpty()){
			if(linkSourceIds!=null){
				for(ObjectId nextLinkedId: linkSourceIds){
					linkedCards = ServiceUtils.getBackLinkedCards(
							getServiceBean(), this.cardFacade.getCardId(), getAttribute()
								.getId(), nextLinkedId);
					if (linkedCards!=null&&!linkedCards.isEmpty())
						break;
				}

			}
		}
	return convertCardsToIds(linkedCards, linkSourceId);
    }

    private ObjectId[] convertCardsToIds(Collection<Card> cards,
	    ObjectId linkSourceId) {
	List<ObjectId> cardIds = new ArrayList<ObjectId>();
	for (Card card : cards) {
	    Attribute linkAttribute = card.getAttributeById(linkSourceId);
	    if (linkAttribute == null) {
		// hack: link source can contain incorrect type of attribute
		linkAttribute = card.getAttributeById(new ObjectId(
			TypedCardLinkAttribute.class, linkSourceId.getId()));
	    }
	    ObjectId id = card.getId();
	    if (linkAttribute instanceof TypedCardLinkAttribute) {
		id = convertCardToTypedId(id,
			(TypedCardLinkAttribute) linkAttribute);
	    }
	    cardIds.add(id);
	}
	return cardIds.toArray(new ObjectId[cardIds.size()]);
    }

    private TypedObjectId convertCardToTypedId(ObjectId cardId,
	    TypedCardLinkAttribute linkAttribute) {
	TypedObjectId[] ids = (TypedObjectId[]) new TypedCardLinkAttributeHandler(
		linkAttribute, getServiceBean()).getAttributeValue();
	for (TypedObjectId id : ids) {
	    if (id.equals(this.cardFacade.getCardId())) {
		return new TypedObjectId(cardId, id.getTypeId());
	    }
	}
	return null;
    }

    @Override
    public void setAttributeValue(Object value) {
		ObjectId sourceId = ((BackLinkAttribute) getAttribute())
			.getLinkSource();
		Collection<ObjectId> sourceIds = ((BackLinkAttribute) getAttribute()).getLinkSources();
	if (value instanceof ObjectId) {
	    this.cardFacade.addParent((ObjectId) value, sourceId);
		    if (sourceIds!=null){
			    for (ObjectId sourceAttrId : sourceIds) {
			    	this.cardFacade.addParent((ObjectId)value, sourceAttrId);
			    }
		    }
	} else if (value instanceof ObjectId[]) {
	    for (ObjectId targetId : (ObjectId[]) value) {
		this.cardFacade.addParent(targetId, sourceId);
			    if (sourceIds!=null){
				    for (ObjectId sourceAttrId : sourceIds) {
				    	this.cardFacade.addParent(targetId, sourceAttrId);
	    }
			    }
		    }
	} else {
	    logger.error(String.format(
		    "Incorrect type '%s' of backlink value: %s", value
			    .getClass(), value));
	}
    }
}