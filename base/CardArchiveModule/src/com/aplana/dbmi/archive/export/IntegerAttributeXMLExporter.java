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
package com.aplana.dbmi.archive.export;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.IntegerAttribute;

/**
 * ������� � XML Integer ���������
 * @author ppolushkin
 *
 */
public class IntegerAttributeXMLExporter extends AttributeXMLExporter {

	public IntegerAttributeXMLExporter(Document doc, Attribute attr) {
		super(doc, attr);
	}
	
	@Override
	public Object export() throws DOMException, ParserConfigurationException {
		if(attr != null && !attr.isEmpty()) {
			return addAttr();
		}
		return null;
	}
	
	@Override
	protected Element addAttr() throws DOMException, ParserConfigurationException {
		Element elemAttr = super.addAttr();
		String value = getValue();
		elemAttr.setAttribute("value", value);
		return elemAttr;
	};
	
	@Override
	public String getValue() {
		final IntegerAttribute intAttr = (IntegerAttribute) attr;
		return String.valueOf(intAttr.getValue());
	}

}
