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
 * Class represent one entry of {@link Reference} dictionary
 * Represents one record from VALUES_LIST table.
 * Dictionaries could be flat and hierarchical. In second case each value could
 * have a parent record in same dictionary (cycles is not allowed).
 * Hence hierarchical dictionary could be presented
 * as a tree of available values.
 */
public class ReferenceValue extends DataObject {
	/**
	 * Identifier used for user-defined values
	 */
	public static final ObjectId ID_ANOTHER = new ObjectId(ReferenceValue.class, 0);
	/**
	 * @deprecated Use ID_ANOTHER instead
	 */
	@Deprecated
	public static final ObjectId VALUE_ANOTHER = ID_ANOTHER;

	private static final long serialVersionUID = 1L;
	private ObjectId reference;
	private String valueRu;
	private String valueEn;
	private int order;
	private boolean active;
	private ObjectId parent;
	private Collection<ReferenceValue> children;

	/**
	 * Sets identifier of {@link ReferenceValue} object
	 * @param id identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(ReferenceValue.class, id));
	}

	/**
	 * Gets identifier of {@link Reference} dictionary
	 * which includes this {@link ReferenceValue}
	 * @return identifier of parent {@link Reference} object
	 */
	public ObjectId getReference() {
		return reference;
	}

	/**
	 * Gets russian name of value
	 * @return russian name of value
	 */
	public String getValueRu() {
		return valueRu;
	}

	/**
	 * Gets english name of value
	 * @return english name of value
	 */
	public String getValueEn() {
		return valueEn;
	}

	/**
	 * Gets localized name of value
	 * @return returns {@link #getValueEn()} or {@link #getValueRu()} depending of caller's locale context
	 */
	public String getValue() {
		return ContextProvider.getContext().getLocaleString(valueRu, valueEn);
	}

	/**
	 * Gets order of this value in dictionary
	 * @return order of value in dictionary
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Checks if this value object is active.
	 * Inactive values are not available in GUI
	 * @return true if this value is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets isActive flag of this value object
	 * Inactive values are not available in GUI
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets order of this value in dictionary
	 * @param order desired order in dictionary
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Sets link to parent dictionary value of this value object
	 * @param parent link to parent {@link ReferenceValue} object.
	 * Parent object should be from the same dictionary as his children.
	 */
	public void setParent(ObjectId parent) {
		this.parent = parent;
	}

	/**
	 * Returns collection of children {@link ReferenceValue} objects
	 * @return collection of children values
	 */
	public Collection<ReferenceValue> getChildren() {
		return children;
	}

	/**
	 * Sets collection of children {@link ReferenceValue} objects
	 * @param children collection of children values
	 */
	public void setChildren(Collection<ReferenceValue> children) {
		this.children = children;
	}

	/**
	 * Sets identifier of {@link Reference} dictionary
	 * which includes this {@link ReferenceValue}
	 * @param reference identifier of parent {@link Reference} object
	 */
	public void setReference(ObjectId reference) {
		this.reference = reference;
	}

	/**
	 * Sets english name of value object
	 * @param valueEn english name of value object
	 */
	public void setValueEn(String valueEn) {
		this.valueEn = valueEn;
	}

	/**
	 * Sets russian name of value object
	 * @param valueRu russian name of value object
	 */
	public void setValueRu(String valueRu) {
		this.valueRu = valueRu;
	}

	/**
	 * Gets identifier of parent {@link ReferenceValue} object
	 * @return identifier of parent {@link ReferenceValue} object
	 */
	public ObjectId getParent() {
		return parent;
	}

	/**
	 * Create new ReferenceValue instance to represent user-defined value.
	 * This method could be used if specified {@link Reference} dictionary misses
	 * value required by user.
	 * @param value String name of new value. This value will be used for both name versions: english and russian
	 * @return newly created {@link ReferenceValue} instance with id = {@link #ID_ANOTHER} and given string value.
	 */
	public static ReferenceValue newAnotherValue(String value) {
		ReferenceValue obj = new ReferenceValue();
		obj.setId(ID_ANOTHER);
		obj.setValueRu(value);
		obj.setValueEn(value);
		return obj;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return getValue();
	}
}
