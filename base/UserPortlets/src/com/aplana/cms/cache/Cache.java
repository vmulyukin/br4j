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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Cache
{
	private int capacity = 24;
	private Map<Cacheable, Object> entries;
	private Queue<Cacheable> history;

	private int hits = 0;
	private int misses = 0;
	private int pushes = 0;

	public Cache() {
		entries = new ConcurrentHashMap<Cacheable, Object>();
		history = new ConcurrentLinkedQueue<Cacheable>();
	}

	public Cache(int capacity) {
		this();
		this.capacity = capacity;
	}

	public int getCapacity ()
	{
		return capacity;
	}

	public int getSize ()
	{
		return entries.size();
	}

	synchronized public void setCapacity (int capacity)
	{
		this.capacity = capacity;
		while (history.size() > capacity)
			remove(history.poll());
	}

	public Object get (Cacheable key) throws Exception
	{
		Object val = entries.get(key);
		if (val != null)
		{
			hits++;
			history.remove(key);
			history.add(key);
			return val;
		}
		misses++;
		return put (key);
	}

	public Object put (Cacheable key/*, Object value*/) throws Exception
	{
		if (entries.containsKey (key))
			history.remove(key);
		Object value = key.getValue();
		history.add(key);
		entries.put(key, value);
		while (history.size() > capacity)
			push();
		return value;
	}

	public void remove (Cacheable key)
	{
		if (!entries.containsKey (key))
			return;
		entries.remove(key);
	}

	protected void push ()
	{
		pushes++;
		Cacheable val = history.poll();
		if (val != null) {
			entries.remove(val);
		}
	}

	public int getHits ()
	{
		return hits;
	}

	public int getMisses ()
	{
		return misses;
	}

	public int getPushes ()
	{
		return pushes;
	}
}