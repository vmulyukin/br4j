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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PortalUserLoginAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.UserRolesAndGroupsAttribute;
import com.aplana.dbmi.action.ImportAttribute;

public class ImportCards implements Action<ImportResult> {
	private static final long serialVersionUID = 1L;
	public static final ObjectId SUCCESS_CARD_SHOW_ATTRIBUTE = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId USER_LOGIN_CARD_ATTRIBUTE = ObjectId.predefined(PortalUserLoginAttribute.class, "jbr.portal.userlogin");
	public static final ObjectId USER_ROLES_CARD_ATTRIBUTE = ObjectId.predefined(UserRolesAndGroupsAttribute.class, "jbr.portal.userRoles");
	public static final String EXCLUDE_SEARCH_STATES = "34145, 106, 303990, 6";
	
	private ObjectId templateId;
	private String templateName;
	private List<ImportAttribute> importAttributes = new ArrayList<ImportAttribute>();
	private boolean checkDoublets = false;
	private boolean updateDoublets = false;
	private String customImportObjectName;
	private int lineNumber=0;

	public ObjectId getTemplateId() {
		return templateId;
	}

	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}


	public Class<?> getResultType() {
		return null;
	}

	public List<ImportAttribute> getImportAttributes() {
		return importAttributes;
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
		return checkDoublets;
	}

	public void setCheckDoublets(boolean checkDoublets) {
		this.checkDoublets = checkDoublets;
	}

	public boolean isUpdateDoublets() {
		return updateDoublets;
	}

	public void setUpdateDoublets(boolean updateDoublets) {
		this.updateDoublets = updateDoublets;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getCustomImportObjectName() {
		return customImportObjectName;
	}

	public void setCustomImportObjectName(String customImportObjectName) {
		this.customImportObjectName = customImportObjectName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return MessageFormat.format("ImportCards [templateId={0}, checkDoublets={1}, updateDoublets={2}, importAttributeCount={3}]", (templateId!=null?templateId.getId():null), checkDoublets, updateDoublets, importAttributes.size());
	}
	
}
