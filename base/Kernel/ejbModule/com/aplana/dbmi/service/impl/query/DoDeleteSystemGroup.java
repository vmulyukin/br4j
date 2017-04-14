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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.DeleteSystemGroupAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ActionQueryBase} descendant used to delete system group. 
 * @see DeleteSystemGroup
 * */
public class DoDeleteSystemGroup extends ActionQueryBase implements WriteQuery
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected final Log logger = LogFactory.getLog(getClass());
    private DataServiceFacade serviceFacade;
    
	@Override
	public Object processQuery() throws DataException 
	{
		DeleteSystemGroupAction action = (DeleteSystemGroupAction)getAction();
		ObjectId groupId = action.getGroupId();
		List<ObjectId> lockedObjects = new ArrayList<ObjectId>();
		boolean isLocked = false;
		try {
			//�������� ������
			getDataServiceBean().doAction(new LockObject(groupId));
			isLocked = true;
				
			//�������� ���� � ������ �������
			final ChildrenQueryBase listGroupRolesQuery = getQueryFactory().getChildrenQuery(SystemGroup.class, SystemRole.class);
			listGroupRolesQuery.setParent(groupId);
			List<SystemRole> groupRoles = getDatabase().executeQuery( this.getUser(), listGroupRolesQuery);
	
			for (SystemRole role : groupRoles) {
				try {
					getDataServiceBean().doAction(new LockObject(role.getId()));
					lockedObjects.add(role.getId());
				} catch (ObjectLockedException e) {
					logger.error("Failed to lock system role " + role.getName() + " in order to delete group from role");
					throw new DataException("group.delete.role.lock.msg", new Object[]{ groupId });
				}
			}
			//�������� ������������� � ������ �������
			final ChildrenQueryBase listGroupUsersQuery = getQueryFactory().getChildrenQuery(SystemGroup.class, Person.class);
			listGroupUsersQuery.setParent(groupId);
			List<Person> groupUsers = getDatabase().executeQuery( this.getUser(), listGroupUsersQuery);

			for (Person user : groupUsers) {
				try {
					getDataServiceBean().doAction(new LockObject( user.getCardId()));
					lockedObjects.add(user.getCardId());
				} catch (ObjectLockedException e) {
					logger.error("Failed to lock user's " + user.getLogin() + " card in order to delete user's group");
					throw new DataException("group.delete.user.lock.msg", new Object[]{ user.getFullName(), e.getLocker().getFullName() });
				}
			}

			final ObjectQueryBase deleteQuery = getQueryFactory().getDeleteQuery(groupId);
			deleteQuery.setId(groupId);
			getDatabase().executeQuery( this.getUser(), deleteQuery);
			logger.debug("System group " + groupId.getId() + " deleted successfully");

		} catch (Exception e) {
			logger.error("System group " + groupId.getId() + " cannot be deleted", e);
			throw new DataException(e.getMessage(), new Object[]{ groupId });
		} finally {
			if (isLocked) {
				try {
					getDataServiceBean().doAction(new UnlockObject(groupId));
				} catch (Exception e) {
					logger.error("Failed to unlock system group " + groupId.getId());
				}
			}
			//��������� ���������� ���� � �������������
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