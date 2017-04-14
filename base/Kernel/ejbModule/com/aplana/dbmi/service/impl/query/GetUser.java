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
import java.util.Collection;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Person} instance from database
 */
public class GetUser extends ObjectQueryBase
{
	/**
	 * Fetches single {@link Person} instance from database
	 * @return {@link Person} object with roles collection initialized 
	 */
	public Object processQuery() throws DataException
	{
		if (Person.ID_CURRENT.equals(getId()))
			setId(getUser().getPerson().getId());
		Person person = (Person) getJdbcTemplate().queryForObject(
				"SELECT p.person_id, p.person_login, p.full_name, p.email, " +	// 1 - 4
					"p.sync_date, p.is_active, p.locked_by, p.lock_time, " + 	// 5 - 8
					"p.card_id " +												// 9
				"FROM person p " +
				"WHERE person_id = ?",
				new Object[] { getId().getId() },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Person person = new Person();
						person.setId(rs.getLong(1));
						person.setLogin(rs.getString(2));
						person.setFullName(rs.getString(3));
						person.setEmail(rs.getString(4));
						person.setSyncDate(rs.getDate(5));
						person.setActive(rs.getBoolean(6));
						if (rs.getObject(7) != null) {
							person.setLocker(rs.getLong(7));
							person.setLockTime(rs.getDate(8));
						}
						if (rs.getObject(9) != null) {
							person.setCardId(new ObjectId(Card.class, rs.getLong(9)));
						}
						return person;
					}
				});
		ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Person.class, Role.class);
		subQuery.setParent(getId());
		person.setRoles((Collection) getDatabase().executeQuery(getUser(), subQuery));
		
		subQuery = getQueryFactory().getChildrenQuery(Person.class, Group.class);
		subQuery.setParent(getId());
		person.setGroups((Collection) getDatabase().executeQuery(getUser(), subQuery));
			/*ListUserRoles subQuery = new ListUserRoles();
			subQuery.setJdbcTemplate(getJdbcTemplate());
			subQuery.setAccessChecker(getAccessChecker());
			subQuery.setUser(getUser());
			subQuery.setParent(getId());
			person.setRoles((Collection) subQuery.processQuery());*/
		return person;
	}
}
