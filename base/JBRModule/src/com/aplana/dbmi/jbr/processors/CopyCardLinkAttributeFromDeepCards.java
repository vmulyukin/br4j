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
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

public class CopyCardLinkAttributeFromDeepCards extends BaseCopyAttributeProcessor {

	private static final long serialVersionUID = 1L;

	private int depth = 10;				// ������� ����������� ������ �� ���������
	
	private ObjectId destCardAttrId;	// ������� � ��������, � ������� ����� ������� ������ ID ��������

	private ObjectId linkChildAttrId;		// �������� �� ���������, �� ������� ����� ������������ ��������� ��������

	private ObjectId linkAttrId; 


	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {	// ������� ����� ���������� (������ ��� ������)
		long startTime = System.currentTimeMillis();
		logger.info( "Start processing processor '"+this.getClass().getName()+"': time = "+ startTime);
		
		final ObjectId cardId = getCardId();
		final UserData user = getSystemUser();	// ������� ������������ ����

		final Card card = this.getCard(user, Attribute.ID_CHANGE_DATE);	// �������� ������ �� ��������
		if (card == null) return null;

		markChangeDate(cardId);	// �������� ������� ���� ��������� � ��������
		updateDateAttrInCard(card, Attribute.ID_CHANGE_DATE);
		final Collection<ObjectId> roots = new ArrayList<ObjectId>();	// ��������� ������ ��� ������ ��������� ��������
		if (linkAttrId != null) {
			Collection<ObjectId> list = null;
			if(CardLinkAttribute.class.isAssignableFrom(linkAttrId.getType())) {
				list = CardUtils.getAttrLinks(card, linkAttrId);
			} else if(BackLinkAttribute.class.isAssignableFrom(linkAttrId.getType())) {
				list = CardUtils.getCardIdsByBackLink(linkAttrId, card.getId(), 
						getQueryFactory(), getDatabase(), getSystemUser());
			}
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
			searchAction.setColumns(columns);	// ������ ���������� ������ ������ �������

			sr = (SearchResult) super.execAction(searchAction, user);	// ��������� ����� ���� �������� � ��������� ��������� (�.�. �� ����������� ��������� ������)
		}
		// ��������� ������ ���������� � ��������� ��������� � ��������� ���������
		final Collection<Card> linkedCards = CardUtils.getCardsList(sr); 

		final Set<ObjectId> linkedCardIds = new HashSet(linkedCards.size());
		for(Card c : linkedCards) {
			linkedCardIds.add(c.getId());
		}
		// ����� id ��������
		if (destCardAttrId != null) {
			// ���������� � ��������
			boolean unlock = false;
			if (cardId != null) {
				execAction(new LockObject(cardId));
				unlock = true;
			}
			try {
				insertCardsAttributeValues( cardId, destCardAttrId, linkedCardIds);
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
					+ ") \n and not exists (select 1 from attribute_value av where av.attribute_code=? and av.number_value=c.card_id)"
			;
		final int count = getJdbcTemplate().update( sql,
			new Object[] {
				destCardId.getId(),
				destAttrId.getId(),
				destAttrId.getId()
			},
			new int[] {
				Types.NUMERIC,
				Types.VARCHAR,
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
			this.linkAttrId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if ("cardLinkAttrId".equalsIgnoreCase(name)) {
			this.destCardAttrId = ObjectIdUtils.getObjectId(CardLinkAttribute.class, value, false);
		} else if ("linkChildAttrId".equalsIgnoreCase(name)) {
			this.linkChildAttrId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
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
