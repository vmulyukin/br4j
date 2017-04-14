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
package com.aplana.cms.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class CacheEx extends Cache
{
	private Log logger = LogFactory.getLog(getClass());

	private Queue<CacheableEx> expiring = new PriorityBlockingQueue<CacheableEx>(11, new Comparator<CacheableEx>() {
		@Override
		public int compare(CacheableEx o1, CacheableEx o2) {
			return (int)(o1.getExpirationTime().getTime() - o2.getExpirationTime().getTime());
		}
	});
	private int expires = 0;

	private long maxGetTime = 0;
	private long avgGetTime = 0;

	private long maxRemoveTime = 0;
	private long avgRemoveTime = 0;

	private long maxPutTime = 0;
	private long avgPutTime = 0;

	public CacheEx()
	{
		super();
	}

	public CacheEx(int capacity)
	{
		super(capacity);
	}

	private void expireItems() {
		Date now = new Date();
		while (expiring.size() > 0 && expiring.peek().getExpirationTime().before(now)) {
			Cacheable val = expiring.poll();
			if (val != null) {
				remove(val);
				expires++;
			}
		}
	}

	public Object get(Cacheable key) throws Exception {
		long t1 = System.currentTimeMillis();
		expireItems();
		Object result = super.get(key);
		long t2 = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Caching: Inside CacheEx: Got key (" + key + ") from cache. Get time: " + (t2 - t1) + " ms");
		}
		return result;
	}

	public Object put(Cacheable key) throws Exception {
		long t1 = System.currentTimeMillis();
		expireItems();
		Object value = super.put(key);
		if (key instanceof CacheableEx && ((CacheableEx) key).getExpirationTime() != null) {
			expiring.offer((CacheableEx) key);
		}
		long t2 = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Caching: Inside CacheEx: Put key (" + key + ") to cache. Put time: " + (t2 - t1) + " ms");
			setPutTime(t2-t1);
		}
		return value;
	}

	public void remove(Cacheable key) {
		long t1 = System.currentTimeMillis();
		super.remove(key);
		if (key instanceof CacheableEx) {
			expiring.remove(key);
		}
		long t2 = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Caching: Inside CacheEx: Remove key (" + key + ") from cache. Remove time: " + (t2 - t1) + " ms");
		}
	}

	/*public int getPushes()
	{
		return super.getPushes() - expires;
	}*/

	public int getExpires()
	{
		return expires;
	}

	public void setGetTime(long newTime) {
		//set max time
		this.maxGetTime = setMaxTime(this.maxGetTime, newTime );

		//set avg
		this.avgGetTime = setAvgTime(this.avgGetTime, newTime );
	}

	public long getGetTimeMax() {
		return maxGetTime;
	}

	public long getGetTimeAvg() {
		return avgGetTime;
	}

	public void setRemoveTime(long newTime) {
		//set max time
		this.maxRemoveTime = setMaxTime(this.maxRemoveTime, newTime );

		//set avg
		this.avgRemoveTime = setAvgTime(this.avgRemoveTime, newTime );
	}

	public long getRemoveTimeMax() {
		return maxRemoveTime;
	}

	public long getRemoveTimeAvg() {
		return avgRemoveTime;
	}

	public void setPutTime(long newTime) {
		//set max time
		this.maxPutTime = setMaxTime(this.maxPutTime, newTime );

		//set avg
		this.avgPutTime = setAvgTime(this.avgPutTime, newTime );
	}

	public long setMaxTime(long oldTime, long newTime) {
		//set max time
		if (oldTime < newTime)
			oldTime = newTime;

		return oldTime;
	}

	public long setAvgTime(long oldTime, long newTime) {
		//set avg
		if (oldTime == 0)
			oldTime = newTime;
		else
			oldTime = (oldTime + newTime) / 2;

		return oldTime;
	}


	public long getPutTimeMax() {
		return maxPutTime;
	}

	public long getPutTimeAvg() {
		return avgPutTime;
	}
}
