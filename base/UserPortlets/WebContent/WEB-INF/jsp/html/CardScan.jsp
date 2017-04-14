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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="java.net.URL" %>

<%@page import="java.util.*" %>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.scanner.CardScanForm"%>	
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
<%
	CardPortletSessionBean sessionBean =
		(CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	String targetAttr = (String)renderRequest.getPortletSession().getAttribute(CardScanForm.TARGET_ATTR);
	String cardIdString = (String)renderRequest.getPortletSession().getAttribute(CardScanForm.SRC_CARD_ID);
	
	PortletURL backURL = renderResponse.createActionURL();
	backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION);
	
	String uploadUrl = new URL(
					renderRequest.getScheme(), renderRequest.getServerName(),
					renderRequest.getServerPort(),
					renderRequest.getContextPath() + "/servlet/scanner-upload").toString();
%>
<div class="buttonPanel">
	<ul>
		<li class="back"
			onmousedown="downBackBut(this)" 
			onmouseup="upBackButton(this)" 
			onmouseover="overBackButton(this)" 
			onmouseout="upBackButton(this)">
			<a href="<%= backURL.toString() %>">
				<div class="ico_back img">&nbsp;</div>
					<p><fmt:message key="view.page.back.link" /></p>
			</a>
		</li>	
	</ul>
</div>
<div id="scannerApplet">
	<!--[if !IE]>-->
		<object id="<portlet:namespace/>-applet"
			classid="java:com/aplana/scanner/ScannerApplet.class"
			type="application/x-java-applet" style="width: 900px; height: 650px;">
			<param name="mayscript" value="true"/>
			<param name="archive" value="<c:url value="/scanner-applet.jar"/>"/>
			<param name="filename" value="<%= cardIdString %>"/>
			<param name="targetUrl" value="<%= uploadUrl %>"/>
			<param name="targetAttr" value="<%= targetAttr %>"/>
			<param name="namespace" value="<portlet:namespace/>"/>
			<param name="java_arguments" value="-Xms1024m -Xmx1024m"/>
	<!--<![endif]-->
			<object id="<portlet:namespace/>-applet-ie"
				classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
				codebase="http://java.sun.com/update/1.5.0/jinstall-1_5_0_11-windows-i586.cab"
				style="width: 900px; height: 650px;">
				<param name="code" value="com/aplana/scanner/ScannerApplet"/>
				<param name="mayscript" value="true"/>
				<param name="id" value="applet-ie"/>
				<param name="archive" value="<c:url value="/scanner-applet.jar"/>"/>
				<param name="filename" value="<%= cardIdString %>"/>
				<param name="targetUrl" value="<%= uploadUrl %>"/>
				<param name="targetAttr" value="<%= targetAttr %>"/>
				<param name="namespace" value="<portlet:namespace/>"/>
			</object>
	<!--[if !IE]>-->
		</object>
	<!--<![endif]-->
</div>
	
		<%
	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());
		
	String needSign = request.getParameter("ds.need.sign");
	ObjectId materialsAttributeId = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	ObjectId attrDocAttach = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	ObjectId attrName = ObjectId.predefined(StringAttribute.class, "name");
	CardLinkAttribute materialsAttribute = (CardLinkAttribute) sessionBean.getActiveCard().getAttributeById(materialsAttributeId);
	ObjectId signatureAttributeId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
	HtmlAttribute signatureAttribute = (HtmlAttribute) sessionBean.getActiveCard().getAttributeById(signatureAttributeId);	
	if (needSign != null && needSign.length()>0){
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

		<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js?open&4" ></script>	
		<applet name="CryptoApplet"	id="CryptoApplet"
			codebase="<%=request.getContextPath()%>"
			archive="SJBCrypto.jar"
			code="com.aplana.crypto.CryptoApplet.class"	WIDTH="1" HEIGHT="1">
			<param name="signOnLoad" value="true">
			<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
			<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
			<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">
			<param name="<%=CryptoApplet.CURENT_USER_PARAMETER %>" value="<%=sessionBean.getServiceBean().getPerson().getId().getId().toString()%>">			
			<PARAM name="separate_jvm" value="true">
			<H1>WARNING!</H1>
			The browser you are using is unable to load Java Applets!
		</applet>
		<form name="signForm" method="post" action="<portlet:actionURL/>"> 
			<input id="signature" type="hidden" name="signature"/>
			<input id="signCards" type="hidden" name="signCards" value="<%=needSign%>"/>
			<input id="<%=CardPortlet.ACTION_FIELD%>" type="hidden" name="<%=CardPortlet.ACTION_FIELD%>"/>
		</form>

		<script>	
			dojo.require('dijit.form.Button');
			dojo.require('dijit.Menu');	
			dojo.require('dijit.Dialog');				
				
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
				document.signForm.signature.value = value;
				document.signForm.submit();
			}

			function submitForm_CloseCard(value) {	
				document.signForm.<%=CardPortlet.ACTION_FIELD%>.value = value;
				document.signForm.submit();
			}
				
		</script>



	<div id="signQuery" dojoType="dijit.Dialog" title="<fmt:message key="upload.apply.ds.query.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="upload.apply.ds.query.message"/></div>
		<div style="float:right; clear: both;" id="dialogButtons">
			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="upload.apply.ds.query.yes"/>
			    <script type="dojo/method" event="onClick" args="evt">
					signDocument();
					dijit.byId('signQuery').hide();
				</script>		
			</button>
			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="upload.apply.ds.query.no"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('signQuery').hide();
					submitForm_CloseCard("<%=CardPortlet.BACK_ACTION%>");
				</script>
			</button>
		</div>
	</div>
	
	<script type="text/javascript" language="javascript">
	dojo.addOnLoad(function() {
		dbmiHideLoadingSplash();
        dojo.style(dijit.byId("signQuery").closeButtonNode,"display","none");
        dojo.style(dojo.byId('scannerApplet'),'visibility','hidden');
		dijit.byId('signQuery').show();
	});
	</script>	

<%
	}
%>
		<form name="signCard" method="post" action="<portlet:actionURL/>"> 
			<input id="cardId" type="hidden" name="cardId"/>
		</form>
		
		<script>
			function sign_request(id){
				document.signCard.cardId.value = id;
				document.signCard.submit();
			}
		</script>