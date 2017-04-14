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

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Query used to perform {@link ListProject} action 
 */
public class DoListLinkedCards extends ActionQueryBase {
	private static final String ID_NAME = "project.name";

	/**
	 * @return {@link SearchResult} object representing list of cards which references given
	 * {@link Card} object via one of {@link CardLinkAttribute card link attributes}
	 */
	@SuppressWarnings("unchecked")
	public Object processQuery() throws DataException {
		final ManagerTempTables mgrTmpTables = new ManagerTempTables(getJdbcTemplate());
		List<ObjectId> cardIds = null;
		
		try {
			final ListProject action = getAction();

			// �������� ��������� ���� ��������� ������������ ��������
			List<LinkPath> paths = null;
			try {
				paths = getLinkPathFromSQL(action.getAttribute());
			} catch (IllegalArgumentException e) {
				throw new DataException("Wrong number of values for LINK and UPLINK for attribute " +
						action.getAttribute().getId(), e);
			} catch (Exception e) {
				logger.error(e);
			}
			
loop:		for (int i = 0; i < paths.size(); i++) {
				String link = paths.get(i).link;
				String uplink = paths.get(i).uplink;
				List<?> ids = Collections.singletonList(action.getCard().getId());
				do {
					// �������� ������� �������� �� ����� LINK
					cardIds = getJdbcTemplate().query(
							"SELECT a.card_id FROM attribute_value a " +
							"WHERE a.attribute_code=? AND a.number_value IN (" + formatIds(ids) + ")",
							new Object[] { link },
							new RowMapper(){
								public ObjectId mapRow(ResultSet rs,
										int rowNum) throws SQLException
								{
									final ObjectId row = new ObjectId(Card.class, rs.getLong(1));
									return row;
								}
							});
					if (cardIds != null && cardIds.size() > 0) {
						break loop;	// ����� ���� ������� �������� ��������
					}
					// ����� �������� ������������� �������� �� ����� UPLINK
					ids = getJdbcTemplate().queryForList(
						"SELECT a.card_id FROM attribute_value a " +
							"WHERE a.attribute_code=? AND a.number_value IN (" + formatIds(ids) + ")",
						new Object[] { uplink },
						Long.class);
					if (logger.isDebugEnabled()){
						logger.debug( "Uplinks: " + formatIds(ids));
					}
				} while (ids.size() > 0);
			}

			final SearchResult searchResult;

			if (cardIds == null || cardIds.size() > ManagerTempTables.MIN_EFFECTIVE_COUNT){
				if (!mgrTmpTables.isStarted()) {
					mgrTmpTables.startAll();
				}
				if (cardIds != null) {
					mgrTmpTables.insertCardIds(cardIds);
				}
				final ExecFetchCards fetcher = new ExecFetchCardsEx(getJdbcTemplate(), getUser(), getSessionId());
				fetcher.setResultColumns(action.getColumns(), true);
				searchResult = fetcher.execute();
			} else {
				final ExecFetchCardsFromIdsArray fetcher = new ExecFetchCardsFromIdsArray(
						getJdbcTemplate(), getUser(), getSessionId());
				fetcher.setResultColumns(action.getColumns(), true);
				fetcher.setObjectIds(cardIds);
				searchResult = fetcher.execute();
			}

			getJdbcTemplate().query(
				"SELECT av.string_value, a.attr_name_rus, a.attr_name_eng \n" + 
				"FROM attribute a \n" +
				"\t LEFT OUTER JOIN attribute_value av \n" +
				"\t\t ON av.attribute_code=a.attribute_code and av.card_id=(?) \n" +
				"WHERE a.attribute_code=(?)",
				new Object[] { action.getCard().getId(), Attribute.ID_NAME.getId() },
				new int[] { Types.NUMERIC, Types.VARCHAR }, // (2010/03 POSTGRE)
				new RowMapper() {
					public Object mapRow( ResultSet rs, int rowNum) throws SQLException {
						String name = rs.getString(1);
						if (name == null) name = "";
						final String linkRu = rs.getString(2);
						final String linkEn = rs.getString(3);
						searchResult.setNameRu(MessageFormat.format(
								ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS).getString(ID_NAME),
								name, linkRu));
						searchResult.setNameEn(MessageFormat.format(
								ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG).getString(ID_NAME),
								name, linkEn));
						return null;
					}
				}
			);
			return searchResult;
		} finally {
			mgrTmpTables.close();
		}
	}

	private String formatIds(List ids) {
		StringBuilder buf = new StringBuilder();
		for (Object id : ids) {
			if (buf.length() > 0)
				buf.append(",");
			buf.append(id);
		}
		return buf.toString();
	}
	
	static class LinkPath {
		String link;
		String uplink;
		
		LinkPath(String link, String uplink) {
			this.link = link;
			this.uplink = uplink;
		}
	}
	
	protected List<LinkPath> getLinkPathFromSQL(ObjectId attrId) throws IllegalArgumentException{
		// �������� ��������� ���� ��������� ������������ ��������
		List<?> strLinks = getJdbcTemplate().queryForList(
				"SELECT ao.option_value FROM attribute_option ao "+
				"WHERE ao.attribute_code=? AND ao.option_code=?",
				new Object[] {attrId.getId(), AttributeOptions.LINK}, String.class);
		List<?> strUplinks = getJdbcTemplate().queryForList(
				"SELECT ao.option_value FROM attribute_option ao "+
				"WHERE ao.attribute_code=? AND ao.option_code=?",
				new Object[] {attrId.getId(), AttributeOptions.UPLINK}, String.class);
		
		if (strLinks.size() == 0) {
			throw new IllegalArgumentException("LINK is not set");
		}
		
		String[] links = ((String)strLinks.get(0)).split(";");
		String[] uplinks = strUplinks.size() > 0 ? ((String)strUplinks.get(0)).split(";") : null;
		
		if (uplinks != null && links.length != uplinks.length) {
			throw new IllegalArgumentException("Wrong number of values for LINK and UPLINK");
		}
		
		int length = links.length;
		List<LinkPath> paths = new ArrayList<LinkPath>(length);
		for (int i = 0; i < length; i++) {
			String link = links[i].trim();
			String uplink = uplinks != null ? uplinks[i].trim() : null;
			paths.add(new LinkPath(link, uplink));
		}
		return paths;
	}
}
