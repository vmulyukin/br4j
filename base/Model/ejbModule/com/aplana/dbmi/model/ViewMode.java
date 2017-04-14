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
 * Represent one record in VIEW_MODE table.
 * Define name (russian and english) for each view mode available in system  
 * @author vialeksandrov
 * @see DataObject
 */
public class ViewMode extends DataObject implements NamedObject{
	private static final long serialVersionUID = 1L;
	
	private String nameRu;
	private String nameEn;
	/**
	 * Returns russian name of view mode object
	 * @return russian name of view mode object
	 */
	public String getNameRu() {
		return nameRu;
	}
	/**
	 * Sets russian name of view mode object
	 * @param nameRus desired value
	 */
	public void setNameRu(String nameRus) {
		this.nameRu = nameRus;
	}
	/**
	 * Returns english name of view mode object
	 * @return english name of view mode object
	 */
	public String getNameEn() {
		return nameEn;
	}
	/**
	 * Sets english name of view mode object
	 * @param nameEng desired value
	 */
	public void setNameEn(String nameEng) {
		this.nameEn = nameEng;
	}
	/**
	 * Returns localized name of view mode object
	 * @return Returns value of {@link #nameRu} or {@link #nameEn} property depending of caller's locale preferences
	 */
	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

    public void setName(LocalizedString name) {
		nameRu = name == null ? null : name.getValueRu();
		nameEn = name == null ? null : name.getValueEn();
	}

	public void setId(ObjectId id) {
		super.setId(id);
		if (id instanceof ObjectIdAndName)
			setName(((ObjectIdAndName) id).getName());
	}
}
