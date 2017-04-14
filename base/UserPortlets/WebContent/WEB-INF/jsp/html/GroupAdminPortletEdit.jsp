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

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>

<%@page import="com.aplana.dbmi.common.utils.portlet.PortletMessage"%>
<%@page import="com.aplana.dbmi.groupadmin.GroupAdminPortlet"%>
<%@page import="com.aplana.dbmi.groupadmin.GroupAdminPortletSessionBean"%>
<%@page import="com.aplana.web.tag.util.StringUtils"%>

<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.SystemGroup"%>
<%@page import="com.aplana.dbmi.model.SystemRole"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONObject"%>

<jsp:include page="CardPageFunctions.jsp"/>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.groupadmin.nl.GroupAdminPortletResource" scope="request"/>

<portlet:actionURL var="backUrl">
	<portlet:param name="<%=GroupAdminPortlet.ACTION_FIELD%>" value="<%=GroupAdminPortlet.BACK_ACTION%>"></portlet:param>
</portlet:actionURL>


<%
	GroupAdminPortletSessionBean sessionBean = (GroupAdminPortletSessionBean)renderRequest.getPortletSession().getAttribute(GroupAdminPortlet.SESSION_BEAN);

	//////////////////
	PortletMessage cardContainer = sessionBean.getPortletMessage();
	String message = null;
	String messageStyle = null;
	if (cardContainer != null){ 
		message = cardContainer.getMessage();
		messageStyle = cardContainer.getMessageStyle();
		if (message == null)
			message = "";
		sessionBean.setPortletMessage(null);
	} else {
		message = renderRequest.getParameter(GroupAdminPortlet.MSG_PARAM_NAME);
		messageStyle = PortletMessage.STYLE_INFO;
	}
	
	Collection<SystemRole> allRoles = (List<SystemRole>)sessionBean.getServiceBean(renderRequest).listAll(SystemRole.class);
	Collection<SystemRole> groupRoles = new ArrayList<SystemRole>();
	if (null != sessionBean.getSystemGroup()) {
		groupRoles = sessionBean.getSystemGroup().getSystemRoles();
	}
	
	List<Person> groupUsers = new ArrayList<Person>();
	List<String> userCardNames = new ArrayList<String>();
	if (sessionBean.isEditMode()) {
		groupUsers = (List<Person>)sessionBean.getServiceBean(renderRequest).listChildren(sessionBean.getSystemGroup().getId(), Person.class);
		
		for (Person user : groupUsers) {
			ObjectId cardId = user.getCardId();
			StringAttribute cardName = null;
			if (cardId != null) {
				Card card = (Card)sessionBean.getServiceBean(renderRequest).getById(cardId);
				cardName = card.getAttributeById(ObjectId.predefined(StringAttribute.class, "name"));
			}
			if (cardName != null) {
				userCardNames.add(cardName.getValue());
			}
		}
	}
	Collections.sort(userCardNames);

	final JSONArray allRolesData = new JSONArray();
	for (SystemRole role : allRoles){
		JSONObject jo = new JSONObject();
		jo.put("id", role.getId().getId());
		jo.put("name", role.getName());
		allRolesData.put(jo);
	}

	final JSONArray groupRolesData = new JSONArray();
	for (SystemRole role : groupRoles){
		JSONObject jo = new JSONObject();
		jo.put("id", role.getId().getId());
		jo.put("name", role.getName());
		groupRolesData.put(jo);
	}
 %>
 
<script type="text/javascript" language="javascript">
	dojo.require("dijit.dijit");
	dojo.require("dijit.form.FilteringSelect");
	dojo.require("dojo.data.ItemFileReadStore");

	<fmt:message key="groupadmin.group.block.main" var="mainBlockTitle"/>
	<fmt:message key="groupadmin.group.block.roles" var="rolesBlockTitle"/>
	<fmt:message key="groupadmin.group.block.users" var="usersBlockTitle"/>

	function f5press(e) {
		//запрещаем нажатие F5 и Ctrl+r 
		if(e.keyCode == 116 || (e.keyCode == 82 && e.ctrlKey)) {
			return false;
		}
	}

	window.onkeydown = function (event) { 
		return f5press(event);
	}
	
	function submitForm(action, elem) { 
		if (elem) elem.onClick = function() { return false };
		document.<%= GroupAdminPortlet.EDIT_FORM_NAME %>.<%= GroupAdminPortlet.ACTION_FIELD %>.value = action;
		document.<%= GroupAdminPortlet.EDIT_FORM_NAME %>.submit();
	}
	
	var allRoleValues = <%=allRolesData%>;
	var groupRolesValues = <%=groupRolesData%>;
	
	var readStore = null;
	
	dojo.addOnLoad(function() {
		readStore = new dojo.data.ItemFileReadStore(
			{data: getStore(allRoleValues, groupRolesValues)}
		);
		
		var groupsSelect = new dijit.form.FilteringSelect(
			{
				store: readStore,
				style: 'width: 80%;',
				required: false,
				autoComplete: false,
				pageSize: 15,
				onChange: function(id) {selectRoleValue(id)}
			},
			dojo.byId('<%=GroupAdminPortlet.GROUP_ROLES_SELECT_FIELD%>')
		);
		refreshRolesControls();
	});
	
	function getStore(allValues, selectedValues) {
		var notAvailableIds = {};
		if (selectedValues != null) {
			for (var i = 0; i < selectedValues.length; i++) {
				notAvailableIds[selectedValues[i].id] = ''
			}
		}
	
		var store = {}
		store.identifier = 'id';
		store.label = 'name';
		store.items = [];
		for (var i = 0; i < allValues.length; i++) {
			var id = allValues[i].id;
			if (notAvailableIds[id] == undefined) {
				var item = {};
				item[store.identifier] = id;
				item[store.label] = allValues[i].name;
				store.items[store.items.length] = item;
			}
		}

		return store;
	}
	
	function refreshRolesTable() {

		var table = dojo.byId('<%=GroupAdminPortlet.GROUP_ROLES_TABLE_FIELD%>'); // Table
		for (var i = table.rows.length - 1; i >= 0; --i) {
			table.deleteRow(i);
		}

		for (var i = 0; i < groupRolesValues.length; i++) {
			var role = groupRolesValues[i];
			var row = table.insertRow(i) //TableRow
			cell = row.insertCell(0)
			cell.innerHTML = "<span title='Удалить роль' class=delete onclick=deselectRoleValue(\'"+ role.id +"\')>&nbsp;</span>"
			var cell = row.insertCell(1) // TableCell
			cell.innerHTML=role.name;
		}
	}
	
	function refreshRolesControls() {
		refreshRolesSelect();
		refreshRolesHidden();
		refreshRolesTable();
	}
	
	function refreshRolesSelect() {
		var select = dijit.byId('<%=GroupAdminPortlet.GROUP_ROLES_SELECT_FIELD%>');
		select.setValue('');
		
		readStore = new dojo.data.ItemFileReadStore(
			{data: getStore(allRoleValues, groupRolesValues)}
		);
		select.store = readStore;
	}
	function refreshRolesHidden() {
		var hidden = dojo.byId('<%= GroupAdminPortlet.SELECTED_ROLES_FIELD %>');
		var selectedRoleIds = [];
		for (var i = 0; i < groupRolesValues.length; i++) {
			selectedRoleIds.push(groupRolesValues[i].id)
		}
		hidden.value = selectedRoleIds.join();
	}

	function selectRoleValue(id) {
		if (id == '')
			return;
		for (var i = 0; i < allRoleValues.length; i++) {
			if (allRoleValues[i].id == id) {
				groupRolesValues.push(allRoleValues[i]);
				break;
			}
		}
		refreshRolesControls();
	}

	function deselectRoleValue(id) {
		if (id == '')
			return;
		for (var i = 0; i < groupRolesValues.length; i++) {
			if (groupRolesValues[i].id == id) {
				groupRolesValues.splice(i, 1);
				break;
			}
		}
		refreshRolesControls();
	}
	
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>


 
 <form name="<%= GroupAdminPortlet.EDIT_FORM_NAME %>" method="post" action="<portlet:actionURL/>"> 
 	<input type="hidden" name="<%= GroupAdminPortlet.ACTION_FIELD %>" value="">
 	<input  type="hidden" id="<%= GroupAdminPortlet.SELECTED_ROLES_FIELD %>" name="<%= GroupAdminPortlet.SELECTED_ROLES_FIELD %>">
 	
 	<div id="fixedCardHeader">
<% if (message != null) {%>

	<table class="<%= messageStyle %>">
		<tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
		<tr class="tr2"><td class="td_21"/><td class="td_22">
		<%= StringUtils.replaceNewlineWithBreak(message) %></td><td class="td_23"/>
		</tr>
	    <tr class="tr3"><td class="td_31"/><td class="td_32"/><td class="td_33"/></tr>
	</table>
<%} %>
	<table class="indexCardMain">
		<col Width="50%" />        
        <col Width="50%" />
        <tr>
	      <!--Заголовок-->
	      <td>
	      </td>
	      <td>
	          <div id="rightIcons" style="width: 100%; margin:0;" >
	            <jsp:include page="GroupEditCardButtonPane.jsp"/>
	          </div>
	      </td>    
	    </tr>
	    <tr>
	      <!--Разделитель-->
	      <td colspan="4">
	        <hr/>
	      </td>
	    </tr>
   </table>
</div>
<script type="text/javascript" language="javascript">
	function initCardContainerHeight() {
		var windowHeight = document.documentElement.clientHeight == 0 ? document.body.clientHeight : document.documentElement.clientHeight;
		dojo.byId("fixedCardContainer").style.height = (windowHeight - (110 + dojo.byId("fixedCardHeader").clientHeight)) +"px"
	}

	dojo.connect(document,"DOMContentLoaded",function(){
		initCardContainerHeight();
	});
	
	dojo.connect(window,"resize",function(){
		initCardContainerHeight();
	});
</script>
<div id="fixedCardContainer">
  <table class="indexCardMain">
    <col Width="50%" />
    <col Width="50%" />
    
	<!--  Заголовок: название роли -->
	<tr>
		
<%		SystemGroup group = sessionBean.getSystemGroup();
		final String groupName 
			= (group != null) 
				? group.getName()
				: "";
		final String headStr = (groupName != null) 
				? groupName
				: "";
%>
			<td colspan="4">
				<div class="icHeader">
					<%= headStr %>
				</div>
			</td>
		</tr>
		<tr class="cardContent"><!--Контент-->    
		  <td class="cont" colspan="2">
		   <table width="100%">
			<col Width="50%" />
			<col Width="50%"/>
			<tr>
			  <td 
			  <%if (sessionBean.isNewMode()) { %>colspan="2"<%} %>
			  >
					<dbmi:blockHeader id="main" title="${mainBlockTitle}"
							displayed="true" savestate="false"/>
						<div class="divPadding" id="BODY_BLOCK_main">
						  <table class="content" width="100%">
							<col Width="20%"/>
							<col Width="80%"/>
							<tr>
								<td>
									<span>
										<label for='<%=GroupAdminPortlet.GROUP_CODE_FIELD%>'><fmt:message key="groupadmin.group.code"></fmt:message></label>
									</span>
								</td>
								<td>
								  <% if (sessionBean.isEditMode()) { %>
								 	 <%=group.getId().getId()%>
									
								  <%} else if (sessionBean.isNewMode()) { %>
									<input type="text" class="string"
											name='<%=GroupAdminPortlet.GROUP_CODE_FIELD%>'
											id='<%=GroupAdminPortlet.GROUP_CODE_FIELD%>'
											value='<%=(group != null && group.getGroupCode() != null) ? group.getGroupCode() : ""%>' />
								  <%} %>
								</td>
							</tr>
							<tr>
								<td>
									<span>
										<label for='<%=GroupAdminPortlet.GROUP_RUS_NAME_FIELD%>'><fmt:message key="groupadmin.group.name_ru"></fmt:message></label>
									</span>
								</td>
								<td>
									<input type="text" class="string"
											name='<%=GroupAdminPortlet.GROUP_RUS_NAME_FIELD%>'
											id='<%=GroupAdminPortlet.GROUP_RUS_NAME_FIELD%>'
											value='<%=(group != null && group.getNameRu() != null) ? group.getNameRu() : ""%>' />
								</td>
							</tr>
							<tr>
								<td>
									<span>
										<label for=<%=GroupAdminPortlet.GROUP_EN_NAME_FIELD%>><fmt:message key="groupadmin.group.name_en"></fmt:message></label>
									</span>
								</td>
								<td>
									<input type="text" class="string"
											name='<%=GroupAdminPortlet.GROUP_EN_NAME_FIELD%>'
											id='<%=GroupAdminPortlet.GROUP_EN_NAME_FIELD%>'
											value='<%=(group != null && group.getNameEn() != null) ? group.getNameEn() : ""%>' />
								</td>
							</tr>
						</table>
					</div>
					
					<dbmi:blockHeader id="roles" title="${rolesBlockTitle}"
							displayed="true" savestate="false"/>
						<div class="divPadding" id="BODY_BLOCK_roles">
						  <table class="content" width="100%">
							<col Width="20%"/>
							<col Width="80%"/>
							<tr>
								<td>
									<span>
										<label for=<%=GroupAdminPortlet.GROUP_ROLES_SELECT_FIELD%>><fmt:message key="groupadmin.group.roles"></fmt:message></label>
									</span>
								</td>
								<td>
									<select id=<%=GroupAdminPortlet.GROUP_ROLES_SELECT_FIELD%>></select>
									<table id=<%=GroupAdminPortlet.GROUP_ROLES_TABLE_FIELD%> style="width: 100%; margin: 10px 0 0 10px"> 
										<colgroup>
											<col width="7%">
											<col width="93%">
										</colgroup>
									</table>
								</td>
							</tr>
						</table>
					</div>
					<% if (sessionBean.isEditMode()) { %>
						<dbmi:blockHeader id="users" title="${usersBlockTitle}"
							displayed="false" savestate="false"/>
						<div class="divPadding" id="BODY_BLOCK_users" style="height: auto; display: none;">
							<table class="content" width="100%">
								<col Width="20%"/>
								<col Width="80%"/>
								<tr>
									<td>
										<span>
											<label for=<%=GroupAdminPortlet.GROUP_USERS_TABLE%>><fmt:message key="groupadmin.group.users"></fmt:message></label>
										</span>
									</td>
									<td>
										<table id=<%=GroupAdminPortlet.GROUP_USERS_TABLE%> style="width: 100%; margin: 10px 0 0 10px">
											<% for (String cardName : userCardNames) { %>
												<tr>
													<td></td>
													<td>
														<%=cardName%>
														<br></br>
													</td>
												</tr>
											<%} %>
											
										</table>
									</td>
								</tr>
							</table>
						</div>
					<% } %>
			  </td>
			</tr>
		   </table>
		  </td>	
		</tr>	
  </table>
 </div>
</form>