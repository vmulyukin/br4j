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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 

<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.PortletMode"%>
<%@page import="com.aplana.dbmi.login.LoginPortletSessionBean"%>
<%@page import="com.aplana.dbmi.login.LoginPortlet"%>
<portlet:defineObjects/>

<fmt:setLocale value="<%= request.getLocale() %>"/> 
<fmt:setBundle basename="com.aplana.dbmi.login.nl.LoginPortletResource"/>

<%
	PortletURL backURL = renderResponse.createActionURL();
	backURL.setPortletMode(PortletMode.VIEW); 			
 %> 	
<div>
<a HRef="<%= backURL.toString() %>"><span><fmt:message key="help.form.back.link" /></span></a>
</div>

<fmt:message key="help.form.timeout.msg" />
<br/>
<br/>
<fmt:message key="help.form.str_0.msg" />
<br/>
<fmt:message key="help.form.str_1.msg" />
<br/>
<fmt:message key="help.form.str_2.msg" />
<br/>
<fmt:message key="help.form.str_3.msg" />
<br/>
<fmt:message key="help.form.str_4.msg" />
<br/>
<br/>
<fmt:message key="help.form.str_5.msg" />
