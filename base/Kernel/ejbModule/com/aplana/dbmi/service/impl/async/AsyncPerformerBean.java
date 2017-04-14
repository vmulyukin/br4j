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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.*;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActiveQueryBases;
import com.aplana.dbmi.service.impl.LogEventBean;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * {@link com.aplana.dbmi.service.impl.async.QueriesPerformer Performer} for
 * queries from {@link com.aplana.dbmi.service.impl.async.QueriesQueue queue},
 * which must be performed asynchronously. <br>
 * Mostly must be used by Spring IoC. <br>
 * <br> {@link #AsyncPerformerBean(QueriesQueue)}:
 * 
 * <pre>
 * {@code
 * <bean id="asyncPerformerBean" class="com.aplana.dbmi.service.impl.async.AsyncPerformerBean">
 * 	<constructor-arg ref="asyncQueriesQueue"/>
 * </bean>
 * }
 * </pre> {@link #AsyncPerformerBean(int, QueriesQueue)}:
 * 
 * <pre>
 * {@code
 * <bean id="asyncPerformerBean" class="com.aplana.dbmi.service.impl.async.AsyncPerformerBean">
 * 	<constructor-arg value="15"/>
 * 	<constructor-arg ref="asyncQueriesQueue"/>
 * </bean>
 * }
 * </pre>
 * 
 * Also is can log all queries in {@link LogEventBean}:
 * 
 * <pre>
 * {@code
 * <bean id="asyncPerformerBean" class="com.aplana.dbmi.service.impl.async.AsyncPerformerBean">
 * 	<constructor-arg ref="asyncQueriesQueue"/>
 * ...
 * 	<property name="logEventBean" ref="logevent" />
 * 
 * </bean>
 * }
 * 
 * @see LogEventBean
 */

@ManagedResource(objectName="br4j:name=asyncPerformerBean", description="MBean for AsyncPerformer")
public class AsyncPerformerBean implements QueriesPerformer, MBeanCustomizable {

	private static final long serialVersionUID = 2804410441876588285L;
	private QueriesQueue<QueryContainer> queue;
	private static final int DEFAULT_POOL_SIZE  = 200;
	private static final int DEFAULT_QUEUE_SIZE = 200;
	// used thread pool
	private AsyncThreadPoolExecutor threadPool = null;
	private LogEventBean logEventBean;
	private static final Log logger = LogFactory
	.getLog(AsyncPerformerBean.class);

	/**
	 * Creates instance of PerformerBean with default pool size (10).
	 * 
	 * @param queue
	 *            instance of asynchronous queue
	 */
	public AsyncPerformerBean(QueriesQueue<QueryContainer> queue, LockManagementSPI lockManagement, ActiveQueryBases aqb) {
		threadPool = new AsyncThreadPoolExecutor(DEFAULT_POOL_SIZE, this, lockManagement, DEFAULT_QUEUE_SIZE, aqb);
		if (this.queue == null) {
			this.queue = queue;
			queue.addListener(this);
		}
	}

	/**
	 * Creates instance of PerformerBean with specified pool size.
	 * 
	 * @param poolSize
	 *            specified pool size
	 * @param queue
	 *            instance of asynchronous queue
	 * @return instance of AsyncPerformerBean
	 */
	public AsyncPerformerBean(int poolSize, int queueSize, QueriesQueue<QueryContainer> queue, LockManagementSPI lockManagement, ActiveQueryBases aqb) {
		threadPool = new AsyncThreadPoolExecutor(poolSize, this, lockManagement, queueSize, aqb);
		//threadPool.setLockManagement();
		if (this.queue == null) {
			this.queue = queue;
			queue.addListener(this);
		}
	}

	/**
	 * Sets queue, whose elements will be performed asynchronously. Mostly must
	 * be used by the Spring IoC.
	 * 
	 * @param queue
	 *            of Async elements
	 * @return instance of AsyncPerformerBean
	 */
	public void setQueue(QueriesQueue<QueryContainer> queue) {
		if (this.queue == null) {
			this.queue = queue;
			queue.addListener(this);
		}
	}

	/**
	 * Returns queue, whose elements will be performed asynchronously.
	 * 
	 * @return queue of Async elements
	 */
	public QueriesQueue<QueryContainer> getQueue() {
		return this.queue;
	}

	@Override
	public void queriesAdded() {
		logger.debug("Listener notified");
		QueryContainer query = queue.getQuery();
		if (query == null) {
			if (logger.isDebugEnabled())
			logger.debug("Nothing to do \n --> Queue is empty.");
		} else {
			threadPool.submit(AsyncTask.getTask(query));
		}
	}

	/**
	 * Sets the {@link LogEventBean}<br>
	 * Mostly must be used by Spring IoC. <br>
	 * 
	 * @param logEventBean
	 * @throws DataException
	 */
	public void setLogEventBean(LogEventBean logEventBean) {
		this.logEventBean = logEventBean;
	}

	/**
	 * Returns the {@link LogEventBean}
	 * 
	 * @return {@link LogEventBean}
	 */
	public LogEventBean getLogEventBean() {
		return this.logEventBean;
	}

	/**
	 * Returns log to EVENT_BEAN property
	 * 
	 * @return true if needed logging
	 */
	public boolean isLogEvent() {
		return (logEventBean != null);
	}

	@Override
	@ManagedAttribute(description="Sets the max number of threads")
	public void setMaxPoolSize(int size) {
		threadPool.setCorePoolSize(size);
		threadPool.setMaximumPoolSize(size);
	}

	@Override
	@ManagedAttribute(description="The max number of threads in the pool")
	public int getMaxPoolSize() {
		return threadPool.getCorePoolSize();
	}

	@Override
	@ManagedAttribute(description="The current number of threads in the pool")
	public int getPoolSize() {
		return threadPool.getPoolSize();
	}

	@Override
	@ManagedAttribute(description="The approximate total number of tasks that have completed execution")
	public long getCompletedTaskCount() {
		return threadPool.getCompletedTaskCount();
	}

	@Override
	@ManagedAttribute(description="The approximate total number of tasks that have ever been scheduled for execution")
	public long getTaskCount() {
		return threadPool.getTaskCount();
	}

	@Override
	@ManagedAttribute(description="The approximate number of threads that are actively executing tasks")
	public long getActiveTaskCount() {
		return threadPool.getActiveCount();
	}
}
