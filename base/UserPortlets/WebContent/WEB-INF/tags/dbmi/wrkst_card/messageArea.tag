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
<%--
    Tag for displaying information message
--%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<%@ tag import="com.aplana.dbmi.card.CardPortlet" %>
<%@ tag import="com.aplana.dbmi.card.CardPortletSessionBean" %>
<%@ tag import="com.aplana.dbmi.model.Card" %>
<%@ tag import="com.aplana.dbmi.model.ObjectId" %>
<%@ tag import="javax.portlet.PortletURL" %>
<%@ tag import="java.util.List" %>
<%@ tag import="java.util.Map" %>

<%@attribute name="card" required="true" type="com.aplana.dbmi.model.Card" %>

<portlet:defineObjects/>

<%
    CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

    //////////////////
    CardPortletSessionBean.PortletMessage cardContainer = sessionBean.getPortletMessage();
    Map<ObjectId,String> cardDesc = null;
    List<ObjectId> cardIds = null;
    String message = null;
    if (cardContainer != null){
        cardDesc = cardContainer.getContainer();
        cardIds = cardContainer.getCardIds();
        message = cardContainer.getMessage();
        if (message == null)
            message = "";
        sessionBean.setPortletMessage( null);
    } else {
        message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
    }

 %>



<% if (message != null) {%>

    <table class="msg">
        <tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
        <tr class="tr2"><td class="td_21"/><td class="td_22">
        <%= message %>
    <%
        if (!(cardDesc == null || cardDesc.isEmpty()) && !(cardIds == null || cardIds.isEmpty()) )
        {
            %>
            <br>
            <ol class="cardinfo">
            <%
                for (ObjectId cid: cardIds)
                {
                    String description = cardDesc.get(cid);
                    StringBuilder textRow = new StringBuilder();
                    PortletURL URL = renderResponse.createActionURL();
                    URL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
                    URL.setParameter(CardPortlet.CARD_ID_FIELD, cid.getId().toString());
                    if (description.contains(CardPortlet.HAS_ATTACHMENTS_TAG))
                    {
                        description = description.replace(CardPortlet.HAS_ATTACHMENTS_TAG,"");
                        textRow.append(description);
                        textRow.append("<a href=\"")
                            //>>>>>
                            .append(request.getContextPath())
                            .append("/ShowMaterialServlet?")
                            .append(CardPortlet.CARD_ID_FIELD)
                            .append("="+cid.getId().toString())
                            //<<<<<
                            .append("\" title=\"������� ��������\" target=\"_blank\"> <span class=\"repeatlist\"></span> </a>");
                    }
                    %>
                    <li class="cardinfo"><%= textRow %></li>
            <%
                } // for
            %>
            </ol>
            <%
        }
    %>
    <%
        if (card == null) {
    %>
            &nbsp;<a HRef="<portlet:actionURL><portlet:param name="<%= CardPortlet.ACTION_FIELD %>" value="<%= CardPortlet.BACK_ACTION %>" /></portlet:actionURL>" ><fmt:message key="view.page.back.link" /></a>
    <%
        }
    %>
            </td><td class="td_23"/>
        </tr>
        <tr class="tr3"><td class="td_31"/><td class="td_32"/><td class="td_33"/></tr>
    </table>

<%} %>
