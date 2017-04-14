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
package com.aplana.dbmi.action;

import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used to check whether given {@link com.aplana.dbmi.model.LockableObject} instance is locked
 * or not.
 * <br> 
 * If object is locked by different user executor will raise {@link com.aplana.dbmi.service.ObjectLockedException}.
 * <br>
 * If object is not locked by current user executor will raise {@link com.aplana.dbmi.service.ObjectNotLockedException}.
 * <br>
 * Additionally execution of this action could fail with {@link com.aplana.dbmi.service.DataException}
 * <br> 
 * Returns null as result 
 *
 */
public class CheckLock implements ObjectAction<Void> {
	private static final long serialVersionUID = 1L;
	protected ObjectId id;
	
	/**
	 * Default constructor
	 */
	public CheckLock() {}
	
	/**
	 * Creates new instance of {@link CheckLock} and initializes its 
	 * {@link #setId(ObjectId) id} property with given value. 
	 * @param id Identifier of {@link LockableObject} to be checked
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */
	public CheckLock(ObjectId id) {
		setId(id);
	}

	/**
	 * Creates new instance of {@link CheckLock} and initializes its 
	 * {@link #setId(ObjectId) id} property with identifier of given {@link LockableObject} 
	 * @param obj {@link LockableObject} to be checked
	 */
	public CheckLock(LockableObject obj) {
		this.id = obj.getId();
	}

	/**
	 * Gets identifier of {@link LockableObject} to be checked
	 * @return identifier of {@link LockableObject} to be checked
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * Sets identifier of {@link LockableObject} to be checked
	 * @param id identifier of {@link LockableObject} to be checked
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */
	public void setId(ObjectId id) {
		if (!LockableObject.class.isAssignableFrom(id.getType()))
			throw new IllegalArgumentException("Object should be lockable");
		this.id = id;
	}
	
	/**
	 * @see Action#getObjectId()
	 */
	@Override
	public ObjectId getObjectId() {
		return id;
	}

	/**
	 * @see Action#getResultType()
	 */
	@Override
	public Class<?> getResultType() {
		return null;
	}

}
