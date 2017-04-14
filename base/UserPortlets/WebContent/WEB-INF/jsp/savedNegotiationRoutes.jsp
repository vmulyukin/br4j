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

<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@taglib prefix="dbmiErr" uri="http://aplana.com/dbmi/tags" %>
<portlet:defineObjects />
<c:if test="${command.message != null}">
	<dbmiErr:errorMessage message="${command.message}"/>
</c:if>


<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

	String message = sessionBean.getMessage();
	if( message != null) {
		sessionBean.setMessage(null);
	} else {
		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
	}

	String cardId = sessionBean.getActiveCard().getId() != null ? sessionBean.getActiveCard().getId().getId().toString() : "";  
	Card card = sessionBean.getActiveCard();  

	%> 
	               
<script type="text/javascript" language="javascript">

function submitForm(action,id) { 
	document.selectNegotiationList.<%= CardPortlet.ACTION_FIELD %>.value = action;
	document.selectNegotiationList.<%= SavedNegotiationRoutes.NEGOTIATION_LIST_ID %>.value = id;
	document.selectNegotiationList.submit();
}
</script>
<dbmi:message text="<%= message %>"/>

<form name="selectNegotiationList" method="post" action="<portlet:actionURL/>">
<input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">  
<input type="hidden" name="<%= SavedNegotiationRoutes.NEGOTIATION_LIST_ID %>" value=""> 
<table class="indexCardMain">
    <tr>
      <td>
          <div class="buttonPanel">
           <fmt:message key="edit.page.cancel.btn" var="choiceClose" />
           <ul>
          	 
          	 <c:set var="submitAction" >submitForm('<%=CardPortlet.BACK_ACTION%>')</c:set>
             
             <ap:button text="${choiceClose}"  onClick="${submitAction}" />
             </ul>
          </div>
      </td>    
    </tr>
    </table>
    
       <table width="100%" style="margin: 10px;">
       <tr>
          <td style="vertical-align: top;">
                <div class="divCaption"></div>
                
                
  <display:table name="listInfo" id="selectedItem" uid="selectedItem" sort="list" class="res">

<%
	NegotiationListInfo item = (NegotiationListInfo) pageContext.getAttribute("selectedItem");
%>
	<display:column maxLength="50" title="Наименование" sortable="true" style="width:20%">
	<% if (item != null) {
		String itemName = item.getListName();
		String itemId = item.getListId();	
	%>
	<a href="javascript:submitForm('<%= SavedNegotiationRoutes.NEGOTIATION_LIST_SUBMIT %>', <%=itemId%>);"><%=itemName%></a>
	<% } %> 	
	</display:column>	
	<display:column maxLength="100"  title="Категория срочности" sortable="true" property="listUrgancyCategory" style="width:20%">
	</display:column>  	
	<display:column maxLength="100"  title="Тип документа" sortable="true" property="listDocType" style="width:20%">
	</display:column>
  </display:table>
            
            </td>
        </tr>
	</table>
</form>

<jsp:include page="html/CardPageFunctions.jsp"/>

