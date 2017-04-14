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
<%@page import="com.aplana.dbmi.ajax.UserRolesSearchInternalPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.Role"%>
<%@page import="com.aplana.dbmi.model.SystemRole"%>
<%@page import="com.aplana.dbmi.model.SystemGroup"%>
<%@page import="com.aplana.dbmi.model.UserRolesAndGroupsAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.UserRolesAndGroupsAttributeEditor"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONObject"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script 
	type="text/javascript" 
	src="<%=request.getContextPath() + "/js/sortable.min.js"%>">
</script>

<link rel="stylesheet" type="text/css" href="/DBMI-Portal/theme/sortable-theme-edit.css" />
<portlet:defineObjects />
	
<%	UserRolesAndGroupsAttribute attr = (UserRolesAndGroupsAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	CardPortletCardInfo cardInfo = CardPortlet.getSessionBean(renderRequest).getActiveCardInfo();
	List<SystemRole> allRoles = (List<SystemRole>)cardInfo.getAttributeEditorData(attr.getId(), UserRolesAndGroupsAttributeEditor.ALL_SYSTEM_ROLES);
	List<SystemGroup> allGroups = (List<SystemGroup>)cardInfo.getAttributeEditorData(attr.getId(), UserRolesAndGroupsAttributeEditor.ALL_SYSTEM_GROUPS);
	
	final JSONArray allRolesData = new JSONArray();
	for (SystemRole role : allRoles){
		JSONObject jo = new JSONObject();
		jo.put("id", role.getId().getId());
		jo.put("name", role.getName());
		allRolesData.put(jo);
	}
	
	final JSONArray allGroupsData = new JSONArray();
	for (SystemGroup group : allGroups){
		JSONObject jo = new JSONObject();
		jo.put("id", group.getId().getId());
		jo.put("name", group.getName());
		allGroupsData.put(jo);
	}
	
	JSONArray selectedRoleIds = new JSONArray();
	if (attr.getAssignedRoles() != null) {
		Iterator iterValues = attr.getAssignedRoles().iterator();
		while (iterValues.hasNext()) {
			String value = (String)iterValues.next();
			selectedRoleIds.put(value);
		}
	}
	
	JSONArray selectedGroupIds = new JSONArray();
	if (attr.getAssignedGroups() != null) {
		Iterator iterValues = attr.getAssignedGroups().iterator();
		while (iterValues.hasNext()) {
			String value = (String)iterValues.next();
			selectedGroupIds.put(value);
		}
	}
	
	Map<String, Set<String>> excludedGroupRoles = attr.getExcludedGroupRoleCodes();
	Map<String, Set<String>> excludedRoleGroups = new HashMap<String, Set<String>>();
	if (excludedGroupRoles != null) {
		for (Map.Entry<String, Set<String>> pair : excludedGroupRoles.entrySet()) {
			final String groupId = pair.getKey();
			for (String roleId : pair.getValue()) {
				if (excludedRoleGroups.containsKey(roleId)) {
				    Set<String>groups = excludedRoleGroups.get(roleId);
				    groups.add(groupId);
				} else {
				    excludedRoleGroups.put(roleId, new HashSet<String>(){{
				    	   add(groupId);
				    }});
				}
			}
		}
	}

	JSONArray excludedRoleGroupIds = new JSONArray();
	if (!excludedRoleGroups.isEmpty()) {
		for (Map.Entry<String, Set<String>> pair : excludedRoleGroups.entrySet()) {
			JSONObject jo = new JSONObject();
			jo.put("roleId", pair.getKey());
			jo.put("groups", pair.getValue());
			excludedRoleGroupIds.put(jo);
		}
	}
%>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<script type="text/javascript" language="javascript">
	dojo.require("dijit.dijit");
	dojo.require("dijit.form.FilteringSelect");
	dojo.require("dojo.data.ItemFileReadStore");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dijit.form.CheckBox");

	var ${attrHtmlId}_mapRoleValues = <%=allRolesData%>;
	var ${attrHtmlId}_mapGroupValues = <%=allGroupsData%>;
	var ${attrHtmlId}_selectedRoleIds = <%=selectedRoleIds%>;
	var ${attrHtmlId}_selectedGroupIds = <%=selectedGroupIds%>;
	var ${attrHtmlId}_excludedRoleGroupIds = <%=excludedRoleGroupIds%>;

	dojo.addOnLoad(function() {
		var dataStore = new dojox.data.QueryReadStore({
			url :'<%=request.getContextPath() + "/servlet/SearchCardServlet"%>'
		});
		
		var select = new dijit.form.FilteringSelect(
			{
				store: dataStore,
				searchAttr: 'label',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
                style: "width: 80%;",
				query: {
					<%= UserRolesSearchInternalPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= UserRolesSearchInternalPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: ''
				},
				onChange: function(id) {${attrHtmlId}_selectUserValue(id)}
			},
			dojo.byId('${attrHtmlId}_usersSelect')
		);
	});
	
	var ${attrHtmlId}_readStore = null;
	
	dojo.addOnLoad(function() {
		${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapRoleValues, ${attrHtmlId}_selectedRoleIds)}
		);
		
		var ${attrHtmlId}_select = new dijit.form.FilteringSelect(
			{
				store: ${attrHtmlId}_readStore,
				style: 'width: 80%;',
				required: false,
				autoComplete: false,
				pageSize: 15,
				onChange: function(id) {${attrHtmlId}_selectRoleValue(id)}
			},
			dojo.byId('${attrHtmlId}_rolesSelect')
		);
		${attrHtmlId}_refreshRolesControls();
	});
	
	dojo.addOnLoad(function() {
		${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapGroupValues, ${attrHtmlId}_selectedGroupIds)}
		);
		
		var ${attrHtmlId}_select = new dijit.form.FilteringSelect(
			{
				store: ${attrHtmlId}_readStore,
				style: 'width: 80%;',
				required: false,
				autoComplete: false,
				pageSize: 15,
				onChange: function(id) {${attrHtmlId}_selectGroupValue(id)}
			},
			dojo.byId('${attrHtmlId}_groupsSelect')
		);
		${attrHtmlId}_refreshGroupsControls();
	});
	
	function ${attrHtmlId}_refreshRolesControls() {
		${attrHtmlId}_refreshRolesSelect();
		${attrHtmlId}_refreshRolesHidden();
		${attrHtmlId}_refreshRolesTable();
	}
	
	function ${attrHtmlId}_refreshGroupsControls() {
		${attrHtmlId}_refreshGroupsSelect();
		${attrHtmlId}_refreshGroupsHidden();
		${attrHtmlId}_refreshGroupsTable();
		${attrHtmlId}_refreshRolesTable();
		${attrHtmlId}_refreshExcludedRoleGroupsHidden();
	}
	
	function ${attrHtmlId}_refreshRolesSelect() {
		var select = dijit.byId('${attrHtmlId}_rolesSelect');
		select.setValue('');
		
		${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapRoleValues, ${attrHtmlId}_selectedRoleIds)}
		);
		select.store = ${attrHtmlId}_readStore;
	}

	function ${attrHtmlId}_refreshGroupsSelect() {
		var select = dijit.byId('${attrHtmlId}_groupsSelect');
		select.setValue('');
		
		${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapGroupValues, ${attrHtmlId}_selectedGroupIds)}
		);
		select.store = ${attrHtmlId}_readStore;
	}

	function ${attrHtmlId}_getStore(mapValues, selectedIds) {
		var notAvailableIds = {};
		if (selectedIds != null) {
			for (var i = 0; i < selectedIds.length; i++) {
				notAvailableIds[selectedIds[i]] = ''
			}
		}
	
		var store = {}
		store.identifier = 'id';
		store.label = 'name';
		store.items = [];
		for (var i = 0; i < mapValues.length; i++) {
			var id = mapValues[i].id;
			if (notAvailableIds[id] == undefined) {
				var item = {};
				item[store.identifier] = id;
				item[store.label] = mapValues[i].name;
				store.items[store.items.length] = item;
			}
		}

		return store;
	}

	function ${attrHtmlId}_selectUserValue(id) {
		var okButton = dojo.byId('${attrHtmlId}_ok')
		if (id == '') {
			okButton.disabled = true
			return
		}
		var hidden = dojo.byId('${attrHtmlId}_userCardToCopy')
		hidden.value = id
		okButton.disabled = false
	}
	
	function ${attrHtmlId}_selectRoleValue(id) {
		if (id == '')
			return;
		${attrHtmlId}_selectedRoleIds.push(id)
		${attrHtmlId}_refreshRolesControls()
	}
	
	function ${attrHtmlId}_selectGroupValue(id) {
		if (id == '')
			return;
		${attrHtmlId}_selectedGroupIds.push(id)
		${attrHtmlId}_refreshGroupsControls();
	}
	
	function ${attrHtmlId}_deselectRoleValue(id) {
		if (id == '')
			return;
		${attrHtmlId}_selectedRoleIds.splice(id, 1);
		${attrHtmlId}_refreshRolesControls();
	}
	
	function ${attrHtmlId}_deselectGroupValue(id) {
		if (id == '')
			return;
		var new_excludedRoleGroupIds = [];
		for (var i = 0; i < ${attrHtmlId}_excludedRoleGroupIds.length; i++) {
			var new_excludedGroups = [];
			for (var k = 0; k < ${attrHtmlId}_excludedRoleGroupIds[i].groups.length; k++) {
				if (${attrHtmlId}_excludedRoleGroupIds[i].groups[k].id != id) {
					new_excludedGroups.push(${attrHtmlId}_excludedRoleGroupIds[i].groups[k]);
				}
			}
			if (new_excludedGroups.length > 0) {
				var item = {};
				item["roleId"] = ${attrHtmlId}_excludedRoleGroupIds[i].roleId;
				item["groups"] = new_excludedGroups;
				new_excludedRoleGroupIds.push(item);
			}
		}
		${attrHtmlId}_excludedRoleGroupIds = new_excludedRoleGroupIds;
		for (var i = 0; i < ${attrHtmlId}_selectedGroupIds.length; i++) {
			if (${attrHtmlId}_selectedGroupIds[i] == id) {
				${attrHtmlId}_selectedGroupIds.splice(i, 1);
				break;
			}
		}
		${attrHtmlId}_refreshGroupsControls();
	}

	function ${attrHtmlId}_changeGroupRole(checkbox) {
		if (checkbox.checked) {
			${attrHtmlId}_includeGroupRole(checkbox.value);
			checkbox.setAttribute("title", "Отключить роль");
		}else {
			${attrHtmlId}_excludeGroupRole(checkbox.value);
			checkbox.setAttribute("title", "Включить роль");
		}
		${attrHtmlId}_refreshExcludedRoleGroupsHidden();
	}

	function ${attrHtmlId}_excludeGroupRole(item) {
		if (item == '')
			return
		${attrHtmlId}_excludedRoleGroupIds.push(JSON.parse(item));
	}
	
	function ${attrHtmlId}_includeGroupRole(item) {
		if (item == '')
			return
		for (var i = 0; i < ${attrHtmlId}_excludedRoleGroupIds.length; i++) {
			if (${attrHtmlId}_excludedRoleGroupIds[i].roleId == JSON.parse(item).roleId) {
				${attrHtmlId}_excludedRoleGroupIds.splice(i, 1);
				break;
			}
		}
	}
	
	function ${attrHtmlId}_refreshGroupsHidden() {
		var hidden = dojo.byId('${attrHtmlId}_assignedGroups')
		hidden.value = ${attrHtmlId}_selectedGroupIds.join();
	}
	
	function ${attrHtmlId}_refreshRolesHidden() {
		var hidden = dojo.byId('${attrHtmlId}_assignedRoles')
		hidden.value = ${attrHtmlId}_selectedRoleIds.join();
	}
	
	function ${attrHtmlId}_refreshExcludedRoleGroupsHidden() {
		var hidden = dojo.byId('${attrHtmlId}_excludedRoleGroups')
		hidden.value = JSON.stringify(${attrHtmlId}_excludedRoleGroupIds);
	}
	
	function ${attrHtmlId}_refreshRolesTable() {
		var table = dojo.byId('${attrHtmlId}_rolesTable') // Table
		for (var i = table.rows.length - 1; i > 0; --i) {
			table.deleteRow(i);
		}
		if (${attrHtmlId}_selectedRoleIds.length == 0 && ${attrHtmlId}_selectedGroupIds.length == 0){
			dojo.style(dojo.byId('${attrHtmlId}_RolesTableDiv'), {display:'none'});
			return;
		}else {
			dojo.style(dojo.byId('${attrHtmlId}_RolesTableDiv'), {display:'block'});
		}
		
		var tBody = dojo.byId('${attrHtmlId}_rolesBody') // tBody
		for (var i = 0; i < ${attrHtmlId}_selectedRoleIds.length; i++) {
			var row = tBody.insertRow(0) //TableRow
			cell = row.insertCell(0) // TableCell
			cell.innerHTML = "<span title='Удалить роль' class=delete onclick=${attrHtmlId}_deselectRoleValue(" + i +")>&nbsp;</span>"
			var cell = row.insertCell(1) // TableCell
			for (var j = 0; j < ${attrHtmlId}_mapRoleValues.length; j++) {
				// get map value by selected id
				if (${attrHtmlId}_mapRoleValues[j].id == ${attrHtmlId}_selectedRoleIds[i])
					cell.innerHTML = ${attrHtmlId}_mapRoleValues[j].name;
			}
			var cell = row.insertCell(2) // TableCell
		}
		
		if (${attrHtmlId}_selectedGroupIds.length > 0) {

			var groupedRoles;
		
			dojo.xhrGet({
				url: "/DBMI-UserPortlets/servlet/UserRolesAndGroupsFilterServlet?reverseMode=1&selectedGroupIds=" 		
						+ ${attrHtmlId}_selectedGroupIds,
				sync: true,
				handleAs: 'json',
				load: function(data) {
					groupedRoles = data;
				},
				error: function(error) {
					console.error(error);
				}
			});
		
			if(!groupedRoles){
				console.error('Не удалось получить список ролей для назначенных групп');
				return;
			}
			for (var i = 0; i < groupedRoles.length; i++) {
				var row = tBody.insertRow(0) //TableRow
				var cell = row.insertCell(0) // TableCell
				
				var item = {};
				item["roleId"] = groupedRoles[i].id;
				item["groups"] = groupedRoles[i].groups;
				
				var excluded = false;
				for (var k = 0; k < ${attrHtmlId}_excludedRoleGroupIds.length; k++) {
					if (${attrHtmlId}_excludedRoleGroupIds[k].roleId == groupedRoles[i].id) {
						${attrHtmlId}_excludedRoleGroupIds[k].groups = groupedRoles[i].groups;
						excluded = true;
						break;
					}
				}
				if (!excluded)
					cell.innerHTML ="<input id=" + groupedRoles[i].id + " title='Отключить роль' type=checkbox value=" + JSON.stringify(item, excludeNames) + " onchange=${attrHtmlId}_changeGroupRole(this) checked>";
				else
					cell.innerHTML ="<input id=" + groupedRoles[i].id + " title='Включить роль' type=checkbox value=" + JSON.stringify(item, excludeNames) + " onchange=${attrHtmlId}_changeGroupRole(this)>";

				var cell = row.insertCell(1) // TableCell
				cell.innerHTML=groupedRoles[i].name;
				cell = row.insertCell(2) // TableCell
				for (var l = 0; l < groupedRoles[i].groups.length; l++) {
					cell.innerHTML +=groupedRoles[i].groups[l].name + "<br>";
				}
			}
		}
	
		dojo.byId('${attrHtmlId}_rolesColumn').removeAttribute("data-sorted");
		Sortable.initTable(table);
		dojo.byId("${attrHtmlId}_rolesColumn").click();
	}

	function excludeNames(key,value)
	{
	    if (key=="name") 
	    	return undefined;
	    else 
	    	return value;
	}
	
	function ${attrHtmlId}_refreshGroupsTable() {

		var table = dojo.byId('${attrHtmlId}_groupsTable'); // Table
		for (var i = table.rows.length - 1; i >= 0; --i) {
			table.deleteRow(i);
		}
	
		var groups;
		var groupsSize = 0;

		dojo.xhrGet({
			url: "/DBMI-UserPortlets/servlet/UserRolesAndGroupsFilterServlet?selectedGroupIds=" + ${attrHtmlId}_selectedGroupIds,
			sync: true,
			handleAs: 'json',
			load: function(data) {
				groups = data;
				groupsSize = data.length;
			},
			error: function(error) {
				console.error(error);
			}
		});

		if(!groups){
			console.error('Не удалось получить список назначенных групп ролей');
			return;
		}

		for (var i = 0; i < groups.length; i++) {
			var group = groups[i];
			var row = table.insertRow(i) //TableRow
			cell = row.insertCell(0)
			cell.innerHTML = "<span title='Удалить группу' class=delete onclick=${attrHtmlId}_deselectGroupValue(\'"+ group.id +"\')>&nbsp;</span>"
			var cell = row.insertCell(1) // TableCell
			cell.innerHTML=group.name;
			cell.innerHTML +='<a href="javascript:form_collapse(\'' + group.id +
						'\')" class="noLine"><span class="arrow_up" id="ARROW_' + group.id + '">&nbsp;</span></a>'
			var iDiv = document.createElement('div');
			iDiv.id = 'BODY_' + group.id;
			iDiv.className = 'block';
			iDiv.style.margin = "10px 0px 10px 10px";
			iDiv.style.display = "none";
			groupRoles = group.roles;
			for (var k = 0; k < groupRoles.length; k++) {
				iDiv.innerHTML += '<p style="font-weight: normal; font-size:10px">' + groupRoles[k].name + '</p>';
			}
			cell.appendChild(iDiv);
		}
	}
	
	function ${attrHtmlId}_deselectRoleValue(id) {
		${attrHtmlId}_selectedRoleIds.splice(id, 1);
		${attrHtmlId}_refreshRolesControls();
	}
	

	function ${attrHtmlId}_submit(action) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = action;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
	
	function ${attrHtmlId}_copyRolesFromUser() {
		var hidden = dojo.byId('${attrHtmlId}_userCardToCopy');
		var user = dojo.byId('${attrHtmlId}_usersSelect');
		hidden.value = user.value;
		${attrHtmlId}_submit('<%= UserRolesAndGroupsAttributeEditor.COPY_USER_ROLES_CARD_ACTION %>');
	}

</script>
<input id="${attrHtmlId}_userCardToCopy" name="${attrHtmlId}_userCardToCopy" type="hidden">
<input id="${attrHtmlId}_assignedGroups" name="${attrHtmlId}_assignedGroups" type="hidden">
<input id="${attrHtmlId}_assignedRoles" name="${attrHtmlId}_assignedRoles" type="hidden">
<input id="${attrHtmlId}_excludedRoleGroups" name="${attrHtmlId}_excludedRoleGroups" type="hidden">

<div id="${attrHtmlId}_RolesAndGroupsDiv" style="margin-left: -140px">
	<table class="content" width="100%" style="margin-left: 50px">
		<colgroup>
			<col width="20%">
			<col width="80%">
	 	</colgroup>
		<tbody>
			<tr>
				<td>
					<span>				  	
						<label for="${attrHtmlId}_usersSelect">Скопировать роли от:</label>
					</span>			
				</td>
				<td>
					<select id="${attrHtmlId}_usersSelect"></select>
					<input type="button"
 						name="${attrHtmlId}_ok"
						id="${attrHtmlId}_ok"
	 					value="Ok"
						onclick="javascript:${attrHtmlId}_submit('<%= UserRolesAndGroupsAttributeEditor.COPY_USER_ROLES_CARD_ACTION %>');" disabled>
				</td>
			</tr>
			<tr>
				<td>
					<span>				  	
						<label for="${attrHtmlId}_groupsSelect">Добавить группу</label>
					</span>
				</td>
				<td>
					<select id="${attrHtmlId}_groupsSelect"></select>
					<table id="${attrHtmlId}_groupsTable" style="width: 100%; margin: 10px 0 0 10px"> 
							<colgroup>
								<col width="7%">
								<col width="93%">
	 						</colgroup>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<span>				  	
						<label for="${attrHtmlId}_rolesSelect">Добавить роль</label>
					</span>			
				</td>
				<td>
					<select id="${attrHtmlId}_rolesSelect"></select>
				</td>
			</tr>
		</tbody>
	</table>
	<div id="${attrHtmlId}_RolesTableDiv">
		<table id="${attrHtmlId}_rolesTable" style="width: 100%; margin-top:10px" class="sortable-theme-edit" data-sortable>
			<colgroup>
				<col width="5%">
				<col width="55%">
				<col width="40%">
			</colgroup>
			<thead>
				<tr>
					<th data-sortable="false"></th>
					<th id="${attrHtmlId}_rolesColumn">Роль</th> 
					<th data-sortable="false">Группа</th>
				</tr>
			</thead>
			<tbody id="${attrHtmlId}_rolesBody"></tbody>
		</table>
	</div>
</div>