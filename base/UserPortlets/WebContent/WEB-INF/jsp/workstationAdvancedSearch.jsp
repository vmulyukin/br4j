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

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi/wrkst_card" prefix="wrkstCard"%>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.search.SearchFilterPortlet"%>
<%@page import="com.aplana.dbmi.search.SearchFilterPortletSessionBean"%>
<%@page import="com.aplana.dbmi.search.workstation.WorkstationAdvancedSearchPortlet"%>

<%@page import="com.aplana.dbmi.gui.*"%>
<%@page import="com.aplana.dbmi.model.TemplateBlock"%>
<%@page import="com.aplana.dbmi.gui.BlockSearchView"%>
<%@page import="com.aplana.dbmi.model.BlockViewParam"%>
<%@page import="com.aplana.dbmi.gui.SearchAttributeView"%>



<%@page import="com.aplana.dbmi.model.TabViewParam"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.ListAttribute"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.PortletService"%>

<%@page import="com.aplana.dbmi.Portal"%>

<!-- <link rel="stylesheet" type="text/css" href="/DBMI-Portal/theme/dbmi_style.css" media="all" /> -->
<style type="text/css">
.rich-calendar-input {
	width: 100px;
}
.attrString {
	width:85%;
}
</style>

<portlet:defineObjects/>


<fmt:setBundle basename="search" scope="request"/>

<script type="text/javascript" language="javascript" src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/js/editorEvents.js") %>">
</script>

<script type="text/javascript" language="javascript">

	function submitSearchForm(action, attrCode, isAsc) {
		lockScreen(); 
	    document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.<%= SearchFilterPortlet.ACTION_FIELD %>.value = action;
	    document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.<%= WorkstationAdvancedSearchPortlet.SORT_ATTR_CODE %>.value = attrCode ? attrCode : "";
	    document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.<%= WorkstationAdvancedSearchPortlet.SORT_ASC %>.value = (isAsc != undefined) ? isAsc : "";
	    document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.submit();
	}

	function clearSorting() {
        document.cookie = "CURRTHCLASS=; -1; path=/;";
        document.cookie = "CURRSORTORDER=; -1; path=/;";
        document.cookie = "CURRSORTCODE=; -1; path=/;";
	}

	dojo.addOnLoad(function() {
		var resultsSection = dojo.query(".main_table, .no_documents");
		if( resultsSection && resultsSection.length > 0) {
			var jspHeaderDiv = dojo.byId("jsp_adv_search_header");
			if (jspHeaderDiv) {
				hideElement(jspHeaderDiv);
			}
		}
		
		//lookup buttons hiding
		var lookupBtns = dojo.query(".lookupBtnDiv");
		if( lookupBtns && lookupBtns.length > 0) {
			for(var i = 0; i < lookupBtns.length; i++) {
				hideElement(lookupBtns[i]);
			}
		}
	});
	
	function hideElement(element) {
		if(element) {
			element.setAttribute('style', 'display:none');
		}
	}
	
</script>


<%
    HashSet searchEditors = new HashSet(40);
    SearchFilterPortletSessionBean sessionBean = (SearchFilterPortletSessionBean)renderRequest.getPortletSession().getAttribute(SearchFilterPortlet.SESSION_BEAN);
    List<BlockSearchView> blockSearchViews = sessionBean.getSearchBlockViews();
    Integer rowColumn = 0;
%>

	<c:set var="headerIcon" value="on_execution_personal"/>
	<c:set var="sessionBean" value="<%=sessionBean%>"/>
	
	<div id="jsp_adv_search_header">
    	<%@include file="docTitleTopHeader.jspf"%>
    </div>

<form name="<%= SearchFilterPortlet.SEARCH_FORM_NAME %>" method="post" action="<portlet:actionURL/>"  >
 
  <input type="hidden" name="<%= SearchFilterPortlet.ACTION_FIELD %>" value="">
  <input type="hidden" name="<%= WorkstationAdvancedSearchPortlet.SORT_ATTR_CODE %>" value="">
  <input type="hidden" name="<%= WorkstationAdvancedSearchPortlet.SORT_ASC %>" value="">
  
<c:set var="blockSearchViews" value="<%=sessionBean.getSearchBlockViews()%>"/>

<div id="workstation_advanced_search" class="workstation_advanced_search">
	
<c:if test="${sessionBean.message != null}">
	<table class="msg">
	    <tr  class="tr1">
	        <td class=td_11></td>
	        <td class=td_12></td>
	        <td class=td_13></td>
	    </tr>
	    
	    <tr class="tr2">
	        <td class=td_21></td>
	        <td class=td_22><c:out value="${sessionBean .message}" /> </td>
	        <td class=td_23></td>
	    </tr>
	    <tr class="tr3">
	        <td class=td_31></td>
	        <td class=td_32></td>
	        <td class=td_33></td>
	    </tr>
	</table>
</c:if>
<div id="workstation_advanced_search_block">

<c:forEach items='${blockSearchViews}' var='block'>
	<%rowColumn=0;%>
    <c:set var="blockViewID">${block.id}</c:set>
    <c:set var="blockTitle"><fmt:message key="${block.name}"/></c:set>
    <c:set var="blockColumnsNumber">${block.columnsNumber}</c:set>
    
    <c:choose>
          <c:when test="${block.currentState eq '<%=BlockViewParam.COLLAPSE%>'}">
              <c:set var="displayBlock" value="false"/>
              <c:set var="blockStyle" value="height: auto; display: none;"/>
          </c:when>
          <c:otherwise>
              <c:set var="displayBlock" value="true"/>
              <c:set var="blockStyle" value=""/>
          </c:otherwise>
    </c:choose>


   <!--   <dbmi:blockHeader id="${blockViewID}" title="${blockTitle}" displayed="${displayBlock}" savestate="false"/>-->
    
    <c:if test="${blockViewID == 'fromSearchBlockDescriptionWSID'}">
    	 <div class="common_div"> <!-- common div for blocks "From" and "To"  -->
    </c:if>
    
    <div class="${block.divClass}" id="BODY_${blockViewID}">
    
    <c:if test="${fn:length(block.name) > 0}">
    	<h1><label class=""><fmt:message key="${block.name}"/></label></h1>
    </c:if>

	<c:choose>
		<c:when test="${block.columnsNumber == 2}">
			<!-- two columns in block -->
			<table class="content" width="100%">
				<col Width="20%"/>
				<col Width="30%"/>
				<col Width="20%"/>
				<col Width="30%"/>
                 
				<c:forEach items="${block.searchAttributes}" var="av">
					<c:choose>
						<c:when test="<%=rowColumn == 0%>">
							<tr>
						</c:when>
						<c:when test="<%=rowColumn == 1%>">
							<c:if test="${av.spanedView}">
								</tr>
                            <tr>
							</c:if>
						</c:when>
					</c:choose>
    		
					<wrkstCard:searchAttributeView attributeView="${av}" cardEditors="<%=searchEditors%>"/>
			
					<c:choose>
						<c:when test="${av.spanedView}">
							<% rowColumn = 2;%>    
						</c:when>
						<c:otherwise>
							<% rowColumn++;%>
						</c:otherwise>
					</c:choose>
			    
					<c:if test="<%=rowColumn == 2%>">
						</tr>
						<%rowColumn=0;%>
					</c:if>
				</c:forEach>
			</table>
		</c:when>
		<c:otherwise>
			<!-- single column in block -->
			<table class="content" width="100%">
				<col Width="40%"/>
				<col Width="60%"/>
		         
				<c:forEach items="${block.searchAttributes}" var="av">
					<tr>
						<wrkstCard:searchAttributeView attributeView="${av}" cardEditors="<%=searchEditors%>"/>
					</tr>
				</c:forEach>
			</table>
		</c:otherwise>
	</c:choose>
  
    </div>
    
    <c:if test="${blockViewID == 'toSearchBlockDescriptionWSID'}">
    	 </div> <!-- end of common div for blocks "From" and "To"  -->
    </c:if>
    
</c:forEach>

    <div class="controls">
            <div class="resolution_buttons">

                <div class="search_search" >
				<button dojoType="dijit.form.Button" type="button">
				    <script type="dojo/method" event="onClick" args="evt">
						clearSorting();
						submitSearchForm('<%= WorkstationAdvancedSearchPortlet.NEW_SEARCH_ACTION %>');
				</script>		
				</button>
                </div>

                <div class="search_clear" >
				<button dojoType="dijit.form.Button" type="button">
				    <script type="dojo/method" event="onClick" args="evt">
						submitSearchForm('<%= SearchFilterPortlet.CLEAR_ACTION %>');
					</script>		
				</a>
				</button>
                </div>
            </div>

    </div>
    <!-- .controls end -->
    <div class="whitespace"></div>	
</div>

	
</div> 
	
</form> 

<jsp:include page="./html/CardPageFunctions.jsp"/>