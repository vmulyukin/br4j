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
package com.aplana.dbmi.roleadmin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import com.aplana.dbmi.common.utils.portlet.PortletMessage;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.UserPrincipal;

/**
 *
 * A sample Java bean that stores portlet instance data in portlet session.
 *
 */
public class RoleAdminPortletSessionBean {
	
	private AsyncDataServiceBean serviceBean = null;
	private SystemRole systemRole = null;
	private boolean editMode = false;
	private boolean newMode = false;
	private boolean openedInEditMode = false;

	private PortletMessage message = null;
	private String backURL = null; 
	private List<Map<String,Object>> roleRulesInfo;
		
	public AsyncDataServiceBean getServiceBean(PortletRequest request) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		}  else {
            String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(Boolean.TRUE);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(Boolean.FALSE);
            }
        }
		return serviceBean;
	}
	private void initDataServiceBean(PortletRequest request) {
		this.serviceBean = PortletUtil.createService(request);
	}

	public boolean isEditMode() {
		return editMode;
	}
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}
	public boolean isNewMode() {
		return newMode;
	}
	public void setNewMode(boolean newMode) {
		this.newMode = newMode;
	}
	
	public SystemRole getSystemRole() {
		return systemRole;
	}
	public void setSystemRole(SystemRole systemRole) {
		this.systemRole = systemRole;
	}
	public String getMessage() {
		return (message == null) ? null : message.getMessage();
	}
	public void setMessage(String message) {
		if (this.message != null)
			this.message.setMessage(message);
		else 
			this.message = new PortletMessage(message);
	}
	
	public void setMessageWithType(String message, PortletMessageType messageType) {
	
		if (this.message != null){
			this.message.setMessage(message);
			this.message.setMessageType(messageType);
		}
		else 
			this.message = new PortletMessage(message, messageType);
	}
	
	public void setMessageWithType(String message, Object[] params, PortletMessageType messageType){
		
		if (params != null)
			message = MessageFormat.format(message, params);
		this.setMessageWithType(message, messageType);
	}
	

	public PortletMessage getPortletMessage(){
		return this.message;
	}

	public void setPortletMessage(PortletMessage pm){
		this.message = pm;
	}
	
	public String getBackURL() {
		return backURL;
	}

	public void setBackURL(String backURL) {
		this.backURL = backURL;
	}
	
	public boolean isOpenedInEditMode() {
		return openedInEditMode;
	}
	public void setOpenedInEditMode(boolean openedInEditMode) {
		this.openedInEditMode = openedInEditMode;
	}
	public List<Map<String, Object>> getRoleRulesInfo() {
		return roleRulesInfo;
	}
	public void setRoleRulesInfo(List<Map<String, Object>> roleRulesInfo) {
		this.roleRulesInfo = roleRulesInfo;
	}
	
}
