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
<%@page import="com.aplana.dbmi.card.BackLinkAttributeEditor"%>
<%@page import="com.aplana.dbmi.service.UserPrincipal"%>
<%@taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<fmt:setBundle basename="com.aplana.dbmi.card.nl.CardPortletResource"/>

<input type="hidden" name="<%= BackLinkAttributeEditor.FIELD_LINKED_ID %>" value=""/>
<script Type ="text/javascript" language="javascript"><!--
	function submitFormBackLinksAdd(attributeId) {
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= BackLinkAttributeEditor.ACTION_ADD %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
	function submitFormBackLinksRemove(attributeId, linkId) {
		if (!confirm('<fmt:message key="edit.link.confirm.remove"/>'))
			return false;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ACTION_FIELD %>.value = '<%= BackLinkAttributeEditor.ACTION_REMOVE %>';
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= CardPortlet.ATTR_ID_FIELD %>.value = attributeId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.<%= BackLinkAttributeEditor.FIELD_LINKED_ID %>.value = linkId;
		document.<%= CardPortlet.EDIT_FORM_NAME %>.submit();
	}
//--></script>

<script type="text/javascript" src="<%=request.getContextPath()%>/js/jit.js" ></script>
<!--[if IE]>
	
		<script type="text/javascript" src="<%=request.getContextPath()%>/js/Extras/excanvas.js"></script>
<![endif]-->

<script type="text/javascript">
dojo.require("dijit.Tooltip");

function openDocBackLink(url) {
	window.location.assign(url);
}

function initTreeBackLink(idCanvas, json) {    
    var canvas = new Canvas('canvas_'+idCanvas, {    
    	'injectInto': idCanvas,  
    	'width': 925,  
    	'height': 400 
    });  
      
    var st= new ST(canvas, {  
    	levelDistance: 50,
		Node: {  
	 		'overridable': true,  
			'type': 'rectangle',
			'height': 32,
			'width': 125
		},  
      	Edge: {  
      		'type': 'bezier',
      		'overridable': true,  
       		'color': '#ccb'  
      	},  
		onCreateLabel: function(label, node) {  
			label.id = node.id;  
			label.innerHTML = node.name;  
			label.onclick = function(){  
				st.onClick(node.id);  
			};  
		},  
		onBeforePlotNode: function(node) {    
			if (node.selected) {  
				node.data.$color = "#fff";  
			} else {  
				node.data.$color = "#ccc";  
			}  
		},  
		onBeforePlotLine: function(adj){  
			if (adj.nodeFrom.selected && adj.nodeTo.selected) {  
				adj.data.$color = "#eed";  
				adj.data.$lineWidth = 3;  
			} else {  
				delete adj.data.$color;  
				delete adj.data.$lineWidth;  
			}  
		},
     	onPlaceLabel: function(domElement, node) {
     		var attrs = node.data.attrs;
        	var html =
				'<div class="doc">'+
					'<table>'+
						'<tr>'+
							'<td><div class="image" onclick="openDocBackLink(\''+node.data.linkURL+'\')" title="Открыть документ"></div></td>'+
							'<td><table>'+
									'<tr><td><div class="text">'+attrs.regNum.value+'</div></td></tr>'+
									'<tr><td><div class="text">'+attrs.regDate.value+'</div></td></tr>'+
							'</table></td>'+
						'</tr>'+
					'</table>'+
				'</div>';
        	domElement.innerHTML = html;
        	
        	var htmlTooltip ='<table class="info">';
			if (attrs.shortDesc.value !== undefined  && attrs.shortDesc.value != '') {
        		htmlTooltip += '<tr><td>'+attrs.shortDesc.value+'</td><tr>';
        	} else {
        		htmlTooltip += '<tr><td>'+'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'+'</td><tr>';
        	}
        	htmlTooltip += '</table>';
        	
   
	        var tooltip = new dijit.Tooltip({
	           	connectId: [domElement.id],
	           	label: htmlTooltip
	        });
      }
    });  
      
    st.loadJSON(json);    
    st.compute();  
    st.onClick(st.root);  
} 
</script>
<%
	if (request.getSession().getAttribute("userName") != null) {
	    request.getSession().setAttribute(CardPortlet.SESSION_USER, new UserPrincipal((String) request.getSession().getAttribute("userName")));
	} else {
	    request.getSession().setAttribute(CardPortlet.SESSION_USER, request.getUserPrincipal());
	}
%>
