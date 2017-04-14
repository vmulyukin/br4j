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
package com.aplana.dbmi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.model.StateSearchItem.SearchItemState;

/**
 * Represents pseudo attribute at extended search for working with collection of (@link StateSearchItemAttribute)
 * that can change their states   
 * 
 * @author skashanski
 *
 */
public class MultipleStateSearchItemAttribute extends Attribute {

	
	private Collection<StateSearchItem> values  = new ArrayList<StateSearchItem>();
	
	/** default state for values*/
	private SearchItemState initState = SearchItemState.UNCHECKED;
	
	private boolean showSelectAll; // true - shows select all checkbox
	
	public Collection<StateSearchItem> getValues() {
		return values;
	}
	
	public JSONObject toJSON(ResourceBundle bundle) throws JSONException {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		for(StateSearchItem state : values) {
			arr.put(state.toJSON(bundle));
		}
		result.put("values", arr);
		return result;
	}

	public void setValues(Collection<StateSearchItem> values) {
		this.values = values;
	}

	public SearchItemState getInitState() {
		return initState;
	}

	public void setInitState(SearchItemState initState) {
		this.initState = initState;
	}

	@Override
	public boolean equalValue(Attribute attr) {
		
		throw new UnsupportedOperationException();
		
	}
	
	public StateSearchItem getFirstStateSearchItem() {
		if (values.isEmpty())
			return null;
		
		return values.iterator().next();
	}

	@Override
	public String getStringValue() {
		StringBuffer  result = new StringBuffer();
		
		for(StateSearchItem item :  values) {
			if (result.length() > 0 )
				result.append(',');
						
			result.append(item.getName());
		}
		return result.toString();

	}

	@Override
	public Object getType() {
		return TYPE_MULTIPLE_STATE_SEARCH_ITEM;
	}

	@Override
	public boolean verifyValue() {
		return true;
	}

	@Override
	public void clear() {
		
		changeStateForAllItems(initState);
		
	}
	
	/**
	 * Changes states for all items of this attribute
	 * @param newState new state 
	 */
	public void changeStateForAllItems(SearchItemState newState) {

		for(StateSearchItem stateSearchItem : values) {
			stateSearchItem.setState(newState);
		}

	}
	
	/**
	 * Change states of given items(objectIds) 
	 * @param stateSearchItems items to change state
	 * @param state new state
	 */
	public void changeStateForItems(Collection<ObjectId> stateSearchItemIds, SearchItemState newState) {
		
		for(StateSearchItem stateSearchItem : values) {
			for(ObjectId stateSearchItemId : stateSearchItemIds ) {
				if (stateSearchItemId.equals(stateSearchItem.getId()))
					stateSearchItem.setState(newState);
			}		
		}
		
		
	}
	
	/**
	 * Change states of given items(objectIds) 
	 * @param stateSearchItems items to change state
	 * @param state new state
	 */
	public void changeStateForItemsValues(Collection<DataObject> stateSearchItemIds, SearchItemState newState) {
		
		for(StateSearchItem stateSearchItem : values) {
			
			boolean existAllItemValues = true;
			
			for(DataObject itemValue : stateSearchItem.getValues()) {
				boolean existItemValue = false; 
				for(DataObject stateSearchItemId : stateSearchItemIds ) {
					if (stateSearchItemId.equals(itemValue))
						existItemValue = true;	
				}
				
				if (!existItemValue)
					existAllItemValues = false;

			}
			if (existAllItemValues)
				stateSearchItem.setState(newState);

			
		}

		
		
	}

	@Override
	public void setValueFromAttribute(Attribute attr){
		throw new UnsupportedOperationException();
	}

	public boolean isShowSelectAll() {
		return showSelectAll;
	}

	public void setShowSelectAll(boolean showSelectAll) {
		this.showSelectAll = showSelectAll;
	}
	
	
	
	
	
	

}
