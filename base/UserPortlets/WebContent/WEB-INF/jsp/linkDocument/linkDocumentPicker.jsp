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
﻿<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ResourceBundle"%>
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

<jsp:include page="linkDocumentPickerInclude.jsp" />

<portlet:defineObjects />

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkPickerEditResource" />

<%
	Attribute attr =  (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String attrCode = (String)attr.getId().getId();
	boolean typed = attr.getType().equals(Attribute.TYPE_TYPED_CARD_LINK);
	final Calendar c = Calendar.getInstance();
	int currentYear = c.get(Calendar.YEAR);
	ResourceBundle bundle = ResourceBundle.getBundle("com.aplana.dbmi.portlet.nl.ContentViewPortlet", request.getLocale());
	String linkDocDlgHeader = bundle.getString("header.linkDoc");
%>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="filterYears" value="${filterYears}" />
<c:set var="filterTemplates" value="${filterTemplates}" />
<c:set var="currentYear" value="<%=currentYear%>" />

<input type="hidden" id="${attrHtmlId}_values" name="${attrHtmlId}_values" value="" />

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

		${attrHtmlId}_typeTitle = 'typeTitle';
		
		documentPickerSwitchVariant('${attrHtmlId}', '${requestScope.activeVariant}', '<%=attrCode%>', <%=typed%>);
		
		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
		
		${attrHtmlId}_types = <%=DocumentPickerAttributeEditor.getJSONMapTypesCardLink((TypedCardLinkAttribute)attr)%>;

		//prepareLinkDocDlg();
	});

	/*function prepareLinkDocDlg() {
		var linkDocDlg = dijit.byId('${attrHtmlId}_dialog');
		if(linkDocDlg) {
			linkDocDlg._onShow = function() {
				restoreModality();
			}
			
			linkDocDlg.onCancel = function() {
				unlockScreen();
				this.hide();
			}
		}
	}

	function restoreModality() {
		var lock = dojo.byId('lockPane');
		lock.innerHTML = '';
		lock.className = 'dlg_lock_on';
	}*/
</script>

<div dojoType="dijit.Dialog" id="${attrHtmlId}_dialog" title="<%=linkDocDlgHeader%>" style="font-size: 70%; text-align: left;">

	<div id="${attrHtmlId}_filter">
		<div>
			<label for="${attrHtmlId}_Year"  style="width : 120px; float:left;"><fmt:message key="filter.year" /></label>
	    	<select id="${attrHtmlId}_Year" dojoType="dijit.form.ComboBox" autocomplete="false" >
		        <c:forEach items="${filterYears}" var="filterYear">
                    	<option
                    		<c:if test="${currentYear == filterYear}">
								selected
							</c:if>
                    	>${filterYear}</option>
	    		</c:forEach>
	    	</select>
		</div>
		<div>
			<label for="${attrHtmlId}_DocType" style="width : 120px; float:left;"><fmt:message key="filter.documentType" /></label>
			<select id="${attrHtmlId}_DocType" dojoType="dijit.form.FilteringSelect" autocomplete="false">
				<c:forEach items="${filterTemplates}" var="filterTemplate" varStatus="rowCounter">
					<option value="${filterTemplate.id.id}"
						<c:if test="${rowCounter.first}">
							selected
						</c:if>
					>${filterTemplate.name}</option>
				</c:forEach>
			</select>
		</div>
		<div>
			<label for="${attrHtmlId}_filterLine" style="width : 120px; float:left;"><fmt:message key="filter.documentName" /></label>
			<input id="${attrHtmlId}_filterLine" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
		</div>
		<div>
			<button id="${attrHtmlId}_filterButton" dojoType="dijit.form.Button" type="button"><fmt:message key="filter.button" /></button>
		</div>
	</div>
    <div id="${attrHtmlId}_loading" align="center">
    	<img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt="" />
    </div>
    <div id="${attrHtmlId}_hierarchy" valign="bottom"></div>
</div>