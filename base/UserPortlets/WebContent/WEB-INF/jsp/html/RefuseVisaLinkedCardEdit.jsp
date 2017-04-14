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

<h1 class="caption">
	<span class="header_icon ${headerIcon}"></span> <fmt:message key="caption.addLinkedAttachment"/>
</h1>


<dbmi:message text="<%= (String) renderRequest.getAttribute(ListEditor.ATTR_MESSAGE) %>"/>

<form name="listEditor" method="post" action="<portlet:actionURL/>">  
	<div class="add_linked_attachment ">
	<input type="hidden" name="<%= ListEditor.FIELD_ACTION %>" value="">

	

<% if (dataProvider instanceof SearchableListDataProvider) { %>
            	<%-- ((SearchableListDataProvider) dataProvider).includeSearchForm(renderRequest, renderResponse); --%>
            	<jsp:include page="<%= ((SearchableListDataProvider) dataProvider).getFormJspPath() %>"/>
            	<hr />	
<% } %>


	
	<div class="left_column">
		<table>
		<tr>
		<td>
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
  <td>
			<button dojoType="dijit.form.Button" style="height: 20px; margin-left: 5px; " 
														type="button" id="addButton">
			<fmt:message key="button.add"/>											
					<script type="dojo/method" event="onClick" args="evt">	
						submitForm('<%= ListEditor.ACTION_ADD %>');
					</script>
 			</button>
 			<p/>
			<button dojoType="dijit.form.Button" style="height: 20px; margin-left: 5px; margin-top: 10px" 
														type="button" id="removeButton">
			<fmt:message key="button.remove"/>											
					<script type="dojo/method" event="onClick" args="evt">	
						submitForm('<%= ListEditor.ACTION_REMOVE %>');
					</script>
 			</button> 
 			 
  	
  </td>
  </tr>
  </table>
	</div>
	<div class="right_column">
	
		<div class="divCaption"><%= dataProvider.getListTitle() %></div>
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
		
	</div>
	
	
	<div class="whole_column"> 
		
<%
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(ListEditor.FIELD_ACTION, ListEditor.ACTION_CLOSE); 
%> 

        <div class="controls">
		<div class="buttonPanel">
<% 
    if (renderRequest.getParameter(ListEditor.ATTR_MESSAGE) == null) {
%>			
						<div class="save">
							<a href="#" onClick="submitForm('<%= ListEditor.ACTION_SAVE %>')" ></a>
    					</div>
    					
    					
<%	} %>

						<div class="close">
							<a href="#" onClick="window.location.replace('<%= backURL.toString() %>')" ></a>
						</div>	
       
                </div>

	</div>	
	
	
</div>
</form>


