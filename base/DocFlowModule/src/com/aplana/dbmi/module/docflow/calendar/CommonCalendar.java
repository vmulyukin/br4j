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
package com.aplana.dbmi.module.docflow.calendar;

import java.util.Calendar;
import java.util.Date;

/**
 * ��������� ��� ������ ��� ����� �������� � ����������
 * @author desu
 */
public class CommonCalendar extends CalendarAPI {
	
	protected CommonCalendar() {}
	
	public boolean isWorkDay(Date date) {
		return true;
	}
	
	public Date addToToday(int increment) {
		return addToDate(increment, new Date());
	}
	
	public Date addToDate(int increment, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		clearParameters(calendar);
		calendar.add(Calendar.DAY_OF_YEAR, increment);
		return calendar.getTime();
	}
	
	public int diff(Date firstDate, Date secondDate) {
		Calendar earlier = Calendar.getInstance();
	    Calendar later   = Calendar.getInstance();
	    if (firstDate.compareTo(secondDate) < 0) {
	        earlier.setTime(firstDate);
	        later.setTime(secondDate);
	    } else {
	        earlier.setTime(secondDate);
	        later.setTime(firstDate);
	    }
	    clearParameters(earlier);
	    clearParameters(later);
		
	    int daysBetween = 0;
	    Calendar date = (Calendar) earlier.clone();  
	    while (date.before(later)) {  
	        date.add(Calendar.DAY_OF_MONTH, 1);  
	        daysBetween++;  
	    }  
		return daysBetween;
	}
}
