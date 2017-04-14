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
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.portlet.BrowsingReportsPortletSessionBean"%>
<%@page import="com.aplana.dbmi.portlet.BrowsingReportsPortlet"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.portlet.BrowsingReportsSearchParameters"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.BrowsingReportsPortlet" scope="request"/>

<%  BrowsingReportsPortletSessionBean sessionBean = (BrowsingReportsPortletSessionBean)renderRequest.getPortletSession().getAttribute(BrowsingReportsPortlet.SESSION_BEAN);
    String valueExportType = sessionBean.getExportType();
    String contextPath = request.getContextPath() + "/servlet/JasperReportServlet?";
    String curDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    String userName = request.getUserPrincipal().getName();
%>

<c:set var="sessionBean" value="<%=sessionBean%>"/>
<c:set var="headerIcon" value="item5"/>

<style type="text/css">
    .control {
        margin-top: 10px;
        margin-left: 0.2em;
    }
    .buttonPanel {
        margin-top: 5px; 
    }
    .selectKitControls {
        width: 400px;
    }
    .kitControls {
        margin-left: 30px;
    }
    .single_column {
        padding: 16px 20px 19px 0;
        width: 650px;
    }
 </style>

<h1 class="caption">
<span class="header_icon ${headerIcon}"></span> <c:out value="${sessionBean.header}"/>
	<a class="button create" href="javascript:handleCreateDocument()"></a>
	<c:if test="${sessionBean.switchNavigatorLink != null}">
		${sessionBean.switchNavigatorLink}
	</c:if>
</h1>

<div id="blockReportSelect" class="additional_agreement">
    <hr>
    <div class="single_column">
        <div class="block">
            <div class="select">
                <!--h2 style="width: 60px;"><label><fmt:message key="title.report" /></label></h2  -->
                <select id="reportSelect"></select>
            </div>
        </div>
    </div>
    <hr>
</div>

<div class="additional_agreement">
    <div id="controls">
    </div>
    <div class="buttonPanel">
        <button dojoType="dijit.form.Button" type="button" id="<portlet:namespace/>_uploadButton">
            <fmt:message key="button.print"/>
            <script type="dojo/method" event="onClick" args="evt">
                print();
            </script>
        </button>
    </div>
</div>

<script type="text/javascript">
    dojo.require("dijit.form.FilteringSelect");
    dojo.require("dojo.data.ItemFileReadStore");
    dojo.require("dojo.data.ItemFileWriteStore");
    dojo.require('dbmiCustom.DateTimeWidget');
    dojo.require("dojox.data.QueryReadStore");
    dojo.require("dijit.form.CheckBox");
    
    var dataReports = <%=sessionBean.getJsonReports() %>;
    var reportId;
    var cookie;
    var isSelect = false;
    var allSelectKitRequire=true;
    // код для работы контрола выбора сотрудников
    // объектов выбраных карточек контролов: {имя_контрола: [выбраные id карточек персон],...}
    // {controlId_1: [...,...,...], controlId_2: [...,...,...], ...}
    var choicesPersonControls = {};
    var choicesCardControls = {};
    var chosenReferenceControls = {};
    var choicesSelectKitControls = {};
    var valuesRadioButton = {}
    //---------------
    var chosenAllRefs = false;
    var colNumber = 1;
    var cardQueryValue = [];
    var groups = [];
    var ignoredSelectKitControls = {};
    var optionsSelectKitControls = {};
	
	var depControls = [];
    //  
    var dataStore = new dojox.data.QueryReadStore({
        url :'<%=request.getContextPath() + "/servlet/SearchCardServlet"%>'
    });
    // ------------------------------------------
    dojo.addOnLoad(function() {
        var reportsStore = new dojo.data.ItemFileReadStore(
            {data: getOptionalsReport(dataReports.reports)}
        );  
        var reportSelect = new dijit.form.FilteringSelect(
            {   style: 'width: 500px;',
                store: reportsStore,
                onChange: function() {updateControls(this.value)}
            },
            dojo.byId('reportSelect')
        );
    });
  
    function setCookie (name, value, expires, path, domain, secure) {
        document.cookie = name + "=" + escape(value) +
          ((expires) ? "; expires=" + expires : "") +
          ((path) ? "; path=" + path : "") +
          ((domain) ? "; domain=" + domain : "") +
          ((secure) ? "; secure" : "");
  }
    
    function getCookie(name) {
        var cookie = " " + document.cookie;
        var search = " " + name + "=";
        var setStr = null;
        var offset = 0;
        var end = 0;
        if (cookie.length > 0) {
            offset = cookie.indexOf(search);
            if (offset != -1) {
                offset += search.length;
                end = cookie.indexOf(";", offset)
                if (end == -1) {
                    end = cookie.length;
                }
                setStr = unescape(cookie.substring(offset, end));
            }
        }
        return(setStr);
    }
    
    function updateControls(repId) {
        cookie = getCookie("References"+'<%=userName%>');
        reportId = repId;
        var div = dojo.byId('controls');
        if (reportId == '') {
            dojo.empty(div);
            isSelect = false;
        } else {
            var descrKit = dataReports.controls[reportId].kits[dataReports.controls[reportId].root];
            isSelect = true;
         	// очищаем глобальные переменные
            clearGlobalVariables();
            drawControls(div, descrKit);
        }
        
    }
    
    function getOptionalsReport(reports) {
        var store = {};
        store.identifier = 'id';
        store.label = 'name';
        store.items = [];
        for (var i in reports) {
            var item = {};
            item[store.identifier] = reports[i].id;
            item[store.label] = reports[i].label;
            store.items[store.items.length] = item;
        }
        return store;
    }
    
    function getOptionalsKit(options, dep_control) {
        var store = {};
        store.identifier = 'id';
        store.label = 'name';
        store.dependentValue = 'depValue';
        store.items = [];
        var all_dep_values = '';
        for (var i in options) {
            var item = {};
            item.id = options[i].ref;
            item.name = options[i].name;
            item.depValue = options[i].dep_value;
            if(all_dep_values == '')
            	all_dep_values = all_dep_values + options[i].dep_value;
            else
            	all_dep_values = all_dep_values + ',' + options[i].dep_value;
            store.items[store.items.length] = item;
        }
        cardQueryValue[cardQueryValue.length] = {control: dep_control, depValues: '', allDepValues: all_dep_values};
        return store;
    }
    
    function changeOptionalsKit(name) {
        var store = {};
        store.identifier = 'id';
        store.label = 'name';
        store.dependentValue = 'depValue';
        store.items = [];
        
        var options = optionsSelectKitControls[name];
        var filteredOptionals = [];
        
        for (var i in options) {
        	var ignoreFlag = false;
        	for(var j in ignoredSelectKitControls[name]) {
        		if(ignoredSelectKitControls[name][j] == options[i].ref)
        			ignoreFlag = true;
        	}
        	if(!ignoreFlag)
        		filteredOptionals[filteredOptionals.length] = options[i];
        }
        
        for (var i in filteredOptionals) {
            var item = {};
            item.id = filteredOptionals[i].ref;
            item.name = filteredOptionals[i].name;
            item.depValue = filteredOptionals[i].dep_value;
            store.items[store.items.length] = item;
        }
        
        var optionsStore = new dojo.data.ItemFileReadStore(
                {data: store}
             );
        
        return optionsStore;
    }
    
    function addControl(div, descrControl) {
        var type = descrControl.type;
        if (type == 'String') {
            addStringControl(div, descrControl.name, descrControl.label);
        } else if (type == 'Long') {
            addLongControl(div, descrControl.name, descrControl.label);
        } 
        else if (type == 'Date') {
            addDateControl(div, descrControl.name, descrControl.label, null);
        } else if (type == 'Boolean') {
            addBooleanCheckBoxControl(div, descrControl.name, descrControl.label, descrControl.value);
        } else if (type == 'SelectKit' || type == 'YearPeriod') {
            addSelectKitControl(div, descrControl.name, descrControl.label, descrControl.options, descrControl.dep_control, descrControl.multiValued, descrControl.defValue, descrControl.allGroups);
        } else if (type == 'Persons') {
            addPersonControl(div, descrControl.name, descrControl.label);
        } else if (type == 'ValuesRef') {
            addValuesRefControl(div, descrControl.name, descrControl.label, descrControl.values);
        } else if (type == 'ValuesRefList') {
            addValuesRefListControl(div, descrControl.name, descrControl.label, descrControl.values, descrControl.defvalues, descrControl.allGroups);
        } else if (type == 'Card') {
            addCardControl(div, descrControl.name, descrControl.label, descrControl.template, descrControl.query, descrControl.sqlxml);
        } else if (type == 'Cards') {
            addCardsControl(div, descrControl.name, descrControl.label, descrControl.template, descrControl.query, descrControl.dep_control, descrControl.dep_attr, descrControl.sqlxml, descrControl.defValue, descrControl.buttons, descrControl.group, descrControl.allGroups, descrControl.allSelected);
        } else if (type == 'Radio'){
        	addRadioButtonControl(div, descrControl.name, descrControl.label, descrControl.options, descrControl.defValue);        	
        } else if (type == 'DatePeriod'){
        	addDatePeriod(div, descrControl.name, descrControl.label, descrControl.options, descrControl.depending); 
        	if(descrControl.depending!=null && descrControl.depending.length!=0){
        		return descrControl;
        	}
        	
        }   
        return null;
        
    }

    function addCardControl(div, name, label, template, query, sqlxml){
        var widget = dijit.byId(name);  
        if (widget != null) {
            widget.destroyRecursive(false);
        }
        
        var control = dojo.create('div', 
            {   innerHTML: '<div>'+ 	
                                '<label><b>'+ label +':</b></label>&nbsp;&nbsp;<select id="' + name + '"></select>'+
                            '</div>'
            }
        );
        
        dojo.addClass(control, 'control');
        div.appendChild(control);
            
        var widget = createCardWidget(name, template, query, sqlxml);       
    }

    // Код контрола выбора карточки
    function createCardWidget(controlId, template, query, sqlxml) {
        var select = new dijit.form.FilteringSelect(
            {
                store: dataStore,
                searchAttr: 'label',
                pageSize: 15,
                searchDelay: 500,
                required: false,
                autoComplete: false,
                onBlur: function() {
                	this.attr('value', this.value);
                },
                style: 'width: 600px;',
                query: {
                    <%= BrowsingReportsSearchParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
                    <%= SearchCardServlet.PARAM_CALLER %>: '<%= BrowsingReportsSearchParameters.CALLER %>',
                    <%= SearchCardServlet.PARAM_IGNORE %>: '',
                    <%= BrowsingReportsSearchParameters.PARAM_TEMPLATE %>: template,
                    <%= BrowsingReportsSearchParameters.PARAM_QUERY %>: query,
                    <%= BrowsingReportsSearchParameters.PARAM_SQLXML %>: sqlxml
                }
            },
            dojo.byId(controlId)
        );
    }
    
    // div - DOM div к которому добавляется контрол
    // name - id и имя возвращаемого параметра
    // label - подпись к параметру
    function addStringControl(div, name, label) {
        var control = dojo.create('div', 
            {   innerHTML: '<label><b>'+label+':</b></label>&nbsp;&nbsp;<input id="'+name+'"type="text" size="34"/>'}
        );
        dojo.addClass(control, 'control');
        div.appendChild(control);
    } 
    
     function addLongControl(div, name, label) {
        var control = dojo.create('div', 
            {   innerHTML: '<label><b>'+label+':</b></label>&nbsp;&nbsp;<input id="'+name+'"type="text" size="34"/>'}
        );
        dojo.addClass(control, 'control');
        div.appendChild(control);
    } 
     
    function destroyWidget(name){
        var widget = dijit.byId(name);  
        if (widget != null) {
            widget.destroyRecursive(false);
        }
    }
     
    function addDatePeriod(div, name, label, options, depending){
    	
    	destroyWidget(name); //удаляем все предыдущие виджеты с таким именем
    	destroyWidget(options[0].id+"inHtml");
    	destroyWidget(options[1].id+"inHtml")
    	destroyWidget(options[0].id);
    	destroyWidget(options[1].id);
    	var widgetFrom= addDateControl(div, options[0].id+"inHtml", options[0].name, options[0].id);
    	var widgetTo = addDateControl(div, options[1].id+"inHtml", options[1].name,options[1].id);
    	
    	//widget=dojo.byId(options[0].id);
    	dojo.connect(widgetFrom._date, 'onChange', 
    			function() {
    				var _value = this.value; 
    				processingDepending(depending,dijit.byId(options[0].id).value, dijit.byId(options[1].id).value);
    			}
    	);
    	
    	//widget=dojo.byId(options[1].id);
    	dojo.connect(widgetTo._date, 'onChange', 
    			function() {
    				var _value = this.value; 
    				processingDepending(depending,dijit.byId(options[0].id).value, dijit.byId(options[1].id).value);
    			}
    	);
    	
    	
    	

        
    }
    
    function processingDepending(depending, widgetFromValue, widgetToValue){
    	var dateFrom=widgetFromValue.valueOf();
    	var dateTo=widgetToValue.valueOf();
    	var dependence=null;
    	for (var i=0; i < depending.length; i++) {
    		dependence=dojo.byId(depending[i].id);
    		if(depending[i].condition=="from=to" && dateFrom==dateTo){
    			processingAction(dependence, depending[i].action)
    		}else if(depending[i].condition=="from#to" && dateFrom!=dateTo){
    			processingAction(dependence, depending[i].action)
    		}   		
    	}    	
    }
    
    function processingAction(widget, action){
    	if(action=="disabled"){
    		widget.disabled=true;
    	}else if(action=="enabled"){
    		widget.disabled=false;
    	}
    }
    
    function addDateControl(div, name, label, idWidget) {
        var widget = dijit.byId(name);  
        if (widget != null) {
            widget.destroyRecursive(false);
        }
    	if(idWidget==null){
    		idWidget=name;
    	}
        var control = dojo.create('div', 
            {   innerHTML: '<label style="float: left;"><b>'+label+':</b></label>&nbsp;&nbsp;<div style="float: left;" id="'+name+'"></div>'}
        );
        dojo.addClass(control, 'control');
        div.appendChild(control);
            
        var widget = new dbmiCustom.DateTimeWidget( 
        {
            nameDate: idWidget, 
            valueString: '<%=curDate%>',
            _widthDate: 100
        });
        widget.placeAt(dojo.byId(name));
        return widget;
    }
    function addBooleanCheckBoxControl(div, name, label, value)
    {
        var t = '';
    if(value == true) t = ' checked="checked"';
    var control = dojo.create
        (
            'div', 
            {
                innerHTML: 
                    '<label><b>' +
                    label +
                    ':</b></label>&nbsp;&nbsp;<input id="' +
                    name +
                    '" type="checkbox"' + t +
                    '"/>'
            }
        );
            dojo.addClass(control, 'control');
            div.appendChild(control);
    }
    function addPersonControl(div, name, label) {
        var widget = dijit.byId(name);  
        if (widget != null) {
            widget.destroyRecursive(false);
        }
        
        var control = dojo.create('div', 
            {   innerHTML: '<div>'+
                                '<label><b>'+ label +':</b></label>&nbsp;&nbsp;<select id="'+ name +'"></select>'+
                                '<table id="personTable_'+ name +'" class="content" style="width: 300px; margin-left: 20px;">'+
                                    '<thead>'+
                                        '<col width="90%"/>'+
                                        '<col width="10%"/>'+
                                    '</thead>'+
                                '</table>'+
                            '</div>'
            }
        );
        
        dojo.addClass(control, 'control');
        div.appendChild(control);
            
        var widget = createPersonWidget(name);
    }

    function addCardsControl(div, name, label, template, query, dep_control, dep_attr, sqlxml, defValue, buttons, group, allGroups, allSelected) {
        var widget = dijit.byId(name);
        if (widget != null) {
            widget.destroyRecursive(false);
        }
        var inner_html = '<div>'+
        '<label><b>'+ label +':</b></label>&nbsp;&nbsp;<select id="'+ name +'"></select>';
		 if(buttons==true)
		            inner_html =inner_html + '<a href="javascript:void(0)" onclick="addAllCards(\''+ name +'\', \'' + dep_control + '\', \''+allGroups+'\', \''+allSelected+'\')">Все</a>&nbsp<a href="javascript:void(0)" onclick="removeAllCards(\''+ name +'\', \'' + dep_control + '\')">Очистить</a>';

		  inner_html =inner_html + '<table id="cardTable_'+ name +'" class="content" style="width: 300px; margin-left: 20px;">'+
		                              '<thead>'+
		                                '<col width="90%"/>'+
		                                '<col width="10%"/>'+
		                              '</thead>'+
		                            '</table>'+
		                          '</div>';
		
		 var control = dojo.create('div', 
		     {   innerHTML: inner_html
		     }
		 );
        dojo.addClass(control, 'control');
        div.appendChild(control);
        
        addGroup(group);
            
        var widget = createCardsWidget(name, template, query, dep_control, dep_attr, sqlxml, defValue);
    }
    
    function addGroup(group) {
    	if(group != '' && group !== undefined){
    		var bool = false;
    		for(var i = 0; i < groups.length; i++){
    			if(groups[i].id == group){
    				bool = true;
    				break;
    			}    		
    		}
    		if(bool == false) {
    			groups[groups.length] = {id: group, value: false};
    		}
    	}
    }
    
    function addValuesRefControl(div, name, label, values) {
        for (var i=0; i < values.length; i++) {
            var widget = dijit.byId(name+'_'+values[i].id); 
            if (widget != null) {
                widget.destroyRecursive(false);
            }
        }
        var control = dojo.create('div', 
            {   innerHTML:  '<table><tr>'+
                                '<td style="vertical-align: top;"><label><b>'+label+':</b></label>&nbsp;&nbsp;</td>'+
                                '<td><table id="table_'+name+'"></table></td>'+
                            '</tr></table>'
            }
        );
        dojo.addClass(control, 'control');
        
        //var table = dojo.create('table');
       // table.id = 'table_'+name;
        //control.appendChild(table);
        div.appendChild(control);
        
        // формируем модель
        var model = {};
        for (var i=0; i < values.length; i++) {
            model[values[i].id] = true;
        }
        
        var table = dojo.byId('table_'+name);
        table.model = model;
        for (var i=0; i < values.length; i++) {
            var row = table.insertRow(i);
            var cell = null;
            cell = row.insertCell(0);
        
            var w_name = name+'_'+values[i].id
            var cb = dojo.create('div', 
                {   innerHTML: '<div style="float: left;" id="'+w_name+'"></div><label style="float: left;">'+values[i].label+'</label>'}
            );
            cell.appendChild(cb);
            
            
            var widget = new dijit.form.CheckBox({
                name: w_name,
                value: true,
                checked: true,
                valueId: values[i].id,
                onChange: function() {
                    model[this.valueId] = this.attr('value');
                }   
            });
    
            widget.placeAt(dojo.byId(w_name));  
        }
    }
    
    function addRadioButtonControl(div, name, label, options, defValue){
        //удаляем старые элементы
    	for (var i=0; i < options.length; i++) {
            var widget = dijit.byId(name+'_'+options[i].id); 
            if (widget != null) {
                widget.destroyRecursive(false);
            }
        }
        // создаём табличку и заворачиваем её в тег <div>
        var control = dojo.create('div', 
                {   innerHTML:  '<table><tr>'+
                                    '<td style="vertical-align: top;"><label><b>'+label+':</b></label>&nbsp;&nbsp;</td>'+
                                    '<td><table id="table_'+name+'"></table></td>'+
                                '</tr></table>'
                }
            );
        
        //блок дочерних элементов
        var kit = dojo.create('div', {innerHTML: ''});
        dojo.addClass(kit, 'kitControls');
        control.appendChild(kit);
        
        //добававляем в control класс control
        dojo.addClass(control, 'control');            
        div.appendChild(control);

        
        var table =  dojo.byId('table_'+name);
       	var activeWidgetName = '';
        for (var i=0; i < options.length; i++) {
            var row = table.insertRow(i);
            var cell = null;
            cell = row.insertCell(0);
        
            var w_name = name+'_'+options[i].id
            var cb = dojo.create('div', 
                {   innerHTML: '<div style="float: left;" id="'+w_name+'"></div><label style="float: left;">'+options[i].name+'</label>'}
            );
            cell.appendChild(cb);
            
            var check=false;            
            if(i==defValue){
            	check=true;
            	valuesRadioButton[name]=options[i].id;
            	activeWidgetName=name;
            }
            
            
            var widget = new dijit.form.RadioButton({
                name: name, //тут правильно
                value: options[i].id,
                checked: check,
                valueId: options[i].id,
                onChange: function() {
                	if(!this.attr('value')){
                		return;
                	}
                	valuesRadioButton[name] = this.attr('value');
                	if(this.attr('checked')==true){
                		updateRadioButton(kit,name);
                	}
                }   
            });
    
            widget.placeAt(dojo.byId(w_name));  
        }
        if(activeWidgetName!=''){
        	updateRadioButton(kit, activeWidgetName)
        }
        
        
    }
    
    function updateRadioButton(kit, widgetName){
    	updateAdditionalControls(kit, valuesRadioButton[widgetName]); 
    }
    
    //--------------------------------------------------------------------------------------------------
    function addValuesRefListControl(div, name, label, values, defvalues, allGroups) 
    {
        var widget = dijit.byId(name);  
        if (widget != null) widget.destroyRecursive(false);   
        var control = dojo.create
        (
                'div', 
                {   
                    innerHTML: 
                        '<label style="float: left;"><b>'+ 
                        label+
                        ':</b></label>&nbsp;&nbsp;<div style="float: left;" id="'+
                        name+
                        '"></div><a href="javascript:void(0)" onclick="addAllref(\''+ name +'\', \''+allGroups+'\')">Все</a>&nbsp<a href="javascript:void(0)" onclick="removeAllref(\''+ name +'\')">Очистить</a>'+
                        '<table><tr><td style="vertical-align:top;">'+
                                    '<table id="referenceTable1_'+ 
                                    name +
                                    '" class="content" style="width: 200px; vertical-align: top; margin-right:20px;">'+
                                    '<thead>'+
                                        '<col width="90%"/>'+
                                        '<col width="10%"/>'+
                                    '</thead>'+
                                    '</table>'+
                            '</td>'+
                            '<td style="vertical-align:top;">'+
                                    '<table id="referenceTable2_'+ 
                                    name +
                                    '" class="content" style="width: 200px; vertical-align: top; margin-right:20px;">'+
                                    '<thead>'+
                                        '<col width="90%"/>'+
                                        '<col width="10%"/>'+
                                    '</thead>'+
                                    '</table>'+
                            '</td>'+
                            '<td style="vertical-align:top;">'+
                                    '<table id="referenceTable3_'+ 
                                    name +
                                    '" class="content" style="width: 200px; vertical-align: top; margin-right:20px;">'+
                                    '<thead>'+
                                        '<col width="90%"/>'+
                                        '<col width="10%"/>'+
                                    '</thead>'+
                                    '</table>'+
                            '</td></tr></table>'}
        );
        dojo.addClass(control, 'control');
        div.appendChild(control);   
        var reportsStore = new dojo.data.ItemFileWriteStore(
                {data: getOptionalsReport(values)}
            );  
        var reportSelect = new dijit.form.FilteringSelect(
            {   
                store: reportsStore,
                required: false,
                autoComplete: false,
                onChange: function(){referenceSelectAdd(name)}
            },
            dojo.byId(name)
        );
        chosenReferenceControls[name] = [];
        chosenAllRefs = false;
        
        for(var i in defvalues)
        {
        	var defid = defvalues[i].id;
        	var deflabel;
        	if(defid == '-1') {
        		deflabel = allGroups;
        		chosenAllRefs = true;
        	}
        	else
        		deflabel = defvalues[i].label;
        	
        	referenceSelectAddToList(name, {id:defid, name:deflabel});
        }
    }

    function clearArray(name)
    {
        var references = chosenReferenceControls[name];
        references.splice(0,references.length);
    }

    function clearTable(table)
    {
        l=table.rows.length;
        for(var i = 1; i <=l; i++){
            table.deleteRow(0);
        }
    }
    
    function clearTables(name)
    {
        clearTable(dojo.byId('referenceTable1_'+name));
        clearTable(dojo.byId('referenceTable2_'+name));
        clearTable(dojo.byId('referenceTable3_'+name));
    }
    
    function addAllref(name, allGroups)
    { 
        if (chosenAllRefs==true) return;
        clearTables(name);
        var references = chosenReferenceControls[name];
        clearArray(name);
        dijit.byId(name).store.revert();
        references[references.length] = {id: -1};
        var table = dojo.byId('referenceTable1_'+name);
        var row = table.insertRow(0);
        var cell = null;
        cell = row.insertCell(0);
        if (reportId=='deadlinesControlOfControlDocumentsReport') {
        	cell.innerHTML = 'По всем типам контроля';
        } else if (allGroups !== 'undefined') {
            cell.innerHTML = allGroups;
        } else {
            cell.innerHTML = 'Все документы';
        }
        cell = row.insertCell(1);
        cell.style.textAlign = 'center';
        cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="removeAllref(\''+ name +'\')">&nbsp;</a>';
        chosenAllRefs=true;
    }
    function removeAllref(name)
    {
        if (chosenAllRefs==true)
            chosenAllRefs=false;
        clearTables(name);
        clearArray(name);
        dijit.byId(name).store.revert();
    }
    function referenceSelectAdd(name) 
    {
        if (chosenAllRefs==true){
            clearTables(name);
            clearArray(name);
            chosenAllRefs=false;
        }
        var widget = dijit.byId(name);
        if (widget.isValid() && widget.item != null) {
            referenceSelectAddToList(name, {id:widget.item.id, name:widget.item.name});
	        widget.store.deleteItem(widget.item);
	        widget.attr('value', '');
        }
    }
    function referenceSelectAddToList(name, ref)
    {
        var x = 1;
        var references = chosenReferenceControls[name];
        for(var i in references) if(references[i].id == ref.id) return;
        var index = references.length;
        if (index>2){
            var mod = index%3;
            switch (mod) {
                case 0: {x=1; break;};
                case 1: {x=2; break;};
                case 2: {x=3; break;};
            }
        }
        else
            switch (index) {
            case 0: {x=1; break;};
            case 1: {x=2; break;};
            case 2: {x=3; break;};
        }
        //alert("index="+index);
        //alert("x="+x);
        references[index] = ref;
        var table = dojo.byId('referenceTable'+x+'_'+name);
        var row = table.insertRow(-1);
        var cell = null;
        cell = row.insertCell(0);
        cell.innerHTML = references[index].name;
        cell = row.insertCell(1);
        cell.style.textAlign = 'center';
        cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="referenceSelectDelete(\''+ name +'\','+ references[index].id +','+x+')">&nbsp;</a>';  
    
    }

    function referenceAddInTable(nameTable,nTable, refName, refId)
    {
        var table = dojo.byId('referenceTable'+nTable+'_'+nameTable);
        var row = table.insertRow(-1);
        var cell = null;
        cell = row.insertCell(0);
        cell.innerHTML = refName;
        cell = row.insertCell(1);
        cell.style.textAlign = 'center';
        cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="referenceSelectDelete(\''+ nameTable +'\','+ refId +','+nTable+')">&nbsp;</a>';   
                
    }
    
    function rebuildTables(name)
    {
        clearTables(name);
        var references = chosenReferenceControls[name];
        var nTable=1;
        for (var i = 0; i < references.length; i++) 
        {
            //alert("i="+i);
            //alert("id="+references[i].id);
            //alert("ref name="+references[i].name);
            //alert("name="+name);
            referenceAddInTable(name, nTable,references[i].name, references[i].id)
            nTable++;
            if (nTable>3)nTable=1;
        }
    }
    
    function referenceSelectDelete(name, refId,tableNum)
    {
        var references = chosenReferenceControls[name];
        var table = dojo.byId('referenceTable'+tableNum+'_'+name);
        for (var i = 0; i < references.length; ++i) {
            if (references[i].id == refId) {
	            references.splice(i, 1);
            }
        } 
        rebuildTables(name);

        
        var widget = dijit.byId(name);
        widget.store.revert();
        for(var i in references) {
            var item = widget.store._getItemByIdentity(references[i].id);
            widget.store.deleteItem(item);
        }
    }
  //----------------------------------------------------------------------------------------------------  
    function addSelectKitControl(div, name, label, options, dep_control, multiValued, defValue, allGroups) {
    	choicesSelectKitControls[name] = [];
    	ignoredSelectKitControls[name] = [];
    	optionsSelectKitControls[name] = options;
    	  var inner_html = '<table>'+
                           '<tr>'+
                             '<td><label><b>'+label+':</b></label>&nbsp;&nbsp;</td>'+
                             '<td class="selectKitControls"><select id="'+name+'"></select></td>';

       if(multiValued==true) {
           inner_html =inner_html + '<td style="padding-left:5px"><a href="javascript:void(0)" onclick="addAllSelectKits(\''+ name +'\', \'' + dep_control + '\', \''+allGroups+'\')">Все</a>&nbsp<a href="javascript:void(0)" onclick="removeAllSelectKits(\''+ name +'\', \'' + dep_control + '\')">Очистить</a>'+'</td>';
       }
       inner_html =inner_html +  '</tr>'+
                          '</table>';
       if(multiValued==true)
           inner_html =inner_html + '<table id="selectKitTable_'+ name +'" class="content" style="width: 300px; margin-left: 20px;">'+
                          '<thead>'+
                            '<col width="90%"/>'+
                            '<col width="10%"/>'+
                          '</thead>'+
                        '</table>'+
                      '</div>';
        var control = dojo.create('div', 
            {   innerHTML: inner_html
            }
        );
        var kit = dojo.create('div', {innerHTML: ''});
        dojo.addClass(kit, 'kitControls');
        control.appendChild(kit);
        dojo.addClass(control, 'control');
        div.appendChild(control);
       
        var optionsStore = new dojo.data.ItemFileReadStore(
           {data: getOptionalsKit(options, dep_control)}
        );
        
        var widget = dijit.byId(name);  
        if (widget != null) {
            widget.destroyRecursive(false);
        } 
        widget = new dijit.form.FilteringSelect
        (
            {   style: 'width: 100%',
                store: optionsStore,
                value: defValue,
                required: false,
                onChange: function() {changeSelectKit(kit, name, multiValued, dep_control);}
            },
            dojo.byId(name)
        );
        if (defValue != "") {
            changeSelectKit(kit, name, multiValued, dep_control);
         }
    }
    
    function updateAdditionalControls(div, kitId) {
        var descrKit = dataReports.controls[reportId].kits[kitId];
        
        drawControls(div, descrKit);
    }
    
    function drawControls(div, descrKit) {
        // очищаем div
        dojo.empty(div);
        var postProcessing = [];
        var postItem=null;
        // выводим контролы по очереди
        for (var i in descrKit) {
        	postItem=addControl(div, descrKit[i]);  
        	if(postItem!=null){
        		postProcessing[i]=postItem;
        	}
        }
        postProcess(postProcessing);
        
    }
    //тут будет выполняться, то что должно выполняться после прорисовки всех виджитов      
    function postProcess(postProcessing){
    	var postItem = null;
    
    	for (var i=0; i < postProcessing.length; i++) { 
    		postItem=postProcessing[i];
        	if(postItem.type=='DatePeriod'){
            	var widgetFromValue = Date.parse(dijit.byId(postItem.options[0].id).valueNode.value);
            	var widgetToValue = Date.parse(dijit.byId(postItem.options[1].id).valueNode.value);        		
        		processingDepending(postItem.depending, widgetFromValue, widgetToValue)
        	}
        }
        refreshDependencies();
    }
    
    function clearGlobalVariables() {
    	groups = [];
    }
    
    function saveReferences()
    {
        var refs = [];
        for (var i in chosenReferenceControls)
        {
            var valueIds = [];
            for (var j = 0; j < chosenReferenceControls[i].length; j++) 
            {
                valueIds[valueIds.length] = chosenReferenceControls[i][j].id + "=" + chosenReferenceControls[i][j].name;
            }
            refs[refs.length] = i + ":" + valueIds.join(',');
        }
        var cookie = refs.join(';');
        var expDate = new Date();
        expDate.setYear(expDate.getYear + 1);
        setCookie("References"+'<%=userName%>', cookie, expDate);
    } 
    
    function print() {
        
        saveReferences();
        if (isSelect == false) {
            alert('<fmt:message key="dialog.selectReport" />');
            return;
        }
        
        var url = '<%=contextPath%>';
        url = url + 'nameConfig='+reportId;
        url = url + '&exportFormat='+dataReports.reports[reportId].exportType;
        if(dataReports.reports[reportId].fileName) {
        	url = url + '&fileName='+dataReports.reports[reportId].fileName;
        }
        
        var fields = [];
        var kitId = dataReports.controls[reportId].root;
        getCurrentControls(fields, kitId, reportId);
        
        for (var i in fields) {
            
        	var require = fields[i].require;
        	var id = fields[i].id;
        	var label = fields[i].label;
            var type = fields[i].type;
            var value = getValuesFromControl(fields[i]);
            var allDiffersFromEmpty = fields[i].allDiffersFromEmpty;
            
            var group = fields[i].group;
        	if(group !== undefined) {
        		if (group != '') {
        			for(var i = 0; i < groups.length; i++)
        				if(groups[i].id == group && value != '') {
        					groups[i].value = true;
        					break;
        			}
        		}
        	}
            
            if(value != 'Cs_-1' && value != 'K_-1' && value != 'R_-1'){
            	if (value != '') {
            		if(fields[i].type=='DatePeriod'){
            			url = url + value;
            		}else{
                		url = url + '&'+id+'='+value;
            		}
            	} else if (require){
                		alert('<fmt:message key="dialog.missingParameter" /> "'+label+'"');
                		return;
            	}
            } else if(allDiffersFromEmpty == 'true'){
            			url = url + '&'+id+'='+value;
            	}
        }
        
        for(var i = 0; i < groups.length; i++){
			if(groups[i].value == false) {
				alert('Не заполнено ни одно из полей');
				return;
			}
			groups[i].value = false;
        }
        
        url=url+"&user='"+'<%=userName%>'+"'";
        window.open(
            url,
            '',
            'width=1000, height=500'
        )
    }
    
    function getCurrentControls(fields, kitId, reportId) {
        var kit = dataReports.controls[reportId].kits[kitId];
        for (var i in kit) {
            var control = kit[i];
            fields[fields.length] = {
                id: control.name,
                type: control.type,
                require: control.require,
                label: control.label,
                allDiffersFromEmpty: control.allDiffersFromEmpty,
                group: control.group,
                options: control.options
            };
            if (control.type == 'SelectKit' ) {
                var widget = dijit.byId(control.name);
                var refKitId = widget.value;    
                getCurrentControls(fields, refKitId, reportId);
            }else if(control.type == 'Radio'){
            	var refKitId = valuesRadioButton[control.name]
            	getCurrentControls(fields, refKitId, reportId);
            }
        }
    }
    
    function getValuesFromControl(field) {
    	var id =field.id;
    	var type =field.type;
        var value;
        if (type == 'String') {
            value = dojo.byId(id).value;
            if (value == '')
                return '';
            value = 'S_'+value;
        } else if (type == 'Long') {
            value = dojo.byId(id).value;
            if (value == '')
                return '';
            value = 'L_'+value;
        } else if(type == 'DatePeriod'){
        	var valueFrom = dijit.byId(field.options[0].id).valueNode.value;
        	var valueTo = dijit.byId(field.options[1].id).valueNode.value;
            if (valueFrom == '' || valueTo == '')
                return '';            
            value = '&'+field.options[0].id+"="+'D_'+valueFrom+'&'+field.options[1].id+"="+'D_'+valueTo;
        }else if (type == 'Date') {
            var control = dijit.byId(id);
            var input = dijit.byId(id).valueNode;
            value = input.value;
            if (value == '')
                return '';      
            value = 'D_'+value;
        } else if (type == 'Boolean') {
            value = dojo.byId(id).checked;
            if(dojo.byId(id).disabled){
            	value=false;
            }
            if (value == '')
                return '';
            value = 'B_'+value;
        } else if (type == 'SelectKit' || type == 'YearPeriod') {
        	var selectIds = [];
        	if (  choicesSelectKitControls[id]==null ||  choicesSelectKitControls[id] == "undefined"){
        		 return '';
        	}
            for (var i = 0; i < choicesSelectKitControls[id].length; i++) 
            {
                selectIds[selectIds.length] = choicesSelectKitControls[id][i].id;
            }
            value = selectIds.join(',');
            /*var widget =  dijit.byId(id);
            value = widget.value;*/
            if (value == '')
                return '';
            value = 'K_'+value;
        } else if (type == 'Persons') {
            var cardIds = [];
            for (var i=0; i < choicesPersonControls[id].length; i++) {
                cardIds[cardIds.length] = choicesPersonControls[id][i].cardId;
            }
            value = cardIds.join(',');
            if (value == '')
                return '';
            value = 'P_'+value;
        } else if (type == "ValuesRef") {
            var values = [];
            var table = dojo.byId('table_'+id);
            var model = table.model;
            for(var i in model) {
                if (true == model[i]) {
                    values.push(i);
                }
            }
            value = values.join(',');
            if (value == '')
                return '';
            value = 'R_'+value;
        } 
        else if (type == "ValuesRefList") 
        {
            var valueIds = [];
            for (var i = 0; i < chosenReferenceControls[id].length; i++) 
            {
                valueIds[valueIds.length] = chosenReferenceControls[id][i].id;
            }
            value = valueIds.join(',');
            if (value == '')
                return '';
            value = 'R_'+value;
        }
        else if (type == 'Card') {
            var widget =  dijit.byId(id);
            value = widget.value;
            if (value == '')
                return '';
            value = 'L_'+value;
        }else if (type == 'Cards') {
            var cardIds = [];
            for (var i=0; i < choicesCardControls[id].length; i++) {
                cardIds[cardIds.length] = choicesCardControls[id][i].cardId;
            }
            value = cardIds.join(',');
            if (value == '')
                return '';
            value = 'Cs_'+value;
        }else if(type=='Radio'){
        	value="Ra_"+valuesRadioButton[id];
        }
        
        return value;
    }
    // Код контрола выбора сотрудника
    function createPersonWidget(controlId) {
        var select = new dijit.form.FilteringSelect(
            {
                store: dataStore,
                searchAttr: 'label',
                pageSize: 15,
                searchDelay: 500,
                required: false,
                autoComplete: false,
                query: {
                    <%= BrowsingReportsSearchParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
                    <%= SearchCardServlet.PARAM_CALLER %>: '<%= BrowsingReportsSearchParameters.CALLER %>',
                    <%= SearchCardServlet.PARAM_IGNORE %>: ''
                },
                onChange: function() { personSelectChanged(controlId); }
            },
            dojo.byId(controlId)
        );
        
        choicesPersonControls[controlId] = [];
    }

    function createCardsWidget(controlId, template, query, dep_control, dep_attr, sqlxml, defValue) {
		if(dep_control){
			depControls.push(controlId);
		}
        var select = new dijit.form.FilteringSelect(
            {
                store: dataStore,
                searchAttr: 'label',
                pageSize: 15,
                searchDelay: 500,
                required: false,
                autoComplete: false,
                query: {
                    <%= BrowsingReportsSearchParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
                    <%= SearchCardServlet.PARAM_CALLER %>: '<%= BrowsingReportsSearchParameters.CALLER %>',
                    <%= SearchCardServlet.PARAM_IGNORE %>: '',
                    <%= BrowsingReportsSearchParameters.PARAM_TEMPLATE %>: template,
                    <%= BrowsingReportsSearchParameters.PARAM_QUERY %>: query,
                    <%= BrowsingReportsSearchParameters.PARAM_DEP_ATTR %>: dep_attr,
                    <%= BrowsingReportsSearchParameters.PARAM_DEP_VALUES %>: getDepValues(controlId),
                    <%= BrowsingReportsSearchParameters.PARAM_SQLXML %>: sqlxml
                },
                onChange: function() { cardSelectChanged(controlId, dep_control); }
            },
            dojo.byId(controlId)
        );
        select.dep_control = dep_control;
		select.parentAttr = dep_attr;
        choicesCardControls[controlId] = [];
        if (typeof defValue !== "undefined") {
        	choicesCardControls[controlId].push(defValue);
            refreshCardControls(controlId, dep_control);
        }
    }
    
    function getDepValues(controlId) {
    	for(var i = 0; i <cardQueryValue.length; i++){
    		if(cardQueryValue[i].control == controlId)
    			return cardQueryValue[i].depValues;
    	}
    	return '';
    }
    
    function personSelectChanged(controlId) {
        var widget = dijit.byId(controlId);
        if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
            var card = widget.item.i;
            var fio = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
            widget.attr('value', '');
            
            var persons = choicesPersonControls[controlId];
            persons[persons.length] = {cardId: card.cardId, name: fio};
            refreshControls(controlId);
        }
    }

    function cardSelectChanged(controlId, dep_control) {
        var widget = dijit.byId(controlId);
        if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
            var card = widget.item.i;
            var fio = card.columns.NAME;
            widget.attr('value', '');
            
            for(var i = 0; i < choicesCardControls[controlId].length; i++)
            	if(choicesCardControls[controlId][i].cardId == -1){
            		choicesCardControls[controlId]=[];
            		break;
            	}
            var cards = choicesCardControls[controlId];
            cards[cards.length] = {cardId: card.cardId, name: fio};
            refreshCardControls(controlId, dep_control);
			refreshDependencies();
        }
    }
	
	function refreshDependencies(){
		depControls.forEach(function(item){
			delimiter = '<%= Attribute.LABEL_ATTR_PARTS_SEPARATOR %>';
			childWidget = dijit.byId(item);
			
			linkPath  = [];
			linkPath.push(childWidget.parentAttr);
			
			parentWidget = dijit.byId(childWidget.dep_control);
			//собираем ссылки до первого заполненного родительского виджета
			//формируем путь из ссылок и получаем значения из первого заполненного родительского виджета
			while (parentWidget){
				if(getAnyDepValues(parentWidget.id)){//если нашли заполненный родительский виджет
					childWidget.query.<%= BrowsingReportsSearchParameters.PARAM_DEP_VALUES %> = getAnyDepValues(parentWidget.id);
					childWidget.query.<%= BrowsingReportsSearchParameters.PARAM_DEP_ATTR %> = linkPath.join(delimiter);
					parentWidget = false;
				} else {//если родительский виджет не заполнен
					linkPath.push(parentWidget.parentAttr);
					if(parentWidget.dep_control){//идем к следующему родителю
						parentWidget = dijit.byId(parentWidget.dep_control);
					} else {//больше родителей нет. ставим пустые зависимости
						parentWidget = false;
						childWidget.query.<%= BrowsingReportsSearchParameters.PARAM_DEP_VALUES %> = '';
						childWidget.query.<%= BrowsingReportsSearchParameters.PARAM_DEP_ATTR %> = '';
					}
				}
			}
		});
	}
	
	function getAnyDepValues(controlId){
		if(choicesCardControls[parentWidget.id]){
			var ids = [];
			choicesCardControls[parentWidget.id].forEach(
				function(item){	
					if(item.cardId != -1){
						ids.push(item.cardId);
					}
				}
			);
			return ids.join(',');
		} else if(choicesSelectKitControls[parentWidget.id]){
			var ids = [];
			choicesSelectKitControls[parentWidget.id].forEach(
				function(item){	
					ids.push(item.depValue);
				}
			);
			return ids.join(',');
		}
		return false;
	}
    
    function refreshControls(controlId) {
        var persons = choicesPersonControls[controlId];
        fillTable(
            persons,
            controlId, 
            'personTable_'+controlId
        );      
        
        // говорим выподающему списку каких пользователей уже не надо выводить
        var select = dijit.byId(controlId);
        var empIds = [];
        for (var i = 0; i < persons.length; i++) {
            empIds[empIds.length] = persons[i].cardId;
        }
        select.query.<%= SearchCardServlet.PARAM_IGNORE %> = empIds.join(',');      
    }

    function refreshCardControls(controlId, dep_control) {
        var cards = choicesCardControls[controlId];
        fillCardTable(
            cards,
            controlId, 
            'cardTable_'+controlId,
            dep_control
        );      
        
        // говорим выподающему списку каких пользователей уже не надо выводить
        var select = dijit.byId(controlId);
        var empIds = [];
        for (var i = 0; i < cards.length; i++) {
            empIds[empIds.length] = cards[i].cardId;
        }
        select.query.<%= SearchCardServlet.PARAM_IGNORE %> = empIds.join(',');      
    }
    
    function fillTable(peoples, controlId, tableId) {
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
            cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="deletLineFromTeble(\''+ controlId +'\','+ peoples[i].cardId +')">&nbsp;</a>';
        }
    }

    function fillCardTable(cards, controlId, tableId, dep_control) {
        var table = dojo.byId(tableId);
        if (table.rows) {
            for (var i = table.rows.length - 1; i >= 0; --i) {
                table.deleteRow(i);
            }
        }
        for (var i = 0; i < cards.length; ++i) {
            var row = table.insertRow(i);
            var cell = null;
            cell = row.insertCell(0);
            cell.innerHTML = cards[i].name;
            cell = row.insertCell(1);
            cell.style.textAlign = 'center';
            cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="deletLineFromCardTeble(\''+ controlId +'\','+ cards[i].cardId +',\''+dep_control+'\')">&nbsp;</a>';
        }
    }
    
    function deletLineFromTeble(controlId, cardId) {
        for (var i = 0; i < choicesPersonControls[controlId].length; ++i) {
            if (choicesPersonControls[controlId][i].cardId == cardId) {
                choicesPersonControls[controlId].splice(i, 1);
            }
        }
        refreshControls(controlId);
    }

    function deletLineFromCardTeble(controlId, cardId, dep_control) {
        for (var i = 0; i < choicesCardControls[controlId].length; ++i) {
            if (choicesCardControls[controlId][i].cardId == cardId) {
                choicesCardControls[controlId].splice(i, 1);
            }
        }
        var cards = choicesCardControls[controlId];
        refreshCardControls(controlId, dep_control);
        refreshDependencies();
    }
    
    function changeSelectKit(kit, name, multiValued, dep_control) {
        var widget = dijit.byId(name);
        if(multiValued==false && (widget.item==null || widget.item == "undefined")){
        	choicesSelectKitControls[name]="undefined";
        	return;
        }
        updateAdditionalControls(kit, widget.item.id); 
        if(multiValued==true) {
        	for(var i = 0; i < choicesSelectKitControls[name].length; i++){
        		if (choicesSelectKitControls[name][i].id==-1) {
                	choicesSelectKitControls[name]=[];
                	break;
            	}
        		if (choicesSelectKitControls[name][i].id==widget.item.id) {
        			widget.attr('value', '');
                	return;
            	}
        	}
            choicesSelectKitControls[name].push(widget.item);
            fillSelectKitTable(name, dep_control);
                        
            var indexFlag = false;
            for(var i in ignoredSelectKitControls[name]) {
            	if(ignoredSelectKitControls[name][i][0] == widget.item.id) {
            		indexFlag = true;
            		break;            		
            	}
            }
            
            if(!indexFlag) {
            	ignoredSelectKitControls[name].push(widget.item.id);
            	widget.store = changeOptionalsKit(name);
            }
            
            widget.attr('value', '');
            	            
        } else {
            choicesSelectKitControls[name]=[];
            choicesSelectKitControls[name].push(widget.item);
        }
       refreshDependencies();
    }
    
    function fillSelectKitTable(name, dep_control)
    {
        var cards = choicesSelectKitControls[name];
        clearTable(dojo.byId('selectKitTable_'+name));
        var table = dojo.byId('selectKitTable_'+name);
        for (var i = 0; i < cards.length; ++i) {
            var row = table.insertRow(i);
            var cell = null;
            cell = row.insertCell(0);
            cell.innerHTML = cards[i].name;
            cell = row.insertCell(1);
            cell.style.textAlign = 'center';
            cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="deletLineFromSelectKitTable(\''+ name +'\',\''+ cards[i].id +'\',\''+ dep_control +'\')">&nbsp;</a>';
        }
   }
    
    function deletLineFromSelectKitTable(controlId, id, dep_control) {
        for (var i = 0; i < choicesSelectKitControls[controlId].length; ++i) {
            if (choicesSelectKitControls[controlId][i].id == id) {
                choicesSelectKitControls[controlId].splice(i, 1);
            }
        }
        
        for(var i in ignoredSelectKitControls[controlId]) {
        	if(ignoredSelectKitControls[controlId][i][0] == id) {
        		ignoredSelectKitControls[controlId].splice(i, 1);
        		var widget = dijit.byId(controlId);
            	widget.store = changeOptionalsKit(controlId);
        	}
        }
        
        fillSelectKitTable(controlId);
        setDepValues(controlId, dep_control);
        if (choicesSelectKitControls[controlId].length==0) 
        	allSelectKitRequire=true;     
    }

    function removeAllSelectKits(controlId, dep_control)
    {
        choicesSelectKitControls[controlId]=[];
        
        if(ignoredSelectKitControls[controlId].length > 0) {
        	ignoredSelectKitControls[controlId]=[];
        	var widget = dijit.byId(controlId);
        	widget.store = changeOptionalsKit(controlId);
        }
        
        clearTable(dojo.byId('selectKitTable_'+controlId));
        allSelectKitRequire=true;
        refreshDependencies();
    }

    function addAllSelectKits(name, dep_control, allGroups)
    {
    	allSelectKitRequire=false;
    	clearTable(dojo.byId('selectKitTable_'+name));
		choicesSelectKitControls[name] = [];
		choicesSelectKitControls[name][0] = {id: -1};
		
		if(ignoredSelectKitControls[name].length > 0) {
        	ignoredSelectKitControls[name]=[];
        	var widget = dijit.byId(name);
        	widget.store = changeOptionalsKit(name);
        }
		
		refreshDependencies();
        var table = dojo.byId('selectKitTable_'+name);
        var row = table.insertRow(0);
        var cell = null;
        cell = row.insertCell(0);
        if (allGroups !="undefined") {
            cell.innerHTML = allGroups;
        } else {
            cell.innerHTML = 'Все';
        }

        cell = row.insertCell(1);
        cell.style.textAlign = 'center';
        cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="removeAllSelectKits(\''+ name +'\', \'' + dep_control + '\')">&nbsp;</a>';
    }

    function removeAllCards(controlId, dep_control)
    {
        choicesCardControls[controlId]=[];
        refreshCardControls(controlId, dep_control);
        //clearTable(dojo.byId('cardTable_'+controlId));
        refreshDependencies();
        
        /*if(dep_control != null && dep_control != "undefined") {    		
    		var kit = dataReports.controls[reportId].kits[dataReports.controls[reportId].root];
    		for (var i in kit) {
    			var control = kit[i];
    			if(control.name == dep_control){
    				if(control.dep_control != null && control.dep_control != "undefined") {
    					removeAllCards(dep_control, control.dep_control);
    				}
    				break;
    			}
    		}
    	}*/
    }
    
    function addAllCards(name, dep_control, allGroups, allSelected)
    {
        //clearTable(dojo.byId('cardTable_'+name));        
        choicesCardControls[name]=[];
        refreshCardControls(name, dep_control);
        var cards = choicesCardControls[name];
        cards[cards.length] = {cardId: -1};
        var table = dojo.byId('cardTable_'+name);
        var row = table.insertRow(0);
        var cell = null;
        cell = row.insertCell(0);
    	if (allGroups != "undefined") {
    		cell.innerHTML = allGroups;
    	} 
    	else if(allSelected != null) {
        	cell.innerHTML = allSelected;
        } else {
        	cell.innerHTML = 'Все';
        }
        cell = row.insertCell(1);
        cell.style.textAlign = 'center';
        cell.innerHTML = '<a href="javascript:void(0)" class="delete" onclick="removeAllCards(\''+ name +'\', \'' + dep_control + '\')">&nbsp;</a>';
        
        refreshDependencies();
    
    }
    
    function addAllDepCards(dep_control) {
    	if(dep_control != null && dep_control != "undefined") {    		
    		var kit = dataReports.controls[reportId].kits[dataReports.controls[reportId].root];
    		for (var i in kit) {
    			var control = kit[i];
    			if(control.name == dep_control){
    				if(control.dep_control != null && control.dep_control != "undefined") {
    		        	var depWidget = dijit.byId(control.dep_control);
    		        	if(depWidget != null && depWidget != "undefined") {        		
    		        		depWidget.query.<%= BrowsingReportsSearchParameters.PARAM_DEP_VALUES %> = '';
    		        		addAllDepCards(control.dep_control);
    		        	}  	
    				}
    				break;
    			}
    		}
    	}
    }
</script>