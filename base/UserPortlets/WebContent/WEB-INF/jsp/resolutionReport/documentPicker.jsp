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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.model.Attribute"%> 
<%@page import="java.util.List"%>
<%@page import="com.aplana.dbmi.model.TypedCardLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.DocumentPickerAttributeEditor"%>
<%@page	import="com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>  
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
 
<jsp:include page="documentPickerInclude.jsp"/>
 
<portlet:defineObjects />   

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkPickerEditResource" /> 
  
<%
	Attribute attr =  (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String attrCode = (String)attr.getId().getId();
	boolean typed = attr.getType().equals(Attribute.TYPE_TYPED_CARD_LINK);	
%> 

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="filterYears" value="${filterYears}" />	 
<c:set var="filterTemplates" value="${filterTemplates}" />
	

<input type="hidden" id="${attrHtmlId}_values"
	name="${attrHtmlId}_values" value="" />
<script language="javascript" type="text/javascript">

	var ${attrHtmlId}_activeVariant = null; 
	var ${attrHtmlId}_variants = ${requestScope.variants};
	var ${attrHtmlId}_select = null;
	<%if (typed) {%>var ${attrHtmlId}_types = null;<%}%>
 
  
  
	dojo.addOnLoad(function() {	 
		dojo.connect(dojo.byId('<portlet:namespace/>_AddLinkBtn'), "onclick", 
				function() {		
					cardLinkPickerDisplayDialog('${attrHtmlId}', '<%=attrCode%>', '<%=attr.getName()%>', 
						<%= attr.isMultiValued()%>, <%=typed%>); 
				});
		dojo.connect(dojo.byId('<portlet:namespace/>_RemoveLinkBtn'), "onclick", 
				function() {		
					documentPickerRemoveCards('${attrHtmlId}', '<%=attrCode%>', <%=typed%>); 
					<portlet:namespace/>_removePreparedDocs();
				});

		dojo.connect(dojo.byId('<portlet:namespace/>_CreateDocBtn'), "onclick", 
				function() {		
					createNewDoc();
				});
		
		${attrHtmlId}_typeTitle = 'typeTitle';
		
		documentPickerSwitchVariant('${attrHtmlId}', '${requestScope.activeVariant}', '<%=attrCode%>', <%=typed%>);
		
		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
		
		${attrHtmlId}_types = <%=DocumentPickerAttributeEditor.getJSONMapTypesCardLink((TypedCardLinkAttribute)attr)%>;
 
		var filterYearCombo = dojo.byId('${attrHtmlId}_Year');
		filterYearCombo.setAttribute('value','');

		
		//display selected related cards 
		cardLinkPickerSetSelectedDocuments('${attrHtmlId}', 'ADMIN_702357', [${requestScope.selectedValues}]
		                                                      			<% if (typed) {%>, true, ${attrHtmlId}_types<%}%>);
		
		
	
	});
 
	<%if (typed) {%>
	var typesCLink = null;
	                
	var xhrArgs = {	
		url: "<%=request.getContextPath()%>/servlet/TreeServlet?idRef=JBR_REF_TYPECLINK",
        handleAs: "json",
        sync: true,
        load: function(data) {
       		typesCLink = data.items;
        },
        error: function(error) {
       		typesCLink = "";
        }
    }

	dojo.xhrGet(xhrArgs);
	
	${attrHtmlId}_selType = null;
	function ${attrHtmlId}_cardLinkPickerRefreshValues() {
		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
		for (var i = 0; i < ${attrHtmlId}_selType.length; i++) {
			if (${attrHtmlId}_selType[i].options[${attrHtmlId}_selType[i].selectedIndex].value == "") {
				variant.types[${attrHtmlId}_selType[i].cardId] = null;
			} else {
				variant.types[${attrHtmlId}_selType[i].cardId] = ${attrHtmlId}_selType[i].options[${attrHtmlId}_selType[i].selectedIndex].value;
			}
		}
		dojo.byId('${attrHtmlId}_values').value = cardLinkPickerStringTypes(variant.types);
	}
	
<%}%>
	


</script>

<table class="res" id="${attrHtmlId}_table"

	<c:if test="${showTitle == false}">	 
		noHead="true"
	</c:if>
	
	<c:if test="${showEmpty == false}">	 
		noEmpty="true"
	</c:if>
	
	 style="width: 100%; margin-top: 0px;">
	 
</table>   
<%
	int currentYear = Integer.parseInt(new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()));
%>

<c:set var="currentYear" value="<%= currentYear %>" />

<div dojoType="dijit.Dialog" id="${attrHtmlId}_dialog" title="<%= attr.getName() %>" style="font-size: 70%; text-align: left;">
			
	<div id="${attrHtmlId}_filter">
		<div>
			<label for="${attrHtmlId}_Year"  style="width : 100px; float:left;"><fmt:message key="filter.year" /></label>
			
	    	<select id="${attrHtmlId}_Year" dojoType="dijit.form.FilteringSelect" autocomplete="false">

		        <c:forEach items="${filterYears}" var="filterYear">
		        	<c:choose>
                    	<c:when test="${filterYear == currentYear}">
                    		<option value="${filterYear}" selected>${filterYear}</option>
                    	</c:when>
                    	<c:otherwise>
                    		<option value="${filterYear}">${filterYear}</option>
                    	</c:otherwise>
                    </c:choose>
	    		</c:forEach>
  
 
	    	</select>		
					  
		</div>
		<div>
			<label for="${attrHtmlId}_DocType" style="width : 100px; float:left;"><fmt:message key="filter.documentType" /></label>
			<select id="${attrHtmlId}_DocType" dojoType="dijit.form.FilteringSelect" autocomplete="false">

				<c:forEach items="${filterTemplates}" var="filterTemplate" varStatus="rowCounter">
					<c:choose>
						<c:when test="${rowCounter.first}">
							<option value="${filterTemplate.id.id}" selected>${filterTemplate.name}</option>
						</c:when>
						<c:otherwise>
							<option value="${filterTemplate.id.id}">${filterTemplate.name}</option>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</select>

					
		</div>
		
		<div >
			<label for="${attrHtmlId}_filterLine" style="width : 100px; float:left;"><fmt:message key="filter.document" /></label>
			<input id="${attrHtmlId}_filterLine" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
		</div>				
		<div >
			<button id="${attrHtmlId}_filterButton" dojoType="dijit.form.Button" type="button"><fmt:message key="filter.button" /></button>
		</div>
	</div> 
    <div id="${attrHtmlId}_loading" align="center">
    	<img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt="" />
    </div>
    <div id="${attrHtmlId}_hierarchy" valign="bottom"></div>
</div>