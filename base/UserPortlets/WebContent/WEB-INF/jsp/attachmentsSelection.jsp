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

<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="java.util.*"%>
<%@page import="com.aplana.dbmi.crypto.*"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.crypto.CryptoApplet"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>


<%@page import="com.aplana.dbmi.crypto.SignatureData"%><portlet:defineObjects />

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

	String message = sessionBean.getMessage();
	if( message != null) {
		sessionBean.setMessage(null);
	} else {
		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
	}

	String cardId = sessionBean.getActiveCard().getId() != null ? sessionBean.getActiveCard().getId().getId().toString() : "";  
	Card card = sessionBean.getActiveCard();  

	%> 
	               
<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js"></script>	               
<script type="text/javascript" language="javascript">

function submitForm(action) { 
	document.attachListEditor.<%= CardPortlet.ACTION_FIELD %>.value = action;
	document.attachListEditor.submit();
}

function getSignature(){

	var ids = [];
	var hashesToSign = [];
	var attrXmls = []; 
	var data = [];

<%	
List attachments = (List) renderRequest.getAttribute("resInfo");
if(null != attachments && attachments.size() > 0) {
	StringBuffer ids = new StringBuffer("["); 
	StringBuffer hashesToSign = new StringBuffer("[");
	StringBuffer attrXmls = new StringBuffer("[");
	StringBuffer data = new StringBuffer("[");
	for(int i = 0; i < attachments.size(); i ++) {
		AttachInfo attachment = (AttachInfo) attachments.get(i);
		if(i > 0) {
			data.append(",");
			ids.append(",");
			hashesToSign.append(",");	
			attrXmls.append(",");	
		}
		data.append("\"").append(attachment.getData()).append("\"");
		ids.append("\"").append(attachment.getAttId()).append("\"");
		hashesToSign.append("\"").append(attachment.getHash()).append("\"");
		attrXmls.append("'").append(attachment.getAttrXML()).append("'");
	}
	ids.append("]");
	hashesToSign.append("]");
	attrXmls.append("]");
	data.append("]");
%>	
	ids = <%=ids%>;
	hashesToSign = <%=hashesToSign%>;
	attrXmls = <%=attrXmls%>;
	data = <%=data%>;
<%
}
%>
	
	var args = {
			stringsArrayHash: hashesToSign, 
			stringsArrayData: data,
   			signAttrXML: attrXmls
   	   		};


  	var signResult = cryptoGetSignature(args);  
	if(signResult.success){		
		if(ids.length > 0) {
			for(i = 0; i < ids.length; i ++) {
				document.getElementById(ids[i] + "_Signature").value = signResult.signature[i];
			}
		}		
		submitForm('<%=DSAttachmentList.SIGN_AND_CLOSE_ACTION%>')
	} else{
		if(signResult.msg == "noapplet"){			
			msg = "Апплет не инициализирован"
		} else if(signResult.msg == "nofields"){
			msg = "нет подписываемых аттрибутов";
		} else {
			msg = signResult.msg;
		}
		if(msg && msg.length > 0) {
			alert(msg);
		}		
	}
}			
</script>

<applet name="CryptoApplet"	id="CryptoApplet"
			codebase="<%=request.getContextPath()%>"
			archive="SJBCrypto.jar" 
			code="com.aplana.crypto.CryptoApplet.class"	WIDTH="1" HEIGHT="1">
			<param name="signOnLoad" value="false">
			<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
			<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
			<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">
			<param name="<%=CryptoApplet.CURENT_USER_PARAMETER %>" value="<%=sessionBean.getServiceBean().getPerson().getId().getId().toString()%>">			
			<PARAM name="separate_jvm" value="true">
			<H1>WARNING!</H1>
			The browser you are using is unable to load Java Applets!
</applet>

<dbmi:message text="<%= message %>"/>

<form name="attachListEditor" method="post" action="<portlet:actionURL/>">
<input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">  

<table class="indexCardMain">
    <col Width="50%" />
    <col Width="50%" />
    <tr>
      <!--Заголовок-->
      <td>
      </td>
      <td>
          <div class="buttonPanel" >
           <fmt:message key="edit.page.cancel.btn" var="choiceClose" />
           <fmt:message key="ds.sign.attachments" var="choiceSave" />
           <ul>
           	 <c:set var="submitAction">submitForm('<%=DSAttachmentList.SIGN_AND_CLOSE_ACTION%>')</c:set>
             <ap:button text="${choiceSave}"  onClick="getSignature()" />
          	 <c:set var="submitAction">submitForm('<%=DSAttachmentList.BACK_ACTION%>')</c:set>
             <ap:button text="${choiceClose}"  onClick="${submitAction}" />
           </ul>
          </div>
      </td>    
    </tr>
    </table>
    
       <table width="100%" style="margin: 10px;">
       <tr>
          <td style="vertical-align: top;">
                <div class="divCaption"></div>
  <display:table name="resInfo" id="selectedItem" uid="selectedItem" sort="list" class="res">

<%
	AttachInfo item = (AttachInfo) pageContext.getAttribute("selectedItem");
%>
	<display:column maxLength="8" title="Выбор">
		<input type="checkbox" name="checkbox" value="<%=item.getAttId()%>" <%=item.isPrime() ? "checked=\"\"" : ""%>/>
	</display:column>
	<display:column title="Наименование" sortable="true" property="attachText"/>	
	<display:column class="hidden" headerClass="hidden">
		<input type="hidden" id="<%=item.getAttId()%>_Signature" name="<%=item.getAttId()%>_Signature"/>
	</display:column>

  </display:table>
            
            </td>
        </tr>
	</table>
</form>

<jsp:include page="html/CardPageFunctions.jsp"/>

