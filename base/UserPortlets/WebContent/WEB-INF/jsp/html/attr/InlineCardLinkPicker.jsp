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
<%@page session="false" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardLinkPickerAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.util.AttrUtils"%>
<%@page	import="com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.card.CardAttributeEditorParameterHelper"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.PersonListResource" />
<%
	CardLinkAttribute attr =  (CardLinkAttribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String attrCodeType = AttrUtils.getAttrTypeString(attr.getClass()) + ":" + (String)attr.getId().getId();
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:url var="storeUrl" value="/servlet/SearchCardServlet">
	<c:param name="<%= CardLinkPickerSearchParameters.PARAM_ATTR_CODE%>" value="<%=(String)attr.getId().getId()%>"/>
	<c:param name="<%= CardLinkPickerSearchParameters.PARAM_ATTR_TYPE_CODE%>" value="<%=attrCodeType%>"/>
	<c:param name="<%= CardLinkPickerSearchParameters.PARAM_NAMESPACE %>" value="<%=renderResponse.getNamespace()%>"/>
	<c:param name="<%= SearchCardServlet.PARAM_CALLER %>" value="<%=CardAttributeEditorParameterHelper.getCallerField(renderRequest)%>"/>
	<c:param name="<%= CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS %>" value="${requestScope.activeVariant}"/>
</c:url>
<script language="javascript" type="text/javascript">
	var ${attrHtmlId}_select = null;
	dojo.addOnLoad(function() {
		var dataStore = new dojox.data.QueryReadStore({
			url : '${storeUrl}'
		});
		
		${attrHtmlId}_select = new dijit.form.FilteringSelect(
			{
				style: 'width: 100%;',
				store: dataStore,
				searchAttr: '<%=CardLinkPickerAttributeEditor.FIELD_LABEL%>',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				name: '${attrHtmlId}_values',
				onBlur: function() {
					if(this.isValid() && this.item != null && this.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
						this.attr('value', this.value);
					} else this.attr('value', '');
				},
				onChange: function() {
					var value = this.attr('value');
					if ('<%= SearchCardServlet.EMPTY_CARD_ID %>' === value) {
						value = '';
					} 
					editorEventManager.notifyValueChanged('<%=(String)attr.getId().getId()%>', value);
				}
			},
			dojo.byId('${attrHtmlId}_select')
		);
		var variants = ${requestScope.variants};		
		var variant = variants['${requestScope.activeVariant}'];
		if (variant.dependencies) {
			for(var i = 0; i < variant.dependencies.length; ++i) {
				var valueAttrCode = variant.dependencies[i];
				editorEventManager.subscribe('<%=(String)attr.getId().getId()%>', valueAttrCode, 'cardLinkPickerOnSubscribedAttrChanged', i);
			}
		}
		${attrHtmlId}_select.attr('value', '<%= attr.isEmpty() ? String.valueOf(SearchCardServlet.EMPTY_CARD_ID) : ((ObjectId)attr.getIdsLinked().iterator().next()).getId().toString()%>');
		//подгружаем в представление атрибута все параметры типа paramN (на случай, если атрибуты, от которых зависит текущий, на форме не представлены редакторами и => изменены не могут)
		var widget = eval('${attrHtmlId}_select');
		for(var param in variant.query) {
			var paramValue = variant.query[param];
			eval('widget.query.' + param + ' = \'' + paramValue + '\'');
		}

		editorEventManager.registerAttributeEditor('<%=(String)attr.getId().getId()%>', '${attrHtmlId}', true, dijit.byId('${attrHtmlId}_select').value);
	});
</script>
<select id="${attrHtmlId}_select"></select>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
