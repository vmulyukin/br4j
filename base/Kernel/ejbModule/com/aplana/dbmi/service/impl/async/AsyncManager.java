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

/**
 * Label class for determinate async/sync queries If {@link #getThreadLabel()}
 * returns 'null' then not decided yet, if returns '-1' then current thread is
 * synchronous, otherwise thread is asynchronous.
 */
public class AsyncManager {

	private static ThreadLocal<Long> rootQuery = new InheritableThreadLocal<Long>();
	private static ThreadLocal<ObjectId> rootQueryBaseUid = new InheritableThreadLocal<ObjectId>();

	public static void setThreadLabel(long id) {
		rootQuery.set(id);
	}

	public static void unsetThreadLabel() {
		rootQuery.remove();
	}

	/**
	 * Returns async or sync current thread
	 * 
	 * @return 'null' if not decided yet, '-1' if current thread is synchronous,
	 *         otherwise thread is asynchronous and returns query cointainer id.
	 */
	public static Long getThreadLabel() {
		return rootQuery.get();
	}
	
	public static void setQueryUid(ObjectId id) {
		rootQueryBaseUid.set(id);
	}
	
	public static ObjectId getQueryUid() {
		return rootQueryBaseUid.get();
	}
	
	public static void unsetQueryUid() {
		rootQueryBaseUid.remove();
	}
}
