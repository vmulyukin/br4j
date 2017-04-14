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
package com.aplana.dbmi.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.User;

/**
 * Class representing information about user logon.
 * Instance of this type serialized to byte array is stored in {@link User#setUserData(byte[]) userData}
 * property of {@link User} object representing user logged in to the DBMI system. 
 */
public class UserData implements Serializable
{
	private static final long serialVersionUID = -656530290172419153L;
	private String address;
	private Person person;
    private ObjectId[] bosses;
	
	//private UserData() { }

	/**
	 * Gets string representation of users address
	 * @return string representation of users address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets string representation of users address
	 * @param address string representation of users address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Gets {@link Person} object representing user
	 * @return {@link Person} object representing user
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Sets {@link Person} object representing user
	 * @param person {@link Person} object representing user
	 */
	public void setPerson(Person person) {
		this.person = person;
	}
	
	/**
	 * Deserializes {@link UserData} instance stored in {@link User#getUserData() userData}
	 * property of given {@link User} object.
	 * @param user {@link User} object to read information from
	 * @return deserialized UserData instance
	 */
	public static UserData read(User user)
	{
		ByteArrayInputStream in = new ByteArrayInputStream(user.getUserData());
		try {
			return (UserData) new ObjectInputStream(in).readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Creates new {@link User} object by information
	 * presented in this {@link UserData object} and initializes it.
	 * @return newly created {@link User} object
	 */
	public User write()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(out).writeObject(this);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		User user = new User();
		user.setUserData(out.toByteArray());
		user.setPerson(person);
		return user;
	}

    public ObjectId[] getBosses() {
        return bosses;
    }

    public void setBosses(ObjectId[] bosses) {
        this.bosses = bosses;
    }

    public void setBosses(Collection bosses) {
        ObjectId[] ids = new ObjectId[bosses.size()];
        int i = 0;
        for (Iterator itr = bosses.iterator(); itr.hasNext(); i++) {
            ids[i] = (ObjectId) itr.next();
        }
        setBosses(ids);
    }

	/**
	 * Returns name and address of user logged into the system.
	 */
	public String toString() {
		if (person == null)
			return "Nobody";
		return person.getFullName() + "/" + address;
	}
}
