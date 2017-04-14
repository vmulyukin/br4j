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
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class PersonFlagsForCommissionChecker extends DataServiceClient implements PersonNotifyChecker {
	
	private static final ObjectId SETTINGS_CARD_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.settings.card");
	private static final ObjectId NOTIF_EVENTS = ObjectId.predefined(TreeAttribute.class, "notification.events");
	
	private ObjectId controlledFlagId;
	private ObjectId nonControlledFlagId;
	private ObjectId eventAttribute;
	
	public void setControlledFlag(String flag) {
		ObjectId flagId = ObjectId.predefined(ReferenceValue.class, flag);
		if (flagId == null || !ReferenceValue.class.equals(flagId.getType()))
			throw new IllegalArgumentException("controlledFlagId must be a reference value id");
		this.controlledFlagId = flagId;
	}
	
	public void setNonControlledFlag(String flag) {
		ObjectId flagId = ObjectId.predefined(ReferenceValue.class, flag);
		if (flagId == null || !ReferenceValue.class.equals(flagId.getType()))
			throw new IllegalArgumentException("nonControlledFlagId must be a reference value id");
		this.nonControlledFlagId = flagId;
	}

	public void setEventAttribute(String eventAttribute) {
		this.eventAttribute = ObjectId.predefined(TreeAttribute.class, eventAttribute);
	}

	@Override
	public boolean checkNotify(Person person, NotificationObject object) {
		if (controlledFlagId == null || nonControlledFlagId == null)
			throw new IllegalStateException("Both controlledFlagId and nonControlledFlagId must be set before use");
		if (person.getCardId() == null) {
			if(logger.isWarnEnabled())
				logger.warn("Person " + person.getFullName() + " (id " + person.getId().getId() + ") has no card");
			return false;
		}
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(person.getCardId());
			Card card = (Card) getDatabase().executeQuery(getSystemUser(), query);
			
			final ObjectId settingsCardId = ((CardLinkAttribute) card.getAttributeById(SETTINGS_CARD_ID)).getSingleLinkedId();
			if(settingsCardId == null) {
				if(logger.isErrorEnabled())
					logger.error("Attribute " + SETTINGS_CARD_ID + " for card " + card.getId().getId() + " is null");
				return false;
			}
			query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(settingsCardId);
			Card settingsCard = (Card) getDatabase().executeQuery(getSystemUser(), query);
			if(eventAttribute==null){
				eventAttribute = NOTIF_EVENTS;
			}
			TreeAttribute flags = (TreeAttribute) settingsCard.getAttributeById(eventAttribute);
			if(flags == null) {
				if(logger.isErrorEnabled())
					logger.error("Attribute " + NOTIF_EVENTS + " for card " + settingsCard.getId().getId() + " is null");
				return false;
			}
			return flags.hasValue(isControlledCommission(object) ? controlledFlagId : nonControlledFlagId);
		} catch (DataException e) {
			if(logger.isErrorEnabled())
				logger.error("Error fetching card " + person.getCardId().getId() +
					" for " + person.getFullName() + " (id " + person.getId().getId() + ")", e);
			return false;
		}
	}

	private boolean isControlledCommission(NotificationObject object) throws DataException {
		if (!SingleCardNotification.class.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("This checker can be used only for card notifications");
		
		Card card = ((SingleCardNotification) object).getCard();
		if (card.getTemplate() == null) {		// seems that card is not loaded
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(card.getId());
			card = (Card) getDatabase().executeQuery(getSystemUser(), query);
		}
		if(card.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.report.internal"))){
			ObjectId resolutionId = card.getCardLinkAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent")).getSingleLinkedId();
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(resolutionId);
			card = (Card) getDatabase().executeQuery(getSystemUser(), query);
		}
		ListAttribute attr = (ListAttribute) card.getAttributeById(
				ObjectId.predefined(ListAttribute.class, "jbr.oncontrol"));
		if(attr.getValue() == null) {
			return false;
		}
		return ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes")
				.equals(attr.getValue().getId());
	}

	@Override
	public Collection<Person> checkNotify(Collection<Person> sourcePersons,
			NotificationObject object) {
		Set<Person> result = new HashSet<Person>();
		for(Person p: sourcePersons){
			if(checkNotify(p,object)){
				result.add(p);
			}
		}
		return result;
	}
}
