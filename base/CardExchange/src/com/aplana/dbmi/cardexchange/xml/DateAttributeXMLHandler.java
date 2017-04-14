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
package com.aplana.dbmi.cardexchange.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DateAttribute;

public class DateAttributeXMLHandler extends AttributeXMLHandler {

	protected final Log logger = LogFactory.getLog(getClass());

	private final static DateFormat timeFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private final static DateFormat timeFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * ������� ����-������� ������� ������ �� ������ ������������� � ���������� ...
	 */ 
	private final static DateFormat[] IN_FMT_BY_PRIORITY = {
		timeFormat1, timeFormat2, dateFormat
	};

	public DateAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}
	@Override
	public AttributeXMLValue[] getValue(Attribute attr) {
		final DateAttribute datAttr = (DateAttribute) attr;
		final DateFormat format = dateFormat;
		// (2011/02/21 Abdullin R) ����� ����� ����������� ������������� ����
		// ��� ���������� xml �� xsd ��� �������� �� ���� (��������, ���������� 
		// � ������� � ������ !?)
		// if (datAttr.isShowTime()) format = timeFormat1;
		final Date val = datAttr.getValue();
		if (val != null) {
			return new AttributeXMLValue[] { new AttributeXMLValue(format.format(val)) };
		}
		return new AttributeXMLValue[] {};
	}

	@Override
	public void setValues(List values, Attribute attr) throws Exception {
		DateAttribute a = (DateAttribute)attr;
		if (values.isEmpty()) {
			a.setValue(null);
		} else {
			final String value = ((AttributeXMLValue)values.get(0)).getValue();
			Date date = null;
			/*
			try{
				date = timeFormat1.parse(value);
			} catch (ParseException e) {
				try{
					date = dateFormat.parse(value);
				}
				catch (ParseException e1){
				}
			}
			*/
			boolean success = false;
			for (DateFormat fmt : IN_FMT_BY_PRIORITY) {
				try{
					date = fmt.parse(value);
					success = true;
					break;
				}
				catch (ParseException e1){
					// ���� ������ �� ������� - ������� ������
				}
			}
			if (!success) {
				logger.warn( "Unknown date format of value <"+ value 
						+ "> converted to NULL, none of predefined formats fits "
						+ IN_FMT_BY_PRIORITY.toString()
					);
			}
			a.setValue(date);
		}
	}

	@Override
	public boolean matchXmlValue(Attribute attr, AttributeXMLValue value) {
		DateAttribute a = (DateAttribute)attr;
		try {
			return a.getValue() == dateFormat.parse(value.getValue());
		} catch (ParseException e) {
			return false;
		}
	}
}
