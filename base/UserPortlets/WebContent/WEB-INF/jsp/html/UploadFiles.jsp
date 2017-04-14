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
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.card.actionhandler.jbr.UploadFilesForm"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.crypto.*"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.crypto.CryptoApplet"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<script type="text/javascript">
	function f5press(e) {
		//запрещаем нажатие F5 и Ctrl+r 
		if(e.keyCode == 116 || (e.keyCode == 82 && e.ctrlKey)) {
			return false;
		}
	}

	window.onkeydown = function (event) { 
		return f5press(event);
	}
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>

<%
PortletURL backURL = renderResponse.createActionURL();
backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION); 
backURL.setWindowState(WindowState.NORMAL);

CardLinkAttribute attr = (CardLinkAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().
									getAttribute(CardPortlet.SESSION_BEAN);
boolean isDsSupport = sessionBean.isDsSupport(renderRequest);

List<String> namesFiles = (List<String>)UploadFilesForm.getConfigParameter(
		UploadFilesForm.ATTR_KEY_NAMES_FILES, attr.getId(), sessionBean);
request.setAttribute("files", namesFiles);

CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());

String displaySelectedArea;
String displayResultArea;
String mode = (String)UploadFilesForm.getConfigParameter(
		UploadFilesForm.ATTR_KEY_MODE, attr.getId(), sessionBean);
Boolean uploaded;
if (UploadFilesForm.MODE_SELECTED.equals(mode)) {
	displaySelectedArea = "block";
	displayResultArea = "none";
	uploaded = false;
} else {
	displaySelectedArea = "none";
	displayResultArea = "block";
	uploaded = true;
}

String message = sessionBean.getMessage();
if( message != null) {
	sessionBean.setMessage(null);
} else {
	message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
}
%>
	<dbmi:message text="<%= message %>"/>

<script type="text/javascript" language="javascript">
var trusted = false;

function backlink(elem, url) {
	trusted = true;
	elem.onclick = function() { return false }
	window.location.href = url;
}
</script>

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
								<table id="filesTable" style="width: 470px;" class="tableUploadFile">
										<tr>
											<td colspan="3" class="rowHeadUploadFile"><h3><fmt:message key="form.upload.file.selected"/></h3></td>
										</tr>
										<tr>
											<th><h3><fmt:message key="form.upload.file.path"/></h3></th>
											<th><h3><fmt:message key="form.upload.file.materialName"/></h3></th>
											<th><h3><fmt:message key="form.upload.file.isPrime"/></h3></th>
										</tr>
								</table>
							</form>
						</td>
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
										<a href="#" class=""><fmt:message key="form.upload.file.upload"/></a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</table>
			</div>
			<div id="resultArea" style="display: <%= displayResultArea %>;">
				<table>
					<tr>
						<td>
							<table id="resultTable" width="320px"  class="tableUploadFile">
								<thead>
									<col width="20px"/>
									<col width="280px"/>
								</thead>
								<tbody>
									<tr>
										<td colspan="2" class="rowHeadUploadFile"><h3><fmt:message key="form.upload.file.result"/></h3></td>
									</tr>
									<c:forEach items="${files}" var="file">
										<tr>
											<td class="rowUploadFile"><split class="upload_ok"/></td><td class="rowUploadFile">${file}</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</td>
					</tr>
					<tr>
						<td>
							<div class="buttonPanel uploadButtonPane">
								<ul>
									<li class=""
										onmouseout="upButton(this)" 
										onmouseup="upButton(this)" 
										onmousedown="downButton(this)" 
										onclick="switchModeSelected()">
										<a href="#" class=""><fmt:message key="form.upload.file.newUpload"/></a>
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
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js?open&4" ></script>
	<script>
				dojo.require('dijit.form.Button');
				dojo.require('dijit.form.DropDownButton');
				dojo.require('dijit.Menu');	
				dojo.require('dijit.Dialog');	
		</script>
		<div id="attachDsQuery" dojoType="dijit.Dialog"
		title="<fmt:message key="upload.apply.card.ds.query.title"/>"
		style="width: 320px; height: 96px">
			<div style="text-align: left;">
				<fmt:message key="change.state.attach.ds.query.message" />
			</div>
			<div style="float: right; clear: both;" id="dialogButtons">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.yes" />
					<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('attachDsQuery').hide();
					if(<%=isDsSupport%> && checkPermission&&signCard(false)){
						trusted = true;
						document.uploadForm.submit();
					} else {
						dijit.byId('doWithOutDsQuery').show();
					}
				</script>
				</button>
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.no" />
					<script type="dojo/method" event="onClick" args="evt">
						trusted = true;
						document.uploadForm.action=document.uploadForm.action + '&desable_ds=true';
						document.uploadForm.submit();
						dijit.byId('attachDsQuery').hide();
				</script>
				</button>
			</div>
		</div>
		
		<div id="doWithOutDsQuery" dojoType="dijit.Dialog"
		title="<fmt:message key="upload.apply.card.ds.query.title"/>"
		style="width: 320px; height: 110px">
			<div style="text-align: left;">
				<fmt:message key="attach.file.without.ds.query.message" />
			</div>
			<div style="float: right; clear: both;" id="dialogButtons">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.yes" />
					<script type="dojo/method" event="onClick" args="evt">
					trusted = true;
					document.uploadForm.action=document.uploadForm.action + '&desable_ds=true';
					document.uploadForm.submit();
					dijit.byId('doWithOutDsQuery').hide();		
				</script>
				</button>
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.no" />
					<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('doWithOutDsQuery').hide();
				</script>
				</button>
			</div>
		</div>
		
		<%if(isDsSupport){%>
		<applet name="CryptoApplet" id="CryptoApplet"
				codebase="<%=request.getContextPath()%>"
				archive="SJBCrypto.jar"
				code="com.aplana.crypto.CryptoApplet.class" WIDTH="1" HEIGHT="1">
				<param name="signOnLoad" value="true">
				<param name="crypto.layer"
					value="<%=CryptoLayer
										.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
				<param name="crypto.layer.params"
					value="<%=CryptoLayer
										.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
				<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">
				<param name="<%=CryptoApplet.CURENT_USER_PARAMETER %>" value="<%=sessionBean.getServiceBean().getPerson().getId().getId().toString()%>">
				<PARAM name="separate_jvm" value="true">
				<H1>WARNING!</H1>
				The browser you are using is unable to load Java Applets!
		</applet>
		<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/crypto.js?open&4"></script>
	    <script>		
				function signCard(submit){
					try{
						prepareSignParams(submit);
						if(window.signParams){
							var stringsArray = eval("[" + window.signParams[0]+ "]");
							var stringsArrayHash = eval("[" + window.signParams[1]+ "]");
							var signAttrXML = eval("[" + window.signParams[2]+ "]");
							var currentSignature = eval("[" + window.signParams[3]+ "]");
							var ids = eval("[" + window.signParams[4]+ "]");
							
							var args = {
								stringsArrayData: stringsArray,
								stringsArrayHash: stringsArrayHash,
								signAttrXML: signAttrXML,
								currentSignature: currentSignature,
								ids: ids
							};
			
							var msg = "";
							var signResult = cryptoGetSignature(args);  
							if(signResult.success){			
								if(submit){
									submitForm_SignCard(signResult.signature, args);
									}
								else{
									return true;
								}	
							} else{
								if(signResult.msg == "noapplet"){			
									msg = "Апплет не инициализирован"
								} else if(signResult.msg == "nofields"){
									msg = "Нет подписываемых аттрибутов";
								} else {
									msg = signResult.msg;
								}
								if(msg && msg.length > 0 && submit) {
									alert(msg);
								}	
								return false;	
							}
						}
					}
					catch (err){
						return false;
					}
				}
				function prepareSignParams(submit){
					window.signParams=[];
					window.signParams[0]="\"Z2ZoZmdoZGZn\"";
					window.signParams[1]="\"WjJab1ptZG9aR1pu\"";
					window.signParams[2]="'<card id=\"0\"><attr type=\"S\" id=\"JBR_INFD_SHORTDESC\"/></card id=\"0\">'";
					window.signParams[3]="''";
					window.signParams[4]="\"12273535\"";
				}


			</script>
		<%}


	String needSign = request.getParameter("ds.need.sign");
	boolean attach_ds = ((needSign != null) && (needSign.length()>0));
	String showAlert = request.getParameter("showAlert");
	ObjectId materialsAttributeId = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	ObjectId attrDocAttach = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	ObjectId attrName = ObjectId.predefined(StringAttribute.class, "name");
	CardLinkAttribute materialsAttribute = (CardLinkAttribute) sessionBean.getActiveCard().getAttributeById(materialsAttributeId);
	ObjectId signatureAttributeId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
	HtmlAttribute signatureAttribute = (HtmlAttribute) sessionBean.getActiveCard().getAttributeById(signatureAttributeId);	

	if (attach_ds){
		ArrayList attInfo = new ArrayList();
		String[] attachForSign = needSign.split(";");
		
		for (int i=0; i<attachForSign.length; i++){
		
			AttachInfo attInfoObject= new AttachInfo();
			ObjectId attachId = new ObjectId(Card.class, Integer.parseInt(attachForSign[i]));
	  		Card attachmentCard = (Card) sessionBean.getServiceBean().getById(attachId);
	  		String attachName = ((StringAttribute) attachmentCard.getAttributeById(attrName)).getValue();
	  			
	  		SignatureConfig sConf = new SignatureConfig(sessionBean.getServiceBean(), attachmentCard);
	  		SignatureData sData = new SignatureData(sConf, attachmentCard);
	  		
	  		attInfoObject.setAttachText(attachName);
	  		attInfoObject.setAttId(attachId.getId().toString());
	  		attInfoObject.setAttrXML(sData.getAttrXML());
	  		attInfoObject.setHash(sData.getAttrValues(sessionBean.getServiceBean(), true, null));
	  		attInfoObject.setData(sData.getAttrValues(sessionBean.getServiceBean(), false,  renderResponse.encodeURL(renderRequest.getContextPath()  + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "=")));
	  				
	  		attInfo.add(attInfoObject);  			
  		}
		
%>

		<form name="signForm" method="post" action="<portlet:actionURL/>"> 
			<input id="signature" type="hidden" name="signature"/>
			<input id="signCards" type="hidden" name="signCards" value="<%=needSign%>"/>
			<input id="<%=CardPortlet.ACTION_FIELD%>" type="hidden" name="<%=CardPortlet.ACTION_FIELD%>"/>
		</form>

		<script>	
			function signDocument(){
				var ids = [];
				var hashesToSign = [];
				var datasToSign = [];
				var attrXmls = []; 
				

			<%	
			if(attInfo.size() > 0) {
				StringBuffer ids = new StringBuffer("["); 
				StringBuffer hashesToSign = new StringBuffer("[");
				StringBuffer datasToSign = new StringBuffer("[");
				StringBuffer attrXmls = new StringBuffer("[");
				for(int i = 0; i < attInfo.size(); i ++) {
					AttachInfo attachment = (AttachInfo) attInfo.get(i);
					if(i > 0) {
						ids.append(",");
						hashesToSign.append(",");	
						datasToSign.append(",");	
						attrXmls.append(",");
					}
					ids.append("\"").append(attachment.getAttId()).append("\"");
					hashesToSign.append("\"").append(attachment.getHash()).append("\"");
					datasToSign.append("\"").append(attachment.getData()).append("\"");
					attrXmls.append("'").append(attachment.getAttrXML()).append("'");					
				}
				ids.append("]");
				hashesToSign.append("]");
				datasToSign.append("]");
				attrXmls.append("]");
			%>	
				ids = <%=ids%>;
				hashesToSign = <%=hashesToSign%>;
				datasToSign = <%=datasToSign%>;
				attrXmls = <%=attrXmls%>;
			<%
			}
			%>
				
				var args = {
						stringsArrayHash: hashesToSign, 
						stringsArrayData: datasToSign, 
			   			signAttrXML: attrXmls
			   	   		};
				var msg = "";
				var signResult = cryptoGetSignature(args);

				if(signResult.success){	
					var signs = "";
					if(ids.length > 0) {
						for(i = 0; i < ids.length; i ++) {
							if (i > 0){
								signs += ";";
							}
							signs += signResult.signature[i];
						}
					}		
							
					submitForm_SignCard(signs);
				} else{
					if(signResult.msg == "noapplet"){			
						msg = "Апплет не инициализирован"
					} else if(signResult.msg == "nofields"){
						msg = "Нет подписываемых аттрибутов";
					} else {
						msg = signResult.msg;
					}
					if(msg && msg.length > 0) {
						alert(msg);
					}		
				}
			}	

			function submitForm_SignCard(value) {	
				trusted = true;
				document.signForm.signature.value = value;
				document.signForm.submit();
			}

			function submitForm_CloseCard(value) {
				trusted = true;
				document.signForm.<%=CardPortlet.ACTION_FIELD%>.value = value;
				document.signForm.submit();
			}
				
		</script>

	<script type="text/javascript" language="javascript">
	dojo.addOnLoad(function() {
		dbmiHideLoadingSplash();
	});
	</script>	

<%
	} else if (!UploadFilesForm.MODE_SELECTED.equals(mode)){%>
		<form name="signForm" method="post" action="<portlet:actionURL/>"> 
			<input id="<%=CardPortlet.ACTION_FIELD%>" type="hidden" name="<%=CardPortlet.ACTION_FIELD%>"/>
		</form>
		<script type="text/javascript">
			trusted = true;
			document.signForm.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.BACK_ACTION%>';
			document.signForm.submit();
		</script>
<!--

//-->
</script>
<%	}
%>

<% 	//Разблокируем открытые на редактирование карточки при выходе пользователя
	//карточек открытых на редактировании может быть несколько, собирем их айди через запятую
	String lockedCardId = "";
	int cnt = 0;
	List<CardPortletCardInfo> listCards = sessionBean.getAllOpenedActiveCards();
	if (!listCards.isEmpty()) {
		cnt = listCards.size();
		for (CardPortletCardInfo info : listCards) {
			lockedCardId += info.getCard().getId().getId().toString() + ",";
		}
		//обрезаем последнюю запятую
		lockedCardId = lockedCardId.substring(0, lockedCardId.length()-1);
	}
%>

<script type="text/javascript" language="javascript">
dojo.require('dijit.Dialog');
window.onbeforeunload = function(evt) {
	if (!trusted && <%= !lockedCardId.isEmpty() %>) {
		return "<fmt:message key="edit.warning.browser.away"><fmt:param value="<%= lockedCardId %>"/></fmt:message>";
	}
}
window.onunload = function(e) {
	if (!trusted && <%= !lockedCardId.isEmpty() %>) {
		var result = dojo.xhrGet({
			url: '<%=request.getContextPath()%>/UnlockCard?id=<%=lockedCardId.split(",")[0]%>',
			sync: true,
			preventCache: true,
			//timeout: 5000,
			load: function() {
				console.log('Unlocked successfully');
				window.location.pathname = '/portal/signout/'
			}
		});
	}
}
</script>

	
	<script>
		function signAction(){
			if(<%=attach_ds%>){
				signDocument();
			}
		}
		dojo.addOnLoad(function() {
			addRow();
		});
		function showAttachDsDialog(){
			if(<%=isDsSupport%>){
				dijit.byId('attachDsQuery').show();
			} else {
				trusted = true;
				document.uploadForm.submit();
			}
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
			cell.innerHTML ='<input type="file" name="<%=UploadFilesForm.FILENAME_PARAM%>"/>';
			var input = cell.childNodes[0];

			cell = row.insertCell(1);
			cell.innerHTML = '<input type="text" name="<%=UploadFilesForm.MATERIALNAME_PARAM%>" style="visibility:hidden;"/>';

			cell = row.insertCell(2);
			cell.innerHTML = '<input type="radio" name="<%=UploadFilesForm.PRIMACY_PARAM%>" style="visibility:hidden;"/>';

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
				<% if(attr.isMultiValued()) { %>
				if (lastRow.cells[0].childNodes[0].value != "")
					addRow();
				<%}%>
			}
			
			var handlerDelete = function deleteRow() {
				table.deleteRow(row.rowIndex);
				<% if(!attr.isMultiValued()) { %>
					addRow();
				<%}%>
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