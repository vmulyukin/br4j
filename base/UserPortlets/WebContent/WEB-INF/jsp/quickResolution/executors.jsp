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

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortlet"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean"%>
<%@page import="com.aplana.dbmi.ajax.QuickResolutionSearchPersonParameters"%>
<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>
<portlet:defineObjects/>

<%	QuickResolutionPortletSessionBean sessionBean = (QuickResolutionPortletSessionBean)renderRequest.getPortletSession().getAttribute(QuickResolutionPortlet.SESSION_BEAN);
%>
<style type="text/css">
   .dijitMenuItem {
     height: 45px;
   }
 </style>

<div class="wizard">
<%	String message = sessionBean.getMessage();
	if (message != null) {
		sessionBean.setMessage(null);
%>
	<div><%=message%></div>
<%	}
%>
<%--h2><strong><fmt:message key="header.step1"/></strong> <fmt:message key="header.descriptionStep1"/></h2--%>
<!-- Выводим ближайщих сотрудников -->
<div class="peoples bottom_line">
<%	
	//формируем вывод ближайщего окружения
	Map immediateMap = sessionBean.getImmediateEmployees();
	Iterator iter = immediateMap.keySet().iterator();
	int numColumns = 3;
	for(int i=1; iter.hasNext(); i++) {
		ObjectId cardId = (ObjectId)iter.next();
		Long id = (Long)cardId.getId();
		String name = (String)immediateMap.get(cardId);
		Map responsible = sessionBean.getResponsible();
		Map additionals = sessionBean.getAdditionals();
		if (responsible.get(id) != null || additionals.get(id) != null) {
%>
		<div id="<portlet:namespace/>_men_<%=id%>" class="men cur">
			<a href="#" id="<portlet:namespace/>_immediate_men_<%=id%>" onclick="<portlet:namespace/>_deselectPersonCard(<%=id%>, '<%=name%>');">
<%		} else {
%>
		<div id="<portlet:namespace/>_men_<%=id%>" class="men">
			<a href="#" id="<portlet:namespace/>_immediate_men_<%=id%>" onclick="<portlet:namespace/>_selectPersonCard(<%=id%>, '<%=name%>');">
<%		}
%>				<p><c:out value="<%=name%>"/></p>
			</a>
		</div>
<% 		if (i%numColumns == 0  ) {
%>
			<div style="clear: left; height: 0pt;"> </div>
<%		}
	}
%>
	<div style="clear: left; height: 0pt;"> </div>
</div>
<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ACTION %>" value=""/>
	<input type="hidden" name="<%= QuickResolutionPortlet.FIELD_NAMESPACE %>" value="<portlet:namespace/>"/>
	<input id="<portlet:namespace/>_responsibleField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_RESPONSIBLE_EXECUTOR %>" value=""/>
	<input id="<portlet:namespace/>_additionalsField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ADDITIONAL_EXECUTORS %>" value=""/>
	<input id="<portlet:namespace/>_externalsField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_EXTERNAL_EXECUTORS %>" value=""/>
	<input id="<portlet:namespace/>_fyiField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_FYI %>" value=""/>
<div class="list">
<table class="content">
	<thead>
		<col width="45%"/>
		<col width="20px"/>
	</thead>
	<tr>
		<td class="executors">
			<table>
				<thead>
					<col width="20%"/>
					<col width="80%"/>
				</thead>
				<tr>
					<td><p class="label"><h2><fmt:message key="label.executors"/></h2></p></td>
					<td>
						<select id="<portlet:namespace/>_executorSelect"/>			
					</td>
				</tr>
				<tr>
					<td valign="top"><p class="label"><fmt:message key="label.responsibleExecutor"/></p></td>
					<td>
						<table id="<portlet:namespace/>_responsibleExecutorTable" class="content">
							<thead>
								<col width="90%"/>
								<col width="10%"/>
							</thead>
						</table>
					</td>
				</tr>
				<tr>
					<td  valign="top"><p class="label"><fmt:message key="label.additionalExecutors"/></p></td>
					<td>
						<table id="<portlet:namespace/>_additionalExecutorsTable" class="content">
							<thead>
								<col width="90%"/>
								<col width="10%"/>
							</thead>
						</table>
					</td>
				</tr>	
			</table>
		</td>

		<td>&nbsp;</td>

		<%--td class="executors">
			<table>
				<thead>
					<col width="20%"/>
					<col width="80%"/>
				</thead>
				<tr>
					<td><p class="label"><h2><fmt:message key="label.fyi"/></h2></p></td>
					<td>
						<select id="<portlet:namespace/>_fyiExecutorSelect"/>			
					</td>
				</tr>
				<tr>
					<td  valign="top"><p class="label">&nbsp;</p></td>
					<td>
						<table id="<portlet:namespace/>_fyiExecutorsTable" class="content">
							<thead>
								<col width="90%"/>
								<col width="10%"/>
							</thead>
						</table>
					</td>
				</tr>	
				
				
				
				<tr>
					<td><p class="label"><h2><fmt:message key="label.extExecutors"/></h2></p></td>
					<td>
						<select id="<portlet:namespace/>_extExecutorSelect"/>			
					</td>
				</tr>
				<tr>
					<td  valign="top"><p class="label">&nbsp;</p></td>
					<td>
						<table id="<portlet:namespace/>_externalExecutorsTable" class="content">							
							<thead>
								<col width="90%"/>
								<col width="10%"/>
							</thead>
						</table>
					</td>
				</tr>	
			</table>

 
			<div class="otherExec">
				<textarea id="<portlet:namespace/>_otherExec" name="<%= QuickResolutionPortlet.FIELD_OTHER_EXEC %>" rows="5" style="width:100%" ><%if (sessionBean.getOtherExec() !=null){%><%=sessionBean.getOtherExec()%><%}%></textarea>
			</div>  		
		</td>
		
	</tr--%>

</table>
</div>
<%-- div class="tools">
	<div class="corn">
		<div style="margin: 0pt auto; width: 250px;">
			<a class="cancel" href="#"
				onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_CANCEL %>')" > </a>
			<a class="next" href="#"
				id="<portlet:namespace/>_nextButton"
				onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_NEXT %>')"> </a>
		</div>
	</div>
</div --%>
</form>
</div>
<script type="text/javascript">
	var <portlet:namespace/>_responsible = ${requestScope.responsible};
	var <portlet:namespace/>_additionals = ${requestScope.additionals};
	var <portlet:namespace/>_externals = ${requestScope.externals};
	var <portlet:namespace/>_fyi = ${requestScope.fyi};
	var <portlet:namespace/>_message = ${requestScope.message};
	
	dojo.addOnLoad(function() {
		dojo.require("dijit.form.Button");
		dojo.require("dojox.data.QueryReadStore");
		dojo.require("dijit.form.FilteringSelect");	
		
		var dataStore = new dojox.data.QueryReadStore({
			url :'<%=request.getContextPath() + "/servlet/SearchCardServlet"%>'
		});
		
		var select = new dijit.form.FilteringSelect(
			{
				store: dataStore,
				searchAttr: 'label',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				query: {
					<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: '',
					<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
				},
				onChange: function() { <portlet:namespace/>_executorSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_executorSelect')
		);
		
		var fyiSelect = new dijit.form.FilteringSelect(
			{
				store: dataStore,
				searchAttr: 'label',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				query: {
					<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: '',
					<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
				},
				onChange: function() { <portlet:namespace/>_fyiExecutorSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_fyiExecutorSelect')
		);
		
		var extSelect = new dijit.form.FilteringSelect(
			{
				store: dataStore,
				searchAttr: 'label',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				query: {
					<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: '',
					<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'ext'
				},
				onChange: function() { <portlet:namespace/>_extExecutorSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_extExecutorSelect')
		);
		<portlet:namespace/>_refreshControls();
		
		if (<portlet:namespace/>_message != '') {
			<portlet:namespace/>_showMessage();
		}
	});
	
	function <portlet:namespace/>_refreshControls() {
		// вызываем код обновления таблиц
		var resp = [];
		if (<portlet:namespace/>_responsible.cardId != undefined) {
			resp[0] = <portlet:namespace/>_responsible;
		} 
		<portlet:namespace/>_fillTable(
			resp, 
			'<portlet:namespace/>_responsibleExecutorTable', 
			'<portlet:namespace/>_responsibleField'
		);		
		<portlet:namespace/>_fillTable(
			<portlet:namespace/>_additionals, 
			'<portlet:namespace/>_additionalExecutorsTable', 
			'<portlet:namespace/>_additionalsField'
		);
		/*<portlet:namespace/>_fillTable(
				<portlet:namespace/>_externals, 
				'<portlet:namespace/>_externalExecutorsTable', 
				'<portlet:namespace/>_externalsField'
		);*/
		/*<portlet:namespace/>_fillTable(
				<portlet:namespace/>_fyi, 
				'<portlet:namespace/>_fyiExecutorsTable', 
				'<portlet:namespace/>_fyiField'
		);*/
		// говорим выподающему списку каких пользователей уже не надо выводить
		var allIds = [];
		for (var i = 0; i < <portlet:namespace/>_additionals.length; ++i) {
			allIds.push(<portlet:namespace/>_additionals[i].cardId);
		}		
		if (<portlet:namespace/>_responsible.cardId != undefined) {
			allIds.push(<portlet:namespace/>_responsible.cardId);
		}
		/*for (var i = 0; i < <portlet:namespace/>_fyi.length; ++i) {
			allIds.push(<portlet:namespace/>_fyi[i].cardId);
		}
		for (var i = 0; i < <portlet:namespace/>_externals.length; ++i) {
			allIds.push(<portlet:namespace/>_externals[i].cardId);
		}*/
		
		var select = dijit.byId('<portlet:namespace/>_executorSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');		
				
		//select = dijit.byId('<portlet:namespace/>_extExecutorSelect');
		//select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');
		
		//select = dijit.byId('<portlet:namespace/>_fyiExecutorSelect');
		//select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');

		// установка значений hidden-ов
		var separator = '<%=QuickResolutionPortlet.SEPARATOR%>';
		var value = "";
		if (<portlet:namespace/>_responsible.cardId != undefined) {
			value = <portlet:namespace/>_responsible.cardId+":"+<portlet:namespace/>_responsible.name;
		}
		dojo.byId('<portlet:namespace/>_responsibleField').value = value;
		
		value = "";
		for(var i=0; i < <portlet:namespace/>_additionals.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_additionals[i].cardId+":"+<portlet:namespace/>_additionals[i].name;
		}
		dojo.byId('<portlet:namespace/>_additionalsField').value = value;
		
		/*value = "";
		for(var i=0; i < <portlet:namespace/>_fyi.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_fyi[i].cardId+":"+<portlet:namespace/>_fyi[i].name;
		}
		dojo.byId('<portlet:namespace/>_fyiField').value = value;

		value = "";
		for(var i=0; i < <portlet:namespace/>_externals.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_externals[i].cardId+":"+<portlet:namespace/>_externals[i].name;
		}
		dojo.byId('<portlet:namespace/>_externalsField').value = value;*/
	}
	
	function <portlet:namespace/>_fillTable(peoples, tableId, hiddenId) {
		var table = dojo.byId(tableId);
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= 0; --i) {
				table.deleteRow(i);
			}
		}
		for (var i = 0; i < peoples.length; ++i) {
			var row = table.insertRow(i);
			var cell = null;
			cell = row.insertCell(0);
			cell.innerHTML = peoples[i].name;
			cell = row.insertCell(1);
			cell.style.textAlign = 'center';
			cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_deselectPersonCard(' + peoples[i].cardId + ',\''+peoples[i].name+'\')">&nbsp;</a>';
		}
	}
	
	function <portlet:namespace/>_selectPersonCard(cardId, name) {
		var picDiv = dojo.byId('<portlet:namespace/>_men_'+ cardId);	
		var pic = dojo.byId('<portlet:namespace/>_immediate_men_'+ cardId);
		if (pic) {
			pic.onclick = function() {
				<portlet:namespace/>_deselectPersonCard(cardId, name);
			};
			picDiv.setAttribute('class', 'men cur');
		}
		if (<portlet:namespace/>_responsible.cardId == undefined) {
			<portlet:namespace/>_responsible.cardId = cardId;
			<portlet:namespace/>_responsible.name = name;
		} else {
			var i = <portlet:namespace/>_additionals.length;
			<portlet:namespace/>_additionals[i] = {cardId: cardId, name: name};
		}
		<portlet:namespace/>_refreshControls();
	}
	
	function <portlet:namespace/>_deselectPersonCard(cardId, name) {
		var picDiv = dojo.byId('<portlet:namespace/>_men_'+ cardId);
		var pic = dojo.byId('<portlet:namespace/>_immediate_men_'+ cardId);
		if (pic) {
			pic.onclick = function() {
				<portlet:namespace/>_selectPersonCard(cardId, name);
			};
			picDiv.setAttribute('class', 'men');
		}
		if (<portlet:namespace/>_responsible.cardId == cardId) {
			<portlet:namespace/>_responsible = {};
		} else {
			for (var i = 0; i < <portlet:namespace/>_additionals.length; ++i) {
				if (<portlet:namespace/>_additionals[i].cardId == cardId) {
					<portlet:namespace/>_additionals.splice(i, 1);
				}
			} 
		}
		for (var i = 0; i < <portlet:namespace/>_externals.length; ++i) {
			if (<portlet:namespace/>_externals[i].cardId == cardId) {
				<portlet:namespace/>_externals.splice(i, 1);
			}
		}
		
		for (var i = 0; i < <portlet:namespace/>_fyi.length; ++i) {
			if (<portlet:namespace/>_fyi[i].cardId == cardId) {
				<portlet:namespace/>_fyi.splice(i, 1);
			}
		}
		<portlet:namespace/>_refreshControls();		
	}
	
	function <portlet:namespace/>_executorSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_executorSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			widget.attr('value', '');
			<portlet:namespace/>_selectPersonCard(card.cardId, name);
		}
	}
	
	function <portlet:namespace/>_extExecutorSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_extExecutorSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.NAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_externals.length;
			<portlet:namespace/>_externals[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}
	
	function <portlet:namespace/>_fyiExecutorSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_fyiExecutorSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_fyi.length;
			<portlet:namespace/>_fyi[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}
	
	function <portlet:namespace/>_submitForm(action) {
		if (action != '<%= QuickResolutionPortlet.ACTION_CANCEL %>' && <portlet:namespace/>_responsible.cardId == undefined) {
			alert('<fmt:message key="message.noExecutor"/>');
			
		} else {
			var form = dojo.byId('<portlet:namespace/>_form');
			dojo.byId('<portlet:namespace/>_action').value = action;
			form.submit();
			return false;
		}
	}
	function <portlet:namespace/>_showMessage() {
		alert(<portlet:namespace/>_message);
	}
</script>