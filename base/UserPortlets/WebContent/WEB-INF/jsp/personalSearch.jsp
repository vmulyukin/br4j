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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@taglib prefix="btn" uri="http://aplana.com/dbmi/tags"%>
<%@page import="com.aplana.dbmi.search.SearchPortlet"%>
<%@page import="com.aplana.dbmi.model.web.WebSearchBean"%>

<script type="text/javascript" language="javascript">
	dojo.require('dijit.form.FilteringSelect');

	function existPersonalSearchName(personalSearchName) {

		for(var i = 0; i < personalSearches.length; i++) {
			if (personalSearchName == personalSearches[i].name)
				return true; 
		}		
		return false;
	}	

	function deletePersonalSearch() {

		var personalSearchToRemoveCombo = dijit.byId("<%= SearchPortlet.SEARCH_ID %>");

		if (!validateComboValue(personalSearchToRemoveCombo)) {
			alert('<fmt:message key="search.deletePersonalSearchMessage" />');
			return;	
		}
		if(confirm('<fmt:message key="confirm.delete.personal.search" />')){
			document.getElementById("linkDelete").onclick = function() { return false; };
			<portlet:namespace/>OnSubmit('<%= WebSearchBean.DELETE_PERSONAL_SEARCH_ACTION %>');
		}
	}	

	function validateComboValue(comboBox) {

		var item = comboBox.item;
		var displayedValue = comboBox.attr("displayedValue");

		if ((item != null) && (displayedValue != null) && (displayedValue != '')) {
			return true;
		}
		return false;
	}

	function savePersonalSearch() {

		var newPersonalSearchName = document.getElementById("<%= SearchPortlet.SEARCH_NAME %>").value;
		if ((!newPersonalSearchName) || (newPersonalSearchName=="")) {
			alert('<fmt:message key="search.emptySearchName"/>');
			return;
		}

		if (existPersonalSearchName(newPersonalSearchName)) {
			if (confirm("<fmt:message key="search.personalSearch.message" />")) {
				<portlet:namespace/>OnSubmit('<%= WebSearchBean.SAVE_PERSONAL_SEARCH_ACTION %>');
			}
		} else {
			<portlet:namespace/>OnSubmit('<%= WebSearchBean.SAVE_PERSONAL_SEARCH_ACTION %>');
		}
	}

	function loadPersonalSearch() {
		var personalSearchToLoadCombo = dijit.byId("<%= SearchPortlet.SEARCH_ID %>");

		if (!validateComboValue(personalSearchToLoadCombo)) {
			alert('<fmt:message key="search.loadPersonalSearchMessage" />');
			return;	
		}
		<portlet:namespace/>OnSubmit('<%= WebSearchBean.LOAD_PERSONAL_SEARCH_ACTION %>');
	}

</script>

<c:set var="searchBlockTitle">
	<fmt:message key="personal.search.parameters" />
</c:set>

<c:set var="personalSearchParameters" value="personalSearchParameters" />
<c:set var="personalSearches" value="${searchBean.personalSearches}" />

<script type="text/javascript" language="javascript">
	var personalSearches = [
		    <c:forEach items="${personalSearches}" var="personalSearch" varStatus="loop">
		       { name: "${personalSearch.name}" , id: "${personalSearch.id.id}"}${!loop.last ? ',' : ''}
		    </c:forEach>
		];

</script>

<dbmi:blockHeader id="${personalSearchParameters}"
	title="${searchBlockTitle}" displayed="false" savestate="false" />

<div class="divPadding" id="BODY_BLOCK_${personalSearchParameters}"
	style="height: auto; display: none;">

<table class="content" width="100%">

	<col Width="30%" />
	<col Width="10%" />
	<col Width="60%" />
	<tr>
		<td>
			<fmt:message key="search.newSearchName" />	
			<input type="text" name="<%= SearchPortlet.SEARCH_NAME %>" id="<%= SearchPortlet.SEARCH_NAME %>"  value=""/>
			<a style="text-decoration: none;" title="<fmt:message key="searchSave"/>" onclick="savePersonalSearch()">
				<img class="savePersonSearch"/>
			</a>
		</td>

		<td>
			&nbsp;
		</td>

		<td>
			<fmt:message key="searchName" />	
			<select id="<%= SearchPortlet.SEARCH_ID %>" name="<%= SearchPortlet.SEARCH_ID %>" dojoType="dijit.form.FilteringSelect" autocomplete="false">
				<c:forEach items="${personalSearches}" var="personalSearch">
					<option class="<%= SearchPortlet.SEARCH_ID %>_opt" value="${personalSearch.id.id}">${personalSearch.name}</option>
				</c:forEach>
			</select>

			<a style="text-decoration: none;" id="linkLoad" title="<fmt:message key="search.load"/>" onclick="loadPersonalSearch()">
				<img class="loadPersonSearch"/>
			</a>

			<a style="text-decoration: none;" id="linkDelete" title="<fmt:message key="search.delete"/>" onclick="deletePersonalSearch()">
				<img class="deletePersonSearch"/>
			</a>

		</td>
	</tr>
</table>

</div>
<jsp:include page="html/CardPageFunctions.jsp"/>