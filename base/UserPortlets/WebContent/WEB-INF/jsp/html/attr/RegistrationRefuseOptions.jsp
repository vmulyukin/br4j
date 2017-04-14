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
<%@page import="java.util.Collection"%>
<%@page import="java.lang.String"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.RegistrationRefuseAttributeEditor"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONObject"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<% 
	StringAttribute attr = (StringAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	Boolean isEditable = (Boolean) request.getAttribute(RegistrationRefuseAttributeEditor.PARAM_IS_EDITABLE);
%>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<select class="attrString" id="${attrHtmlId}">
	<%	
		final JSONArray optionsData = new JSONArray();
		Collection<String> options = (Collection<String>) request.getAttribute(RegistrationRefuseAttributeEditor.PARAM_OPTIONS);
		if(options != null) {
			for(String s : options){
				JSONObject jo = new JSONObject();
				jo.put("name", s);
				optionsData.put(jo);
	%>
				<option><%=s%></option>
			<%}} %>
</select>

<script type="text/javascript">
	var ${attrHtmlId}_select = null;
	var ${attrHtmlId}_optionValues = <%=optionsData.toString()%>;
	dojo.addOnLoad(function() {
		var value = '<%= attr.getValue() == null ? "" : attr.getValue().trim().replaceAll("&", "&amp;").replaceAll("\"", "&quot;") %>';
	 <% if (isEditable) { %>
			dojo.require("dijit.form.ComboBox");
			${attrHtmlId}_select = new dijit.form.ComboBox(
				{
					style: 'width: 100%;',
					name: '${attrHtmlId}',
					required: false,
					autoComplete: false,
					pageSize: 15,
					onChange: function() {editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', this.value);}
				},
				dojo.byId('${attrHtmlId}')
			);
	 <% }else { %>
			dojo.require("dbmiCustom.CustomFilteringSelect");
			dojo.require("dojo.data.ItemFileReadStore");
			
			var store = {};
			store.identifier = 'name';
			store.label = 'name';
			store.items = [];
			for (var i = 0; i < ${attrHtmlId}_optionValues.length; i++) {
				var item = {};
				item[store.identifier] = store.identifier;
				item[store.label] = ${attrHtmlId}_optionValues[i].name;
				store.items[store.items.length] = item;
			}
			${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
				{data: store}
			);
			${attrHtmlId}_select = new dbmiCustom.CustomFilteringSelect(
				{
					style: 'width: 100%;',
					name: '${attrHtmlId}',
					required: false,
					autoComplete: false,
					store: ${attrHtmlId}_readStore,
					searchAttr: 'name',
					pageSize: 15,
					onValueChanged: function() {editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', this.value);}
				},
				dojo.byId('${attrHtmlId}')
			);

			var found = false;
			if (value != "") {
				for (var i = 0; i < ${attrHtmlId}_optionValues.length; i++) {
					if (value == ${attrHtmlId}_optionValues[i].name) {
						found = true;
						break;
					}
				}
				if (!found)
					value = "";
			}
	<% } %>

	editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>', '${attrHtmlId}', false, dojo.byId('${attrHtmlId}').value);
	${attrHtmlId}_select.attr('value', value );
	});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
