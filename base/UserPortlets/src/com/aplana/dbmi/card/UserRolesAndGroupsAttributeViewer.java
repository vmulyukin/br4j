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
package com.aplana.dbmi.card;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Group;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.UngroupedRole;
import com.aplana.dbmi.service.DataServiceBean;

public class UserRolesAndGroupsAttributeViewer extends JspAttributeViewer {
	public final static String ASSIGNED_ROLES = "assignedRoles";
	public final static String ASSIGNED_GROUPS = "assignedGroups";
	public final static String EXCLUDED_ROLES = "excludedRoles";
	
	public UserRolesAndGroupsAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/UserRolesAndGroupsView.jsp");
	}
	
	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean();
			if (null != sessionBean.getActiveCard() && null != sessionBean.getActiveCard().getId()) {
			try {
				//Get groups and ungrouped roles assigned to user
				//Get current groups and ungrouped roles assigned to user
				GetPersonByCard action = new GetPersonByCard(sessionBean.getActiveCard().getId());
				Person person = (Person)serviceBean.doAction(action);
				
				Collection<UngroupedRole> assignedRoles = serviceBean.listChildren(person.getId(), UngroupedRole.class);
				Collection<Group> assignedGroups = serviceBean.listChildren(person.getId(), Group.class);
				Set<String> excludedRoles = new HashSet<String>();
				for (Group group : assignedGroups) {
					Set<String> groupExcludedRoles = group.getExcludedRoles();
					excludedRoles.addAll(groupExcludedRoles);
				}
				
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), ASSIGNED_ROLES, assignedRoles);
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), ASSIGNED_GROUPS, assignedGroups);
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), EXCLUDED_ROLES, excludedRoles);
			} catch (Exception e) {
				logger.error("Cannot get user groups and roles.", e);
			}
		}
		super.writeEditorCode(request, response, attr);
	}
}
