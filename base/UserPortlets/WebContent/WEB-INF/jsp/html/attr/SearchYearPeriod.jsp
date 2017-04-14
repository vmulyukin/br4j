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
<%@page import="com.aplana.dbmi.model.YearPeriodAttribute"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<fmt:setBundle basename="search" scope="request"/>

<% 
	YearPeriodAttribute attr = (YearPeriodAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	
	
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="filterYears" value="<%=attr.getYears()%>" />


<fmt:message key="calendarFrom" />

<select id="${attrHtmlId}_YearFrom" name="${attrHtmlId}_YearFrom" dojoType="dijit.form.ComboBox"  value="<%=attr.getFromYear() %>" autocomplete="false" 
		onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_YearFrom', this.value);">

		<c:forEach items="${filterYears}" var="filterYear">
			<option>${filterYear}</option>
	 	</c:forEach>

</select>


<fmt:message key="calendarTo" />

<select id="${attrHtmlId}_YearTo" name="${attrHtmlId}_YearTo" dojoType="dijit.form.ComboBox"  value="<%=attr.getToYear() %>" autocomplete="false"	
		onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_YearTo', this.value);">

		<c:forEach items="${filterYears}" var="filterYear">
			<option>${filterYear}</option>
	 	</c:forEach>

</select>


<script type="text/javascript">
var ${attrHtmlId}_toolTip=null;
dojo.require("dijit.form.TextBox");
dojo.require("dijit.Tooltip");
dojo.addOnLoad(function() {
editorEventManager.registerAttributeEditor('${attrHtmlId}_YearTo', '${attrHtmlId}_YearTo', false, dojo.byId('${attrHtmlId}_YearTo').value);
editorEventManager.registerAttributeEditor('${attrHtmlId}_YearFrom', '${attrHtmlId}_YearFrom', false, dojo.byId('${attrHtmlId}_YearFrom').value);
dojo.byId('${attrHtmlId}_YearTo').value="<%=attr.getToYear() %>";
dojo.byId('${attrHtmlId}_YearFrom').value="<%=attr.getFromYear() %>";
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
