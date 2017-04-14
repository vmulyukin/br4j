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
package com.aplana.dmsi;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ����� <code>CustomDataSource</code> ��������� ��� ���������� JasperReport'�� � �������������� HashMap'� <code>headFields</code> � 
 * ������ HashMap'�� <code>baseData</code>.
 * ��� ��������� ������ ��������� ������� ��������� ������, � ��� �� ��������� �������� ������� (���� Details)
 * <p>
 *
 * @author  aklyuev
 */
public class CustomDataSource implements JRDataSource {

	public static final String TIMEZONE_NAME = "Europe/Moscow";
	public static final String EMPTY_STR = "";
	public static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");

	public Map<String, Object> headFields;
	public List<Map<String, Object>> baseData;
	Iterator<Map<String, Object>> iter;
	Map<String, Object> currentObject;
	int index = 0;

	public CustomDataSource(Map<String, Object> headFields, List<Map<String, Object>> baseData) {
		this.headFields = headFields;
		this.baseData = baseData;
		if (baseData.size() > 0) {
			Collections.sort(this.baseData, RowComparator);
		} else {
			//���� �������� ������ ���, �� ��������� ������ ������ ��� ����, ����� 
			//���� �� ������������� �������� � ����������
			this.baseData.add(Collections.<String, Object> emptyMap());
		}
		this.iter = this.baseData.iterator();
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException {
		String fieldName = field.getName();

		if (fieldName.equalsIgnoreCase("index")) {
			return index;
		} else if (headFields != null && headFields.containsKey(fieldName)) {
			return formatData(headFields.get(fieldName));
		}
		else if (currentObject != null) {
			return formatData(currentObject.get(fieldName));
		}
		return "";
	}

	@Override
	public boolean next() throws JRException {
		if (iter.hasNext()) {
			currentObject = iter.next();
			index++;
			return true;
		}
		return false;
	}

	private Object formatData(Object value) {
		Object result = EMPTY_STR;
		if (value != null) {
			if (value instanceof XMLGregorianCalendar) {
				Calendar calendar = ((XMLGregorianCalendar)value).toGregorianCalendar();
				if (calendar != null) {
					calendar.setTimeZone(TimeZone.getTimeZone(TIMEZONE_NAME));
					result = formatTime(calendar.getTime());
				}
			} else if (value instanceof Date) {
				result = formatTime((Date) value);
			} else {
				result = value;
			}
		}
		return result;
	}

	private String formatTime(Date time) {
		String result = EMPTY_STR;
		if (time != null) {
			result = DATE_TIME_FORMATTER.format(time);
		} 
		return result;
	}

	private static Comparator<Map<String, Object>> RowComparator = new Comparator<Map<String, Object>>() {

		@Override
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			int result = 0;
			String sender1 = (String) o1.get("senderOrgFullName");
			String sender2 = (String) o2.get("senderOrgFullName");
			if (sender1 != null && sender2 != null) {
				if (!sender1.equalsIgnoreCase(sender2)) {
					result = sender1.compareTo(sender2);
				} else {
					XMLGregorianCalendar created1 = (XMLGregorianCalendar) o1.get("elmCreatedDate");
					XMLGregorianCalendar created2 = (XMLGregorianCalendar) o2.get("elmCreatedDate");
					if (created1 != null && created2 != null) {
						result = created1.compare(created2);
					} else if (created1 == null && created2 != null) {
						result = -1;
					} else if (created1 != null && created2 == null) {
						result = 1;
					}
				}
			} else if (sender1 == null && sender2 != null){
				result = -1;
			} else if (sender1 != null && sender2 == null){
				result = 1;
			}
			return result;
		}

	};
}