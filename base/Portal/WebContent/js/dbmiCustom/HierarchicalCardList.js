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
dojo.require('dbmiCustom.CheckboxTree');
dojo.require('dbmiCustom.TriStateCheckBox');

dojo.require('dojo.fx');
dojo.require("dijit.Tree");

dojo.provide('dbmiCustom.CardColumnsTreeNode');
dojo.declare(
	'dbmiCustom.CardColumnsTreeNode',
	[dbmiCustom.CheckboxTreeNode],
	{
		nodeWidth: '30%', //default
		style: '',
		columnsDescriptor: [], 
		templateString:
			'<div class="dijitTreeNode" waiRole="presentation">' +
				'<div dojoAttachPoint="rowNode" style="overflow:auto;" class="dijitTreeRow" waiRole="presentation" dojoAttachEvent="onmouseenter:_onMouseEnter, onmouseleave:_onMouseLeave, onclick:_onClick, ondblclick:_onDblClick">' +
					'<div style="float:right; ${style}" dojoAttachPoint="columnsNode"></div>' +
					'<img src="${_blankGif}" alt="" dojoAttachPoint="expandoNode" class="dijitTreeExpando" waiRole="presentation">' +
					'<span dojoAttachPoint="expandoNodeText" class="dijitExpandoText" waiRole="presentation"></span>' +
					'<span dojoAttachPoint="contentNode" class="dijitTreeContent" waiRole="presentation">' + 
						'<span dojoType="dijit.form.CheckBox"></span>' +
						'<img src="${_blankGif}" alt="" dojoAttachPoint="iconNode" class="dijitTreeIcon" waiRole="presentation">' +
						'<span dojoAttachPoint="labelNodeParent" class="dijitTreeLabel hierarchicalCardList" wairole="treeitem" tabindex="-1" waiState="selected-false" dojoAttachEvent="onfocus:_onLabelFocus, onblur:_onLabelBlur" style="display: inline-block; width: ${nodeWidth};">' +
							'<div dojoAttachPoint="labelNode" style="max-width: 95%; text-overflow: ellipsis; float:left; white-space: nowrap; overflow: hidden !important; ${style}">' +
							'</div>' +
						'</span>' +
		   			'</span>' +
				'</div>' + 
				'<div dojoAttachPoint="containerNode" class="dijitTreeContainer" waiRole="presentation" style="display: none;"></div>' +
			'</div>',
		widgetsInTemplate: true,
		postCreate: function() {
			this.inherited(arguments);
			if (this.item.columns) {
				// AP 2010/05
				var div = document.createElement("div");
				div.className = 'cardColumns';
				div.style.width = 30;
				if (this.item.infoItems) {
					var span = document.createElement("span");
					span.className = 'arrow';
					span.innerHTML = '&nbsp;';
					var a = document.createElement("a");
					a.href = '#';
					a.className = 'noLine';
					a.appendChild(span);
					div.appendChild(a);
				}
				this.columnsNode.appendChild(div);
				// AP 2010/05
				
				var columnsDescriptor = this.tree.parent.columnsDescriptor;
				var onClickActions = this.tree.parent.onClickActions;
				for (var i = columnsDescriptor.length - 1; i >= 0; --i) {
					var columnDesc = columnsDescriptor[i];
					var columnValue = this.item.columns[i]; 
					var el = document.createElement("div");
					var link = null;
					if (columnDesc.linkAction) {
						var callback = onClickActions[columnDesc.linkAction];
						if (callback != null) {
							link = document.createElement("a");
							link.href = '#';
							link.innerHTML = (columnValue != null) ? columnValue : '';
							el.appendChild(link);
							var clicker = {
								cardId: this.item.cardId,
								callback: callback,
								onclick: function() {
									this.callback(this.cardId);
								}
							}
							dojo.connect(link, 'onclick', clicker, 'onclick');
						}
					}
					if (link == null) {
						if (columnValue == null || columnValue == '') {
							el.innerHTML = '&nbsp;';	
						} else {
							el.innerHTML = columnValue;
						}
					}
					el.className = 'cardColumns';
					el.style.width = columnDesc.width;
					this.columnsNode.appendChild(el);
					var label = this.attr('label');
					this.labelNode.title = label;

					//alert('Item ' + this.item.cardId + ': ' + fullLabel);
					var linkAction;
					if (this.item.asLink == 'true') {
						linkAction = 'open';
					} else if(this.item.asDownloadLink == 'true') {
						linkAction = 'download';
					}
					if(linkAction != null && linkAction != '' && label != null) {
						var callback = onClickActions[linkAction];
						var link = document.createElement("a");
						link.href = '#';
						link.innerHTML = label;//this.labelNode.innerHTML;
						this.labelNode.innerHTML = '';
						this.labelNode.appendChild(link);
						var clicker = {
							cardId: this.item.cardId,
							callback: callback,
							onclick: function() {
								this.callback(this.cardId);
							}
						}
						dojo.connect(link, 'onclick', clicker, 'onclick');
					}
					var style = String(this.item.style);
					
					if(style) { 
						var styleParts = style.split(':');
							
						if(styleParts.length > 1 && styleParts[0].trim() == 'documentPrintForm') { // печатные формы
							var printForm = styleParts[1].trim();
							if(printForm == 'ARM') { // печатная форма в АРМе
								this.labelNodeParent.innerHTML += "<a href='javascript://' onClick='ShowPrintForm("+this.item.cardId+")'><img style='margin-left: 5px;' src='/DBMI-Portal/boss/images/icon_details.gif'></a>";
							} else { // печатная форма в синем
								this.labelNodeParent.innerHTML += "<a href='javascript://' onClick=\"ShowPrintForm("+this.item.cardId+",'"+printForm+"')\"><img style='margin-left: 5px;' src='/DBMI-Portal/boss/images/icon_details.gif'></a>";
							}
						}
					}
				}
				
				// AP 2010/05
				if (this.item.infoItems && this.item.infoItems.length > 0) {
					var panel = document.createElement("div");
					/*panel.style.width = '85%';
					panel.style.cssFloat = 'right';
					panel.style.backgroundColor = '#ddd';*/

					panel.style.width = '860px';
					panel.style.cssFloat = 'right';
					if (!dojo.isChrome) {
						panel.style.marginTop = '2px';
					} else {
						panel.style.marginTop = '-12px';
						panel.style.cssFloat = 'right';
						this.columnsNode.style.width = '500px';
					}
					
					panel.className = 'cardInfo';
					this.columnsNode.appendChild(panel);
					var container = document.createElement("div");
					if (container.style && this.rowNode && this.rowNode.style) {
						container.style.cssText = this.rowNode.style.cssText;
					}
					panel.appendChild(container);
					
					var copyArray = function(source, copyItem) {
						//alert('Copying ' + source.length + ' items');
						var copy = new Array();
						if (source)
							for (var i = 0; i < source.length; i++)
								copy[i] = copyItem(source[i]);
						return copy;
					};
					var copyItem = function(source) {
						//alert ('Copying ' + source.label);
						return {
							id: source.id,
							type: source.type,
							label: source.label,
							asLink: source.asLink,
							collapsed: source.collapsed,
							checked: source.checked,
							children: source.children ? copyArray(source.children, arguments.callee) : [],
							actions: source.actions,
							columns: source.columns,
							cardId: source.cardId,
							style: source.style
						};
					}
					var	model = new dbmiCustom.CheckboxForestStoreModel({
						store: new dojo.data.ItemFileWriteStore({data: {
							identifier: 'id',
							label: 'label',
							items: copyArray(this.item.infoItems, copyItem)}}),
						//query: {type: 'group'},
						childrenAttrs: ['children']
					});
					var tree = new dbmiCustom.CardListTree(
						{
							parent: this.tree.parent,
							cookieName: this.tree.parent.cardListTree.cookieName,
							persist: true,
							model: model,
							showRoot: false,
							openOnClick: false,
							readOnly: true,
							setCheckboxOnClick: false
						},
						container
					);
					/*tree.model.store.fetch({
						queryOptions: {deep: true},
						onItem: function(item) {
							alert(item + ' ' + item.id + ': ' + item.label);
						}
					});*/

					var roller = {
						div: panel,
						rolled: false,
						roll: function(e) {
							var params = {
								node: this.div,
								duration: 250
							}
							if (this.rolled) {
								//this.div.style.display = 'block';
								dojo.fx.wipeIn(params).play();
								e.target.className = 'arrow';
							} else {
								dojo.fx.wipeOut(params).play();
								//this.div.style.display = 'none';
								e.target.className = 'arrow_up';
							}
							this.rolled = !this.rolled;
						}
					}
					dojo.connect(a, 'onclick', roller, 'roll');
				}
				// AP 2010/05
			}
			if (this.tree.readOnly) {
				this.getNodeCheckbox().destroy();
			}
		}
	}

);
	
dojo.provide('dbmiCustom.SelectAllCheckBox');
dojo.declare(
	'dbmiCustom.SelectAllCheckBox',
	[dbmiCustom.TriStateCheckBox],
	{
		parent: null,
		readOnly: false,
		connectH: null,

		_resolveState: function(){
			var unChecked = 0;
			var checked = 0;
			var currStore = this.parent.model.store;
			currStore.fetch({
				query: {cardId: '?*', checked: true},
				queryOptions: {deep: true},
				onComplete: dojo.hitch(function(items, request) {
					checked = items.length;					
				})
			});
			currStore.fetch({
				query: {cardId: '?*', checked: false},
				queryOptions: {deep: true},
				onComplete: dojo.hitch(function(items, request) {
					unChecked = items.length;					
				})
			});
			if (unChecked == 0){
				this._setValueAttr(1);
				return;
			}
			if (checked == 0){
				this._setValueAttr(0);
				return;
			}
			this._setValueAttr(2);
		},
		postCreate: function() {
			this.inherited(arguments);
			if (this.readOnly){
				this._setStyleAttr({display: 'none'});
				return;
			}
			if (this.label==''){
				this._setStyleAttr({display: 'none'});
				return;
			}
			this._resolveState();
			connectH = dojo.connect(this.parent.model, 'onChange', this, this._resolveState);
		},
		onClick: function() {
			this.inherited(arguments);
			var currStore = this.parent.model.store;
			var currModel = this.parent.model;
			var newValue = (this._getValueAttr()=='on' ? true : false);
			
			dojo.disconnect(connectH);
			currModel.doCheckButtons = false;
			currStore.fetch({
				query: {cardId: '?*'},
				queryOptions: {deep: false},
				onComplete: dojo.hitch(function(items, request) {
					dojo.forEach(items,function(item){
						currStore.setValue(item,"checked",newValue);
					});
				})
			});
			currModel.checkButtons();
			currModel.doCheckButtons = true;
			connectH = dojo.connect(this.parent.model, 'onChange', this, this._resolveState);
		}
	}
);

/* 
 * Actually this widget doesn't use cookie to store/restore nodes state,
 * but we need 'persist' flag to be set in order to collapse some nodes (with item.collapsed = true) 
 * at initialization phase  
 **/
dojo.provide('dbmiCustom.CardListTree');
dojo.declare(
	'dbmiCustom.CardListTree',
	[dijit.Tree],
	{	
		setCheckboxOnClick: false,
		parent: null,
		persist: true,		
		widgetsInTemplate: true,
		readonly: false,
		_onKeyPress: function(args) {
			/* do nothing 
			   Переопределеяем метод, потому что иначе винджет зацикливался в Node'ах вызывая функции _onLetterKeyNav и _getNextNode
			*/
		},
		_createTreeNode: function(args){
			if(this.nodeWidth) {
				args.nodeWidth = this.nodeWidth;
			}
			if (args.item.style) {
				args.style = args.item.style;
			}
			if (this.domNode && this.srcNodeRef 
					&& this.domNode.style && this.srcNodeRef.style) {
				this.domNode.style.cssText = this.srcNodeRef.style.cssText;
			}
			return new dbmiCustom.CardColumnsTreeNode(args);
		},
		getIconStyle: function(item, opened) {
			if (item.icon) {
				return {
					'backgroundImage': 'url(\'' + item.icon + '\')'
				};
			} else {
				return {
					'display': 'none'
				};
			}
		},
		_saveState: function() {
			/* do nothing */			
		},		
		_initState: function() {
			/* reading state from model instead of cookies */
			this._openedItemIds = {};
			this.model.store.fetch({
				query: {collapsed: false},
				queryOptions: {deep: true},
				sync: true,
				store: this.model.store,
				tree: this,
				onComplete: function(items, request) {
					for (var i = 0; i < items.length; ++i) {
						var nodeId = request.tree.model.getIdentity(items[i]);
						request.tree._openedItemIds[nodeId] = true;
					}
				}
			});			
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
	}
);

dojo.provide("dbmiCustom.HierarchicalCardListStoreModel");
dojo.declare(
"dbmiCustom.HierarchicalCardListStoreModel",
[dbmiCustom.CheckboxForestStoreModel],
{
	_recursionLevel: 0,
	hierarchicalList: null,
	doCheckButtons: true,
	onChange: function(/*dojo.data.Item*/ item){

		
		++this._recursionLevel;
		this.inherited(arguments);
		--this._recursionLevel;

		var currStore = this.store;
		var newValue = currStore.getValue(item,"checked");
		if(currStore.getValue(item, "checkChildren") == true){	//Распространять значение предка на потомков?
			this.getChildren(item,function(children){   
				dojo.forEach(children,function(child){
					currStore.setValue(child,"checked",newValue);
				});
			}); 
		}
		if ((this._recursionLevel == 0)&&(this.doCheckButtons)) {
			if(currStore.getValue(item, "terminalNodesOnly") == true){
				if(!item.children||item.children.length==0){
					this.checkButtons();
				}
			} else {
				this.checkButtons();
			}
		}
	},
	_onNewItem: function(item, parentInfo){
		if (item.treeModelCommand == 'refresh') {
			this._requeryTop();
			this.inherited(arguments);
		}
	},	
	
	_onDeleteItem: function(item, parentInfo){
		if (item.treeModelCommand == 'refresh') {
			this._requeryTop();
			this.inherited(arguments);
		}
	},	
	
	checkButtons:function(){
		var actions = this.hierarchicalList.actions;
		var activeActions = {};
		for (var i = 0; i < actions.length; ++i) {
			activeActions[actions[i].id] = 0;
		}
		var actionButtonPrefix = this.hierarchicalList.jsId + '_action_'
		var store = this.hierarchicalList.store;
		var hierarchicalList = this.hierarchicalList;
		store.fetch({
			query: {cardId: '?*', checked: true},
			queryOptions: {deep: true},
			onComplete: dojo.hitch(function(items, request) {
				var sz = items.length;					
				for (var i = 0; i < sz; ++i) {
					var itemActions = store.getValues(items[i], 'actions');
					for (var j = 0; j < itemActions.length; ++j) {
						activeActions[itemActions[j]] += 1;
					}
				}
				for (var i = 0; i < actions.length; ++i) {
					var action = actions[i];
					var btn = hierarchicalList._actionButtons[action.id];
					if (action.selectionType == 'none') {
						continue;
					} else if (sz == 0) {
						btn.setAttribute("disabled", true);
						continue;
					} else if (action.selectionType == 'single' && sz != 1) {
						btn.setAttribute("disabled", true);
						continue;
					} else {							
						//if (activeActions[action.id] == sz) {
							btn.setAttribute("disabled", false);
						//} else {
						//	btn.setAttribute("disabled", true);
						//}
					}
				}
			})
		});
	}
});

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require('dojo.data.ItemFileWriteStore');

dojo.provide('dbmiCustom.HierarchicalCardList');
dojo.declare(
	'dbmiCustom.HierarchicalCardList',
	[dijit._Widget, dijit._Templated, dijit._Container],
	{
		url: null,
		store: null,
		model: null,
		checker: null,
		maxHeight: 'none',
		columnsDescriptor: [],
		hasInfoBlocks: false,
		readOnly: true,
		widgetsInTemplate: false,
		_actionButtonPrefix: '',
		_actionButtons: {},
		actions: [],
		onClickActions: {},
		customInfo: {},
		selectAllLabel:'',
		onAction: function(actionId) {
			alert('Action callback is not defined [WidgetId = ' + this.id + ']');
		},
		scrollActionString: '',
		waitingResponse: false,
		templateString: 
			'<div>' +
				'<div dojoAttachPoint="filterBar"></div>' +
				'<div dojoAttachPoint="actionBar" style="float:right;"></div>' +
				'<div style="float:none; clear:both;">'+
				'<div dojoAttachPoint="selectAllCheckBox"></div>' +
				'<div style="float:right; clear:right;" dojoAttachPoint="columnsHeader"></div>' +
				'</div>'+ 
				'<div onscroll="${scrollActionString}" style="clear:both; max-height: ${maxHeight}; overflow: auto; width: 917px;">' +
					'<div dojoAttachPoint="cardListTree"></div>' +
				'</div>' +
				'<input dojoAttachPoint="selectedItems" type="hidden"/>' +
			'</div>',
		notShownCards: [],
		endOfData: false,
		noDocsMsg: null,
		postCreate: function() {
			this.inherited(arguments);
			
			this._actionButtonPrefix = this.jsId + '_action_';			
			var request = {
				data: [],
				notShownCards: [],
				endOfData: false,
				noDocsMsg: null,
        		url: this.url, 
        		handleAs: 'json',
        		sync: true,
		        load: function(response, ioArgs) {
		        	this.data = response.data;
		        	this.notShownCards = response.notShownCards;
		        	this.endOfData = response.endOfData;
		        	this.noDocsMsg = response.noDocsMsg;
        		},
		        error: function(response, ioArgs) {
					console.error('HTTP status code: ', ioArgs.xhr.status);
          			return response;
          		}
			};
			dojo.xhrGet(request);
			var store = new dojo.data.ItemFileWriteStore({data: request.data});
			this.store = store; 
		    this.notShownCards = request.notShownCards;
		    this.endOfData = request.endOfData;
		    this.noDocsMsg = request.noDocsMsg;
			
			var	treeModel = new dbmiCustom.HierarchicalCardListStoreModel({
				hierarchicalList: this,
				store: store,
				childrenAttrs: ['children']
			});
			this.model = treeModel;
			
			if (store._jsonData.items && store._jsonData.items.length < 1) {
				var noDocsMsg = 'No documents found';
				var div = document.createElement("div");
				div.style.width = '100%';
				div.style.color = 'gray';
				div.style.fontStyle = 'italic';
				div.style.textAlign = 'center';
				div.style.fontWeight = 'bold';
				if(request.noDocsMsg && request.noDocsMsg.length > 0) {
					noDocsMsg = request.noDocsMsg;
				}
				div.innerHTML = noDocsMsg;
				this.cardListTree.appendChild(div);
			} else {
				var nodeWidth = this.nodeWidth;
				var tree = new dbmiCustom.CardListTree(
					{
						nodeWidth: nodeWidth,
						parent: this,					
						model: treeModel,
						showRoot: false,
						openOnClick: false,
						readOnly: this.readOnly
					},
					this.cardListTree
				);
			}

			var selectAll = new dbmiCustom.SelectAllCheckBox(
					{
						parent: this,
						readOnly: (this.readOnly)||(request.data.items.length == 0),
						checked: true,
						label:this.selectAllLabel
					},
					this.selectAllCheckBox
				);
			this.checker = selectAll;

			// AP 2010/05
			if (this.hasInfoBlocks) {
				var div = document.createElement("div");
				div.className = 'no_arrow';
				div.innerHTML = '&nbsp;';
				this.columnsHeader.appendChild(div);
			}
			// AP 2010/05
			for (var i = this.columnsDescriptor.length - 1; i >= 0; --i) {
				var el = document.createElement("div");
				var col = this.columnsDescriptor[i];
				el.innerHTML = col.title;
				el.className = 'cardColumns';
				el.style.width = col.width;
				this.columnsHeader.appendChild(el);
			}			
			//if (!this.readOnly) {
				this.selectedItems.name = this.id + '_selectedItems';
				for (var i = 0; i < this.actions.length; ++i) {
					var action = this.actions[i];
					var actionId = this._actionButtonPrefix + action.id;
					var disabled = action.selectionType != 'none';
					var actionButton = new dijit.form.Button({
						label: action.title,
						jsId: actionId,
                        actionref: action,
						parent: this,
						disabled: disabled,
						onClick: function() {


				       		if (this.confirmation) {
				       			if (!confirm(this.confirmation)) {
				       				return;
				       			}
				       		}

				       		if (this.actionref && this.actionref.jsEntryPoint) {
                                try {

                                    eval(this.actionref.jsEntryPoint);

                                } catch (err){
                                    console.error('Error during excecute javascript action entry point: ' + err.description);
                                }

                                return;

                            }

							this.parent.store.fetch({
								onAction: this.parent.onAction,
								query: {cardId: '?*', checked: true},
								queryOptions: {deep: true},
								actionId: this.jsId,
								hierarchicalList: this.parent,
								onComplete: function(items, request) {
									var sel = [];
									var store = request.hierarchicalList.store;
									var actions = request.hierarchicalList.actions;
									for (var i = 0; i < items.length; ++i) {
										var actionId = store.getValue(items[i], 'actions');
										for(var j = 0; j < actions.length; j++){
											 if(actions[j].id == actionId){
												 var id = store.getValue(items[i], 'cardId');
												 if(sel.indexOf(id) == -1) sel[sel.length] = id;
												 break;
											 }
										}									
									}
									request.hierarchicalList.selectedItems.value = sel.join(',');
									request.onAction(request.actionId.replace(request.hierarchicalList._actionButtonPrefix, ''), sel, request.hierarchicalList.customInfo);
								}
							});


						}
					});
					if (action.confirmation) {
						actionButton.confirmation = action.confirmation;
					}
					actionButton.placeAt(this.actionBar);
					this._actionButtons[action.id] = actionButton;
					
				}
			//}
		},
		appendItems: function(items) {
			hstore = this.store;
	        function addRecursively(item, update, father) {
	        	var children = undefined;
	        	if ((typeof item.children == 'object') && item.children.length >= 1) {
	        		children = item.children;
	        		item.children = [];
	        	} else if (update) {
	        		item.treeModelCommand = 'refresh';
	        	}
	        	var newItem;
	        	if (father) {
	        		newItem = hstore.newItem(item, {parent: father, attribute: 'children'});
	        	} else {
	        		newItem = hstore.newItem(item);
	        	}
	        	if (children) {
	        		var last = children.length - 1;
	        		for (var i = 0; i < children.length; i++) {
	        			addRecursively(children[i], update ? i == last : false, newItem);
	        		}
	        	}
	        }
	        var last = items.length - 1;
			for (var i = 0; i < items.length; i++) {
	        	addRecursively(items[i], i == last);
	        }
		}
	}
);
