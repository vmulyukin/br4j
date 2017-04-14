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

public abstract class NamedLockableObject extends LockableObject implements NamedObject {
	
	protected LocalizedString name;

	public void setId(ObjectId id) {
		super.setId(id);
		if (id instanceof ObjectIdAndName)
			setName(((ObjectIdAndName) id).getName());
	}

	public void setName(LocalizedString name) {
		this.name = name;
	}
	
	/**
	 * Returns english name of the object
	 * @return english name of the object
	 */
	public String getNameEn() {
		return name == null ? null : name.getValueEn();
	}
	
	/**
	 * Sets english name of the object
	 * @param nameEn desired value of english name
	 */	
	public void setNameEn(String nameEn) {
		if (name == null)
			name = new LocalizedString();
		name.setValueEn(nameEn);
	}
	
	/**
	 * Returns russian name of the object
	 * @return russian name of the object
	 */	
	public String getNameRu() {
		return name == null ? null : name.getValueRu();
	}
	
	/**
	 * Sets russian name of the object
	 * @param nameRu desired value of russian name
	 */	
	public void setNameRu(String nameRu) {
		if (name == null)
			name = new LocalizedString();
		name.setValueRu(nameRu);
	}
	
	// Function getName() couldn't be defined in this class because currently it returns String in some (older) objects,
	// while in other (newer) objects the same function returns LocalizedString.
	//TODO This should be changed in future!
}
