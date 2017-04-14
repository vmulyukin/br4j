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
package com.aplana.owriter.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Time based cache: the entries will be removed after Maximum Age.
 * 
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */

public class TimeBasedCache
{
	protected final Log logger = LogFactory.getLog(getClass());
	/**
	 * The Maximum Age specifies the amount of time a cached
	 * copy of a resource can be used. If the cached copy of the 
	 * resource is older than the amount of time specified, the
	 * cached copy is removed. 
	 */
	private long maxAge;
	
	private ConcurrentMap < String, Item > store;
	
	/** 
	 * Time based cache Construct.
	 *
	 * @param maxAge maximum age of an entry in milliseconds, which
	 * must be positive
	 * @param cleanupInterval cache cleanup interval in milliseconds, which
	 * must be positive
	 */
	public TimeBasedCache( final long maxAge, final long cleanupInterval ) {
		this.maxAge = maxAge;
		this.store = new ConcurrentHashMap<String, Item>();
		
		if (maxAge > 0 && cleanupInterval > 0) {

            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(cleanupInterval);
                        } catch (InterruptedException ex) {
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
		}
	}
	
	/**
	 * Cache an object.
	 * 
	 * @param key unique identifier to retrieve object
	 * @param value object to cache
	 */
	public void put( final String key, final Object value ) {
		final Item item = new Item(value);
		store.put( key, item);
	}
	
	/**
	 * Fetch an object.
	 * 
	 * @param key unique identifier to retrieve object
	 * @return an object or null in case it isn't stored or it expired
	 */
	public Object get( final String key ) {
		final Item item = getItem( key );
		if ( null != item ) {
			return item.payload;
		}else {
			return null;
		}
	}
	
	/**
	 * Remove an object from cache.
	 * 
	 * @param key unique identifier to retrieve object
	 */
	public void remove( final String key ) {
		store.remove( key );
	}
	
	/**
	 * Get an item, if it expired remove it from cache and return null.
	 * 
	 * @param key unique identifier to retrieve object
	 * @return an item or null
	 */
	private Item getItem( final String key ){
		Item item = null;
		synchronized(store) {
			item = store.get( key ); 
			if (item == null) {
				return null;
			}
		
			if ( System.currentTimeMillis() > (maxAge + item.birth) ) {
				store.remove( key );
				return null;
			}
		}
		
		return item;		
	}
	
	/**
	 * Cleanup time-based cache, remove expired tokens.
	 * @return void
	 */
	private void cleanup() {

		ArrayList<String> deleteKey = new ArrayList<String>((store.size() / 2) + 1);
	 
		if (logger.isDebugEnabled()) {
			logger.debug("Starting time-based cache cleanup, max item age = " + maxAge);
		}
		for (Map.Entry<String, Item> e :store.entrySet()) {
			Item item = e.getValue();
			if (item != null && (System.currentTimeMillis() > (maxAge + item.birth))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Time-based cache cleanup: deleting expired item " 
							+ e.getKey() + " with birth = " + item.birth);
				}
				deleteKey.add(e.getKey());
			}
		}
	 
		for (String key : deleteKey) {
			store.remove(key);
			Thread.yield();
		}
	}

	/**
	 * Value container.
	 */
	private static class Item
	{
		final long birth;
		final Object payload;
		
		Item( Object payload ) 
		{
			this.birth = System.currentTimeMillis();
			this.payload = payload;
		}
	}
}
