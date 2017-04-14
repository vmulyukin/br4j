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

import java.util.List;

public class MultiListAttribute extends ListAttribute {
	private static final long serialVersionUID = 5L;
	
	private List<ReferenceValue> values;

	
	@Override
	public String getStringValue() {
		if(values != null
				&& !values.isEmpty())
		{
			StringBuilder sb = new StringBuilder();
			for(int i=0; i< values.size(); i++) {
				ReferenceValue value = values.get(i);
				if(i != 0)
					sb.append(",");
				sb.append(value.getValue());
			}
			return sb.toString();
		}
		return "";
	}

	@Override
	public boolean verifyValue() {
		return true;
	}

	@Override
	public boolean equalValue(Attribute attr) {
		boolean equals = false;
		if (!(attr instanceof MultiListAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		List<ReferenceValue> otherValues = ((MultiListAttribute) attr).getValues();
		return this.values.equals(otherValues);
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			this.setValues(((MultiListAttribute) attr).getValues());
		}	
	}
	
	public boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	/**
	 * Sets value of attribute to null
	 */
	public void clear() {
		this.values = null;
	}

	public List<ReferenceValue> getValues() {
		return values;
	}

	public void setValues(List<ReferenceValue> values) {
		this.values = values;
	}
}
