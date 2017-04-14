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
import java.util.ArrayList;
import java.util.List;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.action.ParseImportFile.TypeImportObject;
import com.aplana.dbmi.model.DataObject;

public abstract class ImportObjects implements Action {

	private static final long serialVersionUID = 1L;
	
	private List<ImportAttribute> importAttributes = new ArrayList<ImportAttribute>();
	private boolean checkDoublets = false;
	private boolean updateDoublets = false;
	private String customImportObjectName;
	private int lineNumber=0;

	public Class getResultType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public abstract Class<? extends DataObject> getClassImportObject();

	public abstract TypeImportObject getTypeImportObject();

	public List<ImportAttribute> getImportAttributes() {
		return this.importAttributes;
	}

	public void setImportAttributes(List<ImportAttribute> importAttributes) {
		this.importAttributes = importAttributes;
	}

	public void addAllImportAttributes(List<ImportAttribute> importAttributes) {
		this.importAttributes.addAll(importAttributes);
	}

	public void addImportAttribute(ImportAttribute importAttribute) {
		this.importAttributes.add(importAttribute);
	}

	public boolean isCheckDoublets() {
		return this.checkDoublets;
	}

	public void setCheckDoublets(boolean checkDoublets) {
		this.checkDoublets = checkDoublets;
	}

	public boolean isUpdateDoublets() {
		return this.updateDoublets;
	}

	public void setUpdateDoublets(boolean updateDoublets) {
		this.updateDoublets = updateDoublets;
	}

	public String getCustomImportObjectName() {
		return this.customImportObjectName;
	}

	public void setCustomImportObjectName(String customImportObjectName) {
		this.customImportObjectName = customImportObjectName;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public abstract String getBaseDoubletsSqlForObject();

	@Override
	public String toString() {
		return MessageFormat.format("ImportObjects [objectCode={0}, checkDoublets={1}, updateDoublets={2}, importAttributeCount={3}]", 
				(importAttributes!=null && importAttributes.get(0) != null?importAttributes.get(0).getValue():null), checkDoublets, updateDoublets, importAttributes.size());
	}
	
}
