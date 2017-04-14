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

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.filter.SystemRolesFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

//import com.aplana.dbmi.service.impl.query.ListAllSystemRoles;

public class UserRolesAndGroupsAttributeEditor extends JspAttributeEditor {
	public final static String ALL_SYSTEM_ROLES = "allRoles";
	public final static String ALL_SYSTEM_GROUPS = "allGroups";
	public final static String COPY_USER_ROLES_CARD_ACTION = "COPY_USER_ROLES_CARD_ACTION";
	
	public UserRolesAndGroupsAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/UserRolesAndGroupsEdit.jsp");
	}

	public boolean gatherData(ActionRequest request, Attribute attr) throws DataException {
		
		String assignedGroups = request.getParameter(JspAttributeEditor.getAttrHtmlId(attr) + "_assignedGroups");
		String assignedRoles = request.getParameter(JspAttributeEditor.getAttrHtmlId(attr) + "_assignedRoles");
		String excludedRoleGroups = request.getParameter(JspAttributeEditor.getAttrHtmlId(attr) + "_excludedRoleGroups");

		Set<String> assignedGroupCodes = new HashSet<String>();
		Set<String> assignedRoleCodes = new HashSet<String>();
		Map<String, Set<String>> excludedGroupRoleCodes = new HashMap<String, Set<String>>(); // group, roles
		
		if (null != assignedRoles && !assignedRoles.isEmpty()) {
			assignedRoleCodes.addAll(Arrays.asList(assignedRoles.split(",")));
		}
		((UserRolesAndGroupsAttribute) attr).setAssignedRoles(assignedRoleCodes);

		if (null != assignedGroups && !assignedGroups.isEmpty()) {
			assignedGroupCodes.addAll(Arrays.asList(assignedGroups.split(",")));
		}
		((UserRolesAndGroupsAttribute) attr).setAssignedGroups(assignedGroupCodes);
		
		if (null != excludedRoleGroups && !excludedRoleGroups.isEmpty()) {
			try {
				JSONArray dataJArray = new JSONArray(excludedRoleGroups);
				for(int i = 0; i < dataJArray.length(); i++) {
					JSONObject object = (JSONObject)dataJArray.get(i);
					final String roleId = (String)object.get("roleId");
					final JSONArray groupsJArray = (JSONArray)object.get("groups");
	
					for(int k = 0; k < groupsJArray.length(); k++) {
						String groupId = (String)((JSONObject)groupsJArray.get(k)).get("id");
							if (assignedGroupCodes.contains(groupId)) {
								if (excludedGroupRoleCodes.containsKey(groupId)) {
									Set<String>roles = excludedGroupRoleCodes.get(groupId);
									roles.add(roleId);
								} else {
									Set<String> exRoles = new HashSet<String>();
									exRoles.add(roleId);
									excludedGroupRoleCodes.put(groupId, exRoles);
								}
							}
						}
				}
			} catch (JSONException e) {
				logger.error("Cannot set excluded group roles: " + excludedRoleGroups, e);
			}
		}
		((UserRolesAndGroupsAttribute) attr).setExcludedGroupRoleCodes(excludedGroupRoleCodes);
		return true;
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
	}
	
	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		if (!((UserRolesAndGroupsAttribute)attr).isInitialized()) {
			try {
				Collection<SystemRole> allRoles = serviceBean.filter(SystemRole.class, new SystemRolesFilter());
				Collection<SystemGroup> allGroups = serviceBean.listAll(SystemGroup.class);
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), ALL_SYSTEM_ROLES, allRoles);
				sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), ALL_SYSTEM_GROUPS, allGroups);
			} catch (Exception e) {
				logger.error("Cannot get system dictionary groups and roles.", e);
			}
			if (null != sessionBean.getActiveCard() && null != sessionBean.getActiveCard().getId()) {
				try {
					setUserGroupAndRoles(sessionBean.getActiveCard().getId(), serviceBean, (UserRolesAndGroupsAttribute) attr);
				} catch (Exception e) {
						logger.error("Cannot get user groups and roles.", e);
				}
			}
			((UserRolesAndGroupsAttribute) attr).setInitialized(true);
		}

		super.writeEditorCode(request, response, attr);
	}
	
	public boolean processAction(ActionRequest request,
			ActionResponse response, Attribute attr) throws DataException {
		final String action = request.getParameter(CardPortlet.ACTION_FIELD);

		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		DataServiceBean serviceBean = sessionBean.getServiceBean();

		if (COPY_USER_ROLES_CARD_ACTION.equals(action)) {
			String userCardId = request.getParameter(JspAttributeEditor.getAttrHtmlId(attr) + "_userCardToCopy");
			if (userCardId != null && !userCardId.isEmpty()) {
				try {
					setUserGroupAndRoles(new ObjectId(Card.class, Long.parseLong(userCardId)), serviceBean, (UserRolesAndGroupsAttribute) attr);
				} catch (Exception e) {
					logger.error("Cannot set user groups and roles for copying.", e);
				}
			}
		} else {
			return super.processAction(request, response, attr);
		}
		return true;
	}
	
	
	private void setUserGroupAndRoles(ObjectId cardId, DataServiceBean serviceBean, UserRolesAndGroupsAttribute attr)
			throws DataException, ServiceException {
		if (cardId == null || serviceBean == null || attr == null)
			return;
		//Get ungrouped roles, groups and excluded group roles assigned to user
		GetPersonByCard action = new GetPersonByCard(cardId);
		Person person = serviceBean.doAction(action);
		if (null != person) {
			Collection<UngroupedRole> userRoles = serviceBean.listChildren(person.getId(), UngroupedRole.class);
			Set<String> userRoleCodes = new HashSet<String>();
			for (UngroupedRole role : userRoles) {
				userRoleCodes.add(role.getType());
			}
			attr.setAssignedRoles(userRoleCodes);
			
			Collection<Group> userGroups = serviceBean.listChildren(person.getId(), Group.class);
			Map<String, Set<String>> excludedGroupRoleCodes = new HashMap<String, Set<String>>();
			for (Group group : userGroups) {
				excludedGroupRoleCodes.put(group.getType(), group.getExcludedRoles());
			}
			attr.setAssignedGroups(excludedGroupRoleCodes.keySet());
			attr.setExcludedGroupRoleCodes(excludedGroupRoleCodes);
		} else {
			logger.warn("Cannot find user by card " + cardId);
		}
	}
}
