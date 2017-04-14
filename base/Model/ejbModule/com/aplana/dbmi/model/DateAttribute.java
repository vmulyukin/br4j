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

import java.util.Date;

import com.aplana.dbmi.model.util.DateUtils;

/**
 * {@link Attribute} representing simple date value 
 */
public class DateAttribute extends Attribute
{
	private static final long serialVersionUID = 4L;
	// ����������� �������� � ������� ������ (����� "-")
	public static final String defaultTimePattern = "dd-MM-yyyy";
	//public static final String defaultTimePattern = "HH:mm";
	private Date value;
	//private boolean showTime;
	private String timePattern; // Other available values: yyyy-MM-dd HH:mm etc
	/**
	 * Regular expressions to determine whether or not the timePattern contains time value 
	 */
	private static String regExp = ".*(H|k|K|h|m|s|S).*";
	/**
	 * Gets attribute value
	 * @return date value stored in attribute
	 */
	public Date getValue() {
		return value;
	}
	
	/**
	 * Sets attribute value
	 * @param value desired value
	 */
	public void setValue(Date value) {
		this.value = value;
	}

	/**
	 * ��������� ���� � ������ ����-���� (������������ �����, ����� ���������� ���� �� ��)
	 * @param value desired value
	 */
	public void setValueWithTZ(java.util.Date value) {
		
		this.value = DateUtils.setValueWithTZ(value);
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		if (value == null)
			return "";
		/*if (showTime) {
			String pattern = (timePattern != null) 
						? timePattern
						: defaultTimePattern;
			return ContextProvider.getContext().getLocaleDateTime(value, pattern);
		}
		else
			return ContextProvider.getContext().getLocaleDate(value);*/		
		if (this.timePattern != null) 
			return ContextProvider.getContext().getLocaleDateTime(value, this.timePattern);
		else 
			// ��� ������������� � ���������� �������� � getData() � searchAdapter.
			return ContextProvider.getContext().getLocaleDateTime(value, defaultTimePattern);
			
	}
	
	public String getStringValue(String pattern) {
		if (value == null)
			return "";
		return ContextProvider.getContext().getLocaleDateTime(value, pattern);
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_DATE;
	}

	/**
	 * Checks if this attribute contains same value as given {@link Attribute} attr
	 * @return true is values are equal, false otherwise
	 * @throws IllegalArgumentException if attr is not instance of DateAttributeClass
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof DateAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		Date otherValue = ((DateAttribute) attr).getValue();
		return value == null ? otherValue == null : value.equals(otherValue);
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		//if (isMandatory() && value == null)
		//	return false;
		return true;
	}
	
	/**
	 * Sets show time flag
	 * @param showTime desired value of show time flag
	 */
	/*public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}
	*/
	/**
	 * Whether or not the timePattern contains time (for example: HH or mm or ss) value
	 * @return true if contains, else false
	 */
	public boolean isShowTime() {
			return (this.timePattern !=null ? this.timePattern : DateAttribute.defaultTimePattern).matches(regExp);
	}
	
	/**
	 * Set time pattern
	 * @param timePattern 
	 */
	public void setTimePattern(String timePattern) {
		this.timePattern = timePattern;
	}
	
	/**
	 * Returns time pattern, may be null
	 * @return time pattern
	 */
	public String getTimePattern() {
		return timePattern;
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
	
	public int compareTo(Attribute attr) {
		if (!(attr instanceof DateAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		Date otherValue = ((DateAttribute) attr).getValue();
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
			this.setValue(((DateAttribute) attr).getValue());
		}	
	}
}
