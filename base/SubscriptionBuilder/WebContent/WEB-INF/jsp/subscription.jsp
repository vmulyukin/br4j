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
<%@page  contentType="text/html"  pageEncoding="UTF-8" import="com.aplana.dbmi.search.*" %>
<%@page import="com.aplana.dbmi.model.web.*" %>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/tld/treetag.tld" prefix="tree" %>
<%@ taglib uri="/WEB-INF/tld/requesttags.tld" prefix="request" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>

<%@page import="com.aplana.dbmi.model.Notification"%>
<portlet:defineObjects />
<fmt:setBundle basename="subscription"/>

<% String baseURL = renderResponse.encodeURL(renderRequest.getContextPath()); %>


<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />
<c:set var="searchBean" value="${subscriptionBean}"  scope="request"/>

<script type="text/javascript">

    var templates = new Array(
		<c:forEach items="${searchBean.dbTemplates}" var="template" varStatus="status">
               'template${template.id.id}'
        	<c:if test="${not status.last}">,</c:if>
		</c:forEach>
    
    );

    function onAllTemplates(allControl){
		onTemplate();
    }
    
    function onTemplate(){
    	var allTemplateControl = document.getElementById("allTemplates");
    	var templateNumber = 0;
        if (allTemplateControl.checked) {
	        for (var i = 0; i < templates.length; i++){
    	      var templateControl = document.getElementById(templates[i]);
        	  templateControl.disabled = true;
        	}
        } else {
	        for (var i = 0; i < templates.length; i++){
    	      var templateControl = document.getElementById(templates[i]);
        	  templateControl.disabled = false;
			  if(templateControl.checked){
    	        ++ templateNumber;
        	  }
        	}
        }
        
        var defineAttributes = document.getElementById("defineAttributes"); 
        if(templateNumber == 1) {
            defineAttributes.disabled = false;
        }else{
            defineAttributes.disabled = true;        
        }
    }

</script>

<jsp:include flush="true" page="message.jsp"></jsp:include>

<div class=reportheader>

<fmt:message key="mtsSubscriptionSave" var="mtsSubscriptionSave"></fmt:message>
<fmt:message key="mtsSubscriptionClose" var="mtsSubscriptionClose"></fmt:message>

	<div class="buttonPanel">
    	<ul>
<ap:button onClick="${namespace}SetAction('SAVE_ACTION')"  text="${mtsSubscriptionSave}" />
<ap:linkbutton   text="${mtsSubscriptionClose}" >
    <portlet:actionURL>
        <portlet:param name="action" value="CLOSE_ACTION"/>
    </portlet:actionURL>
</ap:linkbutton>
		</ul>
	</div>
   	<div class="reportheaderHR"></div>
</div>

<portlet:actionURL var="formAction">
	<portlet:param name="portlet_action" value="subscription"/>
</portlet:actionURL>
<form:form method="post" commandName="subscriptionBean" action="${formAction}" id="${namespace}SubscriptionForm">

<form:hidden id="${namespace}IsExtendedSearch" path="isExtendedSearch"/>
<form:hidden id="${namespace}IsAttributeSearch" path="isAttributeSearch"/>
<form:hidden id="${namespace}Action" path="action"/>


<table style="width : 100%;">
<col width="35%"/>
<col width="65%"/>
<tbody>
<%
	// SubscriptionFrequency column
 %>
    <tr>
        <td style="text-align: left;" valign="top">
            <table class="partition" style="width: 95%">
             	<tr>
                <td class="partition_left"></td>
                <td class="partition_middle"><fmt:message key="mtsSubscriptionFrequency" /></td>
                <td class="partition_right"></td>
                </tr>
            </table>
			<div class="divPadding">
            <table class="content">
            <col width="5%"/>
			<col width="95%"/>
                   <tr>
                       <td><form:radiobutton path="subscription.frequency"  value="<%= Notification.FREQ_HOURLY %>"/></td>
                       <td><fmt:message key="mtsSubscriptionFrequencyEvent" /></td>
                   </tr>    
                   <tr>
                       <td><form:radiobutton path="subscription.frequency"  value="<%= Notification.FREQ_DAYLY %>"/></td>
                       <td><fmt:message key="mtsSubscriptionFrequencyEveryDay" /></td>
                   </tr>    
                   <tr>
                       <td><form:radiobutton path="subscription.frequency"  value="<%= Notification.FREQ_WEEKLY %>"/></td>
                       <td><fmt:message key="mtsSubscriptionFrequencyEveryWeek" /></td>
                   </tr>    
                   <tr>
                       <td><form:radiobutton path="subscription.frequency"  value="<%= Notification.FREQ_MONTHLY %>"/></td>
                       <td><fmt:message key="mtsSubscriptionFrequencyEveryMonth" /></td>
                   </tr>    
                   <tr>
                       <td><form:radiobutton path="subscription.frequency"  value="<%= Notification.FREQ_NONE %>"/></td>
                       <td><fmt:message key="mtsSubscriptionFrequencyDoNotSend" /></td>
                   </tr>    
			</table>
            </div>
        </td>
  
<%
	// MainAttributes column
 %>
		<td style="text-align: left;" valign="top">
			<table class="partition" style="width: 98%">
            	<tr>
            		<td class="partition_left"></td>
                    <td class="partition_middle">
                    <fmt:message key="mts.subscription.block.main.characteristic.title" />                              
                    </td>
                    <td class="partition_right"></td>
            	</tr>
            </table>
            <div class="divPadding">
            <table class="content">
            <col width="50%"/>
            <col width="50%"/> 
            	<tr>
            		<td colspan="2">
            			<table width="100%">
				            <col width="20%"/>
            				<col width="80%"/> 
            				<tr>
            					<td><fmt:message key="mtsSubscriptionName" />
            					</td>
            					<td ><form:input path="subscription.name" cssStyle="width: 60%" />
            					</td>
            				</tr>
            				<tr>
            					<td><fmt:message key="mtsSubscriptionDescription" />
            					</td>
            					<td><form:input path="subscription.description" cssStyle="width: 60%" />
            					</td>
            				</tr>
            				<tr>
            					<td colspan="2">
            					<hr>
            					</td>
            				</tr>
            				<tr>
            					<td style="font-weight: bold;"><fmt:message key="mts.subscription.search.label" />
            					</td>
            					<td><form:input path="searchText" cssStyle="width: 60%"/>
            					</td>
            				</tr>
            				<tr>
            					<td colspan="2">
                	<table>
                    <tr>
                        <td style="vertical-align: middle;">
                            <form:checkbox id="${namespace}Number" path="number" onclick="${namespace}OnNumberClick()" />
                            <LABEL class="label"><fmt:message key="mtsSearchByNumber" /></LABEL>
                        </td>

                        <td style="vertical-align: middle;">
                            <form:checkbox  id="${namespace}Property" path="property" onclick="${namespace}OnPropertyClick()"/>
                            <LABEL class="label"><fmt:message key="mtsSearchByAttributes" /></LABEL>
                        </td>
                        <td style="vertical-align: middle;">
                           <form:checkbox  id="${namespace}FullText" path="fullText" onclick="${namespace}OnPropertyClick()" />
                            <LABEL class="label"><fmt:message key="mtsSearchFullText" /></LABEL>
                        </td>
                        </tr>
                     </table>
            					
            					</td>
            				</tr>
            			</table>
            		</td>
                </tr>

    <c:choose >
    	<c:when test="${searchBean.isAttributeSearch}">
        	<jsp:include  page="attributeSearch.jsp" />
    	</c:when>
    	<c:otherwise >
        	<jsp:include  page="extendedSearch.jsp" />
    	</c:otherwise>
	</c:choose>   
                   
               </table>
               </div>
           </td>
        </tr>
        
</tbody>                                                       

</table>   

</form:form>


<script>

function <portlet:namespace/>OnNumberClick(){
    if(document.getElementById('<portlet:namespace/>Number').checked==true){
        document.getElementById('<portlet:namespace/>Property').checked=false;
        document.getElementById('<portlet:namespace/>FullText').checked=false;        
    }
}
function <portlet:namespace/>OnPropertyClick(){
    if(document.getElementById('<portlet:namespace/>Property').checked==true
       || document.getElementById('<portlet:namespace/>FullText').checked==true){
        document.getElementById('<portlet:namespace/>Number').checked=false;

    }
}
function <portlet:namespace/>OnExtendedSearch(){
    var isExtendedSearch = document.getElementById('<portlet:namespace/>IsExtendedSearch');
    if(isExtendedSearch.value =='' 
       || isExtendedSearch.value =='false'){
           isExtendedSearch.value='true';
       }  else{
          isExtendedSearch.value='false';
       }
       <portlet:namespace/>SubmitSearchForm();
       return false;
}

function <portlet:namespace/>OnAttributeSearch(curAction){
    var isAttributeSearch = document.getElementById('<portlet:namespace/>IsAttributeSearch');
    if(isAttributeSearch.value =='' 
       || isAttributeSearch.value =='false'){
           isAttributeSearch.value='true';
       }  else{
          isAttributeSearch.value='false';
       }
       var action = document.getElementById('<portlet:namespace/>Action');       
       action.value=curAction;
       <portlet:namespace/>SubmitSearchForm();
       return false;
}


function <portlet:namespace/>SubmitSearchForm(){
      document.getElementById('<portlet:namespace/>SubscriptionForm').submit();
}

function <portlet:namespace/>OnSearch(){
    var action = document.getElementById('<portlet:namespace/>Action');
    action.value='<%= WebSearchBean.SEARCH_ACTION %>';
    <portlet:namespace/>SubmitSearchForm();
    return false;    
}


function <portlet:namespace/>SetAction(curAction){
    var action = document.getElementById('<portlet:namespace/>Action');
    action.value = curAction;
    <portlet:namespace/>SubmitSearchForm();
    return false;    
}

</script>

<c:if test="${!searchBean.isAttributeSearch}">                
<script>
onTemplate();
</script>
</c:if>