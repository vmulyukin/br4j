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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * @link Query used to perform {@link LockObject} action 
 */
public class DoLock extends LockQueryBase {
	
	private static final long serialVersionUID = 2L;  // Database-less version of DoUnlock

	public String getEvent() {
		return null;	//***** No need to log?
	}

	/**
	 * Return identifier of processed object
	 * @return identifier of object being locked
	 */
	public ObjectId getEventObject() {
		return ((LockObject) getAction()).getId();
	}

	/**
	 * Locks given object
	 * @return null
	 * @throws RuntimeException if given object is not lockable
	 * @throws ObjectLockedException if object is already locked by different user
	 * @throws DataException if given object identifier doesn't specify existing 
	 * object in database
	 */
	public Object processQuery() throws DataException	{
		LockObject lo = getAction();
		ObjectId id = lo.getId();
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);

		OperationResult res = storage.acquireLock(id, getUser().getPerson(), getSessionId(), lo.getWaitTimeout(), getLinkedQueriesIds());
		switch (res){
			case DENIED: {
				throw new ObjectLockedException(id, storage.getLocker(id), getLockInfoFromQuery(id));
			}
		}
		return null;
	}
}
