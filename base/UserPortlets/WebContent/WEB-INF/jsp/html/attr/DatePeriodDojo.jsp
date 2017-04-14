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
<%@page import="com.aplana.dbmi.model.DatePeriodAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.DatePeriodDojoAttributeEditor"%>
<%@taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
DatePeriodAttribute attr = (DatePeriodAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
// not sure if we need the following line
String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);

String dateFrom = null;
String dateTo = null;

if (attr.getValueFrom() != null) {
	dateFrom = DatePeriodDojoAttributeEditor.getStringDate(attr.getValueFrom());
}
if (attr.getValueTo() != null) {
	dateTo = DatePeriodDojoAttributeEditor.getStringDate(attr.getValueTo());
}
%>
<c:set var="attrHtmlId" value="<%=DatePeriodDojoAttributeEditor.getAttrHtmlId(attr)%>"/>

<span style="float:left; margin-right:5px; max-width:5%;"><fmt:message key="calendarFrom" /></span>
<div style="float:left; margin-right:10px;" id="${attrHtmlId}_fromDateControl"></div>
<span style="float:left; margin-right:5px; max-width:5%;"><fmt:message key="calendarTo" /></span>
<div style="float:left; margin-right:10px;"  id="${attrHtmlId}_toDateControl"></div>


<script type="text/javascript">
    dojo.addOnLoad(function() {
    	dojo.require('dbmiCustom.DateTimeWidget');
    	dojo.require('dojo.date.stamp');
    	widget = new dbmiCustom.DateTimeWidget( 
    		{
    			nameDate: '${attrHtmlId}_fromDate', 
    			valueString: '<%=(attr.getValueFrom() == null) ? "" : dateFrom%>',
			} 
		);
        widget.placeAt(dojo.byId("${attrHtmlId}_fromDateControl"));
        dojo.connect(widget._date, 'onChange', function() {var _value = this.value; if (this.value instanceof Date) {_value = dojo.date.stamp.toISOString(this.value, {selector: 'date'});} editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>_FROM', _value);});
    });
</script>

<script type="text/javascript">
    dojo.addOnLoad(function() {
        dojo.require('dbmiCustom.DateTimeWidget');
        dojo.require('dojo.date.stamp');
        widget = new dbmiCustom.DateTimeWidget( 
            {
                nameDate: '${attrHtmlId}_toDate', 
                valueString: '<%=(attr.getValueTo() == null) ? "" : dateTo%>',
            } 
        );
        widget.placeAt(dojo.byId("${attrHtmlId}_toDateControl"));
        dojo.connect(widget._date, 'onChange', function() {var _value = this.value; if (this.value instanceof Date) {_value = dojo.date.stamp.toISOString(this.value, {selector: 'date'});} editorEventManager.notifyValueChanged('<%=attr.getId().getId()%>_TO', _value);});
    });
</script>

<script type="text/javascript">
dojo.addOnLoad(function() {
	var _value = dijit.byId('${attrHtmlId}_fromDate').value; 
	if (_value instanceof Date) {_value = dojo.date.stamp.toISOString(_value, {selector: 'date'});}
	editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>_FROM', '${attrHtmlId}_fromDate', false, _value);
	_value = dijit.byId('${attrHtmlId}_toDate').value; 
    if (_value instanceof Date) {_value = dojo.date.stamp.toISOString(_value, {selector: 'date'});}
    editorEventManager.registerAttributeEditor('<%=attr.getId().getId()%>_TO', '${attrHtmlId}_toDate', false, _value);
});
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>
