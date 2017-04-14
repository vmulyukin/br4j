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
<%@page  contentType="text/html"  pageEncoding="UTF-8" import="com.aplana.dbmi.search.*" %>
<%@page import="com.aplana.dbmi.model.web.*" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="com.aplana.dbmi.showlist.MIShowListPortlet"%>


<%@taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>
<%@taglib uri="/WEB-INF/tld/treetag.tld" prefix="tree" %>
<%@taglib uri="/WEB-INF/tld/requesttags.tld" prefix="request" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<portlet:defineObjects/>

<fmt:setBundle basename="search"/>

<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />

<portlet:actionURL var="formAction">
	<portlet:param name="portlet_action" value="<%= SearchPortlet.SEARCH_PORTLET_ACTION %>"/>
</portlet:actionURL>



<c:if test="${searchBean.message != null}">
<table class="msg">
    <tr  class="tr1">
        <td class=td_11></td>
        <td class=td_12></td>
        <td class=td_13></td>
    </tr>
    
    <tr class="tr2">
        <td class=td_21></td>
        <td class=td_22><c:out value="${searchBean.message}" /> </td>
        <td class=td_23></td>
    </tr>
    <tr class="tr3">
        <td class=td_31></td>
        <td class=td_32></td>
        <td class=td_33></td>
    </tr>
</table>
</c:if>


<script type="text/javascript">
var searchText_toolTip=null;
dojo.require("dijit.form.SimpleTextarea");
dojo.require("dijit.form.TextBox");
dojo.require("dijit.form.CheckBox");
dojo.require("dijit.Tooltip");
dojo.require("dijit.form.FilteringSelect");	
dojo.addOnLoad(function() {
	var elem = document.getElementById('<portlet:namespace/>'+'RegNumber');
	if (elem && elem.checked) {
		<portlet:namespace/>OnRegNumberClick();
	} else {
		elem = document.getElementById('<portlet:namespace/>'+'ById');
		if (elem && elem.checked) {
			<portlet:namespace/>OnByIdClick();
		}
	}
});

    var templates = new Array(
		<c:forEach items="${searchBean.dbTemplates}" var="template" varStatus="status">
        	'template${template.id.id}'<c:if test="${not status.last}">,</c:if>
		</c:forEach>
    );

    function onAllTemplates(allControl){
        var isAllTemplates = document.getElementById('<portlet:namespace/>IsAllTemplates');
        if(isAllTemplates.value =='' 
              || isAllTemplates.value == 'false'){
           isAllTemplates.value ='true';
        }else {
           isAllTemplates.value='false';
        }

         for (var i = 0; i < templates.length; i++){
             var templateControl = document.getElementById(templates[i]);
             if(allControl.checked){
                 templateControl.disabled = true;
                 var defineAttributes = document.getElementById("defineAttributes");
                 defineAttributes.disabled = true;
             }else{
                 templateControl.disabled = false;
                 onTemplate();
             }
         }
    }
    
    function onTemplate(){
        var templateNumber = 0;
        for (var i = 0; i < templates.length; i++){
          var templateControl = document.getElementById(templates[i]);
          if(templateControl.checked){
            ++ templateNumber;
          }
        }
        var defineAttributes = document.getElementById("defineAttributes"); 
        if(templateNumber == 1) {
            defineAttributes.disabled = false;
        }else{
            defineAttributes.disabled = true;        
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
	
	function changeUseWholeBase(checkboxElement) {
		var byIdTooltip = dojo.byId('<portlet:namespace/>ByIdTooltip');
		
		if (checkboxElement.checked)
			byIdTooltip.innerHTML = '<fmt:message key="search.by.card.id.wholebase"/>';
		else
			byIdTooltip.innerHTML = '<fmt:message key="search.by.card.id"/>';
	}

</script>


<form:form id="${namespace}SearchForm" action="${formAction}"  method="post" commandName="searchBean" onsubmit="return ${namespace}OnSearch(event)" >
<form:hidden id="${namespace}IsExtendedSearch" path="isExtendedSearch"/>
<form:hidden id="${namespace}IsAllTemplates" path="isAllTemplates"/>
<form:hidden id="${namespace}IsAttributeSearch" path="isAttributeSearch"/>
<form:hidden id="${namespace}Action" path="action"/>


    <table class="SearchExtend">
        <col Width="50%" />
        <col Width="50%" />
        <tr>
            <td colspan="2">
            <!--div class="divCaption">&nbsp;</div> занимает лишнее место -->
            <c:if test="${searchBean.smallSearchView}">
                <table class="minisearch">
                    <tr>
                        <td class="left">
                        	<fmt:message key="search"/>
                        </td>
                        <td class="center">                                           	
                            <form:input path="searchText"/>                            
                        </td>
                        <td class="btnfind">
                            <div  class="buttonPanel"><ul>
                            	<c:set var="searchTitle"><fmt:message key="find"/></c:set>
                            	<c:set var="searchAction" value="return ${namespace}OnSearch()"/>                            
					            <ap:button text="${searchTitle}"  onClick="${searchAction}" />
					        </ul></div>
                        </td>
                        <td class="right">
                        </td>
                    </tr>
                </table>
            </c:if>
            <c:if test="${searchBean.verticalSearchView}">
            	<table>
            	<tr><td class="searchPortlet-topLeft"/><td class="searchPortlet-top"/><td class="searchPortlet-topRight"/></tr>
            	<tr><td class="searchPortlet-left"/><td class="searchPortlet-content">
            	<table class="minisearch">
                	<tr>
                		<td colspan="3">
                			<fmt:message key="search"/>
                		</td>
                	</tr>
                	<tr>
                        <td class="left">
                        </td>
                        <td class="center">
                            <form:input path="searchText"/>
                        </td>
                        <td class="right">
                        </td>                        
                	</tr>
            	</table>
                <%-- table class="findSelector">
                    <tr>
                        <td class="checkBox">
                           <form:checkbox  id="${namespace}RegNumber" path="regNumber" onclick="${namespace}OnRegNumberClick()" />
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox4">
                                <fmt:message key="searchRegNumber" /></LABEL>
                        </td>
                    </tr>                
                    <tr>
                        <td class="checkBox">
                            <form:checkbox  id="${namespace}Property" path="property" onclick="${namespace}OnPropertyClick()"/>
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox2">
                                <fmt:message key="searchByAttributes" /></LABEL>
                        </td>
                    </tr>
                    <tr>
                        <td class="checkBox">
                           <form:checkbox  id="${namespace}FullText" path="fullText" onclick="${namespace}OnFullTextClick()" />
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox3">
                                <fmt:message key="searchFullText" /></LABEL>
                        </td>
                    </tr>
                    <tr>
                    	<td class="checkBox">
                        	<form:checkbox id="${namespace}Number" path="number" onclick="${namespace}OnNumberClick()" />
                    	</td>
                        <td class="label">
                            <LABEL for="CheckBox1">
                                <fmt:message key="searchByNumber" /></LABEL>
                        </td>
                    </tr>                    
                    <tr><td colspan="2">
                        <div  class="buttonPanel"><ul>
                            	<c:set var="searchTitle"><fmt:message key="find"/></c:set>
                            	<c:set var="searchAction" value="return ${namespace}OnSearch()"/>                            
					            <ap:button text="${searchTitle}"  onClick="${searchAction}" />
                        </ul></div>
                    </td></tr>
                </table--%>
                </td><td class="searchPortlet-right"/></tr>
                <tr><td class="searchPortlet-bottomLeft"/><td class="searchPortlet-bottom"/><td class="searchPortlet-bottomRight"/></tr>
              </table>                
			</c:if>
            <c:if test="${searchBean.fullSearchView}">
                <table class="minisearch" style="width: 100%">
                    <tr>
                        <td class="left">
                        </td>
                        <td class="center" style="vertical-align: middle;">
                            <form:input path="searchText" htmlEscape="true" cssStyle="width: 94%; margin-right:0px;"/> 
                            <form:checkbox id="${namespace}searchStrictWordsChecked" path="searchStrictWords" 
                            				cssStyle="width: 1%; margin-right:0px; vertical-align: middle;"
                            				onclick="wordsChangeStrictText(this)" />
                        	<div dojoType="dijit.Tooltip" connectId="${namespace}searchStrictWordsChecked" position="below" >
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
                        <td class="right">
                        </td>
                    </tr>
                </table>
                <table class="findSelector">
                    <tr style="vertical-align: middle;">
                        <td class="empty">
                        </td>
                        <c:choose>
		                     <c:when test="${searchBean.searchByRegnum > 0}">
		                        <td class="checkBox">
		                           <form:checkbox  id="${namespace}RegNumber" path="registernumber" onclick="${namespace}OnRegNumberClick()" />
		                           <div dojoType="dijit.Tooltip" connectId="${namespace}RegNumber" position="below" >
										<c:choose>
				                        	<c:when test="${searchBean.searchByRegnum == 2}">
			                            		<fmt:message key="search.by.regnum.out"/>
			                            	</c:when>
			                            	<c:otherwise>
			                            		<fmt:message key="search.by.regnum"/>
			                            	</c:otherwise>
			                        	</c:choose>
									</div>
		                        </td>
		                        <td class="label">
		                            <LABEL for="CheckBox4">
		                            <c:choose>
			                        	<c:when test="${searchBean.searchByRegnum == 2}">
		                            		<fmt:message key="search2RegNumber"/>
		                            	</c:when>
		                            	<c:otherwise>
		                            		<fmt:message key="searchRegNumber"/>
		                            	</c:otherwise>
		                        	</c:choose>
		                            </LABEL>
		                        </td>
		                	</c:when>
		               	</c:choose>
                        <td class="checkBox">
                           <form:checkbox  id="${namespace}ById" path="byId" onclick="${namespace}OnByIdClick()" />
                           <div id="${namespace}ByIdTooltip" dojoType="dijit.Tooltip" connectId="${namespace}ById" position="below" >
		                        <fmt:message key="search.by.card.id.wholebase"/>
							</div> 
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox2"><fmt:message key="searchById" /></LABEL>
                        </td>                                                
                        <%--td class="checkBox">
                            <form:checkbox  id="${namespace}Property" path="property" onclick="${namespace}OnPropertyClick()"/>
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox2"><fmt:message key="searchByAttributes" /></LABEL>
                        </td--%>
                        <c:choose>
	                        <c:when test="${searchBean.isExtendedSearch}">                        
		                        <td class="checkBox">
        		                   <form:checkbox  id="${namespace}FullText" path="fullText" onclick="${namespace}OnFullTextClick()" />
                		        </td>
		                        <td class="label">
		                            <LABEL for="CheckBox3"><fmt:message key="searchFullText" /></LABEL>
		                        </td>
	                        </c:when>
                        </c:choose>
                        <c:if test="${searchBean.showProjectNumberSearch}">
                    	<td class="checkBox">
                        	<form:checkbox id="${namespace}Number" path="number" onclick="${namespace}OnNumberClick()" />
                    	</td>
                        <td class="label">
                            <LABEL for="CheckBox1">
                                <fmt:message key="searchByProjectNumber" /></LABEL>
                        </td>
                        </c:if>
                        
                        <c:if test="${searchBean.canUseWholeBase}">  	
                       		<td class="checkBox">
                           		<form:checkbox  id="${namespace}WholeBase" path="wholeBase" onclick="changeUseWholeBase(this)"/>
                       		</td>
                       		<td class="label">
                           		<LABEL for="CheckBox">
                           			<fmt:message key="searchWholeBase"/>
                           		</LABEL>
                       		</td>
                       		<script>
                        		changeUseWholeBase(dojo.byId('<portlet:namespace/>WholeBase'));	
                        	</script>
                       	</c:if>
                        
                        <%-- td class="checkBox">
                            <form:checkbox id="${namespace}Number" path="number" onclick="${namespace}OnNumberClick()" />
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox1"><fmt:message key="searchByNumber" /></LABEL>
                        </td>                                                
                        
                        <td class="checkBox">
                           <form:checkbox  id="${namespace}RegisterNumber" path="registernumber" onclick="${namespace}OnRegisterNumberClick()" />
                        </td>
                        <td class="label">
                            <LABEL for="CheckBox4">
                                <fmt:message key="searchRegisterNumber" /></LABEL>
                        </td --%>
                       <c:choose>
	                        <c:when test="${searchBean.visibleCurrentYear}">
								<td class="checkBox" style="width: 10px;">
		                       	<form:checkbox id="searchCurrentYearChecked" path="searchCurrentYear" 
		                            				cssStyle="width: 1%; margin-right:0px;"/>
		                        	<div dojoType="dijit.Tooltip" connectId="searchCurrentYearChecked" position="below" >
										<fmt:message key="search.show.currentdate"/>
									</div>
		                        </td>
		                        <td class="label" style="width: 240px;">
		                            <LABEL for="searchCurrentYear">
		                                <fmt:message key="search.show.currentYear" />
		                            </LABEL>
		                        </td>
                        	</c:when>
                        	<c:otherwise>
		                    	<td style="width: 150px;">
		                        </td>
                        	</c:otherwise>
                        </c:choose>
						
                        <td class="set">
                        	<c:choose>
                        		<c:when test="${searchBean.hideExtendedLink}">
                        		</c:when>
                        		<c:when test="${ not empty searchBean.extendedSearchForm}">
                        		
                        		<%
                        				PortletURL backURL = renderResponse.createRenderURL();
										backURL.setParameters(renderRequest.getParameterMap());
										String backUrlStr = URLEncoder.encode(backURL.toString(), "UTF-8");
			 					%>		
			 					
			                        <a href="${searchBean.extendedSearchPath}?extendedSearchForm=${searchBean.extendedSearchForm}&<%=SearchFilterPortlet.BACK_URL_FIELD%>=<%=backUrlStr%>&<%=MIShowListPortlet.CLEAR_ATTR%>=true" >
	                                    <fmt:message key="searchExtended" />                              
			                        </a>
                        		</c:when>
                        		<c:otherwise>
			                        <a href="#" onClick="return ${namespace}OnExtendedSearch()">
			                          <c:choose >
			                              <c:when test="${searchBean.isExtendedSearch}">
			                                  <fmt:message key="search" />
			                              </c:when>
			                              <c:otherwise >
			                                  <fmt:message key="searchExtended" />                              
			                              </c:otherwise>
			                          </c:choose>   
			                        </a>
                        		</c:otherwise>
                        	</c:choose>
                        	
                        	<c:if test="${ not empty searchBean.resolutionSearchForm}">
                            <%
                                            PortletURL backURL = renderResponse.createRenderURL();
                                            backURL.setParameters(renderRequest.getParameterMap());
                                            String backUrlStr = URLEncoder.encode(backURL.toString(), "UTF-8");
                            %>
                            </td><td class="empty"></td>
                            <td>
                            <a href="${searchBean.extendedSearchPath}?extendedSearchForm=${searchBean.resolutionSearchForm}&<%=SearchFilterPortlet.BACK_URL_FIELD%>=<%=backUrlStr%>&<%=MIShowListPortlet.CLEAR_ATTR%>=true" >
                                <fmt:message key="searchResolution" />
                            </a>
                            </c:if>
						</td>
						<td>
						</td>
					</tr>
				</table>
			</c:if>			
			<c:choose>
				<c:when test="${searchBean.isExtendedSearch}">
					<jsp:include  page="personalSearch.jsp" />
				</c:when>
			</c:choose>
			</td>
		</tr>
        
<c:choose >
	<c:when test="${searchBean.isAttributeSearch && searchBean.isExtendedSearch}">
		<jsp:include  page="attributeSearch.jsp" />
	</c:when>
	<c:when test="${searchBean.isExtendedSearch }">
		<jsp:include  page="extendedSearch.jsp" />
	</c:when>
	<c:otherwise >

	</c:otherwise>
</c:choose>   
       
        
</table>

</form:form>
<script>
function <portlet:namespace/>OnNumberClick(){
	if(document.getElementById('<portlet:namespace/>Number').checked==true){
		setChecked('Property',  false);
		setChecked('FullText',  false);
		setChecked('ById',      false);
		setChecked('RegNumber', false);
	}
}

function <portlet:namespace/>OnPropertyClick(){
	if(document.getElementById('<portlet:namespace/>Property').checked==true){
		setChecked('Number',    false);
		setChecked('FullText',  false);
		setChecked('ById',      false);
		setChecked('RegNumber', false);
	}
}

function <portlet:namespace/>OnFullTextClick(){
	if(document.getElementById('<portlet:namespace/>FullText').checked==true){
		setChecked('Number',    false);
		setChecked('Property',  false);
		setChecked('ById',      false);
		setChecked('RegNumber', false);
	}
}

function <portlet:namespace/>OnRegNumberClick(){
	if(document.getElementById('<portlet:namespace/>RegNumber').checked==true){
		setChecked('FullText', false);
		setChecked('Number',   false);
		setChecked('Property', false);
		setChecked('ById',     false);
		setChecked('searchStrictWordsChecked', false);
		setEnabled('searchStrictWordsChecked', false);
	} else {
		setEnabled('searchStrictWordsChecked', true);
	}
}

function <portlet:namespace/>OnByIdClick(){
	if(document.getElementById('<portlet:namespace/>ById').checked==true){
		setChecked('FullText',  false);
		setChecked('Number',    false);
		setChecked('Property',  false);
		setChecked('RegNumber', false);
		setChecked('searchStrictWordsChecked', true);
		setEnabled('searchStrictWordsChecked', false);
	} else {
		setChecked('searchStrictWordsChecked', false);
		setEnabled('searchStrictWordsChecked', true);
	}
}

function setChecked(elemId, isChecked) {
	var elem = document.getElementById('<portlet:namespace/>'+elemId);
	if (elem) {
		elem.checked = isChecked;
	}
}

function setEnabled(elemId, isEnable) {
	var elem = document.getElementById('<portlet:namespace/>'+elemId);
	if (elem) {
		elem.disabled = !isEnable;
	}
}

function <portlet:namespace/>OnExtendedSearch(){
	var isExtendedSearch = document.getElementById('<portlet:namespace/>IsExtendedSearch');
	if (isExtendedSearch.value =='' || isExtendedSearch.value =='false'){
		isExtendedSearch.value='true';
		<portlet:namespace/>OnSubmit('<%= WebSearchBean.OPEN_EXTENDED_SEARCH_ACTION %>');
	} else {
		isExtendedSearch.value='false';
		<portlet:namespace/>OnSubmit('<%= WebSearchBean.CLOSE_EXTENDED_SEARCH_ACTION %>');
	}
	return false;
}

function <portlet:namespace/>OnAttributeSearch(curAction){
	var isAttributeSearch = document.getElementById('<portlet:namespace/>IsAttributeSearch');
	if (isAttributeSearch.value =='' || isAttributeSearch.value =='false'){
		isAttributeSearch.value='true';
	} else {
		isAttributeSearch.value='false';
	}
	var action = document.getElementById('<portlet:namespace/>Action');       
	action.value=curAction;
	<portlet:namespace/>SubmitSearchForm();
	return false;
}

function <portlet:namespace/>SubmitSearchForm(){	
	document.getElementById('<portlet:namespace/>SearchForm').submit();
}

function <portlet:namespace/>OnSearch(){
	//сделаем проверку поля
	//чтоб пустое, или только пробелы не срабатывало
	var temp=document.getElementById('searchText');
	var tempval = parseInt(temp.value);
	if (temp.value.replace(/^\s+|\s+$/g, '').length) {
		var doc = document.getElementById('<portlet:namespace/>Number');
		if (doc != null && doc.checked && (isNaN(tempval) || tempval != temp.value)) {
			alert('<fmt:message key="search.format.exception"/>');
			return false;
		}
		doc = document.getElementById('<portlet:namespace/>ById');
		if (doc != null && doc.checked && (isNaN(tempval) || tempval != temp.value)) {
			alert('<fmt:message key="search.format.exception"/>');
			return false;
		}
		var action = document.getElementById('<portlet:namespace/>Action');
		action.value='<%= WebSearchBean.SEARCH_ACTION %>';
		<portlet:namespace/>SubmitSearchForm();
		return true;
	} else {
		return false;      
	}
}

function <portlet:namespace/>OnSubmit(curAction){
	var action = document.getElementById('<portlet:namespace/>Action');
	action.value=curAction;
	<portlet:namespace/>SubmitSearchForm();
}

</script>

<c:if test="${searchBean.isExtendedSearch && !searchBean.isAttributeSearch && searchBean.showTemplates}">                
<script>
	onTemplate();
</script>
</c:if>