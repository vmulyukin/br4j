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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.UngroupedRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to remove one {@link UngroupedRole} from user's profile.
 * NOTE: Administrator is not allowed to remove 'Administrator' role from own profile. 
 */
public class DeleteUngroupedUserRole extends ObjectQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of action 'Delete user role' to be written in system log
	 */
	public static final String EVENT_ID = "DEL_ROLE";
	
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
	 * Deletes one record from PERSON_UNGROUPED_ROLE and PERSON_ROLE tables.<br>
	 * @throws DataException if user tries to remove 'Administrator' role from himself or
	 * if error was caught during role revoking.
	 * @return null
	 */
	public Object processQuery() throws DataException
	{
		final RoleInfo info = getUserRoleInfo( (Long) getId().getId(), getJdbcTemplate());
		
		final ObjectId userId = new ObjectId( Person.class, info.personId); 
		checkLock(userId);
		
		final boolean isAdmin = Role.ADMINISTRATOR.equals( info.roleCode);
		final boolean selfRemAdmin = userId.equals( getUser().getPerson().getId() ); 

		if (isAdmin && selfRemAdmin)
			throw new DataException("delete.role.selfadmin");
		
		final RoleInfo roleForDelete = getUserRoleForDelete( info, getJdbcTemplate());

		if (null != roleForDelete) {

			manager.cleanAccessListByRole(roleForDelete.prole_id);

			getJdbcTemplate().update(
					"DELETE FROM person_role pr WHERE pr.prole_id=?",
					new Object[] { roleForDelete.prole_id.getId() },
					new int[] { Types.NUMERIC }
					);
		}

		getJdbcTemplate().update(
				"DELETE FROM person_ungrouped_role WHERE prole_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }
			);

		return null;
	}

	/**
	 * �������� ���������� � ���������������� ����, ������� ����� ������� �� ������� person_role.
	 * @param roleInfo
	 * @param jdbc
	 * @return RoleInfo
	 */
	final static RoleInfo getUserRoleForDelete(RoleInfo ungroupedRole, JdbcTemplate jdbc)
	{
		RoleInfo info = null;
		try {
			info = (RoleInfo) jdbc.queryForObject(
				"SELECT pr.prole_id, p.person_id, pr.role_code FROM person_ungrouped_role pur, person_role pr " +
				"INNER JOIN person p ON pr.person_id=p.person_id " +
				"WHERE pur.prole_id=? and pr.role_code=pur.role_code and pr.person_id=pur.person_id " +
				"AND (pr.role_code NOT IN (SELECT gr.role_code FROM person_role_group prg " +
				"INNER JOIN group_role gr ON prg.group_code = gr.group_code WHERE prg.person_id=p.person_id) " + 
				"OR pr.role_code IN (SELECT per.role_code FROM person_excluded_role per " + 
				"INNER JOIN person_role_group prg ON prg.person_id = ?))",
				new Object[] { ungroupedRole.prole_id.getId(), ungroupedRole.personId },
				new int[] { Types.NUMERIC, Types.NUMERIC},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException 
					{
						final RoleInfo result = new RoleInfo();
						result.prole_id = new ObjectId(Role.class, rs.getLong(1));
						result.personId = rs.getLong(2);
						result.roleCode = rs.getString(3);
						return result;
					}}
				);
		
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
		return info;
	}
	
	
	/*
	 * ��������� ��� ��� ���������� �� ��������� ���� � ������������...
	 */
	static class RoleInfo 
	{
		ObjectId prole_id;
		long personId;
		String roleCode;
		public RoleInfo() {
		}
	}

	/**
	 * �������� ���������� � ���������������� ���� �� ������� person_ungrouped_role.
	 * @param userRoleId
	 * @param jdbc
	 * @return RoleInfo
	 */
	final static RoleInfo getUserRoleInfo(Long userRoleId, JdbcTemplate jdbc)
	{
		final RoleInfo info = (RoleInfo) jdbc.queryForObject(
				"SELECT r.prole_id, p.person_id, r.role_code \n" +
				"FROM person_ungrouped_role r \n" +
				"		INNER JOIN person p ON r.person_id=p.person_id \n" +
				"WHERE r.prole_id=? ",
				new Object[] { userRoleId},
				new int[] { Types.NUMERIC},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
							throws SQLException 
					{
						final RoleInfo result = new RoleInfo();
						result.prole_id = new ObjectId(UngroupedRole.class, rs.getLong(1));
						result.personId = rs.getLong(2);
						result.roleCode = rs.getString(3);
						return result;
					}}
				);
		return info;
	}
}
