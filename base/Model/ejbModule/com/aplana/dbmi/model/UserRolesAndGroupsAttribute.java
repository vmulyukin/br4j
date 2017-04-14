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
package com.aplana.dbmi.model;

import java.util.Map;
import java.util.Set;

/**
 * {@link Attribute} descendant, used to store portal user roles and groups.
 */
public class UserRolesAndGroupsAttribute extends Attribute implements PseudoAttribute {
	private static final long serialVersionUID = 4L;

	private Set<String> assignedGroups;
	private Set<String> assignedRoles;
	private Map<String, Set<String>> excludedGroupRoleCodes;  // key: group code, value: set of role codes
	private boolean initialized;
	
	public Set<String> getAssignedGroups() {
		return assignedGroups;
	}
	public Set<String> getAssignedRoles() {
		return assignedRoles;
	}
	public void setAssignedGroups(Set<String> assignedGroups) {
		this.assignedGroups = assignedGroups;
	}
	public void setAssignedRoles(Set<String> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}
	
	public Map<String, Set<String>> getExcludedGroupRoleCodes() {
		return excludedGroupRoleCodes;
	}
	public void setExcludedGroupRoleCodes(Map<String, Set<String>> excludedGroupRoleCodes) {
		this.excludedGroupRoleCodes = excludedGroupRoleCodes;
	}
	@Override
	public Object getType() {
		return TYPE_USER_ROLES_AND_GROUPS;
	}
	@Override
	public String getStringValue() {
		return "User and Roles Attribute is here !!!";	
	}
	@Override
	public boolean verifyValue() {
		return true;
	}
	@Override
	public boolean equalValue(Attribute attr) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setValueFromAttribute(Attribute attr) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isEmpty() {
		return (null == assignedGroups || assignedGroups.isEmpty()) &&
				(null == assignedRoles || assignedRoles.isEmpty());
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
}
