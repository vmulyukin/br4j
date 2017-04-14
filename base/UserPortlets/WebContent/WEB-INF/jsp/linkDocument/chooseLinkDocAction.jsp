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
<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.ContentViewPortlet"/>
<portlet:defineObjects/>

<SCRIPT>
dojo.require('dijit.Dialog');
dojo.require('dijit.form.Button');
</SCRIPT>

	<div dojoType="dijit.Dialog" id="linkDocDlg" title="<fmt:message key="header"/>" style="font-size: 70%; text-align: left;">
		<input type="hidden" name="linkDocDlg_baseCardId" id="linkDocDlg_baseCardId" value="">
		<input type="hidden" name="bean_namespace" id="bean_namespace" value="<portlet:namespace/>">
		<button id="<portlet:namespace/>_CreateNewDocBtn" dojoType="dijit.form.Button" type="button" onclick="createLinkedDoc()">
			<fmt:message key="btn.createNewDoc"/>
		</button>
		
		<button id="<portlet:namespace/>_AddLinkBtn" dojoType="dijit.form.Button" type="button">
			<fmt:message key="btn.link"/>
		</button>
		
		<button id="<portlet:namespace/>_CancelLinkBtn" dojoType="dijit.form.Button" type="button" onClick="dijit.byId('linkDocDlg').hide();">
			<fmt:message key="btn.cancel"/>
		</button>
	</div>
	
	<div>
		<jsp:include page="linkDocumentPicker.jsp"/>
	</div>
	
	<form id="linkDocForm" method="post">
 		<input type="hidden" name="card" value="">
		<input type="hidden" name="add_E_JBR_DOCL_RELATDOC" value="">
	</form>
