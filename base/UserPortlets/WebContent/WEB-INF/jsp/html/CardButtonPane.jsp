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
<%--
	JSP frgment to be included in CardEdit.jsp and CardView.jsp
	Displays button panel with buttons for changing card state (Publish, Reject & etc.)
	For CardEdit.jsp it also shows Save and Close buttons  
--%>
<%@page import="com.aplana.dbmi.numerator.action.AssignRegistrationAction"%>
<%@page import="com.aplana.dbmi.service.DataServiceBean"%>
<%@page import="com.aplana.dbmi.model.WorkflowMove"%>
<%@page import="com.aplana.dbmi.jbr.processors.GetAttachments"%>
<%@page session="false" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.model.CardState"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Arrays"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="javax.portlet.WindowState"%>
<%@page import="java.util.*"%>
<%@page import="java.util.Map.*"%>
<%@page import="javax.portlet.PortletRequest"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.crypto.CryptoApplet"%>
<%@page import="com.aplana.util.DigitalSignatureUtil"%>
<%@page import="com.aplana.dbmi.common.utils.pdf.PdfUtils"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<%@page import="org.json.JSONObject" %>
<%@page import="com.aplana.dbmi.crypto.*"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.Template"%><portlet:defineObjects />
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" />
<%
	ObjectId outcomingId = ObjectId.predefined(Template.class, "jbr.outcoming");

	CardPortletSessionBean sessionBean = (CardPortletSessionBean) renderRequest
			.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

	String cardId = sessionBean.getActiveCardInfo().getCard().getId() != null ?
			sessionBean.getActiveCardInfo().getCard().getId().getId().toString() : "null";
			
			
	ObjectId signatureAttributeId = ObjectId.predefined(
			HtmlAttribute.class, "jbr.uzdo.signature");
	HtmlAttribute signatureAttribute = (HtmlAttribute) sessionBean.getActiveCard().getAttributeById(signatureAttributeId);
	boolean showSignCardButton = false;

	if (signatureAttribute != null) {
		showSignCardButton = sessionBean.getActiveCardInfo()
				.isCanChange();
	}

	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal
			.getFactory().getConfigService());

	ObjectId materialsAttributeId = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	CardLinkAttribute materialsAttribute = (CardLinkAttribute) sessionBean
			.getActiveCard().getAttributeById(materialsAttributeId);
	boolean showSignAttachmentsButton = null != materialsAttribute
			&& materialsAttribute.getLinkedCount() > 0;
	boolean showSignMenu = showSignCardButton
			|| showSignAttachmentsButton;
	boolean isDsSupport = sessionBean.isDsSupport(renderRequest);
	showSignMenu = showSignMenu && isDsSupport;

	Integer applySignatureOnLoad = (Integer) renderRequest
			.getPortletSession().getAttribute("MI_APPLY_SIGNATURE");
	boolean forceSign = applySignatureOnLoad == null ? false
			: applySignatureOnLoad.intValue() > 0;
	ObjectId state = sessionBean.getActiveCard().getState();
	ObjectId fromState = (ObjectId) renderRequest.getPortletSession()
			.getAttribute("FROM_STATE");
	if (forceSign && state.equals(fromState)) {
		forceSign = false;
	}
	if (applySignatureOnLoad == null){
		applySignatureOnLoad = 0;
	}
	
	if (sessionBean.getActiveCard().getId()!=null){
		List<WorkflowMove> workflowMoves = sessionBean.getActiveCardInfo().getAvailableWorkflowMovesSorted();
		boolean ds_potention = false;
		for(WorkflowMove wMove: workflowMoves){
			if(wMove.getApplyDigitalSignatureOnMove()>0){
				ds_potention = true;
			}
		}
		if (ds_potention || forceSign){
		//ArrayList<String> params = DigitalSignatureUtil.prepareSignatureParams(sessionBean.getServiceBean(), sessionBean.getActiveCard(), applySignatureOnLoad.intValue() == 2,  renderResponse.encodeURL(renderRequest.getContextPath()  + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "="));
		%>
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
					if(<%=isDsSupport%> && window.checkPermission == undefined) {
						alert('<fmt:message key="alert.java.plugin.unable"/>');
					}
					if(<%=isDsSupport%> && window.checkPermission &&signCard(false)){
						submitForm_ChangeState_do(dijit.byId('attachDsQuery').targetState);
					} else if(dijit.byId('attachDsQuery').apply_ds > 2) {
						alert('<fmt:message key="alert.ds.unable"/>')
					} else {
						dijit.byId('doWithOutDsQuery').show();
					}
				</script>
				</button>
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.no" />
					<script type="dojo/method" event="onClick" args="evt">
					if(dijit.byId('attachDsQuery').apply_ds > 2){
						alert('<fmt:message key="alert.doc.not.signed.ds"/>');
						dijit.byId('attachDsQuery').hide();
					} else {
						document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.DISABLE_DS%>.value = '<%=CardPortlet.DISABLE_DS%>';
						submitForm_ChangeState_do(dijit.byId('attachDsQuery').targetState);
						dijit.byId('attachDsQuery').hide();
					}
				</script>
				</button>
			</div>
		</div>
		
		<div id="doWithOutDsQuery" dojoType="dijit.Dialog"
		title="<fmt:message key="upload.apply.card.ds.query.title"/>"
		style="width: 320px; height: 110px">
			<div style="text-align: left;">
				<fmt:message key="change.state.without.ds.query.message" />
			</div>
			<div style="float: right; clear: both;" id="dialogButtons">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="upload.apply.ds.query.yes" />
					<script type="dojo/method" event="onClick" args="evt">
					document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.DISABLE_DS%>.value = '<%=CardPortlet.DISABLE_DS%>';
					submitForm_ChangeState_do(dijit.byId('attachDsQuery').targetState);
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
					if(!submit){
						window.signParams=[];
						window.signParams[0]="\"Z2ZoZmdoZGZn\"";
						window.signParams[1]="\"WjJab1ptZG9aR1pu\"";
						window.signParams[2]="'<card id=\"0\"><attr type=\"S\" id=\"JBR_INFD_SHORTDESC\"/></card id=\"0\">'";
						window.signParams[3]="''";
						window.signParams[4]="\"12273535\"";
					} else {
						dojo.xhrPost({
							url:'<%=request.getContextPath()%>/CardDSParams',
							postData: dojo.toJson({	'namespace':'<%=renderResponse.getNamespace()%>',
													'cardId':'<%=sessionBean.getActiveCard().getId().getId()%>',
													'apply_ds':'<%=applySignatureOnLoad%>'}),
							preventCache: true,
							sync:true,
							load: function(resp, ioArgs){
								window.signParams = JSON.parse(resp);
								if(window.signParams && (window.signParams[0]=="\"\"" || !window.signParams[1]=="\"\"")){
									alert('<fmt:message key="alert.no.attrs.to.sign"/>');
									window.signParams = undefined;
								} 
							},
							error: function(error){
								alert('<fmt:message key="alert.server.get.data.error"/>');
							}
					    });
					}
				}


			</script>
		<%}
	
		if ((showSignCardButton || forceSign) && isDsSupport) {
			if (forceSign) {
	%>
	<input id="signature" type="hidden" class="attrString"
		name="<%=CardPortlet
								.getAttributeFieldName(signatureAttribute)%>"
		value="" />
	<script type="text/javascript" language="javascript">
		function signAction() {
			if ((<%=showSignCardButton%> || <%=forceSign%>) && <%=isDsSupport%> && window.checkPermission){
				signCard(true);
			}
		}
	</script>
	
	<%
				}
			} 
		}
	}

	renderRequest.getPortletSession().removeAttribute(
			"MI_APPLY_SIGNATURE");
	renderRequest.getPortletSession().removeAttribute("FROM_STATE");
%>

<script type="text/javascript" language="javascript">
	dojo.require('dijit.form.Button');
	dojo.require('dijit.Menu');
	function submitForm_ChangeState(needConfirmation, targetState, confirmation, apply_ds) {
		submitForm_ChangeState(needConfirmation, targetState, confirmation, apply_ds, false, null);
	}
	function submitForm_ChangeState(needConfirmation, targetState, confirmation, apply_ds, isCustom, customAction) {
		if (needConfirmation) {
			if (!confirm(confirmation)) {
				return;
			}
		}
		if(dijit.byId('attachDsQuery')){
			dijit.byId('attachDsQuery').targetState = targetState;
		}
		if((apply_ds>0 && <%=isDsSupport%> || apply_ds>2  ) && dijit.byId('attachDsQuery')){
			if(!<%=isDsSupport%> && apply_ds>2){
				alert('<fmt:message key="alert.ds.unable"/>')
				return;
			}
			if (<%=showSignCardButton%> || <%=forceSign%>){
				dijit.byId('attachDsQuery').show();
				dijit.byId('attachDsQuery').apply_ds = apply_ds;
			}
		} else {
			if(isCustom) {
				submitForm(customAction);
			} else {
			submitForm_ChangeState_do(targetState);
		}
	}
	}
	function submitForm_ChangeState_do(wfm_id) {
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.CHANGE_STATE_ACTION%>' + wfm_id;     
		if(<%=sessionBean.getActiveCardInfo().getCard().getTemplate().equals(outcomingId)%> && wfm_id == 395895 ){
			showManualStampDialog();
		} else {
			document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
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
		dojo.byId('signature').value = result;		
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.SIGN_CARD_ACTION%>';
		document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
	}
	function submitForm_CopyFiles() { 
        document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.COPY_FILES_ACTION%>';
        document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
    }
	function submitForm_PrepareSigningCard() { 
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.PREPARE_FOR_SIGN_CARD_ACTION%>';
		document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
	}
	function submitForm_SelectAttachments() { 
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.SELECT_ATTACHMENTS_CARD_ACTION%>';
		document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
	}
	function showDSInfo() {
		window.open('<%=request.getContextPath()%>/CardDSInfoViewer?namespace=<%=renderResponse.getNamespace()%>','<fmt:message key="ds.showinfo.windowTitle"/>');
	}
	
	function getRegistrationNumber_confirmation() {
		dijit.byId('getRegNumberDialog').show();		
	}
	function submitForm_getRegistrationNumber() {
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.GET_REGISTRATION_NUMBER_ACTION%>';
		document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
	}

	function submitForm_chenkOnRepeated() { 
		document.<%=CardPortlet.EDIT_FORM_NAME%>.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.CHECK_ON_REPEATED_ACTION%>';
		document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
	}
	
	function hideMenu(menuId) {
		var menu = dijit.byId(menuId);
		var focus = menu._focused;
		//This code doesn't works.
		/*if (!focus) {
			dijit.popup.close(menu);       			
		}*/
	}
      
	function openMenu(thisObj, menuId, itemId) {
		downButton(thisObj);
		var obj = dojo.byId(itemId);
		var menu = dijit.byId(menuId);
		dijit.popup.open({
			popup: menu,
			around: obj,
			orient: {'BL':'TL', 'BR':'TR', 'TL':'BL', 'TR':'BR'},
			onExecute: function() {
				dijit.popup.close(menu);
			},
			onCancel: function() {
				dijit.popup.close(menu);
			},
			onClose: function() {
			}
		});
	}

	function setDropDownButtonStyle(target, style){
		dojo.forEach(dojo.query('li,a', target), function(entry, i){entry.className = style;});
	}
	
	function showManualStampDialog(){
		if(<%=sessionBean.getStampPosition() != null %>){
			document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
			return;
		}
		var images = [];
		var stampHeight, stampWidth; 
		
		dojo.xhrGet({
			url: '<%=request.getContextPath() + "/ManualStampServlet"%>',	
			sync: true,
			content: {
				cardId: '<%=cardId%>'
			},
			handleAs: 'json',
			load: function(data) {
				if(data){
					images = data.images;
					stampHeight = data.height;
					stampWidth = data.width;
				}
			},
			error: function(error) {
				console.error(error);
			}
		});
		
		var moveHandler = function(e){
			window.e = e;
			cssWidth = stampWidth * e.target.clientWidth / <%= PdfUtils.A4XSizeP%>;
			cssHeight = stampHeight * e.target.clientHeight / <%= PdfUtils.A4YSizeP%>;
			
			var a = jQuery(e.target);
			var d = dijit.byId('manualStampDialog');
			var left = e.pageX - jQuery(d.domNode).position().left - cssWidth / 2;
			if(left < a.position().left){
				left =  a.position().left;
			}
			var top = e.pageY - jQuery(d.domNode).position().top    - cssHeight / 2;
			if(top <  a.position().top){
				top =  a.position().top;
			}
			if(left > a.position().left + a.width() - cssWidth){
				left =  a.position().left + a.width() - cssWidth;
			}
			if(top >  a.position().top + a.height() - cssHeight){
				top = a.position().top + a.height() - cssHeight;
			}
			jQuery('#aim').css('display', 'block');
			jQuery('#aim').css('height', cssHeight);
			jQuery('#aim').css('width', cssWidth);
			jQuery('#aim').css('left',	left);
			jQuery('#aim').css('top',	top);
	    };
		
		
		
		if(images.length == 0){
			document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
			return;
		}
		if(dijit.byId('manualStampDialog')){
			dijit.byId('manualStampDialog').destroy();
		}
		
		var manualStampDialog = new dijit.Dialog({
		     title: '<fmt:message key="title.manual.stamp"/>',
		     style: "height:auto; width: auto",
		     id:'manualStampDialog'
		     }
		 );
		
		var imageContent="";

		manualStampDialog.setContent('<div id="manualStamp"/></div><div id="aim"/>');
		manualStampDialog.show();
		
		images.forEach(function(el){
			createImg(el.number, el.file, moveHandler, stampHeight, stampWidth);
		});
	}
	
	function createImg(id, file, moveHandler, stampHeight, stampWidth){
		var img = document.createElement('img');
		img.id = id;
		img.src =  '<%=request.getContextPath() + "/ManualStampServlet?image="%>' + file;
		document.getElementById('manualStamp').appendChild(img);
		jQuery(img).mousemove(moveHandler);
		jQuery(img).dblclick(function(e){
			position = e.target.id + ':';
			position = position + parseInt(((e.originalEvent.layerX - e.target.x) / 
						e.target.clientWidth) * <%= PdfUtils.A4XSizeP%> - stampWidth/2) + ':';	
			position = position + parseInt(((e.originalEvent.layerY - e.target.y) /
						e.target.clientHeight) * <%= PdfUtils.A4YSizeP%> - stampHeight/2);
			document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.STAMP_POSITION %>.value = position;
			document.<%=CardPortlet.EDIT_FORM_NAME%>.submit();
			jQuery('#aim').remove();
			dijit.byId('manualStampDialog').destroy();
		});
		return img;
	}
</script>

<%
	if (false/*showSignMenu*/) {
%>
<div dojoType="dijit.Menu" id="DSActionsMenu" style="display: none">
	<%
		if (showSignCardButton) {
	%>
	<div dojoType="dijit.MenuItem"
		onClick="submitForm_PrepareSigningCard()">
		<fmt:message key="ds.sign.card" />
	</div>
	<%
		}
			if (showSignAttachmentsButton) {
	%>
	<div dojoType="dijit.MenuItem" onClick="submitForm_SelectAttachments()">
		<fmt:message key="ds.sign.attachments" />
	</div>
	<%
		}
	%>
</div>
<%
	}
%>
<div class="buttonPanel" style="float: right">
	<ul>
		<%
			if (false/*showSignMenu*/) {
		%>
		<li onmousedown="openMenu(this, 'DSActionsMenu', 'DSActionsMenuItem')"
			onmouseup="upButton(this)" onmouseover="overButton(this)"
			onmouseout="upButton(this)" id="DSActionsMenuItem"><a href="#"
			onblur="hideMenu('DSActionsMenu')">
				<div>&nbsp;</div>
				<p>
					<fmt:message key="ds.menu.title" />
				</p>
		</a></li>
		<%
			}
			List<ObjectId> baseDocTemplates = Arrays.asList(
					ObjectId.predefined(Template.class, "jbr.incoming"),
					ObjectId.predefined(Template.class, "jbr.outcoming"),
					ObjectId.predefined(Template.class, "jbr.interndoc"),
					ObjectId.predefined(Template.class, "jbr.incomingpeople"),
					ObjectId.predefined(Template.class, "jbr.npa"),
					ObjectId.predefined(Template.class, "jbr.ord"));
			ObjectId template = sessionBean.getActiveCard().getTemplate();
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			AssignRegistrationAction assignReg = new AssignRegistrationAction();
			assignReg.setCard(sessionBean.getActiveCard());
			
			if(CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getCurrentMode()) && serviceBean.canDo(assignReg)) {
		%>
		<btn:button tooltipKey="tool.check.doubling"
			onClick="submitForm_chenkOnRepeated()" icon="check_on_repeat" />

					<div id="getRegNumberDialog" dojoType="dijit.Dialog"
					title="<fmt:message key="reg.number.dialog"/>"
					style="width: 320px; height: 110px">
						<div style="text-align: left;">
							<fmt:message key="get.reg.number.question" />
						</div>
						<div style="float: right; clear: both;" id="dialogButtons">
							<button dojoType="dijit.form.Button" type="button">
								<fmt:message key="upload.apply.ds.query.yes" />
								<script type="dojo/method" event="onClick" args="evt">
									submitForm_getRegistrationNumber();
									dijit.byId('getRegNumberDialog').hide();		
								</script>
							</button>
							<button dojoType="dijit.form.Button" type="button">
								<fmt:message key="upload.apply.ds.query.no" />
								<script type="dojo/method" event="onClick" args="evt">
									dijit.byId('getRegNumberDialog').hide();
								</script>
							</button>
					</div>
					</div>
				<btn:button tooltipKey="tool.get.reg.number" onClick="getRegistrationNumber_confirmation()"	icon="reg_number"/>	
			<%	
			}
			
			if (baseDocTemplates.contains(template)) {
		%>
		<btn:button onClick="showDSInfo()" textKey="ds.showinfo.button" />

		<%
			}
			List wfms = sessionBean.getActiveCardInfo()
					.getAvailableWorkflowMoves();
			
			boolean changeStateButtonIsShown = false;
			if (wfms != null && wfms.size() > 0) {
				changeStateButtonIsShown = true;
		%>
		<div dojoType="dijit.form.DropDownButton" baseClass="dbmiDropDownButton" 
			onMouseEnter="setDropDownButtonStyle(this.domNode, 'hover')"
			onMouseLeave="setDropDownButtonStyle(this.domNode, '')"
			onMouseDown="setDropDownButtonStyle(this.domNode, 'select')"
		>
				<span>
					<ul>
						<li>
							<a href="#">
								<div>&nbsp;</div>
								<p>
									<fmt:message key="tool.change.status" />
								</p>
							</a>
						</li>
					</ul>
				</span>
				<div dojoType="dijit.Menu" id="changeStateMenu">
					<c:forEach
						items="<%=sessionBean.getActiveCardInfo().getAvailableWorkflowMovesSorted()%>"
						var="workflowMove">
						<c:choose>
							<c:when test="${not workflowMove.needConfirmation}">
								<c:set var="confirmationMessage" value="#" />
							</c:when>
							<c:when test="${not empty workflowMove.confirmation.value}">
								<c:set var="confirmationMessage"
									value="${workflowMove.confirmation.value}" />
							</c:when>
							<c:otherwise>
								<fmt:message var="confirmationMessage"
									key="edit.page.change.state.confirm.msg" />
							</c:otherwise>
						</c:choose>
						<div dojoType="dijit.MenuItem"
							onClick="submitForm_ChangeState(${workflowMove.needConfirmation}, ${workflowMove.id.id}, '${confirmationMessage}' ,${workflowMove.applyDigitalSignatureOnMove})">
							${workflowMove.moveName}</div>
					</c:forEach>
				</div>
		</div>

		<%
			}

			// Если имеется атрибут для хранения вложенных документов - отобразить 
			// кнопку создания связанных документов... 
			if (CardPortlet.CARD_VIEW_MODE.equals(sessionBean.getCurrentMode())

					&& (sessionBean.getDoclinkCreateData() != null)

					&& (sessionBean.getActiveCardInfo() != null)
					&& (sessionBean.getActiveCardInfo().getCard() != null)
					&& (sessionBean.getActiveCardInfo().getCard().getId() != null)

					&& (sessionBean
							.getActiveCardInfo()
							.getCard()
							.getAttributeById(
									sessionBean.getDoclinkCreateData()
											.getAttrBackLinkId()) != null)) {
		%>
		<%-- CREATE DOC LINK:: BUTTON --%>
		<c:set var="dialogCreateLinkedDocId">
			<portlet:namespace />_DialogCreateLinkedDoc</c:set>
		<c:set var="onClickCreateDocLinkBtn">dijit.byId('${dialogCreateLinkedDocId}').show();</c:set>
		<btn:button onClick="${onClickCreateDocLinkBtn}"
			textKey="create.doclink.caption" />
		<%-- end CREATE DOC LINK:: BUTTON --%>

		<%-- CREATE DOC LINK:: DIALOG --%>

		<div dojoType="dijit.Dialog" id="${dialogCreateLinkedDocId}"
			title="<fmt:message key="create.doclink.dialog.title"/>"
			style="width: 320px; height: 120px">
			<div style="text-align: left; padding: 5px;">

				<table cols="2" cellpadding="4" cellspacing="5">

					<col width="50%">
					<col width="50%">

					<%-- Шаблон документа --%>
					<c:if
						test="<%= sessionBean.getDoclinkCreateData().getMapTemplates() != null %>">
						<tr>
							<td><fmt:message key="create.doclink.doctype" /></td>
							<td><select id="createdoclink_template"
								name="createdoclink_template">
									<%--
								<OPTION selected value="784">Внутренний документ</OPTION>
								<OPTION value="224">Входящий</OPTION>
								<OPTION value="364">Исходящий</OPTION>
								<OPTION value="324">Поручение</OPTION>
								  --%>
									<%
										for (java.util.Iterator iter = sessionBean
														.getDoclinkCreateData().getMapTemplates()
														.entrySet().iterator(); iter.hasNext();) {
													// [ObjectId, String]
													final java.util.Map.Entry item = (java.util.Map.Entry) iter
															.next();
									%>
									<option value="<%=((ObjectId) item.getKey()).getId()%>"><%=item.getValue()%>
									</option>
									<%
										}
									%>
							</select></td>
						</tr>
					</c:if>

					<c:if
						test="<%= sessionBean.getDoclinkCreateData().getTypes() != null %>">
						<%-- Тип связи --%>
						<tr>
							<td><fmt:message key="create.doclink.linktype" /></td>
							<td><select id="createdoclink_type"
								name="createdoclink_type">
									<%--
								<option selected value="1502">В ответ на ...</option>
								<option value="1503">Ответ</option>
								<option value="1504">Исполнение</option>
								<option value="1505">Предыдущие</option>
								<option value="1599">Последующие</option>
								<option value="1600">Связан с ...</option>
								<option value="1601">Во исполнение</option>
								<option value="1602">На ОРД</option>
								  --%>
									<%
										for (java.util.Iterator iter = sessionBean
														.getDoclinkCreateData().getTypes().iterator(); iter
														.hasNext();) {
													final ReferenceValue item = (ReferenceValue) iter
															.next();
									%>
									<option value="<%=item.getId().getId()%>"><%=item.getValue()%>
									</option>
									<%
										}
									%>
							</select></td>
						</tr>
					</c:if>
				</table>
			</div>

			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="create.doclink.confirmation.go" />
				<script type="dojo/method" event="onClick" args="evt">
					// var form = dijit.byId( '${dialogCreateLinkedDocId}');
					var form = document;
					var selTemplate = form.getElementById('createdoclink_template');
					var selType = form.getElementById('createdoclink_type');
					var attrCode = '<%=sessionBean.getDoclinkCreateData()
						.getAttrBackLinkCode()%>';
					submitCreateDocLink( selTemplate.value, selType.value, attrCode);
				</script>
			</button>

			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="create.doclink.confirmation.cancel" />
				<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('${dialogCreateLinkedDocId}').hide();
				</script>
			</button>
		</div>

		<script type="text/javascript">
		dojo.require("dijit.Dialog");

		function submitCreateDocLink(doclink_template, doclink_type, attrCode) { 
			var form = document.<%=CardPortlet.EDIT_FORM_NAME%>;
			form.<%=CardPortlet.ACTION_FIELD%>.value = '<%=CardPortlet.CREATE_DOCLINK_ACTION%>';
			form.<%=CardPortlet.ATTR_ID_FIELD%>.value = attrCode;
			form.<%=CardPortlet.PARAM_DOCLINK_TEMPLATE%>.value = doclink_template;
			form.<%=CardPortlet.PARAM_DOCLINK_TYPE%>.value = doclink_type;
			form.submit();
		}
	</script>
		<%-- end CREATE DOC LINK:: DIALOG --%>

		<%
			}
            List<ObjectId> outsts = Arrays.asList(
                    ObjectId.predefined(CardState.class, "registration"),
                    ObjectId.predefined(CardState.class, "before-registration"), 
                    ObjectId.predefined(CardState.class, "delo"));

            List<ObjectId> npasts = Arrays.asList(
                    ObjectId.predefined(CardState.class, "registration"),
                    ObjectId.predefined(CardState.class, "before-registration"), 
                    ObjectId.predefined(CardState.class, "delo"),
                    ObjectId.predefined(CardState.class, "execution"),
                    ObjectId.predefined(CardState.class, "done"),
                    ObjectId.predefined(CardState.class, "ready-to-write-off"));

            List<ObjectId> ordsts = Arrays.asList(
                    ObjectId.predefined(CardState.class, "registration"),
                    ObjectId.predefined(CardState.class, "before-registration"), 
                    ObjectId.predefined(CardState.class, "execution"),
                    ObjectId.predefined(CardState.class, "done"),
                    ObjectId.predefined(CardState.class, "delo"),
                    ObjectId.predefined(CardState.class, "ready-to-write-off"));

			if ((ObjectId.predefined(Template.class, "jbr.outcoming").equals(
					sessionBean.getActiveCard().getTemplate()) && outsts
					.contains(sessionBean.getActiveCard().getState()))
					|| (ObjectId.predefined(Template.class, "jbr.npa").equals(
							sessionBean.getActiveCard().getTemplate()) && npasts
							.contains(sessionBean.getActiveCard().getState()))
					|| (ObjectId.predefined(Template.class, "jbr.ord").equals(
							sessionBean.getActiveCard().getTemplate()) && ordsts
							.contains(sessionBean.getActiveCard().getState()))) {
		%>
		<li onmousedown="downButton(this)" onmouseup="upButton(this)"
			onmouseover="overButton(this)" onmouseout="upButton(this)"
			onclick="submitForm_CopyFiles()"><a href="#" title='<fmt:message key="copy.files" />'><div
					class="copy_files img">&nbsp;</div></a></li>
		<%
			}
			if (CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getCurrentMode())) {
		%>
		<c:if
			test="<%=!sessionBean.getActiveCardInfo().getActionsManager().getActiveActionIds().isEmpty() %>">
			<%-- Выпадающее меню для действий --%>
			<div dojoType="dijit.Menu" id="cardActionsMenu" style="display: none">
				<c:forEach
					items="<%=sessionBean.getActiveCardInfo().getActionsManager().getActiveActionDescriptors()%>"
					var="actionHandlerDescriptor">
					<div dojoType="dijit.MenuItem"
						onClick="submitCardActionsManagerAction('${actionHandlerDescriptor.id}');">
						${actionHandlerDescriptor.title}</div>
				</c:forEach>
			</div>
			<li
				onmousedown="openMenu(this, 'cardActionsMenu', 'cardActionsMenuItem')"
				onmouseup="upButton(this)" onmouseover="overButton(this)"
				onmouseout="upButton(this)" id="cardActionsMenuItem"><a
				href="#" onblur="hideMenu('cardActionsMenu')">
					<div>&nbsp;</div>
					<p>
						<fmt:message key="tool.card.actions" />
					</p>
			</a></li>
		</c:if>
		<c:choose>
			<c:when
				test="<%= sessionBean.getActiveCardInfo().getStoreHandler() != null %>">
				<%-- Если для данной карточки определено собственное действие по сохранению, показываем свою кнопку "Сохранить" --%>
				<c:set var="onClickCallback">submitForm('<%=CardPortlet.CUSTOM_STORE_CARD_ACTION%>')</c:set>
				<btn:button onClick="${onClickCallback}"
					textKey="<%= sessionBean.getActiveCardInfo().getStoreHandler().getStoreButtonTitle() %>" />
				<c:set var="onClickCallback">submitForm('<%=sessionBean.getActiveCardInfo().getStoreHandler().getCloseActionString()%>')</c:set>
				<btn:button onClick="${onClickCallback}"
					textKey="edit.page.cancel.btn" />
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test="<%= sessionBean.getActiveCard().getId() == null && (ObjectId.predefined(Template.class, "jbr.organization").equals(
		                    sessionBean.getActiveCard().getTemplate()) || ObjectId.predefined(Template.class, "jbr.externalPerson").equals(
		                    sessionBean.getActiveCard().getTemplate()))%>">
		             
		                <%-- Кнопку "Сохранить" показываем только для новых карточек --%>
		                <c:set var="onClickCallback">submitForm('<%=CardPortlet.SAVE_AND_CLOSE_EDIT_MODE_ACTION%>')</c:set>
		                <btn:button onClick="${onClickCallback}" textKey="edit.page.generate.btn" />
		                <c:set var="onClickCallback">submitForm('<%=CardPortlet.CLOSE_EDIT_MODE_ANYWAY_ACTION%>')</c:set>
		                <btn:button onClick="${onClickCallback}" textKey="edit.close.confirmation.cancel" />
	            	</c:when>
	            	<%-- (BR4J00029402) Для НПА по нажатию кнопки Закрыть закрываем карточку совсем, без возврата в VIEW_MODE --%>
	            	<c:when test="<%= ObjectId.predefined(Template.class,"jbr.npa").equals(sessionBean.getActiveCard().getTemplate())%>">
		             	<c:if test="<%= sessionBean.getActiveCard().getId() == null %>">
							<%-- Кнопку "Сохранить" показываем только для новых карточек --%>
							<c:set var="onClickCallback">submitForm('<%=CardPortlet.STORE_CARD_ACTION%>')</c:set>
							<btn:button onClick="${onClickCallback}" textKey="edit.page.save.bnt" />
						</c:if>
		                <c:set var="onClickCallback">submitForm('<%=CardPortlet.CLOSE_CARD_ACTION%>')</c:set>
		                <btn:button onClick="${onClickCallback}" textKey="edit.page.cancel.btn" />
	            	</c:when>
	            	<c:otherwise>
						<c:if test="<%= sessionBean.getActiveCard().getId() == null 
										&& (sessionBean.getActiveCardInfo().getChangeStateHandler() == null 
											||  (sessionBean.getActiveCardInfo().getChangeStateHandler() != null
												&& sessionBean.getActiveCardInfo().getChangeStateHandler().isShowFirstSaveButton()))%>">
							<%-- Кнопку "Сохранить" показываем только для новых карточек и только если ее не нужно специально скрыть --%>
							<c:set var="onClickCallback">submitForm('<%=CardPortlet.STORE_CARD_ACTION%>')</c:set>
							<btn:button onClick="${onClickCallback}" textKey="edit.page.save.bnt" />
						</c:if>
						<c:set var="onClickCallback">submitForm('<%=CardPortlet.CLOSE_EDIT_MODE_ACTION%>')</c:set>
						<btn:button onClick="${onClickCallback}" textKey="edit.page.cancel.btn" />
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
		<c:choose>
					<c:when
						test="<%= !changeStateButtonIsShown && sessionBean.getActiveCardInfo().getChangeStateHandler() != null 
									&& sessionBean.getActiveCardInfo().getChangeStateHandler().getWorkflowMove() != null %>">
						<%-- Если для данной карточки определено собственное действие по смене статуса, показываем свою кнопку "Изменить статус" --%>
						<c:set var="changeStateButtonTitle" value="<%=sessionBean.getActiveCardInfo().getChangeStateHandler().getChangeStateButtonTitle()%>"/>
						<div dojoType="dijit.form.DropDownButton" baseClass="dbmiDropDownButton" 
							onMouseEnter="setDropDownButtonStyle(this.domNode, 'hover')"
							onMouseLeave="setDropDownButtonStyle(this.domNode, '')"
							onMouseDown="setDropDownButtonStyle(this.domNode, 'select')"
						>
							<span>
								<ul>
									<li>
										<a href="#">
											<div>&nbsp;</div>
												<p><fmt:message key="${changeStateButtonTitle}" /></p>
										</a>
									</li>
								</ul>
							</span>
							<div dojoType="dijit.Menu" id="customChangeStateMenu">
								<c:set var="workflowMove" value="<%=sessionBean.getActiveCardInfo().getChangeStateHandler().getWorkflowMove()%>"/>
						<c:set var="changeStateActionName" value="<%=sessionBean.getActiveCardInfo().getChangeStateHandler().getChangeStateActionName()%>"/>
								<c:choose>
									<c:when test="${not workflowMove.needConfirmation}">
										<c:set var="confirmationMessage" value="#" />
			</c:when>
									<c:when test="${not empty workflowMove.confirmation.value}">
										<c:set var="confirmationMessage"
											value="${workflowMove.confirmation.value}" />
									</c:when>
			<c:otherwise>
										<fmt:message var="confirmationMessage"
											key="edit.page.change.state.confirm.msg" />
									</c:otherwise>
								</c:choose>
								<div dojoType="dijit.MenuItem"
									onClick="submitForm_ChangeState(${workflowMove.needConfirmation}, ${workflowMove.id.id}, '${confirmationMessage}' ,${workflowMove.applyDigitalSignatureOnMove}
															, true, '${changeStateActionName}')">
									${workflowMove.moveName}</div>
							</div>
						</div>
					</c:when>
				</c:choose>
		<%
			}
		%>
	</ul>
</div>
<script>
	dojo.addOnLoad(function(){
		<% if (sessionBean.getRepeatedDocuments() !=null) { %>
				eval('var repeatedDocs = <%=  new JSONObject(sessionBean.getRepeatedDocuments()) %>');
				var repeatedDocsDialog = new dijit.Dialog({
				    title: '<fmt:message key="title.repeated.docs"/>',
				    style: "height:auto; width: auto",
				    id:'repeatedDocsDialog'
				});
				if(Object.keys(repeatedDocs).length){
					var content = '<table>';
					content = '<tr><td>'+'<fmt:message key="header.repeated.docs"/>' + '</td></tr>';
					dojo.forEach(Object.keys(repeatedDocs), function(entry){
						content = content + '<tr><td>' + repeatedDocs[entry] + '</td></tr>';
					});
					content = content + '</table>';
				} else {
					content =  '<fmt:message key="empty.repeated.docs"/>';
				}
				repeatedDocsDialog.setContent(content);
				repeatedDocsDialog.show();
		<%sessionBean.setRepeatedDocuments(null); 
		} %>
	});
</script>

