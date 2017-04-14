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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" %>

<%@taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@taglib prefix="display" uri="/WEB-INF/tld/displaytag.tld" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.event.*"%>
<%@page import="com.aplana.dbmi.showlist.ShowListTableDecorator"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>


<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.event.nls.EventLogPortletResource" scope="request"/>
<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />

<%
	final EventLogPortletSessionBean bean = (EventLogPortletSessionBean)	
			renderRequest.getPortletSession().getAttribute(EventLogPortlet.SESSION_BEAN_EVENTLOG);	
	request.setAttribute("bean", bean);
	final List<SearchResult.Column> columns = bean.getColumns();
	final List<List<String>> dataList = bean.getDataList();
	request.setAttribute("dataList", dataList);
	
	final String message = (bean.getMessage() != null) 
			? bean.getMessage().getMsg()
			: null;
	bean.setMessage(null);
	
	final PortletURL refreshURL = renderResponse.createActionURL();
	refreshURL.setParameter(EventLogPortlet.PARAM_ACTION, EventLogPortlet.ACTION_TAG_REFRESH);
%>

<table width="100%">

	<div class="divCaption">
	<%	final String title = bean.getTitle(); //bean.getUserIdStr();
		if (title != null && !title.isEmpty()) {
	%>				<dbmi:partitionCaption  message="<%= title %>"/>
	<%			
			}
	%>
	</div>
</table>

<% 
	if (message != null && message.length() > 0)
	{
%>
	  <div class="header">
		<table class="msg">
			<tr class="tr1">
				<td class=td_11>
					<c:if test="${isErrorMsg}">
							<fmt:message key="EventLogPortlet.error"/>
					</c:if>
				</td>
				<td class=td_12></td>
				<td class=td_13></td>
			</tr>

			<tr class="tr2">
				<td class=td_21></td>
				<td class=td_22><%= message %></td>
				<td class=td_23></td>
			</tr>

			<tr class="tr3">
				<td class=td_31></td>
				<td class=td_32></td>
				<td class=td_33></td>
			</tr>
		</table>
	  </div>
<% 	} %>

<table  cellspacing="2mm" cols="2">

	<col width="25%">
	<col width="32%">
	<col width="3%">
	<col width="40%">

	<tbody>
	
	<tr>
		<td>
			<div class="buttonPanel">
			  <ul>
		<%		if (bean.isShowBtnRefresh()) {
 		%>		<HR/>
 				<dbmi:button textKey="btn.refresh" href="<%= refreshURL.toString() %>"/>
		<%		} 
		%>	  </ul>
			</div>
		</td>
	</tr>
	
	
	<tr>
		<td valign="top" colspan="2">
<%
		final ShowListTableDecorator decorator = new ShowListTableDecorator(renderRequest, renderResponse);
		final String decoratorName = "EventLogTableDecorator";
		request.setAttribute(decoratorName, decorator);
		
		final int defaultSortColNum = 1;
		final String defaultOrder = "descending";
		final int pgNumber = 1;
		final int pgSize = 100;
		final int offset  = bean.getDataOffset();
%>

		<table class="content" style="padding: 2mm">
			<tr> 
			  <td valign="top">

				<display:table 
						name="dataList" 
						uid="curItem" 
						id="curItem"
						sort="list" 
						style="margin-top: 0;"
						defaultsort="<%= defaultSortColNum %>" 
						defaultorder="<%= defaultOrder %>"
						pageNumber="<%= pgNumber %>" 
						class="res" 
						pagesize="<%= pgSize %>" 
						decorator="<%= decoratorName %>"
					>
					
				<display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
				<display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
				<display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
				<display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
				<display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>
				<c:set var="contextPath" value="<%=request.getContextPath()%>"/>
				
<%	
				for (int i = offset; i < columns.size(); i++) 
				{
					final String sIndex = "[" + i + "]"; // i+offset
%>					<c:set var="rowId" value="${curItem[0]}" />
					<c:set var="columnClass" value=""/>
					
<%					final SearchResult.Column columnMeta = columns.get(i);
					final String colStyle = "width: " + columnMeta.getWidth() + "em;";
					final String defOrder = (columnMeta.getSorting() != SearchResult.Column.SORT_DESCENGING) 
							? "ascending"
							: "descending";
					
					if (columnMeta.getAction() == null || columnMeta.getAction().getId() == null ) {
					    // колонка для обычных данных %>
						<display:column
                            class="${columnClass}"
                            style="<%= colStyle %>"
                            title="<%= columnMeta.getName()%>" 
                            sortable="<%= columnMeta.isSortable()%>" 
                            maxLength="<%= columnMeta.getWidth() %>" 
                            property="<%= sIndex %>" 
                            defaultorder="<%= defOrder %>"
                        />
<%					} // else 
				  } // for
%>				 
				</display:table>
			  </td>
			</tr> 
		</table>
	  </td>
	</tr>
  </tbody>
</table>