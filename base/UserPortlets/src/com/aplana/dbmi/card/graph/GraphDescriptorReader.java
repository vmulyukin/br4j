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
package com.aplana.dbmi.card.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.MessagesReader;
import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.model.ObjectId;

public class GraphDescriptorReader extends AbstractXmlDescriptorReader {
	private XPathExpression graphExpression = xpath.compile("/graph");
	private XPathExpression cardSetExpression = xpath.compile("./cardSet");
	private XPathExpression linkExpression = xpath.compile("./adjacencies/link");
	private XPathExpression msgsExpression = xpath.compile("/graph/messages");
	private XPathExpression attrsExpression = xpath.compile("./attributes/attribute");
	private XPathExpression paramsExpression = xpath.compile("./parameters/parameter");
	
	
	private MessagesReader messagesReader;
	
	public GraphDescriptorReader() throws XPathExpressionException {
		super();
		messagesReader = new MessagesReader(this.xpath);
	}
	public GraphDescriptor read(InputStream stream) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		GraphDescriptor descriptor = new GraphDescriptor();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		Element graph = (Element)graphExpression.evaluate(doc, XPathConstants.NODE);
		
		int depth = Integer.parseInt(graph.getAttribute("depth"));
		descriptor.setDepth(depth);
		String firstCardSet = graph.getAttribute("firstCardSet");
		descriptor.setNameFirstCardSet(firstCardSet);
		
		NodeList cardSetNodes = (NodeList)cardSetExpression.evaluate(graph, XPathConstants.NODESET);
		Map cardSets = new HashMap();
		for (int i=0; i < cardSetNodes.getLength(); i++) {
			Element cardSetElem = (Element)cardSetNodes.item(i);
			GraphDescriptor.CardSet cs = new GraphDescriptor.CardSet();
			cs.setType(cardSetElem.getAttribute("type"));
			cs.setLabelType(cardSetElem.getAttribute("labelType"));
			
			NodeList linkNodes = (NodeList)linkExpression.evaluate(cardSetElem, XPathConstants.NODESET);
			Map links = new HashMap();
			for (int j=0; j < linkNodes.getLength(); j++) {
				Element linkElem = (Element)linkNodes.item(j);
				String type = linkElem.getAttribute("type");
				Class typeAttr = SearchXmlHelper.getAttrClass(type);
				ObjectId idAttr = new ObjectId(typeAttr, linkElem.getAttribute("linkAttr"));
				links.put(idAttr, linkElem.getAttribute("targetSet"));
			}
			cs.setLinks(links);
			cardSets.put(cs.getType(), cs);
			
			Map attrs = new HashMap();
			NodeList attrNodes = (NodeList)attrsExpression.evaluate(cardSetElem, XPathConstants.NODESET);
			for (int j=0; j < attrNodes.getLength(); j++) {
				Element attrElem = (Element)attrNodes.item(j);
				String type = attrElem.getAttribute("type");
				Class typeAttr = SearchXmlHelper.getAttrClass(type);
				ObjectId idAttr = new ObjectId(typeAttr, attrElem.getAttribute("attr"));
				
				String lableAttr = attrElem.getAttribute("name");
				attrs.put(idAttr, lableAttr);
			}
			cs.setAttrs(attrs);
			
			Map/*nameAttr -> Map(nameParam -> valueParam)*/ cardSetParams = new HashMap();
			NodeList paramNodes = (NodeList)paramsExpression.evaluate(cardSetElem, XPathConstants.NODESET);
			for (int j=0; j < paramNodes.getLength(); j++) {
				Element paramEl = (Element)paramNodes.item(j);
				String nameAttr = paramEl.getAttribute("attr");
				String nameParam = paramEl.getAttribute("name");
				String value = paramEl.getAttribute("value");
				Map/*nameParam -> valueParam*/ attrParams = (Map) cardSetParams.get(nameAttr);
				if (attrParams == null) {
					attrParams = new HashMap();
					cardSetParams.put(nameAttr, attrParams);
				}
				attrParams.put(nameParam, value);
			}
			cs.setParams(cardSetParams);
		}
		descriptor.setCardSets(cardSets);
		
		
		Element messagesNode = (Element)msgsExpression.evaluate(doc, XPathConstants.NODE);
		Messages messages = messagesReader.read(messagesNode);
		descriptor.setMessages(messages);
		return descriptor;
	}
}
