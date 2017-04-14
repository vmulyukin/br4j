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
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StateSearchItem.SearchItemState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represent specific initializer for (@link MultipleStateSearchItemAttribute)
 * It copies values from Search.states field  
 * @author skashanski
 *
 */
public class MultipleStateSearchFilterItemAttributeInitializer extends
		SearchFilterAttributeInitializer<MultipleStateSearchItemAttribute, 	Collection<DataObject>> {

	@Override
	protected Collection<DataObject> getValue(Search search,
			ObjectId attributeId) {

		Collection<?> stateIds = search.getStates();
		List<DataObject> cardStates = converToCardStates(stateIds);
		return cardStates;
	}

	@Override
	protected void setValue(
			MultipleStateSearchItemAttribute searchFilterAttribute,
			Collection<DataObject> searchAttributeValue) {
		
		//at first un-check all items of attribute
		searchFilterAttribute.changeStateForAllItems(SearchItemState.UNCHECKED);
		//then check only passed items 
		searchFilterAttribute.changeStateForItemsValues(searchAttributeValue, SearchItemState.CHECKED);
	}
	
	private List<DataObject> converToCardStates(Collection<?> stateIds) {
		List<DataObject> cardStates = new ArrayList<DataObject>();

		for (Object stateId : stateIds) {
			ObjectId id = new ObjectId(CardState.class, Long.parseLong(((String) stateId)));
			CardState cardState = CardState.createFromId(id);
			cardStates.add(cardState);
		}
		
		return cardStates;
	}
}


