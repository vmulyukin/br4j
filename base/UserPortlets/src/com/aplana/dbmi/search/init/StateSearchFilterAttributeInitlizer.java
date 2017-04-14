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
import com.aplana.dbmi.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represent specific initializer for (@link StateSearchAttribute)
 * It copies values from Search.states field  
 * @author skashanski
 *
 */
public class StateSearchFilterAttributeInitlizer extends
		SearchFilterAttributeInitializer<StateSearchAttribute, Collection<CardState>> {

	@Override
	@SuppressWarnings("unchecked")
	protected void setValue(StateSearchAttribute searchFilterAttribute, Collection<CardState> searchAttributeValue) {
		searchFilterAttribute.setValues((Collection)searchAttributeValue);
	}
	
	@Override
	protected Collection<CardState> getValue(Search search, ObjectId attributeId) {
		Collection<?> stateIds = search.getStates();
		List<CardState> cardStates = convertToCardStates(stateIds);
		return cardStates;
	}
	
	private List<CardState> convertToCardStates(Collection<?> stateIds) {
		List<CardState> cardStates = new ArrayList<CardState>();

		for (Object stateId : stateIds) {
			ObjectId id = new ObjectId(CardState.class, Long.parseLong(stateId.toString()));
			CardState cardState = CardState.createFromId(id);
			cardStates.add(cardState);
		}
		
		return cardStates;
	}
}
