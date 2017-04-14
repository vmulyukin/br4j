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
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" import="java.util.*,com.aplana.dbmi.admin.*" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="com.aplana.dbmi.task.TaskInfo" %>
<%@page import="java.text.MessageFormat" %>

<fmt:setBundle basename="com.aplana.dbmi.admin.nl.TaskControlPortletResource"/>

<portlet:defineObjects/>

<%
	TaskControlPortletSessionBean sessionBean = (TaskControlPortletSessionBean)renderRequest.getPortletSession().getAttribute(com.aplana.dbmi.admin.TaskControlPortlet.SESSION_BEAN);
	String activeTaskId = sessionBean.getActiveTaskId();
	boolean isEditAccessExists = sessionBean.isEditAccessExists();
	if (activeTaskId==null)
		activeTaskId = "";
	boolean isShowWarningMessage = sessionBean.isShowWarningMessage();
	ResourceBundle bundle = ResourceBundle.getBundle("com.aplana.dbmi.admin.nl.TaskControlPortletResource", request.getLocale());
	String message = MessageFormat.format(bundle.getString("task.warning.message"), new Object[]{activeTaskId});
%>
<script type ="text/javascript" language="javascript">
dojo.require("dijit.form.Button"); 
dojo.require("dijit.Dialog");

function showInputs(sel) {
	var task_type = sel.options[sel.selectedIndex].value; 
	var cronInput = document.getElementById("cron");
	var intervalInput = document.getElementById("interval");
	var cronStartHint = document.getElementById("cron_start");
	var intervalStartHint = document.getElementById("interval_start");

	switch (task_type) {
    case "interval_task":
    	cronInput.style.display='none';
    	intervalInput.removeAttribute("style");
    	cronStartHint.style.display='none';
    	intervalStartHint.removeAttribute("style");
        break;
    case "cron_task":
    	cronInput.removeAttribute("style");
    	intervalInput.style.display='none';
    	cronStartHint.removeAttribute("style");
    	intervalStartHint.style.display='none';
        break;
	}
}

</script>

<!-- окошко с предупреждением-->
<div id="warningDialog" dojoType="dijit.Dialog" title="<fmt:message key="task.warning.title"/>" style="width: 320px; height: 150px">
	<div style="text-align: left;"><%=message %></div>	
	
	<button dojoType="dijit.form.Button" type="button">
		<fmt:message key="task.form.action.yes"/>
		<script type="dojo/method" event="onClick" args="evt">
			url = '<portlet:actionURL><portlet:param name="<%= TaskControlPortlet.PARAM_ACTION %>" value="<%= TaskControlPortlet.ACTION_CANCEL %>"/><portlet:param name="<%= TaskControlPortlet.PARAM_ID %>" value="<%= activeTaskId %>"/><portlet:param name="<%= TaskControlPortlet.PARAM_PARAMS_DELETE %>" value="true"/></portlet:actionURL>';
			window.location.replace(url);
		</script>
	</button>			
	<button dojoType="dijit.form.Button" type="button">
		<fmt:message key="task.form.action.no"/>
		<script type="dojo/method" event="onClick" args="evt">
			url = '<portlet:actionURL><portlet:param name="<%= TaskControlPortlet.PARAM_ACTION %>" value="<%= TaskControlPortlet.ACTION_CANCEL %>"/><portlet:param name="<%= TaskControlPortlet.PARAM_ID %>" value="<%= activeTaskId %>"/><portlet:param name="<%= TaskControlPortlet.PARAM_PARAMS_DELETE %>" value="false"/></portlet:actionURL>';
			window.location.replace(url);
		</script>
	</button>			
	<button dojoType="dijit.form.Button" type="button">
		<fmt:message key="task.form.action.cancel"/>
		<script type="dojo/method" event="onClick" args="evt">
				dijit.byId('warningDialog').hide();					
		</script>
	</button>			
</div>

<c:if test="<%= isShowWarningMessage==true%>">
	<script type="text/javascript">
		dojo.addOnLoad(function(){
      		dijit.byId('warningDialog').show();
  		});
	</script>
</c:if>   

<DIV style="margin: 6px">
<table class="res"><thead>
<tr>
<th><fmt:message key="task.form.table.id" /></th>
<th><fmt:message key="task.form.table.name" /></th>
<th><fmt:message key="task.form.table.time" /></th>
<th><fmt:message key="task.form.table.repeat" /></th>
<th><fmt:message key="task.form.table.cron" /></th>
<th><fmt:message key="task.form.table.info" /></th>
<th>&nbsp;</th>
</tr></thead>
<tbody>
<%
	Iterator itr = sessionBean.getTasks().iterator();
	boolean even = true;
	while (itr.hasNext()) {
		TaskInfo info = (TaskInfo) itr.next();
		even = !even;
%>		
<tr class="<%= even ? "even" : "odd" %>">
<td><%=info.getId()%></td>
<td><%=info.getModuleName()%></td>
<td><%=info.getStart()%></td>
<td><%=info.getRepeatIntervalStr()%></td>
<td><%=info.getCronExpr()%></td>
<td><%=info.getInfo() == null? "" : info.getInfo()%></td>
<td>
<%if(isEditAccessExists){ %>
<a href="<portlet:actionURL><portlet:param name="<%= TaskControlPortlet.PARAM_ACTION %>" value="<%= TaskControlPortlet.ACTION_CANCEL %>"/><portlet:param name="<%= TaskControlPortlet.PARAM_ID %>" value="<%= info.getId() %>"/></portlet:actionURL>"><fmt:message key="task.form.action.cancel" /></a>
<%} %>
</td>
</tr>
<%
	}
%>
</tbody></table>

<%if(isEditAccessExists){ %>
<form action="<portlet:actionURL><portlet:param name="<%= TaskControlPortlet.PARAM_ACTION %>" value="<%= TaskControlPortlet.ACTION_CREATE %>"/></portlet:actionURL>" method="post" id="submitForm">

<table>
<tr>
<td width="10%">
<fmt:message key="task.form.table.name" />
</td>
<td width="10%">
<select name="<%= TaskControlPortlet.PARAM_NAME %>">
<option value="" selected="selected">...</option>
<%
	itr = sessionBean.getNames().iterator();
	while (itr.hasNext()) {
		String name = (String) itr.next();
%>
<option value="<%= name %>"><%=name%></option>
<%
	}
%>
</select>
</td>
<td>
</td>
</tr>
<tr>
<td>
	<fmt:message key="task.form.table.time" />
</td>
<td> 
<select id="start" name="<%= TaskControlPortlet.PARAM_START_DAY %>">
<%
	Calendar now = Calendar.getInstance();
	for (int i = now.getMinimum(Calendar.DAY_OF_MONTH); i <= now.getMaximum(Calendar.DAY_OF_MONTH); i++) {
		Calendar day = Calendar.getInstance();
		day.set(Calendar.DAY_OF_MONTH, i);
		if (day.before(now))
	day.add(Calendar.MONTH, 1);
%>
<option value="<%= i %>"<%= i == now.get(Calendar.DATE) ? " selected" : "" %>><%=i%><%=sessionBean.suffix(i)%></option>
<%
	}
%>
</select>
<select name="<%= TaskControlPortlet.PARAM_START_HOUR %>">
<%
	for (int i = now.getMinimum(Calendar.HOUR_OF_DAY); i <= now.getMaximum(Calendar.HOUR_OF_DAY); i++) {
%>
<option value="<%= i %>"<%= i == now.get(Calendar.HOUR_OF_DAY) ? " selected" : "" %>><%=i%></option>
<%
	}
%>
</select>
:
<select name="<%= TaskControlPortlet.PARAM_START_MIN %>">
<%	for (int i = now.getMinimum(Calendar.MINUTE); i <= now.getMaximum(Calendar.MINUTE); i += 1) {
 %>
<option value="<%= i %>"><%= i %></option>
<%	} %>
</select>
</td>
<td>
	<div id="interval_start">
		<fmt:message key="task.form.table.interval_start.hint" />
	</div>
	<div id="cron_start" style="display:none">
		<fmt:message key="task.form.table.cron_start.hint" />
	</div>
</td>
</tr>

<tr>
<td>
<fmt:message key="task.form.table.schedule" />
</td>
<td>
<select name="<%= TaskControlPortlet.PARAM_TASK_TYPE %>" onchange="showInputs(this)">
<option value="interval_task" selected="selected"><fmt:message key="task.form.table.interval"/></option>
<option value="cron_task"><fmt:message key="task.form.table.cron"/></option>
</select>
</td>
<td>
</td>
</tr>

<tr id="interval">
<td>
<fmt:message key="task.form.table.repeat" />
</td>
<td>
<select name="<%= TaskControlPortlet.PARAM_INTERVAL %>">
<option value="" selected="selected">...</option>
<%	for (int i = 1; i <= 60; i += 1) {
 %>
<option value="<%= i %>"><%= i %></option>
<%	} %>
</select>

<select id="unit" name="<%= TaskControlPortlet.PARAM_UNIT %>">
<option value="" selected="selected">...</option>
<option value="minutes">minutes</option>
<option value="hours">hours</option>
<option value="days">days</option>
</select>
</td>
<td>
</td>
</tr>

<tr id="cron" style="display:none">
<td>
<fmt:message key="task.form.table.cron" />
</td>
<td> 
<input type="text" name="<%= TaskControlPortlet.PARAM_CRON_EXPR %>"> 
</td>
<td>
<fmt:message key="task.form.table.cron_expr.hint" />
</td>

</tr>
<tr>
<td>
<fmt:message key="task.form.table.info"/>
</td>
<td> 
<input type="text" name="<%= TaskControlPortlet.PARAM_TASK_INFO %>"> 
</td>
<td>
</td>

</tr>
</table>

<input type="submit" name="submit" value=<fmt:message key="task.form.action.create" />>
</form>
<%} %>
</DIV>