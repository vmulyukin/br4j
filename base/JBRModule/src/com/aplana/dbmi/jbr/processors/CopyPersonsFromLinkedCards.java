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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.StrUtils;

@SuppressWarnings("unchecked")
public class CopyPersonsFromLinkedCards
	extends AbstractCopyPersonProcessor
{
	private static final long serialVersionUID = 1L;

	private ObjectId personAttrId;

	private List<ObjectId> addPersonAttrIds; // PersonAttribute Ids
	private List<ObjectId> addCardPersonAttrIds; // CardLinkAttribute Ids

	private List<ObjectId> linkedPersonAttrIds;
	private List<ObjectId> linkedCardPersonAttrIds;
	private ObjectId linkedCardsStateId;

	private boolean append = false;

	/**
	 * recurseAttrId: ���� �� null, �� ��� �������� �� ���������� ��������-������, 
	 * �������� ����� ����������� ����������;
	 * ��� ����, ����������� ��������� ��������� �������� ������� �� 
	 * recurseReversed: false=�� �������� � �����, true=�� ����� � ���������.
	 */
	private ObjectId recurseAttrId = null; // default class: CardLinkAttribute.class
	private boolean recurseReversed = false;

	@Override
	public Object process() throws DataException 
	{
		if (personAttrId == null) 
			return null;

		final Card curCard = getCard( personAttrId);
		// final ObjectId cardId = (card != null) ? card.getId() : this.getCardId();

		final Set<ObjectId> processedCards = new HashSet<ObjectId>();
		processCard( curCard, processedCards);

		if (logger.isDebugEnabled()) {
			logProcessed(processedCards);
		}

		return null;
	}

	/**
	 * @param processedCards
	 */
	private void logProcessed(Set<ObjectId> processedCards) {
		final StringBuffer buf = new StringBuffer();
		buf.append( "Processed cards counter is: ")
			.append(processedCards.size());
		if (!processedCards.isEmpty()) {
			buf.append("\n");
			for (Iterator<ObjectId> i = processedCards.iterator(); i.hasNext();) {
				final ObjectId id = i.next();
				buf.append((id != null) ? id.getId() : null );
				if (i.hasNext()) buf.append(',');
			}
		}
		logger.debug( buf.toString());
	}

	/**
	 * ��������� ��������� �������� curCard.
	 * @param curCard: �������� ��� ���������.
	 * @param setProcessed: ������ ��� ������������ ��������, ���� �������
	 * �������� � ��� ����, �� ��������� �� ����������� (������ �� ����������� 
	 * ������).
	 * (!) ��� �������� ������������ this.isRecursive: true, ���� ���� ��������� 
	 * ����������� ��������� ��� ��������� �������� ����� ��������� �������.
	 * @throws DataException 
	 */
	private void processCard( 
				Card card, 
				Set<ObjectId> setProcessed 
			) throws DataException
	{
		if (card == null)
			return;

		final ObjectId cardId = card.getId();
		if ( cardId == null || setProcessed.contains(cardId))
			return;

		// ������ � ������ ������������...
		setProcessed.add(cardId);

		// ����� ������� ��������� � �������� �������� ...
		/*
		if (!append)
		CardUtils.dropAttributes( getJdbcTemplate(), 
				new Object[] { 
						personAttrId.getId(), 
						Attribute.ID_CHANGE_DATE.getId() 
				}, cardId );
		*/
		// ������� �������...
		markChangeDate( cardId, true);
		updateDateAttrInCard(card, Attribute.ID_CHANGE_DATE);

		// ����������� ����� ��������� � ���� ����� fetchAttrIds
		// � ���� ������ ������������ ��� ������� �� ���� ���������,
		// ���� ������� ������� � ������ personAttrIds,
		// ����������� � ���� ������ � ������������� � ������� ��������(������������)
		// � ������� linkedPersonAttrId.
		final Set<ObjectId> fetchAttrIds = new HashSet<ObjectId>();
		if (linkedPersonAttrIds != null)
			fetchAttrIds.addAll(linkedPersonAttrIds);
		if (linkedCardPersonAttrIds != null)
			fetchAttrIds.addAll(linkedCardPersonAttrIds);

		// ��������� ��������� � ��������� ������ ��������� ...
		// � ������� �������� �������� ������ ��������� �������� �� ���� �������� (linkAttrId) � ������ ����������� (linkReversed)
		// ���� linkReversed = false - � linkedCards ����� ��������� �������� ��������, true - ������������ 
		// ������� linkAttrId ������ ���� � � ������� �������� � � ������������!!!
		final List<Card> linkedCards = (getLinkAttrId() == null) 
						? null 
						: getLinkedCards(cardId, getLinkAttrId(),
								isLinkReversed(), //false - ��������, true - ������������
						  fetchAttrIds);
		// ������ � ��� � linkedCards ��������� ��� ��������� �������� � ������ �����������
		// ���� ����� cardStateId, �� ����������� ������ �������� � ���� ������� - ����� ��� cardStateId!!!

		// isLinkReversed(): false - ��������, 
		// true - ������������, �������� ���������� ���������� CopyPersonsToLinkedCards
		final String linkedInfo = (isLinkReversed()) ? "parent" : "child";
		if (linkedCards == null || linkedCards.isEmpty())
			logger.debug("No linked "+ linkedInfo+ " card(s) found via link " + getLinkAttrId());
		else
			logger.debug(linkedCards.size() + " linked "+ linkedInfo+ " card(s) found via link "+ getLinkAttrId());

		// ��������� �� ���� ��������� (������������ ��� ��������) ������������� ������...
		// ������ �������� ����� personIds, cardIds
		final Set<ObjectId> personIds = new HashSet<ObjectId>();
		final Set<ObjectId> cardIds = new HashSet<ObjectId>();

		// ���������� ������ ������ �� ������� ��������...
		addCurrentPersons(card, personIds, cardIds);

		// ���������� ������ �� ��������� �������� ...
		// ��� ������ ������� �������� ������
		if (linkedCards != null) {
			for( final Card linkedCard : linkedCards) 
			{
				logger.trace("Processing linked card " + linkedCard.getId().getId());
				if (	linkedCardsStateId == null 
						|| linkedCardsStateId.equals(linkedCard.getState()) ) 
				{
					addPersonsFromPersonAttributes( personIds, linkedCard, linkedPersonAttrIds);
					addCardsFromCardlinkAttributes( cardIds, linkedCard, linkedCardPersonAttrIds);
				}
			}
		}

		// ������� id ������������ �������� � ���� ������...
		final Set<Person> personsFromCards = super.getPersonsByCards(cardIds);

		// // ���������� ����� ������ (�� �������� ������)...
		ObjectIdUtils.fillObjectIdSetFromCollection(personIds, personsFromCards);

		// ���������� � �������� ������� ��������...
		insertCardPersonAttributeValues(cardId, personAttrId, personIds, !append);

		// ��������� �������� ����� �������� ��������...
		// ���� ��������� ������� recurseAttrId,
		// �������� ����������� ���������� ��� ���� ��������� �������� �� ������ recurseAttrId
		if (recurseAttrId != null) {

			// �������� ��������� �������� (���������� �� �������� ������)...
			final List<ObjectId> nextLevelAttrIds = new ArrayList<ObjectId>(20);
			if (this.addPersonAttrIds != null)
				nextLevelAttrIds.addAll(this.addPersonAttrIds);
			if (this.addCardPersonAttrIds != null)
				nextLevelAttrIds.addAll(this.addCardPersonAttrIds);

			// ������ �������� ���������� ������...
			final List<Card> nextLevelList = getLinkedCards(cardId, 
						recurseAttrId, recurseReversed, nextLevelAttrIds);
			// ����� �������: 	���� recurseReversed=false, �� ���������� ��� �����,
			// �����?			���� recurseReversed=true, �� ���������� ��� ���������

			logger.debug( "Recursing card " + cardId.getId() + " via attribute " 
					+ recurseAttrId + "(reverse:"+ recurseReversed +"): "
					+ " found " + (nextLevelList == null ? 0 : nextLevelList.size()) + " card(s)"
					);
			// ������ ���� �� ������� � ������ ��� � �����
			if (nextLevelList != null && !nextLevelList.isEmpty()) {
				for (Card itemCard: nextLevelList) {
					if (itemCard == null) continue; 
					try {
						processCard( itemCard, setProcessed);
					} catch (Throwable ex) {
						logger.error(
								"error recursevely processing card " + cardId.getId()
								+ " at item " + itemCard.getId()
								+ ": \n\t" + ex.getMessage()
						);
						throw new DataException(ex);
					}
				}
			}
		} 
	}

	/**
	 * �������� � destPeronsIds ���� ������ �� ���������, ������������� � ������.
	 * @param destPersonIds
	 * @param card
	 * @param listPersonAttrIds
	 */
	private void addPersonsFromPersonAttributes( Set<ObjectId> destPersonIds,
			final Card card, final Collection<ObjectId> listPersonAttrIds) {
		if (destPersonIds != null && listPersonAttrIds != null && card != null) 
		{
			for( ObjectId itemPersonAttrId: listPersonAttrIds) {
				final PersonAttribute attr = (PersonAttribute)
							card.getAttributeById(itemPersonAttrId);
				if (attr != null)
					ObjectIdUtils.fillObjectIdSetFromCollection (destPersonIds, attr.getValues());
			}
		}
	}

	/**
	 * �������� � destCardIds ��� �������� �� ���������, ������������� � ������.
	 * @param destCardIds
	 * @param card
	 * @param listCardttrIds
	 */
	private void addCardsFromCardlinkAttributes(final Set<ObjectId> destCardIds,
			final Card card, final Collection<ObjectId> listCardlinkAttrIds) 
	{
		if (destCardIds != null && listCardlinkAttrIds != null && card != null) {
			for( ObjectId itemCardAttrId: listCardlinkAttrIds) 
			{
				final CardLinkAttribute attr = 
							card.getCardLinkAttributeById(itemCardAttrId);
				if (attr != null && attr.getIdsLinked() != null)
					destCardIds.addAll(attr.getIdsLinked());
			}
		}
	}

	/**
	 * �������� ������� �� ��������� ������� ��������, ��������� � this.addPersonAttrIds
	 * � � this.addCardPersonAttrIds.
	 * @param destPersonIds: ������, � ������� ���� �������� ������ �� ��������� 
	 * ������� �������� �� ������ this.addPersonAttrIds.
	 * @param destCardId: ������ ��������� ������������� ��������, ��� ���������� 
	 * �� ������ this.addCardPersonAttrIds.
	 */
	private boolean addCurrentPersons(Card curCard, Set<ObjectId> destPersonIds, 
			Set<ObjectId> destCardIds) 
	{
		final boolean hasAddPersons = (addPersonAttrIds != null && !addPersonAttrIds.isEmpty()); 
		final boolean hasAddCards = (addCardPersonAttrIds != null && !addCardPersonAttrIds.isEmpty());
		final boolean needProcess = (hasAddPersons || hasAddCards);
		if (needProcess) {
			addPersonsFromPersonAttributes( destPersonIds, curCard, addPersonAttrIds);
			addCardsFromCardlinkAttributes( destCardIds, curCard, addCardPersonAttrIds);
		}
		return needProcess;
	}

	public Card getCard(ObjectId chkAttrId) 
	{
		Card acard = (Card) getObject();
		if (acard == null)
			acard = (Card) getResult();

		if (acard == null 
				|| (chkAttrId != null && (acard.getAttributeById(chkAttrId) == null) ))
		{	// try to find more by Id...
			final ObjectId id = this.getCardId();
			if (id != null) acard = loadCard( id);
		}
		return acard;
	}

	public Card loadCard(ObjectId cardId)
	{
		if (cardId == null)
			return null;

		try {
			final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
			cardQuery.setAccessChecker(null);
			cardQuery.setId(cardId);
			final Card card = (Card) getDatabase().executeQuery( getSystemUser(), cardQuery);
			return card;
		} catch (DataException e) {
			// logger.warn("Error fetching card " + cardId.getId() + "; skipped", e);
			logger.error( MessageFormat.format( "Error fetching card object with id=''{0}''",
					new Object[]{ cardId }), e);
			return null;
		}
	}

	@Override
	public void setParameter(String name, String value) {
		if ("personAttrId".equalsIgnoreCase(name)) {
			this.personAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if ("addPersonAttrIds".equalsIgnoreCase(name)) {
			this.addPersonAttrIds = stringToAttrIds(PersonAttribute.class, value);
		} else if ("addCardPersonAttrIds".equalsIgnoreCase(name)) {
			this.addCardPersonAttrIds = stringToAttrIds(CardLinkAttribute.class, value);
		} else if ("linkedPersonAttrIds".equalsIgnoreCase(name)) {
			this.linkedPersonAttrIds = stringToAttrIds(PersonAttribute.class, value);
		} else if ("linkedCardPersonAttrIds".equalsIgnoreCase(name)) {
			this.linkedCardPersonAttrIds = stringToAttrIds(CardLinkAttribute.class, value);
		} else if ("linkedCardsStateId".equalsIgnoreCase(name)) {
			this.linkedCardsStateId = ObjectIdUtils.getObjectId(CardState.class, value, true);
		} else if ("writeOperation".equalsIgnoreCase(name)) {
			if ("append".equalsIgnoreCase(value))
				append = true;
		} else if ("recurseReversed".equalsIgnoreCase(name))  {
			this.recurseReversed = StrUtils.stringToBool( value, false);
		} else if ("recurseAttrId".equals(name)) {
			this.recurseAttrId = IdUtils.tryFindPredefinedObjectId(value, CardLinkAttribute.class, false);
		} else {
			super.setParameter(name, value);
		}
	}
}
