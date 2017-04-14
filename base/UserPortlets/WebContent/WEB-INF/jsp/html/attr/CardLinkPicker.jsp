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
<%@page session="false" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="com.aplana.dbmi.model.Attribute"%>
<%@page import="com.aplana.dbmi.model.TypedCardLinkAttribute"%>
<%@page import="com.aplana.dbmi.model.DatedTypedCardLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardLinkPickerAttributeEditor"%>
<%@page import="com.aplana.dbmi.card.CardLinkPickerSearchEditor"%>
<%@page import="com.aplana.dbmi.card.CardLinkPickerWithExtraVariantsSearchEditor"%>
<%@page import="com.aplana.dbmi.card.AjaxPersonSearchEditor"%>
<%@page	import="com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchFilterParameters"%>
<%@page import="com.aplana.dbmi.card.CardPortletCardInfo"%>
<%@page import="com.aplana.dbmi.card.CardAttributeEditorParameterHelper"%>

<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.CardPortletSessionBean"%>
<%@page import="com.aplana.dbmi.model.ContextProvider"%>
<%@page import="com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor"%>
<%@page import="com.aplana.dbmi.card.hierarchy.descriptor.ReplaceDescriptor"%>
<%@page import="com.aplana.dbmi.model.util.AttrUtils"%>
<%@page import="com.aplana.dbmi.model.ObjectId"%>
<%@page import="com.aplana.dbmi.model.ReferenceValue"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.PersonListResource" />
<%
	Attribute attr =  (Attribute)request.getAttribute(JspAttributeEditor.ATTR_ATTRIBUTE);
	String extraJavascript =  (String)request.getAttribute(JspAttributeEditor.ATTR_EXTRA_JAVASCRIPT);
	
	boolean typed = attr.getType().equals(Attribute.TYPE_TYPED_CARD_LINK) || attr.getType().equals(Attribute.TYPE_DATED_TYPED_CARD_LINK);
	boolean dated = attr.getType().equals(Attribute.TYPE_DATED_TYPED_CARD_LINK);
	String attrCode = (String)attr.getId().getId();
	String attrCodeType = AttrUtils.getAttrTypeString(attr.getClass()) + ":" + (String)attr.getId().getId();
	Boolean showInactivePersonsCheckBoxValue = (Boolean)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_SHOW_INACTIVE_PERSONS_CHECKBOX);
	Boolean showExtraVariantsCheckBoxValue = (Boolean)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_SHOW_EXTRA_ATTRS_CHECKBOX);
	Boolean checkExtraVariantsCheckBoxValue = (Boolean)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_CHECK_EXTRA_ATTRS_CHECKBOX);
	String extraVariantsCheckBoxTitleCode = (String)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_EXTRA_ATTRS_CHECKBOX_TITLE_CODE);
	String extraVariantsCheckTitleCode = (String)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_EXTRA_ATTRS_CHECKBOX_CHECKED_TITLE_CODE);
	String extraVariantsUncheckTitleCode = (String)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), AjaxPersonSearchEditor.KEY_EXTRA_ATTRS_CHECKBOX_UNCHECKED_TITLE_CODE);
	Boolean isExtraVeriantsCheckboxOnTopValue = (Boolean)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), CardLinkPickerWithExtraVariantsSearchEditor.KEY_EXTRA_VARIANTS_CHECKBOX_ON_TOP);
	Integer scrollHeightValue = (Integer)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), CardLinkPickerSearchEditor.KEY_SCROLL_HEIGHT);
	
	// получаем атрибут для заполнения текущей выбранной карточки и шаблон, при котором это заполнение идет 
	ObjectId replaceAttrId = null;
	ObjectId replaceTemplateId = null;
	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(renderRequest);
	
	// для дополнительного согласования
	// определяем карточку текущего пользователя, чтобы игнорировать ее при загрузке карточек пользователей (для атрибута список согласования)
	if(sessionBean !=null &&
	sessionBean.getServiceBean() != null
	&& sessionBean.getServiceBean().getPerson() !=null
	&& sessionBean.getServiceBean().getPerson().getCardId() != null
	&& sessionBean.isNeedToSkipCurrentUser(attr)
	) {
%>
		<c:set var="currentUserCardId" scope="page" value="<%=sessionBean.getServiceBean().getPerson().getCardId().getId()%>"/>
	<%
		}
		
	//	CardPortletCardInfo cardInfo = CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), "CardLinkPickerDescriptor");
		CardLinkPickerDescriptor d = (CardLinkPickerDescriptor)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), "cardLinkPickerDescriptor");
		
		if (d!=null){
			ObjectId choiceReferenceValueId = null;
			CardLinkPickerVariantDescriptor vd = d.getVariantDescriptor(choiceReferenceValueId);
			ReplaceDescriptor rd = (vd!=null)?vd.getReplaceAttr():null;
			if (rd!=null){
				replaceAttrId = rd.getAnotherAttrId();
				replaceTemplateId = rd.getTemplateId();
			}
		}
		boolean showInactivePersonsCheckBox = showInactivePersonsCheckBoxValue == null ? false : showInactivePersonsCheckBoxValue.booleanValue();
		boolean showExtraVariantsCheckBox = showExtraVariantsCheckBoxValue == null ? false : showExtraVariantsCheckBoxValue.booleanValue();
		boolean checkExtraVariantsCheckBox = checkExtraVariantsCheckBoxValue == null ? false : checkExtraVariantsCheckBoxValue.booleanValue();
		String checkExtraVariantsCheckBoxString =  checkExtraVariantsCheckBox ? "checked" : "";
		boolean isExtraVariantsCheckboxOnTop = isExtraVeriantsCheckboxOnTopValue == null ? false : isExtraVeriantsCheckboxOnTopValue.booleanValue();
		String scrollHeightProperty = (scrollHeightValue == null || scrollHeightValue <= 0) ? "100%" :  String.valueOf(scrollHeightValue.intValue()) + "px";
	%>
<c:set var="attrHtmlId"
	value="<%=JspAttributeEditor.getAttrHtmlId(attr)%>" />
<c:set var="checkExtraVariantsIds" value="<%=checkExtraVariantsCheckBoxString%>" />
<c:set var="extraVariantsTitleCode" value="<%=extraVariantsCheckBoxTitleCode%>"/>
<c:set var="extraVariantsCheckTitleCode" value="<%=extraVariantsCheckTitleCode%>"/>
<c:set var="extraVariantsUncheckTitleCode" value="<%=extraVariantsUncheckTitleCode%>"/>
<input type="hidden" id="${attrHtmlId}_values"
	name="${attrHtmlId}_values" value="" />
<input type="hidden" id="${attrHtmlId}_variantAlias"
	name="${attrHtmlId}_variantAlias" value="" />

<script language="javascript" type="text/javascript">
	dojo.require('dbmiCustom.DateTimeWidget');
	dojo.require('dbmiCustom.CustomFilteringSelect');
	dojo.require('dojo.date.stamp');
	var ${attrHtmlId}_activeVariant = null;
	var ${attrHtmlId}_variants = ${requestScope.variants};
	var ${attrHtmlId}_select = null;
	var ${requestScope.choiceAttrHtmlId}_radioButtons = [];
	<%if (typed) {%>var ${attrHtmlId}_types = null;<%}%>
	<%if (dated) {%>var ${attrHtmlId}_dates = null;<%}%>
	dojo.addOnLoad(function() {
<c:if test="${requestScope.isLocalChoice}">
	var checked = true;
	<c:forEach items="${requestScope.choiceAttrValues}" var="item" varStatus="itemStatus">
		btn = new dijit.form.RadioButton(
			{
				name: '${requestScope.choiceAttrHtmlId}',
				value: '${item.id.id}',
				checked: checked 
			},
			'${requestScope.choiceAttrHtmlId}_${item.id.id}'
		);
		checked = false;
		${requestScope.choiceAttrHtmlId}_radioButtons[${itemStatus.index}] = btn;
		dojo.connect(dojo.byId(btn.id), 'onclick', function() {${requestScope.choiceAttrHtmlId}_updateView();});
	</c:forEach>
	${requestScope.choiceAttrHtmlId}_updateView();
</c:if>
<c:if test="${requestScope.choiceAttrHtmlId != null}">
		for(var i = 0; i < ${requestScope.choiceAttrHtmlId}_radioButtons.length; ++i) {
			var radioButton = ${requestScope.choiceAttrHtmlId}_radioButtons[i];
			var callback = {
				radioButton: radioButton,
				onChange: function() {
					if (this.radioButton.attr('checked') == true) {
						var alias = this.radioButton.attr('value');
						if (${attrHtmlId}_variants[alias] == null) {
							alias = '<%=CardLinkPickerDescriptor.DEFAULT_REF_ID%>';
						}
						cardLinkPickerSwitchVariant('${attrHtmlId}', alias, '<%=attrCode%>', <%=typed%>, <%=dated%>, ${requestScope.sharedValues});
					}
				}
			}
			dojo.connect(radioButton, 'onChange', callback, 'onChange');   
		}
</c:if>
		var dataStore = new dojox.data.QueryReadStore({
			url :'<%=request.getContextPath() + "/servlet/SearchCardServlet"%>'
		});
		${attrHtmlId}_select = new dbmiCustom.CustomFilteringSelect(
			{
				width: '85%',
				style: 'width: 100%;',				
				store: dataStore,
				searchAttr: '<%=CardLinkPickerAttributeEditor.FIELD_LABEL%>',
				pageSize: 15,
				searchDelay: 500,
				required: false,
				autoComplete: false,
				curUser: '${currentUserCardId}',//текущий пользователь (при обновлении таблицы берется отсюда и добавляется в ignore в CardLinkPickerInclude.jsp)
				query: {
					<%=CardLinkPickerSearchParameters.PARAM_ATTR_CODE%>: '<c:out value="<%=attrCode%>"/>',
					<%=CardLinkPickerSearchParameters.PARAM_NAMESPACE%>: '<portlet:namespace/>',
					<%=CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS%>: '${requestScope.activeVariant}',
					<%=SearchCardServlet.PARAM_CALLER%>: '<%=CardAttributeEditorParameterHelper.getCallerField(renderRequest)%>',
					<%=CardLinkPickerSearchParameters.PARAM_ATTR_TYPE_CODE%>: '<c:out value="<%=attrCodeType%>"/>',
					<c:if test="<%=showInactivePersonsCheckBox%>">
						<%=CardLinkPickerSearchFilterParameters.PARAM_SHOW_INACTIVE_PERSONS%>: 'false' ,
					</c:if>
					<%=SearchCardServlet.PARAM_IGNORE%>: '${currentUserCardId}'
				}, onValueChanged: function(){
					<%if (replaceAttrId!=null&&replaceTemplateId!=null){%>
						cardLinkPickerOnSelectChangedWithReplace('${attrHtmlId}', '<%=attrCode%>', <%=attr.isMultiValued()%>, <%=typed%>, <%=dated%>,'<%=JspAttributeEditor.getAttrHtmlId(replaceAttrId.getId().toString())%>', '<%=replaceAttrId.getId().toString()%>', <%=replaceTemplateId.getId().toString()%>);
					<%} else {%>
						cardLinkPickerOnSelectChanged('${attrHtmlId}', '<%=attrCode%>', <%=attr.isMultiValued()%>, <%=typed%>, <%=dated%>);
					<%}%>
				}
			},
			dojo.byId('${attrHtmlId}_select')
		);

		
		editorEventManager.registerAttributeEditor('<%=attrCode%>', '${attrHtmlId}', false, dijit.byId('${attrHtmlId}_select').value);
		<%if(showExtraVariantsCheckBox){%>
			editorEventManager.registerAttributeEditor('${attrHtmlId}_ExtraVariantFlag', '${attrHtmlId}_ExtraVariantFlag', false, dojo.byId('${attrHtmlId}_ExtraVariantFlag').value);
		<%}%>
		
		var lookupBtn = new dijit.form.Button(
			{
				onClick: function() {
				<%if (replaceAttrId!=null){%>
					cardLinkPickerShowDialogWithReplace('${attrHtmlId}', 
					'<%=attrCode%>', '<%=attr.getName()%>', <%=attr.isMultiValued()%>, <%=typed%>, <%=dated%>,'', '<%=JspAttributeEditor.getAttrHtmlId(replaceAttrId.getId().toString())%>',
					'<%=replaceAttrId.getId().toString()%>', <%=replaceTemplateId.getId().toString()%>
					);
				<%}	else {%>
					cardLinkPickerShowDialog('${attrHtmlId}', 
						'<%=attrCode%>', '<%=attr.getName()%>', <%=attr.isMultiValued()%>, <%=typed%>, <%=dated%>);
				<%}%>					
				}
			},
			dojo.byId('${attrHtmlId}_lookupBtn')
		);	
		cardLinkPickerSwitchVariant('${attrHtmlId}', '${requestScope.activeVariant}', '<%=attrCode%>', <%=typed%>, <%=dated%>, ${requestScope.sharedValues});
		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
		if (variant.dependencies) {
		for(var i = 0; i < variant.dependencies.length; ++i) {
			var valueAttrCode = variant.dependencies[i];
			editorEventManager.subscribe('<%=attrCode%>', valueAttrCode, 'cardLinkPickerOnSubscribedAttrChanged', i);
		}
		//подгружаем в представление атрибута все параметры типа paramN (на случай, если атрибуты, от которых зависит текущий, на форме не представлены редакторами и => изменены не могут)
		var widget = eval('${attrHtmlId}_select');
		for(var param in variant.query) {
			var paramValue = variant.query[param];
			eval('widget.query.' + param + ' = \'' + paramValue + '\'');
		}
		}
		<%if (typed) {%>
			${attrHtmlId}_types = <%=CardLinkPickerAttributeEditor.getJSONMapTypesCardLink((TypedCardLinkAttribute)attr)%>;
		<%}
		   if (dated) {%>
			${attrHtmlId}_dates = <%=CardLinkPickerAttributeEditor.getJSONMapDatesCardLink((DatedTypedCardLinkAttribute)attr)%>;
		<%}%>
		cardLinkPickerSetSelectedCards('${attrHtmlId}', '<%=attrCode%>', '<%= attr.isMultiValued()%>', '<%=attrCodeType%>',[${requestScope.selectedValues}]
			<% if (typed) {%>, true, ${attrHtmlId}_types<%}%> <% if (dated) {%>, true, ${attrHtmlId}_dates <%}%>);
	});
	function ${requestScope.choiceAttrHtmlId}_updateView() {
		for (var k=0; k <${requestScope.choiceAttrHtmlId}_radioButtons.length; k++) {
			var btn = ${requestScope.choiceAttrHtmlId}_radioButtons[k];
			if (btn.checked) {
				dojo.byId(btn.id+'_div').style.fontWeight = 'bold';
			} else {
				dojo.byId(btn.id+'_div').style.fontWeight = 'normal';
			}
		}
	}

	function ${attrHtmlId}_cardLinkPickerChangeShowInactivePersons(checkboxElement) {
		
		var widget = eval('${attrHtmlId}' + '_select');
		if (checkboxElement.checked)				 
			widget.promptMessage ='<fmt:message key="search.show.inactivePersons"/>';
		else
			widget.promptMessage ='<fmt:message key="search.show.activePersons"/>';
						 
		eval('widget.query.<%= CardLinkPickerSearchFilterParameters.PARAM_SHOW_INACTIVE_PERSONS %>' + '= \'' + checkboxElement.checked + '\'');
		widget.focus();
	}

	function ${attrHtmlId}_cardLinkPickerChangeShowExtendedVariants(checkboxElement) {
		
		var widget = eval('${attrHtmlId}' + '_select');
		if (checkboxElement.checked)				 
			widget.promptMessage ='<fmt:message key="${extraVariantsCheckTitleCode}"/>';
		else
			widget.promptMessage ='<fmt:message key="${extraVariantsUncheckTitleCode}"/>';
		widget.focus();
	}
	
	<%if (typed) {%>
		var typesCLink = null;
		
		var xhrArgs = {
			url: "<%=request.getContextPath()+"/servlet/TreeServlet?idRef="+(String)((TypedCardLinkAttribute)attr).getReference().getId()%>",
	        handleAs: "json",
	        load: function(data) {
	       		typesCLink = data.items;
	        },
	        error: function(error) {
	       		typesCLink = "";
	        }
	    }
	
		dojo.xhrGet(xhrArgs);
		
		${attrHtmlId}_selType = null;
		function ${attrHtmlId}_cardLinkPickerRefreshValues() {
			var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
			for (var i = 0; i < ${attrHtmlId}_selType.length; i++) {
				if (${attrHtmlId}_selType[i].options[${attrHtmlId}_selType[i].selectedIndex].value == "") {
					variant.types[${attrHtmlId}_selType[i].cardId] = null;
				} else {
					variant.types[${attrHtmlId}_selType[i].cardId] = ${attrHtmlId}_selType[i].options[${attrHtmlId}_selType[i].selectedIndex].value;
				}
			}
			dojo.byId('${attrHtmlId}_values').value = cardLinkPickerStringTypesAndDates(variant.types, variant.dates);
		}

		
		${attrHtmlId}_typeTitle = 
				<%{String title = (String)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), CardLinkPickerAttributeEditor.KEY_TYPE_CAPTION);%>
				"<%= title!=null ? title : ContextProvider.getContext().getLocaleMessage("search.column.linktype") %>";
				<%}
	}%>
	
	<%if (dated) {%>
	function ${attrHtmlId}_cardLinkPickerRefreshValuesWithDate() {
		var variant = cardLinkPickerGetActiveVariantObject('${attrHtmlId}');
		var cards = variant.cards;
		for (var i = 0; i < cards.length; ++i) {
			var card = cards[i];
			var dateWidget = dijit.byId('${attrHtmlId}_date_'+card.cardId);
			if (dateWidget.getValue()){
				if(dateWidget.getValue() < new Date((new Date()).setHours(0, 0, 0, 0))){
					dateWidget.setValue(new Date((new Date()).setHours(0, 0, 0, 0)));
					continue;
				}
			}
			var dateInput = dojo.query("input[name^=${attrHtmlId}_date_"+card.cardId+"]");
			var dateValue = dateInput[0].value;
			if(dateValue.trim() == "")
				variant.dates[card.cardId] = null;
			else
				variant.dates[card.cardId] = dateValue;
		}
		dojo.byId('${attrHtmlId}_values').value = cardLinkPickerStringTypesAndDates(variant.types, variant.dates);
	}
	
	${attrHtmlId}_dateTitle = 
		<%{String title = (String)CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), CardLinkPickerAttributeEditor.KEY_DATE_TYPE_CAPTION);%>
		"<%= title!=null ? title : ContextProvider.getContext().getLocaleMessage("search.column.datelinktype") %>";
		<%}
	}%>
	
	
</script>

<%
if (extraJavascript != null) {%>
<script language="javascript" type="text/javascript">
<%= extraJavascript %>
</script>
<%} %>

<table width="100%">
	<c:if test="<%=showExtraVariantsCheckBox && isExtraVariantsCheckboxOnTop%>">
		<tr>
			<td>
				<input onclick="${attrHtmlId}_cardLinkPickerChangeShowExtendedVariants(this)"
					onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_ExtraVariantFlag', this.value);" 
					 dojoType="dijit.form.CheckBox" ${checkExtraVariantsIds} id="${attrHtmlId}_ExtraVariantFlag" 
					 name="${attrHtmlId}_ExtraVariantFlag" title='<fmt:message key="${extraVariantsTitleCode}"/>' stype="float:left; margin-left: 10px;"/>

				<label for="${attrHtmlId}_ExtraVariantFlag" style="vertical-align: left; float:middle;"><fmt:message key="${extraVariantsTitleCode}"/></label>	 
			</td>
		</tr>
	</c:if>

	<c:if test="${requestScope.isLocalChoice}">
		<tr style="height: auto;">
			<td colspan="2">
				<c:forEach items="${requestScope.choiceAttrValues}" var="item">
					<div style="float: left; margin-right: 10px;" id="${requestScope.choiceAttrHtmlId}_${item.id.id}_div">
						<input id="${requestScope.choiceAttrHtmlId}_${item.id.id}"	type="radio" name="${requestScope.choiceAttrHtmlId}" value="${item.id.id}">
							${item.value}
						</input>
					</div>
				</c:forEach>
			</td>
		</tr>
	</c:if>
	<tr style="height: auto;">
		<td colspan="2">
		<div id="${attrHtmlId}_AdditionalButtonsBar" style="float: right"></div>
		</td>
	</tr>
	<tr>
		<td width="*" style="vertical-align: middle;"><select
			id="${attrHtmlId}_select"></select></td>
		<c:if test="<%=showExtraVariantsCheckBox && !isExtraVariantsCheckboxOnTop%>">
			<td width="18px" style="padding-left: 10px;">
				<input onclick="${attrHtmlId}_cardLinkPickerChangeShowExtendedVariants(this)"
					onChange="javascript: editorEventManager.notifyValueChanged('${attrHtmlId}_ExtraVariantFlag', this.value);" 
					 dojoType="dijit.form.CheckBox" ${checkExtraVariantsIds} id="${attrHtmlId}_ExtraVariantFlag" 
					 name="${attrHtmlId}_ExtraVariantFlag" title='<fmt:message key="${extraVariantsTitleCode}"/>'/>
			</td>
		</c:if>
		<c:if test="<%=showInactivePersonsCheckBox%>">
			<td width="18px" style="padding-left: 10px;">
				<input onclick="${attrHtmlId}_cardLinkPickerChangeShowInactivePersons(this)" dojoType="dijit.form.CheckBox" 
		 							id="${attrHtmlId}_inactivePersoonButton" name="${attrHtmlId}_inactivePersoonButton" title='<fmt:message key="search.show.flag.inactivePersons"/>'/>
			</td>
		</c:if>
		<td width="34px" style="padding-left: 10px;">
			<div class="lookupBtnDiv">
				<button id="${attrHtmlId}_lookupBtn"><span class="lookup"
					style="padding: 0px; margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
				</button>
			</div>
		</td>
	</tr>
</table>

<%if (typed) {%>
	<div dojoType="dojo.data.ItemFileReadStore" jsId="valueStore" url="<%=request.getContextPath()+"/servlet/TreeServlet?idRef="+(String)((TypedCardLinkAttribute)attr).getReference().getId()%>">
	</div>
<%}%>

<div style="overflow:auto; max-height:<%=scrollHeightProperty%>;">
	<table class="res" id="${attrHtmlId}_table"<%
		if (!((Boolean) CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest, attr.getId(), CardLinkPickerAttributeEditor.KEY_SHOW_TITLE)).booleanValue()) {
			%> noHead="true"<%
		}
		if (!((Boolean) CardAttributeEditorParameterHelper.getAttributeEditorData(renderRequest,attr.getId(), CardLinkPickerAttributeEditor.KEY_SHOW_EMPTY)).booleanValue()) {
			%> noEmpty="true"<%
		}
	%> style="width: 100%; margin-top: 0px;">
	</table>
</div>
<div dojoType="dijit.Dialog" id="${attrHtmlId}_dialog" title="<%= attr.getName() %>" style="text-align: left;">
	<input type="hidden" id="${attrHtmlId}_wasInit" value="false"/>
	<div id="${attrHtmlId}_filter">
		<input id="${attrHtmlId}_filterLine" type="text" dojoType="dijit.form.TextBox" trim="true" style="width: 40em;"/>
		<button id="${attrHtmlId}_filterButton" dojoType="dijit.form.Button" type="button">Фильтровать</button>
	</div>
    <div id="${attrHtmlId}_loading" align="center">
    	<img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" border="0" alt="" />
    </div>
    <div id="${attrHtmlId}_hierarchy"></div>
    <div dojoType="dijit.Dialog" id="${attrHtmlId}_dialog_confirm" title='<fmt:message key="confirmation.question"/>'>
    	<button id="${attrHtmlId}_dialog_yes" dojoType="dijit.form.Button" type="button"><fmt:message key="confirmation.yes"/></button>
    	<button id="${attrHtmlId}_dialog_no" dojoType="dijit.form.Button" type="button"><fmt:message key="confirmation.no"/></button>
    	<button id="${attrHtmlId}_dialog_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="confirmation.cancel"/></button>
    </div>
</div>