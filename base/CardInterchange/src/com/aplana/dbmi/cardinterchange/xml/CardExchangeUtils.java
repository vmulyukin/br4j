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

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.TemplateBlock;

public class CardExchangeUtils {
	private final static String NS = "http://aplana.com/dbmi/exchange/model/Card";
	
	public static Element getCardElement(Card c, Document doc) {
		Element result = doc.createElementNS(NS, "card");
		result.setAttribute("id", String.valueOf(c.getId().getId()));
		result.setAttribute("templateId", String.valueOf(c.getTemplate().getId()));
		Iterator i = c.getAttributes().iterator();
		while (i.hasNext()) {
			TemplateBlock block = (TemplateBlock)i.next();
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				Attribute attr = (Attribute)j.next();
				AttributeXMLHandler xmlType = AttributeXMLHandler.getXmlType(attr.getClass());
				if (xmlType == null) {
					//logger.warn("Unsupported attribute type: " + attr.getClass());
					continue;
				}
				Element attrElem = doc.createElementNS(NS, "attribute");
				// TODO: ��������, ��� ����� �������� ���� ��������� �� �� ������
				String attrCode = (String)attr.getId().getId();
				if (attrCode == null) {
					continue;						
				}
				attrElem.setAttribute("code", attrCode);
				attrElem.setAttribute("type", xmlType.getXmlType());
				if (attr.isMultiValued()) {
					attrElem.setAttribute("multiValued", "true");	
				}
				attrElem.setAttribute("name", attr.getName());
				AttributeXMLValue[] values = xmlType.getValue(attr);
				for (int k = 0; k < values.length; ++k) {
					AttributeXMLValue val = values[k];
					Element valElem = doc.createElementNS(NS, "value");
					valElem.setTextContent(val.getValue());					
					if (val.getDescription() != null) {
						valElem.setAttribute("description", val.getDescription());
					}
					attrElem.appendChild(valElem);
				}
				result.appendChild(attrElem);
			}
		}
		return result;		
	}
}
