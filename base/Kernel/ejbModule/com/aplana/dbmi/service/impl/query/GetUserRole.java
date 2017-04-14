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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Role} instance from database
 */
public class GetUserRole extends ObjectQueryBase {

	/**
	 * Fetches single {@link Role} instance from database from database
	 * @return fully initialized {@link Role} instance
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().queryForObject(
				"SELECT r.prole_id, r.person_id, r.role_code, s.role_name_rus, s.role_name_eng " +
				"FROM person_role r INNER JOIN system_role s ON r.role_code=s.role_code " +
				"WHERE r.prole_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Role role = new Role();
						role.setId(rs.getLong(1));
						role.setPerson(new ObjectId(Person.class, rs.getLong(2)));

						ObjectId systemRoleId = new ObjectId(SystemRole.class, rs.getString(3));
						SystemRole systemRole = (SystemRole)DataObject.createFromId(systemRoleId);
						systemRole.setNameRu(rs.getString(4));
						systemRole.setNameEn(rs.getString(5));
						role.setSystemRole(systemRole);
						return role;
					}
				});
	}
}
