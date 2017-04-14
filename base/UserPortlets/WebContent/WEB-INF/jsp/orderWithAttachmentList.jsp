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

<portlet:defineObjects />

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

function submitForm(action) { 
	document.attachListEditor.<%= CardPortlet.ACTION_FIELD %>.value = action;
	document.attachListEditor.submit();
}
</script>
<dbmi:message text="<%= message %>"/>

<form name="attachListEditor" method="post" action="<portlet:actionURL/>">
<input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">  

<table class="indexCardMain">
    <col Width="50%" />
    <col Width="50%" />
    <tr>
      <!--Заголовок-->
      <td>
      </td>
      <td>
          <div class="buttonPanel" >
           <fmt:message key="edit.page.cancel.btn" var="choiceClose" />
           <fmt:message key="edit.page.save.bnt" var="choiceSave" />
           <ul>
           <c:set var="submitAction">submitForm('<%=CardPortlet.SAVE_AND_CLOSE_EDIT_MODE_ACTION%>')</c:set>
             <ap:button text="${choiceSave}"  onClick="${submitAction}" />
          	 <c:set var="submitAction">submitForm('<%=CardPortlet.BACK_ACTION%>')</c:set>
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
  <display:table name="resInfo" id="selectedItem" uid="selectedItem" sort="list" class="res">

<%
	AttachInfo item = (AttachInfo) pageContext.getAttribute("selectedItem");
%>
	<display:column maxLength="8" title="Выбор">
	<% if (item != null) {
		String itemId = item.getAttId();%>
	<input type="checkbox" name="checkbox" value="<%=itemId%>">
	<% } %>
	</display:column>
	<display:column title="Наименование" sortable="true" property="attachText">
	</display:column> 	

  </display:table>
            
            </td>
        </tr>
	</table>
</form>

<jsp:include page="html/CardPageFunctions.jsp"/>

