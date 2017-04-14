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
package com.aplana.dbmi.module.notif.selector;

import org.apache.commons.lang.time.DateUtils;

import sun.security.jca.GetInstance.Instance;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.module.notif.DataServiceClient;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Selector;

public abstract class ReportSelector extends DataServiceClient implements Selector {
	
	private static final ObjectId RESOLUTION_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent");
	private static final ObjectId REPORT_EXECUTOR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor");
	
	@Override
	public boolean satisfies(Object object) {
		try {
			Card reportCard;
			if(object instanceof Card){
				reportCard = (Card) object;
			} else if (object instanceof ChangeState){
				reportCard = ((ChangeState) object).getCard();
			} else return false;
			 
			
			Card resolutionCard = getLinkedCard(reportCard, RESOLUTION_ID);
			
			PersonAttribute reportCardExecutor = (PersonAttribute) reportCard.getAttributeById(REPORT_EXECUTOR_ID);
			PersonAttribute resolutionCardPerson = (PersonAttribute) resolutionCard.getAttributeById(getPersonAttributeId());
			if(resolutionCardPerson.getValues().contains(reportCardExecutor.getPerson())){
				logger.debug("Satisfied for card " +reportCard.getId() + " and PersonAttributeId " + getPersonAttributeId());
				return true;
			} else {
				logger.debug("Not satisfied for card " +reportCard.getId() + " and PersonAttributeId " + getPersonAttributeId());
				return false;
			}

		} catch (Exception e) {
			logger.warn("Error trying to determine whether commission's term was changed or not", e);
			return false;
		}
	}
	
	public Card getLinkedCard(Card card, ObjectId attrId) {
		if (!CardLinkAttribute.class.equals(attrId.getType()) &&
				!BackLinkAttribute.class.equals(attrId.getType()) &&
				!TypedCardLinkAttribute.class.equals(attrId.getType()))
			throw new IllegalArgumentException("Not a link attribute id");
		Attribute attr = card.getAttributeById(attrId);
		if (attr == null) {
			logger.warn("Attribute " + attrId.getId() + " doesn't exist in card " + card.getId().getId());
			return null;
		}
		ObjectId cardId = null;
		if (CardLinkAttribute.class.equals(attrId.getType()) || TypedCardLinkAttribute.class.equals(attrId.getType())) {
			final CardLinkAttribute linkAttr = (CardLinkAttribute) attr;
			final int c = linkAttr.getLinkedCount();
			if (c > 0) {
				if (c > 1)
					logger.warn(c + " cards linked to card " + card.getId().getId() + "; using first one");
				cardId = linkAttr.getSingleLinkedId();
			}
		
		} else {
			ListProject linked = new ListProject(card.getId());
			linked.setAttribute(attrId);
			try {
				ActionQueryBase query = getQueryFactory().getActionQuery(linked);
				query.setAction(linked);
				SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
				if (result.getCards().size() > 0) {
					if (result.getCards().size() > 1)
						logger.warn(result.getCards().size() + " cards linked to card " +
								card.getId().getId() + "; using first");
					cardId = ((Card) result.getCards().iterator().next()).getId();
				}
			} catch (DataException e) {
				logger.error("Error querying linked cards for card " + card.getId().getId(), e);
			}
		}
		if (cardId == null) {
		    return null;
		}
		return getCard(cardId);
	}

	/**
	 * Fetches a card from database.
	 *
	 * @param cardId ID of card to be fetched
	 * @return Card object with all attributes
	 */
	public Card getCard(ObjectId cardId) {
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id");
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(cardId);
			return (Card) getDatabase().executeQuery(getSystemUser(), query);
		} catch (DataException e) {
			logger.error("Error fetching card " + cardId.getId(), e);
			return null;
		}
	}

	protected abstract ObjectId getPersonAttributeId();
}
