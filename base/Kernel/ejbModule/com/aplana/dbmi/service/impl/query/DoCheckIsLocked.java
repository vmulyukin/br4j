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

import com.aplana.dbmi.action.CheckIsLocked;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * @link Query used to perform {@link CheckIsLocked} action
 */
public class DoCheckIsLocked extends DoLock {

	private static final long serialVersionUID = 1L;

	/**
	 *  Return boolean object identify result of checking for object locking 
	 * @return boolean object identify result of checking for object locking
	 */
	public ObjectId getEventObject() {
		return ((CheckIsLocked) getAction()).getId();
	}

	@Override
	public Boolean processQuery() throws DataException {
		CheckIsLocked lo = getAction();
		ObjectId id = lo.getId();
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		OperationResult result = storage.acquireLock(id, getUser().getPerson(), getSessionId(), lo.getWaitTimeout(), getLinkedQueriesIds());
		switch (result) {
			case NOT_EXISTS:
			case DENIED:
			case INCONSISTENT_STATE:
				return false;

			default:
				return true;
		}
	}
}