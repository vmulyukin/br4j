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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="java.security.cert.X509Certificate"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<portlet:defineObjects/>

<%	 
  		
	StringAttribute attr = (StringAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);	
	
	if (attr == null || attr.getValue() == null || attr.getValue().length() == 0){	
		%><span>Сертификат отсутствует</span><%
	}else{		
		try{			
			X509Certificate cert = (X509Certificate)CryptoLayer.getInstance().getCertFromStringBase64(attr.getStringValue());
			%><br/><br/>
			<TABLE cellspacing=0 cellpadding=0>			
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
		}catch(Exception e){			
			%><span><%=ContextProvider.getContext().getLocaleMessage("signature.error.cert")%></span><%
		}			
	}
%>