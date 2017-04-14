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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;

/**
 * �����-������ ������� csv 
 * @author ynikitin
 *
 */
public class ParseCardImportFile implements Action<List<List<ImportAttribute>>> {
	private static final long serialVersionUID = 1L;
	
	public final static String FILE_ATTRIBUTE_DELIMETER = ";";
	public final static String HEAD_MANDATORY_FLAG = "%";
	public final static String HEAD_DOUBLET_CHECK_FLAG = "!";
	public final static String HEAD_LINK_DELIMETER = "~";
	public final static String HEAD_REFERENCE_FLAG = "v";
	public final static String HEAD_TEMPLATE_FLAG = "t";
	public final static String HEAD_TEMPLATE_DELIMETER = ".";
	public final static String HEAD_TEMPLATE_DEST_ATTRIBUTE_DELIMETER= "->";
	public final static String HEAD_CUSTOM_ATTRIBUTE_CODE_FLAG="@";
		
	private ObjectId templateId;
	private InputStream file;
	/**
	 * Map ��� �������� ������������� ������ ��� ������� ��������� �������� ������� 
	 */
	private Map<ObjectId, Search> cardLinkAttributeSearchMap = new HashMap<ObjectId, Search>();

	private boolean getHead;
	
	public ObjectId getTemplateId() {
		return templateId;
	}

	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}

	public Class<?> getResultType() {
		return ArrayList.class;
	}

	public InputStream getFile() {
		return file;
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public Map<ObjectId, Search> getCardLinkAttributeSearchMap() {
		return cardLinkAttributeSearchMap;
	}

	public void setCardLinkAttributeSearchMap(
			Map<ObjectId, Search> cardLinkAttributeSearchMap) {
		this.cardLinkAttributeSearchMap = cardLinkAttributeSearchMap;
	}
	
	public void addCardLinkAttributeSearch(ObjectId key, Search value){
		this.cardLinkAttributeSearchMap.put(key, value);
	}

	public void setGetHead(boolean b) {
		this.getHead = b;
		
	}

	public boolean isGetHead() {
		return getHead;
	}

}
