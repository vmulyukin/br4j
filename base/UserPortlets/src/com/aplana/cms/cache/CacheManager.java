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

public class CacheManager
{
	private static volatile CacheEx cache;
	
	public static CacheEx getCache() {
		CacheEx result = cache;
		if(result == null) {
			synchronized(CacheManager.class) {
				result = cache;
				if(result == null)
					result = cache = new CacheEx(CacheConfig.getConfig().getIntValue(CacheConfig.KEY_SIZE, 4096));
			}
		}
		return result;
	}
	
	public synchronized static void resetCache() {
		cache = null;
	}
	
	public static String getCacheStats() {
		if (cache == null)
			return "Cache not yet initialized";
		return cache.getSize() + " of " + cache.getCapacity() + " items stored; " +
			cache.getHits() + " hits, " + cache.getMisses() + " misses, " +
			cache.getPushes() + " pushouts, " + cache.getExpires() + " expirations, " + 
			"avg get time = " + cache.getGetTimeAvg() + ", avg put time = " + cache.getPutTimeAvg() +
			", avg remove time = " + cache.getRemoveTimeAvg() +
			", max get time = " + cache.getGetTimeMax() + ", max put time = " + cache.getPutTimeMax() +
			", max remove time = " + cache.getRemoveTimeMax();
	}
}
