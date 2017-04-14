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
import com.aplana.dmsi.types.LinkType;
import com.aplana.dmsi.types.TypedLink;

public class TypedLinkConverter implements Converter, Parametrized {

    Map<String, ObjectId> idByCodes = new HashMap<String, ObjectId>();
    Map<ObjectId, String> codeByIds = new HashMap<ObjectId, String>();

    public void setParameter(String key, Object value) {
	ObjectId id = ObjectIdUtils.getObjectId(ReferenceValue.class,
		(String) value, true);
	idByCodes.put(key, id);
	codeByIds.put(id, key);
    }

    public Object convert(Object value) {
	if (value instanceof TypedLink) {
	    LinkType linkType = ((TypedLink) value).getType();
	    if (linkType.getId() == null) {
		linkType.setId(idByCodes.get(linkType.getCode()));
	    } else if (linkType.getCode() == null) {
		linkType.setCode(codeByIds.get(linkType.getId()));
	    }
	    return value;
	}
	throw new IllegalArgumentException("Unsupported type of argument: "
		+ value.getClass());
    }

}
