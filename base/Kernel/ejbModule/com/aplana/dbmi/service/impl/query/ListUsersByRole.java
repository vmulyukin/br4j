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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * 
 * @author ppolushkin
 *
 */

public class ListUsersByRole extends ChildrenQueryBase {

	@Override
	public Object processQuery() throws DataException {
		
		final ObjectId roleId = getParent();
		
		if(roleId.getId() == null) return null;
		
		final List<Person> result = new ArrayList<Person>();
		getJdbcTemplate().query(
				"SELECT p.person_id, p.person_login, p.full_name, p.email, \n" +	// 1 - 4
					"p.sync_date, p.is_active, p.locked_by, p.lock_time, \n" + 		// 5 - 8
					"p.card_id \n" +												// 9
				"FROM person p join person_role pr on pr.person_id = p.person_id \n" +
				"WHERE pr.role_code = ?",
				new Object[] { roleId.getId() },
				new int[] { Types.VARCHAR },
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
						result.add(person);
						return null;
					}
				});
		
		return result;
	}

}
