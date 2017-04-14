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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.config.ConfigurationException;

public abstract class EnumListAttributeConverter implements Converter,
	Parametrized {

    Map<Enum<?>, ObjectId> idByValues = new HashMap<Enum<?>, ObjectId>();
    Map<ObjectId, Enum<?>> valueByIds = new HashMap<ObjectId, Enum<?>>();

    public void setParameter(String key, Object value) {
	ObjectId id = ObjectIdUtils.getObjectId(ReferenceValue.class,
		(String) value, true);
	Enum<?> enumValue = getValueOf(key);
	idByValues.put(enumValue, id);
	valueByIds.put(id, enumValue);
    }

    public Object convert(Object value) {
	if (value instanceof ReferenceValue) {
	    ObjectId valueId = ((ReferenceValue) value).getId();
	    if (!valueByIds.containsKey(valueId))
		throw new ConfigurationException(
			"Value is not defined for reference value id="
				+ valueId);
	    return valueByIds.get(valueId);
	} else if (value instanceof Enum<?>) {
	    Enum<?> enumValue = (Enum<?>) value;
	    if (!idByValues.containsKey(enumValue))
		throw new ConfigurationException(
			"Reference value is not defined for value " + enumValue);
	    ReferenceValue referenceValue = new ReferenceValue();
	    referenceValue.setId(idByValues.get(enumValue));
	    return referenceValue;
	} else {
	    throw new IllegalArgumentException("Unsupported type of argument: "
		    + value.getClass());
	}
    }

    protected abstract Enum<?> getValueOf(String value);
}
