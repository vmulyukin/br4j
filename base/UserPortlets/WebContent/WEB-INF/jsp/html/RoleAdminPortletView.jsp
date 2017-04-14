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
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>

<%@page import="com.aplana.dbmi.common.utils.portlet.PortletMessage"%>
<%@page import="com.aplana.dbmi.roleadmin.RoleAdminPortlet"%>
<%@page import="com.aplana.dbmi.roleadmin.RoleAdminPortletSessionBean"%>
<%@page import="com.aplana.web.tag.util.StringUtils"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.SystemGroup"%>
<%@page import="com.aplana.dbmi.model.SystemRole"%>
<%@page import="com.aplana.dbmi.action.Search"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>

<jsp:include page="CardPageFunctions.jsp"/>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.roleadmin.nl.RoleAdminPortletResource" scope="request"/>

<portlet:actionURL var="backUrl">
	<portlet:param name="<%=RoleAdminPortlet.ACTION_FIELD%>" value="<%=RoleAdminPortlet.BACK_ACTION%>"></portlet:param>
</portlet:actionURL>


<%
	ObjectId NAME_ATTR_ID = ObjectId.predefined(StringAttribute.class, "name");
	RoleAdminPortletSessionBean sessionBean = (RoleAdminPortletSessionBean)renderRequest.getPortletSession().getAttribute(RoleAdminPortlet.SESSION_BEAN);

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
		message = renderRequest.getParameter(RoleAdminPortlet.MSG_PARAM_NAME);
		messageStyle = PortletMessage.STYLE_INFO;
	}
	
	List<SystemGroup> allGroups = (List<SystemGroup>)sessionBean.getServiceBean(renderRequest).listAll(SystemGroup.class);
	List<SystemGroup> roleGroups = new ArrayList<SystemGroup>();
	if (null != sessionBean.getSystemRole()) {
		roleGroups = sessionBean.getSystemRole().getRoleGroups();
	}
	
	List<Person> roleUsers = new ArrayList<Person>();
	List<String> userCardNames = new ArrayList<String>();

	roleUsers = (List<Person>)sessionBean.getServiceBean(renderRequest).listChildren(sessionBean.getSystemRole().getId(), Person.class);
	
	StringBuffer cardIds = new StringBuffer();
	Iterator<Person> iterator = roleUsers.listIterator();
	while(iterator.hasNext()){
		Person person = iterator.next();
		if(person.getCardId() == null){
			continue;
		}
		ObjectId cardId = person.getCardId();
		cardIds.append(cardId.getId().toString());
		if(iterator.hasNext()){
			cardIds.append(", ");
		}
	}
	Search search = new Search();
	search.setByAttributes(false);
	search.setByCode(true);
	search.setWords(cardIds.toString());
	final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
	final SearchResult.Column col = new SearchResult.Column();
	col.setAttributeId(NAME_ATTR_ID);
	columns.add(col);
	search.setColumns(columns);
	SearchResult searchResult = sessionBean.getServiceBean(renderRequest).doAction(search);
	for (Card user : searchResult.getCards()) {
		if(user.getAttributeById(NAME_ATTR_ID) != null){
			userCardNames.add(user.getAttributeById(NAME_ATTR_ID).getStringValue());
		}
	}

	Collections.sort(userCardNames);

 %>
 
<script type="text/javascript" language="javascript">
	<fmt:message key="roleadmin.role.block.main" var="mainBlockTitle"/>
	<fmt:message key="roleadmin.role.block.groups" var="groupsBlockTitle"/>
	<fmt:message key="roleadmin.role.block.users" var="usersBlockTitle"/>
	<fmt:message key="roleadmin.role.block.rules" var="rulesBlockTitle"/>

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
		document.<%= RoleAdminPortlet.VIEW_FORM_NAME %>.<%= RoleAdminPortlet.ACTION_FIELD %>.value = action;
		document.<%= RoleAdminPortlet.VIEW_FORM_NAME %>.submit();
	}
	
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>
 
 <form name="<%= RoleAdminPortlet.VIEW_FORM_NAME %>" method="post" action="<portlet:actionURL/>">
  	<input type="hidden" name="<%= RoleAdminPortlet.ACTION_FIELD %>" value="">
 	
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
	            <jsp:include page="RoleViewCardButtonPane.jsp"/>
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
		
<%		SystemRole role = sessionBean.getSystemRole();
		final String roleName 
			= (role != null) 
				? role.getName()
				: "";
		final String headStr = (roleName != null) 
				? roleName
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
			<col Width="100%" />
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
										<label for='<%=RoleAdminPortlet.ROLE_CODE_FIELD%>'><fmt:message key="roleadmin.role.code"></fmt:message></label>
									</span>
								</td>
								<td>
								 	 <%=role.getId().getId()%>						
								</td>
							</tr>
							<tr>
								<td>
									<span>
										<label for='<%=RoleAdminPortlet.ROLE_RUS_NAME_FIELD%>'><fmt:message key="roleadmin.role.name_ru"></fmt:message></label>
									</span>
								</td>
								<td>
									<%=(role.getNameRu() != null) ? role.getNameRu() : ""%>
								</td>
							</tr>
							<tr>
								<td>
									<span>
										<label for=<%=RoleAdminPortlet.ROLE_EN_NAME_FIELD%>><fmt:message key="roleadmin.role.name_en"></fmt:message></label>
									</span>
								</td>
								<td>
									<%=(role.getNameEn() != null) ? role.getNameEn() : ""%>
								</td>
							</tr>
						</table>
					</div>
					
					<dbmi:blockHeader id="groups" title="${groupsBlockTitle}"
							displayed="true" savestate="false"/>
						<div class="divPadding" id="BODY_BLOCK_groups">
						  <table class="content" width="100%">
							<col Width="20%"/>
							<col Width="80%"/>
							<tr>
								<td>
									<span>
										<label for=<%=RoleAdminPortlet.ROLE_GROUPS_TABLE_FIELD%>><fmt:message key="roleadmin.role.groups"></fmt:message></label>
									</span>
								</td>
								<td>
									<table id=<%=RoleAdminPortlet.ROLE_GROUPS_TABLE_FIELD%> style="width: 100%; margin: 10px 0 0 10px"> 
										<% for (SystemGroup group : roleGroups) { %>
											<tr>
												<td></td>
												<td>
													<%=group.getName()%>
												</td>
											</tr>
										<%} %>
									</table>
								</td>
							</tr>
						</table>
					</div>
					<dbmi:blockHeader id="users" title="${usersBlockTitle}"
						displayed="false" savestate="false"/>
					<div class="divPadding" id="BODY_BLOCK_users" style="height: auto; display: none;">
						<table class="content" width="100%">
							<col Width="20%"/>
							<col Width="80%"/>
							<tr>
								<td>
									<span>
										<label for=<%=RoleAdminPortlet.ROLE_USERS_TABLE%>><fmt:message key="roleadmin.role.users"></fmt:message></label>
									</span>
								</td>
								<td>
									<table id=<%=RoleAdminPortlet.ROLE_USERS_TABLE%> style="width: 100%; margin: 10px 0 0 10px">
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
					<dbmi:blockHeader id="rules" title="${rulesBlockTitle}"
						displayed="false" savestate="false"/>
					<div class="divPadding" id="BODY_BLOCK_rules" style="height: auto; display: none;">
						<table class="content" width="100%">
							<col Width="20%"/>
							<col Width="80%"/>
							<tr>
								<td>
									<span>
										<label for=<%=RoleAdminPortlet.ROLE_RULES_TABLE%>><fmt:message key="roleadmin.role.rules"></fmt:message></label>
									</span>
								</td>
								<td>
									<table id=<%=RoleAdminPortlet.ROLE_RULES_TABLE%> style="width: 100%; margin: 10px 0 0 10px" cellpadding="5">
										 <tr>
										   <th><fmt:message key="roleadmin.rule.header.template"/></th>
										   <th><fmt:message key="roleadmin.rule.header.type"/></th>
										   <th><fmt:message key="roleadmin.rule.header.name"/></th>
										   <th><fmt:message key="roleadmin.rule.header.target"/></th>
										   <th><fmt:message key="roleadmin.rule.header.profile"/></th>
										   <th><fmt:message key="roleadmin.rule.header.link"/></th>
										   <th><fmt:message key="roleadmin.rule.header.intermed"/></th>
										 </tr>
										<c:forEach items="<%=sessionBean.getRoleRulesInfo()%>" var="row">
											<tr>
												<td>${row.template}</td>
												<td>${row.rule_type}</td>
												<td>${row.rname}</td>
												<td>${row.target}</td>
												<td>${row.profile}</td>
												<td>${row.link}</td>
												<td>${row.intermid}</td>
											</tr>
										</c:forEach>
									</table>
								</td>
							</tr>
						</table>
					</div>

			  </td>
			</tr>

		   </table>
		  </td>	
		</tr>	
  </table>
 </div>
</form>