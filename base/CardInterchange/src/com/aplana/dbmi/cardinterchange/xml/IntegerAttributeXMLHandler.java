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
package com.aplana.dbmi.cardinterchange.xml;

import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.IntegerAttribute;

public class IntegerAttributeXMLHandler extends AttributeXMLHandler {
	public IntegerAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}
	
	public AttributeXMLValue[] getValue(Attribute attr) {
		String val = String.valueOf(((IntegerAttribute) attr).getValue());
		return new AttributeXMLValue[] {new AttributeXMLValue(val)};
	}

	public void setValues(List values, Attribute attr) {
		IntegerAttribute a = (IntegerAttribute)attr;
		if (values.isEmpty()) {
			a.setValue(0);
		} else {
			a.setValue(Integer.parseInt(((AttributeXMLValue)values.get(0)).getValue()));
		}
	}
}
