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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.ireferent.config.ExtensionConfig;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ExtensionUtils;

public class ExtensionHandler extends FieldHandler {

    public ExtensionHandler(ExtensionConfig config) {
	super(null, config);
    }

    @Override
    protected Class<?> getFieldType() {
	return String.class;
    }

    @Override
    protected Object getValue(Object object) {
	if (!(object instanceof WSOItem)) {
	    throw new IllegalArgumentException(
		    "Extension handler can operate only with "
			    + WSOItem.class.getSimpleName());
	}
	WSOItem item = (WSOItem) object;
	WSOCollection values = item.getValues();
	if (isMultivalued()) {
	    return values;
	}
	List<Object> valuesData = values.getData();
	if (!valuesData.isEmpty()) {
	    return valuesData.get(0);
	}
	return null;
    }

    private boolean isMultivalued() {
	return ((ExtensionConfig) getConfig()).isMultivalued();
    }

    @Override
    protected void setValue(Object object, Object value) {
	if (!(object instanceof WSObject)) {
	    throw new IllegalArgumentException(
		    "Extension handler can operate only with "
			    + WSObject.class.getSimpleName());
	}
	WSObject wsObject = (WSObject) object;
	WSOItem item = ExtensionUtils.createItem(getConfig().getFieldName());
	Collection<?> values;
	if (value instanceof Collection<?>) {
	    values = (Collection<?>) value;
	} else {
	    values = Collections.singleton(value);
	}
	ExtensionUtils.setExtensionValues(item, values);
	ExtensionUtils.addExtensions(wsObject, item);
    }
}
