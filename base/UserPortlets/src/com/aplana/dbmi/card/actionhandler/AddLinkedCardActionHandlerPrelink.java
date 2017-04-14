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
package com.aplana.dbmi.card.actionhandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class AddLinkedCardActionHandlerPrelink extends
		AddLinkedCardActionHandler {
	
	
	protected static class TypedCardLinkItemCloseHandler extends AddLinkedCardActionHandler.TypedCardLinkItemCloseHandler {
		
		public TypedCardLinkItemCloseHandler(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean) {
			super(cardLinkId, idsToLinkId, sessionBean);
		}
		
		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
			Attribute attr = previousCardInfo.getCard().getAttributeById(cardLinkId);
			if(idsToLinkId != null && closedCardInfo.getCard().getAttributeById(idsToLinkId) != null){
				Attribute idsToLinkAttr =  closedCardInfo.getCard().getAttributeById(idsToLinkId);
				if(cardLinkId.getType().equals(idsToLinkId.getType())){
					if(cardLinkId.getType().equals(PersonAttribute.class)){
						if(!((PersonAttribute) attr).isMultiValued())
							((PersonAttribute) attr).setPerson(((PersonAttribute) idsToLinkAttr).getPerson());
						else
							((PersonAttribute) attr).getValues().addAll(((PersonAttribute) idsToLinkAttr).getValues());
					} else if(cardLinkId.getType().equals(CardLinkAttribute.class)){
						if(!((CardLinkAttribute) attr).isMultiValued())
							((CardLinkAttribute) attr).addSingleLinkedId(((CardLinkAttribute) idsToLinkAttr).getSingleLinkedId());
						else
							((CardLinkAttribute) attr).addIdsLinked(((CardLinkAttribute) idsToLinkAttr).getIdsLinked());
					} else if(cardLinkId.getType().equals(TypedCardLinkAttribute.class)){
						if(!((TypedCardLinkAttribute) attr).isMultiValued()) {
							Map.Entry<Long,Long> entry = (Map.Entry<Long,Long>) ((TypedCardLinkAttribute) idsToLinkAttr).getTypes().entrySet().iterator().next();
							((TypedCardLinkAttribute) attr).clear();
							((TypedCardLinkAttribute) attr).addType(entry.getKey(), entry.getValue());
						}
						((TypedCardLinkAttribute) attr).getTypes().putAll(((TypedCardLinkAttribute) idsToLinkAttr).getTypes());
					}
					previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
				} else if(cardLinkId.getType().equals(TypedCardLinkAttribute.class) 
							&& idsToLinkAttr.getId().getType().equals(PersonAttribute.class)) {
					if(!((TypedCardLinkAttribute) attr).isMultiValued())
						((TypedCardLinkAttribute) attr).addSingleLinkedId(((PersonAttribute) idsToLinkAttr).getPerson().getCardId());
					else {
						for( Iterator itr = ((PersonAttribute) idsToLinkAttr).getValues().iterator(); itr.hasNext(); )
						{
							((CardLinkAttribute) attr).addLinkedId(((Person) itr.next()).getCardId());
						}
					}
				}
			}
			sessionBean.setEditorData(cardLinkId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET, true);
		}
	}
	
	
	
	@Override
	protected Card createCard() throws DataException, ServiceException {

		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		Card activeCard = sessionBean.getActiveCard();
		CardPortletCardInfo activeCardInfo = sessionBean.getActiveCardInfo();

		// create new card
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		createCard.setLinked(isLinked);
		createCard.setParent(activeCard);
		Card card = (Card) serviceBean.doAction(createCard);
		
		// make a link to child from parent
		if (card != null && idsToLinkAttrId == null) {
			if (attribute.getId().getType().equals(TypedCardLinkAttribute.class)){
				TypedCardLinkAttribute typedAttr = (TypedCardLinkAttribute) attribute;
				Collection<ReferenceValue> values = typedAttr.getReferenceValues();
				if(values == null || values.isEmpty()){
					DataServiceBean serviceBean = sessionBean.getServiceBean();
					try {
						values = serviceBean.listChildren(typedAttr.getReference(), ReferenceValue.class);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				if(values == null || values.isEmpty()) {
					sessionBean.setMessage("addtypedlink.error");
				} else {
					if(!typedAttr.isMultiValued()) {
						typedAttr.clear();
					}
					typedAttr.addType((Long) card.getId().getId(), (Long)values.iterator().next().getId().getId());
				}
			}
			else if(attribute.getId().getType().equals(CardLinkAttribute.class)){
				CardLinkAttribute linkAttr = (CardLinkAttribute) attribute;
				if (linkAttr.isMultiValued()) {
					linkAttr.addLinkedId(card.getId());
				} else { // �������� ������ ���� ��������
					linkAttr.addSingleLinkedId(card.getId());
				}
			}
			activeCardInfo.setAttributeEditorData(attribute.getId(), AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
		} 
		
		if (parentAttributes != null && parentAttributes.size()>0) {
			Card parentCard = getCardPortletSessionBean().getActiveCard();
			if (parentCard != null) {
				for (Map.Entry<ObjectId, ObjectId> entry: parentAttributes.entrySet()) {
					Attribute attrFrom = parentCard.getAttributeById(entry.getKey());
					Attribute attrTo = card.getAttributeById(entry.getValue());
					if (attrFrom != null && attrTo != null && attrFrom instanceof CardLinkAttribute
							&& attrTo instanceof CardLinkAttribute) {
						((CardLinkAttribute)attrTo)
								.setIdsLinked(((CardLinkAttribute)attrFrom).getIdsLinked());
					}
				}
			}
		}
		return card;
	}
	
	@Override
	protected void openNewCard(){
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		try {
	    	sessionBean.openNestedCard(
	    			createCard(),
	    			new TypedCardLinkItemCloseHandler(attribute.getId(), idsToLinkAttrId, sessionBean),
	    			true
	    	);
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() } , PortletMessageType.ERROR);
		}
	}
}
