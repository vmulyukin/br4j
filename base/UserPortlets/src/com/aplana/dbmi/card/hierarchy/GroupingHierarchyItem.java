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
package com.aplana.dbmi.card.hierarchy;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.LocalizedString;

public class GroupingHierarchyItem extends HierarchyItem {
	private LocalizedString label;
	private List children;
	
	public GroupingHierarchyItem(Hierarchy hierarchy) {
		super(hierarchy);
	}
	
	public GroupingHierarchyItem(Hierarchy hierarchy, long id) {
		super(hierarchy, id);
	}	
	
	public List getChildren() {
		return children;
	}
	public void setChildren(List children) {
		this.children = children;
	}		
	protected String getType() {
		return "group";
	}
	public JSONObject toJSONObject() throws JSONException {
		JSONObject jo = super.toJSONObject();
		jo.put("label", label == null ? ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", ContextProvider.LOCALE_RUS).getString("cardlink.stub") : label.getValue());
		jo.put("checked", false);
		JSONArray arr = new JSONArray();
		Iterator i = children.iterator();
		while (i.hasNext()) {
			HierarchyItem child = (HierarchyItem)i.next();
			arr.put(child.toJSONObject());
		}
		jo.put("children", arr);
		return jo;
	}

	public LocalizedString getLabel() {
		return label;
	}

	public void setLabel(LocalizedString label) {
		this.label = label;
	}
	
	public GroupingHierarchyItem makeCopy() {
		GroupingHierarchyItem result = new GroupingHierarchyItem(hierarchy);
		result.setLabel(label);
		result.setChildren(children);
		return result;
	}

}
