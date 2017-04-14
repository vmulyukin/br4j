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
package com.aplana.dbmi.card.hierarchy.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.aplana.dbmi.service.DataServiceBean;

public class DateAttributeHandler extends AttributeHandler {
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private final static DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public DateAttributeHandler(String value, Class clazz) {
		super(value, clazz);
	}
	public Object stringToValue(String st, DataServiceBean serviceBean) throws ParseException {
		try {
			return dateTimeFormat.parseObject(st); 
		} catch (ParseException e) {
			return dateFormat.parseObject(st);
		}		 
	}
}
