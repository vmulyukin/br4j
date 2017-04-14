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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.linksportlet.*" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects/>

<%
	LinksPortletSessionBean sessionBean = (LinksPortletSessionBean)renderRequest.getPortletSession().getAttribute(LinksPortlet.SESSION_BEAN);
%>

<style type="text/css">
div.horisontal {display: inline;}
</style>
<div class=bottomMenu>
<%
	List linkList = sessionBean.getLinkList();
	for (Iterator linkIter = linkList.iterator(); linkIter.hasNext();) {
		AbstractLinkListItem linkListItem = (AbstractLinkListItem) linkIter.next();
 %>
<%= 
	linkListItem.renderToHTML(renderRequest, renderResponse) %>
<%
	}
 %>
</div>