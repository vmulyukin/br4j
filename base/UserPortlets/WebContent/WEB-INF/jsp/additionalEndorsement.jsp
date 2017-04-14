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
<%@page import="com.aplana.dbmi.portlet.AdditionalEndorsementPortletSessionBean"%>
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %> 
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.portlet.AdditionalEndorsementPortlet"%>
<%@page import="com.aplana.dbmi.portlet.ResponsiblePersonsPortlet"%>
<%@page import="com.aplana.dbmi.ajax.QuickResolutionSearchPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.AdditionalEndorsementSearchPersonParameters"%>
<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.AdditionalEndorsementPortlet"/>
<portlet:defineObjects/>


<%
	AdditionalEndorsementPortletSessionBean sessionBean = (AdditionalEndorsementPortletSessionBean)renderRequest.
		getPortletSession().getAttribute(AdditionalEndorsementPortlet.SESSION_BEAN);
%>
	
	<c:set var="sessionBean" value="<%=sessionBean%>"/>
	<c:set var="headerIcon" value="item13"/>
	
	<%@include file="responsiblePersons.jspf"%>

<script type="text/javascript">

	function <portlet:namespace/>_fillTable() {
		var table = dojo.byId('<portlet:namespace/>_endorsersTable');
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= 0; --i) {
				table.deleteRow(i);
			}
		}
		for (var i = 0; i < <portlet:namespace/>_endorsers.length; ++i) {
			var endorser = <portlet:namespace/>_endorsers[i];
			var row = table.insertRow(i);
			
			cell = row.insertCell(0);
			cell.style.textAlign = 'center';
			cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_removeEndorser(' + endorser.cardId + ')">&nbsp;</a>';
			
			cell = row.insertCell(1);
			cell.innerHTML = endorser.name;				
			
			cell = row.insertCell(2);			
			<portlet:namespace/>_addOrderSelect(cell, endorser);
		}
	}
	
	function <portlet:namespace/>_addOrderSelect(cell, endorser) {
		var orderSelect = dojo.create('select', null, cell);
		orderSelect.id = '<portlet:namespace/>_endorserOrder_' + endorser.cardId;
		orderSelect.onchange = function() {<portlet:namespace/>_onOrderSelectChange(orderSelect, endorser.cardId)};
		
		var orderOption = dojo.create('option', null, orderSelect);
		orderOption.value = '';	
		orderOption.innerHTML = '';
		
		for(j = 0; j < 10; j ++) {
			orderOption = dojo.create('option', null, orderSelect);
			orderOption.value = j + 1;	
			orderOption.innerHTML = j + 1;	
		}
		
		if(endorser.order) {
			orderSelect.value = endorser.order;
		} 
	}
	
	function <portlet:namespace/>_onOrderSelectChange(orderSelect, cardId) {
		for (var i = 0; i < <portlet:namespace/>_endorsers.length; ++i) {
			if (<portlet:namespace/>_endorsers[i].cardId == cardId) {
				<portlet:namespace/>_endorsers[i].order = orderSelect.value;
				return;
			}
		} 		
	}
	
	function <portlet:namespace/>_submitForm(action) {
		var form = dojo.byId('<portlet:namespace/>_form');
		
		if('<%=ResponsiblePersonsPortlet.ACTION_DONE%>' == action) { 
			if(!<portlet:namespace/>_validate()) {			
				return false;
			}
			
			for (var i = 0; i < <portlet:namespace/>_endorsers.length; ++i) {
				var endorser = <portlet:namespace/>_endorsers[i];
				dojo.create('input', {type: 'hidden', name: '<%=ResponsiblePersonsPortlet.FIELD_ENDORSERS%>', value: endorser.cardId}, form);
				dojo.create('input', {type: 'hidden', name: '<%=AdditionalEndorsementPortlet.FIELD_ORDER%>' + endorser.cardId, value: endorser.order}, form);
			} 		
		}

		lockScreen();
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
		return false;
	}
	
	function <portlet:namespace/>_validate() {
		if(<portlet:namespace/>_endorsers.length == 0) {
			alert('<fmt:message key="message.noEndorsers"/>');
			return false;
		}
		
		for (var i = 0; i < <portlet:namespace/>_endorsers.length; ++i) {
			if (!<portlet:namespace/>_endorsers[i].order) {
				alert('<fmt:message key="message.noOrder"/> ' + <portlet:namespace/>_endorsers[i].name);
				return false;
			}
		} 		
		return true;
	}
</script>