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
    Tag for displaying single attribute
--%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ tag import="com.aplana.dbmi.card.JspAttributeEditor" %>

<%@attribute name="attributeView" required="true" type="com.aplana.dbmi.gui.AttributeView" %>
<%@attribute name="cardEditors" required="true" type="java.util.HashSet" %>

<portlet:defineObjects/>

<%
	String attrKey = (String) attributeView.getAttribute().getId().getId();
	JspAttributeEditor editor = (JspAttributeEditor) attributeView.getEditor();

	//generate common code(include jsp)
	if ((editor != null) && (!cardEditors.contains(editor.getInitJspPath()))) {
		try {
			out.flush();
			editor.writeCommonCode(renderRequest, renderResponse);
			cardEditors.add(editor.getInitJspPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
%>
<tr>
	<%	if (editor == null || !editor.doesFullRendering(attributeView.getAttribute())) { %>
		<td>
			<span
				<%if (attributeView.isMandatory() && attributeView.isEmpty()) {%>
					class="obligatory"
				<%}%>
			>
				<c:out value="${attributeView.name}"/>
			</span>
		</td>
    	<td>
	<%} else {%>
		<td colspan="2">
			<span
				<%if (attributeView.isMandatory() && attributeView.isEmpty()) {%>
					class="obligatory"
				<%}%>
			>
				<c:out value="${attributeView.name}"/>
			</span>	
	<%}%>
		<div id="<%= "BODY_"+attrKey %>">
<%				if (editor == null) {						%>
									<%= (attributeView.getStringValue() != null) 
											? attributeView.getStringValue()
												.replaceAll("\\&", "&amp;")
												.replaceAll("<", "&lt;")
												.replaceAll("\\n", "<br>")
											: ""							%>
<%				} else {
		
				out.flush();
				try {
					editor.writeEditorCode(renderRequest, renderResponse, attributeView.getAttribute());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}	
			%>
			
		</div>
    </td>
    <td>
        <%	// if attr textArea or tree or cardLinks - collapse
			if (editor != null && editor.isValueCollapsable()) { %>
				<A HREF="javascript:form_collapse('<%= attrKey %>')" class="noLine">
				<span class="arrow" id="<%= "ARROW_"+attrKey %>">&nbsp;</span>
				</A>
		<%	}  %>
    </td>
</tr>
