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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="com.aplana.dbmi.useradmin.*" %>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>

<%@page import="com.aplana.dbmi.model.Person"%>
<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.useradmin.nl.UserAdminPortletResource"/>

<%
	UserAdminPortletSessionBean sessionBean = (UserAdminPortletSessionBean)renderRequest.getPortletSession().getAttribute(UserAdminPortlet.SESSION_BEAN);
  	request.setAttribute( "userList", sessionBean.getUserList());	
%>
<script Type ="text/javascript" language=javascript>

	function submitFormSearch() {
		document.UserListForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.GET_USER_LIST_ACTION %>';	
	    document.UserListForm.submit();
	}
	
	function submitFormEdit(userId) {
		document.UserListForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.EDIT_USER_ACTION %>';	
		document.UserListForm.<%= UserAdminPortlet.SELECT_USER_ID_FIELD %>.value = userId;	
	    document.UserListForm.submit();
	}
</script>

<% 
    if (renderRequest.getParameter(UserAdminPortlet.MSG_PARAM_NAME) != null) {
%>			
<table class="msg">
    <tr  class="tr1">
        <td class=td_11></td>
        <td class=td_12></td>
        <td class=td_13></td>
    </tr>
    
    <tr class="tr2">
        <td class=td_21></td>
        <td class=td_22>
        	<%= renderRequest.getParameter(UserAdminPortlet.MSG_PARAM_NAME) %>
        </td>
        <td class=td_23></td>
    </tr>
    <tr class="tr3">
        <td class=td_31></td>
        <td class=td_32></td>
        <td class=td_33></td>
    </tr>
</table>
	
<% } %>          

<form name="UserListForm" method="post" action="<portlet:actionURL/>">  
	<input type="hidden" name="<%= UserAdminPortlet.ACTION_FIELD %>" value="">
	<input type="hidden" name="<%= UserAdminPortlet.SELECT_USER_ID_FIELD %>" value="">
	
	
    <table width="100%" style="margin: 10px;">
        <col Width="50%" />
        <col Width="50%" />
        <tr>
            <td colspan="2">
                <div class="divCaption">
                    <fmt:message key="user.form.title" />                                
                </div>
                <table class="minisearch" >
                    <tr>
                        <td align="left" style="vertical-align: middle; width: 30% ">
                            <input type="text" name="<%= UserAdminPortlet.SEARCH_TEMPLATE_ID_FIELD %>" value="<%= sessionBean.getSearchTemplate() == null ? "" : sessionBean.getSearchTemplate().trim()  %>" />                           	
						</td>                            
                        <td>
							<div class="buttonPanel" >
  							<ul style="padding: 0px 10px 0pt;">
    							<li onClick="submitFormSearch()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
			    					<a href="#"><fmt:message key="user.form.search.btn"/></a>
    							</li>
			  				</ul>
							</div>        
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
	</table>
</form>

<%
// user table
 %>	

  <display:table name="userList" id="userItem" uid="userItem" sort="list" class="res" pagesize="<%= sessionBean.getListSize() %>" >
    <display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
    <display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
    <display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
    <display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>

	<display:column property="fullName"  titleKey="user.form.list.column.name" sortable="true" />
<%-- 	
	<display:column property="department.value" titleKey="user.form.list.column.depatment" sortable="true" />
 --%>
	<display:column property="email" titleKey="user.form.list.column.email" sortable="true" />
	
	<display:column >
<%
	String userId = ((Person) pageContext.getAttribute("userItem")).getId().getId().toString();
 %>		
 		<a href="javascript: submitFormEdit('<%= userId %>');" ><img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil.gif") %>' border="0" ></a>
	</display:column>
  </display:table>

