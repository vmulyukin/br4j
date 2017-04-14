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

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.portlet.ActionRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.web.tag.util.StringUtils;

public class DatePeriodDojoAttributeEditor extends DateTimeAttributeEditor {

	private boolean toDateInclusive = false;

	public DatePeriodDojoAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/DatePeriodDojo.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/DatePeriodDojoInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException 
	{
		
		return parseParamsDatePeriod(request, attr, getAttrHtmlId(attr));
	}
	
	protected boolean parseParamsDatePeriod(ActionRequest request, Attribute attr,  String attrIdPrefix) throws DataException{
		final DatePeriodAttribute datePeriodAttr = (DatePeriodAttribute) attr;
		final Date fromDate = parseDateValue(request.getParameter(attrIdPrefix+"_fromDate"), datePeriodAttr.getName());
		final Date toDate =  parseDateValue(request.getParameter(attrIdPrefix+"_toDate"), datePeriodAttr.getName());
		
		datePeriodAttr.setValueFrom(fromDate);
		datePeriodAttr.setValueTo(toDate);
		
		if(toDateInclusive && toDate != null){
			datePeriodAttr.setValueTo(setDateInclusive(datePeriodAttr.getValueTo()));
		}
		
		return true;
	}
	
	protected Date setDateInclusive(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTime();
	}
	
	protected Date parseDateValue(String value, String attrName) throws DataException {
		if (!StringUtils.hasText(value))
			return null;
        Format formatter = new SimpleDateFormat(FMT_DATEONLY_YYYY_MM_DD);
		try {
			return (Date) formatter.parseObject(value);
		} catch (ParseException e) {
			throw new DataException("edit.page.error.date", new Object[] { attrName });
		}
	}
	
	// ���������� �������� ���� � ����� yyyy-mm-dd
	public static String getStringDate(Date date) {
		return FORMATTER_YYYY_MM_DD.format(date);
	}
	
	@Override
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase("toDateInclusive")) toDateInclusive = Boolean.parseBoolean(value);
		else super.setParameter(name, value);
	}
}
