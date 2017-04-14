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
<%@page session="false" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page
	import="com.aplana.dbmi.actionhandler.descriptor.ActionHandlerDescriptor"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.card.TextWithTemplatesAttributeEditor"%>
<%@page import="com.aplana.dbmi.model.TextAttribute"%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Map.Entry"%>

<%@page import="org.apache.commons.lang.StringEscapeUtils"%>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects />

<script type="text/javascript">
function addValue(idDiv) {
	var textarea = dojo.byId('<portlet:namespace/>_text_area');
	var div = dojo.byId(idDiv);
	var text = div.textContent || div.innerText;
	
	textarea.focus();
	if (document.selection) {
       	sel = document.selection.createRange();
      	sel.text = text;
    } else if (textarea.selectionStart || textarea.selectionStart == '0') {
        var startPos = textarea.selectionStart;
        var endPos = textarea.selectionEnd;
        if (endPos == textarea.value.length && endPos != 0 && startPos != 0) {
        	text = '\n' + text;
        }
        textarea.value = textarea.value.substring(0, startPos) + text + 
        	textarea.value.substring(endPos, textarea.value.length);
        textarea.setSelectionRange(endPos+text.length, endPos+text.length);
    } else if (textarea.value == '') {
    	textarea.value = text;
    } else {
        textarea.value = textarea.value + '\n' + text;
    }		
}
</script>

<%
    CardPortletSessionBean sessionBean = (CardPortletSessionBean) renderRequest
		    .getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
    TextAttribute attr = (TextAttribute) request
		    .getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
    @SuppressWarnings("unchecked")
    Map<String, ActionHandlerDescriptor> actionsMap = (Map<String, ActionHandlerDescriptor>) sessionBean
		    .getActiveCardInfo().getAttributeEditorData(attr.getId(),
			    TextWithTemplatesAttributeEditor.ACTIONS);
    @SuppressWarnings("unchecked")
    Map<String, List<String>> valuesMap = (Map<String, List<String>>) sessionBean
		    .getActiveCardInfo().getAttributeEditorData(attr.getId(),
			    TextWithTemplatesAttributeEditor.VALUES);
%>
<div class="divAttrLink">
	<div style="float: right">
<%
    for (Entry<String, ActionHandlerDescriptor> action : actionsMap
		    .entrySet()) {
%>
		<button dojoType="dijit.form.DropDownButton">
			<span><%=action.getValue().getTitle().getValue()%></span>
			<menu dojoType="dijit.Menu">
<%
		List<String> values = valuesMap.get(action.getKey());
		if (values == null)
		    values = new ArrayList<String>();
		for (int i = 0; i < values.size(); i++) {
%>
				<menuItem id="<portlet:namespace/>_text_res_<%=i%>" dojoType="dijit.MenuItem"
					onClick="addValue('<portlet:namespace/>_text_res_<%=i%>_text')"><%=values.get(i)%></menuItem>
<%
	    }
%>	
			</menu>
		</button>
<%
    }
%>
	</div>
<%
    int rows = attr.getRowsNumber();
    if (rows < 1)
		rows = 5;
%> 
	<textarea id="<portlet:namespace/>_text_area" class="attrText"
		name="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" 
		rows="<%=rows%>"><%=attr.getValue() == null ? "" : StringEscapeUtils
		    .escapeHtml(attr.getValue())%></textarea>
</div>

