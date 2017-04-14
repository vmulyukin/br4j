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
package com.aplana.ireferent;

import java.lang.reflect.Field;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.ireferent.card.handling.CardFacade;
import com.aplana.ireferent.config.ClassConfig;
import com.aplana.ireferent.config.ClassConfigManager;
import com.aplana.ireferent.config.ExtensionConfig;
import com.aplana.ireferent.config.FieldConfig;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.value.filters.AllElementFilter;
import com.aplana.ireferent.value.filters.CollectionFilter;

public class EntityFactory extends WSObjectFactory {

    private CardFacade cardFacade;

    protected EntityFactory() {
    }

    @Override
    protected Object newWSObject(Card card) throws IReferentException {
	this.processingCard = card;
	Class<? extends WSObject> clazz = getObjectClassByTemlate(card
		.getTemplate());
	ClassConfig config = ClassConfigManager.instance().getConfigByClass(
		clazz);
	return newObject(config);
    }

    protected Object newObject(ClassConfig config) throws IReferentException {
	Class<?> clazz = config.getType();
	Object obj = ReflectionUtils.instantiateClass(clazz);

	cardFacade = new CardFacade(serviceBean, processingCard);
	for (Field field : ReflectionUtils.getFields(clazz)) {
	    final String fieldName = field.getName();
	    FieldConfig fieldConfig = config.getFieldConfig(fieldName);
	    FieldHandler fieldHandler = new FieldHandler(field, fieldConfig);
	    setFieldValue(fieldHandler, obj);
	}
	for (String extensionName : config.getExtensionNames()) {
	    ExtensionConfig extensionConfig = config
		    .getExtensionConfig(extensionName);
	    FieldHandler extensionHandler = new ExtensionHandler(
		    extensionConfig);
	    setFieldValue(extensionHandler, obj);
	}

	return obj;
    }

    private void setFieldValue(FieldHandler fieldHandler, Object obj)
	    throws IReferentException {

	FieldConfig fieldConfig = fieldHandler.getConfig();
	if (fieldConfig == null) {
	    return;
	}
	String fieldName = fieldConfig.getFieldName();
	if (isFieldIgnored(fieldName)) {
	    return;
	}

	ClassConfig subClassConfig = fieldConfig.getConfig();
	ObjectId attributeId = fieldConfig.getAttributeId();

	Object value = null;
	if (attributeId == null) {
	    value = newObject(subClassConfig);
	} else {
	    cardFacade.setAttributeValueController(attributeId, fieldConfig
		    .getValueController());
	    value = cardFacade.getAttributeValue(attributeId);
	}
	if (value == null) {
	    logger.warn(String.format("Value for %s field was not get",
		    fieldName));
	    return;
	}

	FieldConverter fieldConverter = new FieldConverter(fieldHandler
		.getFieldType(), fieldConfig, getSetId());
	value = fieldConverter.convert(value);
	fieldHandler.setFieldValue(obj, value);
    }

    private class FieldConverter {
    	private Class<?> fieldType;
    	private FieldConfig config;
    	private String setId;

    	public FieldConverter(Class<?> fieldType, FieldConfig config, String setId) {
    		this.fieldType = fieldType;
    		this.config = config;
    		this.setId = setId;
    	}

    	public Object convert(Object value) throws IReferentException {
    		if (value instanceof ObjectId[]) {
    			WSObjectFactory relatedFactory = createFactoryForField(config);
    			ObjectId[] ids = (ObjectId[]) value;

    			CollectionFilter commonFilter = config.getFilter() == null ? new AllElementFilter() : config.getFilter();
    			CollectionFilter setIdFilter = new AllElementFilter();
    			if (setId != null && !setId.isEmpty() && config.getFilter(setId) != null){
    				setIdFilter =  config.getFilter(setId);
    			}

    			ids = commonFilter.filterIds(ids);
    			ids = setIdFilter.filterIds(ids);
    			
    			WSOCollection values = relatedFactory.newWSOCollection(ids);
    			if (WSOCollection.class.isAssignableFrom(fieldType)) {
    				commonFilter.filterCollection(values);
    				setIdFilter.filterCollection(values);
    				return values;
    			}
    			return commonFilter.selectOneObject(values);
    		}
    		return value;
    	}
    }
}
