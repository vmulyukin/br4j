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
<%@page import="com.aplana.dbmi.ajax.HierarchicalCardListAttributeParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>
<%@page import="com.aplana.dbmi.card.graph.Graph"%>
<%@page import="javax.portlet.PortletURL"%>
<%@ page import="com.aplana.dbmi.actionhandler.ActionsManager" %>
<%@ page import="com.aplana.dbmi.card.*" %>
<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<% 
	Attribute attr = (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	HierarchyDescriptor desc = HierarchicalCardLinkAttributeViewer.getHierarchyDescriptor(attr.getId(), renderRequest);
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);

%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>
<c:set var="namespace"><portlet:namespace/></c:set>
<c:url var="hierarchyServletURL" value="/servlet/CardHierarchyServlet">
	<c:param name="<%= CardHierarchyServlet.PARAM_CALLER%>" value="<%=HierarchicalCardListAttributeParameters.CALLER %>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_ATTR_CODE%>" value="<%=(String)attr.getId().getId()%>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_NAMESPACE%>" value="${namespace}"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_DESCRIPTOR_KEY%>" value="<%=HierarchicalCardLinkAttributeViewer.HIERARCHY_VIEW_DESCRIPTOR%>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_HIERARCHY_KEY%>" value="<%=HierarchicalCardLinkAttributeViewer.HIERARCHY_VIEW %>"/>
	<c:param name="<%= HierarchicalCardListAttributeParameters.PARAM_STORED_CARDS_KEY%>" value="<%=HierarchicalCardLinkAttributeViewer.HIERARCHY_VIEW_STORED_CARDS%>"/>	
</c:url>

<div name="Java_attr2"></div>
<%
	Boolean isViewGraph = (Boolean)sessionBean.getAttributeEditorData(attr.getId(), HierarchicalCardLinkAttributeViewer.GRAPH_IS_VIEW);
	if (isViewGraph != null && isViewGraph.booleanValue()) {
		Graph graph = (Graph)sessionBean.getAttributeEditorData(attr.getId(), HierarchicalCardLinkAttributeViewer.GRAPH_DATA);
			
		PortletURL link = renderResponse.createActionURL();
		link.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
		link.setParameter(CardPortlet.ATTR_ID_FIELD, (String)attr.getId().getId());
		String prefixId = JspAttributeEditor.getAttrHtmlId(attr);
		String jsonTree = graph.getJSONTree(link, CardPortlet.CARD_ID_FIELD, prefixId).toString();
%>
	<c:set var="htmlAttrId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

	<button dojoType="dijit.form.Button" onclick="dijit.byId('dialog_${htmlAttrId}').show()">Графическое представление</button>
	<div class="graph" dojoType="dijit.Dialog" id="dialog_${htmlAttrId}" title="Графическое представление"
    	 style="width: 925px; height: 422px">
    	<div id="viewGraph_${htmlAttrId}" class="viewGraph"></div>
	</div>
<script language="JavaScript" type="text/javascript">
	dojo.require('dijit.Dialog');
 	dojo.require('dijit.form.Button');
	
	dojo.addOnLoad(function(){
		var json = <%=jsonTree%>;
		initTreeHierarchicalCardList('viewGraph_${htmlAttrId}', json);
	});
</script>
<%
	}
%>		
<script language="JavaScript" type="text/javascript">
	dojo.addOnLoad(function(){	
		var tree = new dbmiCustom.HierarchicalCardList(
			{
				readOnly: <%= desc.isReadonly() %>,
				selectAllLabel: '<fmt:message key="edit.page.hierarchylist.selectall"/>',
				url : '${hierarchyServletURL}',
				columnsDescriptor: <%= desc.getColumnsJSON(HierarchyDescriptor.COLUMNS_MAIN).toString() %>,
				hasInfoBlocks: <%= desc.hasInfoBlocks() %>,
				actions: ${requestScope.actionsManager.actionsJSON},
				onAction: function(actionId, selectedItems, customInfo) {
					submitAttributeActionsManagerAction('<%=attr.getId().getId()%>', actionId);
				},
				onClickActions: {
					'open': function(cardId) {
						submitOpenLinkedCard('<%=attr.getId().getId()%>', cardId);
					},
					'download': function(cardId) {
						downloadCardMaterial(cardId);
					}
				}
			},
			dojo.byId('${attrHtmlId}')
		);
	});
</script>
<div id="${attrHtmlId}"></div>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>