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
<%@page language="java" contentType="text/html"	pageEncoding="UTF-8" session="false"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="com.aplana.dbmi.subscription.DistributionListPortlet"%>
<portlet:defineObjects />

<fmt:setBundle basename="subscription"/>

<jsp:include flush="true" page="message.jsp"></jsp:include>

<fmt:message key="mts.distribution.remove.confirm.msg" var="removeConfirm"></fmt:message>
<script type="text/javascript">
    function <portlet:namespace/>removeDistribution(url){
		if (confirm('${removeConfirm}')){
        	window.location=url;
        }
    }
</script>

<div class="columns">
	<div style="float: left">
        <a style="padding-left: 10px;" href="<portlet:actionURL><portlet:param   name="<%= DistributionListPortlet.REFRESH_ACTION %>" value="<%= DistributionListPortlet.REFRESH_ACTION %>" /></portlet:actionURL>" ><fmt:message key="mts.distribution.refresh.btn" /></a>
	</div>
    <div class="header">
        <div class=innerheader>
             <fmt:message key="mts.distribution.caption"/>  
        </div>
    </div>
</div>   

<fmt:message key="mts.distribution.column.distribution" var="mtsDistributionName" />                              
<fmt:message key="mts.distribution.column.description" var="mtsDescription" />                              
<fmt:message key="mts.distribution.column.date" var="mtsDate" />                              
<fmt:message key="mts.distribution.column.creationName" var="mtsCreationName" />                              
<fmt:message key="mts.distribution.column.searchParameters" var="mtsSearchParameters" />                              

                            
<display:table name="distributionListBean.distributions" id="distribution" sort="list" class="res" pagesize="10">

    <display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
    <display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>

    <display:column ><ap:linkimage enable="true"
                    urlPrefix="<%= renderRequest.getContextPath() %>"
                     enableUrl="/images/pencil.gif" 
                     disableUrl="/images/pencil_disable.gif"><portlet:renderURL>
                                   <portlet:param   name="distribution_id" value="${distribution.id.id}" />                                              
                                   <portlet:param   name="portlet_action" value="distribution" />                                                                                 
                    </portlet:renderURL></ap:linkimage></display:column>
    <display:column title="${mtsDistributionName}" sortable="true"   headerClass="sortable"  > ${distribution.name}</display:column>
    <display:column title="${mtsDescription}" sortable="true"   headerClass="sortable"  > ${distribution.description}</display:column>    
    <display:column title="${mtsDate}" sortable="true"   headerClass="sortable"  > ${distribution.creationDate}</display:column>        
    <display:column title="${mtsCreationName}" sortable="true"   headerClass="sortable"  > ${distribution.creatorName}</display:column>
    <display:column title="${mtsSearchParameters}" sortable="true"   headerClass="sortable"> ${distribution.search.summary}</display:column>   
    <display:column><ap:linkimage enable="true"
        	urlPrefix="<%= renderRequest.getContextPath() %>"
            enableUrl="/images/delete.gif" 
            disableUrl="/images/delete_disable.gif" >javascript: <portlet:namespace/>removeDistribution('<portlet:actionURL><portlet:param   name="<%= DistributionListPortlet.REMOVE_ID %>" value="${distribution.id.id}" /></portlet:actionURL>')</ap:linkimage></display:column>
</display:table>

<c:if test="${distributionListBean.isCanCreate}">
	<fmt:message key="mts.distribution.add.btn" var="mtsDistributionAdd" />                              
	<div class="buttonPanel">
	<ul>
    	<ap:linkbutton  text="${mtsDistributionAdd}">
        	<portlet:renderURL>
	    	    <portlet:param   name="portlet_action" value="distribution" />                                                                                 
    	    </portlet:renderURL>                           
        </ap:linkbutton>
	</ul>
	</div>
</c:if>

