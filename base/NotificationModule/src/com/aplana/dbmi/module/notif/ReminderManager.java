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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class ReminderManager extends DataServiceClient implements DeliverySource
{
	public static final String VAR_USER = "user";
	public static final String VAR_CARDS = "cards";
	public static final String VAR_COLUMNS = "columns";
	
	private String searchFile;
	private Collection recipients;
	private NotificationBean notifier;
	
	public void setSearchFile(String name) {
		this.searchFile = name;
	}
	
	public void setRecipients(Collection recipients) {
		this.recipients = recipients;
	}
	
	public void setNotifier(NotificationBean notifier) {
		this.notifier = notifier;
		notifier.setRecipients(Collections.singleton(new RecipientGroup() {
			public Collection discloseRecipients(NotificationObject object) {
				return Collections.singleton(((Reminder) object).user);
			}
		}));
	}
	
	public boolean buildDelivery(NotificationObject delivery) {
		//Reminder reminder = (Reminder) delivery;
		return true;
	}

	public NotificationBean getNotifier(NotificationObject delivery) {
		return notifier;
	}

	public Collection<NotificationObject> listDeliveries() {
		SearchResult result = searchCards();
		if (result == null || result.getCards().size() == 0)
			return Collections.EMPTY_LIST;
		HashMap<ObjectId, NotificationObject> reminders = new HashMap<ObjectId, NotificationObject>();
		for (Iterator itr = result.getCards().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			SingleCardNotification notification = new SingleCardNotification();
			notification.setCard(card);
			for (Iterator itrGroups = recipients.iterator(); itrGroups.hasNext(); ) {
				RecipientGroup group = (RecipientGroup) itrGroups.next();
				for (Iterator itrUser = group.discloseRecipients(notification).iterator(); itrUser.hasNext(); ) {
					Person user = (Person) itrUser.next();
					Reminder reminder = (Reminder) reminders.get(user.getId());
					if (reminder == null) {
						reminder = new Reminder();
						reminder.user = user;
						reminder.columns = result.getColumns();
						reminders.put(user.getId(), reminder);
					}
					reminder.cards.add(card);
				}
			}
		}
		return reminders.values();
	}
	
	private SearchResult searchCards() {
		try {
			Search search = new Search();
			search.initFromXml(Portal.getFactory().getConfigService().loadConfigFile(searchFile));
			ActionQueryBase query = getQueryFactory().getActionQuery(Search.class);
			query.setAccessChecker(null);
			query.setAction(search);
			return (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
		} catch (DataException e) {
			logger.error("Error searching cards", e);
			return null;
		} catch (IOException e) {
			logger.error("Error reading search XML", e);
			return null;
		}
	}

	private static class Reminder extends NotificationObject
	{
		Person user;
		ArrayList cards = new ArrayList();
		Collection columns;
		
		@Override
		public Map<String, Object> getModel() {
			HashMap model = new HashMap();
			model.put(VAR_USER, user);
			model.put(VAR_CARDS, cards);
			model.put(VAR_COLUMNS, columns);
			return model;
		}
	}
}
