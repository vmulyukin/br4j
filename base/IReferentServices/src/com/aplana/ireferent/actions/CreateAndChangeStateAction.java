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

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.config.ActionsConfigReader;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;

public class CreateAndChangeStateAction implements ActionHandler {

    private static final String CHANGE_STATE_CLASS_PARAM = "changeStateAction";
    private static final String CREATE_CARD_CLASS_PARAM = "createCardAction";

    private Map<String, Object> parameters = new TreeMap<String, Object>();

    public void setParameter(String key, Object value) {
	parameters.put(key, value);
    }

    protected ChangeStateAction newChangeStateAction() {
	ChangeStateAction action;
	if (parameters.containsKey(CHANGE_STATE_CLASS_PARAM)) {
	    action = ReflectionUtils.instantiateClass(ChangeStateAction.class,
		    ActionsConfigReader.ACTIONS_PACKAGE
			    + parameters.get(CHANGE_STATE_CLASS_PARAM));
	} else {
	    action = new ChangeStateAction();
	}
	initializeAction(action);
	return action;
    }

    protected CreateCardAction newCreateCardAction() {
	CreateCardAction action;
	if (parameters.containsKey(CREATE_CARD_CLASS_PARAM)) {
	    action = ReflectionUtils.instantiateClass(CreateCardAction.class,
		    ActionsConfigReader.ACTIONS_PACKAGE
			    + parameters.get(CREATE_CARD_CLASS_PARAM));
	} else {
	    action = new CreateCardAction();
	}
	initializeAction(action);
	return action;
    }

    private void initializeAction(ActionHandler action) {
	for (Entry<String, Object> parameter : parameters.entrySet()) {
	    action.setParameter(parameter.getKey(), parameter.getValue());
	}
    }

    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	CreateCardAction createAction = newCreateCardAction();
	createAction.doAction(serviceBean, object);
	ObjectId cardId = createAction.getCardId();
	WSObject createdCardObject = new WSObject();
	createdCardObject.setId(String.valueOf(cardId.getId()));
	ChangeStateAction changeStateAction = newChangeStateAction();
	changeStateAction.doAction(serviceBean, createdCardObject);
    }
}
