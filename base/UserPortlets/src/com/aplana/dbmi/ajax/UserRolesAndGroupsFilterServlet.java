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
package com.aplana.dbmi.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean.GroupExecutionMode;
import com.aplana.dbmi.support.action.GetGroupExecutionReports;

public class UserRolesAndGroupsFilterServlet extends AbstractDBMIAjaxServlet {
	private static final long serialVersionUID = 1L;
	private static final String PARAM_SELECTED_GROUP_IDS = "selectedGroupIds";
	private static final String PARAM_REVERSE_MODE = "reverseMode";

	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
			DataServiceBean serviceBean = getDataServiceBean(request);
			if (null != request.getParameter(PARAM_SELECTED_GROUP_IDS)) {
				String selectedGroupIds = request.getParameter(PARAM_SELECTED_GROUP_IDS);
				List<String> selectedGroupIdsList = Arrays.asList(selectedGroupIds.split(","));
				if (selectedGroupIdsList.isEmpty())
					return;
				try {
					List<SystemGroup> allGroups = (List<SystemGroup>)serviceBean.listAll(SystemGroup.class);
					if (null == request.getParameter(PARAM_REVERSE_MODE)) {
						//Get groups with their roles
						final JSONArray selectedGroupsData = new JSONArray();
						for (SystemGroup group : allGroups) {
							if (selectedGroupIdsList.contains((String)group.getId().getId())) {
								JSONObject jo = new JSONObject();
								jo.put("id", group.getId().getId());
								jo.put("name", group.getName());
										
								List<SystemRole> groupRoles = (List<SystemRole>)group.getSystemRoles();
								final JSONArray groupRolesData = new JSONArray();
								for (SystemRole role : groupRoles){
									JSONObject joRole = new JSONObject();
									joRole.put("id", role.getId().getId());
									joRole.put("name", role.getName());
									groupRolesData.put(joRole);
								}
								jo.put("roles", groupRolesData);
								selectedGroupsData.put(jo);
							}
						}
						response.getWriter().write(selectedGroupsData.toString());

					} else {
						//Get roles with their groups
						final Map<SystemRole, List<SystemGroup>> allRoleGroups = new HashMap<SystemRole, List<SystemGroup>>();
						for (SystemGroup group : allGroups) {
							if (selectedGroupIdsList.contains((String)group.getId().getId())) {
								List<SystemRole> groupRoles = (List<SystemRole>)group.getSystemRoles();
								for (SystemRole role : groupRoles) {
									if (!allRoleGroups.containsKey(role)) {
										List<SystemGroup> groups = new ArrayList<SystemGroup>();
										groups.add(group);
										allRoleGroups.put(role, groups);
									}else {
										allRoleGroups.get(role).add(group);
									}
								}
							}
						}
						final JSONArray selectedGroupRolesData = new JSONArray();
						for (Map.Entry<SystemRole, List<SystemGroup>> entry : allRoleGroups.entrySet()) {
							JSONObject joRole = new JSONObject();
							SystemRole sRole = entry.getKey();
							joRole.put("id", sRole.getId().getId());
							joRole.put("name", sRole.getName());
										
							List<SystemGroup> roleGroups = entry.getValue();
							// sorting group list
							Collections.sort(
								roleGroups,
								new Comparator() {
									public int compare(Object o1, Object o2) {
										SystemGroup group1 = (SystemGroup)o1, group2 = (SystemGroup)o2;
										if (null == group1.getName()) {
											return -1;
										} else {
											return group1.getName().compareTo(group2.getName());
										}
									}
								}
							);
							final JSONArray roleGroupsData = new JSONArray();
							for (SystemGroup group : roleGroups){
								JSONObject joGroup = new JSONObject();
								joGroup.put("id", group.getId().getId());
								joGroup.put("name", group.getName());
								roleGroupsData.put(joGroup);
							}
							joRole.put("groups", roleGroupsData);
							selectedGroupRolesData.put(joRole);
						}
						response.getWriter().write(selectedGroupRolesData.toString());
					}
				} catch (Exception e) {
					logger.error("Exception caught while loading selected groups: " + selectedGroupIds, e);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}
		}
}