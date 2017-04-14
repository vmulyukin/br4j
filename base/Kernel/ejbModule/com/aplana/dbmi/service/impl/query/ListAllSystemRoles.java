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

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.model.filter.SystemRolesFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Query used to fetch all {@link SystemRole} instances from database
 */
public class ListAllSystemRoles extends QueryBase {
	/**
	 * Fetches all {@link SystemRole} objects from database
	 * @return List of {@link SystemRole} objects representing all record from
	 * SYSTEM_ROLE table, ordered by name.
	 */
	public Object processQuery() throws DataException {
		List result = getJdbcTemplate().query(
				getSqlQuery(getFilter()),
				getRowMapper()
		);
		// sorting result list
		Collections.sort(
			result,
			new Comparator<SystemRole>() {
				public int compare(SystemRole role1, SystemRole role2) {
					if (role1.getName() == null) {
						return -1;
					} else {
						return role1.getName().compareTo(role2.getName());
					}
				}
			}
		);
		return result;
	}

	private String getSqlQuery (Filter filter) {
		String sql = "select role_code, role_name_eng, role_name_rus from system_role ";
		if (filter != null && filter instanceof SystemRolesFilter) {
			sql += ((SystemRolesFilter)filter).getConstraint();
		}
		return sql;
	}

	private RowMapper getRowMapper () {
		return new RowMapper() {
			public Object mapRow(ResultSet rs, int index)
					throws SQLException {
				ObjectId id = new ObjectId(SystemRole.class, rs.getString(1));
				SystemRole result = DataObject.createFromId(id);
				result.setNameEn(rs.getString(2));
				result.setNameRu(rs.getString(3));
				return result;
			}
		};
	}

	@Override
	protected boolean supportsFilter(Class<?> type) {
		return SystemRolesFilter.class.equals(type);
	}

}


