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

import java.util.Set;

/**
 * Each {@link Person user} in system could have one or several {@link SystemGroup system groups}.
 * Group defines set of {@link SystemRoles roles} assigned to user.<br>
 * Each Group object represents one record from SYSTEM_GROUP table.
 */
public class Group extends DataObject
{
	private static final long serialVersionUID = 1L;
	private ObjectId person;
	private SystemGroup systemGroup;
	private Set<String> excludedRoles; // ������ ����� � ������� ������ ������, ����������� � ������������
 	
	public Set<String> getExcludedRoles() {
		return excludedRoles;
	}

	public void setExcludedRoles(Set<String> excludedRoles) {
		this.excludedRoles = excludedRoles;
	}

	/**
	 * Gets {@link SystemGroup} object
	 * @return {@link SystemGroup} object
	 */
	public SystemGroup getSystemGroup() {
		return systemGroup;
	}

	/**
	 * Sets {@link SystemGroup} object
	 * @param role {@link SystemGroup} object to associate with this role object
	 */
	public void setSystemGroup(SystemGroup group) {
		this.systemGroup = group;
	}

	/**
	 * Sets numeric identifier of role object
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Group.class, id));
	}
	
	/**
	 * Use {@link #getSystemRole()}.getId().getId()} instead
	 * @return String identifier of {@link SystemRole} associated with this role object 
	 */ 
	public String getType() {
		return systemGroup == null ? null : (String)systemGroup.getId().getId();
	}

	/**
	 * Gets english name of {@link systemGroup} associated with this group object
	 * @return  english name of {@link systemGroup} associated with this group object
	 */
	public String getNameEn() {
		return systemGroup == null ? null : systemGroup.getNameEn();
	}

	/**
	 * Gets russian name of {@link systemGroup} associated with this group object
	 * @return russian name of {@link SystemGroup} associated with this role object
	 */	
	public String getNameRu() {
		return systemGroup == null ? null : systemGroup.getNameRu();
	}

	/**
	 * Gets localized name of {@link SystemGroup} associated with this group object
	 * @return value of {@link #systemGroup()}.getName()
	 */
	public String getName() {
		return systemGroup == null ? null : systemGroup.getName();
	}

	/**
	 * Gets identifier of {@link Person} associated with this group object
	 * @return identifier of {@link Person} associated with this group object
	 */
	public ObjectId getPerson() {
		return person;
	}

	/**
	 * Sets identifier of {@link Person} associated with this group object 
	 * @param person should be identifier of {@link Person} type
	 */
	public void setPerson(ObjectId person) {
		this.person = person;
	}
}
