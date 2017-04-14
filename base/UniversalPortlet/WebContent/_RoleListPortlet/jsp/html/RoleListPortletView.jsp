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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.universalportlet.*, com.aplana.dbmi.rolelistportlet.*" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.text.MessageFormat"%>
<%@page import="org.displaytag.util.SortingState"%>
<%@page import="java.text.SimpleDateFormat"%>

<%@page import="com.aplana.dbmi.*"%>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="com.aplana.dbmi.common.utils.portlet.PortletMessage"%>
<%@page import="com.aplana.web.tag.util.StringUtils"%>


<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.rolelistportlet.nl.RoleListPortletResource" scope="request"/>
<%
  	UniversalPortletSessionBean sessionBean = (UniversalPortletSessionBean)renderRequest.getPortletSession().getAttribute(UniversalPortlet.SESSION_BEAN);
	List columnsMetaData = sessionBean.getColumnsMetaData();
	request.setAttribute("columnsMetaData", columnsMetaData);
  	List parametersMetaData = sessionBean.getParametersMetaData();
  	List data = sessionBean.getData();
  	String[] initialParams = sessionBean.getInitialParameters();
	request.setAttribute("dataList", data);

  	boolean isPrintMode = sessionBean.isPrintMode();   	
	if( isPrintMode) {
  		sessionBean.setPrintMode(false);
  	}
	boolean editAccessExists = sessionBean.isEditAccessExists();
	PortletService portletService = Portal.getFactory().getPortletService();
	
	PortletMessage message = (PortletMessage)renderRequest.getPortletSession().getAttribute(RoleListPortlet.MSG_PARAM_NAME, PortletSession.APPLICATION_SCOPE);
	renderRequest.getPortletSession().removeAttribute(RoleListPortlet.MSG_PARAM_NAME, PortletSession.APPLICATION_SCOPE);
%>
	
<%
	// PRINT Mode
	if (isPrintMode) { %>

<script>

function showPrintPage() {

var datatable = document.getElementById('printTableDiv').innerHTML;
var NewWindow = window.open("about:blank",'_blank',"resizable=yes,scrollbars=yes,status=yes,titlebar=yes,toolbar=yes,menubar=yes,location=no");
var NewWinDoc = NewWindow.document;
NewWinDoc.writeln('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"');
NewWinDoc.writeln('   "http://www.w3.org/TR/html4/loose.dtd">');

NewWinDoc.writeln('<html><head>');

NewWinDoc.writeln('<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">');
	

NewWinDoc.writeln('<LINK rel=\"stylesheet\" href=\"<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/showList.css") %>\" type=\"text/css\">');

NewWinDoc.writeln('<style type=\"text/css\">');
NewWinDoc.writeln(' @import <%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/print.css") %> print;');
NewWinDoc.writeln('</style>');

NewWinDoc.writeln('</head><body>');

NewWinDoc.writeln('<div class=reportheader>');
NewWinDoc.writeln('<a href=\"#\" onclick=\"window.print();return false\"><span class=\"report printPage_print\">&nbsp;</span></a>');
NewWinDoc.writeln('<a href=\"javascript:onClick=window.close();\"><span class=\"report printPage_close\">&nbsp;</span></a>');
NewWinDoc.writeln('<div class=\"reportheaderHR\"></div>');
NewWinDoc.writeln('</div>');

NewWinDoc.writeln(datatable);
NewWinDoc.writeln('</body></html>');
NewWinDoc.close();
}
</script>

<div style="display: none;" id="printTableDiv" > 
  <display:table name="dataList" id="printTable" uid="printTable" class="res">
    <display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list" /></display:setProperty>
    <display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found" /></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found" /></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found" /></display:setProperty>
	<display:caption><%= sessionBean.getTitle() %></display:caption>
	
<%
	
	for (int i = 0 ; i < columnsMetaData.size() ; i++  ) {
		ColumnDescription columnDescription = (ColumnDescription) columnsMetaData.get(i);

		Object columnValue = "";
		Object columnValuesList = pageContext.getAttribute("printTable");
		if (columnValuesList != null 
			&& !((List)columnValuesList).isEmpty()
			) {
			columnValue = ((List)columnValuesList).get(i);			
		}
		
%>
    <display:column  title="<%= columnDescription.getDisplayName() %>" >
			<%= columnValue %>
	</display:column>
<% 		
	}
 %>	
	
  </display:table>

</div>  

<% } // Print Mode End
 %>
 
    <c:set var="confirmationDialogId"><portlet:namespace/>_confirmationDialog</c:set>
	<div id="${confirmationDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="role.delete.confirmation.title" />" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="role.delete.confirmation.message"/></div>
		<div style="float:right; clear: both;" id="dialogButtons">
			<c:set var="yesBtnId"><portlet:namespace/>_yesBtnId</c:set>	
			<button id="${yesBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="role.delete.confirmation.yes" />
			    <script type="dojo/method" event="onClick" args="evt">
						submitForm_action('<%= RoleListPortlet.DELETE_ROLE_ACTION %>', this);
				</script>		
			</button>
			<c:set var="noBtnId"><portlet:namespace/>_noBtnId</c:set>
			<button id="${noBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="role.delete.confirmation.no" />
				<script type="dojo/method" event="onClick" args="evt">
					document.UniversalPortletForm.<%= RoleListPortlet.MI_DELETE_ENTITY_ID %>.value = "";
					dijit.byId('${confirmationDialogId}').hide();
				</script>	
			</button>	
		</div>
	</div>

<script type="text/javascript">

	function submitForm_single(elem, link) {
		elem.onclick = function() { return false }
		window.location.href = link;
	}
	
	function submitForm_action(action, elem) { 
		if (elem) elem.onClick = function() { return false };
		document.UniversalPortletForm.<%= RoleListPortlet.ACTION_FIELD %>.value = action;
		document.UniversalPortletForm.submit();
	}
	function submitForm_delete(elem, entityId) {
		document.UniversalPortletForm.<%= RoleListPortlet.MI_DELETE_ENTITY_ID %>.value = entityId;
		dijit.byId('${confirmationDialogId}').show();
	}
<%	
	for (Iterator paramIter = parametersMetaData.iterator(); paramIter.hasNext();) {
    	ParameterDescription parameterDescription = (ParameterDescription) paramIter.next();
%>
  		var oldValueDate_<%=parameterDescription.getName()%> = null;
<%
	}
%>
	dojo.addOnLoad(function(){
		  dojo.require("dijit.form.DateTextBox");
		  dojo.require("dojo.date");
		  dojo.require("dojo.date.stamp");
		  dojo.require("dojo.parser");
		  dojo.require('dijit.Dialog');
		  dojo.require('dijit.form.Button');
		  dojo.require("dijit.Tooltip");
		  dojo.require('dijit.dijit');
<%	
		int iPar = 0;
		for (Iterator paramIter = parametersMetaData.iterator(); paramIter.hasNext();iPar++) {
    		ParameterDescription parameterDescription = (ParameterDescription) paramIter.next();
			if (parameterDescription.getType() == ParameterDescription.CALENDAR_TYPE && initialParams != null && initialParams[iPar] != null) {
%>
				oldValueDate_<%=parameterDescription.getName()%> = dojo.date.stamp.fromISOString("<%=initialParams[iPar]%>");
<%
			}
		}
%>
	});

  	function validDate(attrId, oldValueDate) {
  		var inputDate = dijit.byId(attrId);
  		if (!inputDate.isValid(true)) {
	  		inputDate.attr('value', oldValueDate);
	  	} else {
	  		var value = inputDate.attr('value');
	  		if (oldValueDate != null) {
		  		if (value != null) {
			  		oldValueDate.setFullYear(value.getFullYear());
			  		oldValueDate.setMonth(value.getMonth());
			  		oldValueDate.setDate(value.getDate());
		  		} else {
		  			eval('oldValueDate_'+attrId+' = null;');
		  		}
	  		} else {
	  			if (value != null) {
	  				eval('oldValueDate_'+attrId+' = new Date();');
  					eval('oldValueDate_'+attrId+'.setFullYear(value.getFullYear());');
	  				eval('oldValueDate_'+attrId+'.setMonth(value.getMonth());');
	  				eval('oldValueDate_'+attrId+'.setDate(value.getDate());');
	  			}
	  		}
	  	}
  	};
</script>
<portlet:actionURL var="formAction"/>

<!-- Title and toolbar -->
<div class="columns">
    <div class="header">
			<%if(sessionBean.getFilter() != null) {
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(UniversalPortlet.ACTION_FIELD, UniversalPortlet.BACK_TO_REQUEST_ACTION); 
			%>	
			<a HRef="<%= backURL.toString() %>" style="text-decoration: underline;" ><span class="back"><fmt:message key="back.my.request.link"  /></span></a>
			<%} %>
			<div style="margin-top: 10px;"><%= sessionBean.getTitle() %></div>
    </div>
    <div class="toolbar">
    	<div class="buttonPanel">
			<ul>
			<%	
				final HashMap urlParams = new HashMap();

				urlParams.put(RoleListPortlet.MI_CREATE_ENTITY, "true");
				PortletURL backURL = renderResponse.createRenderURL();
				backURL.setParameters(renderRequest.getParameterMap());
				urlParams.put(RoleListPortlet.BACK_URL_FIELD, URLEncoder.encode(backURL.toString(), "UTF-8"));
				final String createRoleURL = portletService.generateLink("dbmi.Role", "dbmi.Role.w.Role", urlParams, renderRequest, renderResponse);
			
			if (sessionBean.isShowRefreshButton())
			{
				PortletURL refreshURL = renderResponse.createActionURL();
				refreshURL.setParameter(UniversalPortlet.ACTION_FIELD, UniversalPortlet.REFRESH_ACTION);
 				%>
 				<btn:button textKey="refresh.btn"
 								href="<%= refreshURL.toString() %>" />
			<%}%>
			<%  if (sessionBean.isShowCreate() && sessionBean.isCreateAccessExists()) {%>
		 			    	<btn:button tooltipKey="tool.role.create"
										href="<%= response.encodeURL(createRoleURL) %>" 
										icon="ico_new"/>
			<%}%>
			<%if (editAccessExists&&sessionBean.isCanImport()&&sessionBean.isShowCreate()) {
				final PortletURL importRoleURL = renderResponse.createActionURL();
				importRoleURL.setParameter(RoleListPortlet.ACTION_FIELD, RoleListPortlet.IMPORT_ROLE_ACTION); 
				importRoleURL.setParameter(RoleListPortlet.BACK_URL_FIELD, URLEncoder.encode(backURL.toString(), "UTF-8"));%> 
				<btn:button tooltipKey="tool.import.roles.xls"
							href="<%= importRoleURL.toString() %>"
							icon="ico_xls_in"/>
			<%}%>
			<%
				final String downloadImportTemplate = sessionBean.getDownloadImportTemplate();
				if(downloadImportTemplate != null) { %>
						
						<btn:button tooltipKey="tool.download.importTemplates"
										href="<%= "/DBMI-UserPortlets/MaterialSearchDownloadServlet?"
										+ "SEARCH_TEMPLATE_ID=jbr.import"
										+ "&SEARCH_STATUS_ID=published"
										+ "&SEARCH_ATTR_ID=list:jbr.loadingDict"
										+ "&SEARCH_ATTR_VALUE=" + downloadImportTemplate %>"
										icon="ico_csv_in"/>	
										
				<% } %>
				
				<% if(sessionBean.isCanBeExportedToExcel()) {%>
		 		<btn:button href="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/universalportlet/exporttoexcel?namespace=" + renderResponse.getNamespace()) %>" 
							tooltipKey="tool.export.cards.xls"
							icon="ico_xls"/>
				<%
				}
				if(sessionBean.isCanBePrinted()) {
				%>
				<c:set var="action_print"><portlet:actionURL><portlet:param name="<%= UniversalPortlet.ACTION_FIELD %>" value="<%= UniversalPortlet.PRINT_ACTION %>"/></portlet:actionURL></c:set>
				<btn:button href="${action_print}" 
							tooltipKey="tool.print.cards"
							icon="ico_print"/>
				<%} %>
       		</ul>
       	</div>		      	     
    </div>
</div>

<div class="universalPortletParameterContainer">
<% if (message != null && message.getMessage() != null && message.getMessageStyle() != null) {%>

	<table class="<%= message.getMessageStyle() %>">
		<tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
		<tr class="tr2"><td class="td_21"/><td class="td_22">
		<%= StringUtils.replaceNewlineWithBreak(message.getMessage()) %></td><td class="td_23"/>
		</tr>
	    <tr class="tr3"><td class="td_31"/><td class="td_32"/><td class="td_33"/></tr>
	</table>
<%} %>

<form name="UniversalPortletForm" action="${formAction}" method="post" >
<input type="hidden" name="<%= RoleListPortlet.ACTION_FIELD %>" value="">
<input type="hidden" name="<%= RoleListPortlet.MI_DELETE_ENTITY_ID %>" value="">
</form>
</div>

<%
RoleListPortletTableDecorator decorator = new RoleListPortletTableDecorator(renderRequest, renderResponse);
decorator.setColumnDescriptions(columnsMetaData);
String decoratorAttributeName = "UniversalPortletTableDecorator";
request.setAttribute(decoratorAttributeName, decorator);

	SortingState sortingState = SortingState.loadSortingStateFromSession(UniversalPortlet.TABLE_ID, renderRequest.getPortletSession());
    int defaultSortAttribute = (sortingState != null && sortingState.getSortColumn() >= 0) ? sortingState.getSortColumn() + 1 : -1;
    String defaultOrderAttribute = (sortingState != null && sortingState.getSortOrder() != 0) ? (sortingState.getSortOrder() == 1 ? "descending" : "ascending") : "ascending";
    int pageNumberAttribute = (sortingState != null && sortingState.getPageNum() != 0) ? sortingState.getPageNum() : 1;

%>

	
<display:table name="dataList" id="<%= UniversalPortlet.TABLE_ID %>" uid="<%= UniversalPortlet.TABLE_ID %>"  sort="list" class="res" pagesize="<%= sessionBean.getPageSize() %>" defaultsort="<%= defaultSortAttribute %>" defaultorder="<%= defaultOrderAttribute %>" pageNumber="<%= pageNumberAttribute %>" decorator="<%= decoratorAttributeName %>">
    <display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list" /></display:setProperty>
    <display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found" /></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found" /></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found" /></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found" /></display:setProperty>
<% 
	List columnValues = (List) pageContext.getAttribute("dataItem");
	
	if (columnValues != null && !columnValues.isEmpty()) {
		String entityId = columnValues.get(1).toString();		
		final Map params = new HashMap();
		params.put(RoleListPortlet.MI_OPEN_ENTITY_ID, entityId);
		params.put(RoleListPortlet.OPEN_FOR_EDIT_FIELD, "true");
		params.put(RoleListPortlet.BACK_URL_FIELD, URLEncoder.encode(backURL.toString(), "UTF-8"));
		String editLink = portletService.generateLink("dbmi.Role", "dbmi.Role.w.Role", params, renderRequest, renderResponse);
	%>
		<display:column>
		<fmt:message var="deleteTooltip" key="tool.role.delete" />
		<c:set var="deleteAllowed"><%=editAccessExists%></c:set>
		<c:set var="onClickCallback">submitForm_delete(this, '<%= entityId %>')</c:set>
		<btn:linkimage enable="${deleteAllowed}" enableIcon="delete"
			toolTip="${deleteTooltip}" onClick="${onClickCallback}">#</btn:linkimage>
		</display:column>

		<display:column>
		<fmt:message var="editTooltip" key="tool.role.edit" />
		<c:set var="editAllowed"><%=editAccessExists%></c:set>
		<c:set var="editLink"><%= editLink %></c:set>
		<btn:linkimage enable="${editAllowed}" enableIcon="edit" disableIcon="edit_disable" 
			toolTip="${editTooltip}" onClick="submitForm_single(this, '${editLink}')">#</btn:linkimage>
		</display:column>
<% } else { %>
<display:column>
</display:column>
<% }

	int colNum = 0;
	for (Iterator metaIter = columnsMetaData.iterator(); metaIter.hasNext(); colNum++) {
		ColumnDescription columnDescription = (ColumnDescription) metaIter.next();
		if (!columnDescription.isHidden()) {
		String indexed = "[" + colNum + "]";
		MessageFormat link = columnDescription.getLink();
		if (link != null) {
			String indexedLink = "link" + indexed;
 %>
	<display:column  title="<%= columnDescription.getDisplayName() %>" sortable="<%= columnDescription.isSortable() %>" property="<%= indexedLink %>" sortProperty="<%= indexed %>" maxLength="<%= columnDescription.getWidth() %>"/>
<%
		} else {
 %>
	<display:column title="<%= columnDescription.getDisplayName() %>" sortable="<%= columnDescription.isSortable() %>"
			property="<%= indexed %>" maxLength="<%= columnDescription.getWidth() %>">
<%= columnValues.get(colNum) != null ? columnValues.get(colNum) : "-" %>
    </display:column>
<%
        }
		}
	}
 %>
</display:table>

<% if (isPrintMode) { %>
<script language="javascript">
	showPrintPage();
</script>
<%} %>