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
package com.aplana.dbmi.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author disanbirdin
 *
 */
public class WebTemplateRWAttributesBean extends WebTemplateBean {

	public final static String DEFAULT_MODE = "DEFAULT_MODE";
	public final static String CUSTOM_MODE = "CUSTOM_MODE";
	public final static String APPLY_TEMPLATE = "APPLY_TEMPLATE";
	public final static String CLOSE_TEMPLATE = "CLOSE_TEMPLATE";
	
	
	public final static String ALL_ROLES = "all_roles"; 

	private static final long serialVersionUID = 121L;
	/**
	 * default settings or custom
	 */
	private String mode;
	private String templateApplyClose;

	private Object cardStateId;
	private Object roleId;
	private List roles;
	private List cardStates;
	private Map templateRWAttributesReadOnly = new HashMap();
	private Map templateRWAttributesRequired = new HashMap();
	private Map templateRWAttributesHidden = new HashMap();

	private boolean rwAttributesInitialized;

	public void reset() {
		cardStateId = null;
		roleId = null;
		roles = null;
		cardStates = null;
		rwAttributesInitialized = false;
		mode = DEFAULT_MODE;
		templateApplyClose = null;
	}

	public void clearMaps() {
		templateRWAttributesReadOnly.clear();
		templateRWAttributesRequired.clear();
		templateRWAttributesHidden.clear();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("WebTemplateRWAttributesBean{");
		sb.append("Mode=").append(mode);
		if (getId() != null && getId().getId() != null) {
			sb.append("TemplateId=").append(getId().getId()).append(",");
		}
		sb.append("CardStateId=").append(cardStateId).append(",");
		sb.append("RoleId=").append(roleId);
		sb.append("}");

		return sb.toString();
	}

	public boolean isRwAttributesInitialized() {
		return rwAttributesInitialized;
	}

	public void setRwAttributesInitialized(boolean rwAttributesInitialized) {
		this.rwAttributesInitialized = rwAttributesInitialized;
	}

	public String getTemplateApplyClose() {
		return templateApplyClose;
	}

	public void setTemplateApplyClose(String templateApplyClose) {
		this.templateApplyClose = templateApplyClose;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isDefaultMode() {
		return DEFAULT_MODE.equals(getMode());
	}

	public Object getCardStateId() {
		return cardStateId;
	}

	public void setCardStateId(Object cardStateId) {
		this.cardStateId = cardStateId;
	}

	public Object getRoleId() {
		return roleId;
	}

	public void setRoleId(Object roleId) {
		this.roleId = roleId;
	}

	public List getRoles() {
		return roles;
	}

	public void setRoles(List roles) {
		this.roles = roles;
	}

	public List getCardStates() {
		return cardStates;
	}

	public void setCardStates(List cardStates) {
		this.cardStates = cardStates;
	}

	public Map getTemplateRWAttributesRequired() {
		return templateRWAttributesRequired;
	}

	public void setTemplateRWAttributesRequired(Map templateRWAttributesRequired) {
		this.templateRWAttributesRequired = templateRWAttributesRequired;
	}

	public Map getTemplateRWAttributesReadOnly() {
		return templateRWAttributesReadOnly;
	}

	public void setTemplateRWAttributesReadOnly(Map templateRWAttributesReadOnly) {
		this.templateRWAttributesReadOnly = templateRWAttributesReadOnly;
	}

	public Map getTemplateRWAttributesHidden() {
		return templateRWAttributesHidden;
	}

	public void setTemplateRWAttributesHidden(Map templateRWAttributesHidden) {
		this.templateRWAttributesHidden = templateRWAttributesHidden;
	}

}
