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
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@ManagedResource(objectName="br4j:name=lockManagement", description="MBean for TimeoutLockManagement")
public class TimeoutLockManagementBean extends LockManagementBean {

	private long timeout = 3 * 1000; //3 sec to wait

	@Override
	public OperationResult acquireLock(ObjectId objectId, Person user, Integer session, long waitTimeout, Collection<ObjectId> queries) {
		if (objectId == null){
			if (logger.isWarnEnabled()){
				logger.warn(getCurrentStateString(null, null, user, session)+"\nINCONSISTENT_STATE\nOperation: acquireLock\nObjectId=null");
			}
			return OperationResult.INCONSISTENT_STATE;
		}
		
		Lock lock;
		synchronized (storage) {
			lock = storage.get(objectId);
			if (lock == null) {
				return createLock(storage, objectId, user, session);
			}
		}
		OperationResult res;
		boolean recursiveCall = false;
		synchronized (lock) {
			res = canLock(lock, user, session, queries);
			if (res == OperationResult.SUCCESS){
				lock.incLockCount();
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, objectId, user, session)+"\nSUCCESS\nOperation: acquireLock");
				}
			} else {
				long to = (waitTimeout < 0) ? timeout : waitTimeout;
				long start = System.currentTimeMillis();
				try {
					lock.wait(to);
				} catch (InterruptedException e) {
					logger.error(getCurrentStateString(lock, objectId, user, session)+"\nWaiting was suddenly interrupted");
				}

				long stop = System.currentTimeMillis() - start;
				if (stop >= to) {
					if (logger.isWarnEnabled()){
						logger.warn(getCurrentStateString(lock, objectId, user, session)+"\nDENIED (Timeout expired)\nOperation: acquireLock");
					}
					return OperationResult.DENIED;
				}

				// ��������� ���������� ����������� ����� ����� ����������
				recursiveCall = true;
			}
		} // sync block

		// ����������� ���� ��� ����������� ������
		if (recursiveCall) {
			res = acquireLock(objectId, user, session, waitTimeout, queries);
		}
		return res;
	}
	
	protected OperationResult createLock(Map<ObjectId, Lock> storage, ObjectId objectId, Person user, Integer session) {
		Lock lock = new Lock();
		lock.setObjectId(objectId);
		lock.setSessionId(session);
		if (isCurrentThreadIsService()){
			setLockInService(lock, user);
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, objectId, user, session)+"\nSUCCESS\nOperation: acquireLock:NEW service mode");
			}
		} else {
			lock.setCustomer(user);
			lock.setLockedBy(user);
			lock.markChanged();
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, objectId, user, session)+"\nSUCCESS\nOperation: acquireLock:NEW user mode");
			}
		}
		storage.put(objectId, lock);
		return OperationResult.NEW;
	}
	
	@Override
	public synchronized OperationResult releaseLocks(Person executor) {
		Long currentThreadId = Thread.currentThread().getId();
		synchronized (storage) {
			for (Iterator<Map.Entry<ObjectId, Lock>> itr =  storage.entrySet().iterator(); itr.hasNext();){
				Map.Entry<ObjectId, Lock> entry = itr.next(); 
				Lock lock = entry.getValue();
				if (currentThreadId.equals(lock.getLockerThreadId())){ // ���������� ������� � ���� ������
					if (isLockIntercepted(entry.getValue())){		// ���������� ������-�� � ��������� ���������
						if (logger.isWarnEnabled()){
							logger.warn(getCurrentStateString(lock, entry.getKey(), executor)+"\nINCONSISTENT_STATE\nOperation: releaseLocks");
						}
						continue;
					}
					if (lock.getCustomer() == null){ 	// ��������� ���� ���� ����������
						if (lock.getLockCount() < 0 && lock.getLockCount() > 1) {
							if (logger.isWarnEnabled()){
								logger.warn(getCurrentStateString(lock, entry.getKey(), executor)+"\nDENIED\nOperation: releaseLocks: INCONSISTENT STATE (lock count: " + lock.getLockCount() + ")");
							}
							return OperationResult.INCONSISTENT_STATE;
						}
						lock.decLockCount();
						if (lock.getLockCount() <= 0){
							if (logger.isDebugEnabled()){
								logger.debug(getCurrentStateString(lock, entry.getKey(), executor)+"\nSUCCESS\nOperation: releaseLocks:REMOVE");
							}
							itr.remove();						// ������� ����������
							synchronized (lock) {
								lock.notifyAll();
							}
						} else {			// ��������� � "���������������� �����", ������ ������ ���������� ��� CreateCard
							lock.setCustomer(lock.getExecutor());
							lock.setExecutor(null);
							lock.setLockerThreadId(null);
							lock.setQueryId(null);
							lock.markChanged();
							if (logger.isDebugEnabled()){
								logger.debug(getCurrentStateString(lock, entry.getKey(), executor)+"\nSUCCESS\nOperation: releaseLocks: switch to service mode");
							}
						}
					} else {							// ��������� �� ���� ���� ����������
						lock.setExecutor(null);
						lock.setLockerThreadId(null);
						lock.setLockedBy(lock.getCustomer());	// ������� �������, ��� ��� ���������� �� ������������
						lock.decLockCount();
						lock.setQueryId(null);
						lock.markChanged();
						if (logger.isDebugEnabled()){
							logger.debug(getCurrentStateString(lock, entry.getKey(), executor)+"\nSUCCESS\nOperation: releaseLocks: switch to user mode");
						}
					}
				}
			}
			inService.remove();
			return OperationResult.RELEASED;
		}
	}

	@Override
	@ManagedAttribute(description="Get default timeout in milliseconds")
	public long getTimeout() {
		return timeout;
	}

	@Override
	@ManagedAttribute(description="Set default timeout in milliseconds")
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
