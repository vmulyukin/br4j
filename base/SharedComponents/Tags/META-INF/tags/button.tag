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

<%@ attribute name="text" rtexprvalue="true"%>
<%@ attribute name="textKey" rtexprvalue="true"%>
<%@ attribute name="tooltip" rtexprvalue="true"%>
<%@ attribute name="tooltipKey" rtexprvalue="true"%>
<%@ attribute name="onClick" rtexprvalue="true"%>
<%@ attribute name="href" rtexprvalue="true"%>
<%@ attribute name="enable" rtexprvalue="true"%>
<%@ attribute name="icon" rtexprvalue="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
 
<c:choose>
	<c:when test="${not empty enable and not enable}">
<li class="disabled">
	<a class="disabled" href="#">
	</c:when>
	<c:otherwise>
<li onmousedown="downButton(this)" 
	onmouseup="upButton(this)" 
    onmouseover="overButton(this)" 
    onmouseout="upButton(this)">
		<c:choose>
			<c:when test="${not empty href and not href}">
	<a href="${href}">
			</c:when>
			<c:otherwise>
	<a href="#" onclick="${onClick}">
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${not empty tooltip and not tooltip}">
		<div	title="${tooltip}"
				<c:if test="${not empty icon}">class="${icon} img"</c:if>>&nbsp;</div>
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${not empty tooltipKey and not tooltipKey}">
				<c:choose>
					<c:when test="${not empty icon}">
		<div class="${icon} img" title="<fmt:message key='${tooltipKey}' />">&nbsp;</div>
					</c:when>
					<c:otherwise>
		<div title="<fmt:message key='${tooltipKey}' />">&nbsp;</div>
					</c:otherwise>
				</c:choose>			
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test="${not empty icon}">
		<div class="${icon} img">&nbsp;</div>
					</c:when>
					<c:otherwise>
		<div>&nbsp;</div>
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${not empty text and not text}">
			<p>${text}</p>
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${not empty textKey and not textKey}">
			<p><fmt:message key="${textKey}" /></p>		
			</c:when>
		</c:choose>
	</c:otherwise>
</c:choose>

</a></li>

