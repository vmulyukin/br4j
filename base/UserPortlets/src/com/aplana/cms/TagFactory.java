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
package com.aplana.cms;

import java.util.Properties;

import org.apache.commons.logging.LogFactory;

public class TagFactory
{
	private static Properties tags = new Properties();
	
	static {
		try {
			tags.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/aplana/cms/tags.properties"));
		} catch (Exception e) {
			LogFactory.getLog(TagFactory.class).error("Error loading tags map", e);
		}
	}
	
	public static TagProcessor getProcessor(String type)
	{
		if (!tags.containsKey(type)) {
			LogFactory.getLog(TagFactory.class).error("No processor defined for tag " + type);
			return null;
		}
		String name = tags.getProperty(type);
		if (!name.contains("."))
			name = "com.aplana.cms.tags." + name;
		try {
			return (TagProcessor) Class.forName(name).newInstance();
		} catch (Exception e) {
			LogFactory.getLog(TagFactory.class).error("Error loading processor for tag " + type, e);
			return null;
		}
	}
}
