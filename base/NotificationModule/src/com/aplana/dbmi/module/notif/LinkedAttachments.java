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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class LinkedAttachments extends DataServiceClient implements AttachmentsSource
{
	private String attribute;

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public Collection getAttachments(NotificationObject object) {
		Map cards = new HashMap();
		loadCards(object, cards);
		ArrayList materials = new ArrayList(cards.size());
		for (Iterator itr = cards.values().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			materials.add(new MaterialDataSource(card.getId(), getDefaultCaller()));
		}
		return materials;
	}

	protected void loadCards(NotificationObject object, Map cards){
		ObjectId cardId = ((SingleCardNotification) object).getCard().getId();
		loadCard(cards, cardId);
		String[] part = attribute.split(":");
		for (int i = 0; i < part.length; i++) {
			for (Iterator itr = cards.values().iterator(); itr.hasNext(); ) {
				Card card = (Card) itr.next();
				itr.remove();
				ObjectId attrId = ObjectId.predefined(CardLinkAttribute.class, part[i]);
				if (attrId == null)
					attrId = ObjectId.predefined(BackLinkAttribute.class, part[i]);
				if (attrId == null)
					attrId = new ObjectId(CardLinkAttribute.class, part[i]);
				Attribute attr = card.getAttributeById(attrId);
				if (attr == null)
					attr = card.getAttributeById(new ObjectId(BackLinkAttribute.class, part[i]));
				if (attr == null) {
					logger.warn("Attribute " + part[i] + " not found in card " +
							card.getId().getId() + "; skipped");
					continue;
				}

				if (Attribute.TYPE_CARD_LINK.equals(attr.getType())) {
					Collection idsLinks = ((CardLinkAttribute) attr).getIdsLinked();
					if (idsLinks != null) {
						for (Iterator iterId = idsLinks.iterator(); iterId.hasNext(); ) {
							loadCard(cards, (ObjectId) iterId.next());
						}
					}
				} else /*if (Attribute.TYPE_BACK_LINK.equals(attr.getType()))*/	{
					ListProject list = new ListProject();
					list.setAttribute(attrId);
					list.setCard(card.getId());
					try {
						ActionQueryBase linkQuery = getQueryFactory().getActionQuery(ListProject.class);
						linkQuery.setAccessChecker(null);
						linkQuery.setAction(list);
						SearchResult links = (SearchResult) getDatabase().executeQuery(getSystemUser(), linkQuery);
						if (links != null && links.getCards() != null) {
							for (Iterator linkItr = links.getCards().iterator();
								linkItr.hasNext();) {
								Card link = (Card) linkItr.next();
								loadCard(cards, link.getId());
							}
						}

					} catch (DataException e) {
						logger.warn("Error fetching cards linked to card " + card.getId().getId() +
								" (attribute " + part[i] + "); skipped");
						continue;
					}
				}
			}
		}
	}

	private void loadCard(Map map, ObjectId cardId)
	{
		if (map.containsKey(cardId))
			return;

		try {
			ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			cardQuery.setAccessChecker(null);
			cardQuery.setId(cardId);
			Card card = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
			map.put(cardId, card);
		} catch (DataException e) {
			logger.warn("Error fetching card " + cardId.getId() + "; skipped", e);
		}
	}

	private QueryFactory outerGetQueryFactory() {
		return getQueryFactory();
	}

	private Database outerGetDatabase() {
		return getDatabase();
	}

	protected ProcessorBase getDefaultCaller(){
	    return new FakeCaller();
	}

	private class FakeCaller extends ProcessorBase
	{
		public FakeCaller(){
		}

		public Object process() throws DataException {
			throw new UnsupportedOperationException("Should never be called");
		}

		public Database getDatabase() {
			return outerGetDatabase();
		}

		public QueryFactory getQueryFactory() {
			return outerGetQueryFactory();
		}

		public UserData getUser() {
			try {
				return getSystemUser();
			} catch (DataException e) {
				logger.error("Error authenticating system", e);
				return null;
			}
		}

	}
}
