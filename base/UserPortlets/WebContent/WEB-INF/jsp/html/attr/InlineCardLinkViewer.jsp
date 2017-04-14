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
<%@page session="false" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.InlineCardLinkAttributeViewer"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />
<%
CardLinkAttribute attr = (CardLinkAttribute) 
	request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
CardPortletSessionBean sessionBean = (CardPortletSessionBean)
	renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
CardPortletCardInfo info = sessionBean.getActiveCardInfo();
boolean isLinked = ((Boolean) info.getAttributeEditorData(attr.getId(),
	 "isLinked")).booleanValue();
%>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkEditResource" />
<c:forEach items="${requestScope.cardLabels}" var="item" varStatus="status">
<%if (isLinked) { %>
	<a href="#" onclick="submitOpenLinkedCard('${requestScope.attrCode}', ${item.cardId})">
<%} else { %>
	<label>
<%} %>
		<c:choose>
			<c:when test="${not empty item.label}">
				<c:out value="${item.label}"/>
			</c:when>
			<c:otherwise>
				<fmt:message key="${label.emptyColumn}"/>
			</c:otherwise>
		</c:choose>	
<%if (isLinked) { %>
	</a>
<%} else { %>
	</label>
<%} %>
	<c:if test="${not status.last}">,</c:if>
</c:forEach>