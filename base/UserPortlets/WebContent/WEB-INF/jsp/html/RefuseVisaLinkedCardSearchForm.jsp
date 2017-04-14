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

<div class="whole_column">
			<input type="text" name="<%= CardLinkData.FIELD_WORDS %>" style="width: 70%;" value="<%= renderRequest.getAttribute(CardLinkData.FIELD_WORDS) %>">
			
			<button dojoType="dijit.form.Button" style="height: 20px; margin-left: 5px; margin-top: -8px" 
														type="button" id="findButton">
			<fmt:message key="button.search"/>											
					<script type="dojo/method" event="onClick" args="evt">	
						submitSearch();
					</script>
 			</button> 



	<p/>
	
	<input id="cardAttachmentLinkedCardSearch"	type="radio" name="cardAttachmentSearchType" value="cardAttachmentLinkedCardSearch"
		<% if (renderRequest.getAttribute(AddVisaLinkedCardAttachmentData.FIELD_BY_LINKED_CARDS) != null) { %>checked="checked"<% } %> ></input> 
		<fmt:message key="flag.linked"/>			
	</input>
			
	<input id="cardAllAttachmentSearch"	type="radio" name="cardAttachmentSearchType" value="cardAllAttachmentSearch" <% if (renderRequest.getAttribute(AddVisaLinkedCardAttachmentData.FIELD_BY_LINKED_CARDS) == null) { %>checked="checked"<% } %>>
		<fmt:message key="flag.allAttachments"/>			
	</input>
</div>



</fmt:bundle>
