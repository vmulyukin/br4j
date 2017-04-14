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
package com.aplana.dbmi.jbr.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ����������� �����.
 * @author RAbdullin
 */
public class CardUtils
{
	final static Log logger = LogFactory.getLog(CardUtils.class);

	private CardUtils() {
	}

	/**
	 * @see com.aplana.dbmi.jbr.processors.AbstractCopyPersonProcessor
	 * ��������� ������� ������� ��������� � ��������� ������� (���� ����)
	 * ��������� ��������.
	 * @param cardsOrIds: ������ �������� (Card[]) ��� id (ObjectId[]).
	 * @param dateAttr: ��� �������� � ����� ���� ��� ���������� now().
	 * @param dropDateAttr: true, ���� ������� ���� ���� �������������� ������� 
	 * �� ��, false: ���� ���.
	 * @return
	 */
	public static int markChangeDate(JdbcTemplate jdbc, Collection<?> cardsOrIds, ObjectId dateAttr,
			boolean dropDateAttr)
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
				CardUtils.dropAttributes( jdbc, new Object[] {dateAttr}, id);
			}
		}
		// ������� �������...
		final String sCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(cardsOrIds); 
		final int result = jdbc.update(
				// (2009/12/02, RuSA) ��� Enterprise DB �������� �������������, ���� � ORA �����
				// OLD:	"insert into attribute_value av (card_id, ..."
				"insert into attribute_value (card_id, attribute_code, date_value) \n" +
				"\t select card_id, ?, ? \n" +
				"\t from card \n" +
				"\t where card_id in (" + sCardIds + ")",
				new Object[] { dateAttr.getId(), new Date() },
				new int[] { Types.VARCHAR, Types.TIMESTAMP }
			); 
		logger.debug( result + " change date attribute "+ dateAttr.getId() + " wrote into card(s) " + sCardIds);
		return result;
	}
	public static int markChangeDate( JdbcTemplate jdbc, Collection<?> cardsOrOds, boolean dropDateAttr) {
		return markChangeDate( jdbc, cardsOrOds, Attribute.ID_CHANGE_DATE, dropDateAttr);
	}
	public static int markChangeDate( JdbcTemplate jdbc, Collection<?> cardsOrOds){
		return markChangeDate( jdbc, cardsOrOds, true);
	}
	public static int markChangeDate( JdbcTemplate jdbc, ObjectId cardId, boolean dropDateAttr) {
		if (cardId == null)
			return -1;
		return markChangeDate( jdbc, Collections.singleton(cardId), Attribute.ID_CHANGE_DATE, dropDateAttr);
	}
	public static int markChangeDate( JdbcTemplate jdbc, ObjectId cardId){
		return markChangeDate( jdbc, cardId, true);
	}

	/**
	 * ��������� DateAttribute ��� �������� �� ��
	 * @param cardId: id ��������.
	 * @param dateAttrId: ��� �������� � ����� ���� ��� ��������.
	 * @param jdbc: �� ����� �� ���������
	 * @return
	 */
	public static DateAttribute getDateAttribute(ObjectId cardId, ObjectId dateAttrId, JdbcTemplate jdbc)
	{
		if (dateAttrId==null||cardId==null||dateAttrId.getId()==null||cardId.getId()==null){
			return null;
		}
		DateAttribute dateAttr = new DateAttribute();
		dateAttr.setId(dateAttrId);
		Date result = null;
		try{
			result = (Date)jdbc.queryForObject(
					"select av.date_value from attribute_value av \n" +
					"\t where card_id = ? and attribute_code = ?",
					new Object[] { cardId.getId(), dateAttrId.getId() },
					new int[] { Types.NUMERIC, Types.VARCHAR },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							Date date = new Date();
							date.setTime(rs.getTimestamp(1).getTime());
							return date;
						}
					});
		} catch (Exception e){
			result = null;
		}
		dateAttr.setValueWithTZ(result);
		logger.debug( "Try to get dateattribute "+ dateAttrId.getId() + " from card " + cardId.getId());
		return dateAttr;
	}
	/**
	 * ���������� ��� �������� ������� ��� ���������.
	 * @param attrId �������
	 * @return ������� � ���� ���������.
	 */
	public static SearchResult.Column createColumn(ObjectId attrId) 
	{
		final SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(attrId);
		return col;
	}

	/**
	 * �������� ������ ������� {@link SearchResult.Column}[] ��� ���������� 
	 * ������ ���������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return ����� ������ �������.
	 */
	public static List<SearchResult.Column> createColumns(ObjectId... attrIds)
	{
		return (attrIds == null)
					? null
					: addColumns( new ArrayList<SearchResult.Column>(attrIds.length), attrIds);
	}

	/**
	 * �������� ��������� ������� ��������� ��������� � ������ �������. 
	 * @param dest ������� ������, � ������� ��������.
	 * @param attrIds id ���������, ��� ������� ��������� �������� ��������� �������.
	 * @return dest.
	 */
	public static List<SearchResult.Column> addColumns(List<SearchResult.Column> dest, ObjectId... attrIds)
	{
		if (dest != null && attrIds != null) {
			for (ObjectId id : attrIds) {
				if (id != null)
					dest.add(createColumn(id));
			}
		}
		return dest;
	}

	public static final Search getFetchAction(String cardIdsList, ObjectId[] attributes) {
		final Search search = new Search();
		search.setByCode(true);
		search.setByAttributes(false);
		search.setWords(cardIdsList);
		if (attributes != null && attributes.length > 0) 
		{
			final ArrayList<SearchResult.Column> columns = 
				new ArrayList<SearchResult.Column>(attributes.length);
			addColumns(columns, attributes);
			search.setColumns(columns);
		}
		return search;
	}

	public static final Search getFetchAction(LinkAttribute cards, ObjectId[] attributes) {
		return getFetchAction( cards.getLinkedIds(), attributes);
	}

	public static final Search getFetchAction(ObjectId cardId, ObjectId[] attributes) {
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("cardId must be card ID");
		return getFetchAction(cardId.getId().toString(), attributes);
	}

	/**
	 * ������� �� �� ��� �������� � ���������� ������ ��� ��������� ��������. 
	 * @param jdbc: ��������� ��������.
	 * @param attrCodes: ���� ��������� ��� �������� (string[] ��� ObjectId[]), 
	 * �� ����� ���� ������,
	 * ������: 
	 * @param cardsOrIds: ������ Card[] ��� ObjectId[], ����� �� �����������, 
	 * ��� ��������� �������� �� ���� ���������.
	 * @param condition: (����) sql-������� ��� ������������ ��������� �������� 
	 * (�� ������� where), � ���� "and ...", ����� ������������ ������� 'av' ���
	 * ������� 'attribute_value'.
	 * @return ���������� ��������� ���������.
	 */
	public static final int dropAttributes(JdbcTemplate jdbc, Object[] attrCodes, 
			Collection<?> cardsOrIds, String condition)
	{
		if (jdbc == null || attrCodes == null || attrCodes.length < 1)
			return -1;

		final StringBuffer bufQuery = new StringBuffer();
		bufQuery.append( "delete from attribute_value av \n");
		bufQuery.append( "where av.attribute_code in ( \n" );

		// ������������ ������ ����� ���������...
		// final List<Object> args = new ArrayList<Object>(attrCodes.length);
		int added = 0;
		for (int i = 0; i < attrCodes.length; i++) {
			if (attrCodes[i] == null)
				continue;

			String attrCode = attrCodes[i].toString().trim();

			ObjectId id = null;
			if (attrCodes[i] instanceof DataObject) {
				final DataObject data = (DataObject) attrCodes[i];
				id = data.getId();
				if (id == null) continue;
			} else if (attrCodes[i] instanceof ObjectId) {
				id = (ObjectId) attrCodes[i];
			}
			if (id != null)
				attrCode = (id.getId() == null) ? "" : id.getId().toString().trim();

			if (attrCode.length() < 1) continue;

			// types.add( Types.VARCHAR);
			// args.add( attrCodes);
			if (added > 0) bufQuery.append( ", \n");
			bufQuery.append("\t\t '").append(attrCode).append("'"); // (!) ���������� ������ � ��������� ��������
			added++;
		}
		if (added == 0) return 0;
		bufQuery.append( " )\n");

		/* 
		 * ������� ��� ������������ �� id ��������, ���� ����� ������ �������� id ...
		 */
		if (cardsOrIds != null && !cardsOrIds.isEmpty())
			bufQuery.append( "\t and card_id in (")
				.append( ObjectIdUtils.numericIdsToCommaDelimitedString(cardsOrIds))
				.append(" ) \n");
		/*
		 * ��� ������������ �������
		 */
		if (condition != null)
			bufQuery.append(condition).append("\n");

		// return jdbc.update( bufQuery.toString(), args.toArray());
		final int result = jdbc.update( bufQuery.toString());
		if (logger.isDebugEnabled()) {
			logger.debug( "Dropped "+ result +" attributes by SQL: \n"+ bufQuery.toString() + " ;"); 
		}
		return result;
	}

	/**
	 * ������� �� �� ��� �������� � ���������� ������ ��� ��������� ��������. 
	 * @param jdbc: ��������� ��������.
	 * @param attrCodes: ���� ��������� ��� �������� (string[] ��� ObjectId[]), 
	 * �� ����� ���� ������,
	 * ������: 
	 * 	CardUtils.dropAttributes( getJdbcTemplate(), 
	 * 		new Object[] { Attribute.ID_CHANGE_DATE.getId(), 'JBR_DOCLINKS'},
	 * 		rootCardIds );
	 * @param cardsOrIds: ������ Card[] ��� ObjectId[], ����� �� �����������, 
	 * ��� ��������� �������� �� ���� ���������.
	 * @return ���������� ��������� ���������.
	 */
	public static final int dropAttributes(JdbcTemplate jdbc, Object[] attrCodes, 
			Collection<?> cardsOrIds) 
	{
		return dropAttributes(jdbc, attrCodes, cardsOrIds, null);
	}

	public static final int dropAttributes(JdbcTemplate jdbc, Object[] attrCodes, 
			ObjectId cardId) {
		if (cardId == null)
			return -1;
		return dropAttributes(jdbc, attrCodes, Collections.singleton(cardId));
	} 

	/**
	 * ��������� ������ ������ �������������� ������ ������������ �������� 
	 * (��� �� ���������).
	 * @param cardsOrIds ������ ������������ �������� (Card[]) ��� id �������� (ObjectId[]).
	 * @param factory: ������� ��������, ������������.
	 * @param database: ������ ������, ������������.
	 * @param user: �� ����� ���� ����������� �������, ������������.
	 * @return Set<Person> ����� ������ <strike>��� null, ���� ��� �� �����</strike> �.�. 01.07.2010
	 * @throws DataException ������ ��� ���������� �������.
	 * @throws IllegalArgumentException �� ������� ������������ ��������.
	 */
	@SuppressWarnings("unchecked")
	public static Set<Person> getPersonsByCards(
			Collection<?> cardsOrIds, 
			QueryFactory factory,
			Database database,
			UserData user) throws DataException
	{
		if (factory == null || database == null || user == null)
			throw new IllegalArgumentException();
		if (cardsOrIds == null || cardsOrIds.isEmpty())
			return new LinkedHashSet<Person>(0);
		logger.debug("Translating card identifiers to person identifiers...");

		final QueryBase lq = factory.getListQuery(Person.class);
		lq.setFilter( new PersonCardIdFilter(ObjectIdUtils.collectionToSetOfIds( cardsOrIds)) );
		final List<Person> persons = (List<Person>) database.executeQuery(user, lq);
		if (persons == null/* || persons.isEmpty()*/)	//***** shouldn't the query return list anyway? - A.P. 01.07.2010
			return new LinkedHashSet<Person>(0);
		if (persons.size() != cardsOrIds.size())
			logger.warn("Person cards found only for " + persons.size() + " users from " + cardsOrIds.size());
		return new LinkedHashSet<Person>( persons);
	}

	/**
	 * id ������� � ����������� ���.
	 */
	public static final ObjectId TEMPL_WS_SETTINGS = ObjectId.predefined(Template.class, "boss.settings");

	/**
	 * U-������� � ������ ������ ������� TEMPL_WS_SETTINGS.
	 */
	public static final ObjectId ATTR_BOSS = ObjectId.predefined(PersonAttribute.class, "boss.owner");

	/**
	 * U-������� � ����������� ������ ������� TEMPL_WS_SETTINGS.
	 */
	public static final ObjectId ATTR_ASSISTANT = ObjectId.predefined(PersonAttribute.class, "boss.assistant");

	public static final ObjectId ATTR_TEMPLATE = new ObjectId( Attribute.class, "_TEMPLATE");
	public static final ObjectId ATTR_STATUS = new ObjectId( Attribute.class, "_STATE");

	private static List<Card> getArmSettingsCards( Person boss, Person assistent, 
			QueryFactory factory,
			Database database,
			UserData user
		) throws DataException 
	{

		final Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singletonList(DataObject.createFromId(TEMPL_WS_SETTINGS)));
		if (boss != null)
			search.addPersonAttribute(ATTR_BOSS, boss.getId());
		if (assistent != null)
			search.addPersonAttribute(ATTR_ASSISTANT, assistent.getId());

		// Collections.singletonList(createColumn(ATTR_ASSISTANT)));
		search.setColumns( Arrays.asList( new SearchResult.Column[] {
				createColumn(ATTR_BOSS), 
				createColumn(ATTR_ASSISTANT),

				createColumn(Attribute.ID_NAME),
				createColumn(ATTR_TEMPLATE),
				createColumn(ATTR_STATUS)
			} ));


		final ActionQueryBase query = factory.getActionQuery(search);
		query.setAction(search);
		final SearchResult result = (SearchResult) database.executeQuery(user, query);
		return getCardsList( result);
	}

	/**
	 * �������� �������� � ����������� ���, � ������� ������ ����. 
	 * @param boss: ������������, ��� �������� ���� �������� ��� ��������.
	 * @param factory
	 * @param database
	 * @param user
	 * @return: null ��� �������� ������ �������� � ����������� ���, � ���������� 
	 * ������������ � ���������� (+������, ������).
	 * @throws DataException
	 */
	public static List<Card> getArmSettingsCardsByBoss( Person boss, QueryFactory factory,
			Database database,
			UserData user
		) throws DataException 
	{
		return getArmSettingsCards(boss, null, factory, database, user);
	}

	/**
	 * �������� �������� � ����������� ���, � ������� ���� ��������� ��������. 
	 * @param assistent: ���������, ��� �������� ���� �������� ��� ��������.
	 * @param factory
	 * @param database
	 * @param user
	 * @return: null ��� �������� ������ �������� � ����������� ���, � ���������� 
	 * ������������ � ���������� (+������, ������).
	 * @throws DataException
	 */
	public static List<Card> getArmSettingsCardsByAssistent( Person assistent, 
			QueryFactory factory,
			Database database,
			UserData user
		) throws DataException 
	{
		return getArmSettingsCards( null, assistent, factory, database, user);
	}


	/**
	 * �� ���������� ������ ��������� �������� � ������� ����������.
	 * ���� attributes �����, �� ����� ��������� ����������� ��������.
	 * @param idOrObjList: ������ id �������� ��� ������ ����� ��������.
	 * @param attributes: ������ ��������.
	 * @return ����������� ��������, ���������� ��� ������� ��������� ��������
	 * ��� null, ���� ��� �� ����� ��� ������ ����.
	 * @throws DataException
	 */
	public static List<Card> expandLinks( 
			Collection<?> idOrObjList, 
			ObjectId[] attributes,
			QueryFactory factory, Database database, UserData user
			)
			throws DataException 
	{
		if (idOrObjList == null || factory == null || database == null)
			return null;

		// ���� ������� ������ ...
		final Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords( ObjectIdUtils.numericIdsToCommaDelimitedString(idOrObjList));

		// ��������� ��������� ...
		if ( attributes != null && attributes.length > 0) {
			final ArrayList<SearchResult.Column> columns = 
				new ArrayList<SearchResult.Column>(attributes.length);
			for (int i = 0; i < attributes.length; i++)
				columns.add(createColumn(attributes[i]));
			columns.add(createColumn(Card.ATTR_STATE));
			search.setColumns(columns);
		}

		return execSearchCards(search, factory, database, user);
	}

	/**
	 * �������� ������ �������� �� ���������� ����������.
	 * @param sr
	 * @return �������� ������ �������� ��� null.
	 */
	public static List<Card> getCardsList( final SearchResult sr)
	{
		if (sr == null)
			return null;
		final List<Card> list = sr.getCards();
		return (list == null || list.isEmpty()) ? null : list;
	}


	/**
	 * �������� �������� �� ���������� �������.
	 * @Example: final List<Card> list = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getUser());
	 * @return �������� ������ �������� ��� null.
	 * @throws DataException
	 */
	public static List<Card> execSearchCards(
			final Action search,
			QueryFactory factory, Database database, UserData user
		) throws DataException 
	{
		if (search == null || factory == null)
			return null;

		// �������� �������...
		final ActionQueryBase searchQuery = factory.getActionQuery(search);
		searchQuery.setAction(search);
		searchQuery.setUser(user);

		// ����������
		final SearchResult result = (SearchResult) database.executeQuery(user, searchQuery);

		return getCardsList(result);
	}
	
	/**
	 * �������� �������� �� ���������� �������.
	 * @Example: final List<Card> list = CardUtils.execSearchCards(search, dataServiceBean);
	 * @return �������� ������ �������� ��� null.
	 */
	public static List<Card> execSearchCards(
			final Action search,
			DataServiceBean service)
	{
		if (service == null)
			return null;

		// ����������
		try {
			final SearchResult result = (SearchResult) service.doAction(search);
			return getCardsList(result);
		} catch (DataException e) {
			logger.error("Error searching cards", e);
			return null;
		} catch (ServiceException e) {
			logger.error("Error searching cards", e);
			return null;
		}
	}
	
	/**
	 * �������� �������� �� ListProject.
	 * @return �������� ������ �������� ��� null.
	 * @throws DataException
	 */
	public static List<Card> execListProject(
			final ObjectId attrId,
			final ObjectId cardId,
			QueryFactory factory, Database database, UserData user
		) throws DataException 
	{
		if (attrId == null || cardId == null || factory == null)
			return null;
		
		// �������� ������...
		final ListProject lp = new ListProject();
		lp.setAttribute(attrId);
		lp.setCard(cardId);

		// ���������� � �������
		long t1 = System.nanoTime();
		final List<Card> result = execSearchCards(lp, factory, database, user); 
		long t2 = System.nanoTime();
		final Formatter f = new Formatter();
		logger.info(f.format("execListProject %2.3f\n", (t1 - t2)/1.0e9));
		return result;
	}
	
	/**
	 * �������� �� �������� ��������� �� backlink.
	 * @return �������� ������ �������� ��� null.
	 * @throws DataException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<ObjectId> getCardIdsByBackLink(
			final ObjectId attrId,
			final ObjectId cardId,
			QueryFactory factory, Database database, UserData user
		) throws DataException 
	{
		if (attrId == null || cardId == null || factory == null)
			return null;
		
		// �������� ������...
		final BulkFetchChildrenCards bfcc = new BulkFetchChildrenCards();
		bfcc.setReverseLink(false);
		bfcc.setParentCardIds(Collections.singletonList(cardId));
		bfcc.setLinkAttributeId(attrId);
		
		long t1 = System.nanoTime();
		
		// �������� �������...
		final ActionQueryBase searchQuery = factory.getActionQuery(bfcc);
		searchQuery.setAction(bfcc);
		searchQuery.setUser(user);
		
		BulkFetchChildrenCards.Result actionResult = database.executeQuery(user, searchQuery);
		
		// ����������
		final Map result = actionResult.getCards();

		// �������
		final List<Card> list =  (List<Card>) result.get(cardId);
		
		long t2 = System.nanoTime();
		final Formatter f = new Formatter();
		logger.info(f.format("getBackLinkedCardsObjectIds %2.3f\n", (t1 - t2)/1.0e9));
		
		if(list == null)
			return null;
	
		final Set<ObjectId> listIds = ObjectIdUtils.cardsToObjectIdsSet(list);
		
		if(listIds != null) {
			return new ArrayList<ObjectId>(listIds);
		}
		return null;
	}

	/**
	 * �������� �������������� ������ ������ �� ��������.
	 * @param attr
	 * @return �������� ������ ��� null.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Person> getAttrPersons( PersonAttribute attr)  
	{
		if (attr == null || attr.getValues() == null || attr.getValues().isEmpty())
			return null;
		return attr.getValues();
	}

	/**
	 * �������� �������������� ������ ������ �� �������� ��������.
	 * @param card
	 * @param attrPerson: id Person-��������.
	 * @param throwIfNoAttr: ������������ ���� �������� ��� � ��������, 
	 * true=������� ���������� DataException, false=������� null.
	 * @return �������� ������ ������ ��� null, ���� ��� ������ �������� ��� ������ ������.
	 * @throws DataException 
	 */
	public static Collection<Person> getAttrPersons( Card card, 
			ObjectId attrPerson, boolean throwIfNoAttr
		) throws DataException
	{
		if (card == null || attrPerson == null)
			return null;

		final PersonAttribute pa = (PersonAttribute) card.getAttributeById(attrPerson);
		if (pa == null)
		{
			if (throwIfNoAttr)
				throw new DataException( "jbr.processor.nodestattr_2", 
						new Object[] { "cardId " + card.getId(), attrPerson} );
			return null;
		}
		return getAttrPersons(pa);
	}

	/**
	 * �������� �������������� ������ id �� cardlink-�������� ��������.
	 * @param card
	 * @param attrCardLink: id cardlink(typedlink)-��������.
	 * @param throwIfNoAttr: ������������ ���� �������� ��� � ��������, 
	 * true=������� ���������� DataException, false=������� null.
	 * @return �������� ������ ������ ��� null, ���� ��� ������ �������� ��� ������ ������.
	 * @throws DataException 
	 */
	public static Collection<ObjectId> getAttrLinks( Card card, 
			ObjectId attrCardLink, boolean throwIfNoAttr
		) throws DataException
	{
		if (card == null || attrCardLink == null)
			return null;

		final CardLinkAttribute pa = card.getCardLinkAttributeById(attrCardLink);
		if (pa == null)
		{
			if (throwIfNoAttr)
				throw new DataException( "jbr.processor.nodestattr_2", 
						new Object[] { "cardId " + card.getId(), attrCardLink} );
			return null;
		}
		return getAttrLinks( pa);
	}

	public static Collection<ObjectId> getAttrLinks( Card card, 
			ObjectId attrCardLink) throws DataException
	{
		return getAttrLinks(card, attrCardLink, false);
	}

	public static Collection<ObjectId> getAttrLinks( CardLinkAttribute attr
		)
	{
		if (attr == null || attr.getIdsLinked() == null || attr.getIdsLinked().isEmpty())
			return null;
		return attr.getIdsLinked();
	}

	/**
	 * �������� ������ ������ �� �������� ��������.
	 * @param card
	 * @param attrPerson: id Person-��������.
	 * @return �������� ������ ������ ��� null, ���� ��� ������ �������� ��� ������ ������.
	 * @throws DataException 
	 */
	public static Collection<Person> getAttrPersons( Card card, 
			ObjectId attrPerson
		) throws DataException
	{
		return getAttrPersons(card, attrPerson, false);
	}

	/**
	 * �� ���������� ��������-������ ��������� �������� � ������� ����������.
	 * ���� attributes �����, �� ����� ��������� ����������� ��������.
	 * @param idOrObjList: ������ id �������� ��� ������ ����� ��������.
	 * @param attributes: ������ ��������.
	 * @return ����������� ��������, ���������� ��� ������� ��������� ��������
	 * ��� null, ���� ��� �� ����� ��� ������ ����.
	 * @throws DataException
	 */
	public static List<Card> expandLinks( 
				CardLinkAttribute linkAttr, 
				ObjectId[] attributes,
				QueryFactory factory, Database database, UserData user
			) throws DataException
	{
		if (linkAttr == null) return null;
		return expandLinks( linkAttr.getIdsLinked(), attributes, factory, database, user);
	}


	/**
	 * ����� ��� ��������� �������� ������� �� �������� ��������� � ���������. 
	 * @param cardId: id ��������.
	 * @param destState: id �������� ���������.
	 * @param availableWoves: ���� �� null, ���� ����� �������� ����������� ������ ��������� ���������.
	 * @param factory: ������� ��������.
	 * @param database: ��-accessor.
	 * @param user: ������ ������������ ��� �������.
	 * @return ������� ��� null, ���� ��� ������.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	public static WorkflowMove findWorkFlowMoveX(ObjectId cardId, ObjectId destState,
			Collection<WorkflowMove> availableWoves,
			QueryFactory factory, Database database, UserData user) 
		throws DataException
	{
		if (	cardId == null || destState == null 
				|| factory == null || database == null)
			return null;
		final ChildrenQueryBase query = factory.getChildrenQuery(Card.class, WorkflowMove.class);
		query.setParent(cardId);

		final Collection<WorkflowMove> available = (Collection<WorkflowMove>) 
			database.executeQuery(user, query);

		if (available != null) {
			if (availableWoves != null)
				availableWoves.addAll(available);
			for (Iterator<WorkflowMove> itr = available.iterator(); itr.hasNext(); ) {
				final WorkflowMove item = itr.next();
				if (destState.equals(item.getToState()))
					return item; // (!) FOUND
			}
		}
		return null; // NOT FOUND
	}

	public static WorkflowMove findWorkFlowMove(ObjectId cardId, ObjectId destState,
			QueryFactory factory, Database database, UserData user) 
		throws DataException
	{
		return findWorkFlowMoveX(cardId, destState, null, factory, database, user);
	}


	/**
	 * ���������� ��������� ������ � ��������.
	 * @param destCardIds ������ ������� ��������
	 * @param destPersonOrCardAttrId ������� ������� ��� ������
	 * @param destPersonOrCardIds ������ person id ��� DataObject ��� ����������
	 * (���� destPersonOrCardId ��� 'U', �� ����� person id, 
	 * ���� 'C' �� id ������������ ��������)
	 * @param preclear true, ����� ��������� ������� ������� ��������� (�.�. 
	 * ����� ������� �������, ������� ��� � personIds).
	 * @return ���-�� ����������� ���������� ������� (� ��������� ������������).
	 */
	public static final int insertCardsPersonAttributeValues(Collection<?> destCardIds,
				ObjectId destPersonOrCardAttrId,
				Collection<?> destPersonOrCardIds,
				boolean preclear,
				JdbcTemplate jdbcTemplate,
				Log logger
		)
	{
		logger.trace("About to write changes into database ...");

		if (!hasNonNull(destCardIds)) {
			logger.info("No destination cards -> insert skipped");
			return (-1);
		}

		final boolean noDestData = !hasNonNull(destPersonOrCardIds); // (destPersonOrCardIds == null) || destPersonOrCardIds.isEmpty();

		int cnt = 0;
		final String ids = IdUtils.makeIdCodesEnum(destCardIds, ", ");

		/* 
		 * ���� ������ ���� ��������, �� ��� ��������� � ������� - �������� � �������,
		 * ����� ���� ��������� - �������.
		 */
		final boolean isSingleId = destCardIds.size() == 1;
		String condCardId = "\t\t c.card_id in (\n\t\t"+ ids+ "\n\t\t) --/in \n";
		Object[] args = new Object[] { destPersonOrCardAttrId.getId() };
		int[] argtypes = new int[] {Types.VARCHAR};
		// ���� ����� ����� ���� id, �� ������� ��� �������� ...
		if (isSingleId) {
			condCardId = "\t\t (c.card_id = ?) \n";
			final ObjectId cardId = (ObjectId) destCardIds.iterator().next();
			args = new Object[] { cardId.getId(), destPersonOrCardAttrId.getId() };
			argtypes = new int[] { Types.NUMERIC, Types.VARCHAR };
		}
		final String sPersons = ObjectIdUtils.numericIdsToCommaDelimitedString(destPersonOrCardIds);

		/* �������� ����������� ��������� ... */
		if (!preclear) {
			logger.debug("preclear is off");
		} else {
			final long delStart_ms = System.currentTimeMillis();
			final int cntDel = jdbcTemplate.update(
					"delete from attribute_value c \n" +
					"where \n" +
							condCardId +
					"		and c.attribute_code = ? \n" +
					( (noDestData) 
							? "" 
							: "		and not c.number_value in (" + sPersons + ")"
					)
					, args, argtypes
				);
			final long durationStart_ms = System.currentTimeMillis() - delStart_ms;
			logger.debug( cntDel + " persons pre-deleted from attribute '"
					+ destPersonOrCardAttrId+ "' of card(s) ["+ ids +"] in "
					+ durationStart_ms + " msec"
				);
		}

		/* ������� ��������� ... */
		if (noDestData) {
			logger.info("Empty persons list for card(s) ["+ ids +"] \n\t -> insert skipped");
		} else {
			// TODO: (2010/07/22, RuSA) ����� �������� � log-��������� �� ������, ���� �������� ��� � ������� ��� ������ ��� (invalid attribute_code)
			final long insStart_ms = System.currentTimeMillis();
			cnt = jdbcTemplate.update(
				"insert into attribute_value (card_id, attribute_code, number_value) \n" +
				"	select distinct c.card_id, a.attribute_code \n" +
				"			, (CASE WHEN (a.data_type='U') THEN p.person_id ELSE p.card_id END) \n" +
				"	from card c, attribute a, person p \n" +
				"	where \n" +
						condCardId + // ������� �� card_id
				"		and (a.attribute_code = ?)" +
				"		and ( \n" +
				"			-- ���� �������  ... \n"+
				"			((a.data_type='U') and p.person_id in (" + sPersons + ") )\n" +
				"			-- ��� ������������ �������� ... \n"+
				"			or ((a.data_type='C') and p.card_id in (" + sPersons + ") )\n" +
				"		) --/and \n"+
				"		and not exists(\n" +
				"				select 1 from attribute_value avExists \n" +
				"				where avExists.card_id = c.card_id \n" +
				" 					and avExists.attribute_code = a.attribute_code \n" +
				" 					and avExists.number_value = p.person_id \n" +
				"		) --/and not exists \n"
				, args, argtypes
			);
			final long durationStart_ms = System.currentTimeMillis() - insStart_ms;
			logger.debug( cnt + " records inserted as attribute '"+ destPersonOrCardAttrId 
					+"'\n\t into card(s) ["+ ids + "] \n\t person ids [" 
					+ sPersons + "] in "
					+ durationStart_ms + " msec"
				);
		} 
		return cnt;
	}

    /**
     * �������� ���������� ��� ������������� �� �������� ���������� (�� �� ���������� ���)
     * @param boss ������������ ������� � ��������������
     * @return ������������ ������� � �����������
     */
    public static PersonAttribute retrieveAssistantsByProfile(PersonAttribute boss, JdbcTemplate jdbc){
        if (boss == null) return null;
        PersonAttribute assistants = new PersonAttribute();
        if (boss.isEmpty()) return assistants;
        String bossIds = ObjectIdUtils.numericIdsToCommaDelimitedString(boss.getValues());
        List<?> result = jdbc.queryForList("select assistant.person_id \n" +
                "from person boss, attribute_value av, person assistant\n" +
                "where \n" +
                "av.attribute_code='JBR_ASSISTANT_FOR' \n" +
                "and av.card_id=assistant.card_id \n" +
                "and boss.person_id=av.number_value \n" +
                "and boss.person_id in (" + bossIds + ")");
        if (result == null || result.isEmpty()) return assistants;

        Collection<Person> persons = new ArrayList<Person>(result.size());
        for (Object value : result){
            Map<?, ?> valueMap = (Map<?, ?>) value;
            BigDecimal personId = (BigDecimal) valueMap.get("person_id");
            long assistantId = personId.longValue();
            Person assistant = new Person();
            assistant.setId(assistantId);
            persons.add(assistant);
        }
        assistants.setValues(persons);
        return assistants;
    }

	static boolean hasNonNull( Collection<?> list) {
		if (list != null) {
			for (Iterator<?> i = list.iterator(); i.hasNext();) {
				if (i.next() != null) 
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines weither card is locked.
	 * @param jdbcTemplate {@JdbcTemplate} used for database access.
	 * @param cardId id of {@Card} under the test.
	 * @return true is locked, false otherwise.
	 */
	public static boolean isLocked (JdbcTemplate jdbcTemplate, Object cardId, Long lockedBy){
		if (cardId == null || jdbcTemplate == null || lockedBy == null){
			return false;
		}
		final Long realLockedBy = getLockedBy(jdbcTemplate, cardId);
		return realLockedBy != null && realLockedBy.equals(lockedBy);
	}
	
	private static Long getLockedBy(JdbcTemplate jdbcTemplate, Object cardId) {
		final Long lockedBy = (Long) jdbcTemplate.queryForObject(
				"select locked_by from card where card_id=?",
				new Object[] { cardId },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum)
						throws SQLException {
						if (rs.getObject(1) == null)
							return null;
						return new Long(rs.getLong(1));
					}
				}
			);
		return lockedBy;
	}
	
	/**
	 * ����� �������� �� ����
	 * @param service
	 * @param words - �� �������� ����� �������
	 * @param columnAttrs - ������������������ ��������� ��� �������
	 * @return - ������ ���������� ��������
	 */
	public static List<Card> loadCardsByCode(DataServiceBean service, String words, ObjectId ...columnAttrs) {
		Search search = new Search();
		search.setByCode(true);
		search.setWords(words);
		search.setColumns(CardUtils.createColumns(columnAttrs));
		return execSearchCards(search, service);
	}
	
	/**
	 * ���������� �������� �� cardId
	 * @param cardId
	 * @param factory
	 * @param database
	 * @param user
	 * @return
	 * @throws DataException
	 */
	public static Card loadCard(ObjectId cardId, QueryFactory factory, Database database, UserData user) throws DataException {
		final ObjectQueryBase cardQuery = factory.getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) database.executeQuery(user, cardQuery);
	}

}
