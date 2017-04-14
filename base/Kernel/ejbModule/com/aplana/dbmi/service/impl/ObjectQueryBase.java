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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

/**
 * {@link QueryBase} descendant, base class for queries
 * used to process single {@link DataObject} instance.<br>
 * This class must be used as the base class for all queries used to
 * fetch/delete single object from database. 
 * @see DataServiceBean#getById(com.aplana.dbmi.service.User, ObjectId)
 * @see DataServiceBean#deleteObject(com.aplana.dbmi.service.User, ObjectId)
 */
public abstract class ObjectQueryBase extends QueryBase {
	private static final long serialVersionUID = 1L;
	
	private ObjectId id;

	/**
	 * Gets identifier of object being processed by query
	 * @return identifier of object being processed by query
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * Sets identifier of object being processed by query 
	 * @param id identifier of object being processed by query
	 */
	public void setId(ObjectId id) {
		this.id = id;
		if (getAccessChecker() != null)
			getAccessChecker().setObject(id);
	}

	/**
	 * @return identifier of object being processed by query
	 */
	final public ObjectId getEventObject() {
		return getId();
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash ^ ((id != null) ? id.hashCode() : 49874654);
		return hash;
	}
}
