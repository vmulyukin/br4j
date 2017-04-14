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
<fmt:setBundle basename="com.aplana.dbmi.gui.nl.PersonListResource"/>

<script language="JavaScript"><!--
function submitSearch() {
	document.<%= ListEditor.FORM_NAME %>.<%= ListEditor.FIELD_ACTION %>.value = '<%= ListEditor.ACTION_SEARCH %>';
	document.<%= ListEditor.FORM_NAME %>.submit();
}
//--></script>

<table class="minisearch">
	<tr>
		<td width="80%">
			<input type="text" name="<%= PersonList.FIELD_NAME %>" style="width: 100%;" value="<%= renderRequest.getAttribute(PersonList.FIELD_NAME) %>">
		</td>
		<td>
			<%-- input type="button" name="<%= ListEditor.ACTION_SEARCH %>" value='<fmt:message key="button.search"/>' onclick="javascript:submitSearch()" --%>
			<div class="buttonPanel"><ul>
				<li onclick="submitSearch()" onmousedown="downButton(this)" onmouseup="upButton(this)" onmouseout="upButton(this)">
				<a href="#"><fmt:message key="button.search"/></a>
			</ul></div>
		</td>
	</tr>
</table>
<div id="clear">&nbsp;</div>
