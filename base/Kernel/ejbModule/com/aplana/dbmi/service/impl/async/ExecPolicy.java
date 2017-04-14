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

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import com.aplana.dbmi.model.InfoMessage;

/**
 * Policy about what do with
 * {@link com.aplana.dbmi.service.impl.async.QueryContainer queries}, which
 * throws exceptions during performing in
 * {@link com.aplana.dbmi.service.impl.async.QueriesPerformer Performers}
 */
public interface ExecPolicy extends Serializable {

	/**
	 * Sets the query to which to apply this policy
	 * 
	 * @param q
	 *            query
	 */
	void setQuery(QueryContainer q);

	/**
	 * Sets queue within with this policy is working
	 * 
	 * @param q
	 *            working queue
	 */
	void setQueue(QueriesQueue<QueryContainer> q);

	/**
	 * Apply this policy to query
	 */
	void performPolicy();

	/**
	 * Check condition of this policy
	 */
	boolean checkApplicability();
	
	/**
	 * Sets the reason of start policy
	 * @param e
	 */
	void setRetryReason(ExecutionException e);
	
	/**
	 * Description message why this policy run
	 * @return
	 */
	InfoMessage getRetryReason();

}
