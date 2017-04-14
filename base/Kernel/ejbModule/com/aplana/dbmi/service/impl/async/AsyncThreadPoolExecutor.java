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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aplana.dbmi.service.impl.query.DoChainAsyncDeliveryAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ErrorMessage;
import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActiveQueryBases;
import com.aplana.dbmi.service.impl.LogEventBean;
import com.aplana.dbmi.service.impl.async.QueriesQueue.RunMode;
import com.aplana.dbmi.service.impl.locks.LockManagementBean;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;
import com.aplana.dbmi.service.impl.query.SmartQuery;

/**
 * Bounded ThreadPoolExecutor with SynchronousQueue working queue (i.e. without
 * working queue).
 */

public class AsyncThreadPoolExecutor extends ThreadPoolExecutor {

	// Indicates that there is no free threads
	private AtomicBoolean threadFull = new AtomicBoolean();
	private AsyncPerformerBean performer;
	private LockManagementSPI lockManagement;
	private ActiveQueryBases activeQueryBases;
	private static final Log logger = LogFactory.getLog(AsyncThreadPoolExecutor.class);

	/**
	 * Handler for rejected tasks
	 */
	private class AsyncRejectedExecutionHandler implements
			RejectedExecutionHandler {
		/**
		 * Sets the pool is fully occupied, when a task cannot be accepted for
		 * execution by {@link ThreadPoolExecutor#execute(Runnable)
		 * execute(Runnable)}
		 */
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			performer.getQueue().rejected();
			if (threadFull.compareAndSet(false, true))
				logOperation("Thread pool is busy.");
		}

	}

	/**
	 * This factory sets async {@link QueriesPerformer#ASYNC_THREAD_GROUP
	 * ThreadGroup}
	 * 
	 */
	private class AsyncThreadFactory implements ThreadFactory {

		private ThreadGroup grp = new ThreadGroup(
				QueriesPerformer.ASYNC_THREAD_GROUP);

		public Thread newThread(Runnable r) {
			return new Thread(grp, r);
		}

	}

	/**
	 * This class notifies that the pool is released
	 */
	private class PoolReleaser implements Runnable {

		CountDownLatch latch;

		public PoolReleaser(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				latch.await();
			} catch (InterruptedException e) {
				threadFull.compareAndSet(false, true);
			}
			threadFull.compareAndSet(true, false);
			logOperation("The thread pool is released.");
			// free threads, notify about that
			performer.queriesAdded();
		}

	}

	public AsyncThreadPoolExecutor(int nThreads, AsyncPerformerBean performer, LockManagementSPI lockManagement, int queueSize, ActiveQueryBases aqb) {
		super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(queueSize));
		this.setThreadFactory(new AsyncThreadFactory());
		this.performer = performer;
		this.lockManagement = lockManagement;
		this.activeQueryBases = aqb;
		this.setRejectedExecutionHandler(new AsyncRejectedExecutionHandler());
	}

	/**
	 * Method notifies queue about accepting this query to performing and mark
	 * it in AsyncManager.<br> {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		QueryContainer query = ((AsyncTask) r).getQueryContainer();
		long id = query.getId();
		ResultStorage resultStorage = performer.getQueue().getResultStorage();
		AsyncResult resultFromStorage = (AsyncResult)resultStorage.get(id);
		if (resultFromStorage != null)
			resultFromStorage.setRealFuture((AsyncTask) r);
		AsyncManager.setThreadLabel(id);
		AsyncManager.setQueryUid(query.getQuery().getUid());
		performer.getQueue().performing(query);
		if (logger.isDebugEnabled())
			logger.debug(new StringBuilder("Executing query \n --> Query: ")
					.append(query.getClassName()).append(". Event: ")
					.append(query.getQuery().getEvent()).append(". ObjectId: ")
					.append(query.getQuery().getEventObject()).append(".").toString());
		
		lockManagement.catchLock(query.getQuery().getUid());
		super.beforeExecute(t, r);
	}

	/**
	 * 
	 * Method passes (if needed) the event to PerformerBean, when the pool is
	 * released and notifies that query was performed. Also in this method runs
	 * {@link ExecPolicy policies} in exception case or sets results to the resultStorage<br>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);

		if (!(r instanceof Future<?>)) {
			logOperation("System Exception during exetuting query. Runnable r must be Future;");
			return;
		}
		QueryContainer query = ((AsyncTask) r).getQueryContainer();
		AsyncManager.unsetThreadLabel();
		AsyncManager.unsetQueryUid();

		if (t == null) {
			try {
				AsyncTask future = (AsyncTask) r;
				future.get();
				// checked, no errors
				//������� ���
				unlockFinally(query, r);
				
				activeQueryBases.remove(query.getQuery());
				
				startLinkedQueryContainer(query.getNext());
				
				if (logger.isDebugEnabled()) {
					logger.debug(new StringBuilder("Query executed sucessfully \n -->  Query: ")
						.append(query.getQuery().getUid()).append(". Event: ")
						.append(query.getQuery().getEvent()).append(". ObjectId: ")
						.append(query.getQuery().getEventObject()).append(".").toString());
				}
				if (performer.isLogEvent()) {
					LogEntry logEntry = query.getQuery().getLogEntry();
					if (logEntry != null) {
						LogEventBean logEventBean = performer.getLogEventBean();
						DataException dataException = new DataException(
								"general.runtime.async.success",
								new Object[] { DataException.RESOURCE_PREFIX
										+ query.getQuery().getClass().getName() });
						InfoMessage info = new InfoMessage(logEntry);
						info.setDescriptionMessage(dataException.getMessage());
						info.setCodeMessage("general.runtime.async.success");
						info.isSucces(1L);
						try {
							logEventBean.logEventExt(query.getQuery().getUser(), info);
						} catch (Exception ex) {
							logger.error("Exception during writing result in EVENT_BEAN");
						}
					}
				}
			} catch (InterruptedException e) {
				logException(query, e);
			} catch (CancellationException e) {
				logOperation("Empty queue.");
			} catch (ExecutionException e) {
				if (query.getPolicy() != null || query.getQuery().getAsyncPolicyName() != null) {
					// Performing exception policy
					final ExecPolicy policy;
					if (query.getPolicy() == null) { // first performing
						policy = (ExecPolicy) query.getBeanFactory().getBean(
								query.getQuery().getAsyncPolicyName());
						policy.setQuery(query);
						policy.setQueue(performer.getQueue());
						query.setPolicy(policy);
					} else {
						policy = query.getPolicy();
					}
					policy.setRetryReason(e);
					if (policy.checkApplicability()) {
						logOperation("Performing container policy: " + policy);
						lockManagement.returnLocksToIntercept(query.getQuery().getUid());
						performer.getQueue().policyStarted(query);
						new Thread(new Runnable() {
							@Override
							public void run() {
								policy.performPolicy();
							}
						}).start();
					} else {
						//������� ���
						unlockFinally(query, r);
						unlockLinkedLocks(query);
						logException(query, e.getCause());
					}
				} else {
					unlockFinally(query, r);
					unlockLinkedLocks(query);
					logException(query, e.getCause());
				}
				if(query.getQuery() instanceof DoChainAsyncDeliveryAction) {
					startLinkedQueryContainer(query.getNext());
				}
			}
		} else {
			//������� ���
			lockManagement.releaseLocks(LockManagementBean.SYSTEM_EXECUTOR_ID);
			unlockLinkedLocks(query);
			logException(query, t);
			throw new RuntimeException(t);
		}

		if (threadFull.get()) {
			// release thread pool if busy in different thread
			CountDownLatch latch = new CountDownLatch(1);
			Thread thread = new Thread(new PoolReleaser(latch));
			thread.start();
			latch.countDown();
		}

	}
	
	private void startLinkedQueryContainer(final QueryContainer q) {
		if (q != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (logger.isDebugEnabled()) {
							QueryContainer prev = q.getPrev();
							Long prevId = null;
							if (prev != null) {
								prevId = prev.getId();
							}
							logger.debug("Start LINKED async query (id=" + q.getId() + "). Prev id = " + prevId);
						}
						Thread.sleep(1000);
						performer.getQueue().addQuery(q, RunMode.IMMEDIATE);
					} catch (Exception e) {
						logger.error("Error while adding linked query container in the queue", e);
					}
				}
			}).start();
		}
	}
	
	private void unlockFinally(QueryContainer qc, Runnable run) {
		//������� ���
		lockManagement.releaseLocks(LockManagementBean.SYSTEM_EXECUTOR_ID);
		if (performer.getQueue().getResultStorage().get(qc.getId()) != null) {
			((AsyncResult) performer.getQueue().getResultStorage()
				.get(qc.getId())).setRealFuture(((AsyncTask) run));
		}
	}
	
	private void unlockLinkedLocks(QueryContainer currentQuery) {
		//������� ���� ���������� query
		QueryContainer q = currentQuery;
		while ((q = q.getNext()) != null) {
			lockManagement.releaseLocksForLinkedQueries(q.getQuery().getUid());
		}
	}

	private void logException(QueryContainer query, Throwable e) {
		performer.getQueue().failed(query);
		activeQueryBases.remove(query.getQuery());
		if (logger.isErrorEnabled()) {
			logger.error(new StringBuilder("Exception during exetuting query \n --> Query: ")
							.append(query.getClassName()).append(". Event: ")
							.append(query.getQuery().getEvent()).append(". ObjectId: ")
							.append(query.getQuery().getEventObject()).append(". Policy: ")
							.append(query.getPolicy()).toString(), e);
		}
		if (performer.isLogEvent() && (query.getQuery().getLogEntry() != null)) {
			DataException dataException = new DataException(
					"general.runtime.async", new Object[] {
							DataException.RESOURCE_PREFIX
									+ query.getQuery().getClass().getName(),
							e.getMessage() }, e);
			ErrorMessage error = new ErrorMessage(e, dataException,
					query.getQuery().getLogEntry());
			error.isSucces(0L);
			LogEventBean logEventBean = performer.getLogEventBean();
			QueryContainer q = query;
			do {
				try {
					logEventBean.logEventExt(q.getQuery().getUser(), error);
				} catch (Exception ex) {
					logger.error("Exception during writing result in EVENT_BEAN");
				}
			} while ((q = q.getNext()) != null);
		}

	}

	/**
	 * Override default value to return AsyncTask<br>
	 */
	@Override
	protected AsyncTask newTaskFor(Runnable runnable, Object value) {
		return (AsyncTask) runnable;
	}

	private void logOperation(String msg) {
		if (logger.isDebugEnabled())
			logger.debug(msg);
	}
	
	public LockManagementSPI getLockManagement() {
		return lockManagement;
	}

}