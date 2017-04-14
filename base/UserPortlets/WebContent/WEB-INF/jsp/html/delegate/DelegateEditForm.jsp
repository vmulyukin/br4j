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
<%@page import="com.aplana.dbmi.delegate.*"%>
<%@page import="javax.portlet.*"%>

<%@taglib prefix="portlet" uri="http://java.sun.com/portlet"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="ap" tagdir="/WEB-INF/tags/subtag" %>

<%@taglib prefix="dbmi" uri="http://aplana.com/dbmi/tags" %>

<portlet:defineObjects/>
<c:set var="namespace" value="<%= renderResponse.getNamespace() %>" />
<fmt:setBundle basename="com.aplana.dbmi.delegate.nls.DelegateListPortletResource" scope="request"/>

<portlet:actionURL var="formEditAction">
	<%-- <portlet:param name="portlet_action" value="<%= DelegateListPortlet.ACTION_TAG_ACCEPT_EDIT %>"/>
	  --%>
	 <c:choose>
		<c:when test="${delegateEditBean.createNew eq true}">
			<portlet:param name="<%= DelegateListPortlet.PARAM_ACTION %>" value="<%= DelegateListPortlet.ACTION_TAG_ACCEPT_EDIT_NEW %>"/>
		</c:when>
		<c:otherwise>
			<portlet:param name="<%= DelegateListPortlet.PARAM_ACTION %>" value="<%= DelegateListPortlet.ACTION_TAG_ACCEPT_EDIT %>"/>
		</c:otherwise>
	</c:choose>
</portlet:actionURL>

<div id="span.edit"><div class="top"><div class="bottom">

<form:form 
		id="${namespace}_DelegateEditForm" 
		name="${namespace}_DelegateEditForm" 
		action="${formEditAction}" 
		method="post" 
		commandName="delegateEditBean"
		>

<script type="text/javascript">

	function parseDate(dateStr) {
		if(!dateStr || dateStr == '')
			return null;
		var parts = dateStr.split("-");
		var year = parts[0];
		var month = parts[1];
		var day = parts[2].split(' ')[0];
		if(parts.length != 3)
			return null;
		return new Date(year, month-1, day);
	}

	dojo.addOnLoad(function() {
		dojo.require('dijit.dijit');
		dojo.require('dijit.form.FilteringSelect');
		dojo.require('dbmiCustom.DateTimeWidget')
		widget = new dbmiCustom.DateTimeWidget( 
			{
				nameDate: 'from_date', 
				nameTime: 'from_time',
				valueDate: parseDate('${delegateEditBean.from_date}'),
				<%-- timePattern: 'HH:mm', --%>
				isShowTime: false
			} 
		);
			widget.placeAt(dojo.byId("${namespace}from_dateControl"));
	});

	dojo.addOnLoad(function() {
		dojo.require('dbmiCustom.DateTimeWidget')
		widget = new dbmiCustom.DateTimeWidget( 
			{
				nameDate: 'to_date', 
				nameTime: 'to_time',
				valueDate: parseDate('${delegateEditBean.to_date}'),
				<%-- timePattern: 'HH:mm', --%>
				isShowTime: false
			} 
		);
			widget.placeAt(dojo.byId("${namespace}to_dateControl"));
	});

/*       Не вызывается
	function <portlet:namespace/>datesSwitcher() {
		var role = document.getElementById("roleId");
		if (role == null) return;
		if( role.value == "null"){
			dijit.byId("from_date").attr("disabled", true);
			dijit.byId("to_date").attr("disabled", true);
		} else {
			dijit.byId("from_date").attr("disabled", false);
			dijit.byId("to_date").attr("disabled", false);
		}
	}*/

	function <portlet:namespace/>_SubmitDelegateEditForm(){
		// dojo.byId('${namespace}_DelegateEditForm').submit(); 	<-  (!?) null.submit()
		var form = document.getElementById('${namespace}_DelegateEditForm');
		form.submit();
	}

</script>

	<H2> <fmt:message key="edit.delegate"/> </H2>

	<%
	final PortletURL cancelURL= renderResponse.createActionURL();
	cancelURL.setParameter(DelegateListPortlet.PARAM_ACTION, DelegateListPortlet.ACTION_TAG_CANCEL); 
	%>

	<table width="100%" cols="1">
		<tr> <td>
	<div class="buttonPanel">
		<ul>
			<c:if test="${delegateEditBean.editAccessExists}">
			<c:set var="submitEditAction" value="return ${namespace}_SubmitDelegateEditForm()"/>
			<dbmi:button textKey="btn.save" onClick="${submitEditAction}" />
			</c:if>

			<c:set var="cancelURLStr" value="<%= cancelURL.toString()%>"/>
			<dbmi:button textKey="btn.cancel"
					onClick="window.location.replace('${cancelURLStr}')"/>
		</ul>
	</div>
		</td></tr>

		<tr> <td>

	<% 
		final Object isfixed = request.getAttribute("isSrcUsrFixed"); 
	 %>
	<c:set var="isSrcUsrFixed" value="<%= isfixed %>" />

	<p> <fmt:message key="edit.user.from"/> </p>
	<%-- form:select id="${namespace}user_from" path="user_from" multiple="false"
			disabled="${isSrcUsrFixed}" 
			cssStyle="width:70mm">
		<c:set var="defaultUserFrom"> <key:message key="edit.user.from.null.value" /></c:set>
		<form:option label="${defaultUserFrom}" value="" />
		<form:options items="${delegateEditBean.userList}" itemLabel="fullName" itemValue="id.id" />
	</form:select--%>
	<select id="${namespace}user_from" dojoType="dijit.form.FilteringSelect" autocomplete="true" name="user_from" style="width:70mm"
        <c:if test="${'true' == isSrcUsrFixed}"> disabled="true" </c:if>
        
        <c:choose>
			<c:when test="${delegateEditBean.createNew eq true && delegateEditBean.refreshUserFrom eq false}">
				value="${delegateEditBean.currentUserId}">
			</c:when>
			<c:otherwise>
				value="${delegateEditBean.user_from}">
			</c:otherwise>
		</c:choose>
		
       <c:forEach items="${delegateEditBean.userList}" var="delegateUser">
                <option value=${delegateUser.id.id}>${delegateUser.fullName}</option>
       </c:forEach>
    </select> 

	<p> <fmt:message key="edit.user.to" /> </p>
	<%-- <form:select id="${namespace}user_to" path="user_to" multiple="false"  cssStyle="width:70mm">
		<form:options items="${delegateEditBean.userList}" itemLabel="fullName" itemValue="id.id" />
	</form:select>--%>
	<select id="${namespace}user_to" dojoType="dijit.form.FilteringSelect" autocomplete="true" name="user_to" style="width:70mm"  value="${delegateEditBean.user_to}">
	   <c:forEach items="${delegateEditBean.userList}" var="delegateUser">
		  <c:if test="${delegateEditBean.currentUserId != delegateUser.id.id}"> <option value=${delegateUser.id.id}>${delegateUser.fullName}</option> </c:if>
	   </c:forEach>
    </select> 
    
	
	<%--<div class="after">
		<p><fmt:message key="edit.date.from"/></p>
		<form:input id="${namespace}from_date" path ="from_date" />
		<label for="${namespace}from_date"><span class="date">&nbsp</span></label>
	</div>
	 --%>
	
	<div class="after">
		<fmt:message key="edit.date.from"/>
		<div id="${namespace}from_dateControl"></div>
	</div>

	<%--
	<div class="before">
		<p><fmt:message key="edit.date.to"/></p>
	 	<form:input id="${namespace}to_date" path="to_date" />
		<label for="${namespace}to_date"><span class="date">&nbsp</span></label>
	</div>
	  --%>

	<div class="after">
		<fmt:message key="edit.date.to"/>
		<div id="${namespace}to_dateControl"></div>
	</div>

	<div style="clear:left; height: 16px">
	</div>

		</td></tr>
	</table>
</form:form>
</div></div></div>