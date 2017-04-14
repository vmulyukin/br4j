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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="com.aplana.dbmi.gui.*" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<portlet:defineObjects/>
<fmt:bundle basename="com.aplana.dbmi.gui.nl.CardLinkEditResource">

<script language="JavaScript"><!--
function submitSearch() {
	document.<%= ListEditor.FORM_NAME %>.<%= ListEditor.FIELD_ACTION %>.value = '<%= ListEditor.ACTION_SEARCH %>';
	document.<%= ListEditor.FORM_NAME %>.submit();
}

function setByNumber(enabled) {
	if (enabled) {
		document.getElementById('cb2').checked=false;
		document.getElementById('cb3').checked=false;
	} else
		document.getElementById('cb1').checked=false;
}
//--></script>

<table class="minisearch">
	<tr>
		<td width="80%">
			<input type="text" name="<%= CardLinkData.FIELD_WORDS %>" style="width: 100%;" value="<%= renderRequest.getAttribute(CardLinkData.FIELD_WORDS) %>"
				   onkeydown="if (event.keyCode == 13) submitSearch()">
		</td>
		<td>
			<%-- input type="button" name="<%= ListEditor.ACTION_SEARCH %>" value='<fmt:message key="button.search"/>' onclick="javascript:submitSearch()" --%>
			<div class="buttonPanel">
				<ul>
					<li onclick="submitSearch()" onmousedown="downButton(this)" onmouseup="upButton(this)" onmouseout="upButton(this)">
					<a href="#"><fmt:message key="button.search"/></a>
				</ul>
			</div>
		</td>
	</tr>
</table>
<table class="findSelector">
	<tr>
		<td class="checkBox">
			<input type="checkbox" name="<%= CardLinkData.FIELD_BY_NUMBER %>" id="cb1" onclick="setByNumber(true)"
				<% if (renderRequest.getAttribute(CardLinkData.FIELD_BY_NUMBER) != null) { %>checked="checked"<% } %>>
		</td>
		<td class="label">
			<label for="cb1"><fmt:message key="flag.number"/></label>
		</td>
		<td class="checkBox">
			<input type="checkbox" name="<%= CardLinkData.FIELD_BY_ATTR %>" id="cb2" onclick="setByNumber(false)"
				<%if (renderRequest.getAttribute(CardLinkData.FIELD_BY_ATTR) != null) { %>checked="checked"<% } %>>
		</td>
		<td class="label">
			<label for="cb2"><fmt:message key="flag.attribute"/></label>
		</td>
		<td class="checkBox">
			<input type="checkbox" name="<%= CardLinkData.FIELD_BY_TEXT %>" id="cb3" onclick="setByNumber(false)"
				<% if (renderRequest.getAttribute(CardLinkData.FIELD_BY_TEXT) != null) { %>checked="checked"<% } %>>
		</td>
		<td>
			<label for="cb3"><fmt:message key="flag.fullText"/></label>
		</td>
	</tr>
</table>
<div id="clear">&nbsp;</div>

</fmt:bundle>
