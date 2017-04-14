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
 * {@link Attribute} descendant used to store {@link ReferenceValue value}
 * selected from {@link Reference dictionary}.
 * In GUI presented as drop-down list with single selection.
 * Dictionary to take available values from is defined in ATTRIBUTE_OPTION table 
 */
public class ListAttribute extends ReferenceAttribute
{
	private static final long serialVersionUID = 5L;
	private ReferenceValue value;
	
	/**
	 * Gets attribute value
	 * {@link ReferenceValue} object presenting single value from dictionary
	 * @return attribute value 
	 */
	public ReferenceValue getValue() {
		return value;
	}
	
	/**
	 * Sets attribute value
	 * @param value desired attribute value
	 */
	public void setValue(ReferenceValue value) {
		this.value = value;
	}
	
	/**
	 * @see Attribute#getType()
	 */
	public String getStringValue() {
		if (value == null)
			return "";
		return value.getValue();
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_LIST;
	}

	/**
	 * @see Attribute#equalValue(Attribute)
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof ListAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		ReferenceValue otherValue = ((ListAttribute) attr).getValue();
		if (value == null || otherValue == null) {
			return value == otherValue;
		}
		return value.getId().equals(otherValue.getId());
	}
	
	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		//if (isMandatory() && value == null)
		//	return false;
		return true;
	}

	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * Sets value of attribute to null
	 */
	public void clear() {
		this.value = null;
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.setValue(((ListAttribute) attr).getValue());
		}		
	}
}
