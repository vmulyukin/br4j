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
import java.util.Date;

public class CacheEx extends Cache
{
	private ArrayList expiring = new ArrayList();
	private int size = 0;
	private int expires = 0;
	
	public CacheEx()
	{
		super();
	}
	
	public CacheEx(int capacity)
	{
		super(capacity);
	}
	
	private void expireItems()
	{
		Date now = new Date();
		while (expiring.size() > 0 && ((CacheableEx) expiring.get(0)).getExpirationTime().before(now))
		{
			remove((CacheableEx) expiring.get(0));
			expires++;
		}
	}
	
	public int getSize()
	{
		return size;
	}

	synchronized public boolean contains(Cacheable key) 
	{
		expireItems();
		return super.contains(key);
	}

	public synchronized Object get(Cacheable key) throws Exception
	{
		expireItems();
		return super.get(key);
	}

	public synchronized Object put(Cacheable key) throws Exception
	{
		expireItems();
		Object value = super.put(key);
		if (key instanceof CacheableEx && ((CacheableEx) key).getSize() > 1)
		{
			int itemSize = ((CacheableEx) key).getSize();
			size += itemSize;
			//while (getCapacity() - size < itemSize)
			push();
		}
		else
			size++;
		if (key instanceof CacheableEx && ((CacheableEx) key).getExpirationTime() != null)
		{
			Date expTime = ((CacheableEx) key).getExpirationTime();
			int i = expiring.size();
			while (i > 0 && expTime.before(((CacheableEx) expiring.get(i - 1)).getExpirationTime()))
				i--;
			expiring.add(i, key);
		}
		return value;
	}

	public synchronized void remove(Cacheable key)
	{
		expiring.remove(key);
		super.remove(key);
		int freed = 1;
		if (key instanceof CacheableEx && ((CacheableEx) key).getSize() > 1)
			freed = ((CacheableEx) key).getSize();
		size -= freed;
	}

	/*public int getPushes()
	{
		return super.getPushes() - expires;
	}*/
	
	public int getExpires()
	{
		return expires;
	}
}
