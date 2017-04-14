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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

public class CacheConfig
{
	public static final String CONFIG_FILE = "dbmi/cmscache.properties";
	public static final String KEY_SIZE = "cache.size";
	public static final String KEY_PREFIX_TEMPLATE = "cache.template.";
	public static final String KEY_PREFIX_CHIDREN = "cache.linked.";
	public static final String KEY_PREFIX_SEARCH = "cache.search.";
	
	private static volatile CacheConfig instance;
	
	public static CacheConfig getConfig() {
		CacheConfig result = instance;
		if(result == null) {
			synchronized(CacheConfig.class) {
				result = instance;
				if(result == null)
					result = instance = new CacheConfig();
				}
			}
		return result;
	}
	
	public int getIntValue(String key, int def) {
		if (!config.containsKey(key))
			return def;
		String value = config.getProperty(key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			logger.warn("Error parsing value of " + key + ": " + value, e);
			return def;
		}
	}
	
	public Iterator<String> enumKeys(String prefix) {
		ArrayList<String> keys = new ArrayList<String>();
		for (Enumeration<?> names = config.propertyNames(); names.hasMoreElements(); ) {
			String key = (String) names.nextElement();
			if (key.startsWith(prefix))
				keys.add(key);
		}
		return keys.iterator();
	}

	protected Log logger = LogFactory.getLog(getClass());
	private Properties config = new Properties();

	private CacheConfig() {
		try {
			long start = System.currentTimeMillis();
			config.load(Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE));
			long duration = System.currentTimeMillis() - start;
			if(logger.isDebugEnabled()) {
				logger.debug("Creating a new CacheConfig instance");
				logger.debug("Cache config file was loaded in "+duration+" ms");
			}
		} catch (IOException e) {
			logger.error("Error reading cache configuration", e);
		}
	}
}
