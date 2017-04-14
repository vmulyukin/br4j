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
<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="path" required="true"  rtexprvalue="true"%>
<%@ attribute name="control" required="true" rtexprvalue="true" type="com.aplana.dbmi.model.web.AbstractControl" %>
<%@ attribute name="urlPrefix" required="true"  rtexprvalue="true"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@ attribute name="valuesDate" rtexprvalue="true" type="java.util.Map" %>
                <c:choose>
                    <c:when test="${control.isTextControl}">
                        <form:input path="${path}['${control.name}']"/>
                    </c:when>
                    <c:when test="${control.isTextareaControl}">
                        <form:textarea path="${path}['${control.name}']"/>
                    </c:when>
                    <c:when test="${control.isCheckboxControl}">
                        <form:checkbox path="${path}['${control.name}']"  value="${control.value}"/>
                    </c:when>
                    <c:when test="${control.isComboboxControl}">
                        <form:select path="${path}['${control.name}']" >
                            <form:option value="-1" label=""></form:option>
                            <form:options items="${control.items}" itemLabel="label" itemValue="value"/>
                        </form:select>
                    </c:when>
                    <c:when test="${control.isTreeControl}">
                        <c:set var="${control.name}" value="${control.tree}" scope="request" />
                        <ap:tree  path="${path}"  tree="${control.name}" node="${control.nodeName}"  includeRootNode="${control.showRoot}" paramPrefix="${control.name}_" urlPrefix="${urlPrefix}"/>                    
                    </c:when>
                        <c:when test="${control.isCalendarControl}">
                        <div id="${control.name}_dateControl" style="float: left"></div>
							<script type="text/javascript">
							    dojo.addOnLoad(function() {
							    	dojo.require('dbmiCustom.DateTimeWidget')
							    	widget = new dbmiCustom.DateTimeWidget( 
							    		{
							    			nameDate: '${path}[${control.name}]', 
							    			valueString: '<%if (valuesDate != null) {%><%=valuesDate.get(control.getName())%><%}%>'
										} 
									);
							        widget.placeAt(dojo.byId('${control.name}_dateControl'));
							    });
							</script>
                    </c:when>
                    <c:when test="${control.isIntegerControl}">
                        <form:input path="${path}['${control.name}']" cssClass="integerInput"/>
                    </c:when>
                </c:choose>
