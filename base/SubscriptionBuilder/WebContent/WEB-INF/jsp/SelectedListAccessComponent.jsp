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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*, com.aplana.dbmi.component.*" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>

<%@page import="com.aplana.dbmi.model.*"%>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.component.nl.AccessComponentPortletResource"/>

<%
	AccessComponent accessComponent = (AccessComponent)renderRequest.getPortletSession().getAttribute(AccessComponent.ACCESS_HANDLER);
	List list = null;
	List selectedList = null;
	if (accessComponent.isDepartmentEdit()) {
		list = accessComponent.getDepartmentList();
		
		selectedList = accessComponent.getSelectedDepartmentList();
	} else {
		list = accessComponent.getUserList();
		selectedList = accessComponent.getSelectedIndividualList();
	}
  	request.setAttribute( "list", list);	
  	request.setAttribute( "selectedList", selectedList);	

%>
<script Type ="text/javascript" language=javascript>

	function submitForm(action) {
		document.SelectedListForm.<%= AccessComponent.ACTION_FIELD %>.value = action;	
	    document.SelectedListForm.submit();
	}

</script>

<% 
    if (renderRequest.getParameter(AccessComponent.MSG_PARAM_NAME) != null) {
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
        	<%= renderRequest.getParameter(AccessComponent.MSG_PARAM_NAME) %>
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


<portlet:actionURL var="formAction">
    <portlet:param name="portlet_action" value="distribution"/>
</portlet:actionURL>


<form name="SelectedListForm" method="post" action="${formAction}">  

	<input type="hidden" name="<%= AccessComponent.ACTION_FIELD %>" value="">

    <table width="100%" style="margin: 10px;">
        <col Width="45%" />
        <col Width="10%" />
        <col Width="45%" />
        <tr>
        	<td colspan="3">
                <div id=rightIcons>
<%
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(AccessComponent.ACTION_FIELD, AccessComponent.BACK_ACTION); 
			backURL.setParameter("portlet_action", "distribution");
%> 
					<div class="buttonPanel">
  						<ul>
<% 
    if (renderRequest.getParameter(AccessComponent.MSG_PARAM_NAME) == null) {
%>			
    					<li onClick="submitForm('<%= AccessComponent.STORE_ACTION %>');" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="access.form.save.bnt"/></a>
    					</li>
			    		<li class="empty"><div>&nbsp;</div></li>
<%	} %>
					    <li onClick="window.location.replace('<%= backURL.toString() %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
					    	<a href="#"><fmt:message key="access.form.cancel.btn"/></a></li>
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

<% if (!accessComponent.isDepartmentEdit()) { %>
		<tr>
            <td colspan="3">
                <table class="minisearch" >
                    <tr>
                        <td align="left" style="vertical-align: middle; width: 30% ">
                            <input type="text" name="<%= AccessComponent.SEARCH_FIELD %>" value="<%= accessComponent.getSearchTemplate() %>" />                           	
						</td>                            
                        <td>
							<div class="buttonPanel" >
  							<ul style="padding: 0px 10px 0pt;">
    							<li onClick="submitForm('<%= AccessComponent.FIND_ACTION %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
			    					<a href="#"><fmt:message key="access.form.find.btn"/></a>
    							</li>
			  				</ul>
							</div>        
                        </td>
                    </tr>
                </table>
			</td>
		</tr>			                
<% } %>                

        <tr>
            <td style="vertical-align: top;">
                <div class="divCaption"><% if (accessComponent.isDepartmentEdit()) {%><fmt:message key="access.form.department.list.label"/><% } else { %><fmt:message key="access.form.user.list.label"/><% } %></div>
  <display:table name="list" id="listItem" uid="listItem" sort="list" class="res" pagesize="<%= accessComponent.getListSize() %>" >
  
<%
	String itemId = "";
	Object item = pageContext.getAttribute("listItem");
	if (item != null) {
	
		if (accessComponent.isDepartmentEdit()) {
			itemId = ((ReferenceValue)item).getId().getId().toString();
		} else {
			itemId = ((Person)item).getId().getId().toString();
		}
	}
 %>  

	<display:column maxLength="8"> 
		<input type="checkbox" name="<%= AccessComponent.ADD_ITEM_ID_FIELD + itemId %>" value="<%= itemId %>" >
	</display:column>	

<% if (accessComponent.isDepartmentEdit()) { %>
	<display:column property="value" titleKey="access.form.column.department" sortable="true" />
<% } else { %>
	<display:column property="fullName"  titleKey="access.form.column.user" sortable="true" />
	<%--  <display:column property="department.value" titleKey="access.form.column.department" sortable="true" /> --%>
<% }%>	
  </display:table>
                
            </td>
            
            <td style="width: 124px;">            
					<div class="buttonPanel">
  						<ul>
    					<li onClick="submitForm('<%= AccessComponent.ADD_ACTION %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="access.form.add.btn"/></a>
    					</li>
			    		<li class="empty"><div>&nbsp;</div></li>
					    <li onClick="submitForm('<%= AccessComponent.REMOVE_ACTION %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
					    	<a href="#"><fmt:message key="access.form.remove.btn"/></a></li>
			  			</ul>
					</div>        

            </td>
            <td style="vertical-align: top;">
                <div class="divCaption"><% if (accessComponent.isDepartmentEdit()) {%><fmt:message key="access.form.department.access.label"/><% } else { %><fmt:message key="access.form.individual.access.label"/><% } %></div>
  <display:table name="selectedList" id="selectedItem" uid="selectedItem" sort="list" class="res" >
<%
	String itemId = "";
	Object item = pageContext.getAttribute("selectedItem");
	if (item != null) {
		if (accessComponent.isDepartmentEdit()) {
			itemId = ((ReferenceValue)item).getId().getId().toString();
		} else {
			itemId = ((Person)item).getId().getId().toString();
		}
	}
 %>  

	<display:column maxLength="8"> 
		<input type="checkbox" name="<%= AccessComponent.REMOVE_ITEM_ID_FIELD + itemId %>" value="<%= itemId %>" >
	</display:column>	
  
  
<% if (accessComponent.isDepartmentEdit()) { %>
	<display:column property="value" titleKey="access.form.column.department" sortable="true" />
<% } else { %>
	<display:column property="fullName"  titleKey="access.form.column.user" sortable="true" />
	<%-- <display:column property="department.value" titleKey="access.form.column.department" sortable="true" /> --%>
	<% }%>
  </display:table>
            
            </td>
        </tr>
	</table>
</form>


