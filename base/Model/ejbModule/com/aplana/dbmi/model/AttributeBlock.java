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

import java.util.Collection;
import java.util.Iterator;

/**
 * Block of card's attributes.
 * Each block could contain several ordered {@link Attribute attributes}
 * This class is used to represent abstract attribute block declaration which
 * is not associated with any template or card.
 * @see TemplateBlock 
 */
public class AttributeBlock extends LockableObject
{
	/**
	 * Identifier of 'COMMON' attribute block, included in every template
	 */
	public static final ObjectId ID_COMMON = new ObjectId(AttributeBlock.class, "COMMON");
	/**
	 * Identifier of 'REMOVED' attribute block. 'REMOVED' block contains attributes that was removed from template after creation of the {@link Card} object. Those attributes will be deleted with next card save  
	 */
	public static final ObjectId ID_REMOVED = new ObjectId(AttributeBlock.class, "REMOVED");
	/**
	 * Identifier of 'REST' attribute block. 'REST' block contains attributes of {@link CardVersion} that is not present in template 
	 */
	public static final ObjectId ID_REST = new ObjectId(AttributeBlock.class, "REST");

	/**
	 * Identifier of block, containing information of material
	 */
	public static final ObjectId ID_MATERIAL = new ObjectId(AttributeBlock.class, "MATERIAL");
	/**
	 * Identifier of block, containing information about cards hierarchy
	 */
	public static final ObjectId ID_HIERARCHY = new ObjectId(AttributeBlock.class, "HIERARCHY");
	
	private static final long serialVersionUID = 2L;
	private String nameRu;
	private String nameEn;
	private boolean active;
	private boolean system;
	private Collection<? extends Attribute> attributes;

	/**
	 * Sets identifier of AttributeBlock
	 * @param id block code
	 */
	public void setId(String id) {
		super.setId(id == null ? null : new ObjectId(AttributeBlock.class, id));
	}
	
	/**
	 * Returns russian name of attribute
	 * @return russian name of attribute
	 */	
	public String getNameRu() {
		return nameRu;
	}
	
	/**
	 * Returns english name of attribute block
	 * @return english name of attribute block
	 */	
	public String getNameEn() {
		return nameEn;
	}
	
	/**
	 * Returns localized name of attribute block
	 * @return returns value of {@link #getNameRu} or {@link #getNameEn} properties depending of caller's locale preferences
	 */
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}
	
	/**
	 * Check if this attribute block is Active
	 * @return true if this block is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Returns value of isSystem flag of attribute block
	 * System attribute blocks shouldn't be editable through GUI
	 * @return true if attribute block is a system block, false - otherwise
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Sets isActive flag of attribute block
	 * Inactive blocks
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Sets english name of attribute block
	 * @param nameEn desired value of english name
	 */		
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	/**
	 * Sets russian name of attribute block
	 * @param nameRu desired value of russian name
	 */	
	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	/**
	 * Sets isSystem flag on Attribute.
	 * System attribute blocks shouldn't be editable through GUI
	 * @param system desired value of isSystem flag
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	/**
	 * Gets collection of atributes associated with this attributes block
	 * @return collection of attributes containing in block
	 */
	@SuppressWarnings("unchecked")
	public <T extends Attribute> Collection<T> getAttributes() {
		return (Collection<T>)attributes;
	}

	/**
	 * Sets {@link Attribute attributes} collection
	 * @param attributes collection of attributes to be set
	 */ 
	public void setAttributes(Collection<? extends Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Checks if this block contains attribute with given code
	 * @param name attribute code
	 * @return true if block contains attribute with given code, false otherwise
	 */
	public boolean hasAttribute(String name) {
		if (attributes == null)
			return false;
		Iterator<? extends Attribute> itr = attributes.iterator();
		while (itr.hasNext()) {
			Attribute attr = itr.next();
			if (name.equals(attr.getId().getId()))
				return true;
		}
		return false;
	}
	
	/**
	 * Find attribute from attribute block by it's {@link ObjectId}
	 * @param id attribute code
	 * @return {@link Attribute} object with given id value or null if no such attribute exists in block
	 */
	public Attribute getAttributeById(ObjectId id) {
		if (attributes == null)
			return null;
		Iterator<? extends Attribute> itr = attributes.iterator();
		while (itr.hasNext()) {
			Attribute attr = itr.next();
			if (id.equals(attr.getId()))
				return attr;
		}
		return null;
	}

	/**
	 * Finds attribute at attribute block by passed attribute code name 
	 * @param name attribute code name
	 * @return {@link Attribute} object with given code value otherwise returns null
	 */
	public Attribute getAttributeByName(String name) {
		if (attributes == null)
			return null;
		
		Iterator<? extends Attribute> itr = attributes.iterator();
		while (itr.hasNext()) {
			Attribute attr = itr.next();
			if (name.equals(attr.getId().getId()))
				return attr;
		}
		return null;
	}	
}
