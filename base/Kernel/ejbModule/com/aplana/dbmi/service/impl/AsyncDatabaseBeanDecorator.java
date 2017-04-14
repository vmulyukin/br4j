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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.model.AsyncTicket;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.async.AsyncManager;
import com.aplana.dbmi.service.impl.async.QueriesQueue.RunMode;
import com.aplana.dbmi.service.impl.async.QueryContainer;
import com.aplana.dbmi.service.impl.locks.LockManagementBean;
import com.aplana.dbmi.service.impl.query.DoLock;
import com.aplana.dbmi.service.impl.query.DoUnlock;
import com.aplana.dbmi.service.impl.query.SmartQuery;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.utils.QueryInspector;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;

/**
 * Asynchronous decorator for DatabaseBean which adds Spring Support.
 */
public class AsyncDatabaseBeanDecorator extends DatabaseBeanDecorator {
	private static ThreadLocal<QueryContainer> rootSyncQuery = new InheritableThreadLocal<QueryContainer>();

	private AsyncDatabaseBeanDecorator(Database db) {
		super(db);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T executeQuery(UserData user, QueryBase query)
			throws DataException {
		if (query == null || factory == null)
			throw new DataException();
		query.setUser(user);

		if (query.isRestored()) { // Query restored from backup
			setBeanFactoryForBackup(query.getPostProcessors());
			setBeanFactoryForBackup(query.getPreProcessors());
			setBeanFactoryForBackup(query.getValidators());
		}

		Long id = AsyncManager.getThreadLabel();
		if (id == null) {
			/**
			 * ����� �������� ��� �������� query (��� ����. ��� � �����.):
			 * 1. �������� ����� ��� ���������� (��� �����. ����, �.�. ���� ��������� ���� ���������)
			 * 2. ��� ������� query ����������� ����������
			 * 3. ��������� �������������� �������, ������� ���������� ����� ��������� ����������
			 * 4. ���������� query � QueryContainer
			 * 5. ������������� � thread local ������� query � �������� ��������� query
			 */
			AsyncManager.setThreadLabel(-1);
			if (isNeedWritePermissions(query)) {
				lockManagement.interceptLock(query, user.getPerson(), user.getPerson());
				lockManagement.catchLock(query.getUid());
				if (TransactionSynchronizationManager.isSynchronizationActive()) {
					//������������ ��������� ������� ����������
					TransactionSynchronizationManager.registerSynchronization(new TransactionListener(query));
				} else {
					//�� ������ ������, ����� ������ query �������� �� � ����������
					logger.error("NOT IN TRANSACTION. " + query.getUid() + ", user=" + user.getPerson().getId() + ", parent=" + query.getParentQuery());
				}
			}
			QueryContainer qc = wrapQuery(user, query);
			rootSyncQuery.set(qc);

			if (query.isAsync()) { // Primary asynchronous query
				if (logger.isInfoEnabled())
					logger.info("Start Primary async query " + query.getUid() + ": user = " + user.getPerson().getId() + " (" + user.getPerson().getFullName() + ")");
				return (T) new AsyncTicket(performAsyncQuery(user, qc));
			} else {               // Primary synchronous query
				if (logger.isInfoEnabled())
					logger.info("Start Primary sync query " + query.getUid() + ": user = " + user.getPerson().getId() + " (" + user.getPerson().getFullName() + ")");

				Object result;
				long start = System.currentTimeMillis();
				try {
					QueryInspector.start(query, "mainQuery");
					activeQueryBases.add(query);
					result = db.executeQuery(user, query);
				} finally {
					AsyncManager.unsetThreadLabel();
					activeQueryBases.remove(query);
					rootSyncQuery.remove();
					QueryInspector.end(System.currentTimeMillis() - start);
					QueryInspector.log();
				}

				//��� ����. query ���� ��� ������ ������� ������ query
				//startLinkedQueryContainer(qc.getNext());
				return (T) result;
			}
		} else if (id == -1) {    // Synchronous inner threads
			QueryContainer currQC = rootSyncQuery.get();
			QueryBase currQ = currQC.getQuery();
			Integer sessionId = currQ.getSessionId();
			query.setSessionId(sessionId);
			if (logger.isDebugEnabled())
				logger.debug("Start Sync inner query");
			return db.executeQuery(user, query);
		} else {                // Asynchronous threads
			QueryContainer q = queue.getRunnedQuery(AsyncManager.getThreadLabel());

			//������������� ������ �������� ������ �� ������ ��������� ������ (�� QueryContainer)
			ContextProvider queryContext = q.getContextProvider();
			Locale queryLocale = queryContext.getLocale();
			ContextProvider currentContext = ContextProvider.getContext();
			currentContext.setLocale(queryLocale);
			if (logger.isDebugEnabled())
				logger.debug(" --> Query: " + query.getUid() + ". Event: " + query.getEvent() + ". ObjectId: "
						+ query.getEventObject() + " is performed in ASYNC thread. Main query '"
						+ q.getClassName() + "' has ID = " + q.getId() + ".");

			Object result;
			long start = System.currentTimeMillis();
			if (query == q.getQuery()) {    // ���������� ��������� �����. query
				activeQueryBases.add(q.getQuery());
				QueryInspector.start(query, "mainAsyncQuery");
				if (logger.isInfoEnabled())
					logger.info("Exec Primary async query");
				result = db.executeQuery(user, query);
				queue.performed(q);
				if (logger.isDebugEnabled())
					logger.debug("End of primary async query");
				QueryInspector.end(System.currentTimeMillis() - start);
				QueryInspector.log();
				//����� ���������� �������� �����. query ���������� ������ ������������
				//������ ������������ � {@link AsyncThreadPoolExecutor}, ����� afterExecute()
			} else { //���������� ��������� query � �����. ������
				if (logger.isDebugEnabled())
					logger.debug("Exec inner async query");
				QueryBase currQ = q.getQuery();
				Integer sessionId = currQ.getSessionId();
				query.setSessionId(sessionId);
				result = db.executeQuery(user, query);
			}
			return (T) result;
		}
	}

	protected boolean isNeedWritePermissions(QueryBase query) {
		//��������� intercept � catch ��� ������ WriteQuery � ��� 100%-�� ��������� DoLock � DoUnlock
		return !(query instanceof DoLock || query instanceof DoUnlock) && (query instanceof WriteQuery);
	}

	/**
	 * Adds this query to asynchronously processing
	 *
	 * @return true, if successfully added
	 * @throws DataException
	 */
	private long performAsyncQuery(UserData user, QueryContainer q)
			throws DataException {
		QueryBase query = q.getQuery();
		if (logger.isInfoEnabled())
			logger.info("Query=" + query.getClass().getName() + "; on event=" + query.getEvent()
					+ " objectId=" + query.getEventObject() + " is in ASYNC mode.");

		//��������� ���������� �����, ����� ������� ������� � ������ ���� ������������ (1)
		//� ������� �������� query (2)
		try {
			query.prepare();
			super.validate(user, query);
		} finally {
			AsyncManager.unsetThreadLabel(); //(1)
			rootSyncQuery.remove();          //(2)
		}

		if (!queue.contains(q)) {
			if (query.getBeanFactory() == null) {
				q.setBeanFactory(factory);
			} else {
				q.setBeanFactory(query.getBeanFactory());
			}
			q.setLockObject(lockManagement.getLockByObject(query.getEventObject()));

			//������� ��� � ��� ������������� � ������� ���� ��� ��������
			if (query instanceof SmartQuery) {
				if (queue.check(q)) {
					queue.persist(q);
					return q.getId();
				}
			}

			/**
			 * "���������� ������" �����. �����.
			 * ������ ��������� �������� �� ����������� ������� ����� query � ����������.
			 * ���� �������� ���������, �� ����. ����� ������������ � �����. ����� �� �����������.
			 * ���� �������� ������, �� ������� ������ ����� ����� ����� ���������� � ������ �����
			 * ���������� ����� ������� ����. ����� (� TransactionListener'e)
			 */
			return queue.addQuery(q, RunMode.DEFERRED);
		}
		throw new DataException("jbr.async.performing.dublicate");
	}

	private QueryContainer wrapQuery(UserData user, QueryBase query) throws DataException {
		QueryContainer q = new QueryContainer(query, !query.isAsync());
		Integer prior = query.getPriority();
		q.setPriority(prior);
		q.setUser(user);
		return q;
	}

	@Override
	public void syncUser(PortalUser person) throws DataException {
		db.syncUser(person);
	}

	/**
	 * ������ ��� ������������� ����������. ������� ���������� (releaseLocks) ����� ���������� ������� ����������
	 * � ����� ���������� �����. �������� {@link com.aplana.dbmi.service.impl.async.AsyncPerformerBean} � ����� �����. ������� � �������.
	 *
	 * @author desu
	 */
	private class TransactionListener extends TransactionSynchronizationAdapter {

		private QueryBase query;

		public TransactionListener(QueryBase q) {
			this.query = q;
		}

		@Override
		public void afterCompletion(int status) {
			//��� ������ ����. query ����� �������\�������� ������� ����������
			if (isNeedWritePermissions(query)) {
				lockManagement.releaseLocks(query.getUser().getPerson());
			}
			//� ���� ������������� ����. ����� �� �����. query (���������) �� ������
			//intercept ���������� �������� � ����� ���������� ������� � ����� �����. ������� � �������
			if (query.isAsync() && status == TransactionSynchronizationAdapter.STATUS_COMMITTED) {
				if (!query.isRestored()) {
					lockManagement.interceptLock(query, query.getUser().getPerson(), LockManagementBean.SYSTEM_EXECUTOR_ID);
				}
				/**���� query ������������ � ����-��, �� �� ���� ��� ��������� ������
				 * �� ����� ����� �������� � ������� (��� �������� ���������� query,
				 * �� ���� ���, � �������� �� ���������)
				 */
				QueryContainer q = query.getQueryContainer();
				if (!q.isLinked())
					queue.notifyListeners(q);
			}
			/**
			 * ���� ������ ���������� ROLLED_BACK, ������ � �������� ��������� ��� ���������� �����. query
			 * � ������� �������� ������ (DataException), ������� �������� ����������. ������ ������ intercept
			 * � ���������� ������� �� ����, �.�. ������� ��� ��� ����� ���.
			 */
		}
	}
}