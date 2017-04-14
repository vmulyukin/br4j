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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

@ManagedResource(objectName="br4j:name=resultStorage", description="MBean for ResultStorage of async tasks", log=true, logFile="jmx.log")
public class ResultStorageBean implements ResultStorage {

	/**
	 * access counter to the storage. Increases every 'put' or 'get' operation.
	 * If counter will overflow then will run the storage cleanup.
	 */
	private int queryCount;
	
	/**
	 * Time before entry expiration and treshold for storage (max size before cleanup).
	 * Be careful in setting large values, it may cause memory leak (A lot of objects
	 * will be stored in memory a long time)
	 */
	private long millisUntilExpiration;
	private int cleanupThreshold;
	
	private boolean useCleanup = true;
	private boolean enabled = true;
	private Map<Long, Entry> storage;
	private static final Log logger = LogFactory.getLog(ResultStorageBean.class);

	/*
	 * Entry that will contain this storage. Entry represents as time stamp of
	 * adding and the object to store itself.
	 */
	static class Entry {
		private long timestamp;
		private Future<Object> val;

		Entry(long timestamp, Future<Object> val) {
			this.timestamp = timestamp;
			this.val = val;
		}

		long timestamp() {
			return timestamp;
		}

		void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		Future<Object> val() {
			return val;
		}

		void setVal(Future<Object> val) {
			this.val = val;
		}
	}

	public ResultStorageBean() {
		storage = new LinkedHashMap<Long, Entry>() {

			private static final long serialVersionUID = -2744195668277961620L;

			/**
			 * Override method of {@link LinkedHashMap} that indicates that this
			 * storage should remove its eldest entry before inserting new
			 * entry.
			 */
			protected boolean removeEldestEntry(Map.Entry<Long, ResultStorageBean.Entry> eldest) {
				return size() > cleanupThreshold;
			}
		};
	}

	@ManagedOperation(description="Get the object from storage by key")
	  @ManagedOperationParameters({
	    @ManagedOperationParameter(name = "key", description = "The ID of async task")})
	public synchronized Future<Object> get(long key) {
		if (!enabled)
			return null;
		if (useCleanup && ++queryCount >= cleanupThreshold) {
			cleanup();
		}
		Entry entry = (Entry) storage.get(key);
		if (entry != null) {
			return entry.val();
		}
		return null;
	}

	public synchronized void put(long key, Future<Object> val) {
		if (!enabled) 
			return;
		if (useCleanup && ++queryCount >= cleanupThreshold) {
			cleanup();
		}
		Entry entry = (Entry) storage.get(key);
		if (entry != null) {
			entry.setTimestamp(System.currentTimeMillis());
			entry.setVal(val);
		} else {
			storage.put(key, new Entry(System.currentTimeMillis(), val));
		}
	}

	@Override
	public synchronized Future<Object> pull(long key) {
		Future<Object> obj = get(key);
		if (obj != null){
			storage.remove(key);
		}
		return obj;
	}

	@ManagedOperation(description="Clear storage. Removes all objects")
	public synchronized void clear() {
		storage.clear();
	}

	private Entry entryFor(long key) {
		Entry entry = (Entry) storage.get(key);
		if (entry != null && ((AsyncResult)entry.val).isSet() && entry.val.isDone()) {
			long delta = System.currentTimeMillis() - entry.timestamp();
			if (delta < 0 || delta >= millisUntilExpiration) {
				storage.remove(key);
				entry = null;
			}
		}
		return entry;
	}

	@ManagedOperation(description="Clean storage. Remove all out-of-date objects from storage. " +
			"Clean works if it is possible, that is boolean parameter useCleanup is set 'true'")
	public void cleanup() {
		logger.warn("Starting cleanup");
		Set<Long> keySet = storage.keySet();
		// Avoid ConcurrentModificationExceptions
		long[] keys = new long[keySet.size()];
		int i = 0;
		for (Iterator<Long> iter = keySet.iterator(); iter.hasNext();) {
			long key = iter.next().longValue();
			keys[i++] = key;
		}
		for (int j = 0; j < keys.length; j++) {
			entryFor(keys[j]);
		}
		queryCount = 0;
		logger.warn("Cleanup finished");
	}

	/* Getters / Setters */

	public Map<Long, Entry> getStorage() {
		return storage;
	}

	@ManagedAttribute(description="Gets the max size of the storage when cleanup process is being started")
	public int getCleanupThreshold() {
		return cleanupThreshold;
	}

	@ManagedAttribute(description="Sets the max size of the storage when cleanup process is being started")
	public void setCleanupThreshold(int cleanupThreshold) {
		this.cleanupThreshold = cleanupThreshold;
	}

	@ManagedAttribute(description="Gets the time in milliseconds which objects will store in storage")
	public long getMillisUntilExpiration() {
		return millisUntilExpiration;
	}

	@ManagedAttribute(description="Sets the time in milliseconds which objects will store in storage")
	public void setMillisUntilExpiration(long millisUntilExpiration) {
		this.millisUntilExpiration = millisUntilExpiration;
	}

	@ManagedAttribute(description="Sets the boolean flag which indicate to use cleanup for the storage in case it sets in 'true' value")
	public void setUseCleanup(boolean useCleanup) {
		this.useCleanup = useCleanup;
	}

	@ManagedAttribute(description="Gets the boolean flag which indicate to use cleanup for the storage in case it sets in 'true' value")
	public boolean isUseCleanup() {
		return useCleanup;
	}
	
	@ManagedAttribute(description="Enable/disable storage")
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@ManagedAttribute(description="Enable/disable storage")
	public boolean isEnabled() {
		return enabled;
	}

	@ManagedAttribute(description="Gets current size of the storage")
	public int getSize() {
		return storage.size();
	}

}
