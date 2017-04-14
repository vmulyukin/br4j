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
import java.util.List;

import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.SortAttribute;

/**
 * Represents DTO for getting area cards
 *  
 * @author skashanski
 *
 */
public class GetCardAreaDTO implements Serializable {
	
	
	private static final long serialVersionUID = 4424992601239182938L;

	/**
	 * user identifier
	 */
	private int userId;
	
	/**
	 * user permissions 
	 */
	private long[] permissionTypes;
	
	/**
	 * page number 
	 */
	private int page;
	
	/**
	 * page size
	 */
	private int pageSize;
	
	/**
	 * collection of {@Attribute}
	 */
	private List<AttributeValue> attributes;
	
	/**
	 * collection of {@Attribute}
	 */
	private List<SortAttribute> sortAttributes;
	
	private String simpleSearchFilter;
	
	
	public GetCardAreaDTO() {
		super();

	}
	
	public GetCardAreaDTO(int userId, long[] permissionTypes, int page,
			int pageSize, List<AttributeValue> attributes) {
		this(userId, permissionTypes, page, pageSize, attributes, null, null);
	}

	public GetCardAreaDTO(int userId, long[] permissionTypes, int page,
			int pageSize, List<AttributeValue> attributes, 
			List<SortAttribute> sortAttributes, String simpleSearchFilter) {
		super();
		this.userId = userId;
		this.permissionTypes = permissionTypes;
		this.page = page;
		this.pageSize = pageSize;
		this.attributes = attributes;
		this.sortAttributes = sortAttributes;
		this.simpleSearchFilter = simpleSearchFilter;
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

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<AttributeValue> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeValue> attributes) {
		this.attributes = attributes;
	}

	public List<SortAttribute> getSortAttributes() {
		return sortAttributes;
	}

	public void setSortAttributes(List<SortAttribute> sortAttributes) {
		this.sortAttributes = sortAttributes;
	}

	public String getSimpleSearchFilter() {
		return simpleSearchFilter;
	}

	public void setSimpleSearchFilter(String simpleSearchFilter) {
		this.simpleSearchFilter = simpleSearchFilter;
	}

}
