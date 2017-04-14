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
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerHierarchyParameters"%>
<%@page import="com.aplana.dbmi.ajax.ResponseTransmittedParameters"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="java.net.URLEncoder"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<fmt:setBundle basename="com.aplana.dbmi.gui.nl.CardLinkPickerEditResource" />

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

<c:url var="hierarchyServletURLTemplate" value="/servlet/CardHierarchyServlet">
	<c:param name="<%= CardHierarchyServlet.PARAM_CALLER %>" value="<%= ResponseTransmittedParameters.CALLER %>"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_REQUEST_TYPE %>" value="##requestType##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_NAMESPACE%>" value="${namespace}"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ATTR_CODE%>" value="##attrCode##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ACTIVE_VARIANT%>" value="##activeVariant##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_CHECKED_CARDS%>" value="##checkedCards##"/>	
	<c:param name="<%= CardHierarchyServlet.PARAM_HIERARCHY_KEY%>" value="##hierarchyKey##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_FILTER_QUERY%>" value="##filterQuery##"/>
	<c:param name="<%= ResponseTransmittedParameters.PARAM_FILTER_TEMPLATE%>" value="##filterTemplate##"/>
	<c:param name="<%= ResponseTransmittedParameters.PARAM_FILTER_STATE%>" value="##filterState##"/>
</c:url>

<c:set var="attrCodePlaceholder"><%=URLEncoder.encode("##attrCode##", "UTF-8")%></c:set>
<c:set var="activeVariantPlaceholder"><%=URLEncoder.encode("##activeVariant##", "UTF-8")%></c:set>
<c:set var="checkedCardsPlaceholder"><%=URLEncoder.encode("##checkedCards##", "UTF-8")%></c:set>
<c:set var="requestTypePlaceholder"><%=URLEncoder.encode("##requestType##", "UTF-8")%></c:set>
<c:set var="hierarchyKeyPlaceholder"><%=URLEncoder.encode("##hierarchyKey##", "UTF-8")%></c:set>
<c:set var="filterQueryPlaceholder"><%=URLEncoder.encode("##filterQuery##", "UTF-8")%></c:set>
<c:set var="filterTemplatePlaceholder"><%=URLEncoder.encode("##filterTemplate##", "UTF-8")%></c:set>
<c:set var="filterStatePlaceholder"><%=URLEncoder.encode("##filterState##", "UTF-8")%></c:set>

<%@include file="/WEB-INF/jsp/html/attr/CardLinkPickerCommon.jsp"%>

<script type="text/javascript" language="javascript">

		dojo.require('dijit.dijit');
		dojo.require('dijit.Dialog');
		dojo.require('dijit.form.ComboBox');
		dojo.require('dijit.form.Button');
		dojo.require('dojox.data.QueryReadStore');		
		dojo.require('dojo.parser');
		dojo.require('dijit.TitlePane');
		dojo.require('dbmiCustom.HierarchicalCardList');
	
	function responseTransmittedDialogActionCallback(attrId, attrCode, actionId, selectedItems, typed) {
		var dlg = dijit.byId(attrId + '_dialog');
		if ('<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>' == actionId) {
			cardLinkPickerSetSelectedDocuments(attrId, attrCode, selectedItems, typed);
			dlg.hide();
		} else if ('<%=CardLinkPickerAttributeEditor.ACTION_CANCEL%>' == actionId) {
			dlg.hide();
		} else {
			document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortletAttributeEditorActionsManager.ACTION %>:' + actionId;
			document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attrCode;
			document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
		}
	}
	
	function hideLoading(attrId)  {
		
		var loading = dojo.byId(attrId + '_loading');		
		loading.setAttribute('style', 'display:none');
		
	}
	
	function validateComboValue(comboBox) {

		var item = comboBox.item;

		var displayedValue = comboBox.attr("displayedValue");

		if ((!item) && (displayedValue != null) && (displayedValue != '')) {
			return false;
		}	

		return true;
			
	}
	
	function cardLinkCreateHierarchy(attrId, attrCode,  typed) {

		var prevHier = dijit.byId(attrId + '_hierarchy');
		
        var activeVariantAlias = eval(attrId + '_activeVariant');         
        var variant = cardLinkPickerGetActiveVariantObject(attrId);
        
      
        var node = dojo.byId(attrId + '_hierarchy');
        var loading = dojo.byId(attrId + '_loading');
        var filterButton = dijit.byId(attrId + '_filterButton');
		var filter = '';
		/*var filterLine = dijit.byId(attrId + '_filterLine');
		var filter = filterLine.attr("value");*/

		/*
		var filterCardId = dijit.byId(attrId + '_filterId');
		var filterId = filterCardId.attr("value");
				 
		var filterYearCombo = dijit.byId(attrId + '_Year');
		var filterYear = filterYearCombo.attr("value");
		*/

		var filterDocTypeCombo = dijit.byId(attrId + '_DocType');
		var filterTemplate = '';
		if(filterDocTypeCombo.item)
			filterTemplate = filterDocTypeCombo.item.value;

		
		/*if (!validateComboValue(filterDocTypeCombo) ) {
			alert('<fmt:message key="filter.template.invalidvalue" />');
			return;
		}*/
		
		var filterDocStateCombo = dijit.byId(attrId + '_DocState');
		var filterState = '';
		if(filterDocStateCombo.item)
			filterState = filterDocStateCombo.item.value;

		
		/*if (!validateComboValue(filterDocStateCombo) ) {
			alert('<fmt:message key="filter.state.invalidvalue" />');
			return;
		}*/
        
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
			.replace('${filterTemplatePlaceholder}', filterTemplate ? filterTemplate : '')
			.replace('${filterQueryPlaceholder}', filter ? filter : '')
			.replace('${filterStatePlaceholder}', filterState ? filterState : '');		
 
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
				url : hierarchyUrl ,
				columnsDescriptor: variant.hierarchyColumns,
				actions: variant.hierarchyActions,
				onAction: function(actionId, selectedItems, customInfo) {
					responseTransmittedDialogActionCallback(customInfo.attrId, customInfo.attrCode, 
									actionId, selectedItems, typed);
				},
				customInfo: { 
					attrId: attrId,
					attrCode: attrCode
				},
				scrollActionString: 'cardLinkPickerHandleScrolling("' + attrId + '","' + attrCode + '")',
			},
			node
		);
		
	}
	
	function getSelectValue(selectElement) {		
		var selIndex = selectElement.selectedIndex;
		return  selectElement.options[selIndex].value;
	}
	
	function cardLinkPickerDisplayDialog(attrId, attrCode, title, multiValued, typed) {
		var dlg = dijit.byId(attrId + '_dialog');

		var filterButton = dijit.byId(attrId + '_filterButton');
				 
   		dojo.connect(filterButton.domNode, 'onclick', function() {
   			cardLinkCreateHierarchy(attrId, attrCode,  typed);
		});
		
		dlg.show();
		hideLoading(attrId);
	}
	
	<%-- Добавление элементов верхнего уровня в конец списка --%>
	function cardLinkPickerAppendElements(attrId, attrCode) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		if (hierarchy.endOfData)
			return;
		var activeVariantAlias = eval(attrId + '_activeVariant');
		var filter = '';

		/*var filterLine = dijit.byId(attrId + '_filterLine');
		var filter = filterLine.attr("value");*/

		var filterDocTypeCombo = dijit.byId(attrId + '_DocType');
		var filterTemplate = '';
		if(filterDocTypeCombo.item)
			filterTemplate = filterDocTypeCombo.item.value;
			
		var filterDocStateCombo = dijit.byId(attrId + '_DocState');
		var filterState = '';
		if(filterDocStateCombo.item)
			filterState = filterDocStateCombo.item.value;	
		
		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape('<%= CardHierarchyServlet.REQUEST_ADD_ITEMS %>'))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', cardLinkPickerGetSelectedItems(hierarchy.store).join(','))
			.replace('${filterTemplatePlaceholder}', filterTemplate ? filterTemplate : '')
			.replace('${filterQueryPlaceholder}', filter ? filter : '')
			.replace('${filterStatePlaceholder}', filterState ? filterState : '');			
		var request = {
       		url: hierarchyUrl,
       		content: {},
       		handleAs: 'json',
       		sync: true,
	        load: function(response, ioArgs) {
	        	hierarchy.appendItems(response.data);
	        	hierarchy.notShownCards = response.notShownCards;
	        	hierarchy.endOfData = response.endOfData;
     		},
	        error: function(error) {
				console.error('Error: ', error);
       		}
		};
		dojo.xhrGet(request);		
	}
	
	function getIgnoredCardIds(attrId) {
		
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		
		var storedCards = variant.cards;

		var ignoreCardIds = '';
		for (var i in variant.cards) {	
			if (ignoreCardIds != '')
				ignoreCardIds = ignoreCardIds + ',';
			ignoreCardIds = ignoreCardIds + variant.cards[i].cardId;
		}

		return ignoreCardIds;		
		
	}
	
	function cardLinkPickerSetSelectedDocuments(attrId, attrCode, selectedCards, typed, typesLink) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		notShown = hierarchy ? hierarchy.notShownCards : [];
 
		var ignoredCardIds = getIgnoredCardIds(attrId);
		
		var request = {
       		url: '<%=request.getContextPath()%>/servlet/SearchCardServlet',
       		content: {
       			'<%=CardLinkPickerSearchParameters.PARAM_ATTR_CODE%>': attrCode,
       			'<%=CardLinkPickerSearchParameters.PARAM_NAMESPACE%>': '${namespace}',
       			'<%=CardLinkPickerSearchParameters.PARAM_VARIANT_ALIAS%>': eval(attrId + '_activeVariant'),
				<%= SearchCardServlet.PARAM_CALLER %>: '<%= CardLinkPickerSearchParameters.CALLER %>',
       			'<%=SearchCardServlet.PARAM_BYCODES%>': selectedCards.concat(notShown).join(','),
       			'<%=SearchCardServlet.PARAM_IGNORE%>': ignoredCardIds,
       		},
       		handleAs: 'json',
       		sync: true,
	        load: function(response, ioArgs) {
	        	var variant = cardLinkPickerGetActiveVariantObject(attrId);
	        	variant.cards = variant.cards.concat(response.items);
	        	if (typed) {
	        		if (typesLink != null)
	        			variant.types = typesLink;
	        		else {
	        			for (var i in response.items) {
	        				variant.types[response.items[i].cardId] = null;
	        			}
	        		}
	        	} 
	        	responseTransmittedRefreshTable(attrId, attrCode, typed);
       		},
	        error: function(response, ioArgs) {
				console.error('HTTP status code: ', ioArgs.xhr.status);
       			return response;
       		}
		}; 
		dojo.xhrGet(request);
	}
	
	function responseTransmittedRefreshTable(attrId, attrCode, typed) {
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
			while (j < columns.length) {
				var column = columns[j];
				cell = row.insertCell(j);
				cell.innerHTML = card.columns[column.attrId];
				++j;
			}
			if (typed) {
				<%-- Выпадающий список выбора типа связи --%>
				cell = row.insertCell(j);
				selType[i] = document.createElement('select');
				selType[i].id = attrId + '_select_type' + (i+firstRow);
				selType[i].cardId = card.cardId;
				selType[i].attrId = attrId;
				selType[i].onchange = eval(attrId + '_cardLinkPickerRefreshValues');

  				var selIndex = 0;
				for (var k=0; k < typesCLink.length; k++) {
					option = document.createElement('option');
					option.text = typesCLink[k].name;
					option.value = typesCLink[k].id;
					if (variant.types[card.cardId] == typesCLink[k].id)	{	
						selIndex = k;
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
			cell = row.insertCell(j);
			cell.style.textAlign = 'right';
			cell.innerHTML = '<a onclick="responseTransmittedRemoveCard(\'' + attrId + '\', \'' + attrCode + '\',' + i + ',' + (typed?'true':'false') + ')" ' +
				'style="text-decoration: none;"><span class="delete">&nbsp;</span></a>';
		}
		dojo.byId(attrId + '_values').value = typed ? cardLinkPickerStringTypes(variant.types) : sel;
	}
	
	function responseTransmittedSwitchVariant(attrId, variantAlias, attrCode, typed) {
		eval(attrId + '_activeVariant = variantAlias');
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		
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
		
			cell = row.insertCell(i);
			cell.innerHTML = '';
		}
		responseTransmittedRefreshTable(attrId, attrCode, typed);
		cardLinkPickerSwitchToolBarVariant(attrId, attrCode, variant, typed);
	}
	
	function responseTransmittedRemoveCard(attrId, attrCode, index, typed) {
		var variant = cardLinkPickerGetActiveVariantObject(attrId);
		if (typed) {
			var cardId = variant.cards[index].cardId;
			delete variant.types[cardId];
		}
		variant.cards.splice(index, 1);
		responseTransmittedRefreshTable(attrId, attrCode, typed);
	}
</script>