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
import com.aplana.dbmi.action.RebindFileCards;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoRebindFileCards extends ActionQueryBase implements WriteQuery {

	final static String SQL_GET_FILE_CARDS = "-- �������� ����� \n"
			+ "SELECT card_id \n" + "FROM card \n"
			+ "WHERE card_id IN (:CARDS) \n"
			+ "AND template_id IN (:TEMPLATES)";

	final static String SQL_UPDATE_AV = "-- ��������� attribute_value \n"
			+ "UPDATE attribute_value av \n"
			+ "SET card_id = :ROOT, attribute_code = :ATTR \n"
			+ "WHERE av.number_value IN (:FILES) \n"
			+ "AND av.card_id IN (:CARDS)";

	final static String SQL_UPDATE_CARDS = "-- ��������� card \n"
			+ "UPDATE card c " + "SET parent_card_id = :ROOT "
			+ "WHERE c.card_id IN (:FILES)";

	@SuppressWarnings("unchecked")
	private <T> Set<T> getParam(Set<ObjectId> param) throws DataException {
		Set<T> result = new HashSet<T>();
		try {
			if (param != null)
				for (ObjectId attr : param) {
					result.add((T) attr.getId());
				}
		} catch (Exception ex) {
			throw new DataException(
					"one of \"RebindFileCards\" action parameter is NULL. Check setter-methods.", ex);
		}
		return result;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object processQuery() throws DataException {

		RebindFileCards rebind = (RebindFileCards) getAction();
		Set<Long> fileTemplates = getParam(rebind.getTargetTemplateIds());
		Long rootId = new Long(rebind.getDestCardId().getId().toString());

		GetCardsTree tree = new GetCardsTree();
		tree.setCardId(rebind.getDestCardId());
		tree.setLinkAttrs(rebind.getLinkAttrIds());
		tree.setReverse(GetCardsTree.Fields.LINKATTRS, rebind.isExcludeLinkAttrIds());
		tree.setReverse(GetCardsTree.Fields.TEMPLATES, rebind.isExcludeTemplateIds());
		tree.setNesting(rebind.getNesting());
		tree.setTemplates(rebind.getCardTreeTemplates());
		ActionQueryBase query = getQueryFactory().getActionQuery(tree);
		query.setAction(tree);

		long time = System.currentTimeMillis();

		final Collection<Long> total = (HashSet<Long>) getDatabase()
				.executeQuery(getUser(), query);
		if (total.isEmpty()) 
			return null;
		final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(
				getJdbcTemplate());
		final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("CARDS", total, Types.NUMERIC).addValue("TEMPLATES", fileTemplates, Types.NUMERIC);

		
		final Collection<Long> files = new HashSet<Long>();
		files.addAll(jdbc.queryForList(
				SQL_GET_FILE_CARDS, args, Long.class));
		if (files.isEmpty()) 
			return null;
		args.addValue("ROOT", rootId, Types.NUMERIC).addValue("FILES", files, Types.NUMERIC).addValue("ATTR", rebind.getDestAttrId().getId().toString(), Types.VARCHAR);
		// TODO: ����� ��������� ������ ��� �����������, �������� �� ���������, ��� ��� ���� �����, ����� � �������� ����� �� �������������� 
		// ��� ��������� ����� � ������ ��������, ����������� ��������, � ��� ������ �� ����� ��, ��� ������ �� ����� ������ �������� (��� ��� 
		// � ������ ����� �� ��������� ����������� �����)
		jdbc.update(SQL_UPDATE_AV, args);
		jdbc.update(SQL_UPDATE_CARDS, args);

		logger.debug("Total time: "
				+ (time - System.currentTimeMillis()));
		return null;
	}

}
