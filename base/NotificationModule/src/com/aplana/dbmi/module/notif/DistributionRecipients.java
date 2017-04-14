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
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

public class DistributionRecipients extends DataServiceClient implements RecipientGroup
{
	public Collection discloseRecipients(NotificationObject object) {
		Distribution distr = (Distribution) ((StoredNotification) object).getNotification();
		ArrayList recipients = new ArrayList();
		for (Iterator itr = distr.getAccessList().iterator(); itr.hasNext(); ) {
			AccessListItem item = (AccessListItem) itr.next();
			if (AccessListItem.TYPE_PERSON == item.getType()) {
				recipients.add(item.getPerson());
			} else if (AccessListItem.TYPE_DEPARTMENT == item.getType()) {
				addDepartment(recipients, item.getDepartment());
			} else if (AccessListItem.TYPE_ROLE == item.getType()) {
				addByRole(recipients, item.getRoleType());
			}
		}
		return recipients;
	}

	private void addDepartment(final List recipients, final ReferenceValue department) {
		try {
			getDatabase().executeQuery(getSystemUser(), new QueryBase() {
				public Object processQuery() throws DataException {
					getJdbcTemplate().query(
						"SELECT p.person_id, p.person_login, p.full_name, p.email " +
						"FROM person p " +
						"WHERE p.is_active=1 AND p.value_id=?",
						new Object[] { department.getId().getId() },
						new RowCallbackHandler() {
							public void processRow(ResultSet rs) throws SQLException {
								Person person = new Person();
								person.setId(rs.getLong(1));
								person.setLogin(rs.getString(2));
								person.setFullName(rs.getString(3));
								person.setEmail(rs.getString(4));
								person.setActive(true);
								recipients.add(person);
							}
						});
					return null;
				}
			});
		} catch (DataException e) {
			logger.error("Error fetching users by department " + department.getId().getId(), e);
		}
	}

	private void addByRole(final List recipients, final String roleType) {
		try {
			getDatabase().executeQuery(getSystemUser(), new QueryBase() {
				public Object processQuery() throws DataException {
					getJdbcTemplate().query(
						"SELECT p.person_id, p.person_login, p.full_name, p.email " +
						"FROM person p " +
						"INNER JOIN person_role r ON p.preson_id=r.person_id " +
						"WHERE p.is_active=1 AND r.role_code=?",
						new Object[] { roleType },
						new RowCallbackHandler() {
							public void processRow(ResultSet rs) throws SQLException {
								Person person = new Person();
								person.setId(rs.getLong(1));
								person.setLogin(rs.getString(2));
								person.setFullName(rs.getString(3));
								person.setEmail(rs.getString(4));
								person.setActive(true);
								recipients.add(person);
							}
						});
					return null;
				}
			});
		} catch (DataException e) {
			logger.error("Error fetching users by role " + roleType, e);
		}
	}
}
