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
package com.aplana.dbmi.cardinterchange;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.cardinterchange.xml.AttributeXMLHandler;
import com.aplana.dbmi.cardinterchange.xml.AttributeXMLValue;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataServiceBean;

public class Importer {
	private class CardInfo {
		ObjectId newCardId;
		Map cardLinks = new HashMap();
	}
	
	private String filename;
	private DataServiceBean serviceBean;
	private Map cardIds;
	private int warningsCount;
	private String identifiersFilename = "identifiers.txt";
	private PrintWriter identifiersWriter;
	
	public Importer() {
		cardIds = new HashMap();
		warningsCount = 0;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setIdentifiersFilename(String identifiersFilename) {
		this.identifiersFilename = identifiersFilename;
	}
	
	public void setServiceBean(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public void importCards() throws Exception {
		warningsCount = 0;
		System.out.println("Importing from file " + filename);		
		FileInputStream fis = new FileInputStream(filename);
		
		if (identifiersFilename != null) {
			identifiersWriter = new PrintWriter(new FileWriter(identifiersFilename));
		} else {
			identifiersWriter = new PrintWriter(System.out);
		}
		
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = parser.parse(fis);
		Element root = doc.getDocumentElement();
		NodeList cardElements = root.getElementsByTagName("card");
		int sz = cardElements.getLength();
		System.out.println("Found " + sz + " cards to be imported from file");
		for (int i = 0; i < sz; ++i) {
			Element cardElement = (Element)cardElements.item(i);
			processCard(cardElement);
			if ((i + 1) % 10 == 0) {
				System.out.println(String.valueOf(i + 1) + " cards imported");
			}
		}
		System.out.println("All " + sz + " cards imported.");
		System.out.println("Updating links and unlocking created cards...");
		int cnt = 0;
		Iterator i = cardIds.keySet().iterator();
		while(i.hasNext()) {
			ObjectId oldCardId = (ObjectId)i.next();
			CardInfo ci = (CardInfo)cardIds.get(oldCardId);
			UnlockObject unlock = new UnlockObject(ci.newCardId);			
			if (!ci.cardLinks.isEmpty()) {
				Card card = (Card)serviceBean.getById(ci.newCardId);
				Iterator j = ci.cardLinks.keySet().iterator();
				while (j.hasNext()) {
					ObjectId attrId = (ObjectId)j.next();
					CardLinkAttribute ca = (CardLinkAttribute)card.getAttributeById(attrId);
					if (ca == null) {
						System.out.println("No attribute with id = " + attrId.getId() + " found in card " + ci.newCardId.getId());
						++warningsCount;
					}
					Set oldLinkIds = (Set)ci.cardLinks.get(attrId);
					List newCardLinks = new ArrayList(oldLinkIds.size());
					Iterator k = oldLinkIds.iterator();
					while (k.hasNext()) {
						ObjectId oldLinkId = (ObjectId)k.next();
						CardInfo linkInfo = (CardInfo)cardIds.get(oldLinkId);
						if (linkInfo == null) {
							System.out.print("Reference to card " + oldLinkId.getId() + " will be ignored as it was not imported");
							continue;
						} else {
							newCardLinks.add(DataObject.createFromId(linkInfo.newCardId));
						}
					}
					ca.setValues(newCardLinks);
				}
				serviceBean.saveObject(card);
			}
			serviceBean.doAction(unlock);
			if ((++cnt) % 10 == 0) {
				System.out.println(String.valueOf(cnt) + " cards processed");
			}
		}
		
		identifiersWriter.close();
		System.out.println("Import finished successfuly with " + warningsCount + " warnings");
	}
	
	private void processCard(Element root) throws Exception {
		ObjectId oldCardId;
		try {
			oldCardId = new ObjectId(Card.class, Long.parseLong(root.getAttribute("id")));
		} catch (Exception e) {
			oldCardId = null;
		}		
		if (oldCardId == null) {
			throw new Exception("Couldn't find original cardId");
		}
		
		ObjectId templateId = new ObjectId(Template.class, Long.parseLong(root.getAttribute("templateId")));
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		Card card = (Card)serviceBean.doAction(createCard);

		NodeList nList = root.getElementsByTagName("attribute");
		for (int i = 0; i < nList.getLength(); ++i) {
			processAttribute((Element)nList.item(i), card);
		}
		
		CardInfo ci = new CardInfo();
		Iterator i = card.getAttributes().iterator();
		while (i.hasNext()) {
			AttributeBlock block = (AttributeBlock)i.next();
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				Attribute attr = (Attribute)j.next();
				if (attr instanceof CardLinkAttribute) {
					CardLinkAttribute ca = (CardLinkAttribute)attr;
					if (!ca.isEmpty()) {
						Set links = ObjectIdUtils.collectionToSetOfIds(ca.getValues());
						ci.cardLinks.put(attr.getId(), links);
						ca.setValues(new ArrayList(0));
					}
				}
			}
		}
		ci.newCardId = (ObjectId)serviceBean.saveObject(card);
		identifiersWriter.println(oldCardId.getId().toString() + "\t -> \t" + ci.newCardId.getId().toString());
		identifiersWriter.flush();
		cardIds.put(oldCardId, ci);
	}
	
	private void processAttribute(Element attributeNode, Card c) throws Exception {
		String attrCode = attributeNode.getAttribute("code");
		String attrType = attributeNode.getAttribute("type");
		AttributeXMLHandler attrXmlType = AttributeXMLHandler.getXmlType(attrType);
		if (attrXmlType == null) {
			System.out.println("Unsupported attribute type '" + attrType + "' ignored");
			++warningsCount;
			return;
		}
		ObjectId attrId = ObjectIdUtils.getObjectId(attrXmlType.getType(), attrCode, false);
		Attribute attr = c.getAttributeById(attrId);
		if (attr == null) {
			System.out.println("Attribute with code = " + attrId.getId() + " not found in destanation card");
			++warningsCount;
			return;
		}		
		NodeList values = attributeNode.getElementsByTagName("value");
		List attrValues = new ArrayList(values.getLength());
		for (int i = 0; i < values.getLength(); ++i) {
			Element value = (Element)values.item(i);
			attrValues.add(new AttributeXMLValue(value.getTextContent()));
			/*
			NodeList children = value.getChildNodes();
			StringBuffer buf = new StringBuffer();
			for (int j = 0; j < children.getLength(); ++j) {
				CDATASection data = (CDATASection)children.item(j);
				buf.append(data.getData());
			}
			attrValues.add(new AttributeXMLValue(buf.toString()));
			*/
		}
		
		if (!attr.isMultiValued() && attrValues.size() > 1) {
			System.out.println("Multiple values found for single-valued attribute '" + attrCode + "'. Only first value will be used.");
			++warningsCount;
		}
		attrXmlType.setValues(attrValues, attr);
	}
	
}
