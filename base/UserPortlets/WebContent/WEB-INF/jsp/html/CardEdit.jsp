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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags/dbmi" prefix="dbmi"%>
<%@ taglib prefix="btn" uri="http://aplana.com/dbmi/tags" %>

<%@page import="java.util.*"%>
<%@page import="javax.portlet.*"%>
<%@page import="com.aplana.dbmi.card.*"%>
<%@page import="com.aplana.dbmi.gui.*"%>
<%@page import="com.aplana.dbmi.model.TemplateBlock"%>
<%@page import="com.aplana.dbmi.model.BlockViewParam"%>
<%@page import="com.aplana.dbmi.model.TabViewParam"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.PersonAttribute"%>
<%@page import="com.aplana.dbmi.model.ListAttribute"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.Card"%>
<%@page import="com.aplana.dbmi.model.Template"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage"%>

<%@page import="com.aplana.dbmi.Portal"%>
<%@page import="com.aplana.web.tag.util.StringUtils"%>
<portlet:defineObjects/>

<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource" scope="request"/>

<script type="text/javascript">
	function f5press(e) {
		//запрещаем нажатие F5 и Ctrl+r 
		if(e.keyCode == 116 || (e.keyCode == 82 && e.ctrlKey)) {
			return false;
		}
	}

	window.onkeydown = function (event) { 
		return f5press(event);
	}
</script>
<script src="/DBMI-UserPortlets/js/blockscroll.js"></script>

<%
	CardPortletSessionBean sessionBean = (CardPortletSessionBean)renderRequest.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);

	//////////////////
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
	//////////////////
	/*String message = sessionBean.getMessage();
	if( message != null) {
		sessionBean.setMessage(null);
	} else {
		message = renderRequest.getParameter(CardPortlet.MSG_PARAM_NAME);
	}*/

	String cardId = sessionBean.getActiveCard().getId() != null ? sessionBean.getActiveCard().getId().getId().toString() : "";  
	Card card = sessionBean.getActiveCard();  
	String adminEmail = sessionBean.getAdminEmail() != null ? sessionBean.getAdminEmail().trim() : "";
	TabsManager tabsManager = sessionBean.getActiveCardInfo().getTabsManager();
 %>                

<script type="text/javascript" language="javascript">

	dojo.require('dijit.Dialog');

	var editorEventManager = {
		editors: {},
		subscriptions: {},		
		registerAttributeEditor: function(attrCode, attrHtmlId, isInline, value) {
			var editorData = this.editors[attrCode];
			if (!editorData) {
				var editorData = {
					'attrCode': attrCode,
					'attrHtmlId': attrHtmlId,
					'isInline': isInline,
					'value': value
				};
				this.editors[attrCode] = editorData;
				this.notifyValueChanged(attrCode, value);
			}					
		},
		subscribe: function(subscriberAttrCode, valueAttrCode, functionName, functionParameter) {
			var subscription = {
				'subscriberAttrCode': subscriberAttrCode,
				'functionName': functionName,
				'functionParameter': functionParameter
			};

			var subscribers = this.subscriptions[valueAttrCode];
			var isAlreadySubscribed = false;
			if (subscribers) {
				for(var i = 0; i < subscribers.length; ++i) {
					var existing = subscribers[i];
					if (existing.subscriberAttrCode == subscriberAttrCode &&
						existing.functionName == functionName &&
						existing.functionParameter == functionParameter) {
						isAlreadySubscribed = true;
					}
				}
				if (!isAlreadySubscribed) {
					subscribers.push(subscription);
				}
			} else {
				this.subscriptions[valueAttrCode] = [subscription];
			}

			var editorData = this.editors[valueAttrCode];
			var sData = this.editors[subscription.subscriberAttrCode];
			if (!isAlreadySubscribed && editorData && sData) {
				eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, editorData.value, subscription.functionParameter);');
			}
		},
		notifyValueChanged: function(attrCode, value) {
			var editorData = this.editors[attrCode];
			if (!editorData) {
				console.error('Not registered attribute code: ' + attrCode);
				return;
			}
			else {
				editorData.value = value;
			}
			var subscribers = this.subscriptions[attrCode];
			if (subscribers) {
				for(var i = 0; i < subscribers.length; ++i) {
					var subscription = subscribers[i];
					var sData = this.editors[subscription.subscriberAttrCode];
					if (sData) {
						eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, value, subscription.functionParameter);');
					}
				}
			}
		}
	};
	
</script>

<script type="text/javascript" language="javascript" src="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/js/mail.js") %>">
</script>
<script type="text/javascript" language="javascript">
function submitForm_TabChange(tabId, elem) { 
	if (elem) elem.onClick = function() { return false };
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.CHANGE_TAB_CARD_ACTION %>';
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_TAB_ID %>.value = tabId;
   	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}
function submitForm_Sort(href, elem) {
	if (elem) elem.onClick = function() { return false };
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.SORT_ACTION %>';
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.SORT_HREF %>.value = href;
	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}

function submitForm(action, elem) { 
	if (elem) elem.onClick = function() { return false };
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = action;
	document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_TAB_ID %>.value = '<%= tabsManager.getActiveTab().getId().getId().toString() %>';
	document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
}
</script>
<%--<dbmi:message text="<%= message %>"/>--%>

<form name="<%= CardPortlet.EDIT_FORM_NAME %>" method="post" action="<portlet:actionURL/>"> 
  <input type="hidden" name="<%= CardPortlet.ACTION_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.ATTR_ID_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.CARD_ID_FIELD %>" value="">
  <input type="hidden" name="<%= LinkChooser.CONTENT_TYPE %>" value="">
  <input type="hidden" name="<%= CardPortlet.CARD_MODE %>" value="<%= CardPortlet.CARD_EDIT_MODE %>"/>
  <input type="hidden" name="<%=CardPortlet.COLLAPSE_ID_BLOCKS %>" id="<%=CardPortlet.COLLAPSE_ID_BLOCKS %>"/>
  <input type="hidden" name="<%= CardPortlet.FIELD_THIS_PAGE %>" value="<portlet:renderURL/>"/>
  <input type="hidden" name="<%= CardPortlet.CARD_TAB_ID %>" value="">
  <input type="hidden" name="<%= CardPortlet.DIALOG_ACTION_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.DIALOG_INPUT_PARAM_NAME %>" value="">
  <input type="hidden" name="<%= CardPortlet.DISABLE_DS %>" value="">  
  
  <input type="hidden" name="<%= CardPortlet.DIALOG_EDITOR_ACTION_FIELD %>" value="">
  <input type="hidden" name="<%= CardPortlet.DIALOG_EDITOR_VALUE %>" value="">
  <input type="hidden" name="<%= CardPortlet.SORT_HREF %>" value="">  
  <input type="hidden" name="<%= CardPortlet.STAMP_POSITION %>" value="">
<%
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
%>
<div id="fixedCardHeader">
<% if (message != null) {%>

	<table class="<%= messageStyle %>">
		<tr  class="tr1"><td class="td_11"/><td class="td_12"/><td class="td_13"/></tr>
		<tr class="tr2"><td class="td_21"/><td class="td_22">
		<%= StringUtils.replaceNewlineWithBreak(message) %>
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
	
<%} %>
	<table class="indexCardMain">
		<col Width="50%" />        
        <col Width="50%" />
        <tr>
	      <!--Заголовок-->
	      <td>
	      </td>
	      <td>
	          <div id="rightIcons" style="width: 100%; margin:0;" >
	            <jsp:include page="CardButtonPane.jsp"/>
	          </div>
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
		dojo.byId("fixedCardContainer").style.height = (windowHeight - (110 + dojo.byId("fixedCardHeader").clientHeight)) +"px"
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
    
	<!--  Заголовок: название карточки -->
	<tr>
<%		final Attribute attributeCardName 
			= sessionBean.getActiveCard().getAttributeById(Attribute.ID_NAME);
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
				final List tabs = tabsManager.getVisibleTabs();	%>
		  <td class="tabsContent" colspan="2">		
			<div class="tabmenu">
			  <ul>
<%				for (ListIterator i = tabs.listIterator(); i.hasNext(); ){
					final TabViewParam tab = (TabViewParam)i.next();							
				    if (tabsManager.isTabActive(tab)){				%>
				<li class="selectTab">
					<a href="#" onclick="submitForm_TabChange('<%= tab.getId().getId().toString() %>', this);" >
					  <%= tab.getName() %>
					</a>
				</li>
<%					}else{							 %>
				<li class="tab">
					<a href="#" onclick="submitForm_TabChange('<%= tab.getId().getId().toString() %>', this);" >
					  <%= tab.getName() %>
					</a>
				</li>
<%					}								 %>					  
<%				}									 %>				    
			  </ul>
			</div>
<%			} 										 %>	
		  </td>
		</tr><!--Вкладки-->
		
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
</form>  
<jsp:include page="CardPageFunctions.jsp"/>


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
var intercepted_href = '';
window.onbeforeunload = function(evt) {
	var isInternalRequest = getUrlVars(intercepted_href)["<%= CardPortlet.INTERNAL_REQUEST_FLAG_FIELD %>"];
	if (document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value == "" && "true" != isInternalRequest && <%= !lockedCardId.isEmpty() %>) {
		return "<fmt:message key="edit.warning.browser.away"><fmt:param value="<%= cardId %>"/></fmt:message>";
	}
}
window.onunload = function(e) {
	var isInternalRequest = getUrlVars(intercepted_href)["<%= CardPortlet.INTERNAL_REQUEST_FLAG_FIELD %>"];
	intercepted_href = "";
	if (document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value == "" && "true" != isInternalRequest) {
		var result = dojo.xhrGet({
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
//вырезаем из ссылки значение переданного параметра
function getUrlVars(href) {
	   var vars = {};
	   var parts = href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
	       vars[key] = value;
	   });
	   return vars;
}
//перехват ссылки при сортировке, результат записывается в глобальную переменную intercepted_href
(function() {
	var refs=dojo.query("th.sortable a");
	for (var i=0, L=refs.length; i<L; i++) {
	    refs[i].onclick=function() {
	    	intercepted_href = this.href; //this.href - адрес ссылки
			this.setAttribute("href", "#");
	    	// Сабмит формы при сортировке, чтобы не терялись изменения при редактировании
	    	submitForm_Sort(intercepted_href, this);
	    }
	}
	 
	})()
</script>

<c:if test="<%= renderRequest.getParameter(CardPortlet.NEED_CLOSE_CONFIRMATION) != null || 
				renderRequest.getParameter(CardPortlet.NEED_CLOSE_CARD_CONFIRMATION) != null ||
				renderRequest.getParameter(CardPortlet.NEED_CUSTOM_CLOSE_CONFIRMATION) != null %>">
<%-- пользователь нажал закрыть, но в карточке есть несохраненные изменения
	поэтому страница редактирования перезагружается и пользователю показывается диалог
	требующий подтверждения закрытия --%>
	<c:set var="confirmationDialogId"><portlet:namespace/>_confirmationDialog</c:set>
	<div id="${confirmationDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="edit.close.confirmation.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="edit.close.confirmation.message"/></div>
		<div style="float:right; clear: both;" id="dialogButtons">
			<c:set var="yesBtnId"><portlet:namespace/>_yesBtnId</c:set>
			<button id="${yesBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="edit.close.confirmation.yes"/>
			    <script type="dojo/method" event="onClick" args="evt">
					<c:if test="<%= renderRequest.getParameter(CardPortlet.NEED_CLOSE_CONFIRMATION) != null %>">
						submitForm('<%= CardPortlet.SAVE_AND_CLOSE_EDIT_MODE_ACTION %>', dijit.byId('${yesBtnId}'));
					</c:if>
					<c:if test="<%= renderRequest.getParameter(CardPortlet.NEED_CLOSE_CARD_CONFIRMATION) != null %>">
						submitForm('<%= CardPortlet.SAVE_AND_CLOSE_CARD_ACTION %>', dijit.byId('${yesBtnId}'));
					</c:if>
					<c:if test="<%= renderRequest.getParameter(CardPortlet.NEED_CUSTOM_CLOSE_CONFIRMATION) != null %>">
						submitForm('<%= CardPortlet.CUSTOM_STORE_CARD_ACTION %>', dijit.byId('${yesBtnId}'));
					</c:if>
				</script>		
			</button>
			<c:set var="noBtnId"><portlet:namespace/>_noBtnId</c:set>
			<button id="${noBtnId}" dojoType="dijit.form.Button" type="button">
				<fmt:message key="edit.close.confirmation.no"/>
			    <script type="dojo/method" event="onClick" args="evt">
					submitForm('<%= CardPortlet.CLOSE_EDIT_MODE_ANYWAY_ACTION %>', dijit.byId('${noBtnId}'));
				</script>
			</button>
			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="edit.close.confirmation.cancel"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('${confirmationDialogId}').hide();					
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

<c:if test="<%= renderRequest.getParameter(CardPortlet.WARNING_KEY_PREFIX) != null%>">
<%-- сообщить пользователю предупреждение при отрисовке карточки, 
	 параметр определяет строковые ресурсы и может быть установлен в processAction --%>
	<c:set var="warningDialogId"><portlet:namespace/>_warningDialog</c:set>
	<c:set var="warningKeyPrefix"><%=renderRequest.getParameter(CardPortlet.WARNING_KEY_PREFIX)%></c:set>
	<div id="${warningDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="${warningKeyPrefix}.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="${warningKeyPrefix}.message"/></div>
		<div style="float:center; clear: both;" id="dialogButtons">
			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="${warningKeyPrefix}.close"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('${warningDialogId}').hide();					
				</script>
			</button>			
		</div>
	</div>
	<script type="text/javascript" language="javascript">
	dojo.require('dijit.Dialog');
	dojo.addOnLoad(function() {
		dbmiHideLoadingSplash();
		dijit.byId('${warningDialogId}').show();
	});
	</script>
</c:if>

<c:if test="<%= renderRequest.getAttribute(CardPortlet.WARNING_KEY_PREFIX) != null%>">
<%-- сообщить пользователю предупреждение при отрисовке карточки 
	 атрибут определяет строковые ресурсы и может быть установлен в doView --%>
	<c:set var="warningDialogId"><portlet:namespace/>_warningDialog</c:set>
	<c:set var="warningKeyPrefix"><%=renderRequest.getAttribute(CardPortlet.WARNING_KEY_PREFIX)%></c:set>
	<div id="${warningDialogId}" dojoType="dijit.Dialog" title="<fmt:message key="${warningKeyPrefix}.title"/>" style="width: 320px; height: 96px">
		<div style="text-align: left;"><fmt:message key="${warningKeyPrefix}.message"/></div>
		<div style="float:center; clear: both;" id="dialogButtons">
			<button dojoType="dijit.form.Button" type="button">
				<fmt:message key="${warningKeyPrefix}.close"/>
			    <script type="dojo/method" event="onClick" args="evt">
					dijit.byId('${warningDialogId}').hide();					
				</script>
			</button>			
		</div>
	</div>
	<script type="text/javascript" language="javascript">
	dojo.require('dijit.Dialog');
	dojo.addOnLoad(function() {
		dbmiHideLoadingSplash();
		dijit.byId('${warningDialogId}').show();
	});
	</script>
</c:if>

<%@include file="CardDialog.jspf"%>
<%@include file="AttributeEditorDialogBlue.jspf"%>
