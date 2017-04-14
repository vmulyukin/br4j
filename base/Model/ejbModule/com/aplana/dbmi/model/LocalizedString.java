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

import java.io.Serializable;

/**
 * Utility class used to represent localized names of various entities in system.
 * For now supports english and russian languages only.
 */
public class LocalizedString implements Serializable {
	private static final long serialVersionUID = 1L;

	private String valueRu;
	private String valueEn;

	/**
	 * Default constructor
	 */
	public LocalizedString() {
	}
	
	/**
	 * Creates new initialized instance
	 * @param ru russian value of string
	 * @param en english value of string
	 */
	public LocalizedString(String ru, String en) {
		setValueRu(ru);
		setValueEn(en);
	}
	
	/**
	 * Gets russian value of string
	 * @return russian value
	 */
	public String getValueRu() {
		return valueRu;
	}

	/**
	 * Sets russian value
	 * @param valueRu desired value of string in russian
	 */
	public void setValueRu(String valueRu) {
		this.valueRu = valueRu;
	}

	/**
	 * Gets english value of string
	 * @return english value
	 */
	public String getValueEn() {
		return valueEn;
	}

	/**
	 * Sets english value
	 * @param valueEn desired value of string in english
	 */
	public void setValueEn(String valueEn) {
		this.valueEn = valueEn;
	}

	/**
	 * Gets value of string in locale choosen by user who calls this method 
	 * @return {@link #getValueEn()} or {@link #getValueRu()} depending on caller's locale context
	 */	
	public String getValue() {
		return ContextProvider.getContext().getLocaleString(valueRu, valueEn);
	}

	/**
	 * Checks if this {@link LocalizedString} instance has empty value for at least one
	 * language. String is considered empty if it is null, or if it contains whitespaces only.
	 * This method is intended for using in various validation routines. 
	 * @return true if empty value(s) exists, false otherwise
	 */
	public boolean hasEmptyValues() {
		return valueRu == null || "".equals(valueRu.trim()) || valueEn == null || "".equals(valueEn.trim());
	}
	
	/**
	 * @return result of {@link #getValue()} or empty string if #getValue return null
	 */
	public String toString() {
		String st = getValue();
		return st == null ? "" : st; 
	}
	
	/**
	 * Copies string values from given {@link LocalizedString} instance 
	 * @param st instance to copy values from
	 */
	public void assign(LocalizedString st) {
		this.valueRu = st.valueRu;
		this.valueEn = st.valueEn;
	}
	
	/**
	 * Compares values of two localized strings in current locale, ignoring case differences. 
	 * @param st {@link LocalizedString}
	 * @return a negative integer, zero, or a positive integer as the
     *	value of specified LocalizedString in current locale is greater than, equal to, or less
     *	than value of this LocalizedString in current locale, ignoring case considerations. 
	 */
	public int compareToIgnoreCase(LocalizedString st) {
		String value1 = getValue(),
			value2 = st == null ? null : st.getValue();
		if (value1 == null && value2 != null) {
			return -1;
		} else if (value1 != null && value2 == null) {
			return 1;
		} else if (value1 == null && value2 == null) {
			return 0;
		} else {
			return value1.compareToIgnoreCase(value2);
		}
	}
}
