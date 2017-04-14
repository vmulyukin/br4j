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

import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * @link Query used to perform {@link CheckLock} action
 */
public class DoCheckLock extends DoLock {

	private static final long serialVersionUID = 1L;

	/**
	 * Return identifier of processed object
	 * 
	 * @return identifier of object being checked
	 */
	public ObjectId getEventObject() {
		return ((CheckLock) getAction()).getId();
	}

	@Override
	public Object processQuery() throws DataException {
		ObjectId id = ((CheckLock) getAction()).getId();
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		OperationResult res = storage.isLockedByUser(id, getUser().getPerson(), getSessionId(), getLinkedQueriesIds());
		switch (res){
			case NOT_EXISTS: {
				throw new ObjectNotLockedException(id);
			}
			case DENIED: {
				throw new ObjectLockedException(id, storage.getLocker(id), getLockInfoFromQuery(id));
			}
		}
		
		return null;
	}
}
