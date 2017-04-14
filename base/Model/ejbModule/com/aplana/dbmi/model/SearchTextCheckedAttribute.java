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

public class SearchTextCheckedAttribute extends SearchStringCheckedAttribute {
	
	private static final long serialVersionUID = 4L;
	private int rowsNumber;
	
	/**
	 * returns number or text lines to show in GUI (min = 5)
	 * @return number or shown text lines
	 */
	public int getRowsNumber() {
		return rowsNumber;
	}
	
	/**
	 * Sets number of text lines to show in GUI
	 * @param rowsNumber desired number of text-line to show in GUI
	 */
	public void setRowsNumber(int rowsNumber) {
		this.rowsNumber = rowsNumber;
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_TEXT;
	}

	/**
	 * @see Attribute#equalValue(Attribute)
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof TextAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		String otherValue = ((TextAttribute) attr).getValue();
		return getValue() == null ? otherValue == null : getValue().equals(otherValue);
	}

}
