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
package com.aplana.dbmi.module.notif;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.ConfigService;
import com.aplana.dbmi.Portal;

import freemarker.cache.TemplateLoader;

public class ConfigTemplateLoader implements TemplateLoader
{
	protected Log logger = LogFactory.getLog(getClass());
	private ConfigService configSvc = Portal.getFactory().getConfigService();
	
	public void closeTemplateSource(Object templateSource) throws IOException {
		((ConfigSource) templateSource).source.close();
	}

	public Object findTemplateSource(String name) throws IOException {
		InputStream source = null;
		try {
			source = configSvc.loadConfigFile("dbmi/mail/" + name);
		} catch (RuntimeException e) {
			// Will be handled below
		}
		if (source == null) {
			logger.warn("Error loading " + name + " mail template");
			return null;
		}
		return new ConfigSource(name, source);
	}

	public long getLastModified(Object templateSource) {
		return -1L;		// *****
	}

	public Reader getReader(Object templateSource, String encoding) throws IOException {
		return new InputStreamReader(((ConfigSource) templateSource).source, encoding);
	}
	
	private static class ConfigSource
	{
		private final InputStream source;
		private final String name;
		
		public ConfigSource(String name, InputStream source) {
			this.name = name;
			this.source = source;
		}

		public boolean equals(Object obj) {
			return obj != null && obj instanceof ConfigSource &&
				name.equalsIgnoreCase(((ConfigSource) obj).name);
		}
	}
}
