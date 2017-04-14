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
 * Class representing default values for card attributes.
 * Those values are used in initialization of newly created card.
 * It is possible to specify default value for each {@link Attribute}
 * in given {@link Template}<br>
 * In case of multivalued attributes (for ex. {@link TreeAttribute}) it is possible
 * to specify set of values (in this case {@link #getValue} will return collection of objects).
 * <br>
 * {@link #getId()} uses Long value of TEMPLATE_ATTR_ID column as identifier
 * @author dsultanbekov
 */
public class DefaultAttributeValue extends DataObject {
	private static final long serialVersionUID = 1L;
	private ObjectId attributeId;
	private Object value;
	
	/**
	 * Returns default value of attribute
	 * Type of result could vary depending of attribute's type:
	 * @return default value of attribute
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets default value of attribute
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns identifier of {@link Attribute} for which default value is specified 
	 * @return identifier of attribute
	 */
	public ObjectId getAttributeId() {
		return attributeId;
	}
	
	/**
	 * Sets identifier of {@link Attribute} for which default value is specified
	 * attributeId.getType should return one of {@link Attribute} descendants 
	 * @param attributeId attribute identifier
	 */
	public void setAttributeId(ObjectId attributeId) {
		this.attributeId = attributeId;
	}
}
