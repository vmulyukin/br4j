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
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ImportAttribute.CUSTOM_ATTRIBUTE_CODES;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ������ ��������� �����
 * @author PPanichev
 * 
 */

public class DoImportRoles extends DoImportObjects implements WriteQuery {

	private static final long serialVersionUID = 1L;
	private String name;

	public Object processQuery() throws DataException {
		return super.processQuery();
	}
	
	/**
	 * @param systemRole - ����������� ��������� ����
	 * @return ����������� ��������� ����
	 * @throws DataException
	 */
	@Override
	protected SystemRole saveObject(ObjectId systemRoleId, UserData user,
			List<ImportAttribute> importAttributes, boolean isNewRole) throws DataException {
		ObjectId savedRole;
		String nameRu=null;
		String nameEn=null;

		List<SystemGroup> roleGroups = new ArrayList<SystemGroup>();
		// ��������� ������, � ������� ����������� ����
		for (ImportAttribute importAttribute : importAttributes) {
			CUSTOM_ATTRIBUTE_CODES customAttributeId = importAttribute
					.getCustomPrimaryCodeId();
			if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_CODE)) {
				String customAttributeValue = importAttribute.getValue();
				roleGroups.addAll(getSystemGroups(customAttributeValue));
			}
			// ���� ���� ����� - ������� ������������ ����
			if (isNewRole) {
				if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_RUS))
					nameRu = importAttribute.getValue();
				if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_NAME_ENG))
					nameEn = importAttribute.getValue();
			}
		}
		
		// ���� ���� ������������ - �����, ��������� � �����������
		if (!isNewRole) {
			SystemRole systemRole = getRoleByValue(systemRoleId);
			boolean isLocked = false;
			try {
				lockRole(systemRole, user);
				isLocked = true;
				systemRole.setRoleGroups(roleGroups);
				final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(systemRole);
				// ��� - ����� �������� �������� ���� �������
				saveQuery.setAccessChecker(null);
				saveQuery.setObject(systemRole);
				savedRole = (ObjectId) getDatabase().executeQuery(user, saveQuery);
			} catch (ObjectLockedException ex) {
				String msg = ContextProvider.getContext().getLocaleMessage(
						"role.form.store.lock.msg");
				MessageFormat.format(msg, ex.getLocker().getFullName());
				logger.debug("System role " + systemRole.getId().getId()
						+ " is locked by " + ex.getLocker().getFullName(), ex);
				throw new DataException(msg);
			} finally {
				if (isLocked) {
					unLockRole(systemRole, user);
				}
			}
		} else {
			// ���� ���� ����� - ��������� ��� ����
			SystemRole systemRole = new SystemRole();
			String roleCode = (String)systemRoleId.getId();
			systemRole.setRoleCode(roleCode);
			systemRole.setRoleGroups(roleGroups);
			systemRole.setNameRu(nameRu);
			systemRole.setNameEn(nameEn);
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(systemRole);
			// ��� - ����� �������� �������� ���� �������
			saveQuery.setAccessChecker(null);
			saveQuery.setObject(systemRole);
			savedRole = (ObjectId) getDatabase().executeQuery(user, saveQuery);
		}
		SystemRole systemRole = getRoleByValue(savedRole);
		name = systemRole.getName();
		return systemRole;
	}
	
	private void lockRole(SystemRole systemRole, UserData user) throws DataException {
		LockObject actionLock = new LockObject(systemRole.getId());
		final ActionQueryBase query = getQueryFactory().getActionQuery(actionLock);
		query.setAction(actionLock);
		getDatabase().executeQuery(user, query);	
	}
	
	private void unLockRole(SystemRole systemRole, UserData user) throws DataException {
		UnlockObject actionUnLock = new UnlockObject(systemRole.getId());
		final ActionQueryBase query = getQueryFactory().getActionQuery(actionUnLock);
		query.setAction(actionUnLock);
		getDatabase().executeQuery(user, query);
	}
	
	private List<SystemGroup> getSystemGroups(String groupValues) throws DataException {
		List<SystemGroup> listSystemGroups = new ArrayList<SystemGroup>();
		String[] values = groupValues.split("]>");
		for(String tempValue: values){ 
			String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim();
			if (value!=null&&!value.isEmpty()){
				//TODO
				// ���� ��� ���������� ���� ����������� ��������� � ������
				// ������ ����������� ���������� ���� � �����,
				// � ��� �� ���������� ����� �����
				
				SystemGroup systemGroup = new SystemGroup();
				systemGroup.setGroupCode(value);
				systemGroup.setId(new ObjectId(SystemGroup.class, value));
				// ��������� �������� �� ������������/������������� ������
				ObjectId groupId = systemGroup.getId();
				ObjectQueryBase groupQuery = getQueryFactory().getFetchQuery(groupId.getType());
				groupQuery.setAccessChecker(null);
				groupQuery.setId(groupId);
				SystemGroup systemGroupFetch = null;
				try {
					systemGroupFetch = (SystemGroup)getDatabase().executeQuery(getUser(), groupQuery);
				} catch(Exception e) {
					throw new DataException("role.form.store.not.group.msg", new Object[]{value});
				}
				listSystemGroups.add(systemGroupFetch);
			}
		}
		return listSystemGroups;
	}
	
	private SystemRole getRoleByValue(ObjectId roleId) throws DataException {
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try get system role with 'role_code' equal {1}", new Object[]{roleId}));
		}
		if (roleId == null || roleId.getId() == null){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Input role is null, searching break", new Object[]{}));
			}
			return null;
		}
		ObjectQueryBase roleQuery = getQueryFactory().getFetchQuery(roleId.getType());
		roleQuery.setAccessChecker(null);
		roleQuery.setId(roleId);
		SystemRole systemRole = (SystemRole)getDatabase().executeQuery(getUser(), roleQuery);
		return systemRole;
	}

	@Override
	protected String getName() {
		return name;
	}
}