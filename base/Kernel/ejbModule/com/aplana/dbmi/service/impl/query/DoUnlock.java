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

import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * Query used to perform {@link UnlockObject} action
 */
public class DoUnlock extends LockQueryBase {
	
	private static final long serialVersionUID = 3L;  // Database-less version of DoUnlock
	
	public String getEvent() {
		return null;	//***** No need to log?
	}

	/**
	 * @return identifier of object being unlocked
	 */
	public ObjectId getEventObject() {
		return ((UnlockObject) getAction()).getId();
	}

	/**
	 * Unlocks object with given identifier
	 * @return null
	 * @throws RuntimeException if given identifier represents object which is not lockable
	 * @throws ObjectNotLockedException if object identified by given identifier is not locked
	 * @throws DataException if given identifier is not present in database 
	 */
	public Object processQuery() throws DataException {
		ObjectId id = ((UnlockObject) getAction()).getId();
		boolean forceUnlock = ((UnlockObject) getAction()).isForceUnlock();	// ������� ��� ����������
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		OperationResult res = OperationResult.NOT_EXISTS;
		
		// � ������� ���������� ������ ���������� ��������� Uid'�� ��������� query
		if (forceUnlock){	// ������� ��� ����������
			res = storage.tryRemoveLock(id, getUser().getPerson(), getSessionId());
		} else {	// ����� ������ ����
			res = storage.releaseLock(id, getUser().getPerson(), getSessionId(), getLinkedQueriesIds());
		}
		switch (res){
			case DENIED: {
				throw new ObjectLockedException(id, storage.getLocker(id), getLockInfoFromQuery(id));
			}
			case NOT_EXISTS:{
				throw new ObjectNotLockedException(id);
			}
		}
		return null;
	}
}
