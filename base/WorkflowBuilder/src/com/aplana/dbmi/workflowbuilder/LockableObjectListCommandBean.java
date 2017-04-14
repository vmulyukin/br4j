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
package com.aplana.dbmi.workflowbuilder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class LockableObjectListCommandBean {
	private String message;
	private List objects;
	private LockableObject selectedObject;
	private AsyncDataServiceBean dataService;
	private Map sortAndPaginationParameters = new HashMap();
	private String editAccessRoles;
	private boolean editAccessExists=false;
	
	public AsyncDataServiceBean getDataService() {
		return dataService;
	}
	
	public void setDataService(AsyncDataServiceBean dataService) {
		this.dataService = dataService;
	}

	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

	public List getObjects() {
		return objects;
	}

	public void setObjects(List objects) {
		this.objects = objects;
	}

	public LockableObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(LockableObject selectedObject) {
		this.selectedObject = selectedObject;
	}

	public Map getSortAndPaginationParameters() {
		return sortAndPaginationParameters;
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}

	public void memorizeSortAndPagination(RenderRequest request) {
		Enumeration e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String paramName = (String)e.nextElement();
			if (paramName.matches("^d-[0-9]+-(p|s|o)$")) {
				sortAndPaginationParameters.put(paramName, request.getParameter(paramName));
			}
		}
	}

	/**
	 * ��������� ������� � �������� ������������ ���� �� �������������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isEditAccessExists() throws DataException, ServiceException{
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(dataService.getUserName());
			checkAction.setRoles(editAccessRoles);
			editAccessExists = (Boolean)dataService.doAction(checkAction);
		} else
			// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
			editAccessExists = true;
		return editAccessExists;
	}
}
