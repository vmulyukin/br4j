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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.model.Attribute;

/**
 * ����� ��� ����������� �������
 * � ����� � ���, ��� ����� CardHistoryRecordAttributeViewer ������ �����������, � �� ���� ������ ��������, �������� ������ ����
 * @author ppolushkin
 * @since 09.12.14
 */
public class RequestToChangeHistoryRecordAttributeViewer extends CardHistoryRecordAttributeViewer {

	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	protected List<List<Object>> getRecordsFromAttribute(Attribute attr) {
		final List<List<Object>> records = new LinkedList<List<Object>>();
		if(attr == null) return records;
		final String text = attr.getStringValue();
		try {
			final Document doc = readDocument(text);
			if (doc != null) {
				final NodeList parts = doc.getElementsByTagName("part");
				List<Object> rec;

				for (int i = 0; i < parts.getLength(); i++) {
					final Element el = (Element) parts.item(i);
					Date date = null;
					String str;
					rec = new ArrayList<Object>();
					for(String col : getColumnsRecord()) {
						if("date".equalsIgnoreCase(getTypesValue(col))) {
							date = parseDate(el.getAttribute(col));
							rec.add(date);
						} else {
							str = el.getAttribute(col);
							rec.add(str);
						}
					}
					records.add(rec);
				}	
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return records;
	}

	protected Date parseDate(String text) {
		Date resultDate = null;
		Date originalDateTime = null;

		try {
			originalDateTime =  DATE_TIME_FORMAT.parse(text);
		} catch (ParseException e) {
			logger.warn(e);
		}

		resultDate = addServerTimezone(originalDateTime);
		return resultDate;
	}

	private static Date addServerTimezone(Date date) {
		if (date == null) return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MILLISECOND, java.util.Calendar.getInstance().getTimeZone().getRawOffset());
		return calendar.getTime();
	}

	@Override
	public void setParameter(String name, String value) {
		super.setParameter(name, value);
	}
	
}
