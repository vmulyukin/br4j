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
package com.aplana.dbmi.model.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author ppolushkin
 *
 */
public class DateUtils {
	
	private static final String FMT_T_HH_MM_SS = "'T'HH:mm:ss";
	protected static final String FMT_DATEONLY_YYYY_MM_DD = "yyyy-MM-dd";
	
	protected static final SimpleDateFormat FORMATTER_YYYY_MM_DD = new SimpleDateFormat(FMT_DATEONLY_YYYY_MM_DD);
	private static final SimpleDateFormat FORMATTER_T_HH_MM_SS = new SimpleDateFormat(FMT_T_HH_MM_SS);
	
	/**
	 * ��������� ���� � ������ ����-���� (������������ �����, ����� ���������� ���� �� ��)
	 * @param value desired value
	 */
	public static Date setValueWithTZ(Date value) {
		// ����������� ����� � ������� ������� ���� (� ������ � ������� ���� ���������� ������������)
		if (value!=null){
			Calendar calendar = Calendar.getInstance();
			/*
			calendar.setTimeInMillis(value.getTime());
			calendar.set(Calendar.ZONE_OFFSET, value.getTimezoneOffset());
			this.value = new java.sql.Date(calendar.getTimeInMillis());
			*/
			calendar.setTime(value);
			final int millisTZO = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
			// ���� ���������� ��� �������
			// this.value = new java.sql.Date(calendar.getTimeInMillis() + millisTZO); 
			return new Date(calendar.getTimeInMillis() + millisTZO); 
		} else {
			return value;
		}		
	}
	
	public static Date toUTC(Date value) {
		if (value != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(value);
			final int millisTZO = c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET);
			return new Date(c.getTimeInMillis() - millisTZO);
		} else {
			return value;
		}
    }
	
	// ���������� �������� ���� � ����� yyyy-mm-dd
	public static String getStringDate(Date date) {
		return FORMATTER_YYYY_MM_DD.format(date);
	}
		
	// ���������� �������� ������� � ����� THH:mm:ss
	public static String getStringTime(Date date) {
		return FORMATTER_T_HH_MM_SS.format(date);
	}
	
	// ��������� � ������� ���� ������������ ���������� ����
	public static Date addDaysToCurrent(int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}
	
	// ��������� � ������� ���� �� ��� ���� ���������� ���� � ����������� �� ���������� �������
	public static Date addDaysToCurrentWIthCond(boolean cond, int days1, int days2) {
		Calendar calendar = Calendar.getInstance();
		if(cond)
			calendar.add(Calendar.DATE, days1);
		else 
			calendar.add(Calendar.DATE, days2);
		return calendar.getTime();
	}
	
	/**
	 * Sets time to zero
	 * @param date
	 * @return
	 */
	public static Date timeToZero(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
