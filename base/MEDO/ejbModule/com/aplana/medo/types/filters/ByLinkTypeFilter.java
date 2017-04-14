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
package com.aplana.medo.types.filters;

import java.util.LinkedList;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dmsi.Parametrized;
import com.aplana.dmsi.card.handling.TypedObjectId;
import com.aplana.dmsi.value.filters.FirstElementFilter;

public class ByLinkTypeFilter extends FirstElementFilter implements
	Parametrized {

    private static final String ALLOWED_TYPES_PARAM = "allowed";
    private static final String RESTRICTED_TYPES_PARAM = "restricted";

    private List<?> allowedTypes;
    private List<?> restrictedTypes;

    public void setParameter(String key, Object value) {
	if (ALLOWED_TYPES_PARAM.equals(key)) {
	    allowedTypes = ObjectIdUtils.commaDelimitedStringToNumericIds(
		    (String) value, ReferenceValue.class);
	} else if (RESTRICTED_TYPES_PARAM.equals(key)) {
	    restrictedTypes = ObjectIdUtils.commaDelimitedStringToNumericIds(
		    (String) value, ReferenceValue.class);
	}
    }

    @Override
    public ObjectId[] filterIds(ObjectId[] ids) {
	List<ObjectId> filteredList = new LinkedList<ObjectId>();
	for (ObjectId id : ids) {
	    ObjectId typeId = ((TypedObjectId) id).getTypeId();
	    if (allowedTypes != null && !allowedTypes.contains(typeId)) {
		continue;
	    }
	    if (restrictedTypes != null && restrictedTypes.contains(typeId)) {
		continue;
	    }
	    filteredList.add(id);
	}
	return filteredList.toArray(new ObjectId[filteredList.size()]);
    }

}
