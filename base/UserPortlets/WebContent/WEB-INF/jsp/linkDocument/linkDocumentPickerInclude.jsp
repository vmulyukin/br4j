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
﻿<%@page session="false" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.aplana.dbmi.card.CardLinkPickerAttributeEditor"%>
<%@page import="com.aplana.dbmi.ajax.CardHierarchyServlet"%>
<%@page import="com.aplana.dbmi.card.CardPortlet"%> 
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager"%>
<%@page import="com.aplana.dbmi.ajax.SearchCardServlet"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerSearchParameters"%>
<%@page import="com.aplana.dbmi.ajax.CardLinkPickerHierarchyParameters"%>
<%@page import="com.aplana.dbmi.ajax.DocumentPickerParameters"%>
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
	<c:param name="<%= CardHierarchyServlet.PARAM_CALLER %>" value="<%= DocumentPickerParameters.CALLER %>"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_REQUEST_TYPE %>" value="##requestType##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_NAMESPACE%>" value="${namespace}"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ATTR_CODE%>" value="##attrCode##"/>
	<c:param name="<%= CardLinkPickerHierarchyParameters.PARAM_ACTIVE_VARIANT%>" value="##activeVariant##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_CHECKED_CARDS%>" value="##checkedCards##"/>	
	<c:param name="<%= CardHierarchyServlet.PARAM_HIERARCHY_KEY%>" value="##hierarchyKey##"/>
	<c:param name="<%= CardHierarchyServlet.PARAM_FILTER_QUERY%>" value="##filterQuery##"/>
	<c:param name="<%= DocumentPickerParameters.PARAM_FILTER_YEAR%>" value="##filterYear##"/>
	<c:param name="<%= DocumentPickerParameters.PARAM_FILTER_TEMPLATE%>" value="##filterTemplate##"/>
	<c:param name="<%= DocumentPickerParameters.PARAM_BASE_CARD_ID%>" value="##baseCardId##"/>
</c:url>

<c:set var="attrCodePlaceholder"><%=URLEncoder.encode("##attrCode##", "UTF-8")%></c:set>
<c:set var="activeVariantPlaceholder"><%=URLEncoder.encode("##activeVariant##", "UTF-8")%></c:set>
<c:set var="checkedCardsPlaceholder"><%=URLEncoder.encode("##checkedCards##", "UTF-8")%></c:set>
<c:set var="requestTypePlaceholder"><%=URLEncoder.encode("##requestType##", "UTF-8")%></c:set>
<c:set var="hierarchyKeyPlaceholder"><%=URLEncoder.encode("##hierarchyKey##", "UTF-8")%></c:set>
<c:set var="filterQueryPlaceholder"><%=URLEncoder.encode("##filterQuery##", "UTF-8")%></c:set>
<c:set var="filterYearPlaceholder"><%=URLEncoder.encode("##filterYear##", "UTF-8")%></c:set>
<c:set var="filterTemplatePlaceholder"><%=URLEncoder.encode("##filterTemplate##", "UTF-8")%></c:set>
<c:set var="baseCardIdPlaceholder"><%=URLEncoder.encode("##baseCardId##", "UTF-8")%></c:set>

<%@include file="/WEB-INF/jsp/html/attr/CardLinkPickerCommon.jsp"%>

<script type="text/javascript" language="javascript">

	dojo.addOnLoad(function() {
		dojo.require('dijit.dijit');
		dojo.require('dijit.Dialog');
		dojo.require('dijit.form.ComboBox');
		dojo.require('dijit.form.Button');
		dojo.require('dojox.data.QueryReadStore');
		dojo.require('dojo.parser');
		dojo.require('dijit.TitlePane');
		dojo.require('dbmiCustom.HierarchicalCardList');
	});
	
	function cardLinkPickerDisplayDialog(attrId, attrCode, title, multiValued, typed) {
		var linkDocDlg = dijit.byId('linkDocDlg');
		if(linkDocDlg) {
			linkDocDlg.hide();
		}
		
		var dlg = dijit.byId(attrId + '_dialog');
		var filterButton = dojo.byId(attrId + '_filterButton');
		filterButton.onclick = function() {
			cardLinkCreateHierarchy(attrId, attrCode,  typed);
		};
		
		dlg.show();
		hideLoading(attrId);
	}
	
	function documentPickerDialogActionCallback(attrId, attrCode, actionId, selectedItems, typed) {
		var dlg = dijit.byId(attrId + '_dialog');
		if ('<%=CardLinkPickerAttributeEditor.ACTION_ACCEPT%>' == actionId) {
			dlg.hide();
			var selStr = selectedItems.join(',');
			saveCurDocLinks(selStr);
		} else { //if ('CardLinkPickerAttributeEditor.ACTION_CANCEL' == actionId) {
			unlockScreen();
			dlg.hide();
		}
	}
	
	function hideLoading(attrId) {
		var loading = dojo.byId(attrId + '_loading');
		if(loading) {
			loading.setAttribute('style', 'display:none');
		}
	}

	function validateComboValue(comboBox) {
		var displayedValue = comboBox.attr("displayedValue");
		if (displayedValue != null && displayedValue.length > 0) {
			return true;
		}
		return false;
	}

	function cardLinkCreateHierarchy(attrId, attrCode, typed) {

		var prevHier = dijit.byId(attrId + '_hierarchy');
		
        var activeVariantAlias = eval(attrId + '_activeVariant');         
        var variant = cardLinkPickerGetActiveVariantObject(attrId);
        
      
        var node = dojo.byId(attrId + '_hierarchy');
        var loading = dojo.byId(attrId + '_loading');
        var filterButton = dijit.byId(attrId + '_filterButton');

		var filterLine = dijit.byId(attrId + '_filterLine');
		var filter = filterLine.attr("value");
		
		var filterYearCombo = dijit.byId(attrId + '_Year');
		var filterYear = filterYearCombo.attr("value");

		var filterDocTypeCombo = dijit.byId(attrId + '_DocType');
		var filterTemplate = '';
		if(filterDocTypeCombo.value) {
			filterTemplate = filterDocTypeCombo.value;
		}


		if (!validateComboValue(filterYearCombo)) {
			alert('<fmt:message key="filter.year.invalidvalue" />');
			return;
		}
		
		if (!validateComboValue(filterDocTypeCombo) ) {
			alert('<fmt:message key="filter.template.invalidvalue" />');
			return;
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

		var baseCardId = '';
		var dlgInput = dojo.byId('linkDocDlg_baseCardId');
		if(dlgInput) {
			baseCardId = dlgInput.value;
		}

		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape(requestType))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', escape(checkedCards.join(',')))
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${filterYearPlaceholder}', filterYear ? filterYear : '')
			.replace('${filterTemplatePlaceholder}', filterTemplate ? filterTemplate : '')
			.replace('${filterQueryPlaceholder}', filter ? filter : '')
			.replace('${baseCardIdPlaceholder}', baseCardId ? baseCardId : '');
		
 
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
					documentPickerDialogActionCallback(customInfo.attrId, customInfo.attrCode, 
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

	function documentPickerSwitchVariant(attrId, variantAlias, attrCode, typed) {
		eval(attrId + '_activeVariant = variantAlias');
	}

	<%-- Добавление элементов верхнего уровня в конец списка --%>
	function cardLinkPickerAppendElements(attrId, attrCode) {
		hierarchy = dijit.byId(attrId + '_hierarchy');
		if (hierarchy.endOfData)
			return;
		var activeVariantAlias = eval(attrId + '_activeVariant');


		var filterLine = dijit.byId(attrId + '_filterLine');
		var filter = filterLine.attr("value");
		
		var filterYearCombo = dijit.byId(attrId + '_Year');
		var filterYear = filterYearCombo.attr("value");

		var filterDocTypeCombo = dijit.byId(attrId + '_DocType');
		var filterTemplate = '';
		if(filterDocTypeCombo.value) {
			filterTemplate = filterDocTypeCombo.value;
		}		
		
		var baseCardId = '';
		var dlgInput = dojo.byId('linkDocDlg_baseCardId');
		if(dlgInput) {
			baseCardId = dlgInput.value;
		}
		
		var hierarchyUrl = '${hierarchyServletURLTemplate}'
			.replace('${requestTypePlaceholder}', escape('<%= CardHierarchyServlet.REQUEST_ADD_ITEMS %>'))
			.replace('${attrCodePlaceholder}', escape(attrCode))
			.replace('${activeVariantPlaceholder}', activeVariantAlias)
			.replace('${hierarchyKeyPlaceholder}', '<%=CardLinkPickerAttributeEditor.HIERARCHY_PREFIX%>' + activeVariantAlias)
			.replace('${checkedCardsPlaceholder}', cardLinkPickerGetSelectedItems(hierarchy.store).join(','))
			.replace('${filterYearPlaceholder}', filterYear ? filterYear : '')
			.replace('${filterTemplatePlaceholder}', filterTemplate ? filterTemplate : '')
			.replace('${filterQueryPlaceholder}', filter ? filter : '')
			.replace('${baseCardIdPlaceholder}', baseCardId ? baseCardId : '');
		
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
     		},
	        error: function(error) {
				console.error('Error: ', error);
				//console.error('HTTP status code: ', ioArgs.xhr.status);
       			//return response;
       		}
		};
		dojo.xhrGet(request);		
	}	
</script>
