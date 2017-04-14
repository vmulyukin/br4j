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
<%--
    Tag for displaying single search attribute
--%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ tag import="com.aplana.dbmi.card.AttributeEditor" %>

<%@attribute name="attributeView" required="true" type="com.aplana.dbmi.gui.SearchAttributeView" %>
<%@attribute name="cardEditors" required="true" type="java.util.HashSet" %>

<portlet:defineObjects/>

<%
    String attrKey = (String) attributeView.getAttribute().getId().getId();
    AttributeEditor editor = attributeView.getEditor();

    //generate common code(include jsp)
    if ((editor != null) && (!cardEditors.contains(editor.getClass()))) {
        try {
            out.flush();
            editor.writeCommonCode(renderRequest, renderResponse);
            cardEditors.add(cardEditors.getClass());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

%>

    <% if (editor == null || !editor.doesFullRendering(attributeView.getAttribute())) { %>
      <td>
        <span>                  
          <c:out value="<%= attributeView.getName()%>"/>
          </span>
      </td>
      
      <c:choose>
		<c:when test="<%=attributeView.isSpanedView()%>">
		  <td colspan="3">
	    </c:when>
	    <c:otherwise>
	      <td>
	    </c:otherwise>
	  </c:choose>
    <%} else {%>
      <td colspan="2">
        <span>                  
          <c:out value="${attributeView.name}"/>
        </span>
   	<%}%>
        <div id="<%= "BODY_"+attrKey %>">

            <% out.flush();
                try {
                    editor.writeEditorCode(renderRequest, renderResponse, attributeView.getAttribute());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            %>
        </div>
    </td>
