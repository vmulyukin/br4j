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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.card.BackLinkAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="com.aplana.dbmi.action.SearchResult.Column"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.HtmlAttribute"%>
<%@page import="com.aplana.dbmi.model.BackLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.LinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.card.LinkedCardUtils"%>
<%@page import="com.aplana.dbmi.card.SecondaryColumnsManager"%>
<%@page import="java.text.MessageFormat"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ListIterator"%>
<%@page import="java.util.Map"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.aplana.dbmi.card.TypedCardLinkAttributeViewer"%>
<%@page import="com.aplana.dbmi.card.graph.Graph"%>
<%@page import="com.aplana.dbmi.card.CertificateInfo"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="com.aplana.dbmi.model.IntegerAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="static com.aplana.web.tag.util.StringUtils.*"%>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.ListEditResource"/>
<portlet:defineObjects/>

<%
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	BackLinkAttribute attr = (BackLinkAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String id = CardPortlet.getAttributeFieldName(attr);
	CardPortletCardInfo info = sessionBean.getActiveCardInfo();
	Collection columns = (info == null) ? null 
			: (Collection) info.getAttributeEditorData( attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST);
	Collection cards = (info == null) ? null 
			: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
	// список карточек с заполненными label-атрибутами
	Map labelColumns = (info == null) ? null 
			: (Map) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_LABEL_COLUMNS_LIST);
	SecondaryColumnsManager scm = (SecondaryColumnsManager) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_SECONDARY_COLUMNS_MANAGER);
	request.setAttribute(id, cards);
%>

<%
	if (sessionBean.getActiveCard() != null && sessionBean.getActiveCard().getId() != null) { // проверка что карточка уже создана
		Boolean isViewGraph = (Boolean)sessionBean.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.GRAPH_IS_VIEW);
		if (isViewGraph != null && isViewGraph.booleanValue()) {
			Graph graph = (Graph)sessionBean.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.GRAPH_DATA);
			
			PortletURL link = renderResponse.createActionURL();
			link.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
			link.setParameter(CardPortlet.ATTR_ID_FIELD, (String)attr.getId().getId());
			String prefixId = JspAttributeEditor.getAttrHtmlId(attr);
			String jsonTree = graph.getJSONTree(link, CardPortlet.CARD_ID_FIELD, prefixId).toString();
%>
	<c:set var="htmlAttrId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>"/>

	<button dojoType="dijit.form.Button" onclick="dijit.byId('dialog_${htmlAttrId}').show()">Графическое представление</button>
	<div class="graph" dojoType="dijit.Dialog" id="dialog_${htmlAttrId}" title="Графическое представление"
    	 style="width: 925px; height: 422px">
    	<div id="viewGraph_${htmlAttrId}" class="viewGraph"></div>
	</div>
<script language="JavaScript" type="text/javascript">
	dojo.require('dijit.Dialog');
 	dojo.require('dijit.form.Button');
	
	dojo.addOnLoad(function(){
		var json = <%=jsonTree%>;
		initTreeBackLink('viewGraph_${htmlAttrId}', json);
	});
</script>
<%
		}
	}
%>

<div class="divAttrLink">
<%@include file="AttributeActionsButtonPane.jsp"%>
<c:if test="<%= Boolean.TRUE.equals(sessionBean.getAttributeEditorData(attr.getId(), BackLinkAttributeEditor.KEY_CREATE)) %>">
<div style="float:right"> 
	<button name="<%= id %>" id="<%= id %>" type="button" dojoType="dijit.form.Button" 
		onclick="javascript:submitFormBackLinksAdd('<%= attr.getId().getId() %>');"><fmt:message key="button.add"/></button>							
</div>
</c:if>
<c:if test="<%= (cards != null && cards.size() > 0) || Boolean.TRUE.equals(info.getAttributeEditorData(attr.getId(), BackLinkAttributeEditor.KEY_SHOW_EMPTY)) %>">
<display:table name="<%= id %>" id="<%= id %>" uid="<%= id %>" class="res"><%--

<display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
<display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
<display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
<display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
<display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>

--%>
	<%-- totaly disable ID columns 
		<display:column style="width: 5em;" title="<%= ContextProvider.getContext().getLocaleMessage("search.column.id") %>" sortable="false" maxLength="10" property="id.id"/>
	  --%>
	<display:setProperty name="basic.show.header" value="<%= info.getAttributeEditorData(attr.getId(), BackLinkAttributeEditor.KEY_SHOW_TITLE).toString() %>"/>

<%
	Card link = (Card) pageContext.getAttribute(id);
	if (cards != null && columns != null) {
		PortletURL url = renderResponse.createActionURL();
		url.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
		url.setParameter(CardPortlet.CARD_ID_FIELD, (link != null) ? link.getId().getId().toString() : "");
		
		Iterator colItr = columns.iterator();
  		while (colItr.hasNext()) {
  			SearchResult.Column col = (SearchResult.Column) colItr.next();
%>
			<display:column style="width:<%=Integer.toString(col.getWidth())%>em;"  title="<%= col.getName() %>" sortable="false" maxLength="<%= col.getWidth() %>"><%
			Attribute attrLink = null;
			if(link != null && col.getAttributeId() != null) {
				attrLink = link.getAttributeById(col.getAttributeId());
			}
			Attribute attrSecondary = scm.getSecondaryColumnValueIfExists(col, link);
			if(attrSecondary != null) {
				attrLink = attrSecondary;
			}
			//if (attrLink != null) {
				if (col.isLinked()) {
					if (attrLink != null && Attribute.TYPE_MATERIAL.equals(attrLink.getType())) {
						%><a href="<%= MessageFormat.format(BackLinkAttributeEditor.MATERIAL_DOWNLOAD_URL, new Object[] { link.getId().getId() }) %>"><%
					} else {
						// (BR4J00029530, YNikitin, 2013/07/02) Поменял прямую ссылку на вызов js-функции, которая перед переходом на новую страницу заполнит CardPortlet.ACTION_FIELD и тем самым мы избежим разблокировки текущей карточки при переходе в связанные.
						// url.toString()
						%><a href="javascript:void(0)" onclick='submitOpenLinkedCard("<%=id %>", <%=link.getId().getId().toString() %> )'><%
					}
				}

				String value = ""; 	
				if (attrLink != null) {
					// для кардлинков и бэклинков обработать MapLabels
					if (Attribute.TYPE_CARD_LINK.equals(attrLink.getType())
							|| Attribute.TYPE_TYPED_CARD_LINK.equals(attrLink.getType())
							|| Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attrLink.getType())
							|| Attribute.TYPE_BACK_LINK.equals(attrLink.getType())) {
						// здесь необходимо добавить обработку карточек с labelAttrId
						if (col.getLabelAttrId()!=null) {	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
							for (ListIterator<?> initr =  SearchResult.getCardsListForLabelColumn(labelColumns, col).listIterator(); initr.hasNext();) {	// для текущей обрабатываемой колонки в карте labelColumnsForCards SearchResult-а
								Card curCard = (Card)initr.next(); 
								if (curCard.getId().getId().equals(link.getId().getId())){		// находим соответсвующую текущему циклу карточку
									attrLink = curCard.getAttributeById(col.getAttributeId());
									break;
								}
							}
						}
						if (null != attrLink)
							value = attrLink.getStringValue();
					} else if (Attribute.TYPE_PERSON.equals(attrLink.getType())) {
						if (col.getLabelAttrId() != null) {	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
							value = SearchResult.getPersonColumnLabel(link, col, labelColumns);
						} else {
							value = attrLink.getStringValue();
						}
					} else {
						value = attrLink.getStringValue();
					}
				}
				
				if(!hasText(value) && col.isShowNullValue()) {
					// переопределяет стандартный key="table.emptyAttr" (строка 212)
					// также здесь не нужен col.isLinked()
					value = col.getNullValue();
				}

				if (CertificateInfo.SIGNATURE_ATTR_ID.equals(col.getAttributeId())) {
					DateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
					//Card linkCard = (Card)sessionBean.getServiceBean().getById(link.getId());
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
							%><c:set var="attrValue" value="<%= displayStatus %>"/><%
						} else {
							%><fmt:message key="sign.none" var="attrValue"/><%
						}
					} else {
						HtmlAttribute signAttr = (HtmlAttribute)linkCard.getAttributeById(col.getAttributeId());
						if (signAttr != null && !signAttr.isEmpty()){
							%><fmt:message key="sign.yes" var="attrValue"/><%
						} else {
							%><fmt:message key="sign.none" var="attrValue"/><%
						}
					}
				} else {
					%><c:set var="attrValue" value="<%=value%>"/>
					  <c:set var="linkedCol" value="<%=col.isLinked()%>"/><% }
				%><c:if test="${linkedCol and empty attrValue}"><fmt:message key="table.emptyAttr" var="attrValue"/></c:if><%
				%><c:out value="${attrValue}"/>
				<c:if test="${linkedCol}"></a></c:if><%--<%
				if (col.isLinked()) {
					%></a><%
				}
			//}
			--%></display:column>
<%
	   	}
	}
%>
	<c:if test="<%= sessionBean.getAttributeEditorData(attr.getId(), BackLinkAttributeEditor.KEY_REMOVE) != null %>">
	<display:column>
<%
	IntegerAttribute attrAccess = (IntegerAttribute) link.getAttributeById(BackLinkAttributeEditor.ID_ACCESS);
%>
		<c:choose>
			<c:when test="<%= BackLinkAttributeEditor.ACCESS_EDIT == attrAccess.getValue() %>">
				<a href="#" onclick="javascript:submitFormBackLinksRemove('<%= attr.getId().getId() %>', '<%= link.getId().getId().toString() %>')">
					<img src="<%= renderRequest.getContextPath() %>/images/delete.gif">
				</a>
			</c:when>
			<c:otherwise>
				<img src="<%= renderRequest.getContextPath() %>/images/delete_disable.gif">
			</c:otherwise>
		</c:choose>
	</display:column>
	</c:if>
</display:table>
</c:if>

</div>
