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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.card.handling.CardFacade;
import com.aplana.ireferent.card.handling.TypedObjectId;
import com.aplana.ireferent.config.ClassConfig;
import com.aplana.ireferent.config.ClassConfigManager;
import com.aplana.ireferent.config.ExtensionConfig;
import com.aplana.ireferent.config.FieldConfig;
import com.aplana.ireferent.types.TypedLink;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ExtensionUtils;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.value.controllers.ValueController;

public class CardHandler {
    private DataServiceBean serviceBean;

    private Set<String> ignoringFields = new HashSet<String>();
    private Set<String> subCardFields = new HashSet<String>();
    private Log logger = LogFactory.getLog(getClass());

    public CardHandler(DataServiceBean serviceBean) {
	this.serviceBean = serviceBean;
    }

    public ObjectId createCard(WSObject wsObject) throws IReferentException {
	CardFacade cardFacade = new CardFacade(serviceBean);
	fillCardFacadeUsingWSObject(cardFacade, wsObject);
	ClassConfig config = ClassConfigManager.instance().getConfigByClass(
		wsObject.getClass());
	cardFacade.createCard(config.getTemplateId());
	return cardFacade.getCardId();
    }

    public void updateCard(WSObject wsObject) throws IReferentException {
	ObjectId cardId = getCardId(wsObject);
	CardFacade cardFacade = new CardFacade(serviceBean, cardId);
	fillCardFacadeUsingWSObject(cardFacade, wsObject);
	cardFacade.updateCard();
    }

    private void fillCardFacadeUsingWSObject(CardFacade cardFacade,
	    WSObject wsObject) throws IReferentException {
	Collection<AttributeValue> objectValues = parseValues(wsObject);
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

    private Collection<AttributeValue> parseValues(WSObject object)
	    throws IReferentException {
	ClassConfig config = ClassConfigManager.instance().getConfigByClass(
		object.getClass());
	Collection<AttributeValue> values = new HashSet<AttributeValue>();
	values.addAll(parseFieldValues(object, config));
	values.addAll(parseExtensions(object, config));
	return values;
    }

    private Collection<AttributeValue> parseFieldValues(Object object,
	    ClassConfig config) throws IReferentException {
	Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();
	Collection<Field> fields = ReflectionUtils.getFields(object.getClass());

	for (Field field : fields) {
	    final String fieldName = field.getName();
	    FieldConfig fieldConfig = config.getFieldConfig(fieldName);
	    FieldHandler fieldHandler = new FieldHandler(field, fieldConfig);
	    attributeValues.addAll(parseField(object, fieldHandler));
	}
	return attributeValues;
    }

    private Collection<AttributeValue> parseExtensions(WSObject object,
	    ClassConfig config) throws IReferentException {
	List<WSOItem> extensions = ExtensionUtils.getExtensions(object);
	Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();

	for (WSOItem extension : extensions) {
	    final String fieldName = extension.getId();
	    ExtensionConfig extensionConfig = config
		    .getExtensionConfig(fieldName);
	    ExtensionHandler extensionHandler = new ExtensionHandler(
		    extensionConfig);
	    attributeValues.addAll(parseField(extension, extensionHandler));
	}
	return attributeValues;
    }

    private Collection<AttributeValue> parseField(Object object,
	    FieldHandler fieldHandler) throws IReferentException {
	Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();

	FieldConfig fieldConfig = fieldHandler.getConfig();
	if (fieldConfig == null) {
	    return attributeValues;
	}

	String fieldName = fieldConfig.getFieldName();
	readFieldConfigInfo(fieldConfig);

	if (isFieldIgnoring(fieldName)) {
	    return attributeValues;
	}

	Object value = fieldHandler.getFieldValue(object);
	if (value == null) {
	    return attributeValues;
	}
	value = convertFieldValue(fieldName, value);

	final ObjectId attributeId = fieldConfig.getAttributeId();
	final ClassConfig subConfig = fieldConfig.getConfig();
	final ValueController valueController = fieldConfig
		.getValueController();

	if (attributeId != null) {
	    attributeValues.add(new AttributeValue(attributeId, value,
		    valueController));
	} else if (subConfig != null) {
	    Collection<AttributeValue> subObjectValues = parseFieldValues(
		    value, subConfig);
	    attributeValues.addAll(subObjectValues);
	}
	return attributeValues;
    }

    private void readFieldConfigInfo(FieldConfig fieldConfig) {
	if (fieldConfig.isReadonly()) {
	    addIgnoring(fieldConfig.getFieldName());
	}
	if (fieldConfig.isNewCard()) {
	    addSubCardFieldNames(fieldConfig.getFieldName());
	}
    }

    private void addIgnoring(String... fieldName) {
	ignoringFields.addAll(Arrays.asList(fieldName));
    }

    private void addSubCardFieldNames(String... fieldName) {
	subCardFields.addAll(Arrays.asList(fieldName));
    }

    private boolean isFieldIgnoring(String fieldName) {
	return ignoringFields.contains(fieldName);
    }

    private boolean isFieldNewCard(String fieldName) {
	return subCardFields.contains(fieldName);
    }

    private static class AttributeValue {
	private final ObjectId attributeId;
	private final Object value;
	private final ValueController valueController;

	public AttributeValue(ObjectId attributeId, Object value,
		ValueController valueController) {
	    this.attributeId = attributeId;
	    this.value = value;
	    this.valueController = valueController;
	}

	public ObjectId getAttributeId() {
	    return this.attributeId;
	}

	public Object getValue() {
	    return this.value;
	}

	public ValueController getValueController() {
	    return valueController;
	}
    }

    private Object convertFieldValue(String fieldName, Object value)
	    throws IReferentException {
	List<Object> data;
	if (value instanceof WSOCollection) {
	    data = ((WSOCollection) value).getData();
	} else {
	    data = Arrays.asList(value);
	}

	WSObjectConverter fieldConverter = getFieldConverter(fieldName);
	Set<ObjectId> cardIdsSet = new HashSet<ObjectId>();
	for (Object item : data) {
	    ObjectId objectCardId = fieldConverter.convert(item);
	    if (objectCardId != null) {
		cardIdsSet.add(objectCardId);
	    }
	}

	if (cardIdsSet.isEmpty()) {
	    return value;
	}
	return cardIdsSet.toArray(new ObjectId[cardIdsSet.size()]);
    }

    private WSObjectConverter getFieldConverter(String fieldName) {
	WSObjectConverter processor;
	if (isFieldNewCard(fieldName)) {
	    processor = new SubObjectCreator();
	} else {
	    processor = new CardIdGetter();
	}
	return processor;
    }

    private abstract class WSObjectConverter {
	public WSObjectConverter() {
	}

	public ObjectId convert(Object value) throws IReferentException {
	    if (value instanceof WSObject) {
		return convert((WSObject) value);
	    } else if (value instanceof TypedLink) {
		TypedLink link = (TypedLink) value;
		ObjectId id = convert(link.getObject());
		return new TypedObjectId(id, link.getType().getId());
	    }
	    return null;
	}

	protected abstract ObjectId convert(WSObject value)
		throws IReferentException;

    }

    private class CardIdGetter extends WSObjectConverter {
	public CardIdGetter() {
	}

	@Override
	protected ObjectId convert(WSObject value) throws IReferentException {
	    return getCardId(value);
	}
    }

    private class SubObjectCreator extends WSObjectConverter {
	public SubObjectCreator() {
	}

	@Override
	protected ObjectId convert(WSObject value) throws IReferentException {
	    return createSubObjectAndReturnId(value);
	}
    }

    protected ObjectId createSubObjectAndReturnId(WSObject obj)
	    throws IReferentException {
	return newRelatedHandler().createCard(obj);
    }

    protected ObjectId getCardId(WSObject obj) {
	String id = obj.getId();
	try {
	    return new ObjectId(Card.class, Long.parseLong(id));
	} catch (NumberFormatException ex) {
	    logger.error("Incorrect id of card (should be numeric): " + id);
	    return null;
	}
    }
}
