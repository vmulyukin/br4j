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
package com.aplana.dbmi.archive.export;

import java.util.Date;

import com.aplana.dbmi.model.ObjectId;

/**
 * ������������� ������� attribute_value_hist
 * ��� �������� � XML
 * ������������ � ������ � CardVersionHist
 * @author ppolushkin
 *
 */
public class AttributeValueHist {

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
	
	

}
