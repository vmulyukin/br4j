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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dmsi.Configuration;

public class TypesManager {

    private static TypesManager instance = null;
    private static Log logger = LogFactory.getLog(TypesManager.class);

    private Map<String, Type> typesByName = new HashMap<String, Type>();

    private TypesManager() throws ConfigurationException {
	TypesConfigReader reader = new TypesConfigReader();
	List<Type> readedTypes = new ArrayList<Type>();
	String[] typesFiles = Configuration.instance().getTypesFileNames();
	for (String typesConfigFile : typesFiles) {
	    readedTypes.addAll(reader.getConfigs(typesConfigFile));
	}
	for (Type type : readedTypes) {
	    if (typesByName.containsKey(type.getName())) {
		throw new ConfigurationException(
			"Two types with same name is defined: "
				+ type.getName());
	    }
	    typesByName.put(type.getName(), type);
	}
    }

    public synchronized static TypesManager instance() throws ConfigurationException {
	if (instance == null) {
	    instance = new TypesManager();
	}
	return instance;
    }

    public Type getTypeByName(String name) throws ConfigurationException {
	if (name == null || "".equals(name)) {
	    throw new IllegalArgumentException("Name should be not empty");
	}

	String mode = Configuration.getCurrentMode();
	String qualifiedName;
	if (mode != null && typesByName.containsKey(qualifiedName = mode + "." + name)) {
		if (logger.isDebugEnabled()) {
			logger.debug("Type name [" + qualifiedName + "] is returned");
		}
		return typesByName.get(qualifiedName);
	}

	if (logger.isDebugEnabled()) {
		logger.debug(" Trying to get type [" + name + "]");
	}

	if (!typesByName.containsKey(name)) {
	    throw new IllegalArgumentException(
		    "There is no type for given name " + name);
	}
	return typesByName.get(name);
    }
}
