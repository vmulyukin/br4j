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

import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.IntegerAttribute;

public class IntegerAttributeXMLHandler extends AttributeXMLHandler {
	public IntegerAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}

	@Override
	public AttributeXMLValue[] getValue(Attribute attr) {
		String val = String.valueOf(((IntegerAttribute) attr).getValue());
		return new AttributeXMLValue[] {new AttributeXMLValue(val)};
	}

	@Override
	public void setValues(List values, Attribute attr) {
		final IntegerAttribute a = (IntegerAttribute)attr;
		int newValue = 0;
		if (!values.isEmpty()) {
			final AttributeXMLValue xmlAttr = (AttributeXMLValue)values.get(0);
			if (xmlAttr != null && xmlAttr.getValue() != null)
				newValue = Integer.parseInt(( xmlAttr.getValue().trim()) );
		}
		a.setValue(newValue);
	}

	@Override
	public boolean matchXmlValue(Attribute attr, AttributeXMLValue value) {
		IntegerAttribute a = (IntegerAttribute)attr;
		try {
			return a.getValue() == Integer.parseInt(value.getValue());
		} catch (NumberFormatException e) {
			return false;
		}
	}
}