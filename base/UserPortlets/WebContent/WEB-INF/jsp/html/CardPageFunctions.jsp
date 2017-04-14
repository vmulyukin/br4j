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
<%@page import="com.aplana.dbmi.card.CardPortlet"%>
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager" %>
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletActionsManager" %>

<script Type ="text/javascript" language="javascript">
    dojo.require('dojo.fx');
    function wipeIt(idDiv, isWipe) {
    	var wipeArgs = {
           	node: idDiv,
           	duration: 250
        };
        if (isWipe) {
          	dojo.style(idDiv, 'display', 'block');
           	dojo.fx.wipeOut(wipeArgs).play();	
        } else {
           	dojo.style(idDiv, 'display', 'none');
           	dojo.fx.wipeIn(wipeArgs).play();
        }
   	}

    function block_collapse(id, isSaveState) {
    	id = 'BLOCK_' + id;
    	form_collapse(id, isSaveState);
	}
   	
   	function form_collapse(id, isSaveState) {
		// скрываем блок
		if (document.getElementById('ARROW_'+id).className == 'arrow') {
			if (isSaveState) {
				setCollapseItem(id, true);
			}
			document.getElementById('ARROW_'+id).className = 'arrow_up';
			wipeIt('BODY_'+id, true);
		// отображаем блок
		} else {
			if (isSaveState) {
				setCollapseItem(id, false);
			}
			document.getElementById('ARROW_'+id).className = 'arrow';
			wipeIt('BODY_'+id, false);
		}		
	}    	
	
	var listCollapseItems = [];
	function setCollapseItem(item, isCollapse) {
		if (isCollapse == true) {
			for(var i=0; i < listCollapseItems.length; i++) {
				if(listCollapseItems[i] == item) {
					return;
				}
			}
			listCollapseItems.push(item);
			dojo.byId('<%=CardPortlet.COLLAPSE_ID_BLOCKS %>').value = listCollapseItems.join(' ');
		} else {
			for(var i=0; i < listCollapseItems.length; i++) {
				if (listCollapseItems[i] == item) {
					listCollapseItems.splice(i, 1);
					dojo.byId('<%=CardPortlet.COLLAPSE_ID_BLOCKS %>').value = listCollapseItems.join(' ');
				}
			}
		}
	}
	function submitCardActionsManagerAction(actionId) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortletActionsManager.ACTION %>:' + actionId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
	function submitAttributeActionsManagerAction(attributeId, actionId) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortletAttributeEditorActionsManager.ACTION %>:' + actionId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
	function submitOpenLinkedCard(attributeId, cardId) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.OPEN_NESTED_CARD_ACTION %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_ID_FIELD%>.value = cardId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
	function downloadCardMaterial(cardId) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= CardPortlet.DOWNLOAD_MATERIAL_ACTION %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.CARD_ID_FIELD%>.value = cardId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
		<%-- old variant
		var url = '<%=request.getContextPath() + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "="%>' + cardId;
		window.open(url, '_blank');
		--%>
	}
	<%--
    var afterCardLoadFunctions = [];
	function addAfterCardLoadFunction(func) {
		afterCardLoadFunctions.push(func);
	}
	// Этот вызов должен быть внизу страницы, он должен выполняться после того как будут выполнены все onLoad обработчики в редакторах атрибутов 
	dojo.addOnLoad(function() {
		for(var i = 0; i < afterCardLoadFunctions.length; ++i) {
			var func = afterCardLoadFunctions[i]; 
			func();
		}
	});
	--%>
</script>