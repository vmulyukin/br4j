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
<%@page import="com.aplana.dbmi.service.UserPrincipal"%>

<style type="text/css">
.AnotherCheckbox {
	position: relative;
	left: 21px;
}
.AnotherInput {
	position: relative;
	left: 19px;
}
</style>

<script type="text/javascript">
	dojo.require("dijit.Tree");
	dojo.require("dijit.form.CheckBox");
	dojo.require("dbmiCustom.CheckboxTree");
	dojo.require("dojo.data.ItemFileWriteStore");
	dojo.require("dojo.data.ItemFileReadStore");
	
	dojo.provide("TreeAttributeCheckboxTreeNode");
	dojo.provide("TreeAttributeCheckboxTree");
	dojo.provide("TreeAttributeCheckboxForestStoreModel");
		dojo.declare(
		"TreeAttributeCheckboxTreeNode",
		[dbmiCustom.CheckboxTreeNode],
		{    			   
		    setNodeCheckboxValue: function(value){  
		    	if(this.getChildren().length == 0 && this.isExpandable){
			    	this.expandoNode.click();
			    	this.expandoNode.click();
		    	}
		        this.getNodeCheckbox().attr('checked',value);
		        
		        if (value == true) {
		        	this.labelNode.style.fontWeight = "bold";
		        } else {
		        	this.labelNode.style.fontWeight = "normal";
		        }
		        /*
		        this.getChildren().forEach(function(el){
		        	el.setNodeCheckboxValue(value);
				});*/
		        refreshInputFromTree(dojo.byId(this.tree.model.attrHtmlId+"_values"), this.tree.model.store);
		    }
		});
		
		dojo.declare(
		"TreeAttributeCheckboxTree",
		[dbmiCustom.CheckboxTree],
		{
		    _createTreeNode: function(/*Object*/ args){
		    	var treeNode = new TreeAttributeCheckboxTreeNode(args);
		    	var nodeCheckbox = treeNode.getNodeCheckbox();
		    	if(this.model.viewMode){
		    		nodeCheckbox.setDisabled(true);
		    	}
		    	nodeCheckbox.node = treeNode;
		    	dojo.connect(nodeCheckbox, 'onClick', function(){
		    		var node = this.node;
		    		var oldValue = false;
		    		if(this.node.item.checked){
		    			oldValue = this.node.item.checked[0];
		    		}
		    		var allChildren = [];
		    		var curLevel = [];
		    		curLevel.push(this.node);
		    		while(curLevel.length > 0){
		    			var nextLevel = [];
		    			curLevel.forEach(function(el){
		    				el.getChildren().forEach(function(el){
		    					nextLevel.push(el);
		    					allChildren.push(el);
		    				});
		    			});
		    			curLevel = nextLevel;
		    		}
		    		allChildren.forEach(function(el){
		    				el.setNodeCheckboxValue(!oldValue);
		    			});
		    		if(oldValue){
			    		var curParent = this.node.getParent();
			    		while(curParent.indent >=0){
			    			curParent.setNodeCheckboxValue(!oldValue);
			    			curParent = curParent.getParent();
			    		}
		    		}
					if(!oldValue){
			    		var curParent = this.node.getParent();
			    		while(curParent.indent >=0){
			    			var allChecked = true;
			    			curParent.getChildren().forEach(function(el){
			    				if(el.item.id[0]!=node.item.id[0] &&!el.item.checked){
			    					allChecked = false;
			    					return;
			    				}
			    				if(el.item.id[0]!=node.item.id[0] && !el.item.checked[0]){
			    					allChecked = false;
			    				}
			    			});
							if(allChecked){
								if(!curParent.item.checked){
									curParent.item.checked = [];
								}
								curParent.item.checked[0]=true;
								curParent.setNodeCheckboxValue(!oldValue);
							}
							curParent = curParent.getParent();
			    		}
					}
		    		
		    			
		    	})
				return treeNode;
			}
		});
		
		dojo.declare(
		"TreeAttributeCheckboxForestStoreModel",
		[dbmiCustom.CheckboxForestStoreModel],
		{
			attrHtmlId: ""
		});
	function refreshInputFromTree(hidden, store) {
		hidden.value = "";
		
		var gotItems = function(items, request){
			var first = true;
			for(var i = 0; i < items.length; i++) {
				if (store.getValue(items[i], 'checked') === true) {
					if (first == false) {
						hidden.value += ', ';
					}
					else {
						first = false;
					}
					hidden.value += store.getValue(items[i], 'id');
				}
			}
		};
		store.fetch({onComplete:gotItems, queryOptions: {deep:true}});
	}
	
	function setTreeFromValues(store, values) {
		var gotItems = function(items, request) {
			if(values.length>0){
				for(var i = 0; i < items.length; i++) {
					store.setValue(items[i], 'checked', false);
					for(var j = 0; j < values.length; j++) {
						if (store.getValue(items[i], 'id') == values[j].id) {
							store.setValue(items[i], 'checked', true);
						}
					}
				}
			}
		}
		store.fetch({onComplete:gotItems, queryOptions: {deep:true}});
	}
	
	function update_view_input(checkbox, input) {
		if (checkbox.checked == true) {
			input.disabled = false;
			input.style.fontWeight = "bold";
		}
		else {
			input.disabled = true;
			input.style.fontWeight = "normal";
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