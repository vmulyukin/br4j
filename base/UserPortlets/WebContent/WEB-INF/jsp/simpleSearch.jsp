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
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.search.*"%>

<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>

<portlet:defineObjects/>

<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />

<portlet:actionURL var="formAction">
	<portlet:param name="portlet_action" value="<%= SimpleSearchPortlet.SEARCH_PORTLET_ACTION %>"/>
</portlet:actionURL>

<div id="search">
<form:form id="${namespace}SearchForm" action="${formAction}" method="post" commandName="searchBean">

<c:set var="searchDocument"><spring:message code="search.document" text="search.document"/></c:set>
<c:set var="search"><spring:message code="search" text="search"/></c:set>

  <script type="text/javascript">
	dojo.addOnLoad(function(){
  		dojo.require("dijit.form.DateTextBox");
  		dojo.require("dojo.parser");
  		dojo.require("dojo._base.html");
  		dojo.require("dojo.fx");
  		dojo.require("dijit._Widget");
  		 
  		var dt = new dijit.form.DateTextBox(
  			{ 
      			onBlur: function() { validDate(dijit.byId('from_date'), from_oldValueDate, 'from'); },
       			displayedValue: '${searchBean.dateFromStr}',
       			name:  'from_date',
      			style: 'width: 60px'
      		},
      		dojo.byId('from_date')
      	);
		dt = new dijit.form.DateTextBox(
  			{ 
      			onBlur: function() { validDate(dijit.byId('to_date'), to_oldValueDate, 'to'); },
       			displayedValue: '${searchBean.dateToStr}',
       			name:  'to_date',
      			style:'width: 60px'
      		},
      		dojo.byId('to_date')
      	);  		
  		${namespace}datesSwitcher();  		
  	});
  	
  	var from_oldValueDate = null;
  	var to_oldValueDate = null;
 	function validDate(inputDate, oldValueDate, attrId) {
 	   if (!inputDate.isValid(true)) {
			inputDate.attr('value', oldValueDate);
		} else {
			var value = inputDate.attr('value');
		  	if (oldValueDate != null) {
				if (value != null) {
					oldValueDate.setFullYear(value.getFullYear());
					oldValueDate.setMonth(value.getMonth());
					oldValueDate.setDate(value.getDate());
		  		} else {
		  			eval(attrId+'_oldValueDate = null;');
		  		}
	  		} else {
	  			if (value != null) {
	  				eval(attrId+'_oldValueDate = new Date();');
	  				eval(attrId+'_oldValueDate.setDate(value.getDate());');
	  				eval(attrId+'_oldValueDate.setMonth(value.getMonth());');
  					eval(attrId+'_oldValueDate.setFullYear(value.getFullYear());');
	  			}
	  		}
	 	}
  	}

  	function <portlet:namespace/>datesSwitcher() {
  	  if(document.getElementById("templateId").value == "null"){
  	  	dijit.byId("from_date").attr("disabled", true);
  	  	dijit.byId("to_date").attr("disabled", true);
  	  } else {
  	  	dijit.byId("from_date").attr("disabled", false);
  	  	dijit.byId("to_date").attr("disabled", false);
  	  }  
  	}

	function SubmitSearchForm(){
		lockScreen();
		dojo.byId('<portlet:namespace/>SearchForm').submit();
	}

	function downloadAttachment(cardId) {
		timerDiv = dojo.create("div", {id: "downloadTimerDiv"}, dojo.query("div.tools")[0], "last");
		timerDiv.style.zIndex = "100";
		timerDiv.style.position = "relative";
		timerDiv.style.marginTop = "-70px";
		timerDiv.style.top = "0";
		timerDiv.style.left = "0";
		timerDiv.style.backgroundColor = "#f0f0f0";
		
		timerImg = dojo.create("img", {src: "/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif"}, timerDiv);
		timerImg.style.display = "block";
		timerImg.style.margin = "auto";
		timerImg.height = "70";
		timerImg.width = "70";

		dojo.xhrGet({
			url:  "/DBMI-UserPortlets/content?item="+cardId+"&views=8595", 
			preventCache: true, 		
			load: function(resp, ioArgs){						
				if(!isNaN(resp)){	
					url = '/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD=' + resp;	
					iframeId = 'AttachmentFrame';
					iframe = dojo.byId(iframeId) 
					
					if(iframe) {
						dojo.destroy(iframe);
					}
					
					iframe = document.createElement("iframe");
					iframe.setAttribute("name", iframeId);
					iframe.setAttribute("id", iframeId);
					iframe.setAttribute("src", url);
					iframe.onload = onAttachmentLoad();
					iframe.style.position = "absolute";
					iframe.style.top = "1px";
					iframe.style.left = "1px";
					iframe.style.width = "1px";
					iframe.style.height = "1px";
					iframe.style.visibility = "hidden";
					document.body.appendChild(iframe);
			    }
			}
		})
	}

	function onAttachmentLoad() {
		timerDiv = dojo.byId('downloadTimerDiv');
		if(timerDiv) {
			dojo.destroy(timerDiv);
		}	
	}

/*	function toggle(obj) {
		var el = document.getElementById(obj);
		if ( el.style.display != 'none' ) {
			el.style.display = 'none';
		}
		else {
			el.style.display = '';
		}
	}
*/
	/**
     * Handling advanced search action
	 */
	 function handleAdvancedSearch() {
		var url = self.location.href;
	 	location.replace("/portal/auth/portal/boss/advancedSearch/ExtendedSearchWindow?extendedSearchForm=searchFormDescriptionWS&MI_BACK_URL_FIELD=" + url);
	 }

	function enterPress(e) {
	if (e.keyCode == 13) {
		return SubmitSearchForm();
	}
}
  </script>
  <form:input id="simpleSearchFilter" path="searchQuery" cssStyle="width:172px;"
  			  onblur="if(this.value=='') this.value='${searchDocument}';"
  			  onfocus="if(this.value=='${searchDocument}') this.value='';" 
			  onkeydown="return enterPress(event)"/>
  <c:set var="searchAction" value="return ${namespace}SubmitSearchForm()"/>
  <a href="#" class="findBtn" title="${search}" onClick="return SubmitSearchForm()">&nbsp;</a>
  <!-- <a href="#" class="showFullSearch" onClick="toggle('fullsearch');return false;"><spring:message code="search.fullsearch"/></a> -->
  <a href="javascript:handleAdvancedSearch()" class="showFullSearch"><spring:message code="search.fullsearch"/></a>
  <div id="fullsearch" style="display:none;">
  <p><spring:message code="search.template"/></p>
  <c:set var="defaultFolder"><spring:message code="search.folder.all" text="search.folder.all"/></c:set>
  <form:select id="templateId" path="templId" multiple="false" onclick="${namespace}datesSwitcher()" cssStyle="width:183px">
	<form:option value="null" label="${defaultFolder}" />
	<form:options items="${searchBean.templates}" itemLabel="name" itemValue="id.id" />
  </form:select>
  <div class="after">
  	<p><spring:message code="search.date.from"/></p>
  	<input type="text" id="from_date"/>
  	<label for="from_date"><span class="date">&nbsp</span></label>
  </div>
  <div class="before">
  	<p><spring:message code="search.date.to"/></p>
  	<input type="text" id="to_date"/>
  	<label for="to_date"><span class="date">&nbsp</span></label>
  </div>
  <div style="clear:left; height: 16px">
  
  </div>
  </div>
</form:form>

</div>
