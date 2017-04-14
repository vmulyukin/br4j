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
<%@page import="com.aplana.dbmi.model.MultiListAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.MultiSelectListSearchEditor"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
MultiListAttribute attr = (MultiListAttribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
String allValues = (String)request.getAttribute("allValuesList");
%>
<%@page import="com.aplana.dbmi.card.MultiSelectAttributeEditor"%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

<script type="text/javascript" language="javascript">
	dojo.require('dijit.dijit');
	dojo.require("dijit.form.FilteringSelect");
	dojo.require("dojo.data.ItemFileReadStore");
	
	/* Формат:
	** {
	**		id1: 'value1',
	**		id2: 'value2',
	**		...
	** }
	*/
	var ${attrHtmlId}_mapValues = <%=allValues%>;
	
	/* Формат
	** [id1, id2,...]
	**/
	var ${attrHtmlId}_selectedIds = <%=MultiSelectListSearchEditor.getSelectedValuesJSON(attr).toString()%>;;
	
	var ${attrHtmlId}_readStore = null
	dojo.addOnLoad(function() {
		readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapValues, ${attrHtmlId}_selectedIds)}
		);
		
		var ${attrHtmlId}_select = new dijit.form.FilteringSelect(
			{
				store: ${attrHtmlId}_readStore,
				style: 'width: 100%;',
				required: false,
				autoComplete: false,
				onBlur: function () {${attrHtmlId}_clearTextInSelect()},
				onChange: function(id) {${attrHtmlId}_selectValue(id)}
			},
			dojo.byId('${attrHtmlId}_select')
		);
		${attrHtmlId}_refreshControls();
	});

	function ${attrHtmlId}_keysByValue(obj){
	    var A=[];
	    for(var p in obj){
		if(obj.hasOwnProperty(p)) A.push([p, obj[p]]);
	    }
	    A.sort(function(a, b){
		var a1= a[1], b1= b[1];
		return a1>b1;
	    });
	    /*for(var i= 0, L= A.length; i<L; i++){
		A[i]= A[i][1];
	    }*/
	    return A;
	}

	${attrHtmlId}_mapValues = ${attrHtmlId}_keysByValue(${attrHtmlId}_mapValues);

	function ${attrHtmlId}_getStore(mapValues, selectedIds) {
		var notAvailableIds = {}
		for (var i = 0; i < ${attrHtmlId}_selectedIds.length; i++) {
			notAvailableIds[${attrHtmlId}_selectedIds[i]] = ''
		}
	
		var store = {}
		store.identifier = 'id';
		store.label = 'name';
		store.items = [];
		for (var i = 0; i < ${attrHtmlId}_mapValues.length; i++) {
			var id = ${attrHtmlId}_mapValues[i][0];
			if (notAvailableIds[id] == undefined) {
				var item = {};
				item[store.identifier] = id;
				item[store.label] = ${attrHtmlId}_mapValues[i][1];
				store.items[store.items.length] = item;
			}
		}

		return store;
	}
	function ${attrHtmlId}_selectValue(id) {
		if (id == '')
			return
		${attrHtmlId}_selectedIds.push(id)
		${attrHtmlId}_refreshControls()
	}
	function ${attrHtmlId}_deselectValue(id) {
		for (var i = 0; i < ${attrHtmlId}_selectedIds.length; i++) {
			if (${attrHtmlId}_selectedIds[i] == id) {
				${attrHtmlId}_selectedIds.splice(i, 1)
				break
			}
		}
		${attrHtmlId}_refreshControls()
	}
	function ${attrHtmlId}_refreshControls() {
		${attrHtmlId}_refreshTable()
		${attrHtmlId}_refreshSelect()
		${attrHtmlId}_refreshHidden()
	}
	function ${attrHtmlId}_refreshSelect() {
		var select = dijit.byId('${attrHtmlId}_select');
		select.setValue('')
		
		${attrHtmlId}_readStore = new dojo.data.ItemFileReadStore(
			{data: ${attrHtmlId}_getStore(${attrHtmlId}_mapValues, ${attrHtmlId}_selectedIds)}
		)
		select.store = ${attrHtmlId}_readStore
	}
	function ${attrHtmlId}_refreshTable() {
		var table = dojo.byId('${attrHtmlId}_table') // Table
		for (var i = table.rows.length - 1; i >= 0; --i) {
			table.deleteRow(i);
		}
		for (var i = 0; i < ${attrHtmlId}_selectedIds.length; i++) {
			var row = table.insertRow(i) //TableRow 
			var cell = row.insertCell(0) // TableCell
			// get a value by key in array[array[key][value]]
			for (var j = 0; j < ${attrHtmlId}_mapValues.length; j++) {
				// get map value by selected id
				if (${attrHtmlId}_mapValues[j][0] == ${attrHtmlId}_selectedIds[i])
					cell.innerHTML=${attrHtmlId}_mapValues[j][1];
			}
			cell = row.insertCell(1)
			cell.innerHTML = '<span class="delete" onclick="${attrHtmlId}_deselectValue('+${attrHtmlId}_selectedIds[i]+')">&nbsp;</span>'
		}
	}
	function ${attrHtmlId}_refreshHidden() {
		var hidden = dojo.byId('${attrHtmlId}_values')
		hidden.value = ${attrHtmlId}_selectedIds.join()
	}
	function ${attrHtmlId}_clearTextInSelect() {
		var select = dijit.byId('${attrHtmlId}_select');
		select.setValue('')
	}
</script>
<input id="${attrHtmlId}_values" name="${attrHtmlId}_values" type="hidden">
<select id="${attrHtmlId}_select"></select>
<table id="${attrHtmlId}_table">
	<col width="145px"/>
</table>