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
import com.aplana.dbmi.model.CardStatusListStateSearchItem;
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.model.StateSearchItem;
import com.aplana.dbmi.model.TemplateStateSearchItem;

import java.util.Collection;

/**
 * Represents visitor/initializer for {@link MultipleStateSearchItemAttribute} 
 * @author skashanski
 *
 */
public class MultipleStateSearchItemAttributeInitializer extends
		SearchAttributeInitializer<MultipleStateSearchItemAttribute> {

	StateSearchItemHandlerInterface stateSearchItemHandler = null;

	@Override
	public void setAttribute(MultipleStateSearchItemAttribute attribute) {
		super.setAttribute(attribute);
		initializeStateSearchItemHandler(attribute);
	}

	private void initializeStateSearchItemHandler(MultipleStateSearchItemAttribute attribute) {
		if (attribute.getValues().isEmpty())
			return;
		
		StateSearchItem firstStateSearchItem = attribute.getValues().iterator().next();
		if (firstStateSearchItem instanceof TemplateStateSearchItem) {
			stateSearchItemHandler = new TemplateStateSearchItemHandler();
		} else	if (firstStateSearchItem instanceof CardStatusListStateSearchItem) {
			stateSearchItemHandler = new CardStatusListStateSearchItemHandler();
		} else 
			throw new IllegalArgumentException("Invalid collection type!");
	}

	@Override
	protected Object getValue() {
		return attribute.getValues();
	}

	@Override
	protected boolean isEmpty() {
		return attribute.getValues().isEmpty();
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		Collection<StateSearchItem> values = (Collection<StateSearchItem>)attrValue;
		stateSearchItemHandler.handle(values, search);		
	}
}
