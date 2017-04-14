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
<%@page language="java" contentType="text/html"	pageEncoding="UTF-8" session="true"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="com.aplana.dbmi.subscription.SubscriptionListPortlet"%>
<portlet:defineObjects />
<fmt:setBundle basename="subscription"/>

<jsp:include flush="true" page="message.jsp"></jsp:include>

<fmt:message key="mts.subscription.remove.confirm.msg" var="removeConfirm"></fmt:message>
<script type="text/javascript">
    function <portlet:namespace/>removeSubscription(url){
		if (confirm('${removeConfirm}')){
        	window.location=url;
        }
    }
</script>
<div class="columns">
	<div style="float: left">
        <a style="padding-left: 10px;" href="<portlet:actionURL><portlet:param   name="<%= SubscriptionListPortlet.REFRESH_ACTION %>" value="<%= SubscriptionListPortlet.REFRESH_ACTION %>" /></portlet:actionURL>" ><fmt:message key="mts.subscription.refresh.btn" /></a>
	</div>
    <div class="header">
        <div class=innerheader>
            <fmt:message key="mtsSubscriptionCaption"/>                              
        </div>
    </div>
 </div>   
 
<fmt:message key="mts.subscription.column.subscriptions" var="mtsSubscriptions" />                              
<fmt:message key="mts.subscription.column.description" var="mtsSubscriptionDescription" />                              
<fmt:message key="mts.subscription.column.date" var="mtsSubscriptionDate" />                              
<fmt:message key="mts.subscription.column.searchParameters" var="mtsSubscriptionSearchParameters" />                              

                            
 
<display:table name="subscriptionListBean.subscriptions" id="subscription" sort="list" class="res" pagesize="10">

    <display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
    <display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>

    <display:column ><ap:linkimage enable="true" 
                    urlPrefix="<%= renderRequest.getContextPath() %>"
                     enableUrl="/images/pencil.gif" disableUrl="/images/pencil_disable.gif"><portlet:renderURL>
                                   <portlet:param   name="subscription_id" value="${subscription.id.id}" />                                              
                                   <portlet:param   name="portlet_action" value="subscription" />                                                                                 
                        </portlet:renderURL></ap:linkimage></display:column>
    <display:column title="${mtsSubscriptions}" sortable="true"   headerClass="sortable"  > ${subscription.name}</display:column>
    <display:column title="${mtsSubscriptionDescription}" sortable="true"   headerClass="sortable"  > ${subscription.description}</display:column>    
    <display:column title="${mtsSubscriptionDate}" sortable="true"   headerClass="sortable"  > ${subscription.creationDate}</display:column>        
    <display:column title="${mtsSubscriptionSearchParameters}" sortable="true"   headerClass="sortable"  > ${subscription.search.summary}</display:column>            
    <display:column><ap:linkimage enable="true"
        	urlPrefix="<%= renderRequest.getContextPath() %>"
            enableUrl="/images/delete.gif" 
            disableUrl="/images/delete_disable.gif" >javascript: <portlet:namespace/>removeSubscription('<portlet:actionURL><portlet:param   name="<%= SubscriptionListPortlet.REMOVE_ID %>" value="${subscription.id.id}" /></portlet:actionURL>')</ap:linkimage></display:column>
</display:table>

<fmt:message key="mts.subscription.add.btn" var="mtsSubscriptionAdd" />                              
<div class="buttonPanel">
	<ul>
		<ap:linkbutton  text="${mtsSubscriptionAdd}">
        	<portlet:renderURL>
            	<portlet:param   name="portlet_action" value="subscription" />                                                                                 
            </portlet:renderURL>                           
		</ap:linkbutton>
	</ul>
</div>
