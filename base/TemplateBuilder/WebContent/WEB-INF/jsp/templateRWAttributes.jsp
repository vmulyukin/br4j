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
<%@page import="com.aplana.dbmi.admin.WebTemplateRWAttributesBean"%>
<%@page import="com.aplana.dbmi.admin.WebTemplateBean"%>
<portlet:defineObjects/>
<% 
String baseURL = renderResponse.encodeURL(renderRequest.getContextPath()); 
WebTemplateRWAttributesBean templateRWAttributesBean = (WebTemplateRWAttributesBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplateRWAttributesPortlet.FORM.templateRWAttributesBean");
WebTemplateBean templateBean = (WebTemplateBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
  	String message = templateRWAttributesBean.getMessage();
	if( message != null) {
  		templateRWAttributesBean.setMessage(null);
  	}
boolean isEditAccessExists = templateBean.isEditAccessExists();	
%>
<fmt:setBundle basename="templates" scope="request"/>

<dbmi:errorMessage message="<%= message %>" />
<dbmi:pageTitle titleKey="templatesAttributesTitle" />

<portlet:actionURL var="formAction1">
    <portlet:param   name="portlet_action" value="editTemplateRWAttr" />
</portlet:actionURL>

<%
    if(request.getAttribute("templateRWAttributesBean") == null){
        request.setAttribute("templateRWAttributesBean", templateRWAttributesBean);
    }
    
    ContextProvider.getContext().setLocale(renderResponse.getLocale());
%>

 <div class=reportheader>
<c:set var="isEditAccessExists" value="<%=isEditAccessExists %>"/>
<dbmi:buttonPanel>
<fmt:message key="templatesApply" var="templatesApply"></fmt:message>
<fmt:message key="templatesClose" var="templatesClose"></fmt:message>
<fmt:message key="templateRWAllRoles" var="templateRWAllRoles" />
<c:if test="${isEditAccessExists}">
<dbmi:button onClick="submitFormByApply();"  text="${templatesApply}" />
</c:if>
<dbmi:linkbutton   text="${templatesClose}" >
        <portlet:renderURL>
        </portlet:renderURL>
</dbmi:linkbutton>
</dbmi:buttonPanel>
            <div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>
  <div class="reportheaderHR"></div>
</div>
            <div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>

<form:form action="${formAction1}"  method="post" commandName="templateRWAttributesBean" id="templateRWAttributesForm">

<form:hidden path="templateApplyClose"/>


<br>

<%--
<table>

     <tr>
         <td><form:radiobutton path="mode"  value="<%= WebTemplateRWAttributesBean.DEFAULT_MODE %>" onclick="disableCustomModeSelectsAndSubmitForm();"/></td>
         <td><fmt:message key="templateRWDefaultMode" /></td>
     </tr>    
     <tr>
         <td><form:radiobutton path="mode"  value="<%= WebTemplateRWAttributesBean.CUSTOM_MODE %>" onclick="enableCustomModeSelectsAndSubmitForm();" /></td>
         <td><fmt:message key="templateRWCustomMode" /></td>
     </tr>    
</table>

<table style="margin-left:30px;">
    <tr>
    	<td>&nbsp;<fmt:message key="templateRWStatus" />&nbsp;</td>
    	<td>
    		
	      <form:select path="cardStateId" onchange="this.form.submit();" disabled="${templateRWAttributesBean.defaultMode}" cssStyle="width:320px;">
		        <form:options items="${templateRWAttributesBean.cardStates}"  itemLabel="name.valueRu" itemValue="id.id"/>
	       </form:select>
    	</td>
    </tr>
    <tr>
    	<td>&nbsp;<fmt:message key="templateRWSystemRole" />&nbsp;</td>
    	<td>

   	       <form:select path="roleId" onchange="this.form.submit();" disabled="${templateRWAttributesBean.defaultMode}" cssStyle="width:320px;">
				<form:option value="<%= WebTemplateRWAttributesBean.ALL_ROLES %>" label="${templateRWAllRoles}" />
		        <form:options items="${templateRWAttributesBean.roles}"  itemLabel="nameRu" itemValue="id.id"/>
	       </form:select>
    	</td>
    </tr>
</table>
 --%>

<c:if test="${templateRWAttributesBean.rwAttributesInitialized}">
    
<table style="width : 100%;">
<col width="50%"/>
<col width="50%"/>
    <tr>
        <td style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateRWAttributesBean.blocksLeft}" />
	        <%@include file="templateRWAttributesColumn.jspf" %>
        </td>
        <td style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateRWAttributesBean.blocksRight}" />
	        <%@include file="templateRWAttributesColumn.jspf" %>
        </td>
    </tr>
    <tr>
		<td colspan="2" style="text-align: left; vertical-align: top;">
	        <c:set var="blocks" value="${templateRWAttributesBean.blocksDown}" />
	        <%@include file="templateRWAttributesColumn.jspf" %>
		</td>
	</tr>
    
</table>

</c:if>

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
    var form = document.getElementById('templateRWAttributesForm');
    form.submit();
}

function submitFormByApply(){
    var tac = document.getElementById('templateApplyClose');
    tac.value="APPLY_TEMPLATE";
	submitForm();
}

function disableCustomModeSelectsAndSubmitForm(){
	disableCustomModeSelects(true);
	submitForm();
}
function enableCustomModeSelectsAndSubmitForm(){
	disableCustomModeSelects(false);
	submitForm();
}
function disableCustomModeSelects(disable){
	document.getElementById('templateApplyClose').value=null;
    var selCardState = document.getElementById('cardStateId');
    var selRole = document.getElementById('roleId');
    selCardState.disabled=disable;
    selRole.disabled=disable;
}

</script>