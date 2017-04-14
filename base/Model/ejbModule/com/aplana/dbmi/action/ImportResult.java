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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.model.ObjectId;

/**
 * ��������� ������ ������� ���������� �������
 * @author ynikitin
 *
 */
public class ImportResult {
	// ������ ��������� �������������� �������
	private List<ImportAttribute> importAttributes;
	
	// ��� ���������� 
	private IMPORT_RESULT_TYPE resultType;
	
	// ��������� � ����������� �������
	private String resultMessage;
	
	// id �������������� �������
	private Set<ObjectId> importObjectIds = new HashSet<ObjectId>();

	public List<ImportAttribute> getImportAttributes() {
		return importAttributes;
	}

	public void setImportAttributes(List<ImportAttribute> importAttributes) {
		this.importAttributes = importAttributes;
	}

	public IMPORT_RESULT_TYPE getResultType() {
		return resultType;
	}

	public void setResultType(IMPORT_RESULT_TYPE resultType) {
		this.resultType = resultType;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public Set<ObjectId> getImportObjectIds() {
		return importObjectIds;
	}

	public void setImportObjectIds(Set<ObjectId> importObjectIds) {
		this.importObjectIds = importObjectIds;
	}

	public void addImportObjectId(ObjectId importObjectId) {
		this.importObjectIds.add(importObjectId);
	}

	public void addAllImportObjectIds(Collection<ObjectId> importObjectIds) {
		this.importObjectIds.addAll(importObjectIds);
	}
	/*
	 * ������������ ��������� ����������� ������� ���������� �������
	 * SUCCESS - ������ ���������� ������� 
	 * DOUBLET - ������ �������� (�������� ��������)
	 * ������ ��������� ���� �� �������������
	 */
	public static enum IMPORT_RESULT_TYPE{SUCCESS, DOUBLET}; 
	
	@Override
	public String toString() {
		return MessageFormat.format(
				"ImportResult: \n" +
				"\t resultType = {0}\n" +
				"\t resultMessage = {1}\n",
				new Object[]{
						resultType.name(), 
						resultMessage});
	}
}