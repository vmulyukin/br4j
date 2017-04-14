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
import java.util.Date;

/**
 * Class representing information about user of system.
 */
public class Person extends LockableObject
{
	/**
	 * Special person identifier representing current system user.
	 * Used in {@link com.aplana.dbmi.action.Search} action
	 */
	public static final ObjectId ID_CURRENT = new ObjectId(Person.class, -1);
	/**
	 * Special person identifier representing {@link com.aplana.dbmi.service.SystemUser}
	 */
	public static final ObjectId ID_SYSTEM = new ObjectId(Person.class, 0);
	
	private static final long serialVersionUID = 3L;
	private String login;
	private String fullName;
	private String email;
	private Date syncDate;
	private boolean active;
	private Collection roles;
	private Collection groups;
	private ObjectId cardId;

	public ObjectId getCardId() {
		return cardId;
	}

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * Sets person's identifier
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Person.class, id));
	}

	/**
	 * Gets person's login
	 * @return person's login
	 */
	public String getLogin() {
		return login;
	}
	
	/**
	 * Gets full name of person
	 * @return full name of person
	 */
	public String getFullName() {
		return fullName;
	}
	
	/**
	 * Gets person's email address
	 * @return person's email address
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Gets date of last synchronization with LDAP 
	 * @return date of last synchronization with LDAP
	 */
	public Date getSyncDate() {
		return syncDate;
	}
	
	/**
	 * Checks if this person is active.
	 * Inactive users couldn't login to system
	 * @return true if person is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets isActive flag on Person object
	 * Inactive users couldn't login to system
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets person's email
	 * @param email desired value of email address
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Sets person's full name
	 * @param fullName full name of person
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * Sets person's login. Should be unique across all persons
	 * @param login user's login
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * Sets date of last synchronization with LDAP
	 * @param syncDate
	 */
	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}

	/**
	 * Returns collection of {@link Role roles} assigned to this person
	 * @return collection of {@link Role roles} assigned to this person
	 */
	public Collection getRoles() {
		return roles;
	}

	/**
	 * Sets collection of {@link Role roles} assigned to this person
	 * @param roles collection of {@link Role roles}
	 */
	public void setRoles(Collection roles) {
		this.roles = roles;
	}

	public Collection getGroups() {
		return groups;
	}

	public void setGroups(Collection groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Person[");
		sb.append("id:").append(getId() != null ? getId().getId() : "null").append(", ");
		sb.append("fullName:").append(this.getFullName());
		sb.append("]");
		return sb.toString();
	}
}
