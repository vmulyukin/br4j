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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.aplana.dbmi.workflowbuilder.LockableObjectListController" %>
<%@ page import="com.aplana.dbmi.workflowbuilder.LockableObjectListCommandBean" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@taglib  prefix="display" uri="/WEB-INF/tag/displaytag.tld" %>

<portlet:defineObjects/>
<%
LockableObjectListCommandBean command = ((LockableObjectListCommandBean)pageContext.findAttribute("command"));
command.memorizeSortAndPagination(renderRequest);
boolean isEditAccessExists = command.isEditAccessExists();
%>

<fmt:setBundle basename="${resourceBundleBasename}" scope="request"/>
<dbmi:errorMessage message="${command.message}"/>
<c:set target="${command}" property="message" value=""/>
<c:set var="contextPath" value="<%=request.getContextPath()%>"/>
<dbmi:pageTitle titleKey="page.title"/>

<c:set var="viewMode" value="${empty command.selectedObject}"/>

<div class="reportheader">
	<c:choose>
		<c:when test="${viewMode}">
			<%@include file="listButtons.jspf"%>
		</c:when>
		<c:otherwise>
			<c:set var="cardStateFormId"><portlet:namespace/>_cardStateForm</c:set>
			<c:set var="submitButtonsFormId" value="${cardStateFormId}"/>
			<c:set var="submitButtonsEnabled" value="true"/>
			<portlet:actionURL var="submitButtonsCancelURL">
				<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=LockableObjectListController.ACTION_CANCEL%>"/>
			</portlet:actionURL>
			<%@include file="submitButtons.jspf"%>
		</c:otherwise>
	</c:choose>
	<div class="reportheaderHR"></div>
</div>
<table class="content"><col width="48%"/><col width="4%"/><col width="48%"/><tr>
<td valign="top">
	<spring:message code="toolTip.edit" var="editToolTip"/>
	<dbmi:partitionCaption messageKey="partition.allObjects"/>
	<display:table class="res" style="margin-top: 0;" name="${command.objects}"
			uid="currentObject" pagesize="<%=LockableObjectListController.PAGE_SIZE%>" sort="list" defaultsort="2">
		<display:setProperty name="basic.msg.empty_list" ><spring:message code="table.basic.msg.empty_list"/></display:setProperty>
		<display:setProperty name="paging.banner.no_items_found" ><spring:message code="table.paging.banner.no_items_found"/></display:setProperty>
		<display:setProperty name="paging.banner.one_item_found" ><spring:message code="table.paging.banner.one_item_found"/></display:setProperty>
		<display:setProperty name="paging.banner.all_items_found" ><spring:message code="table.paging.banner.all_items_found"/></display:setProperty>
		<display:setProperty name="paging.banner.some_items_found" ><spring:message code="table.paging.banner.some_items_found"/></display:setProperty>
		<c:set var="columnClass" value=""/>
		<c:if test="${command.selectedObject.id.id == currentObject.id.id}">
			<c:set var="columnClass" value="alternate"/>
		</c:if>
		<display:column property="id.id" titleKey="column.id" sortable="${viewMode}" class="${columnClass}"/>
		<display:column property="name.value" titleKey="column.name" sortable="${viewMode}" class="${columnClass}"/>
		
		<display:column class="${columnClass}">
			<dbmi:linkimage enable="${command.editAccessExists and viewMode}" 
				urlPrefix="${contextPath}" 
				enableUrl="/images/pencil.gif" 
				disableUrl="/images/pencil_disable.gif"
				toolTip="${editToolTip}">
				<portlet:actionURL>
					<portlet:param name="<%=LockableObjectListController.PARAM_ACTION %>" value="<%=LockableObjectListController.ACTION_EDIT%>"/>				
					<portlet:param name="<%=LockableObjectListController.PARAM_OBJECT_ID%>" value="${currentObject.id.id}"/>
				</portlet:actionURL>
			</dbmi:linkimage>
		</display:column>
	</display:table>
</td>
<td/><%-- Column delimiter --%>
<td valign="top">
	<c:if test="${not viewMode}">
		<c:choose>
			<c:when test="${not empty command.selectedObject.id}">
				<spring:message code="partition.editObject" var="cardStateFormPartition"/>
			</c:when>
			<c:otherwise>
				<spring:message code="partition.createNewObject" var="cardStateFormPartition"/>
			</c:otherwise>
		</c:choose>
		<portlet:actionURL var="cardStateFormAction">
			<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=LockableObjectListController.ACTION_OK%>"/>
		</portlet:actionURL>
		<c:set var="cardStateFormObjectPath" value="selectedObject"/>
		<%@include file="cardStateForm.jspf"%>
	</c:if>
</td>
</tr></table>