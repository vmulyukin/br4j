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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.cardinterchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class Exporter {
	private final static String NS = "http://aplana.com/dbmi/exchange/model/Card";
	private Search search;
	private String filename;
	private Set referencedCards;
	private Set exportedCards;
	private DataServiceBean serviceBean;

	public Exporter() {
		search = new Search();
		search.setByAttributes(true);
		List columns = new ArrayList(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);		
		columns.add(col);
		search.setColumns(columns);
		referencedCards = new HashSet();
		exportedCards = new HashSet();
	}

	public void setServiceBean(DataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public void setTemplates(List templates) {
		search.setTemplates(templates);
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	private void fillReferencedCards(Card card) {
		Iterator i = card.getAttributes().iterator();
		while (i.hasNext()) {
			AttributeBlock block = (AttributeBlock)i.next();
			Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				Attribute attr = (Attribute)j.next();
				if (attr instanceof CardLinkAttribute) {
					CardLinkAttribute ca = (CardLinkAttribute)attr;
					if (!ca.isEmpty()) {
						ObjectIdUtils.fillObjectIdSetFromCollection(referencedCards, ca.getValues());
					}
				}
			}
		}
	}
	
	public void export() throws ParserConfigurationException, DataException, ServiceException, TransformerException, IOException {
		System.out.println("Exporting to file: " + filename);
		Collection cards = ((SearchResult)serviceBean.doAction(search)).getCards();
		System.out.println(cards.size() + " cards found");
		
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = f.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElementNS(NS, "cards");
		doc.appendChild(root);
		Iterator i = cards.iterator();
		while (i.hasNext()) {
			Card card = (Card)i.next();
			ObjectId cardId = card.getId();
			card = (Card)serviceBean.getById(cardId);
			Element cardElem = CardExchangeUtils.getCardElement(card, doc);
			root.appendChild(cardElem);
			fillReferencedCards(card);
			exportedCards.add(card.getId());
			if (exportedCards.size() % 10 == 0) {
				System.out.println(String.valueOf(exportedCards.size()) + " cards exported");
			}
		}

		System.out.println("Writing result to file " + filename);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename, false);
			System.out.println("File opened for writing");		
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(doc);
	        Result result =  new StreamResult(fos);
	        transformer.transform(source, result);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		System.out.println("File written successfully");
		
		int warningCount = 0;
		i = referencedCards.iterator();
		while (i.hasNext()) {
			ObjectId cardId = (ObjectId)i.next();
			if (!exportedCards.contains(cardId)) {
				System.out.println("WARNING: card with id = " + cardId.getId() + " is referenced in one of exported cards but was not exported itself");
				++warningCount;
			}
		}
		if (warningCount == 0) {
			System.out.println("Export finished without warnings");
		} else {
			System.out.println("Export finished with " + warningCount + " warnings");
		}
	}
}
