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

import static com.aplana.dbmi.service.impl.workstation.Util.limitAndOffset;
import static com.aplana.dbmi.service.impl.workstation.Util.userPermissionCheck;
import static com.aplana.dbmi.service.impl.workstation.Util.userPermissionCheckWithClause;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.SearchByTemplatePeriodNameAction;
import com.aplana.dbmi.action.SearchRelatedDocsForReport;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;
import com.aplana.dbmi.utils.StrUtils;

public class DoSearchByTemplatePeriodName extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {

		final SearchByTemplatePeriodNameAction action = 
									(SearchByTemplatePeriodNameAction)getAction();

		final List<Object> argList = new ArrayList<Object>();
		final List<Integer> typeList = new ArrayList<Integer>();

		final String sql = buildSQL(action, argList, typeList);

		final Object[] args = argList.toArray();
		final int[] types = SimpleDBUtils.makeTypes(typeList);

		final List<?> searchResult =
			this.getJdbcTemplate().query(sql, args, types, new RowMapper(){

				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new ObjectId(Card.class, rs.getLong(1));
				}

			});
		logger.info( searchResult.size() + " matches found");
		
		return searchResult;
		
		
	}

	final private static String FMT_FIRST_1= "\t\t UPPER(av_Name.string_value) LIKE UPPER(''%{0}%'') \n";
	final private static String FMT_AND_1 = "\t\t AND UPPER(av_Name.string_value) LIKE UPPER(''%{0}%'') \n";
	final private static String FMT_OR_1 = "\t\t OR UPPER(av_Name.string_value) LIKE UPPER(''%{0}%'') \n";
	final private static String FMT_NOT_1 = "\t\t AND UPPER(av_Name.string_value) NOT LIKE UPPER(''%{0}%'') \n";


	@SuppressWarnings("unchecked")
	private String buildSQL(SearchByTemplatePeriodNameAction action, List<Object> argList, 
			List<Integer> typeList) throws DataException 
	{
		final int userId = ((Long)getUser().getPerson().getId().getId()).intValue();
		final StringBuffer sqlBuf = new StringBuffer();

		sqlBuf.append("SELECT  c.card_id \n");
		sqlBuf.append(	"FROM  card c \n");
		if (action.getName() != null && (!"".equals(action.getName()))) {
			sqlBuf.append(	"JOIN attribute_value AS av_Name \n");
			sqlBuf.append(	" ON av_Name.card_id = c.card_id \n");
			sqlBuf.append(	" AND av_Name.attribute_code='NAME' \n");
		}
		if(action.getRegNums()!=null && !action.getRegNums().isEmpty()){
			sqlBuf.append("JOIN attribute_value AS av_RegNum \n");
			sqlBuf.append(	" ON av_RegNum.card_id = c.card_id \n");
			sqlBuf.append(	" AND av_RegNum.attribute_code='JBR_REGD_REGNUM' \n");
			sqlBuf.append(MessageFormat.format(
					"\n\t\t AND av_RegNum.string_value  IN ({0}) \n",
					new Object[] {StrUtils.buildSqlStringList(action.getRegNums())}
				));	
		}

		final String projectNumbersSQL = buildProjectNumbersSQL(action, argList, typeList);
		if (projectNumbersSQL != null) {
			sqlBuf.append(projectNumbersSQL);
		}
		
		final String outNumbersSQL = buildOutNumbersSQL(action, argList, typeList);
		if (outNumbersSQL != null) {
			sqlBuf.append(outNumbersSQL);
		}
		
		
		final String OGAuthorSQL = buildOGAuthorSQL(action, argList, typeList);
		if (OGAuthorSQL != null) {
			sqlBuf.append(OGAuthorSQL);
		}

		final String periodSQL = buildPeriodSQL(action, argList, typeList);
		if (periodSQL != null)
			sqlBuf.append(periodSQL);

		sqlBuf.append(	" WHERE (4=4) \n");
		
		if (action.getCard()!=null) {
			sqlBuf.append(MessageFormat.format(
					"\n\t\t AND c.card_id  IN ({0}) \n",
					new Object[] {String.valueOf(action.getCard().getId().getId())}
				));
		}
		
		if (action.getIgnoredCards()!=null && !action.getIgnoredCards().isEmpty()) {
			sqlBuf.append("\n\t\t AND c.card_id  NOT IN (").append(ObjectIdUtils.numericIdsToCommaDelimitedString(action.getIgnoredCards())).append(") \n");
		}
		
		/*
		 * ���� ����������, ���������� ������� ����� ��������������� ����������� �� ����������� ����������, 
		 * ���� ��������������� ����������� �� ���� ���������� � ���������� �����������
		 */
		SearchRelatedDocsForReport filterAction = action.getFilterAction();
		if(filterAction != null && filterAction.getCard() != null && filterAction.getCard().getId() != null){
			ActionQueryBase query = getQueryFactory().getActionQuery(filterAction);
			query.setAction(filterAction);
			Collection<ObjectId> ids = (Collection<ObjectId>) getDatabase().executeQuery(getUser(), query);
			if(ids != null && !ids.isEmpty()) sqlBuf.append("\t and c.card_id in (").append(ObjectIdUtils.numericIdsToCommaDelimitedString(ids)).append(") \n");
			else sqlBuf.append("\t and false \n");
		}
		
		if (action.getTemplates() != null && !action.getTemplates().isEmpty()) {
			sqlBuf.append(MessageFormat.format(
				"\n\t\t AND c.template_id  IN ({0}) \n",
				new Object[] {ObjectIdUtils.numericIdsToCommaDelimitedString(action.getTemplates())}
			));
		}

		if (action.isCheckPermission()) {
			sqlBuf.append("\t AND ").append( userPermissionCheck( userId, action.getPermissionTypes()) );
			sqlBuf.insert(0, userPermissionCheckWithClause(userId));
		}
			
		if (action.getName() != null && (!"".equals(action.getName()))) {
			// DONE by Melnikov 12/02/14: (2012/02, RuSA) ����� ���� �� ������� ������ action.getName() �� " + ", " - " � "|" � ��������� ����-��� (����� �������) SQL-������� ...
			final List<SearchUtils.SimplePattern> patterns = SearchUtils.getSearchQueryPatterns(action.getName());
			sqlBuf.append("\t AND (\n");
			for (SearchUtils.SimplePattern pat: patterns) {
				if (pat == null || pat.getValue() == null || pat.getValue().length() == 0 ) 
					continue;
				String fmt = null;
				if ( pat.getOper() == null || pat.getOper().length() == 0 || "+".equals(pat.getOper()) ) {
					fmt = FMT_AND_1;
				} else if ( SearchUtils.SimplePattern.OPER_FIRST.equals(pat.getOper()) ) {
					fmt = FMT_FIRST_1;
				} else if ( "-".equals(pat.getOper()) ) {
					fmt = FMT_NOT_1;
				} else if ( "|".equals(pat.getOper()) ) {
					fmt = FMT_OR_1;
				} else {
					// jbr.dmsi.fileCardServlet.invalidParameter=Invalid parameter {0}: {1}
					throw new DataException("jbr.dmsi.fileCardServlet.invalidParameter", new Object[] { "oper", pat.getOper()} );
				}
				sqlBuf.append( MessageFormat.format( fmt, new Object[]{StrUtils.escapeSpecialCharactersForLikeClause(StringEscapeUtils.escapeSql(pat.getValue()))}));
			}
			sqlBuf.append("\t ) --/AND \n ");
		}

		sqlBuf.append( limitAndOffset( action.getPage(), action.getPageSize() ) );

		return sqlBuf.toString();
	}

	private String buildPeriodSQL(SearchByTemplatePeriodNameAction action,
			List<Object> argList, List<Integer> typeList) {

		if ((action.getStartPeriod() == null)
				&& (action.getEndPeriod() == null))
			return null;

		StringBuilder sqlBuf = new StringBuilder();

		sqlBuf.append("JOIN attribute_value as av_Created ON \n");
		sqlBuf.append("(av_Created.card_id = c.card_id) AND \n");
		sqlBuf.append("(av_Created.attribute_code='CREATED') AND ( \n");

		if (action.getStartPeriod() != null) {
			sqlBuf.append(" ( (av_Created.date_value >= (?)) \n");
			argList.add(action.getStartPeriod());
			typeList.add(new Integer(java.sql.Types.TIMESTAMP));

		}
		if (action.getEndPeriod() != null) {
			sqlBuf.append(" and (av_Created.date_value <  (?)) ) ) \n");
			argList.add(action.getEndPeriod());
			typeList.add(new Integer(java.sql.Types.TIMESTAMP));
		}

		return sqlBuf.toString();
	}
	
	private String buildOGAuthorSQL(SearchByTemplatePeriodNameAction action,
			List<Object> argList, List<Integer> typeList) {
		if (action.getOGAuthor() == null || action.getOGAuthor().isEmpty()) {
			return null;
		}
		
		// (NIsmagilov, 15/04/2013) 
		// I apologize for such a messy code, but I didn't have
		// enough time to figure out something better.

		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("join (\n");
		sqlBuf.append("\t with tmp (card_id) as (\n");
		sqlBuf.append("\t\t select crd.card_id from card crd \n");
		sqlBuf.append("\t\tjoin attribute_value av_OGAuthorSname\n");
		sqlBuf.append("\t\t on av_OGAuthorSname.card_id = crd.card_id\n");
		sqlBuf.append("\t\t and av_OGAuthorSname.attribute_code = 'ADMIN_274992'\n");
		sqlBuf.append("\t\t join attribute_value av_OGAuthorName\n");
		sqlBuf.append("\t\t on av_OGAuthorName.card_id = av_OGAuthorSname.card_id\n");
		sqlBuf.append("\t\t and av_OGAuthorName.attribute_code = 'ADMIN_281034'\n");
		sqlBuf.append("\t\t join attribute_value av_OGAuthorMname\n");
		sqlBuf.append("\t\t on av_OGAuthorMname.card_id = av_OGAuthorSname.card_id\n");
		sqlBuf.append("\t\t and av_OGAuthorMname.attribute_code = 'ADMIN_281035'\n");
		sqlBuf.append("\t\t and lower(av_OGAuthorSname.string_value) || lower(av_OGAuthorName.string_value)\n");
		sqlBuf.append("\t\t\t\t || lower(av_OGAuthorMname.string_value) \n");
		
		if (action.isOGAuthorStrictSearch()) {
			sqlBuf.append("\t\t\t\t ='" + action.getOGAuthor().toLowerCase().replaceAll("\\s", "") + "'\n");
		} else {
			sqlBuf.append("\t\t\t\t like '%" + action.getOGAuthor().toLowerCase().replaceAll("\\s", "") + "%'\n");
		}
		
		sqlBuf.append("\t)\n");
		sqlBuf.append("\t select auth.card_id from card auth\n");
		sqlBuf.append("\t left join attribute_value av_OGAuthor\n");
		sqlBuf.append("\t\t on av_OGAuthor.card_id = auth.card_id\n");
		sqlBuf.append("\t\t and av_OGAuthor.attribute_code = 'JBR_OG_REQ_AUTHOR'\n");
		sqlBuf.append("\t where \n");
		sqlBuf.append("\t(\n");
		sqlBuf.append("\t\t auth.card_id in (select card_id from tmp)\n");
		sqlBuf.append("\t\t or av_OGAuthor.number_value in (select card_id from tmp)\n");
		sqlBuf.append("\t)\n");
		sqlBuf.append("\t and auth.template_id = 864\n");
		sqlBuf.append(") as has_author_card\n");
		sqlBuf.append("on has_author_card.card_id = c.card_id\n");
		
		return sqlBuf.toString();
	}
	
	private String buildOutNumbersSQL(SearchByTemplatePeriodNameAction action,
			List<Object> argList, List<Integer> typeList) {
		
		StringBuilder sqlBuf = new StringBuilder();
		if(action.getOutNumbers() == null || action.getOutNumbers().isEmpty()){
			return null;
		}
		sqlBuf.append("JOIN attribute_value AS av_OutNumbers \n");
		sqlBuf.append(	" ON av_OutNumbers.card_id = c.card_id \n");
		sqlBuf.append(	" AND av_OutNumbers.attribute_code='JBR_REGD_NUMOUT' \n");
		
		if (action.isOutNumbersStrictSearch()) {
			sqlBuf.append(MessageFormat.format(
					"\n\t\t AND av_OutNumbers.string_value  IN ({0}) \n",
					new Object[] {StrUtils.buildSqlStringList(action.getOutNumbers())}
					));	
		} else {
			sqlBuf.append(	"\n\t\t AND av_OutNumbers.string_value  like '%"+ action.getOutNumbers().get(0) + "%' \n");
		}
		
		return sqlBuf.toString();
	}
	
	private String buildProjectNumbersSQL(SearchByTemplatePeriodNameAction action,
			List<Object> argList, List<Integer> typeList) {
		
		StringBuilder sqlBuf = new StringBuilder();
		if(action.getProjectNumbers() == null || action.getProjectNumbers().isEmpty()){
			return null;
		}
		sqlBuf.append("JOIN attribute_value AS av_ProjectNumber \n");
		sqlBuf.append(	" ON av_ProjectNumber.card_id = c.card_id \n");
		sqlBuf.append(	" AND av_ProjectNumber.attribute_code='JBR_PROJECT_NUMBER' \n");
		
		if (action.isProjectNumbersStrictSearch()) {
			sqlBuf.append(MessageFormat.format(
					"\n\t\t AND av_ProjectNumber.number_value  IN ({0}) \n",
					new Object[] {StrUtils.getAsString(action.getProjectNumbers())}
				));	
		} else {
			sqlBuf.append(	"\n\t\t AND av_ProjectNumber.number_value::varchar  like '%"+ action.getProjectNumbers().iterator().next() + "%' \n");
		}
		
		return sqlBuf.toString();
	}

}
