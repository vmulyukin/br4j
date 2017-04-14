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
<%@ page contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.model.* " %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>
<%@page import="com.aplana.dbmi.portlet.CreateCardByExampleCommandBean"%>
<%@page import="com.aplana.dbmi.portlet.CreateCardByExampleController"%><portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.CreateCardByExample"/>
<portlet:defineObjects/>
<c:if test="${command.message != null}">
	<dbmi:errorMessage message="${command.message}"/>
</c:if>

<dbmi:pageTitle><fmt:message key="title"/></dbmi:pageTitle>
<portlet:actionURL var="formAction"/>

<form:form method="post" commandName="command" action="${formAction}">
    <input type="hidden" name="<%= CreateCardByExampleController.PARAM_ACTION %>" value="<%= CreateCardByExampleController.ACTION_CREATE %>"/>
	<input type="hidden" name="<%= CreateCardByExampleController.PARAM_THIS_URL %>" value="<portlet:renderURL/>"/>    
	<table style="width: 100%;">
		<tbody>
			<tr>
				<fmt:message var="partitionCaption" key="partitionCaption"/>
				<td style="text-align: left;">
					<dbmi:partitionCaption message="${partitionCaption}"/>
						<div class="divPadding">
							<table class="content">
								<c:forEach items="${command.examples}" var="exampleCard">
									<%
									    Card templateCard = (Card) pageContext.findAttribute("exampleCard");
									    String name = templateCard.getAttributeById(Attribute.ID_NAME).getStringValue();
									%>
									<tr class="normal">
										<td>
											<form:radiobutton path="selectedId" value="${exampleCard.id.id}"/>
											<c:out value="<%= name%>"/>								
										</td>
									</tr>
								</c:forEach>
							</table>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<input type="submit" value="<fmt:message key="button.createCard"/>" />
				</td>
			</tr>
		</tbody>
	</table>
</form:form>
