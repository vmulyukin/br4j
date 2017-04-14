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
%>
<fmt:setLocale value="<%= request.getLocale() %>"/> 
<fmt:setBundle basename="nls.dbmi"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="<%=request.getLocale()%>"><%@ include file="./head.jspf" %>
<body id="body" class="tundra" >
	<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
	<tbody>
	<tr>
		<td valign="top" class="fixed" id="MenuContaier">
		<script>
		var showBottomFrame = true;
		var bottomFrameVisible = true;
		if(document.body.clientWidth > 1024){
			document.getElementById("MenuContaier").className = "fixed"
		}
		</script>
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
				<h1 class="caption">
					<span class="header_icon on_execution_personal"></span> <fmt:message key="title.extended.search" />
				</h1>
				<div id="searchResults" class="Expanded">
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tbody>
						<tr valign="top">
							<td valign="top">
								<div style="clear:left"></div>
								<p:region regionName="main" regionID="regionMain"/>
							</td>
						</tr>
						<tr id="doc_details">
							<td valign="top">
								<div class="content_lr_border">
                               		<div id="DocumentDetails">&nbsp;</div>
                            	</div>
							</td>
						</tr>
					</tbody>	
					</table>
				</div>
				<div class="content_lr_border">
					<div class="extended_search_delimiter_bg" onClick="switchExtendedSearchRegions('delimiterArrow');">
						<div class="extended_search_delimiter_arrow_up" id="delimiterArrow"/>
					</div>
				</div>
				<div id="searchParams" class="Collapsed">
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tbody>
						<tr valign="top">
							<td valign="top">
								<div style="clear:left"></div>
								<p:region regionName="bottom" regionID="regionBottom"/>
							</td>
						</tr>
					</tbody>	
					</table>
				</div>
				
			</div>
			<!-- end тело документа -->
		</td>
	</tr>
	</table>

<script>

	 dojo.addOnLoad(function(){
		var q = dojo.query("#menu .child LI.current");
		if(q && q.length >0) {
			q[0].parentNode.parentNode.className = "current";
		}
		
		
		if (!showBottomFrame) {
			q = dojo.query("#doc_details");
			dojo.style(q[0], {"display":"none"});
			q = dojo.query("#document_body .table");
			dojo.style(q[0], {"height":"100%"});
		}
		
		folderLinks = dojo.query("#menu ul li a");
		var navigationLinks = dojo.query(".toolbar_top a");
		
		if(folderLinks && folderLinks.length > 0){
			folderLinks.forEach(function(a){dojo.attr(a, 'href',a.href + "&sortColumnId=default")});
		}
		
		if(navigationLinks && navigationLinks.length > 0) {	
			var currSortCode = getCookie('CURRSORTCODE');
		    if(currSortCode && currSortCode.length > 0) {
			    var sortParams = "&SORT_ATTR_CODE=" + currSortCode;
		    	var currSortOrder = getCookie('CURRSORTORDER');
		    	if(currSortOrder && currSortOrder.length > 0) {
		    		sortParams += "&SORT_ASC=" + currSortOrder;
		    	}
		    	navigationLinks.forEach(function(a){dojo.attr(a, 'href',a.href + sortParams)});
			}
		}
				
	 })
</script>
<%@ include file="./columnInclude.jspf" %>
<div id="lockPane" class="lockOff"></div>
</body>
</html>