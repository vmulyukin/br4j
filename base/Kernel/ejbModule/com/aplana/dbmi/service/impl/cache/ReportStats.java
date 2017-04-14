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

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.util.Cache;
import com.aplana.util.CacheEx;

public class ReportStats extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	public Object processQuery() throws DataException {
		Cache cache = (Cache) getBeanFactory().getBean("cache");
		StringBuilder sb = new StringBuilder();
		sb.append(cache.getSize()).append(" of ");
		sb.append(cache.getCapacity()).append(" items stored; ");
		sb.append(cache.getHits()).append(" hits, ");
		sb.append(cache.getMisses()).append(" misses, ");
		sb.append(cache.getPushes()).append(" pushouts, ");
		sb.append(cache.getCloningTimeMax()).append("ms max clone time, ");
		sb.append(cache.getCloningTimeAvg()).append("ms avg clone time");
		if (cache instanceof CacheEx)
			sb.append(", ").append(((CacheEx)cache).getExpires()).append(" expirations");
		logger.info(sb.toString());
		return null;
	}

}
