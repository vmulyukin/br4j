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
<%@page import="com.aplana.dbmi.model.SearchStringCheckedAttribute"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<fmt:setBundle basename="search" scope="request"/>

<%
	SearchStringCheckedAttribute attr = (SearchStringCheckedAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	boolean visibleCheckedFlag = attr.isVisibleCheckedFlag();
	String checkedFlag = attr.isCheckedFlag()?"checked":"";
%>
<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="checked" value="<%=checkedFlag%>" />
<div class="textCheckedStyle">
	<input dojoType="dijit.form.TextBox"  style="width:94%; float:left;"
	 name="${attrHtmlId}_inputString"
	 id="${attrHtmlId}_inputString"
	 value="<%= attr.getValue() == null ? "" : attr.getValue().trim().replaceAll("&", "&amp;").replaceAll("\"", "&quot;") %>"
	 onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_inputString', this.value);"
	/>
</div>
<c:if test="<%=visibleCheckedFlag%>">
		<input  onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_checkedFlag', this.value);" 
			   onClick="${attrHtmlId}_ChangeStrictText(this)"
				dojoType="dijit.form.CheckBox" 
	 					${checked}	id="${attrHtmlId}_checkedFlag" name="${attrHtmlId}_checkedFlag" 
	 					title="<fmt:message key="search.show.inactiveflag.strict"/> "/>
	 	<!-- div dojoType="dijit.Tooltip" connectId="${attrHtmlId}_checkedFlag" position="below" >
			<fmt:message key="search.show.inactiveflag.strict"/>
		</div -->
		
</c:if>

<script type="text/javascript">
var ${attrHtmlId}_toolTip=null;
dojo.require("dijit.form.TextBox");
dojo.require("dijit.Tooltip");
dojo.addOnLoad(function() {
editorEventManager.registerAttributeEditor('${attrHtmlId}_inputString', '${attrHtmlId}_inputString', false, dojo.byId('${attrHtmlId}_inputString').value);
editorEventManager.registerAttributeEditor('${attrHtmlId}_checkedFlag', '${attrHtmlId}_checkedFlag', false, dojo.byId('${attrHtmlId}_checkedFlag').value);

});
function ${attrHtmlId}_ChangeStrictText(checkboxElement) {
	
	var widget = dojo.byId('${attrHtmlId}' + '_inputString');
	
	if(${attrHtmlId}_toolTip==null){
		${attrHtmlId}_toolTip = new dijit.Tooltip({
	        connectId: ["${attrHtmlId}_inputString"],
	        label: ""
	    });
	}
	
	if (checkboxElement.checked)				 
		${attrHtmlId}_toolTip.label='<fmt:message key="search.show.flag.strict"/>';
	else
		${attrHtmlId}_toolTip.label ='<fmt:message key="search.show.flag.nostrict"/>';				 
	
	widget.focus();
}	
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
