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

public class CardHistoryAttribute extends StringAttribute implements PseudoAttribute {
	private static final long serialVersionUID = 1L;

	/**
	 * Always returns true
	 * @return true
	 */
	public boolean equalValue(Attribute attr) {
		return true;
	}

	/**
	 * @see Attribute#getStringValue()
	 */
	public String getStringValue() {
		return "History Attribute is here !!!";	
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_CARD_HISTORY;
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		return true;
	}

	/**
	 * @see Attribute#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/**
	 * This attribute have to be readOnly so
	 * any attempt to make it editable will cause IllegalArgumentException
	 * @param readOnly only true is allowed
	 * @throws IllegalArgumentException if readOnly = false
	 */
	public void setReadOnly(boolean readOnly) {
		if (!readOnly)
			throw new IllegalArgumentException("Card history attribute cannot be writable");
	}

	/**
	 * Always returns true
	 */
	public boolean isEmpty() {
		return true;
	}
}
