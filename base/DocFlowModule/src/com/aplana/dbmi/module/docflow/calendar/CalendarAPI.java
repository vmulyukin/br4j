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

import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.DataException;

/**
 * ������� ����� ��� ����������
 * @author desu
 */
public abstract class CalendarAPI {
	
	private static final Log logger = LogFactory.getLog(CalendarAPI.class);
	protected static final String xml = "dbmi/WorkCalendar.xml";
	protected static Boolean workCalendarEnabled = false;
	
	//�������� ������� ���������
	static Document doc = null;
	static {
		try {
			InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(xml);
			if (stream != null) {
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
				NamedNodeMap attrs = doc.getDocumentElement().getAttributes();
				if (attrs != null && attrs.getNamedItem("enabled") != null 
						&& Boolean.parseBoolean(((Attr)attrs.getNamedItem("enabled")).getValue())) {
					workCalendarEnabled = true;
				} else {
					workCalendarEnabled = false;
				}
			}
		} catch (Exception e) {
			logger.warn("File "+xml+" not found or incorrect. Apply default settings. Using common calendar." );
		}
	}
	
	/**
	 * �������� ��������� � ������������ � ����������� ����������
	 * @return WorkCalendar ��� CommonCalendar
	 */
	public static final CalendarAPI getInstance() {
		if (workCalendarEnabled) {
			return new WorkCalendar();
		}
		return new CommonCalendar();
	}
	
	/**
	 * �������� ������� ���������. ������������ � �������
	 * @return WorkCalendar
	 */
	public static final CalendarAPI getWorkInstance() {
		return new WorkCalendar();
	}
	
	/**���������� �������� �� ���� ������� ���
	 * 
	 * @param date
	 * @return true  ���� ���� - �����������, false � ��������� ������
	 * @throws ParseException
	 */
	abstract public boolean isWorkDay(Date date) throws ParseException;
	
	/** ����� ��������� � ������� ���� "increment" ����
	 * 
	 * @param increment
	 * @return ����+increment, � ������ �������� � ������� ����
	 * @throws DataException 
	 * @throws DatabaseException 
	 */
	abstract public Date addToToday(int increment) throws DataException;
	
	/** ����� ��������� � ����� ���� "increment" ����
	 * 
	 * @param increment - ����� ����, ������� ����� ��������� � ����
	 * @param date - ����, � ������� ����� ��������� increment
	 * @return ����, ����������� �� increment � ������ �������� � ������� ����
	 * @throws DataException 
	 * @throws DataException 
	 */
	abstract public Date addToDate(int increment, Date date) throws DataException;
	
	/** ����� ��������� ������� � ������� ���� ����� ����� ������
	 * 
	 * @param firstDate
	 * @param secondDate
	 * @return
	 * @throws DataException 
	 */
	abstract public int diff(Date firstDate, Date secondDate) throws DataException;
	
	/** ��������� ������� � ���������.
	 * 
	 * @param calendar - ���������, ��� �������� ����� �������� �����
	 */
	protected Calendar clearParameters(Calendar calendar){
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.AM_PM, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar; 
	}
}
