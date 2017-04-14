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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.portlet.ActionRequest;
import org.apache.commons.codec.binary.Hex;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.DataException;

/**
 * ���������: 
 * 		day_time_default: �������� ��-��������� ��� ����� ������� ����� � ������� hh:mm:ss.
 * �����������, ��� ��������� ������� �����, ����� ������ ����� ���� (dd-mm-yyyy),
 * �� ����� ����� ��������� �� �����������:
 * 		�������� null (�� ���������), ��� �����, ��� "now" - ������������ ������� ����� �������,
 * 		��������-������ "null" - �� ����������� ����� ����� ������,
 * 		����� ������ �������� ��������� ��� ���� �������� ����� � ������� "HH:mm:ss".
 * 
 *
 */
public class DateTimeAttributeEditor extends JspAttributeEditor {
	
	/**
	 * �������� ��-��������� ��� ����� ������� ����� � ������� hh:mm:ss.
	 * �����������, ��� ��������� ������� �����, ����� ������ ����� ���� (dd-mm-yyyy),
	 * �� �� ����� �����:
	 * �������� null (�� ���������), ��� �����, ��� "now" - ������������ ������� ����� �������,
	 * 		"null" (������) - �� ����������� ����� �����,
	 * 		����� ������ �������� - ��������� ��� ���� �������� ����� � ������� "HH:mm:ss".
	 */
	private static final String PARAM_DAY_TIME_DEFAULT = "day_time_default";
	/**
	 *  ��������� � ������������� ��������� �� ����������������� �����.
	 *  � ��������� ����� ������������ ������ ���� ��� �������� ��������.
	 */
	private static final String PARAM_DAY_TIME_PATTERN = "datePattern";

	private static final String FMT_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
	protected static final String FMT_DATEONLY_YYYY_MM_DD = "yyyy-MM-dd";
	private static final String FMT_T_HH_MM_SS = "'T'HH:mm:ss";

	private static final SimpleDateFormat FORMATTER_YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat(FMT_YYYY_MM_DD_T_HH_MM_SS);
	protected static final SimpleDateFormat FORMATTER_YYYY_MM_DD = new SimpleDateFormat(FMT_DATEONLY_YYYY_MM_DD);
	private static final SimpleDateFormat FORMATTER_T_HH_MM_SS = new SimpleDateFormat(FMT_T_HH_MM_SS);

	/**
	 * �������� , ������� ��������� ��� ������� ����� "hh:mm", ���� ��� �����.
	 * �������� NULL ��� ������ ������ ����������� ���������� �������� ������� (������� ����������).
	 */
	private String dayTimeDefault = null;
	/**
	 * ��������, ������� ������������� ����������, �������� ������ ����, ������� ����� ����������� ��� ��������� ��������.
	 * ���� �������� NULL ��� �� ������������� ��������, ����� �������������� ������ ���� ��-���������.
	 */
	private String dayTimePattern = null;

	public DateTimeAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/DateTime.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/DateTimeInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException 
	{
		final DateAttribute dateAttr = (DateAttribute) attr;
		final String date = request.getParameter(getAttrHtmlId(attr)+"_date");
		String time = request.getParameter(getAttrHtmlId(attr)+"_time");
		final boolean timeIsSet = (time != null) && !time.equals("");
		final boolean showTime =  dateAttr.isShowTime();
		
		try {
			SimpleDateFormat formatter = null;
			if ((date == null)) {
				return false;
			} else if (date.equals("")) {
				dateAttr.setValue(null);
			} else { // ���� ������
				// ���� ����� �� ����������� � ���� �� ���������� - �� ��������� ����� ��������
				if (timeIsSet || cmpWithDayPrecision(dateAttr.getValue(),FORMATTER_YYYY_MM_DD.parse(date))) {
					if (!showTime
							/*&& cmpWithDayPrecision(dateAttr.getValue(),
									FORMATTER_YYYY_MM_DD.parse(date))*/) {
						// ����� �� ����������
						formatter = FORMATTER_YYYY_MM_DD;
					} else {
						if (dayTimePattern != null && !dayTimePattern.isEmpty() && dayTimePattern.matches(".*(y|M|w|W|d|D|F|E|a|H|k|K|h|m|s|S|z|Z).*")) {
							/*
							 * � �������� "dayTimePattern" ����������� �������� �� ��������� "datePattern" ����������������� �����.
							 * ���� � ���������������� ����� Portal/conf/dbmi/card/editors.xml ��� �������� �������� ���� ���������� �������� �
							 * �������� ���� �� ���� ����������� ������ �� ����� �������, � ��������� ������ ������������ ������ ��-���������.
							 * ������ ��� ������� ���� ������ ���� ������ � ������������ � ��������� ��� SimpleDateFormat.
							 * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat</a>
							 */
							formatter = new SimpleDateFormat(dayTimePattern);
						} else {
							formatter = FORMATTER_YYYY_MM_DD_T_HH_MM_SS;
						}
						if (!timeIsSet) {
							// ����� ����������, �� ��� �� ������
							{
								final Calendar calendar = Calendar.getInstance();
								if (dayTimeDefault == null
										|| "".equals(dayTimeDefault)
										|| "now".equalsIgnoreCase(dayTimeDefault)){
									// ������� ����� ...
									time = "T" + calendar.get(Calendar.HOUR_OF_DAY)
											+ ":" + calendar.get(Calendar.MINUTE)
											+ ":" + calendar.get(Calendar.SECOND);
									formatter = FORMATTER_YYYY_MM_DD_T_HH_MM_SS;
								} else if ("null".equalsIgnoreCase(dayTimeDefault)) {
									// ������ �����
									time = "";
									formatter = FORMATTER_YYYY_MM_DD;
								} else
									// ���� �������� � ����������� ...
									time = "T" + dayTimeDefault;
							}
						}
					}
					if (formatter != null) {
						final Date newDate = formatter.parse(date + time);
						dateAttr.setValue(newDate);
					}
				}
			}			
		} catch (ParseException e) {
			throw new DataException("Value transferred in date or time parametre is badly formatted");
		}

		
/*		if (date.equals("")) {
			dateAttr.setValue(null);
		}
		else {
			try {
				if (dateAttr.isShowTime() && time!= null && !time.equals("")) {
					dateAttr.setValue((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(date+time));
				} else {
					Calendar calendar=Calendar.getInstance();
					//���� ���� �� ������, � ����� ������, �� ������� ������� �����
					time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
					dateAttr.setValue((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")).parse(date+'T'+time));					
				}
			} catch (ParseException e) {
				throw new DataException("Value transferred in date or time parametre is badly formatted");
			}
		}
*/
		return true;
	}
	/**
	 * �������� ���� (���, �����, ����) � ������������� �� ��������� � �������� �������������
	 * @param d1 ���� �� ���������
	 * @param d2 �������� �������������
	 * @return <b>false</b>, ���� ��������� ��� ��� ���� �������� ����� null <br>
	 * <b>true</b>, ���� ���� ��������� ��� ���� �� ��������� �� �����, �� ����� ����������������
	 */
	private static boolean cmpWithDayPrecision(java.util.Date d1, java.util.Date d2) {
		if (d1 == null) return d2 != null;
		if (d2 == null) return false;

		/* (!) ��� �����������, �.�. ������� � ���� ��������, � ������� ����� 
		 * ����� � ���������� ��� �������� ���������, ���� � ������� ���� ��� 
		 * ����� �� ���:
		private static long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
		final long days1 = d1.getTime() / MILLIS_PER_DAY;
		final long days2 = d2.getTime() / MILLIS_PER_DAY;
		return days1 == days2;
		*/

		/* �������� ������ ���, ����� � ���� � ������� ��������� ���� ... */
		final Calendar c1 = Calendar.getInstance();
		final Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		return		!(( c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
				&& 	( c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
				&& 	( c1.get(Calendar.DATE) == c2.get(Calendar.DATE)))
				;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_DAY_TIME_DEFAULT.equalsIgnoreCase(name)) {
			dayTimeDefault = (value == null) ? null : value.trim();
		} else if (PARAM_DAY_TIME_PATTERN.equalsIgnoreCase(name)) {
			dayTimePattern = (value == null) ? null : value.trim();
		} else {
			super.setParameter(name, value);
		}
	}
	
	public static String getAttrHtmlId(Attribute attr) {
		String st = (String)attr.getId().getId();
		st = new String(Hex.encodeHex(st.getBytes()));
		return "attr_" + st; 
	}
	
	// ���������� �������� ���� � ����� yyyy-mm-dd
	public static String getStringDate(DateAttribute attr) {
		return DateUtils.getStringDate(attr.getValue());
	}
	
	// ���������� �������� ������� � ����� THH:mm:ss
	public static String getStringTime(DateAttribute attr) {
		return DateUtils.getStringTime(attr.getValue());
	}

}