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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.filter.UserIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all active {@link Person} objects defined in DBMI database.<br>
 * This query supports {@link UserIdFilter}.
 */
public class ListUsers extends QueryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Fetches all active {@link Person} objects defined in DBMI database.
	 * Result could be filtered by name if {@link UserIdFilter} is specified.
	 * @return list containing all active {@link Person} objects defined in DBMI database.
	 * If {@link UserIdFilter} is defined for query then result list will contain only users
	 * whose name or login matches given string pattern.
	 */
    
    protected String getSqlQuery() {
        String sql = "SELECT /*+INDEX(p)*/ " +
        "p.person_id, p.person_login, p.full_name, p.email, p.sync_date, p.is_active, " +   // 1-6
        "p.locked_by, p.lock_time, p.card_id " +                                            // 7-9
        "FROM person p " +
        "WHERE p.is_active=1 AND " + getFilterClause();
        
        return sql;
    }
    
    protected RowMapper getRowMapper() {
        return 
            new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    Person person = new Person();
                    person.setId(rs.getLong(1));
                    person.setLogin(rs.getString(2));
                    person.setFullName(rs.getString(3));
                    person.setEmail(rs.getString(4));
                    person.setSyncDate(rs.getTimestamp(5));
                    person.setActive(rs.getBoolean(6));
                    if (rs.getObject(7) != null) {
                        person.setLocker(rs.getLong(7));
                        person.setLockTime(rs.getTimestamp(8));
                    }
                    if (rs.getObject(9) != null) {
                        person.setCardId(new ObjectId(Card.class, rs.getLong(9)));
                    }
                    return person;
                }
            };
    }
    
    public Object processQuery() throws DataException {
		@SuppressWarnings("unchecked")
		List<Person> result = getJdbcTemplate().query(
				getSqlQuery(),
				getFilterParams(),
				getRowMapper());
		return processResult(result);
	}
	
	protected String getFilterClause()
	{
		Filter f = getFilter();
		if (f != null) {
			if (f instanceof UserIdFilter) {
				UserIdFilter filter = (UserIdFilter) f;
				if (filter.getString() != null || filter.getString().length() == 0) {
					return "(UPPER(p.person_login) LIKE ? OR UPPER(p.full_name) LIKE ?)";
				}
			} else if (f instanceof PersonCardIdFilter){
				PersonCardIdFilter filter = (PersonCardIdFilter) f;
				if (filter.getCardIds() != null && !filter.getCardIds().isEmpty()) {
					return "(p.card_id in (" + ObjectIdUtils.numericIdsToCommaDelimitedString(filter.getCardIds()) + "))";
				}
			}
		}
		return "(exists (select 1 from person_role pr where pr.person_id = p.person_id))";
	}
	
	private Object[] getFilterParams()
	{
		Filter f = getFilter();
		if (f != null) {
			if (f instanceof UserIdFilter) {
				UserIdFilter filter = (UserIdFilter) f;
				if (filter.getString() != null || filter.getString().length() == 0) {
					return new Object[] {
						filter.getString().toUpperCase() + "%",
						filter.getString().toUpperCase() + "%"
					};
				}
			}
		}
		return null;
	}
	
	private List<Person> processResult(List<Person> users) {
		if (getFilter() instanceof PersonCardIdFilter) {
			HashMap<ObjectId, Person> mapCardToUser = new HashMap<ObjectId, Person>();
			for (Iterator<Person> itr = users.iterator(); itr.hasNext(); ) {
				Person user = itr.next();
				mapCardToUser.put(user.getCardId(), user);
			}
			
			ArrayList<Person> sorted = new ArrayList<Person>(users.size());
			Collection<ObjectId> ids = ((PersonCardIdFilter) getFilter()).getCardIds();
			for (Iterator<ObjectId> itr = ids.iterator(); itr.hasNext(); ) {
				ObjectId id = itr.next();
				if (!mapCardToUser.containsKey(id)) {
					logger.warn("No person found for card " + id.getId());
					continue;
				}
				sorted.add(mapCardToUser.get(id));
			}
			return sorted;
		}
		return users;
	}

	protected boolean supportsFilter(Class<?> type)
	{
		return UserIdFilter.class.equals(type) || PersonCardIdFilter.class.equals(type);
	}
}
