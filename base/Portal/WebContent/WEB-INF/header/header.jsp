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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.jboss.portal.api.PortalURL" %>
<%@page import="java.util.ResourceBundle"%>
<%	Object pagesBean = session.getAttribute("portalPages");
	if (pagesBean != null && !(pagesBean instanceof com.aplana.dbmi.theme.PortalPagesBean)) {
		session.removeAttribute("portalPages");
	}
%>
<jsp:useBean id="portalPages" scope="session" type="com.aplana.dbmi.theme.PortalPagesBean"/>
<c:choose>
	<c:when test="${!portalPages.dbmiPortal}">
		<jsp:include page="header_orig.jsp"/>
	</c:when>
	<c:otherwise>
		<div class="footer" id="footer" >
		<%
		ResourceBundle rb = ResourceBundle.getBundle("Resource", request.getLocale());
		PortalURL defaultPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.DEFAULT_PORTAL_URL");
		PortalURL adminPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.ADMIN_PORTAL_URL");
		if (defaultPortalURL != null) { %>
			&nbsp;&nbsp;<a href="<%= defaultPortalURL %>"><%= rb.getString("PORTAL") %></a>&nbsp;&nbsp;|
		<% }
		   if (adminPortalURL != null) { %>
			&nbsp;&nbsp;<a href="<%= adminPortalURL %>"><%= rb.getString("ADMIN") %></a>&nbsp;&nbsp;|
		<% } %>
		</div>
	</c:otherwise>
</c:choose>