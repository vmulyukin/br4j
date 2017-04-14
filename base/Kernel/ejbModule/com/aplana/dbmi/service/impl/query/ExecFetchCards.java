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

import com.aplana.dbmi.access.delegate.DelegatorBean;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;
import com.aplana.dbmi.utils.SimpleDBUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author RAbdullin
 *
 */
@SuppressWarnings("all")
public class ExecFetchCards extends JdbcDaoSupport {

	public static final String ATTR_TEMPLATE = "_TEMPLATE";
	public static final String ATTR_STATE = "_STATE";
	public static final String APPROVAL_STATE = "_P_APPROVE_STATE";
	public static final String ATTR_UNIVERSAL_TERM = "_UNITERM";
	public static final String ATTR_DIGITAL_SIGNATURE = "_DIGITAL_SIGNATURE";
	public static final String ATTR_PRELIMINARY_TERM = "_PRELIMINARY_TERM";
	public static final String ATTR_ALL_DOCLINKS = "_ALL_DOCLINKS";
	public final static String LOCK_MANAGEMENT_BEAN = "lockManagement";
	

	// ����������� ������ doExec()
	protected List resultAttributes; // ������ �� Objects[], ������� ������ ������� ��. const COL_XXX
	protected List<Column> resultColumns;
	
	// ������� ������ �������� �� ������ resultAttributes[] as Objects[]:
	static int COL_COUNT = 0;

	static final int COL_CARDID = COL_COUNT++;
	static final int COL_ATTRID = COL_COUNT++;
	static final int COL_VAL_INT = COL_COUNT++;

	static final int COL_VAL_STRING = COL_COUNT++;
	static final int COL_VAL_DATE = COL_COUNT++;
	static final int COL_VAL_REF = COL_COUNT++;

	static final int COL_VAL_OTHER = COL_COUNT++;
	static final int COL_VAL_REF_RU = COL_COUNT++;
	static final int COL_VAL_REF_EN = COL_COUNT++;

	static final int COL_VAL_PERSON = COL_COUNT++;
	static final int COL_VAL_TYPE = COL_COUNT++;
	static final int COL_VAL_BINARY = COL_COUNT++;
	static final int COL_PERSON_CARDID = COL_COUNT++;
	
	private Map OptionsCache = null;
	final protected UserData user;
	final protected Search search;
	protected int hits = 0;
	protected int miss = 0;
	


	protected Integer sessionId;
	/**
	 * Creates new ExecFetchCards instance
	 * @param jdbc JdbcTemplate to use
	 */
	public ExecFetchCards(JdbcTemplate jdbc, UserData auser, Integer session) 
	{
		this(jdbc, auser, null, session);
	}

    public ExecFetchCards(JdbcTemplate jdbc, UserData auser, Search asearch, Integer session) {
		super();
		this.user = auser;
		this.search = asearch;
		this.sessionId = session;
		setJdbcTemplate(jdbc);
		// autoDetectUnicodeUse();
	}

	public SearchResult execute() {
		// (2010/02, RuSA) ��������� ������� ������� - ������ ��������, 
		// � ������� ������ ������ �������� ������������ ...
		if (user == null || user.getPerson() == null)
			// ���������� ��� ���������� - ��� �������...
			return null;

		final Search.Filter filter = (search == null) ? null : search.getFilter();

		// �������-�������� ������ ��� ����������� �������� ...
		final String srcTableName = (filter == null) 
						? ManagerTempTables.TMPTABLEID_CARDID
						: ManagerTempTables.TMPTABLEID_CARDID_WINDOW;
		if (filter != null) {
			// copy gtemp_cardid -> gtemp_result_cardid 
			if (!filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS)) {
				// �������� �� ��������� ������� ������ �� �������� �� ������� ���� �����:
				this.copyPermittedCards(filter.getCurrentUserPermissionString(), user.getPerson().getId());
			} else {
				this.copyAllCards(); 
			}
			// ����������/������� �������
			this.updateOrderColumn(filter); // get count(gtemp_result_cardid.card_id)
			this.makeWindow(search, filter); // gtemp_result_cardid -> gtemp_cardid_w
		}

		// �������� ���� ������� �� ��������
		final Map cardACLMap = this.getCardsPermissions( user.getPerson().getId(), srcTableName);

		/* (!) ���� ������� �� �������... */
		this.doExec(srcTableName);

		/* �������� �������� ��� ���������� ... */
		final SearchResult result = new SearchResult();
		result.setColumns( this.resultColumns );

		final List foundCards = buildCards( cardACLMap, result.getColumns(), this.resultAttributes); 
		result.setCards( foundCards); 

		logger.debug("FetchCards.Attributes.Options cache reads:"+ miss+" hits:"+hits);

		return result;
	}

	static void makeInfoColumns(ResultSetMetaData metaData, StringBuffer dstBuf) 
		throws SQLException {
		if (metaData == null || dstBuf == null) return;
		// column names and type...
		dstBuf.append(String.format("\nDataset has %d columns\n", metaData.getColumnCount()));
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			final String colName = metaData.getColumnLabel(i);
			final String colType = metaData.getColumnTypeName(i);
			final int colLen = metaData.getColumnDisplaySize(i);
			final int colDec = metaData.getPrecision(i);
			dstBuf.append(String.format("\t[%d] %s:%s[%d.%d]\n", i, colName, colType, colLen, colDec));
		}
	}
	
	// static final int CMAXROWS = 100;

	/**
	 * ������� � ����� ������ �� �� ans � ���������� �� ����� maxRows �����.
	 * ���� maxRows < 0, �� ��� ������. 
	 */
	static int makeInfoDataSet(final ResultSet ans, StringBuffer dstBuf, int maxRows) 
		throws SQLException	{
		if (dstBuf == null) return -1;
		dstBuf.append("---------------------------------------------\n");
		if (ans == null) {
			dstBuf.append( "\ndataset is null\n");
			dstBuf.append("---------------------------------------------\n");
			return -1;
		}

		int iRows = 0; 
		while (ans.next()) {
			++iRows;
			if (maxRows >= 0 && iRows > maxRows) {
				dstBuf.append(String.format("\n\t ... %d rows exceeded -> breaking \n", maxRows));
				break;
			}
			try {
				dstBuf.append( String.format("\t[%2d]", iRows));
				for (int i = 1; i <= ans.getMetaData().getColumnCount(); i++) 
				{
					final String s = ans.getString(i);
					dstBuf.append( String.format("\t\'%s\'", s));
				}
				dstBuf.append( "\n" );
			} catch (SQLException ex) {
				dstBuf.append(ex);
			}
		} // while
		if (iRows == 0)
			dstBuf.append("\t EMPTY\n");
		dstBuf.append("---------------------------------------------\n");
		return iRows;
	}

	static Card createEmptyCard(long id) {
		final Card card = new Card();
		card.setAttributes(new ArrayList());
		card.setId(id);
		return card;
	}

	/**
	 * ���� ���������� ��������� ����� ��� ����������� ������.
	 * (<0)  =>  �������������.
	 */
	static final int CDBGROWCOUNT = 50;

    protected Map<Long, Card> getCardsPermissions( ObjectId personId, String srcTableName) {
		Search.Filter filter = (this.search!=null)?this.search.getFilter():null;
		final Map<Long, Card> resultMap = new HashMap<Long, Card>();
		/*
		 * �.�. - 22.07.2011
		 * ����� ������� �������� ����
		*/
		getJdbcTemplate().execute("CREATE TEMP TABLE ACL_CARDS (card_id numeric(9), operation character varying(1));");
		getJdbcTemplate().execute("CREATE INDEX ACL_CARDS_IDX ON ACL_CARDS USING btree(card_id);");
		
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("insert into ACL_CARDS(card_id, operation) ");
		// ���� ������� ������������ - �������, �� ��������� ��� �������� � ������ �������������� � ������
		if (Person.ID_SYSTEM.equals(personId)){
			sqlBuf.append("select c.card_id, 'W' from ").append(srcTableName).append(" c \n");
			sqlBuf.append("union all \n");
			sqlBuf.append("select c.card_id, 'R' from ").append(srcTableName).append(" c \n");
		} else if (filter == null || filter.getCurrentUserPermission().equals(Search.Filter.CU_DONT_CHECK_PERMISSIONS)) {
			// ���� ������ �� ����� ��� � ��� ���������� �������, ��� ��������� ����� �� ����,
			// �� ��� �������� �������� �������� ��� ������ ��� ������, ����� ������� �� �� ��������� � ������ ������ � ������,
			// � ������ ��� ������� �������� �������������, ������� ���� ����� ����� ��-���� �� �� ����� ���� (�������� ������������)
			sqlBuf.append("select c.card_id, 'R' from ").append(srcTableName).append(" c \n");
		} else {
			if (!filter.getTemplatesWithoutPermCheck().isEmpty()) {
				// skip permissions check for specified templates
				sqlBuf.append("select c.card_id, 'R' from card c \n");
				sqlBuf.append("join ").append(srcTableName).append(" src on src.card_id=c.card_id \n");
				sqlBuf.append("where c.template_id in (");
				sqlBuf.append(SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(filter.getTemplatesWithoutPermCheck()));
				sqlBuf.append(")\n");
				sqlBuf.append("union \n");
			}
			sqlBuf.append("select c.card_id, acr.operation_code from access_list al \n");
			sqlBuf.append("join access_card_rule acr on acr.rule_id = al.rule_id \n");
			sqlBuf.append("join ").append(srcTableName).append(" c on c.card_id=al.card_id \n");
			sqlBuf.append("where al.person_id = ").append(personId.getId()).append(" \n");
			sqlBuf.append("union \n");
			sqlBuf.append("select c.card_id, acr.operation_code from card c \n");
			sqlBuf.append("join ( \n");
			sqlBuf.append("select ar.rule_id, ar.template_id, ar.status_id  from person_role pr \n");
			sqlBuf.append("join role_access_rule rar on rar.role_code is null or rar.role_code = pr.role_code  \n");
			sqlBuf.append("join access_rule ar on ar.rule_id = rar.rule_id \n");
			sqlBuf.append("where person_id = ").append(personId.getId()).append(" \n");
			sqlBuf.append(") t on (t.template_id is null or t.template_id = c.template_id) and (t.status_id is null or t.status_id = c.status_id) \n");
			sqlBuf.append("join access_card_rule acr on acr.rule_id = t.rule_id \n");
			sqlBuf.append("join ").append(srcTableName).append(" cc on cc.card_id=c.card_id \n");
			sqlBuf.append(" union  \n");
			sqlBuf.append("select c.card_id, acr.operation_code from card c \n");
			sqlBuf.append("\tjoin access_rule r on (r.template_id=c.template_id) and (r.status_id=c.status_id)\n");
			sqlBuf.append("\tjoin role_access_rule rr on (r.rule_id=rr.rule_id and rr.role_code is NULL)\n");
			sqlBuf.append("\tjoin access_card_rule acr on acr.rule_id = r.rule_id\n");
			sqlBuf.append("\tjoin ").append(srcTableName).append(" cc on cc.card_id=c.card_id \n");
		}
		
		final String sqlText = sqlBuf.toString();
		
		getJdbcTemplate().execute(sqlText);



		final String sInfo =
			"Get permissions SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, null, null);// SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);


		try {
			// (2012/12/11, YNikitin) ���� ����������, ��� �������� ������ �������� ���� ���� ��������� � ����������� �� ����, �� ����� ���� ����� �������
			//m_objectIds.clear();
			getJdbcTemplate().query( "select card_id, operation from ACL_CARDS", new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					if (rs == null) return;
					// supposed to get columns in order:
					// 		1) (int)card_id,
					//		2) (String) permission_type,
					final Long idCard = rs.getLong(1);
					Card card = resultMap.get(idCard);
					if (card == null) { // !resultMap.containsKey(idCard)
						// ������� ����� ��������...
						card = createEmptyCard(idCard);
						resultMap.put(idCard, card);
						// (2012/12/11, YNikitin) ���� ����������, ��� �������� ������ �������� ���� ���� ��������� � ����������� �� ����, �� ����� ���� ����� �������
						//m_objectIds.add(card.getId());
					};

					// ���� �����...
					final String permission = rs.getString(2);
					if ("R".equals(permission))
						card.setCanRead(true);
					// else
					if ("W".equals(permission))
						card.setCanWrite(true);
				}
			});
			// ��������� �������� ������ �������� �� ��, � ������� ���� ������
			removeCardsNotPermission(srcTableName);
		} finally {
			getJdbcTemplate().execute("DROP TABLE if exists ACL_CARDS");
			logger.debug( sInfo + "\n Got permissions cards counter :"+ resultMap.size());
		}
		return resultMap;
	}

	private void removeCardsNotPermission(String srcTableName) {
		final Object[] args 
			= new Object[] { };
		final int[] argTypes
			= new int[] {  };
	
		final String sqlText = 
			"\n" +
			"delete from "+ srcTableName + " c \n" + 
			"where not exists (select 1 from ACL_CARDS acl where c.card_id = acl.card_id) \n";
	
		final String sInfo = 
			"Insert SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);
		int insCount = -1;
		try {
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			insCount = jdbc.update( sqlText, 
					new MapSqlParameterSource());
		} finally {
			final String msg = sInfo + "\n FetchCards deleting cards delete number :"+ insCount;
			logger.debug(msg);
		}
	}
    /**
	 * �������� �� ������� srcTableName �������������������� �������� � 
	 * �������������� ������: Id, � ���������� (permission View/Edit) ��� 
	 * ������������ personId.
	 * @param personId id ������� ��� �������� ����
	 * @param srcTableName �������-�������� id ��������
	 * @return map {cardId -> Card}, ������ �������� � ������ ������������ 
	 * ������ id, canRead/canWrite.
	 */
	@Deprecated
    protected Map /*<Long, Card>*/ getCardsPermissions_old( ObjectId personId,
			String srcTableName) 
	{

		// >>> DEBUG
		if (logger.isDebugEnabled()) 
		{
			final StringBuffer sbuf = new StringBuffer(srcTableName + " before Fetching:");

			getJdbcTemplate().query("select * from "+ srcTableName+ " order by card_id",
					new ResultSetExtractor() {
						public Object extractData(ResultSet rs)
								throws SQLException, DataAccessException {

							// �������� �������
							ExecFetchCards.makeInfoColumns( rs.getMetaData(), sbuf);

							// ���� ������ (�� ����� CDBGROWCOUNT �����)
							ExecFetchCards.makeInfoDataSet( rs, sbuf, CDBGROWCOUNT);

							return null;
						}
					});
			logger.debug( sbuf.toString());
		}
		// <<< DEBUG

		/**
		 * ��� ����������� sql-���������:
		 *   1) (:personID)
		 *   2) (:personID)
		 */
//		final Object[] args = new Object[] { personId.getId(), personId.getId()};
//		final int[] argTypes = new int[] { Types.NUMERIC, Types.NUMERIC};
//
//		final String sqlText = 
//				"SELECT c.card_id, ca.permission_type, ca.role_code \n" +
//				"FROM gtemp_cardId gtmp \n" + 
//				"\t  JOIN card c on c.card_id = gtmp.card_id \n" +
//				// (rem 2010/04/01, RuSA) "\t  JOIN template t on t.template_id = c.template_id \n" +
//				"\t  JOIN card_access ca on \n" + // " ca.permission_type = (?) and " 
//				"\t\t\t   ca.object_id = c.status_id \n" +
//				"\t\t\t  AND ca.template_id = c.template_id \n" +
//				"\n" +
//
//				"WHERE \n" + 
//				"\t (\n" +		// (A)
//
//				// -- ������������ ������ � �������� (�� ����� ����, ��� ���� �����)
//				// � �������� ��� ���� �������� ������� ������� 
//				"\t\t  (ca.person_attribute_code is null) \n" + 
//
//				// ���� ������ ������� ������� ����� ���� ��������� ��� ������ �������� 
//				"\t\t  or ( (ca.person_attribute_code is not null) \n" +
//				"\t\t\t  and exists (\n" + 
//				"\t\t\t\t  select null \n" + 
//				"\t\t\t\t  from attribute_value av \n" + 
//				"\t\t\t\t  where av.attribute_code = ca.person_attribute_code \n" + 
//				"\t\t\t\t\t  and av.number_value = (?) \n" + 		// (:personID) --143 
//				"\t\t\t\t\t  and av.card_id = c.card_id \n" + 
//				"\t\t\t  ) \t  )\n" + 	// -- /or 
//				"\t )\n" + 			// -- /(A) 
//				"\n" +
//
//				// --  ������ �� �����: ������-������
//				//    -- ��� ������ ����������� �� �����...
//				"\t  and ( (ca.role_code is null) \n" + 
//				
//				"\t\t  or ( (ca.role_code is not null) \n" + 
//				//    -- ��� ���������� ���� �������� ���� (ca.role_code in ('A','JBR'))+ 
//				"\t\t\t  and exists(\n" + 
//					CardAccessUtils.buildSqlSelectRoles( 
//						"(?)", "1", "c.template_id", " AND (pr.role_code = ca.role_code) " ) +
//
//				"\t\t\t  ) \n" +				// /and exists( 
//				"\t\t  ) \n" +					// /or 
//				"\t  ) \n"						// /and
//				;

		final Object[] args = new Object[] { personId.getId() };
		final int[] argTypes = new int[] { Types.NUMERIC};
		final String sqlText = 
			DelegatorBean.makeSqlSelectCAWithDelegationsEx(
					"c.card_id, ca.permission_type, ca.role_code",
					"JOIN "+ srcTableName +" AS gtmp ON gtmp.card_id = c.card_id",
					"(:personId)", // personId
					null,  // "ca.permission_type = (:permType)",
					"ca.object_id = c.status_id ", // add card_id cond
					null   // no template cond
				);

		final String sInfo = 
			"Get permissions SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);

		final Map resultMap = new HashMap(); // <Long, Card>
		try {
			
			final NamedParameterJdbcTemplate jdbcNamed = 
				new NamedParameterJdbcTemplate(getJdbcTemplate());
			// getJdbcTemplate().query( sqlText, args, argTypes, 
			jdbcNamed.query( sqlText, 
					new MapSqlParameterSource().addValue("personId", args[0], argTypes[0]),
					new RowCallbackHandler()
					{
						public void processRow(ResultSet rs)
								throws SQLException 
						{
							if (rs == null) return;
							// supposed to get columns in order:  
							// 		1) (int)card_id, 
							//		2) (int) permission_type, 
							// 		3) (str)role_code. 
							final Long idCard = new Long(rs.getLong(1));
							Card card = (Card) resultMap.get(idCard);
							if (card == null) { // !resultMap.containsKey(idCard)
								// ������� ����� ��������...
								card = createEmptyCard(idCard.longValue());
								resultMap.put(idCard, card);
							};

							// ���� �����...
							final int permission = rs.getInt(2);
							if (CardAccess.READ_CARD.intValue() == permission)
								card.setCanRead(true);
							/* else */ if (CardAccess.EDIT_CARD.intValue() == permission)
								card.setCanWrite(true);
						}	
					}
			);
		} finally {
			logger.debug( sInfo + "\n Got permissions cards counter :"+ resultMap.size());
		}
		return resultMap;
	}

	private Map getAttributeOptions(Collection attIDs) {
		final Map options = new HashMap();
		List atList = new ArrayList();
		String paramList = "?";
		for (Iterator i = attIDs.iterator(); i.hasNext(); paramList += ",?")
			atList.add(i.next());
		paramList = paramList.substring(2);
		getJdbcTemplate().query(   // gets options from DB
				"SELECT option_code, option_value, attribute_code FROM attribute_option "+
				"WHERE attribute_code IN("+paramList+")",
				atList.toArray(),
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException
					{
						try {
							final String value = rs.getString(2);
							final String attKey = rs.getString(3);
							byte[] data = new byte[0];
							if (value != null)
								data = value.getBytes("UTF-8");

							if (!options.containsKey(attKey))
								options.put(attKey, new HashMap(5));

							( (Map)options.get(attKey) ).put(rs.getString(1), data);
						} catch (UnsupportedEncodingException e) {}
					}
				});
		miss++;
		return options;
	}


	/**
	 * �������� ����� id �������� �� ������� -> �������
	 * @return
	 */
	static Map<Object, Column> makeCol2AttributeMap(Collection<Column> columns) {
		final HashMap<Object, Column> result = new HashMap<Object, Column>(); // id �������� �� ������� -> �������
		for (final Column col : columns) {
			result.put(col.getAttributeId().getId(), col);
		}
		return result;
	}


	protected List<Card> buildCards( Map<Long, Card> cardACLMap, Collection<Column> columns, List attributes) {
		// id �������� �� ������� -> �������
		final Map<Object, Column> attrMap = makeCol2AttributeMap(columns);

		// ����������� ������� ����-�� ����� link-������� 
		// � ����������� ��������� ����� "id-������� -> ��������� �������" 
		{
			boolean linkDefined = false;
			if (columns != null)
				for (final Column col : columns) {
					if (col.isLinked()) {
						linkDefined = true;
						break;
					}
				}
			if (!linkDefined && columns != null) { // ��� ����� link-�������...
				Column col = attrMap.get(Attribute.ID_NAME.getId());
				if (col == null) {// ���� ��� ����� ������� "NAME", �� ������ ����� ����� ������ 
					// �� ������� ������
					for (Column column : columns) {
						col = column;
						if (!col.isIcon()) {
							break;
						}
					}
				}
				if (col != null)
					col.setLinked(true);
			}
		}
		final List<Card> resultList = new ArrayList<Card>();
		final Set resultIds = new HashSet();
		for (Object attribute : attributes) {
			final Object[] row = (Object[]) attribute;
			final Long idCard = (Long) row[COL_CARDID];
			Card card;
			if (!cardACLMap.containsKey(idCard)) {
				// (!?) �������� ��������, ������� ��� � ������ �������� (ACL) ...
				// ... ����� ������
				card = createEmptyCard(idCard);
				cardACLMap.put(idCard, card);
			} else {
				card = cardACLMap.get(idCard);
			}

			final String idAttr = (String) row[COL_ATTRID];
			final Column attrCol = attrMap.get(idAttr);
			ObjectId attributeId = null;
			if (attrCol != null) {
				attributeId = attrCol.getAttributeId();
			}

			if (attributeId == null) {
				// ����� ������ ��������� _STATE, _TEMPLATE, _BACKLINK
				// ... ����������� id �������� �� ��� ���� � ���� ...
				attributeId = new ObjectId(AttrUtils.getAttrTypeClass(row[COL_VAL_TYPE]), row[COL_ATTRID]);
				// ������ �� ������� ��� �������� !?
				// logger.warn( "Attribute \'" + idAttr+ "\' loaded but not present in the search object -> skipped");
				// continue;
			}

			// ��������� � ������� ��������
			Attribute attr = card.getAttributeById(attributeId);
			if (attr == null) {
				// ��� BackLinkAttribute ������� ������� FoundBackLinkAttribute (����� �� ������ ������� �������� ���������)
				/*if (BackLinkAttribute.class.equals(attributeId.getType())) {
					attr = new FoundBackLinkAttribute();
					attr.setId((String) attributeId.getId());
				} else {*/
				attr = DataObject.createFromId(attributeId);
				//}
				card.getAttributes().add(attr);
			}

			if (OptionsCache == null)
				OptionsCache = getAttributeOptions(attrMap.keySet());

			extractAttributeOptions(attr, OptionsCache);

			//setting a date timepattern based on column settings
			for (Object column : columns) {
				Column c = (Column) column;
				if (c.getAttributeId().equals(attr.getId())
						&& attr instanceof DateAttribute) {
					((DateAttribute) attr).setTimePattern(c.getTimePattern());
				}
			}

			if (StringAttribute.class.equals(attr.getClass()))
				((StringAttribute) attr).setValue((String) row[COL_VAL_STRING]);
			else if (TextAttribute.class.equals(attr.getClass()))
				((TextAttribute) attr).setValue((String) row[COL_VAL_STRING]);
			else if (IntegerAttribute.class.equals(attr.getClass()))
				((IntegerAttribute) attr).setValue(((Long) row[COL_VAL_INT]).intValue());
			else if (DateAttribute.class.equals(attr.getClass())) {
				try {
					((DateAttribute) attr).setValueWithTZ((Date) row[COL_VAL_DATE]);
				} catch (NullPointerException e) {
				}
			} else if (ListAttribute.class.equals(attr.getClass()) ||
					TreeAttribute.class.equals(attr.getClass())) {
				final ReferenceValue value = new ReferenceValue();
				value.setId(((Long) row[COL_VAL_REF]).longValue());
				if (ReferenceValue.ID_ANOTHER.equals(value.getId())) {
					value.setValueRu((String) row[COL_VAL_OTHER]);
					value.setValueEn((String) row[COL_VAL_OTHER]);
				} else {
					value.setValueRu((String) row[COL_VAL_REF_RU]);
					value.setValueEn((String) row[COL_VAL_REF_EN]);
				}
				if (ListAttribute.class.equals(attr.getClass()))
					((ListAttribute) attr).setValue(value);
				else {
					TreeAttribute tree = (TreeAttribute) attr;
					if (tree.getValues() == null) {
						tree.setValues(new ArrayList());
					}
					tree.getValues().add(value);
				}
			} else if (PersonAttribute.class.equals(attr.getClass())) {
				final Person person = new Person();
				person.setId(((Long) row[COL_VAL_INT]).longValue());
				person.setFullName((String) row[COL_VAL_PERSON]);
				person.setCardId(new ObjectId(Card.class,
						((Long) row[COL_PERSON_CARDID]).longValue()));
				final PersonAttribute pa = (PersonAttribute) attr;
				if (pa.getValues() == null) {
					pa.setValues(new ArrayList());
				}
				pa.getValues().add(person);
			} else if (CardLinkAttribute.class.equals(attr.getClass())) {
				final Card linkedCard = new Card();
				linkedCard.setId(((Long) row[COL_VAL_INT]).longValue());
				final CardLinkAttribute linksAttr = (CardLinkAttribute) attr;
				// ��������� ������ id ��� ����������� �������� ��������...
				if (linksAttr.getLabelAttrId() == null)
					linksAttr.setLabelAttrId((attrCol != null) ? attrCol.getLabelAttrId() : null);
				linksAttr.addLabelLinkedCard(linkedCard);
			} else if (TypedCardLinkAttribute.class.equals(attr.getClass())) {
				final Card linkedCard = new Card();
				linkedCard.setId(((Long) row[COL_VAL_INT]).longValue());

				TypedCardLinkAttribute linksAttr = (TypedCardLinkAttribute) attr;

				// ��������� ������ id ��� ����������� �������� ��������...
				if (linksAttr.getLabelAttrId() == null)
					linksAttr.setLabelAttrId((attrCol != null) ? attrCol.getLabelAttrId() : null);
				linksAttr.addLabelLinkedCard(linkedCard);
				final Long typeId = Long.valueOf(0).equals(row[COL_VAL_REF]) ? null : (Long) row[COL_VAL_REF];
				linksAttr.addType((Long) row[COL_VAL_INT], typeId);
			} else if (DatedTypedCardLinkAttribute.class.equals(attr.getClass())) {
				final Card linkedCard = new Card();
				linkedCard.setId(((Long) row[COL_VAL_INT]).longValue());

				DatedTypedCardLinkAttribute linksAttr = (DatedTypedCardLinkAttribute) attr;

				// ��������� ������ id ��� ����������� �������� ��������...
				if (linksAttr.getLabelAttrId() == null)
					linksAttr.setLabelAttrId((attrCol != null) ? attrCol.getLabelAttrId() : null);
				linksAttr.addLabelLinkedCard(linkedCard);
				final Long typeId = Long.valueOf(0).equals(row[COL_VAL_REF]) ? null : (Long) row[COL_VAL_REF];
				linksAttr.addType((Long) row[COL_VAL_INT], typeId);
				linksAttr.addDate((Long) row[COL_VAL_INT], DateUtils.setValueWithTZ((Date) row[COL_VAL_DATE]));
			} else if (MaterialAttribute.class.equals(attr.getClass())) {
				final String fileName = (String) row[COL_VAL_STRING];
				final int materialType = ((Long) (row[COL_VAL_REF] == null ? MaterialAttribute.MATERIAL_NONE : row[COL_VAL_REF])).intValue();
				((MaterialAttribute) attr).setMaterialType(materialType);
				((MaterialAttribute) attr).setMaterialName(fileName);
			} else if (attr instanceof BackLinkAttribute) { // (!) BackLinkAttribute 
				final long mainId = ((Long) row[COL_VAL_INT]).longValue();
				((BackLinkAttribute) attr).setLinked(mainId > 0);
				if (mainId > 0) {
					final Card linkedCard = new Card();
					linkedCard.setId(mainId);
					final BackLinkAttribute linksAttr = (BackLinkAttribute) attr;
					// ��������� ������ id ��� ����������� �������� ��������...
					if (linksAttr.getLabelAttrId() == null) {
						linksAttr.setLabelAttrId((attrCol != null) ? attrCol.getLabelAttrId() : null);
					}
					linksAttr.addLabelLinkedCard(linkedCard);
				}
			} else if (HtmlAttribute.class.equals(attr.getClass())) {
				((HtmlAttribute) attr).setValue((String) row[COL_VAL_BINARY]);
			}


			if (ATTR_TEMPLATE.equals(idAttr)) {
				card.setTemplate(((Long) row[COL_VAL_REF]).longValue());
				card.setTemplateNameRu((String) row[COL_VAL_REF_RU]);
				card.setTemplateNameEn((String) row[COL_VAL_REF_EN]);
			} else if (ATTR_STATE.equals(idAttr)) {
				card.setState(new ObjectId(CardState.class, ((Long) row[COL_VAL_REF]).longValue()));
			}

			if (!resultIds.contains(idCard)) {
				resultIds.add(idCard);
				resultList.add(card);
			}
		}
		return resultList;
	}

	/**
	 * �������� �� �� ��������� �������� ����� - ������������ ������ ��� ���.
	 * true in order to use unicode in query strings.
	 * By default the value (ORACLE) SYS_CONTEXT('USERENV','LANGUAGE') 
	 * is checked upon including substring "UTF8". 
	 * @return true if unicode is enabled.

	private boolean autoDetectUnicodeUse()
	{
		boolean result = false;
		// ������:
		// 		SELECT SYS_CONTEXT('USERENV','LANGUAGE') FROM dual
		// 		���������: "RUSSIAN_CIS.CL8MSWIN1251"
		// 
		final String envLang 
			= (String) getJdbcTemplate().queryForObject(
				"SELECT SYS_CONTEXT('USERENV','LANGUAGE') FROM dual",
				String.class
				);
		
		if (!StrUtils.isStringEmpty(envLang))
		{
			final int posDot = envLang.lastIndexOf('.');
			if (posDot >= 0) {
				result = "UTF8".equalsIgnoreCase(	envLang.substring(posDot + 1));
			}
		}
		return result;
	}
	 */

	/**
	 * ������������������ ������� ������:
	 *  attribute_code character varying(20),\n" +	// -1-
	 *  column_with numeric,\n" +    				// -2-
	 *  order_in_list numeric(4),\n" + 				// -3-

	 *  attr_name_rus character varying(128),\n" + 	// -4-
	 *  attr_name_eng character varying(128),\n" + 	// -5-
     *  data_type character(1),\n" + 				// -6-

	 *	sorting numeric(1),\n" + 					// -7-
	 *	linked numeric(1),\n" + 					// -8-
	 * ����� ��� ��������������:
	 *	attr_label character varying(20) ,\n" + 	// -9- (+)(2009/12/11, RuSA) 
     *	attr_label_dataType character(1) \n" + 		// -10- (+)(2009/12/11, RuSA)
	 */
	public class InternalColumnFetchMapper implements RowMapper {
		/**
		 * ������� ���������.
		 */
		public final static int FLDIND_ATTRCODE   = 1;
		public final static int FLDIND_WIDTH      = 2;
		// public final static int FLDIND_ORDER      = 3; unused

		public final static int FLDIND_CAPTION_RU = 4;
		public final static int FLDIND_CAPTION_EN = 5;
		public final static int FLDIND_ATTRTYPE   = 6;
		
		public final static int FLDIND_SORTING    = 7;
		public final static int FLDIND_ISLINKED   = 8;
		public final static int FLDIND_LABELCODE  = 9;

		public final static int FLDIND_LABELTYPE  = 10;
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			final SearchResult.Column column = new SearchResult.Column();
			
			column.setAttributeId( AttributeTypes.createAttributeId(
					rs.getString(FLDIND_ATTRTYPE), rs.getString(FLDIND_ATTRCODE)
					));

			if (rs.getObject(FLDIND_WIDTH) != null)
				column.setWidth(rs.getInt(FLDIND_WIDTH));

			column.setNameRu(rs.getString(FLDIND_CAPTION_RU));
			column.setNameEn(rs.getString(FLDIND_CAPTION_EN));

			if (rs.getObject(FLDIND_SORTING) != null)
				column.setSorting(rs.getInt(FLDIND_SORTING));

			if (rs.getObject(FLDIND_ISLINKED) != null)
				column.setLinked(rs.getInt(FLDIND_ISLINKED) == 1);

			// ������ �� �������, ������� ���� �������� ������ 
			// �������� ����� �� ����
			final int colCount = rs.getMetaData().getColumnCount(); 
			if (colCount > 8) {
				final String labelAttrType = rs.getString(FLDIND_LABELTYPE); 
				if ( labelAttrType != null)
					column.setLabelAttrId( AttributeTypes.createAttributeId( 
							labelAttrType, rs.getString(FLDIND_LABELCODE)));
			}
			return column;
		}
	}

	protected void doExec(String srcTableName) {
		int predefined_columns 
			// = SimpleDBUtils.sqlGetTableRowsCount( getJdbcTemplate(), "gtemp_attr_fetch");
			= (this.resultColumns != null) ? this.resultColumns.size() : 0;

		// final boolean addedDefaultColumns = predefined_columns == 0 ? true : false;

		if (predefined_columns == 0) {

			// int template_count = SimpleDBUtils.sqlGetTableRowsCount( getJdbcTemplate(), "gtemp_template_list");
			long templateId = -1; // (-1) = multi templates

			try {
				templateId = getJdbcTemplate().queryForLong(
							"select distinct c.template_id \n" + 
							"from card c join "+srcTableName+" gtmp \n" +
							"	on gtmp.card_id = c.card_id \n"
						);
			} catch (Exception e) { 
				// ���� �� �������, ���� � sql-������� ����� ����� ������ ��������
				// template_count > 1; 
				templateId = -1; 
			}

			if (templateId > 0) { // ��������� �������� �� ������������� ������� ...
				this.resultColumns = getJdbcTemplate().query(
						"SELECT t.attribute_code, t.column_width, t.order_in_list, \n"
						+ "\t\t  a.attr_name_rus, a.attr_name_eng, a.data_type, \n" 
						+ "\t\t  null as sorting, null as linked \n"
						
						// >>> (2010/01, RuSA) ������� �������� ����������.
						+ "\t FROM template_attribute t \n"
						+ "\t INNER JOIN attribute a ON a.attribute_code=t.attribute_code \n"
						+ "WHERE t.template_id = " + String.valueOf(templateId) + "\n" 
						+ "\t	and t.order_in_list is not null \n"
						+ "\t	and t.is_hidden = 0 \n"
						+ "ORDER BY t.order_in_list\n",
						new InternalColumnFetchMapper()
						// <<< (2010/01, RuSA)
					);
				predefined_columns = (this.resultColumns != null) ? this.resultColumns.size() : 0;
			}

		}

		if (predefined_columns == 0) { // ��������� ����������� ����� ������� 
			// (2009/12/12, RuSA) �������� ������� ����� Insert-�������� �����...
			this.resultColumns = getJdbcTemplate().query(
					"SELECT * FROM (\n" +
					"  SELECT \n" +
					"    attribute_code, \n" +
					"    column_width,\n" + 
					"    (CASE \n" + 
					"        WHEN attribute_code = 'NAME'    then 1\n" + 
					"        WHEN attribute_code = 'DESCR'   then 2\n" + 
					"        WHEN attribute_code = 'REGION'  then 3\n" + 
					"        WHEN attribute_code = 'AUTHOR'  then 4\n" + 
					"        WHEN attribute_code = 'CHANGED' then 5\n" + 
					"        ELSE 6 \n" + // for safety 
					"     END \n" + 
					"    ) as order_in_list,\n" + 

					"    attr_name_rus,\n" + 
					"    attr_name_eng,\n" + 
					"    data_type,\n" + 

					"    cast(null as numeric) as sorting,\n" +
					"    (CASE \n" + 
					"        WHEN attribute_code = 'NAME' then 1 \n" + 
					"        ELSE cast(null as numeric) \n" + 
					"     END \n" + 
					"    ) as linked\n" + 
					"  FROM attribute\n" +

					"  WHERE attribute_code in ( 'NAME', 'DESCR', 'REGION', 'AUTHOR', 'CHANGED')\n" + 
					"\n" + 
					"  UNION SELECT \n" +

					"     '_TEMPLATE',\n" + 
					"     20, \n" + 
					"     6, \n" +

					"     '" + ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS).getString("search.common.template") + "',\n" +
					"     'Template', \n" +  
					"     'L', \n" +

					"     cast(null as numeric) as sorting, \n" + 
					"     cast(null as numeric) as linked \n" + 

//					"  FROM dual \n" + // -- (!) ��� oracle ���� ������� dual, ��� EDB �������������, ��� PostgreSQL ������ 
					") as Tt ORDER BY 3 \n"
					, new InternalColumnFetchMapper()
				);
			predefined_columns = (this.resultColumns != null) ? this.resultColumns.size() : 0;
		}

		final String sAttrCodesList = makeAttrCodesSqlList( this.resultColumns);

		/***********************************************************************
		 * ������������ ������ ������� �� �������� ���������...
		 */
		final StringBuffer lv_sql 
			= new StringBuffer();

		 RowCountCallbackHandler countCallback = new RowCountCallbackHandler();  // not reusable
		 getJdbcTemplate().query("select * from " + srcTableName, countCallback);
		 int rowCount = countCallback.getRowCount();
		 
			lv_sql.append(
				"SELECT a.attr_value_id, \n"		// 1
				+ "\t  a.card_id, \n"			// 2
				+ "\t  a.attribute_code, \n"	// 3
	
				+ "\t  a.number_value, \n"		// 4
				+ "\t  a.string_value, \n"		// 5
				+ "\t  a.date_value,  \n"		// 6
	
				+ "\t  a.value_id, \n"			// 7
				+ "\t  a.another_value, \n"		// 8
				+ "\t  v.value_rus, \n"			// 9
	
				+ "\t  v.value_eng, \n" 		// 10
				+ "\t  p.full_name, \n"			// 11
				+ "\t  gtemp.ordNum as ord, \n"	// 12
	
				+ "\t  attr.data_type, \n"		// 13
				+ "\t  a.long_binary_value, \n"	// 14
				+ "\t  p.card_id \n"			// 15
	
				+ "FROM attribute_value a  \n"
				+ "		INNER JOIN attribute attr on attr.attribute_code=a.attribute_code \n"
				+ "		LEFT OUTER JOIN values_list v on a.value_id=v.value_id \n"
				+ "		LEFT OUTER JOIN person p on a.number_value=p.person_id \n"
				+ "		INNER JOIN "+ srcTableName+ " AS gtemp on a.card_id=gtemp.card_id \n"
				+ "WHERE \n"
				+ "		a.attribute_code in (" + sAttrCodesList + ",'CREATED') \n"
			);

		// ���� ������������� "������" ...
		int need_attribute = countAttributes( this.resultColumns, ATTR_TEMPLATE); // "_TEMPLATE"
		if (need_attribute > 0) 
		{
			lv_sql.append(
				"\n UNION ALL SELECT \n"
				+ "\t  null as attr_value_id, \n"						// 1
				+ "\t  c.card_id, \n"									// 2
				+ "\t '_TEMPLATE' as attribute_code, \n"				// 3

				+ "\t  null as number_value, \n" 						// 4
				+ "\t  null as string_value, \n"						// 5
				+ "\t null as date_value, \n"							// 6

				+ "\t  t.template_id as value_id, \n"					// 7
				+ "\t  null as another_value, \n"						// 8
				+ "\t  t.template_name_rus as value_rus, \n"			// 9

				+ "\t  t.template_name_eng as value_eng, \n"			// 10
				+ "\t  null as full_name, \n"							// 11
				+ "\t  gtemp.ordNum as ord, \n"							// 12

				+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"				// 13
				+ "\t null as long_binary_value, \n"						// 14
				+ "\t null as card_id \n"							// 15

				+ "FROM \n"
				+ "\t  card c \n"
				+ "\t  inner join \n"
				+ "\t  template t on c.template_id=t.template_id \n"
				+ "\t  INNER JOIN "+ srcTableName+ " AS gtemp on c.card_id=gtemp.card_id \n"
			);
		}

		// ���� ������������� "���������" ...
//		need_attribute = 1; // "_STATE" ������ ����������
		need_attribute = countAttributes( this.resultColumns, ATTR_STATE); // "_STATE"
		if (need_attribute > 0) {
			lv_sql.append(
				"\n UNION ALL SELECT \n"

				+ "\t   null as attr_value_id, \n"				// 1
				+ "\t   c.card_id, \n"							// 2
				+ "\t   '_STATE' as attribute_code, \n"			// 3

				+ "\t   null as number_value, \n"				// 4
				+ "\t   null as string_value, \n"				// 5
				+ "\t   null as date_value, \n"					// 6

				+ "\t   c.status_id as value_id, \n"			// 7
				+ "\t   null as another_value, \n"				// 8
				// '||-- null as long_binary_value, 
				+ "\t   s.name_rus as value_rus, \n"			// 9

				+ "\t   s.name_eng as value_eng, \n"			// 10
				+ "\t   null as full_name, \n"					// 11
				+ "\t   gtemp.ordNum as ord, \n"				// 12

				+ "\t \'"+ Attribute.TYPE_LIST + "\', \n"		// 13
				+ "\t null as long_binary_value, \n"			// 14
				+ "\t null as card_id \n"						// 15

				+ "FROM \n"
				+ "\t   card c inner join card_status s on c.status_id=s.status_id \n"
				+ "\t  INNER JOIN "+ srcTableName+ " AS gtemp on c.card_id=gtemp.card_id \n"
			);
		}

		// ���� ������������� "��������" ...
		need_attribute = countAttributesByType( this.resultColumns, "M" );
		if (need_attribute > 0) {
			lv_sql.append( 
				"\n UNION SELECT \n"

				+ "\t  null as attr_value_id, \n"			// 1
				+ "\t  c.card_id, \n"						// 2
				+ "\t  'MATERIAL' as attribute_code, \n"	// 3

				+ "\t  null as number_value,  \n"			// 4
				+ "\t  coalesce(c.file_name, c.external_path) as string_value,  \n"	// 5
				+ "\t  null as date_value, \n"				// 6

				+ "\t  CASE \n"
				+ "\t\t  WHEN c.file_name IS NOT NULL THEN " + MaterialAttribute.MATERIAL_FILE + "\n"
				+ "\t\t  WHEN c.external_path IS NOT NULL THEN " + MaterialAttribute.MATERIAL_URL + "\n"
				+ "\t  END as value_id, \n"				// 7
				+ "\t  null as another_value, \n"			// 8 
				// '||-- c.file_storage as long_binary_value, 
				+ "\t  null as value_rus, \n"				// 9

				+ "\t  null as value_eng, \n"				// 10
				+ "\t  null as full_name, \n"				// 11
				+ "\t  gtemp.ordNum as ord, \n"				// 12

				+ "\t \'"+ Attribute.TYPE_MATERIAL + "\', \n"	// 13
				+ "\t null as long_binary_value, \n"			// 14
				+ "\t null as card_id \n"						// 15

				+ "FROM card c \n"
				+ "\t  INNER JOIN "+ srcTableName+ " AS gtemp on c.card_id=gtemp.card_id \n"
			);
		}
		// ���� ������������� "back-link" ...
		need_attribute = countAttributesByType( this.resultColumns, "B");
		if (need_attribute > 0) {
			lv_sql.append( 
				"\n UNION SELECT \n"

				+ "\t  null as attr_value_id, \n"					// 1
				+ "\t  c.card_id, \n"								// 2 
				+ "\t  a.attribute_code, \n"						// 3

				// ��������, �� ������� ���� ������ �� ������ ����� cardlink, ����-��� backlink'�...
				+ "\t  avLinkFrom.card_id as number_value, \n"		// 4
				+ "\t  null as string_value,  \n"					// 5
				+ "\t  null as date_value, \n"						// 6

				+ "\t  null as value_id, \n"						// 7
				+ "\t  null as another_value, \n" 					// 8
				+ "\t  null as value_rus, \n"						// 9 

				+ "\t  null as value_eng, \n"						// 10
				+ "\t  null as full_name, \n"						// 11
				+ "\t  gtemp.ordNum as ord, \n"						// 12

				+ "\t \'"+ Attribute.TYPE_BACK_LINK + "\', \n"		// 13
				+ "\t null as long_binary_value, \n"				// 14
				+ "\t null as card_id \n"							// 15

				+ "FROM card c \n"
				+ "\t INNER JOIN "+ srcTableName+ " AS gtemp on c.card_id=gtemp.card_id \n"
				+ "\t INNER JOIN template_block tb on tb.template_id = c.template_id \n" 
				+ "\t INNER JOIN attribute a on a.block_code=tb.block_code and a.data_type='B' \n"
				+ "\t LEFT OUTER JOIN attribute_option ao on a.attribute_code = ao.attribute_code and ao.option_code = 'LINK' \n"
				+ "\t LEFT OUTER JOIN attribute_value avLinkFrom \n" 
				+ "\t		on avLinkFrom.number_value=c.card_id \n"
				+ "\t		and avLinkFrom.attribute_code in ( \n"
				+ "\t			select * from functionsplit(ao.option_value, ';') \n"
				+ "\t		) \n"
			);
		}

		// (!) ������� �������� ������ �������� ������ - ���������������� ��������.
		lv_sql.append("ORDER BY ord, attr_value_id\n"); 

		/* Getting result */
		// (!) ����������...
		// open pout_cards for lv_sql;
		this.resultAttributes = getJdbcTemplate().query(
				lv_sql.toString(), 
				new RowMapper() 
				{
					public Object mapRow(ResultSet rs, int rowNum) 
						throws SQLException
					{
						Object[] row = new Object[COL_COUNT];

						row[COL_CARDID] = new Long(rs.getLong(2));
						row[COL_ATTRID] = rs.getString(3);
						row[COL_VAL_INT] = new Long(rs.getLong(4));

						row[COL_VAL_STRING] = rs.getString(5);
						row[COL_VAL_DATE] = rs.getTimestamp(6);
						row[COL_VAL_REF] = new Long(rs.getLong(7));

						row[COL_VAL_OTHER] = rs.getString(8);
						row[COL_VAL_REF_RU] = rs.getString(9);
						row[COL_VAL_REF_EN] = rs.getString(10);

						row[COL_VAL_PERSON] = rs.getString(11);
						row[COL_VAL_TYPE]= rs.getString(13);

						String strBinary = null;
						try {
							strBinary = SimpleDBUtils.getBlobAsStr(rs, 14, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							strBinary = rs.getString(14);
						} 
						row[COL_VAL_BINARY]= strBinary;
						row[COL_PERSON_CARDID] = new Long(rs.getLong(15));

						return row;
					}
				}
		);
		logger.debug("Values of columns fetched. Total number:" + this.resultAttributes.size());
	}

	/**
	 * ������ ������ �������������� ������� (� �� ��������).
	 * @param value: ������ �������.
	 * @param loadMetaData: false=����� ������������ value, ������, ��� � ��� 
	 * ��� ��� ���������;
	 * true=����� ��������� ������ ���������� ��� ����� �������: 
	 * 	- label � label-�������� (�� ����������� �������������� ID � 
	 * �������� value),
	 * 	- ������ ���������� ���� � reloadMetaData[] ������ �������� Width = 0, 
	 * 	- ������� �������� ���� reloadMetaData[] ������ nameRu=null, 
	 * 	- ����-��, ���� nameEn=null. 
	 * .
	 */
	void setResultColumns(Collection<SearchResult.Column> value, boolean reloadMetaData) {
		if (value != null && !value.isEmpty() && reloadMetaData) {
			/* ��������� ID � ��������� ������ �������... */

			// ��������� �������� ��������� �� ��...
			final ColumnDefSet attrSorted = loadAllColumnDefs(value);

			// ������� �������, ����������� � ����������� �������� ������� �� ��... 
			this.resultColumns = new ArrayList(value.size());
			for (SearchResult.Column itemCol : value) {
				final SearchResult.Column newcolumn = makeDestColumn( itemCol, attrSorted);
				resultColumns.add(newcolumn);
			}

		} else { // null ��� empty, ��� "��� ��������" ...
			this.resultColumns = (value == null) ? null : new ArrayList(value);
		}
	}

	/**
	 * ��������� �������� ��������� �� ��.
	 * ������������ �������� � label-�������� �� ������ columns.
	 * @param columns
	 * @return
	 */
	ColumnDefSet loadAllColumnDefs(Collection columns) {
		final String idAttrList = makeAllAttrCodesSqlList(columns);
		final String query = MessageFormat.format(
				"SELECT a.attribute_code, \n" +	// 1
				"\t  a.column_width, \n" +		// 2
				"\t  null, \n" + 				// 3
				"\t  a.attr_name_rus, \n" + 	// 4
				"\t  a.attr_name_eng, \n" + 	// 5
				"\t  a.data_type, \n" +  		// 6
				"\t  null as sorting, \n" + 	// 7
				"\t  null as linked \n" + 		// 8
				"FROM attribute a \n" +
				"WHERE a.attribute_code in ({0}) \n" +
				"ORDER BY a.attribute_code\n",
				idAttrList);
		final List attrRaw = getJdbcTemplate().query( query, new InternalColumnFetchMapper());
		return ColumnDefSet.createByCollection(attrRaw);
	}
	
	/**
	 * @param itemCol: �������, � ������� ��������� attrId, �������� labelAttrId,
	 * ������, ��� � ���� ��������. 
	 * @param attrdefs: ����������� ��������� ������� (�� ������ ��), ��������
	 * ��������� Width, NameRu, NameEn ������������ ������ ����-��� �� itemCol, 
	 * ������ ���� � itemCol ��� ������������ (Width=0, NameXX=null).
	 * @return
	 */
	static Column makeDestColumn(Column column, ColumnDefSet attrdefs) {
		if (column == null) return null;

		final Column result = column.copy();

		if (attrdefs != null) {

			// ���������� ����������� ���� ��� ��������...
			Column colDef = null;
			final String attrCodeName = column.getAttributeId().getId().toString();

			if (!isSpecialAttrCode(attrCodeName)) {
				colDef = attrdefs.getByCodeName(attrCodeName);
				if (colDef != null) {

					result.setAttributeId( colDef.getAttributeId());

					if (column.getNameRu() == null)
						result.setNameRu(  colDef.getNameRu());

					if (column.getNameEn() == null)
						result.setNameEn(  colDef.getNameEn());
				}
			}

			// ���� ����� �������-�������, �� ������� ���� ��������� ������...
			if (column.getLabelAttrId() != null) {
				// ... ��� ������������� �������� ...
				final String labelCodeName = column.getLabelAttrId().getId().toString();
				if (!isSpecialAttrCode(labelCodeName)) {
					colDef = attrdefs.getByCodeName(labelCodeName);
					// �������� Width/Ru/En ��� ������������� �������� ����� ������� ��������
					if (colDef != null) 
						result.setLabelAttrId( colDef.getAttributeId());
				}
			}
			
			// ���������� ���������� �������� (�� ������ �� ���������)
			if (colDef != null) {
				if (column.getWidth() == 0 && colDef.getWidth() > 0)
					result.setWidth( colDef.getWidth());
			}
		}

		return result;
	}

	/**
	 * @param attrCodeName
	 * @return true, ���� ������� �������� ����������� ("_STATE" ��� "_TEMPLATE").
	 */
	static boolean isSpecialAttrCode(String attrCodeName) {
		return (ATTR_STATE.equals(attrCodeName) || ATTR_TEMPLATE.equals(attrCodeName));
	}

	/**
	 * @param resColumns: ��������� �������.
	 * @return ������: ��������-����� ������� � ��������� �������� ����� ���.
	 */
	public static String makeAttrCodesSqlList(final Collection<Column> resColumns) {
		if (resColumns == null) return "'-1'";
		
		final StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (Column column : resColumns) {
			if (!column.isHidden()) { // ������� ������ ��������� �������
				if (!first)
					buf.append(',');
				first = false;
				buf.append("'")
						.append(column.getAttributeId().getId())
						.append("'");
			}
		}
		return buf.toString();
	}

	/**
	 * @param resColumns: ��������� �������.
	 * @return ������: ��������-����� ������� � label-������� � ��������� 
	 * �������� ����� ���.
	 */
	public static String makeAllAttrCodesSqlList(final Collection<Column> resColumns) {

		if (resColumns == null) return "'-1'";
		
		// �������� ����� ������ ��������������� ...
		final HashSet<ObjectId> summaryIds = new HashSet<ObjectId>();
		for (Column column : resColumns) {
			if (!column.isHidden()) { // ������� ������ ��������� �������
				summaryIds.add(column.getAttributeId());
				if (column.getLabelAttrId() != null)
					summaryIds.add(column.getLabelAttrId());
			}
		}
		
		// ��� � ��������� ...
		return makeIdSqlList(summaryIds);
	}
	
	/**
	 * �������� Sql-������ ��������� �������� ID �� ������ ���������������.
	 * @param ids: ������ ���������������.
	 * @return: ������ ��������������� � ��������� �������� ����� ���.
	 */
	public static String makeIdSqlList(Collection<ObjectId> ids) {
		if (ids == null || ids.isEmpty()) return "'-1'";

		final StringBuilder buf = new StringBuilder();
		for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
			final ObjectId id = (ObjectId) iterator.next();
			buf	.append("'")
				.append( id.getId())
				.append("'"); 
			if (iterator.hasNext()) buf.append(',');
		}
		return buf.toString();
	}

	protected int countAttributes(List<Column> resColumns, String attrCodeName) {
		if (resColumns == null || attrCodeName == null) 
			return -1;

		int result = 0;
		for (Column column : resColumns) {
			if (attrCodeName.equals(column.getAttributeId().getId().toString()))
				++result;
		}
		return result;
	}

	/**
	 * @param resColumns
	 * @param attrType
	 * @return
	 */
	protected int countAttributesByType(List<Column> resColumns, String attrType) {
		if (resColumns == null) 
			return -1;
		
		int result = 0;
		final Class attrClass = AttributeTypes.getAttributeClass(attrType);

		for (final Column column : resColumns) {
			if (attrClass.equals(column.getAttributeId().getType()))
				++result;
		}
		return result;
		
	}

	private void extractAttributeOptions(final Attribute attr, final Map cache) {
		try {
			if (cache.containsKey(attr.getId().getId())){  // gets options from cache
				Map options = (Map)cache.get(attr.getId().getId());
				for (Iterator i = options.entrySet().iterator(); i.hasNext(); ){
					Map.Entry opt = (Map.Entry)i.next();
					AttributeOptions.extractOption(attr, (String)opt.getKey(),
							new ByteArrayInputStream((byte[])opt.getValue()),
							getJdbcTemplate(), false);
				}
				hits++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ������ �� ������� "gtemp_cardid" ����������� ������������ personId ��� 
	 *���������� �������� permission.
	 */
	protected void remRestictedCards( String permission, ObjectId personId) {
		/**
		 * ��� ����������� sql-���������:
		 *   1) (:permissionType)
		 *   2) (:personID)
		 *   3) (:personID)
		 */
		// ��� �����������: ( cardId, permissionType, personId, cardId )
		final Object[] args 
			= new Object[] { permission, personId.getId(), personId.getId() };
		final int[] argTypes
			= new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC };

		if (logger.isDebugEnabled()) {
			final long counter = SimpleDBUtils.sqlGetTableRowsCount(getJdbcTemplate( ), "gtemp_cardId");
			logger.debug("before DELETE table gtemp_cardId contains " + counter + " record(s)");
		}

		final String sqlText = 
			"\n" +
			"DELETE \n" + 
			"FROM gtemp_cardId AS gtmp \n" +
			"WHERE NOT EXISTS ( " +
					DelegatorBean.makeSqlSelectCAWithDelegations(
						"1", "(:personId)",
						"ca.permission_type in (:permTypeList) and (ca.object_id = c.status_id)", // permission cond
						"c.card_id = gtmp.card_id",			// card cond
						"c.template_id = ca.template_id"	// template cond
					)
			+ ") \n"
			;

/*	OLD CODE:
		final String sqlText = 
				"DELETE \n" +
				"FROM gtemp_cardId gtmp \n" + 
				"WHERE NOT EXISTS (\n" + 
				"	SELECT 1 \n" +
				"	FROM card c \n" +
				"	JOIN card_access ca " +
				"		ON  ca.object_id = c.status_id \n" +
				"			AND ca.template_id = c.template_id \n" +
				"			AND ca.permission_type in (?) \n" +
				"  WHERE \n" +
				"		c.card_id = gtmp.card_id \n" +
				"		AND ( \n" +
				"			(ca.person_attribute_code IS null) \n" +
				"			OR ( (ca.person_attribute_code IS NOT null) \n" +
				"				 AND EXISTS ( \n" +
				"					SELECT null FROM attribute_value av \n" +
				"					WHERE \n" +
				"						av.attribute_code = ca.person_attribute_code \n" +
				"						AND av.number_value = (?) \n" + // (:personId)
				"						AND av.card_id = gtmp.card_id \n" +
				"				) \n" +
				"			) \n" +
				"	) \n" +
				"	AND ( \n" +
				"		(ca.role_code is null) \n" +
				"		OR ( \n" +
				"			( ca.role_code is not null) \n" +
				"			AND EXISTS ( \n" +
				"				SELECT pr.role_code FROM person_role pr \n" +
				"				WHERE \n" +
				"					pr.person_id = (?) \n" +
				"					AND pr.role_code = ca.role_code \n" +
				"					AND ( \n" +
				"						NOT EXISTS ( \n" +
				"							SELECT 1 FROM person_role_template prt \n" +
					"						WHERE prt.prole_id = pr.prole_id \n" +
				"						) OR (EXISTS ( \n" +
				"							SELECT 1 FROM person_role_template prt \n" +
				"							WHERE prt.prole_id = pr.prole_id AND \n" +
				"								  prt.template_id = c.template_id \n" +
				"						)) \n" +
				"					) \n" +
				"			) \n" +
				"		) \n" +
				"	) \n" +
				") \n";
*/
		final String sInfo = 
			"Restricting SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);
		int delCount = -1;
		try {
			// delCount = getJdbcTemplate().update( sqlText, args, argTypes);
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			delCount = jdbc.update( sqlText, 
					new MapSqlParameterSource()
							.addValue("personId", personId.getId(), Types.NUMERIC)
							.addValue("permTypeList", permission, Types.VARCHAR)
					);
		} finally {
			final String msg = sInfo + "\n FetchCards restricted cards removed number :"+ delCount;
			// System.out.println(msg);
			logger.debug(msg);
		}
	}	

	/**
	 * ����������� ��������������� �������� �� ������� ���� �����
	 * �� ��������� ������� gtemp_result_cardid
	 * @param permission
	 * @param personId
	 */
	private void copyPermittedCards( String permission, ObjectId personId) {
		/**
		 * ��� ����������� sql-���������:
		 *   1) (:permissionType)
		 *   2) (:personID)
		 *   3) (:personID)
		 */
		// ��� �����������: ( cardId, permissionType, personId, cardId )
		final Object[] args 
			= new Object[] { permission, personId.getId(), personId.getId() };
		final int[] argTypes
			= new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC };

		StringBuffer sqlBuf = new StringBuffer();
		sqlBuf.append("insert into gtemp_result_cardid \n"); 
		sqlBuf.append("select t.card_id \n");
		sqlBuf.append("from gtemp_cardId t \n");
// �������� ������ �������� �� �����
/*
			"inner join ( \n" +
			 
					DelegatorBean.makeSqlSelectCAWithDelegations(
						"card_id", "(:personId)",
						"ca.permission_type in (" + permission + ") and (ca.object_id = c.status_id)", // permission cond
						"c.card_id in (select card_id from gtemp_cardId) ",		
						"c.template_id = ca.template_id"	
					)
		sqlBuf.append(") p on (p.card_id = t.card_id)\n");
*/
		
		sqlBuf.append("		join card cc on (cc.card_id = t.card_id) \n");
		sqlBuf.append("where (1=1) \n");
		DoSearch.emmitPermissionWhere(sqlBuf, search.getFilter(), personId, "cc");

		sqlBuf.append("group by t.card_id \n");
		sqlBuf.append("order by min (t.ordnum) \n");
		final String sInfo = 
			"Insert SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlBuf.toString(), args, argTypes);
		int insCount = -1;
		try {
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			insCount = jdbc.update( sqlBuf.toString(),new MapSqlParameterSource() 
					/*new MapSqlParameterSource()
							.addValue("personId", personId.getId(), Types.NUMERIC)*/
					);
		} finally {
			final String msg = sInfo + "\n FetchCards inserted cards insert number :"+ insCount;
			logger.debug(msg);
		}
	}	

	private void copyAllCards() {
		final Object[] args 
			= new Object[] { };
		final int[] argTypes
			= new int[] {  };

		final String sqlText = 
			"\n" +
			"insert into "+ ManagerTempTables.TMPTABLEID_RESULT_CARDID + " \n" + 
			"	select t.card_id \n" +
			"	from gtemp_cardId t \n" +
			"	order by t.ordnum \n";

		final String sInfo = 
			"Insert SQL is:" + SimpleDBUtils.getSqlQueryInfo( sqlText, args, argTypes);
		int insCount = -1;
		try {
			final NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(getJdbcTemplate());
			insCount = jdbc.update( sqlText, 
					new MapSqlParameterSource());
		} finally {
			final String msg = sInfo + "\n FetchCards inserted cards insert number :"+ insCount;
			logger.debug(msg);
		}
	}	

	protected void updateOrderColumn(Search.Filter dataFilter) {
		if (dataFilter == null)
			return;

		/* OLD before (2010/08/31):		
		sql.append(tmpS=="" ? "1" : tmpS);
		sql.append(",1,15)) \n");
		final int affected = getJdbcTemplate().update(sql.toString());
		*/
		/* (2010/09/03, RuSA) OLD: ��-���� ���������� ���� ��������� ��� �������� 
		 * ������, �.�. ����� ����� ������ �� ���������!
		int affected = 0;
		if (filter.isCustomCardSortOrder()) {
			affected = getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM gtemp_cardid");
		} else {
			final StringBuffer sql = new StringBuffer();
			final String tmpS = 
				getColumSqlByAttribute(getOriginalAttribute(dataFilter.getOrderColumn().getAttributeId()),
									   getOriginalAttribute(dataFilter.getOrderColumn().getLabelAttrId()));
			sql.append("UPDATE gtemp_cardid gtemp \n" +
						"SET gtemp.ord=(substring( \n");
			sql.append( (tmpS == "") ? "1" : tmpS );
			sql.append(",1,15)) \n");

			affected = getJdbcTemplate().update(sql.toString());
		}
		 */
		// getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM gtemp_cardid");
		if (dataFilter.getWholeSize() == -1) {
			final int affected = SimpleDBUtils.sqlGetTableRowsCount( getJdbcTemplate(), ManagerTempTables.TMPTABLEID_RESULT_CARDID); 
			dataFilter.setWholeSize(affected);
		}
	}

	protected void makeWindow(Search search, Search.Filter winFilter) {
		final StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO gtemp_cardid_w (card_id) \n");
		sql.append("SELECT gtemp.card_id \n");
		sql.append("FROM gtemp_result_cardid AS gtemp \n");
		sql.append("	JOIN card c on c.card_id=gtemp.card_id \n");
		int offsetJoin = sql.length() - 1;
		sql.append("	GROUP BY gtemp.card_id \n");

		// TODO: �� ����� "MERGE FILTER CHECK"
		if (!DoSearch.emmitSortOrder(sql, search, offsetJoin, "c",
				getJdbcTemplate(), logger, DoSearch.AggregateFunction.min)) {
			// ���� ��� ����� ���������� - �������� ������� ��������� ...
			sql.append("ORDER BY min(ordNum) \n");
		}
		DoSearch.emmitPgLimit(sql, winFilter);

		final int affected = getJdbcTemplate().update(sql.toString());
		logger.debug("Inserted records into gtemp_cardid_w: " + affected);
	}
}