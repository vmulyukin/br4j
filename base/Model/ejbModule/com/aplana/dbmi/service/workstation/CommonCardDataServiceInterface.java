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
package com.aplana.dbmi.service.workstation;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.SearchFilter;

import java.util.Collection;
import java.util.List;

public interface CommonCardDataServiceInterface{

    public final static String NAME = "commonCardDataService";

    public long [] getCardTemplateIdAndStatusId(long id);
    
    public Card findSingleCard(Collection<ObjectId> cardIds, List<AttributeValue> attributes, SearchFilter filter, 
    		int userId, long[] permissionTypes);
    
    public List<Card> getCardsAttributes(Collection<ObjectId> cardIds, List<AttributeValue> attributes, SearchFilter filter, 
    		int userId, long[] permissionTypes);

    public List<Card> getCardsAttributes(List<Card> cardsIdTemplate, List<AttributeValue> attributes, int userId, long[] permissionTypes );

    public Card getCardAttributes(Card cardIdTemplate, List<AttributeValue> attributes, int userId, long[] permissionTypes );
    
    public Collection<Attribute> fillAttributeDefinitions(Collection<Attribute> attributes);
    
    public List<Card> getBackLinkedCards(ObjectId cardId, BackLinkAttribute attribute);

    public boolean userHasAccessToCard( ObjectId cardId, ObjectId userId );
    
    public Collection<Template> getAllTemplateDefinitions();
}
