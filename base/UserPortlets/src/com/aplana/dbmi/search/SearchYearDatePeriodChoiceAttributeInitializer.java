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
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.SearchYearDatePeriodChoiceAttribute;
import com.aplana.dbmi.util.DateUtils;

import java.util.Date;

public class SearchYearDatePeriodChoiceAttributeInitializer extends SearchAttributeInitializer<SearchYearDatePeriodChoiceAttribute>{

	@Override
	protected Object getValue() {
		//do nothing
		return null;
	}

	@Override
	protected boolean isEmpty() {
		return attribute.getValueFrom() == null && attribute.getValueTo() == null;
	}

	@Override
	protected void setValue(Object attrValue, Search search) {
		//do nothing
	}

	@Override
	public void initialize(Search search, boolean saveMode) {
		Date dateTo;
		Date dateFrom;
		if (attribute.isCheckedYear()) {
			dateFrom = DateUtils.beginOfYear(ContextProvider.getContext().getLocale(), Integer.valueOf(attribute.getFromYear()));
			dateTo = DateUtils.endOfYear(ContextProvider.getContext().getLocale(), Integer.valueOf(attribute.getToYear()));
		} else {
			dateFrom = attribute.getValueFrom();
			dateTo = attribute.getValueTo();
		}

		search.addDateAttribute(attribute.getId(), dateFrom,
				dateTo, attribute.isIncludedEmpty());
	}
}
