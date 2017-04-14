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
package com.aplana.cms.tags;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentUtils;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.module.docflow.calendar.CalendarAPI;

public class TimeTag implements TagProcessor {
	public static final String ATTR_FORMAT = "format";
	public static final String ATTR_DURATION = "duration";
	public static final String ATTR_CHANGE_TO = "changeto";
	private static CalendarAPI calendar = CalendarAPI.getInstance();
	private DateAttribute field;
	
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		if (!tag.hasAttribute(ContentProducer.ATTR_FIELD))
			throw new Exception("Attribute 'field' is mandatory");
		Attribute attr = ContentUtils.getAttribute(item, tag.getAttribute(ContentProducer.ATTR_FIELD));
		if (attr == null) { // ��������� ���������� ��������, �� �� ������� ������� ����
			field = null;
			return false;
		}
		if (!(attr instanceof DateAttribute))
			throw new Exception("Attribute " + tag.getAttribute(ContentProducer.ATTR_FIELD) +
					" has unexpected type '"+attr.getClass()+ "' instead of '"+ 
					DateAttribute.class+"' in card " + item.getId().getId() );
		field = (DateAttribute) attr;
		return field.getValue() != null;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		if (tag.hasAttribute(ATTR_DURATION) &&
			Boolean.parseBoolean(tag.getAttribute(ATTR_DURATION))){
			try{
				out.write(String.valueOf(calendar.diff(new Date(), field.getValue())));
			} catch(Exception e){e.printStackTrace();}
			return;
		}
		String date = field.getStringValue();
		if (tag.hasAttribute(ATTR_FORMAT)) {
			String pattern = tag.getAttribute(ATTR_FORMAT);
			if(tag.hasAttribute(ATTR_CHANGE_TO)) {
				date = new SimpleDateFormat(pattern).format( new Date(
						field.getValue().getTime()+Long.parseLong(tag.getAttribute(ATTR_CHANGE_TO))));
			} else
				date = new SimpleDateFormat(pattern).format(field.getValue());
		}

		out.write(date);
	}
}