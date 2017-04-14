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

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;


public class SyncRoleGroups extends ProcessorBase implements DatabaseClient {
	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;
	
	@Override
	public Object process() throws DataException {
		SystemRole systemRole = (SystemRole) getObject();

		final ChildrenQueryBase getChildrenQuery = getQueryFactory().getChildrenQuery(SystemRole.class, SystemGroup.class);
		getChildrenQuery.setParent(systemRole.getId());
		@SuppressWarnings("unchecked")
		List<SystemGroup> oldGroups = (List<SystemGroup>)getDatabase().executeQuery(getUser(), getChildrenQuery);
		
		if (systemRole.getRoleGroups() == null && oldGroups == null) {
			return null;
		}

		List<SystemGroup> groupsToDelete = new ArrayList<SystemGroup>();
		List<SystemGroup> newGroups = systemRole.getRoleGroups();
		if (newGroups == null)
			newGroups = new ArrayList<SystemGroup>();
		//Set<ObjectId> oldGroupIds = new HashSet<ObjectId>();
		for (SystemGroup group : oldGroups){
			if (!newGroups.contains(group)) {
				groupsToDelete.add(group);
			}
		}
		
		newGroups.removeAll(oldGroups);
		updateRoleGroupList(systemRole, newGroups, false);
		updateRoleGroupList(systemRole, groupsToDelete, true);
		
		return null;
	}
	
	private void updateRoleGroupList(SystemRole systemRole, List<SystemGroup> groupList, boolean isDelete) throws DataException {
		for (SystemGroup group : groupList) {
			ObjectId groupId = group.getId();
			//�������� ��������� ������
			execAction(new LockObject(groupId));
			
			try {
				final ObjectQueryBase getFetchQuery = getQueryFactory().getFetchQuery(SystemGroup.class);
				getFetchQuery.setId(groupId);
				group = (SystemGroup)getDatabase().executeQuery(getUser(), getFetchQuery);
				if (isDelete) {
					group.getSystemRoles().remove(systemRole);
				} else {
					group.getSystemRoles().add(systemRole);
				}
				
				//��������� ��������� ������
				/*SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(group);
				saveQuery.setObject(group);
				getDatabase().executeQuery( getUser(), saveQuery);*/
				
				/** PPanichev 31.03.2015
					����� �������� ��������, ���������� ���������� � ������� sql-�������.
				 */
				saveSystemGroup(group);
				
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
								if (isDelete) {
									Set<String> excludedRoles = userGroup.getExcludedRoles();
									if (excludedRoles != null && !excludedRoles.isEmpty()) {
										excludedRoles.remove(systemRole.getId().getId());
									}
								}
								SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(userGroup);
								saveQuery.setObject(userGroup);
								getDatabase().executeQuery( this.getUser(), saveQuery);
								break;
							}
						}
					} finally {
						//��������� ������������
						execAction(new UnlockObject(user.getId()));
					}
				}
			} finally {
				//��������� ��������� ������
				execAction(new UnlockObject(groupId));
			}
		}
	}
	
	private void saveSystemGroup(SystemGroup group) throws DataException {
		//checkLock(group.getId()); ?????????????????????????????????????????????????????
		
		jdbc.update(
				"UPDATE system_group SET group_name_rus = ?, group_name_eng=? " + 
				"WHERE group_code = ?",
				new Object[] {group.getNameRu(), group.getNameEn(), group.getId().getId()},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR }
				);
		
		List<SystemRole> groupRoles = (List<SystemRole>)group.getSystemRoles();
		if (groupRoles != null) {
			
			// Refresh group roles list
			jdbc.update("DELETE FROM group_role WHERE group_code = ?",
					new Object[] { group.getId().getId() },
					new int[] { Types.VARCHAR }
					);
			

			for (SystemRole role : groupRoles) {
				long id = jdbc.queryForLong("SELECT nextval('seq_group_role_id')");
				jdbc.update(
						"INSERT INTO group_role VALUES (?, ?, ?)",
						new Object[] {id, group.getId().getId(), role.getId().getId() },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR }
						);
			}
		}
	
	}
	
	private void checkLock(ObjectId objId)
			throws ObjectLockedException, ObjectNotLockedException, DataException	{
		Action action = new CheckLock(objId);
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		getDatabase().executeQuery(getUser(), query);
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
