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
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Fetches all {@link PersonalSearch} objects which belongs to user, who performs this query
 */
public class ListStoredSearches extends QueryBase
{
	/**
	 * Fetches all {@link PersonalSearch} objects which belongs to user, who performs this query
	 * @return list of all {@link PersonalSearch} objects belonging to user who performs this query
	 */
	public Object processQuery() throws DataException
	{
		ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Person.class, PersonalSearch.class);
		subQuery.setParent(getUser().getPerson().getId());
		return getDatabase().executeQuery(getUser(), subQuery);
			/*ListUserSearches query = new ListUserSearches();
			query.setJdbcTemplate(getJdbcTemplate());
			query.setAccessChecker(getAccessChecker());
			query.setUser(getUser());
			query.setParent(getUser().getPerson().getId());
			return query.processQuery();*/
	}
}
