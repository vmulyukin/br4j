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
	<%@page import="com.aplana.dbmi.model.IntegerAttribute"%>
	<%@page import="com.aplana.dbmi.model.MultipleStateSearchItemAttribute"%>
	<%@page import="com.aplana.dbmi.card.MultipleStateSearchItemAttributeEditor"%>
	<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
	<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
	<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
	
	<%
		MultipleStateSearchItemAttribute attr = (MultipleStateSearchItemAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
		String jsonData = MultipleStateSearchItemAttributeEditor.getJsonData(request, attr);
	%>
	
	<c:set var="data" value="<%=jsonData%>" />
	
	<c:set var="attrHtmlId"
		value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
		
	<c:set var="selectedVal"
		value="<%=MultipleStateSearchItemAttributeEditor.SELECTED_VAL%>" />
	<c:set var="unSelectedVal"
		value="<%=MultipleStateSearchItemAttributeEditor.UNSELECTED_VAL%>" />
		
	<c:set var="showSelectAll" value="<%=attr.isShowSelectAll()%>"/>
	

	<div id="${attrHtmlId}_multiSelectBlock" name="${attrHtmlId}_multiSelectBlock" style="float: left"></div>
	<div id="${attrHtmlId}_selectAll_container" style="float: right;"></div>
	
	<script type="text/javascript">
		dojo.require("dijit.form.Button"); 
		dojo.require("dijit.form.CheckBox");
		
		dojo.addOnLoad(function() {
			${attrHtmlId}_build();
		});
			
 
 		function ${attrHtmlId}_build() {
			var dataStr = '${data}';
			if(dataStr == null || dataStr == '') {
				console.error("empty data");
				return;
			}
			var data = JSON.parse(dataStr);
			var values = data.values;
			var container = dojo.byId('${attrHtmlId}_multiSelectBlock');
			var checkBoxes = [];
			var allChecked = true;
			
			for(var i=0; i<values.length; i++) {
				
				var id = values[i].id;
				var name = values[i].name;
				var label = values[i].label;
				var checked = values[i].checked;
				if(!checked)
					allChecked = false;
				var value = checked ? '${selectedVal}' : '${unSelectedVal}';
				var inputValueId = id+'_value';
				var cb = dojo.create('span', 
		                {
		            		innerHTML: '<span id="'+id+'_widget"></span><input type="hidden" class="attrInteger" name="'+inputValueId+'" id="'+inputValueId+'" value="'+value+'"/><label style="margin: 0 5px 0 0">'+label+'</label>'	
		                }
		            );
				
				container.appendChild(cb);
				
				var widget = new dijit.form.CheckBox({
	                id: name+'_id',
					name: name,
	                value: true,
	                checked: checked,
	                input: dojo.byId(inputValueId),
	                onChange: function() {
	                	this.input.value = (this.checked) ? '${selectedVal}' : '${unSelectedVal}';
	                	if(!this.checked) {
	                		var w = dijit.byId('${attrHtmlId}_selectAll_widget')
	                		if(w)
	                			w.setChecked(false);
	                	}
	                }   
	            },dojo.byId(id+'_widget'));
				checkBoxes.push(widget);
				
			}
			var appendCheckAll = '${showSelectAll}';
			if(appendCheckAll == 'true')
				${attrHtmlId}_appendSelectAllCheckBox(checkBoxes, allChecked);
		}
		
		function ${attrHtmlId}_appendSelectAllCheckBox(checkBoxes, checked) {
			
			var container = dojo.byId('${attrHtmlId}_selectAll_container');
			
	   		var id = '${attrHtmlId}_selectAll';
	        var cb = dojo.create('div', 
	             {
	          		innerHTML: '<span id="'+id+'"></span><label style="margin: 0 5px 0 0">Выбрать все/очистить</label>'
	             }
	         );				
			container.appendChild(cb);
				
	        var widget = new dijit.form.CheckBox({
	          	id: id+'_widget',
	            name: id+'_name',
	            value: true,
	            checked: checked,
	            onClick: function() {
	            	${attrHtmlId}_checkAll(checkBoxes, this.checked);
	            }   
	        });
	    
	        widget.placeAt(dojo.byId(id));
	   		
		}
		
		function ${attrHtmlId}_checkAll(checkBoxes, check) {
			checkBoxes.forEach(function(checkBox){
				checkBox.setChecked(check);
				checkBox.input.value = check ? '${selectedVal}' : '${unSelectedVal}';
	   		});
		}
	</script>