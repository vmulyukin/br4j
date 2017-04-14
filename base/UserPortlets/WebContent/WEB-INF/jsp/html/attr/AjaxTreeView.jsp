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
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Collection"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.TreeAttribute"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue" %>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.AjaxTreeAttributeEditor"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<%
TreeAttribute attr = (TreeAttribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);

boolean fRest = attr.isRestrictedList();
ReferenceValue anotherRefVal = null;
Collection values = attr.getValues();
if (values != null) {
	Iterator iterValues = values.iterator();
	while (iterValues.hasNext()) {
		ReferenceValue value = (ReferenceValue)iterValues.next();
		if (value.getId().equals(ReferenceValue.ID_ANOTHER)) {
			anotherRefVal = value;
			break;
		}
	}
}
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<div dojoType="TreeAttributeCheckboxTree" id="${attrHtmlId}_tree"
	model="${attrHtmlId}_treeModel" showRoot="false" openOnClick="false">
</div>
<%
if (!fRest) {
%>
<input onclick="update_view_input(dojo.byId('${attrHtmlId}_new_check'),dojo.byId('${attrHtmlId}_new_value') )" class="AnotherCheckbox" dojoType="dijit.form.CheckBox" id="${attrHtmlId}_new_check" name="${attrHtmlId}_new_check" <% if (anotherRefVal != null) {%>checked<%}%>/>
<input class="AnotherInput" id="${attrHtmlId}_new_value" name="${attrHtmlId}_new_value" value="<% if (anotherRefVal != null) {%><%=anotherRefVal.getValueRu()%>"<%} else {%>" disabled<%}%>/>
<%
}
%>
<input type="hidden" id="${attrHtmlId}_values" name="${attrHtmlId}_values" value=""/>

<script type="text/javascript">
	var ${attrHtmlId}_treeModel = null;
		var treeStore = new dojo.data.ItemFileWriteStore({url : "<%=request.getContextPath()+"/servlet/TreeServlet?idRef="+(String)attr.getReference().getId()%>"});
		${attrHtmlId}_treeModel = new TreeAttributeCheckboxForestStoreModel({
			store: treeStore,
			childrenAttrs: ["children"],
			query: {type: 'topNodes'},
			viewMode: true
		});
		${attrHtmlId}_treeModel.attrHtmlId = '${attrHtmlId}';
		var values = <%=AjaxTreeAttributeEditor.getJSONValues(attr)%>;
		setTreeFromValues(treeStore, values);
		refreshInputFromTree(dojo.byId('${attrHtmlId}_values'), treeStore);
</script>

