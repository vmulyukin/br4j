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
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

public class DeleteSystemRole extends DeleteAccessRule implements WriteQuery {

	protected AccessRuleManager manager;
	private String[] ruleTablesList = new String[] {"profile_access_rule", "role_access_rule", "person_access_rule"};
	
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	public Object processQuery() throws DataException {
		
		List<Role> roleListForDelete = getJdbcTemplate().query(
				"SELECT pr.prole_id FROM person_role pr " +
				"WHERE pr.role_code = ? ",
				new Object[] { this.getId().getId() },
				new int[] { Types.VARCHAR},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Role role = new Role();
						role.setId(rs.getLong(1));
						return role;
					}
				}
			);
		
		for (Role role : roleListForDelete) {
			manager.cleanAccessListByRole(role.getId());
			getJdbcTemplate().update(
					"DELETE FROM person_role WHERE prole_id=?",
					new Object[] { role.getId().getId() },
					new int[] { Types.NUMERIC }
					);
		}

		getJdbcTemplate().update(
				"DELETE FROM person_ungrouped_role WHERE role_code = ?",
				new Object[] { getId().getId() });

		getJdbcTemplate().update(
				"DELETE FROM person_excluded_role WHERE role_code = ?",
				new Object[] { getId().getId() });

		getJdbcTemplate().update(
				"DELETE FROM group_role WHERE role_code = ?",
				new Object[] { getId().getId() });
		
		getJdbcTemplate().update(
				"DELETE FROM attribute_view_param WHERE role_code = ?",
				new Object[] { getId().getId() });
		
		
		for (String ruleTable : ruleTablesList) {
			deleteRoleAccessRules(ruleTable);
		}

		getJdbcTemplate().update(
				"DELETE FROM system_role WHERE role_code = ?",
				new Object[] { getId().getId() });

		return null;
	}
	
	private void deleteRoleAccessRules(String ruleTable) {
		List<ObjectId> ruleIdsList = getJdbcTemplate().query(
				"SELECT ar.rule_id " +
				"FROM " + ruleTable + " ar " +
				"WHERE ar.role_code=?",
				new Object[] { getId().getId() },
				new int[] { Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return  new ObjectId(AccessRule.class, rs.getLong(1));
					}
				});
		
		for (ObjectId ruleId : ruleIdsList) {
			getJdbcTemplate().update(
					"DELETE FROM " + ruleTable + " WHERE rule_id = ?",
					new Object[] { ruleId.getId() });
			deleteOperationAndRule(ruleId);
		}
	}
}
