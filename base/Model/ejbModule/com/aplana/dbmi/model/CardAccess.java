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
 * @deprecated Replaced with {@link AccessRule} class and its descendants
 * Card access settings (represents records from CARD_ACCESS table)
 * For every template defines who can perform following actions:
 * <ul>
 * 	<li>workflow move (card state changing)</li>
 *  <li>card reading</li>
 *  <li>card editing</li>
 *  <li>card creation</li>
 * </ul>
 * {@link #getPermissionType} method defines type of permission (see list above)<br>
 * {@link #getObjectId} method defines object permissions applied for.<br>
 * <br>
 * Access could be granted for specific {@link SystemRole} - via {@link #setRoleId},
 * or for user mentioned in card - via {@link #setPersonAttributeId}
 * <br>
 * If both personAttributeCode and roleCode is empty, then permission is granted to any user
 */
@Deprecated
public class CardAccess extends DataObject {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constant used to define that card access shouldn't be verified
	 */
	public static Long NO_VERIFYING = new Long(-1);
	/**
	 * Constant used to define permissions for performing given workflow move 
	 * {@link #getObjectId} should return identifier of {@link WorkflowMove} class
	 */
	public static Long WORKFLOW_MOVE = new Long(1);
	/**
	 * Constant used to define permissions for reading card in given state
	 * {@link #getObjectId} should return identifier of {@link CardState} class 
	 */	
	public static Long READ_CARD = new Long(2);
	/**
	 * Constant used to define permissions for editing card in given state  
	 * {@link #getObjectId} should return identifier of {@link CardState} class 
	 */
	public static Long EDIT_CARD = new Long(3);
	/**
	 * Constant used to define permissions for creation card  by given template 
	 * {@link #getObjectId} should return identifier of {@link Template} class 
	 */
	public static Long CREATE_CARD = new Long(4);
	
	private Long permissionType;
	private ObjectId objectId;
	private ObjectId roleId;
	private ObjectId templateId;
	private ObjectId personAttributeId;

	/**
	 * Gets type of permission
	 * Should be one of the following constants: {@link #WORKFLOW_MOVE}, {@link #READ_CARD}, {@link #EDIT_CARD}, {@link #CREATE_CARD}
	 * @return type of permission
	 */
	public Long getPermissionType() {
		return permissionType;
	}
	/**
	 * Sets type of permission
	 * @param permissionType desired type of permission (should be one of the following constants: {@link #WORKFLOW_MOVE}, {@link #READ_CARD}, {@link #EDIT_CARD}, {@link #CREATE_CARD})
	 */
	public void setPermissionType(Long permissionType) {
		this.permissionType = permissionType;
	}
	
	/**
	 * Gets identifier of object for which permission is setted. 
	 * Type of object can vary depending of {@link #getPermissionType} value
	 * @return identifier  
	 */
	public ObjectId getObjectId() {
		return objectId;
	}
	
	/**
	 * Sets identifier of object for which permission is setted. 
	 * Type of object can vary depending of {@link #getPermissionType} value
	 * @param objectId identifier of object
	 */
	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}
	
	/**
	 * Get identifier of role
	 * @return identifier of {@link SystemRole} object
	 */
	public ObjectId getRoleId() {
		return roleId;
	}
	
	/**
	 * Sets identifier of role
	 * @param roleId identifier of {@link SystemRole} object
	 */	
	public void setRoleId(ObjectId roleId) {
		this.roleId = roleId;
	}
	
	/**
	 * Gets identifier of card attribute containing link to {@link Person} object
	 * @return identifier or {@link PersonAttribute} object
	 */
	public ObjectId getPersonAttributeId() {
		return personAttributeId;
	}

	/**
	 * Sets identifier of card attribute containing link to {@link Person} object
	 * @param personAttributeId identifier or {@link PersonAttribute} object
	 */
	public void setPersonAttributeId(ObjectId personAttributeId) {
		this.personAttributeId = personAttributeId;
	}
	
	/**
	 * Gets identifier of {@link Template} whose permission is defined by this {@link CardAccess} instance
	 * @return identifier of {@link Template} whose permission is defined by this {@link CardAccess} instance
	 */
	public ObjectId getTemplateId() {
		return templateId;
	}
	
	/**
	 * Sets identifier of {@link Template} whose permission is defined by this {@link CardAccess} instance
	 * @param templateId identifier of {@link Template} whose permission is defined by this {@link CardAccess} instance
	 */
	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}
}
