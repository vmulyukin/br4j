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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * �����, ����������� ������� ����������� ����������� �� ��������� ��������, � �������
 * ��� �������. ����� ������������ ��� ������������� � ����� <code>beans.xml</code>
 * � �������� �������� ���������, ������������ � �����
 * {@link NotificationBean#setRecipients(Collection)}.
 *
 * ������������ ��������� ������ �������� {@link #setAttribute(String)}.
 * � ���� ����� ������ ���� �������� ������, �������� ������� (��������, � ���� �������,
 * � ����� <code>objectids.properties</code>) �������������� �������� ������ �� 2 �����:
 * <ul>
 * <li> {@link PersonAttribute} &mdash; � ���� ������ ������������ ����������� ����������
 * 		��� ������������, ������������� � ������ ��������;
 * <li> {@link StringAttribute} &mdash; ���������� ����� �������� ������������ � ��������
 * 		e-mail ������ ����������. ��� ���� ������� <code>NAME</code> ���� �� ��������
 * 		��������� ������ ����������. ����� �������, ��� ������������� ��������� � ���������
 * 		��������� �������� (��. ����), ��������� ���������� ����������� ������� �����������,
 * 		������� �� �������� �������������� �������.
 * </ul>
 * ������������� ��������� �������� ����� ������������ � ���� ������ ����������������
 * ������ ��������� ����� {@link CardLinkAttribute}, {@link TypedCardLinkAttribute} ���
 * {@link BackLinkAttribute}, ���������� ���������� (:), ������� � ����� ������ ������������
 * ��� ���������� ��������� �������� - � ������� ������������. ��� ���������� �� �����-����
 * ���� ������� ���������� �������� ��� ��� ������������ ��� ���������� ���������, �.�.
 * �������� ����������� �� ����� ������ ��������.
 *
 * @author apirozhkov
 */
public class CardAttrRecipients extends DataServiceClient implements RecipientGroup
{
	private static final Class[] LINK_TYPES = new Class[]
		{ CardLinkAttribute.class, BackLinkAttribute.class, TypedCardLinkAttribute.class, DatedTypedCardLinkAttribute.class };
	private static final Class[] RECIPIENT_TYPES = new Class[]
		{ PersonAttribute.class, StringAttribute.class };

	private String attribute;
	private String ignoredStates = "";

	/**
	 * ����� ������� ��������� ���������. ��. �������� ������.
	 *
	 * @param attribute ������, ���������� �������� ���������, ���������� ����������� (:)
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public void setIgnoredStates(String ignoredStates) {
		this.ignoredStates = ignoredStates;
	}

	public Collection discloseRecipients(NotificationObject object) {
		if (!SingleCardNotification.class.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("This recipient group can only be used for card notifications");
		
		final ObjectId cardId = ((SingleCardNotification) object).getCard().getId();
		HashMap cards = new HashMap();
		loadCard(cards, cardId);
		final List<ObjectId> ignoredStatesList = new ArrayList<ObjectId>();
		if(!ignoredStates.isEmpty()){
			for(String stStatus: ignoredStates.split(",")){
				ignoredStatesList.add(ObjectId.predefined(CardState.class, stStatus));
			}
		}
		final String[] part = attribute.split(":");
		for (int i = 0; i < part.length - 1; i++) {
			final HashMap levelCards = new HashMap();
			for (Iterator itr = cards.values().iterator(); itr.hasNext(); ) {
				final Card card = (Card) itr.next();
				itr.remove();
				if(ignoredStatesList.contains(card.getState())){
					continue;
				}
				/*ObjectId attrId = ObjectId.predefined(CardLinkAttribute.class, part[i]);
				if (attrId == null)
					attrId = ObjectId.predefined(BackLinkAttribute.class, part[i]);
				if (attrId == null)
					attrId = new ObjectId(CardLinkAttribute.class, part[i]);
				Attribute attr = card.getAttributeById(attrId);
				if (attr == null)
					attr = card.getAttributeById(new ObjectId(BackLinkAttribute.class, part[i]));*/
				Attribute attr = anyAttribute(card, part[i], LINK_TYPES);
				if (attr == null) {
					logger.warn("Attribute " + part[i] + " not found in card " +
							card.getId().getId() + "; skipped");
					continue;
				}

				if (Attribute.TYPE_CARD_LINK.equals(attr.getType()) ||
						Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType())) {
					// (2010/02, RuSA)
					final Collection /*<Card>*/ idsLinks = ((CardLinkAttribute) attr).getIdsLinked();
					if (idsLinks != null) {
						for(Iterator /*ObjectId*/ iterId = idsLinks.iterator(); iterId.hasNext(); ) {
							loadCard(levelCards, (ObjectId) iterId.next());
						}
					}
				} else /*if (Attribute.TYPE_BACK_LINK.equals(attr.getType()))*/
				{
					final ListProject list = new ListProject();
					list.setAttribute(attr.getId());
					list.setCard(card.getId());
					try {
						final ActionQueryBase linkQuery = getQueryFactory().getActionQuery(ListProject.class);
						linkQuery.setAccessChecker(null);
						linkQuery.setAction(list);
						final SearchResult links = (SearchResult) getDatabase().executeQuery(getSystemUser(), linkQuery);
						if (links != null && links.getCards() != null) {
							for( Iterator /*<Card>*/ linkItr = links.getCards().iterator();
								linkItr.hasNext();) {
								final Card link = (Card) linkItr.next();
								loadCard(levelCards, link.getId());
							}
						}

					} catch (DataException e) {
						logger.warn("Error fetching cards linked to card " + card.getId().getId() +
								" (attribute " + part[i] + "); skipped");
						continue;
					}
				}
			}
			cards = levelCards;
		}

		final HashMap recipients = new HashMap();
		for (Iterator itr = cards.values().iterator(); itr.hasNext(); ) {
			Card card = (Card) itr.next();
			/*ObjectId attrId = ObjectId.predefined(PersonAttribute.class, part[part.length - 1]);
			if (attrId == null)
				attrId = new ObjectId(PersonAttribute.class, part[part.length - 1]);*/
			//PersonAttribute attr = (PersonAttribute) card.getAttributeById(attrId);
			Attribute attr = anyAttribute(card, part[part.length - 1], RECIPIENT_TYPES);
			if (attr == null) {
				logger.warn("Attribute " + part[part.length - 1] + " not found in card " +
						card.getId().getId() + "; skipped");
				continue;
			}
			if (Attribute.TYPE_PERSON.equals(attr.getType())) {
				PersonAttribute pAttr = (PersonAttribute) attr;
				if (pAttr.getValues() == null)
					continue;
				for (Iterator valItr = pAttr.getValues().iterator(); valItr.hasNext(); )
				{
					Person person = (Person) valItr.next();
					//***** Does person already contain email?
					recipients.put(person.getId(), person/*.getEmail()*/);
				}
			} else /*if (Attribute.TYPE_STRING.equals(attr.getType()))*/ {
				StringAttribute sAttr = (StringAttribute) attr;
				if (sAttr.getValue() == null || sAttr.getValue().length() == 0)
					continue;
				Person person = new Person();
				person.setEmail(sAttr.getValue());
				person.setFullName(card.getAttributeById(Attribute.ID_NAME).getStringValue());	//*****
				person.setCardId(card.getId());
				recipients.put(card.getId(), person);
			}
		}
		return recipients.values();
	}

	private void loadCard(HashMap map, ObjectId cardId)
	{
		if (map.containsKey(cardId))
			return;

		try {
			final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			cardQuery.setAccessChecker(null);
			cardQuery.setId(cardId);
			final Card card = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
			map.put(cardId, card);
		} catch (DataException e) {
			logger.warn("Error fetching card " + cardId.getId() + "; skipped", e);
		}
	}

	private Attribute anyAttribute(Card card, String id, Class[] types) {
		for (int i = 0; i < types.length; i++) {
			ObjectId attrId = ObjectId.predefined(types[i], id);
			if (attrId != null)
				return card.getAttributeById(attrId);
		}
		for (int i = 0; i < types.length; i++) {
			ObjectId attrId = new ObjectId(types[i], id);
			Attribute attr = card.getAttributeById(attrId);
			if (attr != null)
				return attr;
		}
		return null;
	}
}
