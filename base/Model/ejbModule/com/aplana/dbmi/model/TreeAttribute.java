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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * {@link Attribute} descendant used to store one or more records
 * from hierarchical {@link Reference dictionary}.
 * In GUI is shown as tree witch checkboxes against each node.
 * User could select several nodes simultaneously or (@link {@link ReferenceValue#newAnotherValue(String) specify own value}
 */
public class TreeAttribute extends ReferenceAttribute
{
	private static final long serialVersionUID = 6L;
	private Collection<ReferenceValue> values;

	/**
	 * Returns collection of selected {@link ReferenceValue} objects
	 * @return collection of selected {@link ReferenceValue} objects
	 */
	public Collection<ReferenceValue> getValues() {
		return values;
	}

	/**
	 * Sets collection of selected  {@link ReferenceValue} objects
	 * @param values collection of selected  {@link ReferenceValue} objects
	 */
	public void setValues(Collection<ReferenceValue> values) {
		this.values = values;
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		if (values == null)
			return "";
		StringBuffer buf = new StringBuffer();
		Iterator<ReferenceValue> itr = values.iterator();
		while (itr.hasNext())
		{
			ReferenceValue item = itr.next();
			buf.append(item.getValue());
			if (itr.hasNext())
				buf.append(", ");
		}
		return buf.toString();
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_TREE;
	}

	
	/**
	 * Checks is this attribute contains same selected values set as given attribute attr.
	 * If there is a user-defined values in both attributes then checks that user-defined
	 * strings are equal 
	 * @throws IllegalArgumentException is attr is not TreeAttribute instance
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof TreeAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		TreeAttribute ta = (TreeAttribute) attr;
		Collection<ReferenceValue> otherValues = ta.getValues();
		if (!ObjectIdUtils.isSameDataObjects(values, otherValues)) {
			return false;
		}
		if (!isRestrictedList() && !isEmpty()) {
			ReferenceValue anotherValue = getAnotherValue();
			if (anotherValue != null) {
				ReferenceValue otherAnotherValue = ta.getAnotherValue();
				// otherAnotherValue ����� �� ��������� �� null, ��� ��� �� ���� ��� ���������
				// �� ���������� ���������������
				String val1 = anotherValue.getValue(),
					val2 = otherAnotherValue.getValue();
				if (val1 == null) {
					val1 = "";
				}
				if (val2 == null) {
					val2 = "";
				}
				return val1.equals(val2); 
			}
		}
		return true;
	}

	/**
	 * @see Attribute#getType()
	 */
	public boolean isMultiValued() {
		return true;
	}

	/**
	 * If this attribute have {@link #isRestrictedList()}  flag = true,
	 * then checks that attribute have no user-defined values.
	 * @return false if attribute have isRestrictedList set to true and there is
	 * a record with id = {@link ReferenceValue#ID_ANOTHER} in values collection.
	 * Otherwise returns true
	 */
	public boolean verifyValue() {
		if (isRestrictedList()) {
			if (getAnotherValue() != null) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns user-defined value of this {@link TreeAttribute} if it exists
	 * @return user-defined value of this {@link TreeAttribute} or null if user has not specified it
	 */
	public ReferenceValue getAnotherValue() {
		Iterator<ReferenceValue> itr = values.iterator();
		while (itr.hasNext()) {
			ReferenceValue item = itr.next();
			if (ReferenceValue.ID_ANOTHER.equals(item.getId())) {
				return item;
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	public void clear() {
		this.values = new ArrayList<ReferenceValue>();
	}
	
	/**
	 * Checks whether the attribute contains a value with specific ID.
	 * @param valueId ID of reference value to search
	 * @return true if value was found
	 */
	public boolean hasValue(ObjectId valueId) {
		if (valueId == null || !ReferenceValue.class.equals(valueId.getType()))
			throw new IllegalArgumentException("valueId must be a reference value id");
		if (values == null)
			return false;
		for (Iterator<ReferenceValue> itr = values.iterator(); itr.hasNext(); ) {
			ReferenceValue val = itr.next();
			if (valueId.equals(val.getId()))
				return true;
		}
		return false;
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.clear();
			this.values.addAll(((TreeAttribute) attr).getValues());
		}	
	}
}
