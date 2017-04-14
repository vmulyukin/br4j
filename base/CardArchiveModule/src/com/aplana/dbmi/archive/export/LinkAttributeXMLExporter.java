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

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public abstract class LinkAttributeXMLExporter extends AttributeXMLExporter {
	
	public LinkAttributeXMLExporter(Document doc, Attribute attr) {
		super(doc, attr);
	}

	public abstract Object getValue();
	
	@SuppressWarnings("unchecked")
	@Override
	public Object export() throws DOMException, ParserConfigurationException {
		if(attr != null && !attr.isEmpty()) {
			Element attrElem = addAttr();
			List<ObjectId> value = (List<ObjectId>) getValue();
			addNestedCards(attrElem, value);
			return attrElem;
		}
		return null;
	}
	
	protected void addNestedCards(Element elem, List<ObjectId> values) throws DOMException, ParserConfigurationException {
		Element current = addValuesWrap(elem);
		for(Iterator<ObjectId> it = values.iterator(); it.hasNext();) {
			ObjectId value = it.next();
			if(getCexp() != null && getCexp().getService() != null
					&& getCexp().getNestedCardsToExport().contains(value)
					&& !getCexp().getExportedCards().contains(value)) {
				
				getCexp().getExportedCards().add(value);
				
				try {
					Card nestedCard = (Card) getCexp().getService().getById(value);
					getCexp().export(nestedCard, doc, current);
				} catch (DataException e) {
					logger.error("Error getting card " + value.getId() + ". Export that card is impossible.");
					e.printStackTrace();
					addLinkValue(value, current);
					return;
				}
			} else {
				addLinkValue(value, current);
			}
		}
	}
	
	protected abstract void addLinkValue(ObjectId value, Element elem) throws DOMException, ParserConfigurationException;

}
