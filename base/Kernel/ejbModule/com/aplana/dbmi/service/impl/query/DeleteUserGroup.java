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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to remove one {@link Group} from user's profile.
 */
public class DeleteUserGroup extends ObjectQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of action 'Delete user group' to be written in system log
	 */
	public static final String EVENT_ID = "DEL_GROUP";
	
	/**
	 * @return {@link #EVENT_ID} 
	 */
	public String getEvent()
	{
		return EVENT_ID;
	}

	protected AccessRuleManager manager;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	/**
	 * Deletes one record from PERSON_ROLE_GROUP table and all member roles FROM PERSON_ROLE table 
	 * (if such roles are not assigned to user as ungrouped roles.<br>
	 * @throws DataException if error was caught during group revoking.
	 * @return null
	 */
	public Object processQuery() throws DataException
	{
		// ������� �� ��� ��� ��� ����������� ...
		final GroupInfo info = getUserGroupInfo( (Long) getId().getId(), getJdbcTemplate());

		final ObjectId userId = new ObjectId( Person.class, info.personId);
		checkLock(userId);

		@SuppressWarnings("unchecked")
		// ������� ���� �� ������� person_role, �������� � ������ ��������� ������, ���� ��� �� ������ � ������ ������ �����, ����������� ������������, � �� ��������� ������������ �������������.
		List<Role> roleListForDelete = getJdbcTemplate().query(
				"SELECT pr.prole_id FROM person_role pr " +
				"WHERE pr.person_id=? " +
				"AND pr.role_code IN (SELECT gr.role_code FROM group_role gr WHERE gr.group_code = (SELECT prg.group_code FROM person_role_group prg WHERE prg.prgroup_id=?)) " +
				"AND pr.role_code NOT IN (SELECT pur.role_code FROM person_ungrouped_role pur WHERE pur.person_id=? " +
					"UNION " +
					"SELECT gr1.role_code FROM group_role gr1 " +
					"WHERE gr1.group_code IN (SELECT prg1.group_code FROM person_role_group prg1 WHERE prg1.prgroup_id<>? AND prg1.person_id=?))",
				new Object[] { userId.getId(), getId().getId(), userId.getId(), getId().getId(), userId.getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC },
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
		
		getJdbcTemplate().update("DELETE FROM person_excluded_role WHERE prgroup_id = ?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }
				);
		getJdbcTemplate().update(
				"DELETE FROM person_role_group WHERE prgroup_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }
				);
		
		return null;
	}
	
	/**
	 * ��������� ��� ��� ���������� �� ��������� ������ � ������������...
	 */
	static class GroupInfo 
	{
		long personId;
		String groupCode;
		public GroupInfo() {
		}
	}

	/**
	 * �������� ���������� � ���������������� ������.
	 * @param userGroupId
	 * @param jdbc
	 * @return GroupInfo
	 */
	final static GroupInfo getUserGroupInfo(Long userGroupId, JdbcTemplate jdbc)
	{
		final GroupInfo info = (GroupInfo) jdbc.queryForObject(
				"SELECT p.person_id, prg.group_code \n" +
				"FROM person_role_group prg \n" +
				"		INNER JOIN person p ON prg.person_id=p.person_id \n" +
				"WHERE prg.prgroup_id=? ",
				new Object[] { userGroupId},
				new int[] { Types.NUMERIC},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException 
					{
						final GroupInfo result = new GroupInfo();
						result.personId = rs.getLong(1);
						result.groupCode = rs.getString(2);
						return result;
					}}
				);
		return info;
	}
}
