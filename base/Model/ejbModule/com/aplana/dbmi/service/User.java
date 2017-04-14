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
package com.aplana.dbmi.service;

import java.io.Serializable;

import com.aplana.dbmi.model.Person;

/**
 * Model class representing authenticated DBMI user.
 * Supplement information stored in DBMI database and accessible in form of
 * {@link Person} object via {@link #getPerson()} method with portal-specific
 * information about current user logon. 
 */
public class User implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private Person person;
	private byte[] userData;
	
	/**
	 * Get {@link Person} object representing this user in DBMI database
	 * @return {@link Person} object representing this user in DBMI database
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * Sets {@link Person} object representing this user in DBMI database
	 * @param person {@link Person} object representing this user in DBMI database
	 */
	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * Gets portal-specific portion of user data serialized into byte array.
	 * In particular this contains information about user's current logon.
	 * @return portal-specific portion of user data serialized into byte array
	 */
	public byte[] getUserData() {
		return userData;
	}

	/**
	 * Sets portal-specific portion of user data serialized into byte array. 
	 * @param userData portal-specific portion of user data serialized into byte array.
	 */
	public void setUserData(byte[] userData) {
		this.userData = userData;
	}
}
