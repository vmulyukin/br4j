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
<%@page  contentType="text/html"  pageEncoding="UTF-8" import="com.aplana.dbmi.search.*" %>
<%@page import="com.aplana.dbmi.model.web.*" %>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="java.util.Map" %>
<%@page import="java.util.Iterator" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib uri="/WEB-INF/tld/treetag.tld" prefix="tree" %>
<%@ taglib uri="/WEB-INF/tld/requesttags.tld" prefix="request" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<portlet:defineObjects/>
<fmt:setBundle basename="search"/>
<% if("ru".equals(renderResponse.getLocale().toString()) ||
		"ru_RU".equals(renderResponse.getLocale().toString())){
	    pageContext.setAttribute("ruLocale", Boolean.TRUE);
    }else{
    	pageContext.setAttribute("ruLocale", Boolean.FALSE);
    }
	WebSearchBean bean = (WebSearchBean)renderRequest.getAttribute("searchBean");
	Map valuesDate = bean.getValuesDate();
%>

        <tr>
            <td style="text-align: left">
                <table style="width: 80%">
                    <tr>
                        <td>
                            <table class="partition" style="width: 100%">
                                <tr>
                                    <td class="partition_left">
                                    </td>
                                    <td class="partition_middle">
                                        <fmt:message key="searchTemplates" />                              
                                    </td>
                                    <td class="partition_right">
                                    </td>
                                </tr>
                            </table>
                        </td>
                            <td>
								<ap:link  text="<span class='arrow'>&nbsp;</span>" clazz="noLine">
                                	<portlet:renderURL>
                                    	<portlet:param name="block_id" value="TEMPLATES"/>
										<portlet:param name="portlet_action" value="<%= SearchPortlet.SEARCH_PORTLET_ACTION %>"/>
									</portlet:renderURL>
                                </ap:link>
                            </td>
                        
                    </tr>
                </table>
                <c:if test="${searchBean.showTemplates}"  >      
                <div class="divPadding">
                
                
                <table class="content" >                 
                    <col Width="7%" />
                    <col Width="93%" /> 
                    <tr>
                    <td>
                       <c:choose>
                          <c:when test="${searchBean.isAllTemplates}">
                             <INPUT type="checkbox"  onclick="onAllTemplates(this);"  id="allTemplates" checked="checked"/>
                          </c:when>
                          <c:otherwise>
                             <INPUT type="checkbox"  onclick="onAllTemplates(this);"  id="allTemplates"/>
                          </c:otherwise>
                       </c:choose>
                     </td>
                     <td class="label">
                       <LABEL>
                       <b>
                       <fmt:message key="searchAll" />                              
                       </b>
                       </LABEL>                     
                     </td>
                        
                    </tr>
                    <tr> 
                        <td colspan="2">
                            <hr />
                        </td> 
                    </tr>
                    
        <c:forEach items="${searchBean.viewTemplates}" var="control">
            <c:choose>

                 <c:when test="${ruLocale}">
                     <c:set target="${control}" property="label">
                         <c:out value="${control.labelRu}"></c:out>
                     </c:set>
                  </c:when> 
                  <c:otherwise>
                     <c:set target="${control}" property="label">
                         <c:out value="${control.labelEn}"></c:out>
                     </c:set>
                  </c:otherwise>       
            </c:choose>      
        
            <tr>
                     <td>
                     <form:checkbox path="templates['${control.name}']"  value="${control.value}" id="template${control.name}" onclick="onTemplate()" disabled="${searchBean.isAllTemplates}"/>
                      </td>
                     <td class="label">
                       <LABEL>${control.label}</LABEL>                     
                     </td>
           </tr>
        </c:forEach>    
</table>
            <fmt:message key="searchDefiteAttributes" var="searchDefiteAttributes"/>
                
            <input type="button" onClick="return <portlet:namespace/>OnAttributeSearch('<%= WebSearchBean.ATTRIBUTE_SEARCH_ACTION %>')"  value="${searchDefiteAttributes}" id="defineAttributes"/>
            </div>
            </c:if>
            
        </td>
        <td style="text-align: left">
                <table style="width: 80%;">

                    <tbody>			
                        <tr>
                            <td>
                                <table style="width: 100%;" class="partition">
                                    <tbody>
                                        <tr>
                                            <td class="partition_left">
                                            </td>
                                            <td class="partition_middle">
             <c:choose>                                            
                  <c:when test="${ruLocale}">
                         <c:out value="${searchBean.viewMainBlock.nameRu}"></c:out>
                  </c:when> 
                  <c:otherwise>
                         <c:out value="${searchBean.viewMainBlock.nameEn}"></c:out>
                  </c:otherwise>       
            </c:choose>                             
                                            </td>
                                            <td class="partition_right">
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </td>
                            <td>
								<ap:link  text="<span class='arrow'>&nbsp;</span>" clazz="noLine">
	                                <portlet:renderURL>
    	        	                    <portlet:param name="block_id" value="MAIN_COMMON"/>
										<portlet:param name="portlet_action" value="<%= SearchPortlet.SEARCH_PORTLET_ACTION %>"/>
            	                	</portlet:renderURL>
                                </ap:link>
                            </td>
                            
                        </tr>

                    </tbody>
                </table>    
<c:if test="${searchBean.viewMainBlock.show}"  >                       
                <div class="divPadding">
		           <ap:attributes  valuesDate="<%=valuesDate%>" templateAttributes="${searchBean.viewMainBlock.attributes}"   path="attributes" urlPrefix="<%= renderRequest.getContextPath() %>"/> 
        	   </div>
</c:if>
        </td>
    </tr>

