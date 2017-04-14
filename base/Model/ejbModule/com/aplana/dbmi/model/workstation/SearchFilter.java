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
package com.aplana.dbmi.model.workstation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Person;


public class SearchFilter {
	
	private List<Long> statusIds = new LinkedList<Long>();
	private List<String> statusNames = new LinkedList<String>();
	
	private List<Long> templateIds = new LinkedList<Long>();
	
	private List<String> attributeCodes = new LinkedList<String>();
	private List<String> attributeNames = new LinkedList<String>();
	
	private SearchFilter() {
	}
	
	public List<Long> getStatusIds() {
		return statusIds;
	}
	
	public List<String> getStatusNames() {
		return statusNames;
	}

	public List<Long> getTemplateIds() {
		return templateIds;
	}
	
	public long[] getTemplateIdsAsArray() {
		long[] result = new long[templateIds.size()];
		
		for(int i = 0; i < templateIds.size(); i ++) {
			result[i] = templateIds.get(i);
		}
		
		return result;
	}

	public List<String> getAttributeCodes() {
		return attributeCodes;
	}
	
	public List<String> getAttributeNames() {
		return attributeNames;
	}
	
	public static SearchFilter getSearchFilter(Search search) {
		SearchFilter searchFilter = new SearchFilter();
		
		if(null == search) {
			return searchFilter;
		}
		
		search = search.makeCopy();
		
		searchFilter.addStatusIds(search);
		searchFilter.addTemplateIds(search);
		searchFilter.addAttributeCodes(search);
		
		return searchFilter;
	}
	
	public static SearchFilter getSearchFilter(String[] names, String[] values) {
		SearchFilter searchFilter = new SearchFilter();
		
		if(null == names || values == null) {
			return searchFilter;
		}
		
		for(int i = 0; i < names.length; i ++) {
    		String name = names[i];
    		
    		if(null == values[i]) {
    			continue;
    		}
    		
    		if(name.equalsIgnoreCase("������")) {
    			searchFilter.addStatusName(values[i]);
    		} else {
    			searchFilter.addAttributeName(names[i]);
    		}
    	}
		
		return searchFilter;
	}
	
	private void addStatusName(String name) {
		statusNames.add(name);
	}
	
	private void addAttributeName(String name) {
		attributeNames.add(name);
	}

	private void addStatusIds(Search search) {
		Collection statusIds = search.getStates();
		
		if(null == statusIds || statusIds.isEmpty()) {
			return;
		}
		
		Iterator iterator  = search.getStates().iterator();
		Object status = iterator.next();		
		boolean isString = status instanceof String;
		
		addStatusId(status, isString);
		while(iterator.hasNext()) {
			status = iterator.next();
			addStatusId(status, isString);
		}
	}
	
	private void addStatusId(Object statusId, boolean isString) {
		if(isString) {
			this.statusIds.add(Long.parseLong((String) statusId));
		} else {
			this.statusIds.add((Long) statusId);
		}
	}

	private void addTemplateIds(Search search) {
		Collection<Long> templateIds = search.getTemplates();
		
		if(null == templateIds || templateIds.isEmpty()) {
			return;
		}
		
		Iterator iterator  = search.getTemplates().iterator();
		Object templateId = iterator.next();
		boolean isString = templateId instanceof String;
		
		addTemplateId(templateId, isString);		
		while(iterator.hasNext()) {
			templateId = iterator.next();
			addTemplateId(templateId, isString);
		}
	}
	
	private void addTemplateId(Object templateId, boolean isString) {
		if(isString) {
			this.templateIds.add(Long.parseLong((String) templateId));
		} else {
			this.templateIds.add((Long) templateId);
		}
	}
	
	private void addAttributeCodes(Search search) {
		Collection<Map.Entry> attributes = search.getAttributes();
		
		if(null == attributes || attributes.size() == 0) {
			return;
		}
		
		for(Object attr : attributes) {
			Map.Entry attribute = (Map.Entry) attr;
			String attributeCode = attribute.getKey().toString();
			Object attributeValue = attribute.getValue();
			
			if (null == attributeValue) {
				continue;
			}
			
			if(Person.ID_CURRENT.equals(attributeValue)) {
				this.attributeCodes.add(attributeCode);
			} else {
				throw new IllegalStateException("CMS: Unsupported attribute type in filter! attributeCode: " + 
						attributeCode + "; attributeValue: " + attributeValue);
			}
		}
	}

}
