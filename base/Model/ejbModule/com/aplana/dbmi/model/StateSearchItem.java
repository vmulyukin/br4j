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

import java.util.Collection;
import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents item for extended search with states(Checked/Uncheked)
 * 
 * @author skashanski
 *
 */
public abstract class StateSearchItem  {

	public enum SearchItemState {CHECKED, UNCHECKED}; 

	private ObjectId id = null;
	
	private String name = "";
	
	private SearchItemState state = SearchItemState.UNCHECKED;
	
	public StateSearchItem(ObjectId id) {
		this.id = id;
	}	
	
	public StateSearchItem(ObjectId id, String name) {
		this(id);
		this.name = name;
	}
	
	public JSONObject toJSON(ResourceBundle bundle) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("id", getIdStr());
		result.put("name", getName());
		result.put("label", getName(bundle));
		result.put("checked", isChecked());
		return result;
	}

	public StateSearchItem(ObjectId id, String name, SearchItemState state) {
		this(id, name);
		this.state = state;
	}

	public ObjectId getId() {
		return id;
	}
	
	public String getIdStr() {
		return id.toString();
	}	

	public void setId(ObjectId id) {
		this.id = id;
	}

	public SearchItemState getState() {
		return state;
	}

	public void setState(SearchItemState state) {
		this.state = state;
	}

	public String getName() {
		return name;
	}
	
	public String getName(ResourceBundle bundle) {
		return bundle.getString(name);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isChecked() {
		return state.equals(SearchItemState.CHECKED);
	}
	
	public abstract Collection<DataObject> getValues();
	
}
