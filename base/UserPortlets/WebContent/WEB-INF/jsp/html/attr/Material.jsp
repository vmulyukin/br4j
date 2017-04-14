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
<%@page import="org.springframework.web.context.request.RequestScope"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.MaterialAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>
<portlet:defineObjects/>

<%
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	MaterialAttribute attr = (MaterialAttribute) request.getAttribute(MaterialAttributeEditor.ATTR_ATTRIBUTE);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
%>

<script type="text/javascript">
	function changeMaterialType_File() {
		document.getElementById('file').value=''; 
		document.getElementById('fileUploadBtn').disabled = 'true'; 
		document.getElementById('link').removeAttribute('disabled');
	}     
	
	function changeMaterialType_Link() {
		document.getElementById('fileUploadBtn').removeAttribute('disabled');
		document.getElementById('link').value=''; 
		document.getElementById('link').disabled = 'true';
	}     

	function submitForm_Upload(){
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= MaterialAttributeEditor.UPLOAD_FILE_ACTION %>';     
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();  		
	}
</script>

<table class="content">
    <col Width="25%" />
    <col Width="65%" />
    <col Width="10%"/>
    <tr id="row1">
      <td>       
          <c:if test="${requestScope.islinkEnabled}">
	         <input type="radio" onclick="javascript:changeMaterialType_Link();"  name="RadioButton1"  id="RadioButton1" 
	         <c:if test="${requestScope.fileMaterial}">checked="checked"</c:if>/>
         </c:if>
        <label>
          <fmt:message key="edit.material.block.file.label" />
        </label> 
      </td>
      <td>
        <input id="file" disabled value="<%= sessionBean.getActiveCard().getFileName() == null ? "" :  sessionBean.getActiveCard().getFileName() %> "/>&nbsp;&nbsp;&nbsp;
        <input name="fileUploadBtn" id="fileUploadBtn"  type="button" 
         value="<% if (sessionBean.getActiveCard().getFileName() == null) { %><fmt:message key="edit.material.block.load.btn" /><% } else { %><fmt:message key="edit.material.block.reload.btn" /><% } %>"  
         onclick="javascript:submitForm_Upload();" <c:if test="${not requestScope.fileMaterial}">disabled</c:if>/>
      	
      </td>
      <td>
      </td>                        
    </tr>
    <c:if test="${requestScope.islinkEnabled}">
    <tr id="row2">
      <td>       
        <input type="radio" onclick="javascript:changeMaterialType_File();" name="RadioButton1" id="RadioButton2"
         <c:if test="${not requestScope.fileMaterial}">checked="checked"</c:if>/>
        <label for="RadioButton2">
          <fmt:message key="edit.material.block.link.label" />
        </label>                   
      </td>
      <td>
        <input name="<%= CardPortlet.getAttributeFieldName(attr) + MaterialAttributeEditor.ID_URL %>" type="text" id="link" 
        <c:if test="${requestScope.fileMaterial}">disabled</c:if>             
         value="<%= sessionBean.getActiveCard().getUrl() == null ? "" :  sessionBean.getActiveCard().getUrl() %> " />
      </td>
      <td>
      </td>
    </tr>
	</c:if>
</table>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
