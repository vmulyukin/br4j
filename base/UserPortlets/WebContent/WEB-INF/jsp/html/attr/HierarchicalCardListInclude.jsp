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
<%@page import="com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionsManager"%>
<script language="javascript" type="text/javascript">
	dojo.addOnLoad(function(){
		dojo.require('dbmiCustom.HierarchicalCardList');
	});
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jit.js" ></script>
<!--[if IE]>
	
		<script type="text/javascript" src="<%=request.getContextPath()%>/js/Extras/excanvas.js"></script>
<![endif]-->

<script type="text/javascript">
dojo.require("dijit.Tooltip");

function openDocBackLink(url) {
	window.location.assign(url);
}

function initTreeHierarchicalCardList(idCanvas, json) {    
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
			'height': 56,
			'width': 200
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
							'<td><table>';
			if (node.data.typeNode == 'inbound') {
				html +=			'<tr><td><div class="text">'+attrs.regNum.value+'</div></td></tr>'+
								'<tr><td><div class="text">от '+attrs.regDate.value+'</div></td></tr>';
			} else if (node.data.typeNode == 'resolut'){
				var executors = attrs.executor.value;
				if (attrs.coexecutors.value || attrs.ad_notam.value || attrs.ext_executors.value) {
					executors = executors+',...';
				}
				html +=			'<tr><td><div class="text">Подписант: '+attrs.signer.value+'</div></td></tr>'+
								'<tr><td><div class="text">Исполнитель: '+executors+'</div></td></tr>'+
								'<tr><td><div class="text">Срок: '+attrs.term.value+'</div></td></tr>';
			}
			html +=			'</table></td>'+
						'</tr>'+
					'</table>'+
				'</div>';
        	domElement.innerHTML = html;
        	
        	var textTooltip;
			if (node.data.typeNode == 'inbound' && attrs.shortDesc.value != '') {
        		textTooltip = attrs.shortDesc.value;
        		if (textTooltip.length > 150) {
        			textTooltip = textTooltip.substr(0, 147)+'...';
        		}
        	} else if (node.data.typeNode == 'resolut') {
        		textTooltip = 	'Подписант: '+ attrs.signer.value+'; '+
        						'Исполнитель: '+attrs.executor.value+'; '+
        						'Соисполнители: '+attrs.coexecutors.value+'; '+
        						'К сведению: '+attrs.ad_notam.value+'; '+
        						'Внешние исполнители: '+attrs.ext_executors.value+'; '+
        						'Срок: '+attrs.term.value+'; '+
        						'Контроль: '+attrs.oncont.value;
        		var shortDesc = attrs.shortDesc.value;
        		if (shortDesc.length > 150) {
        			shortDesc = shortDesc.substr(0, 147)+'...';
        		}
        		textTooltip += '; '+shortDesc;
        	} else {
        		textTooltip += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
        	}

        	var htmlTooltip = '<table class="info"><tr><td>'+ textTooltip +'</td></tr></table>';
        	
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