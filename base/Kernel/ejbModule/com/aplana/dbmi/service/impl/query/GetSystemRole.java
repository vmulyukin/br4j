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
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.UngroupedRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link SystemRole} instance from database
 */
public class GetSystemRole extends ObjectQueryBase {

	/**
	 * Fetches single {@link UngroupedRole} instance from database from database
	 * @return fully initialized {@link UngroupedRole} instance
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().queryForObject(
				"SELECT sr.role_code, sr.role_name_rus, sr.role_name_eng " +
				"FROM system_role sr " +
				"WHERE sr.role_code=?",
				new Object[] { getId().getId() },
				new int[] { Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						ObjectId systemRoleId = new ObjectId(SystemRole.class, rs.getString(1));
						SystemRole systemRole = (SystemRole)DataObject.createFromId(systemRoleId);
						systemRole.setRoleCode(rs.getString(1));
						systemRole.setNameRu(rs.getString(2));
						systemRole.setNameEn(rs.getString(3));
						return systemRole;
					}
				});
	}
}
