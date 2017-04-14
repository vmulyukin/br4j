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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.CardHandler;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;

public class ChangeCardAction extends CardAction {

    private static final String CREATE_OBJECT_PARAM = "createObject";
    private static final String TYPE_PARAM = "objectType";

    private boolean isCreateNewObject;
    private Class<? extends WSObject> objectType;

    @Override
    public void setParameter(String key, Object value) {
	if (CREATE_OBJECT_PARAM.equals(key)) {
	    isCreateNewObject = Boolean.parseBoolean((String) value);
	} else if (TYPE_PARAM.equals(key)) {
	    objectType = ReflectionUtils.initializeClass(WSObject.class,
		    (String) value);
	} else {
	    super.setParameter(key, value);
	}
    }

    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	if (isConstructNewObject()) {
	    Collection<Card> cards = getFilteredCards(serviceBean, object);
	    for (Card card : cards) {
		WSObject constructedObject = createObject(card);
		updateCard(serviceBean, constructedObject);
	    }
	} else {
	    updateCard(serviceBean, object);
	}
    }

    protected void updateCard(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	CardHandler cardHandler = new CardHandler(serviceBean);
	cardHandler.updateCard(object);
    }

    protected boolean isConstructNewObject() {
	return getLink() != null || isCreateNewObject;
    }

    protected WSObject createObject(Card card) {
	WSObject object = createObject();
	object.setId(card.getId().getId().toString());
	return object;
    }

    protected WSObject createObject() {
	if (objectType != null) {
	    return ReflectionUtils.instantiateClass(objectType);
	}
	return new WSObject();
    }
}