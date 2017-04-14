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

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %> 
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.portlet.ResolutionReportPortletSessionBean"%>
<%@page import="com.aplana.dbmi.portlet.ResolutionReportPortlet"%>


<%@page import="com.aplana.dbmi.portlet.ResolutionReportPortletAction"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>
<%@page import="com.aplana.dbmi.card.CardLinkPickerAttributeEditor"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="com.aplana.dbmi.ajax.DocumentPickerParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerHierarchyParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="javax.portlet.PortletSession"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.ResolutionReportPortlet"/>
<portlet:defineObjects/>

<c:set var="attrHtmlId" value="documentPickerAttrHtmlId" />
<c:set var="filterYears" value="${requestScope.filterYears}" />	 
<c:set var="filterTemplates" value="${requestScope.filterTemplates}" />

<%	
ResolutionReportPortletSessionBean sessionBean = 
	(ResolutionReportPortletSessionBean) renderRequest.getPortletSession().getAttribute(ResolutionReportPortlet.SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());
%>

<script type="text/javascript">
	dojo.require("dijit.form.Button");
	dojo.require("dijit.Dialog");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dijit.form.FilteringSelect");	
	dojo.require("dijit.layout.SplitContainer");
	dojo.require("dijit.layout.ContentPane");	
	
	var <portlet:namespace/>_groupResolutions = ${requestScope.groupResolutions};

	var <portlet:namespace/>_reportAttachments = ${requestScope.reportAttachments};
	var <portlet:namespace/>_reportPreparedDocs = ${requestScope.reportPreparedDocs};

	var <portlet:namespace/>_message = ${requestScope.message};
	

	dojo.addOnLoad(function() {
							
		if (<portlet:namespace/>_message != '') {
			alert(<portlet:namespace/>_message);
		}
	});
	
	var attachmentLink = '<%=ResolutionReportPortlet.ATTACHMENT_URL%>';
	var preparedDocLink = '<%=ResolutionReportPortlet.PREPARED_DOC_URL%>';
	var createDocBtnName = '<%=ResolutionReportPortlet.CREATE_DOC_BTN%>';

	var <portlet:namespace/>_buttonPressed = null;
	

	
	dojo.addOnLoad(function() {
		<portlet:namespace/>_buildSelectedReports();
		<portlet:namespace/>_buildTable('<portlet:namespace/>_reportAttachments', 
				<portlet:namespace/>_reportAttachments, attachmentLink, true);
		<portlet:namespace/>_buildTable('<portlet:namespace/>_reportPreparedDocs', 
				<portlet:namespace/>_reportPreparedDocs, preparedDocLink, true);
	});
	
	function <portlet:namespace/>_buildSelectedReports(){
		var table = dojo.byId('<portlet:namespace/>_groupResolutions');
		<portlet:namespace/>_groupResolutions.forEach(function(report){
			var row = table.insertRow(-1);
			var cell = row.insertCell(0);
			cell.align = 'left';
			cell.innerHTML = report.name;
		});
	}
	
	function <portlet:namespace/>_buildTable(tableId, jsonData, viewLink, isReportTable) {
		var table = dojo.byId(tableId);
		var idPrefix = isReportTable ? 'report' : '';
		
		for (var i = 0; i < jsonData.length; i++) {
			row = table.insertRow(-1);
				
			cell = row.insertCell(0);
			checkBox = document.createElement('input');
			checkBox.type = 'checkbox';
			checkBox.id = idPrefix + 'checkbox_' + jsonData[i].cardId;
			checkBox.value = jsonData[i].cardId; 
			cell.appendChild(checkBox);

			cell = row.insertCell(1);
			link = document.createElement('a');
			link.href = viewLink + jsonData[i].cardId;
			link.target = '_blank'; 
			link.innerHTML = jsonData[i].name;
			link.id = checkBox.id + "_a";
			cell.appendChild(link);
			cell.align = 'left';
		}
	}

	function <portlet:namespace/>_selectAllItems(tableId, allItemsCheckbox) {
		var checkboxes = dojo.query('#' + tableId + ' input');
		var checked = allItemsCheckbox.checked;

		if(checkboxes) {
			for(var i = 0; i < checkboxes.length; i++) {
				if(checkboxes[i] == allItemsCheckbox) {
					continue;	
				}	
				checkboxes[i].checked = checked;
			}
		}
	}

	function <portlet:namespace/>_removeAttachments() {
		var attachmentsTableBody = dojo.query('#<portlet:namespace/>_reportAttachments tbody')[0];
		var attachmentCheckboxes = dojo.query('#<portlet:namespace/>_reportAttachments input');
		
		if(attachmentCheckboxes) {
			for(var i = 0; i < attachmentCheckboxes.length; i++) {
				if(attachmentCheckboxes[i].checked == true) {
					attachmentsTableBody.removeChild(attachmentCheckboxes[i].parentNode.parentNode);	
				}
			}
		}	
	}

	function <portlet:namespace/>_addAttachment(attachmentsTable, attachmentId, attachmentName){
		checkBox = dojo.byId('reportcheckbox_' + attachmentId);
		if(checkBox) {
			return;
		}
		
		row = attachmentsTable.insertRow(-1);
		
		cell = row.insertCell(0);
		checkBox = document.createElement('input');
		checkBox.type = 'checkbox';
		checkBox.id = 'reportcheckbox_' + attachmentId;
		checkBox.value = attachmentId; 
		cell.appendChild(checkBox);

		cell = row.insertCell(1);
		link = document.createElement('a');
		link.href = attachmentLink + attachmentId;
		link.target = '_blank'; 
		link.innerHTML = attachmentName;
		cell.appendChild(link);
		cell.align = 'left';
	}

	function <portlet:namespace/>_addPreparedDoc(docId, docName){
		var preparedDocsTable = dojo.byId('<portlet:namespace/>_reportPreparedDocs');
		
		checkBox = dojo.byId('reportcheckbox_' + docId);
		if(checkBox) {
			return;
		}
		
		row = preparedDocsTable.insertRow(-1);
		
		cell = row.insertCell(0);
		checkBox = document.createElement('input');
		checkBox.type = 'checkbox';
		checkBox.id = 'reportcheckbox_' + docId;
		checkBox.value = docId; 
		cell.appendChild(checkBox);

		cell = row.insertCell(1);
		link = document.createElement('a');
		link.href = preparedDocLink + docId;
		link.target = '_blank'; 
		link.innerHTML = docName;
		cell.appendChild(link);
		cell.align = 'left';
	}
	
	function <portlet:namespace/>_getRemovedPreparedDocsIds() {
		var preparedDocsTableBody = dojo.query('#<portlet:namespace/>_reportPreparedDocs tbody')[0];
		var preparedDocCheckboxes = dojo.query('#<portlet:namespace/>_reportPreparedDocs input');
		
		var removedIds = new Array();
		
		if(preparedDocCheckboxes) {
			for(var i = 0; i < preparedDocCheckboxes.length; i++) {
				if(preparedDocCheckboxes[i].checked == true) {
					removedIds.push(preparedDocCheckboxes[i].value);	
				}
			}
		}	
		
		return removedIds;
	}
	
	function <portlet:namespace/>_clearPreparedDocsTable(cardsToRemove) {
		var preparedDocsTable = dojo.byId('<portlet:namespace/>_reportPreparedDocs');
		
		if (preparedDocsTable.rows) {
			for (var i = preparedDocsTable.rows.length - 1; i >= 0; --i) {
				for(var j in cardsToRemove) {
					cardId = dojo.query("input", preparedDocsTable.rows[i])[0].value;
					if(cardsToRemove[j] == cardId) {
						preparedDocsTable.deleteRow(i);	
					}
				}
			}
		}
	}
	
	function <portlet:namespace/>_removePreparedDocs() {
		var preparedDocsTableBody = dojo.query('#<portlet:namespace/>_reportPreparedDocs tbody')[0];
		var  preparedDocsCheckboxes = dojo.query('#<portlet:namespace/>_reportPreparedDocs input');
		
		if( preparedDocsCheckboxes) {
			for(var i = 0; i <  preparedDocsCheckboxes.length; i++) {
				if( preparedDocsCheckboxes[i].checked == true) {
					 preparedDocsTableBody.removeChild(preparedDocsCheckboxes[i].parentNode.parentNode);	
				}
			}
		}	
	}

	function <portlet:namespace/>_collectItems(form, tableId, name) {
		var attachmentCheckboxes = dojo.query('#' + tableId + ' input');
		
		if(attachmentCheckboxes) {
			for(var i = 0; i < attachmentCheckboxes.length; i++) {
				input = document.createElement('input');
				input.type = 'hidden';
				input.name = name;
				input.value = attachmentCheckboxes[i].value; 
				
				form.appendChild(input);
			}
		}	
	}

	function <portlet:namespace/>_submitForm(action) {
		lockScreen();
		var form = dojo.byId('<portlet:namespace/>_form');
		<portlet:namespace/>_collectItems(form, '<portlet:namespace/>_reportAttachments', 'materialId');
		<portlet:namespace/>_collectItems(form, '<portlet:namespace/>_reportPreparedDocs', 'preparedDocId');
		<portlet:namespace/>_prepareReportText(form, '<portlet:namespace/>_reportTextarea');
		dojo.byId('<portlet:namespace/>_action').value = action;
		dojo.byId('<portlet:namespace/>_pressedBtn').value = <portlet:namespace/>_buttonPressed;
		
		form.submit();
	}

	function <portlet:namespace/>_addDocument() {
		cardLinkPickerDisplayDialog('documentPickerAttrHtmlId', 'JBR_DOCL_RELATDOC', 'Связанные документы', true, true); 
	}

	function <portlet:namespace/>_doCancel() {
		<portlet:namespace/>_buttonPressed = 'Exit';
		
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		if(filePathInput.value.length > 0) {
			dijit.byId('<portlet:namespace/>_skipAttachmentUploadDialog').show();
			dijit.byId('<portlet:namespace/>_skipYesButton').focus();
		} else {
			dijit.byId('<portlet:namespace/>_cancelConfirmationDialog').show();
			dijit.byId('<portlet:namespace/>_cancelYesButton').focus();
		}
	}

	function <portlet:namespace/>_doExecute() {
		var reportText = dojo.byId('<portlet:namespace/>_reportTextarea');
					
		if(!(reportText && reportText.value.length > 0)) {
			alert('<fmt:message key="msg.emptyReport"/>');
			return false;
		}		

		<portlet:namespace/>_buttonPressed = 'Execute';

		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		if(filePathInput.value.length > 0) {
			dijit.byId('<portlet:namespace/>_skipAttachmentUploadDialog').show();
			dijit.byId('<portlet:namespace/>_skipYesButton').focus();
		} else {
			dijit.byId('<portlet:namespace/>_executeConfirmationDialog').show();
			dijit.byId('<portlet:namespace/>_executeYesButton').focus();
		}
	}

	function <portlet:namespace/>_prepareReportText(form, reportTextareaId){
		// on this branch report text should be inserted in TextAttribute (not HtmlAttribute),
		// therefore converting it in xml commented
		
		//this.getDate = function(){
		//	var d = new Date();
		//	return d.getFullYear() + "-"+to2digits(d.getMonth()+1)+"-"+to2digits(d.getDate())+"T"+to2digits(d.getHours())+":"+to2digits(d.getMinutes())+":"+to2digits(d.getSeconds())
		//}
		
		var obj = dojo.byId(reportTextareaId);
		var text = obj.value;
		
		//var xml = '<?xml version="1.0" encoding="UTF-8"?><report>';
		var htmlText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
		//var newPart = '<part timestamp="'+this.getDate()+'">'+htmlText+"</part>";
			
		//reportText = xml.split('</report>')[0] + newPart + '</report>'; 

		input = document.createElement('input');
		input.type = 'hidden';
		input.name = 'reportText';
		input.value = htmlText;//reportText
		form.appendChild(input);
	}

	function to2digits(v){
		return (v < 10) ? "0" + v : v;
	}

	function <portlet:namespace/>_uploadAttachment() {
		dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', true);    
		
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		var filePath = filePathInput.value;
		var uploadUrl = self.location.protocol + "//" + self.location.host + '<%=ResolutionReportPortlet.MATERIAL_UPLOAD_URL %>';

		var postResponse = document.applets[0].
								postMaterialCard(uploadUrl, document.cookie, filePath, "", null);
		
		if(!postResponse) {
			alert('<fmt:message key="error.material_download"/>');
			return;
		}

		var attachmentData = eval( "(" + postResponse + ")" );
		<portlet:namespace/>_addAttachment(dojo.byId('<portlet:namespace/>_reportAttachments'), 
				attachmentData.id, attachmentData.name);

		filePathInput.value = '';
	}

	function <portlet:namespace/>_selectFile() {
		dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', true);
        var fileName = document.applets[0].getFileName();
        dojo.byId('<portlet:namespace/>_filePath').value = fileName;   
        if(fileName != undefined && fileName != null && fileName != "") {
        dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', false);      
  	}
  	}

	function setBottomFrameHeight() {
		var pageContent = document.getElementById("<portlet:namespace/>_pageContent");
		if (pageContent) {
    		var contentHeight = document.body.clientHeight - 130;
    		pageContent.style.height = contentHeight + 'px';
    	}
	}

	function createNewDoc() {
		<portlet:namespace/>_buttonPressed = createDocBtnName;
		
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		if(filePathInput.value.length > 0) {
			dijit.byId('<portlet:namespace/>_skipAttachmentUploadDialog').show();
			dijit.byId('<portlet:namespace/>_skipYesButton').focus();
		} else {
			<portlet:namespace/>_submitForm('<%= ResolutionReportPortletAction.REDIRECT %>');
		}
	}
</script>

<c:set var="sessionBean" value="<%=sessionBean%>"/>
<c:set var="headerIcon" value="item2"/>

<%@include file="../docTitleTopHeader.jspf"%>

<div class="wizard">
<%	String message = sessionBean.getMessage();
	if (message != null) {
		sessionBean.setMessage(null);
%>
	<div><%=message%></div>
<%	}
%>

<applet name="CryptoApplet"	
	codebase="<%=request.getContextPath()%>"
	archive="SJBCrypto.jar" 
	code="com.aplana.crypto.CryptoApplet.class"	WIDTH=1	HEIGHT=1>
	<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
	<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
	<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">		
	<PARAM name="separate_jvm" value="true">
		<H1><fmt:message key="msg.warning"/></H1><fmt:message key="msg.cannot_load_applet"/>
</applet>

<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
<input type="hidden" id="<portlet:namespace/>_action"  name="<%= ResolutionReportPortlet.FIELD_ACTION %>" value=""/>
<input type="hidden" id="<portlet:namespace/>_pressedBtn"  name="<%= ResolutionReportPortlet.FIELD_PRESSED_BTN %>" value=""/>
<input type="hidden" name="<%= ResolutionReportPortlet.FIELD_NAMESPACE %>" value="<portlet:namespace/>"/>
<div id="<portlet:namespace/>_pageContent" style="overflow-y:auto;">
<div dojoType="dijit.layout.SplitContainer" 
		orientation="horizontal"
        sizerWidth="7"
        activeSizing="false"
        style="border: 1px solid #bfbfbf;
        margin-left: 20px; margin-right: 20px; height: 730px;overflow-y:auto; font-size: 70%; text-align: left;">
	<div id="<portlet:namespace/>_leftPane" dojoType="dijit.layout.ContentPane" sizeMin="100" sizeShare="10" style="overflow: hidden;">
		
		<div class="form_header">
			<h2><fmt:message key="header.reports_from_linked_resolutions"/></h2>
		</div>
		<span class="form_caption"><fmt:message key="header.groupexecution.left.selected.reports"/></span>
		<div style="height: auto;" class="contentDiv">
			<table id="<portlet:namespace/>_groupResolutions">
			</table>
		</div>
	</div>
	
	<div id="<portlet:namespace/>_rightPane" dojoType="dijit.layout.ContentPane" sizeMin="100" sizeShare="10" style="overflow: hidden;">
		
		<div style="height: 25%;">
			<div  class="form_header">
				<h2><fmt:message key="header.resolution_report"/></h2>
			</div>
			<textarea id="<portlet:namespace/>_reportTextarea" class="contentDiv" 
				style="width: 99%; height: 50%;">${requestScope.reportText}</textarea>
		</div>
		
		<div class="NoAppletHide">
			<div align="right" style="width: 100%">
				<table style="width: 100%;" align="right" cellspacing="0">
					<tr>
						<td style="width: 100%" align="right">
							<input readonly style="width: 100%" type="text" id="<portlet:namespace/>_filePath" >
						</td>
						<td align="right">
							<button dojoType="dijit.form.Button" type="button" >
								<fmt:message key="button.browse"/>
								<script type="dojo/method" event="onClick" args="evt">	
									<portlet:namespace/>_selectFile();
								</script>
							</button>	
						</td>	
					</tr>
				</table>	 
			</div>
			<div align="right">
				<button dojoType="dijit.form.Button" type="button" disabled id="<portlet:namespace/>_uploadButton">
					<fmt:message key="button.upload_attachment"/>
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_uploadAttachment();
					</script>
				</button>	
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="button.remove_attachment"/>
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_removeAttachments();
					</script>
				</button>	
			</div>
		</div>
		<span class="form_caption NoAppletHide"><fmt:message key="header.attachments"/></span>
		<div style="height: 11%;" class="contentDiv NoAppletHide">
			<table id="<portlet:namespace/>_reportAttachments"/>
			</table>
		</div>
		
		<div style="height: 3%;">&nbsp;</div>
		
		<div align="right">
			<button id="<portlet:namespace/>_CreateDocBtn" dojoType="dijit.form.Button" type="button">
				<fmt:message key="button.create_new_doc"/>
			</button>
			<button id="<portlet:namespace/>_AddLinkBtn" dojoType="dijit.form.Button" type="button">
				<fmt:message key="button.add_link"/>
			</button>
			<button id="<portlet:namespace/>_RemoveLinkBtn" dojoType="dijit.form.Button" type="button">
				<fmt:message key="button.remove_document"/>
			</button>
		</div>
		<span class="form_caption"><fmt:message key="header.prepared_documents"/></span>
		
		<div style="height: 10%;" class="contentDiv">
			<jsp:include page="documentPicker.jsp"/>
			<table id="<portlet:namespace/>_reportPreparedDocs"/>
			</table>
		</div>
	</div>  
<div style="height: 20px;">&nbsp;</div>
</div>       
  
<div class="tools">
	<div class="corn" style="float: right;">
		<div style="margin: 20px 10px; display: inline-block;">
			<a href="#" onclick="<portlet:namespace/>_doExecute()" class="send_to_approvement"></a>
		</div>
		<div style="display: inline-block; margin: 20px 20px 20px 10px;">
			<a href="#" onclick="<portlet:namespace/>_doCancel()" class="close"></a>
		</div>
	</div>
</div>

<div id="<portlet:namespace/>_cancelConfirmationDialog" dojoType="dijit.Dialog" 
		title="<fmt:message key="header.confirmation"/>" 
		style="width: 320px; height: 105px;font-size: 70%; text-align: left;">
	<div style="text-align: left;"><fmt:message key="msg.save_changes"/></div>
	<div style="float:right; clear: both;">
		<button dojoType="dijit.form.Button" type="button" id="<portlet:namespace/>_cancelYesButton">
			<fmt:message key="button.yes"/>
			<script type="dojo/method" event="onClick" args="evt">	
				dijit.byId('<portlet:namespace/>_cancelConfirmationDialog').hide();
				<portlet:namespace/>_submitForm('<%= ResolutionReportPortletAction.GROUP_SAVE %>');
			</script>
		</button>
		<button dojoType="dijit.form.Button" type="button">
			<fmt:message key="button.no"/>
		    <script type="dojo/method" event="onClick" args="evt">
				dijit.byId('<portlet:namespace/>_cancelConfirmationDialog').hide();
				<portlet:namespace/>_submitForm('<%= ResolutionReportPortletAction.CANCEL %>');
			</script>
		</button>
		<button dojoType="dijit.form.Button" type="button">
			<fmt:message key="button.cancel"/>
			<script type="dojo/method" event="onClick" args="evt">
				dijit.byId('<portlet:namespace/>_cancelConfirmationDialog').hide();
			</script>
		</button>			
	</div>
</div>

<div id="<portlet:namespace/>_executeConfirmationDialog" dojoType="dijit.Dialog" 
		title="<fmt:message key="header.confirmation"/>" 
		style="width: 320px; height: 135px;font-size: 70%; text-align: left;">
	<div style="text-align: left;"><fmt:message key="dialog.title.group.execution"/></div>
	<div style="float:right; clear: both;">
		<button dojoType="dijit.form.Button" type="button" id="<portlet:namespace/>_executeYesButton">
			<fmt:message key="button.yes"/>
			<script type="dojo/method" event="onClick" args="evt">	
				dijit.byId('<portlet:namespace/>_executeConfirmationDialog').hide();
				<portlet:namespace/>_submitForm('<%= ResolutionReportPortletAction.GROUP_EXECUTE %>');
			</script>		
		</button>
		<button dojoType="dijit.form.Button" type="button">
			<fmt:message key="button.no"/>
		    <script type="dojo/method" event="onClick" args="evt">
				dijit.byId('<portlet:namespace/>_executeConfirmationDialog').hide();
			</script>
		</button>		
	</div>
</div>

<div id="<portlet:namespace/>_skipAttachmentUploadDialog" dojoType="dijit.Dialog" 
		title="<fmt:message key="header.confirmation"/>" 
		style="width: 320px; height: 135px;font-size: 70%; text-align: left;">
	<div style="text-align: left;"><fmt:message key="msg.attachment_upload"/></div>
	<div style="float:right; clear: both;">
		<button dojoType="dijit.form.Button" type="button" id="<portlet:namespace/>_skipYesButton">
			<fmt:message key="button.yes"/>
			<script type="dojo/method" event="onClick" args="evt">	
				dijit.byId('<portlet:namespace/>_skipAttachmentUploadDialog').hide();

				if(<portlet:namespace/>_buttonPressed == 'Exit' || <portlet:namespace/>_buttonPressed == createDocBtnName) {
					<portlet:namespace/>_submitForm('<%= ResolutionReportPortletAction.REDIRECT %>');
				} else if(<portlet:namespace/>_buttonPressed == 'Execute') {
					dijit.byId('<portlet:namespace/>_executeConfirmationDialog').show();
					dijit.byId('<portlet:namespace/>_executeYesButton').focus();
				}
			</script>		
		</button>
		<button dojoType="dijit.form.Button" type="button">
			<fmt:message key="button.no"/>
		    <script type="dojo/method" event="onClick" args="evt">
				dijit.byId('<portlet:namespace/>_skipAttachmentUploadDialog').hide();
			</script>
		</button>		
	</div>
</div>

</div> <!-- end of '<portlet:namespace/>_pageContent' div -->

</form>
</div>
