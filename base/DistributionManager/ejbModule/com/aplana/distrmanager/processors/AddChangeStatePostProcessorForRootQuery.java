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
package com.aplana.distrmanager.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.jbr.processors.CheckAndDoDependentChangeState;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.QueryBase;

public class AddChangeStatePostProcessorForRootQuery extends ProcessCard implements Parametrized {
	private static final long serialVersionUID = 1L;

	private Map<String, String> parameters = new HashMap<String, String>();

	@Override
	public Object process() throws DataException {

		CheckAndDoDependentChangeStateForGivenCard processor = new CheckAndDoDependentChangeStateForGivenCard();
		for (Entry<String, String> param : parameters.entrySet()) {
			processor.setParameter(param.getKey(), param.getValue());
		}
		processor.setCardId(getCardId());
		processor.setBeanFactory(getBeanFactory());

		QueryBase primaryQuery = getPrimaryQuery();
		if (primaryQuery != null) {
			primaryQuery.addPostProcessor(processor);
		} else {
			getCurrentQuery().addPostProcessor(processor);
		}

		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}

	private static class CheckAndDoDependentChangeStateForGivenCard extends CheckAndDoDependentChangeState {

		private static final long serialVersionUID = 1L;
		private ObjectId cardId;
		private Card currentCard;
		private ObjectId baseCardLinkId;
		private ObjectId dependentCardLinkId;
		protected final Log logger = LogFactory.getLog(getClass());

		public CheckAndDoDependentChangeStateForGivenCard() {
		}

		@Override
		public void setParameter(String name, String value) {
			if ("compareCardLinks".equals(name)) {
				String[] linkIds = value.split("=");
				if (linkIds.length != 2) {
					throw new IllegalArgumentException("Illegal parameter [" + value + "] for key [" + value
							+ "]. It should be in 'attrId=attrId' format");
				}
				dependentCardLinkId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, linkIds[0], false);
				baseCardLinkId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, linkIds[1], false);
				if (dependentCardLinkId == null || baseCardLinkId == null) {
					throw new IllegalArgumentException("Illegal parameter [" + value + "] for key [" + value
							+ "]. Cardlink attribute id was not parsed");
				}
			} else {
				super.setParameter(name, value);
			}
		}

		public void setCardId(ObjectId cardId) {
			this.cardId = cardId;
			this.currentCard = null;
		}

		@Override
		protected ObjectId getCardId() {
			ObjectId currCardId = null;
			if (this.cardId == null) {
				currCardId = super.getCardId();
			} else {
				currCardId = this.cardId;
			}
			if (currentCard != null && !currentCard.getId().equals(currCardId)) {
				currentCard = null;
			}
			return currCardId;
		}

		@Override
		protected Card getActionCard() {
			if (currentCard == null) {
				ObjectId currCardId = getCardId();
				try {
					currentCard = fetchCard(currCardId);
				} catch (DataException ex) {
					logger.error("Error during fetch card", ex);
					currentCard = (Card) DataObject.createFromId(currCardId);
				}
			}
			return currentCard;
		}

		@Override
		protected boolean checkDependentCard(Card card) throws DataException {
			if (card.getState() != null && sourceStateIds != null && !sourceStateIds.contains(card.getState())) {
				return false;
			}

			if (!super.checkDependentCard(card)) {
				return false;
			}
			ObjectId id = card.getId();
			if (baseCardLinkId == null && dependentCardLinkId == null) {
				return true;
			}
			Card currCard = getActionCard();
			Card linkedCard = fetchCard(id);
			Collection<ObjectId> currCardIds = CardUtils.getAttrLinks(currCard, baseCardLinkId);
			Collection<ObjectId> linkedCardIds = CardUtils.getAttrLinks(linkedCard, dependentCardLinkId);
			return ObjectIdUtils.isIntersectionDataObjects(currCardIds, linkedCardIds);
		}

		private Card fetchCard(ObjectId id) throws DataException {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(id);
			return (Card) getDatabase().executeQuery(getSystemUser(), query);
		}
	}

}
