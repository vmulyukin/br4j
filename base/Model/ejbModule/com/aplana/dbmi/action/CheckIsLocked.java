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
import com.aplana.dbmi.service.ObjectLockedException;

/**
 * {@link Action} implementation used to check locking the object 
 * given {@link com.aplana.dbmi.model.LockableObject} instance.
 * <br> 
 * This action can be performed before any other action that will lock object 
 * {@link com.aplana.dbmi.model.LockableObject}.  
 * <br>
 * Execution of this action is checking object for locker property of current user,
 * return true if given object is already locked by another user or return false if given object is not locked by any users.
 * <br>
 * Returns boolean as Object
 */
public class CheckIsLocked implements ObjectAction<Boolean> {
	private static final long serialVersionUID = 3L;
	private ObjectId id;
	private long waitTimeout = -1;
	
	/**
	 * Default constructor
	 */
	public CheckIsLocked() {}

	/**
	 * Creates new instance of {@link CheckIsLock} and initializes its 
	 * {@link #setId(ObjectId) id} property with given value. 
	 * @param id Identifier of {@link LockableObject} to be checked
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */
	public CheckIsLocked(ObjectId id) {
		setId(id);
	}
	
	/**
	 * Creates new instance of {@link CheckIsLocked} and initializes its 
	 * {@link #setId(ObjectId) id} property with identifier of given {@link LockableObject} 
	 * @param obj {@link LockableObject} to be checked
	 */
	public CheckIsLocked(LockableObject obj) {
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
		if (!LockableObject.class.isAssignableFrom(id.getType())) throw new IllegalArgumentException("Object should be lockable");
		this.id = id;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return Boolean.class;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getId();
	}

	public long getWaitTimeout() {
		return waitTimeout;
	}

	public void setWaitTimeout(long waitTimeout) {
		this.waitTimeout = waitTimeout;
	}

}