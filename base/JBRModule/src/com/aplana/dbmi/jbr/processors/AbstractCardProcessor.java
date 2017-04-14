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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractInterruptibleBatchPreparedStatementSetter;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

public abstract class AbstractCardProcessor
		extends ParametrizedProcessor 
		implements DatabaseClient {
	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbcTemplate;
	private AccessRuleManager accessManager;

	/**
	 * @return identifier of currently processed {@link Card} object
	 */
	public ObjectId getCardId() {
		if (getObject() != null) {
			return getObject().getId();
		}
		final Action action = getAction(); 

		if (action instanceof ChangeState) {
			return ((ChangeState) getAction()).getObjectId();
		} if (action instanceof ObjectAction) {
			final ObjectAction objectAction = (ObjectAction)action;
			if (objectAction.getObjectId().getType().equals(Card.class)) {
				return objectAction.getObjectId();	
			}
		}
		return null;
	}

	/**
	 * ��������� ������ ������ �������������� ������ ������������ �������� 
	 * (��� �� ���������).
	 * @param cardsOrIds ������ ������������ �������� (Card[]) ��� id �������� (ObjectId[]).
	 * @return ����� ������ ��� null, ���� ��� �� �����.
	 * @throws DataException 
	 */
	public Set<Person> getPersonsByCards( Collection<?> cardsOrIds) 
		throws DataException {
			return CardUtils.getPersonsByCards(cardsOrIds, getQueryFactory(), getDatabase(), 
						getSystemUser());
	}

	/**
	 * ��������� ������ �������� � ���� �� ����� ������������ user.
	 * @param action
	 * @param user
	 * @return ������, ������������ �����.
	 * @throws DataException 
	 */
	public <T> T execAction(Action action, UserData user) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(user, query);
	}

	/**
	 * ��������� ������ �������� � ���� �� ����� ���������� ������������.
	 * @param action
	 * @return ������, ������������ �����.
	 * @throws DataException
	 */
	public <T> T execAction(Action action) throws DataException {
		return execAction(action, getSystemUser());
	}

	/**
	 * @return jdbcTemplate to be used for database queries
	 */
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public AccessRuleManager getAccessManager() {
		return accessManager;
	}

	static SearchResult.Column createColumn(ObjectId attrId) 
	{
		return CardUtils.createColumn(attrId);
	}

	final public static List<ObjectId> stringToAttrIds(Class<?> idItemsType, String str) {
		return stringToAttrIds( idItemsType, str, false);
	}

	final public static List<ObjectId> stringToNumericIds(Class<?> idItemsType, String str) {
		return stringToAttrIds(idItemsType, str, true);
	}

	final public static List<ObjectId> stringToAttrIds(Class<?> idItemsType, String str, 
			boolean isNumeric) 
	{
		if (str == null) return null;
		final String[] ids = str.split("\\s*[;,]\\s*");
		final List<ObjectId> result = new ArrayList<ObjectId>(ids.length);
		for (int i = 0; i < ids.length; ++i) {
			final String id = ids[i].trim();
			if (!"".equals(id))
				result.add(ObjectIdUtils.getObjectId(idItemsType, id, isNumeric));
		}
		return result;
	}

	/**
	 * ��������� ��������� �������� (����� ��� ���������).
	 * @param cardId: ��������, ��� ������� �������� ���������.
	 * @param listAttrId: ��������� ������� � �������� cardId.
	 * @param reverseLink: ����������� ��������� ��������� ��������, 
	 *    false: ��������������� �� listAttrId (�.�. ��������� ����� � ��������),
	 *    true: �������� (����� backlink).
	 * @param fetchAttrIds: �������� ��� �������� (������ � id �������� ������), 
	 * null - ������ ������ � id. 
	 * @return ������ ����������� ��������.
	 * @throws DataException
	 */
	protected List<Card> getLinkedCards(ObjectId cardId, ObjectId listAttrId,
			boolean isReverseLink, Collection<ObjectId> fetchAttrIds, UserData user) 
			throws DataException 
	{
		if (listAttrId == null) return null;
		if (user == null) user = getSystemUser(); // getUser();

		/**
		 * ������������ ��������� ������� ...
		 */
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();

		columns.add( AbstractCardProcessor.createColumn(Card.ATTR_STATE));
		columns.add( AbstractCardProcessor.createColumn(Card.ATTR_ID));
		columns.add( AbstractCardProcessor.createColumn(Card.ATTR_TEMPLATE));

		if (fetchAttrIds != null) {
			for (ObjectId fetchAttrId : fetchAttrIds)
				columns.add(AbstractCardProcessor.createColumn(ObjectIdUtils.getIdFrom(fetchAttrId)));
		}

		/**
		 * ������������ List- ��� Search- action.
		 */
		Action action;
		if (BackLinkAttribute.class.isAssignableFrom(listAttrId.getType())) {
			final ListProject listAction = new ListProject();
			listAction.setCard( cardId);
			listAction.setAttribute( listAttrId);
			listAction.setColumns(columns);
			action = listAction;
		} else {
			final FetchChildrenCards fetchAction = new FetchChildrenCards();
			fetchAction.setCardId(cardId);
			fetchAction.setLinkAttributeId(listAttrId);
			fetchAction.setReverseLink(isReverseLink);
			fetchAction.setCardId(cardId);
			fetchAction.setColumns(columns);
			action = fetchAction;
		}

		final List<Card> cards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user);
		return cards;
	}

	protected List<Card> getLinkedCards(ObjectId cardId, ObjectId listAttrId,
			boolean reverseLink, Collection<ObjectId> fetchAttrIds) 
			throws DataException {
		return getLinkedCards(cardId, listAttrId, reverseLink, fetchAttrIds,
					getSystemUser() );
		
	} 

	protected List<Card> getLinkedCards(ObjectId cardId, ObjectId listAttrId,
			boolean reverseLink) throws DataException
	{
		return getLinkedCards( cardId, listAttrId, reverseLink, null);
	}

	/**
	 * ��������� ������� ������� ��������� � ��������� ������� (���� ����)
	 * ��������� ��������.
	 * @param cardsOrIds: ������ �������� (Card[]) ��� id (ObjectId[]).
	 * @param dateAttr: ��� �������� � ����� ���� ��� ���������� now().
	 * @param dropDateAttr: true, ���� ������� ���� ���� �������������� ������� 
	 * �� ��, false: ���� ���.
	 * @return
	 * @throws DataException 
	 */
	protected int markChangeDate( Collection<?> cardsOrIds, final ObjectId dateAttr, 
			boolean dropDateAttr) throws DataException
	{
		if (cardsOrIds == null)
			return -1;

		// �������� null-��������
		for (Iterator<?> i = cardsOrIds.iterator(); i.hasNext();) {
			if (i.next() == null)
				i.remove();
		}
		if (cardsOrIds.isEmpty())
			return -1;

		// �������� ��� ���������� ������...
		if (dropDateAttr) { // ������� ��������� ����������� ��������...
			for (Object obj: cardsOrIds) {
				final ObjectId id = ObjectIdUtils.getIdFrom(obj);
				execAction(new LockObject(id));
				try {
					CardUtils.dropAttributes( getJdbcTemplate(), new Object[] {dateAttr}, id);
				} finally {
					execAction(new UnlockObject(id));
				}
			}
		}

		// ������� �������...
		final String sCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(cardsOrIds);
		// (2011/02/18, YNikitin aka GoRik) ��� ������������� ������ update ����� ������� ��� ��������� �� ������� ����, � ������� �� updateBatch()
		// (2011/02/18, YNikitin aka GoRik) ����������� ���� � ������� ������� ���� ����� �������
		final Date curDate = new Date();
		/* ������ ���������� (������� ���� �� ����������)
		String sql = "insert into attribute_value (card_id, attribute_code, date_value) \n" +
		"\t select card_id, ?, ? \n" +
		"\t from card \n" +
		"\t where card_id in (" + sCardIds + ")"; 
		final int result = getJdbcTemplate().update(
				// (2009/12/02, RuSA) ��� Enterprise DB �������� �������������, ���� � ORA �����
				// OLD:	"insert into attribute_value av (card_id, ..."
				sql,
				new Object[] { dateAttr.getId(), new Date()) },
				new int[] { Types.VARCHAR, Types.TIMESTAMP }
			);
		*/
		// ToDo: ���������� �� ����� OwerwriteAttributes
		String sql = "insert into attribute_value (card_id, attribute_code, date_value) \n" +
		"\t select card_id, ?, ? \n" +
		"\t from card \n" +
		"\t where card_id in (" + sCardIds + ")"; 
		InterruptibleBatchPreparedStatementSetter pssI = 
			new AbstractInterruptibleBatchPreparedStatementSetter() {
	
			@Override
			protected boolean setValuesIfAvailable(PreparedStatement stmt, int index)
																throws SQLException {
					stmt.setString(1, dateAttr.getId().toString());
//					stmt.addBatch();
					/* (2011/02/18, YNikitin aka GoRik) ���������� ��������� �������� ������������ ������� ����, ������ ��� ������� ���� ����-������� - ������� 
					 * ���� ��� �� ��r, �� �������� ������������ ����� 
					 * ����� ������� � ������ ���������� � �������������, ����� ��������� � "���� �����" ������
					 */ 
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					stmt.setTimestamp( 2, sqlTimestamp(curDate), calendar);
					stmt.addBatch();

				return false;
			}
			private java.sql.Timestamp sqlTimestamp(Date date) {
				if (date == null)
					return null;
				return new java.sql.Timestamp(date.getTime());
			}
		};		
		
		ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>(cardsOrIds.size());
		try {
			for (Object obj: cardsOrIds) {
				ObjectId id = ObjectIdUtils.getIdFrom(obj);
				execAction(new LockObject(id));
				lockedCards.add(id);
			}
			getJdbcTemplate().batchUpdate(sql, pssI);
		} finally {
			for (ObjectId id: lockedCards) {
				execAction(new UnlockObject(id));
			}
		}		logger.debug( /*result*/1 + " change date attribute "+ dateAttr.getId() + " wrote into card(s) " + sCardIds);
		return /*result*/1;
	}

	protected int markChangeDate( Collection<?> cardsOrIds, boolean dropDateAttr) throws DataException {
		return markChangeDate( cardsOrIds, Attribute.ID_CHANGE_DATE, dropDateAttr);
	}

	protected int markChangeDate( Collection<?> cardsOrIds) throws DataException{
		return markChangeDate( cardsOrIds, true);
	}

	protected int markChangeDate( ObjectId cardId, boolean dropDateAttr) throws DataException {
		if (cardId == null)
			return -1;
		return markChangeDate( Collections.singleton(cardId), Attribute.ID_CHANGE_DATE, dropDateAttr);
	}

	protected int markChangeDate( ObjectId cardId) throws DataException{
		return markChangeDate( cardId, true);
	}

	/**
	 * �������� DateAttribute ��� �������� �� ��
	 * @param cardId: id ��������.
	 * @param dateAttrId: ��� �������� � ����� ���� ��� ��������.
	 * @return
	 */
	protected void updateDateAttrInCard(Card card, ObjectId dateAttrId){
		DateAttribute dateAttr = card.getAttributeById(dateAttrId);
		DateAttribute dbDateAttr = CardUtils.getDateAttribute(card.getId(), dateAttrId, getJdbcTemplate());
		if ((dateAttr==null&&dbDateAttr==null)||(dateAttr.getValue()==null&&dbDateAttr.getValue()==null)){
			return;
		}
		if (dateAttr!=null&&dbDateAttr!=null){
			dateAttr.setValueFromAttribute(dbDateAttr);
		} else {
			card.getAttributes().add(dbDateAttr);
		}
	}

	/**
	 * �������� DateAttribute ��� �������� �� ��
	 * @param cards: ������ ��������.
	 * @param dateAttrId: ��� �������� � ����� ���� ��� ��������.
	 * @return
	 */
	protected void updateDateAttrInCards(Collection<Card> cards, ObjectId dateAttrId){
		for(Card card : cards){
			DateAttribute dateAttr = card.getAttributeById(dateAttrId);
			DateAttribute dbDateAttr = CardUtils.getDateAttribute(card.getId(), dateAttrId, getJdbcTemplate());
			if ((dateAttr==null&&dbDateAttr==null)||(dateAttr.getValue()==null&&dbDateAttr.getValue()==null)){
				return;
			}
			if (dateAttr!=null&&dbDateAttr!=null){
				dateAttr.setValueFromAttribute(dbDateAttr);
			} else {
				card.getAttributes().add(dbDateAttr);
			}
		}
	}

	protected void recalculateAccessList(final ObjectId cardId) {
		if (cardId != null) {
			if (getPrimaryQuery()!=null){
				getPrimaryQuery().putCardIdInRecalculateAL(cardId);
			} else {
				accessManager.updateAccessToCard(cardId);
			}
		}
    }

    protected void recalculateAccessList(final List<Card> cards) {
		for (Card card: cards) {
			if (getPrimaryQuery()!=null){
				getPrimaryQuery().putCardIdInRecalculateAL(card.getId());
			} else {
				accessManager.updateAccessToCard(card.getId());
			}
		}
    }

    protected void cleanAccessList(final ObjectId cardId) {
		if (cardId != null) {
			//accessManager.cleanAccessListByCard(cardId);
			//accessManager.cleanAccessListBySourceCard(cardId);
			accessManager.cleanAccessListByCardAndSourceAttrs(cardId);
		}
    }

    protected void cleanAccessListByAttributeAndCard(final ObjectId attrId, final ObjectId cardId) {
		if (cardId != null&&attrId!=null) {
			accessManager.cleanAccessListByAttributeOfCard(attrId, cardId);
		}
    }

    protected void cleanAccessListByLinkToCard(final ObjectId cardId, final String attrCodes, final String templateIds) {
		if (cardId != null) {
			accessManager.cleanAccessListByLinkToCard(cardId, attrCodes, templateIds);
		}
    }

    protected void cleanAccessList(final List<Card> cards) {
		for (Card card: cards) {
			//accessManager.cleanAccessListByCard(card.getId());
			//accessManager.cleanAccessListBySourceCard(card.getId());
			accessManager.cleanAccessListByCardAndSourceAttrs(card.getId());
		}
    }

	/**	 * Implementation of {@link DatabaseClient#setJdbcTemplate(JdbcTemplate)} method
	 */	
   public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbcTemplate = jdbc;
	    accessManager = new AccessRuleManager(jdbc);
    }
}