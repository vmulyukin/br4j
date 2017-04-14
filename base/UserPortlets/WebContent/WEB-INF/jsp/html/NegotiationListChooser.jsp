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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.gui.*,com.aplana.dbmi.model.DataObject" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="com.aplana.dbmi.card.negotiation.*"  %> 
<%@page import="com.aplana.dbmi.card.*"  %>


<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.NegotiationListChooser"/>

<% String negotiation = (String) request.getAttribute(NegotiationListChooser.CONTROL_VALUE); %>

<script Type ="text/javascript">
	dojo.require("dijit.Dialog");
	dojo.require("dijit.form.Button");

	<%--function submitForm(action) {
		var form = document.<%=CardPortlet.EDIT_FORM_NAME%>;
		form.<%= NegotiationListChooser.ACTION_FIELD %>.value = action; 
		form.submit();
	}--%>

	function openConfirmForm() {
		var dlg = dijit.byId('confirmClear');
		dlg.show();
	}

	function closeConfirmForm() {
		var dlg = dijit.byId('confirmClear');
		dlg.hide();
	}

	function onOkConfirmForm() {
		var dlg = dijit.byId('confirmClear');
		dlg.hide();
		submitForm('<%=NegotiationListChooser.ACTION_CLEAR%>', this);
	}	
</script>

	<div id="confirmClear" dojoType="dijit.Dialog" title="<fmt:message key="confirm.clear.negotiation.title"/>" style="width: 320px; height: 96px">
		<table>
			<tr>
				<td colspan="2" >
					<div style="text-align: center;"><fmt:message key="confirm.clear.negotiation.text"/></div>
				</td>
			</tr>				
			<tr>
				<td align="right">
					<button id="okButton" dojoType="dijit.form.Button" type="button" onClick="onOkConfirmForm()"><fmt:message key="confirm.clear.negotiation.ok"/></button>
				</td>
				<td align="left">
					<button id="cancalButton" dojoType="dijit.form.Button" type="button" onClick="closeConfirmForm()"><fmt:message key="confirm.clear.negotiation.cancal"/></button>
				</td>
			</tr>
		</table>				
	</div>
	
    <table width="100%" cellpadding="0" cellspacing="0">
		<tr>
			
			<td width="100%">
				<c:if test="<%= negotiation != null%>">
					<%= negotiation %>
				</c:if>  
 			</td>
			<td>
				<c:choose>
					<c:when test="<%= negotiation == null%>">
				    	<button id="selectButton" dojoType="dijit.form.Button" type="button" 
				    			onClick="submitForm('<%=NegotiationListChooser.ACTION_SELECT%>', this)">
				    		<fmt:message key="SELECT"/>
				    	</button>
				  	</c:when>
				  	<c:otherwise>
						<button id="clearButton" dojoType="dijit.form.Button" type="button" 
				    			onClick="openConfirmForm()">
				    		<fmt:message key="CLEAR"/>
				    	</button>
				  	</c:otherwise>
				</c:choose>
 			</td>
 		</tr>
	</table>
