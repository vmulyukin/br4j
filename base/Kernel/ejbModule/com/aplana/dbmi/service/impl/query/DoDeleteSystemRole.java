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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.action.DeleteSystemRoleAction;

/**
 * {@link ActionQueryBase} descendant used to delete system role. 
 * @see DeleteSystemRole
 * */
public class DoDeleteSystemRole extends ActionQueryBase implements WriteQuery
{

    protected final Log logger = LogFactory.getLog(getClass());
    private DataServiceFacade serviceFacade;
    
	@Override
	public Object processQuery() throws DataException 
	{
		DeleteSystemRoleAction action = (DeleteSystemRoleAction)getAction();
		ObjectId roleId = action.getRoleId();
		List<ObjectId> lockedObjects = new ArrayList<ObjectId>();
		boolean isLocked = false;
		try {
			//�������� ����
			getDataServiceBean().doAction(new LockObject(roleId));
			isLocked = true;
				
			//�������� ������ � ������ �����
			final ChildrenQueryBase listRoleGroupsQuery = getQueryFactory().getChildrenQuery(SystemRole.class, SystemGroup.class);
			listRoleGroupsQuery.setParent(roleId);
			List<SystemGroup> roleGroups = getDatabase().executeQuery( this.getUser(), listRoleGroupsQuery);
	
			for (SystemGroup group : roleGroups) {
				try {
					getDataServiceBean().doAction(new LockObject(group.getId()));
					lockedObjects.add(group.getId());
				} catch (ObjectLockedException e) {
					logger.error("Failed to lock system group " + group.getName() + " in order to delete role from group");
					throw new DataException("role.delete.group.lock.msg", new Object[]{ roleId });
				}
			}
			//�������� ������������� � ������ �����
			final ChildrenQueryBase listRoleUsersQuery = getQueryFactory().getChildrenQuery(SystemRole.class, Person.class);
			listRoleUsersQuery.setParent(roleId);
			List<Person> roleUsers = getDatabase().executeQuery( this.getUser(), listRoleUsersQuery);

			for (Person user : roleUsers) {
				try {
					getDataServiceBean().doAction(new LockObject( user.getCardId()));
					lockedObjects.add(user.getCardId());
				} catch (ObjectLockedException e) {
					logger.error("Failed to lock user's " + user.getLogin() + " card in order to delete user's role");
					throw new DataException("role.delete.user.lock.msg", new Object[]{ user.getFullName(), e.getLocker().getFullName() });
				}
			}

			final ObjectQueryBase deleteQuery = getQueryFactory().getDeleteQuery(roleId);
			deleteQuery.setId(roleId);
			getDatabase().executeQuery( this.getUser(), deleteQuery);
			logger.debug("System role " + roleId.getId() + " deleted successfully");

		} catch (Exception e) {
			logger.error("System role " + roleId.getId() + " cannot be deleted", e);
			throw new DataException(e.getMessage(), new Object[]{ roleId });
		} finally {
			if (isLocked) {
				try {
					getDataServiceBean().doAction(new UnlockObject(roleId));
				} catch (Exception e) {
					logger.error("Failed to unlock system role " + roleId.getId());
				}
			}
			//��������� ���������� ������ � �������������
			for (ObjectId id : lockedObjects) {
				try {
					getDataServiceBean().doAction(new UnlockObject(id));
				} catch (Exception e) {
					logger.error("Failed to unlock object with id = " + id);
				}
			}
		}
		return null;
	}

	private DataServiceFacade getDataServiceBean() throws DataException {
		if (this.serviceFacade == null) {
			serviceFacade = new DataServiceFacade();
			serviceFacade.setUser(getUser());
			serviceFacade.setDatabase(getDatabase());
			serviceFacade.setQueryFactory(getQueryFactory());
		}
		return this.serviceFacade;
	}
}