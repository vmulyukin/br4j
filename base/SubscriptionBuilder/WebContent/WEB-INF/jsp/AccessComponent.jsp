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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*, com.aplana.dbmi.component.*" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/treetag.tld" prefix="tree" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 

<%@page import="com.aplana.dbmi.model.*"%>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.component.nl.AccessComponentPortletResource"/>

<%
	AccessComponent accessComponent = (AccessComponent)renderRequest.getPortletSession().getAttribute(AccessComponent.ACCESS_HANDLER);
%>

<script Type ="text/javascript" language=javascript>
    
	function submitFormAccessEdit(action) {
		document.<%= accessComponent.getFormName() %>.<%= AccessComponent.ACTION_FIELD %>.value = action;	
	    document.<%= accessComponent.getFormName() %>.submit();
	}

    function DisabledObj(obj)
    {
    	var isDisable = obj.checked;
        document.getElementById('<%= AccessComponent.MANGER_1_ACCESS_TYPE_ID %>').disabled = isDisable;
        document.getElementById('<%= AccessComponent.MANAGER_2_ACCESS_TYPE_ID %>').disabled = isDisable;
        document.getElementById('<%= AccessComponent.DEPARTMENT_ACCESS_TYPE_ID %>').disabled = isDisable;
        document.getElementById('<%= AccessComponent.INDIVIDUAL_ACCESS_TYPE_ID %>').disabled = isDisable;
        
        if (isDisable) {
	        document.getElementById('<%= AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION %>').style.display='none';
	        document.getElementById('<%= AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION %>').style.display='none';
        } else {
	        document.getElementById('<%= AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION %>').style.display='block';
	        document.getElementById('<%= AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION %>').style.display='block';
		}        
    }

</script>
	<input type="hidden" name="<%= AccessComponent.ACTION_FIELD %>" value="">

	                <table class="content" id="content"> 
	                
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= AccessComponent.ALL_ACCESS_TYPE_ID %>" 
                            	name="<%= AccessComponent.ALL_ACCESS_TYPE_ID %>" 
                                id="<%= AccessComponent.ALL_ACCESS_TYPE_ID %>" 
                                onclick="DisabledObj(this)" 
                                <% if (accessComponent.isAllAccess()) { %> checked="checked" <% } %>
                                />
                            <label for="<%= AccessComponent.ALL_ACCESS_TYPE_ID %>"><fmt:message key="access.type.all" /></label> 
                        </td>                       
                    </tr>
                     <tr >
                        <td><hr></td>                       
                    </tr>
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= AccessComponent.MANGER_1_ACCESS_TYPE_ID %>" 
                            	name="<%= AccessComponent.MANGER_1_ACCESS_TYPE_ID %>" 
                                id="<%= AccessComponent.MANGER_1_ACCESS_TYPE_ID %>" 
                                <% if (accessComponent.isManager1()) { %> checked="checked" <% } %>
                                <% if (accessComponent.isAllAccess()) { %> disabled="disabled" <% } %>
                                />
                            <label for="<%= AccessComponent.MANGER_1_ACCESS_TYPE_ID %>"><fmt:message key="access.type.manager1" /></label> 
                        </td>                       
                    </tr>
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= AccessComponent.MANAGER_2_ACCESS_TYPE_ID %>" 
                            	name="<%= AccessComponent.MANAGER_2_ACCESS_TYPE_ID %>" 
                                id="<%= AccessComponent.MANAGER_2_ACCESS_TYPE_ID %>" 
                                <% if (accessComponent.isManager2()) { %> checked="checked" <% } %>
                                <% if (accessComponent.isAllAccess()) { %> disabled="disabled" <% } %>
                                />
                            <label for="<%= AccessComponent.MANAGER_2_ACCESS_TYPE_ID %>"><fmt:message key="access.type.manager2" /></label> 
                        </td>                       
                    </tr>
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= AccessComponent.DEPARTMENT_ACCESS_TYPE_ID %>" 
                            	name="<%= AccessComponent.DEPARTMENT_ACCESS_TYPE_ID %>" 
                                id="<%= AccessComponent.DEPARTMENT_ACCESS_TYPE_ID %>" 
                                <% if (accessComponent.isDepartment()) { %> checked="checked" <% } %>
                                <% if (accessComponent.isAllAccess()) { %> disabled="disabled" <% } %>
                                />
<%		
			StringBuffer list = new StringBuffer();
	        for (Iterator iter = accessComponent.getAccessDepartmentList().iterator(); iter.hasNext();) {
				ReferenceValue element = (ReferenceValue) iter.next();
				list.append(element.getValue());
				list.append(" \n");
			}
%>
                            <label for="<%= AccessComponent.DEPARTMENT_ACCESS_TYPE_ID %>"><fmt:message key="access.type.department" /></label> 
			<div><textarea readonly="readonly" ><%= list.toString() %></textarea> </div>	
                <div id="ac_rightIcons"> 
					<input name="<%= AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION %>" id="<%= AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION %>"  type="button"
							<% if (accessComponent.isAllAccess()) { %> style="display: none;" <% } %> 
							value="<fmt:message key="access.form.edit.btn"/>"  
							onclick="javascript: submitFormAccessEdit('<%= AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION %>');">
				</div>
                            
                        </td>                       

                    </tr>
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= AccessComponent.INDIVIDUAL_ACCESS_TYPE_ID %>" 
                            	name="<%= AccessComponent.INDIVIDUAL_ACCESS_TYPE_ID %>" 
                                id="<%= AccessComponent.INDIVIDUAL_ACCESS_TYPE_ID %>" 
                                <% if (accessComponent.isIndividual()) { %> checked="checked" <% } %>
                                <% if (accessComponent.isAllAccess()) { %> disabled="disabled" <% } %>
                                />
                            <label for="<%= AccessComponent.INDIVIDUAL_ACCESS_TYPE_ID %>"><fmt:message key="access.type.individual" /></label> 
<%		
			list = new StringBuffer();
				
	        for (Iterator iter = accessComponent.getAccessIndividualList().iterator(); iter.hasNext();) {
				Person element = (Person) iter.next();
				list.append(element.getFullName());
				list.append(" \n");
			}
%>

                            
			<div><textarea readonly="readonly" ><%= list.toString() %></textarea> </div>	
                <div id="ac_rightIcons"> 
					<input name="<%= AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION %>" id="<%= AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION %>"  type="button"
							<% if (accessComponent.isAllAccess()) { %> style="display: none;" <% } %> 
							value="<fmt:message key="access.form.edit.btn"/>"  
							onclick="javascript: submitFormAccessEdit('<%= AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION %>');">
				</div>
                            
                        </td>                       
                    </tr>
                </table>
			
