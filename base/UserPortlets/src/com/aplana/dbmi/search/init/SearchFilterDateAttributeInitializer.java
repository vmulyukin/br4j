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
import com.aplana.dbmi.action.Search.DatePeriod;
import com.aplana.dbmi.model.DateAttribute;

/**
 * Represent specific initializer for (@link DateAttribute)
 * It works with {@link DateAttribute} instances
 * 
 * @author skashanski
 *
 */
public class SearchFilterDateAttributeInitializer extends
		SearchFilterAttributeInitializer<DateAttribute, Search.DatePeriod> {

	@Override
	protected void setValue(DateAttribute searchFilterAttribute,
			DatePeriod searchAttributeValue) {
		searchFilterAttribute.setValue(searchAttributeValue.start);
	}
}
