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

import java.util.Collection;
import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.util.Cache;
import com.aplana.util.CacheableEx;
import com.rits.cloning.Cloner;

public class CachingChildrenQuery extends ChildrenQueryBase implements CacheableEx {
	private static final long serialVersionUID = 1L;
	private static Cloner cloner = null;
	private ChildrenQueryBase delegate;
	private int lifeTime;
	private long timestamp;
	private int size;
	
	public CachingChildrenQuery(ChildrenQueryBase delegate, int lifeTime) {
		if (delegate == null)
			throw new IllegalArgumentException();
		this.delegate = delegate;
		this.lifeTime = lifeTime;
	}

	public void setParent(ObjectId parent) {
		super.setParent(parent);
		delegate.setParent(parent);
	}
	
	public void setFilter(Filter filter) throws DataException {
		super.setFilter(filter);
		delegate.setFilter(filter);
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
		logger.info("List children by " + delegate.getClass().getSimpleName() + " for " + getParent());
		Cache cache = (Cache) getBeanFactory().getBean("cache");
		try {
			Collection<?> result = (Collection<?>) cache.get(this);
			return cloneResult(result, cache);
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
		return size;
	}

	public Object getValue() throws Exception {
		logger.info("Executing " + delegate.getClass().getSimpleName());
		Object result = delegate.processQuery();
		timestamp = System.currentTimeMillis();
		if (result instanceof Collection)
			size = ((Collection<?>) result).size();
		return result;
	}

	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass()))
			return false;
		return delegate.getClass().equals(((CachingChildrenQuery) obj).delegate.getClass()) &&
				getParent().equals(((CachingChildrenQuery) obj).getParent()) &&
				(getFilter() != null) ? getFilter().equals(((CachingChildrenQuery) obj).getFilter()) : 
				null == ((CachingChildrenQuery) obj).getFilter();
	}

	public int hashCode() {
		return delegate.getClass().getName().hashCode() ^ getParent().hashCode() ^
				(getFilter() != null ? getFilter().hashCode() : 12345678);
	}

	private Collection<?> cloneResult(Collection<?> result, Cache cache)
	{
		long t1 = System.currentTimeMillis();
		Collection<?> res = getCloner().deepClone(result);
		long t2 = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug("Cloning time: " + (t2-t1) + "ms");
		cache.setCloningTime(t2-t1);
		return res;
	}	

	protected boolean supportsFilter(Class<?> type) {
		return true;
	}
}
