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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class CopyPersonsToLinkedCards 
	extends AbstractCopyPersonProcessor 
{
	private static final long serialVersionUID = 1L;

	private List<ObjectId> personAttrIds;
	private ObjectId linkedPersonAttrId;

	// (� 2012/12/20) �������� � ����������� (��. personCards � ����)
	
	// (c 2010/10/20) ������ ���������� ��������� ������ ������
	// ������ ������ = ����� ��������� ��������
	// private ObjectId cardStateId;
	private final Set<ObjectId> cardStateIds = new HashSet<ObjectId>(3);

	/* ����� ���������� ������ � linkedPersonAttrId:
	 * 		true: (��-����) ������� ������ �������������� ���������,
	 * 		false: ������� ������ "������������" ����������� �����.
	 */
	private boolean preclearLinkedPersonAttr = true;

	// static final String MSG_CARD_0_HAS_NO_ATTRIBUTE_1 = "Card {0} DOES NOT CONTAIN attribute ''{1}''";

	@Override
	public Object process() throws DataException 
	{
		final Set<ObjectId> linkedDestCardIds = getLinkedCardIds();
		if (linkedDestCardIds == null || linkedDestCardIds.isEmpty()) {
			logger.debug("No linked cards found. Exit.");
			return null;
		}

		if (logger.isDebugEnabled()) {
			final String stLinkedCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(linkedDestCardIds); 
			logger.debug( "found " + linkedDestCardIds.size() + " linked cards with id's: " + stLinkedCardIds);
		}

		// ���� ���� ��������� ��� ��������� ������ ������ ...
		Map<ObjectId, Card> destCards = (preclearLinkedPersonAttr) 
						? null
						: loadCardsByIds(linkedDestCardIds, linkedPersonAttrId)
				;

		/*
		CardUtils.dropAttributes( getJdbcTemplate(), 
				new Object[] { 	linkedPersonAttrId.getId(), 
								Attribute.ID_CHANGE_DATE.getId() 
					},
				linkedDestCardIds );
		 */

		for( ObjectId linkedCardId: linkedDestCardIds ) 
		{
			logger.debug("Processing linked card " + linkedCardId.getId());

			/* ��������� �������� ����� ���� ����� ������� � ������� ���������� 
			 * ����� �������, ������� ��� ������ ��������� �������� ���������� 
			 * ������ ��������, � �������� ��� ������� ����� �������������� 
			 * linkAttrId (�� ��� � �������� �����������) � �������� ������ 
			 * ������ �� ���.
			 */
			
				// ��������� ���� ��������� �������� - (!) ���� ������ ����� � �������� ����.
				// (���� �� ������� � �������� �����������)
			final List<Card> brotherCards = getLinkedCards( linkedCardId, 
						getLinkAttrId(), !isLinkReversed(), personAttrIds);
				
			// �������� ������������� ������ �� ���� Person-��������� ��������...
			//
			final Set<ObjectId> personIds = new HashSet<ObjectId>();
			final Set<ObjectId> personCards = new HashSet<ObjectId>();
			// ��������� ��������� ������ � ������������� ������ ...
			if (!preclearLinkedPersonAttr) {
				includePredList( personIds, linkedCardId, linkedPersonAttrId, destCards);
			}
			for( Card cardBrother: brotherCards) 
			{
				if (	cardStateIds == null || cardStateIds.isEmpty()		// ������ ������ = ����� ��������� �������� 
						|| cardStateIds.contains(cardBrother.getState())
					) 
				{
					// ����������� ���� ��������� � ������ �� personAttrIds... 
					for( final ObjectId personAttrId: personAttrIds) 
					{
						final Attribute attr = cardBrother.getAttributeById(personAttrId);
						if (attr != null) {
							if (attr instanceof PersonAttribute)
								ObjectIdUtils.fillObjectIdSetFromCollection(personIds, ((PersonAttribute)attr).getValues());
							else if (attr instanceof CardLinkAttribute)
								personCards.addAll(((CardLinkAttribute)attr).getIdsLinked());
						}
						else
							logger.warn( MessageFormat.format( 
									MSG_CARD_0_HAS_NO_ATTRIBUTE_1, 
									cardBrother.getId(), personAttrId ));
					}
					// ���������� �������� id ������������ �������� � ���� ������...
					ObjectIdUtils.fillObjectIdSetFromCollection(personIds, super.getPersonsByCards(personCards));
				}
			}

			// ���������� ��������-������ ������ ������� root-��������
			insertCardPersonAttributeValues(linkedCardId, linkedPersonAttrId, personIds, true);
		} // for iterRoor: iterator<ObjectId>

		// ������� �������...
		markChangeDate(linkedDestCardIds);

		return null;
	}

	/**
	 * ��������� ������ �������� �� �� id.
	 * @param cardIds
	 * @return
	 * @throws DataException 
	 */
	private Map<ObjectId, Card> loadCardsByIds(Set<ObjectId> cardIds, ObjectId attrId) throws DataException
	{
		ArrayList attrIds = new ArrayList<ObjectId>();
		attrIds.add(attrId);
		return loadCardsByIds(cardIds, attrIds);
	}

	
	/**
	 * ��������� ������ �������� �� �� id.
	 * @param cardIds
	 * @return
	 * @throws DataException 
	 */
	@SuppressWarnings("unchecked")
	private Map<ObjectId, Card> loadCardsByIds(Set<ObjectId> cardIds, List<ObjectId> attrIds) throws DataException 
	{
		logger.debug( "Fetching linked list "+ cardIds + ", attribute '"+ attrIds.size() + "'");

		final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();
		for (ObjectId attrId : attrIds) {
			cols.add( CardUtils.createColumn(attrId));			
		}
		cols.add( CardUtils.createColumn(Card.ATTR_STATE) );
		cols.add( CardUtils.createColumn(Card.ATTR_TEMPLATE) );

		final Search search = new Search();
		search.setByCode(true);
		search.setWords( IdUtils.makeIdCodesEnum(cardIds, ","));
		search.setColumns(cols);

		final List<Card> found = CardUtils.getCardsList( (SearchResult) doAction(search));
		if (found == null || found.isEmpty())
			return null;

		final Map<ObjectId, Card> result = new HashMap<ObjectId, Card>(found.size());
		ObjectIdUtils.fillObjectIdMapFromCollection(result, found);
		return result;
	}

	private Object doAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}

	/**
	 * ������ � ������� ������ ������ id �� ���������� �������� ������ ��������.
	 * @param destPersonIds: ������� ������
	 * @param srcCardId: id �������� �������� ��� ��������� ��������,
	 * @param srcAttrId: id �������� ��� ��������� ������,
	 * @param loadedCards: ����� ����������� ��������
	 */
	private void includePredList( Set<ObjectId> destPersonIds,
			ObjectId srcCardId, ObjectId srcAttrId,
			Map<ObjectId, Card> loadedCards) 
	{
		if (destPersonIds == null || srcCardId == null || srcAttrId == null
				|| loadedCards == null)
			return;

		final Card existCard = loadedCards.get(srcCardId);
		if (existCard == null) {
			logger.warn( "Card was not loaded: id=" + srcCardId.getId() );
			return;
		}
		final PersonAttribute attr = (PersonAttribute) existCard.getAttributeById(srcAttrId);
		if (attr == null){
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, srcCardId.getId(), srcAttrId ));
			return;
		}
		ObjectIdUtils.fillObjectIdSetFromCollection( destPersonIds, attr.getValues());
	}

	/**
	 * @return
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Set<ObjectId> getLinkedCardIds() throws DataException {
		Set<ObjectId> result = null;
		if (getCardId() != null){
			result = ObjectIdUtils.collectionToSetOfIds(
						getLinkedCards(getCardId(), getLinkAttrId(), isLinkReversed())
					);
		}
		return result;
	}

	@Override
	public void setParameter(String name, String value) {

		if ("personAttrIds".equalsIgnoreCase(name)) {
			personAttrIds = stringToAttrIds(PersonAttribute.class, value);
		} 

		else if ("linkedPersonAttrId".equalsIgnoreCase(name)) {
			this.linkedPersonAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} 

		else if ("cardStateId".equalsIgnoreCase(name)) {
			final ObjectId id = ObjectIdUtils.getObjectId(CardState.class, value, true);
			if (id != null)
				this.cardStateIds.add( id);
		}

		else if ("cardStateIds".equalsIgnoreCase(name)) {
			final List<ObjectId> list = IdUtils.stringToAttrIds(value, CardState.class);
			if (list != null)
				this.cardStateIds.addAll( list); 
		}

		else if ("preclearLinkedPersonAttr".equalsIgnoreCase(name)) {
			if (value != null)
				preclearLinkedPersonAttr = Boolean.parseBoolean(value.trim());
		}

		else {
			super.setParameter(name, value);
		}
	}
}
