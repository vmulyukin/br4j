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
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>
<%@page import="com.aplana.dbmi.ajax.HierarchicalCardListAttributeParameters"%>
<%@ page import="com.aplana.dbmi.card.*" %>
<%@ page import="com.aplana.dbmi.actionhandler.ActionsManager" %>
<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" />

<% 
	Attribute attr = (Attribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	HierarchyDescriptor desc = HierarchicalCardLinkAttributeEditor.getHierarchyDescriptor(attr.getId(), renderRequest);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
    ActionsManager am = (ActionsManager)request.getAttribute(ActionsSupportingAttributeEditor.ACTIONS_MANAGER_KEY);
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>
<c:set var="namespace"><portlet:namespace/></c:set>
<c:url var="hierarchyServletURL" value="/servlet/CardHierarchyServlet">
	<c:param name="<%= CardHierarchyServlet.PARAM_CALLER%>" value="<%=HierarchicalCardListAttributeParameters.CALLER %>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_ATTR_CODE%>" value="<%=(String)attr.getId().getId()%>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_NAMESPACE%>" value="${namespace}"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_DESCRIPTOR_KEY%>" value="<%=HierarchicalCardLinkAttributeEditor.HIERARCHY_EDIT_DESCRIPTOR%>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_HIERARCHY_KEY%>" value="<%=HierarchicalCardLinkAttributeEditor.HIERARCHY_EDIT%>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_STORED_CARDS_KEY%>" value="<%=HierarchicalCardLinkAttributeEditor.HIERARCHY_EDIT_STORED_CARDS%>"/>	
</c:url>

<script language="JavaScript" type="text/javascript">

	dojo.addOnLoad(function(){	
		var widget = new dbmiCustom.HierarchicalCardList(
			{
				jsId: '${attrHtmlId}',
				readOnly: false,
				url : '${hierarchyServletURL}',
				selectAllLabel: '<fmt:message key="edit.page.hierarchylist.selectall"/>',
				columnsDescriptor: <%= desc.getColumnsJSON(HierarchyDescriptor.COLUMNS_MAIN).toString() %>,
				hasInfoBlocks: <%= desc.hasInfoBlocks() %>,
			<%--actions: ${requestScope.actionsManager.actionsJSON},--%>
            	actions: <%=am.getActionsJSON(attr)%>,
				onAction: function(actionId, selectedItems, customInfo) {
					submitAttributeActionsManagerAction('<%=attr.getId().getId()%>', actionId);
				},
				onClickActions: {
					'open': function(cardId) {
						submitOpenLinkedCard('<%=attr.getId().getId()%>', cardId);
					},
					'download': downloadCardMaterial
				},
				onChange: function() {editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', this.value);}
			},
			dojo.byId('${attrHtmlId}')
		);
	});

</script>
<div id="${attrHtmlId}"></div>

<script type="text/javascript">
dojo.addOnLoad(function() {
editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>', '<%=JspAttributeEditor.getAttrHtmlId(attr)%>', false, dijit.byId('${attrHtmlId}').value);
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
