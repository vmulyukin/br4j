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

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Notification;

public class StoredNotification extends NotificationObject {

	public static final String OBJ_NOTIFICATION = "notification";
	public static final String OBJ_COLUMNS = "columns";
	public static final String OBJ_CARDS = "cards";
	public static final String OBJ_LINKS = "links";
	
	private Notification notification;
	private SearchResult foundCards;
	
	@Override
	public Map<String, Object> getModel() {
		HashMap<String, Object> model = new HashMap<String, Object>();
		model.put(OBJ_NOTIFICATION, notification);
		model.put(OBJ_COLUMNS, foundCards.getColumns());
		model.put(OBJ_CARDS, foundCards.getCards());
		return model;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public SearchResult getFoundCards() {
		return foundCards;
	}

	public void setFoundCards(SearchResult foundCards) {
		this.foundCards = foundCards;
	}

}
