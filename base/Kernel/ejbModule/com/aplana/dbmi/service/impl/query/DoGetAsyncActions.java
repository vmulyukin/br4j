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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.GetAsyncActions;
import com.aplana.dbmi.action.GetAsyncActions.ActionState;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.async.QueriesQueue;
import com.aplana.dbmi.service.impl.async.QueryContainer;

public class DoGetAsyncActions extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		@SuppressWarnings("unchecked")
		QueriesQueue<QueryContainer> queue = (QueriesQueue<QueryContainer>) getBeanFactory()
				.getBean(QueriesQueue.BEAN_ASYNC_QUEUE);

		List<QueryContainer> queries = null;

		GetAsyncActions action = getAction();

		if (action.getRunActions().equals(ActionState.RUNNING)) {
			queries = queue.getRunningQueries(getUser());
		} else if (action.getRunActions().equals(ActionState.WAITING)) {
			queries = queue.getWaitingQueries(getUser());
		} else if (action.getRunActions().equals(ActionState.REPEATED)) {
			queries = queue.getRepeatingQueries(getUser());
		} else if (action.getRunActions().equals(ActionState.WAITING_FOR_REPEAT)) {
			queries = queue.getWaitingRepeatQueries(getUser());
		}

		List<LogEntry> entries = new ArrayList<LogEntry>();
		if (queries != null)
			for (QueryContainer qc : queries) {
				QueryBase query = qc.getQuery();
				if (query.getLogEntry() != null) {
					if (qc.getPolicy() != null && qc.getPolicy().getRetryReason() != null) {
						entries.add(qc.getPolicy().getRetryReason());
					} else {
						entries.add(query.getLogEntry());
					}
				}
			}
		return entries;
	}

}
