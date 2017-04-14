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
package org.aplana.br4j.dynamicaccess.xmldef.types;

import org.aplana.br4j.dynamicaccess.xmldef.Permission;
/**
 * Wrapper around {@link Permission}. Used to store the isNew flag, used while editting Permissions.
 * @author atsvetkov
 *
 */
public class PermissionWrapper {

	/**
	 * Enum for rule types. It is introduced to simplify the handling of rule types.
	 * @author atsvetkov
	 *
	 */
	public enum RuleType {
		Role,
		Person,
		Profile,
		Delegation,
		//used for rules that don't have any defined rule type (NO_RULE)
		Undefined;
		
		public boolean isRoleType() {
			return this == Role;
		}

		public boolean isDelegationType() {
			return this == Delegation;
		}

		public boolean isPersonType() {
			return this == Person;
		}

		public boolean isProfileType() {
			return this == Profile;
		}

		public boolean isNoStaticType() {
			return this == Person || this == Profile || this == Delegation;
		}
		
		public boolean isUndefinedType() {
			return this == Undefined;
		}
	}
	
	private Permission permission;

	private String rule;

	private boolean isNew;

	public PermissionWrapper(Permission permission, boolean isNew) {
		this.permission = permission;
		this.isNew = isNew;
	}

	public Permission getPermission() {
		return permission;
	}
	
	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}			
}
