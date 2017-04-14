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
<%@page session="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %>
<%@ taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>


<%@page import="java.util.ListIterator"%>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.card.util.SearchUtils"%>
<%@page import="com.aplana.dbmi.model.TemplateBlock"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.ListAttribute"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.gui.BlockView"%>
<%@page import="com.aplana.dbmi.PortletService"%>
<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.dbmi.gui.TabView"%>
<%@page import="com.aplana.dbmi.model.TabViewParam"%>
<%@page import="com.aplana.dbmi.model.BlockViewParam"%>
<%@page import="com.aplana.dbmi.gui.AttributeView"%>
<%@page import="com.aplana.dbmi.ajax.JasperReportServlet"%>
<%@page import="com.aplana.dbmi.model.StringAttribute"%>
<%@page import="com.aplana.dbmi.model.DateAttribute"%>
<%@page import="com.aplana.dbmi.ajax.JasperReportServlet"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.BackLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage"%>
<%@page import="com.aplana.dbmi.service.DataException"%>
<%@page import="com.aplana.dbmi.model.CardState"%>
<%@page import="com.aplana.cms.tags.FileTag"%> 

<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>
<script type="text/javascript">
	function f5press(e) {
		if(e.keyCode == 116 || (e.keyCode == 82 && e.ctrlKey)) {
			//разрешаем обновлять страницу в режиме просмотра
			trustedAction = true;
			window.location.href += "&reloadOnRefresh=true";		
			return false;
    	}
	}
	//помечаем флагом разрешенные действия
	trustedAction = false;
	
	window.onkeydown = function (event) { 
		return f5press(event);
	}
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>

<script type="text/javascript" language="javascript" src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/js/mail.js") %>">
</script>
<form name="<%= CardPortlet.EDIT_FORM_NAME %>" method="post" action="<portlet:actionURL/>"> 
	<input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">
	<input type="hidden" name="<%= CardPortlet.ATTR_ID_FIELD %>" value="">
	<input type="hidden" name="<%= CardPortlet.CARD_ID_FIELD %>" value="">
	<input type="hidden" name="<%= CardPortlet.CARD_MODE %>" value="<%= CardPortlet.CARD_VIEW_MODE %>"/>
	<input type="hidden" name="<%= CardPortlet.FIELD_THIS_PAGE %>" value="<portlet:renderURL/>"/>

	<input type="hidden" name="<%= CardPortlet.PARAM_DOCLINK_TEMPLATE %>" value="">
	<input type="hidden" name="<%= CardPortlet.PARAM_DOCLINK_TYPE %>" value="">	 
	<input type="hidden" name="<%= CardPortlet.CARD_TAB_ID %>" value="">
	<input type="hidden" name="<%= CardPortlet.DIALOG_INPUT_PARAM_NAME %>" value="">
	<input type="hidden" name="<%= CardPortlet.DISABLE_DS %>" value="">
	
	<input type="hidden" name="<%= CardPortlet.DIALOG_ACTION_FIELD %>" value="">
	<input type="hidden" name="<%= CardPortlet.DIALOG_EDITOR_ACTION_FIELD %>" value="">
    <input type="hidden" name="<%= CardPortlet.DIALOG_EDITOR_VALUE %>" value="">
    <input type="hidden" name="namespace" value='<%= renderResponse.getNamespace() %>'>
    <input type="hidden" name="<%= CardPortlet.STAMP_POSITION %>" value="">  
<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
	PortletService portletService = Portal.getFactory().getPortletService();

	TabsManager tabsManager = sessionBean.getActiveCardInfo().getTabsManager();
	if(tabsManager != null && tabsManager.getActiveTab() != null){
		HashSet<String> jspIncludes = new HashSet<String>(40);
		for (Iterator itr = ((TabView)tabsManager.getActiveTab()).getContainer().getAllRegions().iterator(); itr.hasNext(); ) {
			BlockView block = (BlockView)itr.next();
			for (Iterator j = block.getAttributeViews().iterator(); j.hasNext(); ){
				JspAttributeEditor editor = (JspAttributeEditor) ((AttributeView)j.next()).getEditor();
				if ((editor != null && editor.getInitJspPath() != null)&&(!jspIncludes.contains(editor.getInitJspPath()))) {
					try {
						out.flush();
						editor.writeCommonCode(renderRequest, renderResponse);
						jspIncludes.add(editor.getInitJspPath());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
%>
<script type="text/javascript" language="javascript">
function submitForm_TabChange(tabId, elem) { 
	if (elem) elem.onClick = function() { return false };
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.CHANGE_TAB_CARD_ACTION %>';
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_TAB_ID %>.value = tabId;
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}
function submitForm(action, elem) { 
	if (elem) elem.onClick = function() { return false };
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = action;
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_TAB_ID %>.value = '<%= tabsManager.getActiveTab().getId().getId().toString() %>';
	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}
function submitForm_Export(elem) {
	if (elem) elem.onClick = function() { return false };
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.EXPORT_XLS_ACTION %>';
	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}
</script>
	<div id="fixedCardHeader">
	<%
	/*
	String message = sessionBean.getMessage();
	if( message != null) {
		sessionBean.setMessage(null);
	} else {
		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
	}
	 */
	PortletMessage cardContainer = sessionBean.getPortletMessage();
	Map<ObjectId,String> cardDesc = null;
	List<ObjectId> cardIds = null;
	String message = null;
	String messageStyle = null;
	if (cardContainer != null){ 
		cardDesc = cardContainer.getContainer();
		cardIds = cardContainer.getCardIds();
		message = cardContainer.getMessage();
		messageStyle = cardContainer.getMessageStyle();
		if (message == null)
			message = "";
		sessionBean.setPortletMessage( null);
	} else {
		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
		messageStyle = PortletMessage.STYLE_INFO;
	}

	Card card = sessionBean.getActiveCard();
	String cardId = "";
//	StringBuffer cardURL = new StringBuffer();
	String cardURL = "";
	if (card != null) {
		// parse Id
		if (card.getId() != null) {
			cardId = card.getId().getId().toString();
		}
		String cardPageId = portletService.getPageProperty("cardPage", renderRequest, renderResponse);
		if (cardPageId == null) {
			cardPageId = "dbmi.Card";
		}
		//String pageURL = portletService.generateLink(cardPageId, null, null, renderRequest, renderResponse);

		Map params = new HashMap(1);
		params.put(CardPortlet.EDIT_CARD_ID_FIELD, cardId);
		cardURL = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", params, renderRequest, renderResponse);
	}

	if (message != null) {

%>
<table class="<%= messageStyle %>">
	<tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
	<tr class="tr2"><td class="td_21"/><td class="td_22">
	<%= message %>
<% 
	if (!(cardDesc == null || cardDesc.isEmpty()) && !(cardIds == null || cardIds.isEmpty()) )
	{	
		%>
		<br>
		<ol class="cardinfo">
		<%
			for (ObjectId cid: cardIds)
			{
				String description = cardDesc.get(cid);
				StringBuilder textRow = new StringBuilder();
				PortletURL URL = renderResponse.createActionURL();
				URL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
				URL.setParameter(CardPortlet.CARD_ID_FIELD, cid.getId().toString());
				if (description.contains(CardPortlet.HAS_ATTACHMENTS_TAG))
				{
					description = description.replace(CardPortlet.HAS_ATTACHMENTS_TAG,"");
					textRow.append(description);
					textRow.append("<a href=\"")
						//>>>>>
						.append(request.getContextPath())
						.append("/ShowMaterialServlet?")
						.append(CardPortlet.CARD_ID_FIELD)
						.append("="+cid.getId().toString())
						//<<<<<
						.append("\" title=\"Открыть вложения\" target=\"_blank\"> <span class=\"repeatlist\"></span> </a>");
				}
				else textRow.append(description);
				%>
				<li class="cardinfo"><%= textRow %></li>
		<%
			} // for 
		%>
		</ol>
		<%
	}
%>

<% 
	if (card == null) {
%>
		&nbsp;<a HRef="<portlet:actionURL><portlet:param name="<%= CardPortlet.ACTION_FIELD %>" value="<%= CardPortlet.BACK_ACTION %>" /></portlet:actionURL>" ><fmt:message key="view.page.back.link" /></a>
<%	
	}
%>
		</td><td class="td_23"/>
	</tr>
    <tr class="tr3"><td class="td_31"/><td class="td_32"/><td class="td_33"/></tr>
</table>
	
<% } 
		
//  draw main page
				
	if (card != null) {
		/*
		String collapseAttrId = renderRequest.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (collapseAttrId != null) {
			sessionBean.setItemDisplayedViewMode(collapseAttrId,
					!sessionBean.isItemDisplayedViewMode(collapseAttrId));
		}
		*/
		boolean isPrintMode = sessionBean.getActiveCardInfo().isPrintMode();   	
		if( isPrintMode) {
			sessionBean.getActiveCardInfo().setPrintMode(false);
		}

		ResourceBundle bundle = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale());
//		String adminEmail = sessionBean.getAdminEmail() != null ? sessionBean.getAdminEmail().trim() : "";
		
%>

    <table class="indexCardMain">
        <col Width="50%" />        
        <col Width="50%" />
        <tr><!--Иконки-->
            <td colspan="4">
            <table class="icons">
                <tr style="height:35px;">
                    <td style="float:left;">  
                    
<%

			PortletURL backURL = renderResponse.createActionURL();
			backURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.BACK_ACTION); 
			backURL.setWindowState(WindowState.NORMAL);

			PortletURL url = renderResponse.createRenderURL();

			PortletURL newURL = renderResponse.createActionURL();
			newURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.CREATE_CARD_ACTION); 
			newURL.setWindowState(WindowState.MAXIMIZED);

			PortletURL cloneURL = renderResponse.createActionURL();
			cloneURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.CLONE_CARD_ACTION); 
			cloneURL.setWindowState(WindowState.MAXIMIZED);

			PortletURL editURL = renderResponse.createActionURL();
			editURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.EDIT_CARD_ACTION); 
			editURL.setWindowState(WindowState.MAXIMIZED);
			editURL.setParameter(CardPortlet.CARD_TAB_ID, tabsManager.getActiveTab().getId().getId().toString());

			PortletURL printURL = renderResponse.createActionURL();
			printURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.PRINT_ACTION); 

			PortletURL favoritesURL = renderResponse.createActionURL();
			favoritesURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.ADD_FAVORITES_ACTION); 
	
	String mailSubject = bundle.getString("view.page.mail.subject.msg") + " " + cardId;
//	String mailBody = bundle.getString("view.page.mail.body.msg") + " " + cardURL.toString();
	String mailBody = cardURL.toString();
			
 %> 
				<div class="buttonPanel">
				 <ul>
						<li class="back"
							onmousedown="downBackBut(this)" 
							onmouseup="upBackButton(this)" 
							onmouseover="overBackButton(this)" 
							onmouseout="upBackButton(this)">
							<a onclick="submitForm('<%= CardPortlet.BACK_ACTION %>', this);" href="#" >
								<div class="ico_back img">&nbsp;</div>
									<fmt:message key="view.page.back.link" />
							</a>
						</li>	
						<li
							onmousedown="downButton(this)" 
							onmouseup="upButton(this)" 
							onmouseover="overButton(this)" 
							onmouseout="upButton(this)">
							<a href="/portal/auth/portal/dbmi" >
								<div class="ico_home img">&nbsp;</div>
									<p><fmt:message key="view.page.home.link" /></p>
							</a>
						</li>	
               		</ul>
				</div>
                    </td>
                    <td></td>
                    <td>
                    	<div class="buttonPanel">
							<ul>
<%		if (sessionBean.getActiveCardInfo().isCanChange()) { %> 
					<!-- TODO: вынужденный хардкод. тэг btn не цепляет javaExpression -->
					<btn:button tooltipKey="tool.edit"
						onClick="submitForm('MI_EDIT_CARD_ACTION', this);"
						icon="ico_edit"/>					
<%		} %>                             
<%		if (sessionBean.getActiveCardInfo().isCanCreate()) { %> 
					<!-- TODO: вынужденный хардкод. тэг btn не цепляет javaExpression -->
					<btn:button tooltipKey="tool.copy"
						onClick="submitForm('MI_CLONE_CARD_ACTION', this);"
						icon="ico_clone"/>						    									
<%		} %>                        
					<btn:button tooltipKey="tool.export.card.xls"
						onClick="submitForm_Export(this);"
						icon="ico_xls"/>
<%-- --------------- начало: Мульти-кнопка Печать ------------------------------------------   --%>
<%
	String pdfMergeServletPath = request.getContextPath() + "/PdfMergeServlet?";
	String servletPath = request.getContextPath() + "/servlet/JasperReportServlet?";
	String card_id = "L_"+((Long)card.getId().getId()).toString();
	
	ObjectId incomingId = ObjectId.predefined(Template.class, "jbr.incoming");
	ObjectId outcomingId = ObjectId.predefined(Template.class, "jbr.outcoming");
	ObjectId innerId = ObjectId.predefined(Template.class, "jbr.interndoc");
	ObjectId npaId = ObjectId.predefined(Template.class, "jbr.npa");
	ObjectId ordId = ObjectId.predefined(Template.class, "jbr.ord");
	ObjectId izId = ObjectId.predefined(Template.class, "jbr.infreq");
	ObjectId ogId = ObjectId.predefined(Template.class, "jbr.incomingpeople");
	ObjectId indId = ObjectId.predefined(Template.class, "jbr.independent.resolution");
	
	ObjectId deloState = ObjectId.predefined(CardState.class, "delo");
	ObjectId draftState = ObjectId.predefined(CardState.class, "draft");
	ObjectId prepState = ObjectId.predefined(CardState.class, "preparation");
	ObjectId agrState = ObjectId.predefined(CardState.class, "agreement");
	ObjectId signState = ObjectId.predefined(CardState.class, "sign");
	ObjectId preRegState = ObjectId.predefined(CardState.class, "before-registration");
	ObjectId trashState = ObjectId.predefined(CardState.class, "trash");
	
	List items = new LinkedList();
	Map item = new HashMap();
	
	String arch_report_card_id = null;
	if(card.getState().equals(deloState)){
		CardLinkAttribute docHistoryAttribute = (ObjectId.predefined(CardLinkAttribute.class, "jbr.docHistory")!=null)?
			(CardLinkAttribute)card.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.docHistory")):null;
		if (docHistoryAttribute!=null&&!docHistoryAttribute.getIdsLinked().isEmpty()) {
			arch_report_card_id = docHistoryAttribute.getSingleLinkedId().getId().toString();
					
			String downloadServletPath = FileTag.DOWNLOAD_SERVLET+"?";
			String urlInside= downloadServletPath + "MI_CARD_ID_FIELD=" + arch_report_card_id;
			
			item = new HashMap();
			item.put("name", "Архив");
			item.put("fun", "window.open('"+urlInside+"')");
			items.add(item);
		}
		
	}

		// Диалог печати регистрационного штампа
	String serverPath = (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + ":" + request.getServerPort();	
	String urlStamp = serverPath+request.getContextPath()+"/jsp/RegistrationStamp.jsp?"+
				"namespace=" + renderResponse.getNamespace();
%>
		<script type="text/javascript">
			dojo.require("dijit.Dialog");
			
			function execSelectRegion(pos) {
				dijit.byId('positionStamp').hide();
				url = '<%=urlStamp%>';
				url = url + '&position='+pos;
				window.open(url); 
			}
		</script>

		<div dojoType="dijit.Dialog" id="positionStamp" title="Расположение штампа">
			<table style="width: 150px; border-collapse: collapse;" >
				<tr>
					<td class="selectRegion" onClick="execSelectRegion(1)">1</td>
					<td class="selectRegion" onClick="execSelectRegion(2)">2</td>
					<td class="selectRegion" onClick="execSelectRegion(3)">3</td>
					<td class="selectRegion" onClick="execSelectRegion(4)">4</td>					
				</tr>			
				<tr>
					<td class="selectRegion" onClick="execSelectRegion(5)">5</td>
					<td class="selectRegion" onClick="execSelectRegion(6)">6</td>
					<td class="selectRegion" onClick="execSelectRegion(7)">7</td>
					<td class="selectRegion" onClick="execSelectRegion(8)">8</td>
				</tr>
			</table>
		</div>
<%
	ObjectId cardState = card.getState();

//Если статус карточки не Корзина, Черновик, Подготовка, Подписание, Согласование, Регистрация
	if(!cardState.equals(draftState)
			&& !cardState.equals(prepState)
			&& !cardState.equals(agrState)
			&& !cardState.equals(signState)
			&& !cardState.equals(preRegState)
			&& !cardState.equals(trashState)) {
		//Кнопка "Печать регистрационного штампа" выводится только для Входящих, Исходящих и Внутренних документов
		if (card.getTemplate().equals(incomingId) || card.getTemplate().equals(outcomingId) || 
			card.getTemplate().equals(ogId)	|| card.getTemplate().equals(innerId) || 
			card.getTemplate().equals(npaId)	|| card.getTemplate().equals(ordId)) {
			item = new HashMap();
			item.put("nameKey", "print.select.printStamp");
			item.put("fun", "dijit.byId('positionStamp').show()");
			items.add(item);
		}
	// -----------------------------------------------------------------------------------------
		// Печать номера и дата на бланке для карточки Исходящей
		if (card.getTemplate().equals(outcomingId)) {
			ObjectId attrNumReg = ObjectId.predefined(StringAttribute.class, "regnumber");
			ObjectId attrDateReg = ObjectId.predefined(DateAttribute.class, "regdate");
			String numReg = "S_"+((StringAttribute)card.getAttributeById(attrNumReg)).getStringValue();
			String dateReg = "D_";
			Date date = ((DateAttribute)card.getAttributeById(attrDateReg)).getValue();
			if (date != null) {
				dateReg += (new SimpleDateFormat(JasperReportServlet.DATE_FORMAT)).format(date);
			}
			
			String urlBlank = servletPath+"nameConfig=reportChartBlank"+"&numReg="+numReg+"&dateReg="+dateReg;
			
			item = new HashMap();
			item.put("nameKey", "print.select.printReg");
			item.put("fun", "window.open('"+urlBlank+"')");
			items.add(item);
		}
	}
// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------
if(!card.getState().equals(deloState)){
// Печать формы Входящего
	if (card.getTemplate().equals(incomingId)) {
		String urlIncoming = servletPath+"nameConfig=reportChartIncoming"+"&card_id="+card_id;
		item = new HashMap();
		item.put("nameKey", "print.select.printIncoming");
		item.put("fun", "window.open('"+urlIncoming+"')");		items.add(item);
	}
// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------
// Печать формы ОРД
	//ObjectId ordId = ObjectId.predefined(Template.class, "jbr.ord");
	if (card.getTemplate().equals(ordId)) {
		String urlORD = servletPath+"nameConfig=reportChartORD"+"&card_id="+card_id;
		item = new HashMap();
		item.put("nameKey", "print.select.printORD");
		item.put("fun", "window.open('"+urlORD+"')");		items.add(item);
	}
	
	// -----------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------
	// Печать формы НПА
		//ObjectId ordId = ObjectId.predefined(Template.class, "jbr.ord");
		if (card.getTemplate().equals(npaId)) {
			String urlORD = servletPath+"nameConfig=reportChartORD"+"&card_id="+card_id;				
			item = new HashMap();
			item.put("nameKey", "print.select.printNPA");
			item.put("fun", "window.open('"+urlORD+"')");			items.add(item);
		}
// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------
// Печать формы Исходящего
	if (card.getTemplate().equals(outcomingId)) {
		String urlOutgoing = servletPath+"nameConfig=reportChartOutgoing"+"&card_id="+card_id;	
		item = new HashMap();
		item.put("nameKey", "print.select.printOutgoing");
		item.put("fun", "window.open('"+urlOutgoing+"')");		items.add(item);
	}
// -----------------------------------------------------------------------------------------
// -----------------------------------------------------------------------------------------
// Печать формы Внутренний
	if (card.getTemplate().equals(innerId)) {
		String urlInside = servletPath+"nameConfig=reportChartInside"+"&card_id="+card_id;
		item = new HashMap();
		item.put("nameKey", "print.select.printInner");
		item.put("fun", "window.open('"+urlInside+"')");		items.add(item);
	}
// -----------------------------------------------------------------------------------------
// Печать формы ОГ
	if (card.getTemplate().equals(ogId)) {
		String urlOG = servletPath+"nameConfig=reportChartOG"+"&card_id="+card_id;
		item = new HashMap();
		item.put("nameKey", "print.select.printOG");
		item.put("fun", "window.open('"+urlOG+"')");		items.add(item);
	}
// -----------------------------------------------------------------------------------------
// Печать формы ИЗ
	if (card.getTemplate().equals(izId)) {
		String urlIZ = servletPath+"nameConfig=reportChartIZ"+"&card_id="+card_id;			
		item = new HashMap();
		item.put("nameKey", "print.select.printInfReq");
		item.put("fun", "window.open('"+urlIZ+"')");		items.add(item);
	}
	
// -----------------------------------------------------------------------------------------
// Печать формы Независимого поручения
	if (card.getTemplate().equals(indId)) {		
		String urlIR = servletPath+"nameConfig=reportChartIR"+"&card_id="+card_id;	
		item = new HashMap();
		item.put("nameKey", "print.select.printIndepRes");
		item.put("fun", "window.open('"+urlIR+"')");		items.add(item);
	}
}
	// -----------------------------------------------------------------------------------------	
	// Диалог печати вложений

	if (card.getTemplate().equals(incomingId) ||
			card.getTemplate().equals(innerId) ||
			card.getTemplate().equals(outcomingId) ||
			card.getTemplate().equals(ogId) ||
			card.getTemplate().equals(ordId) ||
			card.getTemplate().equals(npaId) ||
			card.getTemplate().equals(indId))
	{
	%>
	<script type="text/javascript">
		dojo.require("dijit.form.Form");
		dojo.require("dijit.form.CheckBox")
		dojo.require("dijit.form.Button")

		function getCheckedValues() {
			var printCardIds = [];
			var urlEnvelope = '<%=pdfMergeServletPath%>';

			var obj = dojo.byId('print_form');
			if (obj != null) {
			with(dojo.byId('print_form'))
				if (checked){
					urlEnvelope=urlEnvelope + value;
				}
			}

			dojo.query(".print_attach").forEach(function(box){
				var widget = dijit.byNode(box);
				if (widget.checked)
					printCardIds.push(widget.value);
			})

			if (printCardIds.length > 0)
				urlEnvelope = urlEnvelope + "&cardIds=" + printCardIds.join();

			if (urlEnvelope != '<%=pdfMergeServletPath%>' ) {
				dijit.byId('printAttachmentsDialog').hide();
				window.open(urlEnvelope); 
			}else {
				alert("<fmt:message key='print.empty.list'/>");
			}

			return false;
		}
		
		
	<% if(card.getState().equals(deloState)) { 
			if (null != arch_report_card_id) {%>
		function checkAll(setting) {
			dijit.byId('print_<%=arch_report_card_id%>').attr("checked", setting);
			dojo.query(".print_attach").forEach(function(box){
				dijit.byNode(box).attr("checked", setting);
			})
		}
	<%		}
		} else { %>
		function checkAll(setting) {
			dijit.byId('print_form').attr("checked", setting);
			dojo.query(".print_attach").forEach(function(box){
				dijit.byNode(box).attr("checked", setting);
			})
		}
	<% } %>


	</script>

		<div dojoType="dijit.Dialog" id="printAttachmentsDialog" title="<fmt:message key="print.title"/>">
	<%
			String printFormItemLabel = "";
			String printFormItemUrl = "";

			if (card.getTemplate().equals(incomingId)) {
				printFormItemLabel = "print.select.printIncoming";
				printFormItemUrl = "nameConfig=reportChartIncoming"+"&card_id="+card_id;
			}else if (card.getTemplate().equals(innerId)) {
				printFormItemLabel = "print.select.printInner";
				printFormItemUrl = "nameConfig=reportChartInside"+"&card_id="+card_id;
			}else if (card.getTemplate().equals(outcomingId)) {
				printFormItemLabel = "print.select.printOutgoing";
				printFormItemUrl = "nameConfig=reportChartOutgoing"+"&card_id="+card_id;	
			}else if (card.getTemplate().equals(ogId)) {
				printFormItemLabel = "print.select.printOG";
				printFormItemUrl = "nameConfig=reportChartOG"+"&card_id="+card_id;
			}else if (card.getTemplate().equals(ordId)) {
				printFormItemLabel = "print.select.printORD";
				printFormItemUrl = "nameConfig=reportChartORD"+"&card_id="+card_id;
			}else if (card.getTemplate().equals(npaId)) {
				printFormItemLabel = "print.select.printNPA";
				printFormItemUrl = "nameConfig=reportChartORD"+"&card_id="+card_id;
			}else if (card.getTemplate().equals(indId)) {
				printFormItemLabel = "print.select.printIndepRes";
				printFormItemUrl = "nameConfig=reportChartIR"+"&card_id="+card_id;
			}
	%>
	 <table width="100%">
		<col width="10%">
		<col width="90%">

	<% if(card.getState().equals(deloState)) { 
			if (null != arch_report_card_id) {%>
		<tr>
			<td>
						<input id="print_<%=arch_report_card_id%>" name="print[]" dojoType="dijit.form.CheckBox" value="<%=arch_report_card_id%>"
							class="print_attach" checked/>
					</td>
					<td style="text-align: left">
						<label for="print_<%=arch_report_card_id%>"><fmt:message key="print.select.printArchive"/></label>
					</td>
				</tr>
	<%		}
		} else { %>
			<tr>
				<td>
				<input id="print_form" name="print[]" dojoType="dijit.form.CheckBox" value='<%=printFormItemUrl%>' checked/>
			</td>
			<td style="text-align: left">
				<label for="print_form"><fmt:message key='<%=printFormItemLabel%>'/></label>
			</td>
		</tr>
	<%
		}
			ObjectId doclinks = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
			ObjectId materialName = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
			CardLinkAttribute cardLinkAttr = (CardLinkAttribute) card.getAttributeById(doclinks);
			if (cardLinkAttr != null) 
			{
				Iterator<?> iterAttach = cardLinkAttr.getIdsLinked().iterator();
				if (iterAttach.hasNext()) {
	%>
					<tr>
						<td></br>Вложения:</td>
					</tr>
	<%
				}
				while (iterAttach.hasNext()) 
				{
					ObjectId attachId = (ObjectId) iterAttach.next();
					Card attachCard;
					try{
						attachCard = (Card) sessionBean.getServiceBean().getById(attachId);
					} catch (DataException e){
						continue;
					}
					StringAttribute materialNameAttr = (StringAttribute) attachCard.getAttributeById(materialName);
	%>
					<tr>
						<td>
							<input id="print_<%=attachId.getId()%>" name="print[]" dojoType="dijit.form.CheckBox" value="<%=attachId.getId()%>"
								class="print_attach" checked>
						</td>
						<td style="text-align: left">
					<% if (null != materialNameAttr) {%>
							<label for="print_<%=cardId%>"><%=materialNameAttr.getValue()%></label>
						</td>
					</tr>
	<%
					}
				}
			}
	%>
		</table>
		<br/>
		<button onclick="checkAll(true)">
			<fmt:message key="print.select.all"/>
		</button>
		<button onclick="checkAll(false)">
			<fmt:message key="print.clear.all"/>
		</button>
		<button onclick="dijit.byId('printAttachmentsDialog').hide()">
			<fmt:message key="confirm.cancel"/>
		</button>
		<button onclick="getCheckedValues()">
			<fmt:message key="print.ok"/>
		</button>
		</div>
	<%
		//Кнопка Печать вложений 
		item = new HashMap();
		item.put("nameKey", "print.select.printAttachments");
		item.put("fun", "dijit.byId('printAttachmentsDialog').show()");
		items.add(item);
	}

if(!card.getState().equals(deloState)){
	// -----------------------------------------------------------------------------------------	
	// Диалог печати адреса на конверте		
	%>
	<script type="text/javascript">
	dojo.require("dijit.Dialog");
	<%	ObjectId ATTR_METHOD_POST =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.post");
	ObjectId ATTR_METHOD_FLINK =
		ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.flink");
	ObjectId ATTR_METHOD =
		ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");
	ObjectId DISTR_LIST =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.Distribution.DistributionList");
	ListAttribute lAttr = null;
	ObjectId recipientId = ObjectId.predefined(Template.class, "jbr.DistributionListElement");
	ObjectId activeTabId=tabsManager.getActiveTab().getId();
	%>
	
	
	//функция обработки выбора формата конверта
	function execSelectEnvelope(env) {
		dijit.byId('choiceEnvelope').hide();
		<%	
		String string_card_ids = null;
		//если печать конверта вызывается из карточки Рассылка
		if(card.getTemplate().equals(recipientId)){
			lAttr = (ListAttribute) card.getAttributeById(ATTR_METHOD);
		if (lAttr!=null) 
		if(lAttr.getValue()!=null){
			if (ATTR_METHOD_POST.equals(lAttr.getValue().getId()) || ATTR_METHOD_FLINK.equals(lAttr.getValue().getId()))  {
				string_card_ids="S_"+((Long)card.getId().getId()).toString();
			}
		}
		else {%>
			dijit.byId('noSuchMethod').show();	
		<%}
		}
		
		//если печать конверта вызывается с вкладки Рассылка
		if(activeTabId.getId().toString().equals("287") ||
			activeTabId.getId().toString().equals("227") ||
			activeTabId.getId().toString().equals("385") ||
			activeTabId.getId().toString().equals("344")){
			CardLinkAttribute valueAttr =(CardLinkAttribute) card.getAttributeById(DISTR_LIST);
			ObjectId resId=null;
			Card res=null;
			if (valueAttr!=null){
				Iterator iter =  valueAttr.getIdsLinked().iterator();
				while (iter.hasNext()) {
					 resId = (ObjectId) iter.next();
				res = (Card) sessionBean.getServiceBean().getById(resId);
				lAttr = (ListAttribute) res.getAttributeById(ATTR_METHOD);
				if (lAttr!=null)
				if(lAttr.getValue()!=null)
					if (ATTR_METHOD_POST.equals(lAttr.getValue().getId()) || ATTR_METHOD_FLINK.equals(lAttr.getValue().getId()))  {
						if (string_card_ids==null)
							string_card_ids="S_"+((Long)resId.getId()).toString();
						else
							string_card_ids=string_card_ids+", "+ ((Long)resId.getId()).toString();	
					}
			}
			}
		}
		if(string_card_ids!=null && !string_card_ids.equals(""))		{
		%>
		var urlEnvelope='<%=servletPath%>';
		urlEnvelope=urlEnvelope+'nameConfig='+env;
		//urlEnvelope=urlEnvelope+'&exportFormat=DOCX';
		urlEnvelope=urlEnvelope+'&card_id=';
		urlEnvelope=urlEnvelope+'<%=string_card_ids%>';	
		window.open(urlEnvelope); 
		<%
		} else 
		{%>
		//dijit.byId('noSuchMethod').show();	
		<%}%> 
	}
	</script>	

	<!-- окошко с предупреждением, что не заполнено поле Метод отправки-->
<div id="noSuchMethod" dojoType="dijit.Dialog" title="<fmt:message key="print.envelope.message.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="print.envelope.message.text"/></div>	
		
		<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="print.envelope.message.ok"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('noSuchMethod').hide();					
				</script>
			</button>			
	</div>

<!-- меню для выбора формата конверта. в данный момент доступен только формат С5 -->									
<div dojoType="dijit.Dialog" id="choiceEnvelope" title="Печать конверта">
			<table style="width: 150px; border-collapse: collapse;" >
				<tr>
					<td>Конверт C6</td>
				</tr>
				<tr>
					<td>Конверт DL</td>
				</tr>				
				<tr>
					<td class="selectRegion" onClick="execSelectEnvelope('reportEnvelopeC5')">Конверт C5</td>
				</tr>				
				<tr>
					<td>Конверт C4</td>
				</tr>
				<tr>
					<td>Конверт B4</td>
				</tr>
			</table>
		</div>
<%
	//Кнопка "Печать адреса на конверте" выводится только для карточки Рассылки 

	if (card.getTemplate().equals(recipientId) || activeTabId.getId().toString().equals("287") ||
			activeTabId.getId().toString().equals("227") ||
			activeTabId.getId().toString().equals("385") ||
			activeTabId.getId().toString().equals("344")) {
				
		item = new HashMap();
		item.put("nameKey", "print.select.printEnvelope");
		item.put("fun", "dijit.byId('choiceEnvelope').show()");
		items.add(item);
	}
	
// -----------------------------------------------------------------------------------------
 // -----------------------------------------------------------------------------------------
// Печать резолюций
	
	if (card.getTemplate().equals(incomingId)|| card.getTemplate().equals(ordId)||
			card.getTemplate().equals(innerId)	|| card.getTemplate().equals(ogId)||
			card.getTemplate().equals(izId)) {	
		String urlRes = servletPath+"nameConfig=reportPrintResolution"+"&card_id="+card_id;
		item = new HashMap();
		item.put("nameKey", "print.select.printResolution");
		item.put("fun", "window.open('"+urlRes+"')");
		items.add(item);
	}
}
	// -----------------------------------------------------------------------------------------
	// Отчет «Связанные документы для регистрационного номера»
		if ((card.getTemplate().equals(outcomingId))||(card.getTemplate().equals(incomingId))||
				card.getTemplate().equals(innerId)	|| card.getTemplate().equals(ordId) ||
				card.getTemplate().equals(ogId)|| card.getTemplate().equals(npaId)) {
			String urlRes = servletPath+"nameConfig=relatedDocsForTheRegNum"+"&id="+card_id;
			item = new HashMap();
			item.put("nameKey", "print.linked.docs");
			item.put("fun", "window.open('"+urlRes+"')");
			items.add(item);
		}
if(!card.getState().equals(deloState)){
// -----------------------------------------------------------------------------------------
	// Печать резолюций для входящих, внутренних, ОРД. Кнопка "Печать - резолюция".
		ObjectId resolutionId = ObjectId.predefined(Template.class, "jbr.resolution");
		if (card.getTemplate().equals(resolutionId)) {
			String urlRes = servletPath+"nameConfig=reportPrintAssignment"+"&card_id="+card_id;
			item = new HashMap();
			item.put("nameKey", "print.select.printAssignment");
			item.put("fun", "window.open('"+urlRes+"')");
			items.add(item);
		}
	
	// -----------------------------------------------------------------------------------------
	// Отчет «Лист согласования»

// Диалог выбора сортировки Листа согласования
	%>

	<script type="text/javascript">
    dojo.require("dijit.form.Form");
    dojo.require("dijit.form.CheckBox")
    dojo.require("dijit.form.Button")
    
			
			
				function getCheckedValue() {
					
					with(dojo.byId('byDate'))
							if (checked){
								sort="byDate";
							}
					with(dojo.byId('byPerson'))
							if (checked){
								sort="byPerson";
							}


					dijit.byId('visaSortDialog').hide();
					var urlEnvelope='<%=servletPath%>';
					urlEnvelope=urlEnvelope+'nameConfig=negotiationList';
					//urlEnvelope=urlEnvelope+'&exportFormat=DOCX';
					urlEnvelope=urlEnvelope+'&card_id=';
					urlEnvelope=urlEnvelope+'<%=card_id%>';
					urlEnvelope=urlEnvelope+'&sort=S_';
					urlEnvelope=urlEnvelope+sort;
					//dataSource - тип источника данных
					//Формат строки - тип_источника_данных:класс_для_получения_источника_данных:параметры:путь_к_тегу_который_будет_записью
					urlEnvelope=urlEnvelope+'&dataSource=';
					urlEnvelope=urlEnvelope+'com.aplana.dbmi.jasperreports.NegotiationListReportXMLDataSource';	//Формат строки - тип_источника_данных:класс для получения источника данных:параметры 
					window.open(urlEnvelope); 
					
					return false;
				} 
	</script>

	

		<div dojoType="dijit.Dialog" id="visaSortDialog" title="Отчет Лист Согласования">
			<p>Сортировать лист согласования по:</p>
			<br/>
			<input type="radio" dojoType="dijit.form.RadioButton" name="sort" id="byDate" value="byDate" checked="checked"/>
		    		<label for="byDate">
		      			Дате согласования
		    		</label>
	    			<input type="radio" dojoType="dijit.form.RadioButton" name="sort" id="byPerson" value="byPerson" />
	    			<label for="byPerson">
	        			Согласующим
	    			</label>
	   				 <br/>
				<br/>
				<button onclick="getCheckedValue()">
					Напечатать лист согласования
				</button>
		</div>
		<%
		//Кнопка "Лист согласования"   
		outcomingId = ObjectId.predefined(Template.class, "jbr.outcoming");
	 innerId = ObjectId.predefined(Template.class, "jbr.interndoc");
	 npaId = ObjectId.predefined(Template.class, "jbr.npa");
	if (
		card.getTemplate().equals(outcomingId) || 
		card.getTemplate().equals(innerId) ||
		card.getTemplate().equals(npaId)||
		card.getTemplate().equals(ordId) 
	) {
		item = new HashMap();
		item.put("nameKey", "print.visa.list");
		item.put("fun", "dijit.byId('visaSortDialog').show()");
		items.add(item);
			
			
		}
	// -----------------------------------------------------------------------------------------
	
	// Отчет «Справка по проекту» 
	
		if (card.getTemplate().equals(npaId) || card.getTemplate().equals(ordId)){
				String urlRes = servletPath+"nameConfig=Project_Help&exportFormat=PDF&card_id="+card_id;
				item = new HashMap();
				item.put("nameKey", "print.project.help");
				item.put("fun", "window.open('"+urlRes+"')");
				items.add(item);
			}	
}				
		
if(!card.getState().equals(deloState)){
	//Печать карточки
	item = new HashMap();
	item.put("nameKey", "print.select.printCard");
	item.put("fun", "trustedAction=true; window.location.assign('"+printURL.toString()+"');");
	items.add(item);
}
		
	// -----------------------------------------------------------------------------------------	
	request.setAttribute("items", items);

%>
		<btn:selectButton textKey="button.print" id="selectPrint" icon="ico_print" items="${items}"/>
<!-- -------------------- конец: Мульти-кнопка Печать -->

            	</ul>
			</div>
                    </td>
                    <td style="float:right">
						<jsp:include page="CardButtonPane.jsp"/>	
					</td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
	      <!--Разделитель-->
	      <td colspan="4">
	        <hr/>
	      </td>
	    </tr>
       </table>
	</div>
	<script type="text/javascript" language="javascript">
		function initCardContainerHeight() {
			var windowHeight = document.documentElement.clientHeight == 0 ? document.body.clientHeight : document.documentElement.clientHeight;
			dojo.byId("fixedCardContainer").style.height = (windowHeight - (140 + dojo.byId("fixedCardHeader").clientHeight)) +"px"
		}

		dojo.connect(document,"DOMContentLoaded",function(){
			initCardContainerHeight();
		});
		
		dojo.connect(window,"resize",function(){
			initCardContainerHeight();
		});
	</script>
	<div id="fixedCardContainer">
    <table class="indexCardMain">
        <col Width="50%" />        
        <col Width="50%" />
        <!--Заголовок-->
        <!-- (2010/07/28, JBOSS00000620) Убран неинформативный текст
        <tr>
            <td colspan="4">
                <div class="icHeader">
	                <fmt:message key="view.page.title" />
                </div>
            </td>
        </tr>
        -->
        

		<!--  Заголовок: название карточки -->
		<tr>
<%			final Attribute attributeCardName = sessionBean.getActiveCard().getAttributeById(Attribute.ID_NAME);
			final String headStr = (attributeCardName != null) 
					? attributeCardName.getStringValue()
					: "";
%>
			<td colspan="4">
				<div class="icHeader">
					<%= headStr %>
				</div>
			</td>
		</tr>

		<%@include file="CardFeatures.jspf"%>
		
		<!-- Информационный заголовок: шаблон, статус, код карточки -->
        <tr>
          <td colspan="2">
            <table width="100%">
              <col width="*">
              <col align="right" width="32px">
              <tr>
                <td>
                  <div id="BODY_info_header" style="height: auto; display: none;">		  		
                    <table width="100%">
                    <tr>
                      <td>
                        <div class="divPadding">	<!-- Template name and card name -->
                          <table class="content" >
                            <col Width="45%"/>
                            <col Width="55%"/>
                            <tr>
                              <td><fmt:message key="edit.page.template.label" /></td>
                              <td><%= sessionBean.getActiveCard().getTemplateName()  %></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="edit.page.status.label" /></td>
                              <td><%=sessionBean.getActiveCardInfo().getCardState().getName()%></td>
                            </tr>
                          </table>
                        </div>
                      </td>
                      <td>
                        <div class="divPadding">
                          <table class="content" >
                            <col Width="45%" />
                            <col Width="55%"/>
                            <tr>
                              <td>
                                <!-- "Код карточки:" -->
                                <fmt:message key="edit.page.card.id.label" />
                              </td>
                              <td><%= cardId %>	</td>
                            </tr>
                            <tr>
                              <%-- "Отвественный редактор"	--%>
                              <td> </td>
                              <td> </td>
                            </tr>
                          </table>
                        </div>
                      </td>
                    </tr>
                    </table>
                  </div>
                </td>
                <td>
                  <a HREF="javascript:form_collapse('info_header')" class="noLine">  
                    <span  class="arrow_up" id="ARROW_info_header">&nbsp;</span>
                  </a>
                </td>
              </tr>
            </table>
          </td>
        </tr>
		
		<tr><!--Вкладки-->
<%			if (tabsManager.getVisibleTabs().size() > 0){
				final List tabs = tabsManager.getVisibleTabs(); %>
		  <td class="tabsContent" colspan="2">		
			<div class="tabmenu">
			  <ul>
<%				for (ListIterator i = tabs.listIterator(); i.hasNext(); ){
					final TabViewParam tab = (TabViewParam)i.next();		
					PortletURL tabURL = renderResponse.createActionURL();
					tabURL.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.CHANGE_TAB_CARD_ACTION); 
					tabURL.setParameter(CardPortlet.CARD_TAB_ID, tab.getId().getId().toString());
				    if (tabsManager.isTabActive(tab)){				%>
				<li class="selectTab">
					<a href="javascript: submitForm_TabChange('<%= tab.getId().getId().toString() %>', this);" >
					  <%= tab.getName() %>
					</a>
				</li>
<%					}else{							 %>
				<li class="tab">
					<a href="javascript: submitForm_TabChange('<%= tab.getId().getId().toString() %>', this);" >
					  <%= tab.getName() %>
					</a>
				</li>
<%					} 										 %>	
<%				}									 %>				    
			  </ul>
			</div>
<% 			} 										 %>	
		  </td>
		</tr> <!--Вкладки-->
		
		<tr class="cardContent"><!--Контент-->
		  <td class="cont" colspan="2">
		   <table width="100%">
			<col Width="50%" />
			<col Width="50%"/>
			<tr>
			  <td>			<!-- Left column -->
<%		  List blocks; 			%>
<%		  String regionID = "1"; %>
				<%@include file="TabRegion.jspf"%>
			  </td>		
		    
			  <td>	<!-- Right column -->
<%		  regionID = "2"; %>
				<%@include file="TabRegion.jspf"%>
			  </td>	
			</tr>
	    	<tr>	<!-- Bottom column -->
			  <td colspan="2">
<%		  regionID = "3"; %>
				<%@include file="TabRegion.jspf"%>
			  </td>	
			</tr>
		   </table>
		 </td> 
		</tr>	
		<tr>
		  <td colspan="2">
<%		if ((((TabView)tabsManager.getActiveTab()).getContainer().getRegion("1").size()==0)&&
			(((TabView)tabsManager.getActiveTab()).getContainer().getRegion("2").size()==0)&&
			(((TabView)tabsManager.getActiveTab()).getContainer().getRegion("3").size()==0)){ 		  
		  		regionID = "ALL"; %>
			<%@include file="TabRegion.jspf"%>
<%		} %>			
		  </td>
		</tr>	
    </table>
  </div>
  <div id="fixedButtomLinks">
  <div class="bottomLinks">
	<div class="imgContainer horizontal back">
	  <a onclick="submitForm('<%= CardPortlet.BACK_ACTION %>');" href="#" >
    	<fmt:message key="view.page.back.link" />
	  </a>
	</div>
	<div class="imgContainer horizontal home">
	  <a href="<%= portletService.generateLink("dbmi.defaultPage", null, null, renderRequest, renderResponse) %>">
    	<fmt:message key="view.page.home.link" />
	  </a>
	</div>
  </div>
  </div>
</form>
	<jsp:include page="CardPageFunctions.jsp"/>
	<%@include file="CardDialog.jspf"%>
	<%@include file="AttributeEditorDialogBlue.jspf"%>

<c:if test="<%= renderRequest.getParameter(CardPortlet.SHOW_BARCODE_PRINT_DIALOG) != null%>">
	<script type="text/javascript">
		dojo.addOnLoad(function(){
      		dijit.byId('positionStamp').show();
  		});
	</script>
</c:if>   
<c:if test="<%= renderRequest.getParameter(CardPortlet.PRINT_BLANK) != null %>">
<%
	ObjectId attrNumReg = ObjectId.predefined(StringAttribute.class, "regnumber");
	ObjectId attrDateReg = ObjectId.predefined(DateAttribute.class, "regdate");
	String numReg = "S_"+((StringAttribute)card.getAttributeById(attrNumReg)).getStringValue();
	String dateReg = "D_";
	Date date = ((DateAttribute)card.getAttributeById(attrDateReg)).getValue();
	if (date != null) {
		dateReg += (new SimpleDateFormat(JasperReportServlet.DATE_FORMAT)).format(date);
	}
	
	String urlBlank = servletPath+"nameConfig=reportChartBlank"+"&numReg="+numReg+"&dateReg="+dateReg;
%>
	<script type="text/javascript">
		dojo.addOnLoad(function() {
			window.open('<%=urlBlank%>')
		});
	</script>
</c:if>


<% 	//Разблокируем открытые на редактирование карточки при выходе пользователя
	//карточек открытых на редактировании может быть несколько, собирем их айди через запятую
	String lockedCardId = "";
	int cnt = 0;
	List<CardPortletCardInfo> listCards = sessionBean.getAllOpenedActiveCards();
	if (!listCards.isEmpty()) {
		cnt = listCards.size();
		for (CardPortletCardInfo info : listCards) {
			lockedCardId += info.getCard().getId().getId().toString() + ",";
		}
		//обрезаем последнюю запятую
		lockedCardId = lockedCardId.substring(0, lockedCardId.length()-1);
	}
%>

<script type="text/javascript" language="javascript">
dojo.require('dijit.Dialog');
window.onbeforeunload = function(evt) {
	if (document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value == "" && <%= !lockedCardId.isEmpty() %>  && !trustedAction) {
		return "<fmt:message key="edit.warning.browser.away"><fmt:param value="<%= lockedCardId %>"/></fmt:message>";
	}
}
window.onunload = function(e) {
	if (document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value == "" && <%= !lockedCardId.isEmpty() %> && !trustedAction) {
		var result = dojo.xhrGet({
			//отправляем только один id, остальные достанет lockManagement по сессии и пользователю
			url: '<%=request.getContextPath()%>/UnlockCard?id=<%=lockedCardId.split(",")[0]%>',
			sync: true,
			preventCache: true,
			//timeout: 5000,
			load: function() {
				console.log('Unlocked successfully');
				window.location.pathname = '/portal/signout/'
			}
		});
	}
}
</script>

<% 
	// PRINT Mode
	if (isPrintMode) { %>

<script>

function showPrintPage() {

var datatable = document.getElementById('printTableDiv').innerHTML;
var NewWindow = window.open("about:blank",'_newtab'); //Замечание: "_newtab" - работает только в Firefox
var NewWinDoc = NewWindow.document;
NewWinDoc.writeln('<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"');
NewWinDoc.writeln('   "http://www.w3.org/TR/html4/loose.dtd">');

NewWinDoc.writeln('<html><head>');

NewWinDoc.writeln('<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">');
	

NewWinDoc.writeln('<LINK rel=\"stylesheet\" href=\"/DBMI-Portal/theme/dbmi_style.css\" type=\"text/css\">');

NewWinDoc.writeln('<style type=\"text/css\">');
NewWinDoc.writeln(' @import <%= renderResponse.encodeURL(renderRequest.getContextPath() + "/print/print.css") %> print; /* Стиль для печати */');
NewWinDoc.writeln('</style>');

NewWinDoc.writeln('</head><body>');
NewWinDoc.writeln(datatable);
NewWinDoc.writeln('</body></html>');
NewWinDoc.close();
}

</script>

<div style="display: none;" id="printTableDiv" > 
<jsp:include flush="true" page="/WEB-INF/jsp/html/PrintCardView.jsp" />    
</div>  

<script language="javascript">
	showPrintPage();
</script>

<% 
	} 		// end print mode					%>    
<%
	}
// end main check	
 %> 
