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
<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="enable" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="urlPrefix"%>
<%@ attribute name="enableUrl" %>
<%@ attribute name="disableUrl" %>
<%@ attribute name="enableIcon" %>
<%@ attribute name="disableIcon" %>
<%@ attribute name="onClick" required="false" %>
<%@ attribute name="toolTip" required="false" %>
<c:choose>
    <c:when test="${enable}">
<a class="dbmi_linkImage" href="<jsp:doBody />" 
    <c:if test="${not empty onClick}">onclick="${onClick}"</c:if>
    <c:if test="${not empty toolTip}">title="${toolTip}"</c:if>>
        <c:choose>
        	<c:when test="${not empty enableUrl and not enableUrl}">
   	<img src="${urlPrefix}${enableUrl}"  border="0"/>
        	</c:when>
        	<c:otherwise>
   	<span class="${enableIcon}">&nbsp;</span>
        	</c:otherwise>
        </c:choose>
</a>
    </c:when>
    <c:otherwise>
<a class="dbmi_linkImage">
    	<c:choose>
    		<c:when test="${not empty enableUrl and not enableUrl}">
    <img src="${urlPrefix}${disableUrl}"  border="0"/>
    		</c:when>
    		<c:otherwise>
    <span class="${disableIcon}">&nbsp;</span>
    		</c:otherwise>
    	</c:choose>
</a>
    </c:otherwise>
</c:choose>