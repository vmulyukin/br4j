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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.actions.conditions.Condition;
import com.aplana.ireferent.config.ActionsConfigReader;
import com.aplana.ireferent.types.WSOFormAction;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ExtensionUtils;

public class ActionsManager {

    private static ActionsManager instance = null;

    private static final String ACTIONS_FILE = "actions.xml";

    private Map<String, ActionDescriptor> actions = new HashMap<String, ActionDescriptor>();

    public static ActionsManager instance() {
		if (instance == null) {
		    instance = new ActionsManager();
		}
		return instance;
    }

    private ActionsManager() {
		ActionsConfigReader configReader = new ActionsConfigReader();
		List<ActionDescriptor> descriptors = configReader
			.getConfigs(ACTIONS_FILE);
		for (ActionDescriptor descriptor : descriptors) {
		    actions.put(descriptor.getId(), descriptor);
		}
    }

    public ActionDescriptor getActionDescriptorById(String id) {
		if (!actions.containsKey(id))
		    throw new IllegalArgumentException("There is no action with id "
			    + id);
		return actions.get(id);
    }

    public void doAction(DataServiceBean serviceBean, WSObject object,
	    WSOFormAction action) throws IReferentException {
		ActionDescriptor actionDescriptor = getActionDescriptorById(action
			.getId());
		if (!actionDescriptor.getObjectType().isAssignableFrom(
			object.getClass())) {
		    final String errorMessageFormat = "Incorrect type of action argument. Config: %s, Actual: %s";
		    throw new IllegalArgumentException(String.format(
			    errorMessageFormat, actionDescriptor.getObjectType(),
			    object.getClass()));
		}
	
		Map<Condition, List<ActionHandler>> handlersWithconditions = actionDescriptor.getHandlersWithconditions();
		List<WSOItem> extensions = ExtensionUtils.getExtensions(action);
		for(Condition condition : handlersWithconditions.keySet()) {
			if (condition.isPerformed(object, action, serviceBean)) {
				for (ActionHandler handler : handlersWithconditions.get(condition)) {
				    initializeHandlerByExtensions(handler, extensions);
				    handler.doAction(serviceBean, object);
				}
			}
		}
    }

    private void initializeHandlerByExtensions(ActionHandler handler,
	    Collection<WSOItem> extensions) {
		for (WSOItem extension : extensions) {
		    String extensionId = extension.getId();
		    Collection<Object> values = ExtensionUtils
			    .getExtensionValues(extension);
		    for (Object value : values) {
			handler.setParameter(extensionId, value);
		    }
		}
    }
}
