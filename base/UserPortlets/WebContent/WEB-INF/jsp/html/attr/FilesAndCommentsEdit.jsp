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
<%@page import="com.aplana.dbmi.card.CardLinkExAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.LinkedCardUtils"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.aplana.dbmi.card.displaytag.comp.DisplayTagComparatorFactory"%>
<%@page import="com.aplana.dbmi.card.FilesAndCommentsUtils"%>
<%@page import="com.aplana.dbmi.card.CertificateInfo"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkEditResource"/>
<portlet:defineObjects/>

<%
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	CardLinkAttribute attr = (CardLinkAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String id = CardPortlet.getAttributeFieldName(attr);
	// (2010/02, RuSA) OLD: request.setAttribute(id, attr.getValues());
	// request.setAttribute(id, attr.getLabelLinkedCards());
	CardPortletCardInfo info = sessionBean.getActiveCardInfo();
	Collection columns = (info == null) ? null 
			: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST);
	Collection cards = (info == null) ? null 
			: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
	ObjectId fileLinkAttributeId = (ObjectId) info.getAttributeEditorData(attr.getId(), FilesAndCommentsUtils.PARAM_FILE_LINK);
	FilesAndCommentsUtils.RoundData[] roundDataArray = (info == null) ? null 
			: (FilesAndCommentsUtils.RoundData[]) info.getAttributeEditorData(attr.getId(), FilesAndCommentsUtils.ROUND_DATA_ARRAY);
	//request.setAttribute(id, cards);
%>

<div class="divAttrLink">
<%@include file="AttributeActionsButtonPane.jsp"%>

<% 
if (roundDataArray != null) {
	for (int i = roundDataArray.length - 1; i >=0; i--) {
		FilesAndCommentsUtils.RoundData roundData = roundDataArray[i];
		if (roundDataArray.length > 1) {
%>
<div style="padding: 5px 0px 5px 0px;">Итерация <%=roundData.round%></div>
<%
		}
		cards = roundData.fileCardList;
		String data_id = id + "_" + roundData.round;
		request.setAttribute(data_id, cards);
%>

<%@include file="FilesTableEdit.jspf" %>

<%
	List visaDataList = roundData.visaDataList;
	for (int visaDataIndex = 0; visaDataIndex < visaDataList.size(); visaDataIndex++) {
		FilesAndCommentsUtils.RoundVisaData visaData = (FilesAndCommentsUtils.RoundVisaData)visaDataList.get(visaDataIndex);

		cards = visaData.fileCardList;
		data_id = id + "_" + visaData.visaId;
		request.setAttribute(data_id, cards);
%>


<c:if test="<%= (cards != null && cards.size() > 0)%>">
	
	<div style="padding: 0px 0px 0px 40px;"><table class="res" style="margin-top: 5px;">
	
	<tr><td>Автор: <%=visaData.userName%>, замечание: <%=visaData.comment%></td></tr>
	
	<tr><td>
	
	<%@include file="FilesTableEdit.jspf"%>

	</td></tr></table></div>

</c:if>

<%
	}

	}
} else {
	String data_id = id;
	request.setAttribute(data_id, cards);
%>

<%@include file="FilesTableEdit.jspf" %>

<%
}
%>	
</div>
