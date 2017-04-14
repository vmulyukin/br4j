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
 * {@link com.aplana.dbmi.model.Attribute} descendant used to store integer numeric values
 * @author dsultanbekov
 */
public class LongAttribute extends Attribute
{
	private static final long serialVersionUID = 4L;
	private int displayLength;
	private Long value;

	/**
	 * Gets display length of attribute in GUI
	 * Will be used in future. For now not implemented
	 * @return display length of attribute in GUI
	 */
	public int getDisplayLength() {
		return displayLength;
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
	 * Gets value of attribute
	 * @return value of attribute if it is specified, or zero if it is not specified
	 */
	public long getValue() {
		return isNull() ? 0 : value.longValue();
	}

	/**
	 * Sets value of attribute
	 * @param value desired value of attribute's value
	 */
	public void setValue(long value) {
		this.value = new Long(value);
	}

	/**
	 * Checks if value of this attribute is null
	 * @return true if value of attribute is not specified, false if it is specified
	 */
	public boolean isNull() {
		return this.value == null;
	}

	/**
	 * @see com.aplana.dbmi.model.Attribute#getStringValue()
	 */
	public String getStringValue() {
		return String.valueOf(value);
	}

	/**
	 * @see com.aplana.dbmi.model.Attribute#getType()
	 */
	public Object getType() {
		return TYPE_LONG;
	}

	/**
	 * @see com.aplana.dbmi.model.Attribute#equalValue(com.aplana.dbmi.model.Attribute)
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof LongAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		Long otherValue = ((LongAttribute) attr).value;
		return value == null ? otherValue == null : value.equals(otherValue);
	}

	/**
	 * @see com.aplana.dbmi.model.Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		return true;
	}

	/**
	 * Always returns false 
	 */
	public boolean isEmpty() {
		return false;
	}

	/**
	 * Sets value of attribute to null
	 */
	public void clear() {
		this.value = null;
	}
	
	public int compareTo(Attribute attr) {
		if (!(attr instanceof LongAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		Long otherValue = ((LongAttribute) attr).value;
		if (value == null) {
			if (otherValue == null) {
				return 0;
			}
			else {
				return (0 - otherValue.compareTo(value));
			}
		}
		else {
			return value.compareTo(otherValue);
		}
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.setValue(((LongAttribute) attr).getValue());
		}	
	}
}
