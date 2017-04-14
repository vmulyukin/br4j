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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public class StringAttributeHandler extends AttributeHandler {
	public StringAttributeHandler(String value, Class clazz) {
		super(value, clazz);
	}
	public boolean matchValue(Attribute attr, Object value) {
		StringAttribute a = (StringAttribute)attr;
		if (a.getValue() == null || "".equals(a.getValue())) {
			return value == null || "".equals(value);
		}
		return a.getValue().equals(value);
	}
	public Object stringToValue(String st, DataServiceBean serviceBean) throws Exception {
		return st;
	}
}
