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
package com.aplana.dbmi.module.notif;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

public class RoleRecipients extends DataServiceClient implements RecipientGroup {
	
	private static final ObjectId ATTR_NOTIFICATION_FLAGS =
			ObjectId.predefined(TreeAttribute.class, "notification.events");
	
	private ObjectId roleId;
	private ObjectId personFlagId;
	
	public void setRoleId(ObjectId roleId) {
		if (!SystemRole.class.equals(roleId.getType()))
			throw new IllegalArgumentException("roleId must be a SystemRole ID");
		this.roleId = roleId;
	}
	
	public void setRole(String role) {
		setRoleId(ObjectId.predefined(SystemRole.class, role));
	}
	
	public void setPersonFlagId(ObjectId flagId) {
		if (!ReferenceValue.class.equals(flagId.getType()))
			throw new IllegalArgumentException("flagId must be a ReferenceValue ID");
		this.personFlagId = flagId;
	}
	
	public void setPersonFlag(String flag) {
		setPersonFlagId(ObjectId.predefined(ReferenceValue.class, flag));
	}

	@Override
	public Collection discloseRecipients(NotificationObject object) {
		if (roleId == null)
			throw new IllegalStateException("roleId must be defined before use");
		try {
			return (Collection) getDatabase().executeQuery(getSystemUser(), new ListPersonsByRole());
		} catch (DataException e) {
			logger.warn("Error retrieving persons by role " + roleId.getId(), e);
			return Collections.emptyList();
		}
	}

	private class ListPersonsByRole extends QueryBase {

		@Override
		public Object processQuery() throws DataException {
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(roleId.getId());
			
			String flagCheck = "", flagJoin = "";
			if (personFlagId != null) {
				flagJoin = "JOIN attribute_value v ON p.card_id=v.card_id AND v.attribute_code=? ";
				params.add(0, ATTR_NOTIFICATION_FLAGS.getId());
				
				flagCheck = "AND v.value_id=? ";
				params.add(personFlagId.getId());
			}
			
			return getJdbcTemplate().query(
					"SELECT distinct p.person_id, p.person_login, p.full_name, p.email, p.card_id " +
					"FROM person p " +
					"JOIN person_role r ON p.person_id=r.person_id " +
						flagJoin +
					"WHERE p.is_active=1 AND r.role_code=? " +
						flagCheck,
					params.toArray(),
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							Person person = new Person();
							person.setId(rs.getLong(1));
							person.setLogin(rs.getString(2));
							person.setFullName(rs.getString(3));
							person.setEmail(rs.getString(4));
							if (rs.getObject(5) != null)
								person.setCardId(new ObjectId(Card.class, rs.getLong(5)));
							return person;
						}
					});
		}
		
	}
}
