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
package com.aplana.dbmi.jasperreports;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GettingFromXML {
	
	private XPath xpath;
	
	public GettingFromXML(){		
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();		
	}

	// �������� ��������� �������� �� ��������� ������ � xml ��� HtmlAttribute
	public String getLastRecordText(InputStream is) throws Exception {
		ByteArrayInputStream bais = (ByteArrayInputStream) is;
		if (bais != null && bais.available() != 0) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
			final XPathExpression recordExpression = xpath.compile("/report/part");
			NodeList recordNodes = (NodeList)recordExpression.evaluate(doc, XPathConstants.NODESET);
			Element recordEl = (Element)recordNodes.item(recordNodes.getLength() - 1);
			return recordEl.getTextContent();
		}
		return null;
	}
}
