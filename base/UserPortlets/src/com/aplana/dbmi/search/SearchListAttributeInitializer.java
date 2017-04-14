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
package com.aplana.dbmi.search;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ReferenceValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents visitor for ListAttribute 
 * @author skashanski
 *
 */
public class SearchListAttributeInitializer extends SearchAttributeInitializer<ListAttribute>  {

	@Override
	protected boolean isEmpty() {
		ReferenceValue selRefValue = attribute.getValue();
		return selRefValue == null || selRefValue.getId() == null;
	}

	@Override
	protected Object getValue() {
		ReferenceValue selectedRefValue = attribute.getValue();

		if (attribute.getReferenceValues().contains(selectedRefValue)) {
			Collection<ReferenceValue> selValues = new ArrayList<ReferenceValue>();
			selValues.add(selectedRefValue);
			return selValues;
		}

		return Collections.emptyList();

	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		search.addListAttribute(attribute.getId(), (Collection)attrValue);
	}
}
