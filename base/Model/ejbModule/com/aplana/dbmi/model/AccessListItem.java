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

/**
 * Model class used to specify access permissions for specific object
 * in database (target object). For example - access to card's material. <br>
 * Access to target object could be granted for all users, for users with specific roles,
 * for users from specific departments or for predefined set of employees.<br>
 * Full access permissions is defined as collection of AccessListItem instances. All
 * instances associated with same target object should have same {@link #getType() types}.<br>
 * Empty collection of AccessListItem means that target object could be accessed by any users.
 */
public class AccessListItem extends DataObject 
{
	/**
	 * default value of {@link #getType type property}. Should be changed to one of the following:
	 * {@link #TYPE_ROLE}, {@link #TYPE_DEPARTMENT}, {@link #TYPE_PERSON}  
	 */
	public static final short TYPE_NONE = 0;
	/**
	 * value of {@link #getType type property}, indicating that access to material of given card
	 * is restricted to users with spcific roles. See {@link #getRoleType()}
	 */
	public static final short TYPE_ROLE = 1;
	/**
	 * value of {@link #getType type property}, indicating that access to material of given card
	 * is restricted to users from specific departments. See {@link #getDepartment()}
	 */	
	public static final short TYPE_DEPARTMENT = 2;
	/**
	 * value of {@link #getType type property}, indicating that access to material of given card
	 * is restricted to specific users. See {@link #getPerson()}
	 */
	public static final short TYPE_PERSON = 3;
	
	private static final long serialVersionUID = 1L;
	private ObjectId object;
	private short type = TYPE_NONE;
	private Object variant;

	/**
	 * Sets identifier of object
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(AccessListItem.class, id));
	}

	/**
	 * Not used now.
	 * Returns a reference to the object, which access permissions are defined
	 * in this instance of the AccessListItem 
	 * @return object 
	 */
	public ObjectId getObject() {
		return object;
	}
	
	/**
	 * Not used now.
	 * Sets a reference to the object, which access permissions are defined
	 * in this instance of the AccessListItem. 
	 * @param object reference to {@link DataObject}
	 */
	public void setObject(ObjectId object) {
		this.object = object;
	}
	
	/**
	 * Returns type of access permission. 
	 * @return one of the following values: 
	 * {@link #TYPE_NONE}, {@link #TYPE_ROLE}, {@link #TYPE_DEPARTMENT}, {@link #TYPE_PERSON}
	 */	
	public short getType() {
		return type;
	}
	
	/**
	 * Gets identifier of role allowed to access target object
	 * Returns non-null values only for instances where {@link #getType()} = {@link #TYPE_ROLE}
	 * @return string identifier of role 
	 */
	public String getRoleType() {
		if (type != TYPE_ROLE)
			return null;
		return (String) variant;
	}

	/**
	 * Sets identifier of role
	 * Should be used only for instances where {@link #getType()} = {@link #TYPE_ROLE}.
	 * @param role desired identifier of role
	 * @throws IllegalArgumentException if specified role is not one of following {@link com.aplana.dbmi.model.Role#MANAGER_1}, {@link com.aplana.dbmi.model.Role#MANAGER_2}   
	 */	
	public void setRoleType(String role) {
		this.type = TYPE_ROLE;
		this.variant = role;
	}
	
	/**
	 * Gets department allowed to access target object 
	 * (record from {@link com.aplana.dbmi.model.Reference#ID_DEPARTMENT departments dictionary})
	 * Returns non-null values only for instances where {@link #getType()} = {@link #TYPE_DEPARTMENT} 
	 * @return {@link ReferenceValue} object representing record from departments dictionart
	 */
	public ReferenceValue getDepartment() {
		if (type != TYPE_DEPARTMENT)
			return null;
		return (ReferenceValue) variant;
	}
	
	/**
	 * Sets department allowed to access target object
	 * Should be used only for instances where {@link #getType()} = {@link #TYPE_DEPARTMENT} 
	 * @param value record from {@link com.aplana.dbmi.model.Reference#ID_DEPARTMENT departments dictionary}
	 */
	public void setDepartment(ReferenceValue value) {
		if (value == null || !Reference.ID_DEPARTMENT.equals(value.getReference()))
			throw new IllegalArgumentException("Not a department id");
		this.type = TYPE_DEPARTMENT;
		this.variant = value;
	}

	/**
	 * Gets person allowed to access target object
	 * Returns non-null values only for instances where {@link #getType()} = {@link #TYPE_PERSON} 
	 * @return person allowed to access target object
	 */
	public Person getPerson() {
		if (type != TYPE_PERSON)
			return null;
		return (Person) variant;
	}
	
	/**
	 * Gets person allowed to access target object
	 * Should be used only for instances where {@link #getType()} = {@link #TYPE_PERSON} 
	 * @param person person allowed to access target object
	 */
	public void setPerson(Person person) {
		if (person == null)
			throw new IllegalArgumentException("Person can't be null");
		this.type = TYPE_PERSON;
		this.variant = person;
	}
}
