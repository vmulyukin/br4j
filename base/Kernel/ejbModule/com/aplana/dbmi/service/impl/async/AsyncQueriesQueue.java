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
package com.aplana.dbmi.service.impl.async;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.SmartQuery;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * Thread-safely priority bounded queue of
 * {@link com.aplana.dbmi.service.impl.async.QueryContainer queries}, which can be
 * processed asynchronously and can notify {@link QueriesPerformer} about adding
 * queries.<br>
 * Can {@link AsyncBackupBean backup} all queries. <br>
 * Mostly must be used by Spring IoC. <br>
 * <br> {@link #AsyncQueriesQueue(Comparator)}:
 * 
 * <pre>
 * {@code 
 * <bean id="asyncQueriesQueue" class="com.aplana.dbmi.service.impl.async.AsyncQueriesQueue">
 * 	<constructor-arg ref="queryBasePrior" />
 * 	...
 * </bean>}
 * </pre> {@link #AsyncQueriesQueue(int)}:
 * 
 * <pre>
 * {@code 
 * <bean id="asyncQueriesQueue" class="com.aplana.dbmi.service.impl.async.AsyncQueriesQueue">
 * 	<constructor-arg value="15" />
 * 	...
 * </bean>}
 * </pre> {@link #AsyncQueriesQueue(int, Comparator)}
 * 
 * <pre>
 * {@code 
 * <bean id="asyncQueriesQueue" class="com.aplana.dbmi.service.impl.async.AsyncQueriesQueue">
 * 	<constructor-arg value="15" />
 * 	<constructor-arg ref="queryBasePrior" />
 * 	...
 * </bean>}
 * </pre>
 * 
 * @see QueriesQueue
 * @see AsyncBackupBean
 */
@ManagedResource(objectName="br4j:name=asyncQueriesQueue", description="MBean for AsyncQueriesQueue", log=true, logFile="jmx.log")
public class AsyncQueriesQueue implements QueriesQueue<QueryContainer>,
		Serializable, QueriesQueueMBean {

	private static final long serialVersionUID = 4l;
	private static final Log logger = LogFactory.getLog(AsyncQueriesQueue.class);
	private PriorityBlockingQueue<QueryContainer> queue;
	private int size = 11;
	private Set<QueriesPerformer> listeners = new CopyOnWriteArraySet<QueriesPerformer>();
	// running queries
	private Map<Long, QueryContainer> runQueue = new ConcurrentHashMap<Long, QueryContainer>();
	// queries waiting their first execution, divided by user
	private Map<ObjectId, Map<Long, QueryContainer>> userWaitingActions = new ConcurrentHashMap<ObjectId, Map<Long, QueryContainer>>();
	// running queries (first execution), divided by user
	private Map<ObjectId, Map<Long, QueryContainer>> userRunningActions = new ConcurrentHashMap<ObjectId, Map<Long, QueryContainer>>();
	// queries running in repeate by policy, divided by user
	private Map<ObjectId, Map<Long, QueryContainer>> userRepeatingActions = new ConcurrentHashMap<ObjectId, Map<Long, QueryContainer>>();
	// queries waiting for repeat by policy, divided by user
	private Map<ObjectId, Map<Long, QueryContainer>> userWaitingRepeatActions = new ConcurrentHashMap<ObjectId, Map<Long, QueryContainer>>();
	// sync for getting and waiting accept from performer to executing 
	private Semaphore head = new Semaphore(1, true);
	// backup
	private static AsyncBackupBean backupManager;
	// storage for results of queries
	private static ResultStorage resultStorage;

	/**
	 * Creates instance with the specified capacity that orders its elements
	 * according to the specified comparator.
	 */
	public AsyncQueriesQueue(int size, Comparator<QueryContainer> comparator) {
		this.size = size;
		this.queue = new PriorityBlockingQueue<QueryContainer>(size, comparator);
	}

	/**
	 * Creates instance with the default capacity that orders its elements
	 * according to the specified comparator.
	 */
	public AsyncQueriesQueue(Comparator<QueryContainer> comparator) {
		this.queue = new PriorityBlockingQueue<QueryContainer>(size, comparator);
	}

	/**
	 * Creates instance with the specified capacity that orders its elements
	 * according to natural order(in {@link QueryContainer} is FIFO).
	 */
	public AsyncQueriesQueue(int size) {
		this.size = size;
		this.queue = new PriorityBlockingQueue<QueryContainer>(size);
	}
	
	public boolean persist(QueryContainer qc) throws DataException {
		if (backupManager == null)
			return false;
		qc.setAddingTime(System.currentTimeMillis());
		backupManager.write(qc);
		return true;
	}

	/**
	 * ���� ����� immediate (������ �����), �� ��������� query container � ������� � �����
	 * ���������� ������� � ����� ������ � �������.
	 * ���� �� ����� ����������� �������, �� ����������� ������ ��������, ��� ���������� 
	 * � ������� � ����������� ��������. �������� ���������� � ����������� � ���� ������
	 * ������ ������������� ��������� ������� ������� {@link #notifyListeners(QueryContainer q)}
	 */
	@Override
	public long addQuery(QueryContainer query, RunMode mode) throws DataException {
		logOperation("Adding query to queue");
		if (query == null || query.getId() == -1) {
			logOperation(" --> Query is null. Adding this query impossible.");
			throw new DataException("jbr.async.performing");
		}

		if ((queue.size() + runQueue.size()) >= this.size) {
			logOperation(" --> Queue is full. Adding this query impossible.");
			throw new DataException("jbr.async.performing");
		}
		query.setAddingTime(System.currentTimeMillis());
		long id = query.getId();
		
		// create future result template for this query. 
		// real result sets by performer
		resultStorage.put(id, new AsyncResult());
		
		//��� �������� �� ����� ������� � ������� - ��������� � ������� ����� ��� ���
		boolean runImmediate = mode == RunMode.IMMEDIATE;
		boolean offered = runImmediate ? queue.offer(query) : false; 
		if (offered || !runImmediate) {
			ObjectId person = query.getUser().getPerson().getId();
			if (query.getPolicy() == null) { //���� ������ ������ ������� �� � ������ ���������
				addUserEvent(userWaitingActions, person, query);
			}
			try {
				//���� ��������� (������������) �� �� ��� ������� � ����
				//������� ����� ������ �� ������������
				if (!query.isLinked())
					persist(query);
			
				logOperation(logInfo(query));
				// ���� ����� ������� �����, �� ���������� �������
				if (runImmediate) {
					notifyListeners();
				}
				return id;
			} catch (DataException e) {
				queue.remove(query);
				removeUserEvent(userWaitingActions, person, query);
				removeUserEvent(userWaitingRepeatActions, person, query);
				throw e;
			} catch (Exception e) {
				queue.remove(query);
				removeUserEvent(userWaitingActions, person, query);
				removeUserEvent(userWaitingRepeatActions, person, query);
				throw new DataException(e);
			}
		} else {
			resultStorage.pull(id);
			logOperation(" --> Adding this query impossible.");
			throw new DataException("jbr.async.performing");
		}
	}

	@Override
	public void performing(QueryContainer query) {
		logOperation("Start perform query");
		queue.remove(query);
		runQueue.put(query.getId(), query);
		head.release();
		ObjectId person = query.getQuery().getUser().getPerson().getId();
		if (query.getPolicy() == null) {
			removeUserEvent(userWaitingActions, person, query);
			addUserEvent(userRunningActions, person, query);
		} else {
			removeUserEvent(userWaitingRepeatActions, person, query);
			addUserEvent(userRepeatingActions, person, query);
		}
		
		logOperation("End of 'performing' function");
	}
	
	@Override
	public void policyStarted(QueryContainer query) {
		logOperation("Started policy");
		ObjectId person = query.getQuery().getUser().getPerson().getId();
		
		removeUserEvent(userRunningActions, person, query);
		removeUserEvent(userRepeatingActions, person, query);
		addUserEvent(userWaitingRepeatActions, person, query);
		
		logOperation("End of 'policyStarted' function");
	}
	
	private void afterExec(QueryContainer query) {
		logOperation("Removing from DB and runQueue");
		logOperation(logInfo(query));
		synchronized (query) {
			query.done();
		}
		synchronized (runQueue) {
			this.runQueue.remove(query.getId());	
		}
		ObjectId person = query.getQuery().getUser().getPerson().getId();
		
		removeUserEvent(userRunningActions, person, query);
		removeUserEvent(userRepeatingActions, person, query);
	}

	@Override
	public void performed(QueryContainer query) {
		afterExec(query);
		if (backupManager != null)
			backupManager.remove(query.getId());
		logOperation("End of 'performed' function");
	}
	
	@Override
	public void failed(QueryContainer query) {
		afterExec(query);
		if (backupManager != null) {
			QueryContainer q = query;
			do {
				backupManager.remove(q.getId());
				removeUserEvent(userWaitingActions, q.getUser().getPerson().getId(), q);
			} while ((q = q.getNext()) != null);
		}
		logOperation("End of 'failed' function");
	}
	
	@Override
	public QueryContainer getQuery() {
		logOperation("Getting query from queue");
		try {
			head.acquire();
			logOperation("Acquired semaphore");
		} catch (InterruptedException e) {
			logger.error("Error during getting query", e);
			head.release();
		}
		QueryContainer query = queue.peek();
		if (query != null) {
			logOperation(logInfo(query));
		} else {
			logOperation(" --> Queue is empty. Returns 'null'.");
			head.release();
		}
		return query;
	}
	
	@Override
	public void rejected() {
		head.release();
	}

	@Override
	public void addListener(QueriesPerformer listener) {
		logOperation("Registering listener: "
				+ listener.getClass().getSimpleName());
		listeners.add(listener);

	}

	@Override
	public void removeListener(QueriesPerformer listener) {
		logOperation("Removing listener: "
				+ listener.getClass().getSimpleName());
		listeners.remove(listener);

	}

	private String logInfo(QueryContainer query) {
		return " --> Query: " + query.getQuery().getClass().getSimpleName()
				+ ". Event: " + query.getQuery().getEvent() + ". ObjectId: "
				+ query.getQuery().getEventObject() + ". Size of queue is: "
				+ (queue.size() + runQueue.size());
	}

	private void logOperation(String msg) {
		if (logger.isDebugEnabled())
			logger.debug(msg);
	}

	@Override
	public QueryContainer getRunnedQuery(long id) {
		return runQueue.get(id);
	}

	/**
	 * Can be used by Spring IoC: <br>
	 * 
	 * <pre>
	 * {@code 
	 * <bean id="asyncPerformerBean" class="com.aplana.dbmi.service.impl.async.AsyncPerformerBean" init-method="init">
	 * }
	 * </pre>
	 * 
	 * Using CreateDataServiceOnStartupTaskBean is preferable
	 */
	public void init() {
		if (backupManager != null) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						backupManager.restore();
					} catch (DataException e) {
						logger.error("Error during restoring queue");
					}
				}
			});
			executor.shutdown();
		}
	}

	/**
	 * Sets {@link AsyncBackupBean backup} manager
	 * 
	 * @param backup
	 * @see AsyncBackupBean
	 */
	public void setBackupManager(AsyncBackupBean backup) {
		if (backupManager != null) {
			logger.error("Duplicated Spring container.");
			return;
		}
		AsyncQueriesQueue.backupManager = backup;
	}

	/**
	 * Sets result {@link ResultStorage storage}
	 * 
	 * @param storage
	 * @see ResultStorage
	 */
	public void setResultStorage(ResultStorage resultStorage) {
		AsyncQueriesQueue.resultStorage = resultStorage;
	}

	@Override
	public ResultStorage getResultStorage() {
		return resultStorage;
	}

	@Override
	public boolean contains(QueryContainer query) {
		return (queue.contains(query) || runQueue.containsValue(query));
	}

	@Override
	@ManagedAttribute(description="Sets the max size of AsyncQueriesQueue")
	public void setMaxQueueSize(int size) {
		this.size = size;

	}

	@Override
	@ManagedAttribute(description="Gets the max size of AsyncQueriesQueue")
	public int getMaxQueueSize() {
		return size;
	}

	@Override
	public List<QueryContainer> getRunningQueries(UserData user) {
		ArrayList<QueryContainer> result = new ArrayList<QueryContainer>();
		ObjectId person = user.getPerson().getId();
		if (userRunningActions.get(person) != null
				&& !userRunningActions.get(person).isEmpty())
			result.addAll(userRunningActions.get(person).values());
		return result;
	}

	@Override
	public List<QueryContainer> getWaitingQueries(UserData user) {
		ArrayList<QueryContainer> result = new ArrayList<QueryContainer>();
		ObjectId person = user.getPerson().getId();
		if (userWaitingActions.get(person) != null
				&& !userWaitingActions.get(person).isEmpty())
			result.addAll(userWaitingActions.get(person).values());
		return result;
	}

	@Override
	public List<QueryContainer> getRepeatingQueries(UserData user) {
		ArrayList<QueryContainer> result = new ArrayList<QueryContainer>();
		ObjectId person = user.getPerson().getId();
		if (userRepeatingActions.get(person) != null
				&& !userRepeatingActions.get(person).isEmpty())
			result.addAll(userRepeatingActions.get(person).values());
		return result;
	}
	
	@Override
	public List<QueryContainer> getWaitingRepeatQueries(UserData user) {
		ArrayList<QueryContainer> result = new ArrayList<QueryContainer>();
		ObjectId person = user.getPerson().getId();
		if (userWaitingRepeatActions.get(person) != null
				&& !userWaitingRepeatActions.get(person).isEmpty())
			result.addAll(userWaitingRepeatActions.get(person).values());
		return result;
	}

	@Override
	@ManagedAttribute(description="Count of running queries (without repeating)")
	public int getRunningQueriesCount() {
		int size = 0;
		for (Map<Long, QueryContainer> map : userRunningActions.values()) {
			size += map.size();
		}
		return size;
	}

	@Override
	@ManagedAttribute(description="Count of queries which are waiting for first execution")
	public int getWaitingQueriesCount() {
		int size = 0;
		for (Map<Long, QueryContainer> map : userWaitingActions.values()) {
			size += map.size();
		}
		return size;
	}

	@Override
	@ManagedAttribute(description="Count of repeating queries")
	public int getRepeatingQueriesCount() {
		int size = 0;
		for (Map<Long, QueryContainer> map : userRepeatingActions.values()) {
			size += map.size();
		}
		return size;
	}
	
	@Override
	@ManagedAttribute(description="Count of queries which are waiting for repeat")
	public int getWaitingRepeatQueriesCount() {
		int size = 0;
		for (Map<Long, QueryContainer> map : userWaitingRepeatActions.values()) {
			size += map.size();
		}
		return size;
	}

	@Override
	public boolean check(QueryContainer qc) throws DataException {
		ObjectId person = qc.getUser().getPerson().getId();
		for (Iterator<QueryContainer> it =  queue.iterator(); it.hasNext(); ) {
			QueryContainer q = it.next();
			QueryBase query = q.getQuery();
			if (query instanceof SmartQuery) {
				SmartQuery sq  = (SmartQuery) query;
				SmartQuery sqc = (SmartQuery) qc.getQuery();
				if (sq.isPossibleToAdd(sqc)) {
					synchronized (q) {
						if (!q.isDone()) {
							q.setNext(qc);
							addUserEvent(userWaitingActions, person, qc);
							return true;
						}
					}
				}
			}
		}
		synchronized (runQueue) {
			for (Iterator<Map.Entry<Long, QueryContainer>> it =  runQueue.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<Long, QueryContainer> entry = it.next();
				QueryContainer q = entry.getValue();
				QueryBase query = q.getQuery();
				if (query instanceof SmartQuery) {
					SmartQuery sq  = (SmartQuery) query;
					SmartQuery sqc = (SmartQuery) qc.getQuery();
					if (sq.isPossibleToAdd(sqc)) {
						synchronized (q) {
							if (!q.isDone()) {
								q.setNext(qc);
								addUserEvent(userWaitingActions, person, qc);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private void notifyListeners() {
		notifyListeners(null);
	}

	@Override
	public void notifyListeners(QueryContainer q) {
		if (q != null) {
			logOperation("Inserting deferred query into queue");
			queue.offer(q);
		}
		logOperation("Notify listeners");
		new Thread(new Runnable() {
			@Override
			public void run() {
				logOperation("Start of runnable to notify listeners");
				for (QueriesPerformer listener : listeners) {
					listener.queriesAdded();
					logOperation("Listener '" + listener + "' has been notified");
				}
			}
		}).start();
	}
	
	private void addUserEvent(Map<ObjectId, Map<Long, QueryContainer>> map, ObjectId personId, QueryContainer qc) {
		if (map.get(personId) == null)
			map.put(personId, new HashMap<Long, QueryContainer>());
		Map<Long, QueryContainer> m = map.get(personId);
		m.put(qc.getId(), qc);
	}
	
	private void removeUserEvent(Map<ObjectId, Map<Long, QueryContainer>> map, ObjectId personId, QueryContainer qc) {
		if (map.get(personId) != null) {
			Map<Long, QueryContainer> m = map.get(personId);
			m.remove(qc.getId());
		}
	}

	@Override
	@ManagedAttribute(description="Real size of AsyncQueriesQueue")
	public int getQueueSize() {
		return queue.size();
	}
}
