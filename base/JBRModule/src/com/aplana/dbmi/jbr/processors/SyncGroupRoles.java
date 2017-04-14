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
package com.aplana.dbmi.jbr.processors;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;


public class SyncGroupRoles extends ProcessorBase implements DatabaseClient {
	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;
	
	@Override
	public Object process() throws DataException {
		SystemGroup systemGroup = (SystemGroup) getObject();
		
		if (systemGroup == null)
			return null;
		ObjectId groupId = systemGroup.getId();
		
		// �������� ������ ����� � ������
		final ChildrenQueryBase getChildrenQueryRoles = getQueryFactory().getChildrenQuery(SystemGroup.class, SystemRole.class);
		getChildrenQueryRoles.setParent(systemGroup.getId());
		@SuppressWarnings("unchecked")
		List<SystemRole> roleList = (List<SystemRole>)getDatabase().executeQuery(getUser(), getChildrenQueryRoles);
		
		//�������� ������ ������������� ��� ������ ������
		final ChildrenQueryBase getChildrenQuery = getQueryFactory().getChildrenQuery(SystemGroup.class, Person.class);
		getChildrenQuery.setParent(groupId);
		@SuppressWarnings("unchecked")
		List<Person> groupUsers = (List<Person>)getDatabase().executeQuery(getUser(), getChildrenQuery);
		
		for (Person user : groupUsers) {
			//�������� ������������
			execAction(new LockObject(user.getId()));
			try {
				//�������� ���������������� ������
				@SuppressWarnings("unchecked")
				List<Group> userGroups = (List<Group>)user.getGroups();
				for (Group userGroup : userGroups) {
					if (userGroup.getType().equals(groupId.getId())) {
						// ���� ������� ���� �� ������, �� ������� �� excluded ��� ����
						Set<String> excludedRoles = userGroup.getExcludedRoles();
						if (excludedRoles != null && !excludedRoles.isEmpty()) {
							for (Iterator<String> exclIter = excludedRoles.iterator(); exclIter.hasNext();) {
								boolean isExcl = false;
								String excludedRole = exclIter.next();
								for (SystemRole systemRole : roleList) {
									if (excludedRole.equals(systemRole.getId().getId())) {
										// ����� excluded - ��������� � ��.
										isExcl = true;
										break;
									}
								}
								// ���� �� ����� excl ����� ����� ������ - �������
								if (!isExcl)
									exclIter.remove();
							}
						}

						SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(userGroup);
						saveQuery.setObject(userGroup);
						getDatabase().executeQuery(this.getUser(), saveQuery);
						break;
					}
				}
			} finally {
				//��������� ������������
				execAction(new UnlockObject(user.getId()));
			}
		}
		return null;
	}

	@Override
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	/**
	 * ��������� ������ �������� � ���� �� ����� ������������ user.
	 * @param action
	 * @param user
	 * @return ������, ������������ �����.
	 * @throws DataException 
	 */
	public <T> T execAction(Action action, UserData user) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}

	/**
	 * ��������� ������ �������� � ���� �� ����� ���������� ������������.
	 * @param action
	 * @return ������, ������������ �����.
	 * @throws DataException
	 */
	public <T> T execAction(Action action) throws DataException {
		return execAction( action, getSystemUser());
	}
}
