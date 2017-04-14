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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.EmptyAttribute;
import com.aplana.dbmi.action.Search.Filter;
import com.aplana.dbmi.action.Search.NumericIdList;
import com.aplana.dbmi.action.Search.SearchTag;
import com.aplana.dbmi.action.SearchInFiles;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;
import com.aplana.dbmi.utils.SimpleDBUtils;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Query used to perform {@link Search} action
 */
@SuppressWarnings("all")
public class DoSearch extends ActionQueryBase {
	private static final long serialVersionUID = 1L;
	
	private static final String ACTION_DOWNLOAD = "download";
	private static final String CONFIG_SQL_FILE_PREFIX = "dbmi/searchSQL/";
	private static final String CONFIG_SQL_PARAMS_FILE_PREFIX = "parameters/";
	public enum AggregateFunction{min, max, empty};

	/**
	 * ����� ���������� SQL �������� (� ��), ���������� �������� �� ���������� � ����� ���������������.
	 */
	static final long NORMAL_EXEC_TIME_MS = 1000;

	Future<Integer> futureCount = null;
	ExecutorService pool = null;

	/**
	 * Performs given {@link Search} action
	 *
	 * @return {@link SearchResult} object representing result of {@link Search}
	 *         action
	 */
	public SearchResult processQuery() throws DataException {
		// (2009/12/05, RuSA) �������� ��������� ������ "�� ����" ...
		final ManagerTempTables mgrTmpTables = new ManagerTempTables(this.getJdbcTemplate());
		
		SearchResult searchResult;
		final Search search = getAction();

		if (search.isByMaterial()) {
			mgrTmpTables.startAll();
		}
		try {

			if (logger.isDebugEnabled()) {
				logSearchInfo(search);
			}

			final long timeStartGetId = System.currentTimeMillis();

			final Search.Filter filter = ((Search)getAction()).getFilter();
			if (filter!= null)
				filter.setWholeSize(-1);

			List<ObjectId> cardIds = null; // of ObjectId( Card, Long);
			Search finalSearch = search;
			String tag = "default";
			if (search.isBySql()) {
				tag = "bySQL";
				cardIds = searchBySQL( mgrTmpTables, search, search.getSqlXmlName());
			} else if (search.isByAttributes() && search.isByMaterial()) {
				tag = "byAttrAndMaterial";
				//In case to search by material and attributes we need to find a 'text' in attached files   
				Collection<ObjectId> files = getResultSearchByMaterial(search.getWords());
				if (search.getDocLinkAttribute() == null)
					throw new RuntimeException("It is impossible to perform search by attributes and material. DocLinkAttribute was not defined!");
				//clean words as it was used for searching in attachments 
				search.setWords("");
				
				if (files != null) {
					//then we add new restrictions to search by attributes : add only cards that had already been found by search at attachments  
					for(ObjectId fileCardId : files) {
						search.addCardLinkAttribute(search.getDocLinkAttribute(), fileCardId);
					}	
					search.setByMaterial(false);
					final ExecSearchByAttributes executor 
						= new ExecSearchByAttributes(getJdbcTemplate(), mgrTmpTables); 
					cardIds = executor.execute(search, getUser());
				
				}
				//finalSearch = null; // �.�. ��� ������� (��� ���� ������, �� ������ � ���� �������� ���� �� ��������, ������� ���� �� ���� ������� ���������� ��������)
			} else if (search.isByAttributes()) {
				tag = "byAttr";
				ExecSearchByAttributes proc = new ExecSearchByAttributes(getJdbcTemplate(), mgrTmpTables);
				cardIds = proc.execute(search, getUser());
				//finalSearch = null; // �.�. ��� ������� (��� ���� ������, �� ������ � ���� �������� ���� �� ��������, ������� ���� �� ���� ������� ���������� ��������)				
			} else if (search.isByCode()) {
				tag = "byCode";
				cardIds = searchByCodes(search.getWords());
			} else if (search.isByMaterial()) {
				tag = "byMaterial";
				searchByMaterial(mgrTmpTables, search.getWords());
			}
			
			final long timeEnd1 = System.currentTimeMillis();
			final long timeDurationGetId = timeEnd1 - timeStartGetId;
			if (timeDurationGetId > NORMAL_EXEC_TIME_MS)
				logger.warn("getCardsIds("+ tag+"," + search.getSqlXmlName()+ ")   stage is "+ timeDurationGetId + " ms long \n"
						+ "\t cardIdsList.count="+ ((cardIds!=null) ? String.valueOf(cardIds.size()): "null")
						);

			if (search.isDontFetch())
				// ��������� ������ ���������� ��� ����� �������� �������� ...
				return null;
			
			removeIgnoredIds(cardIds, search.getIgnoredIds());

			if (search.getFetchLink() != null) {
				cardIds = replaceFoundByLinked(cardIds, search.getFetchLink());
			}

			// (!) Fetch/Load cards ...
			if (cardIds == null || cardIds.size() > ManagerTempTables.MIN_EFFECTIVE_COUNT){
				//fetch using temp tables
				if ( !mgrTmpTables.isStarted() ) {
					mgrTmpTables.startAll();
				}
				if (cardIds != null) {
					mgrTmpTables.insertCardIds(cardIds);
					cardIds = null;
				}
				final ExecFetchCards fetcher = new ExecFetchCardsEx(getJdbcTemplate(), getUser(),
						finalSearch, getSessionId());
				fetcher.setResultColumns( search.getColumns(), true);
				searchResult = fetcher.execute();
			} else {
				//fetch using list
				final ExecFetchCardsFromIdsArray fetcher = new ExecFetchCardsFromIdsArray(
						getJdbcTemplate(), getUser(), finalSearch, getSessionId());
				fetcher.setResultColumns( search.getColumns(), true);
				fetcher.setObjectIds(cardIds);
				searchResult = fetcher.execute();
			}

			correctHoleSize( searchResult, search.getFilter());
			postInit(search, searchResult);

		} catch (BadSqlGrammarException e) {
			logger.error("BadSqlGrammar: ", e);
			throw new DataException("action.search.wrong.query", new Object[] {StrUtils.untagWords(search.getWords())}, e);
		} catch (DataException e) {
			logger.error("processQuery: ", e);
			if (e.getCause() instanceof BadSqlGrammarException) {
				throw new DataException("action.search.wrong.query", new Object[] {StrUtils.untagWords(search.getWords())}, e);
			}
			throw e;
		} catch (Exception e) {
			logger.error("processQuery: ", e);
			throw new DataException(e.getMessage(), e);
		} finally {
			if (mgrTmpTables.isStarted()){
				mgrTmpTables.close();
			}
		}

		/*
		 * (2009/12/11, RuSA) ���� ���� ������� �� �������� ��������� ��������
		 * labelASttributes ��� CardLinkAttribute's. � �.�. ������ processLabelsInAttributes
		 * ���� ����� FetchCards, �� ���� ������� ��������� ������� �� ������
		 * (��. ���� ����: finally manTmp.close).
		 */
		processLabelsInAttributes(searchResult);

		return searchResult;
	}

	/**
	 * @param search
	 */
	private void logSearchInfo(final Search search) {
		final StringBuffer info = new StringBuffer("Search requested:\r\n");
		info.append("\t\tWords: " + search.getWords() + "\r\n");
		info.append("\t\tBy:" + (search.isByCode() ? " code" : "") +
				(search.isByAttributes() ? " attributes" : "") +
				(search.isByMaterial() ? " material" : "") +
				(search.isBySql() ? " sql" : "") + "\r\n");
		info.append("\t\tTemplates: " + search.getTemplates() + "\r\n");
		info.append("\t\tStates: " + search.getStates() + "\r\n");
		if (search.getAttributes() != null)
			for (Iterator<?> itr = search.getAttributes().iterator(); itr.hasNext(); ) {
				Map.Entry<?, ?> attr = (Map.Entry<?,?>) itr.next();
				info.append("\t\t\t" + attr.getKey());
				if (Boolean.class.equals(attr.getValue().getClass()))
					info.append(" contains words");
				else if (Search.DatePeriod.class.equals(attr.getValue().getClass())) {
					Search.DatePeriod period = (Search.DatePeriod) attr.getValue();
					info.append(" from " + period.start + " to " + period.end);
				} else if (Search.Interval.class.equals(attr.getValue().getClass())) {
					Search.Interval interval = (Search.Interval) attr.getValue();
					info.append(" from " + interval.min + " to " + interval.max);
				} else if (Collection.class.isAssignableFrom(attr.getValue().getClass()))
					info.append(" in " + attr.getValue());
				else if (Person.ID_CURRENT.equals(attr.getValue()))
					info.append(" is current user");
				else if (NumericIdList.class.equals(attr.getValue().getClass()))
					info.append(" in " + ((NumericIdList) attr.getValue()).getCommaDelimitedString());
				else if (EmptyAttribute.class.equals(attr.getValue().getClass()))
					info.append(" is empty");
				else if (String.class.equals(attr.getValue().getClass()))
					info.append(" = " + attr.getValue());
				else
					info.append(" (unknown filter: " + attr.getValue() + ")");
				info.append("\r\n");
			}
		logger.debug(info);
	}


	/**
	 * @param result
	 * @param filter 
	 */
	private void correctHoleSize(SearchResult result, Filter filter) {
		if (result == null || filter == null) return;

		int curCount = (result.getCards() == null) ? 0: result.getCards().size();
		final int pgSize = filter.getPageSize();
		if ( (result.getCards() != null)
			&& !filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS)) {
			// ���� ������ �� ������
			curCount = countWithAnyRights( result.getCards() );
			if ( filter.getWholeSize() == -1 || curCount > filter.getWholeSize()
				)
				filter.setWholeSize( curCount);
		}

		if ( 	(filter.getWholeSize() == -1)
				|| ( (pgSize == 0) && (curCount < filter.getWholeSize()) ) // ��� �������� -> ����� ���-�� �� ����� ��������� ������� ...
			)
		{
			filter.setPage(1);
			filter.setWholeSize(curCount);
		} else if ( (pgSize > 0) && (curCount < pgSize) && filter.getPage() > 0) {
			// ��������� ��������...
			filter.setWholeSize( curCount + pgSize * (filter.getPage() - 1) );
		} 

		if (filter.getPage() * pgSize > filter.getWholeSize() && pgSize > 0) {
			filter.setPage(filter.getWholeSize()/pgSize + 1);
		}
	}

	/**
	 * ������� ���-�� �������� , �� ������� ���� �����.
	 * @param cards
	 * @return
	 */
	private int countWithAnyRights(List<?> cards) {
		if (cards == null) return 0;
		int count = 0;
		for (Iterator<?> iterator = cards.iterator(); iterator.hasNext();) {
			final Card card= (Card) iterator.next();
			if (card == null || !(card.getCanRead() || card.getCanWrite()) )
				continue;
			count++;
		}
		return count;
	}


	private String getSQLLinkedCards(ObjectId fetchLink, String ids)
	{
		String query = null;
		if (CardLinkAttribute.class.equals(fetchLink.getType()))
			query =
				"SELECT number_value FROM attribute_value " +
				"WHERE card_id IN (" + ids + ") " +
				"AND attribute_code=?";
		else if (BackLinkAttribute.class.equals(fetchLink.getType()))
			// TODO: ���� ������������ ����� �������� functionbacklink 
			query =
				"SELECT card_id FROM attribute_value " +
				"WHERE number_value IN (" + ids + ") " +
				"AND attribute_code=" +
				"(SELECT option_value FROM attribute_option " +
				"WHERE option_code='LINK' AND attribute_code=?)";
		return query;
	}

	/**
	 * ��������� ������ ��� ��������.
	 * @param cardIds: ������ ObjectId ��������, ���� null, �� ���������� id 
	 * �������� ����� ��������� ������� gtemp_cardid.
	 * @param fetchLink: ������ ���� C/E/B, �� ������� ���� "������" ��� ���������
	 * ��������� ��������.
	 * @return ���� �� ����� cardIds == null, �� ��������� null � ������ ����������
	 * ����� gtemp_cardid ����� ���-�� ������ ObjectId(Card) ��������� ��������.
	 */
	private List<ObjectId> replaceFoundByLinked(List<ObjectId> cardIds, ObjectId fetchLink) {
		final boolean useTempTable = (cardIds == null);

		final StringBuffer found = new StringBuffer();
		if (useTempTable) {
			// (!) ��� ������ -> ���� ����� ��� �� ���� �������.
			getJdbcTemplate().query( "SELECT card_id FROM gtemp_cardid", 
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						if (found.length() > 0)
							found.append(',');
						found.append(rs.getString(1));
					}
				});
			getJdbcTemplate().update("DELETE FROM gtemp_cardid");
		} else {
			for (Iterator<ObjectId> iter = cardIds.iterator(); iter.hasNext(); ) {
				if (found.length() > 0)
					found.append(',');
				found.append(iter.next().getId());
			}
		}

		if (found.length() == 0)
			return null;

		final String sql = getSQLLinkedCards(fetchLink, found.toString());

		if (useTempTable) {
			getJdbcTemplate().update("INSERT INTO gtemp_cardid (card_id)" + sql,
				new Object[] { fetchLink.getId() });
			return null;
		}

		@SuppressWarnings("unchecked")
		final List<ObjectId> ids = getJdbcTemplate().query( sql,
				new Object[] { fetchLink.getId() }, new RowMapper(){
			public Object mapRow(ResultSet rs,
					int rowNum) throws SQLException 
			{
				final Object row = new ObjectId(Card.class, rs.getLong(1)); // � ���������� ����� ���� ������� �����
				return row;
			}
		});
		return ids;
	}

	static Object[] addElement(Object[] src, Object newElem) {
		final int srcLen = src.length;
		final Object[] result = new Object[srcLen + 1];
		System.arraycopy(src, 0, result, 0, srcLen);
		result[srcLen] = newElem;
		return result;
	}

	static int[] addElement( int[] src, int newElem )
	{
		final int srcLen = src.length;
		final int[] result = new int[srcLen + 1];
		System.arraycopy(src, 0, result, 0, srcLen);
		result[srcLen] = newElem;
		return result;
	}

	private void postInit(Search search, SearchResult searchResult)
	{
		searchResult.setNameRu(search.getNameRu());
		searchResult.setNameEn(search.getNameEn());
		if (searchResult.getNameRu() == null
				|| searchResult.getNameRu().length() == 0)
			searchResult.setNameRu(getResourceMessage("ru", search
					.getWords() == null ? "search.name.numbers"
					: "search.name.words", search.getWords()));
		if (searchResult.getNameEn() == null
				|| searchResult.getNameEn().length() == 0)
			searchResult.setNameEn(getResourceMessage("en", search
					.getWords() == null ? "search.name.numbers"
					: "search.name.words", search.getWords()));


		if (checkMaterialInfoMustBeCreated(search)) {

			final StringBuffer sql = new StringBuffer();
			sql.append("SELECT c.file_name     AS FILE_NAME, ");
			sql.append("       c.external_path AS EXTERNAL_PATH ");
			sql.append("FROM   card c ");
			sql.append("WHERE  c.card_id=?");

			// prepare
			final Collection<Card> cards = searchResult.getCards();
			for (Iterator<Card> iterator = cards.iterator(); iterator.hasNext();) {
				Card card = iterator.next();

				final Map<?,?> queryForMap = getJdbcTemplate().queryForMap(
						sql.toString(), new Object[] { card.getId().getId() });
				final String fileName = (String) queryForMap.get("FILE_NAME");
				final String externalPath = (String) queryForMap.get("EXTERNAL_PATH");

				if (fileName != null) {
					addMaterialAttributeFile(card, fileName);
				} else if (externalPath != null) {
					addMaterialAttributeUrl(card, externalPath);
				}

			}
		}
	}

	/**
	 * �������� label-��������� � CardLinkAttribute's.
	 * @param sr: ����������� ���������
	 * @return true, ���� ���� ��������-label � ��� ���������.
	 * @throws DataException
	 */
	boolean processLabelsInAttributes( SearchResult sr)
		throws DataException
	{
		if (sr == null || sr.getColumns() == null) {
			return false;
		}

		boolean found = false;
		for (final SearchResult.Column col : safeGetColumns( sr)) {
			if (col != null && col.getLabelAttrId() != null) {
				found = true;
				processLabledColumn(col, sr);
			}
		}

		return found;
	}

	/**
	 * @param column
	 * @param sr
	 * @throws DataException
	 */
	private void processLabledColumn(SearchResult.Column column, SearchResult sr)
		throws DataException
	{
		if ( sr==null || sr.getCards() == null) return;

		// ������ ������ �������� � ����������� ��������� ��� ������� label-������� 
		final List<Card> labelCardList = (List<Card>) sr.getCardsListForLabelColumn(column);
		// �������� �� ��������� ���� �������� � ������ ������ "��������"...
		//
		final ObjectId labelId = column.getLabelAttrId();
		if (labelId == null) return;

		final ObjectId colAttrId = column.getAttributeId();

		final ColumnAttributeSubQuery subQuery = new ColumnAttributeSubQuery(column);

		// ���������� ��������, � ������� ���� ����� �������
		// �������� �� ��������� �������� ������ � ����������� ��������� colAttrId 
		for (final Card card : labelCardList) {
			final Attribute linkAttr = card.getAttributeById(colAttrId);
			if (linkAttr != null && (linkAttr instanceof LinkAttribute || linkAttr instanceof PersonAttribute)) // �������� � �������� ��� ��������� ��������
				subQuery.regCardsIds(linkAttr);
		}

		// TODO: �����??? - (2011/02/05, RuSA) ����� ��������� ������������, ��������, �������� ��� ��������� ��� ��������� ���� (��� ����) ���-��������� �� �������� "���������".
		// ��������� � ������ �������� � �������� �������� (��� ��������� jsp ���������� ���������� ��������� � label-��������� � �������� ��������)
		// PersonAttribute ����� ��������� ������
		for (final Card card : safeGetCards(sr)) {
			final Attribute linkAttr = card.getAttributeById(colAttrId);
			if (linkAttr != null && (linkAttr instanceof LinkAttribute))
				subQuery.regCardsIds(linkAttr);
		}

		// ���������� � ���������...
		subQuery.process();
	}

	/**
	 * ��������� � ����������� ��� �������� ������ � �������������� ���������,
	 * ������� ���� ��������� ���������.
	 * @author RAbdullin
	 *
	 */
	class ColumnAttributeSubQuery {

		// �������, � ������� ���������� labelAttrId != null
		SearchResult.Column column;

		// ������ ��������� ��� �������� (id ����� �����������)
		final List<Attribute> links = new ArrayList<Attribute>();

		// id ��������
		final Set<ObjectId>/*CardId*/ cardIds = new HashSet<ObjectId>();

		final List<ObjectId>/*<CardId>*/ pathToLabelAttr = new ArrayList<ObjectId>();

		ColumnAttributeSubQuery(SearchResult.Column column) {
			this.column = column;
			if (column.getPathToLabelAttr() != null)
				pathToLabelAttr.addAll(column.getPathToLabelAttr());
		}

		/*
		 * ��������� ������ �� ��������� ���������.
		 * ��������� ��������� �������� ����� ��������.
		 * @throws DataException
		 */
		void process() throws DataException {
			final Map<ObjectId, Card> answer = executeLoadAttributes();
			setAttributesFrom(answer);
		}

		/**
		 * ���������� � �������� cards �������� �� loaded � id-������� ���������
		 * �� attributes.
		 * @param loaded
		 */
		private void setAttributesFrom (Map<ObjectId, Card> mapCards) {
			// ��������� �������� �������� �������� ������� �� �����������
			if (column == null) return;
			for (final Attribute attr : links) {
				if (attr instanceof LinkAttribute) {
					final Map<ObjectId, String> map = ((LinkAttribute) attr).getLabelLinkedMap();
					for (final Map.Entry<ObjectId, String> link : map.entrySet() ) {
						final Card linked = mapCards.get(link.getKey());
						if (linked != null) {
							final Attribute labelAttr = linked.getAttributeById(column.getLabelAttrId());
							if (labelAttr != null) {
								//���� ��� ������� � �������� ���� ListAttributes, �� � �������� �������� ����� id'��� ReferenceValue
								if (column.isIcon() && Attribute.TYPE_LIST.equals(labelAttr.getType())) {
									link.setValue(String.valueOf(((ListAttribute) labelAttr).getValue().getId().getId()));
								} else {
									link.setValue(labelAttr.getStringValue());
								}
							} else {
								link.setValue(null);
							}
						} else {	// ����� ����� ����
							link.setValue(null);
						}
					}
				} else if (attr instanceof PersonAttribute) {
					Collection<Person> values = ((PersonAttribute) attr).getValues();
					for (Iterator<Person> itrV = values.iterator(); itrV.hasNext(); ) {
						final Person item = itrV.next();
						Card linked = mapCards.get(item.getCardId());
						if (linked != null) {
							final Attribute labelAttr = linked.getAttributeById(column.getLabelAttrId());
							if (labelAttr != null) {				
								item.setFullName(labelAttr.getStringValue());
							} else {
								item.setFullName(null);
							}
						} else {
							item.setFullName(null);
						}
					}
				}
			}
		}

		public void regCardsIds(Attribute attr) {
			final boolean isLink = (attr instanceof LinkAttribute);
			final boolean isPerson = (attr instanceof PersonAttribute);
			if( isLink && ( (LinkAttribute)attr ).getLinkedCount() == 0 )
				return;
			this.links.add(attr);
			if (isLink)
				this.cardIds.addAll(((LinkAttribute)attr).getIdsLinked());
			else if (isPerson) {
				Collection<Person> values = ((PersonAttribute)attr).getValues();
				for( Iterator<Person> itr = values.iterator(); itr.hasNext(); )
				{
					final Person item = itr.next();
					this.cardIds.add(item.getCardId());
			}
			}
		}

		private Map<ObjectId, Card> executeLoadAttributes() throws DataException {
			final Map<ObjectId, Card> loaded = new HashMap<ObjectId, Card>();
			// TODO: (2010/1226, RuSA) ����������� � ����� search/setName.
			if (this.pathToLabelAttr.size() == 0) {
				// �������� ������ �������� ���� �������� cardIds � ��������� column.getLabelAttrId() 
				final Search search = makeSearchObj(cardIds, column.getLabelAttrId(), column);
				final ExecFetchCardsFromIdsArray fetcher = new ExecFetchCardsFromIdsArray(
						getJdbcTemplate(), getUser(), search, getSessionId());
				fetcher.setResultColumns( search.getColumns(), true);
				fetcher.setObjectIds( new ArrayList<ObjectId>(cardIds));
				final SearchResult res = fetcher.execute();
				// ��� �������� ������� �������� ������� �� ������� �� ��������� ��������� ������� � ���� pathToLabelAttr
				if (res != null) {
					final SearchResult.Column curCol = (SearchResult.Column)res.getColumns().toArray()[0];
					// (YNikitin, 2011/04/20) ����� ������� ������, ������ ���� �� ����� ���� ������������� ����� ������������ �������
					//(NGaleev, 2011/05/20) ��� �� ������ �������� ������� ����� � xml �������������
					if (!column.getIsParentName() && !column.isUseGivenTitle()){
						column.setNameRu(curCol.getNameRu());		
						column.setNameEn(curCol.getNameEn());
					}

					if (res.getCards() != null) {
						// ������ ����� ����������� ��������� ��������
						for( final Card card : safeGetCards( res)) { 
							loaded.put(card.getId(), card);
						}
					}
				}
				return loaded;
			}

			Map<ObjectId, List<ObjectId>> linksMap = 
					new HashMap<ObjectId, List<ObjectId>>();
			final Set<ObjectId> cardIds_int = new HashSet<ObjectId>(cardIds);
			for ( ObjectId id : cardIds_int) {
				final List<ObjectId> id_list = new ArrayList<ObjectId>();
				id_list.add(id);
				linksMap.put(id, id_list);
			}

			while (!this.pathToLabelAttr.isEmpty()) {
				final Search search = 
					makeSearchObj(cardIds_int, this.pathToLabelAttr.get(0), null);
			final ActionQueryBase query = getQueryFactory().getActionQuery(search);
			query.setAction(search);
				final SearchResult res = 
					(SearchResult) getDatabase().executeQuery(getUser(), query);
				// ��� �������� ������� �������� ������� �� ������� �� ��������� ��������� ������� � ���� pathToLabelAttr
				final SearchResult.Column curCol = (SearchResult.Column)res.getColumns().toArray()[0]; 
				// (YNikitin, 2011/04/20) ����� ������� ������, ������ ���� �� ����� ���� ������������� ����� ������������ �������
				//(NGaleev, 2011/05/20) ��� �� ������ �������� ������� ����� � xml �������������
				if (!column.getIsParentName() && !column.isUseGivenTitle()){ 
					column.setNameRu(curCol.getNameRu());		
					column.setNameEn(curCol.getNameEn());
				}
				cardIds_int.clear();
				/* (OKravchenko,2011/06/15) � linksMap � temp_links � �������� 
				 * �������� ����� ������������ ������ id-������ ��������, � 
				 * ������� ���� cardLink �� id-�����, ��������� � �������� �����
				 */
				final Map<ObjectId, List<ObjectId>> temp_links = 
					new HashMap<ObjectId, List<ObjectId>>();
				for (Card card : safeGetCards(res) ){
					final Attribute attr = card.getAttributeById(this.pathToLabelAttr.get(0));

					final boolean isCardLink = (attr instanceof CardLinkAttribute);
					if (attr ==  null || !( isCardLink))
						continue;
					final Collection<ObjectId> idsLinked = 
							(isCardLink) 	? ((CardLinkAttribute)attr).getIdsLinked()
							: null;
					if (idsLinked != null && !idsLinked.isEmpty()) {
						final ObjectId id = idsLinked.iterator().next();

						final List<ObjectId> temp_collect = temp_links.get(id); // ��� ������� id �������� ��� cardId, ���
						if( temp_collect != null ) 						  // ���� id �����������
							temp_collect.addAll(linksMap.get(card.getId()));
						else // TODO: (11/06.19, RuSA) ��������� �� ����� �� ��� ��������� ������� ������ �������, ������ ������ �� linksMap.get(card.getId()). 
							temp_links.put( id, linksMap.get(card.getId()));

						final List<ObjectId> t_cardIds = linksMap.get(id);
						if( t_cardIds != null ) t_cardIds.add(card.getId());
					}
				}
				cardIds_int.addAll( temp_links.keySet() );
				linksMap = temp_links;
				this.pathToLabelAttr.remove(0);
			}
			final Search search = makeSearchObj(cardIds_int, column.getLabelAttrId(), null);

			// �������� ������ �������� ���� �������� cardIds � ��������� column.getLabelAttrId() 
			final ExecFetchCardsFromIdsArray fetcher = new ExecFetchCardsFromIdsArray(
					getJdbcTemplate(), getUser(), search, getSessionId());
			fetcher.setResultColumns( search.getColumns(), true);
			fetcher.setObjectIds( new ArrayList<ObjectId>(cardIds_int));
			final SearchResult res = fetcher.execute();

			if (res.getCards() != null) {
				for( Card card : safeGetCards(res) ) {
					final List<ObjectId> cardsIds = linksMap.get(card.getId());
					if (cardsIds == null) continue;
					for(ObjectId cid : cardsIds)
						loaded.put(cid, card);
				} // for
		}

			return loaded;
		}

		private Search makeSearchObj(Set<ObjectId> cardsIds, ObjectId labelAttrId, SearchResult.Column column)
		{
			// ��������� ������ Id's (���������) ��������
			final StringBuffer idsBuf = new StringBuffer();
			for (Iterator<ObjectId> iterator = cardsIds.iterator(); iterator.hasNext();) {
				final ObjectId id = iterator.next();
				idsBuf.append(id.getId());
				if (iterator.hasNext()) idsBuf.append(", ");
			}

			// �������� ���������� �������
			final Search search = new Search();
			search.setByCode(true);
			search.setByAttributes(false);
			search.setWords(idsBuf.toString());

			final SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(labelAttrId);
			if(column != null) {
				col.setTimePattern(column.getTimePattern());
			}
			search.setColumns(Collections.singletonList(col));

			return search;
		}

	} // class ColumnAttributeSubQuery

	private void addMaterialAttributeFile(Card card, String fileName) {
		MaterialAttribute ma = new MaterialAttribute();
		ma.setId((String) Attribute.ID_MATERIAL.getId());
		ma.setMaterialType(MaterialAttribute.MATERIAL_FILE);
		ma.setMaterialName(fileName);
		card.getAttributes().add(ma);
	}

	private void addMaterialAttributeUrl(Card card, String externalPath) {
		MaterialAttribute ma = new MaterialAttribute();
		ma.setId((String) Attribute.ID_MATERIAL.getId());
		ma.setMaterialType(MaterialAttribute.MATERIAL_URL);
		ma.setMaterialName(externalPath);
		card.getAttributes().add(ma);
	}

	private boolean checkMaterialInfoMustBeCreated(Search search) {
		Collection<SearchResult.Column> columns = search.getColumns();
		if (columns != null && columns.size() > 0) {
			for (Iterator<SearchResult.Column> iterator = columns.iterator(); iterator.hasNext();) {
				SearchResult.Column column = iterator.next();
				if(column !=null && ACTION_DOWNLOAD.equals(column.getAction())){
					return true;
				}
			}
		}
		return false;
	}

	private List<ObjectId> searchByCodes(final String codes) {
		final List<ObjectId> cardIds = new ArrayList<ObjectId>();
		if (codes != null && !"".equals(codes.trim())) {
			String codesWithoutTag;
			if (codes.startsWith(Search.SearchTag.TAG_SEARCH_ID.toString())) { 
				codesWithoutTag = codes.substring(Search.SearchTag.TAG_SEARCH_ID.toString().length());
			} else {
				codesWithoutTag = codes;
			}
			final String[] numbers = codesWithoutTag.split("\\s*[,;]\\s*");
			/* BEFORE OPTIMIZATION OLD CODE: (!) no access checking performed here */
			for (int i = 0; i < numbers.length; i++) {
				try {
					cardIds.add( new ObjectId(Card.class, Long.valueOf(numbers[i])));
				} catch (NumberFormatException e) {
					if (logger.isWarnEnabled()){
						logger.warn("Card id:" + numbers[i] + " parsing error, skipped: " + e.getMessage());
					}
					continue;
				}
			}
		}
		return cardIds;
	}

	
	/**
	 * ������������� ����� �� ���������� � id.
	 * ������� �� ������ ExecSearchByAttributes
	 * @param sqlBuf - ��������� �������
	 * @param nvlWords - �������� ��� ������
	 */
	final static void universalSearch(StringBuffer sqlBuf, String nvlWords, String cardAlias, boolean strictFlag, List<String> sqlArgs, List<Integer> sqlTypes){
		Search.SearchTag tag = StrUtils.findSearchTag(nvlWords);
		nvlWords = StrUtils.untagWords(nvlWords);
		final String sqlWords;
		
		//���� � ��������� ������ ���� ���� ������ * �� � ����� ������ 
		//(������ ��� �������� ����� ��� �����) ���� ��� �������� ����� �� �����
		if (tag == SearchTag.TAG_SEARCH_FULL_TEXT) {
			sqlWords = nvlWords;
		} else if (!strictFlag || (nvlWords != null && nvlWords.contains(StrUtils.WILDCARD_CHARACTER))) {
			sqlWords = StrUtils.wrapStringForLike(nvlWords);
		} else {
			sqlWords = nvlWords;
		}
		
		if (tag != SearchTag.TAG_SEARCH_ID) {
			// ������ �� ���������, ����� �� ������ ���� ����� ���������: 
			// ���������� ����� �� ������ "������" ��� "���������"...

			// (2010/01, RuSA) ���. ������� ��� ������ ������ �������� � ����� �����
			// (2010/12/08, RuSA) ���������� ������ Exists ����� ������� Join
			sqlBuf.append("\n\t\t JOIN attribute_value av98 ON ( \n");
			sqlBuf.append("\t\t\t 		(av98.card_id = ");
			sqlBuf.append(cardAlias);
			sqlBuf.append(".card_id) \n");
			sqlBuf.append("\t\t\t	AND ( \n");
			
			switch (tag) {
				case TAG_SEARCH_REGNUM:
					sqlBuf.append("\t\t\t	(av98.attribute_code = 'JBR_REGD_REGNUM') and \n");
					break;
				case TAG_SEARCH_TWO_REGNUM:
					sqlBuf.append("\t\t\t	(av98.attribute_code in ('JBR_REGD_REGNUM','JBR_REGD_NUMOUT')) and \n");
					break;
			}
			
			sqlBuf.append("\t\t\t  ((av98.string_value IS NOT NULL) \n");
			
			final String queryFunctionStart;
			final String queryFunctionEnd;
			final String compareFunction;
			final String queryDbFunctionStart;
			final String queryDbFunctionEnd;
			if (tag == SearchTag.TAG_SEARCH_FULL_TEXT && !strictFlag) {
				queryFunctionStart = "create_fts_query(";
				queryFunctionEnd   = ")";
				compareFunction = "@@";
				queryDbFunctionStart = "create_fts_vector(";
				queryDbFunctionEnd   = ")";
			} else if (strictFlag) {
				queryFunctionStart = "upper(substr(";
				queryFunctionEnd   = ",0,2000))";
				compareFunction = "=";
				queryDbFunctionStart = "upper(substr(";
				queryDbFunctionEnd   = ",0,2000))";
			} else {
				queryFunctionStart = "(";
				queryFunctionEnd   = ")";
				compareFunction = "ilike";
				queryDbFunctionStart = "(";
				queryDbFunctionEnd   = ")";
			}
			
			sqlBuf.append(MessageFormat.format( "\t\t\t\t	AND {0} av98.string_value {1} {2} {3} ? {4} \n", 
					queryDbFunctionStart,
					queryDbFunctionEnd,
					compareFunction,
					queryFunctionStart, 
					queryFunctionEnd
					));
			sqlArgs.add(sqlWords);
			sqlTypes.add(java.sql.Types.VARCHAR);
			
			sqlBuf.append("\t\t\t\t  )\n");
		} else { //TAG_SEARCH_ID
			sqlBuf.append("\n\t\t JOIN card av98 ON ( \n");
			sqlBuf.append("\t\t\t 	(av98.card_id = ");
			sqlBuf.append(cardAlias);
			sqlBuf.append(".card_id) \n");
			sqlBuf.append("\t\t\t	AND ( \n");
			sqlBuf.append("\t(av98.card_id = ? ) \n");
			sqlArgs.add(nvlWords);
			sqlTypes.add(java.sql.Types.NUMERIC);
		}
		
		//------------------------------------------------------------------------
		sqlBuf.append( "\t\t\t		) -- AND \n" );
		sqlBuf.append( "\t\t ) -- JOIN \n" ); // (*) JOIN (...)
	}
	
	// TODO: USE DelegatorBean.makeSqlSelectCAWithDelegationsEx(
	/**
	 * �������� � sql �������� ���� �� �������� �������� �������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param filter ��������� ������, ���� null, �� ������ �� �������� � sql.
	 * @param user ������������, ������������ �������� ���� ��������� �����.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitPermissionCheck( sbuf, search.getFilter(), getUser(), "c");
	 */
	final static public boolean emmitPermissionWhere(StringBuffer destSqlBuf, 
			Search.Filter filter, 
			Long personId, 
			String cardAlias)
	{
		if ((filter == null) ||
			(filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS)))
			return false;
		
		if(filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS_FINAL_DOC)){
			destSqlBuf.append("AND( \n");
			destSqlBuf.append("\t" + cardAlias + ".is_active = 1) \n");
			return false;
		}
			
		// ��� ������� �� ��������� ����� ���� �� ����� �������� � ����� �������
		if (!((Long)Person.ID_SYSTEM.getId()).equals(personId)){
			StringBuilder withBlock = new StringBuilder();
			withBlock.append("WITH access_1 as (\n");
			withBlock.append("\tSELECT template_id, status_id \n");
			withBlock.append("\tFROM role_access_rule a_rr \n");
			withBlock.append("\tJOIN access_rule a_r ON a_rr.rule_id=a_r.rule_id \n");
			withBlock.append("\tJOIN access_card_rule a_cr ON a_r.rule_id=a_cr.rule_id and " + sqlPermissionCheck(filter.getCurrentUserPermission(), "a_cr") + "\n");
			withBlock.append("\rJOIN person_role a_pr ON a_rr.role_code=a_pr.role_code AND a_pr.person_id="+personId+"\n");
			withBlock.append("),\n");
			withBlock.append("access_2 as ( \n");
			withBlock.append("\tSELECT template_id, status_id \n");
			withBlock.append("\tFROM role_access_rule a_rr \n");
			withBlock.append("\tJOIN access_rule a_r ON a_rr.rule_id=a_r.rule_id AND a_rr.role_code IS NULL \n");
			withBlock.append("\tJOIN access_card_rule a_cr ON a_r.rule_id=a_cr.rule_id and " + sqlPermissionCheck(filter.getCurrentUserPermission(), "a_cr") + "\n");
			withBlock.append(") -- WITH END\n");
			
			destSqlBuf.insert(0, withBlock.toString());
			
			destSqlBuf.append("AND( \n");
			destSqlBuf.append("\t" + cardAlias + ".is_active = 1 OR \n");
			destSqlBuf.append("\tEXISTS (SELECT role_code FROM person_role WHERE person_id=" + personId + " AND role_code = 'A')) \n"); 
			destSqlBuf.append("AND( \n");
			if (!filter.getTemplatesWithoutPermCheck().isEmpty()) {
				 // skip permissions check for specified templates
				destSqlBuf.append("\t" + cardAlias + ".template_id in (" 
						+ SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(filter.getTemplatesWithoutPermCheck()) +")\n");
				destSqlBuf.append("\tOR \n"); 
			}
			destSqlBuf.append("\tEXISTS (\n");
			destSqlBuf.append("\t\tSELECT 1\n");
			destSqlBuf.append("\t\tFROM access_list a_l\n");
			destSqlBuf.append("\t\tJOIN access_card_rule a_cr1 ON a_l.rule_id=a_cr1.rule_id\n");
			destSqlBuf.append("\t\tWHERE " + sqlPermissionCheck(filter.getCurrentUserPermission(), "a_cr1") + "\n");
			destSqlBuf.append("\t\t\tAND a_l.person_id=" + personId + "\n");
			destSqlBuf.append("\t\t\tAND " + cardAlias + ".card_id=a_l.card_id\n");
		
			destSqlBuf.append("\t) OR EXISTS (\n");
			destSqlBuf.append("\t\tSELECT 1\n");
			destSqlBuf.append("\t\tFROM access_1 ar WHERE \n");
			destSqlBuf.append("\t\t\t    (" + cardAlias + ".template_id=ar.template_id)\n");
			destSqlBuf.append("\t\t\tAND (" + cardAlias + ".status_id  =ar.status_id)\n");
			
			destSqlBuf.append("\t) OR EXISTS (\n");  // all users permissions
			destSqlBuf.append("\t\tSELECT 1\n");
			destSqlBuf.append("\t\tFROM access_2 ar WHERE \n");
			destSqlBuf.append("\t\t\t    (" + cardAlias + ".template_id=ar.template_id)\n");
			destSqlBuf.append("\t\t\tAND (" + cardAlias + ".status_id  =ar.status_id)\n");
			destSqlBuf.append("\t) -- EXISTS\n");
			destSqlBuf.append(") -- AND  \n");
		}
		return true;
	}

    private static String sqlPermissionCheck(Long permission, String tableAlias) {
        if (Search.Filter.CU_READ_PERMISSION.equals(permission))
            return tableAlias + ".operation_code='R'";
        else if (Search.Filter.CU_WRITE_PERMISSION.equals(permission))
            return tableAlias + ".operation_code='W'";
        else if (Search.Filter.CU_RW_PERMISSIONS.equals(permission))
            return "(" + tableAlias + ".operation_code='R' OR " + tableAlias + ".operation_code='W')";
        throw new IllegalArgumentException("Unknown operation filter: " + permission);
    }


	/**
	 * �������� � sql �������� ���� �� �������� �������� �������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param filter ��������� ������, ���� null, �� ������ �� �������� � sql.
	 * @param user ������������, ������������ �������� ���� ��������� �����.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitPermissionCheck( sbuf, search.getFilter(), getUser(), "c");
	 */
	final static boolean emmitPermissionWhere(StringBuffer destSqlBuf, 
			Search.Filter filter, 
			ObjectId personId, 
			String cardAlias)
	{
		return emmitPermissionWhere(destSqlBuf, filter, 
			(personId != null) ? (Long) personId.getId() : new Long(0), 
			cardAlias);
	}

	/*final static boolean emmitPermissionFrom(StringBuffer destSqlBuf, 
			Search.Filter filter, 
			ObjectId personId, 
			String cardAlias)
	{
		return emmitPermissionFrom(destSqlBuf, filter, 
			(personId != null) ? (Long) personId.getId() : new Long(0), 
			cardAlias);
	}*/

	/**
	 * �������� � sql �������� ���� �� �������� �������� �������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param filter ��������� ������, ���� null, �� ������ �� �������� � sql.
	 * @param user ������������, ������������ �������� ���� ��������� �����.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitPermissionCheck( sbuf, search.getFilter(), getUser(), "c");
	 */
	final static boolean emmitPermissionWhere(StringBuffer destSqlBuf, 
			Search.Filter filter, 
			UserData user, 
			String cardAlias)
	{
		return emmitPermissionWhere(destSqlBuf, filter, 
			(user != null && user.getPerson() != null) ? user.getPerson().getId() : null, 
			cardAlias);
	}

	/*final static boolean emmitPermissionFrom(StringBuffer destSqlBuf, 
			Search.Filter filter, 
			UserData user, 
			String cardAlias)
	{
		return emmitPermissionFrom(destSqlBuf, filter, 
			(user != null && user.getPerson() != null) ? user.getPerson().getId() : null, 
			cardAlias);
	}*/

	/**
	 * ��������� sql-�������� ��� �������� ��������� ��������� ���� � ���������
	 * ������ (��������� ������������ �������� 'IN' ��� '=' � ~ �� ���������� 
	 * ��������� � ������ ids. 
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param ids ������ �������� {@link DataObject} ��� �������� id {@link ObjectId}, 
	 * ���� null, �� ������ �� �������� � sql.
	 * @param fldAlias sql-����� ��� ������� � ������������ ����.
	 * @return true, ���� ��������� ���������, false ���� ���.
	 */
	final static private boolean emmitNumericIds( StringBuffer destSqlBuf,
			Collection<?> ids, String fldAlias)
	{
		if (destSqlBuf == null || ids == null || ids.isEmpty())
			return false;
		final String strIds = ObjectIdUtils.numericIdsToCommaDelimitedString(ids);
		final boolean hasSeveralStates = (ids.size() > 1);
		final String oper = (hasSeveralStates) ? "IN" : "=";
		destSqlBuf.append( String.format( "\n\t\t AND "+ fldAlias+ " %s (%s) \n", 
				new Object[] { oper, strIds}));
		return true;
	}

	/**
	 * �������� � sql AND-�������� �������� �������� ������ ��������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param templateIds ������ �������� {@link Template} ��� id �������� 
	 * {@link ObjectId}, ���� null, �� ������ �� �������� � sql.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitTemplateCheck( sbuf, search.getTemplates(), "ccc");
	 */
	final static public boolean emmitTemplateChk(StringBuffer destSqlBuf,
			Collection<?> templateIds, 
			String cardAlias)
	{
		return emmitNumericIds(destSqlBuf, templateIds, cardAlias + ".template_id");
	}

	/**
	 * �������� � sql AND-�������� �������� �������� �������� ������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param stateIds ������ �������� �������� {@link CardState card states} ��� 
	 * id �������� {@link ObjectId}, ���� null, �� ������ �� �������� � sql.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitStateCheck( sbuf, search.getStates(), "ccc");
	 */
	final static public boolean emmitStateChk(StringBuffer destSqlBuf,
			Collection<?> stateIds, 
			String cardAlias)
	{
		return emmitNumericIds(destSqlBuf, stateIds, cardAlias + ".status_id");
	}

	/**
	 * �������� � sql AND-�������� ���������� ������������ �������� �� ��������� ������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param ignoredIds ������ ��������, ������� � �������� ������ ������� �� ������, 
	 * ���� null, �� ������ �� �������� � sql.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitIgnoredChk( sbuf, search.getIgnoredIds(), "ccc");
	 */
	final static public boolean emmitIgnoredChk( StringBuffer destSqlBuf,
			Set<?> ignoredIds, 
			String cardAlias)
	{
		if (ignoredIds==null||ignoredIds.size()==0){
			return false;
		}
		final String ignoredList = ObjectIdUtils.numericIdsToCommaDelimitedString(ignoredIds);
		
		destSqlBuf.append("AND (\n");
		destSqlBuf.append(cardAlias+".card_id not in ("+ignoredList+"))\n");
		return true;
	}

	/**
	 * �������� � sql LIMIT-OFFSET ������� �������� �������.
	 * @param destSqlBuf ������� �����, � ������� ����������� SQL.
	 * @param filter ������ (�.�. null), �� �������� �������� LIMIT/OFFSET.
	 * @param cardAlias sql-����� ��� �������� ������� � ���������� (card).
	 * @return true, ���� ��������� ���������, false ���� ���.
	 * @example: emmitPgLimit( sbuf, search.getFilter(), "ccc");
	 */
	final static public boolean emmitPgLimit(StringBuffer destSqlBuf, Filter filter, Long limit) {
		int pgSz = 0;
		if (filter != null) {
			pgSz = limit == null || limit == 0 ? 
					filter.getPageSize() : 
					filter.getPageSize() < limit ? 
							filter.getPageSize() : 
							limit.intValue();
		}
		if (pgSz > 0) {
			final int pgNum = (filter.getPage() > 0) ? filter.getPage() : 1;
			final String sLimit = "\n\t\t LIMIT " + pgSz + " OFFSET " + ((pgNum - 1) * pgSz) + " \n";
			destSqlBuf.append(sLimit);
			return true;
		}
		return false;
	}
	
	final private String replaceSqlParams(Map<String, Object> paramsAliases, String sqlText) {
		final Set<Map.Entry<String, Object>> args = paramsAliases.entrySet();
		// ���� �� ���� ���������� � �������� �� �� ��������������� ��������� � sql-�������
		for (Map.Entry<String, Object> arg : args) {
			final String value;
			Object obj = arg.getValue();
			if (null != obj) {
				if (obj instanceof Collection) {
					value = ObjectIdUtils.numericIdsToCommaDelimitedString((Collection<?>)obj);
				} else {
					value = obj.toString();
				}
			} else {
				value = "";
			}
			sqlText = sqlText.replaceAll(arg.getKey(), value);
		}
		return sqlText;
	}
	
	final static public boolean emmitPgLimit(StringBuffer destSqlBuf, Filter filter) {
		return emmitPgLimit(destSqlBuf, filter, null);
	}


	final static int MAX_BLOCK_SIZE = 4096;

	private String getResourceMessage(String lang, String key, Object param)
	{
		ResourceBundle messages = ResourceBundle.getBundle(ContextProvider.MESSAGES, new Locale(lang));
		return MessageFormat.format(messages.getString(key), new Object[] { param });
	}

	
	/**
	 * ��������� ������ ��������, ��������������� ���������� ����������.
	 * @param x
	 */
	private Collection<ObjectId> getResultSearchByMaterial(String words) throws DataException {

		final SearchInFiles action = new SearchInFiles();
		action.setWords(words);

		final ActionQueryBase q = getQueryFactory().getActionQuery(action);
		q.setAction(action);
		Collection<ObjectId> result = getDatabase().executeQuery(getUser(), q); 
		return result;
	}

	private void searchByMaterial(ManagerTempTables mgrTmpTables, String words) throws DataException {
		final Collection<ObjectId> result = getResultSearchByMaterial(words); 
		mgrTmpTables.insertCardIds(result);
	}

	// ����� ������ SQL ������ ��� ������ ����� ���������� "JOIN"
	final static public String tagJoinMarkerBegin = "\n -- >>> $SORT ORDER JOIN BEGIN$ \n";
	final static public String tagJoinMarkerEnd   = "\n -- <<< $SORT ORDER JOIN END$ \n";

	final static void insertAfterWithBlock(StringBuffer sqlBuf, String str) {
		String substr = "-- WITH END\n";
		int index = sqlBuf.indexOf(substr);
		index = index == -1 ? 
				index = 0 : 
				index + substr.length();
		sqlBuf.insert(index, str);
	}
	
	/**
	 * ���������� ���� ��������� ���������� SQL.
	 * (!) ��������� �������
	 * @param mgrTmpTables ������������ ��� ������� ���-�� ���������� id 
	 * �������� (�� {@link ManagerTempTables.TMPTABLEID_CARDID}).
	 * @param sqlXmlName
	 * @return ������ cardId ���� �������� �� ����� (��. {@link ManagerTempTables.MIN_EFFECTIVE_COUNT}}), 
	 * � Null ����� (� ���� ������ ������ ���������� ��/����� ��������� ������� 
	 * {@link ManagerTempTables.TMPTABLEID_CARDID}. 
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private List<ObjectId> searchBySQL(ManagerTempTables mgrTempTables, Search search, String sqlXmlName)
		throws DataException
	{
		if (StrUtils.isStringEmpty(sqlXmlName))
			throw new DataException( "com.aplana.dbmi.service.impl.query.SQLQueryEmpty");

		try {
			final Search.Filter filter = search.getFilter();
			final boolean hasOrderedColumn = hasOrderedColumn(filter);

			checkSecurity(sqlXmlName);

			// ���� � ����������� �������
			final String sqlParamsName = search.getSqlParametersName();
			
			final SearchSQL searchSql = new SearchSQL();
			searchSql.setParamsAliases(search.getParamsAliases());
			{
				final InputStream sqlStream
					= Portal.getFactory().getConfigService().loadConfigFile( CONFIG_SQL_FILE_PREFIX + sqlXmlName);
				
				if(sqlParamsName != null && sqlParamsName.length() > 0){
					
					checkSecurity(sqlParamsName);
					
					final InputStream paramsStream
					= Portal.getFactory().getConfigService().loadConfigFile( CONFIG_SQL_FILE_PREFIX + CONFIG_SQL_PARAMS_FILE_PREFIX + sqlParamsName);
					
					Properties props = new Properties();
					props.load(paramsStream);
					
					searchSql.xmlRead(sqlStream, props);
				}
				else
					searchSql.xmlRead(sqlStream, null);
			}
			
			// ��������� � ������� temp_cardid ��� ��� ������ SQL-������ ������������...
			String sqlText = searchSql.getSqlText();
			if (logger.isTraceEnabled()) {
				logger.trace( "searchBySQL() XML SQL is "
						+ SimpleDBUtils.getSqlQueryInfo( sqlText, null, null)
					);
			}

			final StringBuffer sqlBuf = new StringBuffer();
			final List<String> sqlArgList = new ArrayList<String>();
			final List<Integer> sqlArgTypes = new ArrayList<Integer>();
			
			if (hasOrderedColumn) {
				sqlBuf.append("SELECT cc.card_id \n");
			} else {
				sqlBuf.append("SELECT DISTINCT cc.card_id, row_number() over() as rn\n");
			}
			sqlBuf.append( "FROM ( \n");
			sqlBuf.append( sqlText);
			sqlBuf.append( ") AS xx(card_id) \n ");
			sqlBuf.append( "	JOIN card AS cc on cc.card_id = xx.card_id \n ");

			// ������� ����� ���-�� ��� ������� ������ �� ������ ������� ���������� ...
			sqlBuf.append( tagJoinMarkerBegin);
			sqlBuf.append( tagJoinMarkerEnd);

			final String words = search.getWords();
			
			final boolean isEmptyWords = StrUtils.isStringEmpty(words);
			
			// (!)	��� ���� �������� ORACLE ������ "_" �������� ����������� 
			// 		� �������� "���� ����� ������".
			final String nvlWords = StrUtils.nvl(StringEscapeUtils.escapeSql(words), "_");
			
			if(!isEmptyWords && !search.isByCode()) {
				universalSearch(sqlBuf, nvlWords, "cc", search.isStrictWords(), sqlArgList, sqlArgTypes);
			}
			
			/*if (filter != null) {
				emmitPermissionFrom( sqlBuf, filter, getUser(), "cc");
			}*/
			
			final int[] sqlTypes = SimpleDBUtils.makeTypes(sqlArgTypes);
			final Object[] sqlArgs = sqlArgList.toArray();
			
			sqlBuf.append("\n WHERE (1=1) \n");

			boolean useTempTable = false;
			int rowCount = -1; // ������� ������� ...

			// ���� ���� ������ - ����� �������� SQL... 
			if (filter == null) {
				// (!) ���� ��������
				emmitTemplateChk( sqlBuf, search.getTemplates(), "cc");

				// (!) ���� ��������
				emmitStateChk( sqlBuf, search.getStates(), "cc");
				
				final StringBuffer countSql = new StringBuffer(sqlBuf);
				insertAfterWithBlock(countSql, "SELECT distinct card_id FROM (\n");
				countSql.append(") AS zz(card_id)\n");
				if (search.getSearchLimit() > 0){
					countSql.append("LIMIT " + search.getSearchLimit() + "\n");
				}
				insertAfterWithBlock(countSql, "SELECT COUNT(1) FROM (\n");
				countSql.append(") AS yy\n");
				pool = Executors.newFixedThreadPool(1);
				futureCount = pool.submit(new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						return getJdbcTemplate().queryForInt(countSql.toString(), sqlArgs, sqlTypes);
					}
				});
				if (mgrTempTables != null) {
					try {
						rowCount = futureCount.get();
					} catch (Exception e) {
						logger.error(e);
						throw new DataException(e);
					} finally {
						pool.shutdown();
					}
					useTempTable = rowCount > ManagerTempTables.MIN_EFFECTIVE_COUNT;
				}
			} else {
				// ����� + ����

				// �����...
				// (!) �������� ����
				emmitPermissionWhere( sqlBuf, filter, getUser(), "cc");

				// (!) ���� ��������
				emmitTemplateChk( sqlBuf, search.getTemplates(), "cc");

				// (!) ���� ��������
				emmitStateChk( sqlBuf, search.getStates(), "cc");

				// (YNikitin, 2013/05/23) ���������� ��� ��������� �������� �� ������ ��������� ����������, 
				// �.�. ���� ����� �� ������� �� ����� ��������� ������ �� ��, �� ��������� �������� �� ������ �� ��������
				// ����������� ������� �� ��������� �������� ���������� �������� � ���� ����, ��� ����� correctHoleSize
				// �����, ��� ����� ��� ��������� �������� (��� 26592, 28161)
				emmitIgnoredChk( sqlBuf, search.getIgnoredIds(), "cc");

				// ����� ���-�� ������� ...
				pool = Executors.newFixedThreadPool(1);
				final StringBuffer countSql = new StringBuffer(sqlBuf);
				insertAfterWithBlock(countSql, "SELECT distinct card_id FROM (\n");
				countSql.append(") AS zz(card_id)\n");
				if (search.getSearchLimit() > 0){
					countSql.append("LIMIT " + search.getSearchLimit() + "\n");
				}
				insertAfterWithBlock(countSql, "SELECT COUNT(1) FROM (\n");
				countSql.append(") AS yy\n");
				futureCount = pool.submit(new Callable<Integer>() {
					@Override
					public Integer call() throws Exception {
						return getJdbcTemplate().queryForInt(countSql.toString(), sqlArgs, sqlTypes);
					}
				});
				if (mgrTempTables != null && filter.getPageSize() == 0) {
					try {
						rowCount = futureCount.get();
					} catch (Exception e) {
						logger.error(e);
						throw new DataException(e);
					} finally {
						pool.shutdown();
					}
					useTempTable = rowCount > ManagerTempTables.MIN_EFFECTIVE_COUNT;
				}
				
				if (hasOrderedColumn) {
					// add GROUP BY (instead off DISTINCT) to exclude duplicates of card id-s
					sqlBuf.append("GROUP BY cc.card_id");
					// �������� ������������ ������� ����������...
					emmitSortOrder( sqlBuf, search, sqlBuf.indexOf(tagJoinMarkerEnd), 
						"cc", getJdbcTemplate(), logger, /*AggregateFunction.min*/ AggregateFunction.empty);
				} else {
					sqlBuf.append("ORDER BY rn");
				}

				// ������������ ����� ������ getPageSize �������� �� �������������
				emmitPgLimit( sqlBuf, filter, search.getSearchLimit());
			}

			// ������ �������� ������ ���������� �� ������ ����� �������� ...
			//sqlBuf.replace(0, selCount.length() - 2, "SELECT cc.card_id \n");

			if (useTempTable) {
				// ���������� ���� ���� (�� ����� ��������� �� ���� ������) ...
				if (!mgrTempTables.isStarted())
					mgrTempTables.startAll();
				// INSERT INTO
				if (hasOrderedColumn) {
					sqlBuf.insert( 0, "INSERT INTO "+ ManagerTempTables.TMPTABLEID_CARDID +"(card_id) \n");
				} else {
					sqlBuf.insert( 0, "INSERT INTO "+ ManagerTempTables.TMPTABLEID_CARDID +"(card_id, ordnum) \n");
				}
			}

			sqlText = sqlBuf.toString();
			if (logger.isTraceEnabled()) {
				logger.trace( "Final searchBySQL() SQL is \n"
						+ SimpleDBUtils.getSqlQueryInfo( sqlText, null, null)
					);
			}

			int found = -1;
			List<ObjectId> result = null;
			if (!search.isCountOnly()) {
				if (useTempTable) {
					found = this.getJdbcTemplate().update( sqlText, sqlArgs, sqlTypes);
				} else {
					result = getJdbcTemplate().query(
							sqlText,
							sqlArgs,
							sqlTypes,
							new RowMapper() {
								public Object mapRow(ResultSet rs, int rowNum) 
									throws SQLException {
									return  new ObjectId(Card.class, rs.getLong(1));
								}
							});
					if (result != null) 
						found = result.size();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Found " + found + " cards");
				}
			} 
			try {
				filter.setWholeSize(futureCount.get());
			} catch (Exception e) {
				logger.error(e);
				throw new DataException(e);
			} finally {
				if (!pool.isShutdown()) 
					pool.shutdown();
			}

			return result;
		} catch (IOException ex) {
			throw new DataException("com.aplana.dbmi.service.impl.query.XMLLoadError_1",
							new Object[] { sqlXmlName },
							ex);
		}
	}


	private void checkSecurity(String checkString) throws DataException{
		// (!) � ����� ������������ ����� �������� �������� ��
		// ������ ��������� ��������, �������� ��������� �� ���...
		if (checkString.indexOf("..") >= 0)
			throw new DataException("com.aplana.dbmi.service.impl.query.UnsecureFileName_1",
					new Object[] { checkString });
	}
	
	
	/**
	 * Performs additional query to learn the right type of given ObjectId attribute
	 * @param genericTypedAttr
	 * @return ObjectId with right type of an attribute
	 */
	final static ObjectId getOriginalAttribute( JdbcTemplate jdbc,
			ObjectId genericTypedAttr )
	{
		if (genericTypedAttr == null)
			return null;
		if (!genericTypedAttr.getType().equals(Attribute.class))
			return genericTypedAttr;
		String type;
		try {
			type = (String) jdbc.queryForObject("SELECT data_type FROM attribute WHERE attribute_code=?", 
					new Object[]{genericTypedAttr.getId()}, String.class);
		} catch (DataAccessException e) {
			return genericTypedAttr;
		}
		return new ObjectId(AttributeTypes.getAttributeClass(type), genericTypedAttr.getId());
	}


	/**
	 * �������� � sql ������� ���������� � ������� �������� ��� �������� dataAttr 
	 * � ���� ������ "JOIN "+ joinAlias.
	 * @param destSql: ���� ���������� ���������� JOIN.
	 * @param dataAttrs: �������� � ������� ��� �������� �������� (����� :).
	 * @param labelAttrForCardLink: (������������ ������ ���� dataAttr ��� 
	 * CardLink ��� BackLink) ������� �� ��������� �������� ��� ��������� ��������.
	 * @param baseCardTableAlias: ������� ������� ������� � ��������� card_id, 
	 * card_status, template_id, �������� "gtemp".
	 * @param joinAlias:������� ��� �������, � ������� ����� ���������� �����������.
	 * @return sql-�������� ������� ��� "�����������" �������� dataAttr (��� labelAttrForCardLink ��� cardLink'��).
	 * (!) ������������� ����������� ������ (ru/en) ��� ������������ ��������, 
	 * �������� � �������� ���� "ABC_rus"/"ABC_eng".
	 */
	final static String appendJoinDataSqlByAttribute(
				StringBuffer destSqlJoinPart,
				List<ObjectId> dataAttrs,
				ObjectId labelAttrForCardLink,
				List<ObjectId> pathToLabelAttr,
				String baseCardTableAlias,
				String joinAlias,
				int valueOrder,
				org.apache.commons.logging.Log logger,
				AggregateFunction aggregateFunction)
	{
		StringBuffer colAlias = new StringBuffer();
		String resultColAlias = "";

		try {

			final String localeSuffix = ContextProvider.getContext().getLocaleString("rus", "eng");

			final boolean isPseudo = ExecFetchCardsEx.findPseudoAttribute(dataAttrs.get(0)) != -1;
			final Class<?> clazz = (isPseudo) ? ListAttribute.class : dataAttrs.get(0).getType();

			final Object labelAttrName = (labelAttrForCardLink != null) 
						? labelAttrForCardLink.getId() 
						: Attribute.ID_NAME.getId();

			// {0} = joinAlias
			// {1} = dataAttr[attr_code]
			// {2} = labelAttrForCardLink[attr_code]
			// {3} = localeSuffix
			// {4} = baseCardTableAlias
						
			final int arraySize = dataAttrs.size();
			
			String sqlFmt = 
					"\n	LEFT JOIN attribute_value AS {0} \n" +
					"		ON {0}.card_id = {4}.card_id \n";
			if(arraySize == 1)
				sqlFmt +=
					"		AND {0}.attribute_code=''{1}'' \n ";
			
			if (DateAttribute.class.isAssignableFrom(clazz)) {
				colAlias.append("{0}.date_value");
				if(arraySize > 1) {
					sqlFmt += selectFilledValue(dataAttrs, clazz, new String("date_value"), aggregateFunction);
				}
		}
		else if (StringAttribute.class.isAssignableFrom(clazz)) {
				colAlias.append("{0}.string_value");
				if(arraySize > 1) {
					sqlFmt += selectFilledValue(dataAttrs, clazz, new String("string_value"), aggregateFunction);
				}
		}
		else if (TextAttribute.class.isAssignableFrom(clazz)) {
			colAlias.append("{0}.string_value");
			if(arraySize > 1) {
				sqlFmt += selectFilledValue(dataAttrs, clazz, new String("string_value"), aggregateFunction);
			}
		}
		else if (IntegerAttribute.class.isAssignableFrom(clazz)) {
			colAlias.append("{0}.number_value");
			if(arraySize > 1) {
				sqlFmt += selectFilledValue(dataAttrs, clazz, new String("number_value"), aggregateFunction);
			}
		} 
		else if (ReferenceAttribute.class.isAssignableFrom(clazz)){ //includes ListAttribute & TreeAttribute
			if (Card.ATTR_STATE.getId().equals(dataAttrs.get(0).getId())) {
				sqlFmt = "	JOIN card_status AS {0} ON {0}.status_id = {4}.status_id \n";
				colAlias.append("{0}.name_{3}");
			} else if (Card.ATTR_TEMPLATE.getId().equals(dataAttrs.get(0).getId())) {
					sqlFmt = "	JOIN template AS {0} ON {0}.template_id = {4}.template_id \n";
					colAlias.append("{0}.template_name_{3}");
			} else if (Card.ATTR_UNITERM.getId().equals(dataAttrs.get(0).getId())){
					sqlFmt = "\n	LEFT JOIN attribute_value AS {0} \n"
					+ "		ON {0}.card_id = {4}.card_id \n"
					+ "		AND {0}.attribute_code in "
					+	"("
					+		"''JBR_VISA_TODATE'', "
					+		"''ADMIN_893447'', "
					+		"''JBR_INFORM_DATA'', "
					+		"''JBR_INF_TERM'', "
					+		"''ADMIN_726875'', "
					+		"''ADMIN_5976960'', "
					+		"''JBR_SIGN_TODATE'', "
					+		"''JBR_RASSM_TODATE'', "
					+		"''JBR_TCON_TERM'', "
					+		"''JBR_IMPL_DEADLINE'', "
					+		"''JBR_PCON_DATE''"
					+	") \n"
					+ "AND {0}.date_value is not null";
					colAlias.append("{0}.date_value");
			//���� ��������� �� ��������� ���������, �� ����� � value_id
			} else{
				switch(valueOrder){
					case 0: {
						sqlFmt = 
							"	LEFT JOIN values_list AS {0} \n" +
							"		ON {0}.value_id = ( \n" +
							"			SELECT MIN(avXX.value_id) \n" +
							"			FROM attribute_value avXX \n" +
							"			WHERE avXX.attribute_code = ''{1}'' \n" +
							"					AND avXX.card_id = {4}.card_id \n" +
							"		)";
						colAlias.append("{0}.value_{3}"); // "value_rus"/"value_eng"
					} break;
					case 1: {
						sqlFmt = 
							"	LEFT JOIN values_list AS {0} \n" +
							"		ON {0}.value_id = ( \n" +
							"			SELECT MIN(avXX.value_id) \n" +
							"			FROM attribute_value avXX \n" +
							"			WHERE avXX.attribute_code = ''{1}'' \n" +
							"					AND avXX.card_id = {4}.card_id \n" +
							"		)";
						colAlias.append("{0}.order_in_level");
					} break;
				}
			}
		}
		// DONE PersonAttribute � CardLinkAttribute ����� ���� multi valued � ��������������� ���������� ����� ������ ����� 
		//			��� ���������� ��� � ����� ������� �� ���������. 
		//	����� ���������� ��������� �� person_id � card_id ��� PersonAttribute � CardLinkAttribute ��������������.
		else if (PersonAttribute.class.isAssignableFrom(clazz)){
			sqlFmt = 
					"	LEFT JOIN person AS {0} \n" +
					"		ON {0}.person_id = ( \n" +
					"			SELECT avXX.number_value \n" +
					"			FROM attribute_value avXX \n" +
					"			WHERE avXX.attribute_code in (" + SimpleDBUtils.stringIdentifiersToCommaSeparatedDoubleQuotedSqlString(dataAttrs) + ") \n" +
					"					AND avXX.card_id = {4}.card_id \n" +
					"			order by avXX.attr_value_id" +
					"			limit 1" +
					"		)";
			colAlias.append("{0}.full_name");
		} /*else if (CardLinkAttribute.class.isAssignableFrom(clazz)) {
			// (!) includes TypedCardLinkAttribute.class
			sqlFmt =
				"	LEFT JOIN attribute_value AS {0} \n" +
				"		ON {0}.card_id = ( \n" +
				"			SELECT MIN(avXX.number_value) \n" +
				"			FROM attribute_value avXX \n" +
				"			WHERE avXX.attribute_code = ''{1}'' \n" +
				"					AND avXX.card_id = {4}.card_id \n" +
				"		) \n" +
				"		AND {0}.attribute_code = ''{2}'' \n";  // (!) labelAttrForCardLink
			// (!) TODO: ��������� ������� ���� ���������������� � ����� ��� �������� ���������� ��������� (� �� ������ string_value)
			resultColAlias = "{0}.string_value";
		}*/
		else if (CardLinkAttribute.class.isAssignableFrom(clazz) || BackLinkAttribute.class.isAssignableFrom(clazz)) {
			// (!) includes TypedCardLinkAttribute.class
			sqlFmt =
				"	LEFT JOIN attribute_value AS {0} \n" +
				"		ON {0}.card_id = ( \n"+
				"						   SELECT orderCard.vvalue FROM (\n";
				
			for(Iterator<ObjectId> i = dataAttrs.iterator(); i.hasNext();) {
				final ObjectId dataAttr = (ObjectId)i.next();
				final Class<?> attrClazz = dataAttr.getType();
				if(CardLinkAttribute.class.isAssignableFrom(attrClazz)){
					sqlFmt += 
				"						SELECT MIN(avXX.number_value) as vvalue \n" +
				"							FROM attribute_value avXX \n" +
				"							WHERE avXX.attribute_code = ''" + dataAttr.getId().toString() + "'' \n" +
				"								AND avXX.card_id = {4}.card_id \n";
				} else if(BackLinkAttribute.class.isAssignableFrom(attrClazz)){
					sqlFmt += 
				"						select functionbacklink(ccc.card_id, o1.option_value, o2.option_value) as vvalue from card ccc \n" +
				"							INNER JOIN template_block tb on tb.template_id = ccc.template_id \n" +
				"							INNER JOIN attribute a on a.block_code=tb.block_code and a.data_type=''B'' \n" +
				"								and a.attribute_code = ''" + dataAttr.getId().toString() + "'' \n" +
				"							LEFT JOIN attribute_option o1 on a.attribute_code = o1.attribute_code \n" +
				"								and o1.option_code = ''" + AttributeOptions.UPLINK + "'' \n" +
				"							LEFT JOIN attribute_option o2 on a.attribute_code = o2.attribute_code \n" +
				"								and o2.option_code = ''" + AttributeOptions.LINK +"'' \n" +
				"							WHERE ccc.card_id = {4}.card_id	\n";
				} else {
					if(!dataAttr.getId().equals(labelAttrForCardLink.getId()))
						throw new DataException( "store.cardaccess.wrong.class", new Object[] { labelAttrForCardLink.getType(), dataAttr.getType() } );
					// store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}''
					sqlFmt += 
				" 						select {4}.card_id \n ";
				}
				
				if(i.hasNext())
					sqlFmt += 
				" 						UNION \n ";
			}
				
			sqlFmt +=
				
			    "						) as orderCard join attribute_value av_o on orderCard.vvalue=av_o.card_id and av_o.attribute_code = ''{2}'' \n "+
			    "						ORDER BY vvalue \n" +
				"						LIMIT 1 \n" +
				"						) \n";
			
			// ���� pathToLabelAttr �� null � �� �����, �� ����� �������� �� CardLink/BackLink ����� ��� ������� �� CardLink, �������� � ��������� ��������.
			sqlFmt += moveViaPathToLabelAttr(pathToLabelAttr, colAlias, makeAttributeValueColumn(labelAttrForCardLink));
			
			// (!) TODO: ��������� ������� ���� ���������������� � ����� ��� �������� ���������� ��������� (� �� ������ string_value)
			//resultColAlias = "{0}.string_value";
		}
			else {
				throw new DataException( "store.cardaccess.wrong.class", new Object[] { "C,D,E,H,I,L,S,T,U", clazz } );
				// store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}''
			}
			resultColAlias = MessageFormat.format( colAlias.toString(), new Object[]{ 
				joinAlias , dataAttrs.get(0).getId(), labelAttrName, localeSuffix,
				baseCardTableAlias });
		destSqlJoinPart.append( MessageFormat.format( sqlFmt, new Object[]{ 
				joinAlias , dataAttrs.get(0).getId(), labelAttrName, localeSuffix,
				baseCardTableAlias }));
		}
		catch (Exception e) {
			logger.error( MessageFormat.format("Error in getColumSqlByAttribute( ''{0}'', ''{1}'')", 
					new Object[]{ dataAttrs.get(0), labelAttrForCardLink}), e);
			resultColAlias = "";
		} 
		return resultColAlias;
	}
	
	
	/**
	 * ����� ������������ �������� � ����������� ���������,
	 * ������� ���� ����� :
	 * @param dataAttrs ������ ��������� ����� :
	 * @param clazz ����� ������� (��������) �������� � ������
	 * @param avColumn �������� ���� �� attribute_value ������ ����� ����� �������� ��������
	 * @throws DataException 
	 */
	final static String selectFilledValue(List<ObjectId> dataAttrs, Class<?> clazz, String avColumn, AggregateFunction aggregateFunction) throws DataException {
		String sql = "		AND {0}.attribute_code = ( \n" +
		"									select attribute_code from ( \n" +
		"										with \n" +
		"										sortCodeVal as ( \n";
		for(Iterator<ObjectId> i = dataAttrs.iterator(); i.hasNext();) {
			final ObjectId dataAttr = (ObjectId)i.next();
			final Class<?> attrClazz = dataAttr.getType();
			if(!clazz.equals(attrClazz))
				throw new DataException( "store.cardaccess.wrong.class", new Object[] { clazz, attrClazz } );
				// store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}''
			sql +=
				"											select attribute_code from attribute_value \n " +
				"											where card_id = {4}.card_id \n " +
				"											and attribute_code = ''" + dataAttr.getId().toString() + "'' \n " +
				"											and ''" + avColumn + "'' is not null \n ";
			if(i.hasNext())
				sql +=
				"											UNION \n ";
		}
		sql +=
				"										) \n" +
				"										select attribute_code, date_value from sortCodeVal \n" +
				"										order by date_value"; 
		switch(aggregateFunction) {
			case min: sql += " asc \n";
				break;
			case max: sql += " desc \n";
			break;
		}
		sql +=
				"									) as sortCode \n" +
				"									LIMIT 1 \n" +
				"								) \n";
	
		return sql;
	}
	
	
	/**
	 * ���� ����� ��������� �������� CardLink/Backlink (id)
	 * ���������� ������� ��� �� ������� �� CardLink (labelAttrId)
	 * @param sql ����������� SQL-�����
	 * @param pathToLabelAttr ������ CardLink-���������, �� ������� ���������� �������
	 * @param resultColAlias ����, �� �������� ������ ���� �������� �������� ��������
	 * @throws DataException 
	 */
	final static String moveViaPathToLabelAttr(List<ObjectId> pathToLabelAttr, StringBuffer resultColAlias, String avColumn) throws DataException {
		String sql = "";
		if(pathToLabelAttr != null && !pathToLabelAttr.isEmpty()) {
			for(int i = 0; i < pathToLabelAttr.size(); i++) {
				ObjectId attr = pathToLabelAttr.get(i);
				if(CardLinkAttribute.class.equals(attr.getType())) {
					if(i == 0) {
						sql +=
								"		AND {0}.attribute_code = ''"+attr.getId().toString()+"'' \n" +
								"	LEFT JOIN attribute_value AS {0}_"+new Integer(i+1).toString()+" \n" +
								"		ON {0}_"+new Integer(i+1).toString()+".card_id = {0}.number_value \n";
					}
					if(i > 0 && i < pathToLabelAttr.size()-1){
						sql +=
								"		AND {0}_"+new Integer(i).toString()+".attribute_code = ''"+attr.getId().toString()+"'' \n" +
								"	LEFT JOIN attribute_value AS {0}_"+new Integer(i+1).toString()+" \n" +
								"		ON {0}_"+new Integer(i+1).toString()+".card_id = {0}_"+new Integer(i).toString()+".number_value \n";
					}
					if(i > 0 && i == pathToLabelAttr.size()-1) {
						sql +=
								"		AND {0}_"+new Integer(i).toString()+".attribute_code = ''"+attr.getId().toString()+"'' \n" +
								"	LEFT JOIN attribute_value AS {0}_"+new Integer(i+1).toString()+" \n" +
								"		ON {0}_"+new Integer(i+1).toString()+".card_id = {0}_"+new Integer(i).toString()+".number_value \n" +
								"		AND {0}_"+new Integer(i+1).toString()+".attribute_code = ''{2}'' \n";
						resultColAlias.append("{0}_"+new Integer(i+1).toString()+"." + avColumn);
					}
					if(i == 0 && i == pathToLabelAttr.size()-1) {
						sql += "		AND {0}_"+new Integer(i+1).toString()+".attribute_code = ''{2}'' \n";
						resultColAlias.append("{0}_"+new Integer(i+1).toString()+"." + avColumn);
					}
				} else {
					throw new DataException( "store.cardaccess.wrong.class", new Object[] { "CardLinkAttribute or BackLinkAttribute", attr.getType() } );
					// store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}''
				}
			}				
		} else {				
			sql += "		AND {0}.attribute_code = ''{2}'' \n";  // (!) labelAttr
			resultColAlias.append("{0}." + avColumn);
		}
		
		return sql;
	}
	
	
	/**
	 * ���������� �������� ���� � ������� attribute_value 
	 * � ������������ � ����� �������� inkAttr �� xml 
	 * @param labelAttrForCardLink �������� linkAttr
	 * @return s �������� ���� � ������� attribute_value
	 * @throws DataException 
	 */
	final static String makeAttributeValueColumn(ObjectId labelAttrForCardLink) throws DataException{
		String s = "string_value";
		if(labelAttrForCardLink == null)
			return s;
		final Class<?> clazz = labelAttrForCardLink.getType();
		if(StringAttribute.class.isAssignableFrom(clazz) || TextAttribute.class.isAssignableFrom(clazz))
			return s;
		else if(DateAttribute.class.isAssignableFrom(clazz))
				s = "date_value";
		else if(IntegerAttribute.class.isAssignableFrom(clazz))
				s = "number_value";
		//else
			//throw new DataException( "store.cardaccess.wrong.class", new Object[] { "S,T,D or I", labelAttrForCardLink.getType() } );
		// store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}''
		return s;
	}
	
	/**
	 * �������� � "where" ����� ����������.
	 * @param destSqlBuf ����������� SQL-�����
	 * @param search ���������
	 * @param offsetJoin �������� � ������ destSqlBuf, � �������� ����� ��������� 
	 * ������ "JOIN"
	 * @param baseCardTableAlias: ������� ������� ������� � ��������� card_id, 
	 * card_status, template_id, �������� "gtemp".
	 * @return true, ���� ���������� ���� ��������� � false �����.
	 * @example emmitSortOrder( sqlBuf, search, sqlBuf.indexOf(tagJoinMarkerEnd) );
	 */
	final static boolean emmitSortOrder( StringBuffer destSqlBuf,
			Search search,
			Search.Filter filter, 
			int offsetJoin,
			String baseCardTableAlias,
			JdbcTemplate jdbc,
			org.apache.commons.logging.Log logger
			) {
		return emmitSortOrder(destSqlBuf, search, offsetJoin, baseCardTableAlias, jdbc, logger, AggregateFunction.empty);
	}

	/**
	 * @param filter ��������� �� search
	 * @return true if Search contains sort attribute in columns or false otherwise
	 */
	final static boolean hasOrderedColumn(Search.Filter filter) {

		if (filter == null || filter.getOrderedColumns() == null || filter.getOrderedColumns().isEmpty() ) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @param destSqlBuf ����������� SQL-�����
	 * @param search ���������
	 * @param offsetJoin �������� � ������ destSqlBuf, � �������� ����� ��������� 
	 * ������ "JOIN"
	 * @param baseCardTableAlias: ������� ������� ������� � ��������� card_id, 
	 * card_status, template_id, �������� "gtemp".
	 * @param aggregateFunction: needed to use with GROUP BY
	 * 
	 * @return true, ���� ���������� ���� ��������� � false �����.
	 * @example emmitSortOrder( sqlBuf, search, sqlBuf.indexOf(tagJoinMarkerEnd) );
	 */
	final static boolean emmitSortOrder( StringBuffer destSqlBuf,
			Search search,
			int offsetJoin,
			String baseCardTableAlias,
			JdbcTemplate jdbc,
			org.apache.commons.logging.Log logger,
			AggregateFunction aggregateFunction
			)
	{
		
		Search.Filter filter = search.getFilter();
		if (!hasOrderedColumn(filter)) return false;
		boolean useAggregatetSort = false;
		final StringBuffer bufJoinList = new StringBuffer();	// ��� ������ JOIN
		final StringBuffer bufOrderByColList = new StringBuffer( "\n\t ORDER BY " );
		// ���� ���������� AggregateFunction.empty - �.�. ��� ���� �������� ���������� �����,
		// �� ������� ���������� �� ������� ���������� Search.
		// ���� � Search ���������� �� ������ - ���������� �� �����.
		if (aggregateFunction.equals(AggregateFunction.empty)) {
				useAggregatetSort = true;
		}
		// TODO: �� ����� "MERGE FILTER CHECK"
		int iTable = 0;
		for ( Iterator<?> iterator = filter.getOrderedColumns().iterator(); iterator.hasNext();) 
		{
			final Search.OrderedColumn orderedCol = (Search.OrderedColumn) iterator.next();
			if (orderedCol.getColumn() == null) continue;
			int sortAggregate = orderedCol.getColumn().getSorting();
			if (useAggregatetSort) 
				switch(sortAggregate) {
					case Column.SORT_ASCENDING: {
						aggregateFunction = AggregateFunction.min;
						break;
					}
					case Column.SORT_DESCENGING: {
						aggregateFunction = AggregateFunction.max;
						break;
					}
				}
			if(orderedCol.getColumn().getSortAttrPaths()!=null && orderedCol.getColumn().getSortAttrPaths().size() > 0){
				iTable = generateOrderStatmentsBySortPaths(search,
						baseCardTableAlias, jdbc, logger, aggregateFunction,
						bufJoinList, bufOrderByColList, iTable, orderedCol);
			} else {
				iTable = generateOrderStatmentsByDefault(search,
						baseCardTableAlias, jdbc, logger, aggregateFunction,
						bufJoinList, bufOrderByColList, iTable, orderedCol);
			}
		} // for
		if (iTable == 0) // ������ �����������...
			return false;

		bufJoinList.append("\n");
		destSqlBuf.insert( offsetJoin, bufJoinList);
		destSqlBuf.append(bufOrderByColList);
		destSqlBuf.append("\n");
		return true;
	}
	
	private static int generateOrderStatmentsBySortPaths(Search search,
			String baseCardTableAlias, JdbcTemplate jdbc,
			org.apache.commons.logging.Log logger, AggregateFunction aggregateFunction,
			final StringBuffer bufJoinList,
			final StringBuffer bufOrderByColList, int iTable,
			final Search.OrderedColumn orderedCol) {
		String sortJoinAliasName = "sort_av_column_"+iTable;
		String sortOrderCol = getBufOrderByColList("", orderedCol.getColumn().getSortAttrPaths());
		bufJoinList.append("left join attribute_value " + sortJoinAliasName + " on " + sortJoinAliasName + ".attr_value_id = \n");
		bufJoinList.append("(select sortAttr.attr_value_id from (\n");
		Iterator<List<ObjectId>> i =  orderedCol.getColumn().getSortAttrPaths().iterator();
		String alias = "";
		while (i.hasNext()){
			List<ObjectId> sortAttrPath = i.next();
			if(sortAttrPath.size()>0){
				String[] statmentByPath = generateJoinStatmentByPath(sortAttrPath, baseCardTableAlias, iTable, sortOrderCol);
				bufJoinList.append(statmentByPath[0]);
				alias = statmentByPath[1];
			}
			if(i.hasNext()){
				bufJoinList.append("UNION \n");
			}
		}		
		bufJoinList.append(") as sortAttr \n");
		String order = "order by sortAttr" + sortOrderCol + "";
		bufJoinList.append(order);
		switch(aggregateFunction) {
		case min: bufJoinList.append(" asc \n");
			break;
		case max: bufJoinList.append(" desc \n");
			break;
		}
		bufJoinList.append("limit 1\n");
		bufJoinList.append(")\n");
		String orderAlias = getBufOrderByColList(sortJoinAliasName, orderedCol.getColumn().getSortAttrPaths());
		if(!AggregateFunction.empty.equals(aggregateFunction)){
			bufOrderByColList.insert(0, ", " + orderAlias);
		}
		bufOrderByColList.append(orderAlias);
		if (orderedCol.getColumn().getSorting() == SearchResult.Column.SORT_DESCENGING) {
			bufOrderByColList.append(" DESC");
		}
		return ++iTable;
	}
	
	static String[] generateJoinStatmentByPath(List<ObjectId> sortAttrPath, String baseCardTableAlias, int iTable, String selectCol){
		StringBuilder singlePathSql = new StringBuilder();
		String inNumberAlias = "sort_join_head_c_" + iTable + ".card_id";
		int join_index = 0;
		for(ObjectId attr: sortAttrPath){
			if(inNumberAlias != null){
				inNumberAlias = generateJoinStatmentByNode(singlePathSql, inNumberAlias, attr, join_index);
				join_index++;
			}
		}
		singlePathSql.append("where sort_join_head_c_" + iTable + ".card_id = "+baseCardTableAlias+".card_id \n");
		return new String[] {"select " + inNumberAlias + ".attr_value_id, " + inNumberAlias + selectCol + "\n from card sort_join_head_c_" + iTable + " \n" + singlePathSql.toString(), inNumberAlias};
	}
	
	static String generateJoinStatmentByNode(StringBuilder buff, String inNumberAlias, ObjectId attrId, int join_index){
		StringBuilder singleNodeSql = new StringBuilder();
		String numberAlias;
		String alias;
		String outNumberAlias;
		
		if(BackLinkAttribute.class.equals(attrId.getType())){
			alias = getCardAlias(join_index);
			numberAlias = alias + ".card_id"; //{2}
			singleNodeSql.append("JOIN card {2} on {3} = (\n");
			singleNodeSql.append("\t select functionbacklink(ccc.card_id, o1.option_value, o2.option_value) as vvalue from card ccc\n");
			singleNodeSql.append("\t INNER JOIN template_block tb on tb.template_id = ccc.template_id  \n");
			singleNodeSql.append("\t INNER JOIN attribute a on a.block_code=tb.block_code and a.data_type=''B'' and a.attribute_code = ''{1}'' \n");
			singleNodeSql.append("\t LEFT JOIN attribute_option o1 on a.attribute_code = o1.attribute_code and o1.option_code = ''UPLINK''\n");
			singleNodeSql.append("\t LEFT JOIN attribute_option o2 on a.attribute_code = o2.attribute_code and o2.option_code = ''LINK'' \n");
			singleNodeSql.append("\t WHERE ccc.card_id = {0})\n");
			outNumberAlias = numberAlias;
		} else if (CardLinkAttribute.class.equals(attrId.getType())
				|| TypedCardLinkAttribute.class.equals(attrId.getType())
				|| DatedTypedCardLinkAttribute.class.equals(attrId.getType())){
			alias = getAttributeValueAlias(join_index);
			numberAlias = alias + ".card_id";
			singleNodeSql.append("JOIN attribute_value {2} on {0} = {3} and {2}.attribute_code = ''{1}''\n");
			outNumberAlias = alias + ".number_value";
		} else if (PersonAttribute.class.equals(attrId.getType())){
			alias = getPersonAlias(join_index);
			numberAlias = alias + ".card_id";
			String avAlias = getAttributeValueAlias(join_index);
			singleNodeSql.append("JOIN attribute_value "+avAlias+" on {0} = " + avAlias +".card_id and " + avAlias + ".attribute_code = ''{1}''\n");
			singleNodeSql.append("JOIN person {2} on "+avAlias+".number_value = {2}.person_id \n");
			outNumberAlias = alias + ".card_id";
		} else {
			alias = getAttributeValueAlias(join_index);
			numberAlias = alias;
			singleNodeSql.append("JOIN attribute_value {2} on {0} = {2}.card_id and {2}.attribute_code = ''{1}''\n");
			outNumberAlias = alias;
		}
		buff.append(MessageFormat.format(singleNodeSql.toString(), new Object[]{inNumberAlias, attrId.getId(), alias, numberAlias}));
		return outNumberAlias;
	}
	
	static private String getCardAlias(int i){
		return "sort_c_"+ i;
	}
	
	static private String getAttributeValueAlias(int i){
		return "sort_av_"+ i;
	}
	
	static private String getPersonAlias(int i){
		return "sort_p_"+ i;
	}
	
	static private String getBufOrderByColList(String columnAlias, List<List<ObjectId>> sortAttrPaths){
		Iterator<List<ObjectId>> i = sortAttrPaths.iterator();
		while(i.hasNext()){
			List<ObjectId> ids = i.next();
			if(ids.size() > 0){
				ObjectId firstSortAttrId = ids.get(ids.size()-1);
				if(DateAttribute.class.equals(firstSortAttrId.getType())){
					return columnAlias + ".date_value";
				} else if(StringAttribute.class.equals(firstSortAttrId.getType()) || 
						TextAttribute.class.equals(firstSortAttrId.getType())){
					return columnAlias + ".string_value";
				} else {
					return columnAlias + ".number_value";
				}
			}
		}
		return "";
	}


	private static int generateOrderStatmentsByDefault(Search search,
			String baseCardTableAlias, JdbcTemplate jdbc,
			org.apache.commons.logging.Log logger, AggregateFunction aggregateFunction,
			final StringBuffer bufJoinList,
			final StringBuffer bufOrderByColList, int iTable,
			final Search.OrderedColumn orderedCol) {
		if (orderedCol.getColumn().getSorting() != SearchResult.Column.SORT_NONE
				&& orderedCol.getColumn().getAttributeId() != null
		) {
				final int group = orderedCol.getColumn().getGroupId();
			
				List<ObjectId> columnIds = getColumnIds(search, orderedCol, group, jdbc);
				
				if(columnIds == null || columnIds.isEmpty())
					return iTable;

				final int valueOrder = orderedCol.getColumn().getValueOrder();
				iTable++;

				final String orderAlias = 
						appendJoinDataSqlByAttribute( bufJoinList,
								columnIds,
								getOriginalAttribute( jdbc, orderedCol.getColumn().getLabelAttrId()),
								orderedCol.getColumn().getPathToLabelAttr(),
								baseCardTableAlias,
								"avSort_" + iTable,
								valueOrder,
								logger,
								aggregateFunction
						);
				if (orderAlias != null && orderAlias.length() > 0) {
					if (iTable > 1) {
						bufOrderByColList.append(", ");
					}
					if (!AggregateFunction.empty.equals(aggregateFunction)) {
						bufOrderByColList.insert(0, ", " + orderAlias);
					}
					bufOrderByColList.append(orderAlias);
					if (orderedCol.getColumn().getSorting() == SearchResult.Column.SORT_DESCENGING) {
						bufOrderByColList.append(" DESC");
					}
				}
		}
		return iTable;
	}
	
		//�������� ������� ��� ����������
		final static ObjectId getSortAttrId(SearchResult.Column column) {
			return (column.getAttributeId());
		}		
		
		//�������� ��� �������� ������ ������� ������ ����� :
		final static List<ObjectId> getColumnIds(Search search, Search.OrderedColumn orderedCol, int group, JdbcTemplate jdbc) {
			List<ObjectId> result = new ArrayList<ObjectId>();
			
			if(group == 0) {
				final ObjectId sortAttrId = getSortAttrId(orderedCol.getColumn());

				if (sortAttrId == null)
					return null;
				result.add(getOriginalAttribute(jdbc, sortAttrId));
				return result;
			}
			
			for(Iterator<SearchResult.Column> iterator = search.getColumns().iterator(); iterator.hasNext();) {
				
				final SearchResult.Column column = iterator.next();
				if(group > 0 && column.getGroupId() == group) {
					final ObjectId attrId = getSortAttrId(column);
					
					if(attrId != null)
						result.add(getOriginalAttribute(jdbc, attrId));
				}
			}
			return result;
		}


	/**
	 * 
	 * @param operListIds: ���� null �� ������������ � �������� ���������� �� 
	 * ��������� �������, ����� � ���� ������.
	 * @param ignored: ����� ������������ ObjectIds.
	 */
	private void removeIgnoredIds(List<ObjectId> operListIds, Set<?> ignored) {
		if ((ignored == null )||(ignored.isEmpty()))
			return;
		if (operListIds != null)
		{	// �������� ���� �������� ������ operListIds
			for ( Iterator<ObjectId> i = operListIds.iterator(); i.hasNext(); ) 
			{
				final Object obj = i.next();
				if (obj == null) continue;
				final ObjectId id = ObjectIdUtils.getIdFrom(obj);
				if (ignored.contains(id))
					i.remove();
			}
			return;
		}
		final String iList = ObjectIdUtils.numericIdsToCommaDelimitedString(ignored);
		int affected = getJdbcTemplate().update("DELETE FROM gtemp_cardid WHERE card_id IN(" + iList + ")");
		if (logger.isDebugEnabled()) {
			logger.debug("Deleted from gtemp_cardid: " + affected + " rows");
		}
	}

	/**
	 * ����� ��� �������� ������������ �������� �������.
	 * ���� ��� ������ ����� � ���������.
	 * @author RAbdullin
	 *
	 */
	class SearchSQL
	{
		/**
		 * ��� ����������� ��������� ID �������� ������������
		 */
		private static final String SUBST_CURRENTUSER = "%CURRENTUSER%";

		/**
		 * ��� ����������� �������� ID �������� ������������ � ���� ���, ���
		 * ����������� ���-���� �������� ������������ � ������ ������.
		 */
		private static final String SUBST_CURRENTUSERS = "%CURRENTUSERS%";

		/**
		 * XML tags
		 */
		private static final String xmlTAG_ROOT = "searchQuery";
		private static final String xmlTAG_SQL = "query/sql";

        /**
         * ��� ����������� ������� ��������
         */
        private static final String SUBST_CURRENTTIMEZONE = "%CURRENTTIMEZONE%";

        /**
         * ��� ����������� ��������, ������� �������������� ��������� ����,
         * �������� �������� ��� �������� �� 02.01.2014: %DATETIMEZONE=2014-01-02%
         */
        private static final String DATETIMEZONE_PATTERN = "%DATETIMEZONE=2\\d\\d\\d-\\d\\d-\\d\\d%";

        /**
		 * Internals
		 */
		private String sqlText;
		
		private Map<String, Object> paramsAliases = new HashMap<String, Object>();

		/**
		 * @param inputStream
		 */
		public void xmlRead(InputStream xml, Properties props)
			throws DataException
		{
			try {
				final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);

				final Element rootElem = doc.getDocumentElement();
				if (!xmlTAG_ROOT.equals(rootElem.getNodeName()))
					throw new Exception(xmlTAG_ROOT + " element expected");

				// ������ ������ ������ SQL...
				final Element sqlNode
				   = (Element) SearchXmlHelper.getTagNode(rootElem, xmlTAG_SQL, null, null);
				if (sqlNode == null)
					throw new DataException( "com.aplana.dbmi.service.impl.query.SQLQueryEmpty");

				// ��������� sql-������� � ������������ �������������
				this.setSqlText(SearchXmlHelper.getTagContent(sqlNode));
				
				if(props != null && !props.isEmpty()){
					final Set<String> parameters = parseParameters(this.getSqlText());
					
					for(String param : parameters){
						String realValue = getValueToReplace(param, props);
						if(realValue != null)
							replaceParameter(param, realValue);
					}
				}
				
			} catch (Exception e) {
				throw new DataException("action.search.init", e);
			}
		}
		
		public Set<String> parseParameters(String sql){
			
			Set<String> result = new HashSet<String>();
			if (!StrUtils.isStringEmpty(sql)){
				Pattern pattern = Pattern.compile("%.*?%", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
				Matcher matcher = pattern.matcher(sql);
				while(matcher.find()){
					String param = matcher.group();
					result.add(param);
				}
			}
			return result;
		}
		
		// ����������� �������� �������� �� ������ 
		public String getValueToReplace(String param, Properties props) throws ParseException{
			
			String clearParam = param.replaceAll("%", "");
			
			ObjectId attrId = IdUtils.tryFindPredefinedObjectId(clearParam, null, false);
			
			if(attrId != null){
				if(Template.class.isAssignableFrom(attrId.getType())
						|| CardState.class.isAssignableFrom(attrId.getType())){
					return ((Long)attrId.getId()).toString();
				}
				else
					return "'" + attrId.getId().toString() + "'";
			}
			else {
				if (props.containsKey(clearParam)){
					return "'" + props.getProperty(clearParam) + "'";
				}
			}
			
			return null;
		}

		// ������ ��������� �� �������� ��������
		public void replaceParameter(String name, String value)
		{
			if (!StrUtils.isStringEmpty(this.sqlText))
			{
				this.sqlText = this.sqlText.replaceAll(name, value);
			}
		}
		
		public void setParamsAliases(final Map<String, Object> paramsAliases) {
			this.paramsAliases = paramsAliases;
		}
		
		/**
		 * @param sqlText the sqlText to set
		 * @throws DataException
		 */
		public void setSqlText(final String sqlText) throws DataException {

			this.sqlText = sqlText;

			// ����������������...
			if (!StrUtils.isStringEmpty(this.sqlText))
			{
				final ObjectId usrId
						= (getUser() != null && getUser().getPerson() != null)
								? getUser().getPerson().getId()
								: null;

				// ����������� "�������� ������������" ...
				this.sqlText = this.sqlText.replaceAll(SUBST_CURRENTUSER,
						 ((usrId != null) ? usrId.getId().toString() : "-1") 
						);

				// ������������ �������� � ���, ��� ��� ����������� ���-��� ...
				if (this.sqlText.indexOf(SUBST_CURRENTUSERS) >= 0) {
					/* TODO: (NATIVE_DELEGATE) �������� ��������� ������������� - �� ������ ������ (��-�� ��������) ������� ����
					>>>
					String ids = "-1";
					if (usrId != null && usrId.getId() != null) {
						final DelegateManagerImpl mgr = new DelegateManagerImpl( getBeanFactory());
						final ArrayList listAll = new ArrayList(10);
						listAll.add(usrId);
						final Set setBosses = mgr.getPersonsCanDoAs(usrId, null);
						if (setBosses != null)
							listAll.addAll(setBosses);
						ids = SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(listAll);
					}
					<<<
					  */
					String ids = ((usrId != null) ? usrId.getId().toString() : "-1");// TODO: �� ���� �� �������������
					this.sqlText = this.sqlText.replaceAll(SUBST_CURRENTUSERS, ids);
				}
				if (!paramsAliases.isEmpty())
					this.sqlText = replaceSqlParams(paramsAliases, this.sqlText);

                //����������� �������
                Pattern pattern = Pattern.compile(DATETIMEZONE_PATTERN);
                Matcher matcher = pattern.matcher(this.sqlText);
                while (matcher.find()) {
                    String[] match = matcher.group().replaceAll("%", "").split("=");
                    DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                    try {
                        Date date = df.parse(match[1]);
                        this.sqlText = this.sqlText.replaceFirst(matcher.group(), String.valueOf(date.getTimezoneOffset()));
                    } catch (ParseException e) {
                        logger.error("Can't parse date " + match[1] + "from string " + matcher.group(), e);
                        throw new DataException(e);
                    }
                }
                this.sqlText = this.sqlText.replaceAll(SUBST_CURRENTTIMEZONE, String.valueOf(new Date().getTimezoneOffset()));

			}
		}

		/**
		 * @return the sqlText
		 */
		public String getSqlText() {
			return sqlText;
		}
	}
	
	static final Collection<SearchResult.Column> safeGetColumns( Search search) {
		return (search == null) ? null : search.getColumns();
	}

	static final Collection<SearchResult.Column> safeGetColumns( SearchResult sr) {
		return (sr == null) ? null : sr.getColumns();
	}

	static final List<Card> safeGetCards(final SearchResult res) {
		return (res != null) ? res.getCards() : null;
	}

}