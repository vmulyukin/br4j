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
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %> 
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %> 
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.portlet.ChooseDocTemplatePortlet"%>
<%@page import="com.aplana.dbmi.portlet.ChooseDocTemplatePortletSessionBean"%>
<%@page import="com.aplana.web.tag.util.StringUtils"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.ChooseDocTemplatePortlet"/>
<portlet:defineObjects/>

<%	
ChooseDocTemplatePortletSessionBean sessionBean = (ChooseDocTemplatePortletSessionBean) renderRequest.getPortletSession()
		.getAttribute(ChooseDocTemplatePortlet.SESSION_BEAN);
%>
	<c:set var="sessionBean" value="<%=sessionBean%>"/>
	<c:set var="headerIcon" value="item13"/>
	
	<%@include file="docTitleTopHeader.jspf"%>

	<div class="choose_template">

<%
	String message = sessionBean.getMessage();
	if (message != null) {
		sessionBean.setMessage(null);
%>
		<div><%=message%></div>
<%
	}
%>

<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
	<input type="hidden" id="<portlet:namespace/>_action" name="<%=ChooseDocTemplatePortlet.FIELD_ACTION%>" value=""/>
	<input type="hidden" name="<%=ChooseDocTemplatePortlet.FIELD_NAMESPACE%>" value="<portlet:namespace/>"/>
	<input type="hidden" id="<portlet:namespace/>_<%=ChooseDocTemplatePortlet.FIELD_BACK_URL%>" value="<%=StringUtils.hasLength(sessionBean.getBackUrl()) ? URLEncoder.encode(sessionBean.getBackUrl(), "UTF-8") : "" %>"/>
	<input type="hidden" id="<portlet:namespace/>_<%=ChooseDocTemplatePortlet.FIELD_LINK_TO_CARD%>" value="${sessionBean.linkToCard}"/>

	<div class="templates_list">
			<c:forEach var="item" varStatus="status" items="${sessionBean.allowedTemplates}">
				<input type="radio" dojoType="dijit.form.RadioButton" name="<portlet:namespace/>_templateChoice" 
					id="<portlet:namespace/>_templateChoice${status.index}" value="${item.key.id}"
					<c:if test="${status.first}">
						checked
					</c:if>
				/>
				<label for="<portlet:namespace/>_templateChoice${status.index}">${item.value}</label><br />
			</c:forEach>
	</div>

	<div class="controls">
		<div class="resolution_buttons">
            <div class="cancel_btn">
				<a href="#" onclick="<portlet:namespace/>_submitForm('<%= ChooseDocTemplatePortlet.ACTION_CANCEL %>')"></a>
			</div>
			<div class="next_btn">
				<a href="#" onclick="<portlet:namespace/>_handleNextBtn()"></a>
			</div>
		</div>
    </div><!-- .controls end -->

</form>
</div>

<script type="text/javascript" language="javascript">
	dojo.require("dijit.form.RadioButton");

	function <portlet:namespace/>_submitForm(action) {
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
		return false;
	}

	function <portlet:namespace/>_handleNextBtn() {
		var url = "/portal/auth/portal/boss/workstationCard/WorkstationCardWindow?action=e&windowstate=normal&mode=view&MI_CREATE_CARD=";

		var selectedTemplate = '';
		dojo.query("INPUT[type=radio]").forEach(function(r) {if(r.checked) {selectedTemplate = r.value}});

		url += selectedTemplate;
		
		var linkToCardField = dojo.byId('<portlet:namespace/>_linkToCard');
		if (linkToCardField && linkToCardField.value.length > 0) {
			url += "&MI_LINK_TO_CARD=" + linkToCardField.value;
		}

		var backUrlField = dojo.byId('<portlet:namespace/>_backURL');
		if (backUrlField && backUrlField.value.length > 0) {
			url += "&MI_BACK_URL_FIELD=" + backUrlField.value;
		}
		
		if (selectedTemplate == '1255') {
			url = '/portal/auth/portal/boss/indepResolution/Content?formAction=initIndepRes&action=1';
			if (backUrlField && backUrlField.value.length > 0) {
	            url += "&backURL=" + backUrlField.value;
	            url += "&doneURL=" + backUrlField.value;
	            url += "&stateInit=initCreate";
	        }
			if (linkToCardField && linkToCardField.value.length > 0) {
				url += "&linkToCard=" + linkToCardField.value;
			}
		}
		
		url += "&cardCreateErrorURL=";
		var errorUrl = "/portal/auth/portal/boss/chooseDocTemplate/Content?action=1&windowstate=normal&mode=view&formAction=init";
		errorUrl += "&linkToCard=" + linkToCardField.value;
		errorUrl += "&backURL=" + backUrlField.value;
		
		url += encodeURIComponent(errorUrl);
		
		location.replace(url);
	}
</script>