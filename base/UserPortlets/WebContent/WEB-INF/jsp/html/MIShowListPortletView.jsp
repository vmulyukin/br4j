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
<%@page import="com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandlerFactory"%>
<%@page session="false" contentType="text/html" pageEncoding="UTF-8" %>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.*"%>
<%@page import="com.aplana.dbmi.showlist.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.action.SearchResult"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.displaytag.util.SortingState"%>
<%@ page import="com.aplana.dbmi.model.ObjectId" %>
<%@ page import="com.aplana.dbmi.model.Template" %>
<%@ page import="com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandlerFactory" %>

<%@taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>

<portlet:defineObjects/>
<fmt:setBundle basename="com.aplana.dbmi.showlist.nl.MIShowListPortletResource" scope="request"/>

<script type="text/javascript">
dojo.require("dijit.Tooltip");
dojo.require("dijit.Dialog");

function showDialog(html, title) {
	var d = dijit.byId('<portlet:namespace/>_filesDialog');
	if(d) d.destroy();
	var h = document.body.clientHeight;
	var w = document.body.clientWidth;
	var filesDialog = new dijit.Dialog({
		id: '<portlet:namespace/>_filesDialog',
		title: title,
		content: html,
		style: {width:(w/3)+'px'}
	});
	
	filesDialog.show();
}
function showAttachmentsDialog(dataUrl,card) {
	dojo.xhrGet({
		url: dataUrl,	
		sync: true,
		content: {
			cardId: card
		},
		handleAs: 'json',
		load: function(data) {
			showDialog(data.html, data.label);
		},
		error: function(error) {
			console.error(error);
		}
	});
}
</script>
<script language="javascript" type="text/javascript">
	dojo.addOnLoad(function(){
		dojo.require('dbmiCustom.Notifier');
		var notifWidget = new dbmiCustom.Notifier();
		notifWidget.placeAt(document.body);
		notifWidget.startup();
	});
</script>
<%
	MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean)renderRequest.getPortletSession().getAttribute(MIShowListPortlet.SESSION_BEAN);	
	List metaDataDesc = sessionBean.getMetaDataDesc();
	request.setAttribute( "dataList", sessionBean.getDataList());
	request.setAttribute("dataColumns", sessionBean.getRowExData());
	String tableID = MIShowListPortlet.TABLE_ID+String.valueOf(sessionBean.hashCode());
	
	boolean isPrintMode = sessionBean.isPrintMode();   	
	if( isPrintMode) {
		sessionBean.setPrintMode(false);
	}
	
	boolean editAccessExists = sessionBean.isEditAccessExists();
	String message = sessionBean.getMessage();
	if( message != null) {
		sessionBean.setMessage(null);
	} else {
		message = (String)renderRequest.getPortletSession().getAttribute(MIShowListPortlet.MSG_PARAM_NAME, PortletSession.APPLICATION_SCOPE);
		renderRequest.getPortletSession().removeAttribute(MIShowListPortlet.MSG_PARAM_NAME, PortletSession.APPLICATION_SCOPE);
	}
	
%>

<btn:errorMessage message="<%= message %>"/>

<%
    // get caption & menu
	String title =  (	(sessionBean.getTitle() != null) 
	        		 && (sessionBean.getTitle().getValue() != null)
	        		 )
	        			? sessionBean.getTitle().getValue()
	       				: "";
 %>	

<% if (sessionBean.isFavoritesViewPortlet()) { %>
	<script type="text/javascript">
    	function removeFavorites(url){
			if (confirm('<fmt:message key="remove.fovorites.confirm.msg"/>')){
				window.location=url;
			}
		}
	</script>
<% } %>


<div class="columns" style="padding-bottom:10px; height: 35px;">
 <table width="100%" style="margin-top: -10px;">
  <tr>
   <td>
		<%  
			/* Create Header Text/JSP by priority:
			     1) HeaderJsp; 2) HeaderText; 3) (default) title
			 */
			if (sessionBean.isShowHeader()) 
			{
		%>
		 		<!--div class="innerheader">  убираем название - не нужно, лишнее место
		<%  		 		
				final String headerJsp = sessionBean.getHeaderJsp();
				final String headerText = sessionBean.getHeaderText();
				if ( headerJsp != null) {
		 %>
		 			<jsp:include page="<%= headerJsp %>" />
		 <%     } else if (headerText != null) 
		 		{
		 %>  		
		 			<%= headerText %>
		 			<hr/>
		 <%    } else { 
		 %>    	
		 			<%= title %>
		 <%    }
		 %>
		       </div-->
		 <%	 		
			}
		 %>

	<div style="float: left">
<%
		boolean showRefresh = false;
		if (sessionBean.isRequestViewPortlet()) {
			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.BACK_TO_REQUEST_ACTION); 
%>	
			<a HRef="<%= backURL.toString() %>" style="text-decoration: underline;" ><span class="back"><fmt:message key="back.my.request.link" /></span></a>
	
<%		} 
		
		if (sessionBean.isShowBtnRefresh()) {
			showRefresh = true;
		}
%>
<%--
			PortletURL refreshURL = renderResponse.createActionURL();
			refreshURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.REFRESH_ACTION);

			<a href="<%= refreshURL.toString() %>" style="padding-left: 10px;"><fmt:message key="refresh.btn" /></a>
--%>	
	</div>
   </td>
   <td>
<% 	
	PortletService portletService = Portal.getFactory().getPortletService();
	String cardPageId = portletService.getPageProperty("cardPage", renderRequest, renderResponse);
	if (cardPageId == null) {
		cardPageId = "dbmi.Card";
	}

	if (sessionBean.isShowToolbar() || showRefresh) 
	{ 
%>
		<div class="toolbar" style="float: right">
			<div class="buttonPanel">
				<ul>
<%
			final HashMap urlParams = new HashMap();

			String action = sessionBean.getCurrentTemplate() != null ? sessionBean.getCurrentTemplate().toString() :
				CardPortlet.CREATE_CARD_ID_FIELD;
			urlParams.put(CardPortlet.CREATE_CARD_ID_FIELD, action);

			PortletURL backURL = renderResponse.createRenderURL();
			backURL.setParameters(renderRequest.getParameterMap());
			urlParams.put(CardPortlet.BACK_URL_FIELD, URLEncoder.encode(backURL.toString(), "UTF-8"));
			final String createCardURL = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", urlParams, renderRequest, renderResponse);
			
			final PortletURL printURL = renderResponse.createActionURL();
			printURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.PRINT_ACTION); 
			final PortletURL moveURL = renderResponse.createActionURL();
			final PortletURL moveAllURL = renderResponse.createActionURL();
			final PortletURL importURL = renderResponse.createActionURL();
			importURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.IMPORT_ACTION); 
			importURL.setParameter(MIShowListPortlet.BACK_URL_ATTR, URLEncoder.encode(backURL.toString(), "UTF-8"));
			final PortletURL confirmURL = renderResponse.createActionURL();
			final PortletURL rejectURL = renderResponse.createActionURL();

			
			final String exportURL = renderResponse.encodeURL(renderRequest.getContextPath() + "/mishowlistportlet/exporttoexcel?namespace=" + renderResponse.getNamespace());
						
			if (showRefresh)
			{
				PortletURL refreshURL = renderResponse.createActionURL();
				refreshURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.REFRESH_ACTION); 
 %>					<btn:button textKey="refresh.btn"
 								href="<%= refreshURL.toString() %>" />
<%			}
			if (sessionBean.isCanGroupExecution()) {%>
			<btn:button tooltipKey="tool.group.execution"
						href="javascript: openResolutionReportsList()"
						icon="ico_groupexec"/>
			<% }
			if (sessionBean.isCanGroupResolution()) {%>
			<btn:button tooltipKey="tool.group.resolution"
						href="javascript: openGroupResolutionDocumentList()"
						icon="ico_groupres"/>
			<% }
			if (sessionBean.isShowToolbar()) {
				if (editAccessExists&&sessionBean.isCanCreate()) {
					if(createCardURL.matches("(.*)delo(.*)") || createCardURL.matches("(.*)archive(.*)")){
						//skip button creating
					}
					else {					
%> 
					
		 			    	<btn:button tooltipKey="tool.new.card" 
										href="<%= response.encodeURL(createCardURL) %>" 
										icon="ico_new"/>	
						
<%					}
				}
			if (editAccessExists&&sessionBean.isCanImportCards()&&sessionBean.isCanCreate()) { 
%> 
						<btn:button tooltipKey="tool.import.xls"
									href="<%= importURL.toString() %>"
									icon="ico_xls_in"/>	
<%			
			}
%> 

<%		
			final String downloadImportTemplate = sessionBean.getDownloadImportTemplate();
			if(editAccessExists&&downloadImportTemplate != null) { %>
					
					<btn:button tooltipKey="tool.download.importTemplates"
									href="<%= renderRequest.getContextPath() + "/MaterialSearchDownloadServlet?"
									+ MaterialSearchDownloadServlet.PARAM_SEARCH_TEMPLATE_ID + "=jbr.import"
									+ "&" + MaterialSearchDownloadServlet.PARAM_SEARCH_STATUS_ID + "=published"
									+ "&" + MaterialSearchDownloadServlet.PARAM_SEARCH_ATTR_ID + "=list:jbr.loadingDict"
									+ "&" + MaterialSearchDownloadServlet.PARAM_SEARCH_ATTR_VALUE + "=" + downloadImportTemplate %>"
									icon="ico_csv_in"/>	
									
			<% } %>
			
			<% 
				ObjectId cardId = sessionBean.getCsvTemplateCardId();
				if(cardId != null) {
					String exportFileCardId = cardId.getId().toString();
					String csvExportUrl = renderRequest.getContextPath() + "/ExportToCsvServlet?namespace=" + renderResponse.getNamespace()
							+ "&" + ExportToCsvServlet.PARAM_EXPORT_TEMPLATE + "=" + ( sessionBean.getCurrentTemplate() != null ? sessionBean.getCurrentTemplate().toString() : "" )
							+ "&" + ExportToCsvServlet.PARAM_CSV_TEMPLATE_FILE_CARD_ID + "=" + exportFileCardId;
			%>
			<c:set var="csvExportUrl" value="<%=csvExportUrl%>"/>
			<btn:button tooltipKey="tool.download.exportToCsv"
									onClick="window.open('${csvExportUrl}')"
									icon="ico_csv_out"/>	
			<% } %>
					<btn:button tooltipKey="tool.export.xls"
								href="<%= exportURL %>"
								icon="ico_xls"/>	
					<btn:button tooltipKey="tool.print"
								href="<%= printURL.toString() %>"
								icon="ico_print"/>
<%		
			if (sessionBean.isCanMoveAll()) { 
				moveURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.MOVE_PAGE_ACTION);
				moveAllURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.MOVE_ACTION);
				importURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.IMPORT_ACTION);
				confirmURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.CONFIRM_MOVE_ACTION);	
				rejectURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.REJECT_MOVE_ACTION);	
				String moveCount = portletService.getPageProperty(MIShowListPortlet.MOVE_MODE, renderRequest, renderResponse);
%> 
				<script type="text/javascript" language="javascript">
					dojo.require('dijit.form.Button');
					
					function showLoadScreen() {
						dojo.require("dojo.NodeList-traverse");
						dojo.byId('dbmiLoadingSplash').style.display = 'block'; 
						dojo.byId('sizer').style.display = 'none';
						dojo.query('#dbmiLoadingSplash').children().children()[0].style.fontSize = '24px';
						dojo.query('#dbmiLoadingSplash').children().children()[0].innerHTML = '<fmt:message key="move.cards.notification"/>';
					}
					
					function submitAction() {
						window.location.href = '<%= moveURL.toString() %>';
					}
				</script>
					<btn:button tooltipKey="tool.to.archive.page" 
								onClick="showLoadScreen(); submitAction();"
								icon="ico_archive_page"/>

			<c:set var="move_n_cards"><fmt:message key="tool.to.archive"><fmt:param value="<%= moveCount %>"/></fmt:message></c:set>
					<btn:button tooltip="${move_n_cards}"
								href="<%= moveAllURL.toString() %>"
								icon="ico_archive_all"/>		
										
			<c:if test="<%= renderRequest.getParameter(MIShowListPortlet.NEED_CONFIRM) != null  && 
			 renderRequest.getParameter(MIShowListPortlet.NEED_CONFIRM).equalsIgnoreCase("true")  %>">		
				<script type="text/javascript" language="javascript">
					dojo.require('dijit.form.Button');	
				</script>
				<c:set var="confirmationDialogId"><portlet:namespace/>_confirmationDialog</c:set>
				<div id="${confirmationDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="move.all.confirmation.title"/>" style="width: 320px; height: 96px">
					<div style="text-align: left;"><fmt:message key="move.all.confirmation"/></div>
					<div style="float:right; clear: both;" id="dialogButtons">
						<button dojoType="dijit.form.Button" type="button">
							<fmt:message key="move.all.confirmation.yes"/>
			   				<script type="dojo/method" event="onClick" args="evt">
								dijit.byId('${confirmationDialogId}').hide();
								dojo.require("dojo.NodeList-traverse");
								dojo.byId('dbmiLoadingSplash').style.display = 'block'; 
								dojo.byId('sizer').style.display = 'none';
								dojo.query('#dbmiLoadingSplash').children().children()[0].style.fontSize = '24px';
								dojo.query('#dbmiLoadingSplash').children().children()[0].innerHTML = '<fmt:message key="move.cards.notification"/>';
								window.location.href = '<%= confirmURL.toString() %>';
							</script>	
						</button>
						<button dojoType="dijit.form.Button" type="button">
							<fmt:message key="move.all.confirmation.no"/>
			   				<script type="dojo/method" event="onClick" args="evt">
								dijit.byId('${confirmationDialogId}').hide();
								window.location.href = '<%= rejectURL.toString() %>';					
							</script>	
						</button>
					</div>
				</div>
				<script type="text/javascript" language="javascript">
					dojo.require('dijit.Dialog');
					dojo.addOnLoad(function() {
						dbmiHideLoadingSplash();
						dijit.byId('${confirmationDialogId}').show();
					});
				</script>			
			</c:if>	

<%			}

			
    final ObjectId jbrIncomming = ObjectId.predefined(Template.class, "jbr.incoming");
    final Long currentTemplate = sessionBean.getCurrentTemplate();
    // BR4J00009271 - Hide "Upload card from file" button // aminnekhanov, 23.07.2012
    //if (currentTemplate != null && jbrIncomming != null && jbrIncomming.getId().equals(currentTemplate)){
    if(false){

    String namespace = renderResponse.getNamespace();
%>
                    <btn:button tooltipKey="tool.upload.card"
                                href="javascript:uploadFilesToCard()"
                                icon="ico_save_xml"/>

<div id="dbmiUploadingSplash" style="position:fixed; top: 0; left: 0;  width: 100%;  height: 120%; z-index:99; display: none;" >
    <div style="width:100%; height:100%; background-color: #ffffff;">
        <br />
        <br />
        <p style="text-align: center;">
        <center><img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt=""></center>
        </p>
    </div>
</div>

<applet name="DownloadCardsApplet"	id="DownloadCardsApplet"
		codebase="<%=request.getContextPath()%>"
        archive="SJBCrypto.jar"
		code="com.aplana.crypto.DownloadCardsApplet.class"	WIDTH=1	HEIGHT=1>

        <param name="download.applet.file.name" value="<fmt:message key="download.applet.file.name"/>">
        <param name="download.applet.folder.name" value="<fmt:message key="download.applet.folder.name"/>">
        <param name="download.applet.folder" value="<fmt:message key="download.applet.folder"/>">
        <param name="download.applet.type" value="<fmt:message key="download.applet.type"/>">
        <param name="download.applet.cancel" value="<fmt:message key="download.applet.cancel"/>">
        <param name="download.applet.select" value="<fmt:message key="download.applet.select"/>">

 	    <H1>WARNING!</H1>
	    The browser you are using is unable to load Java Applets!
</applet>

<script language="javascript">

    function dbmiShowLoadingUploadSplash() {
		dojo.byId('dbmiUploadingSplash').style.display = 'block';
	}

    function dbmiHideLoadingUploadSplash() {
		dojo.byId('dbmiUploadingSplash').style.display = 'none';
	}

    String.prototype.format = function() {

        var formatted = this;

        for (var i = 0; i < arguments.length; i++) {

            var regexp = new RegExp('\\{' + i + '\\}', 'gi');
            formatted = formatted.replace(regexp, arguments[i]);

        }

        return formatted;
    };
    
    function handleError(err) {
        var errorMessageFormat = '<fmt:message key="upload.error.common"/>';
        var errorMessage;
        if (err.message) {
            errorMessage = err.message;
        } else {
            errorMessage = err;
        }
        alert(errorMessageFormat.format(errorMessage));
    }

    function uploadFilesToCard(){
        var uploadTitle = '<fmt:message key="upload.select.directory.title"/>';

        var appl = dojo.byId("DownloadCardsApplet");
        var directoryFullPath = null;

        if (appl) {
            try {
                appl.setLocale('<%=request.getLocale().getLanguage()%>', '<%=request.getLocale().getCountry()%>');
                directoryFullPath = appl.selectFolder(uploadTitle);
            } catch (err) {
                handleError(err);
            }
        }
        if (directoryFullPath) {            
            if (dbmiShowLoadingUploadSplash) {
                dbmiShowLoadingUploadSplash();
            }            
            try {
                var downloadResult;
                var url = self.location.protocol + "//" + self.location.host + "<%=request.getContextPath()%>";
                appl.setContextUrl(url);
                appl.setNamespace('<portlet:namespace/>');
                appl.setWorkingDirectory(directoryFullPath);
                downloadResult = appl.uploadFilesToCards('.xml');
                alert("<fmt:message key="upload.success"/>".format(downloadResult));
            } catch (err){
                handleError(err);
            } finally{
                if (dbmiHideLoadingUploadSplash) {
                    dbmiHideLoadingUploadSplash();
                }
            }            
        }
    }
</script>


<%			}
    	}
%>



				</ul>
			</div>
		</div>
<% 	}
%>
    </td>
   </tr>
  </table>
</div>

<script type="text/javascript" language="javascript">
function submitForm_single(elem, link) {
	elem.onclick = function() { return false }
	window.location.href = link;
}
</script>

<%
// end caption & menu

// table
	request.setAttribute("metaDataDesc", metaDataDesc);

	ShowListTableDecorator decorator = new ShowListTableDecorator(renderRequest, renderResponse);
	decorator.setLinkPg( sessionBean.getLinkPg());
	decorator.setLinkUrl( sessionBean.getLinkUrl());

	//decorator.setLink(editCardURL.toString());
	String decoratorAttributeName = "ShowListTableDecorator";
	request.setAttribute(decoratorAttributeName, decorator);
	final int offset = sessionBean.getDataOffset(); 
%>
  <display:table name="dataList" 
		uid="<%= tableID %>" sort="list" class="res" decorator="<%= decoratorAttributeName %>">

	<display:setProperty name="basic.msg.empty_list" ><fmt:message key="table.basic.msg.empty_list"/></display:setProperty>
	<display:setProperty name="paging.banner.placement" >both</display:setProperty>
	<display:setProperty name="paging.banner.no_items_found" ><fmt:message key="table.paging.banner.no_items_found"/></display:setProperty>
	<display:setProperty name="paging.banner.one_item_found" ><fmt:message key="table.paging.banner.one_item_found"/></display:setProperty>
	<display:setProperty name="paging.banner.all_items_found" ><fmt:message key="table.paging.banner.all_items_found"/></display:setProperty>
	<display:setProperty name="paging.banner.some_items_found" ><fmt:message key="table.paging.banner.some_items_found"/></display:setProperty>

<% if (!sessionBean.getDataList().isEmpty()) {
		List dataList = (List)pageContext.getAttribute(tableID);
		String entityId = dataList.get(0).toString();		
		PortletURL addFavoritesURL = renderResponse.createActionURL();
		addFavoritesURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.ADD_FAVORITES_ACTION); 
		addFavoritesURL.setParameter(MIShowListPortlet.ENTITY_ID_FIELD, entityId); 

		final Map params = new HashMap();
		params.put( CardPortlet.EDIT_CARD_ID_FIELD, entityId);
		params.put( CardPortlet.BACK_URL_FIELD, request.getAttribute(MIShowListPortlet.BACK_URL_ATTR));
		params.put( CardPortlet.OPEN_FOR_EDIT_FIELD, "true");

		String editLink = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", params, renderRequest, renderResponse);

		if (sessionBean.isShowRowIconEditView()) { %>
		<display:column class="searchColumnId">
			<fmt:message var="editTooltip" key="tooltip.edit"/>
			<c:set var="editAllowed"><%=editAccessExists&&((Boolean)dataList.get(1)).booleanValue() %></c:set>
			<c:set var="editLink"><%= editLink %></c:set>
			<btn:linkimage enable="${editAllowed}" enableIcon="edit" disableIcon="edit_disable" 
						   toolTip="${editTooltip}" onClick="submitForm_single(this, '${editLink}')">#</btn:linkimage>
		</display:column>
	<% } else { %>
		<display:column>
		</display:column>
	<% } %>
<% } %>	
<%
	for (int i = offset ; i < metaDataDesc.size() ; i++  ) {
		SearchResult.Column columnMeta = (SearchResult.Column)metaDataDesc.get(i);

		String colStyle = "width: " + columnMeta.getWidth() + "em;";
		String indexed = "[" + i + "]"; // i+offset

		if (columnMeta.isHidden()) {
%>
			<display:column  title=""/>
<%
		} else if (columnMeta.isIcon()) {
			String iconIndex = "icon"+indexed;
%>
			<display:column  title="" property="<%= iconIndex %>" />
<%
		} else if (columnMeta.isLinked()) {
			String indexedLink = "cardLink" + indexed;
%>
<display:column style="<%= colStyle %>"  title="<%= columnMeta.getName() %>" sortable="<%= columnMeta.isSortable() %>" maxLength="<%= columnMeta.getWidth() %>" property="<%= indexedLink %>" sortProperty="<%= indexed %>" />
<%
		} else {
%>
<display:column style="<%= colStyle %>"  title="<%= columnMeta.getName() %>" sortable="<%= columnMeta.isSortable() %>" maxLength="<%= columnMeta.getWidth()>columnMeta.getTextLength()?columnMeta.getWidth():columnMeta.getTextLength() %>" property="<%= indexed %>" />
<%			
			}	
	}
 %>	
	
<% if (!sessionBean.getDataList().isEmpty()
		&& sessionBean.isFavoritesViewPortlet()) { 
	
		String entityId = ((List)pageContext.getAttribute(tableID)).get(0).toString();

		PortletURL removeFavoritesURL = renderResponse.createActionURL();
		removeFavoritesURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.REMOVE_FAVORITES_ACTION); 
		removeFavoritesURL.setParameter(MIShowListPortlet.ENTITY_ID_FIELD, entityId); 

%>
	<display:column >
	<a href="javascript: removeFavorites('<%= removeFavoritesURL.toString() %>');" style="text-decoration: none;" ><span class="delete">&nbsp;</span></a>
	</display:column>

<% } %>
	
	
  </display:table>

<% 
	// PRINT Mode
	if (isPrintMode) { %>

<script>

function showPrintPage() {

dojo.query("#printTableDiv .columns").forEach(dojo.destroy); //Панель навигации между страницами списка не нужна в режиме печати
var datatable = document.getElementById('printTableDiv').innerHTML;
var NewWindow = window.open("about:blank",'_blank',"resizable=yes,scrollbars=yes,status=yes,titlebar=yes,toolbar=yes,menubar=yes,location=no");
var NewWinDoc = NewWindow.document;
NewWinDoc.writeln('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"');
NewWinDoc.writeln('   "http://www.w3.org/TR/html4/loose.dtd">');

NewWinDoc.writeln('<html><head>');

NewWinDoc.writeln('<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">');
	

NewWinDoc.writeln('<LINK rel=\"stylesheet\" href=\"<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/showList.css") %>\" type=\"text/css\">');

NewWinDoc.writeln('<style type=\"text/css\">');
NewWinDoc.writeln(' @import <%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/print.css") %> print; /* Стиль для печати */');
NewWinDoc.writeln('</style>');

NewWinDoc.writeln('</head><body>');

NewWinDoc.writeln('<div class=reportheader>');
NewWinDoc.writeln('<a href=\"#\" onclick=\"window.print();return false\"><span class=\"report printPage_print\">&nbsp;</span></a>');
NewWinDoc.writeln('<a href=\"javascript:onClick=window.close();\"><span class=\"report printPage_close\">&nbsp;</span></a>');
NewWinDoc.writeln('<div class=\"reportheaderHR\"></div>');
NewWinDoc.writeln('</div>');

NewWinDoc.writeln(datatable);
NewWinDoc.writeln('</body></html>');
NewWinDoc.close();
window.location.reload(); //Так как в режиме печати список будет выводится целиком, без деления на страницы, нужно обновить основное окно.
}

</script>

<%
// caption & menu

	String tabCaption  = sessionBean.getTitle() == null ? "" : (sessionBean.getTitle().getValue() == null ? "" : sessionBean.getTitle().getValue());
 %>	

<div style="display: none;" id="printTableDiv" > 
  <display:table name="dataList" id="printTable" uid="printTable" class="res">
	<display:caption><%= tabCaption %></display:caption>
	
<%
	for (int i = offset ; i < metaDataDesc.size() ; i++  ) {
		SearchResult.Column columnMeta = (SearchResult.Column)metaDataDesc.get(i);

		Object columnValue = "";
		List columnValuesList = (List) pageContext.getAttribute("printTable");
		if (columnValuesList != null 
			&& !columnValuesList.isEmpty()
			) {
            // (YNikitin, 2012/07/18) если значение в очередной колонке = null, то выводим 0 
			columnValue = (columnValuesList.get(i)!=null)?columnValuesList.get(i):"0"; // .get(i+offset);			
		}
		
%>
	<display:column  title="<%= columnMeta.getName() %>" >
			<%= columnValue %>
	</display:column>
<% 		
	}
 %>	
	
  </display:table>

</div>

<script language="javascript">
	showPrintPage();
</script>

<% } %>

<%	
if (sessionBean.isCanGroupExecution() || sessionBean.isCanGroupResolution()) {
	final HashMap specUrlParams = new HashMap();
	PortletURL backURL = renderResponse.createRenderURL();
	backURL.setParameters(renderRequest.getParameterMap());
	
	specUrlParams.put(CardPortlet.CREATE_CARD_ID_FIELD, SpecificCustomStoreHandlerFactory.GROUP_EXECUTION_TEMPLATE);
	specUrlParams.put(CardPortlet.SPEC_ACTION_MODE_FIELD, SpecificCustomStoreHandlerFactory.GROUP_EXECUTION_ACTION);
	specUrlParams.put(CardPortlet.BACK_URL_FIELD, URLEncoder.encode(backURL.toString(), "UTF-8"));
	
	final String groupExecURL = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", specUrlParams, renderRequest, renderResponse);
	
	specUrlParams.put(CardPortlet.CREATE_CARD_ID_FIELD, SpecificCustomStoreHandlerFactory.GROUP_RESOLUTION_TEMPLATE);
	specUrlParams.put(CardPortlet.SPEC_ACTION_MODE_FIELD, SpecificCustomStoreHandlerFactory.GROUP_RESOLUTION_ACTION);
	
	final String groupResolutionURL = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", specUrlParams, renderRequest, renderResponse);
	
	%>
	<script>
	dojo.require("dojox.grid.EnhancedGrid");        
	dojo.require("dojo.data.ItemFileReadStore");
	dojo.require("dojox.grid.enhanced.plugins.IndirectSelection");
	dojo.require("dojo.parser");
	dojo.require("dojo.date.locale");
	dojo.require("dojo.date.stamp");
	dojo.require("dijit.Dialog");
	dojo.require("dijit.form.Button");
	
	function formatDate(datum){
	    var d = dojo.date.stamp.fromISOString(datum);
	    if(d){
	    	return dojo.date.locale.format(d, {selector: 'date', formatLength: 'medium'});
	    } else {
	    	return null;
	    }
	}
	
	function openResolutionReportsList(){
		
		var reports;
		var reportsSize = 0;
		
		dojo.xhrGet({
			url: '<%=request.getContextPath() + "/GroupExecutionCardServlet"%>',	
			sync: true,
			content: {
				mode: '<%=sessionBean.getGroupExectionMode().toString()%>'
			},
			handleAs: 'json',
			load: function(data) {
				reports = data;
				reportsSize = data.length;
			},
			error: function(error) {
				console.error(error);
			}
		});
		if(reportsSize==0){
			alert('<fmt:message key="alert.no.reports.to.execution"/>');
			return;
		}
		var reportsStore = new dojo.data.ItemFileReadStore({
		    data: {
		    	identifier: "id",
		    	items: reports
		    }
		});
		
		if(dijit.byId("groupExecDialog")){
			var grid = dijit.byId('groupExecGrid');
			grid.store.close();
			grid.setStore(reportsStore);
			dijit.byId("groupExecDialog").show();
			grid.update();
			return;
		}
		
		  var groupExecDialog = new dijit.Dialog({
		       title: '<fmt:message key="title.group.execution"/>',
		       style: "height:auto; width: auto",
		       id:'groupExecDialog'
		       }
		   );
		  
		  groupExecDialog.setContent('<div id="groupExec" class="groupExec_on"></div>'+
		  							'<div id="actions" class="groupExecActions">'+
		  								'<button id="groupExecButtonNode" type="button"/>'+
		  								'<button id="cancelGroupExec" type="button"/>'+
		  							'</div>');
		groupExecDialog.show();
		  
		
		var layout = [	{field: "resolution",name: "Резолюция",width: "230px"},
						{field: "deadline",name: "Срок",width: "70px", formatter: formatDate},
						{field: "signer",name: "Подписант",width: "150px"},
						{field: "regnum",name: "Рег.номер",width: "100px"},
						{field: "regdate",name: "Рег. дата",width: "70px", formatter: formatDate},
						{field: "descr",name: "Краткое содержание",width: "230px"}
					];
		
		var table = new dojox.grid.EnhancedGrid({
		
		    store: reportsStore,
		    structure: layout,
		    id: "groupExecGrid",
		    rowsPerPage: reportsSize,
		    autoHeight: true,
		    sortInfo: 3,
		    plugins: {indirectSelection: {headerSelector:true, width:"40px", styles:"text-align: center;"}}
		}, document.createElement("div"));
		
		dojo.byId("groupExec").appendChild(table.domNode);
		
		table.startup();
		
		var groupExecButton = new dijit.form.Button({
			label: '<fmt:message key="title.prepare.exec.report"/>',
			id: "groupExecButton",
			onClick: function(){
				var selectedObjectds = dijit.byId('groupExecGrid').selection.getSelected();
				if(selectedObjectds.length == 0){
					alert('<fmt:message key="alert.item.not.selected"/>');
					return;
				}
				var selectedIds = [];
				selectedObjectds.forEach(function(el){
					selectedIds.push(el.id[0]);
				});
				window.location = "<%= response.encodeURL(groupExecURL) %>" + '&REPORT_GROUP=' +selectedIds.join("_");
			}
		}, "groupExecButtonNode").startup();
		
		var cancelGroupExec = new dijit.form.Button({
			label: '<fmt:message key="move.all.confirmation.no"/>',
			id: "cancelGroupExec",
			onClick: function(){
				dijit.byId("groupExecDialog").hide()
			}
		}, "cancelGroupExec").startup();
	
	}
	
	function openGroupResolutionDocumentList(){
		//TODO	
		var docs;
		var docsSize = 0;

		dojo.xhrGet({
			url: '<%=request.getContextPath() + "/GroupResolutionCardServlet"%>',	
			sync: true,
			content: {
				mode: '<%=sessionBean.getGroupResolutionMode().toString()%>'
			},
			handleAs: 'json',
			load: function(data) {
				docs = data;
				docsSize = data.length;
			},
			error: function(error) {
				console.error(error);
			}
		});
		if(docsSize==0){
			alert('<fmt:message key="alert.no.docs.for.resolution.creating"/>');
			return;
		}
		var docsStore = new dojo.data.ItemFileReadStore({
		    data: {
		    	identifier: "id",
		    	items: docs
		    }
		});
		
		if(dijit.byId("groupResolutionDialog")){
			var grid = dijit.byId('groupResolutionGrid');
			grid.store.close();
			grid.setStore(docsStore);
			dijit.byId("groupResolutionDialog").show();
			grid.update();
			return;
		}
		
		  var groupResolutionDialog = new dijit.Dialog({
		       title: '<fmt:message key="title.group.resolution"/>',
		       style: "height:auto; width: auto",
		       id:'groupResolutionDialog'
		       }
		   );
		  
		  groupResolutionDialog.setContent('<div id="groupResolution" class="groupResolution_on"></div>'+
		  							'<div id="res_actions" class="groupExecActions">'+
		  								'<button id="groupResolutionButtonNode" type="button"/>'+
		  								'<button id="cancelGroupResolution" type="button"/>'+
		  							'</div>');
		  groupResolutionDialog.show();
		  
		
		var layout = [	{field: "template",name: "Шаблон",width: "100px"},
						{field: "regnum",name: "Рег.номер",width: "100px"},
						{field: "regdate",name: "Рег. дата",width: "70px", formatter: formatDate},
						{field: "descr",name: "Краткое содержание",width: "200px"},
		              	{field: "resolution",name: "Резолюция",width: "200px"},
		              	{field: "signer",name: "Подписант",width: "150px"},
						{field: "deadline",name: "Срок",width: "70px", formatter: formatDate}
					];
		
		var table = new dojox.grid.EnhancedGrid({
		
		    store: docsStore,
		    structure: layout,
		    id: "groupResolutionGrid",
		    rowsPerPage: docsSize,
		    autoHeight: true,
		    plugins: {indirectSelection: {headerSelector:true, width:"40px", styles:"text-align: center;"}}
		}, document.createElement("div"));
		
		dojo.byId("groupResolution").appendChild(table.domNode);
		
		table.startup();
		
		var groupResolutionButton = new dijit.form.Button({
			label: '<fmt:message key="title.prepare.group.resolution"/>',
			id: "groupResolutionButton",
			onClick: function(){
				var selectedObjectds = dijit.byId('groupResolutionGrid').selection.getSelected();
				if(selectedObjectds.length == 0){
					alert('<fmt:message key="alert.item.not.selected"/>');
					return;
				}
				var selectedIds = [];
				selectedObjectds.forEach(function(el){
					selectedIds.push(el.id[0]);
				});
				window.location = "<%= response.encodeURL(groupResolutionURL)%>" + '&DOCS_GROUP=' +selectedIds.join("_");
			}
		}, "groupResolutionButtonNode").startup();
		
		var cancelGroupResolution = new dijit.form.Button({
			label: '<fmt:message key="move.all.confirmation.no"/>',
			id: "cancelGroupResolution",
			onClick: function(){
				dijit.byId("groupResolutionDialog").hide()
			}
		}, "cancelGroupResolution").startup();
	
	}
	
	</script>
<% } %>