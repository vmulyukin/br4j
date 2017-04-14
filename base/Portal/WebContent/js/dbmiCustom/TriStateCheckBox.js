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
/**
  read more at http://wirehopper.com/tri.php
**/

dojo.require('dijit.form._FormWidget');

dojo.provide('dbmiCustom.TriStateCheckBox');
dojo.declare(
	'dbmiCustom.TriStateCheckBox',
	[dijit.form._FormWidget, dijit._Container],
	{
		templateString: 
		 '<div class="dijitTreeRow">'+	
		 	'<div class="dijitCheckBox" waiRole="presentation" dojoAttachEvent="onclick:_onClick" dojoAttachPoint="focusNode" style="float:left">'+
		 		'<input type="hidden" name="${name}" dojoAttachPoint="inputNode"/>'+
		 	'</div>'+
		 	'<div dojoAttachPoint="labelNode" style="float:left">dfjhjgkdfjgkdfjg</div>'+
		 '</div>',
		baseClass:'',
		name:'',
		alt:'',
		value:'off',
		type:'hidden',
		tabIndex:'0',
		label:'initial label',
		disabled:false,
		readOnly:false,
		required:false,
		attributeMap:dojo.mixin(
				dojo.clone(dijit._Widget.prototype.attributeMap),
				{
					onchange:'inputNode',
					value:'inputNode',
					disabled:'focusNode',
					readOnly:'focusNode',
					id:'inputNode',
					tabIndex:'focusNode',
					alt:'focusNode',
					invalidMessage:'focusNode',
					errorMessage:'focusNode',
					promptMessage:'focusNode'
				}
		),
		invalidMessage:'',
		promptMessage:'',
		errorMessage:'',

// valid: valid values
//	Possible values
		valid: Array('off','on','indeterminate'),
		i: 2,
		_onClick: function(/*Event*/ e){
// summary: internal function to handle click actions
			if(this.disabled || this.readOnly){
				dojo.stopEvent(e); 
				return false;
			}
			this.i++;
			if (this.i>=this.valid.length-1)
				this.i=0;
			this._setValueAttr(this.i);
			this.onClick(e);
			this.onChange(e);
		},
		_setStateClass:function(){
			switch (this.i)
			{
				case 0: this.focusNode.setAttribute('class','dijitCheckBox');break;
				
				case 2:	this.focusNode.setAttribute('class','dijitCheckBox dijitCheckBoxIndeterminate');break;
			}
		},
		_getValueAttr:function()
		{
			return this.value;
		},
		_setValueAttr:function(v)
		{
			switch (typeof v)
			{
				case 'string':
					v=dojo.indexOf(this.valid,v);
					break;
				case 'boolean':
					v=(v)?1:0;
					break;
				case 'number':
					break;
				default:
					alert('Invalid value type');
					return;
			}
			this.i=v;
			var x=this.valid[this.i];
			this.value=x;
			dojo.attr(this.inputNode,'value',x);
			this._setStateClass();
			this.inherited("onChange",arguments);
		},
		_setCheckedAttr:function(_b){
			this._setValueAttr(_b?1:0);
		},
		_getCheckedAttr:function()
		{
			return this.i==1;
		},
		_setDisabledAttr:function(_1){
			this.disabled=_1;
			dojo.attr(this.focusNode,"disabled",_1);
			dijit.setWaiState(this.focusNode,"disabled",_1);
			if(_1){
				this._hovering=false;
				this._active=false;
				this.focusNode.removeAttribute("tabIndex");
			}else{
				this.focusNode.setAttribute("tabIndex",this.tabIndex);
			}
			this._setStateClass();
		},
		isValid:function(){
			return (this.value==this.valid[0]) || (this.value==this.valid[1]);
		},
		reset:function(){
			this._hasBeenBlurred=false;
			this._setValueAttr(2);
		},

		isFocusable:function(){
			return !this.disabled&&!this.readOnly&&this.focusNode&&(dojo.style(this.focusNode,"display")!="none");
		},
		focus:function(){
			dijit.focus(this.focusNode);
		},
		onChange:function(_13){
		},
		postCreate:function(){
			this._setValueAttr(this.inputNode.value);
			this.inherited(arguments);
			this._onChangeActive=true;
			this._setStateClass();
			this.labelNode.innerHTML = this.label;
//			alert(this.label);
//			alert(this.labelNode.innerText);
		}
	}
);

 