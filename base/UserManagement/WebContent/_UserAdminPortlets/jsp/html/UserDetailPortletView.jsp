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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.useradmin.*" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/treetag.tld" prefix="tree" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>

<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="com.jenkov.prizetags.tree.itf.*"%>
<%@page import="com.jenkov.prizetags.tree.impl.*"%>


<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.useradmin.nl.UserAdminPortletResource"/>

<%
	UserAdminPortletSessionBean sessionBean = (UserAdminPortletSessionBean)renderRequest.getPortletSession().getAttribute(UserAdminPortlet.SESSION_BEAN);
	
	List treeIdList = new ArrayList();
	List templateIdList = new ArrayList();
%>

<script Type ="text/javascript" language=javascript>

    function submitForm_Add() { 
		document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.ADD_USER_ROLE_ACTION %>';
    	document.EditUserForm.submit();  		
	}     
    
    function submitForm_Store() { 
		document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.STORE_USER_ACTION %>';
    	document.EditUserForm.submit();  		
	}     
		
    function submitForm_TreeCollapse(nodeId){
		document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.TREE_COLLAPSE_ACTION %>';     
		document.EditUserForm.<%= UserAdminPortlet.ATTR_ID_FIELD %>.value = nodeId; 
    	document.EditUserForm.submit();  		
    }

    function submitForm_TreeExpand(nodeId){
		document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.TREE_EXPAND_ACTION %>';     
		document.EditUserForm.<%= UserAdminPortlet.ATTR_ID_FIELD %>.value = nodeId; 
    	document.EditUserForm.submit();  		
    }
    
	function submitFormRoleEdit(roleId) {
		document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.EDIT_USER_ROLE_ACTION %>';	
		document.EditUserForm.<%= UserAdminPortlet.ATTR_ID_FIELD %>.value = roleId;	
	    document.EditUserForm.submit();
	}
	function submitFormRoleRemove(roleId) {
		if (confirm("<fmt:message key="user.form.remove.confirm.msg"/>")) {
			document.EditUserForm.<%= UserAdminPortlet.ACTION_FIELD %>.value = '<%= UserAdminPortlet.REMOVE_USER_ROLE_ACTION %>';	
			document.EditUserForm.<%= UserAdminPortlet.ATTR_ID_FIELD %>.value = roleId;	
	    	document.EditUserForm.submit();
    	}
	
	}
	function hiddenObj(obj) {
		if (obj.options[obj.selectedIndex].value == '<%=Role.ADMINISTRATOR%>') {      
			document.getElementById('templateBlock').style.display='none';       
			document.getElementById('regionBlock').style.display='none';       
		} else {
			document.getElementById('templateBlock').style.display='';
			document.getElementById('regionBlock').style.display='';
		}
	}

    function SelectObj(obj, array)
    {
        if (array == null || array.length == 0 ) alert('Массив имен контролов пуст');
        if (obj.id == array[0])
        {
            for(var i =1; i<array.length;i++)
                document.getElementById(array[i]).checked = obj.checked;
        }
        else
        {
            if (obj.checked == true)
            {
                var isAll = true;
                for(var i =1; i<array.length;i++)
                if(!document.getElementById(array[i]).checked)
                    isAll = false;
                if (isAll)
                {
                document.getElementById(array[0]).checked = true;
                }
            }
            else
                document.getElementById(array[0]).checked = false;
        }
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

<form name="EditUserForm" method="post" action="<portlet:actionURL/>"> 
	<input type="hidden" name="<%= UserAdminPortlet.ACTION_FIELD %>" value="">
	<input type="hidden" name="<%= UserAdminPortlet.ATTR_ID_FIELD %>" value="">

    <table class="indexCardMain">
        <col Width="50%" />        
        <col Width="50%" />        
        <tr><!--Заголовок-->
            <td colspan="4">
            <div id=container>
                <div id=rightIcons>
<%
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(UserAdminPortlet.ACTION_FIELD, UserAdminPortlet.BACK_ACTION); 
			backURL.setWindowState(WindowState.MAXIMIZED);
 %> 
 
					<div class="buttonPanel">
  						<ul>
    					<li onClick="submitForm_Store()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="edit.form.save.bnt"/></a>
    					</li>
			    		<li class="empty"><div>&nbsp;</div></li>
					    <li onClick="window.location.replace('<%= backURL.toString() %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
					    	<a href="#"><fmt:message key="edit.form.cancel.btn"/></a></li>
			  			</ul>
					</div>        

                </div>
            </div>
            </td>    
        </tr>
        <tr><!--Разделитель-->
            <td colspan="4">
                <hr />
            </td>
        </tr>        
        
<% 
	if (sessionBean.getUser() != null) { 
		Person person = sessionBean.getUser();
%>        
        <tr><!--Контент-->
        <!-- TODO заменить на тег <partitionCaption> -->
            <td >
                <table class="partitionContainer">
                    <tr>
                        <td>
                            <table class="partition">
                                <tr>
                                    <td class="partition_left">
                                    </td>
                                    <td class="partition_middle">
                                       <fmt:message key="edit.user.block.name" />
                                    </td>
                                    <td class="partition_right">
                                    </td>
                                </tr>
                            </table>                
                        </td>
                    </tr>
                </table>
				<div class="divPadding">              
                <table class="content">
                    <col width="35%"/>
                    <col width="65%"/>
                    <tr>
                        <td><fmt:message key="edit.user.block.name.label" /></td>
                        <td><%= person.getFullName() %></td>
                    </tr>
                    <tr>
                        <td><fmt:message key="edit.user.block.email.label" /></td>
                        <td><%= person.getEmail() %></td>
                    </tr>
<%-- Выбор департамента теперь не нужен. Вместо него нужно реализовать выбор карточки персоны                    
                    <tr>
                        <td><fmt:message key="edit.user.block.department.label" /></td>
                        <td>
<% 
%>

				<select name="<%= UserAdminPortlet.DEPARTMENT_FIELD %>" <% if (sessionBean.isEditMode()) { %> disabled="disabled" <% } %> >
					<option value="-1" <% if (person.getDepartment() == null) { %> SELECTED <%} %> >...</option>
<%
			List departmentList = (ArrayList)sessionBean.getReferenceEntitiesEditMode().get(UserAdminPortlet.DEPARTMENT_FIELD);
			for (Iterator it = departmentList.iterator(); it.hasNext();) {
				ReferenceValue referenceValue = (ReferenceValue) it.next();
%>
					<option value="<%= referenceValue.getId().getId() %>" 
					<% if (person.getDepartment() != null && referenceValue.getId().equals(person.getDepartment().getId()) ) { %> SELECTED <%} %> 
					><%= referenceValue.getValue() %></option>	
<%
			}
%>
				</select>

                        </td>
                    </tr>
 --%>                    
                </table>
                </div>
                        <!-- TODO заменить на тег <partitionCaption> -->
                
                <table class="partitionContainer">
                    <tr>
                        <td>
                            <table class="partition">
                                <tr>
                                    <td class="partition_left">
                                    </td>
                                    <td class="partition_middle">
                                       <fmt:message key="edit.role.block.name" />
                                    </td>
                                    <td class="partition_right">
                                    </td>
                                </tr>
                            </table>                
                        </td>
                        <td>
                        </td>
                    </tr>
                </table>
				<div class="divPadding" style="">              
                <table class="content">
                    <tr>
                        <td>
<%
	boolean sortable = true;
	if (sessionBean.isEditMode()) {
		sortable = false;
	}
	
  	request.setAttribute( "roleList", person.getRoles());	
	
 %>                        
                        
  <display:table name="roleList" id="roleItem" uid="roleItem" sort="list" class="res" >

<% 
	Role userRole = (Role) pageContext.getAttribute("roleItem"); 
	String roleId = "";
	StringBuffer templates = new StringBuffer();
	StringBuffer regions = new StringBuffer();
	
	if (userRole != null) {
		roleId = userRole.getId().getId().toString();
	
		if (userRole.getTemplates() != null) {
			for (Iterator iter = userRole.getTemplates().iterator(); iter.hasNext();) {
				Template element = (Template) iter.next();
				if (templates.length() > 0) {
					templates.append(", ");
				}
				templates.append(element.getName());
			}
		}
		if (userRole.getRegions() != null) {
			for (Iterator iter = userRole.getRegions().iterator(); iter.hasNext();) {
				ReferenceValue element = (ReferenceValue) iter.next();
				if (regions.length() > 0) {
					regions.append(", ");
				}
				regions.append(element.getValue());
			}
		}
	}	
%>
	<display:column titleKey="edit.role.block.column.template" sortable="<%= sortable %>" ><% 
		if (userRole != null && userRole.getTemplates() != null && userRole.getTemplates().isEmpty()) { %><fmt:message key="edit.role.parameters.block.type.template.all"/><% } else { %><%= templates.toString() %><% } 
	%></display:column>
	<display:column titleKey="edit.role.block.column.region" sortable="<%= sortable %>" ><% 
		if (userRole != null && userRole.getRegions() != null && userRole.getRegions().isEmpty()) { %><fmt:message key="region.tree.root.name"/><% } else { %><%= regions.toString() %><% } 
	%></display:column>
	<display:column property="name" titleKey="edit.role.block.column.role" sortable="<%= sortable %>" />
	
	<display:column >
<%
	if (sessionBean.isEditMode()) {
%>
 		<img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil_disable.gif") %>' border="0" >&nbsp;&nbsp;
		<img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/delete_disable.gif") %>' border="0" >
<%
	
	} else {
 %>		
 		<a href="javascript: submitFormRoleEdit('<%= roleId %>');" ><img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/pencil.gif") %>' border="0" ></a>
		<a href="javascript: submitFormRoleRemove('<%= roleId %>');" ><img src='<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/delete.gif") %>' border="0" ></a>
<%	} %>		
	</display:column>
  </display:table>

                        </td>
                    </tr>
<% if (!sessionBean.isEditMode()) { %>                    
                    <tr>
                        <td>
                <div id=rightIcons>
                               
					<div class="buttonPanel">
  						<ul>
    					<li onClick="submitForm_Add()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
		    				<a href="#"><fmt:message key="edit.role.block.add.bnt"/></a>
    					</li>
    					</ul>
					</div>        
				</div>
                        </td>
                    </tr>
<% } %>
                </table>
                </div>
                
                
            </td>
<% 
	// Edit user role
	if (sessionBean.isEditMode()) {  
		Role userRole = sessionBean.getRole();
	
	%>            
            <td > 
         <!-- TODO заменить на тег <partitionCaption> -->
                <table class="partitionContainer">
                    <tr>
                        <td>
                            <table class="partition">
                                <tr>
                                    <td class="partition_left">
                                    </td>
                                    <td class="partition_middle">
                                       <fmt:message key="edit.role.parameters.block.name" />
                                    </td>
                                    <td class="partition_right">
                                    </td>
                                </tr>
                            </table>                
                        </td>
                    </tr>
                </table>
				<div class="divPadding">              
                <table class="content">
                    <col Width="25%"/>
                    <col Width="75%"/>
                    <tr>
                        <td><fmt:message key="edit.role.parameters.block.type.label" /></td>
                        <td>
<% 
%>
				<select name="<%= UserAdminPortlet.ROLE_TYPE_FIELD %>" onchange=hiddenObj(this)>
					<c:forEach items="<%=sessionBean.getRoleTypes()%>" var="roleType">
						<option value="${roleType.id.id}"
							<c:if test="<%=((SystemRole)pageContext.getAttribute("roleType")).getId().getId().equals(userRole.getType())%>">
								selected
							</c:if>
						>
							<c:out value="${roleType.name}"/>
						</option>	
					</c:forEach>
				</select>

                        </td>
                    </tr>
                    <tr id="templateBlock"
                <% if (userRole.getType() != null && userRole.getType().equals(Role.ADMINISTRATOR) ) { %>
						style="display: none;" 
				<% } %>
						>
						<td><fmt:message key="edit.role.parameters.block.type.template" /></td>
                        <td>
	                <table class="content" id="content"> 
	                <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= UserAdminPortlet.ROOT_ID %>" 
                            	name="<%= UserAdminPortlet.TEMPLATE_CARD_FIELD + UserAdminPortlet.ROOT_ID %>" 
                                id="<%= UserAdminPortlet.TEMPLATE_CARD_FIELD + UserAdminPortlet.ROOT_ID %>" 
                                onclick="SelectObj(this, templateList)" 

                                <% if (userRole.getTemplates() != null && userRole.getTemplates().isEmpty()) { %> checked="checked" <% } %>

                                />
                            <label for="<%= UserAdminPortlet.TEMPLATE_CARD_FIELD + UserAdminPortlet.ROOT_ID %>"><fmt:message key="edit.role.parameters.block.type.template.all" /></label> 
                        </td>                       
                    </tr>
	                
                     <tr >
                        <td><hr></td>                       
                    </tr>
	                
<%
		List templateList = (ArrayList)sessionBean.getReferenceEntitiesEditMode().get(UserAdminPortlet.TEMPLATE_CARD_FIELD);
		List templates = (ArrayList)userRole.getTemplates();
		
		for (int i = 0; i < templateList.size(); i++) {
			Template template = (Template) templateList.get(i);
			String templateFieldId = UserAdminPortlet.TEMPLATE_CARD_FIELD + template.getId().getId();
			templateIdList.add(templateFieldId);
%>
                     <tr >
                        <td>
                            <input type="checkbox" 
                            	value="<%= templateFieldId %>" 
                            	name="<%= templateFieldId %>" 
                                id="<%= templateFieldId %>" 
                                onclick="SelectObj(this, templateList)" 
                                
                                <% if ((userRole.getTemplates() != null && userRole.getTemplates().isEmpty()) ||(userRole.getTemplates() != null &&  templates.contains(templateFieldId))) { %> checked="checked" <% } %>
                                />
                            <label for="<%= templateFieldId %>"><%= template.getName() %></label> 
                        </td>                       
                    </tr>
<% 			
		}
 %>
                </table>
                        </td>
                    </tr>
                    <tr  id="regionBlock"
                <% if (Role.ADMINISTRATOR.equals(userRole.getType())) { %>
						style="display: none;" 
				<% } %>
						>
                        <td><fmt:message key="edit.role.parameters.block.type.region" /></td>
                        <td>

<% 
		Tree tree = (Tree)sessionBean.getReferenceEntitiesEditMode().get(UserAdminPortlet.REGION_FIELD);  
		
		String regionTree = "REGION_TREE";  
		String regionTreeNode = "REGION_TREE_NODE";
		request.setAttribute(regionTree, tree);
		
		
%>

		<DIV class="divAtrTree">
        	<%-- Generating the Tree HTML --%>
        
        	<table>
        
        		<tree:tree tree="<%= regionTree %>" node="<%= regionTreeNode %>" includeRootNode="true">
            	<tr>
            		<td>
            		    <table>
            			<tr>
            				<td><tree:nodeIndent    node="<%= regionTreeNode %>" indentationType="type"><%--
		    		        	--%><tree:nodeIndentVerticalLine indentationType="type" ><%--
			 	    		    	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/verticalLine.gif") %>"><%--
	    			        	--%></tree:nodeIndentVerticalLine><%--
    				        	--%><tree:nodeIndentBlankSpace   indentationType="type" ><%--
			 	    		    	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/blankSpace.gif") %>"><%--
    			    	    	--%></tree:nodeIndentBlankSpace><%--
		    		        --%></tree:nodeIndent></td>	
    		 		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="false" hasChildren="true"  isLastChild="false">
            				<td><a href="javascript:submitForm_TreeExpand('<tree:nodeId node="<%= regionTreeNode %>"/>');"><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/collapsedMidNode.gif") %>"><%--
		    		     	--%></a></td>
            		</tree:nodeMatch>
            		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="true"  hasChildren="true"  isLastChild="false">
            				<td><a href="javascript:submitForm_TreeCollapse('<tree:nodeId node="<%= regionTreeNode %>"/>');"><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/expandedMidNode.gif") %>"><%--
		    		        --%></a></td>
            		</tree:nodeMatch>
            		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="false" hasChildren="true"  isLastChild="true" >
	            			<td><a href="javascript:submitForm_TreeExpand('<tree:nodeId node="<%= regionTreeNode %>"/>');"><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/collapsedLastNode.gif") %>"><%--
		    		        --%></a></td>
            		</tree:nodeMatch>
            		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="true"  hasChildren="true"  isLastChild="true" >
            				<td><a href="javascript:submitForm_TreeCollapse('<tree:nodeId node="<%= regionTreeNode %>"/>');"><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/expandedLastNode.gif") %>"><%--
		    		        --%></a></td>
            		</tree:nodeMatch>
            		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="false" hasChildren="false" isLastChild="false">
             	    		<td><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/noChildrenMidNode.gif") %>"><%--
		    		        --%></td>     
            		</tree:nodeMatch>
            		<tree:nodeMatch    node="<%= regionTreeNode %>" expanded="false" hasChildren="false" isLastChild="true" >
             	    		<td><%--
		    		        	--%><img src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/images/tree/noChildrenLastNode.gif") %>"><%--
		    		        --%></td>    
            		</tree:nodeMatch>            		
            		
<%		
		ITreeIteratorElement nodeElement = (ITreeIteratorElement) request.getAttribute(regionTreeNode);
		ITreeNode node = nodeElement.getNode();
		String nodeName = ((ReferenceValue)node.getObject()).getValue();
		if (node.getParent() != null) {
			treeIdList.add(node.getId());
		}
%>
            				<td valign="top">
<% 
        if (node.getType().equals(UserAdminPortlet.TREE_VARIABLE_VALUE_TYPE)) {
            if (userRole != null && userRole.getRegions() != null && userRole.getRegions().isEmpty()) { %>
            		<tree:nodeMatch node="<%= regionTreeNode %>" selected="true">
            			<input type="checkbox" 
            			id="<tree:nodeId node="<%= regionTreeNode %>"/>"  
            			name="<tree:nodeId node="<%= regionTreeNode %>"/>" 
            			value="<tree:nodeId node="<%= regionTreeNode %>"/>" 
           				onclick="SelectObj(this, treeList)"
           				/>
            		</tree:nodeMatch>
            <% } else {
%>  				
            		<tree:nodeMatch node="<%= regionTreeNode %>" selected="true">
            				<input type="checkbox" 
            				id="<tree:nodeId node="<%= regionTreeNode %>"/>"  
            				name="<tree:nodeId node="<%= regionTreeNode %>"/>" 
            				value="<tree:nodeId node="<%= regionTreeNode %>"/>" 
            				checked
            				onclick="SelectObj(this, treeList)"
            				/>
            		</tree:nodeMatch>
		            <tree:nodeMatch node="<%= regionTreeNode %>" selected="false">
		            		<input type="checkbox" 
		            		id="<tree:nodeId node="<%= regionTreeNode %>"/>" 
		            		name="<tree:nodeId node="<%= regionTreeNode %>"/>" 
		            		value="<tree:nodeId node="<%= regionTreeNode %>"/>"
		            		onclick="SelectObj(this, treeList)"
		            		/>
		            </tree:nodeMatch>
<% }} %>
        			<tree:nodeMatch node="<%= regionTreeNode %>">
            			<label for="<tree:nodeId node="<%= regionTreeNode %>"/>" ><%= ((ReferenceValue)node.getObject()).getValue() %></label>
            		</tree:nodeMatch>
		            
		            
							</td>
						</tr>
						</table>	
        	 		</td>
        	 	</tr>        	 	
        	</tree:tree>
        </table>
	</DIV>

                        </td>
                    </tr>
                    
                                    
                </table>
                </div>
<% 		} %>            
        </tr>   
<% } %>             
    </table>

<%
	if (sessionBean.isEditMode()) {  
 %>
    
<script type="text/javascript">

//<![CDATA[

var templateList =  new Array('<%= UserAdminPortlet.TEMPLATE_CARD_FIELD + UserAdminPortlet.ROOT_ID %>',
<%
		for (int i = 0; i < templateIdList.size(); i++) {
		
%>
				'<%= templateIdList.get(i).toString() %>'<% if (i != (templateIdList.size() - 1)) { %> , <% } %>
<% 		} 	%>
								);
								
var treeList =  new Array('<%= UserAdminPortlet.REGION_FIELD + UserAdminPortlet.ROOT_ID %>',
<%
		for (int i = 0; i < treeIdList.size(); i++) {
%>
				'<%= treeIdList.get(i).toString() %>'<% if (i != (treeIdList.size() - 1)) { %> , <% } %>
<% 		} 	%>
								);


//]]>

</script>
    
<% } %>    
</form>                    
    