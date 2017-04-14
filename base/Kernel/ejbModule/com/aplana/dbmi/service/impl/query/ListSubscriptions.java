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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query user to fetch all {@link Subscription} objects which belongs to user who performs this query
 */
public class ListSubscriptions extends QueryBase 
{
	/**
	 * Fetches all {@link Subscription} objects which belongs to user who performs this query
	 * @return list containing  all {@link Subscription} objects which belongs to user who performs this query
	 */
	public Object processQuery() throws DataException
	{
		ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Person.class, Subscription.class);
		subQuery.setParent(getUser().getPerson().getId());
		return getDatabase().executeQuery(getUser(), subQuery);
			/*ListUserSubscriptions query = new ListUserSubscriptions();
			query.setJdbcTemplate(getJdbcTemplate());
			query.setAccessChecker(getAccessChecker());
			query.setUser(getUser());
			query.setParent(getUser().getPerson().getId());
			return query.processQuery();*/
	}
}
