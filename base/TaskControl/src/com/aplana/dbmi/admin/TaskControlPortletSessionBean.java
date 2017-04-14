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
package com.aplana.dbmi.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.task.Scheduler;
import com.aplana.dbmi.task.SchedulerException;
import com.aplana.dbmi.task.TaskInfo;
import com.aplana.dbmi.action.CheckRolesForUser;

/**
 *
 * A sample Java bean that stores portlet instance data in portlet session.
 *
 */
public class TaskControlPortletSessionBean
{
	private Scheduler scheduler;
	private Collection names = new ArrayList();
	private String error = null;
	private boolean showWarningMessage = false;
	private String activeTaskId = null;
	private DataServiceBean serviceBean = null;
	private String editAccessRoles;
	
	protected DataServiceBean getServiceBean(PortletRequest request) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	public DataServiceBean getServiceBean(HttpServletRequest request, String namespace) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	private void initDataServiceBean(PortletRequest request) {
		this.serviceBean = PortletUtil.createService(request);
	}
	
	private void initDataServiceBean(HttpServletRequest request) {
		this.serviceBean = ServletUtil.createService(request);
	}

	public TaskControlPortletSessionBean()
	{
		scheduler = Portal.getFactory().getSchedulerService();
		try {
			names = scheduler.getAvailableTasks();
		} catch (SchedulerException e) {
			setError(e.getMessage());
		}
	}
	
	public void startTask(TaskInfo task)
	{
		try {
			if (isEditAccessExists()){
				scheduler.startTask(task);
			} else {
				throw new DataException("admin.edit.access.error");
			}
		} catch (Exception e) {
			setError(e.getMessage());
		}
	}
	
	/**
	 * �������� ��������� �� ������ ���������� 
	 * � ������������ ����������� ���������� ���������� (��� �������� � ���������� �������) (����� �������� ��� �������������)
	 * @param id - UID ���������
	 */
	public void cancelTask(String id)
	{
		cancelTask(id, false);
	}
	
	/**
	 * �������� ��������� �� ������ ���������� 
	 * � ������� ������� ��� �������� ��������� ���������� (��� �������� � ���������� �������)
	 * @param id - UID ���������
	 * @param isDeleteParams - ��(������� ���������)/���(�������� ��� �������� � ���������� �������)
	 */
	public void cancelTask(String id, boolean isDeleteParams)
	{
		try {
			if (isEditAccessExists()){
				scheduler.cancelTask(id, isDeleteParams);
			} else {
				throw new DataException("admin.edit.access.error");
			}
		} catch (Exception e) {
			setError(e.getMessage());
		}
	}

	public Collection getNames() {
		return names;
	}
	
	public Collection getTasks() {
		try {
			return scheduler.getScheduledTasks();
		} catch (SchedulerException e) {
			setError(e.getMessage());
			return Collections.EMPTY_LIST;
		}
	}
	
	public String getError() {
		return error;
	}
	
	public boolean isTaskParamsExists(String id) {
		try {
			int paramsCount = scheduler.getSchedulerParametersCount(id);
			return (paramsCount>0);
		} catch (SchedulerException e) {
			setError(e.getMessage());
			return false;
		}	
	}

	public void setError(String message) {
		error = message;
	}
	
	public void clearError() {
		error = null;
	}
	
	public boolean isShowWarningMessage() {
		return showWarningMessage;
	}

	public void setShowWarningMessage(boolean showWarningMessage) {
		this.showWarningMessage = showWarningMessage;
	}

	public String getActiveTaskId() {
		return activeTaskId;
	}

	public void setActiveTaskId(String activeTaskId) {
		this.activeTaskId = activeTaskId;
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
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
			checkAction.setPersonLogin(serviceBean.getUserName());
			checkAction.setRoles(editAccessRoles);
			return (Boolean)serviceBean.doAction(checkAction);
		}
		// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
		return true;
	}

	public String suffix(int number) {
		if ((number / 10) % 10 == 1)
			return "th";
		if (number % 10 == 1)
			return "st";
		if (number % 10 == 2)
			return "nd";
		if (number % 10 == 3)
			return "rd";
		return "th";
	}
}
