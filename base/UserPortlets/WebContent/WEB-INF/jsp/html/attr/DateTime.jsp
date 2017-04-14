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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.model.DateAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.DateTimeAttributeEditor"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<%
DateAttribute attr = (DateAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);

String date = null;
String time = null;
if (attr.getValue() != null) {
	date = DateTimeAttributeEditor.getStringDate(attr);
	time = DateTimeAttributeEditor.getStringTime(attr);
}
String timePattern = attr.getTimePattern() == null ? DateAttribute.defaultTimePattern : attr.getTimePattern();
%>

<c:set var="attrHtmlId" value="<%=DateTimeAttributeEditor.getAttrHtmlId(attr)%>"/>

<div id="${attrHtmlId}_dateControl"></div>
<script type="text/javascript">
    dojo.addOnLoad(function() {
    	dojo.require('dbmiCustom.DateTimeWidget');
    	dojo.require('dojo.date.stamp');
    	widget = new dbmiCustom.DateTimeWidget( 
    		{
    			nameDate: '${attrHtmlId}_date', 
    			nameTime: '${attrHtmlId}_time',
    			valueString: '<%=(attr.getValue() == null) ? "" : (date+time)%>',
				timePattern: '<%=timePattern%>'
<%				if(attr.isShowTime()){%>
				, isShowTime: true
<%				}%>
			} 
		);
        widget.placeAt(dojo.byId("${attrHtmlId}_dateControl"));
        dojo.connect(widget._date, 'onChange', function() {var _value = this.value; if (this.value instanceof Date) {_value = dojo.date.stamp.toISOString(this.value, {selector: 'date'});} editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>', _value);});
    });
</script>

<script type="text/javascript">
dojo.addOnLoad(function() {
	var _value = dijit.byId('${attrHtmlId}_date').value; 
	if (_value instanceof Date) {_value = dojo.date.stamp.toISOString(_value, {selector: 'date'});}
	editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>', '${attrHtmlId}_date', false, _value);
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
