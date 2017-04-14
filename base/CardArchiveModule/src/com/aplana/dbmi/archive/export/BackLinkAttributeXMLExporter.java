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
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

public class BackLinkAttributeXMLExporter extends LinkAttributeXMLExporter {
	
	public BackLinkAttributeXMLExporter(Document doc, Card card, Attribute attr) {
		super(doc, attr);
		this.card = card;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<ObjectId> getValue() {
		List<ObjectId> list = new ArrayList<ObjectId>();
		try {
			ListProject lp = new ListProject();
			lp.setCard(card.getId());
			lp.setAttribute(attr.getId());
			SearchResult result = (SearchResult) getCexp().getService().doAction(lp);
			if(result != null && result.getCards() != null) {
				final List<Card> listCards = result.getCards();
				final Set<ObjectId> listIds = ObjectIdUtils.cardsToObjectIdsSet(listCards);
				fillListCardIds(list, listIds.iterator());
			}
		} catch (DataException e) {
			logger.error("Error getting cards by ListProject for card " + card.getId() + " and attr " + attr.getId() + ". Using values from model (could be deprecated(!))");
			final Collection col = ((BackLinkAttribute) attr).getIdsLinked();
			fillListCardIds(list, col.iterator());
		} catch (Exception e) {
			logger.error(e);
		}
		return list;
	}
	
	/**
	 * ��������� ������ id ���������� ���������
	 * @param list
	 * @param it
	 */
	private void fillListCardIds(List<ObjectId> list, Iterator<ObjectId> it) {
		while (it.hasNext())
		{
			ObjectId objId = (ObjectId) it.next();
			list.add(objId);
		}
	}
	
	@Override
	protected void addLinkValue(ObjectId value, Element elem) throws DOMException, ParserConfigurationException {
		Element child = getDoc().createElement("value");
		child.setTextContent(String.valueOf(value.getId()));
		elem.appendChild(child);
	}

}
