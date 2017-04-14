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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ attribute name="path" required="true"  %>
<%@ attribute name="templateAttributes" required="true" rtexprvalue="true" type="java.util.ArrayList" %>
<%@ attribute name="urlPrefix" required="true"  %>
<%@ attribute name="valuesDate" rtexprvalue="true" type="java.util.Map" %>

<portlet:defineObjects/>
<fmt:setBundle basename="search"/>
<% if("ru".equals(renderResponse.getLocale().toString()) || 
			"ru_RU".equals(renderResponse.getLocale().toString())){
		jspContext.setAttribute("ruLocale", Boolean.TRUE);
	}else{
		jspContext.setAttribute("ruLocale", Boolean.FALSE);
	}

%>
<table class="content" >                 
                    <col Width="7%" />
                    <col Width="93%" /> 
        <c:forEach items="${templateAttributes}" var="templateAttribute">
            <c:choose>

                 <c:when test="${ruLocale}">
                     <c:set target="${templateAttribute}" property="label">
                         <c:out value="${templateAttribute.labelRu}"></c:out>
                     </c:set>
                  </c:when> 
                  <c:otherwise>
                     <c:set target="${templateAttribute}" property="label">
                         <c:out value="${templateAttribute.labelEn}"></c:out>
                     </c:set>
                  </c:otherwise>       
            </c:choose>      
        
            <tr>
                <c:choose>
                    <c:when test="${templateAttribute.isTextControl}">
                     <td>
                     </td><td>                     
                       <div  class="divAtrList">
                       ${templateAttribute.label}
                       </div>         
                       <div>
                         <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                       </div>  
                      </td>
                    </c:when>
                    <c:when test="${templateAttribute.isTextareaControl}">
                     <td>
                     </td><td>                     
                        <div  class="divAtrList">
                        ${templateAttribute.label}
                        </div>         
                        <div>
                         <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                        </div> 
                      </td>
                    </c:when>
                    <c:when test="${templateAttribute.isCheckboxControl}">
                     <td>
                         <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 

                      </td>
                     <td class="label">
                       <LABEL>${templateAttribute.label}</LABEL>                     
                     </td>
                    </c:when>
                    <c:when test="${templateAttribute.isComboboxControl}">
                     <td >
                     </td><td>
                       <div class="divAtrList">
                           ${templateAttribute.label}
                       </div> 
                       <div>
                         <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                       </div>  
                      </td>
                    </c:when>
                    <c:when test="${templateAttribute.isTreeControl}">
                    <td>
                    </td><td>
                    <div class="divAtrTree">
                         <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                    </div>     
                      </td>
                    </c:when>
                    <c:when test="${templateAttribute.isCalendarControl}">
                      <td>
                     </td><td>                      
                      <div  class="divAtrList">
                       ${templateAttribute.label}
                       </div> 
                       <div>
                          <div style="float: left"><fmt:message key="calendarFrom" /></div>
                           <ap:control  valuesDate="${valuesDate}" path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                              <c:set var="oldName" value="${templateAttribute.name}"/>
                              <c:set target="${templateAttribute}" property="name"   value="${templateAttribute.name}_new" />
                          <div style="float: left"><fmt:message key="calendarTo" /></div>
                           <ap:control  valuesDate="${valuesDate}" path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                              <c:set target="${templateAttribute}" property="name"   value="${oldName}" />
                       </div>   
                      </td>
                    </c:when>
                    <c:when test="${templateAttribute.isIntegerControl}">
                      <td>
                     </td><td>                      
                      <div  class="divAtrList">
                       ${templateAttribute.label}
                       </div> 
                       <div>
                          <fmt:message key="integerFrom" />
                              <c:set var="oldName" value="${templateAttribute.name}"/>
                              <c:set target="${templateAttribute}" property="name"   value="${templateAttribute.name}_from" />
                           <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                              <c:set target="${templateAttribute}" property="name"   value="${oldName}_to" />
                          <fmt:message key="integerTo" />
                           <ap:control  path="${path}" urlPrefix="${urlPrefix}" control="${templateAttribute}" /> 
                              <c:set target="${templateAttribute}" property="name"   value="${oldName}" />
                       </div>   
                      </td>
                    </c:when>
                </c:choose>
      
           </tr>
        </c:forEach>    
</table>
