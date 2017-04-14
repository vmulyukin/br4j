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
package com.aplana.dmsi.card;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.handling.CardFacade;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.ClassConfigManager;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.object.ObjectParser;
import com.aplana.dmsi.object.ObjectParserConfigurator;
import com.aplana.dmsi.object.ObjectParser.AttributeValue;
import com.aplana.dmsi.types.DMSIObject;

public class CardHandler implements ObjectParserConfigurator {

    private DataServiceFacade serviceBean;
    private Log logger = LogFactory.getLog(getClass());

    public CardHandler(DataServiceFacade serviceBean) {
	this.serviceBean = serviceBean;
    }

    public ObjectId createCard(DMSIObject dmsiObject) throws DMSIException {
	CardFacade cardFacade = new CardFacade(serviceBean);
	fillCardFacadeUsingDMSIObject(cardFacade, dmsiObject);
	ClassConfig config = getConfig(dmsiObject);
	cardFacade.createCard(config.getTemplateId());
	return cardFacade.getCardId();
    }

    public void updateCard(DMSIObject dmsiObject) throws DMSIException {
	ObjectId cardId = getCardId(dmsiObject);
	CardFacade cardFacade = new CardFacade(serviceBean, cardId);
	fillCardFacadeUsingDMSIObject(cardFacade, dmsiObject);
	cardFacade.updateCard();
    }

    private void fillCardFacadeUsingDMSIObject(CardFacade cardFacade,
	    DMSIObject dmsiObject) throws DMSIException {
	ObjectParser parser = new ObjectParser(this);
	Collection<AttributeValue> objectValues = parser
		.parseValues(dmsiObject);
	for (AttributeValue attributeValue : objectValues) {
	    cardFacade.addAttributeValue(attributeValue.getAttributeId(),
		    attributeValue.getValue(), attributeValue
			    .getValueController());
	}
    }

    protected CardHandler newRelatedHandler() {
	CardHandler cardHandler = new CardHandler(serviceBean);
	return cardHandler;
    }

    public ClassConfig getConfig(DMSIObject object) {
	return ClassConfigManager.instance()
		.getConfigByClass(object.getClass());
    }

    public boolean isFieldIgnoring(FieldConfig fieldConfig) {
	return fieldConfig.isReadonly();
    }

    public DMSIObjectConverter getFieldConverter(FieldConfig fieldConfig) {
	DMSIObjectConverter complexValueConverter;
	if (fieldConfig.getComplexFieldConverter() != null) {
	    complexValueConverter = fieldConfig.getComplexFieldConverter();
	} else if (fieldConfig.isNewCard()) {
	    complexValueConverter = new SubObjectCreator();
	} else {
	    complexValueConverter = new CardIdGetter();
	}
	complexValueConverter.setServiceBean(serviceBean);
	return complexValueConverter;
    }

    private class CardIdGetter extends DMSIObjectConverter {
	public CardIdGetter() {
	}

	@Override
	protected ObjectId convert(DMSIObject value) throws DMSIException {
	    return getCardId(value);
	}
    }

    private class SubObjectCreator extends DMSIObjectConverter {
	public SubObjectCreator() {
	}

	@Override
	protected ObjectId convert(DMSIObject value) throws DMSIException {
	    return newRelatedHandler().createCard(value);
	}

	@Override
	protected boolean shouldTypeOfValueBeChecked() {
	    return true;
	}
    }

    protected ObjectId getCardId(DMSIObject obj) {
	String id = obj.getId();
	try {
	    return new ObjectId(Card.class, Long.parseLong(id));
	} catch (NumberFormatException ex) {
	    logger.error("Incorrect id of card (should be numeric): " + id);
	    return null;
	}
    }

}
