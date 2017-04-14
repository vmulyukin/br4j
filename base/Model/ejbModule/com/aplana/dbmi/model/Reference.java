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

/**
 * This class represent dictionaries defined in database (REFERENCE_LIST table in database)
 * Dictionary is a named set of string {@link ReferenceValue values} with unique identifiers.
 * Each dictionary have unique identifier and human-readable description.
 */
public class Reference extends DataObject
{
	/**
	 * Identifier for regions dictionary
	 */
	public static final ObjectId ID_REGION = new ObjectId(Reference.class, "REGION");
	/**
	 * Identifier for departments dictionary
	 */
	public static final ObjectId ID_DEPARTMENT = new ObjectId(Reference.class, "DEPARTMENT");
	/**
	 * Identifier for positions dictionary
	 */
	public static final ObjectId ID_POSITION = new ObjectId(Reference.class, "POSITION");
	
	private static final long serialVersionUID = 1L;
	private String description;
	private Collection values;
	
	/**
	 * Sets identifier of reference object
	 * @param id desired value of identifier
	 */
	public void setId(String id) {
		super.setId(id == null ? null : new ObjectId(Reference.class, id));
	}

	/**
	 * Gets description of dictionary
	 * @return description of dictionary
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Gets collection of values included in this dictionary
	 * @return collection of {@link ReferenceValue} objects comprising this dictionary
	 */
	public Collection getValues() {
		return values;
	}

	/**
	 * Sets dictionary description
	 * @param description desired value of dictionary description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets set of values comprising this dictionary
	 * @param values collection of {@link ReferenceValue} objects
	 */
	public void setValues(Collection values) {
		this.values = values;
	}
}
