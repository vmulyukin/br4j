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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.YearPeriodAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.util.DateUtils;

public class YearPeriodAttributeEditor extends DatePeriodDojoAttributeEditor {
	
	public YearPeriodAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/SearchYearPeriod.jsp");
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException {
		YearPeriodAttribute yearPeriodAttribute = (YearPeriodAttribute) attr;
		List<Integer> years = generateYears(yearPeriodAttribute.getYearStart());
		yearPeriodAttribute.setYears(years);
		setCurrentYear(yearPeriodAttribute);
	} 
	

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {

		String attrIdPrefix = getAttrIdPrefix(attr);
		
		String yearFrom = request.getParameter(attrIdPrefix+"_YearFrom");
		String yearTo = request.getParameter(attrIdPrefix+"_YearTo");
		
		YearPeriodAttribute attribute = (YearPeriodAttribute) attr;
		attribute.setFromYear(yearFrom);
		attribute.setToYear(yearTo);
		attribute.setCurrentYear(false);
		
		if(!checkValidYear(yearFrom) || !checkValidYear(yearTo)){
			return false;
		}
			
		
		attribute.setValueFrom(getDateFrom(Integer.valueOf(yearFrom)));
		attribute.setValueTo(getDateTo(Integer.valueOf(yearTo)));	
		

		return true;
	}
	
	
	
	protected boolean checkValidYear(String year){
		if(year==null || year.isEmpty() || !year.matches("[0-9]+")){
			return false;
		}
		return true;
	}
	
	
	protected Date getDateFrom(int fromYear) {
		return DateUtils.beginOfYear(ContextProvider.getContext().getLocale(), fromYear);
	}
	
	protected Date getDateTo(int toYear) {
		return DateUtils.endOfYear(ContextProvider.getContext().getLocale(), toYear);
	}	
	
	public String getAttrIdPrefix(Attribute attr){
		return JspAttributeEditor.getAttrHtmlId(attr);
	}
	
	
	protected List<Integer> generateYears(int yearStart){
		return generateYears(yearStart, 0);
	}
	
	
	/**
	 * ���������� ������ �����, � ��������� �� �������� 
	 * @param yearStart
	 * @return
	 */
	protected List<Integer> generateYears(int yearStart, int addYear){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		List<Integer> years = new ArrayList<Integer>(3);
		for(int i=yearStart; i<=calendar.get(Calendar.YEAR)+addYear; i++){
			years.add(i);
		}
		return years;
	}
	
	protected void setCurrentYear(YearPeriodAttribute yearPeriodAttribute){
		if(!yearPeriodAttribute.isCurrentYear()){
			return;
		}
		int currenYear = getUnitDate(new Date(), Calendar.YEAR);
		yearPeriodAttribute.setFromYear(String.valueOf(currenYear));
		yearPeriodAttribute.setToYear(String.valueOf(currenYear));
	}
	
	/**
	 * ���������� ������� ����
	 * @param date
	 * @param unitType - ��������� �� Calendar. �������� Calendar.YEAR
	 * @return
	 */
	protected int getUnitDate(Date date, int unitType){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(unitType);
	}
	
	

}
