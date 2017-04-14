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
<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*" %>
<%@page import="java.text.MessageFormat" %>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.portlet.*"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.crypto.*"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.CardImportPortlet" scope="request"/>

<%
CardImportPortletSessionBean sessionBean = (CardImportPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardImportPortlet.SESSION_BEAN);
boolean updateDoubletsSupport = sessionBean.isSupportUpdateExistsDoublets();
boolean checkForUpdateDoubletsSupport = sessionBean.isSupportCheckForExistsDoublets();
boolean сheckForExistsDoublets = sessionBean.isCheckForExistsDoublets();
boolean updateExistsDoublets = sessionBean.isUpdateExistsDoublets();
ResourceBundle bundle = ResourceBundle.getBundle("com.aplana.dbmi.portlet.nl.CardImportPortlet", request.getLocale());
if (sessionBean==null){
	PortletURL backURL = renderResponse.createActionURL();
	backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION);
	backURL.setWindowState(WindowState.NORMAL);
	String undefinedParameters = bundle.getString("form.upload.input.parameters.notFind");
%>

	<table>
	<tr>
		<td style="padding-bottom: 20px;">
			<div class="buttonPanel">
				<ul>
					<li class="back"
						onmousedown="downBackBut(this)" 
						onmouseup="upBackButton(this)" 
						onmouseover="overBackButton(this)" 
						onmouseout="upBackButton(this)">
						<a onclick="backlink(this, '<%= backURL.toString() %>');" href="#" >
							<div class="ico_back img">&nbsp;</div>
							<p><fmt:message key="view.page.back.link" /></p> 
						</a>
					</li>	
				</ul>
			</div>
		</td>
	</tr>
	<tr>
		<td colspan="3" class="rowHeadUploadFile"><h3><%= undefinedParameters%></h3></td>
	</tr>
	</table>
<%
} else {
	Template inputTemplate = sessionBean.getTemplate();
	String objectName = sessionBean.getObjectName();
	String startMessage = MessageFormat.format(bundle.getString("form.upload.file.title"), objectName);
	String backURL;
	if (sessionBean!=null){
		backURL = sessionBean.getBackUrl();
	} else {
		backURL = "";
	}
	String displaySelectedArea;
	String displayResultArea;
	Boolean uploaded;
	displaySelectedArea = "block";
	displayResultArea = "none";
	uploaded = false;
	
	String message = sessionBean.getMessage();
	int lineCount = 0;
	if (message!=null&&!message.isEmpty())
		lineCount = (message.split("<br>").length+1)*20+80;
	if (lineCount==0){
		lineCount = 100;
	}
/*if( message != null) {
	sessionBean.setMessage(null);
} else {
	message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
}*/
%>
	<c:set var="selectedVal"
		value="<%=CardImportPortlet.SELECTED_VAL%>" />
	<c:set var="unSelectedVal"
		value="<%=CardImportPortlet.UNSELECTED_VAL%>" />
	<c:set var="isDoubletsChecked" value="<%= сheckForExistsDoublets%>"/>
	<c:set var="isUpdateDoublets" value="<%= updateExistsDoublets%>"/>
	<c:set var="isSupportCheckUpdateElement" value="<%= checkForUpdateDoubletsSupport%>"/>
	<c:set var="isSupportUpdateElement" value="<%= updateDoubletsSupport%>"/>
	<c:set var="checkItemId1" value="isDoubletsChecked"/>
	<c:set var="checkItemId2" value="isUpdateDoublets"/>

<script type="text/javascript" language="javascript">
var trusted = false;

function backlink(elem, url) {
	trusted = true;
	elem.onclick = function() { return false }
	window.location.href = url;
}
dojo.require("dijit.form.Button"); 
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.Dialog");

 
function showLoadScreen() {
	dojo.require("dojo.NodeList-traverse");
	dojo.byId('dbmiLoadingSplash').style.display = 'block'; 
	dojo.byId('sizer').style.display = 'none';
	dojo.query('#dbmiLoadingSplash').children().children()[0].style.fontSize = '24px';
	dojo.query('#dbmiLoadingSplash').children().children()[0].innerHTML = '<%=startMessage%>';
}

// were used for checked buttons 
function onToggleButtonClick(element, dependElement, hiddenElementName) {
	var _value = element.checked ? ${selectedVal} : ${unSelectedVal};
	<% // ToDo: пока непонятно стоит ли насовсем убирать зависимость активности второй галочки от первой, поэтому пока просто отключаем 
	if (/*updateDoubletsSupport*/false){ %>
	if (dependElement!=null){
		dojo.byId(dependElement).disabled = !element.checked; 
		dojo.byId(dependElement).enabled = element.checked;
		var label = dojo.byId(dependElement+'_label');
		if (label!=null){
			if (_value==${selectedVal}){
				label.style.color = "black"
			} else {
				label.style.color = "gray"
			}
		}
	}
	<% }%>
	dojo.byId(hiddenElementName).value = _value;
}


</script>
	<!-- окошко с предупреждением-->
	<div id="noSuchMethod" dojoType="dijit.Dialog" title="<fmt:message key="form.upload.warning.dialog.title"/>" style="width: 320px; height: <%= lineCount%>px">
		<div style="text-align: left;"><%=message %></div>	
		
		<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="print.envelope.message.ok"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('noSuchMethod').hide();					
				</script>
			</button>			
	</div>

	<table>
	<tr>
		<td style="padding-bottom: 20px;">
			<div class="buttonPanel">
				<ul>
					<li class="back"
						onmousedown="downBackBut(this)" 
						onmouseup="upBackButton(this)" 
						onmouseover="overBackButton(this)" 
						onmouseout="upBackButton(this)">
						<a onclick="backlink(this, '<%= backURL.toString() %>');" href="#" >
							<div class="ico_back img">&nbsp;</div>
							<p><fmt:message key="view.page.back.link" /></p> 
						</a>
					</li>	
				</ul>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<div id="selectedArea" style="display: <%=displaySelectedArea %>;">
				<table>
					<tr>
						<td>
							<form name="uploadForm" method="post" action="<portlet:actionURL/>" enctype="multipart/form-data" > 
								<table id="filesTable" style="width: 800px;" class="tableUploadFile">
										<tr>
											<td colspan="3" class="rowHeadUploadFile"><h3><%= startMessage%></h3></td>
										</tr>
										<tr>
											<td colspan="3" class="rowHeadUploadFile"><h3><fmt:message key="form.upload.file.selected"/></h3></td>
										</tr>
										<tr>
											<th><h3><fmt:message key="form.upload.file.path"/></h3></th>
										</tr>
								</table>
						 		<c:choose>
						 			<c:when test="${isDoubletsChecked}">
						 				<input type="hidden" class="attrInteger"	name="${checkItemId1}_value"
								 			id="${checkItemId1}_value"	value="${selectedVal}"/>
						 			</c:when>
						 			<c:otherwise>
										<input type="hidden" class="attrInteger"	name="${checkItemId1}_value"
								 			id="${checkItemId1}_value"	value="${unSelectedVal}"/>
						 			</c:otherwise>
						 		</c:choose>
						 		<c:choose>
						 			<c:when test="${isUpdateDoublets}">
						 				<input type="hidden" class="attrInteger"	name="${checkItemId2}_value"
								 			id="${checkItemId2}_value"	value="${selectedVal}"/>
						 			</c:when>
						 			<c:otherwise>
										<input type="hidden" class="attrInteger"	name="${checkItemId2}_value"
								 			id="${checkItemId2}_value"	value="${unSelectedVal}"/>
						 			</c:otherwise>
						 		</c:choose>
							</form>
						</td>
					</tr>
					<tr>
						<td><br></td>
					</tr>
					<tr>
						<td>
						<c:if test="${isSupportCheckUpdateElement}">
				 		<c:choose>
				 			<c:when test="${isDoubletsChecked}">
				 				<input onclick="onToggleButtonClick(this, '${checkItemId2}_button', '${checkItemId1}_value')" dojoType="dijit.form.CheckBox" 
				 							checked id="${checkItemId1}_button" name="${checkItemId1}_button" on/>
				 				<label id="${checkItemId1}_button_label" for="${checkItemId1}_button"><fmt:message key="form.check.for.doublets"/></label>			
				 			</c:when>
				 			<c:otherwise>
				 				<input onclick="onToggleButtonClick(this, '${checkItemId2}_button', '${checkItemId1}_value')" dojoType="dijit.form.CheckBox" 
				 							id="${checkItemId1}_button" name="${checkItemId1}_button"/>
						 		<label id="${checkItemId1}_button_label" for="${checkItemId1}_button"><fmt:message key="form.check.for.doublets"/></label>	
				 			</c:otherwise>
				 		</c:choose>
				 		</c:if>
						</td>
					</tr>
					<tr>
						<td>
						<c:if test="${isSupportUpdateElement}">
				 		<c:choose>
				 			<c:when test="${isUpdateDoublets}">
				 				<input onclick="onToggleButtonClick(this, null, '${checkItemId2}_value')" dojoType="dijit.form.CheckBox" 
				 							checked id="${checkItemId2}_button" name="${checkItemId2}_button"/>
				 				<label id="${checkItemId2}_button_label" for="${checkItemId2}_button"><fmt:message key="form.update.doublets"/></label>			
				 			</c:when>
				 			<c:otherwise>
				 				<input onclick="onToggleButtonClick(this, null, '${checkItemId2}_value')" dojoType="dijit.form.CheckBox" 
				 							id="${checkItemId2}_button" name="${checkItemId2}_button"/>
						 		<label id="${checkItemId2}_button_label" for="${checkItemId2}_button"><fmt:message key="form.update.doublets"/></label>	
				 			</c:otherwise>
				 		</c:choose>
						</c:if>
						</td>
					</tr>
					<tr>
						<td><br></td>
					</tr>
					<tr>
						<td>
							<div class="buttonPanel uploadButtonPane">
								<ul>
									<li class=""
										onmouseout="upButton(this)" 
										onmouseup="upButton(this)" 
										onmousedown="downButton(this)" 
										onclick="showAttachDsDialog()">
										<a href="#" class=""><fmt:message key="form.upload.start.import"/></a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</td>
	</tr>
	</table>
	<script>
		dojo.require('dijit.form.Button');
		dojo.require('dijit.form.DropDownButton');
		dojo.require('dijit.Menu');
		dojo.require('dijit.Dialog');
	</script>
		
	<script type="text/javascript" language="javascript">
	dojo.addOnLoad(function() {
		dbmiHideLoadingSplash();
		<% // ToDo: пока непонятно стоит ли насовсем убирать возможность отключать вторую галочку при сохранении её видимости 
		if (false/*(!updateDoubletsSupport&&checkForUpdateDoubletsSupport)||!sessionBean.isCheckForExistsDoublets()*/){ %>
		dojo.byId('${checkItemId2}_button').disabled = true; 
		dojo.byId('${checkItemId2}_button').enabled = false;
		var label = dojo.byId('${checkItemId2}_button_label');
		if (label!=null){
			label.style.color = "gray"
		}
		<% }%>
	});
	</script>	

	<c:if test="<%= renderRequest.getParameter(CardImportPortlet.SHOW_WARNING_MESSAGE) != null%>">
		<script type="text/javascript">
			dojo.addOnLoad(function(){
	      		dijit.byId('noSuchMethod').show();
	  		});
		</script>
	</c:if>   
	
	<script>
		dojo.addOnLoad(function() {
			addRow();
		});

		function showAttachDsDialog(){
			showLoadScreen()
			trusted = true;
			document.uploadForm.submit();
		}

		function submitUploadForm() {
			trusted = true;
			document.uploadForm.submit();
		}

		function addRow() {
			var table = dojo.byId('filesTable');
			var row = table.insertRow(table.rows.length);
			var cell = null;
			cell = row.insertCell(0);
			cell.className = 'rowUploadFile';
			cell.innerHTML ='<input type="file" name="<%=CardImportPortlet.FILENAME_PARAM%>"/>';
			var input = cell.childNodes[0];

			cell = row.insertCell(1);
			cell.innerHTML = '<input type="text" name="<%=CardImportPortlet.MATERIALNAME_PARAM%>" style="visibility:hidden;"/>';

			cell = row.insertCell(3);
			cell.className = 'rowUploadFile';
			cell.innerHTML = '<a href="#" class="delete" style="visibility:hidden;">&nbsp;</a>';

			
			var handlerSelected = function selectedFile() {
				var isValid=(function(){
					// forbidden characters \ / : * ? " < > |
					var rg1=/^[^\\/:\*\?"<>\|]+$/;
					// cannot start with dot (.)
					var rg2=/^\./;
					// forbidden file names
					var rg3=/^(nul|prn|con|lpt[0-9]|com[0-9])(\.|$)/i;
					return function isValid(fname){
						return rg1.test(fname)&&!rg2.test(fname)&&!rg3.test(fname);
					}
				})();

				if (!isValid(/[^/\\]*$/.exec(this.value)[0])) {
					alert ('<fmt:message key="upload.page.invalid.file.name"></fmt:message>');
					row.cells[0].childNodes[0].value ="";
					for(var i = 1; i < row.cells.length; i++){
						row.cells[i].childNodes[0].style.visibility = 'hidden';
					}
					return;
				}

				row.cells[1].childNodes[0].value = /[^/\\]*$/.exec(this.value)[0];
				for(var i = 1; i < row.cells.length; i++){
					row.cells[i].childNodes[0].style.visibility = 'visible';
				}
				var table = dojo.byId('filesTable');
				var lastRow = table.rows[table.rows.length - 1];
				/*if (lastRow.cells[0].childNodes[0].value != "")
					addRow();*/
			}
			
			var handlerDelete = function deleteRow() {
				table.deleteRow(row.rowIndex);
			}
			
			input.onchange = handlerSelected;

			row.cells[row.cells.length - 1].childNodes[0].onclick = handlerDelete;
		}
		
		function switchModeSelected() {
			var selectedArea = dojo.byId('selectedArea')
			selectedArea.style.display = 'block'
			
			var resultArea = dojo.byId('resultArea')
			resultArea.style.display = 'none'
		}
	</script>
<% } 
%>