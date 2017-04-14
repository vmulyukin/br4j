/**
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
package com.aplana.dbmi.action;

import java.text.MessageFormat;

import com.aplana.dbmi.model.ObjectId;


/**
 * �������� �������� �������������� ������� , 
 * ������������ ��� �������� ������������ ���������� �� ��������� ������������� ��������
 * @author ynikitin
 *
 */
public class ImportAttribute implements Cloneable{
	// ��� �������� � ������������� �������
	private ObjectId primaryCodeId;
	
	// ��������� ��� �������� � ������������� �������, �������� LOGIN - ������� ������ �� ����� ������������, �.�. ���� ������ ���� id ��������, ���� id ����� ������� �� � ������ 
	private CUSTOM_ATTRIBUTE_CODES customPrimaryCodeId;
	
	// ������� �������������� ��������
	private boolean isMandatory;
	
	// ������� �������������� ��������
	private boolean isDoubletCheck;

	// ��������� �������� ��������
	private String value;
	
	// ������� ������� � ��������-������ �� �������� primaryCodeId 
	private ObjectId linkCodeId;
	
	// id ������� ��������� ��������, � ������� ���� ������ ������� �������
	private ObjectId linkTemplateId;
	
	// ������� ����, ��� primaryCodeId ��������� �� ����������
	private boolean isReference;

	// ������� ����, ��� linkCodeId ��������� �� ��������� �������, �������� LOGIN, �.�. ���� ������ ���� id ��������, ���� id ����� ������� �� � ������ 
	private CUSTOM_ATTRIBUTE_CODES customLinkCodeId;

	// �������� �����������
	private String referenceName;
	
	// �������� �������� � ������������� �������� (��������, ������������ ������� �� ����� ���� ������) - ���������� ��� �������� ������� ����� �������� � ������� ������ ���� ������� �� ���
	private String troubleMessage;
	
	/*
	 * ������������ ��������� ��������� ����� ���������, �.�. ���, ������� �� �������� ��� �������� ������ ��������� ������ � �������, 
	 * �� ������� ����� � ��������� � ��������� ��������:
	 * LOGIN - ����� ������������ (person_login �� ������� person), ������������ ��� ������ �������������, � ����� ��� �������� ������ 
	 * ROLE_CODE - ��� ��������� ���� (role_code �� ������� system_role), ������������ ��� ���������� ����� � ������ �������������, ��� ������ � �������� ����� ����
	 * ROLE_NAME_RUS - ������� �������� ��������� ���� (role_name_rus �� ������� system_role), ������������ ��� ������ ����� � ���������� �� � ������������, ��� ������ � �������� ����� ����   
	 * ROLE_NAME_ENG - ���������� �������� ��������� ���� (role_name_eng �� ������� system_role), ��� ������ � �������� ����� ����
	 * GROUP_CODE - ��� ��������� ������ ����� (group_code �� ������� system_group), ������������ ��� ������ � �������� ���� � ������, ��� ������ � �������� ����� ������
	 * GROUP_NAME_RUS - ������� �������� ��������� ������ ����� (group_name_rus �� ������� system_group), ������������ ��� ������ � ���������� ������ ����� � ������������, ��� ������ � �������� ����� ������
	 * GROUP_NAME_ENG - ���������� �������� ��������� ������ ����� (group_name_eng �� ������� system_group), ������������ ��� ������ � �������� ����� ������
	 *
	 * � ���������� ������������ �������� (�������� �������, �������� �������)
	 */
	public static enum CUSTOM_ATTRIBUTE_CODES{
		LOGIN("person", "person_login"),
		ROLE_CODE("system_role", "role_code"),
		ROLE_NAME_RUS("system_role", "role_name_rus"),
		ROLE_NAME_ENG("system_role", "role_name_eng"),
		GROUP_CODE("system_group", "group_code"),
		GROUP_NAME_RUS("system_group", "group_name_rus"),
		GROUP_NAME_ENG("system_group", "group_name_eng");
		
		private String tableName;
		private String columnName;
		
		CUSTOM_ATTRIBUTE_CODES(String tableName, String columnName) {
			this.tableName = tableName;
			this.columnName = columnName;
		}
		
		public String getTableName() {
			return tableName;
		}
		public String getColumnName() {
			return columnName;
		}
		
	}; 
	
	public ObjectId getPrimaryCodeId() {
		return primaryCodeId;
	}

	public void setPrimaryCodeId(ObjectId primaryCodeId) {
		this.primaryCodeId = primaryCodeId;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ObjectId getLinkCodeId() {
		return linkCodeId;
	}

	public void setLinkCodeId(ObjectId linkCodeId) {
		this.linkCodeId = linkCodeId;
		this.isReference = false;
	}

	public ObjectId getLinkTemplateId() {
		return linkTemplateId;
	}

	public void setLinkTemplateId(ObjectId linkTemplateId) {
		this.linkTemplateId = linkTemplateId;
	}

	public boolean isReference() {
		return isReference;
	}

	public void setReference(boolean isReference) {
		this.isReference = isReference;
	}
	

	public String getReferenceName() {
		return referenceName;
	}

	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}

	@Override
	public ImportAttribute clone() throws CloneNotSupportedException {
		ImportAttribute sel = (ImportAttribute) super.clone();
		sel.setValue(null);
		return sel;
	}

	public CUSTOM_ATTRIBUTE_CODES getCustomPrimaryCodeId() {
		return customPrimaryCodeId;
	}

	public void setCustomPrimaryCodeId(CUSTOM_ATTRIBUTE_CODES customPrimaryCodeId) {
		this.customPrimaryCodeId = customPrimaryCodeId;
	}

	public CUSTOM_ATTRIBUTE_CODES getCustomLinkCodeId() {
		return customLinkCodeId;
	}

	public void setCustomLinkCodeId(CUSTOM_ATTRIBUTE_CODES customLinkCodeId) {
		this.customLinkCodeId = customLinkCodeId;
	}

	public boolean isDoubletCheck() {
		return isDoubletCheck;
	}

	public void setDoubletCheck(boolean isDoubletCheck) {
		this.isDoubletCheck = isDoubletCheck;
	}

	public String getTroubleMessage() {
		return troubleMessage;
	}

	public void setTroubleMessage(String troubleMessage) {
		this.troubleMessage = troubleMessage;
	}
	
	public boolean isTrouble(){
		return (troubleMessage!=null&&!troubleMessage.isEmpty());
	}

	@Override
	public String toString() {
		return MessageFormat.format(
				"ImportCardAttribute: \n" +
				"\t primaryCodeId = {0}\n" +
				"\t isMandatory = {1}\n" +
				"\t isDoubletCheck = {2}\n" +
				"\t value = {3}\n" +
				"\t linkCodeId = {4}\n" +
				"\t linkTemplateId = {5}\n" +
				"\t isReference = {6}\n"+ 
				"\t referenceName = {7}\n"+ 
				"\t customPrimaryCodeId = {8}\n"+ 
				"\t customLinkCodeId = {9}\n"+
				"\t isTrouble = {10}\n",
				new Object[]{
						(primaryCodeId==null?null:primaryCodeId.getId()), 
						isMandatory,
						isDoubletCheck,
						value, 
						(linkCodeId==null?null:linkCodeId.getId()), 
						(linkTemplateId==null?null:linkTemplateId.getId()), 
						isReference,
						referenceName,
						customPrimaryCodeId,
						customLinkCodeId,
						isTrouble()});
	}
}
