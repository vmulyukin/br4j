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

import java.util.List;

/**
 * Represent one record in SYSTEM_ROLE table.
 * Define name (russian and english) for each role available in system  
 * @author dsultanbekov
 * @see DataObject
 */
public class SystemRole extends LockableObject implements NamedObject{
	private static final long serialVersionUID = 1L;
	
	private String roleCode;
	private String nameRu;
	private String nameEn;
	private List<SystemGroup> roleGroups;

	public List<SystemGroup> getRoleGroups() {
		return roleGroups;
	}

	public void setRoleGroups(List<SystemGroup> roleGroups) {
		this.roleGroups = roleGroups;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	/**
	 * Returns russian name of role object
	 * @return russian name of role object
	 */
	public String getNameRu() {
		return nameRu;
	}
	/**
	 * Sets russian name of role object
	 * @param nameRus desired value
	 */
	public void setNameRu(String nameRus) {
		this.nameRu = nameRus;
	}
	/**
	 * Returns english name of role object
	 * @return english name of role object
	 */
	public String getNameEn() {
		return nameEn;
	}
	/**
	 * Sets english name of role object
	 * @param nameEng desired value
	 */
	public void setNameEn(String nameEng) {
		this.nameEn = nameEng;
	}
	/**
	 * Returns localized name of role object
	 * @return Returns value of {@link #nameRu} or {@link #nameEn} property depending of caller's locale preferences
	 */
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

    public void setName(LocalizedString name) {
		nameRu = name == null ? null : name.getValueRu();
		nameEn = name == null ? null : name.getValueEn();
	}

	public void setId(ObjectId id) {
		super.setId(id);
		if (id instanceof ObjectIdAndName)
			setName(((ObjectIdAndName) id).getName());
	}
}
