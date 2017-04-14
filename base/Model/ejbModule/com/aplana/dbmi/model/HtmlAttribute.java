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
 * {@link Attribute} descendant used to store large amount of text 
 * (ususally fragments of HTML markup).
 * For storage BLOB field is used, so there is almost no limitation on value size
 */
public class HtmlAttribute extends TextAttribute
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Attribute#equalValue(Attribute)
	 */
	public boolean equalValue(Attribute attr) {
		if (!(attr instanceof HtmlAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		String otherValue = ((StringAttribute) attr).getValue();
		return getValue() == null ? otherValue == null : getValue().equals(otherValue);
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_HTML;
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		return true;
	}
}
