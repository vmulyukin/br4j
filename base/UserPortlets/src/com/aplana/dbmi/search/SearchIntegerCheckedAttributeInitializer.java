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
import com.aplana.dbmi.action.Search.IntegerSearchConfigValue.SearchType;
import com.aplana.dbmi.model.SearchIntegerCheckedAttribute;

public class SearchIntegerCheckedAttributeInitializer extends SearchAttributeInitializer<SearchIntegerCheckedAttribute> {

	@Override
	protected Object getValue() {		
		return attribute.getValue();
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		search.addIntegerAttribute(attribute.getId(), (Integer)attrValue, getSearchType());
	}

	@Override
	protected boolean isEmpty() {		
		return attribute.getValue()<1;
	}
	
	protected boolean isCheckedFlag(){
		return attribute.isCheckedFlag();
	}
	
	protected SearchType getSearchType() {
		if (isCheckedFlag()) {
			return Search.IntegerSearchConfigValue.SearchType.EXACT_MATCH;
		}		
		return Search.IntegerSearchConfigValue.SearchType.CONTAINS;		
	}
}
