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
 * {@link Action} implementation used to unlock 
 * given {@link com.aplana.dbmi.model.LockableObject} instance.
 * <br>
 * This action must be performed after other actions changing this 
 * {@link com.aplana.dbmi.model.LockableObject}.  
 * <br>
 * Execution of this action unlocks object and sets it's locker property to null,
 * or throws {@link ObjectLockedException} if given object is not locked or
 * if it is locked by another user.
 * <br>
 * Returns null as result
 */
public class UnlockObject implements ObjectAction<Void> {
	private static final long serialVersionUID = 1L;
	private ObjectId id;
	private boolean forceUnlock;
	
	/**
	 * Default constructor
	 */
	public UnlockObject() {
	}
	
	/**
	 * Creates new instance of {@link UnlockObject} and initializes its 
	 * {@link #setId(ObjectId) id} property with given value. 
	 * @param id Identifier of {@link LockableObject} to be unlocked
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */	
	public UnlockObject(ObjectId id) {
		setId(id);
		setForceUnlock(false);
	}
	
	/**
	 * Creates new instance of {@link UnlockObject} and initializes its 
	 * {@link #setId(ObjectId) id} property with given value. 
	 * @param id Identifier of {@link LockableObject} to be unlocked
	 * @param forceUnlock mark that must be release all locks 
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */	
	public UnlockObject(ObjectId id, boolean forceUnlock) {
		setId(id);
		setForceUnlock(forceUnlock);
	}

	/**
	 * Creates new instance of {@link UnlockObject} and initializes its 
	 * {@link #setId(ObjectId) id} property with identifier of given {@link LockableObject} 
	 * @param obj {@link LockableObject} to be unlocked
	 */	
	public UnlockObject(LockableObject obj) {
		this.id = obj.getId();
	}
	
	/**
	 * Gets identifier of {@link LockableObject} to be unlocked
	 * @return identifier of {@link LockableObject} to be unlocked
	 */
	public ObjectId getId() {
		return id;
	}
	
	/**
	 * Sets identifier of {@link LockableObject} to be unlocked
	 * @param id identifier of {@link LockableObject} to be unlocked
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link LockableObject} identifier 
	 */
	public void setId(ObjectId id) {
		if (!LockableObject.class.isAssignableFrom(id.getType()))
			throw new IllegalArgumentException("Object should be lockable");
		this.id = id;
	}

	public boolean isForceUnlock() {
		return forceUnlock;
	}

	public void setForceUnlock(boolean forceUnlock) {
		this.forceUnlock = forceUnlock;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return null;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getId();
	}
}
