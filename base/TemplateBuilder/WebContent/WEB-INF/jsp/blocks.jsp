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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="tree" uri="/WEB-INF/tags/treetag.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="com.aplana.dbmi.admin.WebBlockBean"%>
<portlet:defineObjects/>

<% 
String baseURL = renderResponse.encodeURL(renderRequest.getContextPath()); 
WebBlockBean blockBean = (WebBlockBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.BlocksPortlet.FORM.blockBean");
  	String message = blockBean.getMessage();
	if( message != null) {
  		blockBean.setMessage(null);
  	}
    ContextProvider.getContext().setLocale(renderResponse.getLocale());
	boolean isEditAccessExists = blockBean.isEditAccessExists();	
%>
<c:set var="bundleBasename" value="templates" scope="request" />
<c:set var="searchBean" value="${blockBean.webSearchBean}"  scope="request"/>
<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />
<fmt:setBundle basename="${bundleBasename}" scope="request"/>
<dbmi:errorMessage message="<%= message %>" />
<dbmi:pageTitle titleKey="templatesBlocksTitle" />

<portlet:actionURL var="formAction">
    <portlet:param name="portlet_action" value="editBlocks"/>
</portlet:actionURL>
<c:set var="isEditAccessExists" value="<%=isEditAccessExists %>"/>
<c:if test="${not empty blockBean.realId}">
    <div class="reportheader">
        <dbmi:buttonPanel>
                <fmt:message key="templatesSave" var="templatesSave" />
                <fmt:message key="templatesClose" var="templatesClose" />
                <c:if test="${isEditAccessExists}">
                <dbmi:button text="${templatesSave}"  onClick="OnSubmit('SAVE')" />
                </c:if>
                <dbmi:linkbutton text="${templatesClose}" >
                    <portlet:actionURL>
                        <portlet:param name="action" value="BLOCKS_CLOSE"/>
                    </portlet:actionURL>
                </dbmi:linkbutton>
        </dbmi:buttonPanel>
        <div class="reportheaderHR"></div>
    </div>
</c:if>

<form:form action="${formAction}" method="post" commandName="blockBean" id="blocksForm">
    <form:hidden path="realId"/>
    <form:hidden path="action" id="blocksAction"/>
    <form:hidden path="tmpId" id="tmpId"/>

<form:hidden id="IsExtendedSearch" path="webSearchBean.isExtendedSearch"/>
<form:hidden id="IsAttributeSearch" path="webSearchBean.isAttributeSearch"/>
<form:hidden id="Action" path="webSearchBean.action"/>

    <table style="width : 100%; clear: both;">
        <col width="30%"/>
        <col width="70%"/>
        <tr>
            <td style="text-align: left;">
               <dbmi:partitionCaption messageKey="templatesBlockList" />                              
               <div class="divPadding">
               <table class="content">
                   <col width="60%"/>
                   <col width="20%"/> 
                   <col width="10%"/>                    
                   <col width="10%"/>                                       
                    <c:forEach items="${blockBean.blocks}" var="block">
                        <c:choose>
                            <c:when test="${blockBean.realId == block.id.id}">
                             <tr class="alternate">
                            </c:when>
                            <c:when test="${not (block.active and empty blockBean.realId)}">
                                <tr class="inactive">
                            </c:when>
                            <c:otherwise>
                            <tr class="normal">
                            </c:otherwise>
                        </c:choose>
                  <td style="padding-left: 10px;">
                      ${block.name}
                  </td>
                  <td>
                      <c:choose>
                          <c:when test="${block.active}">
                              <fmt:message key="templatesActive" />                               
                          </c:when>
                          <c:otherwise>
                              <fmt:message key="templatesPassive" />                               
                          </c:otherwise>
                      </c:choose>
                  </td>
                  <td style="vertical-align: top;">
                      <dbmi:linkimage enable="${isEditAccessExists and block.active and empty blockBean.realId}"
                          urlPrefix="<%= renderRequest.getContextPath() %>"
                          enableIcon="edit" disableIcon="edit_disable" >
                              <portlet:actionURL>
                                  <portlet:param name="block_edit_id"  value="${block.id.id}"/>
                              </portlet:actionURL>#blockAttributesPanel
                      </dbmi:linkimage>        
                  </td>
                  <td>
                      <dbmi:linkimage enable="${isEditAccessExists and block.active and empty blockBean.realId}"
                          urlPrefix="<%= renderRequest.getContextPath() %>"
                          enableIcon="delete" disableIcon="delete_disable" >
                              <portlet:actionURL>
                                  <portlet:param name="block_delete_id"  value="${block.id.id}"/>
                              </portlet:actionURL>
                      </dbmi:linkimage>        
                  </td>
              </tr>
              </c:forEach>
			<c:if test="${isEditAccessExists}">
              <tr>
                  <td colspan="4" aling="right"> 
                     <dbmi:buttonPanel>
                             <fmt:message key="templatesAddBlock" var="blockText" />                              
                             <dbmi:linkbutton enable="${empty blockBean.realId}" text="${blockText}">
                                 <portlet:actionURL>
                                     <portlet:param name="block_new_id"  value="-1"/>
                                 </portlet:actionURL>                           
                             </dbmi:linkbutton>
                     </dbmi:buttonPanel>
                  </td>
                  </tr>
             </c:if>
                </table>
               </div>                
<c:if test="${not empty blockBean.realId}">
<div id="blockAttributesPanel">
<%@include file="blockAttributesPanel.jspf" %>
</div>                
</c:if>
                
            </td>
            <td style="text-align: left;" valign="top">
<%@include file="attributePanel.jspf" %>
            </td>

        </tr>

    </table>
</form:form>



<script type="text/javascript">
function OnSubmit(action){
    var actionEl  = document.getElementById("blocksAction");
    actionEl.value = action;
    var blocksFormEl = document.getElementById('blocksForm');
    blocksFormEl.submit();
}

function OnAttributeType(value){
    var fromAttributeDivEl = document.getElementById('fromAttributeDiv');
    if(value == 'tree'){
        fromAttributeDivEl.style.display = 'block';
    } else{
        fromAttributeDivEl.style.display = 'none';
    }
}

function OnTmpId(action, id){
    var tmpIdEl = document.getElementById("tmpId");
    tmpIdEl.value = id;
    OnSubmit(action);
}

function <portlet:namespace/>OnAttributeSearch(curAction){
    var isAttributeSearch = document.getElementById('IsAttributeSearch');
    if(isAttributeSearch.value =='' 
       || isAttributeSearch.value =='false'){
           isAttributeSearch.value='true';
       }  else{
          isAttributeSearch.value='false';
       }
       var action = document.getElementById('Action');       
       action.value=curAction;
       <portlet:namespace/>SubmitSearchForm();
       return false;
}


function <portlet:namespace/>SubmitSearchForm(){
      document.getElementById('blocksForm').submit();
}
    
</script>
