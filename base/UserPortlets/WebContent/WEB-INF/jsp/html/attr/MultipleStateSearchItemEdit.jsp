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
	
	<% MultipleStateSearchItemAttribute attr = (MultipleStateSearchItemAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); %>
	
	<c:set var="attrHtmlId"
		value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
		
	<c:set var="selectedVal"
		value="<%=MultipleStateSearchItemAttributeEditor.SELECTED_VAL%>" />
	<c:set var="unSelectedVal"
		value="<%=MultipleStateSearchItemAttributeEditor.UNSELECTED_VAL%>" />
		
		
	<c:set var="items" value="<%=attr.getValues()%>" />


	<script type="text/javascript">
/*		
		dojo.require("dijit.form.Button"); 
 
 		// were used for checked buttons 
		function onToggleButtonClick(element, hiddenElementName) {
			var _value = element.checked ? ${selectedVal} : ${unSelectedVal};
			dojo.byId(hiddenElementName).value = _value;
		}
*/
	function onLinkClick(element, hiddenElementName) {
	
		var hiddenElement = dojo.byId(hiddenElementName);
		//var textElement = dojo.byId(textElementName);

		if (hiddenElement.value == "${selectedVal}") {
			dojo.byId(hiddenElementName).value = "${unSelectedVal}";
			element.setAttribute("class", "colored_black");
			element.setAttribute("className", "colored_black"); // for IE which does not recognize "class"
		} else {
			dojo.byId(hiddenElementName).value = "${selectedVal}";
			element.setAttribute("class", "colored_red");
			element.setAttribute("className", "colored_red"); // for IE which does not recognize "class"
		}
	}
	</script>
	
	<div id="${attrHtmlId}_multiSelectBlock" name="${attrHtmlId}_multiSelectBlock" style="float: left">
		
		<c:forEach items='${items}' var='item'>
		
			<c:set var="isItemChecked" value="${item.checked}"/>
			<c:set var="attrItemId" value="${item.idStr}"/>
	 			
	 		<c:choose>
	 			<c:when test="${isItemChecked}">
					<input type="hidden" class="attrInteger"	name="${attrItemId}_value"
			 			id="${attrItemId}_value"	value="${selectedVal}"/>
<!--			 			
			 		<span class="colored_red" id="${attrItemId}_text" onClick="onLinkClick('${attrItemId}_text', '${attrItemId}_value');">	
				 			<fmt:message key="${item.name}"/>
	 				</span>
 	 				
					<button dojoType="dijit.form.ToggleButton" id="${attrItemId}_button" 
							checked iconClass="dijitCheckBoxIcon"  onClick="onToggleButtonClick(this, '${attrItemId}_value')">
						<fmt:message key="${item.name}"/>
					</button>				
-->
					<a href="#" class="colored_red" id="${attrItemId}_checkLink" style="text-decoration: none" 
						onClick="onLinkClick(this, '${attrItemId}_value'); return false;"><fmt:message key="${item.name}"/></a>
					&nbsp;&nbsp;&nbsp;
	 			</c:when>
	 			<c:otherwise>
	 			
					<input type="hidden" class="attrInteger"	name="${attrItemId}_value"
			 			id="${attrItemId}_value"	value="${unSelectedVal}"/>
<!--
			 		<span id="${attrItemId}_text" class="colored_black" onClick="onLinkClick('${attrItemId}_text', '${attrItemId}_value');">	
				 			<fmt:message key="${item.name}"/>
	 				</span>

					<button dojoType="dijit.form.ToggleButton" id="${attrItemId}_button" iconClass="dijitCheckBoxIcon" 
						onClick="onToggleButtonClick(this, '${attrItemId}_value')" >
						<fmt:message key="${item.name}"/>
					</button>
-->
 					<a href="#" class="colored_black"  style="text-decoration: none" id="${attrItemId}_checkLink" 
 						onClick="onLinkClick(this, '${attrItemId}_value'); return false;"><fmt:message key="${item.name}"/></a>
					&nbsp;&nbsp;&nbsp;
	 			</c:otherwise>
	 		</c:choose>
		</c:forEach>
	</div>
