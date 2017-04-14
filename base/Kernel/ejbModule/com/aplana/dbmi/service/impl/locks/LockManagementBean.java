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

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.async.AsyncManager;
import com.rits.cloning.Cloner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ���������:<br>
 * ���������� ����� ������ ������ ������������ �� ������, �������� ����������� ����������, 
 * �� �����������: ���������� ����������� (������� ������ ������� �� ������������).<br>
 * ������������� ����� ������ ������ ������������ �� ������, �������� ����������� ����������.
 * ������������� ����� ������������ �� ������� ������, ���� ���� ����� ��������������� ���������� (������������ �� id ������). 
 * ������������� ����������: �� ��������� ������������ ��������� ������������� �� ����������.
 * ��������� ������������� ����������� � �������� ���������� ������� ������������ ���������� ����������
 * ����������� ������������ � ����� ������ ������������. 
 * �� ������ ���������� ����� ���� ����������� ����� �������: ���� id ������ ��������� �� ������ � �� ���������
 * � id ������ ������������, �� ���������� �������.
 */
public class LockManagementBean implements LockManagementMBean, LockManagementSPI {

	protected static Log logger = LogFactory.getLog(LockManagementBean.class);

	public static final Person SYSTEM_EXECUTOR_ID = DataObject.createFromId(Person.ID_SYSTEM);;

	protected Map<ObjectId, Lock> storage = new HashMap<ObjectId, Lock>();
	protected ThreadLocal<Boolean> inService = new ThreadLocal<Boolean>();

	public static class Lock implements Serializable {
		private static final long serialVersionUID = 4L;
		private ObjectId objectId;
		private Person customer;
		private Person executor;
		private Person lockedBy;
		private Long lockerThreadId;
		private Date created;
		private Date changed;
		private int lockCount;
		private Integer sessionId;
		private transient ObjectId queryId;
		
		/**
		 * �������� ���������� ��� ����������� ����������.
		 */
		Lock() {
			created = new Date();
			lockCount = 1;
		}

		ObjectId getObjectId() {
			return objectId;
		}

		void setObjectId(ObjectId objectId) {
			this.objectId = objectId;
		}

		Person getCustomer() {
			return customer;
		}

		void setCustomer(Person customerId) {
			this.customer = customerId;
		}

		Person getExecutor() {
			return executor;
		}

		void setExecutor(Person executorId) {
			this.executor = executorId;
		}

		Person getLockedBy() {
			return lockedBy;
		}

		void setLockedBy(Person lockedBy) {
			this.lockedBy = lockedBy;
		}

		Long getLockerThreadId() {
			return lockerThreadId;
		}

		void setLockerThreadId(Long lockerThreadId) {
			this.lockerThreadId = lockerThreadId;
		}

		void markChanged(){
			changed = new Date();
		}

		Date getCreated() {
			return created;
		}

		void setCreated(Date created) {
			this.created = created;
		}

		Date getChanged() {
			return changed;
		}

		void setChanged(Date changed) {
			this.changed = changed;
		}
		
		int incLockCount(){
			return ++lockCount;
		}
		
		int decLockCount(){
			return --lockCount;
		}
		
		private void setLockCount(int count){
			lockCount = count;
		}
		
		int getLockCount(){
			return lockCount;
		}
		
		ObjectId getQueryId() {
			return queryId;
		}

		void setQueryId(ObjectId queryId) {
			this.queryId = queryId;
		}
		
		Integer getSessionId() {
			return sessionId;
		}
		
		void setSessionId(Integer sessionId) {
			this.sessionId = sessionId;
		}

		public String toString(){
			return "ObjectId:\t"+getObjectId()+
					"\nSessionId:\t"+getSessionId()+
					"\nThreadId:\t"+getLockerThreadId()+
					"\ncustomerId:\t"+personToString(getCustomer())+
					"\nexecutorId:\t"+personToString(getExecutor())+
					"\nlockedBy:\t"+personToString(getLockedBy())+
					"\nqueryId:\t"+getQueryId()+
					"\ncreated:\t"+getCreated()+
					"\nchanged:\t"+getChanged()+
					"\nlockCount:\t"+lockCount;
		}
		
		/**
		 * ������������ ������� {@link Lock}.
		 */
		public Lock clone() {
			return new Cloner().deepClone(this);
		}
	}

	/**
	 * ������������ ���������� �� ��������� ������ ���������������� ������� {@link LockManagementBean.Lock}.
	 * ������������ ��� ������������� �� ��������� {@link LockManagementBean}.
	 */
	public class LockInfo{
		private ObjectId objectId;
		private ObjectId customer;
		private ObjectId executor;
		private ObjectId lockedBy;
		private ObjectId queryId;
		private Date created;
		private Date changed;
		private int lockCount;
		private boolean inService = false;
		private Integer sessionId;
		
		public LockInfo(Lock lock) throws IllegalArgumentException{
			if (lock == null){
				return;
			}
			if (lock.getObjectId() == null || lock.getObjectId().getId() == null){
				throw new IllegalArgumentException("Illegal lock object id.");
			}
			this.objectId = new ObjectId(lock.getObjectId().getType(), lock.getObjectId().getId());
			
			if (lock.getCustomer() != null){
				customer = new ObjectId(lock.getCustomer().getId().getType(), lock.getCustomer().getId().getId());
			}
			if (lock.getExecutor() != null){
				executor = new ObjectId(lock.getExecutor().getId().getType(), lock.getExecutor().getId().getId());
			}
			if (lock.getLockedBy() != null){
				lockedBy = new ObjectId(lock.getLockedBy().getId().getType(), lock.getLockedBy().getId().getId());
			}
			if (lock.getCreated() != null){
				created = (Date)lock.getCreated().clone();
			}
			if (lock.getChanged() != null){
				created = (Date)lock.getChanged().clone();
			}
			if (lock.getSessionId() != null){
				sessionId = lock.getSessionId();
			}
			if (lock.getQueryId() != null){
				queryId = new ObjectId(lock.getQueryId().getType(), lock.getQueryId().getId());
			}
			lockCount = lock.getLockCount();
			inService = isLockInService(lock) || isLockIntercepted(lock);
		}

		public ObjectId getObjectId() {
			return objectId;
		}
		
		public ObjectId getCustomer() {
			return customer;
		}

		public ObjectId getExecutor() {
			return executor;
		}

		public ObjectId getLockedBy() {
			return lockedBy;
		}

		public Date getCreated() {
			return created;
		}

		public Date getChanged() {
			return changed;
		}

		public int getLockCount() {
			return lockCount;
		}

		public boolean isInService() {
			return inService;
		}
		
		public Date getLockDate() {
			return changed;
		}
		
		public Integer getSessionId() {
			return sessionId;
		}
		
		public ObjectId getQueryId() {
			return queryId;
		}
	}

	@Override
	public OperationResult canLock(ObjectId objectId, Person user, Integer sessionId) {
		return canLock(objectId, user, sessionId, null);
	}
	
	@Override
	public OperationResult canLock(ObjectId objectId, Person user, Integer sessionId, Collection<ObjectId> queries) {
		if (objectId == null){
			if (logger.isWarnEnabled()){
				logger.warn(getCurrentStateString(null, null, user, sessionId)+"\nINCONSISTENT_STATE\nOperation: canLock\nObjectId=null");
			}
			return OperationResult.INCONSISTENT_STATE;
		}
		
		Lock lock = storage.get(objectId);
		return canLock(lock, user, sessionId, queries);
	}
	
	protected OperationResult canLock(Lock lock, Person user, Integer sessionId, Collection<ObjectId> queries) {
		if (lock == null){
			return OperationResult.SUCCESS;
		}

		Long currentThreadId = Thread.currentThread().getId();
		
		// ������ ���������� �� ������������
		// ��� ���������� �������� (��������� ��� �������, �.�. ����� ��������� ������ � ���� ���������)
		if (isLockInService(lock) || isLockIntercepted(lock)){
			if (isCurrentThreadIsService()){ 	// ������ �� ���������� �� ������ ������������
				if (currentThreadId.equals(lock.getLockerThreadId())){  // ������� ����� ����������� ��� ����������
					return OperationResult.SUCCESS;  // ���������
				} else {	// ������ ����� ����������� ��� ����������
					// ���� ��� intercept ���������� �� query �� �������� (�� ������ 'queries'), �� ���������
					if (isLockIntercepted(lock) && queries != null && queries.contains(lock.queryId)) {
						return OperationResult.SUCCESS;  // ���������
					}
					return OperationResult.DENIED;	// ������, ���������� �� ������������ ������ �������
				}
			} else {							// ������ �� ���������� �� �� ������ ������������
				return OperationResult.DENIED;	// ������, ���������� �� ������������ (��� ����. ������������)
			}
		} else { // ������ ���������� �� �� ������������
			if (lock.getCustomer() == null) {	//������-�� ��� ���������
				return OperationResult.INCONSISTENT_STATE;
			}
			if (user.getId().equals(lock.getCustomer().getId())){ // ���������� ������� �������������
				if (sessionId != null) {
					if (sessionId.equals(lock.getSessionId())) {
						return OperationResult.SUCCESS;  // ��������� �������� �����������
					} else {
						return OperationResult.DENIED; //��������� ���� �� ������ ������
					}
				}
				return OperationResult.DENIED; //��������� ���� �� ����������� ������
			} else {	// ���������� �� ������� �������������
				return OperationResult.DENIED;	// ������, ���������� ��������� ������ ������������� 
			}
		}
	}
	
	@Override
	public OperationResult isLockedByUser(ObjectId objectId, Person user, Integer sessionId) {
		return isLockedByUser(objectId, user, sessionId, null);
	}

	@Override
	public OperationResult isLockedByUser(ObjectId objectId, Person user, Integer sessionId, Collection<ObjectId> queries) {
		Lock lock = storage.get(objectId);
		if (lock == null){
			return OperationResult.NOT_EXISTS;
		}
		
		return canLock(objectId, user, sessionId, queries);
	}
	
	@Override
	public synchronized OperationResult acquireLock(ObjectId objectId, Person user, Integer session, long timeout) {
		return acquireLock(objectId, user, session, timeout, null);
	}
	
	@Override
	public synchronized OperationResult acquireLock(ObjectId objectId, Person user, Integer session, long timeout, Collection<ObjectId> queries) {
		if (objectId == null){
			if (logger.isWarnEnabled()){
				logger.warn(getCurrentStateString(null, null, user, session)+"\nINCONSISTENT_STATE\nOperation: acquireLock\nObjectId=null");
			}
			return OperationResult.INCONSISTENT_STATE;
		}
		
		Lock lock = storage.get(objectId);
		if (lock == null){
			lock = new Lock();
			lock.setObjectId(objectId);
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
			synchronized (storage) {
				storage.put(objectId, lock);
			}
			return OperationResult.NEW;
		}
		OperationResult res = canLock(lock, user, session, null);
		if (res == OperationResult.SUCCESS){
			lock.incLockCount();
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, objectId, user, session)+"\nSUCCESS\nOperation: acquireLock");
			}
		} else {
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, objectId, user, session)+"\nDENIED\nOperation: acquireLock");
			}
		}
		return res;
	}
	
	@Override
	public synchronized OperationResult releaseLock(ObjectId objectId, Person user, Integer sessionId) {
		return releaseLock(objectId, user, sessionId, null);
	}
	
	@Override
	public synchronized OperationResult releaseLock(ObjectId objectId, Person user, Integer sessionId, Collection<ObjectId> queries) {
		Lock lock = storage.get(objectId);
		Long currentThreadId = Thread.currentThread().getId();

		if (lock == null){				//���������� ��� ����� ������� ���
			return OperationResult.NOT_EXISTS;
		}
		
		if (isLockInService(lock) || isLockIntercepted(lock)){ 		// ������ ���������� �� ������������
			if (currentThreadId.equals(lock.getLockerThreadId())){  // ������� ����� ����������� ��� ����������
				lock.decLockCount();
				lock.markChanged();
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nSUCCESS\nOperation: releaseLock:service mode");
				}
				return OperationResult.SUCCESS;  // ��������� (������� ��������� � ����� ������������)
			} else {	// ������ ����� ����������� ��� ����������
				// intercept ���������� �� query �� ��������
				// �� �������� ��� ��� ���������� � ������� ��������� queries - ������ Uid ��������� query 
				if (isLockIntercepted(lock) && queries != null && queries.contains(lock.queryId)) {
					lock.decLockCount();
					lock.markChanged();
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nSUCCESS\nOperation: releaseLock:service mode (linked intercepted lock)");
					}
					return OperationResult.SUCCESS; // ���������
				}
				if (user.getId().equals(lock.getCustomer().getId())){  // ��� ���������� ����������� ������� ������������
					lock.setCustomer(null);		// ������� ���������� ���. ������������
					lock.decLockCount();
					lock.markChanged();
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nSUCCESS\nOperation: releaseLock:service mode");
					}
					return OperationResult.SUCCESS; // ���������
				} else {
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nDENIED\nOperation: releaseLock\nLock in service");
					}
					return OperationResult.DENIED;	// ������, ���������� �� ������������, 
													// ����������� �� ���� ������������ (��� ����. ������������)
				}
			}
		} else { // ������ ���������� �� �� ������������
			if (user.getId().equals(lock.getCustomer().getId())){ // ���������� ������� �������������
				if (sessionId != null && !sessionId.equals(lock.getSessionId())) { //����������� � ������ ������
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nDENIED\nOperation: releaseLock\nLocked by this user in other session");
					}
					return OperationResult.DENIED;
				} else { //����������� � ��� �� ������, ���� ������ �� ������ (�� ��������� �� � ������� ����)
					lock.decLockCount();
					lock.markChanged();
					if (lock.getLockCount() <= 0){			// �������� ��������� ����������
						synchronized (storage) {
							storage.remove(objectId);			// ������� ����������
						}
						synchronized (lock) {
							lock.notifyAll();
						}
						if (logger.isDebugEnabled()){
							logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nSUCCESS\nOperation: releaseLock:REMOVE");
						}
					}
					return OperationResult.SUCCESS;  	// ���������
				}
			} else {	// ���������� �� ������� �������������
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, objectId, user, sessionId)+"\nDENIED\nOperation: releaseLock\nLocked by other user");
				}
				return OperationResult.DENIED;	// ������, ���������� ��������� ������ ������������� 
			}
		}
	}
	/**
	 * ����������� �������� �� ��������� (�������, ��� ������ ���������� �� ������ �������������, �� � �������� - �� ������������).
	 * ��������������� ��� ���������� ������� � ����� ������� ������������� owner. ������������� ���������� ���������� ������� ��
	 * �������� ���������� ������������� ������� ���� {@link QueryBase}, � ������ ���������� �������� ��� ���������������.
	 * ����������� ������������ ���������� ����� ������ ����� ���� ��� ��������� ������� � ���� �� ���������� ��������
	 * � ���� ������� �������� ���������� ��������� � ���������.
	 * ����������.
	 */
	@Override
	public synchronized OperationResult interceptLock(QueryBase query, Person owner, Person executor) {
		ObjectId objectId = query.getEventObject();
		ObjectId queryId = query.getUid();
		Integer  sessionId = query.getSessionId();
		List<Lock> locks;
		if (owner.getId().equals(SYSTEM_EXECUTOR_ID.getId())){
			Lock lock = storage.get(objectId);
			locks = lock != null ? java.util.Collections.singletonList(storage.get(objectId)) 
								 : new ArrayList<Lock>();
		} else {								// �������� ��� ���������� � ���������������� ������,
			locks = getLocksByCustomer(owner, sessionId);	// ������� �������� ������ ������������
		}
		if (locks.isEmpty()){		// ��� ���������� ���������� ���������� ��� ������ ������������
			return OperationResult.NOT_EXISTS; // ����� ���������� �� ����������
		}
		
		for (Lock lock : locks){
			if (isLockIntercepted(lock)){										// ����������� 
				if (logger.isWarnEnabled()){
					logger.warn(getCurrentStateString(lock, objectId, executor, sessionId)+"\nINCONSISTENT_STATE\nOperation: interceptLock\nLock is intercepted");
				}
//				return OperationResult.INCONSISTENT_STATE;							// ��������� �������� ������ �������������
				continue;
			} 
			
			if (isLockInService(lock)){		// ���������� �� ������������
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, objectId, executor, sessionId)+"\nDENIED\nOperation: interceptLock\nLock is service");
				}
//				return OperationResult.DENIED;							// ��������� �������� ���������� �� ������������
				continue;
			}
			
			if (!owner.getId().equals(lock.getCustomer().getId())){ // ��������� ������ �� ��� ��� ���������
				if (logger.isWarnEnabled()){
					logger.warn(getCurrentStateString(lock, objectId, executor, sessionId)+"\nDENIED\nOperation: interceptLock\nLock is occupied by other user");
				}
//				return OperationResult.DENIED; // ���������
				continue;
			}
			
			lock.setLockedBy(executor);
			lock.setExecutor(executor);
			lock.setQueryId(queryId);
			lock.markChanged();
			lock.incLockCount();
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, objectId, executor, sessionId)+"\nSUCCESS\nOperation: interceptLock");
			}
		}
		return OperationResult.SUCCESS;
	}
	
	/**
	 * ����������� �������� �� ������� (�������, ��� ������ ���������� �� ������ �������������, �� � �������� - �� ������������).
	 * ������������� ��� ���������� ������������� � ����� ������� � ������������ ������� ������������� owner.
	 * ��������� ������������ ���������� ����� ������ ��������������� ����� ��� ��� ���� ��������� � ���������� ����������������� ��������
	 * � ���� ������� �������� ���������� ��������� � ���������.
	 */
	@Override
	public synchronized OperationResult catchLock(ObjectId queryId){
		inService.set(true);			// ������ �������, ��� ��� ����� ������������

		List<Lock> locks;
/*		
		if (owner.getId().equals(SYSTEM_EXECUTOR_ID.getId())){
			Lock lock = storage.get(objectId);
			locks = (lock != null) ? java.util.Collections.singletonList(storage.get(objectId)) 
								 : new ArrayList<Lock>();
		} else {
			locks = getLocksByCustomer(owner);
		}
		if (locks.isEmpty()){		// ��� ���������� ���������� ���������� ��� ������ ������������
			return OperationResult.NOT_EXISTS; // ����� ���������� �� ����������
		}
*/

		locks = getLocksByQueryId(queryId);  // ������ ���������� ������������� � ������ ���������� ������������ query
		Long currentThreadId = Thread.currentThread().getId();

		for (Lock lock : locks){
			if (!isLockIntercepted(lock)){ // ���������� �� ����������
				if (logger.isWarnEnabled()){
					logger.warn(getCurrentStateString(lock, null, null)+"\nINCONSISTENT_STATE\nOperation: catchLock");
				}
//				return OperationResult.INCONSISTENT_STATE; 
				continue;
			}
/*			
			if (lock.getCustomer() != null){ // ����������� �� ���������������� ���������� ?
				if (!owner.getId().equals(lock.getCustomer().getId())){ // ��������� ������ �� ��� ��� ���������
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, objectId, executor)+"\nDENIED\nOperation: catchLock");
					}
//					return OperationResult.DENIED; // ���������
					continue;
				}
			}
			
			if (!executor.getId().equals(lock.getExecutor().getId())){ // ��������� ����������� �� ��� ��� ���������
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, objectId, executor)+"\nDENIED\nOperation: catchLock\nIntercepted by other executor ");
				}
//				return OperationResult.DENIED; // ���������
				continue;
			}
*/			
			lock.setLockerThreadId(currentThreadId);
			lock.markChanged();
			inService.set(true);			// ������ �������, ��� ��� ����� ������������
			if (logger.isDebugEnabled()){
				logger.debug(getCurrentStateString(lock, null, null)+"\nSUCCESS\nOperation: catchLock");
			}
		}
		return OperationResult.SUCCESS;
	}
	
	/**
	 * ����������� ��������
	 * ���������� ��� ����������, ��������� �� ����� ���������� ���� �������
	 */
	@Override
	public synchronized OperationResult releaseLocks(Person executor) {
		//not used now. overriden
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
				/*
				if (!executor.getId().equals(lock.getExecutor().getId())){ // ��������� ��������� �� ��� ��� ���������
					logger.error(getCurrentStateString(lock, entry.getKey(), executor)+"\nDENIED\nOperation: releaseLocks");
					continue;
				}
				*/
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
					lock.setQueryId(null);
					lock.decLockCount();
					lock.markChanged();
					if (logger.isDebugEnabled()){
						logger.debug(getCurrentStateString(lock, entry.getKey(), executor)+"\nSUCCESS\nOperation: releaseLocks: switch to user mode");
					}
				}
			}
		}
		}
		inService.remove();
		return OperationResult.RELEASED;
	}
	
	public boolean isCurrentThreadIsService(){
		return Boolean.TRUE.equals(inService.get());
	}
	
	String getCurrentStateString(Lock lock, ObjectId objectId, Person user){
		return getCurrentStateString(lock, objectId, user, -1);
	}
	
	String getCurrentStateString(Lock lock, ObjectId objectId, Person user, Integer session){
		String lockState;
		if (lock != null){ 
			lockState = "\n\tObjectId:\t"+lock.getObjectId()+
						"\n\tThreadId:\t"+lock.getLockerThreadId()+
						"\n\tSessionId:\t"+lock.getSessionId()+
						"\n\tcustomerId:\t"+personToString(lock.getCustomer())+
						"\n\texecutorId:\t"+personToString(lock.getExecutor())+
						"\n\tlockedBy:\t"+personToString(lock.getLockedBy())+
						"\n\tqueryId:\t"+lock.getQueryId()+
						"\n\tcreated:\t"+lock.getCreated()+
						"\n\tchanged:\t"+lock.getChanged()+
						"\n\tlockCount:\t"+lock.getLockCount();
		} else {
			lockState = "NULL";
		}
 
		return "Lock: " + lockState +
				"\nEventObject:\t"+objectId+
				"\ncurrentSessionId:\t"+session+
				"\ncurrentThreadId:\t"+Thread.currentThread().getId()+
				"\ncurrentThreadName:\t"+Thread.currentThread().getName()+
				"\ncurrentThreadGroup:\t"+Thread.currentThread().getThreadGroup()+
				"\nuser:\t\t"+ personToString(user);
	}
	
	protected static String personToString (Person person){
		if (person == null)
			return "null";
		return person.getId().toString() + " (" + person.getLogin() + ")";
	}

	@Override
	public Person getLocker(ObjectId objectId) {
		return (storage.get(objectId) == null) ? null
				: storage.get(objectId).getLockedBy();
	}

	@Override
	public Lock getLockByObject(ObjectId objectId) {
		return storage.get(objectId);
	}

	@Override
	public LockInfo getLockInfoByObject(ObjectId objectId) {
		if (objectId == null){
			return null;
		}
		LockInfo lockInfo = new LockInfo(getLockByObject(objectId));
		return lockInfo;
	}

	protected static boolean isLockIntercepted(Lock lock){
		return ((lock.getExecutor() != null) && (lock.getLockerThreadId() == null));
	}

	protected static boolean isLockInService(Lock lock){
		return ((lock.getExecutor() != null) && (lock.getLockerThreadId() != null));
	}
	
	protected void setLockInService(Lock lock, Person executor){
		lock.setExecutor(executor);
		lock.setLockedBy(executor);
		lock.setLockerThreadId(Thread.currentThread().getId());
		lock.setQueryId(AsyncManager.getQueryUid());
		lock.markChanged();
	}
	
	protected List<Lock> getLocksByCustomer(Person customer) {
		return getLocksByCustomer(customer, null);
	}
	
	protected List<Lock> getLocksByCustomer(Person customer, Integer sessionId){
		List<Lock> locks = new ArrayList<Lock>();
		if (customer == null) return locks;
		
		CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> copySet = createReadOnlyCopy();
		for (Map.Entry<ObjectId, Lock> entry : copySet) {
			Lock lock = entry.getValue();
			if (lock.getCustomer() == null) {
				continue;
			}
			if (customer.getId().equals(lock.getCustomer().getId())) {
				if (sessionId == null) {
					//locks.add(lock);
				} else if (sessionId.equals(lock.getSessionId())) {
					locks.add(lock);
				}
			}
		}
		return locks;
	}

	protected List<Lock> getLocksByQueryId(ObjectId queryId){
		List<Lock> locks = new ArrayList<Lock>();
		if (queryId == null) return locks;
		
		CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> copySet = createReadOnlyCopy();
		for (Map.Entry<ObjectId, Lock> entry : copySet) {
			Lock lock = entry.getValue();
			if (queryId.equals(lock.getQueryId())){
				locks.add(lock);
			}
		}
		return locks;
	}

	@Override
	@ManagedAttribute(description="Get count of locks in storage")
	public int getCount() {
		return storage.size();
	}

	@Override
	@ManagedOperation(description="Release locks older than pointed age in milliseconds ")
	  @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "age", description = "The age of lock in milliseconds")})
	public void releaseAgedLocks(long ageMs) {
		if (ageMs < 0) return;
		Date now = new Date();
		synchronized (storage) {
			for (Iterator<Map.Entry<ObjectId, Lock>> itr =  storage.entrySet().iterator(); itr.hasNext();){
				Lock lock = itr.next().getValue();
				if ((now.getTime() - lock.getChanged().getTime()) >= ageMs)
					itr.remove();
			}
		}
	}

	@Override
	@ManagedOperation(description="Get all locks")
	public String listLocks() {
		StringBuilder buf = new StringBuilder();
		CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> copySet = createReadOnlyCopy();
		for (Map.Entry<ObjectId, Lock> entry : copySet){
			Lock lock = entry.getValue();
			buf.append(lock.getClass().getName()).append(":").append(lock.hashCode()).append(":\n");
			buf.append("object:\t\t").append(entry.getKey()).append("\n");
			buf.append(lock.toString()).append("\n\n");
		}
		return buf.toString();
	}

	@Override
	@ManagedOperation(description="Release lock for current object")
	@ManagedOperationParameters({
	    @ManagedOperationParameter(name = "class", description = "The full class name of object for release. com.aplana.... etc"),
	    @ManagedOperationParameter(name = "id", description = "The ID of object for release. Example: 12345678 for cards or 'JBR_FILES' for attributes")})
	public boolean releaseLockForObject(String clazz, String id) throws ClassNotFoundException {
		Class<?> cl = Class.forName(clazz);
		ObjectId foundId;
		try{
			foundId = ObjectIdUtils.getObjectId(cl, id, true);
		} catch (NumberFormatException e){
			foundId = ObjectIdUtils.getObjectId(cl, id, false);
		}
		boolean removed;
		synchronized (storage) {
			removed = storage.remove(foundId) != null;
		}
		return removed;
	}
	
	@Override
	@ManagedOperation(description="Release locks for current user by login or id")
	@ManagedOperationParameters({
	    @ManagedOperationParameter(name = "userInfo", description = "login or id, for example: 'ev.aleksandrova' or '4035' without quotes")})
	public void releaseLocksForPerson(String userInfo) {
		if (userInfo != null && !userInfo.isEmpty()) {
			synchronized (storage) {
				for (Iterator<Map.Entry<ObjectId, Lock>> iter = storage.entrySet().iterator(); iter.hasNext(); ) {
					Lock lock = iter.next().getValue();
					if (!isLockInService(lock) && !isLockIntercepted(lock)) {
						if (lock.getCustomer() != null && 
								(userInfo.equals(lock.getCustomer().getLogin()) 
								|| userInfo.equals(lock.getCustomer().getId().getId().toString()))) {
							iter.remove();
						}
					}
				}
			}
		}
	}
	
	protected CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> createReadOnlyCopy() {
		CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> copySet;
		synchronized (storage) {
			copySet = new CopyOnWriteArraySet<Map.Entry<ObjectId,Lock>>(storage.entrySet());
		}
		return copySet;
	}
	
	@Override
	public void restore(QueryBase query, Lock lock) {
		lock.setLockerThreadId(null);
		lock.setCustomer(null);
		lock.setLockCount(1);
		lock.setQueryId(query.getUid());
		query.setSessionId(lock.getSessionId());
		synchronized (storage) {
			storage.put(query.getEventObject(), lock);
		}
	}

	@Override
	public long getTimeout() {
		return 0;
	}

	@Override
	public void setTimeout(long timeout) throws IOException {
		throw new IOException("Operation is not suppoted");
		
	}

	@Override
	public OperationResult tryRemoveLock(ObjectId objectId, Person user, Integer sessionId) {
		Lock lock = getLockByObject(objectId);
		if (lock == null){
			return OperationResult.NOT_EXISTS;
		}

		if ((user == null) || (objectId == null)){
			return OperationResult.DENIED;
		}
		
		OperationResult res = releaseLock(objectId, user, sessionId, null);
		if (!res.equals(OperationResult.SUCCESS)){
			return res;
		}
		
		if ((isLockIntercepted(lock) || isLockInService(lock))){  
			return OperationResult.INCONSISTENT_STATE;	// ��������� ������������� ������� ���������� ���� ��� �� �������������
		}
		if (lock.getCustomer() == null){ // ���������� �� �� ������������ � ������ �� �����������
			return OperationResult.INCONSISTENT_STATE; // ���������� ��������
		}

		if (lock.getCustomer().getId().equals(user.getId())){ // �������� ���������� � ������ ������ ���������
			//���� ���������� ���� ������� �� ������ ������, �� ��������� ��
			if (sessionId != null && !sessionId.equals(lock.getSessionId())) {
				return OperationResult.INCONSISTENT_STATE; //���������� � ������ ������
			}
			synchronized (storage) {
				storage.remove(objectId);
			}		
			return OperationResult.SUCCESS;
		}
		return OperationResult.NOT_EXISTS;
	}
	
	public void returnLocksToIntercept(ObjectId queryId) {
		List<Lock> locks = getLocksByQueryId(queryId);
		for (Lock lock : locks) {
			lock.setLockerThreadId(null);
		}
	}

	public List<LockInfo> getLockInfosByCustomer(Person customer) {
		return getLockInfosByCustomer(customer, null);
	}
	
	@Override
	public List<LockInfo> getLockInfosByCustomer(Person customer, Integer sess) {
		List<LockInfo> lockInfos = new ArrayList<LockInfo>();
		List<Lock> locks = getLocksByCustomer(customer, sess);
		for (Lock lock : locks){
			LockInfo lockInfo = new LockInfo(lock);
			lockInfos.add(lockInfo);
		}
		return lockInfos;
	}

	@Override
	public List<ObjectId> getCardIdsOnService() {
		CopyOnWriteArraySet<Map.Entry<ObjectId, Lock>> copySet = createReadOnlyCopy();
		List<ObjectId> result = new ArrayList<ObjectId>(); 
		for (Map.Entry<ObjectId, Lock> entry : copySet) {
			Lock lock = entry.getValue();
			if(lock.getExecutor() != null){
				result.add(lock.getObjectId());
			}
		}
		return result;
	}
	
	@Override
	public void releaseLocksForLinkedQueries(ObjectId queryId) {
		if (queryId == null) return;
		List<Lock> locks = getLocksByQueryId(queryId);
		synchronized (storage) {
			for (Lock lock : locks) {
				lock.decLockCount();
				storage.remove(lock.getObjectId());
				synchronized (lock) {
					lock.notify();
				}
				if (logger.isDebugEnabled()){
					logger.debug(getCurrentStateString(lock, lock.getObjectId(), LockManagementBean.SYSTEM_EXECUTOR_ID)+"\nSUCCESS\nOperation: releaseLinkedLocks:REMOVE LINKED");
				}
			}
		}
	}
}
