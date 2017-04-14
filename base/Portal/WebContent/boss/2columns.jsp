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
<%@ taglib uri="/WEB-INF/lib/portal-layout.tld" prefix="p"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="java.util.ResourceBundle"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="java.util.List"%>
<%@page import="com.aplana.dbmi.action.GetDelegateListByLogin"%>
<%@page import="com.aplana.dbmi.service.SystemUser"%>
<%@page import="com.aplana.dbmi.service.DataServiceBean"%>

<%
	ResourceBundle rb = ResourceBundle.getBundle("nls.dbmi", request.getLocale());
	String title = rb.getString("title.boss");
	String copyright = rb.getString("copyright");
	String logout = rb.getString("link.logout");
%>
<fmt:setLocale value="<%= request.getLocale() %>"/>
<fmt:setBundle basename="nls.dbmi"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="<%=request.getLocale()%>"><%@ include file="./head.jspf" %>
<body id="body" class="tundra">
	<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
	<tbody>
	<tr>
		<td valign="top" class="fixed" id="MenuContaier" >

		<%  DataServiceBean service = new DataServiceBean();
	    	service.setUser(new SystemUser());
	    	service.setAddress("localhost");
	    	GetDelegateListByLogin action = new GetDelegateListByLogin();
	    	action.setLogin(request.getUserPrincipal().getName());
	    	List<String> list = (List<String>) service.doAction(action);
	    	String uname = request.getParameter(DataServiceBean.USER_NAME);
	    	String userName = request.getUserPrincipal().getName();
			if (uname != null) {
			    if (list.contains(uname)) {
	            	request.getSession().setAttribute(DataServiceBean.USER_NAME, uname);
		        } else if (userName.equals(uname)) {
		            request.getSession().removeAttribute(DataServiceBean.USER_NAME);
		        }
			}
			if (request.getParameter("logged") != null) {
	            request.getSession().removeAttribute(DataServiceBean.USER_NAME);
	        }
	    	String delegateUser = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
	    	if (delegateUser == null) {
	    	    delegateUser = userName;
	    	}
		%>
			<!-- begin блок слева -->
			<div id="menu">
				<!-- begin логотип -->
				<div id="logo"><img src="<%= request.getContextPath() %>/boss/images/gerb.png" alt="<%= title %>"></div>
				<!-- end логотип -->
				<div id="fullname"><h4><fmt:message key="boss.menu.caption" /></h4></div>
				<p:region regionName="left" regionID="regionLeft"/>
			</div>
			<!-- end блок слева -->

		</td>
		<td valign="top">
			<div id="top">
				<div class="userarea">
					<a class="username" href="/portal/auth/portal/dbmi/PersonalArea/arm/">
					<% if (request.getUserPrincipal() != null) { %>
						<%=Portal.getFactory().getUserService().getByLogin(userName).getFullName() %>
					<% } %>
					</a>
					&nbsp;
					<%if (!delegateUser.equals(userName)) { %>
	   	    			<%=rb.getString("for.the.user")%>&nbsp;
	   				<%}%>
	   				<select onchange="changeUser(this.value);">
					<option value="<%=userName%>"><%=Portal.getFactory().getUserService().getByLogin(userName).getFullName() %></option>
					<% for(String name : list) {
					    if (delegateUser.equals(name)) { %>
					        <option value="<%=name%>" selected="selected"><%=Portal.getFactory().getUserService().getByLogin(name).getFullName() %></option>
					    <% } else { %>
					        <option value="<%=name%>"><%=Portal.getFactory().getUserService().getByLogin(name).getFullName() %></option>
					    <%}
					  }%>
					</select>
					&nbsp;|&nbsp;
					<a href="/portal/auth/portal/dbmi/PersonalArea/arm/">Основной интерфейс</a>
					&nbsp;|&nbsp;
					<a href="/portal/signout" class="logout">Выход</a>	
				</div>
			</div>
			<!-- begin тело документа -->
			<div id="document_body">
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tbody>
						<tr valign="top">
							<td valign="top">
								<div style="clear:left"></div>
								<p:region regionName="main" regionID="regionMain"/>
							</td>
						</tr>
					</tbody>	
				</table>
			</div>
			<!-- end тело документа -->
		</td>
	</tr>
	</table>
<%@ include file="./columnInclude.jspf" %>
<div id="lockPane" class="lockOff"></div>
</body>
</html>