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

import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ReferenceValue;

import java.util.Collection;

/**
 * Represent specific initializer for (@link ListAttribute)
 * It works with {@link ListAttribute} instances
 * 
 * @author skashanski
 *
 */
public class SearchFilterListAttributeInitializer extends
		SearchFilterAttributeInitializer<ListAttribute, Collection<ReferenceValue>> {

	@Override
	protected boolean isEmpty(Collection<ReferenceValue> searchAttributeValue) {
		return super.isEmpty(searchAttributeValue) || searchAttributeValue.isEmpty();
	}

	@Override
	protected void setValue(ListAttribute searchFilterAttribute,
			Collection<ReferenceValue> searchAttributeValue) {
		//Verifies if there is only one value ...it is possible to set only one value for ListAttribute
		if (searchAttributeValue.size() != 1)
			throw new RuntimeException("Invalid searchAttribute value for attribute : " + searchFilterAttribute.getId());

		ReferenceValue value = searchAttributeValue.iterator().next();
		searchFilterAttribute.setValue(value);
	}
}
