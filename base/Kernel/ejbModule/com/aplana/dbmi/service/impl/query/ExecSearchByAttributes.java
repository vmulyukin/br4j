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

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.*;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.search.ext.*;
import com.aplana.dbmi.model.search.ext.LinkedObjectId;
import com.aplana.dbmi.model.search.ext.RouteSearchObjectId.RouteSearchNode;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.cache.CardFilteringCache;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;
import com.aplana.dbmi.utils.SimpleDBUtils;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author RAbdullin
 *	���������� �������� �� java ������ oracle package "PKG_SEARCH":: 
 *		PREPARE_SET_CARDS_BY_ATTR_TBL (6 ����������); 
 */
@SuppressWarnings("all")
public class ExecSearchByAttributes extends JdbcDaoSupport
{
	private static final long LONG_SEARCH_TIME = 2*60*1000; //2 min - very long query
	private ManagerTempTables mgrTempTables;
	Future<Integer> futureCount = null;
	ExecutorService pool = null;

	public ExecSearchByAttributes(JdbcTemplate jdbc) {
		setJdbcTemplate(jdbc);
	}

	public ExecSearchByAttributes(JdbcTemplate jdbc, ManagerTempTables mgrTempTables) {
		this(jdbc);
		this.mgrTempTables = mgrTempTables;
	}

	/**
	 * Emulates stored procedure with parameters defined in given {@link Search} object.
	 * @param search {@link Search} object containing search parameters
	 * @param user information about user, who performs stored procedure call. Used if
	 * {@link Search} object contains conditions with reference to {@link Person#ID_CURRENT 'current user'}
	 * variable.  
	 * @return the loaded cards' ids
	 * @throws Exception
	 */
	public List<ObjectId> execute( final Search search, final UserData user) 
		throws DataException
	{
		try {
			try {
				final ArrayList<ObjectId> cached =
					CardFilteringCache.instance().findCards( search, ( Long ) user.getPerson().getId().getId() );
				return cached;
			} catch ( IllegalArgumentException e ) {
				logger.trace("cards' search cache is not used");
				return execByAttrTable( search, (Long)user.getPerson().getId().getId() );
			}
		} catch (Exception ex) {
			throw new DataException(ex);
		}
	}
	
	private String formSearchConfigValue(String alias, TextSearchConfigValue searchCfgValue) {
		
		String result = "";
		
		String searchValue = StringEscapeUtils.escapeSql(searchCfgValue.value);
		
		if (searchCfgValue.searchType == Search.TextSearchConfigValue.EXACT_MATCH) {
			result =  alias + ".string_value =''{0}''";
		} else if (searchCfgValue.searchType == Search.TextSearchConfigValue.EXACT_MATCH_NOT_CASE_SENSITIVE) {
			result =  "UPPER(substr(" + alias + ".string_value, 0, 2000)) = UPPER(substr(''{0}'', 0, 2000)) \n" +
						"   AND " + alias + ".string_value IS NOT NULL \n";
		} else if (searchCfgValue.searchType == Search.TextSearchConfigValue.CONTAINS) {
			searchValue = "%" + StrUtils.escapeSpecialCharactersForLikeClause(searchValue) + "%";
			result = "UPPER(" + alias + ".string_value) like UPPER(''{0}'')";
		} else if (searchCfgValue.searchType == Search.TextSearchConfigValue.STARTS_FROM) {
			searchValue = StrUtils.escapeSpecialCharactersForLikeClause(searchValue) + "%";
			result = "UPPER(" + alias + ".string_value) like UPPER(''{0}'')";
		}
		
		return MessageFormat.format(result, new Object[] {searchValue});
		
		
	}
	
	private String formSearchConfigValue(String alias, IntegerSearchConfigValue integerSearchConfigValue) {
		
		String result = "";
		
		String searchValue = integerSearchConfigValue.value.toString();
		
		if (IntegerSearchConfigValue.SearchType.EXACT_MATCH.equals(integerSearchConfigValue.searchType)) {
			result =  alias + ".number_value ={0}";
		} else {
			searchValue = "%" + StrUtils.escapeSpecialCharactersForLikeClause(searchValue) + "%";
			result = "UPPER(text(" + alias + ".number_value)) like UPPER(''{0}'')";
		}		
		return MessageFormat.format(result, new Object[] {searchValue});		
	}
	
	
	
	private List<ObjectId> execByAttrTable( final Search search, final Long personId) throws Exception {
		final List /*<String>*/ attributeCodes = new ArrayList();

		String words = search.getWords();

		// old: String materials,
		// TODO: ����������� ������ MATERIAL_URL (��� 2) ����� ��������� !!!
		final boolean chkNotNullExternalPath 
			= String.valueOf(Card.MATERIAL_URL).equals( StrUtils.getAsString( search.getMaterialTypes(), null));

		final boolean isEmptyWords = StrUtils.isStringEmpty(words);

		// (!)	��� ���� �������� ORACLE ������ "_" �������� ����������� 
		// 		� �������� "���� ����� ������".
		final String nvlWords = StrUtils.nvl(StringEscapeUtils.escapeSql(words), "_"); 

		// ����� SQL-�������, ��� ����������� ��������� � �� ���� ...
		final StringBuffer sqlBuf = new StringBuffer();
		final List /*Object*/ argList = new ArrayList();
		final List /*Integer*/ typeList = new ArrayList();

		sqlBuf.append( "FROM  card c \n");


		//����� 08.10.2010 ����������� ����������
		//�������� �: 08.12.2010, ������������� ���� �� DoSearch
		sqlBuf.append( DoSearch.tagJoinMarkerBegin); // ������� ����� ��� ������� JOIN ����� ����������
		sqlBuf.append( DoSearch.tagJoinMarkerEnd);


		////////////////////////////////////////////////////////////////////
		// ��������
		StringBuilder emptyAttrString = null;
		StringBuilder existAttrString = null;
		int i = 100;
		for( Iterator itr = search.getFullAttributes().iterator(); itr.hasNext(); ) 
		{
			final String alias = "av_" + i++; 
			final String linkedAlias = "av_" + i++;// ��������� ������� �������
			final Map.Entry attr = (Map.Entry) itr.next();
			final ObjectId id = (ObjectId)attr.getKey();
			final String attribute_code = id.getId().toString(); // (String) attr.getKey()	
			
			if (id instanceof RouteSearchObjectId) {
				//RouteSearchObjectId attributes ����������� � WHERE ������
				//����� JOIN ������� ������
				continue;
			}
			if (attr.getValue() == null) {
				continue;
			}
			final Class clazz = attr.getValue().getClass();

			if (Boolean.class.equals(clazz)) {
				attributeCodes.add( attribute_code );
			} else if (String.class.equals(clazz) || Long.class.equals(clazz)) {
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(attribute_code) }
						
				));//��������� �� number_value ��� ������ �� ������ �������, ���� �������� �� ��� ������ �� ���� ����� ��������� � string_value		
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t ({0}.number_value=''{1}'') \n",
						new Object[] {	alias, StringEscapeUtils.escapeSql(String.valueOf(attr.getValue())) }
				));
				sqlBuf.append( "\n\t\t )" ); // (*)
			} else if (TextSearchConfigValue.class.equals(clazz)) {
				final Search.TextSearchConfigValue searchCfgValue = (Search.TextSearchConfigValue) attr.getValue();
				String sql =  createSqlStringAttr(alias, id, searchCfgValue);
				sqlBuf.append(sql);

				
			} else if(IntegerSearchConfigValue.class.equals(clazz)){
				final Search.IntegerSearchConfigValue integerSearchConfigValue =  (Search.IntegerSearchConfigValue) attr.getValue();	
				String sql =  createSqlIntegerAttr(alias, id, integerSearchConfigValue);
				sqlBuf.append(sql);
			} else if (Search.DatePeriod.class.equals(clazz)) {
				final Search.DatePeriod period =
					(Search.DatePeriod) attr.getValue();

				if ((period.start == null) && (period.end == null))
					continue;

				// (!) (2010/12/02, RuSA) ����������� ��������: JOIN ��������� ������� ������ EXISTS (��� ������� �������� ������ ��� ������ ����������)
				if (!(id instanceof LinkedObjectId)) {
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON  \n" ); // (*)
					sqlBuf.append("(");	
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t ({0}.attribute_code=''{1}'') AND ( \n",
							new Object[] {	alias, StringEscapeUtils.escapeSql(attribute_code) }
					)); 		// (**)
				} else {
					LinkedObjectId linkedObjectId = (LinkedObjectId)id;
					if(linkedObjectId.isBackLinked()) {
						sqlBuf.append( createSqlBackLinkAttr(id, alias, linkedAlias));
						sqlBuf.append(  MessageFormat.format("\n\t\t JOIN attribute_value as {0} ON {0}.card_id = {1}.card_id "
												+"\t AND {0}.attribute_code=''{2}'' \n ",
							new Object[] {	
							alias,
							linkedAlias,
							StringEscapeUtils.escapeSql(attribute_code)
						}
						));
						sqlBuf.append("\n\t\t\t\t AND ( ");
					} else {
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON  \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" );
					String condition = "\n\t\t\t\t ( " + alias +".attribute_code =''" + ((LinkedObjectId)id).getLinkedCode() + "'') \n\t\t\t\t";
					condition = condition + "\n\t\t\t\t  JOIN  attribute_value AS " + linkedAlias;
					condition = condition + "\n\t\t\t\t  ON " + linkedAlias + ".card_id = " + alias+".number_value \n\t\t\t\t";
					condition = condition + " AND " + linkedAlias + ".attribute_code = ''{0}'' \n\t\t\t\t AND (";
					sqlBuf.append( MessageFormat.format(condition,new Object[] {StringEscapeUtils.escapeSql(attribute_code)}));					
				}
				}

				if (period.isIncludedEmpty()) {
					sqlBuf.append("\t\t\t\t\t (" + alias +".date_value is NULL) OR ( ");
					
				}

				/* � ����������� ��������, ��� � inline-�����������, �� ����� 
				 * ����������� ��������� ����-��������� ��������������
				 * ���� � ������ ��� ������� � ����� sql-�������, � ���
				 * �������������� - ��� ��� ��������� �������� sql-�������.
				 */

				sqlBuf.append( "\t\t\t\t\t\t ( "); // (***)
				if ( (period.start != null) && period.start.equals(period.end) ) 
				{
					// checks if the date value is greater than or equal the provided date with time 00:00:00.000
					// and less than tomorrow date with 00:00:00.000 time
					if (!(id instanceof LinkedObjectId))
	    				sqlBuf.append("(" + alias +".date_value >= (?) AND " + alias +".date_value < (?))" );
	    			else 
	    				sqlBuf.append("(" + linkedAlias +".date_value >= (?) AND " + linkedAlias +".date_value < (?))" );
	    			
					Date date = DateUtils.timeToZero(period.start);
					
	    			argList.add( DateUtils.toUTC(date) );
	    			argList.add( DateUtils.toUTC(tomorrow(date)));
					typeList.add( new Integer(java.sql.Types.TIMESTAMP));
					typeList.add( new Integer(java.sql.Types.TIMESTAMP));
					
				} else { // ������� a � b ��������
					boolean argA = false;
					if (period.start != null) {
						argA = true;
						sqlBuf.append( "("+ alias +".date_value >= (?)) ");
						argList.add( DateUtils.toUTC(period.start));
						typeList.add( new Integer(java.sql.Types.TIMESTAMP));
					}
					if (period.end != null) {
						if (argA) sqlBuf.append( "and ");
						/* (2010/01/15, RuSA) �.�. ������������ ������������ 
						 * "����� 14 � 15 ������ 06/2009" �������������, ��� ���� �������� 
						 * 14 � 15-��, �� ���������� ������� ������ �����: 
						 * "'2009/06/14' <= t  �  t < '2009/06/16'",
						 * � ��� ���� ������ ����� ������ �����: "� 14�� �����
						 * �� 15 ����� 16:20", �� ������� ����� ����� ��: 
						 * "'2009/06/14' <= t  �  t <= '2009/06/15 16:20'",
						 */
						if (isStartOfTheDay(period.end)) {
							// ���������� � "������"
							sqlBuf.append( "("+ alias +".date_value <  (?)) ");
							argList.add( DateUtils.toUTC(tomorrow(period.end)));
							typeList.add( new Integer(java.sql.Types.TIMESTAMP));
						} else { 
							sqlBuf.append( "("+ alias +".date_value <= (?)) ");
							argList.add( DateUtils.toUTC(period.end));
							typeList.add( new Integer(java.sql.Types.TIMESTAMP));
						}
					}
				}
				sqlBuf.append( ") \n" ); // (***)

				if (period.isIncludedEmpty()) //need to close one more condition at this case 
				sqlBuf.append( "\t\t\t\t\t ) \n"); // (**)
				
				sqlBuf.append( "\t\t\t\t\t ) \n"); // (**)
				if (!(id instanceof LinkedObjectId))
					sqlBuf.append( "\t\t )\n" ); // (*): JOIN (...)

			} else if (Search.Interval.class.equals(clazz)) {
				final Interval interval 
					= (Search.Interval) attr.getValue();
				if ((interval.min == Interval.EMPTY) && (interval.max == Interval.EMPTY))
					continue;

				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t "+ alias +".attribute_code=''{0}'' AND ( \n",
						new Object[] {	StringEscapeUtils.escapeSql(attribute_code)}
				)); // (**)

				sqlBuf.append( "\t\t\t\t\t\t ( "); // (***)
				if (interval.max == interval.min)
				{	// ���������� ������ ������� (x==a)
					sqlBuf.append( MessageFormat.format(
							"{0}.number_value = {1} ",
							new Object[] { alias, String.valueOf(interval.min) }
						));
				} else { // ������� a � b ��������
					boolean argA = false;
					if (interval.min != Interval.EMPTY) {
						argA = true;
						sqlBuf.append( MessageFormat.format(
								"({0}.number_value >= {1}) ",
								new Object[] { alias, String.valueOf(interval.min) }
						));
					}
					if (interval.max != Interval.EMPTY) {
						if (argA) sqlBuf.append( "and ");
						sqlBuf.append( MessageFormat.format(
								"({0}.number_value <= {1}) ",
								new Object[] { alias, String.valueOf(interval.max) }
						));
					}
				}
				sqlBuf.append( ") \n" ); // (***)
				sqlBuf.append( "\t\t\t\t\t ) \n"); // (**)
				sqlBuf.append( "\t\t )\n" ); // (*): EXISTS(...)

			} else if (Collection.class.isAssignableFrom(clazz)) {
				final Iterator itrVal = ((Collection) attr.getValue()).iterator();

				final HashSet /*Long*/ idList = new HashSet();
				final List<String> anotherValues = new ArrayList<String>(); // ������ ��������
				while (itrVal.hasNext()) {
					final ReferenceValue value = (ReferenceValue) itrVal.next();
					if(ReferenceValue.ID_ANOTHER.equals(value.getId())) {
						anotherValues.add(value.getValueRu());
					} else idList.add( value.getId().getId());
				}
				
				if(!(id instanceof LinkedObjectId)){
					sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
					sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); // where (cond1) OR (cond2) ...
					
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t ( {0}.attribute_code=''{1}'' AND ( \n",
								new Object[] {	
										alias,
									StringEscapeUtils.escapeSql(attribute_code)
							}
					));
					
					if (!idList.isEmpty()) {
							sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t\t {0}.value_id in ({1}) \n",
							new Object[] {
									alias,
									SimpleDBUtils.getAsSqlIdList(idList)
								}
						));
						if(!anotherValues.isEmpty())
							sqlBuf.append("OR ");
					}
					
					if(!anotherValues.isEmpty()) {
						sqlBuf.append( MessageFormat.format( 
								"{0}.another_value in ({1}) \n",
							new Object[] {	
									alias,
										SimpleDBUtils.getAsSqlStrList(anotherValues)
							}
					));
					}
					
					sqlBuf.append( " )\t)\t\t ) \n" ); // (*) EXISTS (...)					
				}else{
					LinkedObjectId linkedObjectId = (LinkedObjectId)id;
					if(linkedObjectId.isBackLinked()) {
						sqlBuf.append( createSqlBackLinkAttr(id, alias, linkedAlias));
						sqlBuf.append(  MessageFormat.format("\n\t\t JOIN attribute_value as {0} ON {0}.card_id = {1}.card_id "
												+"\t AND {0}.attribute_code=''{2}'' \n ",
							new Object[] {	
							alias,
							linkedAlias,
							StringEscapeUtils.escapeSql(attribute_code)
						}
						));
					sqlBuf.append( MessageFormat.format( 
								"\t\t\t\t\t AND {0}.value_id in ({1}) \n",
								new Object[] {	
										linkedAlias,
										SimpleDBUtils.getAsSqlIdList(idList)
								}
						));
					} else {
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); // where (cond1) OR (cond2) ...
						sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t ( {0}.attribute_code=''{1}'' )",
							new Object[] {	
									alias,
									((LinkedObjectId)id).getLinkedCode()									
							}
					));
					sqlBuf.append( "\t\t ) \n" ); // (*) EXISTS (...)
					
					sqlBuf.append("\n\t\t\t\t  JOIN  attribute_value AS " + linkedAlias);
					sqlBuf.append("\n\t\t\t\t  ON " + linkedAlias + ".card_id = " + alias+".number_value \n\t\t\t\t AND");
					//sqlBuf.append( " AND " + linkedAlias + ".attribute_code = ''{0}'' \n\t\t\t\t AND ");
					sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t (  {0}.attribute_code=''{1}'' AND \n" +
							"\t\t\t\t\t {0}.value_id in ({2}) ) \n",
							new Object[] {	
									linkedAlias,
									StringEscapeUtils.escapeSql(attribute_code),
									SimpleDBUtils.getAsSqlIdList(idList)
							}
					));
				}				
				}

			} else if (Person.ID_CURRENT.equals(attr.getValue())) {
				sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
				sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); 
				sqlBuf.append( MessageFormat.format( 
						"\t\t\t\t\t {0}.attribute_code=''{1}'' AND \n"+
						"\t\t\t\t\t {0}.number_value={2} \n",
						new Object[] {	
								alias,
								StringEscapeUtils.escapeSql(attribute_code),
								String.valueOf(personId.longValue())
						}
				));
				sqlBuf.append( "\t\t )\n" ); // (*): EXISTS(...)
			} else if (NumericIdList.class.equals(clazz)) {
				NumericIdList list = (NumericIdList)attr.getValue();
				if (list.isEmpty()) {
					//logger.warn("No ids specified for attribute with code '" + attribute_code + "'. Ignoring filter clause");
				}
				if(!(id instanceof LinkedObjectId)){
					if(id.getType().isAssignableFrom(BackLinkAttribute.class)) {
						
						sqlBuf.append( MessageFormat.format(
								"\n\t\t JOIN attribute_option ao_{0} ON ( ao_{0}.attribute_code = ''{1}'' AND ao_{0}.option_code = ''LINK'' ) " +
								"\n\t\t JOIN attribute_value {0} on (" +
								"\n\t\t {0}.attribute_code = ao_{0}.option_value AND {0}.card_id in ({2}) AND {0}.number_value = c.card_id" 		
								,  new Object[]{
										alias,
										id.getId(),
										list.getCommaDelimitedString()
								}
						));
						sqlBuf.append( "\t\t ) \n" ); // (*) EXISTS (...)
					} else {
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); // where (cond1) OR (cond2) ...
						sqlBuf.append( MessageFormat.format(
							"\t\t\t\t ( {0}.attribute_code in ({1}) AND \n" +
							"\t\t\t\t\t {0}.number_value in ({2}) ) \n",
							new Object[] {	
									alias,
									getAttrIdsStringFromObjectId(id),
									list.getCommaDelimitedString()
							}
						));
						sqlBuf.append( "\t\t ) \n" ); // (*) EXISTS (...)
					}
				} else {
					LinkedObjectId linkedObjectId = (LinkedObjectId)id;
					if (linkedObjectId.isBackLinked()) {
						sqlBuf.append( createSqlBackLinkAttr(id, alias, linkedAlias));
						sqlBuf.append(getFilteredObjectIdString(id, linkedAlias + ".card_id"));
						sqlBuf.append( MessageFormat.format(
								"\n\t\t JOIN attribute_value as {0} ON {0}.card_id = {1}.card_id "
								+"\n\t\t AND {0}.attribute_code in ({2}) \n\t\t AND {0}.number_value in ({3}) \n",
								alias, linkedAlias, getAttrIdsStringFromObjectId(id), list.getCommaDelimitedString()
						));
					} else {
						sqlBuf.append( "\n\t\t JOIN attribute_value as "+ alias +" ON ( \n" ); // (*)
						sqlBuf.append( "\t\t\t 		("+ alias +".card_id = c.card_id) AND \n" ); // where (cond1) OR (cond2) ...
						sqlBuf.append( MessageFormat.format( 
							"\t\t\t\t ( {0}.attribute_code=''{1}'' )", alias, ((LinkedObjectId)id).getLinkedCode()
						));
						sqlBuf.append( "\t\t ) \n" ); // (*) EXISTS (...)
						sqlBuf.append(getFilteredObjectIdString(id,alias + ".number_value"));
						sqlBuf.append("\n\t\t\t\t  JOIN  attribute_value AS " + linkedAlias);
						sqlBuf.append("\n\t\t\t\t  ON " + linkedAlias + ".card_id = " + alias+".number_value \n\t\t\t\t AND");
						//sqlBuf.append( " AND " + linkedAlias + ".attribute_code = ''{0}'' \n\t\t\t\t AND ");
						sqlBuf.append(MessageFormat.format(
								"\t\t\t\t (  {0}.attribute_code in ({1}) AND \n" +
								"\t\t\t\t\t {0}.number_value in ({2}) ) \n",
								linkedAlias,
								getAttrIdsStringFromObjectId(id),
								list.getCommaDelimitedString()
						));
					}
				}
			//��� EmptyAttribute � ExistAttribute � ������ ������ ��� ����������� � ����������
			//��������� �������� �� ������� ��������� �������, �.�. ������� �����������
			//������ ��� ����� (�������) ��������
			} else if (EmptyAttribute.class.equals(clazz)) {
				if (emptyAttrString == null) {
					emptyAttrString = new StringBuilder();
				} else {
					emptyAttrString.append(", ");
				}
				emptyAttrString.append("'" + StringEscapeUtils.escapeSql(attribute_code) + "'");
			} else if (ExistAttribute.class.equals(clazz)) {
				if (existAttrString == null) {
					existAttrString = new StringBuilder();
				} else {
					existAttrString.append(", ");
				}
				existAttrString.append("'" + StringEscapeUtils.escapeSql(attribute_code) + "'");
			} else {
				//logger.warn("Unsupported attribute class '" + clazz
				//		+ "' of search attribute '"+ attribute_code + "'=" + attr.getValue());
			}
		}

		 /* (2010/01/15) ���������� � person �� ������, ��������� ��� � ������� Exists ���� */
		if (!isEmptyWords && !search.isByCode()) {
			sqlBuf.append(createSqlWords(search.getWords(), attributeCodes, "av99", search.isStrictWords(), argList, typeList));
		}

		// ����� ����������� �  emmitPermissionWhere
		//DoSearch.emmitPermissionFrom( sqlBuf, search.getFilter(), personId, "c");
		
		sqlBuf.append( "WHERE (1=1) \n");

		// TODO: USE DelegatorBean.makeSqlSelectCAWithDelegationsEx(
		// (!) ����� ����� 
		// (!) �������� ���� ������ ���� "��������" ...
		DoSearch.emmitPermissionWhere( sqlBuf, search.getFilter(), personId, "c");

		//if (!isEmptyWords )	{		-- Commented out - �.�. 21.08.2010 - ������!
		if (search.isByCode()) {
			if (isEmptyWords)
				return null;
			sqlBuf.append("\t\t AND c.card_id in (");
			Iterator<String> iterator = Arrays.asList(nvlWords.split(",")).iterator();
			while(iterator.hasNext()){
				Long id = Long.parseLong(iterator.next().trim());
				argList.add(id);
				typeList.add(java.sql.Types.NUMERIC);
				sqlBuf.append("?");
				if(iterator.hasNext()){
					sqlBuf.append(",");
				}
			}
			sqlBuf.append(")");
		} 
		//} 

		// -- Checking for specified source type (for URL processing only!)
		if (chkNotNullExternalPath) {
			sqlBuf.append( "\t\t AND c.external_path IS NOT NULL \n");
		}

		// (!) ���� ��������
		// (2009/12/25, RuSA) lv_sql.append( "\t\t AND c.template_id IN ( SELECT template_id FROM gtemp_template_list) \n" );
		DoSearch.emmitTemplateChk( sqlBuf, search.getTemplates(), "c");

		// (!) ���� ��������
		DoSearch.emmitStateChk( sqlBuf, search.getStates(), "c");
		
		//����� �� RouteSearchAttribute
		emmitRouteSearchAttribute(sqlBuf, search.getFullAttributes(), "c");
		
		// (!) ���� ������������ ID  (Smirnov A. : 06.07.12)
		if (search.getIgnoredIds() != null && search.getIgnoredIds().size() != 0 ){
			String ignorAdd = new String("\n\t\t AND c.card_id NOT IN (");
			boolean first = true;
			for(Iterator itr = search.getIgnoredIds().iterator(); itr.hasNext();){
				ObjectId objectId = (ObjectId) itr.next();
				if (first){
					first = false;
				}else{
					ignorAdd += ", ";
				}
				ignorAdd += objectId.getId().toString();
			}
			ignorAdd += ")";
			sqlBuf.append(ignorAdd);
		}

		if (emptyAttrString != null) {
			sqlBuf.append("\n\t\t AND NOT EXISTS ( \n");
			sqlBuf.append("\t\t\t SELECT av.attr_value_id \n");
			sqlBuf.append("\t\t\t\t FROM attribute_value av \n");
			sqlBuf.append("\t\t\t\t WHERE av.card_id = c.card_id AND av.attribute_code IN (" + emptyAttrString + ")) \n");
		}

		if (existAttrString != null) {
			for(String existAttribute : existAttrString.toString().split(",")) {
				sqlBuf.append("\n\t\t AND EXISTS ( \n");
				sqlBuf.append("\t\t\t SELECT av.attr_value_id \n");
				sqlBuf.append("\t\t\t\t FROM attribute_value av \n");
				sqlBuf.append("\t\t\t\t WHERE av.card_id = c.card_id AND av.attribute_code = " + existAttribute.trim());
				sqlBuf.append("\n\t\t ) \n");
			}
		}

		final Object[] args = argList.toArray();

		/* (!) ������� �������� ���-�� ����� - ����� � ������
		 * � ������ ��� ������������ ���� ������� ��� ��� (?)
		 */
		final StringBuffer countSql = new StringBuffer(sqlBuf);
		DoSearch.insertAfterWithBlock(countSql, "SELECT distinct c.card_id \n");
		countSql.append(") AS zz(card_id)\n");
		if (search.getSearchLimit() > 0){
			countSql.append("LIMIT " + search.getSearchLimit() + "\n");
		}
		
		final int[] types = SimpleDBUtils.makeTypes(typeList);
		
		DoSearch.insertAfterWithBlock(countSql, "SELECT COUNT(1) FROM (\n");
		pool = Executors.newFixedThreadPool(1);
		futureCount = pool.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getJdbcTemplate().queryForInt(countSql.toString(), args, types);
			}
		});
		int rowCount = 0;

		if (search.isDontFetch()) {
			try {
				rowCount = futureCount.get();
				search.getFilter().setWholeSize(rowCount);
			} finally {
				pool.shutdown();
			}
			return new ArrayList();
		}
		
		/*
		 * ������ �������� ������ ���������� �� ������ ����� �������� ...
		 */
		DoSearch.insertAfterWithBlock(sqlBuf, "SELECT c.card_id \n");

		// add GROUP BY (instead off DISTINCT) to exclude duplicates of card id-s
		sqlBuf.append(" GROUP BY c.card_id \n");

		// ���������� ...
		DoSearch.emmitSortOrder( sqlBuf, search, sqlBuf.indexOf(DoSearch.tagJoinMarkerEnd),
				"c", getJdbcTemplate(), logger, DoSearch.AggregateFunction.min);

		// ������������ ����� ������ getPageSize ��������
		DoSearch.emmitPgLimit( sqlBuf, search.getFilter());
		
		final int pgSz = search.getFilter().getPageSize();
		// ��������� ������� ���� �� ������������ ��������� ������� ��� ���...
		// (������������ ���� ���� ���� �������� ���� ������ � ������ �������� 
		// �� �����, � ������� ���� �������)
		boolean useTempTable = false;
		if (this.mgrTempTables != null && pgSz == 0) {
			try {
				rowCount = futureCount.get();
			} finally {
				pool.shutdown();
			}
			useTempTable = rowCount > ManagerTempTables.MIN_EFFECTIVE_COUNT;
		}
		if (useTempTable) {
			// ���������� ���� ���� (�� ����� ��������� �� ���� ������) ...
			if (!mgrTempTables.isStarted())
				mgrTempTables.startAll();
			// INSERT INTO
			sqlBuf.insert( 0, "INSERT INTO "+ ManagerTempTables.TMPTABLEID_CARDID +" \n");
		}

		//
		////////////////////////////////////////////////////////////////////

		// execute immediate lv_sql;
		final String sqlText = sqlBuf.toString();
		
		if (logger.isTraceEnabled()) {
			logger.trace("Generated selector SQL is: \n" + SimpleDBUtils.getSqlQueryInfo( sqlText, args, types) );
		}


		int found = -1;
		List result = null;

		long fetchStart = System.currentTimeMillis();
		if (useTempTable) {
			found = this.getJdbcTemplate().update( sqlText, args, types);
		} else {
			/* ����� 08.10.2010. �������� id �������� �����, ��� ��������� ������ */
			result = getJdbcTemplate().query(
					sqlText,
					args,
					types,
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) 
							throws SQLException {
							return  new ObjectId(Card.class, rs.getLong("card_id"));
						}
					});
			if (result != null) 
				found = result.size();
		}
		long fetchEnd = System.currentTimeMillis();
		if (fetchEnd - fetchStart > LONG_SEARCH_TIME) {
			logger.error("Long SQL. Time: " + (fetchEnd - fetchStart) + "\nArgs:" + Arrays.toString(args)+ "\n SQL: " + sqlText);
		}
		try {
			search.getFilter().setWholeSize(futureCount.get());
		} finally {
			if (!pool.isShutdown())
				pool.shutdown();
		}

		//logger.info( found + " matches found");

		return result;
	}
	
	private String createSqlBackLinkAttr(ObjectId id, String alias, String linkedAlias) {
		LinkedObjectId linkedObjectId = (LinkedObjectId) id;
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t join attribute_option " + alias + "_ao on "+ alias +"_ao.attribute_code = '"+linkedObjectId.getLinkedCode()+"' and "+ alias +"_ao.option_code = 'LINK' \n");
		sb.append("\t\t\t join attribute_value "+linkedAlias+" on c.card_id = "+linkedAlias+".number_value and "+linkedAlias+".attribute_code = " + alias +"_ao.option_value \n");
		return sb.toString();
	}
	
	private String createSqlIntegerAttr(String alias, ObjectId id,
			IntegerSearchConfigValue integerSearchConfigValue) {
		StringBuilder sqlBuf = new StringBuilder();
		String linkedAlias = "link_"+alias;
		String attribute_code = getAttrToString(id);
		
		if (integerSearchConfigValue.value==null)
			return sqlBuf.toString();
		
		if (!(id instanceof LinkedObjectId)) {
			sqlBuf.append("\n\t\t JOIN attribute_value AS " + alias);
			sqlBuf.append("\n\t\t\t ON " + alias + ".card_id = c.card_id");
			String condition = "\n\t\t\t\t AND ( " + alias +".attribute_code =''{0}'') \n\t\t\t\t AND ";
			sqlBuf.append(MessageFormat.format(condition, new Object[] {StringEscapeUtils.escapeSql(attribute_code)}));
			String conditionValue = formSearchConfigValue(alias, integerSearchConfigValue);
			sqlBuf.append(conditionValue);
			
		} else {
			LinkedObjectId linkedObjectId = (LinkedObjectId)id;
			if(linkedObjectId.isBackLinked()) {
				sqlBuf.append( createSqlBackLinkAttr(id, alias, linkedAlias));
				sqlBuf.append(  MessageFormat.format("\n\t\t JOIN attribute_value as {0} ON {0}.card_id = {1}.card_id "
										+"\t AND {0}.attribute_code=''{2}'' \n ",
					new Object[] {	
					alias,
					linkedAlias,
					StringEscapeUtils.escapeSql(attribute_code)
				}
				));
				sqlBuf.append("\n\t\t\t\t AND ");
			} else {
				sqlBuf.append("\n\t\t JOIN attribute_value AS " + alias);
				sqlBuf.append("\n\t\t\t ON " + alias + ".card_id = c.card_id");
			String condition = "\n\t\t\t\t AND ( " + alias +".attribute_code =''" + ((LinkedObjectId)id).getLinkedCode() + "'') \n\t\t\t\t";
			condition = condition + "\n\t\t\t\t  JOIN  attribute_value AS " + linkedAlias;
			condition = condition + "\n\t\t\t\t  ON " + linkedAlias + ".card_id = " + alias+".number_value \n\t\t\t\t";
			condition = condition + " AND " + linkedAlias + ".attribute_code = ''{0}'' \n\t\t\t\t AND ";
			sqlBuf.append(MessageFormat.format(condition, new Object[] {StringEscapeUtils.escapeSql(attribute_code)}));
			}
			String conditionValue = formSearchConfigValue(linkedAlias, integerSearchConfigValue);
			sqlBuf.append(conditionValue);
		}
		return sqlBuf.toString();
		
	}

	private String createSqlWords(String value, List attributeCodes, String linkedAlias, boolean strictWords, List<String> sqlArgs, List<Integer> sqlArgTypes){
		StringBuffer sqlBuf = new StringBuffer();
		String nvlWords = StrUtils.nvl(StringEscapeUtils.escapeSql(value), "_");
		
		if (attributeCodes.size() > 0)
		{	// ������� �� ���������
			nvlWords = StrUtils.untagWords(nvlWords);
			String sqlWords = StrUtils.wrapStringForLike(nvlWords);
			sqlBuf.append( MessageFormat.format( 
					"\n\t\t JOIN attribute_value {1} ON ( \n"+
					"\t\t\t 		({1}.card_id = c.card_id) \n"+
					"\t\t\t		AND ( {1}.attribute_code IN ({0}))\n" +
					"\t\t\t		AND ( {1}.string_value IS NOT NULL)\n" +
					"\t\t\t		AND UPPER({1}.string_value) like UPPER(?) \n",
					
					new Object[] {	
							SimpleDBUtils.getAsSqlStrList(attributeCodes),
							linkedAlias
					}
				));
			sqlArgs.add(sqlWords);
			sqlArgTypes.add(java.sql.Types.VARCHAR);
			sqlBuf.append( "\t\t ) \n" ); // (*) JOIN (...)
		} else {			
			DoSearch.universalSearch(sqlBuf, nvlWords, "c", strictWords, sqlArgs, sqlArgTypes);			
		}
		return sqlBuf.toString();
	}
	
	private String createSqlStringAttr(String alias, ObjectId id, TextSearchConfigValue searchCfgValue){
		StringBuilder sqlBuf = new StringBuilder();
		String linkedAlias = "link_"+alias;
		String attribute_code = getAttrToString(id);

		if (StrUtils.isStringEmpty(searchCfgValue.value))
			return sqlBuf.toString();

		//String searchValue = StringEscapeUtils.escapeSql(searchCfgValue.value);

		if (!(id instanceof LinkedObjectId)) {
			sqlBuf.append("\n\t\t JOIN attribute_value AS " + alias);
			sqlBuf.append("\n\t\t\t ON " + alias + ".card_id = c.card_id");
			String condition = "\n\t\t\t\t AND ( " + alias +".attribute_code =''{0}'') \n\t\t\t\t AND ";
			sqlBuf.append(MessageFormat.format(condition, new Object[]{StringEscapeUtils.escapeSql(attribute_code)}));
			String conditionValue = formSearchConfigValue(alias, searchCfgValue);
			sqlBuf.append(conditionValue);
			
		} else {
			LinkedObjectId linkedObjectId = (LinkedObjectId)id;
			if(linkedObjectId.isBackLinked()) {
				sqlBuf.append( createSqlBackLinkAttr(id, alias, linkedAlias));
				sqlBuf.append(  MessageFormat.format("\n\t\t JOIN attribute_value as {0} ON {0}.card_id = {1}.card_id "
										+"\t AND {0}.attribute_code=''{2}'' \n ",
					new Object[] {	
					alias,
					linkedAlias,
					StringEscapeUtils.escapeSql(attribute_code)
				}
				));
				sqlBuf.append("\n\t\t\t\t AND ");
			} else {
				sqlBuf.append("\n\t\t JOIN attribute_value AS " + alias);
				sqlBuf.append("\n\t\t\t ON " + alias + ".card_id = c.card_id");
			String condition = "\n\t\t\t\t AND ( " + alias +".attribute_code =''" + ((LinkedObjectId)id).getLinkedCode() + "'') \n\t\t\t\t";
			condition = condition + "\n\t\t\t\t  JOIN  attribute_value AS " + linkedAlias;
			condition = condition + "\n\t\t\t\t  ON " + linkedAlias + ".card_id = " + alias+".number_value \n\t\t\t\t";
			condition = condition + " AND " + linkedAlias + ".attribute_code = ''{0}'' \n\t\t\t\t AND ";
			sqlBuf.append(MessageFormat.format(condition, new Object[] {StringEscapeUtils.escapeSql(attribute_code)}));
			}
			String conditionValue = formSearchConfigValue(linkedAlias, searchCfgValue);
			sqlBuf.append(conditionValue);
		}
		return sqlBuf.toString();
	}
	
	private String getAttrToString(ObjectId id){
		return id.getId().toString();		
	}


	/**
	 * @param dt
	 * @return true, ���� dt ������ �����.
	 */
	private boolean isStartOfTheDay(final Date dt) {
		// � �����-�� ���� ������� �����-�� true, ���� dt ����� 
		// ������� "�������������� �����"
		/*return 		(dt.getHours() == 0)	
					&&	(dt.getMinutes() == 0)
					&&	(dt.getSeconds() == 0);
		 */
		final Calendar c = Calendar.getInstance();
		c.setTime(dt);
		return	(c.get(Calendar.HOUR) == 0)
			&&	(c.get(Calendar.MINUTE) == 0)
			&&	(c.get(Calendar.SECOND) == 0);
	}
	
	private Date tomorrow(final Date dt) {
		final Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}
	
	private String getAttrIdsStringFromObjectId(ObjectId id){
		StringBuffer result = new StringBuffer();
		result.append("\'").append(id.getId().toString()).append("\'");
		if(MultipleObjectId.class.isAssignableFrom(id.getClass())){
			Iterator<ObjectId> i = ((MultipleObjectId)id).getExtraAtrrIds() != null?
					((MultipleObjectId)id).getExtraAtrrIds().iterator():null;
			if(i!=null && ((MultipleObjectId)id).isEnableExtraAttrIds()){
				while(i.hasNext()){
					result.append(",").append("\'").append(StringEscapeUtils.escapeSql(i.next().getId().toString())).append("\'");
				}
			} 
		}
		return result.toString();
	}
	
	private void emmitRouteSearchAttribute(StringBuffer sqlBuff, Collection attributes, String baseCardAlias){
		for( Iterator itr = attributes.iterator(); itr.hasNext(); ) 
		{
			final Map.Entry attr = (Map.Entry) itr.next();
			final ObjectId attrId = (ObjectId)attr.getKey();
			final Object attrValue = attr.getValue();
	
			if (attrId instanceof RouteSearchObjectId) {
				RouteSearchObjectId routeSearchObjectId = (RouteSearchObjectId) attrId;
				//���� �������� �������� ������ ������������� ��� ��������� �������� (Search.FullySatisfyingSearchAttribute.isFull() == true),
				// �� ������ ��������������� �������
				//�.�. ���� "��� �������� ������ ���� �����", �� ������ ������� "�� ������ ���� �����������"
				if(attrValue instanceof Search.FullySatisfyingSearchAttribute){
					if(((Search.FullySatisfyingSearchAttribute) attrValue).isFull()) {
						sqlBuff.append("\t AND NOT EXISTS( \n");
					} else {
						sqlBuff.append("\t AND EXISTS( \n");
					}
				} else {
					sqlBuff.append("\t AND EXISTS( \n");
				}

				sqlBuff.append("\t\t SELECT 1 FROM card c_route\n");
				List<String> chechingAliases = generateJoinStatmentByRoutes(sqlBuff, routeSearchObjectId.getRoutes(), "c_route");
				generateWhereStRouteSearchAttribute(sqlBuff, baseCardAlias, chechingAliases, attrId, attrValue);
				sqlBuff.append("\t ) \n"); 
			}
			
		}
	}
	
	static List<String> generateJoinStatmentByRoutes(StringBuffer sqlBuff, List<List<RouteSearchNode>> routes, String baseCardTableAlias){
		int join_index = 0;
		List<String> chechingAliases = new ArrayList<String>();
		for(List<RouteSearchNode> sortAttrPath: routes){
			String inNumberAlias = baseCardTableAlias + ".card_id";
			for(RouteSearchNode attr: sortAttrPath){
				if(inNumberAlias != null){
					StringBuilder singlePathSql = new StringBuilder();
					inNumberAlias = generateJoinStatmentByNode(singlePathSql, inNumberAlias, attr, join_index);
					sqlBuff.append(singlePathSql.toString());
					join_index++;
				}
			}
			if(inNumberAlias != null){
				chechingAliases.add(inNumberAlias);
			}
		}
		return chechingAliases;
	}
	
	static String generateJoinStatmentByNode(StringBuilder buff, String inNumberAlias, RouteSearchNode attrId, int join_index){
		StringBuilder singleNodeSql = new StringBuilder();
		String alias;
		String outNumberAlias;
		
		if(BackLinkAttribute.class.equals(attrId.getLinkAttr().getType())){//������ ������� �������� (��� UPLINK)
			alias = getCardAlias(join_index);//{2}
			singleNodeSql.append("\t\t left join attribute_option {2}_ao on {2}_ao.attribute_code = ''{1}'' and {2}_ao.option_code = ''LINK'' \n");
			singleNodeSql.append("\t\t left join attribute_value {2} on {2}.number_value = {0} and {2}.attribute_code = {2}_ao.option_value \n");
			if(attrId.hasValidStatuses()){
				singleNodeSql.append("\t\t left join card {2}_st on {2}.card_id = {2}_st.card_id and {2}_st.status_id in ({3}) \n");
				outNumberAlias = alias + "_st.card_id";
			} else {
				outNumberAlias = alias + ".card_id";
			}
		} else if (CardLinkAttribute.class.equals(attrId.getLinkAttr().getType())
				|| TypedCardLinkAttribute.class.equals(attrId.getLinkAttr().getType())
				|| DatedTypedCardLinkAttribute.class.equals(attrId.getLinkAttr().getType())){
			alias = getAttributeValueAlias(join_index);
			singleNodeSql.append("\t\t LEFT JOIN attribute_value {2} on {0} = {2}.card_id and {2}.attribute_code = ''{1}''\n");
			if(attrId.hasValidStatuses()){
				singleNodeSql.append("\t\t left join card {2}_st on {2}.number_value = {2}_st.card_id and {2}_st.status_id in ({3}) \n");
				outNumberAlias = alias + "_st.card_id";
			} else {
				outNumberAlias = alias + ".number_value";
			}
		} else if(PersonAttribute.class.equals(attrId.getLinkAttr().getType()) && !attrId.isLastNodeFlag()){
			alias = getAttributeValueAlias(join_index);
			singleNodeSql.append("\t\t LEFT JOIN attribute_value {2} on {0} = {2}.card_id and {2}.attribute_code = ''{1}''\n");
			singleNodeSql.append("\t\t LEFT JOIN person {2}_person on {2}_person.person_id = {2}.number_value\n");
			outNumberAlias = alias+"_person.card_id";
		} else {
			alias = getAttributeValueAlias(join_index);
			singleNodeSql.append("\t\t LEFT JOIN attribute_value {2} on {0} = {2}.card_id and {2}.attribute_code = ''{1}''\n");
			outNumberAlias = alias;
		}
		buff.append(MessageFormat.format(singleNodeSql.toString(), new Object[]{inNumberAlias, attrId.getLinkAttr().getId(), alias, 
																ObjectIdUtils.numericIdsToCommaDelimitedString(attrId.getValidStatuses())}));
		return outNumberAlias;
	}
	
	private static String generateWhereStRouteSearchAttribute(StringBuffer buff, String baseCardTableAlias,
	                                                          List<String> checkingAliases, ObjectId attrId, Object attrValue){
		buff.append("\t\t where " + baseCardTableAlias+".card_id = c_route.card_id \n");
		final Class clazz = attrValue.getClass();
		if (NumericIdList.class.equals(clazz)) {
			NumericIdList list = (NumericIdList) attrValue;
			generateWhereSt(buff, checkingAliases, "number_value", list.getCommaDelimitedString());
		} else if (attrValue instanceof Search.FullySatisfyingSearchAttribute) {
			generateWhereStForEmptyOrExistsValue(buff, checkingAliases, defineAttributeColumnValue(attrId),
					(Search.FullySatisfyingSearchAttribute)attrValue);
		}
		return "";
	}

	private static void generateWhereSt(StringBuffer buff, List<String> checkingAliases,
	                                    String column, String strValue) {
		buff.append("\t\t AND ( \n");
		for(Iterator<String> i = checkingAliases.iterator();i.hasNext();){
			String checkingAlias = i.next();
			checkingAlias = checkingAlias.split("\\.")[0]; //�������� ������ alias, ��� �������
			buff.append("\t\t " + checkingAlias + "." + column + " in (" + strValue + ")");
			if(i.hasNext()){
				buff.append(" OR \n");
			}
		}
		buff.append(") \n");
	}

	private static void generateWhereStForEmptyOrExistsValue(StringBuffer buff,  List<String> checkingAliases,
	                                    String column, Search.FullySatisfyingSearchAttribute value) {
		buff.append("\t\t AND ( \n");
		for(Iterator<String> i = checkingAliases.iterator();i.hasNext();){
			String checkingAlias = i.next().split("\\.")[0]; //�������� ������ alias, ��� �������
			buff.append("\t\t " + checkingAlias + "." + column + " " + value.getCondition());
			if (i.hasNext()){
				buff.append(" OR \n");
			}
		}
		buff.append(") \n");
	}
	
	static private String getCardAlias(int i){
		return "search_c_"+ i;
	}
	
	static private String getAttributeValueAlias(int i){
		return "search_av_"+ i;
	}

	private static String defineAttributeColumnValue (ObjectId id){
		if(id.getType().isAssignableFrom(CardLinkAttribute.class)
				|| id.getType().isAssignableFrom(PersonAttribute.class)
				|| id.getType().isAssignableFrom(IntegerAttribute.class)){
			return "number_value";
		} else if (id.getType().isAssignableFrom(HtmlAttribute.class)){
			return "long_binary_value";
		} else if (id.getType().isAssignableFrom(StringAttribute.class)){
			return "string_value";
		} else if (id.getType().isAssignableFrom(ReferenceAttribute.class)){
			return "value_id";
		}
		return null;
	}

	private String getFilteredObjectIdString(ObjectId id, String alias){
		if(id instanceof FilteredObjectId){
			StringBuffer sqlBuf = new StringBuffer();
			FilteredObjectId filteredObjectId = (FilteredObjectId) id;
			if(!filteredObjectId.getLinkedCardStatus().isEmpty()){
				sqlBuf.append( "\n\t\t\t\t  JOIN  card AS filtered_c  ON filtered_c.card_id = " + alias+" AND");
				sqlBuf.append("\n\t\t\t\t  filtered_c.status_id " + (filteredObjectId.isReverseCardStatus() ? "not in" : "in") + " ("
						+ ObjectIdUtils.numericIdsToCommaDelimitedString(filteredObjectId.getLinkedCardStatus())
						+ ")");
			}
			return sqlBuf.toString();
		} else {
			return "";
		}
	}
}