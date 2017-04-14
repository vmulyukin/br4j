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
import java.util.Iterator;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class SimpleReminderManager extends DataServiceClient implements DeliverySource {
	
	public static final String VAR_CARD = "card";
	//public static final String VAR_USER = "user";
	
	private String searchFile;
	private NotificationBean notifier;

	public void setSearchFile(String searchFile) {
		this.searchFile = searchFile;
	}

	public void setNotifier(NotificationBean notifier) {
		this.notifier = notifier;
	}

	@Override
	public Collection<NotificationObject> listDeliveries() {
		SearchResult result = searchCards();
		ArrayList<NotificationObject> notifications =
				new ArrayList<NotificationObject>(result.getCards().size());
		for (Iterator<Card> itr = result.getCards().iterator(); itr.hasNext(); ) {
			Card card = itr.next();
			SingleCardNotification notif = new SingleCardNotification();
			notif.setCard(card);
			notifications.add(notif);
		}
		return notifications;
	}

	@Override
	public NotificationBean getNotifier(NotificationObject delivery) {
		return notifier;
	}

	@Override
	public boolean buildDelivery(NotificationObject delivery) {
		return true;
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
}
