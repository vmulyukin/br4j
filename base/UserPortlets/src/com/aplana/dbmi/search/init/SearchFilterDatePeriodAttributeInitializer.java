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
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DatePeriodAttribute;

import java.util.Calendar;
import java.util.Date;


/**
 * Represent specific initializer for (@link DatePeriodAttribute)
 * It works with {@link DatePeriodAttribute} instances
 * 
 * @author skashanski
 *
 */
public class SearchFilterDatePeriodAttributeInitializer extends
		SearchFilterAttributeInitializer<DatePeriodAttribute, Search.DatePeriod> {

	@Override
	protected void setValue(DatePeriodAttribute searchFilterAttribute,
			DatePeriod searchAttributeValue) {
		
		searchFilterAttribute.setValueFrom(searchAttributeValue.start);
		searchFilterAttribute.setValueTo(searchAttributeValue.end);
		if (isCurentYear(searchAttributeValue)) 
			searchFilterAttribute.setCurrentYear(true);
		else 
			searchFilterAttribute.setCurrentYear(false);
	}
	
	protected boolean isCurentYear(DatePeriod searchAttributeValue) {
		Calendar calendar = Calendar.getInstance(ContextProvider.getContext().getLocale());
		Date dateFrom = getDateFrom(calendar);
		Date dateTo = getDateTo(calendar);
		return dateFrom.equals(searchAttributeValue.start) && dateTo.equals(searchAttributeValue.end);
	}
	
	private Date getDateFrom(Calendar calendar) {
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, currentYear);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTime();
	}
	
	private Date getDateTo(Calendar calendar) {
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.clear();
		calendar.set(Calendar.YEAR, currentYear);
		calendar.set(Calendar.MONTH, Calendar.DECEMBER);
		int lastDate = calendar.getActualMaximum(Calendar.DATE);
		calendar.set(Calendar.DATE, lastDate); 
		int lastDay = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DAY_OF_MONTH, lastDay);
		return calendar.getTime();
	}
}
