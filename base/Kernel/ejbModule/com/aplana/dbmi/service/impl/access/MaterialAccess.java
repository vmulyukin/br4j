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
package com.aplana.dbmi.service.impl.access;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;

/**
 * Access checker used to determine if current user is allowed to
 * download material file attached to card.<br>
 * Checks is performed based on content of ACCESS_CONTROL_LIST table
 * @see com.aplana.dbmi.model.AccessListItem
 */
@Deprecated
public class MaterialAccess extends CardModify
{
	public boolean checkAccess() throws DataException
	{
		/*return isPublic() ||
				hasPersonalAccess() ||
				hasDepartmentAccess() ||
				hasRoleAccess() ||
				hasRole(Role.ADMINISTRATOR);*/
		// TODO: (2009/12/05) ������ true, �.�. ����� ���������� ������ ������� �� �������������.
		return true;
		/*
		String reason = null;
		boolean grant = false;
		if (isPublic()) {
			grant = true;
			reason = "Not restricted";
		}
		if (!grant && hasPersonalAccess()) {
			grant = true;
			reason = "Individual permission";
		}
		if (!grant && hasDepartmentAccess()) {
			grant = true;
			reason = "Department permission";
		}
		if (!grant && hasRoleAccess()) {
			grant = true;
			reason = "Role permission";
		}
		if (!grant && hasRole(Role.ADMINISTRATOR)) {
			grant = true;
			reason = "Administrative privilegy";
		}
		if (!grant && super.checkAccess()) {
			grant = true;
			reason = "Write access to card";
		}
		if (!grant && isAuthor()) {
			grant = true;
			reason = "Author";
		}
		logger.info("Material " + getObject().getId().getId() + " access for " + getUser().getPerson().getFullName() +
				" " + (grant ? "GRANTED" : "DENIED") + (reason == null ? "" : ": " + reason));
		return grant;
		 */
	}
	
	private boolean isPublic() {
		return getJdbcTemplate().queryForInt(
				"SELECT COUNT(acl_id) FROM access_control_list WHERE card_id=?",
				new Object[] { getObject().getId().getId() }) == 0;
	}
	
	private boolean hasPersonalAccess() {
		return getJdbcTemplate().queryForInt(
				"SELECT COUNT(acl_id) FROM access_control_list WHERE card_id=? AND person_id=?",
				new Object[] { getObject().getId().getId(), getUser().getPerson().getId().getId() }) > 0;
	}
	
	private boolean hasRoleAccess() {
		return getJdbcTemplate().queryForInt(
				//TODO: region level raising is not implemented
				"SELECT COUNT(a.acl_id) FROM access_control_list a " +
				"INNER JOIN person_role r ON a.role_code=r.role_code " +
				
// � ������ ������ BR4J00036917 ������� ����������� ����-������, ����-������
				
//				"LEFT OUTER JOIN person_role_region rr ON r.prole_id=rr.prole_id " +
				"WHERE a.card_id=? AND r.person_id=? AND (a.role_code=? " +
					"OR (a.role_code=?" +
//					"AND (rr.value_id IS NULL OR rr.value_id IN (" +
//						"SELECT v.value_id FROM values_list v START WITH v.value_id IN (" +
//							"SELECT av.value_id FROM attribute_value av " +
//							"WHERE av.card_id=a.card_id AND av.attribute_code=?)" +
//						"CONNECT BY PRIOR v.parent_value_id=v.value_id))" +
						"))",
				new Object[] {
						getObject().getId().getId(),
						getUser().getPerson().getId().getId(),
						Role.MANAGER_1, Role.MANAGER_2,
						Attribute.ID_REGION.getId() }) > 0;
	}
	
	private boolean hasDepartmentAccess() {
		return getJdbcTemplate().queryForInt(
				"SELECT COUNT(a.acl_id) FROM access_control_list a " +
				"INNER JOIN person p ON a.value_id=p.value_id " +
				"WHERE a.card_id=? AND p.person_id=?",
				new Object[] { getObject().getId().getId(), getUser().getPerson().getId().getId() }) > 0;
	}
	
	private boolean isAuthor()
	{
		long authorId = getJdbcTemplate().queryForLong(
				"SELECT number_value FROM attribute_value WHERE card_id=? AND attribute_code=?",
				new Object[] { getObject().getId().getId(), Attribute.ID_AUTHOR.getId() });
		return getUser().getPerson().getId().equals(new ObjectId(Person.class, authorId));
	}	
}
