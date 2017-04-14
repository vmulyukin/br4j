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
package com.aplana.dmsi.object;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.card.DMSIObjectConverter;
import com.aplana.dmsi.card.OneToManyDMSIObjectConverter;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ReflectionUtils;
import com.aplana.dmsi.value.controllers.ValueController;

public class ObjectParser {

	private final ObjectParserConfigurator objectParserConfigurator;

	public ObjectParser(ObjectParserConfigurator objectParserConfigurator) {
		this.objectParserConfigurator = objectParserConfigurator;
	}

	public Collection<AttributeValue> parseValues(DMSIObject object) throws DMSIException {
		Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();
		ClassConfig config = objectParserConfigurator.getConfig(object);
		attributeValues.addAll(parseFieldValues(object, config));
		return attributeValues;
	}

	private Collection<AttributeValue> parseFieldValues(Object object, ClassConfig config) throws DMSIException {
		Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();
		Collection<Field> fields = ReflectionUtils.getAllFields(object.getClass());

		for (Field field : fields) {
			final String fieldName = field.getName();
			FieldConfig fieldConfig = config.getFieldConfig(fieldName);
			FieldHandler fieldHandler = new FieldHandler(field, fieldConfig);
			attributeValues.addAll(parseField(object, fieldHandler));
		}
		return attributeValues;
	}

	private Collection<AttributeValue> parseField(Object object, FieldHandler fieldHandler) throws DMSIException {
		Collection<AttributeValue> attributeValues = new HashSet<AttributeValue>();

		FieldConfig fieldConfig = fieldHandler.getFieldConfig();
		if (fieldConfig == null) {
			return attributeValues;
		}

		if (objectParserConfigurator.isFieldIgnoring(fieldConfig)) {
			return attributeValues;
		}

		Object value = fieldHandler.getFieldValue(object);
		if (value == null) {
			return attributeValues;
		}

		final ObjectId attributeId = fieldConfig.getAttributeId();
		final ClassConfig subConfig = fieldConfig.getConfig();

		if (attributeId != null) {
			value = convertFieldValue(fieldConfig, value);
			attributeValues.add(new AttributeValue(attributeId, value, fieldConfig.getValueController()));
		} else if (subConfig != null) {
			Collection<AttributeValue> subObjectValues = parseFieldValues(value, subConfig);
			attributeValues.addAll(subObjectValues);
		}
		return attributeValues;
	}

	public static class AttributeValue {
		private final ObjectId attributeId;
		private final Object value;
		private final ValueController valueController;

		public AttributeValue(ObjectId attributeId, Object value, ValueController valueController) {
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

	private Object convertFieldValue(FieldConfig fieldConfig, Object value) throws DMSIException {
		Collection<?> data;
		if (value instanceof Collection) {
			data = (Collection<?>) value;
		} else {
			data = Arrays.asList(value);
		}

		DMSIObjectConverter fieldConverter = objectParserConfigurator.getFieldConverter(fieldConfig);

		Set<ObjectId> cardIdsSet = new HashSet<ObjectId>();
		boolean isConverted = false;
		for (Object item : data) {
			ObjectId objectCardId = fieldConverter.convert(item);
			if (objectCardId != null) {
				cardIdsSet.add(objectCardId);
			}
			if (fieldConverter instanceof OneToManyDMSIObjectConverter) {
				Set<ObjectId> extraValues = ((OneToManyDMSIObjectConverter) fieldConverter).getExtraValues();
				cardIdsSet.addAll(extraValues);
			}
			isConverted |= fieldConverter.isConverted();
		}

		if (!isConverted) {
			return value;
		}
		return cardIdsSet.toArray(new ObjectId[cardIdsSet.size()]);
	}
}
