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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.ireferent.config.FieldConfig;
import com.aplana.ireferent.types.WSOCollection;

public class FieldHandler {
    private Field field;
    private FieldConfig config;
    private Log logger = LogFactory.getLog(this.getClass());

    public FieldHandler(Field field, FieldConfig config) {
	this.field = field;
	this.config = config;
    }

    protected FieldConfig getConfig() {
	return this.config;
    }

    protected Class<?> getFieldType() {
	return this.field.getType();
    }

    public Object getFieldValue(Object object) {
	Object value = getValue(object);
	return convertValue(value);
    }

    protected Object getValue(Object object) {
	Object value = null;
	try {
	    field.setAccessible(true);
	    value = field.get(object);
	} catch (IllegalArgumentException ex) {
	    logger.error(String.format("Error during value of %s field get",
		    field.getName()), ex);
	} catch (IllegalAccessException ex) {
	    logger.error(String.format("Error during value of %s field get",
		    field.getName()), ex);
	}
	return value;
    }

    public void setFieldValue(Object obj, Object value) {
	if (value == null)
	    return;

	value = convertValue(value);
	setValue(obj, value);
    }

    private Object convertValue(Object value) {
	if (value instanceof WSOCollection) {
	    return convertCollection((WSOCollection) value);
	}
	return convertAtomValue(value);
    }

    private WSOCollection convertCollection(WSOCollection collection) {
	WSOCollection convertedCollection = new WSOCollection();
	List<Object> convertedCollectionData = convertedCollection.getData();
	for (Object atomValue : collection.getData()) {
	    convertedCollectionData.add(convertAtomValue(atomValue));
	}
	return convertedCollection;
    }

    private Object convertAtomValue(Object value) {
	if (value == null)
	    return null;

	if (config.getConverter() != null) {
	    value = config.getConverter().convert(value);
	}
	return value;
    }

    protected void setValue(Object obj, Object value) {
	try {
	    field.setAccessible(true);
	    field.set(obj, value);
	} catch (IllegalArgumentException ex) {
	    final String messageFormat = "Error during setting value %s of %s field for %s object";
	    logger.error(String.format(messageFormat, value, field.getName(),
		    obj), ex);
	} catch (IllegalAccessException ex) {
	    final String messageFormat = "Error during setting value %s of %s field for %s object";
	    logger.error(String.format(messageFormat, value, field.getName(),
		    obj), ex);
	}
    }

}