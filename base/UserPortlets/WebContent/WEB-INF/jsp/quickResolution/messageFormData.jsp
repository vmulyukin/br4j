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
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortlet"%>
<%@page import="com.aplana.dbmi.Portal"%>

<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean"%>
<%@page import="com.aplana.dbmi.service.PortletUtil"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>
<portlet:defineObjects/>
<%
	QuickResolutionPortletSessionBean sessionBean = 
		(QuickResolutionPortletSessionBean)renderRequest.getPortletSession().
		getAttribute(QuickResolutionPortlet.SESSION_BEAN);
%>

<c:set var="sessionBean" value="<%=sessionBean%>"/>

<div class="resolution">

	<div class="left_column">
		<h1><fmt:message key="label.message"/></h1>
		<p class="breif">
			<c:out value="${sessionBean.message}"/>
		</p>
	</div>

</div>

<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">

	<div class="resolution">
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ACTION %>" value=""/>	
	<hr/>
	<br/>
        <div style="float: left;">
        	<div class="controls">
            	<div class="resolution_buttons">
            		<div class="cancel_resolution">
						<a href="#"
							onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_CANCEL %>')"></a>
					</div>
            	</div>
			</div><!-- .controls end -->
 		</div>
 	</div>
</form>
		

<script type="text/javascript">
	
	function <portlet:namespace/>_submitForm(action) {
		<portlet:namespace/>_submitForm_do(action);
	}
	
	function <portlet:namespace/>_submitForm_do(action){
		lockScreen();
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
	}
</script>