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
package com.aplana.dbmi.service.impl.access;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.AccessCheckerBase;

/**
 * Access checker used to restrict access for 'personal' objects 
 * (subscriptions, personal searches, etc.) 
 */
public class PersonalArea extends AccessCheckerBase
{
	public boolean checkAccess() throws DataException
	{
		DataObject obj = getObject();
		String table;
		String idField;
		if (obj instanceof Subscription) {
			table = "notification_rule";
			idField = "notif_rule_id";
		} else if (obj instanceof PersonalSearch) {
			table = "person_search";
			idField = "person_search_id";
		} else if (obj instanceof Card) {
			table = "person_card";
			idField = "card_id";
		} else
			throw new RuntimeException("Not a personal object");
		if (obj.getId() == null)
			return true;	// Anyone can create personal objects
		try {
			long userId = getJdbcTemplate().queryForLong(
					"SELECT person_id FROM " + table + " WHERE " + idField + "=?",
					new Object[] { obj.getId().getId() });
			return new ObjectId(Person.class, userId).equals(getUser().getPerson().getId());
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DataException("access.personal.notfound", new Object[] { obj.getId() });
		}
	}
}
