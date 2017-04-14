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
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortlet"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.ajax.QuickResolutionSearchPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>

<%@page import="java.lang.Boolean"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean"%>
<%@page import="com.aplana.dbmi.service.PortletUtil"%>
<%@page import="com.aplana.util.DigitalSignatureUtil"%>
<%@page import="com.aplana.dbmi.service.DataServiceBean"%>
<%@page import="com.aplana.dbmi.model.WorkflowMove"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>
<portlet:defineObjects/>
<%
	QuickResolutionPortletSessionBean sessionBean = 
		(QuickResolutionPortletSessionBean)renderRequest.getPortletSession().
		getAttribute(QuickResolutionPortlet.SESSION_BEAN);

	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());

	ArrayList<String> params =  (ArrayList<String>) renderRequest.getPortletSession().
			getAttribute(QuickResolutionPortlet.DS_PARAMS);
	renderRequest.getPortletSession().removeAttribute(QuickResolutionPortlet.APPLY_DS);
	renderRequest.getPortletSession().removeAttribute(QuickResolutionPortlet.DS_PARAMS);
	renderRequest.getPortletSession().setAttribute("userName", sessionBean.getCurrentPerson().getLogin(), 1);
	
	DataServiceBean serviceBean = PortletUtil.createService(renderRequest);
	boolean isDsSupport = DigitalSignatureUtil.isDsSupport(serviceBean);
	
	Integer applyDS = null;
	if(sessionBean.getWorkflowMoveId()!=null){
		applyDS = ((WorkflowMove)serviceBean.getById(sessionBean.getWorkflowMoveId())).getApplyDigitalSignatureOnMove();
	} else {
		applyDS = (Integer) renderRequest.getPortletSession().getAttribute(QuickResolutionPortlet.APPLY_DS);
	}
%>

<c:set var="sessionBean" value="<%=sessionBean%>"/>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>

<script type="text/javascript">

	
		dojo.require("dijit.form.Button");
		dojo.require("dijit.form.RadioButton");
		dojo.require("dojox.data.QueryReadStore");
		dojo.require("dijit.form.FilteringSelect");	
		dojo.require("dijit.form.DateTextBox");
	  	dojo.require("dijit.form.CheckBox");
	  	dojo.require("dojo.date");
  		dojo.require("dojo.date.stamp");
  		dojo.require("dijit.layout.BorderContainer");
  		dojo.require("dijit.layout.TabContainer");
  		dojo.require("dijit.layout.ContentPane");
	  	dojo.require("dojo.parser");
	  	
		function loadAttach(id){


			dojo.byId("AttachIframe").src='<%=request.getContextPath()%>/MaterialDownloadServlet?MI_CARD_ID_FIELD='+id+'&noname=1&pdf=1';
		}



		function getAttachFile(id){	
		   
		    var applet = dojo.byId("CryptoApplet");    
		    var obj = dojo.byId("PDFView");
		    
		    if(self.attachPath && self.attachPath != ""){
		    	obj.CloseAllDocuments()
		    }
		    
		    try{   
		    	var url =  self.location.protocol + "//" + self.location.host + getAttachUrl(id);
		        try{console.log(url)}catch(e){}
			    var path = applet.downloadAndSaveMaterial(url, document.cookie);
		        
		    }catch(ex){alert("Ошибка загрузки вложения. " + ex); return}
		    
		    if(path=="failure"){alert("Ошибка загрузки вложения. "); return}
		       
		    try{console.log(path)}catch(e){}
		    obj.SetDevInfo("PVA20-MU3MQ-ORO0E-IQIWU-HQ1XB-4V6HF","PDFX3$Henry$300604_Allnuts#");    
			obj.OpenDocument(path, 0, 0, 0);
			obj.SetProperty("International.LocaleID", 0, 0);
			obj.SetProperty('View.Bars[0].Visible', 1, 0); //меню
		    obj.SetProperty('View.Bars[3].Visible', 1, 0); //выделение
		    obj.SetProperty('View.Bars[4].Visible', 1, 0); //масштаб
		    obj.SetProperty('View.Bars[4].Visible', 1, 0); //масштаб
		    obj.SetProperty('View.Bars[6].Visible', 1, 0); //пометки
		    obj.SetDocumentProperty(0,'View.Bars[1].Visible', 1, 0); //страницы
		    obj.SetDocumentProperty(0,'View.Bars[2].Visible', 1, 0); //расположение  
		    obj.SetProperty('Documents[0].Pages.Zoom', 80, 0); //зум
		    self.attachPath = path;
		}

		//работа с аттачами через плагин
		function getAttachUrl(id){
			return "/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD="+id+"&noname=1&pdf=1";
		}
		
</script>


<c:set var="headerIcon" value="on_execution_by_subordinates"/>

<%@include file="../docTitleTopHeader.jspf"%>

<%@include file="../baseCardDataSection.jspf"%>
 

 
<applet id="CryptoApplet" name="CryptoApplet"	
	codebase="<%=request.getContextPath()%>"
	archive="SJBCrypto.jar"
	code="com.aplana.crypto.CryptoApplet.class"	WIDTH=1	HEIGHT=1>
	<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
	<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
	<param name="curent.user" value="<%=sessionBean.getCurrentPerson().getId().getId().toString()%>">
	<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">
	<PARAM name="separate_jvm" value="true">
	<H1><fmt:message key="msg.warning"/></H1><fmt:message key="msg.cannot_load_applet"/>
</applet>
<% 
if(applyDS != null && applyDS > 0 && params != null && !params.isEmpty()) {
%>
	<script>
			dojo.require('dijit.form.Button');
			dojo.require('dijit.Menu');	
			dojo.require('dijit.Dialog');			
			function signCard(){

				var args = {
					stringsArrayData: [<%=params.get(0)%>],
					stringsArrayHash: [<%=params.get(1)%>], 
			   		signAttrXML: [<%=params.get(2)%>],
			   		currentSignature: [<%=params.get(3)%>],
			   		ids: [<%=params.get(4)%>]
			   	};

				var msg = "";
				var signResult = cryptoGetSignature(args);  
				
				if(signResult.success){			
					submitForm_SignCard(signResult.signature, args);
				} else{
					if(signResult.msg == "noapplet"){			
						msg = "<fmt:message key="apply.card.ds.query.appletNotInitialized"/>"
					} else if(signResult.msg == "nofields"){
						msg = "<fmt:message key="apply.card.ds.query.nothingToSign"/>";
					} else {
						msg = signResult.msg;
					}
					if(msg && msg.length > 0) {
						alert(msg);
					}		
				}
			}	

			function submitForm_SignCard(value, args) {
				var result = "";
				for (var i=0; i<value.length; i++){
					if (i > 0){
						result += "###";
					}
					result += args.ids[i] + "::";
					if (args.currentSignature[i].length > 0){
						result += args.currentSignature[i];
					}
					result += value[i];
				}
				dojo.byId('<portlet:namespace/>_signature').value = result;		
				lockScreen();
				var form = dojo.byId('<portlet:namespace/>_form');
				dojo.byId('<portlet:namespace/>_action').value = "<%=QuickResolutionPortlet.ACTION_SAVE_DS_AND_EXIT%>";
				form.submit();
			}	
		</script>

		<script type="text/javascript" language="javascript">
			function signAction(){
				lockDsScreen();
				signCard();
				unlockScreen();
			}
			</script>
<%
} 
%>
        
<form id="<portlet:namespace/>_form" method="post"  action="<portlet:actionURL/>">
	<div class="resolution">
	<div class="refuse" style="float: left; clear: both;">
	<input id="<%= QuickResolutionPortlet.FIELD_SUPPORTS_DS%>" name="<%=QuickResolutionPortlet.FIELD_SUPPORTS_DS%>" value="true" type="hidden"/>
	<input id="<portlet:namespace/>_signature" name="<%=QuickResolutionPortlet.PARAM_SIGNATURE%>" type="hidden" class="attrString" value="" />
	<input id="<portlet:namespace/>_responsibleField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_RESPONSIBLE_EXECUTOR %>" value=""/>
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ACTION %>" value=""/>
	<input id="<portlet:namespace/>_responsibleField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_RESPONSIBLE_EXECUTOR %>" value=""/>
	<input id="<portlet:namespace/>_refuseAttachmentField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_FILE_ATTACHMENT %>" value=""/>
	
		<hr />	

        <div class="left_column">

            <textarea name="<%=QuickResolutionPortlet.FIELD_TEXT_RESOLUTION%>" class="resolution_text" placeholder="Комментарий" id="<portlet:namespace/>_text_res"><c:out value="${sessionBean.resolutionText}"/></textarea>
            
            <div class="clear_resolution" >
				<a onclick="<portlet:namespace/>_clearEditor(); return false"  href="#" /></a>
			</div>            



        </div><!-- .left_column end -->

            <div class="center_column">
                <ul>
                <!-- typical resolution output -->
					<c:forEach var="typicalVisa" items="${sessionBean.typicalVisasTexts}" varStatus="status" >
						<li>
							<a id="<portlet:namespace/>_text_res_${status.count}" class="dotted" href="#" 
								onClick="<portlet:namespace/>_setFocus('<portlet:namespace/>_text_res_${status.count}');return false;"><c:out value="${typicalVisa}"/></a>
						</li>
						
					</c:forEach>
                </ul>
            </div>

        <hr />	

	<!-- скрыто! -->
    <div class="left_column" style="display:none">
            <div class="time_frames" style="margin-top: 0px">

                <div class="date_select">

                    <label style="float: left;"><fmt:message key="label.term"/></label>
						<input dojoType="dijit.form.DateTextBox" type="text"   id="<portlet:namespace/>_term"
							name="<%=QuickResolutionPortlet.FIELD_TERM%>"  style="width: 210px; height: 22px; margin-left: 107px" class="date_input"
						value="<%if(sessionBean.getTermValue()!=null){%><%=(new SimpleDateFormat("yyyy-MM-dd")).format(sessionBean.getTermValue())%><%}%>"/>
                </div>

                <label style="float: left"><fmt:message key="label.executors"/></label>
   				<select style="width: 200px;" id="<portlet:namespace/>_executorSelect" />
   				
            </div>
            	<div class="list">
					<table id="<portlet:namespace/>_responsibleExecutorTable" class="res"  style="width: 100%; margin-top: 0px;">
					</table>
			    </div>
            
            
        </div><!-- .left_column end -->
	
	 
	

        <div class="center_column" style="width: 520px;">
        <div class="upload"> 
            <label style="width: 140px; margin: 0px" ><fmt:message key="label.uploadAttachment"/></label>
            <br />
            
            <input readonly style="width: 200px; height: 22px;  clear: both; margin-top: 18px" type="text" id="<portlet:namespace/>_filePath" />
            
			<button dojoType="dijit.form.Button" type="button" style="height: 20px; margin-left: 15px; margin-top: -8px">
				<fmt:message key="button.browse"/>
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_selectFile();
					</script>
			</button>
			
             
			<button dojoType="dijit.form.Button" style="height: 20px; margin-left: 5px; margin-top: -8px" 
														type="button" disabled id="<portlet:namespace/>_uploadButton">
				<fmt:message key="button.upload_attachment"/>
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_uploadAttachment();
					</script>
				</button>
				
				<!-- button dojoType="dijit.form.Button" class="NoAppletHide" style="height: 20px; margin-left: 5px; margin-top: -8px" 
														type="button" id="<portlet:namespace/>_addLinkedButton">
					
				<fmt:message key="button.addLinkedAttachment"/>
				
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_ADD_LINKED_FILES %>');
					</script>
				</button-->
					
            

            <table id="<portlet:namespace/>_refuseAttachmentTable"> 
           </table> 
        </div>
        </div><!-- .center_column end -->



 
        <div class="controls">
            <div class="resolution_buttons">
            	<div class="cancel_resolution" >
					<a href="#"
						onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_CANCEL %>')"  ></a>
					</div>	
                <div class="submit_resolution" >
					<a  href="#"
						onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_DONE %>')"  ></a>
                </div>
            </div>

        </div><!-- .controls end -->
	         
	
 </div>
 </div>

</form>
<div id="attachDsQuery" dojoType="dijit.Dialog"
		title="<fmt:message key="apply.card.ds.query.title"/>"
		style="width: 320px; height: 96px">
			<div style="text-align: left;">
				<fmt:message key="change.state.attach.ds.query.message" />
			</div>
			<div style="float: right; clear: both;" id="dialogButtons">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="apply.ds.query.yes" />
					<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('attachDsQuery').hide();
					if(<%=isDsSupport%> && checkPermission&&test_signCard(false)){
						<portlet:namespace/>_submitForm_do(dijit.byId('attachDsQuery').action);
					} else if (<%=applyDS%>>2){
						alert("Формирование ЭП невозможно. Документ не подписан ЭП и не будет направлен далее по маршруту.");
					} else {
						dijit.byId('doWithOutDsQuery').show();
					}
				</script>
				</button>
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="apply.ds.query.no" />
					<script type="dojo/method" event="onClick" args="evt">
						dijit.byId('attachDsQuery').hide();
						if (<%=applyDS%> >2 ){
							alert("Документ не подписан ЭП и не будет направлен далее по маршруту.");
						} else {
							dojo.byId('<%= QuickResolutionPortlet.FIELD_SUPPORTS_DS %>').value = false;
							<portlet:namespace/>_submitForm_do(dijit.byId('attachDsQuery').action);
						}
				</script>
				</button>
			</div>
		</div>
		
		<div id="doWithOutDsQuery" dojoType="dijit.Dialog"
		title="<fmt:message key="apply.card.ds.query.title"/>"
		style="width: 320px; height: 110px">
			<div style="text-align: left;">
				<fmt:message key="change.state.without.ds.query.message" />
			</div>
			<div style="float: right; clear: both;" id="dialogButtons">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="apply.ds.query.yes" />
					<script type="dojo/method" event="onClick" args="evt">
						dojo.byId('<%= QuickResolutionPortlet.FIELD_SUPPORTS_DS %>').value = false;
						dijit.byId('doWithOutDsQuery').hide();		
						<portlet:namespace/>_submitForm_do(dijit.byId('attachDsQuery').action);
				</script>
				</button>
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="apply.ds.query.no" />
					<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('doWithOutDsQuery').hide();
				</script>
				</button>
			</div>
		</div>

<script type="text/javascript">
	var <portlet:namespace/>_responsible = ${requestScope.responsible};
	var <portlet:namespace/>_refuseAttachments = ${requestScope.refuseAttachments};
	
	var <portlet:namespace/>_message = ${requestScope.message};
	

	dojo.require("dijit.form.Button");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dijit.form.FilteringSelect");
			

	dojo.addOnLoad(function() {
		
		var dataStore = new dojox.data.QueryReadStore({
			url :'<%=request.getContextPath() + "/servlet/SearchCardServlet"%>'
		});
		
		var select = new dijit.form.FilteringSelect(
			{
				store: dataStore,
				searchAttr: 'label',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
	            //style: "width: 200px; height: 21px;",
				query: {
					<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: '',
					<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
				},
				onChange: function() { <portlet:namespace/>_executorSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_executorSelect')
		);

				
		<portlet:namespace/>_refreshControls();
		
		if (<portlet:namespace/>_message != '') {
			<portlet:namespace/>_showMessage();
		}
	});

	function <portlet:namespace/>_setFocus(idDiv) {
		var textarea = document.getElementById('<portlet:namespace/>_text_res');
		var div = dojo.byId(idDiv);
		var text = div.textContent || div.innerText;
		if (textarea.value == '') {
			textarea.value = text;
		} else {
			textarea.value = textarea.value + '\n' + text;
		}
	}

	function <portlet:namespace/>_clearEditor() {
		var editor = dojo.byId('<portlet:namespace/>_text_res');
		editor.value = '';
	}
	

	function <portlet:namespace/>_uploadAttachment() {
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		var filePath = filePathInput.value;
		if(filePath == "") return;
		dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', true);    
		
		
		var uploadUrl = self.location.protocol + "//" + self.location.host + '<%=QuickResolutionPortlet.MATERIAL_UPLOAD_URL %>';
		var resolutionCardId = '<%= sessionBean.getIdResolution() %>';

		var postResponse = document.applets[0].
								postMaterialCard(uploadUrl, document.cookie, filePath, "", null);
		
		if(!postResponse) {
			alert('<fmt:message key="error.material_download"/>');
			return;
		}

		var attachmentData = eval( "(" + postResponse + ")" );
		<portlet:namespace/>_addAttachment(dojo.byId('<portlet:namespace/>_refuseAttachmentTable'), 
				attachmentData.id, attachmentData.name);

		filePathInput.value = '';
	}	


	function isCardAttached(attachmentName) {
		//check if given card has been already attached 
		for (var i = 0; i < <portlet:namespace/>_refuseAttachments.length; ++i) {
			if (<portlet:namespace/>_refuseAttachments[i].name == attachmentName) {
				return true;
			}
		}
		return false;
		
	}

	function <portlet:namespace/>_removeExecutor(id) {
		
		<portlet:namespace/>_removeCard(id, <portlet:namespace/>_responsible);
	}	 

	function <portlet:namespace/>_removeAttachment(id) {
		
		<portlet:namespace/>_removeCard(id, <portlet:namespace/>_refuseAttachments);
	}	

	function <portlet:namespace/>_removeCard(id, cardsArray) {
		
		for (var i = 0; i < cardsArray.length; ++i) {
			if (cardsArray[i].cardId == id) {
				cardsArray.splice(i, 1);
			}
		}	

		<portlet:namespace/>_refreshControls();

		
	}
	
	function <portlet:namespace/>_addAttachment(attachmentsTable, attachmentId, attachmentName){

		if (isCardAttached(attachmentName))
			return;//given card has been attached...skip this action  
		
		var i = <portlet:namespace/>_refuseAttachments.length;
		<portlet:namespace/>_refuseAttachments[i] = {cardId: attachmentId, name: attachmentName};
		
		<portlet:namespace/>_refreshControls();

		
	}	

	function <portlet:namespace/>_selectFile() {
        var fileName = document.applets[0].getFileName();
        dojo.byId('<portlet:namespace/>_filePath').value = fileName;   
        dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', false);      
  	}
	

	
	function <portlet:namespace/>_fillTable(peoples, tableId, hiddenId, isExecutor) {
		var table = dojo.byId(tableId);
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= 0; --i) {
				table.deleteRow(i);
			}
		}
		for (var i = 0; i < peoples.length; ++i) {
			var row = table.insertRow(i);
			var cell = null;
			cell = row.insertCell(0);
			cell.style.textAlign = 'center';
			if (isExecutor) {
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_removeExecutor(' + peoples[i].cardId + ')">&nbsp;</a>';
			}	else {
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_removeAttachment(' + peoples[i].cardId + ')">&nbsp;</a>';
			}	
						
			
			cell = row.insertCell(1);
			cell.innerHTML = peoples[i].name;
			
		}
	}
	
	function <portlet:namespace/>_executorSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_executorSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.NAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_responsible.length;
			<portlet:namespace/>_responsible[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}
	
	
	function <portlet:namespace/>_refreshControls() {
		
		<portlet:namespace/>_fillTable(
			<portlet:namespace/>_responsible, 
			'<portlet:namespace/>_responsibleExecutorTable', 
			'<portlet:namespace/>_responsibleField',
			true
		);

		<portlet:namespace/>_fillTable(
				<portlet:namespace/>_refuseAttachments, 
				'<portlet:namespace/>_refuseAttachmentTable', 
				'<portlet:namespace/>_refuseAttachmentField',
				false
			);		
		
		// говорим выподающему списку каких пользователей уже не надо выводить
		var allIds = [];
		for (var i = 0; i < <portlet:namespace/>_responsible.length; ++i) {
			allIds.push(<portlet:namespace/>_responsible[i].cardId);
		}		
		
		var select = dijit.byId('<portlet:namespace/>_executorSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');		
				
		
	
		// установка значений hidden-ов
		var separator = '<%=QuickResolutionPortlet.SEPARATOR%>';
		var value = "";
		if (<portlet:namespace/>_responsible.cardId != undefined) {
			value = <portlet:namespace/>_responsible.cardId+":"+<portlet:namespace/>_responsible.name;
		}
		dojo.byId('<portlet:namespace/>_responsibleField').value = value;

		var id_delimiter = '<%=QuickResolutionPortlet.ID_DELIMITER%>';
		value = "";
		for(var i=0; i < <portlet:namespace/>_refuseAttachments.length; i++) {
			if (i > 0) {
				value += separator;
			}
			value += <portlet:namespace/>_refuseAttachments[i].cardId + id_delimiter + <portlet:namespace/>_refuseAttachments[i].name;
		}
		dojo.byId('<portlet:namespace/>_refuseAttachmentField').value = value;
		
	}		
	
	function <portlet:namespace/>_submitForm(action) {
		try{
			miniLog();
		} catch (err){
			 var xhrArgs = {
				      url: "/DBMI-UserPortlets/minilog/log?error=true",
				      postData: "ErrorBrowser "+navigator.userAgent
				    }
			 dojo.xhrPost(xhrArgs);
		}

		if('<%=QuickResolutionPortlet.ACTION_DONE%>' == action) { 
			if(!<portlet:namespace/>_validate()) {			
				return false;
			}
			if(checkAppletExists(true) && <%=isDsSupport%>) {
				dijit.byId('attachDsQuery').action = action;
				dijit.byId('attachDsQuery').show();
			}else{
				if(<%=applyDS%> >2){
					alert("Переход должен сопровождаться обязательным формированием ЭП. У вас отсутствует сертификат ЭП. Действие отменено.");
				} else {
					dojo.byId('<%= QuickResolutionPortlet.FIELD_SUPPORTS_DS %>').value = false;
					<portlet:namespace/>_submitForm_do(action);
				}
			}
		} else {
			<portlet:namespace/>_submitForm_do(action);
		}
	}
	
	function <portlet:namespace/>_submitForm_do(action) {
		lockScreen();
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
	}
	
	function <portlet:namespace/>_validate() {
		var resText = dojo.byId('<portlet:namespace/>_text_res');

		if(resText && resText.value.length > 0) {
			return true;
		}
		alert('<fmt:message key="message.emptyComment"/>');
		return false;
	}
	
	function test_signCard(submit){
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
					if(!submit){
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
		window.signParams[4]="\"7777\"";
	}

</script>