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

import com.aplana.dbmi.model.util.ObjectIdUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link Attribute} descendant used to store links for {@link Person} objects
 * NOTE: Person objects stored in values collection could be empty objects 
 * with only identifier information initialized. If you need to work with Person object
 * referenced in this Attribute, you should fetch full Person object from DB
 * by performing additional query
 * <br>
 * This attribute could be single- or multi-valued.
 *  
 */
public class PersonAttribute extends Attribute {
	private static final long serialVersionUID = 6L;
	//private ObjectId value;
	//private Person person;
	private Collection<Person> values;

	private boolean multiValued = true;
	
	/**
	 * Gets collection of referenced {@link Person} objects
	 * @return value of attribute
	 */
	public Collection<Person> getValues() {
		return values;
	}

	/**
	 * Sets value of attribute
	 * @param values desired attribute value
	 */
	public void setValues(Collection<Person> values) {
		this.values = values;
	}

	/**
	 * This method should be used for single-valued attributes
	 * Returns identifier of {@link Person} object assigned as attribute value
	 * or null if attribute don't have value.
	 * @throws IllegalStateException if attribute have more than one values.
	 * @deprecated
	 */
	public ObjectId getValue() {
		if (values == null || values.size() == 0)
			return null;
		if (values.size() > 1)
			throw new IllegalStateException("Getting single value from multivalued attribute");
		return values.iterator().next().getId();
		//return person == null ? null : person.getId();
	}

	/**
	 * Sets empty {@link Person} object with given identifier as value of attribute
	 * Note that resulting {@link Person} object will have only identifier field 
	 * filled and all other properties will be empty 
	 * @param value ObjectId of Person object to be set as attribute value 
	 * @deprecated 
	 * @throws IllegalArgumentException if value is not identifier of {@link Person} object 
	 */
	public void setValue(ObjectId value) {
		values = new ArrayList<Person>();
		if (value == null) {
			return;
		}
		if (!Person.class.equals(value.getType()))
			throw new IllegalArgumentException("Not a person's id");
		values.add(DataObject.<Person>createFromId(value));
	}

	/**
	 * Returns {@link Person} object assigned as attribute value
	 * or null if attribute don't have value.
	 * This method should be used for single-valued attributes only.
	 * @return {@link Person} object assigned as attribute value 
	 * @throws IllegalStateException if attribute have more than one values. 
	 */
	public Person getPerson() {
		if (values == null || values.size() == 0)
			return null;
		if (values.size() > 1)
			throw new IllegalStateException("Getting single value from multivalued attribute");
		return values.iterator().next();
	}

	/**
	 * Returns full name of {@link Person} object assigned as attribute value
	 * or empty string if attribute don't have value or if person's object fullname property is null.
	 * This method should be used for single-valued attributes only.
	 * @return name of person or empty string 
	 * @throws IllegalStateException if attribute have more than one values. 
	 */
	public String getPersonName() {
		Person person = getPerson();
		if (person == null || person.getFullName() == null)
			return "";
		return person.getFullName();
	}

	/**
	 * Returns email address of {@link Person} object assigned as attribute value
	 * or empty string if attribute don't have value or if person's object email property is null.
	 * This method should be used for single-valued attributes only.
	 * @return name of person or empty string 
	 * @throws IllegalStateException if attribute have more than one values. 
	 */
	public String getEmail() {
		Person person = getPerson();
		if (person == null || person.getEmail() == null)
			return "";
		return person.getEmail();
	}

	/**
	 * Sets value of attribute to single {@link Person} object
	 * @param person desired value of attribute. If null then this method will have no effect.
	 */
	public void setPerson(Person person) {
		values = new ArrayList<Person>();
		if (person != null)
			values.add(person);
	}
	
	/** 
	 * @see #setValue(ObjectId)
	 */
	@Deprecated
	public void setPerson(ObjectId id) {
		this.setValue(id);
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		if (values == null || values.isEmpty())
			return "";
		final StringBuilder buf = new StringBuilder();
		for( Iterator<Person> itr = values.iterator(); itr.hasNext(); )
		{
			final Person item = itr.next();
			String info;
			if (item == null) { 
				info = "null";
			} else {
				info = item.getFullName(); // (!) ��� ���������� ������
				if (info == null || info.trim().length() == 0)
					// ���� ����� - ���������� id...
					info = (item.getId() != null) 
							? MessageFormat.format( "id({0})", item.getId().getId())
							: "null";
			}
			buf.append(info);
			if (itr.hasNext())
				buf.append(", ");
		}
		return buf.toString();
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_PERSON;
	}

	/**
	 * Checks if this attribute contains same set of {@link Person persons}
	 * as given attribute attr. Person objects are considered equal
	 * if they have equals ids
	 * @throws IllegalArgumentException if attr is not a {@link PersonAttribute} instance
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof PersonAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		
		PersonAttribute pa = (PersonAttribute)attr;
		return ObjectIdUtils.isSameDataObjects(values, pa.getValues());
	}

	public boolean intersectionValue(Attribute attr) {
		if (!(attr instanceof PersonAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		PersonAttribute pa = (PersonAttribute)attr;
		return ObjectIdUtils.isIntersectionDataObjects(values, pa.getValues());
	}
	
	/**
	 * Sets if attribute can be single or multivalued.
	 * @param multiValued true/false
	 */
	public void setMultiValued(boolean multiValued) {
		this.multiValued = multiValued;
	}

	/**
	 * Checks if attribute could have more than one value
	 * default value=true
	 */
	public boolean isMultiValued() {
		return this.multiValued;
	}

	/**
	 * Checks that single-valued atributes contains no more than one link to {@link Person}
	 */
	public boolean verifyValue() {
		//if (isMandatory() && (values == null || values.size() == 0))
		//	return false;
		if (!isMultiValued() && values != null && values.size() > 1)
			return false;
		return true;
	}

	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	public void clear() {
		this.values = new ArrayList<Person>();
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			this.values.addAll(((PersonAttribute) attr).getValues());
		}		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PersonAttribute[");
		sb.append("attrId: ").append(getId() != null ? getId().getId() : "null").append(", ");
		sb.append("values: [");
		if (!isEmpty()) {
			Iterator<Person> iter = getValues().iterator();
			while(iter.hasNext()) {
				sb.append(iter.next());
				if(iter.hasNext()) {
					sb.append(", ");
				}
			}
		} else {
			sb.append("empty");
		}
		sb.append("]");
		sb.append("]");
		return sb.toString();
	}
}
