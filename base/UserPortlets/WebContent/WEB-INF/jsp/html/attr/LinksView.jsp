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
<%@page import="com.aplana.dbmi.card.LinkedCardUtils"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.LinkAttribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.text.MessageFormat "%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ListIterator"%>
<%@page import="com.aplana.dbmi.card.CertificateInfo"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.card.SecondaryColumnsManager"%>


<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>

<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardLinkAttributeViewer"%>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.ListEditResource"/>

<portlet:defineObjects/>
<%
	Attribute attr = (Attribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	String id = CardPortlet.getAttributeFieldName(attr);
	// (2010/02, RuSA) OLD: request.setAttribute(id, attr.getValues());
	// request.setAttribute(id, attr.getIdsLinked());
	CardPortletCardInfo info = sessionBean.getActiveCardInfo();
	//request.setAttribute(id, sessionBean.getAttributeEditorData( attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST));
	Collection columns = (info == null) ? null 
			: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST);
	Collection cards = (info == null) ? null 
			: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
	// список карточек с заполненными label-атрибутами
	Map labelColumns = (info == null) ? null 
			: (Map) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_LABEL_COLUMNS_LIST);
	request.setAttribute(id, cards);
	SecondaryColumnsManager scm = (SecondaryColumnsManager) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_SECONDARY_COLUMNS_MANAGER);
%>
<%@include file="AttributeActionsButtonPane.jsp"%>
<c:if test="<%= (cards != null && cards.size() > 0) || ((Boolean) info.getAttributeEditorData(attr.getId(), CardLinkAttributeViewer.KEY_SHOW_EMPTY)).booleanValue() %>">
	<display:table name="<%= id %>" id="<%= id %>" uid="<%= id %>" class="res" style="margin:0px 0px 10px 10px;">
		<display:setProperty name="basic.show.header" value="<%= info.getAttributeEditorData(attr.getId(), CardLinkAttributeViewer.KEY_SHOW_TITLE).toString() %>"/>
		
		<%-- totaly disable ID columns 
		<display:column style="width: 5em;" title="<%= ContextProvider.getContext().getLocaleMessage("search.column.id") %>" sortable="false" maxLength="10" property="id.id"/>
		--%>
<%
	if (columns != null) {
		Iterator colItr = columns.iterator();
		Card linked = (Card) pageContext.getAttribute(id);
		PortletURL link = null;
		String downloadCardId = null;
		if (linked != null) {
			link = renderResponse.createActionURL();
			link.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
			link.setParameter(CardPortlet.CARD_ID_FIELD, linked.getId().getId().toString());
			link.setParameter(CardPortlet.ATTR_ID_FIELD, (String)attr.getId().getId());
			downloadCardId = linked.getId().getId().toString();
		}

		while (colItr.hasNext()) {
			SearchResult.Column originalColumn = (SearchResult.Column) colItr.next();
			// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			// (YNikitin, 2012/08/06) код для замены заменямых атрибутов на заменимые
			SearchResult.Column col = null;
			if (originalColumn.isReplaceAttribute()){	// те атрибуты, на которые надо заменить другие, в чистом виде не отображаются 
				continue;
			}
			
			col = SearchResult.getRealColumnForCardIfItReplaced(originalColumn, linked, columns);	// заменяем оригинальную колонку на заменяемую (если у колоки нет заменямой, то возвращается сама колонка)
			// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			String style = "width: " + col.getWidth() + "em;";
%>
			<display:column style="<%= style %>"  title="<%= col.getName()%>" sortable="false" maxLength="<%= col.getWidth() %>">
<%
			Attribute attrVal = linked.getAttributeById(col.getAttributeId());

			if(scm != null
					&& col != null && linked != null) {
				Attribute attrSecondary = scm.getSecondaryColumnValueIfExists(col, linked);
				if(attrSecondary != null) {
					attrVal = attrSecondary;
				}
			}

			String value = ""; 	
			if (attrVal != null){
				if (Attribute.TYPE_CARD_LINK.equals(attrVal.getType()) // для кардлинков и бэклинков обработать MapLabels
						|| Attribute.TYPE_TYPED_CARD_LINK.equals(attrVal.getType())
						|| Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attrVal.getType())
						|| Attribute.TYPE_BACK_LINK.equals(attrVal.getType())) {
					// здесь необходимо добавить обработку карточек с labelAttrId
					if (col.getLabelAttrId()!=null){	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
						for (ListIterator<?> initr =  SearchResult.getCardsListForLabelColumn(labelColumns, col).listIterator(); initr.hasNext();) {	// для текущей обрабатываемой колонки в карте labelColumnsForCards SearchResult-а
							Card curCard = (Card)initr.next(); 
							if (curCard.getId().getId().equals(linked.getId().getId())){		// находим соответсвующую текущему циклу карточку
								attrVal = curCard.getAttributeById(col.getAttributeId());
								break;
							}
						}
					}
						value = attrVal.getStringValue();
				}else if (Attribute.TYPE_PERSON.equals(attrVal.getType())) {
					if (col.getLabelAttrId() != null) {	// если колонка содержит заполненный в настройках атрибут LabelAttrId
						value = SearchResult.getPersonColumnLabel(linked, col, labelColumns);
					}else {
						value = attrVal.getStringValue();
						}
				}else {
					value = attrVal.getStringValue();
				}
			}
			
			if (CertificateInfo.SIGNATURE_ATTR_ID.equals(col.getAttributeId())){
				DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
				//Card linkCard = (Card)sessionBean.getServiceBean().getById(linked.getId());
				// Непонятно, зачем надо было заново подгружать карточку, когда для отображения атрибута Подпись, он уже загружен в карточке linked 
				// (если его там нет, то и при новой загрузке из БД не будет, а вот проверка прав при загрузке будет обязательно и на ней можно свалиться, если в самом кардлинкэдиторе отключена проверка)???
				Card linkCard = linked;
				if(sessionBean.isVerify_ds()){
					List<CertificateInfo> cerInfos = CertificateInfo.readCertificateInfo(linkCard, sessionBean.getServiceBean(), sessionBean.getResourceBundle(), dateFormat);
					if (cerInfos != null && cerInfos.size() > 0){
						boolean valid = true;
						String displayStatus = null;
						for (CertificateInfo certificateInfo : cerInfos) {
							displayStatus = certificateInfo.getSignState(); 
							if (!certificateInfo.isSignValid()){
								valid = false;
								break;
							}
						}	
%>						<%= displayStatus %>
<%					}else{
%>						<fmt:message key="sign.none" />
<%					}
				} else {
					HtmlAttribute signAttr = (HtmlAttribute)linkCard.getAttributeById(col.getAttributeId());
					if (signAttr != null && !signAttr.isEmpty()){
%>						<fmt:message key="sign.yes" />
<%					} else {
%>						<fmt:message key="sign.none" />
<%					}
				}
			} else if (col.isLinked()) {
				if (MaterialAttribute.class.equals(col.getAttributeId().getType())) {
%>					<a href="#" onclick="downloadCardMaterial(<%= downloadCardId %>)"><%
				} else {
					// (BR4J00029530, YNikitin, 2013/07/02) Поменял прямую ссылку на вызов js-функции, которая перед переходом на новую страницу заполнит CardPortlet.ACTION_FIELD и тем самым мы избежим разблокировки текущей карточки при переходе в связанные.
					// link.toString()
%>					<a href="javascript:void(0)" onclick='submitOpenLinkedCard("<%=(String)attr.getId().getId() %>", <%=linked.getId().getId().toString() %> )'>
<%				}
			}
%>			<%= value %><%
			if (col.isLinked()) {
%>				</a>
<%			}
			if (Attribute.ID_DESCR.equals(col.getAttributeId())) {
				switch(linked.getMaterialType()){
					case MaterialAttribute.MATERIAL_NONE:
%>						<fmt:message key="material.none" />
<%						break;
					case MaterialAttribute.MATERIAL_FILE:
%>						<a href="#" onclick="downloadCardMaterial(<%= downloadCardId %>)">
							<fmt:message key="material.download.file" />
						</a>
<%						break;
					case MaterialAttribute.MATERIAL_URL:
%>						<a href="<%= linked.getUrl() %>"  target="_blank" ><fmt:message key="material.open.external.url" /></a>
<%						break;
				}
			}
%>			</display:column>
<%		}
	}
%>
	</display:table>
</c:if>	