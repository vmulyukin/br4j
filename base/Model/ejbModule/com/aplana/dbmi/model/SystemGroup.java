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

import java.util.Collection;

/**
 * Represent one record in SYSTEM_GROUP table.
 * Define name (russian and english) for each group available in system  
 * @author vialeksandrov
 * @see DataObject
 */
public class SystemGroup extends LockableObject implements NamedObject{
	private static final long serialVersionUID = 1L;
	
	private String groupCode;
	private String nameRu;
	private String nameEn;
	private Collection<SystemRole> systemRoles;

	public String getGroupCode() {
		return groupCode;
	}
	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}
	
	public Collection<SystemRole> getSystemRoles() {
		return systemRoles;
	}
	public void setSystemRoles(Collection<SystemRole> systemRoles) {
		this.systemRoles = systemRoles;
	}
	/**
	 * Returns russian name of group object
	 * @return russian name of group object
	 */
	public String getNameRu() {
		return nameRu;
	}
	/**
	 * Sets russian name of group object
	 * @param nameRus desired value
	 */
	public void setNameRu(String nameRus) {
		this.nameRu = nameRus;
	}
	/**
	 * Returns english name of group object
	 * @return english name of group object
	 */
	public String getNameEn() {
		return nameEn;
	}
	/**
	 * Sets english name of group object
	 * @param nameEng desired value
	 */
	public void setNameEn(String nameEng) {
		this.nameEn = nameEng;
	}
	/**
	 * Returns localized name of group object
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
