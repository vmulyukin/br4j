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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.RenderRequest;

import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;

public class WebTemplateAccessBean {
	public static class AccessItem {
		private PersonAttribute personAttribute;
		private SystemRole role;
		public PersonAttribute getPersonAttribute() {
			return personAttribute;
		}
		public void setPersonAttribute(PersonAttribute personAttribute) {
			this.personAttribute = personAttribute;
		}
		public SystemRole getRole() {
			return role;
		}
		public void setRole(SystemRole role) {
			this.role = role;
		}
	}
	
	public static class Permissions {
		private Long type;
		private DataObject dataObject; 
		private List accessItems;
		private LocalizedString[] names;
		private boolean allowForAll;
		
		public boolean isAllowForAll() {
			return allowForAll;
		}
		public void setAllowForAll(boolean isAllowForAll) {
			this.allowForAll = isAllowForAll;
		}
		public LocalizedString[] getNames() {
			return names;
		}
		public void setNames(LocalizedString[] names) {
			this.names = names;
		}
		public ObjectId getObjectId() {
			return dataObject == null ? null : dataObject.getId();
		}
		public DataObject getDataObject() {
			return dataObject;
		}
		public void setDataObject(DataObject dataObject) {
			this.dataObject = dataObject;
		}
		public List getAccessItems() {
			return accessItems;
		}
		public void setAccessItems(List accessItems) {
			this.accessItems = accessItems;
		}		
		public Long getType() {
			return type;
		}
		public void setType(Long type) {
			this.type = type;
		}
		public String getPartitionCaptionKey() {
			if (CardAccess.READ_CARD.equals(type)) {
				return "partition.EditCardReadPermissions";
			} else if (CardAccess.EDIT_CARD.equals(type)) {
				return "partition.EditCardEditPermissions";
			} else if (CardAccess.WORKFLOW_MOVE.equals(type)) {
				return "partition.EditWorkflowMovePermissions";
			} else if (CardAccess.CREATE_CARD.equals(type)) {
				return "partition.EditCardCreatePermissions";
			} else {
				throw new IllegalStateException("Unknown permission type: " + type);
			}
		}
		public boolean isCardCreate() {
			return CardAccess.CREATE_CARD.equals(type);
		}
	}
	
	private Map sortParameters = new HashMap();	
	private boolean success;
	private Template template;
	private Collection personAttributes;
	private Collection systemRoles;
		
	private String message;
	private List workflowMovePermissions;
	private List cardReadPermissions;
	private List cardEditPermissions;
	private Permissions cardCreatePermissions;
	private Permissions selectedPermissions;
	
	private String attrCode;
	private String roleCode;

	private CardState initialState;

	public CardState getInitialState() {
		return initialState;
	}

	public void setInitialState(CardState initialState) {
		this.initialState = initialState;
	}

	public Permissions getSelectedPermissions() {
		return selectedPermissions;
	}

	public void setSelectedPermissions(Permissions selectedPermissions) {
		this.selectedPermissions = selectedPermissions;
	}

	public WebTemplateAccessBean() {
		workflowMovePermissions = new ArrayList();
		cardReadPermissions = new ArrayList();
		cardEditPermissions = new ArrayList();
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public Collection getPersonAttributes() {
		return personAttributes;
	}

	public void setPersonAttributes(Collection personAttributes) {
		this.personAttributes = personAttributes;
	}

	public Collection getSystemRoles() {
		return systemRoles;
	}

	public void setSystemRoles(Collection systemRoles) {
		this.systemRoles = systemRoles;
	}

	public List getWorkflowMovePermissions() {
		return workflowMovePermissions;
	}

	public void setWorkflowMovePermissions(List workflowMovePermissions) {
		this.workflowMovePermissions = workflowMovePermissions;
	}

	public List getCardReadPermissions() {
		return cardReadPermissions;
	}

	public void setCardReadPermissions(List cardReadPermissions) {
		this.cardReadPermissions = cardReadPermissions;
	}

	public List getCardEditPermissions() {
		return cardEditPermissions;
	}

	public void setCardEditPermissions(List cardEditPermissions) {
		this.cardEditPermissions = cardEditPermissions;
	}

	public Permissions getCardCreatePermissions() {
		return cardCreatePermissions;
	}

	public void setCardCreatePermissions(Permissions cardCreatePermissions) {
		this.cardCreatePermissions = cardCreatePermissions;
	}
	
	public Permissions getPermissions(Long type, Long objectId) {
		if (CardAccess.CREATE_CARD.equals(type)) {
			return cardCreatePermissions;		
		} else {
			List list = null;
			if (CardAccess.EDIT_CARD.equals(type)) {
				list = cardEditPermissions;
			} else if (CardAccess.READ_CARD.equals(type)) {
				list = cardReadPermissions;
			} else if (CardAccess.WORKFLOW_MOVE.equals(type)) {
				list = workflowMovePermissions;
			} else {
				return null;
			}
			Iterator i = list.iterator();
			while (i.hasNext()) {
				Permissions permissions = (Permissions)i.next();
				if (objectId.equals(permissions.getObjectId().getId())) {
					return permissions;
				}
			}
			return null;
		}
	}

	public List getAvailableSystemRoles() {
		if (selectedPermissions == null) {
			return null;
		} else if (!CardAccess.CREATE_CARD.equals(selectedPermissions.getType())) {
			return new ArrayList(systemRoles);
		}
		List result = new ArrayList();
		Iterator i = systemRoles.iterator();
		while (i.hasNext()) {
			SystemRole role = (SystemRole)i.next();
			ObjectId roleId = role.getId();
			Iterator j = selectedPermissions.accessItems.iterator();
			boolean found = false;
			while (j.hasNext()) {
				AccessItem accessItem = (AccessItem)j.next();
				if (accessItem.getRole() != null && roleId.equals(accessItem.getRole().getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				result.add(role);
			}
		}
		return result;
	}

	public String getAttrCode() {
		return attrCode;
	}

	public void setAttrCode(String attrCode) {
		this.attrCode = attrCode;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	
	public PersonAttribute getPersonAttribute(String attrCode) {
		if (attrCode == null || "".equals(attrCode)) {
			return null;
		}
		Iterator i = personAttributes.iterator();
		while (i.hasNext()) {
			PersonAttribute attr = (PersonAttribute)i.next();
			if (attrCode.equals(attr.getId().getId())) {
				return attr;
			}
		}
		throw new IllegalArgumentException("Wrong attrCode: " + attrCode);
	}
	
	public SystemRole getSystemRole(String roleCode) {
		if (roleCode == null || "".equals(roleCode)) {
			return null;
		}
		Iterator i = systemRoles.iterator();
		while (i.hasNext()) {
			SystemRole role = (SystemRole)i.next();
			if (roleCode.equals(role.getId().getId())) {
				return role;
			}
		}
		throw new IllegalArgumentException("Wrong roleCode: " + roleCode);		
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public Map getSortParameters() {
		return sortParameters;
	}

	public void memorizeSort(RenderRequest request) {
		Enumeration e = request.getParameterNames();
		while (e.hasMoreElements()) {
			String paramName = (String)e.nextElement();
			if (paramName.matches("^d-[0-9]+-(s|o)$")) {
				sortParameters.put(paramName, request.getParameter(paramName));
			}
		}
	}
}
