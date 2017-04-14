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
package com.aplana.dbmi.useradmin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

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
public class UserAdminPortletSessionBean {
	
	private String searchTemplate = "";
	private AsyncDataServiceBean serviceBean = null;
	private Collection roleTypes;

	// view list properties
	private int listSize = 25;
  	private List userList = null;
  	
  	
  	// user properties
	private Person user = null;
	private Role role = null;
	// reference properties
	private HashMap referenceEntitiesEditMode = new HashMap();

	private boolean isEditMode = false;
	private boolean isDetailViewMode = false;
		
	protected AsyncDataServiceBean getServiceBean(PortletRequest request) {
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
	
	public String getSearchTemplate() {
		return searchTemplate;
	}

	public void setSearchTemplate(String searchTemplate) {
		this.searchTemplate = searchTemplate;
	}

	public int getListSize() {
		return listSize;
	}

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	public HashMap getReferenceEntitiesEditMode() {
		return referenceEntitiesEditMode;
	}

	public void setReferenceEntitiesEditMode(HashMap referenceEntitiesEditMode) {
		this.referenceEntitiesEditMode = referenceEntitiesEditMode;
	}

	public Person getUser() {
		return user;
	}

	public void setUser(Person user) {
		this.user = user;
	}

	public List getUserList() {
		return userList == null ? new ArrayList() : userList;
	}

	public void setUserList(List userList) {
		this.userList = userList;
	}
	public boolean isDetailViewMode() {
		return isDetailViewMode;
	}
	public void setDetailViewMode(boolean isDetailViewMode) {
		this.isDetailViewMode = isDetailViewMode;
	}
	public boolean isEditMode() {
		return isEditMode;
	}
	public void setEditMode(boolean isEditMode) {
		this.isEditMode = isEditMode;
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public Collection getRoleTypes() {
		return roleTypes;
	}
	public void setRoleTypes(Collection roleTypes) {
		this.roleTypes = roleTypes;
	}
	public SystemRole getSystemRole(String roleCode) {
		if (roleTypes == null) {
			return null;
		}
		
		Iterator i = roleTypes.iterator();
		while (i.hasNext()) {
			SystemRole systemRole = (SystemRole)i.next();
			if (systemRole.getId().getId().equals(roleCode)) {
				return systemRole;
			}
		}
		return null;
	}
}
