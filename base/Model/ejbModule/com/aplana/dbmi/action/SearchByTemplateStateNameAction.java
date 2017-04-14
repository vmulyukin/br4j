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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import com.aplana.dbmi.model.ObjectId;
import antlr.collections.List;

public class SearchByTemplateStateNameAction implements Action {

	private static final long serialVersionUID = 2888326834496049823L;
	
	private long[] permissionTypes = null;
	private int page = 1;
	private int pageSize = 50;
	private String name = null;
	private HashMap<ObjectId, ObjectId> additionalFilter;
	
	protected Collection templates = new ArrayList();
	protected Collection states = new ArrayList();

	public Class getResultType() {
		return List.class;
	}
	
	public Collection getTemplates() {
		return templates;
	}

	public void setTemplates(Collection templates) {
		this.templates = templates;
	}
	
	public Collection getStates() {
		return states;
	}

	public void setStates(Collection states) {
		this.states = states;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public HashMap<ObjectId, ObjectId> getAdditionalFilter() {
		return additionalFilter;
	}

	public void setAdditionalFilter(HashMap<ObjectId, ObjectId> additionalFilter) {
		this.additionalFilter = additionalFilter;
	}
}
