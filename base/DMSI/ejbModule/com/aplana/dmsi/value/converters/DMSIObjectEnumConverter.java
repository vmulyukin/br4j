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
package com.aplana.dmsi.value.converters;

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.config.ConfigurationException;
import com.aplana.dmsi.types.DMSIObject;

public abstract class DMSIObjectEnumConverter implements Converter, Parametrized {

	Map<Enum<?>, ObjectId> idByValues = new HashMap<Enum<?>, ObjectId>();
	Map<ObjectId, Enum<?>> valueByIds = new HashMap<ObjectId, Enum<?>>();
	private Enum<?> defaultEnum;

	public void setParameter(String key, Object value) {
		if ("defaultEnum".equals(key)) {
			defaultEnum = getValueOf((String) value);
			return;
		}
		ObjectId id = ObjectIdUtils.getObjectId(Card.class, (String) value, true);
		Enum<?> enumValue = getValueOf(key);
		idByValues.put(enumValue, id);
		valueByIds.put(id, enumValue);
	}

	public Object convert(Object value) {
		if (value instanceof DMSIObject) {
			String id = ((DMSIObject) value).getId();
			ObjectId valueId = ObjectIdUtils.getObjectId(Card.class, id, true);
			if (valueByIds.containsKey(valueId)) {
				return valueByIds.get(valueId);
			} else if (defaultEnum != null) {
				return defaultEnum;
			} else {
				throw new ConfigurationException("Value is not defined for value id=" + valueId);
			}
		} else if (value instanceof Enum<?>) {
			Enum<?> enumValue = (Enum<?>) value;
			if (!idByValues.containsKey(enumValue))
				throw new ConfigurationException("Value is not defined for " + enumValue);
			DMSIObject obj = createObject();
			obj.setId(idByValues.get(enumValue).getId().toString());
			return obj;
		} else {
			throw new IllegalArgumentException("Unsupported type of argument: " + value.getClass());
		}
	}

	protected abstract Enum<?> getValueOf(String value);

	protected DMSIObject createObject() {
		DMSIObject obj = new DMSIObject();
		return obj;
	}
}
