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
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.ajax.QuickResolutionSearchPersonParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>

<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.portlet.QuickResolutionPortletSessionBean"%>
<%@page import="com.aplana.crypto.CryptoLayer"%>
<%@page import="com.aplana.dbmi.Portal"%>

<fmt:setBundle basename="com.aplana.dbmi.portlet.nl.QuickResolutionPortlet"/>

<portlet:defineObjects/>

<%
	QuickResolutionPortletSessionBean sessionBean = 
		(QuickResolutionPortletSessionBean)renderRequest.getPortletSession().
		getAttribute(QuickResolutionPortlet.SESSION_BEAN);
	CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());
%>

<c:set var="sessionBean" value="<%=sessionBean%>"/>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/crypto.js" ></script>

<script type="text/javascript" language="javascript">
/*
	function dbmiHideLoadingSplash() {
		dojo.byId('dbmiLoadingSplash').style.display = 'none'; 
		dojo.byId('sizer').style.display = 'block';
	}
	dojo.addOnLoad(dbmiHideLoadingSplash);
	function dbmiShowLoadingSplash() {
		dojo.byId('dbmiLoadingSplash').style.display = 'block'; 
		dojo.byId('sizer').style.display = 'none';
	}	

*/
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
		if ((action == '<%= QuickResolutionPortlet.ACTION_DONE %>' || action == '<%= QuickResolutionPortlet.ACTION_DONE_PLUS %>') 
		&& !isFutureDay('<portlet:namespace/>_term')) {
			alert('<fmt:message key="message.termNotFuture"/>');
		} else {		
			if(action == '<%= QuickResolutionPortlet.ACTION_DONE %>' || action == '<%= QuickResolutionPortlet.ACTION_DONE_PLUS %>'){
				getSignature();			
			}
			form.submit();
			dbmiShowLoadingSplash();
			return false;
		}
	}	

	function <portlet:namespace/>_setFocus(idDiv) {
		var textarea = document.getElementById('<portlet:namespace/>_text_res');
		var div = dojo.byId(idDiv);
		var text = div.textContent || div.innerText;
		if (textarea.value == '') {
			textarea.value = text;
		} else {
			textarea.value = textarea.value + '\n' + text;
		}
	}
	var <portlet:namespace/>_oldTermControl = null;
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
	function <portlet:namespace/>_changePerControl(checkControl, termControl) {
		if (checkControl.checked) {
			termControl.setDisabled(false);
			termControl.attr('value', dojo.date.stamp.fromISOString("<%=QuickResolutionPortlet.getNextDate()%>"));
			<portlet:namespace/>_oldTermControl = dojo.date.stamp.fromISOString("<%=QuickResolutionPortlet.getNextDate()%>");
		} else {
			termControl.setDisabled(true);
			termControl.attr('value',null);
			<portlet:namespace/>_oldTermControl = null;
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
					<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
					<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
					<%= SearchCardServlet.PARAM_IGNORE %>: ''
				},
				onChange: function() { <portlet:namespace/>_controllerSelectChanged(); }
			},
			dojo.byId('<portlet:namespace/>_controllerSelect')
		);
		if (<portlet:namespace/>_message != '') {
			<portlet:namespace/>_showMessage();
			//стираем сообщение, его больше не надо показывать
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
		var editor = dojo.byId('<portlet:namespace/>_text_res');
		editor.value = '';
	}
	
	function getSignature(){
		try{
			var signValue = dojo.byId("<portlet:namespace/>_text_res").value;
					
			var fieldsToSign= [{
				id: "T:<%=QuickResolutionPortlet.ATTR_RESOLUT.getId().toString()%>",								
				value: signValue
			}];
			
			var args = {
				noDecode: true,
				fields: fieldsToSign,	
				storeName: "<%= com.aplana.dbmi.crypto.SignatureData.PARAM_CERTSTORE %>",
				userName: "<%= renderRequest.getRemoteUser()%>"
			};
													
			var signResult = cryptoGetSignature(args); 
			//alert(dojo.toJson(signResult))
			if(signResult.success){			
				dojo.byId("signature").value = signResult.signature;				
			}	
						
		}catch(ex){		
			alert('<fmt:message key="label.control"/>' + ex)
		}
	}
	
	var dates = {
		    convert:function(d) {
		        // Converts the date in d to a date-object. The input can be:
		        //   a date object: returned without modification
		        //  an array      : Interpreted as [year,month,day]. NOTE: month is 0-11.
		        //   a number     : Interpreted as number of milliseconds
		        //                  since 1 Jan 1970 (a timestamp) 
		        //   a string     : Any format supported by the javascript engine, like
		        //                  "YYYY/MM/DD", "MM/DD/YYYY", "Jan 31 2009" etc.
		        //  an object     : Interpreted as an object with year, month and date
		        //                  attributes.  **NOTE** month is 0-11.
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
		        // Compare two dates (could be of any type supported by the convert
		        // function above) and returns:
		        //  -1 : if a < b
		        //   0 : if a = b
		        //   1 : if a > b
		        // NaN : if a or b is an illegal date
		        // NOTE: The code inside isFinite does an assignment (=).
		        return (
		            isFinite(a=this.convert(a).valueOf()) &&
		            isFinite(b=this.convert(b).valueOf()) ?
		            (a>b)-(a<b) :
		            NaN
		        );
		    },
		    inRange:function(d,start,end) {
		        // Checks if date in d is between dates in start and end.
		        // Returns a boolean or NaN:
		        //    true  : if d is between start and end (inclusive)
		        //    false : if d is before start or after end
		        //    NaN   : if one or more of the dates is illegal.
		        // NOTE: The code inside isFinite does an assignment (=).
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
		now.setMinutes(0,0,0);// min,sec,millisec
		now.setDate(now.getDate());
		now.setMonth(now.getMonth());
		/*now = now.toUTCString();*/

		return value == null || dates.compare(value, now) != -1; /* value > yesterday */;
	}

	function <portlet:namespace/>_showMessage() {
		alertDialog({text:<portlet:namespace/>_message});
	}

	
	// upload attachments functions
	function <portlet:namespace/>_uploadAttachment() {
		var filePathInput = dojo.byId('<portlet:namespace/>_filePath');
		var filePath = filePathInput.value;
		if(filePath == "") return;
		dijit.byId('<portlet:namespace/>_uploadButton').setAttribute('disabled', true);    
		
		
		var uploadUrl = self.location.protocol + "//" + self.location.host + '<%=QuickResolutionPortlet.MATERIAL_UPLOAD_URL %>';
		var resolutionCardId = '<%= sessionBean.getIdResolution() %>';

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
		//check if given card has been already attached 
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

	function <portlet:namespace/>_removeExtExecutor(id) {
		<portlet:namespace/>_removeCard(id, <portlet:namespace/>_externals);
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


<c:url var="workFlowURL" value="/servlet/JasperReportServlet">
	<c:param name="nameConfig" value="reportPrintExecutionResolution"/>
	<c:param name="card_id" value="L_${sessionBean.parentId}"/>
</c:url>

 
<!-- 
	<div id="dbmiLoadingSplash" style="display: block;  height: 220px;" >
	<div style="width:100%; height:100%; background-color: #ffffff;">
		<p>Загрузка...</p>

		<br />
		<br />
		<p><img
			src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif"
			border="0" alt="">
		</p>
	</div>
	
 -->
 	
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

<c:set var="workflowBaseCardId" value="L_${sessionBean.baseId}"/>

<%@include file="../docTitleTopHeader.jspf"%>
<div id="DIV_DOC_MAIN" style="margin-right:10px; overflow-y:auto">
<%@include file="../baseCardDataSection.jspf"%>


<form id="<portlet:namespace/>_form" method="post" action="<portlet:actionURL/>">

	<input id="<portlet:namespace/>_responsibleField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_RESPONSIBLE_EXECUTOR %>" value=""/>
	<input id="<portlet:namespace/>_additionalsField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ADDITIONAL_EXECUTORS %>" value=""/>
	<input id="<portlet:namespace/>_externalsField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_EXTERNAL_EXECUTORS %>" value=""/>
	<input id="<portlet:namespace/>_controllersField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_CONTROLLERS %>" value=""/>
	<input id="<portlet:namespace/>_onControlField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ON_CONTROL %>" value=""/>
	<input id="<portlet:namespace/>_resolutionAttachmentField" type="hidden" name="<%= QuickResolutionPortlet.FIELD_FILE_ATTACHMENT %>" value=""/>
	<input id="<portlet:namespace/>_action" type="hidden" name="<%= QuickResolutionPortlet.FIELD_ACTION %>" value=""/>
	
	<div class="resolution">

		<hr />	
        
		<div class="left_column">

            <textarea name="<%=QuickResolutionPortlet.FIELD_TEXT_RESOLUTION%>" class="resolution_text" placeholder="<fmt:message key="label.textResolution"/>" id="<portlet:namespace/>_text_res"><c:out value="${sessionBean.resolutionText}"/></textarea>
            <div class="clear_resolution" >
				<a onclick="<portlet:namespace/>_clearEditor(); return false;"  href="#" /></a>
			</div>			
			
            <div class="resolution_templates">
                <ul>
                <!-- typical resolution output -->
					<c:forEach var="standardResolution" items="${sessionBean.standartResolutionTexts}" varStatus="status" >
						<li>
							<a id="<portlet:namespace/>_text_res_${status.count}" class="dotted" href="Javascript: void(0);" 
								onClick="<portlet:namespace/>_setFocus('<portlet:namespace/>_text_res_${status.count}');return false;"><c:out value="${standardResolution}"/></a>
						</li>
						
					</c:forEach>
                </ul>
		
                
            </div>

           
            
            

        </div><!-- .left_column end -->

        
        
        <div class="center_column">
        
            <div class="block"> 
                <div class="select">
						<input dojoType="dijit.form.RadioButton" id="<portlet:namespace/>_executorChoice" name="<portlet:namespace/>_groupChoice"
						type="radio"  checked="true" />
						<label class=""><fmt:message key="label.executors"/></label>
	                   	<select id="<portlet:namespace/>_executorSelect" />
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_responsibleExecutorTable" class="res"  style="width: 100%; margin-top: 0px;">
					</table>
                </div>
            </div>

            <div class="block"> 
                <div class="select">
					<input dojoType="dijit.form.RadioButton" id="<portlet:namespace/>_additionalExecutorChoice" name="<portlet:namespace/>_groupChoice"
						type="radio" />
						<label class=""><fmt:message key="label.additionalExecutors"/></label>						
						<select id="<portlet:namespace/>_additionalExecutorsSelect"/>
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_additionalExecutorsTable" class="res" style="width: 100%; margin-top: 0px;">
					</table>
                </div>
            </div>
            
            <div class="block"> 
                <div class="select">
						<label class="" style="margin-left: 23px;"><fmt:message key="label.externalExecutors"/></label>
						<select id="<portlet:namespace/>_externalExecutorsSelect"/>
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_externalExecutorsTable" class="res" style="width: 100%; margin-top: 0px;">
					</table>
                </div>
            </div>
            
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


        </div><!-- .center_column end -->
        
        
        <c:set var="immediateEmployeesMap" value="<%=sessionBean.getImmediateEmployees()%>"/>
        <c:set var="responsibleEmployeesMap" value="<%=sessionBean.getResponsible()%>"/>

        
    	<div class="right_column">
	
            <div class="environment">
                <div class="arrow"></div>

                <div class="persons_list">
                	
                    <ul>
					<c:forEach var="entry" items="${immediateEmployeesMap}">
						<c:set var="isResponsibleEmployeeUsed" value="${responsibleEmployeesMap[entry.key.id]}"/>
						<c:set var="isAdditionalEmployeeUsed" value="${sessionBean.additionals[entry.key.id]}"/>
						<c:choose>
							<c:when test="${isResponsibleEmployeeUsed !=null}">
								<li><a href="Javascript: void(0);" class="inactive" id="<portlet:namespace/>_immediate_men_${entry.key.id}" onclick="<portlet:namespace/>_deselectPersonCard(${entry.key.id}, '${entry.value}');">${entry.value}</a></li>
			                 </c:when> 
			                 <c:otherwise>
       							<li><a href="Javascript: void(0);" class="dotted" id="<portlet:namespace/>_immediate_men_${entry.key.id}" onclick="<portlet:namespace/>_selectPersonCard(${entry.key.id}, '${entry.value}');">${entry.value}</a></li>
							 </c:otherwise>
						</c:choose>
						
					</c:forEach>
                    </ul>
                </div>
            </div>

        </div><!-- .right_column end -->
        
		<hr />

        <div class="left_column">
           <div class="time_frames">
              <div id="date_term" class="date_select">
                 <label><fmt:message key="label.term"/></label>
                 <script type="text/javascript">
                    dojo.addOnLoad(function() {
                       dojo.require('dbmiCustom.DateTimeWidget');
                       dojo.require('dojo.date.stamp');
                       widget = new dbmiCustom.DateTimeWidget( 
                          {
                             nameDate: '<%=QuickResolutionPortlet.FIELD_TERM_DATE%>',
                             nameTime: '<%=QuickResolutionPortlet.FIELD_TERM_TIME%>',
                             valueString: '<%=sessionBean.getTermString()%>',
                             timePattern: '<%=sessionBean.getTermTimePattern()%>',
                             isShowTime: <%=sessionBean.isTermShowTime()%>,
                             _widthDate: 130,
                             _styleTime: 'width: 100px; margin-top: 4px; border:none;'
                          }
                       );
                       widget.placeAt(dojo.byId("date_term"));
                       dojo.connect(widget._date, 'onChange', function() {var _value = this.value; if (this.value instanceof Date) {_value = dojo.date.stamp.toISOString(this.value, {selector: 'date'});}});
                    });
                 </script>
              </div>

                <div class="date_select">
                    <label><fmt:message key="label.control"/></label>
						<input dojoType="dijit.form.DateTextBox" type="text" 
														   name="<%=QuickResolutionPortlet.FIELD_CONTROL_TERM%>"
														   id="<portlet:namespace/>_term_control"
														   style = "width: 130px;"
														   onBlur="<portlet:namespace/>_validDate(dijit.byId('<portlet:namespace/>_term_control'), 
														   										  <portlet:namespace/>_oldTermControl,
														   										  'oldTermControl')" 
														<%if(sessionBean.getControlTerm()!=null){%>
														   value="<%=(new SimpleDateFormat("yyyy-MM-dd")).format(sessionBean.getControlTerm())%>"
														<%} else {%>
														   value=""
														<%}%>
													/>
                    
                </div>
                <div class="date_select" id="<portlet:namespace/>_preliminary_term_control">
                    <label><fmt:message key="label.preliminaryTerm"/></label>
                    <input dojoType="dijit.form.DateTextBox" type="text" id="<portlet:namespace/>_preliminary_term"
                    name="<%=QuickResolutionPortlet.FIELD_PRELIMINARY_TERM%>"  style = "width: 130px; margin: 0; padding: 0; "
                            onBlur="<portlet:namespace/>_validDate(dijit.byId('<portlet:namespace/>_preliminary_term'), 
                            <portlet:namespace/>_oldPreliminaryTerm, 'oldPreliminaryTerm')"
                        value="<%if(sessionBean.getPreliminaryTerm()!=null){%><%=(new SimpleDateFormat("yyyy-MM-dd")).format(sessionBean.getPreliminaryTerm())%><%}%>"/> 
                </div>

            </div>
        </div>

    	<div class="center_column">
            <div class="block">
            	<div class="checkBox">
            		<c:set var="onControlVal" value="<%=sessionBean.getIsOnControl()%>"/>            		
            		<c:choose>
						<c:when test="${onControlVal}">
							<input onclick="<portlet:namespace/>_enableControllers()" dojoType="dijit.form.CheckBox" 
	 							checked id="<portlet:namespace/>_onControlCB" name="<portlet:namespace/>_onControlCB"/>
	 						<label for="<portlet:namespace/>_onControlCB"><fmt:message key="label.onControl"/></label>	
						</c:when>
						<c:otherwise>
							<input onclick="<portlet:namespace/>_enableControllers()" dojoType="dijit.form.CheckBox" 
	 							id="<portlet:namespace/>_onControlCB" name="<portlet:namespace/>_onControlCB"/>
	 						<label for="<portlet:namespace/>_onControlCB"><fmt:message key="label.onControl"/></label>
						</c:otherwise>
					</c:choose>
                </div>
                
                <div class="select">
                    <label class="no-radio" for="<portlet:namespace/>_controllersSelect"><fmt:message key="label.controllers"/></label>
					<select id="<portlet:namespace/>_controllersSelect"/>
                </div>
                <div class="list">
					<table id="<portlet:namespace/>_controllersTable" class="content">
						<thead>
							<col />
							<col />
						</thead>
					</table>
                </div>
            </div>

        </div>

        <hr style="background: white;" />
		<!-- begin ЭЦП -->
		<input id="signature" name="<%=QuickResolutionPortlet.FIELD_SIGNATURE%>" type="hidden" value="" />
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
						onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_CANCEL %>')"  ></a>
					</div>	
                <div class="submit_resolution" >
					<a  href="#"
						onclick="<portlet:namespace/>_submitForm('<%= QuickResolutionPortlet.ACTION_DONE %>')"  ></a>
                </div>
            </div>

    </div><!-- .controls end -->
</div>
<script type="text/javascript">
	var <portlet:namespace/>_responsible = ${requestScope.responsible};
	var <portlet:namespace/>_additionals = ${requestScope.additionals};
	var <portlet:namespace/>_externals = ${requestScope.externals};
	var <portlet:namespace/>_controllers = ${requestScope.controllers};
	
	var <portlet:namespace/>_message = ${requestScope.message};

	dojo.require("dijit.form.Button");
	dojo.require("dojox.data.QueryReadStore");
	dojo.require("dijit.form.FilteringSelect");	
	
	
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
                style: "width: 160px; height: 21px;",
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

		var additionalExecutorsSelect = new dijit.form.FilteringSelect(
				{
					store: dataStore,
					searchAttr: 'label',
					pageSize: 15,
					searchDelay: 500,
					required: false,
					autoComplete: false,
                    style: "width: 160px; height: 21px;",
					query: {
						<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
						<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
						<%= SearchCardServlet.PARAM_IGNORE %>: '',
						<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
					},
					onChange: function() { <portlet:namespace/>_additionalExecutorsSelectChanged(); }
				},
				dojo.byId('<portlet:namespace/>_additionalExecutorsSelect')
			);
		
		var externalExecutorsSelect = new dijit.form.FilteringSelect(
				{
					store: dataStore,
					searchAttr: 'label',
					pageSize: 15,
					searchDelay: 500,
					required: false,
					autoComplete: false,
                    style: "width: 160px; height: 21px;",
					query: {
						<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
						<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
						<%= SearchCardServlet.PARAM_IGNORE %>: '',
						<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: '<%= QuickResolutionSearchPersonParameters.EXT_OPTION %>' 
					},
					onChange: function() { <portlet:namespace/>_externalExecutorsSelectChanged(); }
				},
				dojo.byId('<portlet:namespace/>_externalExecutorsSelect')
			);		
		

		var controllersSelect = new dijit.form.FilteringSelect(
				{
					store: dataStore,
					searchAttr: 'label',
					pageSize: 15,
					searchDelay: 500,
					required: false,
					autoComplete: false,
                    style: "width: 160px; height: 21px;",
					query: {
						<%= QuickResolutionSearchPersonParameters.PARAM_NAMESPACE %>: '<portlet:namespace/>',
						<%= SearchCardServlet.PARAM_CALLER %>: '<%= QuickResolutionSearchPersonParameters.CALLER %>',
						<%= SearchCardServlet.PARAM_IGNORE %>: '',
						<%= SearchCardServlet.PARAM_QUICHR_OPTIONS %>: 'int'
					},
					onChange: function() { <portlet:namespace/>_controllersSelectChanged(); }
				},
				dojo.byId('<portlet:namespace/>_controllersSelect')
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
		
		<portlet:namespace/>_fillTable(
				<portlet:namespace/>_externals, 
				'<portlet:namespace/>_externalExecutorsTable', 
				'<portlet:namespace/>_externalsField'
		);
		
		<portlet:namespace/>_fillTable(
				<portlet:namespace/>_controllers, 
				'<portlet:namespace/>_controllersTable', 
				'<portlet:namespace/>_controllersField'
		);		
		<portlet:namespace/>_enableControllers();

		<portlet:namespace/>_fillTable(
				<portlet:namespace/>_resolutionAttachments,
				'<portlet:namespace/>_resolutionAttachmentTable',
				'<portlet:namespace/>_resolutionAttachmentField',
				false
			);
		
		// говорим выподающему списку каких пользователей уже не надо выводить
		var allIds = [];
		for (var i = 0; i < <portlet:namespace/>_additionals.length; ++i) {
			allIds.push(<portlet:namespace/>_additionals[i].cardId);
		}		
		if (<portlet:namespace/>_responsible.cardId != undefined) {
			allIds.push(<portlet:namespace/>_responsible.cardId);
		}
		

		for (var i = 0; i < <portlet:namespace/>_controllers.length; ++i) {
			allIds.push(<portlet:namespace/>_controllers[i].cardId);
		}

		var extIds = [];
		for (var i = 0; i < <portlet:namespace/>_externals.length; ++i) {
			extIds.push(<portlet:namespace/>_externals[i].cardId);
		}	
		
		var select = dijit.byId('<portlet:namespace/>_executorSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');		

		select = dijit.byId('<portlet:namespace/>_controllersSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');

		select = dijit.byId('<portlet:namespace/>_additionalExecutorsSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = allIds.join(',');		

		select = dijit.byId('<portlet:namespace/>_externalExecutorsSelect');
		select.query.<%= SearchCardServlet.PARAM_IGNORE %> = extIds.join(',');

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

		value = "";
		for(var i=0; i < <portlet:namespace/>_externals.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_externals[i].cardId+":"+<portlet:namespace/>_externals[i].name;
		}
		dojo.byId('<portlet:namespace/>_externalsField').value = value;

		value = "";
		for(var i=0; i < <portlet:namespace/>_controllers.length; i++) {
			if (i > 0) {
				value +=separator;
			}
			value += <portlet:namespace/>_controllers[i].cardId+":"+<portlet:namespace/>_controllers[i].name;
		}
		dojo.byId('<portlet:namespace/>_controllersField').value = value;

		var id_delimiter = '<%=QuickResolutionPortlet.ID_DELIMITER%>';
		value = "";
		for(var i=0; i < <portlet:namespace/>_resolutionAttachments.length; i++) {
			if (i > 0) {
				value += separator;
			}
			value += <portlet:namespace/>_resolutionAttachments[i].cardId + id_delimiter + <portlet:namespace/>_resolutionAttachments[i].name;
		}
		dojo.byId('<portlet:namespace/>_resolutionAttachmentField').value = value;
		
	}
	
	function <portlet:namespace/>_fillTable(records, tableId, hiddenId) {
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
			} else if ('<portlet:namespace/>_externalExecutorsTable' == tableId) {
				cell.innerHTML = '<a href="#" class="delete" onclick="<portlet:namespace/>_removeExtExecutor(' + records[i].cardId + '); return false;">&nbsp;</a>';
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
		
		var executorChoice = dojo.byId('<portlet:namespace/>_executorChoice');
		var additionalExecutorChoice = dojo.byId('<portlet:namespace/>_additionalExecutorChoice');
		if (executorChoice.checked) {
			addResponsiblePersonCard(<portlet:namespace/>_responsible, cardId, name);
		} else if (additionalExecutorChoice.checked) {
			addPersonCard(cardId, name, <portlet:namespace/>_additionals, 100);
						
		} 		
		<portlet:namespace/>_refreshControls();
	}

	/**
		Adds person to specific java-script array
		cardId - card identifier
		name - person's name
		field_array - java script array variable
		maxCount - maximum allowed value    
	*/
	function addPersonCard(cardId, name, field_array, maxCount) {

		if (maxCount <= 0)
			return;//do nothing...invalid value

				
		//there are restrictions on array length
		if (maxCount == field_array.length) {
			field_array[field_array.length-1] = {cardId: cardId, name: name};
			return;
		}	
		//add new value to the end of array
		field_array[field_array.length] = {cardId: cardId, name: name};
			
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

		//replaces with new value 
		field_array.cardId = cardId;
		field_array.name = name;
				
	}	
	
	
	
	function <portlet:namespace/>_deselectPersonCard(cardId, name) {
		var hrefPerson = dojo.byId('<portlet:namespace/>_immediate_men_'+ cardId);
		if ( hrefPerson) {
			hrefPerson.setAttribute('class', 'dotted');
			hrefPerson.onclick=function() {
				<portlet:namespace/>_selectPersonCard(cardId, name);
			};
			
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
		
		var controllersCheckBox = dojo.byId('<portlet:namespace/>_onControlCB');
		if(controllersCheckBox && controllersCheckBox.checked) {
			for (var i = 0; i < <portlet:namespace/>_controllers.length; ++i) {
				if (<portlet:namespace/>_controllers[i].cardId == cardId) {
					<portlet:namespace/>_controllers.splice(i, 1);
				}
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
			//<portlet:namespace/>_selectPersonCard(card.cardId, name);
			addResponsiblePersonCard(<portlet:namespace/>_responsible, card.cardId, name);		
			<portlet:namespace/>_refreshControls();
		}
	}
	
	

	function <portlet:namespace/>_controllersSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_controllersSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_controllers.length;
			<portlet:namespace/>_controllers[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}	
		

	function <portlet:namespace/>_additionalExecutorsSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_additionalExecutorsSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.JBR_PERS_SNAME+' '+card.columns.JBR_PERS_NAME+' '+card.columns.JBR_PERS_MNAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_additionals.length;
			<portlet:namespace/>_additionals[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}

	function <portlet:namespace/>_externalExecutorsSelectChanged() {
		var widget = dijit.byId('<portlet:namespace/>_externalExecutorsSelect');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var name = card.columns.NAME;
			widget.attr('value', '');
			var i = <portlet:namespace/>_externals.length;
			<portlet:namespace/>_externals[i] = {cardId: card.cardId, name: name};
			<portlet:namespace/>_refreshControls();
		}
	}	
	
	function <portlet:namespace/>_submitForm(action) {
		if (action != '<%= QuickResolutionPortlet.ACTION_CANCEL %>' && <portlet:namespace/>_responsible.cardId == undefined) {
			alert('<fmt:message key="message.noExecutor"/>');
			
		} else {
			lockScreen();
			var form = dojo.byId('<portlet:namespace/>_form');
			dojo.byId('<portlet:namespace/>_action').value = action;
			form.submit();
			return false;
		}
	}

	dojo.addOnLoad(function(){
		var h = document.documentElement.clientHeight == 0 ? document.body.clientHeight : document.documentElement.clientHeight;
		
		dojo.byId("DIV_DOC_MAIN").style.height = (h - 180) +"px"
	})
	
	function <portlet:namespace/>_enableControllers() {
		var enabled = false;
		var controllersCheckBox = dojo.byId('<portlet:namespace/>_onControlCB');
		if(controllersCheckBox && controllersCheckBox.checked) {
			enabled = true;
		}

		// enable/disable controllers combo-box and its label
		dijit.byId("<portlet:namespace/>_controllersSelect").setDisabled(!enabled);
		var ctrlsLbl = dojo.byId('<portlet:namespace/>_controllersSelect_label');
		var lblClass = enabled ? 'no-radio active_label' : 'no-radio inactive_label';
		ctrlsLbl.setAttribute("class", lblClass);
		
		var table = dojo.byId('<portlet:namespace/>_controllersTable');
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= 0; --i) {
				var row = table.rows[i];
				var delRecCell = row.cells[0];
				var nameCell = row.cells[1];
				
				var classVal = enabled ? 'active_label' : 'inactive_label';
				nameCell.setAttribute("class", classVal);

				classVal = enabled ? 'delete' : 'delete_inactive';
				var delLink = delRecCell.firstChild;
				delLink.setAttribute("class", classVal);
    		}
    	}
		
		// hiding preliminary date selector if resolution is not on control 
        var prelimTermControl = dojo.byId('<portlet:namespace/>_preliminary_term_control');
        var controlClass = enabled ? 'date_select' : 'date_select hidden';
        prelimTermControl.setAttribute("class", controlClass);

		// fill hidden value
		dojo.byId('<portlet:namespace/>_onControlField').value = enabled;
	}
</script>