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
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.TextAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@page import="com.aplana.dbmi.card.XSLTReportAttributeEditor"%>
<portlet:defineObjects />

<%  TextAttribute attr = (TextAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	int rows = attr.getRowsNumber();
	if (rows < 1) rows = 5;
	CardPortletCardInfo info = CardPortlet.getSessionBean(renderRequest).getActiveCardInfo();
	String text = (String)info.getAttributeEditorData(attr.getId(), XSLTReportAttributeEditor.KEY_EDITOR_DATA);
	if (text == null)
		text = "";
%>
<textarea class="attrText"
 name="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"
 id="<%= JspAttributeEditor.getAttrHtmlId(attr) %>"
 rows="<%= rows %>"
 onChange="javascript: editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', this.value);"
><%= StringEscapeUtils.escapeHtml(text.toString()) %></textarea>
<%= info.getAttributeEditorData(attr.getId(), XSLTReportAttributeEditor.KEY_XSLT_RESULT) %>

<script type="text/javascript">
dojo.addOnLoad(function() {
editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>', '<%= JspAttributeEditor.getAttrHtmlId(attr) %>', false, dojo.byId('<%= JspAttributeEditor.getAttrHtmlId(attr) %>').value);
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
