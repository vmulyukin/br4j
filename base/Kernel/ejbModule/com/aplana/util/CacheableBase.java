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

import java.util.Date;

abstract public class CacheableBase implements CacheableEx
{
	protected Object id;
	protected int weight = 1;
	protected Date expiration = null;
	
	protected CacheableBase(Object id)
	{
		if (id == null)
			throw new IllegalArgumentException("Cache item's key can't be null");
		this.id = id;
	}
	
	protected void setExpiration(int interval)
	{
		if (interval < 0)
			expiration = null;
		else
			expiration = new Date(System.currentTimeMillis() + interval * 1000);
	}

	public Date getExpirationTime()
	{
		return expiration;
	}

	public int getSize()
	{
		return weight;
	}

	public int hashCode()
	{
		return id.hashCode();
	}

	public boolean equals(Object obj)
	{
		if (obj == null || !obj.getClass().equals(this.getClass()))
			return false;
		return id.equals(((CacheableBase) obj).id);// obj.hashCode() == hashCode();
	}
}
