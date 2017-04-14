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
package com.aplana.ireferent.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypesManager {

    private static TypesManager instance = null;

    private Map<String, Type> typesByName = new HashMap<String, Type>();

    private TypesManager() throws ConfigurationException {
	TypesConfigReader reader = new TypesConfigReader();
	List<Type> readedTypes = reader.getConfigs("types.xml");
	for (Type type : readedTypes) {
	    typesByName.put(type.getName(), type);
	}
    }

    public static TypesManager instance() throws ConfigurationException {
	if (instance == null) {
	    instance = new TypesManager();
	}
	return instance;
    }

    public Type getTypeByName(String name) throws ConfigurationException {
	if (name == null || "".equals(name)) {
	    throw new IllegalArgumentException("Name should be not empty");
	}

	if (!typesByName.containsKey(name)) {
	    throw new IllegalArgumentException(
		    "There is no type for given name " + name);
	}
	return typesByName.get(name);
    }
}
