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
 * {@link Attribute} descendant used to store materials attached to card
 * Material is a file stored in database or link to external resource (in form of URL).
 */
public class MaterialAttribute extends Attribute
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constant value used as one of possible results of {@link #getMaterialType()}
	 * This value indicates that there is no attached material stored in attribute 
	 */
	public static final int MATERIAL_NONE = 0;
	/**
	 * Constant value used as one of possible results of {@link #getMaterialType()}
	 * This value indicates that material exists and it is a file  
	 */
	public static final int MATERIAL_FILE = 1;
	/**
	 * Constant value used as one of possible results of {@link #getMaterialType()}
	 * This value indicates that material exists and it is a link to external resource 
	 */	
	public static final int MATERIAL_URL = 2;
	
	private int materialType;
	private String materialName;
	private int materialVersion;

	/**
	 * Gets type of material
	 * @return one of the following constants: {@link #MATERIAL_FILE}, 
	 * {@link #MATERIAL_NONE}, {@link #MATERIAL_URL}
	 */	public int getMaterialType() {
		return materialType;
	}

	/**
	 * Sets type of material
	 * @param materialType desired value of material type. Should be one of the following 
	 * constants: {@link #MATERIAL_FILE}, {@link #MATERIAL_NONE}, {@link #MATERIAL_URL}
	 */
	public void setMaterialType(int materialType) {
		this.materialType = materialType;
	}

	/**
	 * Gets name of material
	 * @return name of material
	 */
	public String getMaterialName() {
		return materialName;
	}

	/**
	 * Sets name of material
	 * @param materialName desired value of material name
	 */
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	
	public int getMaterialVersion() {
		return materialVersion;
	}
	
	public void setMaterialVersion(int materialVersion) {
		this.materialVersion = materialVersion;
	}

	/**
	 * Material attributes are not comparable, so this method always return false
	 * @return always returns false 
	 */
	public boolean equalValue(Attribute attr) {
		return false;
	}

	/**
	 * Return string representation of attribute value.
	 * In case of MaterialAttribute returns name of material
	 * @return name of material  
	 */
	public String getStringValue() {
		return materialName == null ? "" : materialName;
	}

	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_MATERIAL;
	}

	/**
	 * @see Attribute#verifyValue()
	 */
	public boolean verifyValue() {
		return true;
	}

	public boolean isEmpty() {
		return materialType == MATERIAL_NONE;
	}

	/**
	 * Sets material type to {@link #MATERIAL_NONE} and materialName to null
	 */
	public void clear() {
		setMaterialType(MATERIAL_NONE);
		setMaterialName(null);
	}

	@Override
	public void setValueFromAttribute(Attribute attr) {
		if(this.getClass().isAssignableFrom(attr.getClass())){
			MaterialAttribute material = (MaterialAttribute) attr;
			this.setMaterialName(material.getMaterialName());
			this.setMaterialType(material.getMaterialType());
			this.setMaterialVersion(material.getMaterialVersion());
		}
		
	}

	/*public boolean isReadOnly() {
		return true;
	}

	public void setReadOnly(boolean readOnly) {
		if (!readOnly)
			throw new IllegalArgumentException("Material attribute cannot be writable");
	}*/
}
