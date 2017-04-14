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
<%@page import="com.aplana.dbmi.portlet.RequestToChangeConsPortlet"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.ajax.RequestToChangeSearchPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.portlet.RequestToChangeConsPortletSessionBean"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>

<portlet:defineObjects/>

<%
	RequestToChangeConsPortletSessionBean sessionBean = 
		(RequestToChangeConsPortletSessionBean) renderRequest.getPortletSession().
		getAttribute(RequestToChangeConsPortlet.SESSION_BEAN);
	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());
%>

<c:set var="sessionBean" value="<%=sessionBean%>"/>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>

<script type="text/javascript" language="javascript">
	function dbmiHideLoadingSplash() {
		dojo.byId('dbmiLoadingSplash').style.display = 'none'; 
		dojo.byId('layoutBC').style.display = 'block';
	}
	dojo.addOnLoad(dbmiHideLoadingSplash);
	function dbmiShowLoadingSplash() {
		dojo.byId('dbmiLoadingSplash').style.display = 'block'; 
		dojo.byId('layoutBC').style.display = 'none';
	}

</script>
    
<script type="text/javascript">
	var <portlet:namespace/>_message = ${requestScope.message};
	var <portlet:namespace/>_resolutionAttachments = ${requestScope.resolutionAttachments};

	var entityMap = {
		    "&": "&amp;",
		    "<": "&lt;",
		    ">": "&gt;",
		    '"': '&quot;',
		    "'": '&#39;',
		    "/": '&#x2F;'
		  };
	
	function escapeHtml(string) {
		return String(string).replace(/[&<>"'\/]/g, function (s) {
			return entityMap[s];
		});
	}
	
	dojo.require("dijit.form.Button");
	dojo.require("dijit.form.RadioButton");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dijit.form.FilteringSelect");	
	dojo.require("dijit.form.DateTextBox");
	dojo.require("dijit.form.CheckBox");
	dojo.require("dojo.date");
	dojo.require("dojo.date.stamp");
	dojo.require("dijit.layout.BorderContainer");
	dojo.require("dijit.layout.TabContainer");
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dojo.parser");

	function <portlet:namespace/>_submitForm(action) {
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		if ((action == '<%= RequestToChangeConsPortlet.ACTION_DONE %>') 
		&& !isFutureDay('<portlet:namespace/>_term')) {
			alert('<fmt:message key="message.termNotFuture"/>');
		} else {		
			if(action == '<%= RequestToChangeConsPortlet.ACTION_DONE %>'){
				<%-- getSignature(); --%>
			}
			form.submit();
			dbmiShowLoadingSplash();
			return false;
		}
	}	

	function <portlet:namespace/>_setFocus(idDiv) {
		var textarea = document.getElementById('<portlet:namespace/>_comment');
		var div = dojo.byId(idDiv);
		var text = div.textContent || div.innerText;
		if (textarea.value == '') {
			textarea.value = text;
		} else {
			textarea.value = textarea.value + '\n' + text;
		}
	}
	
	var <portlet:namespace/>_oldTermControl = null;
	var <portlet:namespace/>_oldTermChange = null;
	var <portlet:namespace/>_oldPreliminaryTerm = null;
	
	function <portlet:namespace/>_validDate(inputDate, oldDate, nameInput) {
  		if (!inputDate.isValid(true)) {
	  		inputDate.attr('value', oldDate);
	  	} else {
	  		var value = inputDate.attr('value');
	  		if (oldDate != null) {
		  		if (value != null) {
			  		oldDate.setFullYear(value.getFullYear());
			  		oldDate.setMonth(value.getMonth());
			  		oldDate.setDate(value.getDate());
		  		} else {
		  			eval('<portlet:namespace/>_'+nameInput+' = null;');
		  		}
	  		} else {
	  			if (value != null) {
	  				eval('<portlet:namespace/>_'+nameInput+' = new Date();');
  					eval('<portlet:namespace/>_'+nameInput+'.setFullYear(value.getFullYear());');
	  				eval('<portlet:namespace/>_'+nameInput+'.setMonth(value.getMonth());');
	  				eval('<portlet:namespace/>_'+nameInput+'.setDate(value.getDate());');
	  			}
	  		}
	  	}
	}

	dojo.addOnLoad(function() {
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
                style: "width: 50px; height: 20px;",
				query: {
					<%= RequestToChangeSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= RequestToChangeSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: ''
				},
				onChange: function() { <portlet:namespace/>_controllerSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_controllerSelect')
		);
		if (<portlet:namespace/>_message != '') {
			<portlet:namespace/>_showMessage();
			<portlet:namespace/>_message='';
		}
	});
	function <portlet:namespace/>_controllerSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_controllerSelect');
		var hidden = dojo.byId('<portlet:namespace/>_controller');
		var show = dojo.byId('<portlet:namespace/>_nameController');
		
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			
			show.textContent = name;
			hidden.value = card.cardId+':'+name;
		}
	}
	
	function <portlet:namespace/>_clearEditor() {
		var editor = dojo.byId('<portlet:namespace/>_comment');
		editor.value = '';
	}
	
	<%-- function getSignature(){
		try{
			var signValue = dojo.byId("<portlet:namespace/>_comment").value;
					
			var fieldsToSign= [{
				id: "T:<%=RequestToChangeConsPortlet.ATTR_RESOLUT.getId().toString()%>",								
				value: signValue
			}];
			
			var args = {
				noDecode: true,
				fields: fieldsToSign,	
				storeName: "<%= com.aplana.dbmi.crypto.SignatureData.PARAM_CERTSTORE %>",
				userName: "<%= renderRequest.getRemoteUser()%>"
			};
													
			var signResult = cryptoGetSignature(args);
			if(signResult.success){			
				dojo.byId("signature").value = signResult.signature;				
			}	
						
		}catch(ex){		
			alert('<fmt:message key="label.control"/>' + ex)
		}
	} --%>
	
	var dates = {
		    convert:function(d) {
		        return (
		            d.constructor === Date ? d :
		            d.constructor === Array ? new Date(d[0],d[1],d[2]) :
		            d.constructor === Number ? new Date(d) :
		            d.constructor === String ? new Date(d) :
		            typeof d === "object" ? new Date(d.year,d.month,d.date) :
		            NaN
		        );
		    },
		    compare:function(a,b) {
		        return (
		            isFinite(a=this.convert(a).valueOf()) &&
		            isFinite(b=this.convert(b).valueOf()) ?
		            (a>b)-(a<b) :
		            NaN
		        );
		    },
		    inRange:function(d,start,end) {
		       return (
		            isFinite(d=this.convert(d).valueOf()) &&
		            isFinite(start=this.convert(start).valueOf()) &&
		            isFinite(end=this.convert(end).valueOf()) ?
		            start <= d && d <= end :
		            NaN
		        );
		    }
		}
	
	function isFutureDay(idInput) {
		var input = dijit.byId(idInput);
		var value = input.attr('value');

		var now = new Date();
		now = new Date();
		now.setHours(0);
		now.setMinutes(0,0,0);
		now.setDate(now.getDate());
		now.setMonth(now.getMonth());

		return value == null || dates.compare(value, now) != -1; /* value > yesterday */;
	}

	function <portlet:namespace/>_showMessage() {
		alert(<portlet:namespace/>_message);
	}

	function <portlet:namespace/>_uploadAttachment() {
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		var filePath = filePathInput.value;
		if(filePath == "") return;
		dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', true);    
		
		
		var uploadUrl = self.location.protocol + "//" + self.location.host + '<%=RequestToChangeConsPortlet.MATERIAL_UPLOAD_URL %>';

		var postResponse = document.applets[0].
								postMaterialCard(uploadUrl, document.cookie, filePath, "", null);
		
		if(!postResponse) {
			alert('<fmt:message key="error.material_download"/>');
			return;
		}

		var attachmentData = eval( "(" + postResponse + ")" );
		<portlet:namespace/>_addAttachment(dojo.byId('<portlet:namespace/>_resolutionAttachmentTable'), 
				attachmentData.id, attachmentData.name);

		filePathInput.value = '';
	}

	function isCardAttached(attachmentName) {
		for (var i = 0; i < <portlet:namespace/>_resolutionAttachments.length; ++i) {
			if (<portlet:namespace/>_resolutionAttachments[i].name == attachmentName) {
				return true;
			}
		}
		return false;
	}

	function <portlet:namespace/>_addAttachment(attachmentsTable, attachmentId, attachmentName){
		if (isCardAttached(attachmentName))
			return;//given card has been attached...skip this action  
		var i = <portlet:namespace/>_resolutionAttachments.length;
		<portlet:namespace/>_resolutionAttachments[i] = {cardId: attachmentId, name: attachmentName};
		<portlet:namespace/>_refreshControls();
	}

	function <portlet:namespace/>_selectFile() {
        var fileName = document.applets[0].getFileName();
        dojo.byId('<portlet:namespace/>_filePath').value = fileName;
        var uploadButtonDisabled = true;
        if(fileName && fileName.length > 0) {
        	uploadButtonDisabled = false;
        }
        dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', uploadButtonDisabled);
  	}

	function <portlet:namespace/>_removeAttachment(id) {
		<portlet:namespace/>_removeCard(id, <portlet:namespace/>_resolutionAttachments);
	}

	function <portlet:namespace/>_removeCard(id, cardsArray) {
		for (var i = 0; i < cardsArray.length; ++i) {
			if (cardsArray[i].cardId == id) {
				cardsArray.splice(i, 1);
			}
		}	
		<portlet:namespace/>_refreshControls();
	}
	
</script>
 	
<div id="layoutBC" persist="true" style="width: 100%">
	<div id="dbmiLoadingSplash" style="display: block;  height: 220px;" >
		<div style="width:100%; height:100%; background-color: #ffffff;">
			<br />
			<br />
			<p style="text-align: center;"><img
				src="<%=request.getContextPath() + "/js/dbmi_loading.gif"%>"
				border="0" alt="" />
			</p>
		</div>
	</div>
</div>


<c:set var="headerIcon" value="header_icon item13"/>

<%@include file="../docTitleTopHeader.jspf"%>
<div id="DIV_DOC_MAIN" style="margin-right:10px; overflow-y:auto">
<%--include file="../baseCardDataSection.jspf"--%>


<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">
	<input id="<portlet:namespace/>_consideratorsField" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_CONSIDERATORS %>" value=""/>
	<input id="<portlet:namespace/>_newsField" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_NEW_CONSIDERATORS %>" value=""/>
	<input id="<portlet:namespace/>_requestTypesField" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_REQUEST_TYPE %>" value=""/>
	<input id="<portlet:namespace/>_responsibleField" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_RESPONSIBLE %>" value=""/>
	<input id="<portlet:namespace/>_resolutionAttachmentField" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_FILE_ATTACHMENT %>" value=""/>
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_ACTION %>" value=""/>

	<input id="<portlet:namespace/>_isCurrentUserConsiderator" type="hidden" name="<%= RequestToChangeConsPortlet.FIELD_ACTION %>" value=""/>

	<div class="resolution">
		
		<hr/>
		
		<div class="left_column">
			<textarea name="<%=RequestToChangeConsPortlet.FIELD_COMMENT%>" class="resolution_text" placeholder="<fmt:message key="label.textResolution"/>" id="<portlet:namespace/>_comment"><c:out value="${sessionBean.comment}"/></textarea>
            <div class="clear_resolution" >
				<a onclick="<portlet:namespace/>_clearEditor(); return false;"  href="#" /></a>
			</div>
        </div>

		<div class="center_column-720">
			<div class="block">
				<div class="select">
            		<label class="no-radio-100" for="<portlet:namespace/>_requestTypes"><fmt:message key="label.requestType"/></label>
               		<select id="<portlet:namespace/>_requestTypes"/>
            	</div>
            	<div class="list">
					<table id="<portlet:namespace/>_requestTypesTable" class="content">
					</table>
                </div>
			</div>
			<br>
            <div class="block">
            	<div class="select">
                	<label class="no-radio-100" for="<portlet:namespace/>_considerators"><fmt:message key="label.considerator"/></label>
					<select id="<portlet:namespace/>_considerators"/>
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_consideratorsTable" class="content">
					</table>
                </div>
            </div>
			<br>
            <div class="block">
            	<div class="select">
                	<label class="no-radio-100" for="<portlet:namespace/>_newSelect"><fmt:message key="label.newCons"/></label>
					<select id="<portlet:namespace/>_newSelect"/>
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_newTable" class="content">
					</table>
                </div>
            </div>
            <br>
		</div>
		
		<div class="right_column">
		</div>
		
		<hr />

    	<div class="left_column">
			<div class="time_frames">
                <div id="date_change" class="date_select">
                    <label><fmt:message key="label.change.term"/></label>
						<input dojoType="dijit.form.DateTextBox" type="text" id="<portlet:namespace/>_term_change" disabled="true"
							name="<%=RequestToChangeConsPortlet.FIELD_TERM_CHANGE%>" style = "width: 138px; margin: 0; padding: 0; "
							onBlur="<portlet:namespace/>_validDate(dijit.byId('<portlet:namespace/>_term_change'), 
							 <portlet:namespace/>_oldTermChange, 'oldTermChange')" value=""/>
                </div>
            </div>
        </div>
        
        <div class="center_column-720">
		</div>
        
        <div class="right_column">
		</div>
		
		<hr />
		
		<div class="left_column">
			<div class="upload NoAppletHide">
				<label style="width: 140px; margin: 0px"><fmt:message key="label.uploadAttachment"/>:</label>
				<br />
				<input readonly style="width: 160px; height: 22px;  clear: both; margin-top: 18px" type="text" id="<portlet:namespace/>_filePath" />

				<button dojoType="dijit.form.Button" type="button" style="height: 20px; margin-top: -8px">
					<fmt:message key="button.browse"/>
					<script type="dojo/method" event="onClick" args="evt">
						<portlet:namespace/>_selectFile();
					</script>
				</button>

				<button dojoType="dijit.form.Button" style="height: 20px; margin-top: -8px" 
						type="button" disabled id="<portlet:namespace/>_uploadButton">
					<fmt:message key="button.upload_attachment"/>
					<script type="dojo/method" event="onClick" args="evt">	
						<portlet:namespace/>_uploadAttachment();
					</script>
				</button>
				
				<table id="<portlet:namespace/>_resolutionAttachmentTable"> 
				</table>
			</div>
		</div>
		
		<div class="center_column-720">
		</div>
		
		<div class="right_column">
		</div>
		
        <hr style="background: white;" />
        
		<!-- begin ЭЦП -->
		<input id="signature" name="<%=RequestToChangeConsPortlet.FIELD_SIGNATURE%>" type="hidden" value="" />
		<applet 
			name="CryptoApplet"
			id="CryptoApplet"
			codebase="<%=request.getContextPath()%>"
			archive="SJBCrypto.jar" 
			code="com.aplana.crypto.CryptoApplet.class" 
			WIDTH= 1 
			HEIGHT=1>
			<param name="crypto.layer" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER)%>">
			<param name="crypto.layer.params" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS)%>">
			<param name="timestamp.address" value="<%=CryptoLayer.getConfigParam(CryptoLayer.CLIENT_TIMESTAMP_ADDRESS)%>">		
			<PARAM name="separate_jvm" value="true">
			<H1><fmt:message key="msg.warning"/></H1><fmt:message key="msg.cannot_load_applet"/>
			
		</applet>
		<!-- end ЭЦП --> 
       
	</div><!-- .resolution end -->
</form>
</div>
<div class="resolution">
	<div class="controls">
            <div class="resolution_buttons">
            	<div class="cancel_resolution" >
					<a href="#"
						onclick="<portlet:namespace/>_submitForm('<%= RequestToChangeConsPortlet.ACTION_CANCEL %>')"></a>
					</div>	
                <div class="submit_resolution" >
					<a  href="#"
						onclick="<portlet:namespace/>_submitForm('<%= RequestToChangeConsPortlet.ACTION_DONE %>')"></a>
                </div>
            </div>

    </div><!-- .controls end -->
</div>
<script type="text/javascript">
	var <portlet:namespace/>_news = [];
	
	var <portlet:namespace/>_message = ${requestScope.message};
	
	var <portlet:namespace/>_resolutionAttachments = ${requestScope.resolutionAttachments};

	dojo.require("dijit.form.Button");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dojo.data.ItemFileWriteStore");
	dojo.require("dijit.form.FilteringSelect");
	dojo.require("dijit.form.DateTextBox");
	dojo.require("dojo.parser");
	
	dojo.addOnLoad(function() {
		
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
				disabled: true,
				style: "width: 160px; height: 21px;",
				query: {
					<%= RequestToChangeSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= RequestToChangeSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: '',
					<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
				},
				onChange: function() { <portlet:namespace/>_newSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_newSelect')
		);
		
		var typesStore = new dojo.data.ItemFileWriteStore(
				{data: getOptionalsType(<%=sessionBean.getJsonTypes()%>)}
			);
		
		var requestType = new dijit.form.FilteringSelect(
				{
					store: typesStore,
					required: false,
					style: "width: 160px; height: 21px;",
					onChange: function() { <portlet:namespace/>_requestTypeChanged(); }
				},
				dojo.byId('<portlet:namespace/>_requestTypes')
			);
		
		var consideratorsStore = new dojo.data.ItemFileWriteStore(
				{data: getOptionalsType(<%=sessionBean.getJsonConsiderators()%>)}
		);

		var considerators = new dijit.form.FilteringSelect(
				{
					store: consideratorsStore,
					required: false,
					style: "width: 160px; height: 21px;",
					onChange: function() { <portlet:namespace/>_consideratorsChanged(); }
				},
				dojo.byId('<portlet:namespace/>_considerators')
		);

		var widget_considerator_flag = dojo.byId('<portlet:namespace/>_isCurrentUserConsiderator');
		widget_considerator_flag.value = '<%=sessionBean.getCurrentConsidDoc()%>';

		respStore = new dojo.data.ItemFileWriteStore(
                {data: getOptionalsType(<%=sessionBean.getJsonResp()%>)}
            );
		
		optionals = getOptionalsType(<%=sessionBean.getJsonResp()%>)
		
		separator = '<%=RequestToChangeConsPortlet.SEPARATOR%>';
		
		counter = 0;
				
		<portlet:namespace/>_refreshControls();
		
		if (<portlet:namespace/>_message != '') {
			<portlet:namespace/>_showMessage();
		}
		
		<%-- dijit.byId('<portlet:namespace/>_newSelect').set('disabled', true);
		dojo.connect(dijit.byId('<portlet:namespace/>_term_control'), "onKeyPress", function(evt){
	        dojo.stopEvent(evt);
	    });
		dijit.byId('<portlet:namespace/>_respUser').set('disabled', true);
		dojo.connect(dijit.byId('<portlet:namespace/>_term_change'), "onKeyPress", function(evt){
	        dojo.stopEvent(evt);
	    });  --%>
	});
	
	function addRespUserBox(widgetId) {
		var widget = dijit.byId('<portlet:namespace/>_respUser' + widgetId);
	    if(widget) {
	    	widget.destroy();
	    }
		var respUser = new dijit.form.FilteringSelect(
				{
					store: respStore,
					required: false,
	                style: "width: 160px; height: 21px;",
					onChange: function() { <portlet:namespace/>_respChanged(widgetId); }
				},
				dojo.byId('<portlet:namespace/>_respUser' + widgetId)
			);
	}
	
	function addDateTextBox(widgetId) {
		var widget = dijit.byId('<portlet:namespace/>_select_date' + widgetId);
	    if(widget) {
	    	widget.destroy();
	    }
		var dt = new dijit.form.DateTextBox(
	  			{ 
	      			onBlur: function() { <portlet:namespace/>_validDate(dijit.byId('<portlet:namespace/>_select_date' + widgetId), '<portlet:namespace/>_oldSelectDate' + widgetId, 'oldSelectDate' + widgetId); },
	       			name:  '<portlet:namespace/>_select_date' + widgetId,
	      			style:'width: 138px',
	      			onChange: function() { <portlet:namespace/>_termChanged(widgetId); }
	      		},
	      		dojo.byId('<portlet:namespace/>_select_date' + widgetId)
	      	); 
	}
	
	function <portlet:namespace/>_validDate(inputDate, oldDate, nameInput) {
  		if (!inputDate.isValid(true)) {
	  		inputDate.attr('value', oldDate);
	  	} else {
	  		var value = inputDate.attr('value');
	  		if (oldDate != null) {
		  		if (value != null) {
			  		oldDate.setFullYear(value.getFullYear());
			  		oldDate.setMonth(value.getMonth());
			  		oldDate.setDate(value.getDate());
		  		} else {
		  			eval('<portlet:namespace/>_'+nameInput+' = null;');
		  		}
	  		} else {
	  			if (value != null) {
	  				eval('<portlet:namespace/>_'+nameInput+' = new Date();');
  					eval('<portlet:namespace/>_'+nameInput+'.setFullYear(value.getFullYear());');
	  				eval('<portlet:namespace/>_'+nameInput+'.setMonth(value.getMonth());');
	  				eval('<portlet:namespace/>_'+nameInput+'.setDate(value.getDate());');
	  			}
	  		}
	  	}
	}
	
	function getOptionalsType(types) {
		var store = {};
        store.identifier = 'id';
        store.label = 'name';
        store.items = [];
        for (var i in types) {
            var item = {};
            item[store.identifier] = types[i].id;
            item[store.label] = types[i].label;
            store.items[store.items.length] = item;
        }
        return store;
    }
	
	function <portlet:namespace/>_refreshControls(flag, name) {
		// вызываем код обновления таблиц
		
		<portlet:namespace/>_fillTable(
			<portlet:namespace/>_news,
			'<portlet:namespace/>_newTable', 
			'<portlet:namespace/>_newsField',
			flag,
			name
		);

		<portlet:namespace/>_fillTableAttach(
				<portlet:namespace/>_resolutionAttachments,
				'<portlet:namespace/>_resolutionAttachmentTable',
				'<portlet:namespace/>_resolutionAttachmentField',
				false
			);
		
		// говорим выподающему списку каких пользователей уже не надо выводить
		var allIds = [];
		for (var i = 0; i < <portlet:namespace/>_news.length; ++i) {
			allIds.push(<portlet:namespace/>_news[i].cardId);
		}
		
		var select = dijit.byId('<portlet:namespace/>_newSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');

		// установка значений hidden-ов
		var value = "";
		for(var i=0; i < <portlet:namespace/>_news.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_news[i].cardId
				  	 +":"+<portlet:namespace/>_news[i].resp+":"+<portlet:namespace/>_news[i].dt;
		}
		dojo.byId('<portlet:namespace/>_newsField').value = value;

		value = "";

		var id_delimiter = '<%=RequestToChangeConsPortlet.ID_DELIMITER%>';
		value = "";
		for(var i=0; i < <portlet:namespace/>_resolutionAttachments.length; i++) {
			if (i > 0) {
				value += separator;
			}
			value += <portlet:namespace/>_resolutionAttachments[i].cardId + id_delimiter + <portlet:namespace/>_resolutionAttachments[i].name;
		}
		dojo.byId('<portlet:namespace/>_resolutionAttachmentField').value = value;
		
	}
	
	
	function <portlet:namespace/>_fillTable(records, tableId, hiddenId, isAdd, name) {
		var table = dojo.byId(tableId);
		if(!isAdd) {
			if (table.rows) {
				var trList = table.getElementsByTagName('tr');
				for (var i = 0; i < trList.length; i++) {
					var tdList = trList[i].getElementsByTagName('td');
					if(tdList[1].innerHTML == name) {
						table.deleteRow(i);
						break;
					}
				}
			}
		} else {
		
		for(var i = 0; i < records.length; i++) {
			if(records[i].name == name) {
				//var i = records.length;
				var row = table.insertRow(table.rows.length);
				
				var cell = null;
				cell = row.insertCell(0);
				cell.style.textAlign = 'center';
		
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_deselectPersonCard(' + records[i].cardId + ',\''+escapeHtml(records[i].name)+'\'); return false;">&nbsp;</a>';
		
				cell = row.insertCell(1);
				cell.innerHTML = records[i].name;
		
				cell = row.insertCell(2);
				
				var widgetId = counter++;
				records[i].widget = widgetId;
		
				var wdg = 
				'<div class="select">' +
        			'<label for="<portlet:namespace/>_respUser' + widgetId + '"><fmt:message key="label.responsible" /></label>' +
           			'<select id="<portlet:namespace/>_respUser' + widgetId + '"/>' +
        		'</div>';
        		cell.innerHTML = wdg;
        		
        		addRespUserBox(widgetId);
        
        		cell = row.insertCell(3);
        		cell.innerHTML =
        		'<div class="time_frames>' +
        			'<div id="date_term' + widgetId + '" class="date_select">' +
        				'<label><fmt:message key="label.term"/></label>' +
        				'<input type="text" value="' + records[i].dt + '" id="<portlet:namespace/>_select_date' + widgetId + '"/>' +
            		'</div>' +
        		'</div>';
        
        		addDateTextBox(widgetId);
			}
        
		}
        
		}
		
	}
	
	function <portlet:namespace/>_fillTableAttach(records, tableId, hiddenId) {
		var table = dojo.byId(tableId);
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= 0; --i) {
				table.deleteRow(i);
			}
		}
		for (var i = 0; i < records.length; ++i) {
			var row = table.insertRow(i);
			var cell = null;
			cell = row.insertCell(0);
			cell.style.textAlign = 'center';
			if('<portlet:namespace/>_resolutionAttachmentTable' == tableId) {
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_removeAttachment(' + records[i].cardId + '); return false;">&nbsp;</a>';
			} else {
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_deselectPersonCard(' + records[i].cardId + ',\''+escapeHtml(records[i].name)+'\'); return false;">&nbsp;</a>';
			}
			
			cell = row.insertCell(1);
			cell.innerHTML = records[i].name;
			
		}
	}
	
	function <portlet:namespace/>_selectPersonCard(cardId, name) {
		var hrefPerson = dojo.byId('<portlet:namespace/>_immediate_men_'+ cardId);
		if (hrefPerson) {
			hrefPerson.setAttribute('class', 'inactive');
			hrefPerson.onclick=function() {
				<portlet:namespace/>_deselectPersonCard(cardId, name);
			};
			
		}
		addResponsiblePersonCard(<portlet:namespace/>_news, cardId, name);
		<portlet:namespace/>_refreshControls(true, name);
	}
	
	function addResponsiblePersonCard(field_array, cardId, name) {

		if (field_array.cardId) {
			//firstly deselect current href element  
			var hrefPerson = dojo.byId('<portlet:namespace/>_immediate_men_'+ field_array.cardId);
			var storeCardId =  field_array.cardId;
			var storeCardName =  field_array.name;
			if (hrefPerson) {
				hrefPerson.setAttribute('class', 'dotted');
				hrefPerson.onclick=function() {
					<portlet:namespace/>_selectPersonCard(storeCardId, storeCardName);
				};
				
			}	
		}

		var i = field_array.length;
		field_array[i] = {cardId: cardId, name: name, resp: '', dt: '', widget: -1};
	}	
	
	function <portlet:namespace/>_deselectPersonCard(cardId, name) {
		var hrefPerson = dojo.byId('<portlet:namespace/>_immediate_men_'+ cardId);
		if ( hrefPerson) {
			hrefPerson.setAttribute('class', 'dotted');
			hrefPerson.onclick=function() {
				<portlet:namespace/>_selectPersonCard(cardId, name);
			};
			
		}
		var person;
		for (var i = 0; i < <portlet:namespace/>_news.length; ++i) {
			if (<portlet:namespace/>_news[i].cardId == cardId) {
				person = <portlet:namespace/>_news[i].name;
				<portlet:namespace/>_news.splice(i, 1);
				break;
			}
		}
		
		<portlet:namespace/>_refreshControls(false, person);	
	}

	function <portlet:namespace/>_requestTypeChanged() {
		var widget = dijit.byId('<portlet:namespace/>_requestTypes');
		if (widget.isValid() && widget.item != null) {
			dojo.byId('<portlet:namespace/>_requestTypesField').value = widget.item.id;

			var widget_news = dijit.byId('<portlet:namespace/>_newSelect');
			var widget_term = dijit.byId('<portlet:namespace/>_term_control');
			var widget_change = dijit.byId('<portlet:namespace/>_term_change');

			if(widget.item.id == 1125 || widget.item.id == 1126 || widget.item.id == 1127) {
				widget_news.setAttribute('disabled', false);
				dojo.connect(widget_term, "onKeyPress", function(evt){});  
			} else {
				widget_news.setAttribute('disabled', true);
				dojo.connect(widget_term, "onKeyPress", function(evt){
					dojo.stopEvent(evt);
				});
			}

			if(widget.item.id == 1129) {
				widget_change.setAttribute('disabled', false);
				dojo.connect(widget_change, "onKeyPress", function(evt){});  
			} else {
				widget_change.setAttribute('disabled', true);
				dojo.connect(widget_change, "onKeyPress", function(evt){
					dojo.stopEvent(evt);
				});
			}

		} else {
			dojo.byId('<portlet:namespace/>_requestTypesField').value = null;
		}
	}

	function <portlet:namespace/>_consideratorsChanged() {
		var widget = dijit.byId('<portlet:namespace/>_considerators');
		if (widget.isValid() && widget.item != null) {
			dojo.byId('<portlet:namespace/>_consideratorsField').value = widget.item.id;
		}
	}

	function <portlet:namespace/>_respChanged(widgetId) {
		var widget = dijit.byId('<portlet:namespace/>_respUser' + widgetId);
		if (widget.isValid()) {
			var newsId = -1;
			for(var i = 0; i < <portlet:namespace/>_news.length; i++) {
				if(<portlet:namespace/>_news[i].widget == widgetId) {
					newsId = i;
					break;
				}
			}
			if(newsId == -1) {
				return;
			}
			<portlet:namespace/>_news[newsId].resp = widget.item.id;
			var value = "";
			for(var i=0; i < <portlet:namespace/>_news.length; i++) {
				if (i > 0) {
					value +=separator;
				}
				value += <portlet:namespace/>_news[i].cardId
					  	 +":"+<portlet:namespace/>_news[i].resp+":"+<portlet:namespace/>_news[i].dt;
			}
			dojo.byId('<portlet:namespace/>_newsField').value = value;
		}
	}
	
	function <portlet:namespace/>_termChanged(widgetId) {
		var widget = dijit.byId('<portlet:namespace/>_select_date' + widgetId);
		if (widget.isValid()) {
			var newsId = -1;
			for(var i = 0; i < <portlet:namespace/>_news.length; i++) {
				if(<portlet:namespace/>_news[i].widget == widgetId) {
					newsId = i;
					break;
				}
			}
			if(newsId == -1) {
				return;
			}
			<portlet:namespace/>_news[newsId].dt = widget.value;
			var value = "";
			for(var i=0; i < <portlet:namespace/>_news.length; i++) {
				if (i > 0) {
					value +=separator;
				}
				value += <portlet:namespace/>_news[i].cardId
					  	 +":"+<portlet:namespace/>_news[i].resp+":"+<portlet:namespace/>_news[i].dt;
			}
			dojo.byId('<portlet:namespace/>_newsField').value = value;
		}
	}
	
	function <portlet:namespace/>_newSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_newSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			widget.attr('value', '');
			<portlet:namespace/>_selectPersonCard(card.cardId, name);
		}
	}
	
	function <portlet:namespace/>_submitForm(action) {
		if (action == '<%= RequestToChangeConsPortlet.ACTION_DONE %>') {
			var widget_types = dojo.byId('<portlet:namespace/>_requestTypesField');
			var widget_comm = dojo.byId('<portlet:namespace/>_comment');
			var widget_news = dojo.byId('<portlet:namespace/>_newsField');
			var widget_change = dijit.byId('<portlet:namespace/>_term_change');
			var widget_current_considerator = dojo.byId('<portlet:namespace/>_isCurrentUserConsiderator');
			var widget_selected_considerator = dojo.byId('<portlet:namespace/>_consideratorsField');
			
			if(widget_types.value == null || widget_types.value == '') {
				alert('<fmt:message key="message.noRequestType"/>');
				return false;
			} else if(widget_comm.value == null || widget_comm.value == '') {
				alert('<fmt:message key="message.noComment"/>');
				return false;
			} else if((widget_current_considerator == null || widget_current_considerator.value == 'null' || widget_current_considerator.value == '') &&
					 (widget_selected_considerator.value == null || widget_selected_considerator.value == '')) {
				alert('<fmt:message key="message.noConsiderator"/>');
				return false;
			}<%-- else if((widget_news.value == null || widget_news.value == '')
						&& (widget_types.value == 1125 || widget_types.value == 1126 || widget_types.value == 1127)) {
				alert('<fmt:message key="message.noNews"/>');
				return false;
			} --%> else if((widget_change.value == null || widget_change.value == '')
						&& widget_types.value == 1129) {
				alert('<fmt:message key="message.noTerm"/>');
				return false;
			}
		}
		lockScreen();
		var form = dojo.byId('<portlet:namespace/>_form');
		dojo.byId('<portlet:namespace/>_action').value = action;
		form.submit();
		return false;
	}
	
	function <portlet:namespace/>_showMessage() {
		alert(<portlet:namespace/>_message);
	}
	
	dojo.addOnLoad(function(){
		var h = document.documentElement.clientHeight == 0 ? document.body.clientHeight : document.documentElement.clientHeight;
		
		dojo.byId("DIV_DOC_MAIN").style.height = (h - 180) +"px"
	})
	
</script>