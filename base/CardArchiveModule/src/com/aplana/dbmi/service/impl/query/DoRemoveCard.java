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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.action.GetCardsTree;
import com.aplana.dbmi.action.RemoveCard;
import com.aplana.dbmi.action.RemoveFile;
import com.aplana.dbmi.archive.CardArchiver;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <b>Query used to perform {@link RemoveCard} action.</b>
 */
public class DoRemoveCard extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	final static String SQL_REMOVE_ATTRIBUTES = " -- ������� �������� (����� ���� � �������� ��������) \n"
			+ "DELETE FROM attribute_value WHERE card_id IN (:CARD_IDS)";

	final static String SQL_REMOVE_ATTRIBUTES_HISTORY = " -- ������� ������� ��������� (����� ���� � �������� ��������) \n"
			+ "DELETE FROM attribute_value_hist WHERE card_id IN (:CARD_IDS)";

	final static String SQL_REMOVE_CARDS = " -- ������� �������� \n"
			+ "DELETE FROM card WHERE card_id IN (:CARD_IDS)";

	final static String SQL_REMOVE_ROOT_CARDS = "-- ������� �������� �������� \n" 
		 	+ "DELETE FROM card WHERE card_id IN (:ROOT_ID)";

	final static String SQL_REMOVE_CARDS_VERSION = " -- ������� �������� �� card_version \n"
			+ "DELETE FROM card_version WHERE card_id IN (:CARD_IDS)";

	final static String SQL_REMOVE_ACTION_LOG = " -- ������� ������� ������� �������� �� action_log \n"
			+ "DELETE FROM action_log WHERE card_id IN (:CARD_IDS)";

	final static String SQL_CHECK_CARDS = " -- ��������� �������� \n"
			+ "SELECT av.card_id FROM attribute_value av \n"
			+ "INNER JOIN attribute a on a.attribute_code = av.attribute_code \n"
			+ "WHERE av.number_value IN (:CARD_IDS) \n"
			+ "AND a.data_type IN ('C','E','F')";
	
	final static String SQL_GET_REPLICATION_CARDS = " -- ��������� �������� ���������� \n"
			+ "SELECT av.card_id FROM attribute_value av \n"
			+ "INNER JOIN attribute a on a.attribute_code = av.attribute_code \n"
			+ "WHERE av.number_value IN (:CARD_IDS) \n"
			+ "AND a.data_type IN ('C','E','F')\n" 
			+ "AND av.template_id = 2400";
	
	final static String SQL_GET_REPLICATION_HISTORY_CARDS = "-- ��������� �������� ������� ����������\n" +
			"SELECT number_value\n" +
			"FROM attribute_value\n" +
			"where card_id in (:CARD_IDS) and attribute_code = 'REPLIC_HIST_ATTR'";
	
	final static String SQL_GET_FILE_CARDS = " -- ������� �������� ����-�������� \n"
			+ "SELECT DISTINCT c.card_id FROM card c \n"
			+ "WHERE c.card_id IN (:CARD_IDS) \n"
			+ "AND template_id IN (:TEMPLATE_IDS)";

    private AccessRuleManager accessManager;

	@SuppressWarnings("unchecked")
	private static <T> Set<T> getParam(Set<ObjectId> param) throws DataException {
		Set<T> result = new HashSet<T>();
		if (param != null)
			for (ObjectId attr : param) {
				result.add((T) attr.getId());
			}
		else
			throw new DataException(
					"one of \"RemoveCard\" action parameter is NULL. Check setter-methods.");
		return result;
	}

    public void setJdbcTemplate(JdbcTemplate jdbc) {
        super.setJdbcTemplate(jdbc);
        accessManager = new AccessRuleManager(jdbc);
    }

	@SuppressWarnings("unchecked")
	@Override
	public Object processQuery() throws DataException {

		final RemoveCard move = getAction();
		// �������� ������
		final Long rootCardId = new Long(move.getCardId().getId().toString());
		final Set<String> savedAttrIds = getParam(move.getSavedAttrIds());
		final Set<Long> fileTemplateIds = getParam(move.getFileTemplateIds());

		GetCardsTree tree = new GetCardsTree();
		tree.setCardId(move.getCardId());
		tree.setIgnoredCards(move.getIgnoredCardIds());
		tree.setLinkAttrs(move.getLinkAttrIds());
		tree.setTemplates(move.getTemplateIds());
		tree.setReverse(GetCardsTree.Fields.LINKATTRS, move.getExclusions()[0]);
		tree.setReverse(GetCardsTree.Fields.TEMPLATES, move.getExclusions()[1]);

		ActionQueryBase query = getQueryFactory().getActionQuery(tree);
		query.setAction(tree);
		final Set<Long> total = getDatabase().executeQuery(getUser(), query);
		
		// ��������� SQL ��������
		final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
				getJdbcTemplate());
		final MapSqlParameterSource args = new MapSqlParameterSource();
		total.add(rootCardId);
		args.addValue("CARD_IDS", total, Types.NUMERIC);
		total.addAll(jdbc.queryForList(SQL_GET_REPLICATION_CARDS, args,
				Long.class));
		total.addAll(jdbc.queryForList(SQL_GET_REPLICATION_HISTORY_CARDS, args,
				Long.class));
		
		total.remove(rootCardId);
		
		CardArchiver cardArchiver = new CardArchiver(getQueryFactory(), getDatabase(), getUser());
		cardArchiver.setRootCardId(rootCardId);
		cardArchiver.setCardsToDelete(total);
		cardArchiver.archive();

		long allStartTimes = System.currentTimeMillis();

		if (total.isEmpty()) {
			logger.info("No cards removed, only root card");
		} else {


			// ��������� ������ �� ������� ��������� � ����� ������� ������ ��������
			if (move.isCheckLinks()) {
				Set<Long> boundCards = new HashSet<Long>();
				boundCards.addAll(jdbc.queryForList(SQL_CHECK_CARDS, args,
						Long.class));
				boundCards.removeAll(total);
				boundCards.remove(rootCardId);
				if (!boundCards.isEmpty()) {
					throw new DataException("jbr.card.remove.tree.error",
							new Object[] { boundCards });
			}
			}

			// ������� ����� ��������
			try {
				args.addValue("TEMPLATE_IDS", fileTemplateIds, Types.NUMERIC);
				final List<Long> fileCards = jdbc.queryForList(
						SQL_GET_FILE_CARDS, args, Long.class);
				if (!fileCards.isEmpty()) {
					for (Long card : fileCards) {
						RemoveFile remove = new RemoveFile();
						remove.setCardId(new ObjectId(Card.class, card));
						ActionQueryBase qu = getQueryFactory().getActionQuery(
								remove);
						qu.setAction(remove);
						getDatabase().executeQuery(getUser(), qu);
						logger.debug("File in Card: " + card
								+ " removed permanently.");
					}
				}

			} catch (Exception ex) {
				logger.error("Error while deleting files.");
				throw new DataException("Error while deleting files. ", ex);
			}

			// ������� ��������
			{
				total.remove(rootCardId);
				try {
					//������� � ������ ������������� access_list ��� ��������, �.�. access_list � attribute_value ������� ������� ������.
					cleanAccessList(total, rootCardId);
				} catch (Exception ex) {
					logger.error("Error while cleaning access list.");
					throw new DataException("Error while cleaning access list. ", ex);
				}
				try {
					jdbc.update(SQL_REMOVE_ATTRIBUTES, args);
				} catch (Exception ex) {
					logger.error("Error while deleting attributes.");
					throw new DataException("Error while deleting attributes. ", ex);
				}
				try {
					updateAccessListToCards(total, rootCardId);
				} catch (Exception ex) {
					logger.error("Error while updating access list.");
					throw new DataException("Error while updating access list. ", ex);
				}
			}

			// ������� ������� �������� ��������� �������� ��������
			{
				try {
					jdbc.update(SQL_REMOVE_CARDS_VERSION, args);
				} catch (Exception ex) {
					logger.error("Error while deleting card version.");
					throw new DataException("Error while deleting card history. ", ex);
				}
				try {
					jdbc.update(SQL_REMOVE_ACTION_LOG, args);
				} catch (Exception ex) {
					logger.error("Error while deleting action log.");
					throw new DataException("Error while deleting card history. ", ex);
				}
				try {
					jdbc.update(SQL_REMOVE_ATTRIBUTES_HISTORY, args);
				} catch (Exception ex) {
					logger.error("Error while deleting attribute history.");
					throw new DataException("Error while deleting card history. ", ex);
				}
			}
			
			// ������� �������� � ��� �� ������
			{
				try {
					cleanAccessList(total, rootCardId);
				} catch (Exception ex) {
					logger.error("Error while cleaning access list.");
					throw new DataException("Error while cleaning access list. ", ex);
				}
				try {
					jdbc.update(SQL_REMOVE_CARDS, args);
				} catch (Exception ex) {
					logger.error("Error while deleting cards.");
					throw new DataException("Error while deleting cards. ", ex);
				}
				try {
					updateAccessListToCards(total, rootCardId);
				} catch (Exception ex) {
					logger.error("Error while updating access list to cards.");
					throw new DataException("Error while updating access list to cards. ", ex);
				}
			}
		}
		
		//������� ������� �������� ��������, ���� ���������
		if(move.isRemoveRootCardHistosy()){
			final MapSqlParameterSource rootArgs = new MapSqlParameterSource();
			Set<Long> rootCardIdsSet = new HashSet<Long>();
			rootCardIdsSet.add((Long)move.getCardId().getId());
			rootArgs.addValue("CARD_IDS", rootCardIdsSet, Types.NUMERIC);
			jdbc.update(SQL_REMOVE_CARDS_VERSION, rootArgs);
			jdbc.update(SQL_REMOVE_ACTION_LOG, rootArgs);
			jdbc.update(SQL_REMOVE_ATTRIBUTES_HISTORY, rootArgs);
		}
		

	
		// ������� ���-���������
		if (move.isRemoveRootCard()) {
			total.add(rootCardId);
		args.addValue("ROOT_ID", rootCardId, Types.NUMERIC);

		try {
				args.addValue("CARD_IDS", rootCardId, Types.NUMERIC).addValue(
						"SAVE_ATTRS", savedAttrIds, Types.VARCHAR);
				String sql = (savedAttrIds.isEmpty()) ? SQL_REMOVE_ATTRIBUTES
						: SQL_REMOVE_ATTRIBUTES + " AND attribute_code NOT IN (:SAVE_ATTRS)";
				cleanAccessList(total, rootCardId);
				jdbc.update(sql, args);
				updateAccessListToCards(total, rootCardId);
				
				if (savedAttrIds.isEmpty()) {
					jdbc.update(SQL_REMOVE_CARDS_VERSION, args);
			}
				sql = (savedAttrIds.isEmpty()) ? SQL_REMOVE_ACTION_LOG
						: SQL_REMOVE_ACTION_LOG + " AND attribute_code NOT IN (:SAVE_ATTRS)";
				jdbc.update(sql, args);
				
				sql = (savedAttrIds.isEmpty()) ? SQL_REMOVE_ATTRIBUTES_HISTORY
						: SQL_REMOVE_ATTRIBUTES_HISTORY + " AND attribute_code NOT IN (:SAVE_ATTRS)";
				jdbc.update(sql, args);
				
				// ������� ��� ���
				if (savedAttrIds.isEmpty()) {
					jdbc.update(SQL_REMOVE_ROOT_CARDS, args);
		}
		
		} catch (Exception ex) {
			logger.error("Error while deleting root attributes.");
				throw new DataException(
						"Error while deleting root attributes. ", ex);
		}
		}
		logger.debug("Time to remove cards: "
				+ (allStartTimes - System.currentTimeMillis()));
		return total;
	}

	/**
	 * Updates access_list to root card and the set of related cards.
	 * @param total set of related card ids.
	 * @param rootCardId root card id.
	 */
	private void updateAccessListToCards(final Collection<Long> total, Long rootCardId) {
		for (Long cardId : total) {
			updateAccessListToCard(cardId);
		}
		updateAccessListToCard(rootCardId);
	}

	private void updateAccessListToCard(Long rootCardId) {
		ObjectId rootObjectId = new ObjectId(Card.class, rootCardId);		
		// � ����� ����������� ��� ��������� ������� ���� ����� ����������� ����� ���������� ���� ����-����������� ������������ �����        
		this.getPrimaryQuery().putCardIdInRecalculateAL(rootObjectId);	
		//accessManager.updateAccessToCard(rootObjectId);
	}

	private void cleanAccessList(final Collection<Long> total, Long rootCardId) {
		cleanAccessListToCard(rootCardId);
		cleanAccessListToCardTree(total);
	}

	private void cleanAccessListToCard(Long rootCardId) {
		ObjectId rootObjectId = new ObjectId(Card.class, rootCardId);
		//accessManager.cleanAccessListByCard(rootObjectId);
		//accessManager.cleanAccessListBySourceCard(rootObjectId);
		accessManager.cleanAccessListByCardAndSourceAttrs(rootObjectId);
	}

	private void cleanAccessListToCardTree(final Collection<Long> total) {
		for (Long cardId : total) {
			cleanAccessListToCard(cardId);
		}
	}

}
