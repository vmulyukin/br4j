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
 * View parameters for template attributes.
 * Each object defines display parameters of one {@link Attribute attribute} for
 * given {@link SystemRole role} and {@link CardState card state}<br>
 * Each object of this class maps to exactly one row in 
 * ATTRIBUTE_VIEW_PARAM table.<br>
 * This class should be used for CRUD operations only, 
 * for business logic use {@link AttributeViewParam} instead.<br>
 * {@link #getId} uses values from REC_ID column as identity
 * @author dsultanbekov
 */
public class AttributeViewParamDetail extends AttributeViewParam {
	private static final long serialVersionUID = 1L;

	private long templateAttributeId;
	private String attributeCode;
	private String roleCode;
	private long stateId;
	private ObjectId personAttributeId;

	/**
	 * Returns identifier of personAttribute
	 * @return
	 */
	public ObjectId getPersonAttributeId() {
		return personAttributeId;
	}
	
	/**
	 * Sets identifier of personAttribute
	 * @param personAttributeId
	 */
	public void setPersonAttributeId(ObjectId personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	/**
	 * Returns identifier of attribute, included in template (value of TEMPLATE_ATTR_ID column)
	 * @return identifier of attribute, included in template
	 */
	public long getTemplateAttributeId() {
		return templateAttributeId;
	}
	/**
	 * Sets identifier of attribute included in template (value of TEMPLATE_ATTR_ID column)
	 * @param templateAttributeId desired identifier value
	 */
	public void setTemplateAttributeId(long templateAttributeId) {
		this.templateAttributeId = templateAttributeId;
	}
	
	/**
	 * Gets string identifier of corresponding {@link Attribute}
	 * @return string identifier of corresponding attribute
	 */
	public String getAttributeCode() {
		return attributeCode;
	}
	
	/**
	 * Sets string identifier of corresponding {@link Attribute}
	 * @param attributeCode
	 */
	public void setAttributeCode(String attributeCode) {
		this.attributeCode = attributeCode;
	}
	
	/**
	 * Gets {@link SystemRole role} code associated with given object
	 * @return role code
	 */
	public String getRoleCode() {
		return roleCode;
	}
	
	/**
	 * Sets {@link SystemRole role} code associated with given object 
	 * @param roleCode desired value of role code
	 */
	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
	
	/**
	 * Gets {@link CardState card state} associated with given object
	 * @return card status id
	 */
	public long getStateId() {
		return stateId;
	}
	
	/**
	 * Sets {@link CardState card state} associated with given object
	 * @param stateId desired card status id
	 */
	public void setStateId(long stateId) {
		this.stateId = stateId;
	}
}
