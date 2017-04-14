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
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.model.SearchYearDatePeriodChoiceAttribute;
import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;



/**
 * Represent specific initializer for (@link DatePeriodAttribute)
 * It works with {@link DatePeriodAttribute} instances
 * 
 * @author skashanski
 *
 */
public class SearchYearDatePeriodChoiceAttributeInitializer extends
		SearchFilterAttributeInitializer<SearchYearDatePeriodChoiceAttribute, Search.DatePeriod> {
	
	@Override
	protected void setValue(
			SearchYearDatePeriodChoiceAttribute searchFilterAttribute,
			DatePeriod searchAttributeValue) {

		if (isChekedYear(searchAttributeValue)) {
			searchFilterAttribute.setCheckedYear(true);
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(searchAttributeValue.start);
		    int year = cal.get(Calendar.YEAR);
			searchFilterAttribute.setToYear(String.valueOf(year));
			searchFilterAttribute.setFromYear(String.valueOf(year));
		} else {
			searchFilterAttribute.setCheckedYear(false);
			searchFilterAttribute.setValueFrom(searchAttributeValue.start);
			
			searchFilterAttribute.setValueTo(searchAttributeValue.end);
		}
	}
	
	protected boolean isChekedYear(DatePeriod searchAttributeValue) {
		
		Calendar cal = Calendar.getInstance();
	    cal.setTime(searchAttributeValue.start);
	    int year = cal.get(Calendar.YEAR);
	    
		return (DateUtils.truncate(searchAttributeValue.start, Calendar.YEAR).equals(searchAttributeValue.start) &&
				DateUtils.addDays(DateUtils.addYears(DateUtils.truncate(searchAttributeValue.end, Calendar.YEAR),1),-1)
							.equals(searchAttributeValue.end) &&
				DateUtils.truncate(searchAttributeValue.start, Calendar.YEAR).equals(DateUtils.truncate(searchAttributeValue.end, Calendar.YEAR)));
	}
}
