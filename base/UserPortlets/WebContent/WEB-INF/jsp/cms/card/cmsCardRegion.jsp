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
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="com.aplana.dbmi.gui.*"%>
<%@page import="java.util.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi/wrkst_card" prefix="wrkstCard"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<c:forEach items='${regionBlockViews}' var='block'>
    <c:set var="blockViewID">${block.id}</c:set>
    <c:set var="blockTitle" value="${block.name}"/>

    <c:choose>
          <c:when test="${block.currentState eq '<%=BlockViewParam.COLLAPSE%>'}">
              <c:set var="displayBlock" value="false"/>
              <c:set var="blockStyle" value="height: auto; display: none;"/>
          </c:when>
          <c:otherwise>
              <c:set var="displayBlock" value="true"/>
              <c:set var="blockStyle" value=""/>
          </c:otherwise>
    </c:choose>

    <dbmi:blockHeader id="${blockViewID}" title="${blockTitle}" displayed="${displayBlock}" savestate="false"/>
    <div class="divPadding" id="BODY_BLOCK_${blockViewID}" style="${blockStyle}">

      <table class="content" width="100%">

          <col Width="30%"/>
          <col Width="60%"/>
          <col Width="10%"/>

          <c:forEach items="${block.attributeViewsCollection}" var="av">
			<%
          		AttributeView attrView =  (AttributeView)pageContext.getAttribute("av");
          		AttributeEditor attrEditor = attrView.getEditor();
				if ((attrEditor != null) && Boolean.TRUE.equals(workstationCardInfo.getAttributeEditorData(attrView.getId(), AttributeEditor.KEY_VALUE_CHANGED))) {
					attrEditor.loadAttributeValues(attrView.getAttribute(), renderRequest);
					workstationCardInfo.setAttributeEditorData(attrView.getId(), AttributeEditor.KEY_VALUE_CHANGED, Boolean.FALSE);
				}
          	%>
          	<c:if test="${av.visible}">
    	 		<wrkstCard:attributeView attributeView="${av}" cardEditors="<%=cardEditors%>"/>	
    		</c:if>
          </c:forEach>
      </table>
      <%-- end of content table --%>
    </div>
</c:forEach>
