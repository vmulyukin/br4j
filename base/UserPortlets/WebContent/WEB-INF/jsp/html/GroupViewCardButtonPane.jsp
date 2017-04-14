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
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib prefix="btn" uri="http://aplana.com/dbmi/tags"%>

<%@page import="com.aplana.dbmi.groupadmin.GroupAdminPortlet"%>
	<c:set var="confirmationDialogId"><portlet:namespace/>_confirmationDialog</c:set>
	<div id="${confirmationDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="group.delete.confirmation.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="group.delete.confirmation.message"/></div>
		<div style="float:right; clear: both;" id="dialogButtons">
			<c:set var="yesBtnId"><portlet:namespace/>_yesBtnId</c:set>
			<button id="${yesBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="group.delete.confirmation.yes"/>
			    <script type="dojo/method" event="onClick" args="evt">
						submitForm('<%=GroupAdminPortlet.DELETE_GROUP_ACTION%>');
				</script>		
			</button>
			<c:set var="noBtnId"><portlet:namespace/>_noBtnId</c:set>
			<button id="${noBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="group.delete.confirmation.no"/>
				<script type="dojo/method" event="onClick" args="evt">
					dijit.byId('${confirmationDialogId}').hide();
				</script>	
			</button>	
		</div>
	</div>
	<script type="text/javascript" language="javascript">
		dojo.require('dijit.Dialog');
		dojo.require('dijit.form.Button');
	</script>

	<div class="buttonPanel" style="float: right">
		<ul>
			<c:set var="onClickCallback">submitForm('<%=GroupAdminPortlet.BACK_ACTION%>')</c:set>
			<btn:button onClick="${onClickCallback}" textKey="group.admin.close.btn" />

			<c:set var="onClickCallback">submitForm('<%=GroupAdminPortlet.OPEN_EDIT_MODE_ACTION%>')</c:set>
			<btn:button tooltipKey="tool.edit"
				onClick="${onClickCallback}"
				icon="ico_edit"/>
			<c:set var="onClickCallback">dijit.byId('${confirmationDialogId}').show()</c:set>
			<btn:button onClick="${onClickCallback}" textKey="group.admin.delete.btn"/>
		</ul>
	</div>