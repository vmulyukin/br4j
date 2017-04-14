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
<%@page import="com.aplana.dbmi.delegate.*"%>
<%@page import="com.aplana.dbmi.delegate.DelegateListSessionBean.*"%>
<%@page import="com.aplana.dbmi.delegate.DelegateListPortlet"%>
<%@page import="com.aplana.dbmi.showlist.ShowListTableDecorator"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%-- 
<%@page import="com.aplana.dbmi.*"%>
<%@page import="com.aplana.dbmi.util.JspUtils"%>
<%@page import="org.displaytag.util.SortingState"%>
--%>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.delegate.nls.DelegateListPortletResource" scope="request"/>
<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />

<style type="text/css">
	.oddRow { 
		background: #eee;
		padding: 5px;
		border: solid 1px grey; 
	}
</style> 

<%
	// данные делегирований ...
	final DelegateListSessionBean bean = (DelegateListSessionBean)
			// renderRequest.getAttribute("delegateListBean");	
			renderRequest.getPortletSession().getAttribute(DelegateListPortlet.SESSION_BEAN_DELEGATE);	
	request.setAttribute( "bean", bean);
	final List<SearchResult.Column> columns = bean.getColumns();
	final List<List<String>> dataList = bean.getDataList();
	request.setAttribute( "dataList", dataList);
	boolean isEditAccessExists = bean.isEditAccessExists();	
%>
	<c:set var="isErrorMsg" value="<%= ((bean.getMessage() != null) && bean.getMessage().isError()) %>"/>
	<c:set var="isSrcUsrFixed" value="<%= (bean.isSourceUserSelectable() ? "false" : "true") %>" scope="request" />
<%
	final String message = (bean.getMessage() != null) 
				? bean.getMessage().getMsg()
				: renderRequest.getParameter(DelegateListPortlet.PARAM_MSG);
	bean.setMessage(null);

	// данные для редактирования одного делегирования ...
	final DelegateEditBean editBean = (DelegateEditBean) 
			renderRequest.getPortletSession().getAttribute(DelegateListPortlet.BEAN_EDIT);
	request.setAttribute( DelegateListPortlet.BEAN_EDIT, editBean);
	final boolean isEditing = (editBean != null);

	// final PortletURL url = renderResponse.createRenderURL();

	final PortletURL refreshURL = renderResponse.createActionURL();
	refreshURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_REFRESH);

	final PortletURL backURL = renderResponse.createActionURL();
	backURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_BACK);
	backURL.setWindowState(WindowState.NORMAL);

	final PortletURL gotoURL = renderResponse.createActionURL();
	gotoURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_GOTO_TAB); 
	gotoURL.setWindowState(WindowState.MAXIMIZED);

	final PortletURL createURL = renderResponse.createActionURL();
	createURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_CREATE_DELEGATE); 
	createURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.delegateFromPerson.toString() );
	createURL.setWindowState(WindowState.MAXIMIZED);

	//final PortletURL acceptURL= renderResponse.createActionURL();
	//acceptURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_ACCEPT_EDIT);

	final PortletURL cancelURL= renderResponse.createActionURL();
	cancelURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_CANCEL); 

	final PortletURL editURL = renderResponse.createActionURL();
	editURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_EDIT_DELEGATE); 
	editURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.delegateFromPerson.toString() );
	editURL.setWindowState(WindowState.MAXIMIZED);

	//final PortletURL deleteURL = renderResponse.createActionURL();
	//deleteURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_DELETE_DELEGATE); 
	//deleteURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.delegateFromPerson.toString() );
	//deleteURL.setWindowState(WindowState.MAXIMIZED);
%>


<%-- Верхние кнопки: "Назад"  "Делегирования"  "История"  "От других" --%>
<table width="100%">
	<% if (!isEditing) { %>
		<tr> <td>
			<div class="buttonPanel">
				<ul>
					<li class="back">
						<a href="<%= backURL.toString() %>">
							<div class="ico_back img">&nbsp;</div>
							<p><fmt:message key="page.back.link" /></p>
						</a>
					</li>	
<!--   Кнопки спрятались
<%
                    gotoURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.delegateFromPerson.toString());
%>					<dbmi:button textKey="btn.page1" href="<%= gotoURL.toString()%>" />
<%
					gotoURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.history.toString() );
%>					<dbmi:button textKey="btn.page2" href="<%= gotoURL.toString()%>" />
<%
					gotoURL.setParameter(DelegateListPortlet.PARAM_TAB_ID, DelegateListSessionBean.EDelegatePgTab.givenToPerson.toString() );
%>					<dbmi:button textKey="btn.page3" href="<%= gotoURL.toString()%>" /> -->
				</ul>
			</div>
		</td></tr>
<%	} %>

	<div class="divCaption">
	<%	final Title title = bean.getTitle();
		if (title != null &&  title.isShow()) { 
			if ( title.isUseJspName() ) {
	%>				<jsp:include page="<%= title.getJspName() %>" />
	<%			} else { 
	%>				<dbmi:partitionCaption  message="<%= title.getHtmlText() %>"/>
	<%			}
			}
	%>
	</div>

	<%if (isEditAccessExists){ %>
	<%-- Кнопки:  Сохранить  Отменить  Добавить --%>
	<tr> <td>
		<div class="buttonPanel">
<%			if (bean.getPgTab() == EDelegatePgTab.delegateFromPerson) {
%>			<ul>
				<% if (!isEditing) {%>
					<HR/>
					<%-- <c:set var="acceptURLStr" value="<%= acceptURL.toString()--% >"/> acceptURL закомментирован выше
					<dbmi:button textKey="btn.save"
							onClick="window.location.replace('${acceptURLStr}')"
						/> --%>

					<%--
					<c:set var="cancelURLStr" value="<%= cancelURL.toString()%>"/>
					<dbmi:button textKey="btn.cancel"
							onClick="window.location.replace('${cancelURLStr}')"
						/>
					  --%>
					<c:set var="addURLStr" value="<%= createURL.toString()%>"/>
					<dbmi:button textKey="btn.add"
							onClick="window.location.replace('${addURLStr}')"
						/>
				<% } %>
			</ul>
<%			}
 %>		</div>
	</td></tr>
	<%} %>	
</table>


<script type="text/javascript">
	function <portlet:namespace/>_SubmitDelegateForm(){
		var form = document.<%= DelegateListPortlet.FORM_NAME %>;
		form.submit();
	}

</script>

<% 
	if (message != null && message.length() > 0) 
	{
%>
	  <div class="header">
	  	<c:choose>
	  		<c:when test="${isErrorMsg}">
	  			<table class="err_msg">
	  		</c:when>
	  		<c:otherwise>
	  			<table class="msg">
	  		</c:otherwise>
	  	</c:choose>
			<tr class="tr1">
				<td class=td_11>
					<!--<c:if test="${isErrorMsg}">
							<fmt:message key="DelegateListPortlet.error"/>
					</c:if>-->
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
	<%-- Заголовок tab-страницы --%>
<%--	
	  <tr>
		<td>
			<div class="divCaption">
<%				final Title title = bean.getTitle();
				if (title != null &&  title.isShow()) { 
					if ( title.isUseJspName() ) {
		%>				<jsp:include page="<%= title.getJspName() %>" />
		<%			} else { 
		%>				<%= title.getHtmlText() %>
		<%			}   
				}
		%>	</div>
		</td>
	  </tr>
  --%>

	  <tr>
		<td>
			<div style="float: left">
		<%		if (bean.isShowBtnRefresh()) {
 		%>			<a href="<%= refreshURL.toString() %>" style="padding-left: 10px;"><fmt:message key="DelegateListPortlet.refresh" /></a>
		<%		} 
		%>	</div>
		</td>
		<%--td>	userId: <%= bean.getUserIdStr() %--%>
		</td>
		<td></td>
		<td></td>
	  </tr>

<c:set var="submitAction" value="return ${namespace}_SubmitDelegateForm()"/>


	<%-- end caption & menu --%>

	  <tr>
		<td valign="top" colspan="2">
<%
		  // data DELEGATE table...
		  final ShowListTableDecorator decorator = new ShowListTableDecorator(renderRequest, renderResponse);
		  // decorator.setLinkPg( sessionBean.getLinkPg());
		  // decorator.setLinkUrl( sessionBean.getLinkUrl());
		  // decorator.setLink( editCardURL.toString());
		  final String decoratorName = "DelegateTableDecorator";
		  request.setAttribute(decoratorName, decorator);

		  final int defaultSortColNum = -1;
		  final String defaultOrder = "ascending";
		  final int pgNumber = 1;
		  final int pgSize = 100;
		  final int offset  = bean.getDataOffset();
%>
		  <%-- <dbmi:partitionCaption messageKey="templatesListCaption" />  --%>
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
				  <c:set var="isEditing" value="${not empty delegateEditBean}"/>
<%	
				  for (int i = offset ; i < columns.size() ; i++  ) 
				  {
					final String sIndex = "[" + i + "]"; // i+offset
%>					<c:set var="rowId" value="${curItem[0]}" />
					<c:set var="columnClass" value=""/>
					<c:if test="${ (delegateEditBean.editDelegateIdx >=0) && (delegateEditBean.editDelegateIdx + 1 == rowId)}">
						<c:set var="columnClass" value="alternate"/>
					</c:if>
					
<%					final SearchResult.Column columnMeta = columns.get(i);
					final String colStyle = "width: " + columnMeta.getWidth() + "em;";
					String defOrder = defaultOrder;
					if (columnMeta.getSorting() == SearchResult.Column.SORT_ASCENDING) {
						defOrder = "ascending";
					} else if (columnMeta.getSorting() == SearchResult.Column.SORT_DESCENGING) {
						defOrder = "descending";
					}
					
					final Class attrClass = columnMeta.getAttributeId().getType();

					if (DelegateListPortlet.COL_ACTION_EDIT.equals(columnMeta.getAction().getId() )){
					              // колонка с вызовом редактирования %>
                        <display:column class="${columnClass}" 
                            title="<%= columnMeta.getName() %>"
                            style="vertical-align: top;"
                            >
                            
                            <c:set var="isEditable" value="<%=bean.isDelegationEditable(Integer.parseInt((String)pageContext.getAttribute("rowId")) - 1)%>"/>
                            <c:set var="isEditAccessExists" value="<%=isEditAccessExists%>"/>

                            <dbmi:linkimage 
                                enable="${not isEditing && isEditAccessExists && isEditable}"
                                urlPrefix="${contextPath}" 
                                enableIcon="edit" disableIcon="edit_disable"
                            >
                                <portlet:actionURL> 
                                  <portlet:param 
                                        name="<%= DelegateListPortlet.PARAM_ACTION %>" 
                                        value="<%= DelegateListPortlet.ACTION_TAG_EDIT_DELEGATE %>" />
                                  <portlet:param 
                                        name="<%= DelegateListPortlet.PARAM_DELEGATE_IDX %>" 
                                        value= "${rowId}" />
                                </portlet:actionURL>
                            </dbmi:linkimage>
                        </display:column>
                    <%}
					if (DelegateListPortlet.COL_ACTION_DELETE.equals( columnMeta.getAction().getId() )) { 
                                   // колонка с вызовом удаления %>
                          <display:column class="${columnClass}" 
                            title="<%= columnMeta.getName() %>"
                            style="vertical-align: top;"
                            >
                            
                            <c:set var="isEditable" value="<%=bean.isDelegationEditable(Integer.parseInt((String)pageContext.getAttribute("rowId")) - 1)%>"/>
                            <c:set var="isEditAccessExists" value="<%=isEditAccessExists%>"/>
	                            
	                            <dbmi:linkimage 
	                                enable="${not isEditing && isEditAccessExists && isEditable}"
	                                urlPrefix="${contextPath}" 
	                                enableIcon="delete" disableIcon="delete_disable"
	                            >
	                                <portlet:actionURL> 
	                                  <portlet:param 
	                                        name="<%= DelegateListPortlet.PARAM_ACTION %>" 
	                                        value="<%= DelegateListPortlet.ACTION_TAG_DELETE_DELEGATE %>" />
	                                  <portlet:param 
	                                        name="<%= DelegateListPortlet.PARAM_DELEGATE_IDX %>" 
	                                        value= "${rowId}" />
	                                </portlet:actionURL>
	                            </dbmi:linkimage>
                            </display:column>
                     <%} 
					if (columnMeta.getAction().getId() == null ) { 
					    // колонка для обычных данных %>
						<display:column 
                            class="${columnClass}"
                            style="<%= colStyle %>"
                            title="<%= columnMeta.getName()%>" 
                            sortable="<%= columnMeta.isSortable()%>" 
                            maxLength="<%= columnMeta.getWidth() %>" 
                            property="<%= sIndex %>" 
                            defaultorder="<%= defOrder %>"
                            comparator="<%= DelegateHelper.getDelegateListComparator(attrClass) %>"
                        />
<%					} // else 
				  } // for
 %>				</display:table>
			  </td>
			</tr> 
		  </table>

		</td>

		<td>
		</td>

		<%-- Форма редактирования одного делегата --%>
		<td valign="top">
<%			if (isEditAccessExists&&editBean != null) {
%>				<div id="blockAttributesPanel">
					<%-- %@include file="DelegateEditForm.jsp" --%>
					<jsp:include page="/WEB-INF/jsp/html/delegate/DelegateEditForm.jsp" />
				</div>
<%			} %>
		</td>
	  </tr>
	</tbody>
</table>
