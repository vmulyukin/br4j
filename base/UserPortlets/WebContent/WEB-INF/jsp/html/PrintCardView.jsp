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
<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %> 

<%@page import="java.util.*"%>
<%@page import="com.aplana.dbmi.card.*"%>

<%@page import="com.aplana.dbmi.model.TemplateBlock"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.CardVersion"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.gui.BlockView"%>
<%@page import="com.aplana.dbmi.gui.AttributeView"%>
<%@page import="com.aplana.dbmi.gui.TabView"%>
<%@page import="com.aplana.dbmi.model.TabViewParam"%>

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>

<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	
// main check
	Card card = sessionBean.getActiveCard();
	String cardId = "";
		
	if (card != null) {
		// parse Id
		if (card.getId() != null) {
			cardId = card.getId().getId().toString();
		} %>


<div class="reportheader">
	<a style="margin: 10px; float: left;" href="#" onclick="window.print();return false"><span class="report printPage_print">&nbsp;</span></a>
	<a style="margin: 10px; float: left;" href="javascript:onClick=window.close();"><span class="report printPage_close">&nbsp;</span></a>
</div>
<div class="reportheaderHR"></div>


    <table class="indexCardMain">
        <col Width="50%" />        
        <col Width="50%" />        
        <tr><!--Заголовок-->
            <td colspan="4">
                <div class="icHeader">
	                <fmt:message key="view.page.title" />
                </div>
            </td>
        </tr>
        
		<tr>
		  <td> <!-- Left column -->
			<div class="divPadding">	<!-- Template name and card name -->
			  <table class="content" >
				<col Width="45%"/>
				<col Width="55%"/>
				<tr>
				  <td><fmt:message key="edit.page.template.label" /></td>
				  <td><%= card.getTemplateName()  %></td>
				</tr>
				<tr>
				  <td><fmt:message key="edit.page.status.label" /></td>
				  <td><%=sessionBean.getActiveCardInfo().getCardState().getName()%></td>
				</tr>
			  </table>
			</div>
			                        <!-- TODO заменить на тег <partitionCaption> -->
            <table class="partitionContainer">
                <col width="90%"/>
                <col width="10%"/>
                <tr>
                  <td>
                    <table class="partition">
                       <tr>
                         <td class="partition_left"></td>
                         <td class="partition_middle">
                            <fmt:message key="view.material.block.name" />
                         </td>
                         <td class="partition_right"></td>
                       </tr>
                    </table>                
                  </td>
                  <td></td>
                </tr>
            </table>
            
            <div class="divPadding">
              <table class="content">
                <col width="90%"/>
                <col width="10%"/>
                <tr id="row1">
                  <td>
<% if (!(card.getMaterialType() == Card.MATERIAL_FILE)) {				%>   
                    <div class="access">&nbsp;</div>
                    <div style="float:left" >
                       <fmt:message key="view.material.block.access.msg" />
                    </div>
                        
<% } else {
	 if (card.getMaterialType() == Card.MATERIAL_FILE) { 			%>                            
                    <div class="file">&nbsp;</div>
                    <div style="float:left" >
                      <%= card.getFileName() %>
                    </div>
<% 		} else if (card.getMaterialType() == Card.MATERIAL_URL) { %>
					<span>
					  <fmt:message key="view.material.block.external.label" />
					</span>
	                <div class="link">&nbsp;</div>
                    <div style="float:left" >
                      <%= card.getUrl() %>
                    </div>
<% 			} else if (card.getMaterialType() == Card.MATERIAL_NONE) { %>
					<span>
					  <fmt:message key="view.material.block.none.label" />
					</span>
<% 			}
	}									%>                            
                  </td>
                  <td></td>
                </tr>
              </table>
            </div>
		  </td>
		  <td>  <!-- Right column -->
			<div class="divPadding">
			  <table class="content" >
				<col Width="45%" />
				<col Width="55%"/>
				<tr>
				  <td>
					<!-- "Код карточки:" -->
					<fmt:message key="edit.page.card.id.label" />
				  </td>
				  <td><%= cardId %>	</td>
				</tr>
				<tr>
					<%-- "Отвественный редактор"	--%>
				  <td> </td>
				  <td> </td>
				</tr>
			  </table>
			</div>
			                        <!-- TODO заменить на тег <partitionCaption> -->
            <table class="partitionContainer">
              <col width="90%"/>
              <col width="10%"/>
              <tr>
                <td>
                  <table class="partition">
                    <tr>
                      <td class="partition_left"></td>
                      <td class="partition_middle">
                       	<fmt:message key="view.project.block.name" />
                      </td>
                      <td class="partition_right"></td>
                    </tr>
                  </table>                
                </td>
                <td></td>
              </tr>
            </table>
			<div class="divPadding">              
              <table class="content" >
                <col Width="90%" />                    
                <col Width="10%" />
                <tr>
                  <td>
                    <div>
<%-- А.П. 21.05.08 --%>
<% if (card.getParent() == null) { %>
					  <fmt:message key="view.project.block.nocard" />
<% } else { %>
                   	  ID <%= card.getParent().getId() %>
                      <br />
                      <%= card.getParentName() %>
<% } 		%>
<%-- А.П. 21.05.08 --%>
                      <br />
                    </div>
                  </td>
                  <td></td>
                </tr>
                <tr>
                  <td>
                    <fmt:message key="view.project.block.refer.link" />
                  </td>
                  <td></td>
                </tr>                    
              </table>
            </div>   
		  </td>
		</tr>

<% 		  List blocks = ((TabView)sessionBean.getActiveCardInfo().getTabsManager().getActiveTab()).
                                                  getContainer().getAllRegions();
		  for (ListIterator i = blocks.listIterator(); i.hasNext(); ){
			  final BlockView block = (BlockView)i.next();  		%>
        <tr><!--Контент-->
		  <td colspan="2">  
            <table class="partition">
              <tr>
				<td class="partition_left"></td>
                <td class="partition_middle">
                  	<%= block.getName() %>
                </td>
                <td class="partition_right"></td>
              </tr>
			</table> 
		  </td>
		</tr> 
		<tr> 
		  <td colspan="2">            
<% 			  for (ListIterator j = block.getAttributeViews().listIterator(); j.hasNext(); ) {
					final AttributeView av = (AttributeView)j.next();
					if (!av.isVisible())
						// пропускаем невидимые...
						continue;							%>
			<div class="divPadding">			
            <table class="content" >
              <col width="30%"/>
              <col width="60%"/>
              <col width="10%"/>
              <tr>
                <td><%= av.getName() %></td>
                <td>
			  	  <%= av.getStringValue() %>
				</td>
                <td></td>
              </tr>
            </table>
            </div>
<%				} 				%>
		  </td>
		</tr> 
<%		  } 				%>
	  </table>   
<%	  } 				%>
