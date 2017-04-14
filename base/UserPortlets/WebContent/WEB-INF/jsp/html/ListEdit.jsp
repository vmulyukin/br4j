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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.gui.*,com.aplana.dbmi.model.DataObject" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@ page import="com.aplana.dbmi.model.ObjectId" %>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.ListEditResource"/>

<%
	ListEditor editor = (ListEditor) renderRequest./*getPortletSession().*/getAttribute(IListEditor.ATTR_INSTANCE);
	ListDataProvider dataProvider = editor.getDataProvider();
	request.setAttribute("list", dataProvider.getListData());
	request.setAttribute("selected", editor.getCurrentList());
%>
<script Type ="text/javascript" language="javascript"><!--

	function submitForm(action) {
		document.listEditor.<%= ListEditor.FIELD_ACTION %>.value = action;	
	    document.listEditor.submit();
	}

//--></script>

<dbmi:message text="<%= (String) renderRequest.getAttribute(ListEditor.ATTR_MESSAGE) %>"/>

<form name="listEditor" method="post" action="<portlet:actionURL/>">  

	<input type="hidden" name="<%= ListEditor.FIELD_ACTION %>" value="">

    <table width="100%" style="margin: 10px;">
        <col Width="45%" />
        <col Width="10%" />
        <col Width="45%" />
        <tr>
        	<td colspan="3">
                <div id="rightIcons">
<%
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(ListEditor.FIELD_ACTION, ListEditor.ACTION_CLOSE); 
%> 
					<div class="buttonPanel">
  						<ul>
<% 
    if (renderRequest.getParameter(ListEditor.ATTR_MESSAGE) == null) {
%>			
    					<li onClick="submitForm('<%= ListEditor.ACTION_SAVE %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="button.save"/></a>
    					</li>
			    		<li class="empty"><div>&nbsp;</div></li>
<%	} %>
					    <li onClick="window.location.replace('<%= backURL.toString() %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
					    	<a href="#"><fmt:message key="button.close"/></a></li>
			  			</ul>
					</div>        
                </div>
            </td>    
        </tr>
        <tr><!--Разделитель-->
            <td colspan="3">
                <hr />
            </td>
        </tr>                	

<% if (dataProvider instanceof SearchableListDataProvider) { %>
        <tr>
            <td>
            	<%-- ((SearchableListDataProvider) dataProvider).includeSearchForm(renderRequest, renderResponse); --%>
            	<jsp:include page="<%= ((SearchableListDataProvider) dataProvider).getFormJspPath() %>"/>
			</td>
			<td colspan="2"></td>
		</tr>
<% } %>

        <tr>
            <td style="vertical-align: top;">
                <div class="divCaption"><%= dataProvider.getListTitle() %></div>
  <display:table name="list" id="listItem" uid="listItem" sort="list" class="res" pagesize="<%= editor.getListSize() %>" >
	<display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
  	<display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>  
<%
	DataObject item = (DataObject) pageContext.getAttribute("listItem");
%>
	<display:column maxLength="8"><% if (item != null) {
		String itemId = item.getId().getId().toString(); %>
		<input type="checkbox" name="<%= ListEditor.FIELD_ADD_PREFIX + itemId %>" value="<%= itemId %>" >
	<% } %></display:column>

<%
	List columns = dataProvider.getColumns();
	for (Iterator itr = columns.iterator(); itr.hasNext(); ) {
		String column = (String) itr.next();
%>
	<display:column title="<%= dataProvider.getColumnTitle(column) %>" sortable="false"
		maxLength="<%= dataProvider.getColumnWidth(column) %>">
		<%
			if (editor.isDisplayLinkedColumns() && dataProvider.isColumnLinked(column)) {
				String downloadUrl = response.encodeURL(request.getContextPath() + "/MaterialDownloadServlet?" +   CardPortlet.CARD_ID_FIELD + "=" + item.getId().getId());
			%>
				<a href="<%= downloadUrl %>">
					<%= dataProvider.getValue(item.getId(), column) %>
				</a>	
			<%
			} else if (item != null) { %>
				<%= dataProvider.getValue(item.getId(), column) %>
			<% }
		
		%>
		</display:column>  

<%	} %>	
  </display:table>
                
            </td>
            
            <td style="width: 124px;">            
					<div class="buttonPanel">
  						<ul>
    					<li onClick="submitForm('<%= ListEditor.ACTION_ADD %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="button.add"/></a>
    					</li>
			    		<li class="empty"><div>&nbsp;</div></li>
					    <li onClick="submitForm('<%= ListEditor.ACTION_REMOVE %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
					    	<a href="#"><fmt:message key="button.remove"/></a></li>
			  			</ul>
					</div>        

            </td>
            <td style="vertical-align: top;">
                <div class="divCaption"><%= dataProvider.getSelectedListTitle() %></div>
  <display:table name="selected" id="selectedItem" uid="selectedItem" sort="list" class="res" >

<%
	ObjectId selectedItemId = (ObjectId) pageContext.getAttribute("selectedItem");
%>
	<display:column maxLength="8"><% if (selectedItemId != null) {
		String itemId = selectedItemId.getId().toString(); %>
		<input type="checkbox" name="<%= ListEditor.FIELD_REMOVE_PREFIX + itemId %>" value="<%= itemId %>">
	<% } %></display:column>	
  
<%
	List columns = dataProvider.getColumns();
	for (Iterator itr = columns.iterator(); itr.hasNext(); ) {
		String column = (String) itr.next();
%>
	<display:column title="<%= dataProvider.getColumnTitle(column) %>" sortable="false"
		maxLength="<%= dataProvider.getColumnWidth(column) %>"><%
	if (selectedItemId != null) { %><%= dataProvider.getValue(selectedItemId, column) %><% } %></display:column>  

<%	} %>	
  </display:table>
            
            </td>
        </tr>
	</table>
</form>


