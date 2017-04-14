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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.SearchFilter;
import com.aplana.dbmi.service.workstation.CommonCardDataServiceInterface;

import java.util.*;

public class CommonCardDataService implements CommonCardDataServiceInterface {

    private CardService cardService;

    public CardService getCardService() {
        return cardService;
    }

    public void setCardService(CardService cardService) {
        this.cardService = cardService;
    }

    public long [] getCardTemplateIdAndStatusId(long id) {
        return getCardService().getCardTemplateIdAndStatusId(id);
    }

    public List<Card> getCardsAttributes(Collection<ObjectId> cardIds, List<AttributeValue> attributes, SearchFilter filter, 
    		int userId, long[] permissionTypes) {
    	if(null == cardIds || cardIds.size() == 0) {
    		return Collections.emptyList();
    	}
    	
    	long[] cardIdsArray = extractIdsFromObjectIds(cardIds);	    
    	// Get filtered card, template and status ids
    	List<long[]> filteredIds = getCardService().getCardsTemplateIdAndStatusId(cardIdsArray, filter, userId, 0);
    	
    	if(null == filteredIds || filteredIds.size() == 0) {
    		return Collections.emptyList();
    	}
    	
    	List<EmptyCard> cards = new ArrayList<EmptyCard>(filteredIds.size());
    	cardIdsArray = new long[filteredIds.size()];
    	
    	int i = 0;
    	for (long[] ids : filteredIds) {
            cards.add(new EmptyCard(ids[0], ids[1], ids[2]));            
            cardIdsArray[i] = ids[0];
            i++;
        }    	  	
    	
        HashMap<Long, Collection<AttributeValue>> cardsAttributes
                = getCardService().getCardsAttributes(cardIdsArray, attributes, userId, permissionTypes, true);

        return Util.createCards(cards, cardsAttributes, getCardService());
    }
    
    public Card findSingleCard(Collection<ObjectId> cardIds, List<AttributeValue> attributes, SearchFilter filter, 
    		int userId, long[] permissionTypes) {
    	if(null == cardIds || cardIds.isEmpty()) {
    		return null;
    	}
    	
    	long[] cardIdsArray = extractIdsFromObjectIds(cardIds);	    
    	// Get filtered card, template and status ids
    	List<long[]> filteredIds = getCardService().getCardsTemplateIdAndStatusId(cardIdsArray, filter, userId, 1);
    	
    	if(null == filteredIds || filteredIds.isEmpty()) {
    		return null;
    	}
    	
    	HashMap<Long, Collection<AttributeValue>> cardsAttributes = 
    		getCardService().getCardsAttributes(new long[] {filteredIds.get(0)[0]}, attributes, userId, permissionTypes, true);

    	EmptyCard card = new EmptyCard(filteredIds.get(0)[0], filteredIds.get(0)[1], filteredIds.get(0)[2]);
    	List<Card> cards = Util.createCards(Collections.singletonList(card), cardsAttributes, getCardService());
    	
    	return cards.get(0);
    }

    public List<Card> getCardsAttributes(List<Card> cardsIdTemplate, List<AttributeValue> attributes, int userId, long[] permissionTypes ) {
        HashMap<Long, Collection<AttributeValue>> cardsAttributes
                = getCardService().getCardsAttributes(extractIds(cardsIdTemplate), attributes, userId, permissionTypes, true);

        return Util.createCards(makeEmptyCards(cardsIdTemplate), cardsAttributes, getCardService());
    }

    public Card getCardAttributes(Card cardIdTemplate, List<AttributeValue> attributes, int userId, long[] permissionTypes ) {
        return getCardsAttributes(Collections.singletonList(cardIdTemplate), attributes, userId, permissionTypes).get(0);
    }
    
    public Collection<Attribute> fillAttributeDefinitions(Collection<Attribute> attributes) {
    	
    	return getCardService().fillAttributeDefinitions(attributes);
    }
    
    public AttributeDef getAttributeDefinition( String code ) {
    	return getCardService().getAttributeDefinition(code);
    }

    public boolean userHasAccessToCard( ObjectId cardId, ObjectId userId ){
        return getCardService().userHasAccessToCard(cardId, userId);
    }

    public List<Card> getBackLinkedCards(ObjectId cardId, BackLinkAttribute attribute){
        long cardIdL = (Long)cardId.getId();
        HashMap<Long, ArrayList<Long>> backLinkedCards = getCardService().getBackLinkedCards(new long[]{cardIdL}, Collections.singleton(attribute.getId().getId().toString()));
        if (backLinkedCards == null) return null;
        ArrayList<Long> ids = backLinkedCards.get(cardIdL);
        if (ids == null) return null;
        List<Card> cardWithIds = new ArrayList<Card>(ids.size());
        for (Long id : ids) {
            Card cardWithId = new Card();
            cardWithId.setId(id);
            cardWithIds.add(cardWithId);
        }
        return cardWithIds;
    }

    private List<EmptyCard> makeEmptyCards(List<Card> cardIdsTemplates) {
        List<EmptyCard> ret = new ArrayList<EmptyCard>(cardIdsTemplates.size());
        for (int i = 0; i < cardIdsTemplates.size(); i++) {
            EmptyCard emptyCard = new EmptyCard((Long) cardIdsTemplates.get(i).getId().getId());
            ObjectId template = cardIdsTemplates.get(i).getTemplate();
            if (template != null){
                emptyCard.setTemplateId((Long) cardIdsTemplates.get(i).getTemplate().getId());
            }
            if (cardIdsTemplates.get(i).getState() != null){
                emptyCard.setStatusId((Long)cardIdsTemplates.get(i).getState().getId());
            }
            ret.add(i, emptyCard);
        }
        return ret;
    }

    private long[] extractIds(List<Card> cardIdsTemplates) {
        long[] ret = new long[cardIdsTemplates.size()];
        for (int i = 0; i < cardIdsTemplates.size(); i++)
            ret[i] = Long.parseLong(cardIdsTemplates.get(i).getId().getId().toString());
        return ret;
    }


    private long[] extractIdsFromObjectIds(Collection<ObjectId> cardIds) {
    	long[] cardIdsArray = new long[cardIds.size()];
    	
    	int i = 0;
    	for (ObjectId cardId : cardIds) {
    		Long cardIdLong = (Long) cardId.getId();
            cardIdsArray[i] = cardIdLong;
            i++;
        }    
    	
    	return cardIdsArray;
    }
    
    public Collection<Template> getAllTemplateDefinitions() {
    	
    	return getCardService().getAllTemplateDefinitions();
    	
    }

}