<%--

      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to you under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.

--%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.model.Group"%>
<%@page import="com.aplana.dbmi.model.SystemRole"%>
<%@page import="com.aplana.dbmi.model.UngroupedRole"%>
<%@page import="com.aplana.dbmi.model.UserRolesAndGroupsAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.UserRolesAndGroupsAttributeViewer"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONObject"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script 
	type="text/javascript" 
	src="<%=request.getContextPath() + "/js/sortable.min.js"%>">
</script>

<link rel="stylesheet" type="text/css" href="/DBMI-Portal/theme/sortable-theme-view.css" />

<portlet:defineObjects />

<%	UserRolesAndGroupsAttribute attr = (UserRolesAndGroupsAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	CardPortletCardInfo cardInfo = CardPortlet.getSessionBean(renderRequest).getActiveCardInfo();
	List<UngroupedRole> assignedRoles = (List<UngroupedRole>)cardInfo.getAttributeEditorData(attr.getId(), UserRolesAndGroupsAttributeViewer.ASSIGNED_ROLES);
	List<Group> assignedGroups = (List<Group>)cardInfo.getAttributeEditorData(attr.getId(), UserRolesAndGroupsAttributeViewer.ASSIGNED_GROUPS);
	Set<String> excludedRoles = (Set<String>)cardInfo.getAttributeEditorData(attr.getId(), UserRolesAndGroupsAttributeViewer.EXCLUDED_ROLES);

	final JSONArray assignedRolesData = new JSONArray();
	
	if (null != assignedRoles) {
		for (UngroupedRole role : assignedRoles){
			JSONObject jo = new JSONObject();
			jo.put("id", role.getType());
			jo.put("name", role.getName());
			assignedRolesData.put(jo);
		}
	}
	
	final JSONArray assignedGroupsData = new JSONArray();
	final JSONArray selectedGroupIds = new JSONArray();
	
	if (null != assignedGroups) {
		for (Group group : assignedGroups){
			JSONObject jo = new JSONObject();
			jo.put("id", group.getType());
			jo.put("name", group.getName());

			List<SystemRole> groupRoles = (List<SystemRole>)group.getSystemGroup().getSystemRoles();
			final JSONArray groupRolesData = new JSONArray();
			for (SystemRole role : groupRoles){
				JSONObject joRole = new JSONObject();
				joRole.put("id", role.getId().getId());
				joRole.put("name", role.getName());
				groupRolesData.put(joRole);
			}
			jo.put("roles", groupRolesData);
			assignedGroupsData.put(jo);
			selectedGroupIds.put(group.getType());
		}
	}
	
	final JSONArray excludedRoleIds = new JSONArray();
	if (null != excludedRoles) {
		for (String exRoleCode : excludedRoles){
			excludedRoleIds.put(exRoleCode);
		}
	}
%>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<script type="text/javascript" language="javascript">
	dojo.require("dijit.dijit");
	dojo.require("dijit.form.FilteringSelect");
	dojo.require("dojo.data.ItemFileReadStore");

	var ${attrHtmlId}_mapRoleValues = <%=assignedRolesData%>;
	var ${attrHtmlId}_mapGroupValues = <%=assignedGroupsData%>;
	var ${attrHtmlId}_excludedRoleIds = <%=excludedRoleIds%>;
	var ${attrHtmlId}_selectedGroupIds = <%=selectedGroupIds%>;

	dojo.addOnLoad(function() {
		${attrHtmlId}_refreshGroupsTable();
		${attrHtmlId}_refreshRolesTable();
	});
	
	function ${attrHtmlId}_refreshGroupsTable() {
		var table = dojo.byId('${attrHtmlId}_groupsTable'); // Table
		for (var i = table.rows.length - 1; i >= 0; --i) {
			table.deleteRow(i);
		}

		for (var j = 0; j < ${attrHtmlId}_mapGroupValues.length; j++) {
			var row = table.insertRow(j); //TableRow
			var cell = row.insertCell(0); // TableCell
			var group = ${attrHtmlId}_mapGroupValues[j];
			cell.innerHTML=${attrHtmlId}_mapGroupValues[j].name;
			cell.innerHTML +='<a href="javascript:form_collapse(\'' + group.id + '\')" class="noLine"><span class="arrow_up" id="ARROW_' + group.id + '">&nbsp;</span></a>'
			var iDiv = document.createElement('div');
			iDiv.id = 'BODY_' + group.id;
			iDiv.className = 'block';
			iDiv.style.margin = "10px 0px 10px 10px";
			iDiv.style.display = "none";
			groupRoles = group.roles;
			for (var k = 0; k < groupRoles.length; k++) {
				iDiv.innerHTML += '<p style="font-weight: normal; font-size: 10px">' + groupRoles[k].name + '</p>';
			}
			cell.appendChild(iDiv);
		}
	}
		
	function ${attrHtmlId}_refreshRolesTable() {
		var table = dojo.byId('${attrHtmlId}_rolesTable') // Table
		for (var i = table.rows.length - 1; i > 0; --i) {
			table.deleteRow(i);
		}
		
		if (${attrHtmlId}_mapRoleValues.length == 0 && ${attrHtmlId}_selectedGroupIds.length == 0){
			dojo.style(dojo.byId('${attrHtmlId}_RolesTableDiv'), {display:'none'});
			return;
		}else {
			dojo.style(dojo.byId('${attrHtmlId}_RolesTableDiv'), {display:'block'});
		}
			
		var tBody = dojo.byId('${attrHtmlId}_rolesBody') // tBody
		for (var i = 0; i < ${attrHtmlId}_mapRoleValues.length; i++) {
			var row = tBody.insertRow(0); //TableRow
			var cell = row.insertCell(0); // TableCell
			cell.innerHTML = ${attrHtmlId}_mapRoleValues[i].name;
			var cell = row.insertCell(1); // TableCell
		}
			
		if (${attrHtmlId}_selectedGroupIds.length > 0) {

			var groupedRoles;
			var groupedRolesSize = 0;
			
			dojo.xhrGet({
				url: "/DBMI-UserPortlets/servlet/UserRolesAndGroupsFilterServlet?reverseMode=1&selectedGroupIds=" 		
						+ ${attrHtmlId}_selectedGroupIds,
				sync: true,
				handleAs: 'json',
				load: function(data) {
					groupedRoles = data;
					groupedRolesSize = data.length;
				},
				error: function(error) {
					console.error(error);
				}
			});
			
			if(!groupedRoles){
				console.error('Не удалось получить список ролей для назначенных групп');
				return;
			}
			outer:
			for (var i = 0; i < groupedRoles.length; i++) {
				for (var k = 0; k < ${attrHtmlId}_excludedRoleIds.length; k++) {
					if (${attrHtmlId}_excludedRoleIds[k] == groupedRoles[i].id) {
						//do not display excluded roles
						continue outer;
					}
				}

				var row = tBody.insertRow(0) //TableRow
				var cell = row.insertCell(0) // TableCell
				cell.innerHTML=groupedRoles[i].name;
				var cell = row.insertCell(1) // TableCell
				for (var k = 0; k < groupedRoles[i].groups.length; k++) {
					cell.innerHTML += groupedRoles[i].groups[k].name + '</br>';
				}
			}
		}
		
		dojo.byId('${attrHtmlId}_rolesColumn').removeAttribute("data-sorted");
		Sortable.initTable(table);
		dojo.byId("${attrHtmlId}_rolesColumn").click();
	}
</script>

<span>				  	
	<p><b>Группы ролей:</b></p>
</span>
<div id="${attrHtmlId}_GroupsTableDiv" style="margin: 10px 0px 10px 10px";>
	<table id="${attrHtmlId}_groupsTable"> 
		<colgroup>
			<col width="90%">
			<col width="10%">
		</colgroup>
	</table>
</div>
<span>				  	
	<p><b>Роли пользователя:</b></p>
</span>
<div id="${attrHtmlId}_RolesTableDiv">
	<table id="${attrHtmlId}_rolesTable" style="width: 100%" class="sortable-theme-view" data-sortable>
		<colgroup>
			<col width="60%">
			<col width="40%">
		</colgroup>
		<thead>
			<tr>
				<th id="${attrHtmlId}_rolesColumn">Роль</th> 
				<th data-sortable="false">Группа</th>
			</tr>
		</thead>
		<tbody id="${attrHtmlId}_rolesBody"></tbody>
	</table>
</div>
