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
<%@page import="com.aplana.dbmi.portlet.DocumentDataPortletSessionBean"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.portlet.DocumentDataPortlet"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.DocumentDataPortlet"/>
<portlet:defineObjects/>

<style type="text/css">

#document_body .fieldTitle {
	color: #999
}

#document_body .fileItem {
	margin-left: 10px;
}

</style>

<%
DocumentDataPortletSessionBean sessionBean = (DocumentDataPortletSessionBean)renderRequest.getPortletSession()
		.getAttribute(DocumentDataPortlet.SESSION_BEAN);
%>
<SCRIPT src="/DBMI-UserPortlets/js/boss.js"></SCRIPT>

<SCRIPT>
var <portlet:namespace/>_attachments = ${requestScope.attachments};
var <portlet:namespace/>_infomaterials = ${requestScope.infomaterials};
var <portlet:namespace/>_linkedToDocs = ${requestScope.linkedToDocs};
var <portlet:namespace/>_linkedFromDocs = ${requestScope.linkedFromDocs};

var docId = <%= sessionBean.getDocId()%>;

function showJasperReport() {
	var url;
	if(<%=sessionBean.isDeloState()%>){
		url = '<%=sessionBean.getArchiveReportURL()%>';
		if (url=='null') {
			alert('Архивная печатная форма по данному документу отсутствует');
			url='';
		}
	} else {
		var nameConf = printForms['${requestScope.template}'];
		url = '/DBMI-UserPortlets/servlet/JasperReportServlet?nameConfig='+nameConf+'&card_id=L_'+docId+'&noname=1';
	}
	switchData(url);
}

function switchData(src) {
	dojo.query('iframe')[0].src = src;
}

dojo.addOnLoad(function() {

	var doc_data_msg = dojo.byId('doc_data_msg');
	if(doc_data_msg && doc_data_msg.value.length > 0) {
		return;
	}
	showJasperReport();
	<portlet:namespace/>_buildTable('<portlet:namespace/>_attachments', <portlet:namespace/>_attachments);
	<portlet:namespace/>_buildPlainTable('<portlet:namespace/>_inomaterials', <portlet:namespace/>_infomaterials);
	<portlet:namespace/>_buildDocsTable('<portlet:namespace/>_linkToDocs', <portlet:namespace/>_linkedToDocs, true);
	<portlet:namespace/>_buildDocsTable('<portlet:namespace/>_linkFromDocs', <portlet:namespace/>_linkedFromDocs);
});

function <portlet:namespace/>_buildTable(tableId, jsonData) {
	var iterLbl = '<fmt:message key="label.iteration"/>';
	var noAttachmentsLbl = '<fmt:message key="label.noAttachments"/>';
	var table = dojo.byId(tableId);
	var iterNr = jsonData.length;
	if (iterNr == -1) {
		<portlet:namespace/>_displayAttachments(table, jsonData[0].parrent);
	} else {
		for (var i = iterNr; i > 0; i--) {
			<portlet:namespace/>_addTableStr(table, iterLbl + ' ' + i, 'bold_font');
			var iterAttachments = jsonData[i-1].parrent;
			if (iterAttachments.length > 0) {
				<portlet:namespace/>_displayAttachments(table, iterAttachments);
			} else {
				<portlet:namespace/>_addTableStr(table, noAttachmentsLbl, 'fieldTitle underlined fileItem');
			}
			<portlet:namespace/>_addComments(table, jsonData[i-1].child);
		}
	}
}

function <portlet:namespace/>_buildPlainTable(tableId, jsonData) {
	var table = dojo.byId(tableId);
	var iterNr = jsonData.length;
	if (iterNr == -1) {
		<portlet:namespace/>_displayAttachments(table, jsonData[0].parrent);
	} else {
		for (var i = iterNr; i > 0; i--) {
			var iterAttachments = jsonData[i-1].parrent;
			if (iterAttachments.length > 0) {
				<portlet:namespace/>_displayAttachments(table, iterAttachments);
			}
			<portlet:namespace/>_addComments(table, jsonData[i-1].child);
		}
	}
}

function <portlet:namespace/>_addComments(table, child){
	if(typeof child == 'undefined' || child == null || child.length<1){
		return;
	}
	var comment = '<fmt:message key="label.comment"/>';
	var author = '<fmt:message key="label.author"/>';
	for(var i = 0; i<child.length; i++){
		var str = comment+": "+ child[i].author + ", " + child[i].comment;
		<portlet:namespace/>_addTableStr(table, str, 'font_comment');
		<portlet:namespace/>_displayAttachments(table, child[i].files, 'padding_data');
	}	
}

function <portlet:namespace/>_addTableStr(table, str, className) {
	row = table.insertRow(-1);
	cell = row.insertCell(0);
	label = document.createElement('label');
	label.innerHTML = str;
	
	if(className) {
		dojo.addClass(label, className);
	}
	cell.appendChild(label);
}

function <portlet:namespace/>_displayAttachments(table, jsonData) {
	<portlet:namespace/>_displayAttachments(table, jsonData, null);
}

function <portlet:namespace/>_displayAttachments(table, jsonData, className) {
	for (var i = 0; i < jsonData.length; i++) {
		row = table.insertRow(-1);
		cell = row.insertCell(0);
		
		<portlet:namespace/>_addStyle(cell, className);
		
		link = document.createElement('a');
		link.setAttribute('onclick', 'switchData(getAttachUrl('+jsonData[i].cardId+'))');
		link.innerHTML = jsonData[i].name;
		dojo.addClass(link, 'fileItem');
		cell.appendChild(link);		
		link = document.createElement('a');
		link.setAttribute('class', 'download_attachment');
		link.setAttribute('onclick', 'return saveAttachment(this, getSaveAttachUrl('+jsonData[i].cardId+'));');
		link.title = '<fmt:message key="label.saveFile"/>';
		cell.appendChild(link);
	}
}

function <portlet:namespace/>_addStyle(component, className){
	if(className) {
		dojo.addClass(component, className);
	}
}

function <portlet:namespace/>_buildDocsTable(tableId, jsonData, isLinkedTo) {
	var table = dojo.byId(tableId);
	
	for (var i = 0; i < jsonData.length; i++) {
		row = table.insertRow(-1);
		var cellId = 0;
		if(isLinkedTo) {
			cell = row.insertCell(cellId);
			cell.innerHTML = jsonData[i].linkType;
			cellId += 1;
		}
		cell = row.insertCell(cellId);
		var primeAttId = jsonData[i].primeAttachmentId;
		if(primeAttId && primeAttId.length > 0) {
			link = document.createElement('a');
			link.setAttribute('onclick', 'switchData(getAttachUrl('+primeAttId+'))');
			link.innerHTML = jsonData[i].name;
			cell.appendChild(link);
		} else {
			cell.innerHTML = jsonData[i].name;
		}
		link = document.createElement('a');
		link.setAttribute('class', 'doc_data_icon');
		link.setAttribute('onclick', 'showDocumentData('+jsonData[i].cardId+');');
		cell.appendChild(link);
		cell.align = 'left';
	}
}

function getAttachUrl(id) {
	return getSaveAttachUrl(id)+"&noname=1&pdf=1";
}

function getSaveAttachUrl(id) {
	return "/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD="+id;
}

function saveAttachment(downloadLink, url) {
	downloadLink.href = url;
	return true;
}

function showDocumentData(id) {
	var href = '/portal/auth/portal/boss/documentData/Content?&action=1&cardId='+id;
	window.open(href,'_blank');
}

function setBottomFrameHeight() {
	var pageContent = document.getElementById("<portlet:namespace/>_document_data");
	if (pageContent) {
		var contentHeight = document.body.clientHeight - 100;
		pageContent.style.height = contentHeight + 'px';
	}
}
</SCRIPT>

<c:set var="sessionBean" value="<%=sessionBean%>"/>
<input type='hidden' id='doc_data_msg' value='${sessionBean.message}'>

<c:choose>
	<c:when test="${not empty sessionBean.message}">
		<div class="document_data" id="<portlet:namespace/>_document_data">
			<div class="err_msg">${sessionBean.message}</div>
		</div>
	</c:when>
	<c:otherwise>
		<c:set var="headerIcon" value="item13"/>
		<h1 class="caption">
		<span class="header_icon ${headerIcon}"></span> <c:out value="${sessionBean.header}"/>
		</h1>
		
		<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
			<div class="document_data" id="<portlet:namespace/>_document_data">
				<hr/>
				<div class="left_column">
					<div class="left_column_int">
						<p><a onclick="showJasperReport();"><fmt:message key="label.printForm"/></a></p>
						
						<span class="form_item"><fmt:message key="label.attachments"/>:</span>
						<div class="contentDiv">
							<table id="<portlet:namespace/>_attachments"/>
							</table>
						</div>
						<span class="form_item"><fmt:message key="label.inomaterials"/>:</span>
						<div class="contentDiv">
							<table id="<portlet:namespace/>_inomaterials"/>
							</table>
						</div>
						<span class="form_item"><fmt:message key="label.linkToDocs"/>:</span>
						<div class="contentDiv">
							<table id="<portlet:namespace/>_linkToDocs"/>
							</table>
						</div>
						<span class="form_item"><fmt:message key="label.linkFromDocs"/>:</span>
						<div class="contentDiv">
							<table id="<portlet:namespace/>_linkFromDocs"/>
							</table>
						</div>
					</div>
				</div>
				<div class="right_column">
					<div class="right_column_int">
						<iframe width="100%"  height="100%" border="0" scrolling="auto"/>
						</iframe>
					</div>
				</div>
			</div>
		</form>
	</c:otherwise>
</c:choose>
