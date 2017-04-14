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

import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.GetCardsTree;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetCardsTree extends ActionQueryBase {

	final static String SQL_GET_FILL_ATTRIBUTES = "-- �������� ������ ��� �������� cardlink-�������� \n"
			+ "SELECT a.attribute_code \n"
			+ "FROM attribute a \n"
			+ "INNER JOIN attribute_value av ON a.attribute_code = av.attribute_code \n"
			+ "WHERE a.attribute_code NOT IN (:LINK) \n"
			+ "AND a.data_type in ('C', 'E', 'F') AND av.card_id IN (:CARD_IDS)"
			+ "UNION\n"
			+ "SELECT a.attribute_code \n"
			+ "FROM attribute a \n"
			+ "INNER JOIN attribute_value av ON a.attribute_code = av.attribute_code \n"
			+ "WHERE a.attribute_code NOT IN (:LINK) \n"
			+ "AND a.data_type in ('C', 'E', 'F') AND av.number_value IN (:CARD_IDS)";

	final static String SQL_GET_ALL_FILL_ATTRIBUTES = "-- �������� ������ ��� �������� cardlink-�������� (���� �� ������ ������������) \n"
			+ "SELECT a.attribute_code \n"
			+ "FROM attribute a \n"
			+ "INNER JOIN attribute_value av ON a.attribute_code = av.attribute_code \n"
			+ "WHERE a.data_type in ('C', 'E', 'F') AND av.card_id IN (:CARD_IDS)"
			+ "UNION\n"			
			+ "SELECT a.attribute_code \n"
			+ "FROM attribute a \n"
			+ "INNER JOIN attribute_value av ON a.attribute_code = av.attribute_code \n"
			+ "WHERE a.data_type in ('C', 'E', 'F') AND av.number_value IN (:CARD_IDS)";


	final static String SQL_GET_CARDS_LEVEL = "-- �������� ���� ���� \n"
			+ "SELECT DISTINCT av.number_value \n"
			+ "FROM attribute_value av \n"
			+ "INNER JOIN card c ON c.card_id = av.number_value \n"
			+ "WHERE av.attribute_code IN (:LINK) -- ������� ����� \n"
			+ "AND av.card_id IN (:CARD_IDS) \n"
			+ "AND c.template_id NOT IN (:TEMPLATE_IDS)\n"
			+ "UNION\n"
			+ "SELECT DISTINCT av.card_id \n"
			+ "FROM attribute_value av \n"
			+ "INNER JOIN card c ON c.card_id = av.card_id \n"
			+ "WHERE av.attribute_code IN (:LINK) -- ������� ����� \n"
			+ "AND av.number_value IN (:CARD_IDS) \n"
			+ "AND c.template_id NOT IN (:TEMPLATE_IDS)";

	final static String SQL_GET_ALL_CARDS_LEVEL = "-- �������� ���� ���� ����� �������� \n"
			+ "SELECT DISTINCT av.number_value \nFROM attribute_value av \n"
			+ "WHERE av.attribute_code IN (:LINK) \n"
			+ "AND av.card_id IN (:CARD_IDS) \n"
			+ "UNION\n"
			+ "SELECT DISTINCT av.card_id \nFROM attribute_value av \n"
			+ "WHERE av.attribute_code IN (:LINK) \n"
			+ "AND av.number_value IN (:CARD_IDS)";

	final static String PREFIX = "DoGetCardsTree@ ";
	
	@SuppressWarnings("unchecked")
	private <T> Set<T> getParam(Set<ObjectId> param) throws DataException {
		Set<T> result = new HashSet<T>();
		if (param != null)
			for (ObjectId attr : param) {
				result.add((T) attr.getId());
			}
		else
			throw new DataException(
					"One of \"GetTreeCards\" action parameter is NULL. Check setter-methods.");
		return result;

	}

	@Override
	public Object processQuery() throws DataException {
		GetCardsTree action = (GetCardsTree) getAction();
		
		long time = System.currentTimeMillis();
		
		final Long rootId = new Long(action.getCardId().getId().toString());
		final Set<Long> templates = this.<Long> getParam(action.getTemplates());
		final Set<String> attrs = this.<String> getParam(action
				.getLinkAttrs());
		final Set<Long> ignoredCards = this.<Long> getParam(action.getIgnoredCards());

		final Collection<Long> total = new HashSet<Long>();
		total.add(rootId);
		final Collection<Long> result = new HashSet<Long>();
		//nesting = 0 (���, ����� �������� �����)
		if (action.getNesting() < 0  )
			result.add(rootId);
		// ������� ����
		final Collection<Long> currentLevel = new HashSet<Long>();
		// C-�������� ��������
		final Collection<String> linkAttrs = new HashSet<String>();
		
		final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
				getJdbcTemplate());
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("CARD_IDS", currentLevel, Types.NUMERIC)
				.addValue("TEMPLATE_IDS", templates, Types.NUMERIC)
				.addValue("LINK", linkAttrs, Types.VARCHAR);

		// ����������� �������
		final String GET_CARD_LEVEL;
		if (templates.isEmpty())
			GET_CARD_LEVEL = SQL_GET_ALL_CARDS_LEVEL;
		else if (action.isReverse(GetCardsTree.Fields.TEMPLATES)) {
			GET_CARD_LEVEL = SQL_GET_CARDS_LEVEL;
		} else
			GET_CARD_LEVEL = SQL_GET_CARDS_LEVEL.replaceAll("NOT IN", "IN");
		
		currentLevel.add(rootId);
		int nest = 0;
		while (!currentLevel.isEmpty()) {
			linkAttrs.clear();
			linkAttrs.addAll(attrs);
			
			// ����������� C-���������
			if (action.isReverse(GetCardsTree.Fields.LINKATTRS)) {
				final String SQL_GET_ATTRIBUTES;
				if (!attrs.isEmpty()) 				
					SQL_GET_ATTRIBUTES = SQL_GET_FILL_ATTRIBUTES;
				else
					SQL_GET_ATTRIBUTES = SQL_GET_ALL_FILL_ATTRIBUTES;
				
				@SuppressWarnings("unchecked")
				final Collection<String> levelAttrs = jdbc.queryForList(SQL_GET_ATTRIBUTES, args,
						String.class);
				linkAttrs.clear();
				linkAttrs.addAll(levelAttrs);
			} 
			if (!linkAttrs.isEmpty()) {	
				// ���������� ������� ����
				@SuppressWarnings("unchecked")
				final Collection<Long> level = jdbc.queryForList(
						GET_CARD_LEVEL, args, Long.class);
				level.removeAll(total);
				currentLevel.clear();
				currentLevel.addAll(level);
				total.addAll(currentLevel);				
				if (action.getNesting() <= nest++)
					result.addAll(currentLevel);
			} else 
				currentLevel.clear();			

		}
		
		// ������������ �����
		result.removeAll(ignoredCards);
		
		logger.debug(PREFIX + "Runtime: " +(time - System.currentTimeMillis()));
		return result;
	}
}
