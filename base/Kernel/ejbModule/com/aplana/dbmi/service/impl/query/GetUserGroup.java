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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Group} instance from database
 */
public class GetUserGroup extends ObjectQueryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Fetches single {@link Group} instance from database from database
	 * @return fully initialized {@link Group} instance
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().queryForObject(
				"SELECT prg.prgroup_id, prg.person_id, prg.group_code, sg.group_name_rus, sg.group_name_eng " +
				"FROM person_role_group prg INNER JOIN system_group sg ON prg.group_code=sg.group_code " +
				"WHERE prg.prgroup_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					@SuppressWarnings("unchecked")
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Group group = new Group();
						group.setId(rs.getLong(1));
						group.setPerson(new ObjectId(Person.class, rs.getLong(2)));

						ObjectId systemGroupId = new ObjectId(SystemGroup.class, rs.getString(3));
						SystemGroup systemGroup = (SystemGroup)DataObject.createFromId(systemGroupId);
						systemGroup.setNameRu(rs.getString(4));
						systemGroup.setNameEn(rs.getString(5));
						List<SystemRole> rolesList = getJdbcTemplate().query(
								"SELECT gr.role_code, sr.role_name_rus, sr.role_name_eng " +
								"FROM group_role gr INNER JOIN system_role sr ON gr.role_code=sr.role_code " +
								"WHERE gr.group_code=?",
								new Object[] { systemGroupId.getId() },
								new int[] { Types.VARCHAR },
								new RowMapper() {
									public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
										ObjectId systemRoleId = new ObjectId(SystemRole.class, rs.getString(1));
										SystemRole systemRole = (SystemRole)DataObject.createFromId(systemRoleId);
										systemRole.setNameRu(rs.getString(2));
										systemRole.setNameEn(rs.getString(3));						
										return systemRole;
									}
								});
						// sorting result list
						Collections.sort(
							rolesList,
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
						systemGroup.setSystemRoles(rolesList);
						group.setSystemGroup(systemGroup);
						return group;
					}
				});
	}
}
