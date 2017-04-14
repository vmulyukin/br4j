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
<%@page import="com.aplana.dbmi.model.SearchYearDatePeriodChoiceAttribute"%>
<%@page import="com.aplana.dbmi.card.SearchYearDatePeriodChoiceEditor"%>
<%@page import="com.aplana.dbmi.model.YearPeriodAttribute"%>
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
	SearchYearDatePeriodChoiceAttribute attr = (SearchYearDatePeriodChoiceAttribute) request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE); 
	String checkedFlagYear = attr.isCheckedYear()?"checked":"";
	String checkedFlagDatePeriod = !attr.isCheckedYear()?"checked":"";
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	String dateFrom = null;
	String dateTo = null;
	String valueChecked=SearchYearDatePeriodChoiceEditor.DATE_PERIOD;
	if(checkedFlagDatePeriod.isEmpty()){
		valueChecked=SearchYearDatePeriodChoiceEditor.YEAR;
	}

	if (attr.getValueFrom() != null) {
		dateFrom = SearchYearDatePeriodChoiceEditor.getStringDate(attr.getValueFrom());
	}
	if (attr.getValueTo() != null) {
		dateTo = SearchYearDatePeriodChoiceEditor.getStringDate(attr.getValueTo());
	}
	
%>

<script>

	function yearListOnBlur(attrId) {
		var widget =  dijit.byId(attrId);
		if (!widget.isValid()) {
			widget.attr('value', '<%=attr.getFromYear()%>');
		}
	}
</script>

<c:set var="attrHtmlId" value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="filterYears" value="<%=attr.getYears()%>" />
<c:set var="checkedYers" value="<%=checkedFlagYear%>" />
<c:set var="checkedDatePeriod" value="<%=checkedFlagDatePeriod%>" />
<c:set var="valueChecked" value="<%=valueChecked%>" />

<div>
	<div style="float:left;">
		<input type="radio" dojoType="dijit.form.RadioButton" name="${attrHtmlId}_choice" id="${attrHtmlId}_choiceYear" 
				value="<%=SearchYearDatePeriodChoiceEditor.YEAR %>"  ${checkedYers}
				onClick="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_choice', this.value);"/>
	</div>
			
	<label for="${attrHtmlId}_choiceYear"><fmt:message key="search.year" /></label>
	<select id="${attrHtmlId}_YearFrom" name="${attrHtmlId}_YearFrom" dojoType="dijit.form.FilteringSelect"  value="<%=attr.getFromYear() %>" autocomplete="false" 
			onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_YearFrom', this.value);"
			onBlur="javascript: yearListOnBlur('${attrHtmlId}_YearFrom');">
			<c:forEach items="${filterYears}" var="filterYear">
				<option value="${filterYear}">${filterYear}</option>
		 	</c:forEach>
	</select>
</div>
</br>

<div style="margin-top:0px;">
<div style="float:left; margin-right:5px;">
<input type="radio" dojoType="dijit.form.RadioButton" name="${attrHtmlId}_choice" id="${attrHtmlId}_choiceDatePeriod" 
		${checkedDatePeriod}
		value="<%=SearchYearDatePeriodChoiceEditor.DATE_PERIOD %>" 
		onClick="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_choice', this.value);"/>
		
<label for="${attrHtmlId}_choiceDatePeriod"><fmt:message key="search.datePeriod" /></label>
</div>

<span style="float:left; margin-right:5px;  max-width:5%;"><fmt:message key="calendarFrom" /></span>
<div style="float:left; margin-right:10px;" id="${attrHtmlId}_fromDateControl"></div>
<span style="float:left; margin-right:5px;  max-width:5%;"><fmt:message key="calendarTo" /></span>
<div style="float:left; margin-right:10px;"  id="${attrHtmlId}_toDateControl"></div>
</div>

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
var ${attrHtmlId}_toolTip=null;
dojo.require("dijit.form.TextBox");
dojo.require("dijit.Tooltip");
dojo.addOnLoad(function() {
	
editorEventManager.registerAttributeEditor('${attrHtmlId}_choice', '${attrHtmlId}_choice', false, '${valueChecked}');
editorEventManager.registerAttributeEditor('${attrHtmlId}_YearFrom', '${attrHtmlId}_YearFrom', false, dojo.byId('${attrHtmlId}_YearFrom').value);
dojo.byId('${attrHtmlId}_YearFrom').value="<%=attr.getFromYear() %>";


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
