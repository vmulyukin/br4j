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
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.ajax.HierarchyConnection"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>

<script type="text/javascript" language="javascript">


	function cardLinkPickerStringTypes(types) {
		var str = [];
		for (var key in types) {
			str[str.length] = key+':'+types[key];
		}
		return str.join('<%=CardLinkPickerAttributeEditor.ID_DELIMITER%>');
	}
	
	function cardLinkPickerGetActiveVariantObject(attrId) {
		return eval(attrId + '_variants[' + attrId + '_activeVariant]');
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
								jsId: attrId+action.id,
								disabled: disabled,
								onClick: function() {
											commonCardLinkPickerDialogActionCallback(
													attrId, 
													attrCode, 
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



	function commonCardLinkPickerDialogActionCallback(attrId, attrCode, actionId, selectedItems, typed, dated) {
		var dlg = dijit.byId(attrId + '_dialog');
		if ('<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>' == actionId) {
			cardLinkPickerSetSelectedCards(attrId, attrCode, 'empty',selectedItems, typed, dated);
			dlg.hide();
		} else if ('<%=CardLinkPickerAttributeEditor.ACTION_CANCEL%>' == actionId) {
			dlg.hide();
		} else {
			document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortletAttributeEditorActionsManager.ACTION %>:' + actionId;
			document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attrCode;
			document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
		}
	}

	function cardLinkPickerRemoveCard(attrId, attrCode, index, typed, dated) {
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		if (typed) {
			var cardId = variant.cards[index].cardId;
			delete variant.types[cardId];
		}
		variant.cards.splice(index, 1);
		cardLinkPickerRefreshTable(attrId, attrCode, typed, dated);
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


	<%-- Обработчик события прокрутки списка --%>
	function cardLinkPickerHandleScrolling(attrId, attrCode) {
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


	

</script>