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
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.StateSearchAttribute;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents initializer for StateSearchAttribute
 * 
 *  
 * @author skashanski
 *
 */
public class StateSearchAttributeInitializer extends SearchAttributeInitializer<StateSearchAttribute>  {

	@Override
	protected boolean isEmpty() {
		Collection states = attribute.getValues();
		return  states == null || states.isEmpty();
	}

	@Override
	protected Object getValue() {
		return attribute.getValues();
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		Collection<String> result = new ArrayList<String>();

		//converts CardStates to Id list
		for (Object o : ((Collection) attrValue)) {
			CardState cardState = (CardState) o;
			result.add(cardState.getId().getId().toString());
		}

		search.setStates(result);
	}
}
