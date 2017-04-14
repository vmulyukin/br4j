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
package com.aplana.ireferent.card.handling;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.util.ServiceUtils;

class BackLinkAttributeHandler extends AttributeHandler {

    private final CardFacade cardFacade;
    private DataServiceBean handlerServiceBean;
    private Log logger = LogFactory.getLog(getClass());

    public BackLinkAttributeHandler(CardFacade cardFacade, Attribute attribute,
	    DataServiceBean serviceBean) {
	super(attribute);
	this.cardFacade = cardFacade;
	this.handlerServiceBean = serviceBean;
    }

    @Override
    public Object getAttributeValue() {
	Collection<ObjectId> linkedCards = ServiceUtils.getBackLinkedCards(
		handlerServiceBean, this.cardFacade.getCardId(), attribute
			.getId());
	return linkedCards.toArray(new ObjectId[linkedCards.size()]);
    }

    @Override
    public void setAttributeValue(Object value) {
	ObjectId sourceId = ((BackLinkAttribute) attribute).getLinkSource();
	if (value instanceof ObjectId) {
	    this.cardFacade.addParent((ObjectId) value, sourceId);
	} else if (value instanceof ObjectId[]) {
	    for (ObjectId targetId : (ObjectId[]) value) {
		this.cardFacade.addParent(targetId, sourceId);
	    }
	} else {
	    logger.error(String.format(
		    "Incorrect type '%s' of backlink value: %s", value
			    .getClass(), value));
	}
    }
}