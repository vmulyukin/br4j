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
package com.aplana.ireferent.actions;

import java.util.Collection;
import java.util.Collections;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ServiceUtils;

public class ChangeLinkStateAction extends ChangeStateAction {
	
	 private static final String FIND_LINKED_ON_USER = "findLinkedOnUser";
	 private static final String FIND_LINKED_ON_LINK = "findLinkedOnLink";
	 private static final String FIND_LINKED_ON_STATE = "findLinkedOnState";

	 private ObjectId findLinkedOnUser;
	 private ObjectId findLinkedOnLink;
	 private ObjectId findLinkedOnState;
	
	 public void setParameter(String key, Object value) {
		if (FIND_LINKED_ON_USER.equals(key)) {
			findLinkedOnUser = ObjectIdUtils.getObjectId(PersonAttribute.class,
	    		    (String) value, false);	
	    } else
	    if (FIND_LINKED_ON_LINK.equals(key)) {
	    	findLinkedOnLink =  ObjectIdUtils.getObjectId(CardLinkAttribute.class,
	    		    (String) value, false);		
	    } else
	    if (FIND_LINKED_ON_STATE.equals(key)) {
	    	findLinkedOnState = ObjectIdUtils.getObjectId(CardState.class,
	    		    (String) value, true);
	    } else                	
		    super.setParameter(key, value);
	}

	 @Override
	 public void doAction(DataServiceBean serviceBean, WSObject object)
		    throws IReferentException {
	    String cardIdDoc = object.getId();
	    if (null == cardIdDoc || cardIdDoc.isEmpty()) {
	    	throw new IReferentException("Card Id for action 'CreateLinkCardAction' is null. Type: "
	    		    + object.getType());
	    }
	    Person person = findLinkedOnUser != null ?serviceBean.getPerson():null;
	    Collection<Card> cardsExistLink = getCards(serviceBean, cardIdDoc, person);
	    
	    if (!isOptional() && cardsExistLink.isEmpty()) {
		    throw new IReferentException("Cards for processing were not found");
		}

		for (Card card : cardsExistLink) {
		    WorkflowMove workflowMove = calculateWorkflowMove(serviceBean, card);
		    if (workflowMove == null) {
			throw new IReferentException(
				"It is impossible to change state of card "
					+ card.getId().getId());
		    }
		    changeState(serviceBean, card, workflowMove);
		}
	 }
	 
	private Collection<Card> getCards(DataServiceBean serviceBean,
			String cardId, Person person) throws IReferentException {
		Search search = new Search();
		search.setByAttributes(true);
		if (null != findLinkedOnUser) {
			if (null == person) {
				throw new IReferentException(
						"Person for action 'ChangeLinkedStateAction' is null.");
			}
			search.addPersonAttribute(findLinkedOnUser, person.getId());
		}
		if (null != findLinkedOnLink) {
			search.addCardLinkAttribute(findLinkedOnLink, new ObjectId(
					Card.class, Long.parseLong(cardId)));
		}
		if (null != findLinkedOnState) {
			search.setStates(Collections.singletonList(findLinkedOnState));
		}

		return ServiceUtils.searchCards(serviceBean, search, null);
	}
}
