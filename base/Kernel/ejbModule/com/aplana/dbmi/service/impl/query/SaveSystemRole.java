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

import java.sql.Types;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to save {@link SystemRole} object instances.
 * Create/Updates single row in SYSTEM_ROLE table.
 */
public class SaveSystemRole extends SaveQueryBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'New role added' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_ROLE"; //add new action type!!
	/**
	 * Identifier of 'Role changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_ROLE"; //add new action type!!

	private SystemRole role;

	protected AccessRuleManager manager;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	/**
	 * @return {@link #EVENT_ID_CREATE} if new {@link SystemRole} object is saved,
	 * {@link #EVENT_ID_CHANGE} otherwise.
	 */
	public String getEvent()
	{
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	protected ObjectId processNew() throws DataException
	{
		role = (SystemRole) getObject();

		getJdbcTemplate().update(
				"INSERT INTO system_role (role_code, role_name_rus, role_name_eng) VALUES (?, ?, ?)",
				new Object[] { role.getRoleCode(), role.getNameRu(), role.getNameEn() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR }
				);
		role.setId(new ObjectId(SystemRole.class, role.getRoleCode()));
		return role.getId();
	}

	protected void processUpdate() throws DataException
	{
		role = (SystemRole) getObject();
		
		checkLock(role.getId());
		
		getJdbcTemplate().update(
				"UPDATE system_role SET role_name_rus = ?, role_name_eng=? " + 
				"WHERE role_code = ?",
				new Object[] {role.getNameRu(), role.getNameEn(), role.getId().getId()},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR }
				);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void validate() throws DataException {
		
		role = (SystemRole) getObject();
		
		if(isNew()) {
			Boolean exists = (Boolean) getJdbcTemplate().queryForObject("select count(*)>0 from system_role where role_code = ?",
					new Object[] { role.getRoleCode() },
					new int[] { Types.VARCHAR },
					Boolean.class);
			if(exists){
				throw new DataException("role.create.exists",new Object[] { role.getRoleCode() });
			}
		}
	}
}
