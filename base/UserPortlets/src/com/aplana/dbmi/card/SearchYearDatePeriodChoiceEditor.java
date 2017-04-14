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
package com.aplana.dbmi.card;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.model.SearchYearDatePeriodChoiceAttribute;
import com.aplana.dbmi.model.YearPeriodAttribute;
import com.aplana.dbmi.service.DataException;

public class SearchYearDatePeriodChoiceEditor extends YearPeriodAttributeEditor {
	
	public final static String YEAR="year";
	public final static String DATE_PERIOD="date_period";
	

	
	public SearchYearDatePeriodChoiceEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/SearchYearDatePeriodChoice.jsp");
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		SearchYearDatePeriodChoiceAttribute yearPeriodAttribute = (SearchYearDatePeriodChoiceAttribute) attr;
		List<Integer> years = generateYears(yearPeriodAttribute.getYearStart(), yearPeriodAttribute.getAdditionalYear());
		yearPeriodAttribute.setYears(years);
		setCurrentYear(yearPeriodAttribute);
	} 
	

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {

		String attrIdPrefix = getAttrIdPrefix(attr);
		
		String checked = request.getParameter(attrIdPrefix+"_choice");
		if(checked.equals(YEAR)){
			((SearchYearDatePeriodChoiceAttribute)attr).setCheckedYear(true);
			return parseParamsYear(request,  attr, attrIdPrefix);
		}else{
			((SearchYearDatePeriodChoiceAttribute)attr).setCheckedYear(false);
			return super.parseParamsDatePeriod(request,  attr, attrIdPrefix);
		}
		

	}

	private boolean parseParamsYear(ActionRequest request, Attribute attr, String attrIdPrefix) {
		String yearFrom = request.getParameter(attrIdPrefix+"_YearFrom");
		String yearTo = request.getParameter(attrIdPrefix+"_YearFrom");
		
		SearchYearDatePeriodChoiceAttribute attribute = (SearchYearDatePeriodChoiceAttribute) attr;
		attribute.setFromYear(yearFrom);
		attribute.setToYear(yearTo);
		attribute.setCurrentYear(false);
		
		if(!checkValidYear(yearFrom) || !checkValidYear(yearTo)){
			return false;
		}	
		return true;
	}

}
