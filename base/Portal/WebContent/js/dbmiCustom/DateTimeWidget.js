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
    dojo.require('dijit._Widget');
    dojo.require('dijit._Templated');
    dojo.require('dijit.Dialog');
    dojo.require('dijit.form.Button');
    dojo.require('dojo.data.ItemFileReadStore');
    dojo.require('dijit.form.DateTextBox');
    dojo.require('dijit.form.TimeTextBox');
    dojo.require('dijit._Container');

	dojo.provide('dbmiCustom.DateTimeWidget');
	

    dojo.declare('dbmiCustom.DateTimeWidget',
    	[dijit._Widget, dijit._Templated, dijit._Contained], {
        isContainer: true,
        //templateString: '<div><div dojoAttachPoint="date"></div><label dojoAttachPoint="calendar"><span class="date">&nbsp</span></label><div dojoAttachPoint="time"></div><div>',
          templateString: '<div><div dojoAttachPoint="date"></div><label dojoAttachPoint="calendar"></label><div dojoAttachPoint="time"></div><div>',
        nameDate: '',
        nameTime: '',
        _date: null,
        _time: null,
        isShowTime: false,
        _widthDate: 90,
  		_widthTime: 78,
		_styleDate:'',
		_styleTime:'',
  		timePattern: '',
  		valueDate: null,
  		valueString: '',
        postCreate: function(){
			this.inherited(arguments);
			if (this.valueDate == null && this.valueString != '') {
				this.valueDate = dojo.date.stamp.fromISOString(this.valueString);
			}
			this._date = new dijit.form.DateTextBox(
				{
					name: this.nameDate,
					id: this.nameDate,
					value: this.valueDate,
					style: this._styleDate != '' ? this._styleDate : 'width: ' + this._widthDate + 'px'
				}, 
				this.date
			);
			this.connect(this._date, 'onBlur', '_validDate');
			this.connect(this._date, 'onChange', '_validDate');

			this.calendar.htmlFor = this.nameDate;
			if (this.isShowTime) {
				var pattern = "HH";
			    var regex = new RegExp('m');
				if (regex.test(this.timePattern)) {
					pattern = pattern + ":mm";
				}
				regex = new RegExp('s');
				if (regex.test(this.timePattern)) {
					pattern = pattern + ":ss";
				}
				this._time = new dijit.form.TimeTextBox(
					{
						name: this.nameTime,
						value: this.valueDate,
						constraints: {timePattern: pattern},
						style: this._styleTime != '' ? this._styleTime : 'width: ' + this._widthTime + 'px'
					},
					this.time
				);
				this.connect(this._time, 'onBlur', '_validTime');
				this._validTime();
			}
			this._validDate();
		},
		_oldValueDate: null,
		_validDate: function() {
	  		if (!this._date.isValid(true)) {
		  		this._date.attr('value', this._oldValueDate);
		  	} else {
		  		var value = this._date.attr('value');
		  		if (value == null) {
		  			this._oldValueDate = null;
		  		} else { 
			  		if (this._oldValueDate == null) {
				  		this._oldValueDate = new Date();
			  		}
			  		this._oldValueDate.setFullYear(value.getFullYear());
				  	this._oldValueDate.setMonth(value.getMonth());
				  	this._oldValueDate.setDate(value.getDate());
			  	}
		  	}
		  	if (this._date.attr('value') == null) {
		  		if (this._time != null) {
		  			this._time.attr('value', null);
		  			this._time.attr('disabled', true);
		  		}
		  	} else {
		  		if (this._time != null && this._time.attr('disabled') == true) {
		  			this._time.attr('disabled', false);
		  		}
		  	}
  		},
  		_oldValueTime: null,
  		_validTime: function() {
	  		if (!this._time.isValid(true)) {
	  			this._time.attr('value', this._oldValueTime);
	  		} else {
	  			var value = this._time.attr('value');
	  			if (value == null) {
	  				this._oldValueTime = null;
	  			} else {
		  			if (this._oldValueTime == null) {
			  			this._oldValueTime = new Date();
			  		}
			  		this._oldValueTime.setHours(value.getHours());
			  		this._oldValueTime.setMinutes(value.getMinutes());
			  		this._oldValueTime.setSeconds(value.getSeconds());
		  		}
	  		}
  		}
	});