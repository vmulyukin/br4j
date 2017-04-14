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

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %> 


<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>

<%@page import="com.aplana.dbmi.model.Template;"%>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>


<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);	

  	String message = sessionBean.getMessage();
	if( message != null) {
  		sessionBean.setMessage(null);
  	} else {
  		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
  	}
  		
%>
	
<script Type ="text/javascript" language=javascript>
    function showElement(objID){
     
        var obj = document.getElementById(objID);
        
        if (obj==null) return;
        
        var arrow = document.getElementById('arrow');
        var arrow_up = document.getElementById('arrow_up');
        
        if(obj.style.display=='none')
        {
            obj.style.display='block';
            arrow.style.display='block';
            arrow_up.style.display='none';
            
        } else {
            obj.style.display='none';
            arrow_up.style.display='block';
            arrow.style.display='none';
		}
    }
     
    function submitSelectTemplateForm() { 
    	document.SelectTemplateForm.submit();  		
	}     
</script>

<% 
    if (message != null) {
%>			
<table class="msg">
    <tr  class="tr1">
        <td class=td_11></td>
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
	
<% } %> 
 
    <table class="indexCardMain">
        <col Width="50%" />        
        <col Width="50%" />        
        <tr>
            <td colspan="4">
                  <table class="icons">
                <tr style="height:25px;">
                    <td style="text-align:left">
<%

			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION); 
			if (sessionBean.getBackURL() == null) {
				backURL.setWindowState(WindowState.MAXIMIZED);
			} else {
				backURL.setWindowState(WindowState.NORMAL);
			}
			
 %> 	        
				<div class="buttonPanel">
					<ul>
						<li class="back"
							onmousedown="downBackBut(this)" 
							onmouseup="upBackButton(this)" 
							onmouseover="overBackButton(this)" 
							onmouseout="upBackButton(this)">
							<a href="<%= backURL.toString() %>">
								<div class="ico_back img">&nbsp;</div>
									<p><fmt:message key="template.page.back.link" /></p>
							</a>
						</li>	
               		</ul>
				</div>	                                 
                    </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr><!--Заголовок-->
            <td colspan="4" class ="EditNewCardHeader">
            	<fmt:message key="template.page.template.msg"/>
            </td>    
        </tr>
        <tr><!--Контент-->
            <td > <!--Левая колонка-->
<form name="SelectTemplateForm" method="post" action="<portlet:actionURL/>">  
	<input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="<%= CardPortlet.CREATE_CARD_ACTION %>">

                <table>
                    <col Width=90% />
                    <col Width=10% />
                    <tr>
                        <td>
                            <table class="partition">
                                <tr>
                                    <td class="partition_left">
                                    </td>
                                    <td class="partition_middle">
                                    	<fmt:message key="template.page.template.label"/>
                                    </td>
                                    <td class="partition_right">
                                    </td>
                                </tr>
                            </table>                
                        </td>
                        <td>
                        <span id="arrow" class="arrow" onclick="showElement('content')">&nbsp;</span>                        
                        <span id="arrow_up" class="arrow_up" onclick="showElement('content')" style="display: none;">&nbsp;</span>                        
                        </td>
                    </tr>
                </table>
                <div class="divPadding">
                                   
                <table class="content" id="content"> 
<%
		List templateList = sessionBean.getTemplateList();
		for (int i = 0; i < templateList.size(); i++) {
			Template template = (Template) templateList.get(i);
			if (template.isActive()) {
%>
                     <tr >
                        <td>
                            <input type="radio" 
                            	value="<%= template.getId().getId() %>" 
                            	name="<%= CardPortlet.TEMPLATE_ID_FIELD %>" 
                                id="<%= CardPortlet.TEMPLATE_ID_FIELD + "_" + i %>" 
                                <% if (i == 0) { %> 
                                checked="checked" 
                                <% } %>
                                />
                            <label for="<%= CardPortlet.TEMPLATE_ID_FIELD + "_" + i %>"><%= template.getName() %></label> 
                        </td>                       
                    </tr>
                    
<% 		
			}	
		}
 %>
                                                                                                                                                                            
                </table>
				</div>                
</form>                
            </td>
                        
        </tr>
        <tr>
        <td colspan=2 style="text-align:left">
			<div class="buttonPanel">
  				<ul>
    			<li onClick="submitSelectTemplateForm()" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
    				<a href="#"><fmt:message key="template.page.ok.bnt"/></a>
    			</li>
			    <li class="empty"><div>&nbsp;</div></li>
			    <!--li onClick="window.location.replace('<%= backURL.toString() %>')" onmousedown="downButton(this)"   onmouseup="upButton(this)" onmouseout="upButton(this)">
			    	<a href="#"><fmt:message key="template.page.close.btn"/></a></li-->
			  	</ul>
			</div>        
        </td>
        </tr>
    </table>
    
