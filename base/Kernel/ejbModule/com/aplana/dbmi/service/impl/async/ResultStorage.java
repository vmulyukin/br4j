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

import java.util.concurrent.Future;

/**
 * Interface for the {@link ResultStorageBean} MBean (spring beans configurable)
 * 
 * @author desu
 * 
 */
public interface ResultStorage {
	/**
	 * Get the object from storage by key
	 * 
	 * @param key
	 *            Id of the {@link QueryContainer}
	 * @return Result of the {@link QueryContainer} work
	 */
	public Future<Object> get(long key);

	/**
	 * Put the result of executed asynchronous query into storage
	 * 
	 * @param key
	 *            Id of the {@link QueryContainer}
	 * @param val
	 *            result of the {@link QueryContainer} work
	 */
	public void put(long key, Future<Object> val);

	/**
	 * Remove the object from storage by key
	 * 
	 * @param key
	 *            Id of the {@link QueryContainer}
	 * @return Result of the {@link QueryContainer} work
	 */
	public Future<Object> pull(long key);

	/**
	 * Clear storage. Removes all objects.
	 */
	public void clear();

	/**
	 * Clean storage. Remove all out-of-date objects from storage.
	 * Clean works if it is possible, that is boolean parameter useCleanup
	 * is set 'true'
	 */
	public void cleanup();

	/**
	 * Gets max size of the storage. If you will add object at full storage,
	 * then the eldest object from storage will be removed. 
	 * @return Maximum size of the storage.
	 */
	public int getCleanupThreshold();

	/**
	 * Sets the max size of the storage
	 * @param maxEntries count of the maximum possible saved entries
	 */
	public void setCleanupThreshold(int maxEntries);

	/**
	 * Gets the time in milliseconds which objects will store in storage
	 * @return Time in milliseconds until stored object has not been disappeared
	 */
	public long getMillisUntilExpiration();

	/**
	 * Sets the time in milliseconds which objects will store in storage
	 * @param millisUntilExpiration time in milliseconds
	 */
	public void setMillisUntilExpiration(long millisUntilExpiration);

	/**
	 * Sets the boolean flag which indicate to use cleanup for the storage in case it sets in 'true' value
	 * @param useCleanup flag to use cleanup ('true' - need to use)
	 */
	public void setUseCleanup(boolean useCleanup);

	/**
	 * Gets the boolean flag which indicate to use cleanup for the storage in case it sets in 'true' value
	 * @return flag which indicate to use cleanup ('true' - need to use)
	 */
	public boolean isUseCleanup();

	/**
	 * Gets current size of the storage.
	 * @return Count of current stored objects in storage.
	 */
	public int getSize();

}
