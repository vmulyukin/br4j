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

import java.util.Date;

/**
 * Lockable object is a DataObject that could be 'locked' by user.
 * It is not possible to edit locked object from anyone except of locker.
 * Hence to edit lockable object it is neccessary to
 * {@link com.aplana.dbmi.action.LockObject lock} it first. 
 * <br> 
 * Supplement DataObject with information about locker and time of lock. 
 */
abstract public class LockableObject extends DataObject
{
	private static final long serialVersionUID = 1L;
	
	private ObjectId locker;
	private Date lockTime;

	/**
	 * Returns time of lock execution
	 * @return time of lock
	 */
	public Date getLockTime() {
		return lockTime;
	}

	/**
	 * Returns identifier of {@link Person} who locked this object
	 * or null if object is unlocked
	 * @return identifier of locker or null if object is unlocked
	 */
	public ObjectId getLocker() {
		return locker;
	}

	/**
	 * Checks is object is locked
	 * @return true if object is locked or false otherwise
	 */
	public boolean isLocked() {
		return locker != null;
	}

	/**
	 * Sets identifier of locker.
	 * locker param should be an identifier of {@link Person} class 
	 * @param locker identifier of {@link Person} who locked this object
	 */
	public void setLocker(ObjectId locker) {
		if (locker != null && !Person.class.equals(locker.getType()))
			throw new IllegalArgumentException("Object can be locked only by Person");
		this.locker = locker;
	}
	
	/**
	 * Sets locker to given value
	 * @param locker long identifier of {@link Person} object
	 */
	public void setLocker(long locker) {
		this.locker = new ObjectId(Person.class, locker);
	}

	/**
	 * Sets time of lock execution
	 * @param lockTime time of lock execution
	 */
	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}
}
