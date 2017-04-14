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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.*" %>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.card.actionhandler.ChangeOrderAction.ChangeOrderForm"%>

<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<portlet:actionURL var="backUrl">
	<portlet:param name="<%=CardPortlet.ACTION_FIELD%>" value="<%=CardPortlet.BACK_ACTION%>"></portlet:param>
</portlet:actionURL>

<dbmi:message text="${errorMessage}"/>

<table>
	<tr>
		<td style="padding-bottom: 20px;">
			<div class="buttonPanel">
				<ul>
					<li class="back">
						<a href="${backUrl}">
							<div class="ico_back img">&nbsp;</div>
							<p><fmt:message key="view.page.back.link" /></p> 
						</a>
					</li>
					<li>
						<a onclick="dojo.byId('orderForm').submit();" href="#"><fmt:message key="edit.page.generate.btn"/></a>
					</li>
				</ul>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<form name="selectForm" id="orderForm" method="post" action="<portlet:actionURL/>">
				<input type="hidden" name="<%=CardPortlet.ACTION_FIELD%>" value="<%=ChangeOrderForm.ACTION_CHANGE%>" />
				<table>
					<tr>
					<c:forEach items="${tableHead}" var="column">
						<th width="200px"><c:out value="${column}"></c:out></th>
					</c:forEach>
					</tr>
					<c:forEach items="${tableRows}" var="row">
						<tr>
							<input type="hidden" name="prev_value_${row.id}" value="${row.selectedItem.id.id}"/>
							<c:forEach items="${row.columnValues}" var="columnValue">
								<td><c:out value="${columnValue}"></c:out></td>
							</c:forEach>
							
							<td><select name="curr_value_${row.id}" <c:if test="${row.disabled}">disabled</c:if>>
							<c:forEach items="${row.items}" var="item">
								<option value="${item.id.id}" <c:if test="${item.id.id == row.selectedItem.id.id}">selected</c:if> >
								<c:out value="${item.value}"></c:out></option>
							</c:forEach>
							</select></td>
						</tr>
					</c:forEach>
				</table>
			</form>
		</td>
	</tr>
	
</table>	