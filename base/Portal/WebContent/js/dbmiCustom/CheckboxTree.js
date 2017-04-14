/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
dojo.provide("dbmiCustom.CheckboxTreeNode");

dojo.require("dijit.Tree");
dojo.require("dijit.form.CheckBox");

dojo.declare(
"dbmiCustom.CheckboxTreeNode",
[dijit._TreeNode],
{    			   
    templateString:dojo.cache("dijit","templates/TreeNode.html","<div class=\"dijitTreeNode\" waiRole=\"presentation\"\n\t><div dojoAttachPoint=\"rowNode\" class=\"dijitTreeRow\" waiRole=\"presentation\" dojoAttachEvent=\"onmouseenter:_onMouseEnter, onmouseleave:_onMouseLeave, onclick:_onClick, ondblclick:_onDblClick\"\n\t\t><img src=\"${_blankGif}\" alt=\"\" dojoAttachPoint=\"expandoNode\" class=\"dijitTreeExpando\" waiRole=\"presentation\"\n\t\t><span dojoAttachPoint=\"expandoNodeText\" class=\"dijitExpandoText\" waiRole=\"presentation\"\n\t\t></span\n\t\t><span dojoAttachPoint=\"contentNode\"\n\t\t\tclass=\"dijitTreeContent\" waiRole=\"presentation\">\n\t\t\t<span dojoType=\"dijit.form.CheckBox\"></span><img src=\"${_blankGif}\" alt=\"\" dojoAttachPoint=\"iconNode\" class=\"dijitTreeIcon\" waiRole=\"presentation\"\n\t\t\t><span dojoAttachPoint=\"labelNode\" class=\"dijitTreeLabel\" wairole=\"treeitem\" tabindex=\"-1\" waiState=\"selected-false\" dojoAttachEvent=\"onfocus:_onLabelFocus, onblur:_onLabelBlur\"></span>\n\t\t</span\n\t></div>\n\t<div dojoAttachPoint=\"containerNode\" class=\"dijitTreeContainer\" waiRole=\"presentation\" style=\"display: none;\"></div>\n</div>\n"),
    widgetsInTemplate: true,

    // return the dijit.Checkbox inside the tree node
    getNodeCheckbox: function(){
		if (this._supportingWidgets.length > 0) {
			return this._supportingWidgets[0];
		}
		return null;
    },
      
    setNodeCheckboxValue: function(value){  
        this.getNodeCheckbox().attr('checked',value);
    },

      
    postCreate: function(){
		this.inherited(arguments);

        // preload
        // get value from the store (JSON) of the property "checked" and set the checkbox
		//  store.isItem to exclude dummy root in case of ForestStoreModel
		if (this.tree.model.store.isItem(this.item)) {
			if (this.tree.model.store.hasAttribute(this.item, 'checked')) {
				var attrValue = this.tree.model.store.getValue(this.item, 'checked');
				var val = (attrValue === true || (attrValue.toLowerCase && attrValue.toLowerCase() === 'true'));		
				this.setNodeCheckboxValue(val) ;
			}
		}
		
		// if setCheckboxOnClick is true
		if (this.tree.setCheckboxOnClick == false) {
			// connect onChange of the checkbox to alter the model of the tree
			dojo.connect(this.getNodeCheckbox(),'onChange',this,
					function(){
						var checkboxWidget = this.getNodeCheckbox();
						var checkboxValue = checkboxWidget.attr('value');
						this.tree.model.store.setValue(this.item,"checked",(checkboxValue == false ? false : true));
						
						//console.log('onChange checkbox on ' + this.tree.model.getIdentity(this.item));
					});
		}
	
    },
          
    getCheckedNodesList: function(nodeArray){
        if ( this.getNodeCheckbox().isChecked() ) {
            nodeArray.push(this.item.label);
		}
            
        this.getChildren().forEach(getCheckedNodesList(nodeArray), this);            
    }            
       
});

dojo.provide("dbmiCustom.CheckboxTree");

dojo.declare(
"dbmiCustom.CheckboxTree",
[dijit.Tree],
{
	// setCheckboxOnClick: Boolean
	//		If true, clicking a node will change the checkbox state
	//		If true, openOnClick will be set to false
	setCheckboxOnClick: false,
	
	postMixInProperties: function(){
		
		if (this.setCheckboxOnClick == true) {
			// if setCheckboxOnClick is true, openOnClick must be false
			openOnClick = false;
			
			dojo.connect(this, "onClick", this, function(/* dojo.data */ item, /*TreeNode*/ node, /*Event*/ evt) {
				var isChecked = this.model.store.getValue(item, 'checked');
				isChecked = (isChecked != null && (isChecked === true || (isChecked.toLowerCase && isChecked.toLowerCase() === 'true')));
				this.model.store.setValue(item, 'checked', !isChecked);
				
				// log
				//console.log('onClick on ' + this.model.store.getValue(item, 'name'));
			});
		}

		this.inherited(arguments);
	},

    _createTreeNode: function(/*Object*/ args){
		// summary:
		//		creates a TreeNode
		// description:
		//		Developers can override this method to define their own TreeNode class;
		//		However it will probably be removed in a future release in favor of a way
		//		of just specifying a widget for the label, rather than one that contains
		//		the children too.
		return new dbmiCustom.CheckboxTreeNode(args);
	},
	
    _onItemChange: function(/*Item*/ item){
        this.inherited(arguments);
		
		//---
        var model = this.model,
			identity = model.getIdentity(item),
			nodes = this._itemNodesMap[identity];

        if(nodes){
			var newValue = this.model.store.getValue(item,"checked");
			dojo.forEach(nodes,function(node){
				node.setNodeCheckboxValue(newValue);
			});
		}
    }

});


dojo.provide("dbmiCustom.CheckboxTreeStoreModel");

dojo.declare(
"dbmiCustom.CheckboxTreeStoreModel",
[dijit.tree.TreeStoreModel],
{
	onChange: function(/*dojo.data.Item*/ item){
		var currStore = this.store;
		var newValue = currStore.getValue(item,"checked");

		this.getChildren(item,function(children){      
			dojo.forEach(children,function(child){
				currStore.setValue(child,"checked",newValue);
			});
		});    
	}
});


dojo.provide("dbmiCustom.CheckboxForestStoreModel");

dojo.declare(
"dbmiCustom.CheckboxForestStoreModel",
[dijit.tree.ForestStoreModel],
{
	onChange: function(/*dojo.data.Item*/ item){
		/*
		var currStore = this.store;
		var newValue = currStore.getValue(item,"checked");

		this.getChildren(item,function(children){      
			dojo.forEach(children,function(child){
				currStore.setValue(child,"checked",newValue);
			});
		});   
		*/ 
	}
});