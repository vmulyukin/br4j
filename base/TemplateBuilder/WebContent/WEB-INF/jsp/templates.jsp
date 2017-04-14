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
<%@page  contentType="text/html"  pageEncoding="windows-1251" import="java.util.*,javax.portlet.*,com.aplana.dbmi.model.* " %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="com.aplana.dbmi.admin.WebTemplateBean"%>
<portlet:defineObjects/>
<% String baseURL = renderResponse.encodeURL(renderRequest.getContextPath()); %>
<fmt:setBundle basename="templates" scope="request"/>

<%
    WebTemplateBean templateBean = (WebTemplateBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
    if(request.getAttribute("com.aplana.mts.mi.admin.TemplatesPortlet.FORM.templateBean") == null){
        request.setAttribute("com.aplana.mts.mi.admin.TemplatesPortlet.FORM.templateBean", templateBean);
    }
	boolean isEditAccessExists = templateBean.isEditAccessExists();	
    ContextProvider.getContext().setLocale(renderResponse.getLocale());
    
  	String message = templateBean.getMessage();
	if( message != null) {
  		templateBean.setMessage(null);
  	}
 %>

<script type="text/javascript">
    function submitTemplatesForm(action){
        var form = document.getElementById("templatesForm");
        form.templateAction.value = action;
        form.submit();
    }
    
    function clickMoreInfo(){
        var actionURL = "";
        var form = document.getElementById("templatesForm");
        <c:choose>
            <c:when   test="${templateBean.changed}">
            	<fmt:message key="templatesSave" var="templatesSave"/>
				if (confirm('${templatesSave}?')) {
					actionURL='<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateAttr"/><portlet:param name="template_id" value="1"/></portlet:renderURL>';
					form.setAttribute('action', actionURL);
					form.submit();
                }
            </c:when>
			<c:otherwise>
				actionURL='<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateAttr"/></portlet:renderURL>';
				form.setAttribute('action', actionURL);
				form.submit();
			</c:otherwise>
		</c:choose>
    }

    function clickRWAttributesInfo(){
        var actionURL = "";
        var form = document.getElementById("templatesForm");
        <c:choose>
            <c:when   test="${templateBean.changed}">
            	<fmt:message key="templatesSave" var="templatesSave"/>
				if (confirm('${templatesSave}?')) {
					actionURL='<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateRWAttr"/><portlet:param name="template_id" value="1"/></portlet:renderURL>';
					form.setAttribute('action', actionURL);
					form.submit();
                }
            </c:when>
			<c:otherwise>
				actionURL='<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateRWAttr"/></portlet:renderURL>';
				form.setAttribute('action', actionURL);
				form.submit();
			</c:otherwise>
		</c:choose>
    }

    function clickMoreInfoRedirect() {
    	var form = document.getElementById("templatesForm");    	
    	form.redirectURL.value = '<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateAttr"/></portlet:renderURL>';
    	form.templateAction.value = '<%=WebTemplateBean.EDIT_TEMPLATE_ATTR_ACTION%>';
    	form.submit();
    }

    function clickRWAttributesInfoRedirect() {
    	var form = document.getElementById("templatesForm");    	
    	form.redirectURL.value = '<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateRWAttr"/></portlet:renderURL>';
    	form.templateAction.value = '<%=WebTemplateBean.EDIT_TEMPLATE_RW_ATTR_ACTION%>';
    	form.submit();
    }

    function clickWorkflowMoveRequiredAttrRedirect() {
    	var form = document.getElementById("templatesForm");    	
    	form.redirectURL.value = '<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateWorkflowMovesReqAttr"/><portlet:param name="action" value="init"/></portlet:renderURL>';
    	form.templateAction.value = '<%=WebTemplateBean.EDIT_TEMPLATE_WORFLOW_MOVE_REQ_ATTR_ACTION%>';
    	form.submit();
    }
    
    function clickEditAccess() {
    	var form = document.getElementById("templatesForm");    	
    	form.redirectURL.value = '<portlet:renderURL><portlet:param name="portlet_action" value="editTemplateAccess"/></portlet:renderURL>';
    	form.templateAction.value = '<%=WebTemplateBean.EDIT_TEMPLATE_ACCESS_ACTION %>';
    	form.submit();
    }
</script>

<dbmi:errorMessage message="<%= message %>" />
<dbmi:pageTitle titleKey="templatesTitle" />
<portlet:actionURL var="formAction">
</portlet:actionURL>
<c:set var="isEditAccessExists" value="<%=isEditAccessExists %>"/>
<form:form method="post" commandName="templateBean" action="${formAction}" id="templatesForm">
    <form:hidden path="realId"/>
    <form:hidden path="templateAction"/>
    <form:hidden path="redirectURL"/>
    <!-- 
    <input type="hidden" name="portlet_action" value="" />
    <input type="hidden" name="save_template" value="" />
     -->
    <c:if test="${isEditAccessExists and not empty templateBean.realId}">
        <div class="reportheader">
            <dbmi:buttonPanel>
					<fmt:message key="templatesSave" var="templatesSave"></fmt:message>
					<fmt:message key="templatesClose" var="templatesClose"></fmt:message>
					<dbmi:button onClick="submitTemplatesForm('SAVE_ACTION')"  text="${templatesSave}" />
					<dbmi:linkbutton   text="${templatesClose}" >
					    <portlet:actionURL>
					        <portlet:param name="templateAction" value="CLOSE_ACTION"/>
					    </portlet:actionURL>
					</dbmi:linkbutton>
            </dbmi:buttonPanel>
            <div class="reportheaderHR"></div>
        </div>
    </c:if>

<table style="width : 100%;">
	<col width="50%"/>
	<col width="50%"/>
	<tbody>
	    <tr>
            <td style="text-align: left;">
               <dbmi:partitionCaption messageKey="templatesListCaption" />                              
               <div class="divPadding">
                   <table class="content">
	                   <col width="60%"/>
	                   <col width="20%"/> 
	                   <col width="10%"/>                    
	                   <col width="10%"/>                                       
                       <c:forEach items="${templateBean.templates}" var="template">
                       <c:choose>
                           <c:when test="${templateBean.id.id == template.id.id}">
                               <tr class="alternate" >
                           </c:when>
                           <c:when test="${not template.active}">
                               <tr class="inactive">
                           </c:when>
                           <c:otherwise>
                               <tr class="normal" >
                           </c:otherwise>
                       </c:choose>
                       <td>${template.name}</td>
                       <td>
                       <c:choose>
                            <c:when test="${template.active}">
                                <fmt:message key="templatesActive" />                               
                            </c:when>
                            <c:otherwise>
                                <fmt:message key="templatesPassive" />                                                          
                            </c:otherwise>
                       </c:choose> 
                       </td>
                       <td style="vertical-align: top;">
                           <dbmi:linkimage enable="${isEditAccessExists and template.active and empty templateBean.realId}"
                               urlPrefix="<%= renderRequest.getContextPath() %>"
                               enableIcon="edit" disableIcon="edit_disable">
                                 <portlet:actionURL>
                                     <portlet:param name="edit_id" value="${template.id.id}" />                                              
                                 </portlet:actionURL>
                           </dbmi:linkimage>
                       </td>
                       <td>
                           <dbmi:linkimage enable="${isEditAccessExists and template.active and empty templateBean.realId}"
                               urlPrefix="<%= renderRequest.getContextPath() %>"
                               enableIcon="delete" disableIcon="delete_disable">
                                 <portlet:actionURL>
                                     <portlet:param name="delete_id" value="${template.id.id}" />                                              
                                 </portlet:actionURL>
                           </dbmi:linkimage>
                       </td>
                   </tr>
               </c:forEach>
               <tr>
                   <td colspan="4" align="right">
                       <dbmi:buttonPanel>
                               <fmt:message key="templatesAddTemplate" var="templatesAddTemplate" />
                               <c:if test="${isEditAccessExists}">
                               <dbmi:linkbutton text="${templatesAddTemplate}" enable="${empty templateBean.realId}">
			                       <portlet:actionURL>
							           <portlet:param   name="new_id" value="-1" />
							       </portlet:actionURL>
                               </dbmi:linkbutton>
                               </c:if>
                       </dbmi:buttonPanel>
                   </td>
               </tr>
           </table>
        </div>
        </td>
        <td style="text-align: left;" valign="top">
            <c:if test="${templateBean.showTemplate}">
                <c:set target="${templateBean}" property="redirectURL">
                    <portlet:renderURL>
                        <portlet:param   name="action" value="editTemplateAttr" />
                     </portlet:renderURL>
                </c:set>
                <dbmi:partitionCaption messageKey="templatesEditTemplate" />                              
                <div class="divPadding">
                    <table class="content">
                        <col width="50%"/>
                        <col width="50%"/> 
                        <tr>
                            <td>
                                <fmt:message key="templatesTemplateNameRu" /> 
                            </td>
                            <td>
                                <form:input path="nameRu"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <fmt:message key="templatesTemplateNameEn" /> 
                            </td>
                            <td>
                                <form:input path="nameEn"/>
                            </td>
                        </tr>
                        <tr>
                        	<td>
                        		<fmt:message key="dbmiTemplatesWorkflow" />
                        	</td>
                        	<td>                        		
                        		<form:select path="workflowId" items="${templateBean.workflows}" itemLabel="name" itemValue="id.id"/>
                        	</td>
                        </tr>
                        <tr>
                        	<td>
                        		<fmt:message key="templatesShowInSearch" />
                        	</td>
                        	<td>                        		
                        		<form:checkbox path="showInSearch"  />
                        	</td>
                        </tr>
                        <tr>
                        	<td>
                        		<fmt:message key="templatesShowInCreateCard" />
                        	</td>
                        	<td>                        		
                        		<form:checkbox path="showInCreateCard"  />
                        	</td>
                        </tr>
                        <tr>
                            <td align="right">
                            </td>
                            <td>
                                <dbmi:buttonPanel>
						                <fmt:message key="templatesRWAttributes" var="templatesRWAttributes"/>                                          
						                <dbmi:button  onClick="return clickRWAttributesInfoRedirect();" text="${templatesRWAttributes}"/>
						                <fmt:message key="templatesMoreInfo" var="templatesMoreInfo"/>                                          
						                <dbmi:button  onClick="return clickMoreInfo();" text="${templatesMoreInfo}" />
						                <fmt:message key="templateWorkflowMovesReqAttr" var="templateWorkflowMovesReqAttr"/>                                          
						                <dbmi:button  onClick="return clickWorkflowMoveRequiredAttrRedirect();" text="${templateWorkflowMovesReqAttr}"/>
						                <fmt:message key="templatesEditAccess" var="templatesEditAccess"/>                                          
						                <c:if test="${isEditAccessExists}">
						                <dbmi:button  onClick="return clickEditAccess();" text="${templatesEditAccess}" />
						                </c:if>
                                </dbmi:buttonPanel>
                            </td>
                        </tr>
                    </table>
                </div> 
                <dbmi:partitionCaption messageKey="templatesBlocksPosition" />                              
                <div class="divPadding">                
	                <table class="content">
	                    <col width="50%"/>
	                    <col width="50%"/>                
	                    <tr>
	                        <td valign="top">
	                            <table>
	                                <c:forEach var="block1" items="${templateBean.blocksLeft}"   varStatus="status" >
	                                    <tr>
	                                        <td>
	                                            ${block1.name}
	                                        </td>    
	                                        <td align="left">
	                                            <ap:blockmove block="${block1}"  block_number="0" urlPrefix="<%= renderRequest.getContextPath() %>" status="${status}" position="1"/>
	                                        </td>
	                                        <td>
	                                            <dbmi:linkimage enable="${isEditAccessExists and !block1.system}"
	                                                urlPrefix="<%= renderRequest.getContextPath() %>"
	                                                enableUrl="/images/delete.gif" disableUrl="/images/delete_disable.gif" >
	                                                <portlet:actionURL>
	                                                    <portlet:param   name="block_remove_id" value="${block1.id.id}" />
	                                                    <portlet:param   name="block_column" value="0" />                  
	                                                </portlet:actionURL>
	                                            </dbmi:linkimage>
	                                        </td>
	                                    </tr>
	                                </c:forEach>
	                            </table>
	                        </td>
	                        <td valign="top">
	                            <table>
	                                <c:forEach var="block2" items="${templateBean.blocksRight}"  varStatus="status" >
	                                    <tr>
	                                        <td>
	                                            ${block2.name}
	                                        </td>
	                                        <td>
	                                            <ap:blockmove block="${block2}" block_number="1" urlPrefix="<%= renderRequest.getContextPath() %>" status="${status}" position="2"/>
	                                        </td>
	                                        <td>
	                                            <dbmi:linkimage enable="${isEditAccessExists and !block2.system}"
	                                                urlPrefix="<%= renderRequest.getContextPath() %>"
	                                                enableUrl="/images/delete.gif" disableUrl="/images/delete_disable.gif" >
	                                                <portlet:actionURL>
	                                                    <portlet:param   name="block_remove_id" value="${block2.id.id}" />
	                                                    <portlet:param   name="block_column" value="1" />                  
	                                                </portlet:actionURL>
	                                            </dbmi:linkimage>
	                                        </td>
	                                    </tr>
	                                </c:forEach>
	                            </table>
	                        </td>
	                    </tr>
	                    <tr>
	                    	<td colspan="2">
	                    		<hr>
	                            <table align="center"><%--style="margin: 10px 50px 0px 50px"--%>
	                                <c:forEach var="block3" items="${templateBean.blocksDown}"  varStatus="status" >
	                                    <tr>
	                                        <td>
	                                            ${block3.name}
	                                        </td>
	                                        <td>
	                                            <ap:blockmove block="${block3}" block_number="2" urlPrefix="<%= renderRequest.getContextPath() %>" status="${status}" position="3"/>
	                                        </td>
	                                        <td>
	                                            <dbmi:linkimage enable="${isEditAccessExists and !block3.system}"
	                                                urlPrefix="<%= renderRequest.getContextPath() %>"
	                                                enableUrl="/images/delete.gif" disableUrl="/images/delete_disable.gif" >
	                                                <portlet:actionURL>
	                                                    <portlet:param   name="block_remove_id" value="${block3.id.id}" />
	                                                    <portlet:param   name="block_column" value="2" />                  
	                                                </portlet:actionURL>
	                                            </dbmi:linkimage>
	                                        </td>
	                                    </tr>
	                                </c:forEach>
	                            </table>
	                    	</td>
	                    </tr>
	                    <tr>
	                        <td>
	                            <form:select path="blockId" >
	                                <form:options items="${templateBean.blockItems}" itemLabel="labelRu" itemValue="value"/>
	                            </form:select>
	                        </td>
	                        <td>
	                            <dbmi:buttonPanel>
	                                    <fmt:message key="templatesAddBlock" var="templatesAddBlock"></fmt:message>
						                <c:if test="${isEditAccessExists}">
	                                    <dbmi:button onClick="submitTemplatesForm('BLOCK_ACTION')"  text="${templatesAddBlock}" />
	                                    </c:if>                        
	                            </dbmi:buttonPanel>
	                        </td>                        
	                    </tr>
	                </table>
	            </div>
	        </c:if>
	    </td>
	</tr>
</tbody>    
</table>
</form:form>
