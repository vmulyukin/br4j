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

public class RoleAccessRule extends AccessRule {

	private static final long serialVersionUID = 1L;
	
	private SystemRole role;

	public SystemRole getRole() {
		return role;
	}

	public void setRole(SystemRole role) {
		//if (role != null && role.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved role");
		this.role = role;
	}
	
	public void setRole(ObjectId roleId) {
		if (roleId != null && !SystemRole.class.equals(roleId.getType()))
			throw new IllegalArgumentException("Not a role ID");
		this.role = roleId == null ? null : (SystemRole) SystemRole.createFromId(roleId);
	}
}
