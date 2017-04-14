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

import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class SubscriptionRecipients extends DataServiceClient implements RecipientGroup
{
	public Collection discloseRecipients(NotificationObject object) {
		Subscription subscr = (Subscription) ((StoredNotification) object).getNotification();
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
			query.setAccessChecker(null);
			query.setId(subscr.getPersonId());
			Person person = (Person) getDatabase().executeQuery(getSystemUser(), query);
			return Collections.singleton(person);
		} catch (DataException e) {
			logger.error("Error fetching subscription" + subscr.getId().getId() + "'s owner", e);
			return Collections.EMPTY_LIST;
		}
	}
}
