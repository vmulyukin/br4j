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
<%@page contentType="text/html" pageEncoding="UTF-8"
	import="java.util.*,javax.portlet.*,com.aplana.dbmi.model.*"%>
<%@ taglib prefix="c" uri="/WEB-INF/tags/c.tld"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag"%>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib  prefix="display" uri="/WEB-INF/tags/displaytag.tld" %>
<%@ page import="com.aplana.dbmi.admin.WebTemplateWorkflowMovesReqAttrBean"%>
<%@page import="com.aplana.dbmi.admin.WebTemplateBean"%>
<portlet:defineObjects />
<%
	String baseURL = renderResponse.encodeURL(renderRequest.getContextPath());
	WebTemplateBean templateBean = (WebTemplateBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
	boolean isEditAccessExists = templateBean.isEditAccessExists();	
//	WebTemplateWorkflowMovesReqAttrBean templateWorkflowMovesReqAttrBean = (WebTemplateWorkflowMovesReqAttrBean) renderRequest.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplateWorkflowMovesReqAttrPortlet.FORM.templateWorkflowMovesReqAttrBean");
//	String message = null;
//	if (templateWorkflowMovesReqAttrBean != null && templateWorkflowMovesReqAttrBean.getMessage() != null) {
//		templateWorkflowMovesReqAttrBean.setMessage(null);
//	}
%>
<fmt:setBundle basename="templates" scope="request" />


<%-- <dbmi:errorMessage message="<%= message %>" /> 
  --%>
<c:set var="isEditAccessExists" value="<%=isEditAccessExists %>"/>
<dbmi:pageTitle titleKey="template.wfm.attrlist.required.colTitle.moveTitle" />

<portlet:actionURL var="formAction1">
		<portlet:param 
				name="portlet_action"
				value="editTemplateWorkflowMovesReqAttr" 
		/>
</portlet:actionURL>

<%
//	if (request.getAttribute("templateWorkflowMovesReqAttrBean") == null) {
//		request.setAttribute("templateWorkflowMovesReqAttrBean", templateWorkflowMovesReqAttrBean);
//	}

	ContextProvider.getContext().setLocale(renderResponse.getLocale());
%>

<div class=reportheader>

	<dbmi:buttonPanel>
		<fmt:message key="templatesApply" var="templatesApply"></fmt:message>
		<fmt:message key="templatesClose" var="templatesClose"></fmt:message>
        <c:if test="${isEditAccessExists}">
		<dbmi:button onClick="submitFormByApply();" text="${templatesApply}" />
		</c:if>
		<dbmi:linkbutton text="${templatesClose}">
			<portlet:renderURL/>
		</dbmi:linkbutton>
	</dbmi:buttonPanel>

	<div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>
	<div class="reportheaderHR"></div>
</div>

<div style="clear: both;"><!-- А всё потому, что некоторые свирстальщики очень любят флоатеры --></div>

<form:form 
		id="templateWorkflowMovesReqAttrForm"
		action="${formAction1}" 
		method="post"
		commandName="templateWorkflowMovesReqAttrBean">

		<form:hidden path="templateApplyClose" />

		<form:hidden path="removeWorkflowMoveId" />
		<form:hidden path="removeTemplateBlockId" />
		<form:hidden path="removeAttributeId" />

		<form:hidden path="selectedAction" />
	<br>

	<dbmi:partitionCaption messageKey="template.wfm.attrlist.required.colTitle.moveListTitle" />

	<fmt:message key="template.wfm.attrlist.required.colTitle.required.asTemplate" var="textAsTemplate" />
	<fmt:message key="template.wfm.attrlist.required.colTitle.required.assigned" var="textMustBeAssigned" /> 
	<fmt:message key="template.wfm.attrlist.required.colTitle.required.blank" var="textMustBeBlank" /> 

	<div class="divPadding">

	<display:table 
			name="${templateWorkflowMovesReqAttrBean.showWorkflowMovesRequiredFields}"  
			id="showItem" 
			sort="list" 
			class="res" 
			style="margin-top: 0;">

		<display:column titleKey="template.wfm.attrlist.required.colTitle.move" 
				sortable="true" 
				property="wfmCaption"/>
		<display:column titleKey="template.wfm.attrlist.required.colTitle.templateBlock" 
				sortable="true" 
				property="templateBlock.name"/>
		<display:column titleKey="template.wfm.attrlist.required.colTitle.attribute" 
				sortable="true" 
				property="attribute.name"/>
		<%--
		<display:column titleKey="template.wfm.attrlist.required.colTitle.required" 
				sortable="false" 
				property="mustBeSet"/>
		  --%>

		<display:column titleKey="template.wfm.attrlist.required.colTitle.required" 
						sortable="false" > 
			<c:choose>
				<c:when test="${showItem.set_BLANK}"> ${textMustBeBlank} </c:when>
				<c:when test="${showItem.set_ASSIGNED}"> ${textMustBeAssigned} </c:when>
				<c:otherwise> ${textAsTemplate} </c:otherwise>
			</c:choose>
		</display:column>

		<display:column>
			<dbmi:linkimage
					enable="true" 
					urlPrefix="<%= renderRequest.getContextPath() %>" 
					onClick="submitFormByDelete2('${showItem.workflowMove.id.id}', '${showItem.templateBlock.id.id}', '${showItem.attribute.id.id}');"
					enableUrl="/images/delete.gif" disableUrl="/images/delete_disable.gif" >#
			</dbmi:linkimage>
		</display:column>

		<display:footer>

			<tr>
				<td> <hr/> </td>
				<td> <hr/> </td>
				<td> <hr/> </td>

				<td> <hr/> </td>
				<td> <hr/> </td>
			</tr>

			<tr>
				<!-- (1) WorkFlow Moves  -->
				<td>
					<c:if test="${templateWorkflowMovesReqAttrBean.showWorkflowMovesDropDown}">
						<fmt:message key="template.wfm.attrlist.required.AllWorkflowMoves" var="WFMAllItemsText" /> 
						<form:select path="selectedWorkflowMoveId"
							onchange="this.form.submit();">
							<form:option 
								value="<%= WebTemplateWorkflowMovesReqAttrBean.ALL_WORKFLOW_MOVES %>" 
								label="${WFMAllItemsText}" />
							<form:options
								items="${templateWorkflowMovesReqAttrBean.workflowMoves}"
								itemLabel="name.valueRu"
								itemValue="id.id" />
						</form:select>
					</c:if>
				</td>

				<!-- (2) Using templates block of workflow -->
				<td>
					<c:if test="${templateWorkflowMovesReqAttrBean.showTemplateBlocksDropDown}">
						<fmt:message key="template.wfm.attrlist.required.AllTemplateBlocks" var="textTemplateBlocksAll" /> 
						<form:select path="selectedTemplateBlockId" onchange="this.form.submit();">
							<form:option 
								value="<%= WebTemplateWorkflowMovesReqAttrBean.ALL_TEMPLATE_BLOCKS %>" 
								label="${textTemplateBlocksAll}" />
							<form:options
								items="${templateWorkflowMovesReqAttrBean.blocks}"
								itemLabel="name" itemValue="id.id" />
						</form:select>
					</c:if>
				</td>

				<td>
					<!-- (3) Atrributes of the SELECTED TEMPLATE BLOCK(2) -->
					<c:if test="${templateWorkflowMovesReqAttrBean.showAttributeDropDown}">
						<fmt:message key="template.wfm.attrlist.required.AllAttributes" var="textAttributesAll" /> 
						<form:select path="selectedAttributeId" onchange="this.form.submit();">
							<form:option 
								value="<%= WebTemplateWorkflowMovesReqAttrBean.ALL_ATTRIBUTES %>" 
								label="${textAttributesAll}" />
							<form:options
								items="${templateWorkflowMovesReqAttrBean.selectedTemplateBlockAttributes}"
								itemLabel="name"
								itemValue="id.id" />
						</form:select>
					</c:if>
				</td>

				<td>
					<!-- (4) MustBeSet state of the selected attribute (3)  -->
					<c:if test="${templateWorkflowMovesReqAttrBean.showRequiredRadioItems}">
						<form:select 
								path="selectedRequiredState" 
								onchange="submitFormByAdd();">
							<form:option 
									value="<%= String.valueOf(WorkflowMoveRequiredField.MUSTBESET_ASSIGNED) %>"
									label="${textMustBeAssigned}" />
							<form:option 
									value="<%= String.valueOf(WorkflowMoveRequiredField.MUSTBESET_BLANK) %>"
									label="${textMustBeBlank}" />
						</form:select>
					</c:if>
				</td>
				<td>
					<c:if test="${templateWorkflowMovesReqAttrBean.showRequiredRadioItems}">
						<c:choose>
							<c:when test="${templateWorkflowMovesReqAttrBean.selectedExist}">
								<dbmi:linkimage 
										enable="true" 
										urlPrefix="<%= renderRequest.getContextPath() %>"
										onClick="submitFormByDelete();"
										enableUrl="/images/delete.gif" disableUrl="/images/delete_disable.gif">
								</dbmi:linkimage>
							</c:when>
							<c:otherwise>
								<dbmi:linkimage 
										enable="true" 
										urlPrefix="<%= renderRequest.getContextPath() %>"
										onClick="submitFormByAdd();"
										enableUrl="/images/add.gif" disableUrl="/images/add_disable.gif">
								</dbmi:linkimage>
							</c:otherwise>
						</c:choose>
					</c:if>
				</td>

			</tr>
		</display:footer>
	</display:table>
	</div>

</form:form>

<script type="text/javascript">

function setApplyAction(action)
{
	var tac = document.getElementById('templateApplyClose');
	tac.value=action;
}

function submitFormByApply(){
	var form = document.getElementById('templateWorkflowMovesReqAttrForm');
	setApplyAction( "APPLY_TEMPLATE");
	form.submit();
}

function submitFormByAdd(){
	var form = document.getElementById('templateWorkflowMovesReqAttrForm');
	// form.required.value='true';
	form.selectedAction.value='add';
	form.submit();
}

function submitFormByDelete(){
	var form = document.getElementById('templateWorkflowMovesReqAttrForm');
	// form.required.value='false';
	form.selectedAction.value='rem';
	form.submit();
}

function submitFormByDelete2(workflowMoveIdId, templateBlockIdId, attributeIdId){
	var form = document.getElementById('templateWorkflowMovesReqAttrForm');
	form.removeWorkflowMoveId.value=workflowMoveIdId;
	form.removeTemplateBlockId.value=templateBlockIdId;
	form.removeAttributeId.value=attributeIdId;
	form.selectedAction.value='rem';
	form.submit();
}
</script>