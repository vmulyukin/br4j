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
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to save {@link Group} object instances.
 * Create/Updates single row in GROUP_ROLE table as well as its children in
 * PERSON_GROUP_ROLE tables. All group roles are copied to PERSON_ROLE table.
 */
public class SaveUserGroup extends SaveQueryBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'New group added' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_GROUP";
	/**
	 * Identifier of 'Group changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_GROUP";

	private Group group;
	protected AccessRuleManager manager;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	/**
	 * @return {@link #EVENT_ID_CREATE} if new {@link Group} object is saved,
	 * {@link #EVENT_ID_CHANGE} otherwise.
	 */
	public String getEvent()
	{
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	/**
	 * @return identifier of {@link Person} whose group is being saved
	 */
	public ObjectId getEventObject()
	{
		return ((Group) getObject()).getPerson();
	}

	protected ObjectId processNew() throws DataException
	{
		group = (Group) getObject();
		
		checkLock(group.getPerson());
		
		group.setId(getJdbcTemplate().queryForLong("SELECT nextval('seq_pr_group_id')"));
		getJdbcTemplate().update(
				"INSERT INTO person_role_group (prgroup_id, person_id, group_code) VALUES (?, ?, ?)",
				new Object[] { group.getId().getId(), group.getPerson().getId(), group.getType() },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);

		Set<String> excludedRoles = group.getExcludedRoles();
		if (excludedRoles != null) {
			for (String roleCode : excludedRoles) {
				getJdbcTemplate().update(
						"INSERT INTO person_excluded_role (prgroup_id, role_code) VALUES (?, ?)",
						new Object[] { group.getId().getId(), roleCode },
						new int[] { Types.NUMERIC, Types.VARCHAR }
						);
			}
		}
		
		insertGroupRoles(group);

		return group.getId();
	}

	protected void processUpdate() throws DataException
	{
		group = (Group) getObject();
	
		final ObjectId userId = group.getPerson();

		checkLock(userId);

		// Refresh excluded group roles list
		getJdbcTemplate().update("DELETE FROM person_excluded_role WHERE prgroup_id = ?",
				new Object[] { group.getId().getId() },
				new int[] { Types.NUMERIC }
				);

		Set<String> excludedRoles = group.getExcludedRoles();

		if (excludedRoles != null) {
			for (String roleCode : excludedRoles) {
				getJdbcTemplate().update(
						"INSERT INTO person_excluded_role (prgroup_id, role_code) " + 
						"SELECT ?, ? " +
						"WHERE NOT EXISTS(SELECT 1 FROM person_excluded_role per WHERE per.prgroup_id = ? AND per.role_code = ?)",
						new Object[] { group.getId().getId(), roleCode, group.getId().getId(), roleCode },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR }
						);
			}
		}
		// ������� ���� �� ������� person_role, ���� ��� ��������� ��� ������� �� ������� ������ � �� ��������� ������������ �������������.
		List<Role> roleListForDelete = getJdbcTemplate().query(
				"SELECT pr.prole_id FROM person_role pr " +
				"WHERE pr.person_id = ? " +
				"AND ((pr.role_code IN (SELECT per.role_code FROM person_excluded_role per WHERE per.prgroup_id = ?) " +
				"OR pr.role_code NOT IN (SELECT gr.role_code FROM group_role gr INNER JOIN person_role_group prg ON gr.group_code=prg.group_code and prg.person_id=?)) " +
				"AND pr.role_code NOT IN (SELECT pur.role_code FROM person_ungrouped_role pur WHERE pur.person_id = ?)) ",
				new Object[] { userId.getId(), group.getId().getId(), userId.getId(), userId.getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC },
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
		insertGroupRoles(group);
	}
	
	public void insertGroupRoles(Group group) {
		@SuppressWarnings("unchecked")
		List<SystemRole> groupRoleList = getJdbcTemplate().query(
				"SELECT gr.role_code " +
				"FROM group_role gr " +
				"WHERE gr.group_code=?",
				new Object[] { group.getSystemGroup().getId().getId() },
				new int[] { Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						ObjectId systemRoleId = new ObjectId(SystemRole.class, rs.getString(1));
						SystemRole systemRole = (SystemRole)DataObject.createFromId(systemRoleId);
						return systemRole;
					}
				});
		
		for (SystemRole role : groupRoleList) {
			int count = getJdbcTemplate().queryForInt(
					"SELECT COUNT(1) FROM person_role pr WHERE pr.role_code = ? AND pr.person_id = ?",
					new Object[] { role.getId().getId(), group.getPerson().getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
					);
			if (count == 0) {
				long id = getJdbcTemplate().queryForLong("SELECT nextval('seq_prole_id')");
				getJdbcTemplate().update(
					"INSERT INTO person_role (prole_id, person_id, role_code) SELECT ? as prole_id, ? as person_id, ? as role_code " +
					"WHERE NOT EXISTS (SELECT 1 FROM person_excluded_role per WHERE per.prgroup_id = ? AND per.role_code = ?)",
					new Object[] { id, group.getPerson().getId(), role.getId().getId(), group.getId().getId(), role.getId().getId() },
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR }
					);
				manager.updateAccessByRole(new ObjectId(Role.class, id));
			}
		}
	}
}
