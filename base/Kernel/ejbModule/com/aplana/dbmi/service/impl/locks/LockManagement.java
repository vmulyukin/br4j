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
package com.aplana.dbmi.service.impl.locks;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.Lock;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.LockInfo;

import java.util.Collection;
import java.util.List;

/**
 * This storage work with program locks to {@link com.aplana.dbmi.model.Card
 * cards}
 * 
 */
public interface LockManagement {
	enum OperationResult {
		NEW, EXISTS, NOT_EXISTS, RELEASED, DENIED, INCONSISTENT_STATE, SUCCESS
	}
	
	OperationResult canLock(ObjectId objectId, Person user, Integer session);
	
	OperationResult canLock(ObjectId objectId, Person user, Integer session, Collection<ObjectId> queries);
	
	OperationResult isLockedByUser(ObjectId objectId, Person user, Integer session);
	
	OperationResult isLockedByUser(ObjectId objectId, Person user, Integer session, Collection<ObjectId> queries);

	OperationResult acquireLock(ObjectId objectId, Person user, Integer session, long waitTimeout);
	
	OperationResult releaseLock(ObjectId objectId, Person user, Integer session);
	
	OperationResult tryRemoveLock(ObjectId objectId, Person user, Integer session);

	OperationResult interceptLock(QueryBase query, Person owner, Person executor);
	
	OperationResult catchLock(ObjectId queryId);
	
	OperationResult releaseLocks(Person executor);
	
	/**
	 * Returns customer id (user who locked this card)
	 * 
	 * @param id
	 *            Card objectId
	 * @return customer id
	 */
	Person getLocker(ObjectId id);

	/**
	 * Returns Lock object by locked Card objectId and user
	 * 
	 * @param objectId card's id
	 * @return null if card not locked or locked by other
	 */
	Lock getLockByObject(ObjectId objectId);
	
	LockInfo getLockInfoByObject(ObjectId objectId);
	
	List<LockInfo> getLockInfosByCustomer(Person customer, Integer sess);
	
	List<ObjectId> getCardIdsOnService();
}
