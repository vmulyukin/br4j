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
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.crypto.SignatureData"%>
<%@page import="com.aplana.dbmi.crypto.Base64Encoder"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Vector"%>
<%@page import="java.util.List"%>
<%@page import="org.ajax4jsf.javascript.ScriptUtils"%>
<%@page import="java.security.cert.*"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%> 
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>
<portlet:defineObjects/>
<% 
CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
StringAttribute attr = (StringAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);

	%>
	<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>
	
	<input id="signature" type="hidden" class="attrString"
	 name="<%= JspAttributeEditor.getAttrHtmlId(attr) %>"
	 value="<%= attr.getValue() == null ? "" : attr.getValue().trim().replaceAll("&", "&amp;").replaceAll("\"", "&quot;") %>"
	/>

	<div style="display:block"><span id="signingInfo"></span></div>

<%
	Card card = sessionBean.getActiveCard();
	HtmlAttribute signAttr = (HtmlAttribute) card.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature"));
	
	if (signAttr == null || signAttr.getValue() == null || signAttr.getValue().length() == 0){	
		%><span>Подпись отсутствует</span><%
	}else{
		
		List<SignatureData> signatureDatas = SignatureData.getAllSignaturesInfo(signAttr.getStringValue(), card);
		
		for (int i=0; i<signatureDatas.size(); i++){
			SignatureData signData = signatureDatas.get(i);
			
			boolean result = signData.verify(sessionBean.getServiceBean(), true);
			
			Card signer = signData.getSigner();
			X509Certificate cert = signData.getCert509();
			if (signer == null && cert == null){
				%><span>Не установлен владелец подписи</span><br/><br/><%
			}else{	
			
				if (signer != null){
					String signerName = signer.getAttributeById(new ObjectId(StringAttribute.class, "NAME")).getStringValue();
					%><span><i><%=signerName%></i></span><br/><br/><%
				}else{					
					%><span>Не установлен владелец подписи</span><br/><br/><%					
				}
					
					
				if(result){
					%><span style="color:green">Подпись верна</span>	<%
				}else{
					if(signData.getMessage().length() > 0){
						%><span style="color:red"><%=signData.getMessage()%></span>	<%
					}else{
						%><span style="color:red; font-size:14px; font-weight:bold">Подпись не верна!</span>	<%
					}				
				}
				if(cert!= null){
					%><br/><br/>
					<TABLE cellspacing=0 cellpadding=0>
					<TR><TD colspan=2><b>Сертификат</b></TD>
					<TR><TD width=100>Кем выдан:</TD>
					<TD><%=cert.getIssuerX500Principal().toString()%></TD></TR>
					<TR><TD width=100>Кому выдан:</TD>
					<TD><%=cert.getSubjectX500Principal().toString()%></TD></TR>					
					<TR><TD width=100>Номер:</TD>
					<TD><%=cert.getSerialNumber()%></TD></TR>
					<TR><TD width=100>Алгоритм:</TD>
					<TD><%=cert.getSigAlgName()%></TD></TR>
					<TR><TD width=100>Действителен:</TD>
					<TD>с&nbsp; <%=cert.getNotBefore().toLocaleString()%> 
					<br>по <%=cert.getNotAfter().toLocaleString()%>
					</TD></TR>
					</TABLE>
					<%		
				}else{
					%><span>сертификат не найден</span><%
				}
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
%>	