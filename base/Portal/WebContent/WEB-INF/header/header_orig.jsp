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
<%@ page import="org.jboss.portal.api.PortalURL" %>
<%@ page import="java.security.Principal" %>
<%@page import="java.util.ResourceBundle"%>


<%

   ResourceBundle rb = ResourceBundle.getBundle("Resource", request.getLocale());
   Principal principal = (Principal)request.getAttribute("org.jboss.portal.header.PRINCIPAL");
   PortalURL dashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.DASHBOARD_URL");
   PortalURL loginURL = (PortalURL)request.getAttribute("org.jboss.portal.header.LOGIN_URL");
   PortalURL defaultPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.DEFAULT_PORTAL_URL");
   PortalURL adminPortalURL = (PortalURL)request.getAttribute("org.jboss.portal.header.ADMIN_PORTAL_URL");
   PortalURL editDashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.EDIT_DASHBOARD_URL");
   PortalURL copyToDashboardURL = (PortalURL)request.getAttribute("org.jboss.portal.header.COPY_TO_DASHBOARD_URL");
   PortalURL signOutURL = (PortalURL)request.getAttribute("org.jboss.portal.header.SIGN_OUT_URL");
%>

<%
   if (principal == null)
   {
%>

<%if(request.getAttribute("ssoEnabled") == null){%>
<script type="text/javascript">
    /* <![CDATA[ */
    if (typeof isModalLoaded != 'undefined'){
        document.write('<a href=\"#\" onclick=\"alertModal(\'login-modal\',\'login-modal-msg\');return false;\">Login</a>');
    }else{
        document.write('<a href=\"<%= loginURL %>\">Login</a>');
    }
   //set the iframe src for login modal to requested URL
   var iframeSrc = '<%= loginURL %>' + '?loginheight=0';
   document.getElementById('loginIframe').src = iframeSrc;
   /* ]]> */
</script>

<noscript>
      <a href="<%= loginURL %>"><%= rb.getString("LOGIN") %></a>
</noscript>
<%}else{%>
<a href="<%= loginURL %>"><%= rb.getString("LOGIN") %></a>
<%}%>


<%
}
else
{
%>
<script type="text/javascript">
   /* <![CDATA[ */
   //we don't need the iframe/modal if logged in
   document.getElementById('loginIframe').src = '';
   /* ]]> */
</script>
<%= rb.getString("LOGGED") %>: <%= principal.getName() %><br/><br/>


<%
   if (dashboardURL != null)
   {
%>&nbsp;&nbsp;<a href="<%= dashboardURL %>"><%= rb.getString("DASHBOARD") %></a>&nbsp;&nbsp;|<%
   }

   if (defaultPortalURL != null)
   {
%>&nbsp;&nbsp;<a href="<%= defaultPortalURL %>"><%= rb.getString("PORTAL") %></a>&nbsp;&nbsp;|<%
   }

   if (adminPortalURL != null)
   {
%>&nbsp;&nbsp;<a href="<%= adminPortalURL %>"><%= rb.getString("ADMIN") %></a>&nbsp;&nbsp;|<%
   }

   if (editDashboardURL != null)
   {
%>&nbsp;&nbsp;<a href="<%= editDashboardURL %>"><%= rb.getString("CONFIGURE_DASHBOARD") %></a>&nbsp;&nbsp;|<%
   }

   if (copyToDashboardURL != null)
   {
%>&nbsp;&nbsp;<a href="<%= copyToDashboardURL %>"><%= rb.getString("COPY_TO_DASHBOARD") %></a>&nbsp;&nbsp;|<%
   }
%>&nbsp;&nbsp;<a href="<%= signOutURL %>"><%= rb.getString("MENU_LOGOUT") %></a>
<%
   }
%>
