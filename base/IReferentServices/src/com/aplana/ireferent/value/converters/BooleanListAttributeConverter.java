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
package com.aplana.ireferent.value.converters;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.ireferent.Parametrized;

public class BooleanListAttributeConverter implements Converter, Parametrized {

    private static final String YES_VALUE_PARAM = "yesValueId";
    private static final String NO_VALUE_PARAM = "noValueId";

    private ObjectId yesValueId;
    private ObjectId noValueId;

    public BooleanListAttributeConverter() {
    }

    public void setParameter(String key, Object value) {
	if (YES_VALUE_PARAM.equals(key)) {
	    yesValueId = ObjectIdUtils.getObjectId(ReferenceValue.class,
		    (String) value, true);
	} else if (NO_VALUE_PARAM.equals(key)) {
	    noValueId = ObjectIdUtils.getObjectId(ReferenceValue.class,
		    (String) value, true);
	}
    }

    public Object convert(Object value) {
	if (yesValueId == null || noValueId == null) {
	    throw new IllegalStateException(
		    "Mandatory fields of converter were not initialized");
	}
	if (value instanceof ReferenceValue) {
	    return yesValueId.equals(((ReferenceValue) value).getId());
	} else if (value instanceof Boolean) {
	    ReferenceValue ref = new ReferenceValue();
	    if (value.equals(true)) {
		ref.setId(yesValueId);
	    } else {
		ref.setId(noValueId);
	    }
	    return ref;
	} else {
	    throw new IllegalArgumentException("Unsupported type of argument: "
		    + value.getClass());
	}
    }
}
