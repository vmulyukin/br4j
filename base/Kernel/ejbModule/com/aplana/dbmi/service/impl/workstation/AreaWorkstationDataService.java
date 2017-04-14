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
package com.aplana.dbmi.service.impl.workstation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.Status;
import com.aplana.dbmi.service.impl.mapper.AttributeValueMapperUtils;
import com.aplana.dbmi.service.impl.workstation.dao.AreaWorkstationQueryInterface;
import com.aplana.dbmi.service.workstation.AreaWorkstationDataServiceInterface;
import com.aplana.dbmi.service.workstation.GetCardAreaDTO;
import com.aplana.dbmi.service.workstation.GetCardAreaQtyDTO;

/**
 * Common parent class that describes area(folder) workstation.
 * It contains methods for getting area(folder) cards and area card's quantities
 *    
 * @author skashanski
 *
 */
public class AreaWorkstationDataService implements
		AreaWorkstationDataServiceInterface {
	
	
	private AreaWorkstationQueryInterface workstationDAO = null;
	
	private CardService cardService = null;
	
	
	
	

	public void setCardService(CardService cardService) {
		this.cardService = cardService;
	}



	public void setWorkstationDAO(AreaWorkstationQueryInterface workstationDAO) {
		this.workstationDAO = workstationDAO;
	}
	
	
	

	public List<Card> getCards(GetCardAreaDTO getCardAreaDTO) {
		
		cardService.setSortableAttributeTypes(getCardAreaDTO.getSortAttributes());
		List<EmptyCard> supervisorCardIds = workstationDAO.getCards(getCardAreaDTO.getUserId(), 
				getCardAreaDTO.getPermissionTypes(),getCardAreaDTO.getPage(),getCardAreaDTO.getPageSize(), 
				getCardAreaDTO.getSortAttributes(), getCardAreaDTO.getSimpleSearchFilter());
		
		if (supervisorCardIds.isEmpty())
			return new ArrayList<Card>();

		/*
		    ����������:
		 	fillEmptyAttributes - ������������� true, ����� ��������� �������� ��� ��������� �������� empty. 
		 	� ������, ��� ��������� ����, �������� <content link="�����������" field="����������� ������������:" empty="<content link='�����������' field='������ ������������'/>
		 	���� ������ false, � �������� '�����������' ��� � �������, �� empty �� ��������������
		 */
		HashMap<Long, Collection<AttributeValue>> cardAttributes = getCardsAttributes(supervisorCardIds, getCardAreaDTO.getAttributes(), getCardAreaDTO.getUserId(), getCardAreaDTO.getPermissionTypes(), true); 
		
		return createCards(supervisorCardIds, cardAttributes);
		
	}
	
	
	protected HashMap<Long, Collection<AttributeValue>> getCardsAttributes(List<EmptyCard> onExecutionCards, List<AttributeValue> attributes, int userId, long[] permissionTypes) {
		return getCardsAttributes(onExecutionCards, attributes, userId, permissionTypes, false);
	}
	
	protected HashMap<Long, Collection<AttributeValue>> getCardsAttributes(List<EmptyCard> onExecutionCards, List<AttributeValue> attributes, int userId, long[] permissionTypes, boolean fillEmptyAttributes) {
		
		long[] cardIds = getCardIds(onExecutionCards);

		return cardService.getCardsAttributes(cardIds, attributes, userId, permissionTypes, fillEmptyAttributes);
		

	}
	

	
	
	protected long[] getCardIds(List<EmptyCard> reworkCards) {
		
		long[] cardIds = new long[reworkCards.size()];
		
		int i = 0;
		
		for(EmptyCard reworkCard : reworkCards) {
			cardIds[i++] = reworkCard.getId();
		}
		
		return cardIds;
	}	
	
	
	
	
	protected List<Card> createCards(List<EmptyCard> empryCards,
			HashMap<Long, Collection<AttributeValue>> cardAttributes) {
		
		List<Card> result = new ArrayList<Card>();
		

		for(EmptyCard emptyCard : empryCards) {
			
			long cardIdLong = emptyCard.getId();
			
			Card card = (Card)Card.createFromId(new ObjectId(Card.class, emptyCard.getId()));
			
			Collection<AttributeValue> attributeValues = cardAttributes.get(cardIdLong);
			Collection<Attribute> attributes =  AttributeValueMapperUtils.map(attributeValues);
			attributes =  cardService.fillAttributeDefinitions(attributes);
			
			//fillCardLinkAttributes(attributes);
			
			card.setAttributes(attributes);

            if (emptyCard.getTemplateId() != EmptyCard.NOT_DEFINED) {
                ObjectId templateId = new ObjectId(Template.class, emptyCard.getTemplateId());
                card.setTemplate(templateId);
                Template template = getTemplate(templateId);
                card.setTemplateNameRu(template.getNameRu());
                card.setTemplateNameEn(template.getNameEn());
            }

            if (emptyCard.getStatusId() != EmptyCard.NOT_DEFINED){
                Status statusDefinition = cardService.getStatusDefinition((int) emptyCard.getStatusId());
                card.setState(new ObjectId(CardState.class, emptyCard.getStatusId()));
                // RU not a bug! need for correct work
                card.setStateName(new LocalizedString(statusDefinition.getNameRu(), statusDefinition.getNameRu()));
            }
			
			result.add(card);
		}
		return result;
	}
	
	
	private Template getTemplate(ObjectId templateId) {
		
		int templateIdNum = Integer.parseInt(templateId.getId().toString());
		
		return cardService.getTemplateDefinition(templateIdNum);

	}	
	
	

	public List getQuantity(GetCardAreaQtyDTO getCardAreaQtyDTO) {
		
		return workstationDAO.getCardsQty(getCardAreaQtyDTO.getUserId(), getCardAreaQtyDTO.getPermissionTypes(), 
				getCardAreaQtyDTO.getSimpleSearchFilter());
		
	}

}
