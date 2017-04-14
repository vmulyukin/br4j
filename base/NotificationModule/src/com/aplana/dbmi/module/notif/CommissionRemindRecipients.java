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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public abstract class CommissionRemindRecipients extends DataServiceClient implements RecipientGroup {

	private ObjectQueryBase cardQuery;
	private ObjectQueryBase personQuery;
	
	private HashMap<ObjectId, Card> profileCache;
	
	protected ObjectQueryBase getCardQuery() throws DataException {
		if (cardQuery == null) {
			cardQuery = getQueryFactory().getFetchQuery(Card.class);
			cardQuery.setAccessChecker(null);
		}
		return cardQuery;
	}
	
	protected ObjectQueryBase getPersonQuery() throws DataException {
		if (personQuery == null) {
			personQuery = getQueryFactory().getFetchQuery(Person.class);
			personQuery.setAccessChecker(null);
		}
		return personQuery;
	}
	
	protected Card getPersonProfile(ObjectId personId) {
		return profileCache.get(personId);
	}
	
	protected void clearProfileCache() {
		profileCache = null;
	}

	protected void addFilteredPersons(Map<ObjectId, Person> recipients,
			Collection<Person> persons, ObjectId flagId, PersonFilter filter) {
		if (profileCache == null) {
			profileCache = new HashMap<ObjectId, Card>();
		}
		for (Iterator<Person> itr = persons.iterator(); itr.hasNext(); ) {
			Person person = itr.next();
			try {
				ObjectQueryBase cardQuery = getCardQuery();
				cardQuery.setId(person.getCardId());
				Card personCard = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
				profileCache.put(person.getId(), personCard);
				TreeAttribute flags = (TreeAttribute) personCard.getAttributeById(
						ObjectId.predefined(TreeAttribute.class, "notification.events"));
				if (flags.hasValue(flagId) && (filter == null || filter.proceedPerson(person, personCard)))
					recipients.put(person.getId(), person);
			} catch (Exception e) {
				logger.warn("Error processing person " + person.getId().getId(), e);
				continue;
			}
		}
	}
	
	protected Collection<Person> getAssistants(Collection<Person> persons) {
		try {
			GetAssistants search = new GetAssistants();
			search.setChiefIds(persons);
			
			ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
			Collection<ObjectId> assistantIds =
					(Collection<ObjectId>) getDatabase().executeQuery(getSystemUser(), query);
			
			ArrayList<Person> assistants = new ArrayList<Person>(assistantIds.size());
			ObjectQueryBase personQuery = getPersonQuery();
			for (Iterator<ObjectId> itr = assistantIds.iterator(); itr.hasNext(); ) {
				ObjectId id = itr.next();
				personQuery.setId(id);
				Person person = (Person) getDatabase().executeQuery(getSystemUser(), personQuery);
				assistants.add(person);
			}
			return assistants;
		} catch (DataException e) {
			logger.warn("Error fetching assistants", e);
			return Collections.emptyList();
		}
	}
	
	protected boolean isCommissionOnControl(Card commission) {
		try {
			if (commission.getTemplate() == null) {		// seems that card is not loaded
				ObjectQueryBase query = getCardQuery();
				query.setId(commission.getId());
				commission = (Card) getDatabase().executeQuery(getSystemUser(), query);
			}
			ListAttribute attr = (ListAttribute) commission.getAttributeById(
					ObjectId.predefined(ListAttribute.class, "jbr.oncontrol"));
			return ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes")
					.equals(attr.getValue());
		} catch (Exception e) {
			logger.warn("Error reading commission card " + commission.getId().getId(), e);
			return false;
		}
	}
	
	protected interface PersonFilter {
		public boolean proceedPerson(Person person, Card profile);
	}
}
