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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;

/**
 * ����� ��������� ������� ������� ��������� � ������� � � �������
 * ���������� ������ ObjectId ��������� 
 * @author ynikitin
 *
 */
public class GetAttributeCodeByName implements Action {
	public static long MANY_ATTRIBUTE_CODE_ERROR = 1;
	public static long MANY_ATTRIBUTE_CODE_GET_FIRST = 2;
	
	private ObjectId templateId;
	private List<String> attrNames;
	private boolean isRusLang = true;
	private boolean isCodes = true;
	
	private long manyAttributesType = MANY_ATTRIBUTE_CODE_ERROR; 

	public ObjectId getTemplateId() {
		return templateId;
	}

	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}

	public List<String> getAttrNames() {
		return attrNames;
	}

	public void setAttrNames(List<String> attrNames) {
		this.attrNames = attrNames;
	}

	public boolean isRusLang() {
		return isRusLang;
	}

	public void setRusLang(boolean isRusLang) {
		this.isRusLang = isRusLang;
	}

	public long getManyAttributesType() {
		return manyAttributesType;
	}

	public void setManyAttributesType(long manyAttributesType) {
		this.manyAttributesType = manyAttributesType;
	}

	public Class getResultType() {
		// TODO Auto-generated method stub
		return ArrayList.class;
	}
	
	public boolean isCodes() {
		return isCodes;
	}

	public void setCodes(boolean isCodes) {
		this.isCodes = isCodes;
	}

	public String toString(){
		return 	"GetAttributeCodeByName:\n" +
				"\t templateId = "+(templateId==null?null:templateId.getId())+"\n"+
				"\t attributes = "+attrNames.toString()+"\n"+
				"\t isRusLang = "+isRusLang+"\n"+
				"\t isCodes = "+isCodes+"\n"+
				"\t manyAttributesType = "+(manyAttributesType==1?"ERROR":"GET_FIRST")+"\n";
	}
}
