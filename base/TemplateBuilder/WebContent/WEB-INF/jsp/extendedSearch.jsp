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
<%@ page  contentType="text/html"  pageEncoding="UTF-8"%>
<%@ page import="com.aplana.dbmi.model.*" %>
<%@ page import="com.aplana.dbmi.model.web.*" %>
<%@page import="java.util.Map" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib prefix="tree" uri="/WEB-INF/tags/treetag.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<portlet:defineObjects/>

<fmt:setBundle basename="${bundleBasename}"/>

<%
	WebSearchBean bean = (WebSearchBean)renderRequest.getAttribute("searchBean");
	Map valuesDate = bean.getValuesDate();
%>

<!-- extendedSearch -->
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

<% 
ContextProvider.getContext().setLocale(renderResponse.getLocale());
%>
    <tr>
<%
	// left column
 %>	
    	<td style="text-align: left" valign="top">
        <dbmi:partitionCaption messageKey="block.search.title" />                              

		<div class="divPadding">
                <table class="content" >                 
                    <col Width="7%" />
                    <col Width="93%" /> 
                    <tr>
                        <td>
		                     <form:checkbox path="webSearchBean.isAllTemplates" onclick="onAllTemplates(this);"  id="allTemplates"/>
                        </td>
                     <td class="label">
                       <LABEL for="allTemplates"><b><fmt:message key="searchAll" /></b></LABEL>
                     </td>
                        
                    </tr>
                    <tr> 
                        <td colspan="2">
                            <hr />
                        </td> 
                    </tr>
                    
        <c:forEach items="${searchBean.viewTemplates}" var="control">
            <tr>
                     <td>
                     <form:checkbox path="webSearchBean.templates['${control.name}']"  value="${control.value}" id="template${control.name}" onclick="onTemplate()"/>
                      </td>
                     <td class="label">
                       <LABEL for="template${control.name}">${control.label}</LABEL>                     
                     </td>
           </tr>
        </c:forEach>    
				</table>
            <fmt:message key="defineCharacteristic.link" var="searchDefiteAttributes"/>
                
            <input type="button" onClick="return <portlet:namespace/>OnAttributeSearch('<%= WebSearchBean.ATTRIBUTE_SEARCH_ACTION %>')"  value="${searchDefiteAttributes}" id="defineAttributes"/>
            <br/>
            <form:checkbox path="webSearchBean.lastVersionSearch" id="lastVersionSearch" />
                <label for="lastVersionSearch"><fmt:message key="SearchLastVersion.label" /></label>                              
		</div>
            
        </td>
<%// rigth column  %> 
        <td style="text-align: left" valign="top">
            <dbmi:partitionCaption message="${searchBean.viewMainBlock.name}" />			
			<div class="divPadding">
                <ap:attributes valuesDate="<%=valuesDate%>" templateAttributes="${searchBean.viewMainBlock.attributes}" path="webSearchBean.attributes" urlPrefix="<%= renderRequest.getContextPath() %>"/> 
           	</div>
        </td>
    </tr>
<c:if test="${!searchBean.isAttributeSearch}">
<script>
onTemplate();
</script>
</c:if>
<!-- /extendedSearch -->
