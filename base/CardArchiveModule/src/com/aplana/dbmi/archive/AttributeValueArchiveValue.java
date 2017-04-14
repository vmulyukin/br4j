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
package com.aplana.dbmi.archive;

import java.util.Date;

import com.aplana.dbmi.model.ObjectId;

public class AttributeValueArchiveValue {
	
	private ObjectId attributeCode;
	private Long numberValue;
	private String stringValue;
	private Date dateValue;
	private Long valueId;
	private String anotherValue;
	private String longBinaryValue;
	
	public ObjectId getAttributeCode() {
		return attributeCode;
	}
	
	public void setAttributeCode(ObjectId attributeCode) {
		this.attributeCode = attributeCode;
	}
	
	public Long getNumberValue() {
		return numberValue;
	}
	
	public void setNumberValue(Long numberValue) {
		this.numberValue = numberValue;
	}
	
	public String getStringValue() {
		return stringValue;
	}
	
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public Date getDateValue() {
		return dateValue;
	}
	
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
	
	public Long getValueId() {
		return valueId;
	}
	
	public void setValueId(Long valueId) {
		this.valueId = valueId;
	}
	
	public String getAnotherValue() {
		return anotherValue;
	}
	
	public void setAnotherValue(String anotherValue) {
		this.anotherValue = anotherValue;
	}
	
	public String getLongBinaryValue() {
		return longBinaryValue;
	}
	
	public void setLongBinaryValue(String longBinaryValue) {
		this.longBinaryValue = longBinaryValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((anotherValue == null) ? 0 : anotherValue.hashCode());
		result = prime * result
				+ ((attributeCode == null) ? 0 : attributeCode.hashCode());
		result = prime * result
				+ ((dateValue == null) ? 0 : dateValue.hashCode());
		result = prime * result
				+ ((longBinaryValue == null) ? 0 : longBinaryValue.hashCode());
		result = prime * result
				+ ((numberValue == null) ? 0 : numberValue.hashCode());
		result = prime * result
				+ ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((valueId == null) ? 0 : valueId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeValueArchiveValue other = (AttributeValueArchiveValue) obj;
		if (anotherValue == null) {
			if (other.anotherValue != null)
				return false;
		} else if (!anotherValue.equals(other.anotherValue))
			return false;
		if (attributeCode == null) {
			if (other.attributeCode != null)
				return false;
		} else if (!attributeCode.equals(other.attributeCode))
			return false;
		if (dateValue == null) {
			if (other.dateValue != null)
				return false;
		} else if (!dateValue.equals(other.dateValue))
			return false;
		if (longBinaryValue == null) {
			if (other.longBinaryValue != null)
				return false;
		} else if (!longBinaryValue.equals(other.longBinaryValue))
			return false;
		if (numberValue == null) {
			if (other.numberValue != null)
				return false;
		} else if (!numberValue.equals(other.numberValue))
			return false;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		if (valueId == null) {
			if (other.valueId != null)
				return false;
		} else if (!valueId.equals(other.valueId))
			return false;
		return true;
	}
}
