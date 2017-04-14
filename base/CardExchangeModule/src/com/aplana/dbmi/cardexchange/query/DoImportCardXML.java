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
package com.aplana.dbmi.cardexchange.query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hsqldb.Types;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.cardexchange.action.ImportCardXml;
import com.aplana.dbmi.cardexchange.service.CardExchangeException;
import com.aplana.dbmi.cardexchange.xml.AttributeXMLHandler;
import com.aplana.dbmi.cardexchange.xml.AttributeXMLValue;
import com.aplana.dbmi.cardexchange.xml.CardExchangeUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;

public class DoImportCardXML extends ActionQueryBase implements WriteQuery {

	public Object processQuery() throws DataException {
		ImportCardXml action = (ImportCardXml) getAction();
		System.out.println("Process query!");
		try {
			return executeInternal(action.getXmlData());
		} catch (DataException e) {
			throw e;
		} catch (Exception e) {
			System.out.println("Not a DataException caught!");
			// e.printStackTrace();
			throw new CardExchangeException("cardImport.failed",
					new Object[] { e.getMessage() }, e);
		}
	}

	private ObjectId executeInternal(InputStream xmlData) throws Exception {
		QueryFactory queryFactory = getQueryFactory();
		UserData user = getUser();
		Database database = getDatabase();

		DocumentBuilder parser = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = parser.parse(xmlData);
		Element root = doc.getDocumentElement();

		ObjectId cardId;
		try {
			cardId = new ObjectId(Card.class, Long.parseLong(root
					.getAttribute("id")));
		} catch (Exception e) {
			cardId = null;
		}
		ObjectId statusId;
		try {
			statusId = new ObjectId(CardState.class, Long.parseLong(root
					.getAttribute("status")));
		} catch (Exception e) {
			statusId = null;
		}

		Card card;
		if (cardId == null) {
			// creating new card
			logger.warn("Card id is null!");
			ObjectId templateId = new ObjectId(Template.class, Long
					.parseLong(root.getAttribute("templateId")));
			CreateCard createCard = new CreateCard();
			createCard.setTemplate(templateId);
			ActionQueryBase qCreate = queryFactory.getActionQuery(createCard);
			qCreate.setAction(createCard);
			card = (Card) database.executeQuery(user, qCreate);
		} else {
			// locking existant card
			System.out.println("Card id is not null!");
			ObjectQueryBase qFetch = queryFactory.getFetchQuery(Card.class);
			qFetch.setId(cardId);
			card = (Card) database.executeQuery(user, qFetch);
			if (card == null) {
				// TODO: throw exception
				logger.warn("Card not found");
			} else {
				logger.warn("Card found");
				LockObject lock = new LockObject(card);
				ActionQueryBase qLock = queryFactory.getActionQuery(lock);
				qLock.setAction(lock);
				database.executeQuery(user, qLock);
			}
			// ��������� �� ��� ������������ �������� ������ ������			
			statusId = null;
		}

		try {
			NodeList nList = root.getElementsByTagName("attribute");
			for (int i = 0; i < nList.getLength(); ++i) {
				processAttribute((Element) nList.item(i), card);
			}
			SaveQueryBase qSave = queryFactory.getSaveQuery(card);
			qSave.setObject(card);
			cardId = (ObjectId) database.executeQuery(user, qSave);
			if (statusId != null) {
				setStatus(card, statusId);
			}
		} finally {
			if (cardId != null) {
				UnlockObject unlock = new UnlockObject(card);
				ActionQueryBase qUnlock = queryFactory.getActionQuery(unlock);
				qUnlock.setAction(unlock);
				database.executeQuery(user, qUnlock);
			}
		}
		return cardId;
	}
	
	private void processAttribute(Element attributeNode, Card c)
			throws Exception {
		String attrCode = attributeNode.getAttribute("code");
		String attrType = attributeNode.getAttribute("type");
		AttributeXMLHandler attrXmlType = AttributeXMLHandler
				.getXmlType(attrType);
		NodeList srchList = null;

		if (attrXmlType == null) {
			logger.error("wrong attribute type: " + attrType);
			return;
		}
		ObjectId attrId = CardExchangeUtils.getObjectId(attrXmlType.getType(),
				attrCode, false);
		Attribute attr = c.getAttributeById(attrId);
		if (attr == null) {
			logger.warn("Undefined attribute: " + attrCode);
			return;
		}

		List attrValues = new ArrayList();
		srchList = attributeNode.getElementsByTagName("search");
		if (srchList.getLength() > 0) {
			// Need to search
			
			StrictSearch action = new StrictSearch();
			String searchValue = null;
			String searchType = null;
			String code = null;
			List<ObjectId> res = new ArrayList<ObjectId>();
			Card foundCard = null;

			for (int i = 0; i < srchList.getLength(); ++i) {
				Element search = (Element) srchList.item(i);
				// attrValues.add(new
				// AttributeXMLValue(value.getTextContent()));
				searchType = search.getAttribute("type");
				searchValue = search.getTextContent();
				if (searchType.equals("template")) {
					action.addTemplate((Template) DataObject
							.createFromId(new ObjectId(Template.class,
									(new Long(searchValue)).longValue())));
				} else {
					code = search.getAttribute("field");
					if (searchType.equals("string")) {
						action.addStringAttribute(new ObjectId(
								StringAttribute.class, code), searchValue);
					} else if (searchType.equals("text")) {
						action.addStringAttribute(new ObjectId(
								TextAttribute.class, code), searchValue);
					} else if (searchType.equals("integer")) {
						action.addIntegerAttribute(new ObjectId(
								IntegerAttribute.class, code), (new Integer(
								searchValue)).intValue(), (new Integer(
								searchValue)).intValue());
					}
				}
			}
			try {
				ActionQueryBase searchQuery = getQueryFactory().getActionQuery(action);
				searchQuery.setAction(action);
				res = (List) getDatabase().executeQuery(getSystemUser(), searchQuery);
				if (res.size() > 0) {
					System.out.println("Found: " + res.size());
					Long cardId = null;
					String cardIdStr = null;
					for (ObjectId id : res) {
						System.out.println(id);
						ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
						cardQuery.setId(id);
						foundCard = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
						cardId = ((Long) id.getId());
						cardIdStr = cardId.toString();
						attrValues.add(new AttributeXMLValue(cardIdStr));
					}
				} else {
					logger.info("No results found!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			// Strict values
			NodeList values = attributeNode.getElementsByTagName("value");
			for (int i = 0; i < values.getLength(); ++i) {
				Element value = (Element) values.item(i);
				attrValues.add(new AttributeXMLValue(value.getTextContent()));
			}
		}

		if (!attr.isMultiValued() && attrValues.size() > 1) {
			logger.warn("Multiple values found for single-valued attribute '"
					+ attrCode + "'. Only first value will be used.");
		}

		attrXmlType.setValues(attrValues, attr);
	}

	private void setStatus(final Card card, final ObjectId statusId){
		logger.info("Change status of the card "+card.getId().getId());
		getJdbcTemplate().update("UPDATE card SET status_id=? WHERE card_id=?", 
				new Object[]{statusId.getId(), card.getId().getId()}, 
				new int[]{Types.NUMERIC, Types.NUMERIC});
	}

	private boolean needSearch(Element attrElement) {
		boolean retVal = false;
		NodeList nList = null;

		try {
			nList = attrElement.getElementsByTagName("search");
			if (nList.getLength() > 0) {
				retVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}
}
