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
package com.aplana.dbmi.search.init;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.IsExistSearchAttributeEditor;
import com.aplana.dbmi.model.IsExistSearchAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

/**
 * Represent specific initializer for {@link IsExistSearchAttribute}
 * It works with {@link IsExistSearchAttribute} instances
 * @author desu
 *
 */
public class SearchFilterExistAttributeInitializer extends
		SearchFilterAttributeInitializer<IsExistSearchAttribute, ReferenceValue> {

	@Override
	protected void setValue(IsExistSearchAttribute searchFilterAttribute, ReferenceValue searchAttributeValue) {
		searchFilterAttribute.setIsExistFlag(searchAttributeValue);
	}
	
	@Override
	protected ReferenceValue getValue(Search search, ObjectId attributeId) {
		Object val = search.getAttribute(attributeId);
		ReferenceValue ref = ReferenceValue.createFromId(IsExistSearchAttributeEditor.NO_MATTER_ID);
		if (val instanceof Search.EmptyAttribute) {
			ref = ReferenceValue.createFromId(IsExistSearchAttributeEditor.NO_ID);
		} else if (val instanceof Search.ExistAttribute) {
			ref = ReferenceValue.createFromId(IsExistSearchAttributeEditor.YES_ID);
		}
		return ref;
	}
}
