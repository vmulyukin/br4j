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
package com.aplana.dbmi.actionhandler.descriptor;

import java.util.Map;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.card.ExtraJavascriptInfo;
import com.aplana.dbmi.card.extra.ExtraJavascriptBuilder;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.LocalizedString;

public class ActionHandlerDescriptor {

	private Class<? extends ActionHandler> handlerClass;

	private String id;
	private CardFilterCondition condition;
	private SelectionType selectionType;
	private Map<String, String> parameters;
	private LocalizedString title;
	private LocalizedString confirmation;
	private boolean forEditMode = true;
	private boolean forViewMode = false;
	private boolean needWritePermission = false;
	private String roleForMode;

    private ExtraJavascriptInfo extraJavascriptInfo;

	public CardFilterCondition getCondition() {
		return condition;
	}

	public void setCondition(CardFilterCondition condition) {
		this.condition = condition;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public SelectionType getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(SelectionType selectionType) {
		this.selectionType = selectionType;
	}

	public LocalizedString getTitle() {
		return title;
	}

	public void setTitle(LocalizedString title) {
		this.title = title;
	}

	public LocalizedString getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(LocalizedString confirmation) {
		this.confirmation = confirmation;
	}

	public boolean isNeedConfirmation() {
		return confirmation != null;
	}

	public Class<? extends ActionHandler> getHandlerClass() {
		return handlerClass;
	}

	public void setHandlerClass(Class<? extends ActionHandler> handlerClass) {
		this.handlerClass = handlerClass;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public boolean isForEditMode() {
		return forEditMode;
	}
	public void setForEditMode(boolean forEditMode) {
		this.forEditMode = forEditMode;
	}
	public boolean isForViewMode() {
		return forViewMode;
	}
	public void setForViewMode(boolean forViewMode) {
		this.forViewMode = forViewMode;
	}

    public ExtraJavascriptInfo getExtraJavascriptInfo() {
        return extraJavascriptInfo;
    }

    public void setExtraJavascriptInfo(ExtraJavascriptInfo extraJavascriptInfo) {
        this.extraJavascriptInfo = extraJavascriptInfo;
    }
    
    public boolean isNeedWritePermission() {
		return needWritePermission;
	}
    
    public void setNeedWritePermission(boolean needWritePermission) {
		this.needWritePermission = needWritePermission;
	}

	public String getRoleForMode() {
		return roleForMode;
	}

	public void setRoleForMode(String roleForMode) {
		this.roleForMode = roleForMode;
	}
}
