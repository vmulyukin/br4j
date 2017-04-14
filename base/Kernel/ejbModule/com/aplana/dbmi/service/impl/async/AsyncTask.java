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

import java.util.concurrent.FutureTask;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.Database;

/**
 * Future task wrapper for {@link AsyncPerformer}. Can save
 * DataException
 * 
 * 
 */
public class AsyncTask extends FutureTask<Object> {

	private AsyncPerformer performer;

	private AsyncTask(AsyncPerformer p) {
		super(p, null);
		performer = p;
	}

	public static AsyncTask getTask(QueryContainer query) {
		AsyncPerformer p = new AsyncPerformer(query);
		AsyncTask task = new AsyncTask(p);
		p.setTask(task);
		return task;
	}

	public QueryContainer getQueryContainer() {
		return performer.getQueryContainer();
	}

	/**
	 * Performer for asynchronous
	 * {@link com.aplana.dbmi.service.impl.async.QueryContainer queries}.
	 */
	private static class AsyncPerformer implements Runnable {

		private QueryContainer queryContainer;
		private AsyncTask task;
		public AsyncPerformer(QueryContainer query) {
			this.queryContainer = query;
		}
		
		public void setTask(AsyncTask task) {
			this.task = task;
		}

		/**
		 * Returns performed
		 * {@link com.aplana.dbmi.service.impl.async.QueryContainer query}
		 * 
		 * @return query
		 */
		public QueryContainer getQueryContainer() {
			return this.queryContainer;
		}

		@Override
		public void run() {

			try {
				task.set(((Database) queryContainer.getBeanFactory().getBean(
						DataServiceBean.BEAN_DATABASE)).executeQuery(queryContainer
						.getQuery().getUser(), queryContainer.getQuery()));
			} catch (DataException e) {
				task.setException(e);
			}
		}
	}
}
