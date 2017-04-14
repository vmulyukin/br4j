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
package com.aplana.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName="br4j:name=staticCache", description="MBean for Cache", log=true, logFile="jmx.log")
public class Cache {
	private int capacity = 1024;
	private HashMap<Cacheable, Object> entries;
	private ArrayList<Cacheable> history = new ArrayList<Cacheable>();
	
	private int hits = 0;
	private int misses = 0;
	private int pushes = 0;
	private long maxCloningTime = 0;
	private long avgCloningTime = 0;
	
	public Cache() {
		entries = new HashMap<Cacheable, Object> ((capacity * 4 + 3) / 3);		// assuming load factor 0.75
	}
	
	public Cache(int capacity) {
		this.capacity = capacity;
		entries = new HashMap<Cacheable, Object> ((capacity * 4 + 3) / 3);
	}

	synchronized public boolean contains(Cacheable key) {
		return entries.containsKey (key);
	}

	synchronized public Object get(Cacheable key) throws Exception {
		if (entries.containsKey(key)) {
			hits++;
			history.remove(key);
			history.add(key);
			return entries.get(key);
		}
		misses++;
		return put(key);
	}
	
	public Object put(Cacheable key) throws Exception {
		Object value = key.getValue();
		synchronized (this) {
			history.remove(key);
			history.add(key);
			entries.put(key, value);
			push();
		}
		return value;
	}
	
	synchronized public void remove(Cacheable key) {
		if (!entries.containsKey(key))
			return;
		history.remove(key);
		entries.remove(key);
	}
	
	synchronized protected void push() {
		while (getSize() > capacity) {
			pushes++;
			Cacheable key = history.remove(0);
			entries.remove(key);
			remove(key);
		}
	}
	
	@ManagedAttribute(description="Hits in cache")
	public int getHits() {
		return hits;
	}
	
	@ManagedAttribute(description="Misses in cache")
	public int getMisses() {
		return misses;
	}
	
	@ManagedAttribute(description="Count of pushes")
	public int getPushes() {
		return pushes;
	}
	
	@ManagedAttribute(description="Set capacity")
	public synchronized void setCapacity(int cap) {
		this.capacity = cap;
		while (history.size() > capacity)
			remove(history.get(0));
	}
	
	@ManagedAttribute(description="Get capacity")
	public int getCapacity() {
		return capacity;
	}
	
	@ManagedAttribute(description="Current size")
	public int getSize() {
		return entries.size();
	}
	
	public void setCloningTime(long newTime) {
		//set max time
		if (this.maxCloningTime < newTime)
			this.maxCloningTime = newTime;
		
		//set avg
		if (this.avgCloningTime == 0)
			this.avgCloningTime = newTime;
		else 
			this.avgCloningTime = (this.avgCloningTime + newTime) / 2;
	}
	
	@ManagedAttribute(description="Max time of deepClone")
	public long getCloningTimeMax() {
		return maxCloningTime;
	}
	
	@ManagedOperation(description="Clear cache")
	public void clear() {
		synchronized (this) {
			history.clear();
			entries.clear();
		}
	}
	
	@ManagedAttribute(description="Avg cloning time")
	public long getCloningTimeAvg() {
		return avgCloningTime;
	}
}