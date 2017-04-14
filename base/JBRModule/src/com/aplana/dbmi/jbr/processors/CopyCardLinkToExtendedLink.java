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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.GetPersonByCardId;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ��������� ��� ����������� ������ �� �������� ��������� �� ��������� (from) � TypedCardLinkAttribute
 * ��� � DatedTypedCardLinkAttribute (to)
 * ����� ���������� ������ �������� (from) �� ������� � ������������� �������� (checkPersonTo - Person or CardLink) 
 * - ������� � �������� ������� ������ �� ��������, ������� ������� ������ 
 * @author ppolushkin
 */
public class CopyCardLinkToExtendedLink extends CopyCardLink {

	private static final long serialVersionUID = 1L;
	
	/**
	 * ������� � �������� �������� �� �������� ����� �������� ������ ��� ��������� � �������� ������� (to)
	 */
	private static final String PARAM_FROM_LIST = "fromList";
	
	/**
	 * ������� � �������� �������� �� �������� ����� ���� ��� ��������� � �������� ������� (to)
	 */
	private static final String PARAM_FROM_DATE = "fromDate";
	
	/**
	 * ������� � �������� �������� ��� ������ �������
	 */
	private static final String PARAM_CHECK_PERSON_FROM = "checkPersonFrom";
	
	/**
	 * ������������ ������� ��� ������ ������
	 */
	private static final String PARAM_CHECK_PERSON_TO = "checkPersonTo";
	
	/**
	 * ������� �������� ������������ ��� ������ �������
	 */
	private static final String PARAM_CHECK_CURR_PERSON = "checkCurrentPerson";
	
	private ObjectId fromList;
	private ObjectId fromDate;
	private ObjectId checkPersonFrom;
	private ObjectId checkPersonTo;
	private ObjectId checkCurrentPerson;
	
	
	@Override
	public Object process() throws DataException {
		
		if(fromId == null || getToIds().isEmpty()) logger.info("Mandatory parameter isn't set. Exiting.");
		
		Card card = getCard();
		
		if(!checkContidions(card)) {
			return card;
		}

		final Collection<ObjectId> fromCardsIds = traverseCardLinksChain(card.getId(), fromPath); // �������� ��
		final Collection<ObjectId> toCardsIds = traverseCardLinksChain(card.getId(), toPath); // �������� ������� ������ �� ����� ����������������
		final Collection<Card> fromCards = fetchCards(fromCardsIds, Collections.singleton(fromId), true);
		final Collection<Card> toCards = fetchCards(toCardsIds, Arrays.asList(toId, checkCurrentPerson), true);
		final Card toCard = !CollectionUtils.isEmpty(toCards) ? toCards.iterator().next() : null;
		
		if(toCard == null) {
			logger.warn("Card to copy attributes not found. Exit.");
			return getResult();
		}
		
		// �������� ��������� �������� � �������
		List<ObjectId> fromCardsIdFinal = new ArrayList<ObjectId>();
		CardLinkAttribute cardLinkAttr;
		for(Card cc : fromCards) {
			cardLinkAttr = cc.getAttributeById(fromId);
			fromCardsIdFinal.addAll(cardLinkAttr.getIdsLinked());
		}
		Collection<Card> fromCardsFinal = fetchCards(fromCardsIdFinal, Arrays.asList(new ObjectId[] {fromList, fromDate, checkPersonFrom}), true);

		
		DatedTypedCardLinkAttribute copyAttr;
		try {
			copyAttr = (DatedTypedCardLinkAttribute) getToIds().iterator().next().getType().newInstance();
		} catch(Exception e) {
			throw new DataException(e);
		}
		
		for(Card c: fromCards) {
			addFromValuesToAttribute(copyAttr, c, toCard, fromCardsFinal);
		}
		
		List<ObjectId> copyAttrIds = new ArrayList<ObjectId>(copyAttr.getIdsLinked());
		//copyAttr.clear();
		
		TypedCardLinkAttribute saveAttr;
		saveAttr = toCard.getAttributeById(toId);
		saveAttr.clear();
		for(ObjectId id : copyAttr.getIdsLinked()) {
			saveAttr.addLinkedId(id);
			saveAttr.addType((Long) id.getId(), (Long) copyAttr.getType((Long) id.getId()));
			((DatedTypedCardLinkAttribute) saveAttr).addDate((Long) id.getId(), copyAttr.getDate((Long) id.getId()));
		}
		
		List<Person> personAttrTo = null;
		final Attribute checkPersonToAttr = card.getAttributeById(checkPersonTo);
		if(checkPersonToAttr == null) {
			saveAttrs(toCard, fromCardsIds, toCardsIds);
			if (getResult() instanceof Card) {
				return card;
			}
			return getResult();
		}
		if(checkPersonToAttr instanceof TypedCardLinkAttribute) {
			GetPersonByCardId action = new GetPersonByCardId();
			action.setIds(((TypedCardLinkAttribute) checkPersonToAttr).getIdsLinked());
			final ActionQueryBase query = getQueryFactory().getActionQuery(action);
			query.setAction(action);
			personAttrTo = getDatabase().executeQuery(getSystemUser(), query);
		}
		
		for(Card c: fromCards) {
			CardLinkAttribute srcAttr = (CardLinkAttribute) c.getAttributeById(fromId);
			for(ObjectId id : srcAttr.getIdsLinked()) {
				if(copyAttrIds.contains(id))
					continue;
				final Card checkCard = getCardFromCollById(fromCardsFinal, id);
				final PersonAttribute checkPersonFromAttr = checkCard.getAttributeById(checkPersonFrom);
				if(checkPersonToAttr instanceof PersonAttribute) {
					if(checkPersonFromAttr.getPerson() != null
							&& checkPersonFromAttr.getPerson().equals(((PersonAttribute) checkPersonToAttr).getPerson())) {
						TypedCardLinkAttribute linkAttr;
						
						linkAttr = toCard.getAttributeById(toId);
						//linkAttr.clear();
						linkAttr.addLinkedId(id);
					}
				} else if(checkPersonToAttr instanceof TypedCardLinkAttribute) {
					if(checkPersonFromAttr.getPerson() != null
							&& (personAttrTo == null || (personAttrTo != null && personAttrTo.contains(checkPersonFromAttr.getPerson())))
							&& checkCardConditions(id, fromConditions)) {
						TypedCardLinkAttribute linkAttr;
						linkAttr = toCard.getAttributeById(toId);
						//linkAttr.clear();
						linkAttr.addLinkedId(id);
						ListAttribute listAttr = checkCard.getAttributeById(fromList);
						if(listAttr != null) {
							linkAttr.addType((Long) id.getId(), (Long) (listAttr.getValue() != null ? listAttr.getValue().getId().getId() : null));
						}
						if(linkAttr instanceof DatedTypedCardLinkAttribute) {
							DateAttribute dateAttr = checkCard.getAttributeById(fromDate);
							if(dateAttr != null) {
								((DatedTypedCardLinkAttribute) linkAttr).addDate((Long) id.getId(), dateAttr.getValue());
							}
						}
					}
				} else {
					logger.error("Attribute " + checkPersonToAttr.getId().getId() + " must instanceof PersonAttribute or TypedCardLinkAttribute");
				}
			}
		}

		saveAttrs(toCard, fromCardsIds, toCardsIds);
		
		if (getResult() instanceof Card) {
			return card;
		}
		return getResult();
	}
	
	
	private Card getCardFromCollById(Collection<Card> cards, ObjectId id) {
		if(cards == null || id == null)
			return null;
		for(Card c : cards) {
			if(id.equals(c.getId())) {
				return c;
			}
		}
		return null;
	}
	
	
	private void saveAttrs(Card toCard, Collection<ObjectId> fromCardsIds, Collection<ObjectId> toCardsIds) throws DataException {
			TypedCardLinkAttribute linkAttr = toCard.getAttributeById(toId);
			doOverwriteCardAttributes(toCard.getId(), linkAttr);
			
			logger.info(
					"Joined values from attribute " + fromId.getId() + " of cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(fromCardsIds)
					+ " have been copied into attribute " + getToIds() + " of cards " + ObjectIdUtils.numericIdsToCommaDelimitedString(toCardsIds)
			);
	}
	
	
	protected void addFromValuesToAttribute(CardLinkAttribute copyAttr, Card c, Card toCard, Collection<Card> fromCardsFinal) throws DataException {
		CardLinkAttribute srcAttr = (CardLinkAttribute)c.getAttributeById(fromId);
		DateAttribute da;
		ListAttribute la;
		CardLinkAttribute curr;
		for(ObjectId id : srcAttr.getIdsLinked()) {
			if(checkCardConditions(id, fromConditions)) {
				curr = toCard.getAttributeById(checkCurrentPerson);
				if(curr == null 
						|| (curr != null && !curr.getIdsLinked().contains(id))) {
					continue;
				}
				copyAttr.addLinkedId(id);
				Card cc = getCardFromCollById(fromCardsFinal, id);
				if(cc == null)
					continue;
				da = cc.getAttributeById(fromDate);
				if(da != null && copyAttr instanceof DatedTypedCardLinkAttribute) {
					((DatedTypedCardLinkAttribute) copyAttr).addDate((Long) id.getId(), da.getValue());
				}
				la = cc.getAttributeById(fromList);
				if(da != null && copyAttr instanceof TypedCardLinkAttribute) {
					((TypedCardLinkAttribute) copyAttr)
						.addType((Long) id.getId(), 
								(Long) ((la.getValue() != null 
										&& la.getValue().getId() != null)
											? la.getValue().getId().getId()
											: null));
				}
			}
		}
	}
	
	
	@Override
	protected <T extends CardLinkAttribute> Set<T> getToAttrs(Card c) {
		return Collections.singleton(c.<T>getAttributeById(toId));
	}
	
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_FROM_LIST.equalsIgnoreCase(name.trim())) {
			fromList = AttrUtils.getAttributeId(value.trim());
			if(!ListAttribute.class.isAssignableFrom(fromList.getType())) {
				throw new IllegalArgumentException("Attribute " + name + " must have type ListAttribute");
			}
		} else if(PARAM_FROM_DATE.equalsIgnoreCase(name.trim())) {
			fromDate = AttrUtils.getAttributeId(value.trim());
			if(!DateAttribute.class.isAssignableFrom(fromDate.getType())) {
				throw new IllegalArgumentException("Attribute " + name + " must have type DateAttribute");
			}
		} else if(PARAM_CHECK_PERSON_FROM.equalsIgnoreCase(name.trim())) {
			checkPersonFrom = AttrUtils.getAttributeId(value.trim());
			if(!PersonAttribute.class.isAssignableFrom(checkPersonFrom.getType())) {
				throw new IllegalArgumentException("Attribute " + name + " must have type PersonAttribute");
			}
		} else if(PARAM_CHECK_PERSON_TO.equalsIgnoreCase(name.trim())) {
			checkPersonTo = AttrUtils.getAttributeId(value.trim());
			if(!PersonAttribute.class.isAssignableFrom(checkPersonTo.getType())
					&& !TypedCardLinkAttribute.class.isAssignableFrom(checkPersonTo.getType())) {
				throw new IllegalArgumentException("Attribute " + name + " cannot be " + checkPersonTo.getType());
			}
		} else if(PARAM_CHECK_CURR_PERSON.equalsIgnoreCase(name.trim())) {
			checkCurrentPerson = AttrUtils.getAttributeId(value.trim());
			if(!CardLinkAttribute.class.isAssignableFrom(checkCurrentPerson.getType())) {
				throw new IllegalArgumentException("Attribute " + name + " cannot be " + checkCurrentPerson.getType());
			}
		} else {
			super.setParameter(name, value);
		}
	}

}
