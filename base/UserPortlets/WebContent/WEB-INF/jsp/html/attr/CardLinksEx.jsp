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
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.LinkAttribute"%>
<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ListIterator"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.text.MessageFormat "%>
<%@page import="com.aplana.dbmi.card.displaytag.comp.DisplayTagComparatorFactory"%>
<%@page import="com.aplana.dbmi.card.CertificateInfo"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.card.SecondaryColumnsManager"%>


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
	// список карточек с заполненными label-атрибутами
	Map labelColumns = (info == null) ? null 
			: (Map) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_LABEL_COLUMNS_LIST);
	request.setAttribute(id, cards);
	SecondaryColumnsManager scm = (SecondaryColumnsManager) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_SECONDARY_COLUMNS_MANAGER);
%>

<div class="divAttrLink">
<%@include file="AttributeActionsButtonPane.jsp"%>
<c:if test="<%= (cards != null && cards.size() > 0) || ((Boolean) info.getAttributeEditorData(attr.getId(), CardLinkExAttributeEditor.KEY_SHOW_EMPTY)).booleanValue() %>">
<display:table name="<%= id %>" id="<%= id %>" uid="<%= id %>" class="res">
	<display:setProperty name="basic.show.header" value="<%= info.getAttributeEditorData(attr.getId(), CardLinkExAttributeEditor.KEY_SHOW_TITLE).toString() %>"/>
	<col width="50%"/>
	<col width="45%"/>
	<col width="5%" style="text-align: right"/>
<%--

<display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>

--%>
	<%-- 
		<display:column style="width: 5em;" title="<%= ContextProvider.getContext().getLocaleMessage("search.column.id") %>" sortable="false" maxLength="10" property="id.id"/>
	  --%>
<%
	Card link = (Card) pageContext.getAttribute(id);
	String downloadUrl = null;
	PortletURL openUrl = null;
	if (link != null) {
		downloadUrl =  response.encodeURL(request.getContextPath() + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "=" + link.getId().getId().toString());
		openUrl = renderResponse.createActionURL();
		openUrl.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
		openUrl.setParameter(CardPortlet.CARD_ID_FIELD, link.getId().getId().toString());
		openUrl.setParameter(CardPortlet.ATTR_ID_FIELD, (String)attr.getId().getId());
	}

	if (columns != null) {
		Iterator colItr = columns.iterator();
  		while (colItr.hasNext()) {
  			SearchResult.Column col = (SearchResult.Column) colItr.next();
  			
  			Attribute attrVal = link == null ? null : link.getAttributeById(col.getAttributeId());
  			
  			if(scm != null
  					&& col != null && link != null) {
	  			Attribute attrSecondary = scm.getSecondaryColumnValueIfExists(col, link);
				if(attrSecondary != null) {
					attrVal = attrSecondary;
				}
  			}

			String value = "";
			if (attrVal != null){
				if ((Attribute.TYPE_CARD_LINK.equals(attrVal.getType()) ||	// для кардлинков и бэклинков обработать MapLabels
						Attribute.TYPE_TYPED_CARD_LINK.equals(attrVal.getType())) || (Attribute.TYPE_BACK_LINK.equals(attrVal.getType()))) {
					// здесь необходимо добавить обработку карточек с labelAttrId
					if (col.getLabelAttrId()!=null){	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
						for (ListIterator<?> initr =  SearchResult.getCardsListForLabelColumn(labelColumns, col).listIterator(); initr.hasNext();) {	// для текущей обрабатываемой колонки в карте labelColumnsForCards SearchResult-а
							Card curCard = (Card)initr.next(); 
							if (curCard.getId().getId().equals(link.getId().getId())){		// находим соответсвующую текущему циклу карточку
								attrVal = curCard.getAttributeById(col.getAttributeId());
								break;
							}
						}
					}
					if (null != attrVal)
						value = attrVal.getStringValue();
				}else if (Attribute.TYPE_PERSON.equals(attrVal.getType())) {
					if (col.getLabelAttrId() != null) {	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
						value = SearchResult.getPersonColumnLabel(link, col, labelColumns);
					}else {
						value = attrVal.getStringValue();
					}
				}else {
					value = attrVal.getStringValue();
				}
			}
		%>
	<display:column style="<%= "width: " + col.getWidth() + "em;"%>"  
		title="<%= col.getName() %>" sortable="true" maxLength="<%= col.getWidth() %>"
		comparator="<%= DisplayTagComparatorFactory.getComparatorClass(attrVal).getCanonicalName() %>"> <%
		
		pageContext.setAttribute("value", value);
		if (CertificateInfo.SIGNATURE_ATTR_ID.equals(col.getAttributeId())){
			DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
			//Card linkCard = (Card)sessionBean.getServiceBean().getById(linked.getId());
			// Непонятно, зачем надо было заново подгружать карточку, когда для отображения атрибута Подпись, он уже загружен в карточке linked 
			// (если его там нет, то и при новой загрузке из БД не будет, а вот проверка прав при загрузке будет обязательно и на ней можно свалиться, если в самом кардлинкэдиторе отключена проверка)???
			Card linkCard = link;
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
					%>
						<%= displayStatus %>
					<%						
				}else{
					%>
				<fmt:message var = "value" key="sign.none" />
					<%						
				}
			} else {
				HtmlAttribute signAttr = (HtmlAttribute)linkCard.getAttributeById(col.getAttributeId());
				if (signAttr != null && !signAttr.isEmpty()){
					%><fmt:message key="sign.yes" /><%
				} else {
					%><fmt:message key="sign.none" /><%
				}
			}
		}else if (col.isLinked()) {
			if (MaterialAttribute.class.equals(col.getAttributeId().getType())) {
				%><a href="<%= downloadUrl %>"><%
			} else {
				// (BR4J00029530, YNikitin, 2013/07/02) Поменял прямую ссылку на вызов js-функции, которая перед переходом на новую страницу заполнит CardPortlet.ACTION_FIELD и тем самым мы избежим разблокировки текущей карточки при переходе в связанные.
				// openUrl.toString()
			%>
			<a href="javascript:void(0)" onclick='submitOpenLinkedCard("<%=(String)attr.getId().getId() %>", <%=link.getId().getId().toString() %> )'>
			<%
			}			
		}
		%><c:out value="${value}"/><%
		if (col.isLinked()) {
			%></a><% 
		}%>
	</display:column>
<%
	   	}
	}
%>
	<c:if test="<%= sessionBean.getAttributeEditorData(attr.getId(), CardLinkExAttributeEditor.KEY_REMOVE) != null %>">
	<display:column style="text-align:right;">
		<a style="text-decoration: none; text-align: right;" href="#" onclick="javascript:submitFormCardLinksExRemove('<%= attr.getId().getId() %>', '<%= link.getId().getId().toString() %>')">
			<span class="delete">&nbsp</span>
		</a>
	</display:column>
	</c:if>
</display:table>
</c:if>

</div>
