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
<%@ page import="com.aplana.dbmi.workflowbuilder.WorkflowController" %>
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
<c:set var="namespace"><portlet:namespace/></c:set>

<div class="reportheader">
	<c:choose>
		<c:when test="${viewMode}">
			<%@include file="listButtons.jspf"%>
		</c:when>
		<c:otherwise>
			<script type="text/javascript" language="javascript">
				function ${namespace}_submitForm(formId, action, objectId) {
					var form = document.getElementById(formId);
					form.<%=LockableObjectListController.PARAM_ACTION%>.value = action;
					form.<%=LockableObjectListController.PARAM_OBJECT_ID%>.value = objectId;
					form.submit();
				}
			</script>
			<c:set var="editMoveMode" value="${not empty command.selectedMove}"/>
			<c:set var="addObjectMode" value="${not empty command.addedObject}"/>
			<c:set var="workflowFormId" value="${namespace}_workflowForm"/>
			<c:set var="onClick">
				${namespace}_submitForm('${workflowFormId}', '<%=WorkflowController.ACTION_SAVE%>', '')
			</c:set>
			<dbmi:buttonPanel>
				<spring:message code="button.ok" var="captionOk"/>
				<spring:message code="button.cancel" var="captionCancel"/>
				<spring:message code="button.save" var="captionSave"/>
				<c:if test="${command.editAccessExists}">
				<dbmi:button 
					onClick="document.getElementById('${workflowFormId}').submit()" 
					text="${captionOk}" 
					enable="${not editMoveMode and not addObjectMode}"/>
				<dbmi:button 
					onClick="${onClick}" 
					text="${captionSave}" 
					enable="${not editMoveMode and not addObjectMode}"/>					
				</</c:if>
				<dbmi:linkbutton text="${captionCancel}" enable="${not editMoveMode and not addObjectMode}">
					<portlet:actionURL>
						<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=LockableObjectListController.ACTION_CANCEL%>"/>
					</portlet:actionURL>
				</dbmi:linkbutton>
			</dbmi:buttonPanel>
		</c:otherwise>
	</c:choose>
	<div class="reportheaderHR"></div>
</div>
<table class="content"><col width="48%"/><col width="4%"/><col width="48%"/><tr>
<td valign="top">
	<spring:message code="toolTip.edit" var="editToolTip"/>
	<%--
	<spring:message code="toolTip.deactivate" var="deactivateToolTip"/>
	 --%>
	<dbmi:partitionCaption messageKey="partition.allObjects"/>
	<script type="text/javascript" language="javascript">
		function ${namespace}_deactivateConfirmation() {
			return confirm('<spring:message code="msg.deactivateConfirmation"/>');
		}
	</script>
	<display:table class="res" style="margin-top: 0;" name="${command.objects}"
			uid="currentObject" pagesize="<%=LockableObjectListController.PAGE_SIZE%>" sort="list" defaultsort="2">
		<display:setProperty name="basic.msg.empty_list" ><spring:message code="table.basic.msg.empty_list"/></display:setProperty>
		<display:setProperty name="paging.banner.no_items_found" ><spring:message code="table.paging.banner.no_items_found"/></display:setProperty>
		<display:setProperty name="paging.banner.one_item_found" ><spring:message code="table.paging.banner.one_item_found"/></display:setProperty>
		<display:setProperty name="paging.banner.all_items_found" ><spring:message code="table.paging.banner.all_items_found"/></display:setProperty>
		<display:setProperty name="paging.banner.some_items_found" ><spring:message code="table.paging.banner.some_items_found"/></display:setProperty>
		<c:choose>
			<c:when test="${command.selectedObject.id.id == currentObject.id.id}">
				<c:set var="columnClass" value="alternate"/>
			</c:when>
			<c:otherwise>
				<c:set var="columnClass" value=""/>
			</c:otherwise>
		</c:choose>
		<display:column property="id.id" titleKey="column.id" sortable="${viewMode}" class="${columnClass}"/>
		<display:column property="name.value" titleKey="column.name" sortable="${viewMode}" class="${columnClass}"/>
		<%--
		<display:column sortable="false" class="${columnClass}">
			<c:choose>
				<c:when test="${currentObject.active}">
					<spring:message code="label.active"/>
				</c:when>
				<c:otherwise>
					<spring:message code="label.inactive"/>
				</c:otherwise>
			</c:choose>
		</display:column>
		--%>
		<display:column class="${columnClass}">
			<dbmi:linkimage enable="${command.editAccessExists and viewMode and currentObject.active}" 
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
		<%--
		<display:column class="${columnClass}">
			<dbmi:linkimage enable="${viewMode and currentObject.active}"
				urlPrefix="${contextPath}"
				enableUrl="/images/delete.gif"
				disableUrl="/images/delete_disable.gif"
				toolTip="${deactivateToolTip}"
				onClick="return ${namespace}_deactivateConfirmation();">
				<portlet:actionURL>
					<portlet:param name="<%=LockableObjectListController.PARAM_ACTION %>" value="<%=WorkflowController.ACTION_DEACTIVATE%>"/>				
					<portlet:param name="<%=LockableObjectListController.PARAM_OBJECT_ID%>" value="${currentObject.id.id}"/>
				</portlet:actionURL>
			</dbmi:linkimage>
		</display:column>
		--%>
	</display:table>
</td>
<td/><%-- Column delimiter --%>
<td valign="top">
	<c:if test="${not viewMode and command.editAccessExists}">
		<c:choose>
			<c:when test="${not empty command.selectedObject.id}">
				<spring:message code="partition.editObject" var="partitionSelectedObject"/>
			</c:when>
			<c:otherwise>
				<spring:message code="partition.createNewObject" var="partitionSelectedObject"/>
			</c:otherwise>
		</c:choose>
		<dbmi:partitionCaption message="${partitionSelectedObject}"/>
		<portlet:actionURL var="formAction"/>
		<form:form action="${formAction}" id="${workflowFormId}">
			<input type="hidden" name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=LockableObjectListController.ACTION_OK%>"/>
			<input type="hidden" name="<%=LockableObjectListController.PARAM_OBJECT_ID%>"/>
			<table class="content"><col width="33%"/><col width="57%"/><col width="10%"/>
				<tr>
					<td><span class="obligatory"><spring:message code="label.nameRu"/></span></td>
					<td><form:input path="selectedObject.name.valueRu" cssStyle="width: 100%;" disabled="${editMoveMode or addObjectMode}"/></td>
					<td/>
				</tr>
				<tr>
					<td><span class="obligatory"><spring:message code="label.nameEn"/></span></td>
					<td><form:input path="selectedObject.name.valueEn" cssStyle="width: 100%;" disabled="${editMoveMode or addObjectMode}"/></td>
					<td/>
				</tr>
				<tr>
					<td><span class="obligatory"><spring:message code="label.initialState"/></span></td>
					<td>
						<form:select path="initialStateId" cssStyle="width: 100%;" disabled="${editMoveMode or addObjectMode}">
							<form:option value="<%=String.valueOf(WorkflowController.NOT_SELECTED)%>">
								<spring:message code="option.notSelected"/>
							</form:option>
							<form:options items="${cardStates}" itemValue="id.id" itemLabel="name"/>
						</form:select>
						<spring:message code="toolTip.addCardState" var="addCardStateToolTip"/>
					</td>						
					<td>
						<c:set var="onClick">
							${namespace}_submitForm('${workflowFormId}', '<%=WorkflowController.ACTION_ADD_CARD_STATE%>', '<%=WorkflowController.ADDED_OBJECT_INITIAL_STATE%>')
						</c:set>
						<dbmi:linkimage enable="${not editMoveMode and not addObjectMode}" 
							urlPrefix="${contextPath}" 
							enableUrl="/images/add.gif" 
							disableUrl="/images/add_disable.gif"
							toolTip="${addCardStateToolTip}"
							onClick="${onClick}"
						>#</dbmi:linkimage>
					</td>
				</tr>				
			</table>
		</form:form>
		<spring:message code="toolTip.deleteMove" var="deleteMoveToolTip"/>			
		<dbmi:partitionCaption messageKey="partition.workflowMoves"/>
		<display:table class="res" style="margin-top: 0;" name="${moves}" uid="moveItem" sort="list">
			<c:set var="columnClass" value=""/> 
			<c:if test="${command.selectedMoveKey == moveItem.key}">
				<c:set var="columnClass" value="alternate"/>
			</c:if>
			<display:column property="name" titleKey="column.moveName" sortable="${not editMoveMode and not addObjectMode}" class="${columnClass}"/>
			<display:column property="from.name.value" titleKey="column.fromState" sortable="${not editMoveMode and not addObjectMode}" class="${columnClass}"/>
			<display:column property="to.name.value" titleKey="column.toState" sortable="${not editMoveMode and not addObjectMode}" class="${columnClass}"/>
			<display:column class="${columnClass}">
				<c:set var="onClick">
					${namespace}_submitForm('${workflowFormId}', '<%=WorkflowController.ACTION_EDIT_WORKFLOW_MOVE%>', '${moveItem.key}')
				</c:set>			
				<dbmi:linkimage enable="${not editMoveMode}" 
					urlPrefix="${contextPath}" 
					enableUrl="/images/pencil.gif" 
					disableUrl="/images/pencil_disable.gif"
					toolTip="${editToolTip}"
					onClick="${onClick}"
				>#</dbmi:linkimage>
			</display:column>
			<display:column class="${columnClass}">
				<c:set var="onClick">
					${namespace}_submitForm('${workflowFormId}', '<%=WorkflowController.ACTION_DELETE_WORKFLOW_MOVE%>', '${moveItem.key}')
				</c:set>
				<dbmi:linkimage enable="${not editMoveMode}" 
					urlPrefix="${contextPath}" 
					enableUrl="/images/delete.gif" 
					disableUrl="/images/delete_disable.gif"
					toolTip="${deleteMoveToolTip}"
					onClick="${onClick}"
				>#</dbmi:linkimage>
			</display:column>
		</display:table>
		<c:set var="onClick">
			${namespace}_submitForm('${workflowFormId}', '<%=WorkflowController.ACTION_ADD_WORKFLOW_MOVE%>', '')
		</c:set>		
		<dbmi:buttonPanel>
			<spring:message code="button.addMove" var="captionAddMove"/>
			<dbmi:button text="${captionAddMove}"
				onClick="${onClick}"
				enable="${not editMoveMode and not addObjectMode}"/>
		</dbmi:buttonPanel>
		<c:choose>
			<c:when test="${addObjectMode}">
				<c:if test="${command.addedObjectKey != 'logAction'}">
					<spring:message code="partition.addCardState" var="cardStateFormPartition"/>
					<portlet:actionURL var="cardStateFormAction">
						<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=WorkflowController.ACTION_SUBMIT_ADDED_OBJECT%>"/>
					</portlet:actionURL>
					<c:set var="cardStateFormId" value="${namespace}_cardStateForm"/>
					<c:set var="cardStateFormObjectPath" value="addedObject"/>
					<%@include file="cardStateForm.jspf"%>
					<c:set var="submitButtonsFormId" value="${cardStateFormId}"/>
				</c:if>
				<c:if test="${command.addedObjectKey == 'logAction'}">
					<spring:message code="partition.addLogAction" var="logActionFormPartition"/>
					<portlet:actionURL var="logActionFormAction">
						<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=WorkflowController.ACTION_SUBMIT_ADDED_OBJECT%>"/>
					</portlet:actionURL>
					<c:set var="logActionFormId" value="${namespace}_logActionForm"/>
					<c:set var="logActionFormObjectPath" value="addedObject"/>
					<%@include file="logActionForm.jspf"%>
					<c:set var="submitButtonsFormId" value="${logActionFormId}"/>
				</c:if>
				<c:set var="submitButtonsEnabled" value="true"/>
				<portlet:actionURL var="submitButtonsCancelURL">
					<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=WorkflowController.ACTION_CANCEL_ADDED_OBJECT%>"/>
				</portlet:actionURL>
				<%@include file="submitButtons.jspf"%>
			</c:when>
			<c:when test="${editMoveMode}">
				<c:choose>
					<c:when test="${empty command.selectedMoveKey}">
						<spring:message code="partition.createMove" var="partitionSelectedMove"/>
					</c:when>
					<c:otherwise>
						<spring:message code="partition.editMove" var="partitionSelectedMove"/>
					</c:otherwise>
				</c:choose>			
				<dbmi:partitionCaption message="${partitionSelectedMove}"/>
				<c:set var="submitButtonsFormId" value="${namespace}_moveForm"/>
				<form:form action="${formAction}" id="${submitButtonsFormId}">
					<input type="hidden" name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=WorkflowController.ACTION_SUBMIT_WORKFLOW_MOVE_OK%>"/>
					<input type="hidden" name="<%=LockableObjectListController.PARAM_OBJECT_ID%>"/>			
					<table class="content"><col width="33%"/><col width="57%"/><col width="10%"/>
						<tr>
							<td><spring:message code="label.moveNameRu"/></td>
							<td><form:input path="selectedMove.name.valueRu" cssStyle="width: 100%;"/></td>
							<td/>
						</tr>
						<tr>
							<td><spring:message code="label.moveNameEn"/></td>
							<td><form:input path="selectedMove.name.valueEn" cssStyle="width: 100%;"/></td>
							<td/>
						</tr>
						<tr>
							<td><span class="obligatory"><spring:message code="label.fromState"/></span></td>
							<td>
								<form:select path="selectedMoveFromStateId" cssStyle="width: 100%;">
									<form:option value="<%=String.valueOf(WorkflowController.NOT_SELECTED)%>">
										<spring:message code="option.notSelected"/>
									</form:option>
									<form:options items="${cardStates}" itemValue="id.id" itemLabel="name.value"/>
								</form:select>
							</td>
							<td>
								<c:set var="onClick">
									${namespace}_submitForm('${submitButtonsFormId}', '<%=WorkflowController.ACTION_ADD_CARD_STATE%>', '<%=WorkflowController.ADDED_OBJECT_FROM_STATE %>')
								</c:set>
								<dbmi:linkimage enable="true" 
									urlPrefix="${contextPath}" 
									enableUrl="/images/add.gif" 
									disableUrl="/images/add_disable.gif"
									toolTip="${addCardStateToolTip}"
									onClick="${onClick}"
								>#</dbmi:linkimage>
							</td>
						</tr>
						<tr>
							<td><span class="obligatory"><spring:message code="label.toState"/></span></td>
							<td>
								<form:select path="selectedMoveToStateId" cssStyle="width: 100%;">
									<form:option value="<%=String.valueOf(WorkflowController.NOT_SELECTED)%>">
										<spring:message code="option.notSelected"/>
									</form:option>
									<form:options items="${cardStates}" itemValue="id.id" itemLabel="name.value"/>
								</form:select>
							</td>
							<td>
								<c:set var="onClick">
									${namespace}_submitForm('${submitButtonsFormId}', '<%=WorkflowController.ACTION_ADD_CARD_STATE%>', '<%=WorkflowController.ADDED_OBJECT_TO_STATE%>')
								</c:set>
								<dbmi:linkimage enable="true" 
									urlPrefix="${contextPath}" 
									enableUrl="/images/add.gif" 
									disableUrl="/images/add_disable.gif"
									toolTip="${addCardStateToolTip}"
									onClick="${onClick}"
								>#</dbmi:linkimage>
							</td>
						</tr>
						<tr>
							<td><span class="obligatory"><spring:message code="label.logAction"/></span></td>
							<td>
								<form:select path="selectedMoveLogActionId" cssStyle="width: 100%;">
									<form:option value="<%=String.valueOf(WorkflowController.NOT_SELECTED)%>">
										<spring:message code="option.notSelected"/>
									</form:option>
									<form:options items="${command.logActions}" itemValue="id.id" itemLabel="name.value"/>
								</form:select>
							</td>
							<spring:message code="toolTip.addLogAction" var="addLogActionToolTip"/>
							<td>
								<c:set var="onClick">
									${namespace}_submitForm('${submitButtonsFormId}', '<%=WorkflowController.ACTION_ADD_LOG_ACTION%>', '<%=WorkflowController.ADDED_OBJECT_LOG_ACTION%>')
								</c:set>
								<dbmi:linkimage enable="true" 
									urlPrefix="${contextPath}" 
									enableUrl="/images/add.gif" 
									disableUrl="/images/add_disable.gif"
									toolTip="${addLogActionToolTip}"
									onClick="${onClick}"
								>#</dbmi:linkimage>
							</td>
						</tr>
						<script type="text/javascript" language="javascript">
							function ${namespace}_toggleNeedConfirmation(checkbox) {
								checkbox.form.elements['selectedMove.confirmation.valueRu'].disabled = !checkbox.checked;
								checkbox.form.elements['selectedMove.confirmation.valueEn'].disabled = !checkbox.checked;
							}
						</script>
						<tr>
							<td><spring:message code="label.closeCard"/></td>
							<td><form:checkbox path="selectedMove.closeCard"/></td>
						</tr>
						<tr>
							<td><spring:message code="label.applyDS"/></td>
							<td>
								<form:select path="selectedMove.applyDigitalSignatureOnMove">
									<form:options items="${dsmodes}" itemValue="key" itemLabel="value"/>
								</form:select>
							</td>
						</tr>
						<tr>
							<td><spring:message code="label.needConfirmation"/></td>
							<td><form:checkbox path="selectedMove.needConfirmation" onclick="${namespace}_toggleNeedConfirmation(this)"/></td>
						</tr>
						<tr>
							<td><spring:message code="label.confirmationRu"/></td>
							<td><form:input path="selectedMove.confirmation.valueRu" cssStyle="width: 100%;" disabled="${not command.selectedMove.needConfirmation}"/></td>
						</tr>
						<tr>
							<td><spring:message code="label.confirmationEn"/></td>
							<td><form:input path="selectedMove.confirmation.valueEn" cssStyle="width: 100%;" disabled="${not command.selectedMove.needConfirmation}"/></td>
						</tr>
					</table>
				</form:form>
				<c:set var="submitButtonsEnabled" value="${not addObjectMode}"/>
				<portlet:actionURL var="submitButtonsCancelURL">
					<portlet:param name="<%=LockableObjectListController.PARAM_ACTION%>" value="<%=WorkflowController.ACTION_SUBMIT_WORKFLOW_MOVE_CANCEL%>"/>
				</portlet:actionURL>
				<%@include file="submitButtons.jspf"%>
			</c:when>			
		</c:choose>
	</c:if>
</td>
</tr></table>