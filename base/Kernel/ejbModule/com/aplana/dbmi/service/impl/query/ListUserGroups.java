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
import java.sql.Types;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all {@link Group groups} 
 * defined for given parent {@link Person} object.
 */
public class ListUserGroups extends ChildrenQueryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Fetches all {@link Group groups} defined for given parent {@link Person} object.
	 * @return list containing {@link Group} objects representing set of groups assigned
	 * to given {@link Person} object.
	 */
	@SuppressWarnings("unchecked")
	public Object processQuery() throws DataException
	{
		List<Group> result = getJdbcTemplate().query(
				"SELECT prg.prgroup_id, prg.group_code, sg.group_name_rus, sg.group_name_eng " +
				"FROM person_role_group prg INNER JOIN system_group sg ON prg.group_code=sg.group_code " +
				"WHERE prg.person_id=?",
				new Object[] { getParent().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Group group = new Group();
						group.setId(rs.getLong(1));
						ObjectId systemGroupId = new ObjectId(SystemGroup.class, rs.getString(2));
						SystemGroup systemGroup = (SystemGroup)DataObject.createFromId(systemGroupId);
						systemGroup.setNameRu(rs.getString(3));
						systemGroup.setNameEn(rs.getString(4));
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
						group.setPerson(getParent());
						final List<String> excludedRolesList = getJdbcTemplate().query(
								"SELECT DISTINCT per.role_code " +
								"FROM person_excluded_role per " +
								"WHERE per.prgroup_id=?",
								new Object[] { group.getId().getId() },
								new int[] { Types.NUMERIC },
								new RowMapper() {
									public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
										String roleCode = rs.getString(1);
										return roleCode;
									}
								});
						group.setExcludedRoles(new HashSet<String>() {{ addAll(excludedRolesList); }});
						return group;
					}
				});
		// sorting result list
		Collections.sort(
			result,
			new Comparator<Group>() {
				public int compare(Group group1, Group group2) {
					if (group1.getName() == null) {
						return -1;
					} else {
						return group1.getName().compareTo(group2.getName());
					}
				}
			}
		);
		return result;
	}
}
