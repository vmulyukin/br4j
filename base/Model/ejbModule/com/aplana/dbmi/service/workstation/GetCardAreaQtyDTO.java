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
package com.aplana.dbmi.service.workstation;

import java.io.Serializable;

/**
 * Represents DTO for getting area card's quantities
 *  
 * @author skashanski
 *
 */
public class GetCardAreaQtyDTO implements Serializable  {
	
	private static final long serialVersionUID = 8940979568205098067L;

	/**
	 * user identifier
	 */
	private int userId;
	
	/**
	 * user permissions 
	 */
	private long[] permissionTypes;
	private String simpleSearchFilter;	
	
	public GetCardAreaQtyDTO(int userId, long[] permissionTypes, String simpleSearchFilter) {
		super();
		this.userId = userId;
		this.permissionTypes = permissionTypes;
		this.simpleSearchFilter = simpleSearchFilter;
	}

	public GetCardAreaQtyDTO() {
		super();
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public long[] getPermissionTypes() {
		return permissionTypes;
	}

	public void setPermissionTypes(long[] permissionTypes) {
		this.permissionTypes = permissionTypes;
	}

	public String getSimpleSearchFilter() {
		return simpleSearchFilter;
	}

	public void setSimpleSearchFilter(String simpleSearchFilter) {
		this.simpleSearchFilter = simpleSearchFilter;
	}
	
}
