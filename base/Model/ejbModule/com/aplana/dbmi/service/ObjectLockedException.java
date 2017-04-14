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
import com.aplana.dbmi.model.Person;

/**
 * Exception thrown when user tries to change {@link com.aplana.dbmi.model.LockableObject}
 * locked by another user
 */
public class ObjectLockedException extends DataException
{
	private static final long serialVersionUID = 1L;
	private static final String MESSAGE_ID = "general.locked";
	private static final String MESSAGE_EXT_ID = "general.locked.ext.info";
	private ObjectId id;
	private Person locker;
	private String info;
	
	/**
	 * Creates new exception object with information about object and it's locker.
	 * @param id identifier of {@link com.aplana.dbmi.model.LockableObject}
	 * @param locker {@link com.aplana.dbmi.model.Person} who locked target object
	 */
	public ObjectLockedException(ObjectId id, Person locker, String info)
	{
		super(info == null ? MESSAGE_ID : MESSAGE_EXT_ID, new Object[] { id.getId(), locker.getFullName() == null ? "JBoss Referent System" : locker.getFullName(), info });
		this.id = id;
		this.locker = locker;
		this.info = info;
	}

	/**
	 * Gets identifier of locked object
	 * @return identifier of locked object
	 */
	public ObjectId getObjectId() {
		return id;
	}

	/**
	 * Gets {@link com.aplana.dbmi.model.Person} object representing user,
	 * who locked object identified by {@link #getObjectId()}
	 * @return person who locked object identified by {@link #getObjectId()}
	 */
	public Person getLocker() {
		return locker;
	}
	
	/**
	 * Gets additional info that were received from Lock object
	 * @return additional information about locked object
	 */
	public String getInfo() {
		return info;
	}
}
