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
package com.aplana.dbmi.service;

import com.aplana.dbmi.model.ObjectId;

/**
 * Exception thrown when user tries to change {@link com.aplana.dbmi.model.LockableObject}
 * which is not locked by him, or when user tries to unlock {@link com.aplana.dbmi.model.LockableObject}
 * which is not locked by him.<br>
 * Shows message with information about object caused error.
 */
public class ObjectNotLockedException extends DataException
{
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE_ID = "general.notlocked";
	private static final String SECOND_MESSAGE_ID = "general.notlocked.by.you";
	private ObjectId id;
	private String fullName;

	/**
	 * Creates new ObjectNotLockedException instance
	 * @param id identifier of object caused error
	 */
	public ObjectNotLockedException(ObjectId id)
	{
		super(MESSAGE_ID, new Object[] { id.getId() });
		this.id = id;
	}
	/**
	 * Creates new ObjectNotLockedException instance
	 * @param id identifier of object caused error
	 * @param locker who lock this object
	 */
	public ObjectNotLockedException(ObjectId id, String locker)
	{
		super(SECOND_MESSAGE_ID, new Object[] { id.getId(), locker });
		this.id = id;
	}

	/**
	 * Returns identifier of object which caused error
	 * @return identifier of object which caused error
	 */
	public ObjectId getObjectId() {
		return id;
	}
	
	/**
	 * Returns who lock this object
	 * @return who lock this object
	 */
	public String getFullName() {
		return fullName;
	}
	
}
