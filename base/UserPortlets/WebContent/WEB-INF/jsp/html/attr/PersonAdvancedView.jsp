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
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.Person"%>
<%@page import="com.aplana.dbmi.action.Search"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="com.aplana.dbmi.jbr.util.CardUtils"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.PersonAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.PersonAdvancedViewer"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.service.DataServiceBean" %>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="java.util.Collection"%>
<%@taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%> 
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.PersonListResource"/>
<portlet:defineObjects/>
<% 
PersonAttribute attr = (PersonAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
DataServiceBean serviceBean = sessionBean.getServiceBean();
ObjectId name = ObjectId.predefined(StringAttribute.class, "name");
ObjectId firstName = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
ObjectId middleName = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
ObjectId lastName = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
ObjectId phone = ObjectId.predefined(StringAttribute.class, "jbr.person.phone");
ObjectId position = ObjectId.predefined(StringAttribute.class, "jbr.person.position");
ObjectId dep = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.dept");
String fName = "";
String mName = "";
String lName = "";
String ph = "";
String depName = "";
String pos = "";
String url = "";
if (attr != null && attr.getValues() != null){
	Person[] persons = (Person[])attr.getValues().toArray(new Person[attr.getValues().size()]);
	
	if(persons.length > 0)
	{
		%>
		<div>
			<table class="res" style="width: 100%; margin-top: 0px;">
				<tr>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Фамилия</td>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Имя</td>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Отчество</td>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Телефон</td>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Департамент</td>
					<td style="color: rgb(102, 102, 102);font-weight: 700;width: 80px;">Должность</td>
				</tr>
		<%
		for (int ii=0; ii<persons.length; ii++){
			Person person = persons[ii];
			if(person.getCardId() == null){String s[] = person.getFullName().split(" "); fName = s[0]; lName = s[1];}
			else{
				Search search = CardUtils.getFetchAction
				(
						person.getCardId(), 
						new ObjectId[]{firstName, middleName, lastName, phone, position, dep}
				);
				SearchResult searchResult = (SearchResult)serviceBean.doAction(search);
				Card personCard = (Card) searchResult.getCards().iterator().next();
				CardLinkAttribute depId = (CardLinkAttribute) personCard.getAttributeById(dep);
				if(depId != null && depId.getSingleLinkedId() != null)
				{
					Search searchDepartment = CardUtils.getFetchAction
					(
						((CardLinkAttribute) personCard.getAttributeById(dep)).getSingleLinkedId(), 
						new ObjectId[]{name}
					);
					SearchResult searchResultDep = (SearchResult)serviceBean.doAction(searchDepartment);
					Card depCard = (Card) searchResultDep.getCards().iterator().next();
					if(depCard.getAttributeById(name) != null)
					depName = depCard.getAttributeById(name).getStringValue();
				}
				PortletURL openUrl = null;
				openUrl = renderResponse.createActionURL();
				openUrl.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
				openUrl.setParameter(CardPortlet.CARD_ID_FIELD, person.getCardId().getId().toString());
				if(personCard.getAttributeById(lastName) != null) openUrl.setParameter(CardPortlet.ATTR_ID_FIELD, (String)lastName.getId());
				
				if(personCard.getAttributeById(firstName) != null) fName = personCard.getAttributeById(firstName).getStringValue();
				if(personCard.getAttributeById(middleName) != null) mName = personCard.getAttributeById(middleName).getStringValue();
				if(personCard.getAttributeById(lastName) != null) lName = personCard.getAttributeById(lastName).getStringValue();
				if(personCard.getAttributeById(phone) != null) ph = personCard.getAttributeById(phone).getStringValue();
				if(personCard.getAttributeById(position) != null) pos = personCard.getAttributeById(position).getStringValue();
				url = openUrl.toString();
			}
	%>
	<tr>
		<td>
			<%if(url != ""){ 
				// (BR4J00029530, YNikitin, 2013/07/02) Поменял прямую ссылку на вызов js-функции, которая перед переходом на новую страницу заполнит CardPortlet.ACTION_FIELD и тем самым мы избежим разблокировки текущей карточки при переходе в связанные.
				//<a href="url">lName</a>
			%>
			<a href="javascript:void(0)" onclick='submitOpenLinkedCard("<%=(String)lastName.getId() %>", <%=person.getCardId().getId().toString() %> )'><%=lName%></a>
			<%} else { %>
			<%=lName%>
			<%} %>
		</td>
		<td><%=fName%></td>
		<td><%=mName%></td>
		<td><%=ph%></td>
		<td><%=depName%></td>
		<td><%=pos%></td>
	</tr>
	<%
	};

%>
</table>
<br>
</div>
<%
	
	};
}
%>