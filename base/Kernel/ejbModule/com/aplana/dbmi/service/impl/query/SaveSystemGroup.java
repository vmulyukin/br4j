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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

/**
 * Query used to save {@link SystemGroup} object instances.
 * Create/Updates single row in SYSTEM_GROUP and ROLE_GROUP tables.
 */
public class SaveSystemGroup extends SaveQueryBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'New group added' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_GROUP"; //add new action type!!
	/**
	 * Identifier of 'Group changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_GROUP"; //add new action type!!

	private SystemGroup group;

	protected AccessRuleManager manager;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	/**
	 * @return {@link #EVENT_ID_CREATE} if new {@link SystemGroup} object is saved,
	 * {@link #EVENT_ID_CHANGE} otherwise.
	 */
	public String getEvent()
	{
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	protected ObjectId processNew() throws DataException
	{
		group = (SystemGroup) getObject();

		getJdbcTemplate().update(
				"INSERT INTO system_group (group_code, group_name_rus, group_name_eng) VALUES (?, ?, ?)",
				new Object[] { group.getGroupCode(), group.getNameRu(), group.getNameEn() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR }
				);
		group.setId(new ObjectId(SystemGroup.class, group.getGroupCode()));
		
		Collection<SystemRole> groupRoles = group.getSystemRoles();
		if (groupRoles != null) {	
			for (SystemRole role : groupRoles) {
				long id = getJdbcTemplate().queryForLong("SELECT nextval('seq_group_role_id')");
				getJdbcTemplate().update(
						"INSERT INTO group_role VALUES (?, ?, ?)",
						new Object[] {id, group.getId().getId(), role.getId().getId() },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR }
						);
			}
		}
		return group.getId();
	}

	protected void processUpdate() throws DataException
	{
		group = (SystemGroup) getObject();
		
		checkLock(group.getId());
		
		getJdbcTemplate().update(
				"UPDATE system_group SET group_name_rus = ?, group_name_eng=? " + 
				"WHERE group_code = ?",
				new Object[] {group.getNameRu(), group.getNameEn(), group.getId().getId()},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR }
				);
		
		List<SystemRole> groupRoles = (List<SystemRole>)group.getSystemRoles();
		if (groupRoles != null) {
			
			// Refresh group roles list
			getJdbcTemplate().update("DELETE FROM group_role WHERE group_code = ?",
					new Object[] { group.getId().getId() },
					new int[] { Types.VARCHAR }
					);
			

			for (SystemRole role : groupRoles) {
				long id = getJdbcTemplate().queryForLong("SELECT nextval('seq_group_role_id')");
				getJdbcTemplate().update(
						"INSERT INTO group_role VALUES (?, ?, ?)",
						new Object[] {id, group.getId().getId(), role.getId().getId() },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR }
						);
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void validate() throws DataException {
		
		group = (SystemGroup) getObject();
		
		if(isNew()) {
			Boolean exists = (Boolean) getJdbcTemplate().queryForObject("select count(*)>0 from system_group where group_code = ?",
					new Object[] { group.getGroupCode() },
					new int[] { Types.VARCHAR },
					Boolean.class);
			if(exists){
				throw new DataException("role.group.create.exists",new Object[] { group.getGroupCode() });
			}
		}
	}
}
