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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="java.util.*"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.dbmi.PortletService"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>
<portlet:defineObjects/>
<%
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	MaterialAttribute attr = (MaterialAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String cardId = sessionBean.getActiveCard().getId().getId().toString();

	PortletService portletService = Portal.getFactory().getPortletService();
	String cardPageId = portletService.getPageProperty("cardPage", renderRequest, renderResponse);
	if (cardPageId == null)
		cardPageId = "dbmi.Card";
	Map params = new HashMap(1);
	params.put(CardPortlet.EDIT_CARD_ID_FIELD, cardId);
	String cardURL = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", params, renderRequest, renderResponse);
%>
<table class="content">
	<col Width="90%"/>
	<col Width="10%"/>
	<tr id="row1">
		<td>
			<c:choose>
				<c:when test="${!requestScope.canDownload}">
					<div class="access">&nbsp;</div>
					<div style="float:left" ><fmt:message key="view.material.block.access.msg" /></div>
					<a HRef="<portlet:actionURL><portlet:param name="<%= CardPortlet.ACTION_FIELD %>" value="<%= CardPortlet.ACCESS_SEND_MAIL_ACTION %>" /><portlet:param name="<%= CardPortlet.CARD_URL_FIELD %>" value="<%= cardURL.toString() %>" /></portlet:actionURL>" ><fmt:message key="view.material.block.access.link" /></a>
				</c:when>
				<c:when test="<%=attr.getMaterialType() == MaterialAttribute.MATERIAL_FILE%>">
					<div class="file">&nbsp;</div>
					<div style="float:left; padding: 0px 5px 0px 0px;" ><%= attr.getMaterialName() %></div>
					<a href="#" onclick="downloadCardMaterial(<%= cardId %>)">
						<fmt:message key="view.material.block.load.link" />
					</a>
				</c:when>
				<c:when test="<%= attr.getMaterialType() == MaterialAttribute.MATERIAL_URL %>">
					<%
					String materialUrl = attr.getMaterialName().trim();
					String materialHref = materialUrl;
					if (!(materialHref.startsWith("http://") 
							|| materialHref.startsWith("https://") 
							|| materialHref.startsWith("ftp://"))) {
						materialHref = "http://" + materialHref;
					}%>
					<span><fmt:message key="view.material.block.external.label" /></span>
					<div class="link">&nbsp;</div>
					<a HRef="<%= materialHref %>"  target="_blank" ><%= materialUrl %></a>
				</c:when>
				<c:when test="<%= attr.getMaterialType() == MaterialAttribute.MATERIAL_NONE %>">
					<span><fmt:message key="view.material.block.none.label" /></span>
				</c:when>
			</c:choose>
		</td>
		<td></td>
	</tr>
</table>