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
package com.aplana.dbmi.card.hierarchy.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.card.hierarchy.util.AttributeHandler;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class ConditionReader extends AbstractXmlDescriptorReader {
	private XPathExpression statusExpr = xpath.compile("./status");
	private XPathExpression templateExpr = xpath.compile("./template");
	
	public ConditionReader(XPath xpath) throws XPathExpressionException {
		super(xpath);
	}

	public CardFilterCondition readCondition(Element conditionElem, DataServiceBean serviceBean) throws XPathExpressionException {
		CardFilterCondition result = new CardFilterCondition();
		result.setStates(readObjectIds(conditionElem, statusExpr, CardState.class, true));
		result.setTemplates(readObjectIds(conditionElem, templateExpr, Template.class, true));
		NodeList children = conditionElem.getElementsByTagName("attribute");
		Map attributes = new HashMap(children.getLength());
		for (int j = 0; j < children.getLength(); ++j) {
			Element attrElem = (Element)children.item(j);
			String type = attrElem.getAttribute("type");
			AttributeHandler xmlType = AttributeHandler.getXmlType(type);
			ObjectId attrId = ObjectIdUtils.getObjectId(xmlType.getType(), attrElem.getAttribute("id"), false);
			NodeList valueNodes = attrElem.getElementsByTagName("value");			
			List values = new ArrayList();
			for (int k = 0; k < valueNodes.getLength(); ++k) {
				Element value = (Element)valueNodes.item(k);
				String st = value.getTextContent();
				try {
					values.add(xmlType.stringToValue(st, serviceBean));
				} catch (Exception e) {
					logger.error("Exception caught while reading attribute constraint value: [attrId = " + attrId.getId() + ", value = " + st + "]. Value is ignored.");
				}
			}
			NodeList rangeNodes = attrElem.getElementsByTagName("range");
			for (int k = 0; k < rangeNodes.getLength(); ++k) {
				Element range = (Element)rangeNodes.item(k);
				String st = range.getTextContent();
				try {
					values.add(xmlType.parseValuesRange(st, serviceBean));
				} catch (Exception e) {
					logger.error("Exception caught while parsing attribute constraint values range: [attrId = " + attrId.getId() + ", range = " + st + "]. Range is ignored.");
				}
			}
			attributes.put(attrId, values);
		}
		result.setAttributes(attributes);
		return result;
	}
	
	private List readObjectIds(Node parentNode, XPathExpression xpathExpr, Class type, boolean isNumeric) throws XPathExpressionException {
		NodeList nodes = (NodeList)xpathExpr.evaluate(parentNode, XPathConstants.NODESET);
		List result = new ArrayList(nodes.getLength());
		for (int i = 0; i < nodes.getLength(); ++i) {
			Element node = (Element)nodes.item(i);
			String st = node.getTextContent();
			result.add(ObjectIdUtils.getObjectId(type, st, isNumeric));
		}
		return result;
	}
}
