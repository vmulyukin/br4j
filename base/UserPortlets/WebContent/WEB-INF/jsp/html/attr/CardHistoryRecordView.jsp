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
<%@page import="com.aplana.dbmi.model.CardHistoryAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardHistoryRecordAttributeViewer"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects />

<%	CardHistoryAttribute attr = (CardHistoryAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	CardPortletCardInfo cardInfo = CardPortlet.getSessionBean(renderRequest).getActiveCardInfo();
	LinkedHashMap headTable = (LinkedHashMap)cardInfo.getAttributeEditorData(attr.getId(), CardHistoryRecordAttributeViewer.KEY_HEAD_TABLE);
	List/*HashMap (name -> value)*/ table = (List)cardInfo.getAttributeEditorData(attr.getId(), CardHistoryRecordAttributeViewer.KEY_CONTENT_TABLE);
	Map/*name -> type*/ types = (Map)cardInfo.getAttributeEditorData(attr.getId(), CardHistoryRecordAttributeViewer.KEY_TYPES);
	Boolean showTime = (Boolean) cardInfo.getAttributeEditorData(attr.getId(), CardHistoryRecordAttributeViewer.KEY_SHOW_TIME);
%>

<table class="res CardHistoryRecordAttribute">
<%	for (int i=0; i < headTable.keySet().size(); i++){
%>
	<col class="<%= i %>">
<%
	}
%>
  <tr>
<%	Iterator names = headTable.keySet().iterator();
	while (names.hasNext()) {
%>
    <th><%=(String)headTable.get(names.next())%></th>
<%
	}
%>
  </tr>
<%	Iterator rows = table.iterator();
	while (rows.hasNext()) {
%>
  <tr>
<%
		names = headTable.keySet().iterator();
		Map row = (Map)rows.next();
		while (names.hasNext()) {
			String name = (String)names.next();
			Object value = row.get(name);
			String type = (String) types.get(name);
			String text = CardHistoryRecordAttributeViewer.formatValue(type, value, showTime);
%>
    <td><%= text %></td>
<%		}
%>
  </tr>
<%	} %>
</table>

