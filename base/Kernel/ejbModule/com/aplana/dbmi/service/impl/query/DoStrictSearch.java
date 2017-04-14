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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.DatePeriod;
import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.action.Search.Interval;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

public class DoStrictSearch extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		StrictSearch search = getAction();
		
		final StringBuffer sqlBuf = new StringBuffer();
		final List<Object> argList = new ArrayList<Object>();
		final List<Integer> typeList = new ArrayList<Integer>();

//			sqlBuf.append( "INSERT INTO  gtemp_cardid (card_id) \n");
		sqlBuf.append( "\t SELECT  c.card_id ");
		sqlBuf.append( "\t FROM  card c \n");
		sqlBuf.append("\t WHERE (1=1) \n");
// ----------------------- Templates -----------------------------------------------
		if (search.getTemplates() != null) {
			if (!search.getTemplates().isEmpty()) {
				sqlBuf.append(MessageFormat.format(
						"\n\t\t AND c.template_id IN ({0}) \n",
						ObjectIdUtils.numericIdsToCommaDelimitedString(search.getTemplates())
					));
			}
		}

// ----------------------- Attributes -----------------------------------------------
		for (Iterator<Map.Entry<Object,Object>> itr = search.getAttributes().iterator(); itr.hasNext(); ) {
			final Map.Entry<Object,Object> attr = itr.next();
			final String attribute_code = attr.getKey().toString();

			if (attr.getValue() == null) {
				logger.warn( String.format("search prepeare: attribute '%s' has NULL corresponding value (!?)",
						attribute_code
					));
				continue;
			}
			final Class<?> clazz = attr.getValue().getClass();

			if (Search.Interval.class.equals(clazz)) {
				final Interval interval = (Search.Interval) attr.getValue();
				if (interval.min == Interval.EMPTY && interval.max == Interval.EMPTY)
					continue;

				sqlBuf.append( "\n\t\t AND EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av1.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av1 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av1.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av1.attribute_code=''{0}'' AND ( \n",
						StringEscapeUtils.escapeSql(attribute_code)
				));

				if (interval.max == interval.min) {	// ���������� ������ ������� (x==a)
					sqlBuf.append( MessageFormat.format(
							"av1.number_value = {0} )\n ",
							String.valueOf(interval.min)
						));
				} else { // ������� a � b ��������
					boolean argA = false;
					if (interval.min != Interval.EMPTY) {
						argA = true;
						sqlBuf.append( MessageFormat.format(
								"(av1.number_value >= {0}) ",
								String.valueOf(interval.min)
						));
					}
					if (interval.max != Interval.EMPTY) {
						if (argA) sqlBuf.append( "AND ");
						sqlBuf.append( MessageFormat.format(
								"(av1.number_value <= {0}) ",
								String.valueOf(interval.max)
						));
					}
					sqlBuf.append( ") \n" );
				}
				sqlBuf.append( "\t\t )\n" );
//   --------------- �������� ������� --------------------------------
				sqlBuf.append( "\n\t\t AND NOT EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av2.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av2 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av2.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av2.attribute_code=''{0}'' AND ( \n",
						StringEscapeUtils.escapeSql(attribute_code)
				));

				if (interval.max == interval.min) {
					// ���������� ������ ������� (x==a)
					sqlBuf.append( MessageFormat.format(
							"av2.number_value <> {0} )\n ",
							String.valueOf(interval.min)
						));
				} else { // ������� a � b ��������
					boolean argA = false;
					if (interval.min != Interval.EMPTY) {
						argA = true;
						sqlBuf.append( MessageFormat.format(
								"(av2.number_value < {0}) ",
								String.valueOf(interval.min)
						));
					}
					if (interval.max != Interval.EMPTY) {
						if (argA) sqlBuf.append( "AND ");
						sqlBuf.append( MessageFormat.format(
								"(av2.number_value > {0}) ",
								String.valueOf(interval.max)
						));
					}
					sqlBuf.append( ") \n" );
				}
				sqlBuf.append( "\t\t )\n" );
			} else if (Person.ID_CURRENT.equals(attr.getValue())) {
				sqlBuf.append( "\n\t\t AND EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av4.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av4 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av4.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av4.attribute_code=''{0}'' AND \n"+
						"\t\t\t\t\t av4.number_value={1} \n",
						StringEscapeUtils.escapeSql(attribute_code),
						String.valueOf(((Long)getUser().getPerson().getId().getId()).longValue())
				));
				sqlBuf.append( "\t\t )\n" );
//   --------------- �������� ������� --------------------------------
				sqlBuf.append( "\n\t\t AND NOT EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av4.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av4 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av4.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av4.attribute_code=''{0}'' AND \n"+
						"\t\t\t\t\t av4.number_value <> {1} \n",
						StringEscapeUtils.escapeSql(attribute_code),
						String.valueOf(((Long)getUser().getPerson().getId().getId()).longValue())
				));
				sqlBuf.append( "\t\t )\n" );
			} else if (StrictSearch.StringValue.class.equals(clazz)) {
				StrictSearch.StringValue stringValue = (StrictSearch.StringValue) attr.getValue();
				sqlBuf.append( "\n\t\t AND EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av4.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av4 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av4.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av4.attribute_code=''{0}'' AND \n"+
						(stringValue.isCaseSensitive() ?
							"\t\t\t\t\t av4.string_value = ''{1}'' \n" :
							"\t\t\t\t\t upper(av4.string_value) = upper(''{1}'') \n"),
						StringEscapeUtils.escapeSql(attribute_code),
						StringEscapeUtils.escapeSql(stringValue.getValue())
				));
				sqlBuf.append( "\t\t )\n" );
//   --------------- �������� ������� --------------------------------
				sqlBuf.append( "\n\t\t AND NOT EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av4.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av4 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av4.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av4.attribute_code=''{0}'' AND \n"+
						(stringValue.isCaseSensitive() ?
							"\t\t\t\t\t av4.string_value <> ''{1}'' \n" :
							"\t\t\t\t\t upper(av4.string_value) <> upper(''{1}'') \n"
						),
						StringEscapeUtils.escapeSql(attribute_code),
						StringEscapeUtils.escapeSql(stringValue.getValue())
				));
				sqlBuf.append( "\t\t )\n" );
			} else if (Search.DatePeriod.class.equals(clazz)) {
				final DatePeriod interval = (Search.DatePeriod) attr.getValue();
				if (interval.start == null && interval.end == null)
					continue;

				sqlBuf.append( "\n\t\t AND EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av1.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av1 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av1.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av1.attribute_code=''{0}'' AND ( \n",
						StringEscapeUtils.escapeSql(attribute_code)
				));

				if (interval.end == interval.start) {	
					// ���������� ������ ������� (x==a)
					sqlBuf.append( MessageFormat.format(
							"av1.date_value = {0} )\n ",
							String.valueOf(interval.start)
						));
				} else { // ������� a � b ��������
					boolean argA = false;
					if (interval.start != null) {
						argA = true;
						sqlBuf.append( MessageFormat.format(
								"(av1.date_value >= {0}) ",
								String.valueOf(interval.start)
						));
					}
					if (interval.end != null) {
						if (argA) sqlBuf.append( "AND ");
						sqlBuf.append( MessageFormat.format(
								"(av1.date_value <= {0}) ",
								String.valueOf(interval.end)
						));
					}
					sqlBuf.append( ") \n" );
				}
				sqlBuf.append( "\t\t )\n" );
//   --------------- �������� ������� --------------------------------
				sqlBuf.append( "\n\t\t AND NOT EXISTS ( \n" );
				sqlBuf.append( "\t\t\t SELECT av2.card_id \n");
				sqlBuf.append( "\t\t\t FROM attribute_value av2 \n");
				sqlBuf.append( "\t\t\t WHERE \t (av2.card_id = c.card_id) AND \n" );
				sqlBuf.append( MessageFormat.format(
						"\t\t\t\t\t av2.attribute_code=''{0}'' AND ( \n",
						StringEscapeUtils.escapeSql(attribute_code)
				));

				if (interval.end == interval.start) {
					// ���������� ������ ������� (x==a)
					sqlBuf.append( MessageFormat.format(
							"av2.date_value <> {0} )\n ",
							String.valueOf(interval.end)
						));
				} else { // ������� a � b ��������
					boolean argA = false;
					if (interval.start != null) {
						argA = true;
						sqlBuf.append( MessageFormat.format(
								"(av2.date_value < {0}) ",
								String.valueOf(interval.start)
						));
					}
					if (interval.end != null) {
						if (argA) sqlBuf.append( "AND ");
						sqlBuf.append( MessageFormat.format(
								"(av2.date_value > {0}) ",
								String.valueOf(interval.end)
						));
					}
					sqlBuf.append( ") \n" );
				}
				sqlBuf.append( "\t\t )\n" );
			}
		}

		final String sqlText = sqlBuf.toString();
		final Object[] args = argList.toArray();
		final int[] types = SimpleDBUtils.makeTypes(typeList);
		logger.trace( "Search SQL is "
				+ SimpleDBUtils.getSqlQueryInfo( sqlText, args, types)
		);

		@SuppressWarnings("unchecked")
		List<ObjectId> searchResult = this.getJdbcTemplate().query(sqlText, args, types, new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ObjectId(Card.class, rs.getLong(1));
			}
		});
		if (logger.isInfoEnabled())
			logger.info( searchResult.size() + " matches found");
		return searchResult;
	}

}
