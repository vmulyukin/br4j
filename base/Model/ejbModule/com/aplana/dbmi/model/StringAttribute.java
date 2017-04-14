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
 * {@link Attribute} descendant, used to store short string values.
 * In GUI shows as single text field 
 */
public class StringAttribute extends Attribute
{
	private static final long serialVersionUID = 4L;
	private int displayLength;
	private String value;
	
	/**
	 * Gets display length of attribute in GUI
	 * Will be used in future. For now not implemented
	 * @return display length of attribute in GUI
	 */	
	public int getDisplayLength() {
		return displayLength;
	}
	
	/**
	 * Gets value of attribute
	 * @return string value stored in attribute
	 */	
	public String getValue() {
		return value;
	}

	/**
	 * Sets display length of attribute in GUI
	 * Will be used in future. For now not implemented
	 * @param displayLength desied value of display length of attribute in GUI
	 */	
	public void setDisplayLength(int displayLength) {
		this.displayLength = displayLength;
	}

	/**
	 * Sets value of attribute
	 * @param value desired string value
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		return value == null ? "" : value;
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_STRING;
	}

	/**
	 * Checks if this attribute has same value as given {@link StringAttribute}
	 * During comparison heading and trailing whitespaces are removed from both strings,
	 * null strings are considered equals to empty string. 
	 * @throws IllegalArgumentException if attr is not e {@link StringAttribute} instance
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof StringAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		String thisValue = value == null ? "" : value.trim();
		String otherValue = ((StringAttribute) attr).getValue();
		otherValue = otherValue == null ? "" : otherValue.trim();
		return thisValue.equals(otherValue);
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		//if (isMandatory() && (value == null || value.length() == 0))
		//	return false;
		return true;
	}

	public boolean isEmpty() {
		return value == null || "".equals(value);
	}

	/**
	 * Sets value of attribute to empty string
	 */
	public void clear() {
		value = "";
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.setValue(((StringAttribute) attr).getValue());
		}	
	}
}
