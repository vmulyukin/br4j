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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
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
import com.aplana.dbmi.service.impl.UserData;

/**
 * @comment RAbdullin
 * ��������� ���������� ������ �� ��������� �������� � ���� �� ��������� 
 * �������� �������� �� ���������� �������:
 *   1) (������� ���������� ���� ��������� � �������� Attribute.ID_CHANGE_DATE 
 * ��� ������� ��������)
 *   2) ���� �� ����� �������� "linkAttrId", �� �� �������� ������� ����������� 
 * ���� ������� ��������, ���� �����, �� �� �������� ����������� �������� �� 
 * ����� ������;
 *   3) ����������� ��� ��������� � ��� �������� � ������� linkChildAttrId � 
 * ������� ����������� �� ����� depth;
 *   4) ���� �� ����� �������� linkedCardsStateId, �� ����������� ��� ��������,
 * ����� ���������� ������ � ��������� ���� ��������� ������� (���������);
 *   5) �����, � ���������� ��������� ���������� ��� �������, ���������� � 
 * ������� � ������ �� ������������ linkedPersonAttrIds (�������� ���� 
 * "U"/PersonAttribute) � linkedCardPersonAttrIds (�������� ���� "C"/
 * CardLinkAttribute), ������� ��� cardlink-������� ����������, ��� � ��� ���� 
 * �������� ������, � �� ���� �������;
 *   6) ���������� ������������ ������ ������ ��������� � ������� destPersonAttrId 
 * ���� "U" �������� ��������.
 * 	 7)��� ������ ��������� writeOperation (�������� append, overwrite) ����� ��������� 
 * (� �� ������ ��������������) ��������� Id � ��� ������������ � ������� �������� destPersonAttrId
 * ������� ��������.
 */
// �������� linkReversed, ���������� � ����� �������� query.xml � ���������� �� ������������ (����� ���� ������ ����)
public class CopyPersonsFromDeepCards extends AbstractCopyPersonProcessor {

	private static final long serialVersionUID = 1L;

	private int depth = 10;				// ������� ����������� ������ �� ���������

	private ObjectId destPersonAttrId;	// ������� � ��������, � ������� ������ ������� ������ ������
	private ObjectId destCardAttrId;	// ������� � ��������, � ������� ����� ������� ������ ID ��������, � ������� ��� PersonID

	private List<ObjectId> linkedPersonAttrIds;	// ������ ��������� �� ��������� ���������, �� ������� ���������� � ����� ����������� PersonID � ���������� � ����� ������
	private List<ObjectId> linkedCardPersonAttrIds;

	private ObjectId linkedCardsStateId;	// ��������� ��������, ������� ����� ��������������
	private ObjectId linkChildAttrId;		// �������� �� ���������, �� ������� ����� ������������ ��������� ��������

	private boolean append = false;			// � ������ true, destPersonAttrId ����� ����������� ���������, ����� - ������������ ������ 


	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {	// ������� ����� ���������� (������ ��� ������)
		long startTime = System.currentTimeMillis();
		logger.info( "Start processing processor '"+this.getClass().getName()+"': time = "+ startTime);
		// final Long cardId = (Long)getCardId().getId();
		final ObjectId cardId = getCardId();
		final UserData user = getSystemUser();	// ������� ������������ ����

		final Card card = this.getCard(user, Attribute.ID_CHANGE_DATE);	// �������� ������ �� ��������
		if (card == null) return null;

		final List<ObjectId> oldPersonIds = new ArrayList<ObjectId>();	// ���� ������������ ������ ������
		final List<ObjectId> oldCardsIds = new ArrayList<ObjectId>();	// � ���� ������������� CardID ��� ������
		if (append) {	// ���� ����� ���������� ��������� ��������, �� � ������ ���������� ��� ��������� �������� 
			if (destPersonAttrId != null) { 
				try {
					final Collection<Person> persons = super.getPersonsList( card, destPersonAttrId, true);
					if (persons != null && !persons.isEmpty())
						oldPersonIds.addAll( ObjectIdUtils.collectionToSetOfIds(persons));
				} catch (NullPointerException e) {
					logger.warn( "U-attribute "+ destPersonAttrId+" in card #"+ cardId.getId()+" is not set or null");
				}
			}
			if (destCardAttrId != null) { 
				try {
					final Collection<ObjectId> ids = CardUtils.getAttrLinks(card, destCardAttrId);
					if (ids != null)
						oldCardsIds.addAll( ids);
				} catch (NullPointerException e) {
					logger.warn( "C-Attribute "+ destPersonAttrId + " in card #"+ cardId.getId()+" is not set or null");
				}
			}
		}

//		CardUtils.dropAttributes(getJdbcTemplate(), 
//				new Object[] { /*destPersonAttrId,*/ destCardAttrId, Attribute.ID_CHANGE_DATE}, 
//				cardId);	// ������� ��������� destPersonAttrId, destCardAttrId, Attribute.ID_CHANGE_DATE � �������� ��������
		markChangeDate(cardId);	// �������� ������� ���� ��������� � ��������
		updateDateAttrInCard(card, Attribute.ID_CHANGE_DATE);

		final Set<ObjectId> fetchAttrIds = new HashSet<ObjectId>();	// ��������� ��������� ��� ������ ������ � ��������� ���������
		if (linkedPersonAttrIds != null)
			fetchAttrIds.addAll(linkedPersonAttrIds);
		if (linkedCardPersonAttrIds != null)
			fetchAttrIds.addAll(linkedCardPersonAttrIds);

		final Collection<ObjectId> roots = new ArrayList<ObjectId>();	// ��������� ������ ��� ������ ��������� ��������
		if (this.getLinkAttrId() != null) {
			final Collection<ObjectId> list = CardUtils.getAttrLinks(card, this.getLinkAttrId());
			if (list != null)
				roots.addAll(list);
		} else	// ���� ������� cardlink-�������� ������, �� ������ ����
			roots.add(getCardId());

		if (roots.isEmpty()){	// ���� �� ������ ����� ��� ������ ���, �� � ������ ������
			logger.debug("No linked cards found. Exit.");
			return null;
		}

		SearchResult sr = null;
		{
			final GetDeepChildren action = new GetDeepChildren();
			action.setChildTypeId(linkChildAttrId);
			action.setDepth(depth);
			action.setRoots(roots);
			final Set<ObjectId> childs = (Set<ObjectId>) super.execAction(action, user);	// ����� ���� ����� ��� ������ ��������� ������ 
			childs.addAll(roots);	// �������� �����

			final Search searchAction = new Search();	// ��������� �����
			searchAction.setByCode(true);				// ����� ������ - �� ���� ��������
			searchAction.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(childs));	// ������ ������ �������� ����� ������� 

			final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();	// ������ ������� � ��
			columns.add( AbstractCardProcessor.createColumn(Card.ATTR_STATE));

			for( Iterator<ObjectId> i = fetchAttrIds.iterator(); i.hasNext(); ) {
				columns.add(AbstractCardProcessor.createColumn(i.next()));	// ��������� ������ ������� ��������� ��� ������ ������
			}
			searchAction.setColumns(columns);	// ������ ���������� ������ ������ �������

			sr = (SearchResult) super.execAction(searchAction, user);	// ��������� ����� ���� �������� � ��������� ��������� (�.�. �� ����������� ��������� ������)
		}
		// ��������� ������ ���������� � ��������� ��������� � ��������� ���������
		final Collection<Card> linkedCards = CardUtils.getCardsList(sr); 

		// ���� �������� �� ��������� ���������...
		final Set<ObjectId> personIds = new HashSet<ObjectId>();	// ��������� ������
		final Set<ObjectId> cardIds = new HashSet<ObjectId>();		// ��������� cardID ��� ������
		for( Card linkedCard: linkedCards )	// ����������� �� ��������� ��������
		{
			logger.trace("Processing linked card " + linkedCard.getId().getId());			
			if (linkedCardsStateId == null || linkedCard.getState().equals(linkedCardsStateId)) {	// ���� �� �������� ���������� �� ���������� �������� ��� �� ��������� ��������� �������� ��������� � �������� ��������
				if (linkedPersonAttrIds != null) {	// ��������� ��������� ������ ���������� �� ����������� � linkedPersonAttrIds ���������, �������������� � ������ �������� 
					for( ObjectId itemPersonAttrId : linkedPersonAttrIds) {
						final PersonAttribute attr = (PersonAttribute)linkedCard.getAttributeById(itemPersonAttrId);
						if (attr != null)
							ObjectIdUtils.fillObjectIdSetFromCollection(personIds, attr.getValues());
					}
				}
				if (linkedCardPersonAttrIds != null) {	// ��������� ��������� cardlink-�� ���������� �� ����������� � linkedCardPersonAttrIds ���������, �������������� � ������ ��������
					for( ObjectId itemCardAttrId: linkedCardPersonAttrIds){
						final CardLinkAttribute attr = linkedCard.getCardLinkAttributeById(itemCardAttrId);
						if (attr != null)
							ObjectIdUtils.fillObjectIdSetFromCollection(cardIds, attr.getIdsLinked());
					}
				}
			}
		}

		// ����� id ������...
		if (destPersonAttrId != null) {
			final Set<ObjectId> outPersonIds = new HashSet<ObjectId>(personIds);
			if (append)
				outPersonIds.addAll(oldPersonIds);

			// ���������� �������� id ������������ �������� � ���� ������...
			ObjectIdUtils.fillObjectIdSetFromCollection( outPersonIds, super.getPersonsByCards(cardIds));

			// ���������� � ��������...
			boolean unlock = false;
			if (cardId != null) {
				execAction(new LockObject(cardId));
				unlock = true;
			}
			try {
				insertCardPersonAttributeValues( cardId, destPersonAttrId, outPersonIds, true);
			} finally {
				if (unlock)
					execAction(new UnlockObject(cardId));
			}
		}

		// ����� cardlinks ...
		if (destCardAttrId != null) {
			final Set<ObjectId> outCardIds = new HashSet<ObjectId>(cardIds);
			if (append)
				outCardIds.addAll(oldCardsIds);

			// �������� ������� � �������� �� ������������ ��������...
			for (Person person : super.getPersonsByCards(personIds)) {
				outCardIds.add(person.getCardId());
			}

			// ���������� � �������� ������ �� �������� ...
			boolean unlock = false;
			if (cardId != null) {
				execAction(new LockObject(cardId));
				unlock = true;
			}
			try {
				insertCardsAttributeValues( cardId, destCardAttrId, outCardIds);
			} finally {
				if (unlock)
					execAction(new UnlockObject(cardId));
			}
		}

		logger.info( "Finish processing processor '"+this.getClass().getName()+"': workingtime = "+ (System.currentTimeMillis()-startTime));
		return null;
	}


	/**
	 * ��������� ������ �� ������ �������� � ��������� �������.
	 * @param destCardId: id-��������, ��� ������� ����� ���������� ������.
	 * @param destAttrId: cardlink-������� ��� ������.
	 * @param outCardIds: ������ ������.
	 * @return ���������� ��������� �������.
	 * @throws DataException 
	 */
	private int insertCardsAttributeValues(ObjectId destCardId,
			ObjectId destAttrId, Set<ObjectId> outCardIds) throws DataException 	{
		if (destCardId == null || destAttrId == null 
				|| outCardIds == null || outCardIds.isEmpty())
			return -1;
		final String cardInfo = "card "+ destCardId.getId() + " attribute "+ destAttrId; 
		logger.debug( "Saving "+ outCardIds.size()+" cardlinks into "+ cardInfo);
		final String sql =	// �������� � ������� �������� � �������� ������� �������� ������������� �������� �� ��������� ��������
				"insert into attribute_value \n" +
				"(card_id, attribute_code, number_value) \n" +
				"	select ?, ?, c.card_id \n" +
				"	from card c \n" +
				"	where c.card_id in (" 
					+ ObjectIdUtils.numericIdsToCommaDelimitedString(outCardIds) 
					+ ") \n"
			;
		final int count = getJdbcTemplate().update( sql,
			new Object[] {
				destCardId.getId(),
				destAttrId.getId()
			},
			new int[] {
				Types.NUMERIC,
				Types.VARCHAR
			}
		);		logger.debug( count + " records inserted into "+ cardInfo);

		return count;
	}


	@Override
	public Card getCard( UserData user, final ObjectId attrToChk) 
		throws DataException
	{
		if (user == null)
			user = getSystemUser();

		// ��������� �������� �������� � ������ ��������� (����� ���� � ��� �� 
		// ��������� ��� ��������� � ������ ���� ���������: 
		// Store/ChangeState, pre/post.
		Card acard = null;
		if (getObject() instanceof Card)
			acard = (Card) getObject();
		else if (getAction() instanceof ChangeState)
			acard = ((ChangeState) getAction()).getCard();

		// �������� ������� ��������...
		if (	acard == null  
				|| (   (attrToChk != null)
						&& (acard.getAttributeById(attrToChk) == null) // ��� ������������ ��������...
				)
		)
		{	// ��� �������� ��� ��� ��������... �������� ��������� �� id ...
			final ObjectId cardId = this.getCardId();
			if (cardId != null)  {
				// acard = loadCardById( id, user);
				final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
				// TODO: ����� �������� �������� ���� ������� ��� � �����-�� ���, ����� ��������� ���-�� ������
				cardQuery.setAccessChecker(null);
				cardQuery.setId(cardId);
				acard = (Card) getDatabase().executeQuery( user, cardQuery);
			}
		}
		return acard;
	}


	@Override
	public void setParameter(String name, String value) {	// ��������� ���������� ���������� �� ������ ���������� ������� (��������� ��������� � ����� �������� ����� ��������)
		if ("linkAttrId".equalsIgnoreCase(name)) {
			if ("none".equalsIgnoreCase(value)||"null".equalsIgnoreCase(value)) {
				setLinkAttrId(null);
				return;
			}
		}	
		if ("personAttrId".equalsIgnoreCase(name)
				|| "destPersonAttrId".equalsIgnoreCase(name) ) {
			this.destPersonAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if ("cardAttrId".equalsIgnoreCase(name)
					|| "destCardAttrId".equalsIgnoreCase(name) ) {
			this.destCardAttrId = ObjectIdUtils.getObjectId( CardLinkAttribute.class, value, false);
		} else if ("linkedPersonAttrIds".equalsIgnoreCase(name)) {
			linkedPersonAttrIds = stringToAttrIds(PersonAttribute.class, value);
		} else if ("linkedCardPersonAttrIds".equalsIgnoreCase(name)) {
			linkedCardPersonAttrIds = stringToAttrIds(CardLinkAttribute.class, value);
		} else if ("linkedCardsStateId".equalsIgnoreCase(name)) {
			this.linkedCardsStateId = ObjectIdUtils.getObjectId(CardState.class, value, true);
		} else if ("linkChildAttrId".equalsIgnoreCase(name)) {
			this.linkChildAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else if ("writeOperation".equalsIgnoreCase(name)) {
			if ("append".equalsIgnoreCase(value))
				append = true;
		} else if ("depth".equalsIgnoreCase(name)) {
			try {
				this.depth = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				depth = 10;
			}  
		} else {
			super.setParameter(name, value);
		}
	}
}