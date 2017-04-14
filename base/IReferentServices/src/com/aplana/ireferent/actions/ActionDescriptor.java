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
package com.aplana.ireferent.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aplana.ireferent.actions.conditions.Condition;
import com.aplana.ireferent.types.WSObject;

public class ActionDescriptor {

	private String title;
	private String id;
	private Class<? extends WSObject> objectType;
	private LinkedHashMap<Condition, List<ActionHandler>> handlersWithconditions = 
			new LinkedHashMap<Condition, List<ActionHandler>>();

    public String getTitle() {
	return this.title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getId() {
	return this.id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Class<? extends WSObject> getObjectType() {
	return this.objectType;
    }

    public void setObjectType(Class<? extends WSObject> objectType) {
	this.objectType = objectType;
    }

	public Map<Condition, List<ActionHandler>> getHandlersWithconditions() {
		return Collections.unmodifiableMap(handlersWithconditions);
	}

	public void addHandlerWithconditions(Condition condition, ActionHandler handler) {
		if (handlersWithconditions.containsKey(condition)) {
			List<ActionHandler> handlers = handlersWithconditions.get(condition);
			handlers.add(handler);
		} else {
			List<ActionHandler> handlers = new ArrayList<ActionHandler>();
			handlers.add(handler);
			handlersWithconditions.put(condition, handlers);
		}
	}

}
