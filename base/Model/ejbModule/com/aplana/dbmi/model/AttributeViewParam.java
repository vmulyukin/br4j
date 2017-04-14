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
 * Attributes view parameters for given card and user.<br>
 * Defines display parameters for given {@link Attribute attribute} of {@link Card} 
 * for given {@link Person}.<br>
 * Values of isMandatory, isHidden and isReadonly flags is integral values, based on all roles available for given person.
 * For example attribute is not readonly if it is not readonly for at least one role of given user.<br>
 * This class should be used only on read-only basis 
 * (to determine display parameters of attribute in GUI, or in queries). 
 * For CRUD operations use {@link AttributeViewParamDetail}<br>
 * {@link #getId} returns String attribute codes as identity
 */
public class AttributeViewParam extends DataObject {	
	private static final long serialVersionUID = 1L;
	
	private ObjectId attributeId;
	private boolean mandatory;
	private boolean hidden;
	private boolean readOnly;
	/**
	 * Checks if given attribute is mandatory for given user in current {@link CardState card state}
	 * Mandatory attributes must have non-empty value during during changing of {@link CardState card's status} 
	 * @return true if attribute is mandatory, false otherwise
	 */
	public boolean isMandatory() {
		return mandatory;
	}
	/**
	 * Sets isMandatory flag
	 * Mandatory attributes must have non-empty value during during changing of {@link CardState card's status} 
	 * @param mandatory desired value of isMandatory flag
	 */	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	/**
	 * Checks if given attribute is hidden for given user in current card state
	 * Hidden attributes are not shown while viewing/editing {@link Card} through GUI 
	 * @return true if attribute is hidden, false otherwise
	 */
	public boolean isHidden() {
		return hidden;
	}
	/**
	 * Sets isHidden flag
	 * Hidden attributes are not shown while viewing/editing {@link Card} through GUI 
	 * @param hidden desired value of isHidden flag
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	/**
	 * Checks if given attribute is not editable for given user in current card state
	 * Readonly attributes is not editable while editing {@link Card} through GUI. 
	 * @return true if attribute is not ediatble, false otherwise
	 */	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * Sets isReadonly flag
	 * Readonly attributes is not editable while editing {@link Card} through GUI. 
	 * @param readonly desired value of flag
	 */
	public void setReadOnly(boolean readonly) {
		this.readOnly = readonly;
	}
	
	public ObjectId getAttribute() {
		return attributeId;
	}
	
	public void setAttribute(ObjectId attributeId) {
		this.attributeId = attributeId;
	}

	public void setAttribute(String attributeId) {
		this.attributeId = (attributeId == null ? null : new ObjectId(Attribute.class, attributeId));
	}
}
