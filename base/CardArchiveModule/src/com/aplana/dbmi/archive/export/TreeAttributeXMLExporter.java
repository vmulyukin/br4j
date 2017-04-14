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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.ReferenceValue;

/**
 * ������� � XML Tree ���������
 * @author ppolushkin
 *
 */
public class TreeAttributeXMLExporter extends AttributeXMLExporter {

	public TreeAttributeXMLExporter(Document doc, Attribute attr) {
		super(doc, attr);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Object export() throws DOMException, ParserConfigurationException {
		if(attr != null && !attr.isEmpty()) {
			Element attrElem = addAttr();
			List<String> value = getValue();
			addValues(attrElem, value);
			return attrElem;
		}
		return null;
	}
	
	@Override
	public List<String> getValue() {
		final Collection col = ((TreeAttribute) attr).getValues();
		List<String> list = new ArrayList<String>();
		Iterator it = col.iterator();
		while (it.hasNext())
		{
			ReferenceValue rv = (ReferenceValue) it.next();
			list.add(String.valueOf(rv.getId().getId()));
		}
		return list;
	}

}
