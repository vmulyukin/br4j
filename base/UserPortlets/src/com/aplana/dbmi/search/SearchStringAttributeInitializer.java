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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.web.tag.util.StringUtils;


/**
 * Represents visitor for StringAttribute 
 * @author skashanski
 *
 */
public class SearchStringAttributeInitializer extends SearchAttributeInitializer<StringAttribute>  {

	public static final ObjectId ATTR_REG_NUMBER = ObjectId.predefined(
			StringAttribute.class, "regnumber");

	public static final ObjectId ATTR_INCOMING_OUTNUMBER = ObjectId.predefined(
			StringAttribute.class, "jbr.incoming.outnumber");

	@Override
	protected Object getValue() {
		return attribute.getValue();
	}
	
	/**
	 * Returns attribute search type @see Search.TextSearchConfigValue 
	 */
	protected int getSearchType() {
		if ( ATTR_INCOMING_OUTNUMBER.equals(attribute.getId()) )
				return Search.TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE;
		return Search.TextSearchConfigValue.CONTAINS;
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		search.addStringAttribute(attribute.getId(),(String)attrValue, getSearchType());
	}

	@Override
	protected boolean isEmpty() {
		return !StringUtils.hasLength(attribute.getValue());
	}
}
