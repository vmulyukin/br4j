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
<%@page  contentType="text/html"  pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.model.* " %>
<%@ taglib prefix="c" uri="/WEB-INF/tags/c.tld" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="com.aplana.dbmi.admin.WebTemplateAttributesBean"%>
<%@page import="com.aplana.dbmi.admin.WebTemplateBean"%>
<portlet:defineObjects/>
<% 
String baseURL = renderResponse.encodeURL(renderRequest.getContextPath()); 
WebTemplateAttributesBean templateAttributesBean = (WebTemplateAttributesBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplateAttributesPortlet.FORM.templateAttributesBean");
WebTemplateBean templateBean = (WebTemplateBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
  	String message = templateAttributesBean.getMessage();
	if( message != null) {
  		templateAttributesBean.setMessage(null);
  	}
%>
<fmt:setBundle basename="templates" scope="request"/>

<dbmi:errorMessage message="<%= message %>" />
<dbmi:pageTitle titleKey="templatesAttributesTitle" />

<portlet:actionURL var="formAction1">
    <portlet:param   name="portlet_action" value="editTemplateAttr" />
</portlet:actionURL>

<%
    if(request.getAttribute("templateAttributesBean") == null){
        request.setAttribute("templateAttributesBean", templateAttributesBean);
    }
    
    ContextProvider.getContext().setLocale(renderResponse.getLocale());
%>

 <div class=reportheader>
<dbmi:buttonPanel>
<fmt:message key="templatesSave" var="templatesSave"></fmt:message>
<fmt:message key="templatesClose" var="templatesClose"></fmt:message>
<dbmi:button onClick="submitForm();"  text="${templatesSave}" />
<dbmi:linkbutton   text="${templatesClose}" >
        <portlet:renderURL>
        </portlet:renderURL>
</dbmi:linkbutton>
</dbmi:buttonPanel>
            <div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>
  <div class="reportheaderHR"></div>
</div>
            <div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>

<form:form action="${formAction1}"  method="post" commandName="templateAttributesBean" id="templateAttributesForm">
<table style="width : 100%;">
<col width="50%"/>
<col width="50%"/>
    <tr>
        <td style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateAttributesBean.blocksLeft}" />
	        <%@include file="templateAttributesColumn.jspf" %>
        </td>
        <td style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateAttributesBean.blocksRight}" />
	        <%@include file="templateAttributesColumn.jspf" %>
        </td>
    </tr>
    <tr>
		<td colspan="2" style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateAttributesBean.blocksDown}" />
	        <%@include file="templateAttributesColumn.jspf" %>
		</td>
	</tr>
    
</table>
</form:form>
<script type="text/javascript">
function OnInResult(id){
    var widthEl = document.getElementById("width_" + id);
    var inresultEl = document.getElementById("inresult_" + id);
    var numberEl = document.getElementById("number_" + id);
    numberEl.disabled = ! inresultEl.checked;
    widthEl.disabled = ! inresultEl.checked;
}

function submitForm(){
    var form = document.getElementById('templateAttributesForm');
    form.submit();
}
</script>