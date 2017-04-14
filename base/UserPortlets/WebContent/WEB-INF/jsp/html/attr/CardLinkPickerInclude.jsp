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
<%@page import="com.aplana.dbmi.card.CardLinkPickerAttributeEditor"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.model.CardLinkAttribute"%>
<%@page import="com.aplana.dbmi.card.LinkedCardUtils"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerHierarchyParameters"%>
<%@page import="com.aplana.dbmi.card.CardAttributeEditorParameterHelper"%>
<%@page import="com.aplana.dbmi.card.JspAttributeEditor"%>
<%@page import="javax.portlet.PortletException"%>
<%@page import="javax.portlet.RenderRequest"%>
<%@page import="javax.portlet.RenderResponse"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.aplana.dbmi.service.UserPrincipal"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<%@page import="com.aplana.dbmi.ajax.SearchCardServletParameters"%>
<%@page import="com.aplana.dbmi.ajax.HierarchyConnection"%>
<style type="text/css">
.thinButton .dijitButtonNode {
	width: 75px;
	margin: 5px 0px 0px 5px;
	border: 1px solid #c0c0c0;
	border-bottom: 1px solid #9b9b9b;
	padding: 0.1em 0.2em 0.2em 0.2em;
	background: inherit;
	background-color: transparent;
}
</style>

<c:set var="namespace"><portlet:namespace/></c:set>
<c:url var="hierarchyServletURLTemplate" value="/servlet/CardHierarchyCachingServlet">
	<c:param name="<%= CardHierarchyServlet.PARAM_CALLER %>" value="<%= CardLinkPickerHierarchyParameters.CALLER %>"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_REQUEST_TYPE %>" value="##requestType##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_NAMESPACE%>" value="${namespace}"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ATTR_CODE%>" value="##attrCode##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ACTIVE_VARIANT%>" value="##activeVariant##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_CHECKED_CARDS%>" value="##checkedCards##"/>	
	<c:param name="<%= CardHierarchyServlet.PARAM_HIERARCHY_KEY%>" value="##hierarchyKey##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_FILTER_QUERY%>" value="##filterQuery##"/>
</c:url>

<c:set var="attrCodePlaceholder"><%=URLEncoder.encode("##attrCode##", "UTF-8")%></c:set>
<c:set var="activeVariantPlaceholder"><%=URLEncoder.encode("##activeVariant##", "UTF-8")%></c:set>
<c:set var="checkedCardsPlaceholder"><%=URLEncoder.encode("##checkedCards##", "UTF-8")%></c:set>
<c:set var="requestTypePlaceholder"><%=URLEncoder.encode("##requestType##", "UTF-8")%></c:set>
<c:set var="hierarchyKeyPlaceholder"><%=URLEncoder.encode("##hierarchyKey##", "UTF-8")%></c:set>
<c:set var="filterQueryPlaceholder"><%=URLEncoder.encode("##filterQuery##", "UTF-8")%></c:set>

<jsp:include page="../CardPageFunctions.jsp"/>

<portlet:defineObjects />

<script type="text/javascript" language="javascript">
	dojo.require('dijit.dijit');
	dojo.require('dijit.form.FilteringSelect');
	dojo.require('dijit.form.Button');
	dojo.require('dojox.data.QueryReadStore');		
	dojo.require('dojo.parser');
	dojo.require('dijit.TitlePane');
	dojo.require('dijit.Dialog');
	dojo.require('dijit.DialogUnderlay');
	dojo.require('dbmiCustom.HierarchicalCardList');
	dojo.require('dbmiCustom.DateTimeWidget');
	dojo.require('dojo.date.stamp');
	dojo.require("dijit.form.RadioButton");
/*
	var attributeEditorManager = {
		editors: {},
		subscriptions: {},		
		registerAttributeEditor: function(attrCode, attrHtmlId, isInline) {
			var editorData = {
				'attrCode': attrCode,
				'attrHtmlId': attrHtmlId,
				'isInline': isInline,
			};
			this.editors[attrCode] = editorData;					
		},
		subscribe: function(subscriberAttrCode, valueAttrCode, paramIndex) {
			var subscription = {
				'subscriberAttrCode': subscriberAttrCode,
				'paramIndex': paramIndex
			};

			var subscribers = this.subscriptions[valueAttrCode];
			if (subscribers) {
				subscribers.push(subscription);
			} else {
				this.subscriptions[valueAttrCode] = [subscription];
			}
		},
		notifyValueChanged: function(attrCode, value) {
			var editorData = this.editors[attrCode];
			if (!editorData) {
				console.error('Not registered attribute code: ' + attrCode);
				return;
			}
			var subscribers = this.subscriptions[attrCode];
			if (subscribers) {
				for(var i = 0; i < subscribers.length; ++i) {
					var subscription = subscribers[i];
					var sData = this.editors[subscription.subscriberAttrCode];
					cardLinkPickerOnSubscribedAttrChanged(sData.attrCode, sData.attrHtmlId, subscription.paramIndex, value, sData.isInline);
				}
			}
		}
	};
*/
	function cardLinkPickerOnSubscribedAttrChanged(attrCode, attrHtmlId, isInline, value, paramIndex) {
		var widget = eval(attrHtmlId + '_select');
		
		eval('widget.query.param' + paramIndex + ' = \'' + value + '\'');
		if (!isInline) {
			var variant = cardLinkPickerGetActiveVariantObject(attrHtmlId);
			eval('variant.query.param' + paramIndex + ' = \'' + value + '\'');
			//cardLinkPickerSetSelectedCards(attrHtmlId, attrCode, '');
		} else {
			//widget.attr('value', '<%= SearchCardServlet.EMPTY_CARD_ID %>');
		}
	}
  
	function cardLinkPickerSwitchVariant(attrId, variantAlias, attrCode, typed, dated, sharedValues) {
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
    	if(sharedValues && variant){
    		shareVariantValues(attrId, variant);
    	}
		eval(attrId + '_activeVariant = variantAlias');
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		dojo.byId(attrId+'_variantAlias').value = variantAlias;
		
		var widget = eval(attrId + '_select');
		widget.query.<%= CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS %> = variantAlias;
		
		var table = dojo.byId(attrId + '_table');
		if (table.getAttribute("noHead") != "true") {
			var row = table.insertRow(0);
			var cell = null;				
			var columns = variant.columns;
			var i = 0;
			while(i < columns.length) {
				var column = columns[i];
				cell = row.insertCell(i);
				cell.innerHTML = column.title;
				cell.style.width = column.width;
				cell.style.fontWeight = 'bold';
				cell.style.color = '#666666';
				++i;
			}
			if (typed) {
				cell = row.insertCell(i);
				cell.innerHTML = eval(attrId + '_typeTitle');
				cell.style.fontWeight = 'bold';
				cell.style.color = '#666666';
				++i;
			}
			if (dated) {
				cell = row.insertCell(i);
				cell.innerHTML = eval(attrId + '_dateTitle');
				cell.style.fontWeight = 'bold';
				cell.style.color = '#666666';
				++i;
			}

			for(var param in variant.query) {
				var paramValue = variant.query[param];
				eval('widget.query.' + param + ' = \'' + paramValue + '\'');
			}
			
			cell = row.insertCell(i);
			cell.innerHTML = '';
		}

		var btn = dijit.byId(attrId + '_lookupBtn');
		if (btn != null) {
			if (variant.hierarchySupported == false) {
				dojo.style(btn.domNode, 'display', 'none');	
			} else {
				dojo.style(btn.domNode, 'display', '');		
			}
		}
		
		cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
		cardLinkPickerSwitchToolBarVariant(attrId, attrCode, variant, typed, dated);
	}

	function cardLinkPickerSwitchToolBarVariant(attrId, attrCode, variant, typed, dated){
		var toolBar = dojo.byId(attrId+'_AdditionalButtonsBar');
		try{
			toolBar.innerHTML='';
			if (!(variant==null)){
				var actions = variant.hierarchyActions;
				if (!(actions == null)){
					for (var i = 0; i < actions.length; ++i) {
						var action = actions[i];
						var disabled = action.selectionType != 'none';
						if (!((action.id == '<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>') ||
							(action.id == '<%=CardLinkPickerAttributeEditor.ACTION_CANCEL%>')))	{
							var actionButton = new dijit.form.Button({
								label: action.title,
								actionID: action.id,
                                actionref: action,
								jsId: attrId+action.id,
								disabled: disabled,
								onClick: function() {
											cardLinkPickerDialogActionCallback(
													attrId, 
													attrCode, 
													false, 
													this.actionID, 
													null,
													typed,
													dated);
										}
							});	
							actionButton.placeAt(toolBar);
						}
					}
				}
			}
		} catch(err) {
		}
	}
	
	function cardLinkPickerRefreshTable(attrId, attrCode, typed, dated) {
		if (typed) {
			eval(attrId + '_selType = new Array();') ;
			var selType = eval(attrId + '_selType');
		}
		var table = dojo.byId(attrId + '_table');
		var firstRow = 1;
		if (table.getAttribute("noHead") == "true")
			firstRow = 0;
		if (table.rows) {
			for (var i = table.rows.length - 1; i >= firstRow; --i) {
				table.deleteRow(i);
			}
		}
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		var columns = variant.columns;
		var cards = variant.cards;
		var sel = [];
		if (table.getAttribute("noEmpty") == "true") {
			table.style.display = (cards.length > 0) ? '' : 'none';
		}
		for (var i = 0; i < cards.length; ++i) {
			var card = cards[i];
			sel[sel.length] = card.cardId;
			var row = table.insertRow(i + firstRow);
			var cell = null;
			var j = 0;
			var column_linked = variant.fieldLinked;			
			while (j < columns.length) {
				var column = columns[j];
				cell = row.insertCell(j);
				if (j == column_linked) {
					<%
					PortletURL openCardUrl = null;
					openCardUrl = renderResponse.createActionURL();
					openCardUrl.setParameter(CardPortlet.ACTION_FIELD, CardPortlet.OPEN_NESTED_CARD_ACTION);
					openCardUrl.setParameter(CardPortlet.CARD_ID_FIELD, "1234567890REPLACE1234567890");
					%>
					var link_str = "<%=openCardUrl.toString()%>";
					var card_no = "" + card.cardId;
					link_str = link_str.replace("1234567890REPLACE1234567890", card_no);
					//cell.innerHTML = "<a href='" + link_str + "'>" + card.columns[column.attrId] + "</a>";
					cell.innerHTML = "<a href=\"javascript:void(0)\" onclick='submitOpenLinkedCard(\"" + attrId + "\", " + card_no + ")'>" + card.columns[column.attrId] + "</a>";
				} else {
					//cell.innerHTML = card.columns[column.attrId];
					if (card.columns[column.attrId] != null) {cell.innerHTML = card.columns[column.attrId];}
				}
				++j;
			}
			if (typed) {
				<%-- Выпадающий список выбора типа связи --%>
				cell = row.insertCell(j);
				selType[i] = document.createElement('select');
				selType[i].id = attrId + '_select_type' + (i+firstRow);
				selType[i].cardId = card.cardId;
				selType[i].attrId = attrId;
				//selType.onchange = typedCardLinkPickerRefreshValues;
				selType[i].onchange = eval(attrId + '_cardLinkPickerRefreshValues');

			
				var option = document.createElement('option');
				option.text = "";
				try {
  					selType[i].add(option, null); // standards compliant
  				} catch(ex) {
  					selType[i].add(option); // IE only
  				}
  				var selIndex = 0;
				for (var k=0; k < typesCLink.length; k++) {
					option = document.createElement('option');
					option.text = typesCLink[k].name;
					option.value = typesCLink[k].id;
					if (variant.types[card.cardId] == typesCLink[k].id)	{
						selIndex = k+1;
					}
					try {
	  					selType[i].add(option, null); // standards compliant
	  				} catch(ex) {
	  					selType[i].add(option); // IE only
	  				}
				} 
				selType[i].selectedIndex = selIndex;
				cell.appendChild(selType[i]);
				++j;
			}
			if (dated) {
			<%-- Поле для редактирования даты --%>
				cell = row.insertCell(j);
				var dateLink = null;
				if (variant.dates[card.cardId] != null)	{
					dateLink = variant.dates[card.cardId];
				}
				dateWidget = dijit.byId(attrId+'_date_'+card.cardId);
			    if(dateWidget) {
			    	dateWidget.destroy();
			    }
				var dateWidget = new dbmiCustom.DateTimeWidget(
			    		{
			    			nameDate: attrId+'_date_'+card.cardId,
			    			valueString: dateLink
						}
					);
				cell.innerHTML = '<div id="'+attrId+'_dateControl_'+card.cardId+'"></div>';
				dateWidget.placeAt(dojo.byId(attrId+'_dateControl_'+card.cardId));
				dojo.connect(dateWidget._date, 'onChange', attrId + '_cardLinkPickerRefreshValuesWithDate');
		        ++j;
			}
			cell = row.insertCell(j);
			cell.style.textAlign = 'right';
			cell.innerHTML = '<a onclick="cardLinkPickerRemoveCard(\'' + attrId + '\', \'' + attrCode + '\',' + i + ',' + (typed?'true':'false') + ',' + (dated?'true':'false') + ')" ' +
				'style="text-decoration: none;"><span class="delete">&nbsp;</span></a>';
		}
		var widget = eval(attrId + '_select');
		var curUser = widget.curUser;
		if(curUser != '') {
			sel.push(curUser);
		}
		sel = sel.join('<%=CardLinkPickerAttributeEditor.ID_DELIMITER%>');
		widget.query.<%= SearchCardServlet.PARAM_IGNORE %> = sel;
		if(dated) {
			dojo.byId(attrId + '_values').value = cardLinkPickerStringTypesAndDates(variant.types, variant.dates);
		} else if(typed) {
			dojo.byId(attrId + '_values').value = cardLinkPickerStringTypes(variant.types)
		} else {
			dojo.byId(attrId + '_values').value = sel;
		}
		editorEventManager.notifyValueChanged(attrCode, sel);
	}

	function cardLinkPickerStringTypes(types) {
		var str = [];
		for (var key in types) {
			str[str.length] = key+':'+types[key];
		}
		return str.join('<%=CardLinkPickerAttributeEditor.ID_DELIMITER%>');
	}
	
	function cardLinkPickerStringTypesAndDates(types, dates) {
		if(dates == undefined || dates == null) {
			return cardLinkPickerStringTypes(types);
		}
		var str = [];
		for (var key in types) {
			str[str.length] = key+':'+types[key]+':'+dates[key];
		}
		return str.join('<%=CardLinkPickerAttributeEditor.ID_DELIMITER%>');
	}
	
	function cardLinkPickerGetActiveVariantObject(attrId) {
		return eval(attrId + '_variants[' + attrId + '_activeVariant]');
	}
	
	function cardLinkPickerRemoveCard(attrId, attrCode, index, typed, dated) {
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		var cardId = variant.cards[index].cardId;
		if (typed) {
			delete variant.types[cardId];
		}
		if (dated) {
			delete variant.dates[cardId];
		}
		variant.cards.splice(index, 1);
		cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
	}
	
	// Удалить из массива все карточки начиная с index и до конца
	function cardLinkPickerRemoveCards(attrId, attrCode, index, typed, dated) {
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		for(var i = index; i < variant.cards.length; i++) {
			var cardId = variant.cards[i].cardId;
			if (typed) {
				delete variant.types[cardId];
			}
			if (dated) {
				delete variant.dates[cardId];
			}
		}
		variant.cards.splice(index, variant.cards.length - index);
		cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
	}
	
	function cardLinkPickerOnSelectChanged(attrId, attrCode, multiValued, typed, dated) {
		var widget = eval(attrId + '_select');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var cards = card.children != null ? card.children : [card]; 
			var variant = cardLinkPickerGetActiveVariantObject(attrId);
			if (multiValued == true) {	
				variant.cards = variant.cards.concat(cards);		
			} else {
				cards = [cards[0]];
				if(cards[0] != null) variant.cards = [cards[0]];
			}
			if (typed)
				for(var i in cards)
					variant.types[cards[i].cardId] = null;
			if (dated)
				for(var i in cards)
					variant.dates[cards[i].cardId] = null;
			widget.attr('value', '');
			cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
		}
	}
	
	function cardLinkPickerOnSelectChangedWithReplace(attrId, attrCode, multiValued, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId) {
		var widget = eval(attrId + '_select');
		if (widget.isValid() && widget.item != null && widget.item.i.cardId != '<%=SearchCardServlet.EMPTY_CARD_ID%>') {
			var card = widget.item.i;
			var cards = card.children != null ? card.children : [card];
			for(var i in cards){
				var card1 = cards[i]; 
				var selectTemplateId = card1.template;
				var variant = null;
                try {
                	variant = cardLinkPickerGetActiveVariantObject(replaceAttrId);
                } catch (err){
                    console.error('Error during get variant for replaceAttr '+replaceAttrId+': '+ err.description);
                    variant = null;
                }
   				// если шаблон выбранной карточки совпадает с шаблоном, при котором значение надо записывать в другой атрибут,
   				// причём другой атрибут есть на карточке, то заполняем выбранное значение в этот другой атрибут
				if ((selectTemplateId==eqTemplateId)&&(!(variant == null))){
					if (multiValued == true) {	
						cardLinkPickerAddNewCard(replaceAttrId, card1);
					} else {
						var cards1 = [cards[0]];
						if(cards1[0] != null) variant.cards = [cards1[0]];
					}
					if (typed)
						variant.types[cards[i].cardId] = null;
					if (dated)
						variant.dates[cards[i].cardId] = null;
					cardLinkPickerRefreshTable(replaceAttrId, replaceAttrCode, typed, dated);
				} else { 
					variant = cardLinkPickerGetActiveVariantObject(attrId);
					if (multiValued == true) {	
						cardLinkPickerAddNewCard(attrId, card1);
					} else {
						var cards1 = [cards[0]];
						if(cards1[0] != null) variant.cards = [cards1[0]];
					}
					if (typed)
						variant.types[cards[i].cardId] = null;
					if (dated)
						variant.dates[cards[i].cardId] = null;
					cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
				}
			}
			widget.attr('value', '');
		}
	}

	function cardLinkPickerDialogActionCallback(attrId, attrCode, multiValued, actionId, selectedItems, typed, dated) {
		var dlg = dijit.byId(attrId + '_dialog');
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		if ('<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>' == actionId) {
			cardLinkPickerSetSelectedCards(attrId, attrCode, multiValued, 'empty', selectedItems, typed, variant.types, dated, variant.dates);
			dlg.hide();
		} else if ('<%=CardLinkPickerAttributeEditor.ACTION_CANCEL%>' == actionId) {
			dlg.hide();
		} else {

			if (this.actionref && this.actionref.jsEntryPoint) {

                try {

                    eval(this.actionref.jsEntryPoint);

                } catch (err){
                    console.error('Error during excecute javascript action entry point: ' + err.description);
                }

                return;

           	}

           	var mainDialog = dijit.byId(attrId + "_dialog");
			
			if(mainDialog && mainDialog.open){
				//Получить подтверждение
				var dialog = dijit.byId(attrId + "_dialog_confirm");
				var yesButton = dijit.byId(attrId + '_dialog_yes');
				dojo.connect(yesButton.domNode, 'onclick', function() {
					cardLinkPickerSetSelectedCards(attrId, attrCode, multiValued, 'empty', selectedItems, typed, variant.types, dated, variant.dates);
					submitAttributeActionsManagerAction(attrCode, actionId);
				});
				var noButton = dijit.byId(attrId + '_dialog_no');
				dojo.connect(noButton.domNode, 'onclick', function() {
					submitAttributeActionsManagerAction(attrCode, actionId);
				});
				var cancelButton = dijit.byId(attrId + '_dialog_cancel');
				dojo.connect(cancelButton.domNode, 'onclick', function() {
					dialog.hide();
				});
				dialog.show();
			} else {
				submitAttributeActionsManagerAction(attrCode, actionId);
			}
		}
	}
	
	function cardLinkPickerDialogActionCallbackWithReplace(attrId, attrCode, actionId, selectedItems, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId) {
		var dlg = dijit.byId(attrId + '_dialog');
		if ('<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>' == actionId) {
			cardLinkPickerSetSelectedCardsWithReplace(attrId, attrCode, 'empty',selectedItems, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId);
			dlg.hide();
		} else if ('<%=CardLinkPickerAttributeEditor.ACTION_CANCEL%>' == actionId) {
			dlg.hide();
		} else {

			if (this.actionref && this.actionref.jsEntryPoint) {

                try {

                    eval(this.actionref.jsEntryPoint);

                } catch (err){
                    console.error('Error during excecute javascript action entry point: ' + err.description);
                }

                return;

           	}

           	var mainDialog = dijit.byId(attrId + "_dialog");
			
			if(mainDialog && mainDialog.open){
				//Получить подтверждение
				var dialog = dijit.byId(attrId + "_dialog_confirm");
				var yesButton = dijit.byId(attrId + '_dialog_yes');
				dojo.connect(yesButton.domNode, 'onclick', function() {
					cardLinkPickerSetSelectedCardsWithReplace(attrId, attrCode, 'empty',selectedItems, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId);
					submitAttributeActionsManagerAction(attrCode, actionId);
				});
				var noButton = dijit.byId(attrId + '_dialog_no');
				dojo.connect(noButton.domNode, 'onclick', function() {
					submitAttributeActionsManagerAction(attrCode, actionId);
				});
				var cancelButton = dijit.byId(attrId + '_dialog_cancel');
				dojo.connect(cancelButton.domNode, 'onclick', function() {
					dialog.hide();
				});
				dialog.show();
			} else {
				submitAttributeActionsManagerAction(attrCode, actionId);
			}
		}
	}

		<%-- Обработчик события прокрутки списка --%>
	function cardLinkPickerHandleScrolling(attrId, attrCode) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		if (!hierarchy.waitingResponse) {
			function getRelativeScrollTop(elem) {
				return elem.scrollTop / (elem.scrollHeight - elem.clientHeight);
			}
			function getRelativeScrollBottom(elem) {
				return 1 - (elem.scrollHeight - elem.clientHeight - elem.scrollTop) / elem.scrollHeight;
			}
			var evt = window.event || arguments.callee.caller.arguments[0];
			var target = evt.target || evt.srcElement;
			if (getRelativeScrollBottom(target) > <%=HierarchyConnection.SCROLL_BOTTOM_POS%>)
				cardLinkPickerAppendElements(attrId, attrCode);
		}
	}
	
	function cardLinkPickerGetSelectedItems(store) {
		var rq = {
			query: {cardId: '?*', checked: true},
			queryOptions: {deep: true},
			myStore: store,
			selectedItems: [],
			onComplete: function(items, request) {
				var sel = [];
				var store = request.myStore;
				for (var i = 0; i < items.length; ++i) {
					sel[sel.length] = store.getValue(items[i], 'cardId'); 
				}
				request.selectedItems = sel;
			}
		};
		store.fetch(rq);
		return rq.selectedItems;
	}
	
	<%-- Добавление элементов верхнего уровня в конец списка --%>
	function cardLinkPickerAppendElements(attrId, attrCode) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		if (hierarchy.endOfData)
			return;
		var activeVariantAlias = eval(attrId + '_activeVariant');
		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape('<%= CardHierarchyServlet.REQUEST_ADD_ITEMS %>'))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', cardLinkPickerGetSelectedItems(hierarchy.store).join(','));
		var request = {
       		url: hierarchyUrl,
       		content: {},
       		handleAs: 'json',
       		sync: true,
	        load: function(response, ioArgs) {
	        	<%--hierarchy = dijit.byId(attrId + '_hierarchy');--%>
	        	hierarchy.appendItems(response.data);
	        	hierarchy.notShownCards = response.notShownCards;
	        	hierarchy.endOfData = response.endOfData;
				hierarchy.waitingResponse = false;
     		},
	        error: function(response, ioArgs) {
				console.error('HTTP status code: ', ioArgs.xhr.status);
				hierarchy.waitingResponse = false;
       			return response;
       		}
		};
		hierarchy.waitingResponse = true;
		dojo.xhrGet(request);		
	}
		
	function cardLinkPickerShowDialog(attrId, attrCode, title, multiValued, typed, dated, query) {
		var dlg = dijit.byId(attrId + '_dialog');
		var prevHier = dijit.byId(attrId + '_hierarchy');
        var activeVariantAlias = eval(attrId + '_activeVariant');        
        var variant = cardLinkPickerGetActiveVariantObject(attrId);
        
        var node = dojo.byId(attrId + '_hierarchy');
        var loading = dojo.byId(attrId + '_loading');
        var filterLine = dijit.byId(attrId + '_filterLine');
        var filterButton = dijit.byId(attrId + '_filterButton');
        
 		node.setAttribute('style', 'display:none');
 		loading.setAttribute('style', 'display:block');

		var requestType;
		if (document.getElementById(attrId + '_wasInit').value == 'false') {
			document.getElementById(attrId + '_wasInit').value = 'true';
       		dojo.connect(filterLine.domNode, 'onkeypress', function(evt) {
				if (evt.keyCode == dojo.keys.ENTER) {
					cardLinkPickerShowDialog(attrId, attrCode, title, multiValued, typed, dated, filterLine.attr("value"));
				}});
       		dojo.connect(filterButton.domNode, 'onclick', function() {
				cardLinkPickerShowDialog(attrId, attrCode, title, multiValued, typed, dated, filterLine.attr("value"));});
			dojo.connect(dlg, "hide", function() {
				node = dojo.byId(attrId + '_hierarchy');
				newNode = node.cloneNode(false);
       			node.parentNode.replaceChild(newNode, node);
				dijit.byId(attrId + '_hierarchy').destroyRecursive();
			});
		}

        var checkedCards = [];
		if (prevHier) {
			requestType = '<%= CardHierarchyServlet.REQUEST_FILTER_HIERARCHY %>';
			checkedCards = cardLinkPickerGetSelectedItems(prevHier.store)
		} else {
			requestType = '<%= CardHierarchyServlet.REQUEST_OPEN_HIERARCHY %>';			
        	for (var i = 0; i < variant.cards.length; ++i) {
        		checkedCards[i] = variant.cards[i].cardId;
        	}
		}

		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape(requestType))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', escape(checkedCards.join(',')))
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${filterQueryPlaceholder}', query ? query : '');
			

		for (var param in variant.query) {
			var paramValue = variant.query[param];
			if ('' !== paramValue) {
				hierarchyUrl += '&' + param + '=' + escape(paramValue);
			}
		}
        
       	if (prevHier != null) {
       		newNode = node.cloneNode(false);
       		node.parentNode.replaceChild(newNode, node);
        	prevHier.destroyRecursive();
        	node = newNode;
        }
        variant.hierarchyColumns.lengthColumns = 80;
        var hierarchy = new dbmiCustom.HierarchicalCardList(
			{
				readOnly: false,
				maxHeight: '400px',
				url : hierarchyUrl,
				columnsDescriptor: variant.hierarchyColumns,
				actions: variant.hierarchyActions,
				onAction: function(actionId, selectedItems, customInfo) {
					cardLinkPickerDialogActionCallback(customInfo.attrId, customInfo.attrCode, multiValued, 
									actionId, selectedItems, typed, dated);
				},
				onClickActions: {
					'open': function(cardId) {
						submitOpenLinkedCard(attrCode, cardId);
					}
				},
				customInfo: {
					attrId: attrId,
					attrCode: attrCode
				},
				scrollActionString: 'cardLinkPickerHandleScrolling("' + attrId + '","' + attrCode + '")',
			},
			node
		);
		dlg.show();
 		loading.setAttribute('style', 'display:none');
 		node.setAttribute('style', 'display:block');
 	}
	
	function cardLinkPickerShowDialogWithReplace(attrId, attrCode, title, multiValued, typed, dated, query, replaceAttrId, replaceAttrCode, eqTemplateId) {
		var dlg = dijit.byId(attrId + '_dialog');
		var prevHier = dijit.byId(attrId + '_hierarchy');
        var activeVariantAlias = eval(attrId + '_activeVariant');        
        var variant = cardLinkPickerGetActiveVariantObject(attrId);
        
      
        var node = dojo.byId(attrId + '_hierarchy');
        var loading = dojo.byId(attrId + '_loading');
        var filterLine = dijit.byId(attrId + '_filterLine');
        var filterButton = dijit.byId(attrId + '_filterButton');
        
 		node.setAttribute('style', 'display:none');
 		loading.setAttribute('style', 'display:block');

		var requestType;
		if (document.getElementById(attrId + '_wasInit').value == 'false') {
			document.getElementById(attrId + '_wasInit').value = 'true';
       		dojo.connect(filterLine.domNode, 'onkeypress', function(evt) {
				if (evt.keyCode == dojo.keys.ENTER) {
					cardLinkPickerShowDialogWithReplace(attrId, attrCode, title, multiValued, typed, dated, filterLine.attr("value"), replaceAttrId, replaceAttrCode, eqTemplateId);
				}});
       		dojo.connect(filterButton.domNode, 'onclick', function() {
       			cardLinkPickerShowDialogWithReplace(attrId, attrCode, title, multiValued, typed, dated, filterLine.attr("value"), replaceAttrId, replaceAttrCode, eqTemplateId);});
			dojo.connect(dlg, "hide", function() {
				node = dojo.byId(attrId + '_hierarchy');
				newNode = node.cloneNode(false);
       			node.parentNode.replaceChild(newNode, node);
				dijit.byId(attrId + '_hierarchy').destroyRecursive();
			});
		}

        var checkedCards = [];
		if (prevHier) {
			requestType = '<%= CardHierarchyServlet.REQUEST_FILTER_HIERARCHY %>';
			checkedCards = cardLinkPickerGetSelectedItems(prevHier.store)
		} else {
			requestType = '<%= CardHierarchyServlet.REQUEST_OPEN_HIERARCHY %>';			
        	for (var i = 0; i < variant.cards.length; ++i) {
        		checkedCards[i] = variant.cards[i].cardId;
        	}
		}

		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape(requestType))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', escape(checkedCards.join(',')))
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${filterQueryPlaceholder}', query ? query : '');
			

		for (var param in variant.query) {
			var paramValue = variant.query[param];
			if ('' !== paramValue) {
				hierarchyUrl += '&' + param + '=' + escape(paramValue);
			}
		}
        
       	if (prevHier != null) {
       		newNode = node.cloneNode(false);
       		node.parentNode.replaceChild(newNode, node);
        	prevHier.destroyRecursive();
        	node = newNode;
        }
        variant.hierarchyColumns.lengthColumns = 80;
        var hierarchy = new dbmiCustom.HierarchicalCardList(
			{
				readOnly: false,
				maxHeight: '400px',
				url : hierarchyUrl,
				columnsDescriptor: variant.hierarchyColumns,
				actions: variant.hierarchyActions,
				onAction: function(actionId, selectedItems, customInfo) {
					cardLinkPickerDialogActionCallbackWithReplace(customInfo.attrId, customInfo.attrCode, 
									actionId, selectedItems, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId);
				},
				onClickActions: {
					'open': function(cardId) {
						submitOpenLinkedCard(attrCode, cardId);
					}
				},
				customInfo: {
					attrId: attrId,
					attrCode: attrCode
				},
				scrollActionString: 'cardLinkPickerHandleScrolling("' + attrId + '","' + attrCode + '")',
			},
			node
		);
		dlg.show();
 		loading.setAttribute('style', 'display:none');
 		node.setAttribute('style', 'display:block');
 	}

 	function cardLinkPickerSetSelectedCards(attrId, attrCode, multiValued, attrCodeType, selectedCards, typed, typesLink, dated, datesLink) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		notShown = hierarchy ? hierarchy.notShownCards : [];
		var request = {
       		url: '<%=request.getContextPath()%>/servlet/SearchCardServlet',
       		content: {
       			'<%=CardLinkPickerSearchParameters.PARAM_ATTR_CODE%>': attrCode,
    			<%= CardLinkPickerSearchParameters.PARAM_ATTR_TYPE_CODE %>: attrCodeType,
       			'<%=CardLinkPickerSearchParameters.PARAM_NAMESPACE%>': '${namespace}',
       			'<%=CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS%>': eval(attrId + '_activeVariant'),
				<%= SearchCardServlet.PARAM_CALLER %>: '<%= CardAttributeEditorParameterHelper.getCallerField(renderRequest) %>',
       			'<%=SearchCardServlet.PARAM_BYCODES%>': selectedCards.concat(notShown).join(',')
       		},
       		handleAs: 'json',
       		sync: true,
	        load: function(response, ioArgs) {
	        	var variant = cardLinkPickerGetActiveVariantObject(attrId);
	        	variant.cards = response.items;
	        	var selectedItems = new Array();
	        	for (var vc in variant.cards) selectedItems[selectedItems.length] = variant.cards[vc].cardId;
	        	if(!multiValued && variant.cards.length > 1) {
	        		// (BR4J00035647) Убрано, т.к. изменения ПолушкинаП покрывают этот функционал.
	        		// (BR4J00035873, BR4J00036746) Возвращаем для случая, когда нажата кнопка Фильтровать.
	        		// (BR4J00036995 11.08.2014) Добавлено удаление всех карточек из списка кроме последней выбранной
	        		cardLinkPickerRemoveCards(attrId, attrCode, 1, typed, dated);
	        	}
	        	if (typed) {
	        		if (typesLink != null) {
	        			for (var i in selectedItems) {
	        				if(typesLink[selectedItems[i]] == undefined) {
	        					variant.types[selectedItems[i]] = null;
	        				} else {
	        					variant.types[selectedItems[i]] = typesLink[selectedItems[i]];
	        				}
	        			}
	        			var keys = Object.keys(typesLink);
	        			for (var i in keys) {
	        				if(indexOfNotStrict(selectedItems, keys[i]) < 0) {
	        					if(variant.types[keys[i]] != undefined) {
		        					delete variant.types[keys[i]];
	        					}
	        				}
	        			}
	        		}
	        			//variant.types = typesLink;
	        		else {
	        			variant.types = [];
	        			for (var i in selectedItems) {
	        				variant.types[selectedItems[i]] = null;
	        			}
	        		}
	        	}
	        	if (dated) {
	        		if (datesLink != null) {
	        			for (var i in selectedItems) {
	        				if(datesLink[selectedItems[i]] == undefined) {
	        					variant.dates[selectedItems[i]] = null;
	        				} else {
	        					variant.dates[selectedItems[i]] = datesLink[selectedItems[i]];
	        				}
	        			}
	        			var keys = Object.keys(datesLink);
	        			for (var i in keys) {
	        				if(indexOfNotStrict(selectedItems, keys[i]) < 0) {
	        					if(variant.dates[keys[i]] != undefined) {
		        					delete variant.dates[keys[i]];
	        					}
	        				}
	        			}
	        		}
	        		else {
	        			variant.dates = [];
	        			for (var i in selectedItems) {
	        				variant.dates[selectedItems[i]] = null;
	        			}
	        		}
	        	}
	        	cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
       		},
	        error: function(response, ioArgs) {
				console.error('HTTP status code: ', ioArgs.xhr.status);
       			return response;
       		}
		};
		dojo.xhrGet(request);
	}
 	
 	function shareVariantValues(attrId, variant){
		var variants = eval(attrId + '_variants');
		var keysArray = Object.keys(variants);
		
		keysArray.forEach(function(el){
			var vt = eval('variants.' + el);
			if(vt.choiceValue != variant.choiceValue){
    			vt.cards = variant.cards.slice(0);
    			if(variant.types){
    				vt.types = jQuery.extend(true, {}, variant.types);
    			}
    			if(variant.dates){
    				vt.dates = jQuery.extend(true, {}, variant.dates);
    			}
			}
		});
 	}
 	
 	function indexOfNotStrict(arr, elem) {
 		for (var i in arr) {
 			if(arr[i] == elem) {
 				return i;
 			}
 		}
 		return -1;
 	}

 	function cardLinkPickerSetSelectedCardsWithReplace(attrId, attrCode, attrCodeType, selectedCards, typed, dated, replaceAttrId, replaceAttrCode, eqTemplateId) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		notShown = hierarchy ? hierarchy.notShownCards : [];
		var request = {
       		url: '<%=request.getContextPath()%>/servlet/SearchCardServlet',
       		content: {
       			'<%=CardLinkPickerSearchParameters.PARAM_ATTR_CODE%>': attrCode,
    			<%= CardLinkPickerSearchParameters.PARAM_ATTR_TYPE_CODE %>: attrCodeType,
       			'<%=CardLinkPickerSearchParameters.PARAM_NAMESPACE%>': '${namespace}',
       			'<%=CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS%>': eval(attrId + '_activeVariant'),
				<%= SearchCardServlet.PARAM_CALLER %>: '<%= CardAttributeEditorParameterHelper.getCallerField(renderRequest) %>',
       			'<%=SearchCardServlet.PARAM_BYCODES%>': selectedCards.concat(notShown).join(',')
       		},
       		handleAs: 'json',
       		sync: true,
	        load: function(response, ioArgs) {

    			for(var i in response.items){
    				var card1 = response.items[i]; 
    				var selectTemplateId = card1.template;
					var variant = null;
	                try {
	                	variant = cardLinkPickerGetActiveVariantObject(replaceAttrId);
	                } catch (err){
	                    console.error('Error during get variant for replaceAttr '+replaceAttrId+': '+ err.description);
	                    variant = null;
	                }
    				// если шаблон выбранной карточки совпадает с шаблоном, при котором значение надо записывать в другой атрибут,
    				// причём другой атрибут есть на карточке, то заполняем выбранное значение в этот другой атрибут
    				if ((selectTemplateId==eqTemplateId)&&(!(variant == null))){
    					if (!(variant==null)){
	   						cardLinkPickerAddNewCard(replaceAttrId, card1);
	    					if (typed)
	    						variant.types[cards[i].cardId] = null;
	    					if (dated)
	    						variant.dates[cards[i].cardId] = null;
	    					cardLinkPickerRefreshTable(replaceAttrId, replaceAttrCode, typed, dated);
    					}
    				} else { 
    					variant = cardLinkPickerGetActiveVariantObject(attrId);
   						cardLinkPickerAddNewCard(attrId, card1);
    					if (typed)
    						variant.types[cards[i].cardId] = null;
    					cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
    				}
    			}
       		},
	        error: function(response, ioArgs) {
				console.error('HTTP status code: ', ioArgs.xhr.status);
       			return response;
       		}
		};
		dojo.xhrGet(request);
	}

	// добавление в список значений кардлинка новой карточки с проверкой, чтобы её там до этого не было
	function cardLinkPickerAddNewCard(attrId, newCard){
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		var isExists = false;
		for(var i in variant.cards){
			if (variant.cards[i].cardId == newCard.cardId){
				isExists = true;
				break;
			}
		}
		if (!(isExists==true)){
			variant.cards = variant.cards.concat(newCard);
		}		
	}
</script>
<%	
	if (request.getSession().getAttribute("userName") != null) {
	    request.getSession().setAttribute(CardPortlet.SESSION_USER, new UserPrincipal((String) request.getSession().getAttribute("userName")));
	} else {
	    request.getSession().setAttribute(CardPortlet.SESSION_USER, request.getUserPrincipal());
	}
%>