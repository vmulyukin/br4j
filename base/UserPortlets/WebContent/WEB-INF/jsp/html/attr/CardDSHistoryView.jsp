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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="java.util.List"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.net.URLEncoder"%>

<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardHistoryAttributeViewer"%>
<%@page import="com.aplana.dbmi.model.CardHistoryRecord"%>

<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardHistoryResource"/>
<portlet:defineObjects/>
<style type="text/css">
	table.cardHistory{
		margin-top: 0;
	}

	table.headCardHistory{
		margin-bottom: 0;
	}

	table.cardHistory thead{
		display: none;
	}
</style>
<% 
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	Attribute attr = (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	List recs = (List)sessionBean.getActiveCardInfo().getAttributeEditorData(attr.getId(), CardHistoryAttributeViewer.CARD_HISTORY_LIST);
	if (recs == null || recs.isEmpty())
		return;
	request.setAttribute("recs", recs);
%>
	<table class="res headCardHistory">
		<thead>
			<tr>
				<th style="width: 15%;"><fmt:message key="history.table.date"/></th>
				<th style="width: 19%;"><fmt:message key="history.table.user"/></th>
				<th style="width: 24%;"><fmt:message key="history.table.description"/></th>
				<th style="width: 42%;"><fmt:message key="history.table.exinf"/></th>
			</tr>
		</thead>
	</table>
	<div style="overflow: auto; max-height: 35em;">
		<display:table name="recs" id="rec" class="res cardHistory">
<%
			CardHistoryRecord record = (CardHistoryRecord) pageContext.getAttribute("rec");
			final String dateValue = sdf.format(record.getDate());
%>
			<display:column headerClass="h_header" style="width: 15%;"><%= dateValue %></display:column>
			<display:column headerClass="h_header" style="width: 20%;" property="actorFullName"/>
			<display:column headerClass="h_header" style="width: 20%;">
<%
				String versionId = record.getVersionId(); 
				if (versionId == null || "".equals(versionId)) {
%>
				<%= record.getActionName() %>
<%
				} else {
					StringBuilder url = new StringBuilder(request.getContextPath() + "/servlet/JasperReportServlet?");
					url.append("nameConfig=reportChartCardChanges");
					url.append("&cardId=L_" + sessionBean.getActiveCard().getId().getId());
					url.append("&versionId=L_" + versionId);
					url.append("&actor=S_" + URLEncoder.encode(record.getActorFullName(), "UTF-8"));
					url.append("&date=S_" + URLEncoder.encode(dateValue, "UTF-8"));
					url.append("&action=S_" + URLEncoder.encode(record.getActionName(), "UTF-8"));
					url.append("&attrExist=S_" + URLEncoder.encode(record.getVisibleAttr(), "UTF-8"));
%>
					<a href="<%=url.toString()%>" target="_blank"><%=record.getActionName()%></a>
<%
				} 
%>
			</display:column>
	  <display:column headerClass="h_header" style="width: 25%;" property="comment"/>
	  <display:column headerClass="h_header" style="width: 20%;" property="delegateUserFullName"/>
	</display:table>
	</div>
