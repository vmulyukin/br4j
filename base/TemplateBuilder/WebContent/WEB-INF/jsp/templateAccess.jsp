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
<%@page  contentType="text/html"  pageEncoding="UTF-8"%>
<%@ page import="com.aplana.dbmi.admin.TemplateAccessPortlet"%>
<%@ page import="com.aplana.dbmi.admin.WebTemplateAccessBean"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@taglib  prefix="display" uri="/WEB-INF/tags/displaytag.tld" %>

<portlet:defineObjects/>
<%
	((WebTemplateAccessBean)pageContext.findAttribute("templateAccessBean")).memorizeSort(renderRequest);
%>

<fmt:setBundle basename="templateAccess" scope="request"/>
<dbmi:pageTitle>
	<fmt:message key="templateAccessTitle">
		<fmt:param value="${templateAccessBean.template.name}"/>
	</fmt:message>
</dbmi:pageTitle>

<dbmi:errorMessage message="${templateAccessBean.message}"/>
<c:set target="${templateAccessBean}" property="message" value=""/>

<c:set var="namespace">
	<portlet:namespace/>
</c:set>
<c:set var="contextPath" value="<%=renderRequest.getContextPath()%>"/>
<c:set var="formId" value = "${namespace}_templateAccessForm"/>
<fmt:message key="link.editPermissions" var="editPermissionsToolTip"/>
<script type="text/javascript">
	function ${namespace}_submitEditPermissions(type, objectId) {
		var form = document.getElementById('${formId}');
		form.<%=TemplateAccessPortlet.PARAM_ACTION%>.value = '<%=TemplateAccessPortlet.ACTION_EDIT_PERMISSION%>';
		form.<%=TemplateAccessPortlet.PARAM_PERMISSION_TYPE%>.value = type;
		form.<%=TemplateAccessPortlet.PARAM_OBJECT_ID%>.value = objectId;
		form.submit();
	}
	
	function ${namespace}_submitDeleteAccessItem(itemIndex) {
		var form = document.getElementById('${formId}');
		form.<%=TemplateAccessPortlet.PARAM_ACTION%>.value = '<%=TemplateAccessPortlet.ACTION_DELETE_ACCESS_ITEM%>';
		form.<%=TemplateAccessPortlet.PARAM_ITEM_INDEX%>.value = itemIndex;
		form.submit();
	}
	
	function ${namespace}_submitForm(action) {
		var form = document.getElementById('${formId}');
		form.<%=TemplateAccessPortlet.PARAM_ACTION%>.value = action;
		form.submit();
	}
</script>

<portlet:actionURL var="formAccessAction"/>
<form:form action="${formAccessAction}"  method="post" commandName="templateAccessBean" id="${formId}">
	<input type="hidden" name="portlet_action" value="editTemplateAccess"/>
	<input type="hidden" name="<%=TemplateAccessPortlet.PARAM_ACTION%>" value="<%=TemplateAccessPortlet.ACTION_OK%>"/>
	<input type="hidden" name="<%=TemplateAccessPortlet.PARAM_PERMISSION_TYPE%>"/>
	<input type="hidden" name="<%=TemplateAccessPortlet.PARAM_OBJECT_ID%>"/>
	<input type="hidden" name="<%=TemplateAccessPortlet.PARAM_ITEM_INDEX%>"/>
	<div class="reportheader">
		<c:set var="onClick">${namespace}_submitForm('<%=TemplateAccessPortlet.ACTION_CANCEL%>')</c:set>	
		<dbmi:buttonPanel>
			<fmt:message key="button.Ok" var="captionOk"></fmt:message>
			<fmt:message key="button.Cancel" var="captionCancel"></fmt:message>
			<dbmi:button onClick="document.getElementById('${formId}').submit();" text="${captionOk}" />
			<dbmi:button onClick="${onClick}" text="${captionCancel}" />
		</dbmi:buttonPanel>
		<div class="reportheaderHR"></div>
	</div>
	<table class="content"><col width="48%"/><col width="4%"/><col width="48%"/><tr>
		<td valign="top">
			<dbmi:partitionCaption messageKey="partition.CreateCardAccess" />
			<table class="res" style="margin-top: 0;">
				<col width="90%"/>
				<col width="10%"/>	
				<c:set var="permissionsItem" value="${templateAccessBean.cardCreatePermissions}"/>
				<tr	<c:if test="${templateAccessBean.cardCreatePermissions == templateAccessForm.selectedPermissions}">class="alternate"</c:if>>
					<td>
						<%@include file="singlePermissionView.jspf"%>
					</td>
				</tr>
			</table>
			<dbmi:partitionCaption messageKey="partition.CardReadAccess" />
			<display:table name="${templateAccessBean.cardReadPermissions}" uid="cardReadPermissions" sort="list" class="res" style="margin-top: 0;">
				<display:column titleKey="column.cardState" sortable="true" property="names[0].value"/>
				<display:column titleKey="column.permissions">
					<c:set var="permissionsItem" value="${cardReadPermissions}"/>
					<%@include file="singlePermissionView.jspf"%>
				</display:column>
			</display:table>
			<dbmi:partitionCaption messageKey="partition.CardEditAccess" />
			<display:table name="${templateAccessBean.cardEditPermissions}" uid="cardEditPermissions" sort="list" class="res" style="margin-top: 0;">
				<display:column titleKey="column.cardState" sortable="true" property="names[0].value"/>
				<display:column titleKey="column.permissions">
					<c:set var="permissionsItem" value="${cardEditPermissions}"/>
					<%@include file="singlePermissionView.jspf"%>
				</display:column>
			</display:table>
			<dbmi:partitionCaption messageKey="partition.WorkflowMoveAccess" />
			<display:table name="${templateAccessBean.workflowMovePermissions}" uid="workflowMovePermissions" sort="list" class="res" style="margin-top: 0;">
				<display:column titleKey="column.workflowMove" sortable="true" property="names[0].value"/>
				<display:column titleKey="column.fromState" sortable="true" property="names[1].value"/>
				<display:column titleKey="column.toState" sortable="true" property="names[2].value"/>
				<display:column titleKey="column.permissions">
					<c:set var="permissionsItem" value="${workflowMovePermissions}"/>
					<%@include file="singlePermissionView.jspf"%>
				</display:column>
			</display:table>
		</td>
		<td/>
		<td valign="top">
			<c:if test="${not empty templateAccessBean.selectedPermissions}">
				<c:set var="editPermissionsPartitionCaption">
					<fmt:message key="${templateAccessBean.selectedPermissions.partitionCaptionKey}">
						<c:if test="${not templateAccessBean.selectedPermissions.cardCreate}">
							<fmt:param value="${templateAccessBean.selectedPermissions.names[0].value}"/>
						</c:if>
					</fmt:message>
				</c:set>
				<dbmi:partitionCaption message="${editPermissionsPartitionCaption}" />
				<c:if test="${templateAccessBean.selectedPermissions != templateAccessBean.cardCreatePermissions}">
					<form:checkbox path="selectedPermissions.allowForAll" onclick="${namespace}_submitForm('')"/>
					<fmt:message key="label.allowForAll"/>
				</c:if>
				<c:if test="${not templateAccessBean.selectedPermissions.allowForAll}">					
					<fmt:message key="link.deleteAccessItem" var="deleteAccessItemToolTip"/>
					<fmt:message key="link.addAccessItem" var="addAccessItemToolTip"/>
					<%--
						Only system role access could be specified while editing permissions 
						for new cards creation, so total number of columns will be 2 
					--%>
					<c:set var="colNum">
						<c:choose>
							<c:when test="${templateAccessBean.selectedPermissions.cardCreate}">2</c:when>
							<c:otherwise>3</c:otherwise>						
						</c:choose>
					</c:set>
					<table class="res" style="margin-top: 0;" class="content">					
						<thead>
							<th style="font-weight: bold;"><fmt:message key="column.systemRole"/></th>
							<c:if test="${colNum == 3}">
								<th style="font-weight: bold;"><fmt:message key="column.personAttribute"/></th>
							</c:if>
							<th/>
						</thead>
						<tbody>
						<c:forEach items="${templateAccessBean.selectedPermissions.accessItems}" var="accessItem" varStatus="accessItemStatus">
							<tr>
								<td>
									<c:if test="${not empty accessItem.role}">
										<b><c:out value="${accessItem.role.name}" /></b>
									</c:if>
								</td>
								<c:if test="${colNum == 3}">
									<td>
										<c:if test="${not empty accessItem.personAttribute}">
											<c:out value="${accessItem.personAttribute.name}" />
										</c:if>
									</td>
								</c:if>
								<td style="width: 10%;">
									<dbmi:linkimage enable="true" 
										urlPrefix="${contextPath}" 
										enableUrl="/images/delete.gif" 
										disableUrl="/images/delete_disable.gif"
										onClick="${namespace}_submitDeleteAccessItem(${accessItemStatus.index})"
										toolTip="${deleteAccessItemToolTip}"
									>
										#
									</dbmi:linkimage>
								</td>
							</tr>
						</c:forEach>
						<c:if test="${empty templateAccessBean.selectedPermissions.accessItems}">
							<tr><td colspan="${colNum}"><b><fmt:message key="label.noAccess" /></b></td></tr>
						</c:if>
						<fmt:message key="label.notSelected" var="notSelectedLabel"/>
						<tr>
							<td>
								<form:select cssStyle="width: 100%;" path="roleCode">
									<form:option value="" label="${notSelectedLabel}"/>
									<form:options items="${templateAccessBean.availableSystemRoles}" itemValue="id.id" itemLabel="name"/>
								</form:select>
							</td>
							<c:if test="${colNum == 3}">
								<td>
									<form:select cssStyle="width: 100%;" path="attrCode">
										<form:option value="" label="${notSelectedLabel}"/>
										<form:options items="${templateAccessBean.personAttributes}" itemValue="id.id" itemLabel="name"/>
									</form:select>
								</td>
							</c:if>
							<c:set var="onClick">${namespace}_submitForm('<%=TemplateAccessPortlet.ACTION_ADD_ACCESS_ITEM%>')</c:set>
							<td style="width: 10%;">
								<dbmi:linkimage enable="true" 
									urlPrefix="${contextPath}" 
									enableUrl="/images/add.gif" 
									disableUrl="/images/add_disable.gif"
									onClick="${onClick}"
									toolTip="${addAccessItemToolTip}"
								>#</dbmi:linkimage>
							</td>
						</tr>
						</tbody>
					</table>
				</c:if>
			</c:if>
		</td>
	</tr></table>
</form:form>
</script>