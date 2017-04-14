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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,javax.portlet.*,com.aplana.dbmi.universalportlet.*" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="java.text.MessageFormat"%>
<%@page import="com.aplana.dbmi.model.*"%>
<%@page import="org.displaytag.util.SortingState"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.universalportlet.nl.*"%>
<%@taglib prefix="ap" tagdir="/WEB-INF/tags" %>

<portlet:defineObjects/>

<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />

<fmt:setBundle basename="com.aplana.dbmi.universalportlet.nl.UniversalSearchPortletResource" scope="request"/>

<portlet:actionURL var="formAction"/>

<%
	UniversalSearchPortletSessionBean sessionBean = (UniversalSearchPortletSessionBean) renderRequest.getPortletSession().getAttribute(UniversalSearchPortlet.SESSION_BEAN);
	
%>

<script>
var searchText_toolTip=null;
function <portlet:namespace/>SubmitSearchForm(){	
      document.getElementById('<portlet:namespace/>SearchForm').submit();
}

function <portlet:namespace/>checkForm(e){
	//перехватываем нажатия enter	
	if (e.keyCode == 13) {
		<portlet:namespace/>OnSearch();		
	}	
}
function <portlet:namespace/>OnSearch(){
	//сделаем проверку поля
	//чтоб пустое, или только пробелы не срабатывало
	var temp=document.getElementById('searchText');
	var tempval = parseInt(temp.value);
	if (temp.value.replace(/^\s+|\s+$/g, '').length)
		{
		var action = document.getElementById('<portlet:namespace/>Action');
    	action.value='<%= UniversalSearchPortlet.SEARCH_ACTION %>';
    	<portlet:namespace/>SubmitSearchForm();
    	return true;
   	 	}
	else {
    return false;      
	}
}
function wordsChangeStrictText(checkboxElement) {
	
	var widget = dojo.byId('searchText');
	
	if(searchText_toolTip==null){
		searchText_toolTip = new dijit.Tooltip({
	        connectId: ["searchText"],
	        label: "",
	        position: ["below"]
	    });
	}
	
	if (checkboxElement.checked)				 
		searchText_toolTip.label='<fmt:message key="search.show.flag.strict"/>';
	else
		searchText_toolTip.label ='<fmt:message key="search.show.flag.nostrict"/>';			
	widget.focus();
}
</script>

<form id="${namespace}SearchForm" action="${formAction}" method="post" onkeydown="${namespace}checkForm(event)">
<!-- form:form id="${namespace}SearchForm" action="${formAction}"  method="post" commandName="searchBean" onkeydown="${namespace}chekForm(event)" -->
<!-- form:hidden id="${namespace}Action" path="action"/-->
<input id="${namespace}Action" name="<%= UniversalSearchPortlet.ACTION %>" type="hidden"/>
<table class="SearchExtend">
        <col Width="50%" />
        <col Width="50%" />
        <tr>
            <td colspan="2">
            <table class="minisearch" style="width: 100%">
                    <tr>
                        <td class="left">
                        </td>
                        <td class="center" style="vertical-align: middle;">
                        	<input type="text" id="searchText"
                        			name="<%= UniversalSearchPortlet.PARAM_SEARCH_WORD %>" style="width: 94%; margin-right:0px;"/>
                            <input type="checkbox" id="searchStrictWordsChecked" name="<%= UniversalSearchPortlet.PARAM_IS_STRICT %>"
                            		style="width: 1%; margin-right:0px; vertical-align: middle;"
                            		onclick="wordsChangeStrictText(this)" />
                            <!-- form:input path="searchText" htmlEscape="true" cssStyle="width: 94%; margin-right:0px;"/--> 
                            <!-- form:checkbox id="searchStrictWordsChecked" path="searchStrictWords" 
                            				cssStyle="width: 1%; margin-right:0px; vertical-align: middle;"
                            				onclick="wordsChangeStrictText(this)" /-->
                        	<div dojoType="dijit.Tooltip" connectId="searchStrictWordsChecked" position="below" >
								<fmt:message key="search.show.inactiveflag.strict"/>
							</div>                           
                        </td>
                        <!-- td style="vertical-align: middle; width:1%" >

                        </td-->
                        <td class="btnfind">
                            <div  class="buttonPanel"><ul>
                            	<c:set var="searchTitle"><fmt:message key="search"/></c:set>
                            	<c:set var="searchAction" value="return ${namespace}OnSearch()"/>                            
					            <ap:button text="${searchTitle}"  onClick="${searchAction}" />
                            </ul></div>
                        </td>
                    </tr>
           </table>
            </td>
        </tr>
</table>
</form>
<script>
<% if(sessionBean != null && sessionBean.getFilter() != null) {%>
document.getElementById("searchText").value = '<%= sessionBean.getFilter().getColumnValue() %>';
<% if(sessionBean.getFilter().isStrict()) {%>
document.getElementById("searchStrictWordsChecked").checked = true;
<%}}%>
</script>
<!-- /form:form-->