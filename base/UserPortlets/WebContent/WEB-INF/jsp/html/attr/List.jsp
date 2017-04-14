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
<%@page import="com.aplana.dbmi.model.ListAttribute"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.ListAttributeEditor"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>
<%
	Attribute attr = (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
%>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />

<script language="javascript" type="text/javascript">
	var items = ${requestScope.valuesList};
	var itemsObj = eval(items);
	var ${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore({ data: items });
	var ${attrHtmlId}_select = null;
	dojo.addOnLoad(function() {
		var listSize = 15;
		itemsObj.items.forEach(function(item){ //incrementing list size by adding on invisible values quantity
			if(!item.visible)
				listSize++;
		});
		${attrHtmlId}_select = new dijit.form.FilteringSelect(
			{
				store: ${attrHtmlId}_readStore,
				query: { ${requestScope.query} },
				style: 'width: 100%;',
				name: '${attrHtmlId}',
				pageSize: listSize,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				onClick:  function() {searchListOnClick('${attrHtmlId}');},
				onBlur:   function() {searchListOnBlur('${attrHtmlId}');},
				onChange: function() {editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', this.value);}
			},
			dojo.byId('${attrHtmlId}_select')
		);		

		editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>', '${attrHtmlId}_select', false, dijit.byId('${attrHtmlId}_select').value);
		${attrHtmlId}_select.attr('value', '${requestScope.selectedValueId}');
	});
</script>

<select id="${attrHtmlId}_select"></select>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
