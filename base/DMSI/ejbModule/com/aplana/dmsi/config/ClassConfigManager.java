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
package com.aplana.dmsi.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dmsi.Configuration;

public class ClassConfigManager {

    private static ClassConfigManager instance = null;

    private Map<Class<?>, ClassConfig> configsByClass = new HashMap<Class<?>, ClassConfig>();

    private ClassConfigManager() throws ConfigurationException {
	String[] mappingFiles = Configuration.instance().getMappingFileNames();
	ClassesConfigReader reader = new ClassesConfigReader();
	for (String mappingConfigFile : mappingFiles) {
	    initializeConfigs(reader.getConfigs(mappingConfigFile));
	}
    }

    public synchronized static ClassConfigManager instance() {
	if (instance == null) {
	    instance = new ClassConfigManager();
	}
	return instance;
    }

    public ClassConfig getConfigByClass(Class<?> clazz) {
	if (!configsByClass.containsKey(clazz))
	    throw new IllegalArgumentException("There is no config for "
		    + clazz);
	return configsByClass.get(clazz);
    }

    protected void initializeConfigs(List<ClassConfig> configs)
	    throws ConfigurationException {
	for (ClassConfig conf : configs) {
	    if (configsByClass.containsKey(conf.getType())) {
		throw new ConfigurationException(
			"Two configurations for same type is defined: "
				+ conf.getType());
	    }
	    configsByClass.put(conf.getType(), conf);
	}
    }

}
