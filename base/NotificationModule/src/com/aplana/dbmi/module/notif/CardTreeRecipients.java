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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CardTreeRecipients extends DataServiceClient implements RecipientGroup {

	private ObjectId linkAttrId;
	private ObjectId rootAttrId;
	private ObjectId personAttrId;
	private CardFilter filter;
	
	private ObjectQueryBase cardQuery;
	
	public void setLinkAttrId(ObjectId linkAttrId) {
		if (!BackLinkAttribute.class.equals(linkAttrId.getType()))
			throw new IllegalArgumentException("linkAttrId must be a CardLinkAttribute ID");
		this.linkAttrId = linkAttrId;
	}
	
	public void setLinkAttr(String linkAttr) {
		setLinkAttrId(ObjectId.predefined(BackLinkAttribute.class, linkAttr));
	}
	
	public void setRootAttrId(ObjectId rootAttrId) {
		if (!BackLinkAttribute.class.equals(rootAttrId.getType()))
			throw new IllegalArgumentException("rootAttrId must be a BackLinkAttribute ID");
		this.rootAttrId = rootAttrId;
	}
	
	public void setRootAttr(String rootAttr) {
		setRootAttrId(ObjectId.predefined(BackLinkAttribute.class, rootAttr));
	}
	
	public void setPersonAttrId(ObjectId personAttrId) {
		if (!PersonAttribute.class.equals(personAttrId.getType()))
			throw new IllegalArgumentException("personAttrId must be a PersonAttribute ID");
		this.personAttrId = personAttrId;
	}
	
	public void setPersonAttr(String personAttr) {
		setPersonAttrId(ObjectId.predefined(PersonAttribute.class, personAttr));
	}
	
	public void setFilter(CardFilter filter) {
		this.filter = filter;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection discloseRecipients(NotificationObject object) {
		if (personAttrId == null || linkAttrId == null)
			throw new IllegalStateException("link & person attributes must be defined before use");
		if (!SingleCardNotification.class.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("This recipient group can only be used for card notifications");
		
		Card card = ((SingleCardNotification) object).getCard();
		Map<ObjectId, Card> cards;
		if (rootAttrId == null) {
			cards = Collections.singletonMap(card.getId(), card);
		} else {
			BackLinkAttribute attr = (BackLinkAttribute) card.getAttributeById(rootAttrId);
			if (attr == null) {
				logger.warn("Attribute " + rootAttrId.getId() + " not found in card " + card.getId().getId());
				return Collections.emptyList();
			}
			//���������� ��������
			cards = execListProject(card);
		}
		
		HashMap<ObjectId, Person> recipients = new HashMap<ObjectId, Person>();
		
			while (cards.size() > 0) {
				HashMap<ObjectId, Card> nextLevel = new HashMap<ObjectId, Card>();
				for (Iterator<Card> itr = cards.values().iterator(); itr.hasNext(); ) {
					card = itr.next();
					if (filter != null && !filter.isCardSuitable(card))
						continue;
					
					PersonAttribute users = (PersonAttribute) card.getAttributeById(personAttrId);
					if (users == null) {
						logger.warn("Attribute " + personAttrId.getId() + " not found in card " + card.getId().getId());
						continue;
					}
					for (Iterator<Person> itrUser = (Iterator<Person>) users.getValues().iterator(); itrUser.hasNext(); ) {
						Person user = itrUser.next();
						recipients.put(user.getId(), user);
					}
					
					BackLinkAttribute attr = (BackLinkAttribute) card.getAttributeById(linkAttrId);
					if (attr == null) {
						logger.warn("Attribute " + linkAttrId.getId() + " not found in card " + card.getId().getId());
						continue;
					}
					nextLevel.putAll(execListProject(card));
				}
				cards = nextLevel;
			}
		return recipients.values();
	}
	
	private Map<ObjectId, Card> execListProject(Card card) {
		final ListProject lp = new ListProject();
		lp.setAttribute(rootAttrId);
		lp.setCard(card.getId());
		List<Card> cr = null;
		try {
			ActionQueryBase searchQuery = getQueryFactory().getActionQuery(lp);
			searchQuery.setAction(lp);
			searchQuery.setUser(getSystemUser());
			final SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), searchQuery);
			cr = result.getCards();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap<ObjectId, Card>();
		}
		if(cr != null && !cr.isEmpty()) {
			Map<ObjectId, Card> cards = new HashMap<ObjectId, Card>(cr.size());
			for(Card c : cr)  {
				cards.put(c.getId(), c);
			}
			return cards;
		}
		return new HashMap<ObjectId, Card>();
	}
	
	/*private HashMap<ObjectId, Card> loadCards(Collection<ObjectId> ids) {
		if (cardQuery == null) {
			try {
				cardQuery = getQueryFactory().getFetchQuery(Card.class);
			} catch (DataException e) {
				throw new RuntimeException("Unexpected exception", e);		// Shall never be executed
			}
			cardQuery.setAccessChecker(null);
		}
		HashMap<ObjectId, Card> cards = new HashMap<ObjectId, Card>(ids.size());
		for (Iterator<ObjectId> itr = ids.iterator(); itr.hasNext(); ) {
			ObjectId id = itr.next();
			cardQuery.setId(id);
			try {
				cards.put(id, (Card) getDatabase().executeQuery(getSystemUser(), cardQuery));
			} catch (DataException e) {
				logger.warn("Error loading card " + id.getId());
			}
		}
		return cards;
	}*/

}
