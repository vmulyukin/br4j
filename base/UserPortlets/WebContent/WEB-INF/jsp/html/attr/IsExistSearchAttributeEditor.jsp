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
<%@page import="com.aplana.dbmi.model.IsExistSearchAttribute"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	IsExistSearchAttribute attr = (IsExistSearchAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String extraJavascript =  (String) request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
%>
<c:set var="style">
	<c:choose>
		<c:when test="${requestScope.isVerticalAlignment}">margin-bottom: 3px;</c:when>
		<c:otherwise>float: left; margin-right: 10px;</c:otherwise>
	</c:choose>
</c:set>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>
<c:forEach items="${requestScope.valuesList}" var="item">
	<div style="${style}" id="${attrHtmlId}_${item.id.id}_div">
		<input id="${attrHtmlId}_${item.id.id}"	type="radio" name="${attrHtmlId}" value="${item.id.id}">
			${item.value}
		</input>
	</div>
</c:forEach>
<c:set var="attrValue" value="<%= attr.getStringValue() %>"/>
<script type="text/javascript" language="javascript">
	var ${attrHtmlId}_radioButtons = [];
	var ${attrHtmlId}_flagValue = <%=attr.getIsExistFlag() != null ? attr.getIsExistFlag().getId().getId() : null%>;

	dojo.addOnLoad(function() {
		var btn = null;

	<c:forEach items="${requestScope.valuesList}" var="item" varStatus="itemStatus">
		btn = new dijit.form.RadioButton(
			{
				name: '${attrHtmlId}',
				value: '${item.id.id}',
				checked:  ${item.id.id} == ${attrHtmlId}_flagValue 
			},
			'${attrHtmlId}_${item.id.id}'
		);
		${attrHtmlId}_radioButtons[${itemStatus.index}] = btn;
		dojo.connect(dojo.byId(btn.id), 'onclick', function() {${attrHtmlId}_updateView(); editorEventManager.notifyValueChanged('<%=JspAttributeEditor.getAttrHtmlId(attr)%>', this.value);});
	</c:forEach>
	${attrHtmlId}_updateView();
	});
	
	function ${attrHtmlId}_updateView() {
		for (var k=0; k < ${attrHtmlId}_radioButtons.length; k++) {
			var btn = ${attrHtmlId}_radioButtons[k];
			if (btn.checked) {
				dojo.byId(btn.id+'_div').style.fontWeight = 'bold';
			} else {
				dojo.byId(btn.id+'_div').style.fontWeight = 'normal';
			}
		}
	}
</script>

<script type="text/javascript">
dojo.addOnLoad(function() {
	var ${attrHtmlId}_value = null;
	for(var i in ${attrHtmlId}_radioButtons) {
		var btn = ${attrHtmlId}_radioButtons[i];
		if (btn.checked) {
			${attrHtmlId}_value = btn.value;
		}
	}
editorEventManager.registerAttributeEditor('<%=JspAttributeEditor.getAttrHtmlId(attr)%>', '${attrHtmlId}', false, ${attrHtmlId}_value);
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
