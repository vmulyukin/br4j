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
package com.aplana.dbmi.service.impl.cache;

import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.util.Cache;
import com.aplana.util.CacheableEx;
import com.rits.cloning.Cloner;

public class CachingObjectQuery extends ObjectQueryBase implements CacheableEx {
	private static final long serialVersionUID = 1L;
	private static Cloner cloner = null;
	private ObjectQueryBase delegate;
	private int lifeTime;
	private long timestamp;
	
	public CachingObjectQuery(ObjectQueryBase delegate, int lifeTime) {
		if (delegate == null)
			throw new IllegalArgumentException();
		this.delegate = delegate;
		this.lifeTime = lifeTime;
	}
	
	public void setId(ObjectId id) {
		super.setId(id);
		delegate.setId(id);
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		delegate.setJdbcTemplate(jdbc);
	}

	public void setUser(UserData user) {
		super.setUser(user);
		delegate.setUser(user);
	}
	
	private Cloner getCloner() {
		if (cloner == null) {
			cloner = (Cloner) getBeanFactory().getBean("cloner");
		}
		return cloner;
	}

	public Object processQuery() throws DataException {
		logger.info("Fetch object " + getId());
		Cache cache = (Cache) getBeanFactory().getBean("cache");
		try {
			DataObject obj = (DataObject) cache.get(this);
			return cloneObject(obj, cache);
		} catch (DataException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unexpected exception", e);
			throw new DataException("general.runtime",
					new Object[] { DataException.RESOURCE_PREFIX + delegate.getClass().getName(), e.getMessage() });
		}
	}

	public String getEvent() {
		return delegate.getEvent();
	}

	public Date getExpirationTime() {
		return new Date(timestamp + lifeTime * 1000);
	}

	public int getSize() {
		return 1;
	}

	public Object getValue() throws Exception {
		logger.info("Executing " + delegate.getClass().getSimpleName());
		Object result = delegate.processQuery();
		timestamp = System.currentTimeMillis();
		return result;
	}

	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass()))
			return false;
		return getId().equals(((CachingObjectQuery) obj).getId());
	}

	public int hashCode() {
		return getId().hashCode();
	}

	private DataObject cloneObject(DataObject object, Cache cache)
	{
		long t1 = System.currentTimeMillis();
		DataObject res = getCloner().deepClone(object);
		long t2 = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug("Cloning time: " + (t2-t1) + "ms");
		cache.setCloningTime(t2-t1);
		return res;
	}
}
