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
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.LinkedCardUtils"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.util.ObjectIdUtils"%>
<%@page import="com.aplana.dbmi.model.TypedCardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.DatedTypedCardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.DateAttribute"%>
<%@page import="com.aplana.dbmi.card.DateTimeAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.util.DateUtils"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ListIterator"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Date"%>
<%@page import="com.aplana.dbmi.card.graph.Graph"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="org.apache.commons.lang.StringUtils"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="com.aplana.dbmi.model.MaterialAttribute"%>
<%@page import="com.aplana.dbmi.card.TypedCardLinkAttributeViewer"%>
<%@page import="com.aplana.dbmi.card.DatedTypedCardLinkAttributeViewer"%>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.ListEditResource"/>

<portlet:defineObjects/>
<%
	TypedCardLinkAttribute attr = (TypedCardLinkAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String id = CardPortlet.getAttributeFieldName(attr);

	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	Collection items = sessionBean.getServiceBean().listChildren(attr.getReference(), ReferenceValue.class);
	Map types = ObjectIdUtils.collectionToObjectIdMap(items);

	// (2010/02, RuSA) OLD: request.setAttribute(id, attr.getValues());
	// request.setAttribute(id, attr.getIdsLinked()); request.setAttribute(id, attr.getLabelLinkedCards());
	CardPortletCardInfo info = sessionBean.getActiveCardInfo();
	Collection columns = (info == null) ? null 
		: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST);
	Collection cards = (info == null) ? null 
		: (Collection) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
	Map labelColumns = (info == null) ? null 
		: (Map) info.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_LABEL_COLUMNS_LIST);
	request.setAttribute(id, cards);
	// Collection columns = attr.getColumns();
	
	String typedCaption = (String) info.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.KEY_TYPE_CAPTION);
	if(typedCaption == null) {
		typedCaption = ContextProvider.getContext().getLocaleMessage("search.column.linktype");
	}
	String dateCaption = (String) info.getAttributeEditorData(attr.getId(), DatedTypedCardLinkAttributeViewer.KEY_DATE_TYPE_CAPTION);
	if(dateCaption == null) {
		dateCaption = ContextProvider.getContext().getLocaleMessage("search.column.datelinktype");
	}

	//CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	//sessionBean.getCard().getMaterialType(); ***** что это было?!
%>

<%
	Boolean isViewGraph = (Boolean)sessionBean.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.GRAPH_IS_VIEW);
	if (isViewGraph != null && isViewGraph.booleanValue()) {
		Graph graph = (Graph)sessionBean.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.GRAPH_DATA);
		
		PortletURL link = renderResponse.createActionURL();
		link.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
		link.setParameter(CardPortlet.ATTR_ID_FIELD, (String)attr.getId().getId());
		String prefixId = JspAttributeEditor.getAttrHtmlId(attr);
		String jsonTree = graph.getJSONTree(link, CardPortlet.CARD_ID_FIELD, prefixId).toString();
%>
	<c:set var="htmlAttrId" value="<%=prefixId%>"/>

	<button dojoType="dijit.form.Button" onclick="dijit.byId('dialog_${htmlAttrId}').show()">Графическое представление</button>
	<div class="graph" dojoType="dijit.Dialog" id="dialog_${htmlAttrId}" title="Графическое представление"
    	 style="width: 925px; height: 422px">
    	<div id="viewGraph_${htmlAttrId}" class="viewGraph"></div>
	</div>
	<%@include file="AttributeActionsButtonPane.jsp"%>
<script language="JavaScript" type="text/javascript">
	dojo.require('dijit.Dialog');
 	dojo.require('dijit.form.Button');
	
	dojo.addOnLoad(function(){
		var json = <%=jsonTree%>;
		initTree('viewGraph_${htmlAttrId}', json);
	});
</script>
<%
	}
%>
<c:if test="<%=(cards != null && cards.size() > 0) || ((Boolean) info.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.KEY_SHOW_EMPTY)).booleanValue()%>">
	<display:table name="<%=id%>" id="<%=id%>" uid="<%=id%>" class="res" style="margin:0px 0px 10px 10px;">
		<display:setProperty name="basic.show.header" value="<%=info.getAttributeEditorData(attr.getId(), TypedCardLinkAttributeViewer.KEY_SHOW_TITLE).toString()%>"/>
		<%-- totaly disable ID columns 
			<display:column style="width: 5em;" title="<%= ContextProvider.getContext().getLocaleMessage("search.column.id") %>" sortable="false" maxLength="10" property="id.id"/>
		  --%>
<%	if (columns != null) {
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
			
			SearchResult.Column col = null;
			if (originalColumn.isReplaceAttribute()){	// те атрибуты, на которые надо заменить другие, в чистом виде не отображаются 
				continue;
			}
			
			col = SearchResult.getRealColumnForCardIfItReplaced(originalColumn, linked, columns);
			
%>			<display:column style="width: <%=col.getWidth()%>em;"  title="<%=col.getName()%>" sortable="false" maxLength="<%=col.getWidth()%>">
<%			Attribute attrVal = linked.getAttributeById(col.getAttributeId());
			String value = "";
			if (attrVal != null) {
				if (Attribute.TYPE_CARD_LINK.equals(attrVal.getType()) // для кардлинков и бэклинков обработать MapLabels
						|| Attribute.TYPE_TYPED_CARD_LINK.equals(attrVal.getType())
						|| Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attrVal.getType())
						|| Attribute.TYPE_BACK_LINK.equals(attrVal.getType())) {
					// здесь необходимо добавить обработку карточек с labelAttrId
					if (col.getLabelAttrId() != null && labelColumns != null){	// если колонка содержит заполненный в настройках атрибут LabelAttrId  
						for (ListIterator<?> initr =  SearchResult.getCardsListForLabelColumn(labelColumns, col).listIterator(); initr.hasNext();) {	// для текущей обрабатываемой колонки в карте labelColumnsForCards SearchResult-а
							Card curCard = (Card)initr.next(); 
							if (curCard.getId().getId().equals(linked.getId().getId())){		// находим соответсвующую текущему циклу карточку
								attrVal = curCard.getAttributeById(col.getAttributeId());
								break;
							}
						}
					}
					value = attrVal.getStringValue();
				} /*else if(Attribute.TYPE_PERSON.equals(attrVal.getType())) {
					if (col.getLabelAttrId() != null) {	// если колонка содержит заполненный в настройках атрибут LabelAttrId
						value = SearchResult.getPersonColumnLabel(linked, col, labelColumns);
					} else {
						value = attrVal.getStringValue();
					}
				} */ else {
					value = attrVal.getStringValue();
				}
			}
			
			if (col.isLinked()) {
				if (MaterialAttribute.class.equals(col.getAttributeId().getType())) {
%>					<a href="#" onclick="downloadCardMaterial(<%= downloadCardId %>)">
<%				} else {
					// (BR4J00029530, YNikitin, 2013/07/02) Поменял прямую ссылку на вызов js-функции, которая перед переходом на новую страницу заполнит CardPortlet.ACTION_FIELD и тем самым мы избежим разблокировки текущей карточки при переходе в связанные.
					// link.toString()
%>					<a href="#" onclick='submitOpenLinkedCard("<%=(String)attr.getId().getId()%>", <%= downloadCardId %> )'>
<%				}
				if (attrVal == null)
					value = "(no value)";
			}
%>			<%=value%>
<%			if (col.isLinked()) {
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
%>						<a href="<%=linked.getUrl()%>"  target="_blank" ><fmt:message key="material.open.external.url" /></a>
<%						break;
				}
			}
%>			</display:column>
<%		}
	}
%>
	<display:column style="width: <%=col.getWidth()%>em;" title="<%=typedCaption%>" sortable="false">
<%
	Card linked = (Card) pageContext.getAttribute(id);
	if (attr.getTypes().get(linked.getId().getId()) == null) {
%>		<label></label>
<%	} else {
%>		<label><%=((ReferenceValue)types.get(new ObjectId(ReferenceValue.class, attr.getTypes().get(linked.getId().getId())))).getValueRu()%></label>
<%	}
%>	</display:column>
		
	<!-- Колонка отображается в случает если пришел DatedTypedCardLinkAttribute -->
<%	if(DatedTypedCardLinkAttribute.class.equals(attr.getClass())) {
		DatedTypedCardLinkAttribute datedTypedAttr = (DatedTypedCardLinkAttribute) attr;
%>		<display:column style="width: <%= col.getWidth() %>em;" title="<%= dateCaption %>" sortable="false">
<%		Card linked = (Card) pageContext.getAttribute(id);
		String dateString = datedTypedAttr.getFormattedDateValue((Long) linked.getId().getId());
		if (StringUtils.isEmpty(dateString)) {
%>			<label></label>
<%		} else {
%>			<label><%= dateString %></label>
<%		}
%>		</display:column>
<%	}
%>	</display:table>
</c:if>
	
<%--
	request.getSession().setAttribute(CardPortlet.SESSION_USER, request.getUserPrincipal());
--%>
