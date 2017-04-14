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
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.search.SearchFilterPortlet"%>
<%@page import="com.aplana.dbmi.search.SearchFilterPortletSessionBean"%>

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

<%@page import="com.aplana.dbmi.Portal"%>

<portlet:defineObjects/>


<fmt:setBundle basename="search" scope="request"/>
<c:set var="wordsId" value="<%=SearchFilterPortlet.SEARCH_WORDS%>" />

<script type="text/javascript" language="javascript">
dojo.require("dijit.form.TextBox")
dojo.require("dijit.form.CheckBox")
dojo.require("dijit.Tooltip")
dojo.require("dijit.form.Button")
var ${wordsId}_toolTip=null;

	var editorEventManager = {
		editors: {},
		subscriptions: {},		
		registerAttributeEditor: function(attrCode, attrHtmlId, isInline, value) {
			var editorData = this.editors[attrCode];
			if (!editorData) {
				var editorData = {
					'attrCode': attrCode,
					'attrHtmlId': attrHtmlId,
					'isInline': isInline,
					'value': value
				};
				this.editors[attrCode] = editorData;
				this.notifyValueChanged(attrCode, value);
			}					
		},
		subscribe: function(subscriberAttrCode, valueAttrCode, functionName, functionParameter) {
			var subscription = {
				'subscriberAttrCode': subscriberAttrCode,
				'functionName': functionName,
				'functionParameter': functionParameter
			};

			var subscribers = this.subscriptions[valueAttrCode];
			var isAlreadySubscribed = false;
			if (subscribers) {
				for(var i = 0; i < subscribers.length; ++i) {
					var existing = subscribers[i];
					if (existing.subscriberAttrCode == subscriberAttrCode &&
						existing.functionName == functionName &&
						existing.functionParameter == functionParameter) {
						isAlreadySubscribed = true;
					}
				}
				if (!isAlreadySubscribed) {
					subscribers.push(subscription);
				}
			} else {
				this.subscriptions[valueAttrCode] = [subscription];
			}

			var editorData = this.editors[valueAttrCode];
			var sData = this.editors[subscription.subscriberAttrCode];
			if (!isAlreadySubscribed && editorData && sData) {
				eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, editorData.value, subscription.functionParameter);');
			}
		},
		notifyValueChanged: function(attrCode, value) {
			var editorData = this.editors[attrCode];
			if (!editorData) {
				console.error('Not registered attribute code: ' + attrCode);
				return;
			}
			else {
				editorData.value = value;
			}
			var subscribers = this.subscriptions[attrCode];
			if (subscribers) {
				for(var i = 0; i < subscribers.length; ++i) {
					var subscription = subscribers[i];
					var sData = this.editors[subscription.subscriberAttrCode];
					if (sData) {
						eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, value, subscription.functionParameter);');
					}
				}
			}
		}
	};


	function submitForm(action) { 
		if(document.getElementById("<%=SearchFilterPortlet.SEARCH_FULL_TEXT%>").checked==true){
			document.getElementById("<%=SearchFilterPortlet.SEARCH_FULL_TEXT%>").value="true";
		}
		document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.<%= SearchFilterPortlet.ACTION_FIELD %>.value = action;
		document.<%= SearchFilterPortlet.SEARCH_FORM_NAME %>.submit();
	}
	
	function WordsChangeStrictText(checkboxElement) {
		
		var widget = dojo.byId('${wordsId}');
		
		if(${wordsId}_toolTip==null){
			${wordsId}_toolTip = new dijit.Tooltip({
		        connectId: ["${wordsId}"],
		        label: ""
		    });
		}
		
		if (checkboxElement.checked)				 
			${wordsId}_toolTip.label='<fmt:message key="search.show.flag.strict"/>';
		else
			${wordsId}_toolTip.label ='<fmt:message key="search.show.flag.nostrict"/>';				 
		
		widget.focus();
	}

	
	
</script>

<%
	HashSet searchEditors = new HashSet(40);
	SearchFilterPortletSessionBean sessionBean = (SearchFilterPortletSessionBean)renderRequest.getPortletSession().getAttribute(SearchFilterPortlet.SESSION_BEAN);
    List<BlockSearchView> blockSearchViews = sessionBean.getSearchBlockViews();	
	String checkedFlag = sessionBean.isSearchStrictWords()?"checked":"";
%>
<c:set var="checked" value="<%=checkedFlag%>" />
<c:set var="sessionBean" value="<%=sessionBean%>"/>
<div class="icHeader">
	<fmt:message key="searchExtended" />-<fmt:message key="${sessionBean.header}" />
</div>

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




<form name="<%= SearchFilterPortlet.SEARCH_FORM_NAME %>" method="post" action="<portlet:actionURL/>">
 
  <input type="hidden" name="<%= SearchFilterPortlet.ACTION_FIELD %>" value="">
  
  
  
       	<table class="minisearch" width="100%">
			<col Width="1%"/>
			<col Width="85%"/>
			<col Width="4%"/>
			<col Width="10%"/>
       	
           	<tr>
              <td class="left">
              </td>
              <td class="center">
              
                   <input dojoType="dijit.form.TextBox"  name="${wordsId}" id="${wordsId}" value="<%= sessionBean.getSearchWords()%>"/>
                   
            </td>
            <td>            	
					<input onChange="javascript: editorEventManager.notifyValueChanged('<%=SearchFilterPortlet.SEARCH_STRICT_TEXT%>', this.value);" 
						   onClick="WordsChangeStrictText(this)"
						   dojoType="dijit.form.CheckBox" 
				 					${checked}	id="<%=SearchFilterPortlet.SEARCH_STRICT_TEXT%>" name="<%=SearchFilterPortlet.SEARCH_STRICT_TEXT%>" />
				 					
				 	<div dojoType="dijit.Tooltip" connectId="<%=SearchFilterPortlet.SEARCH_STRICT_TEXT%>" position="below" >
						<fmt:message key="search.show.inactiveflag.strict"/>
					</div>
				
            </td>
			<td class="right">
				<button dojoType="dijit.form.Button" type="button">
					<fmt:message key="search"/>
				    <script type="dojo/method" event="onClick" args="evt">
			submitForm('<%= SearchFilterPortlet.SEARCH_ACTION %>');
				</script>		
				</button>
            <td>            
          </tr>
        </table>   
  <table class="findSelector">
    <tr>
		 <td class="empty">
         </td>
    
        <td class="checkBox">
        
          <input type="checkbox" id="<%=SearchFilterPortlet.SEARCH_FULL_TEXT%>" name="<%=SearchFilterPortlet.SEARCH_FULL_TEXT%>" 
          		<c:if test="${sessionBean.byMaterial}">checked</c:if>
          </input>
          
        </td>
        <td class="label">
          <LABEL for="CheckBox3">
		    <fmt:message key="searchAttachments" /></LABEL>
       </td>
       
	   	<td class="set">
		  	<!-- Simple search -->
			<a href="#" align="right"  onClick="submitForm('<%= SearchFilterPortlet.BACK_ACTION %>')">
				<fmt:message key="simpleSearch"/>		
			</a>
		</td>
		
		 <td class="empty">
         </td>
		
		<td class="set">
			<a href="#" align="right"  onClick="submitForm('<%= SearchFilterPortlet.CLEAR_ACTION %>')">
				<fmt:message key="clear"/>
			</a>
		</td>
       
    </tr>
  </table>
 
  <%@include file="personalSearchFilter.jspf"%>

<%

for (BlockSearchView block : blockSearchViews ) {
	
	  String blockViewID = block.getId().toString();
  
	  if (block.getCurrentState() == BlockViewParam.COLLAPSE){			%>

	  	<c:set var="searchBlockTitle"><fmt:message key="<%= block.getName()%>"/></c:set>
	  	
		<dbmi:blockHeader id="<%= blockViewID %>" title="${searchBlockTitle}" 
				displayed="false" savestate="false"/>
				
		<div class="divPadding" id="<%="BODY_BLOCK_"+blockViewID %>"	style="height: auto; display: none;">
		
<%	 } else {		%>

	  	<c:set var="searchBlockTitle"><fmt:message key="<%= block.getName()%>"/></c:set>

		<dbmi:blockHeader id="<%= blockViewID %>" title="${searchBlockTitle}" 
			displayed="true" savestate="false"/>
		<div class="divPadding" id="<%="BODY_BLOCK_"+blockViewID %>">
		
<%	 }						%>


	  <table class="content" width="100%">
	  
		<col Width="30%"/>
		<col Width="60%"/>
		<col Width="10%"/>
		
<% 		  for (SearchAttributeView av : block.getSearchAttributes() ) {
	
				String attrKey = block.getId().toString() + "_" + (String)av.getAttribute().getId().getId();
				AttributeEditor searchEditor = av.getEditor();
				
				//generate common code(include jsp) 
				if ((searchEditor != null)&&(!searchEditors.contains(searchEditor.getClass()))) {
					try {
						out.flush();
						searchEditor.writeCommonCode(renderRequest, renderResponse);
						searchEditors.add(searchEditors.getClass());
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

%>
		<tr>
<%  			if (searchEditor == null || !searchEditor.doesFullRendering(av.getAttribute())) {  %>
		  <td>
		  	<span>				  	
		  	  <c:out value="<%= av.getName()%>"/>
		  	</span>
		  </td>
		  <td>
<%				} else {		%>
		  <td colspan="2">
		  	<span>				  	
		  		<c:out value="<%= av.getName()%>"/>
		  	</span>		
<%				}				%>
			<div id="<%= "BODY_"+attrKey %>">
			
<%			out.flush();
			try {
				searchEditor.writeEditorCode(renderRequest, renderResponse, av.getAttribute());
			} catch (Exception e) {
				e.printStackTrace();
			}
			%>			
			</div>
		  </td>
		  <td>
<%
		// if attr textArea or tree or cardLinks - collapse 
		if (searchEditor != null && searchEditor.isValueCollapsable()) {			%>
			<A HREF="javascript:form_collapse('<%= attrKey %>')" class="noLine">  
				<span  class="arrow" id="<%= "ARROW_"+attrKey %>">&nbsp;</span>
			</A>
<%		}
		if(searchEditor.isCollapsedByDefault()) {
			%>
			<script>form_collapse('<%= attrKey %>')</script>
			<%
		}

	}					
%>
		  </td>
		</tr>
	  </table>	<%-- end of content table --%>
	</div>
<%	}   %>  

      	<table class="minisearch" width="100%">
			<col Width="100%"/>       	
           	<tr>
			<td class="right">
				<button style="float:right;" dojoType="dijit.form.Button" type="button">
					<fmt:message key="search"/>
				    <script type="dojo/method" event="onClick" args="evt">
			submitForm('<%= SearchFilterPortlet.SEARCH_ACTION %>');
				</script>		
				</button>
            <td>            
          </tr>
        </table>  
</form>  

<jsp:include page="./html/CardPageFunctions.jsp"/>