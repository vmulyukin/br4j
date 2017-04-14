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
package com.aplana.dbmi.jbr.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.CardDoubletException;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.impl.Validator;
import com.aplana.dbmi.service.impl.query.AttributeUtils;

/**
 * A processor that checks if the set of provided attributes (defined in PARAM_ATTR_IDS parameter) are unique
 * 	within cards of provided templates (defined in PARAM_TEMPLATE_ID parameter OR current card template by default)
 * 
 */
public class CheckAttributeUniquenessProcessor extends ProcessCard implements Validator {
	private static final long serialVersionUID = 1L;
	
	private static final String PARAM_ATTR_IDS = "attrIds";
	private static final String PARAM_TEMPLATE_IDS = "templateIds";
	private static final String PARAM_STATUS_IDS = "statusIds";
	private static final String PARAM_ATTR_CODE_TYPE_DELIMITER = ":";
	
	private static final String PARAM_ERROR_MESSAGE = "error_message"; // parameter that determines the message
																	   // that will be raised if the uniqueness checking fails
	
	private List<ObjectId> testAttrIds; // attribute ids to check
	private List<ObjectId> templateIds; // template ids to filter by
	private List<ObjectId> stateIds;	// state ids to filter by
	private String errorMessage;		// checking failure message
	
	private List<Attribute> testAttrs = new ArrayList<Attribute>();

	@Override
	public Object process() throws DataException {
		if(testAttrIds != null && !testAttrIds.isEmpty()) {
			Card currentCard = getCard();
			if(currentCard != null) {
				final DataServiceBean service = new DataServiceBean();
				service.setUser(new SystemUser());
				
				final List<ObjectId> attrsToRetreive = new ArrayList<ObjectId>(testAttrIds.size()); // attributes need to be retrieved
				for(ObjectId attrId : testAttrIds) {
					Attribute attr = currentCard.getAttributeById(attrId);
					if(attr == null) {
						attrsToRetreive.add(attrId);
					} else testAttrs.add(attr);
				}
				if(!attrsToRetreive.isEmpty()
						&& currentCard.getId() != null
						&& currentCard.getId().getId() != null) {
						
					loadAttributes(currentCard, attrsToRetreive, service);
				}
				
				final Card card = currentCard;
				final Search search = new Search();
				// if no templates were provided, perform search within cards of the current card template
				final List<ObjectId> templates = templateIds != null ? templateIds : Collections.singletonList(card.getTemplate());
				
				if(stateIds != null)
					search.setStates(stateIds);
				
				search.setTemplates(templates);
				search.setByAttributes(true);
				search.addAttributes(testAttrs);
				
				// retrieve attributes to be compared
				search.setColumns(new ArrayList<SearchResult.Column>(){{
					for(final ObjectId attrId : testAttrIds) {
						add(new SearchResult.Column(){{
							setAttributeId(attrId);
						}});
					}
				}});
				
				try {
					SearchResult result = service.doAction(search);
					final List<Card> foundCards = result.getCards();
					
					if(foundCards != null && !foundCards.isEmpty()) {
						
						if(currentCard.getId() == null) { // the card creation stage
							checkUniqueness(foundCards, testAttrs);
						} else { // the card is already created and stored in database (closing card edit mode)
							for(Card c : foundCards) {
								if(c != null
										&& c.getId() != null
										&& !c.getId().equals(currentCard.getId())) // a different card found with the same attribute values
									checkUniqueness(foundCards, testAttrs);
							}
						}
					}
				} catch(ServiceException e) {
					throw new DataException(e);
				}
			}
		}
		return null;
	}
	
	/**
	 * iterates over found cards list and checks attribute values uniqueness
	 * 
	 * @param foundCards found cards by provided attributes
	 * @param attrs attributes to check
	 * @throws CardDoubletException
	 */
	private void checkUniqueness(List<Card> foundCards, List<Attribute> attrs) throws CardDoubletException {
		//��������� �������� ���������, ��� ������� �� ������ �������� �� ������������ (������ ��� Debug ������ ������������)
		ArrayList<String> attrValues = logger.isDebugEnabled() ? new ArrayList<String>() : null;
		for(Attribute attr : attrs) {
			for(Iterator<Card> itr = foundCards.iterator(); itr.hasNext();) {
				Card c = itr.next();
				Attribute a = c.getAttributeById(attr.getId());
				/*
				  if the current card attribute value is null and found card's attribute value is not null 
				  OR
				  if the current card attribute value is not null and found card's attribute value is null
				  	- removing card from found cards list
				*/
				if((AttributeUtils.isEmpty(attr) && !AttributeUtils.isEmpty(a)) 
						||	(!AttributeUtils.isEmpty(attr) && AttributeUtils.isEmpty(a))) {
					itr.remove();
				} else {
					if (logger.isDebugEnabled()) {
						attrValues.add(a.getStringValue());
					}
				}
			}
		}
		if(!foundCards.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug(errorMessage + " :" + attrValues.toString());
			}
			throw new CardDoubletException(errorMessage);
		}
	}
	
	/**
	 * loads card attributes
	 * @param card
	 * @param attrIds attributes need to be loaded
	 * @param service
	 * @throws DataException
	 */
	private void loadAttributes(Card card, List<ObjectId> attrIds, DataServiceBean service) throws DataException {
		final Search search = new Search();
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		for(ObjectId attrId : attrIds) {
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(attrId);
			columns.add(col);
		}
		search.setColumns(columns);
		search.setByCode(true);
		String cardId = card.getId().getId().toString();
		search.setWords(cardId);
		try {
			SearchResult result = service.doAction(search);
			if(result.getCards().size() > 1) {
				String msg = "More than one card has been found with id: "+cardId;
				logger.error(msg);
				throw new DataException(msg);
			}
			card = result.getCards().get(0);
			
			for(ObjectId attrId : attrIds) {
				Attribute attr = card.getAttributeById(attrId);
				if(attr != null)
					testAttrs.add(attr);
			}
		} catch(ServiceException e) {
			throw new DataException(e);
		}
	}
	
	private void resolveAttrIds(String ids) {
		testAttrIds = new ArrayList<ObjectId>();
		String[] idsArr = ids.split(",");
		for(String idStr : idsArr)
			testAttrIds.add(ObjectIdUtils.getAttrObjectId(idStr.trim(), PARAM_ATTR_CODE_TYPE_DELIMITER));
	}
	
	private void resolveTemplateIds(String ids) {
		templateIds = new ArrayList<ObjectId>();
		String[] idsArr = ids.split(",");
		for(String idStr : idsArr)
			templateIds.add(ObjectIdUtils.getObjectId(Template.class, idStr.trim(), true));
	}
	
	private void resolveStatusIds(String ids) {
		stateIds = new ArrayList<ObjectId>();
		String[] idsArr = ids.split(",");
		for(String idStr : idsArr)
			stateIds.add(ObjectIdUtils.getObjectId(CardState.class, idStr.trim(), true));
	}
	
	@Override
	public void setParameter(String name, String value) {
		if(PARAM_ATTR_IDS.equalsIgnoreCase(name)) {
			if(value != null && !value.equals(""))
				resolveAttrIds(value);
		} else if(PARAM_TEMPLATE_IDS.equalsIgnoreCase(name)) {
			if(value != null && !value.equals(""))
				resolveTemplateIds(value);
		} else if(PARAM_ERROR_MESSAGE.equalsIgnoreCase(name)) {
			errorMessage = value;
		} else if(PARAM_STATUS_IDS.equalsIgnoreCase(name)) {
			if(value != null && !value.equals(""))
				resolveStatusIds(value);
		} else super.setParameter(name, value);
	}

}
