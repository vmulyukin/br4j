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
<%@page import="com.aplana.dbmi.Portal"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@page import="com.aplana.dbmi.PortletService"%>
<portlet:defineObjects/>
<P>Добро пожаловать, <%= request.getUserPrincipal().getName() %>!
<br/>Подождите несколько мгновений, Вы будете перенаправлены на главную страницу.</P>
<%
	PortletService svc = Portal.getFactory().getPortletService();
	String defPage = svc.getPageProperty("defaultPage", renderRequest, renderResponse);
	if (defPage == null || defPage.length() == 0)
		defPage = "dbmi.defaultPage";
	String link = svc.generateLink(defPage, null, null, renderRequest, renderResponse);
%>
<script type="text/javascript">
window.location='<%= link %>';
</script>
<p>Если этого не произошло, <a href="<%= link %>">нажимте на эту ссылку</a></p>
