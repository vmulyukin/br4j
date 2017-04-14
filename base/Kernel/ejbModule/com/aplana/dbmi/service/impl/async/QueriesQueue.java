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

import java.util.List;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

/**
 * Queue performed by
 * {@link com.aplana.dbmi.service.impl.async.QueriesPerformer QueriesPerformer},
 * which can publish events about adding queries.
 * 
 * 
 * @param <QueryContainer>
 *            queries type
 */
public interface QueriesQueue<E extends QueryContainer> {

	/**
	 * Identifier off Asynchronous Queue
	 */
	String BEAN_ASYNC_QUEUE = "asyncQueriesQueue";
	
	enum RunMode {
		/**
		 * ������ ����� (���������� � ������� + ����������� �����. �������� � ����� ������� � �������)
		 */
		IMMEDIATE,
		/**
		 * ���������� ������ (����� ��������� ���������� ����������)
		 * �������� ���������� � ����������� �������� ���������� �������� ����� �������
		 * {@link #notifyListeners(QueryContainer)}.
		 */
		DEFERRED
	}

	/**
	 * Adds query, which must be processed asynchronously, to queue
	 * If mode is RunMode.IMMEDIATE then query will be offered into queue
	 * and will be submitted for execution.
	 * If mode is RunMode.DEFERRED then query just will be validated for errors,
	 * but offering into queue and submitting (notify listener) must be invoked later
	 * by method {@link #notifyListeners(QueryContainer)}.
	 * 
	 * @param query which must be processed asynchronously
	 * @param mode for executing queue
	 * @return added query id
	 */
	long addQuery(QueryContainer query, RunMode mode) throws DataException;

	/**
	 * Check if query from parameter could be added (be linked) to another async. query
	 * which in execution now (in pending queue or running queue) 
	 * @param qc query that pretend to be inserted into queue or be linked to some query
	 * @return has been linked or not (true, false)
	 * @throws DataException
	 */
	boolean check(QueryContainer qc) throws DataException;
	
	/**
	 * Retrieves (but not removes) the query, which must be processed firstly
	 * according to the specified ordering, or returns null if this queue is
	 * empty. If the retrieved query has been accepted for execution,
	 * {@link #performing(QueryContainer)} calling needed.
	 * 
	 * @return query to be processed
	 */
	QueryContainer getQuery();

	/**
	 * Notifies that specified query is runned
	 * 
	 * @param query
	 *            , which is performed
	 */
	void performing(QueryContainer query);

	/**
	 * Notifies that Performer was rejected the query
	 */
	void rejected();

	/**
	 * Notifies that specified query was performed (no matter successfully or
	 * not)
	 * 
	 * @param query
	 *            , which was performed
	 */
	void performed(QueryContainer query);
	
	void failed(QueryContainer query);
	
	void policyStarted(QueryContainer query);

	/**
	 * Adds listener of queue
	 * 
	 * @param listener
	 */
	void addListener(QueriesPerformer listener);

	/**
	 * Removes listener of queue
	 * 
	 * @param listener
	 */
	void removeListener(QueriesPerformer listener);

	/**
	 * Returns query runned in current thread
	 * 
	 * @return query
	 */
	QueryContainer getRunnedQuery(long id);

	/**
	 * Returns true if this queue contains the specified element.
	 * 
	 * @param query
	 *            object to be checked for containment in this queue
	 * @return true if this queue contains the specified element
	 */
	boolean contains(QueryContainer query);

	/**
	 * Returns result {@link ResultStorage storage} which used by this queue
	 * 
	 * @return ResultStorage
	 * @see ResultStorage
	 */
	ResultStorage getResultStorage();

	/**
	 * Returns queries that run by performer now
	 * 
	 * @param user
	 * @return run queries
	 */
	List<QueryContainer> getRunningQueries(UserData user);

	/**
	 * Returns queries that are waiting for performing
	 * 
	 * @param user
	 *            whose queries
	 * @return waiting queries
	 */
	List<QueryContainer> getWaitingQueries(UserData user);

	/**
	 * Returns repeated queries after the
	 * {@link com.aplana.dbmi.service.impl.async.ExecPolicy policy} is applied
	 * 
	 * @param user
	 *            whose query
	 * @return repeated queries
	 */
	List<QueryContainer> getRepeatingQueries(UserData user);
	
	/**
	 * Returns waiting for repeat queries after the
	 * {@link com.aplana.dbmi.service.impl.async.ExecPolicy policy} is applied
	 * 
	 * @param user
	 *            whose query
	 * @return repeated queries
	 */
	List<QueryContainer> getWaitingRepeatQueries(UserData user);
	
	/**
	 * Persistent.
	 * Write query container into the database ('async_queue' table)
	 * @param qc
	 * @return true if success, false - otherwise
	 */
	boolean persist(QueryContainer qc) throws DataException;
	
	/**
	 * Notifying {@link #AsyncPerformerBean} about new query container in the queue
	 * If parameter is set (not null), offer this parameter (QueryContainer) in queue
	 * before notifying.
	 * @param q query that need to be offered into queue.  May be null if there is no need 
	 * to insert something, just notify {@link #AsyncPerformerBean}.
	 */
	public void notifyListeners(QueryContainer q);
}
