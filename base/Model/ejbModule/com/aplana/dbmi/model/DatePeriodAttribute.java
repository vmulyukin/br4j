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
package com.aplana.dbmi.model;

import java.util.Date;

import org.springframework.util.StringUtils;

/**	
 * Represents date period 
 * 
 * @author skashanski
 *
 */
public class DatePeriodAttribute extends Attribute {

	
	private DateAttribute dateAttribute;
	
	/** flag to indicate if we need to include empty dates */
	private boolean includedEmpty = false; 
	
	/**
	 * from date 
	 */
	private Date valueFrom;

	/**
	 * to date 
	 */
	private Date valueTo;
	
	/* flag to indicate that current year has been chosen*/
	private boolean currentYear = true;
	
	
	
	public DatePeriodAttribute(DateAttribute dateAttribute) {
		
		super();
		
		this.dateAttribute = dateAttribute;
	}

	
	public boolean isCurrentYear() {
		return currentYear;
	}



	public void setCurrentYear(boolean currentYear) {
		this.currentYear = currentYear;
	}


	


	public boolean isIncludedEmpty() {
		return includedEmpty;
	}


	public void setIncludedEmpty(boolean includedEmpty) {
		this.includedEmpty = includedEmpty;
	}


	public boolean equalValue(Attribute attr) {
		
		if (!(attr instanceof DatePeriodAttribute))
			throw new IllegalArgumentException("Incorrect attribute type comparison: " +
					getClass().getName() + " [" + getId().getId() + "] against " +
					attr.getClass().getName() + " [" + attr.getId().getId() + "]");
		
		Date otherValueFrom = ((DatePeriodAttribute) attr).getValueFrom();
		
		boolean result =  valueFrom == null ? otherValueFrom == null : valueFrom.equals(otherValueFrom);
		if (!result)
			return result;
		
		
		Date otherValueTo = ((DatePeriodAttribute) attr).getValueTo();
		result =   valueTo == null ? otherValueTo == null : valueTo.equals(otherValueTo);

		if (!result)
			return result;
		
		return this.getId().equals(((DatePeriodAttribute) attr).getId());


	}

	public String getStringValue() {
		
		String valueFromStr = getStringValue(valueFrom);
		
		String valueToStr = getStringValue(valueTo);
		
		if ((!StringUtils.hasText(valueFromStr)) && (!StringUtils.hasText(valueFromStr)))
			return ""; 
		
		return  valueFromStr + "-" +  valueToStr;
	}
	
	private String getStringValue(Date value) {
		
		if (value == null)
			return "";
		
	//	if (dateAttribute.isShowTime()) {
			//String pattern;
		if (dateAttribute.getTimePattern() != null)
			// pattern = dateAttribute.getTimePattern();
			return ContextProvider.getContext().getLocaleDateTime(value,
					dateAttribute.getTimePattern());

		return ContextProvider.getContext().getLocaleDate(value);
			
			
			//return ContextProvider.getContext().getLocaleDateTime(value,
			//		pattern);
	//	} else
			//return ContextProvider.getContext().getLocaleDate(value);

	}

	public Object getType() {
		
		return TYPE_DATE_PERIOD;
		
	}

	public boolean verifyValue() {
		
		if (valueFrom == null)
			return true;
		
		return valueFrom.before(valueTo) || valueFrom.equals(valueTo);
	}

	
	
	
	public void clear() {
		
		valueFrom = null;
		
		valueTo = null;
		
		currentYear = true; 
	}
	
	
	

	public boolean isEmpty() {

		return ((valueFrom == null) && (valueTo == null));
		
	}

	public Date getValueFrom() {
		return valueFrom;
	}

	public void setValueFrom(Date valueFrom) {
		this.valueFrom = valueFrom;
	}

	public Date getValueTo() {
		return valueTo;
	}

	public void setValueTo(Date valueTo) {
		this.valueTo = valueTo;
	}

	public DateAttribute getDateAttribute() {
		return dateAttribute;
	}



	public void setDateAttribute(DateAttribute dateAttribute) {
		this.dateAttribute = dateAttribute;
	}


	@Override
	public void setValueFromAttribute(Attribute attr){
		throw new UnsupportedOperationException();
	}
	
	
	
	
	
	

}
