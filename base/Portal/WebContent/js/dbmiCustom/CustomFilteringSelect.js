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
dojo.provide("dbmiCustom.CustomFilteringSelect");
dojo.require("dijit.form.ComboBox");
dojo.declare(
"dbmiCustom.CustomFilteringSelect",
[dijit.form.FilteringSelect],
{
	onChangeAllowed: true,//���� ������� ������������ ������� onChange
	_selectOption:function(evt){// ����������� ��� ��������� �������� �� ������� �� enter ���� ������ ������
		if(evt){
			this._announceOption(evt.target);
		}
		this._hideResultList();
		this._setCaretPos(this.focusNode,this.focusNode.value.length);
		dijit.form._FormValueWidget.prototype._setValueAttr.call(this,this.value,true);
		this.onChangeAllowed = false;//��������� ����� ������ onChange
		this.onValueChanged();
	}, onChange: function() {//����������� ����� _selectOption, ���� ���������� �� _selectOption � ������ ��������� �������� �������� tab ��� ������ ������
		if(this.onChangeAllowed) //(**1**)
			this.onValueChanged();
		this.onChangeAllowed = true;
	}, onValueChanged: function() {}//������� ��������� ��������, �������������� �� ����� ������������� �������
});

/*
 * ������ ������ ����������� �� FilteringSelect
 * 
 * �������� ��� �������� ��������� ������������ ������������� FilteringSelect ��� ��������� ��������,
 * ���� ������� ������� ���������� ������� (��� BR4J00035716) (�� ���������� onChange)
 * 
 * onChangeAllowed - ���� ������� ������������ ������� onChange ����� ��� ����, ����� ��������� ������������ onChange,
 * ���� �������� _selectOption
 * 
 * �������� ��� �������� ��������� ��������:
 * 
 * 1) �� ������� �� enter ���� ������ - ���������� ���������� ������� _selectOption, ����� onChange
 * 		� ������ ������ onChangeAllowed - �� ���� ������� ������� onValueChanged (**1**),
 * 		����� ��� ���� �� ������� 2 ����: � _selectOption � � onChange
 * 
 * 2) �������� tab ���� ������ ������ - ���������� ������ onChange
 * 
 */