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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.utils.StrUtils;

// TODO: (2010/09/27, RuSA) ������ ��� U-�������� ���������� � ������������ �� ��� ������ ������ ��������. �� ������ �� U-������� ��� ����� �� �������� ?!
/**
 * (2010/09/18, RuSA) 
 * ����������� U-�������� �� ��������� �������� � ������, � ����������� ���
 * ������� ������������.
 * (!) ����� ������ ������������� ������������� ������ ��� ����-����������.
 * 
 * ���������:
 *		linkAttrId: (������������) cardlink-������� �������� ��������, ��� 
 * ��������� ������ ��������� ��������, �� ������� ����� ����������� �������;
 *
 *		destPersonAttrId: user-������� �������� ��������, ��� ������ �������������
 * �� ��������� �������� (��� recurseAttrId != null, ����� ������� ������� 
 * ��������� ��������);
 *  (!) �� 2010/09/18 �� ��������� �������� ������ ���������� ��� user-��������.
 *
 *		recurseAttrId: ���� �����, �� ��� �������, ����� ������� ��������� ���������� 
 *  (������� � ��������) �������� "������������" �������� � ��������� � ���
 *  ������ ���������. ���� null, �� �������������� ������ �������� ��������.
 *
 *		enSummaryPersonList: ���� ������� ���������� ������ ������ ��� ��������.
 * ������������ ������ ��� recurseLinkAttrId != null:
 *	false = ������ ������ ������ �������� ����������� ������ �� ��������������� 
 *��������� � ��� ��������,
 *	true (��-����) = ��������� ���������� ������� ������ ���� ��������� ����� 
 * ����� ��������, ������� ������ �� ���� ����� ��������, � ��������� �� ���
 * �������� ������. 
 *
 */
// TODO: �������� ����������� ������� ��������� srcPersonIds ������ id ��������� �� ��������� �������� ��� ��������� ������ ������.
public class CopyMassOfPersonsFromCL
	extends AbstractCopyPersonProcessor
{
	/**
	 * SQL ��������� ��� �������� �������� �������� � ��� ���������� �����
	 */
	private static final String SQLARG_DEST_USER_ATTR = "DEST_USER_ATTR";
	private static final String SQLARG_CLINK_ATTR = "CLINK_ATTR";

	private static final long serialVersionUID = 1L;

	/**
	 * Id �������� user-��������
	 */
	private ObjectId destPersonAttrId;

	/**
	 *  true=�������� ������� ������ ��������������, 
	 *  false (�� ����)=�������� (��� ������������),
	 */
	private boolean preClearDestList = false;

	/**
	 * �������, ����� ������� ���������� ����� ���������� "������������" ������.
	 * null = �������� �� �����.
	 */
	private ObjectId recurseLinkAttrId = null;

	static final boolean DEFAULT_enSummaryPersonList = true;
	/**
	 * ���� ������� ���������� ������ ������ ��� ��������.
	 */
	private boolean enSummaryPersonList = DEFAULT_enSummaryPersonList;
	
	static final int UNLIMITED_LEVELS = -1;
	/**
	 * ����������� �� ���-�� ������� � ������ ���������� ��� ������ ������� ������.
	 * ���� <0, �� ����������� ���.
	 */
	private int maxLevels = UNLIMITED_LEVELS;

	/* PLAN:
	private List<ObjectId> srcPersonIds;
	private List<ObjectId> srcCardLinkPersonIds;
	private ObjectId linkedCardsStateId;
	 */


	@Override
	public void setParameter(String name, String value) {

		if ("destPersonAttrId".equalsIgnoreCase(name)) {
			this.destPersonAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		}

		else if ("preClearDestList".equalsIgnoreCase(name)) {
			this.preClearDestList = StrUtils.stringToBool( value, false);
		}

		else if ("recurseBkLink".equalsIgnoreCase(name)) {
			this.recurseLinkAttrId = IdUtils.smartMakeAttrId( value, BackLinkAttribute.class);
		}

		else if ("enSummaryPersonList".equalsIgnoreCase(name)) {
			this.enSummaryPersonList = StrUtils.stringToBool( value, DEFAULT_enSummaryPersonList);
		}

		else if ("maxLevels".equalsIgnoreCase(name)) {
			this.maxLevels = Integer.parseInt( value);
		}

//		else if ("addPersonAttrIds".equalsIgnoreCase(name)) {
//			this.addPersonAttrIds = stringToAttrIds(PersonAttribute.class, value);
//		}

		else {
			super.setParameter(name, value);
		}
	}


	/*
	 * �������� �������� � U-������� (:DEST_USER_ATTR) � ��������� (%CARD_IDS%),
	 * ���� ���� ������ �� ��������� (�� �������� :CLINK_ATTR) ��������, ��������
	 * ������������. ���������� � ������ "��������� ����������": ��� ���������
	 * ���������� ��������� ����� ����� ��������� ������������� �� �������� 
	 * ��������� (�.�. ������ �� ���� ����� ��������� C/E-������� ����������
	 * ��������� 'JBR_DOCL_RELATDOC').
	 * 	1) %CARD_IDS% ������ id �������� / �������� �������� (��� ���������������);
	 * 	2) :CLINK_ATTR ��� �-�������� ����� ('JBR_DOCL_RELATDOC');
	 * 	3) :DEST_USER_ATTR ��� U-�������� ��� ������ ('JBR_RD_FROM_RELDOCS').
	 */
	final static String SQL_GET_PERSONS_BY_LINK = 
		"SELECT DISTINCT avThis.card_id \n" +
		"		, destA.attribute_code -- $DEST_USER_ATTR \n" +
		"		, avChildUsers.number_value -- person_id \n " +
		"FROM attribute_value avThis \n" +
		"		-- ��� U-�������� ��������, ��������� �� ������ ($CLINK_ATTR)... \n" +
		"		JOIN attribute_value avChildUsers \n" +
		"			ON avChildUsers.card_id=avThis.number_value \n" +
		"			AND avThis.attribute_code= (:CLINK_ATTR) \n" + // -- 'JBR_DOCL_RELATDOC'
		"			AND avThis.card_id in (%CARD_IDS%) \n" + // �������� ��� ������� string-������
		"		JOIN attribute aUser \n" +
		"			ON aUser.attribute_code=avChildUsers.attribute_code \n" +
		"			AND aUser.data_type = 'U' \n" +
		"\n" +
		// �������� ������� destA, ������ ����� �� ���� ���� ���������� ���������� � ������� ...
		"		-- ������� ������� (������������-����) \n" +
		"		, (SELECT cast( (:DEST_USER_ATTR) as VARCHAR(20)) as attribute_code \n" +
		"		) as destA(attribute_code) \n" +
		"\n"
	;
	final static String SQL_PREDELETE_ONE_LEVEL_OLD = 
		"DELETE FROM attribute_value avExists \n" +
		"WHERE \n" +
		"		avExists.card_id in (%CARD_IDS%) \n" + // �������� ��� ������� string-������
		"		AND avExists.attribute_code = (:DEST_USER_ATTR) \n" +
		"		-- ��������� ��� ���������� ������� ��������... \n" +
		"		AND NOT Exists ( \n" +
		"			SELECT 1 \n" +
		"			FROM attribute_value avThis \n" +
		"				-- ��� U-�������� ��������, ��������� �� ������ ($CLINK_ATTR)... \n" +
		"				JOIN attribute_value avChildUsers \n" +
		"					ON avChildUsers.card_id=avThis.number_value \n" +
		"					AND avThis.attribute_code= (:CLINK_ATTR) \n" + // -- 'JBR_DOCL_RELATDOC'
		"					AND avThis.card_id in (%CARD_IDS%) \n" + // �������� ��� ������� string-������
		"				JOIN attribute aUser \n" +
		"					ON aUser.attribute_code=avChildUsers.attribute_code \n" +
		"					AND aUser.data_type = 'U' \n" +
		"			WHERE \n" +
		"				avExists.number_value = avChildUsers.number_value \n" +
		"		) -- /AND NOT EXISTS \n"
	;

	final static String SQL_INSERT_ONE_LEVEL_NO_DUPS =
		"INSERT INTO attribute_value ( card_id, attribute_code, number_value ) \n" +
		SQL_GET_PERSONS_BY_LINK +
		"WHERE \n" +
		"	-- ��������� ��� ������������ ������� ��������... \n" +
		"	NOT Exists ( \n" +
		"			SELECT 1 \n" +
		"			FROM attribute_value avExists \n" +
		"			WHERE 	avExists.card_id = avThis.card_id \n" +
		"					AND avExists.attribute_code = destA.attribute_code  -- ($DEST_USER_ATTR) \n" +
		"					AND avExists.number_value = avChildUsers.number_value \n" +
		"	) -- /NOT EXISTS \n"
	;

	/**
	 * ��������� cardlink, ����-�� bklink ��������.
	 */
	final static String SQL_GET_CARD_LINK_BY_BKLINK = 
		"SELECT ao.option_value \n" + 
		"FROM attribute_option ao \n" +
		"WHERE 	ao.option_code='LINK' \n"+
		"		and ao.attribute_code=? \n"
	;

	/**
	 * ��������� ����� ������ ������ ����� � ���� �� ����������������� �����.
	 * 	1) %CARD_IDS% ������ id �������� (��� ���������������);
	 * 	2) :CLINK_ATTR ��� �-�������� ����� � ����� ('JBR_DOCL_RELATDOC');
	 * 	3) :CLINK_PARENT_ATTR ��� �-�������� ����� �� ��������� ('JBR_DOCL_RELATDOC');
	 */
	final static String SQL_GET_CARDS_LEVEL_UP_AND_DOWN =
		"-- ����� ������� \"����\": ���������������� ������ �� ������� ������ ��-������ \n" +
		"SELECT 	avDown.number_value \n" +
		"FROM	attribute_value avDown \n" +
		"WHERE	avDown.attribute_code=(:CLINK_ATTR) -- ������� ����� \n" +
		"	and avDown.card_id in (%CARD_IDS%)  -- ������� ������� ��������� �� ����� \n" +
		"UNION \n" +
		"-- ����� ������� \"�����\": ������ �� ������ ������� \n"+
		"SELECT 	avUp.card_id \n" + 
		"FROM 	attribute_value avUp \n" +
		"WHERE	avUp.attribute_code=(:CLINK_PARENT_ATTR) -- ������� ����� \n" +
		"	and avUp.number_value in (%CARD_IDS%)  -- ���������� ������� ��������� �� ������ \n"
	;

	/*
	 * �������� �������� � U-�������� (:DEST_USER_ATTR) � ��������� (%CARD_IDS%),
	 * ���� ���� ������ �� ���� �� ��������, �������� ������������. 
	 * 	1) %CARD_IDS% ������ id �������� ��� ���������������;
	 * 	2) :DEST_USER_ATTR ��� U-�������� ��� ������ ('JBR_RD_FROM_RELDOCS').
	 */
	final static String SQL_SELECT_ALL_CARDS_USERS = 
		"			SELECT DISTINCT(avTree.number_value) \n" +
		"			FROM attribute_value avTree \n" +
		"			WHERE avTree.attribute_code IN ( \n"+
		"						-- ��� U-�������� �������� ... \n" +
		"						SELECT attribute_code FROM attribute \n"+ 
		"						WHERE data_type = 'U' AND attribute_code != :"+ SQLARG_DEST_USER_ATTR + " \n" +
		"					) --/IN \n" +
		"				AND avTree.card_id in ( \n" +
		"						%CARD_IDS% \n" + // �������� ��� ������� string-������
		"				) --/in \n" 
		;
	final static String SQL_PREDELELET_ALL_CARDS_OLD_USERS =
		"DELETE FROM attribute_value avExists \n" +
		"WHERE \n" +
		"	avExists.card_id in ( \n" +
		"			%CARD_IDS% \n" + // �������� ��� ������� string-������
		"	) --/in \n" +
		"	AND avExists.attribute_code = :"+ SQLARG_DEST_USER_ATTR + " \n" + // $DEST_USER_ATTR
		"	AND avExists.number_value NOT IN ( \n" +
					SQL_SELECT_ALL_CARDS_USERS +
		"	) --/NOT IN \n"
		;
	final static String SQL_INSERT_ALL_CARDS_USERS =
		"INSERT INTO attribute_value ( card_id, attribute_code, number_value ) \n" +
		"\n" +
		"SELECT DISTINCT c.card_id \n" +
		"		, :"+ SQLARG_DEST_USER_ATTR + " \n" + // $DEST_USER_ATTR
		"		, usr.person_id \n " + 
		"FROM card c, \n" +
		"		(	 \n" +
					SQL_SELECT_ALL_CARDS_USERS +
		"		) as usr(person_id) \n"+
		"WHERE \n" +
		"	c.card_id in ( \n" +
		"			%CARD_IDS% \n" + // �������� ��� ������� string-������
		"	) --/in \n" +
		"	-- ��������� ��� ������������ ������� ��������... \n" +
		"	AND NOT Exists ( \n" +
		"			SELECT 1 \n" +
		"			FROM attribute_value avExists \n" +
		"			WHERE 	avExists.card_id = c.card_id \n" +
		"					AND avExists.attribute_code = :"+ SQLARG_DEST_USER_ATTR + " \n" +
		"					AND avExists.number_value = usr.person_id \n" +
		"	) \n"
	;

	@Override
	public Object process() throws DataException 
	{
		if (destPersonAttrId == null || super.getLinkAttrId() == null) {
			logger.warn( " Processor is not configured completely: check destPersonAttrId/linkAttrId -> exiting");
			return null;
		}

		final ObjectId cardId = super.getCardId();
		if (cardId == null || cardId.getId() == null) {
			logger.warn( "This processor can be applied only for saved cards (current card is NULL) -> exiting");
			return null;
		}

		long startTime = System.currentTimeMillis();
		try {
			logger.info(" maxLevels is "+ this.maxLevels);
			if (recurseLinkAttrId != null && enSummaryPersonList)
				processCardsGlobally( cardId);
			else 
				processCardsLocally( cardId);
		} finally {
			final long duration = System.currentTimeMillis() - startTime;
			logger.debug("Executing time: " + duration);
		}

		return null;
	}

	/**
	 * ��������� ���� ��������� �������� ������������ - "���� ��������� ��".
	 * @param cardId 
	 * @throws DataException 
	 */
	private void processCardsGlobally(ObjectId startCardId) throws DataException {
		/* 
		 * ���� ������ �������� (�������� ����������), 
		 * ��������� ����� ������� recurseLinkAttrId...
		 */
		final Set<ObjectId> allCards = collectAllCards(startCardId, getLinkAttrId(), getRecurseLinkAttrId(), getMaxLevels());
		if (allCards == null || allCards.size() < 2) {
			// ���� �������� ��� ����� ����� - ������� ...
			logger.info("No linked cards found for active card "+ startCardId.getId()+ " -> exiting" );
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Active card "+ startCardId.getId() 
					+ " has full linked cards set as (count="+ allCards.size() 
					+") [" + allCards + "]"
				);
		}

		/* ������� ������� ��������� � �������� �������� ...
		 */
		/*
		if (preClearDestList) {
			CardUtils.dropAttributes( getJdbcTemplate(), 
					new Object[] { 
						destPersonAttrId.getId(),
						Attribute.ID_CHANGE_DATE.getId()
					}, allCards
				);
		};
		markChangeDate( allCards, !preClearDestList);
		*/

		/* ������� ������� ���������...
		 */
		markChangeDate( allCards, true);

		final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
		final MapSqlParameterSource args = new MapSqlParameterSource();

		args.addValue( SQLARG_DEST_USER_ATTR, destPersonAttrId.getId(), Types.VARCHAR)
			.addValue( SQLARG_CLINK_ATTR, getLinkAttrId().getId(), Types.VARCHAR);

		final String cardsIds = ObjectIdUtils.numericIdsToCommaDelimitedString(allCards);

		ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>(allCards.size());
		try {
			//�� ���������
			for (ObjectId id : allCards) {
				execAction(new LockObject(id));
				lockedCards.add(id);
			}
			/* smart-�������� ���������� ������ ...
			 */
			if (preClearDestList)
			{
				final long msecStartDelete = System.currentTimeMillis();
				final String sql = SQL_PREDELELET_ALL_CARDS_OLD_USERS.replaceAll("%CARD_IDS%", cardsIds);

				final int iDel = jdbc.update(sql, args);
				if (logger.isInfoEnabled()) {
					final long msecDurationDelete = System.currentTimeMillis() - msecStartDelete;
					logger.info( "deleted in " + msecDurationDelete+ " msec "
						+ iDel + " old records from attribute '"
						+ destPersonAttrId 
						+ "' of cards (count="+ allCards.size() +") ["+ cardsIds + "]"
					);
				}
			}

			/* smart-���������� ������������� ������ ...
			 */
			{
				final long msecStartUpdate = System.currentTimeMillis();
				final String sql = SQL_INSERT_ALL_CARDS_USERS.replaceAll("%CARD_IDS%", cardsIds);
				
				final int iupd = jdbc.update(sql, args);
				if (logger.isInfoEnabled()) {
					final long msecDurationUpdate = System.currentTimeMillis() - msecStartUpdate;
					logger.info( "inserted in " + msecDurationUpdate+ " msec "
						+ iupd + " records into the attribute '"
						+ destPersonAttrId 
						+ "' of cards (count="+ allCards.size() +") ["+ cardsIds + "]"
					);
				}
			}
		} finally {
			//�� ������������
			for (ObjectId id : lockedCards) {
				execAction(new UnlockObject(id));
			}
		}
	}

	/**
	 * ��������� ��������� ��������: �� ������� �� cardlink-������ "����" �� �����...
	 * @throws DataException 
	 */
	private void processCardsLocally(ObjectId startCardId) throws DataException 
	{

		// ������� ������ "��������" ��������...
		Set<ObjectId> currentCardIds = new HashSet<ObjectId>();
		currentCardIds.add(startCardId);

		int maxScanLevels = this.getMaxLevels();
		while (!currentCardIds.isEmpty())
		{
			/*
			// ����� ������� ��������� � �������� �������� ...
			if (preClearDestList) {
				CardUtils.dropAttributes( getJdbcTemplate(), 
						new Object[] { 
					destPersonAttrId.getId(),
					Attribute.ID_CHANGE_DATE.getId()
				}, 
				currentCardIds);
			};

			// ������� ������� ���������...
			markChangeDate( currentCardIds, !preClearDestList);
			 */

			// ������� ������� ���������...
			markChangeDate( currentCardIds, true);

			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			final MapSqlParameterSource args = new MapSqlParameterSource();
			args.addValue(CopyMassOfPersonsFromCL.SQLARG_DEST_USER_ATTR, destPersonAttrId.getId(), Types.VARCHAR)
				// .addValue("CARD_ID", cardId.getId(), Types.NUMERIC)
				.addValue(CopyMassOfPersonsFromCL.SQLARG_CLINK_ATTR, getLinkAttrId().getId(), Types.VARCHAR);

			final String cardsIds = ObjectIdUtils.numericIdsToCommaDelimitedString(currentCardIds);

			ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>(currentCardIds.size());
			try {
				//�� ���������
				for (ObjectId id : currentCardIds) {
					execAction(new LockObject(id));
					lockedCards.add(id);
				}
				/* 
				 * smart-��������
				 */
				if (preClearDestList)
				{
					final long msecStartDelete = System.currentTimeMillis();
					final String sqlDel = SQL_PREDELETE_ONE_LEVEL_OLD.replaceAll("%CARD_IDS%", cardsIds);
					final int iDel = jdbc.update(sqlDel, args);
					if (logger.isInfoEnabled()) {
						final long msecDurationDelete = System.currentTimeMillis() - msecStartDelete;
						logger.info( "deleted in " + msecDurationDelete+ " msec "
							+ iDel + " old records from attribute '"
							+ destPersonAttrId + "' of cards ["+ cardsIds + "]"
						);
					}
				}


				/* 
				 * smart-insert
				 */
				{
					final long msecStartUpdate = System.currentTimeMillis();
					final String sqlIns = SQL_INSERT_ONE_LEVEL_NO_DUPS.replaceAll("%CARD_IDS%", cardsIds);
					final int iupd = jdbc.update(sqlIns, args);
					if (logger.isInfoEnabled()) {
						final long msecDurationUpdate = System.currentTimeMillis() - msecStartUpdate;
						logger.info( "inserted in " + msecDurationUpdate+ " msec "
							+ iupd + " records into the attribute '"
							+ destPersonAttrId + "' of cards ["+ cardsIds + " ]"
						);
					}
				}
			} finally {
				//�� ������������
				for (ObjectId id : lockedCards) {
					execAction(new UnlockObject(id));
				}
			}

			if (recurseLinkAttrId == null)
				return;

			if ( maxScanLevels-- == 0) {
				logger.info( "scan breaked due to maxLevel limit = "+ this.getMaxLevels());
				break; // while
			}

			/* 
			 * ��������� ������ "�������������" ������ ...
			 */

			// �������� �������� recurseLinkAttrId ��������...
			final Search search = CardUtils.getFetchAction(cardsIds, new ObjectId[] {recurseLinkAttrId} );
			final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser()); 

			currentCardIds.clear();
			if (cards != null) {
				for (Card card : cards) {
					final Collection<ObjectId> list = getAllLinkedIdsByAttr(card, recurseLinkAttrId, getSystemUser());
					if (list != null)
						currentCardIds.addAll(list);
				}
			}
		} // while
	}

	@SuppressWarnings("unchecked")
	public Collection<ObjectId> getAllLinkedIdsByAttr( final Card card, 
			final ObjectId attrId, final UserData user
		) throws DataException
	{
		if (card == null || attrId == null) 
			return null;

		final Attribute attr = card.getAttributeById(attrId);
		Collection<ObjectId> result = null;
		if (CardLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			final CardLinkAttribute clink = (CardLinkAttribute) attr;
			if (clink.getIdsLinked() != null && !attr.isEmpty())
				result = CardUtils.getAttrLinks(clink);
		} else if (BackLinkAttribute.class.isAssignableFrom(attr.getClass())) {
			// �������� ids ��� back-link ��������...
			final List<Card> rslist = CardUtils.getCardsList(execListProject(
					card.getId(), attr.getId(), user));
			if (rslist != null)
				result = ObjectIdUtils.collectionToSetOfIds(rslist);
		}
		return result;
	}

	private SearchResult execListProject( ObjectId cardId, ObjectId backLinkAttrId, 
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);

		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		final SearchResult rs = (SearchResult) getDatabase().executeQuery( user, query);
		return rs;
	}

	/**
	 * ������� ��� ��������� �� �������� linkAttrId �������� � ���� ������.
	 * ����������� ��������� ����������������.
	 * ���� ���������������� ��������� ������ ������: "���� ������� �����-����" 
	 * �� ������. ��� �������� "��������" ����� � ������ ��������� ��������������.
	 * @param startCardId: ��������� �������� ������������� ������.
	 * @param linkAttrId: ������� ����� (������ �������� � ���� ������ - ���� 
	 * ������� "����").
	 * @param maxLevels: ����� ����������� �� ���-�� "�������" ��� ������ ������.
	 * ���� <0, �� ������������.
	 * @return ������ ��������� ��������.
	 */
	@SuppressWarnings("unchecked")
	private Set<ObjectId> collectAllCards(ObjectId startCardId, ObjectId childAttrId,
			ObjectId parentLinkId, int maxScanLevels)
	{
		if (startCardId == null || startCardId.getId() == null
			|| childAttrId == null || childAttrId.getId() == null
			|| parentLinkId == null || parentLinkId.getId() == null
			)
			return null;

		// ��������� = ������ ������
		final Set<Long> total = new HashSet<Long>(10);
		final long msecStart = System.currentTimeMillis();

		// ���� ���������
		final String childAttrCode = childAttrId.getId().toString();
		String parentAttrCode = parentLinkId.getId().toString();
		if (BackLinkAttribute.class.isAssignableFrom(parentLinkId.getType())) {
			// ��������� C-��������, ����-�� BkLink-�������� ...
			parentAttrCode = (String) getJdbcTemplate().queryForObject(
					SQL_GET_CARD_LINK_BY_BKLINK, 
					new Object[] { parentAttrCode }, 
					new int[] {Types.VARCHAR}, 
					String.class );
			
		}

		// ������� ������ - ���� ����� � ���� ����.
		final Set<Long> currentLevelCardIds = new HashSet<Long>(10);
		currentLevelCardIds.add( (Long) startCardId.getId());
		while (!currentLevelCardIds.isEmpty()) 
		{
			total.addAll(currentLevelCardIds);
			if ( maxScanLevels-- == 0) {
				logger.info( "scan breaked due to maxLevel limit = "+ this.getMaxLevels());
				break; // while
			}

			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			final MapSqlParameterSource args = new MapSqlParameterSource();
			args.addValue(CopyMassOfPersonsFromCL.SQLARG_CLINK_ATTR, childAttrCode, Types.VARCHAR)
				.addValue("CLINK_PARENT_ATTR", parentAttrCode, Types.VARCHAR)
				;

			final String cardsIds = longToCommaDelimitedString(currentLevelCardIds);
			// final String cardsIds = currentLevelCardIds.toString();
			final String sql = SQL_GET_CARDS_LEVEL_UP_AND_DOWN.replaceAll("%CARD_IDS%", cardsIds);

			// ������ ��������� ������� ������������ �����-���� (����� ����� 
			// ���������� ���������� ��������� ����������� �����������) ... 
			List<Long> level = null;
			try {
				level = jdbc.queryForList( sql, args, Long.class );
			} catch (EmptyResultDataAccessException e) {
				break;
			}

			// ������������ ������������ ������ ��� ��� ����������� id ...
			currentLevelCardIds.clear();
			for (Long itemId : level) {
				if (itemId ==  null || itemId.longValue() == 0)
					continue;
				if (total.contains(itemId)) // ��� ����...
					continue;
				currentLevelCardIds.add(itemId);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("got next level cards as (count="
						+ currentLevelCardIds.size() +") ["
						+ currentLevelCardIds + "]");
			}
		} // while

		final Set<ObjectId> result = new HashSet<ObjectId>(total.size());
		for (Long i : total) {
			result.add( new ObjectId( Card.class, i.longValue() ));
		}
		if (logger.isInfoEnabled()) {
			final long msecDuration = System.currentTimeMillis() - msecStart;
			logger.info("collected "+ result.size() +" cards in "+ msecDuration+ " msec");
		}
		return result;
	}

	/**
	 * @param currentLevelCardIds
	 * @return
	 */
	private String longToCommaDelimitedString(Set<Long> ids) {
		if (ids == null || ids.isEmpty())
			return "-1";
		final StringBuffer result = new StringBuffer();
		for ( Iterator<Long> i = ids.iterator(); i.hasNext(); ) 
		{
			final Long obj = i.next();
			if (obj == null) continue;
			result.append( obj.longValue() );
			if (i.hasNext())
				result.append(',');
		}
		return result.toString();
	}

	/**
	 * @return the destPersonAttrId
	 */
	public ObjectId getDestPersonAttrId() {
		return this.destPersonAttrId;
	}

	/**
	 * @param destPersonAttrId the destPersonAttrId to set
	 */
	public void setDestPersonAttrId(ObjectId destPersonAttrId) {
		this.destPersonAttrId = destPersonAttrId;
	}

	/**
	 * @return the preClearDestList
	 */
	public boolean isPreClearDestList() {
		return this.preClearDestList;
	}

	/**
	 * @param preClearDestList the preClearDestList to set
	 */
	public void setPreClearDestList(boolean preClearDestList) {
		this.preClearDestList = preClearDestList;
	}

	/**
	 * @return the recurseLinkAttrId
	 */
	public ObjectId getRecurseLinkAttrId() {
		return this.recurseLinkAttrId;
	}

	/**
	 * @param recurseLinkAttrId the recurseLinkAttrId to set
	 */
	public void setRecurseLinkAttrId(ObjectId recurseLinkAttrId) {
		this.recurseLinkAttrId = recurseLinkAttrId;
	}

	/**
	 * @return the enSummaryPersonList
	 */
	public boolean getEnSummaryPersonList() {
		return this.enSummaryPersonList;
	}

	/**
	 * @param value the enSummaryPersonList to set
	 */
	public void setEnSummaryPersonList(boolean value) {
		this.enSummaryPersonList = value;
	}

	public int getMaxLevels() {
		return this.maxLevels;
	}

	public void getMaxLevels(int value) {
		this.maxLevels = value;
	}
}
