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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;

public class MessagesReader extends AbstractXmlDescriptorReader {
	private XPathExpression langExpr = xpath.compile("./lang");
	private XPathExpression msgExpr = xpath.compile("./message");

	public MessagesReader(XPath xpath) throws XPathExpressionException {
	}
	
	public Messages read(Element messagesNode) throws XPathExpressionException {
		Messages result = new Messages();
		if (messagesNode != null) {
			NodeList languages = (NodeList)langExpr.evaluate(messagesNode, XPathConstants.NODESET);
			for (int i = 0; i < languages.getLength(); ++i) {
				Element lang = (Element)languages.item(i);
				String code = lang.getAttribute("code");
				NodeList messages = (NodeList)msgExpr.evaluate(lang, XPathConstants.NODESET);
				for (int j = 0; j < messages.getLength(); ++j) {
					Element message = (Element)messages.item(j);
					result.add(message.getAttribute("key"), message.getAttribute("value"), code);
				}
			}
		}
		return result;
	}
}
