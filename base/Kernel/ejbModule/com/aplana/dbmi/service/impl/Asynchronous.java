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

/**
 * Interface defining a conditional clause in {@link QueryFactory} config file.
 * Could be used to define the runtime method query processing (sync or async)
 * and async performing policy.
 * 
 * @see BasePropertySelector
 */
public interface Asynchronous {

	/**
	 * Returns true if given object must be created to performed asynchronously
	 * 
	 * @return true, if asynchronously, null if not set
	 */
	public Boolean isAsync();

	/**
	 * Sets asynchronous perform of this object
	 * 
	 * @param async
	 *            true, if asynchronously
	 */
	public void setAsync(Boolean async);

	/**
	 * Sets the policy name which may perform on specific asynchronous query
	 * 
	 * @param policy
	 */
	public void setPolicyName(String policy);

	/**
	 * Returns name of policy which may perform on specific asynchronous query.
	 * May be null( nothing to do)
	 * 
	 * @return
	 */
	public String getPolicyName();
	
	/**
	 * Returns priority of query. Used by AsynqQueriesQueue
	 * @return Integer value, greter than 0. 0 - the minimal priority.
	 */
	public Integer getPriority();
	
	/**
	 * Sets priority for query.
	 * @param i Minimum - 0. Maximum - unlimited;
	 */
	public void setPriority(Integer i);
}
