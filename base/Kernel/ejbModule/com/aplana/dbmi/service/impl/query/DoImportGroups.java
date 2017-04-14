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

public class DoImportGroups extends DoImportObjects implements WriteQuery {

	private static final long serialVersionUID = 1L;
	private String name;

	public Object processQuery() throws DataException {
		return super.processQuery();
	}
	
	/**
	 * @param systemGroup - ����������� ��������� ������
	 * @return ����������� ��������� ������
	 * @throws DataException
	 */
	@Override
	protected SystemGroup saveObject(ObjectId systemGroupId, UserData user,
			List<ImportAttribute> importAttributes, boolean isNewGroup) throws DataException {
		ObjectId savedGroup;
		String nameRu=null;
		String nameEn=null;

		List<SystemRole> groupRoles = new ArrayList<SystemRole>();
		// ��������� ����, ������� ������ � ������
		for (ImportAttribute importAttribute : importAttributes) {
			CUSTOM_ATTRIBUTE_CODES customAttributeId = importAttribute
					.getCustomPrimaryCodeId();
			if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.ROLE_CODE)) {
				String customAttributeValue = importAttribute.getValue();
				groupRoles.addAll(getSystemRoles(customAttributeValue));
			}
			// ���� ������ ����� - ������� ������������ ������
			if (isNewGroup) {
				if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_RUS))
					nameRu = importAttribute.getValue();
				if (customAttributeId.equals(CUSTOM_ATTRIBUTE_CODES.GROUP_NAME_ENG))
					nameEn = importAttribute.getValue();
			}
		}
		
		// ���� ������ ������������ - �����, ��������� � �����������
		if (!isNewGroup) {
			SystemGroup systemGroup = getGroupByValue(systemGroupId);
			boolean isLocked = false;
			try {
				lockGroup(systemGroup, user);
				isLocked = true;
				systemGroup.setSystemRoles(groupRoles);
				final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(systemGroup);
				// ��� - ����� �������� �������� ���� �������
				saveQuery.setAccessChecker(null);
				saveQuery.setObject(systemGroup);
				savedGroup = (ObjectId) getDatabase().executeQuery(user, saveQuery);
			} catch (ObjectLockedException ex) {
				String msg = ContextProvider.getContext().getLocaleMessage(
						"group.form.store.lock.msg");
				MessageFormat.format(msg, ex.getLocker().getFullName());
				logger.debug("System group " + systemGroup.getId().getId()
						+ " is locked by " + ex.getLocker().getFullName(), ex);
				throw new DataException(msg);
			} finally {
				if (isLocked) {
					unLockGroup(systemGroup, user);
				}
			}
		} else {
			// ���� ������ ����� - ��������� ��� ����
			SystemGroup systemGroup = new SystemGroup();
			String groupCode = (String)systemGroupId.getId();
			systemGroup.setGroupCode(groupCode);
			systemGroup.setSystemRoles(groupRoles);
			systemGroup.setNameRu(nameRu);
			systemGroup.setNameEn(nameEn);
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(systemGroup);
			// ��� - ����� �������� �������� ���� �������
			saveQuery.setAccessChecker(null);
			saveQuery.setObject(systemGroup);
			savedGroup = (ObjectId) getDatabase().executeQuery(user, saveQuery);
		}
		SystemGroup systemGroup = getGroupByValue(savedGroup);
		name = systemGroup.getName();
		return systemGroup;
	}
	
	private void lockGroup(SystemGroup systemGroup, UserData user) throws DataException {
		LockObject actionLock = new LockObject(systemGroup.getId());
		final ActionQueryBase query = getQueryFactory().getActionQuery(actionLock);
		query.setAction(actionLock);
		getDatabase().executeQuery(user, query);	
	}
	
	private void unLockGroup(SystemGroup systemGroup, UserData user) throws DataException {
		UnlockObject actionUnLock = new UnlockObject(systemGroup.getId());
		final ActionQueryBase query = getQueryFactory().getActionQuery(actionUnLock);
		query.setAction(actionUnLock);
		getDatabase().executeQuery(user, query);
	}
	
	private List<SystemRole> getSystemRoles(String roleValues) throws DataException {
		List<SystemRole> listSystemRoles = new ArrayList<SystemRole>();
		String[] values = roleValues.split("]>");
		for(String tempValue: values){ 
			String value = tempValue.replaceAll("<\\[", "").replaceAll("]>", "").trim();
			if (value!=null&&!value.isEmpty()){
				SystemRole systemRole = new SystemRole();
				systemRole.setRoleCode(value);
				systemRole.setId(new ObjectId(SystemRole.class, value));
				// ��������� �������� �� ������������/������������� ����
				ObjectId roleId = systemRole.getId();
				ObjectQueryBase roleQuery = getQueryFactory().getFetchQuery(roleId.getType());
				roleQuery.setAccessChecker(null);
				roleQuery.setId(roleId);
				SystemRole systemRoleFetch = null;
				try {
					systemRoleFetch = (SystemRole)getDatabase().executeQuery(getUser(), roleQuery);
				} catch(Exception e) {
					throw new DataException("group.form.store.not.role.msg", new Object[]{value});
				}
				listSystemRoles.add(systemRoleFetch);
			}
		}
		return listSystemRoles;
	}
	
	private SystemGroup getGroupByValue(ObjectId groupId) throws DataException {
		if (logger.isDebugEnabled()){
			logger.debug(MessageFormat.format("Try get system group with 'group_code' equal {1}", new Object[]{groupId}));
		}
		if (groupId == null || groupId.getId() == null){
			if (logger.isDebugEnabled()){
				logger.debug(MessageFormat.format("Input group is null, searching break", new Object[]{}));
			}
			return null;
		}
		ObjectQueryBase groupQuery = getQueryFactory().getFetchQuery(groupId.getType());
		groupQuery.setAccessChecker(null);
		groupQuery.setId(groupId);
		SystemGroup systemGroup = (SystemGroup)getDatabase().executeQuery(getUser(), groupQuery);
		return systemGroup;
	}

	@Override
	protected String getName() {
		return name;
	}
}