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
<%@page import="com.aplana.dbmi.model.Attribute"%> 
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Calendar"%>
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

<portlet:defineObjects />

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkPickerEditResource" /> 

<%
	Attribute attr =  (Attribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String attrCode = (String)attr.getId().getId();
	CardPortletCardInfo info = CardPortlet.getSessionBean(renderRequest).getActiveCardInfo();
	boolean typed = attr.getType().equals(Attribute.TYPE_TYPED_CARD_LINK);
	List<String> visibleInputs = (List<String>) info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_INPUT_IDS);
	String visibleReferenceValues = (String) info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_REFERENCE_LIST_IDS);
	Map<Long, String> referenceValuesIdsFoTemplates = (Map<Long, String>) info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_REFERENCE_LIST_FOR_LINK_TEMPLATES);
%>

<c:set var="attrHtmlId" 
	value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="showTitle"
	value="<%=(Boolean)info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_SHOW_TITLE)%>" />

<c:set var="showEmpty"
	value="<%=(Boolean)info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_SHOW_EMPTY)%>" />		
	
<c:set var="filterYears"
	value="<%=info.getAttributeEditorData(attr.getId(),DocumentPickerAttributeEditor.KEY_YEARS)%>" />
	
<c:set var="thisYear"
    value="<%= Calendar.getInstance().get(Calendar.YEAR)%>" />

<c:set var="filterTemplates"
	value="<%=info.getAttributeEditorData(attr.getId(),DocumentPickerAttributeEditor.KEY_DOC_TYPES)%>" />


<input type="hidden" id="${attrHtmlId}_values"
	name="${attrHtmlId}_values" value="" />
<script language="javascript" type="text/javascript">

	var ${attrHtmlId}_activeVariant = null; 
	var ${attrHtmlId}_variants = ${requestScope.variants};
	var ${attrHtmlId}_select = null;
	<%if (typed) {%>var ${attrHtmlId}_types = null;<%}%>

	dojo.addOnLoad(function() {	
		var addLinkBtn = new dijit.form.Button(
			{
				onClick: function() {	
				
					cardLinkPickerDisplayDialog('${attrHtmlId}',  
					'<%=attrCode%>', '<%=attr.getName()%>', <%= attr.isMultiValued()%>, <%=typed%>); 
				}
			},
			dojo.byId('${attrHtmlId}_AddLinkBtn')
		); 


		${attrHtmlId}_typeTitle = 
			<%{String title = (String)info.getAttributeEditorData(attr.getId(), DocumentPickerAttributeEditor.KEY_TYPE_CAPTION);%>
			"<%= title!=null ? title : ContextProvider.getContext().getLocaleMessage("search.column.linktype") %>";
			<%}%>

		documentPickerSwitchVariant('${attrHtmlId}', '${requestScope.activeVariant}', '<%=attrCode%>', <%=typed%>);

		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');

		${attrHtmlId}_types = <%=DocumentPickerAttributeEditor.getJSONMapTypesCardLink((TypedCardLinkAttribute)attr)%>;

		//clear filter values
		var filterDocTypeCombo = dijit.byId('${attrHtmlId}_DocType');
		if(filterDocTypeCombo) filterDocTypeCombo.setAttribute('value','');

		var filterYearSelect = dijit.byId('${attrHtmlId}_Year');
		if(filterYearSelect) filterYearSelect.setAttribute('value','${thisYear}');

		
		//display selected related cards 
		cardLinkPickerSetSelectedDocuments('${attrHtmlId}', '<%=attrCode%>', [${requestScope.selectedValues}]
		<% if (typed) {%>, true, ${attrHtmlId}_types<%}%>);


	});

	<%if (typed) {%>
	var typesCLink = null;

	var xhrArgs = {	
		url: "<%=request.getContextPath()+"/servlet/TreeServlet?idRef="+(String)((TypedCardLinkAttribute)attr).getReference().getId()+(visibleReferenceValues==null||visibleReferenceValues.isEmpty()?"":"&filterRef="+visibleReferenceValues) %>",
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
	<%if (referenceValuesIdsFoTemplates!=null&&!referenceValuesIdsFoTemplates.isEmpty()) {%>
	var filterTypesForLinkTemplates = <%=DocumentPickerAttributeEditor.getJSONMapTypesForTemplates(referenceValuesIdsFoTemplates)%>;
	<%}%>	
	
<%}%>
	


</script>


<table width="100%">
	<tr style="height: auto;">
		<td colspan="2">
			<div id="${attrHtmlId}_AdditionalButtonsBar" style="float: right"></div>
			<div style="float: right"><button id="${attrHtmlId}_AddLinkBtn"> <fmt:message key="addLink.button" /></button></div>
		</td>
	<tr>
</table>


<table class="res" id="${attrHtmlId}_table"

	<c:if test="${showTitle == false}"> 
		noHead="true"
	</c:if>

	<c:if test="${showEmpty == false}">
		noEmpty="true"
	</c:if>

	 style="width: 100%; margin-top: 0px;">

</table>


<div dojoType="dijit.Dialog" id="${attrHtmlId}_dialog" title="<%= attr.getName() %>" style="text-align: left;">

	<div id="${attrHtmlId}_filter">
		<%if(visibleInputs != null && !visibleInputs.isEmpty()){ %>
			<%if(visibleInputs.contains("Scope")){ %>
				<div>
					<label for="${attrHtmlId}_Scope"  style="width : 100px; float:left;"><fmt:message key="filter.scope" /></label>
					<select id="${attrHtmlId}_Scope" dojoType="dijit.form.FilteringSelect" autocomplete="false"	>
						<option value="1" selected><fmt:message key="filter.scope.subreports" /></option>
						<option value="2"><fmt:message key="filter.scope.wholeDoc" /></option>
						<option value="0"><fmt:message key="filter.scope.everything" /></option>
					</select>
				</div>
			<%} %>
			
			<%if(visibleInputs.contains("Year")){ %>
				<div>
					<label for="${attrHtmlId}_Year"  style="width : 100px; float:left;"><fmt:message key="filter.create.year" /></label>
					<select id="${attrHtmlId}_Year" dojoType="dijit.form.FilteringSelect"  value="" autocomplete="false">
						<c:forEach items="${filterYears}" var="filterYear">
							<option value="${filterYear}">${filterYear}</option>
			 			</c:forEach>
					</select>	
				</div>
			<%} %>
			
			<%if(visibleInputs.contains("DocType")){ %>
				<div>
					<label for="${attrHtmlId}_DocType" style="width : 100px; float:left;"><fmt:message key="filter.documentType" /></label>
					<select id="${attrHtmlId}_DocType" dojoType="dijit.form.ComboBox"  value=""  autocomplete="false">
		
						<c:forEach items="${filterTemplates}" var="filterTemplate">
							<option value="${filterTemplate.id.id}">${filterTemplate.name}</option>
						</c:forEach>
		
					</select>
		
				</div>
			<%} %>
			
			<%if(visibleInputs.contains("filterLine")){ %>
				<div >
					<label for="${attrHtmlId}_filterLine" style="width : 100px; float:left;"><fmt:message key="filter.document" /></label>
					<input id="${attrHtmlId}_filterLine" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
				</div>
				<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterLine" position="below" >
					<fmt:message key="filter.document.title" />
				</div>
				<div id="${attrHtmlId}_filterLineStrictSearchTooltip" dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterLineStrictSearchCheckBox" 
				        position="below" label="Bingo">
                </div>
			<%} %>
			
			<%if(visibleInputs.contains("filterId")){ %>
				<div >
					<label for="${attrHtmlId}_filterId" style="width : 100px; float:left;"><fmt:message key="filter.id" /></label>
					<input id="${attrHtmlId}_filterId" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
				</div>
				<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterId" position="below" >
					<fmt:message key="filter.id.title" />
				</div>
			<%} %>
			
			<%if(visibleInputs.contains("filterRegNum")){ %>
				<div>
					<label for="${attrHtmlId}_filterRegNum" style="width: 100px; float: left;"><fmt:message key="filter.regnum" /></label>
					<input id="${attrHtmlId}_filterRegNum" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
				</div>
				<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterRegNum" position="below" >
					<fmt:message key="filter.regnum.title" />
				</div>
			<%} %>
			
			<%if(visibleInputs.contains("filterProjectNumber")){ %>
				<br/>
				<div>
					<label for="${attrHtmlId}_filterProjectNumber" style="width: 100px; float: left;"><fmt:message key="filter.projectnumber" /></label>
					<input id="${attrHtmlId}_filterProjectNumber" type="text" dojoType="dijit.form.NumberTextBox" constraints="{min:1}" trim="true" style="width: 28em;"/>
					<input dojoType="dijit.form.CheckBox" id="${attrHtmlId}_filterProjectNumberStrictSearchCheckBox" 
							name="${attrHtmlId}_filterProjectNumberStrictSearchCheckBox" 
                           	checked="true"/>
					<label for="${attrHtmlId}_filterProjectNumberStrictSearchCheckBox" style="width: 100px;">
						<fmt:message key="filter.strictSearch" />
					</label>
					<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterProjectNumberStrictSearchCheckBox" position="below" >
						<fmt:message key="filter.strictSearch.title" />
					</div>
				</div>
				<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterProjectNumber" position="below" >
					<fmt:message key="filter.projectnumber.title" />
				</div>
			<%} %>
			<%if(visibleInputs.contains("filterOGAuthor")){ %>
                <br/>
                <div>
                    <label for="${attrHtmlId}_filterOGAuthor" style="width: 100px; float: left;"><fmt:message key="filter.ogAuthor" /></label>
                    <input id="${attrHtmlId}_filterOGAuthor" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 28em;"/>
                    <input dojoType="dijit.form.CheckBox" id="${attrHtmlId}_filterOGAuthorStrictSearchCheckBox" 
                    		name="${attrHtmlId}_filterOGAuthorStrictSearchCheckBox" 
                           checked="false"/>
					<label for="${attrHtmlId}_filterOGAuthorStrictSearchCheckBox" style="width: 100px;">
						<fmt:message key="filter.strictSearch" />
					</label>
					<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterOGAuthorStrictSearchCheckBox" position="below" >
						<fmt:message key="filter.strictSearch.title" />
					</div>
                </div>
                <div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterOGAuthor" position="below" >
                    <fmt:message key="filter.ogAuthor.title" />
                </div>
            <%} %>
            <%if(visibleInputs.contains("filterOutNumber")){ %>
                <br/>
                <div>
                    <label for="${attrHtmlId}_filterOutNumber" style="width: 100px; float: left;"><fmt:message key="filter.outNumber" /></label>
                    <input id="${attrHtmlId}_filterOutNumber" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 28em;"/>
                    <input dojoType="dijit.form.CheckBox" id="${attrHtmlId}_filterOutNumberStrictSearchCheckBox" 
                    		name="${attrHtmlId}_filterOutNumberStrictSearchCheckBox" 
                           checked="true"/>
					<label for="${attrHtmlId}_filterOutNumberStrictSearchCheckBox" style="width: 100px;">
						<fmt:message key="filter.strictSearch" />
					</label>
					<div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterOutNumberStrictSearchCheckBox" position="below" >
						<fmt:message key="filter.strictSearch.title" />
					</div>
                </div>
                <div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_filterOutNumber" position="below" >
                    <fmt:message key="filter.outNumber.title" />
                </div>
            <%} %>
		<%} %>	
		<div >
			<button id="${attrHtmlId}_filterButton" dojoType="dijit.form.Button" type="button"><fmt:message key="filter.button" /></button>
		</div>
	</div> 
	<div id="${attrHtmlId}_loading" align="center">
		<img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt="" />
	</div>

	<div id="${attrHtmlId}_hierarchy" valign="bottom"></div>

</div>