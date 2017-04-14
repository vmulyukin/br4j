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
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CertAttributeEditor"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>

<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>
<portlet:defineObjects/>
<% 
StringAttribute attr = (StringAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
String certAttrName = CardPortlet.getAttributeFieldName(attr);
String certHashAttrName = CertAttributeEditor.PARAM_CERTHASH;

CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
Card card = sessionBean.getActiveCard();
StringAttribute attrHash = (StringAttribute) card.getAttributeById(CertAttributeEditor.certHashAttrId);

CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());

%>

<input type="hidden"
 name="<%= certAttrName %>"
 value="<%= attr.getValue() == null ? "" : attr.getValue().trim().replaceAll("&", "&amp;").replaceAll("\"", "&quot;") %>"
/>
<input type="hidden" name="<%=certHashAttrName %>" value="<%= attrHash.getValue() == null ? "" : attrHash.getValue().trim().replaceAll("&", "&amp;").replaceAll("\"", "&quot;") %>">

<script Type ="text/javascript" language=javascript>  
	function selectFile() {
		  var appl = dojo.byId("CryptoApplet");
          var fileName = appl.getFileName("Выберите cer файл в кодировке Base64");
         
          if(fileName != ""){
          	var cObj = appl.getCryptoObjectFromFile(fileName);
          	document.forms[0].<%= certAttrName %>.value = cObj.getBase64Cert();
          	document.forms[0].<%=certHashAttrName %>.value = cObj.getCertHash();
          	dojo.byId("CertInfo").innerHTML = cObj.getSubject();          	
          }        
    }
</script>


	<div class="buttonPanel" >
			<ul>
				<li class="empty"><div>&nbsp;</div></li>
	   			<li onClick="selectFile()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
	  				<a href="#">Вставить из .cer файла</a>
	   			</li>			   
		  	</ul>
	</div>

	<applet name="CryptoApplet"	id="CryptoApplet" 	
		codebase="<%=request.getContextPath()%>"
		archive="SJBCrypto.jar" 
		code="com.aplana.crypto.CryptoApplet.class"	WIDTH=1	HEIGHT=1>
		<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
		<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
		<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">		
		<PARAM name="separate_jvm" value="true">
	<H1>WARNING!</H1>
	The browser you are using is unable to load Java Applets!
	</applet>
	
	<DIV id="CertInfo"></DIV>
	