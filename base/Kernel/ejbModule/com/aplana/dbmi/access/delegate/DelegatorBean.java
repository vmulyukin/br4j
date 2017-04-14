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
/**
 * 
 */
package com.aplana.dbmi.access.delegate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PermissionDelegate;
import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.util.CacheEx;
import com.aplana.util.CacheableBase;

/**
 * @author RAbdullin
 *
 */
public class DelegatorBean 
		// extends JndiObjectLocator
		extends JdbcDaoSupport
		implements DelegateManager, DatabaseClient // , BeanFactoryAware 
{
	final protected Log logger = super.logger;
	// private JdbcTemplate jdbc;
	private Database database;
	// private BeanFactory factory;


	public DelegatorBean() {
		super();
	}

	/*
	// interface DatabaseClient
	public void setJdbcTemplate(JdbcTemplate jdbcTmpl) {
		this.jdbc = jdbcTmpl;
	}

	protected JdbcTemplate getJdbcTemplate() {
		return this.jdbc;
	}
	*/

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		//	return (Database) factory.getBean(DataServiceBean.BEAN_DATABASE);
		this.database = database;
	}


//	interface BeanFactoryAware
//	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
//		this.factory = beanFactory;
//	}

	/**
	 * ��������� ������ ���������, � ������� �������� �������� ����.
	 * @param idFieldName: ��� ����, ��� �������� ������ �������� id, ������ ���
	 * "delegation_id", "src_person_id" ��� "delegate_person_id".
	 * @param id: �������������.
	 * @param jdbc
	 * @return ����� PermissionDelegate[].
	 */
	static Set /*<PermissionDelegate>*/ queryDelegatesByField( 
			String idFieldName, ObjectId id, JdbcTemplate jdbc)
	{
		if (jdbc == null || id == null) 
			return null;

		Validate.notEmpty(idFieldName, "Invalid field name in loading delegate");

		final String sqlText = 
					PermissionDelegateMapper.QUERY_DELEGATES +
					"WHERE "+ idFieldName + " = (?) \n"
		;
		final List /*PermissionDelegate */ loadList = jdbc.query(
				sqlText,
				new Object[] { id.getId() },
				new int[] { Types.NUMERIC },
				new PermissionDelegateMapper( jdbc ) 
		);

		return new HashSet /*<PermissionDelegate>*/ ( loadList);
	}


	/* (non-Javadoc)
	 * @see com.aplana.dbmi.access.delegate.DelegateManager#getDelegatesFromPerson(com.aplana.dbmi.model.ObjectId)
	 */
	public Set getDelegatesFromPerson(ObjectId personId) throws DataException {
		if (personId == null)
			return null;

		return queryDelegatesByField( "src_person_id", personId, getJdbcTemplate()); 
	}


	/* (non-Javadoc)
	 * @see com.aplana.dbmi.access.delegate.DelegateManager#getDelegatesToPerson(com.aplana.dbmi.model.ObjectId)
	 */
	public Set getDelegatesToPerson(ObjectId personId) throws DataException {
		if (personId == null)
			return null;
		return queryDelegatesByField( "delegate_person_id", personId, getJdbcTemplate());
	}


	/* (non-Javadoc)
	 * @see com.aplana.dbmi.access.delegate.DelegateManager#getDelegatedPersons(com.aplana.dbmi.model.ObjectId, java.util.Set)
	 */
	public Set getDelegatedPersons(ObjectId bossId, Set permissions)
			throws DataException {
		if (bossId  == null)
			return null;

		final Set /*<PersonId>*/ result = new HashSet(5);

		scandDelegatesWithPermissions(
					this.getDelegatesFromPerson( bossId), 
					permissions, 
					new ProcessPermissionDelegate(){
						public void process(PermissionDelegate delegate) {
							result.add(delegate.getToPersonId());
					}}
				);

		return result;
	}


	/**
	 * ��� ����������� �������� �������������� ���-���� ��� ���������� ������.
	 * @author RAbdullin
	 */
	private class CachEntryForBossList extends CacheableBase {

		/**
		 * ����� ���������� ���������� ������.
		 */
		int time_ms_list_expire = (1000* 60)/*min*/ * 1 / 2;		// 30 sec

		Set /*PersonId*/ bosses;

		CachEntryForBossList( ObjectId personId, Set /*PersonId*/ newList) 
		{
			this( personId );
			this.setBosses( newList);
		}

		public CachEntryForBossList(ObjectId personId) {
			super( personId );
		}

		public Object getValue() throws Exception {
			return this.bosses;
		}

		void setBosses(Set newList)
		{
			this.bosses = newList;
			this.setExpiration(time_ms_list_expire);
		}
	}


	private final static CacheEx bossCash = new CacheEx(50);

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.access.delegate.DelegateManager#getPersonsCanDoAs(com.aplana.dbmi.model.ObjectId, java.util.Set)
	 */
	public Set getPersonsCanDoAs(ObjectId personId, Set permissions)
			throws DataException 
	{
		if (personId == null)
			return null;

		try {
			final CachEntryForBossList entry = new CachEntryForBossList( personId);
			final boolean cachable = permissions == null; 

			if (cachable && bossCash.contains(entry)) // ��� ���� � ���� ...
				return (Set) bossCash.get(entry);

			// �������� �� ��...
			final Set /*<PersonId>*/ result = new HashSet();
			final Date _now = new Date();

			scandDelegatesWithPermissions(
					this.getDelegatesToPerson( personId), 
					permissions, 
					new ProcessPermissionDelegate(){
						public void process(PermissionDelegate delegate) {
							if (delegate != null && delegate.isActiveAt(_now))
								result.add(delegate.getFromPersonId());
					}}
				);

			// �������� ...
			if (cachable) {
				entry.setBosses(result);
				bossCash.put(entry);
			}

			return result;
		} catch (Exception ex) {
			throw new DataException(ex);
		}
	}


	/* (non-Javadoc)
	 * @see com.aplana.dbmi.access.delegate.DelegateManager#getAllPermissionSets()
	 */
	public Set getAllPermissionSets() throws DataException {
		return getPermissionSetQueryAll(getJdbcTemplate()).loadAllPermissionSets();
	}


	/***************************************************************************
	 * STATIC 
	 */

	/**
	 * ����������� �������� � ���������� ��, ������� ����� ����� �� ��� �� 
	 * ���������� ������.
	 * @param delegates: ����� ����������.
	 * @param permissions: ������ ������ ���� (CardAccess[]), ���� null, �� 
	 * �������� ��� �� ������ delegates.
	 * @param processor: ����������� ����������, ���� null, �� ������ ����� 
	 * ��������� ���-�� ���������� ���������.
	 * @return ���������� ���������� ���������.
	 */
	static int scandDelegatesWithPermissions(
			Set /*<PermissionDelegate>*/ delegates,
			Set /*<CardAccess>*/ permissions, 
			ProcessPermissionDelegate processor) 
	{
		if (delegates == null || delegates.isEmpty())
			return 0;

		int result = 0;

		// TODO: �������� ���� ����� ������� ��������� ��� ������� ������������
		// toPersonId ����������� ���� ������ ��� ���� � ������ ����� ��������
		// ��������� permissions � ���.
		for (Iterator iterator = delegates.iterator(); iterator.hasNext();) 
		{
			final PermissionDelegate delegate = (PermissionDelegate) iterator.next();
			final PermissionSet permSet = delegate.getPermissions();
			if ( 	(permissions == null)
					||
					( 	(permSet != null) && (permSet.getSet() != null) 
						&& permSet.getSet().containsAll( permissions)
					)
				)
			{	// ����� ��������� ���� ������ ������� ��������������� ���������
				++result;
				if (processor != null)
					processor.process(delegate);
			} 
		}
		return result;
	} 


	final static QueryPermissionSet getPermissionSetQuery(JdbcTemplate jdbc)
	{
		if (jdbc == null) return null;

		final QueryPermissionSet query = new QueryPermissionSet();
		// query.setBeanFactory(this.getBeanFactory());
		// query.setUser(this.getUser());
		query.setJdbcTemplate(jdbc);

		return query;
	}


	final static LoadAllPermissionSets getPermissionSetQueryAll(JdbcTemplate jdbc)
	{
		if (jdbc == null) return null;

		final LoadAllPermissionSets query = new LoadAllPermissionSets();
		// query.setBeanFactory(this.getBeanFactory());
		// query.setUser(this.getUser());
		query.setJdbcTemplate(jdbc);

		return query;
	}


	/**
	 * @author RAbdullin
	 * Callback ��� ������������ ������� ���������.
	 */
	interface ProcessPermissionDelegate
	{
		void process( PermissionDelegate delegate);
	}

	////////////////////////////////////////////////////////////////////////////
	// SUPPORT
	/**
	 * This mapper create PermissionDelegate using such fields' names:
	 *   1) "delegation_id"
	 *   2) "src_person_id"
	 *   3) "delegate_person_id"
	 *   4) "start_at"
	 *   5) "end_at"
	 *   6) "delegation_role_id"
	 *   7) "is_src_has_access_too"
	 */
	public static class PermissionDelegateMapper implements RowMapper 
	{
		final JdbcTemplate jdbc;
		static final String QUERY_DELEGATES =
				"SELECT  \n" +
				"\t   delegation_id  \n" +		// 1
				"\t , src_person_id \n" +		// 2
				"\t , delegate_person_id \n" +	// 3

				"\t , start_at \n" +			// 4
				"\t , end_at \n" +				// 5
				
				"\t , created_at \n"+			// 6
				"\t , creator_person_id \n"+	// 7
				//"\t , delegation_role_id \n" +	// 

				//"\t , is_src_has_access_too \n"+// 
				//"\t , is_active \n"+			// 

				"FROM delegation \n"
			;


		/**
		 * ������ ����� ������ ������ ������ ResultSet.
		 */
		public int FLD_DELEGATION_ID         = 1;
		public int FLD_SRC_PERSON_ID         = 2;
		public int FLD_DELEGATE_PERSON_ID    = 3;

		public int FLD_START_AT              = 4;
		public int FLD_END_AT                = 5;
		
		public int FLD_CREATED_AT			= 6;
		public int FLD_CREATOR_ID			= 7;
		//public int FLD_DELEGATION_ROLE_ID    = 6;

		//public int FLD_IS_SRC_HAS_ACCESS_TOO = 7;
		//public int FLD_IS_ACTIVE             = 8;


		public PermissionDelegateMapper( JdbcTemplate jdbc) {
			Validate.notNull( jdbc, "Sql connection must be not null" );
			this.jdbc = jdbc;
		}


		public Object mapRow(ResultSet rs, int rowNum) 
			throws SQLException 
		{
			final Delegation resultDelegate = new Delegation();

			long delegateId = rs.getLong( FLD_DELEGATION_ID);
			resultDelegate.setId( delegateId);

			resultDelegate.setFromPersonId( rs.getLong( FLD_SRC_PERSON_ID));
			resultDelegate.setToPersonId( rs.getLong( FLD_DELEGATE_PERSON_ID));
			// resultDelegate.setFromPersonHasAccessToo( rs.getBoolean( FLD_IS_SRC_HAS_ACCESS_TOO));
			/*resultDelegate.setFromPersonHasAccessToo( 
					rs.getInt( FLD_IS_SRC_HAS_ACCESS_TOO) == 1
			);
			resultDelegate.setActive( rs.getInt( FLD_IS_ACTIVE) == 1);*/
			
			resultDelegate.setCreatedAt(rs.getDate(FLD_CREATED_AT));
			resultDelegate.setCreatorPersonId(rs.getLong(FLD_CREATOR_ID));

			final Date atStart = rs.getDate( FLD_START_AT);
			final Date atEnd = rs.getDate( FLD_END_AT);
			try {
				resultDelegate.setPeriod( atStart, atEnd);
			} catch (DataException ex) {
				ex.printStackTrace();
				final SQLException sqlWrapper =
					new SQLException("Delegation (id='"+ delegateId 
							+"') invalid date startAt/endAt pair values: start='"
							+ atStart+ "', end='"
							+ atEnd+ "' ."
						);
				sqlWrapper.initCause(ex);
				throw sqlWrapper;
			}

			// (!) �������� ������ ���� ...
			//final long psetID = rs.getLong( FLD_DELEGATION_ROLE_ID);
			/*try { 
				final PermissionSet permSet = loadPermissionSet( psetID);
				resultDelegate.setPermissions( permSet);
			} catch (DataException ex) {
				ex.printStackTrace();
				// resultDelegate.setPermissions( null);
				final SQLException sqlWrapper =
					new SQLException("Delegation (id='"+ delegateId 
						+"')  permission set (id='"+ psetID
						+"') load problem");
				sqlWrapper.initCause(ex);
				throw sqlWrapper;
			}*/

			return resultDelegate;
		}


		/**
		 * ��������� �� id �� �� ����� ����������.
		 * @param psetId	����������� id.
		 * @return ������ ����������
		 * @throws DataException
		 */
		public PermissionSet loadPermissionSet( final long psetId ) 
			throws DataException
		{
			// getQueryFactory().getFetchQuery(PermissionSet.class);
			return getPermissionSetQuery(jdbc).loadPermissionSet(psetId);
		}


	}


	/**
	 * @author RAbdullin
	 *    1) premission_set::delegation_role_id
	 *    2) premission_set::name_rus 
	 *    3) premission_set::name_eng
	 *    4) card_access::rec_id
	 *    5) card_access::permission_type
	 *    6) card_access::object_id
	 *    7) card_access.template_id
	 */
	public static class PermissionSetMapper implements RowMapper 
	{
		/**
		 * ������ SELECT-������� (��� WHERE �����) ��� ��������� ������� (�����)
		 * �������������.
		 */
		public final static String SQL_QUERY_PermissionSet = 
				"SELECT  \n" +
				"\t   drole.delegation_role_id \n" +	// 1
				"\t , drole.name_rus \n" +				// 2
				"\t , drole.name_eng \n" +				// 3

				"\t , ca.rec_id \n" +					// 4 
				"\t , ca.permission_type \n" +			// 5 
				"\t , ca.object_id \n" +				// 6

				"\t , ca.template_id \n" +				// 7

				"FROM Delegation_Role drole \n" +
				"\t\t LEFT OUTER JOIN Permission perm on perm.delegation_role_id = drole.delegation_role_id \n" +
				"\t\t LEFT OUTER JOIN card_access ca on ca.rec_id = perm.card_access_id \n"
			;

//		final PermissionSet resultSet;
//		final long loadId;
//		public PermissionSetMapper(PermissionSet destinationSet, long loadId) {
//			Validate.notNull( destinationSet, "PermissionSet must be not null" );
//			this.resultSet = destinationSet;
//			this.loadId = loadId;
//		}

//		public PermissionSetMapper(long loadId) {
//			this( new PermissionSet(), loadId);
//		}

		final private Map /*<Long, PermissionSet>*/ setById = new HashMap();
		/**
		 * ����������� ������: key=LongId.
		 */
		public Map /*<Long, PermissionSet>*/ getMapPermissionsSet()
		{
			return this.setById;
		}

		public Object mapRow(ResultSet rs, int rowNum) 
			throws SQLException 
		{
			final Long loadingId=  new Long( rs.getLong(1));

			PermissionSet resultSet = (PermissionSet) setById.get( loadingId);
			if (resultSet == null) {
				// ��� ���� ������...
				resultSet = new PermissionSet();
				resultSet.setId( loadingId.longValue() );
				this.setById.put( loadingId, resultSet);
			} else // �������� id �� ������ ������... 
				if (resultSet.getId() == null || !resultSet.getId().getId().equals(loadingId))
					throw new SQLException("Internal error: inconsequent PermSet id of loaded and loading set");

			// ���� � ���������� ��� ��� ���-����� - ������ ���...
			if (resultSet.getName().getValue() == null) {
				resultSet.setNameLacales( rs.getString(2), rs.getString(3));
			}

			if (rs.getObject(4) != null) { // exists permission
				final CardAccess perm = new CardAccess();
				perm.setObjectId( new ObjectId( CardAccess.class, rs.getLong(4)));
				perm.setTemplateId( new ObjectId(Template.class, rs.getLong(7)) );

				try {
					setPermissionAndObjectId( perm, rs.getLong(5), rs.getLong(6));
				} catch (DataException ex) {
					ex.printStackTrace();
					final SQLException sqlEx = new SQLException( "PermissionSet mapping failed");
					sqlEx.initCause(ex);
					throw sqlEx;
				}

				resultSet.addItem( perm);
			}

			return resultSet;
		}
	}


	/**
	 * ���������� ����� ���� �������� - �������� � ������� ������ (������, 
	 * ������ ��� �������).
	 * @param permissionType
	 * @param objID
	 * @throws DataException 
	 */
	static void setPermissionAndObjectId( CardAccess ca, long permissionType, long objID ) 
		throws DataException
	{
		ca.setPermissionType( new Long(permissionType) );
		ca.setObjectId( makeObjectId(objID, permissionType));
	}


	/**
	 * �������� id ������� � ��� ���, �������� ��������� permissionType:
	 * 		WORKFLOW_MOVE: ������ id ��� ������ {@link WorkflowMove},
	 * 		READ_CARD or EDIT_CARD: ��� ������ {@link CardState},
	 * 		CREATE_CARD: ��� {@link Template},
	 * 		������ �������� �����������.
	 * @param objID 
	 * @throws DataException
	 */
	static ObjectId makeObjectId(long objID, long permissionType) 
		throws DataException 
	{
		ObjectId newId = null;

//			case PERMIT_NONE:
//				// ��� ��������� ���������� ������ null-������ 
//				if (id != 0)
//					throw new DataException( "store.cardaccess.wrong.permissiontype", 
//							new Object[] { "" + permissionType + " , objId<>0" } );
//				break;

		if (permissionType == CardAccess.WORKFLOW_MOVE.longValue()) {
			newId = new ObjectId( WorkflowMove.class, objID);
		} else if (permissionType == CardAccess.READ_CARD.longValue()
					|| permissionType == CardAccess.EDIT_CARD.longValue()
				) {
			newId = new ObjectId( CardState.class, objID);
		} else if (permissionType == CardAccess.CREATE_CARD.longValue()) {
			newId = new ObjectId( Template.class, objID);
		} else
			throw new DataException( "store.cardaccess.wrong.permissiontype", 
						new Object[] { new Long(permissionType) } );
		return newId;
	}

	/** �������� ������ ���� (������ card_access) � �������� � ������ 
	 * ������������� ��������� ������� (person), ��� ���������� ��������(��) 
	 * (permissonType) � ���������(���) (card) �/��� �������� (���) (template).
	 * ������: 
	 * 		makeSqlSelectCAWithDelegations( "ca.*, c.card_id", 
	 * 			personId.getId().toString(),
	 * 			"ca.permission_type = 2",
	 * 			"c.card_id=gtmp.card_id",  // ��� c.card_id=(:cardId)
	 * 			null // no template cond
	 * 		);
	 * 
	 * @param inlineResultFields: ������ �������� �����, �������� "*" ��� "1",
	 * �������� ��� �������� ����������� JOIN ������: ca=card_access, c=card,
	 * (!) ��������� ������� ������������ �����������.
	 * @param inlinePersonList: ���� �������� ��� ������ �������� ��� id-������.
	 * @param inlinePermCond: �������� ����, ��������, 
	 * 		"ca.permission_type = (:permType)", 
	 * 		��� "ca.permission_type = ?", 
	 * 		��� "ca.permission_type in (2,3)".
	 * @param inlineCardCond: ������� ��� ������ ��������, 
	 * 		��������, "c.card_id=(:cardId)".
	 * @param inlineTemplateCond: ������� ��� ������ ��������,
	 * 		��������, "c.template_id = (:templateId)".
	 * @return ������� sql-���������.
	 */
	public static String makeSqlSelectCAWithDelegations(
			String inlineResultFields,
			String inlinePersonList,

			String inlinePermissionCond, 
			String inlineCardCond, 
			String inlineTemplateCond 
			)
	{
		return makeSqlSelectCAWithDelegationsEx( inlineResultFields, null, 
				inlinePersonList, inlinePermissionCond, inlineCardCond, 
				inlineTemplateCond); 
	}

	public static String makeSqlSelectCAWithDelegationsEx(
			String inlineResultFields,
			String inlineJoin,
			String inlinePersonList,

			String inlinePermissionCond, 
			String inlineCardCond, 
			String inlineTemplateCond 
			) 
	{
		final StringBuffer sqlBuf = new StringBuffer(); 
		sqlBuf.append( "\n		SELECT "+ (inlineResultFields != null ? inlineResultFields : "*") +" \n");
		sqlBuf.append( "		FROM card_access ca \n"); 

		final boolean useCardJoin = (inlineCardCond != null) 
					&& (inlineCardCond.trim().length() > 0);
		if (useCardJoin) {
			// (!) (bug fix, MLarin, RuSA) ������� "JOIN", ��� �������� ����������� 
			// �������� ��������, ��� ���������� ���� �� ����� �������� �������� 
			// �������, ������ �� �������� ��������� �� ��� ����� �����������.
			// �� ����� ���������� JOIN : ���� ��� ������� �� �������� 
			// ��������, �� ������� card �� ������������ � JOIN �� �����. 
			sqlBuf.append( "			JOIN card AS c \n"); 
			sqlBuf.append( "				ON c.template_id = ca.template_id \n");
		}
		/* (2010/12/08, RusA) �������� � ������ Exists
		sqlBuf.append( "			LEFT JOIN attribute_value avUsr \n");  
		sqlBuf.append( "				ON avUsr.card_id = c.card_id \n"); 
		sqlBuf.append( "				AND avUsr.attribute_code = ca.person_attribute_code \n"); 
		sqlBuf.append( "				AND avUsr.number_value in ("+ inlinePersonList + ") \n" );
		 */ 
		//sqlBuf.append( "			LEFT OUTER JOIN delegation AS dlg \n" );
		//sqlBuf.append( "				ON dlg.delegation_role_id = p.delegation_role_id \n" );
		//sqlBuf.append( "				AND dlg.delegate_person_id in ("+ inlinePersonList + ") \n" );

		// -- ���������� ��� ������ ����������...
		sqlBuf.append( emit( "	",  inlineJoin ) ); // JOIN

		sqlBuf.append( "WHERE (1=1) \n"); 

		sqlBuf.append( emit( "		AND", inlinePermissionCond)  + " \n" );

		// "  AND ca.object_id = c.status_id \n"   ��� CardRW-checker
		// "  AND ca.object_id = (:objectID) \n"   ��� WFM-checker (object checker)
		// sqlBuf.append( "			AND (ca.object_id = c.status_id) -- :status_id \n");
		sqlBuf.append( emit( "		AND", inlineCardCond) + " \n" );

		// AND c.template_id = (:templateId) 	--< ��� ���������-�������
		// sqlBuf.append( "			AND (ca.template_id = c.template_id) -- :template_id \n");
		sqlBuf.append( emit( "		AND", inlineTemplateCond)  + " \n" );

		sqlBuf.append( "		AND ( \n");
		sqlBuf.append( "			-- all_user_permissions \n");
		sqlBuf.append( "			(ca.role_code IS NULL AND ca.person_attribute_code IS NULL) \n");
		sqlBuf.append( "			-- ���� � ����������� � �������� ... \n");
		sqlBuf.append( "			OR ( \n");
		sqlBuf.append( "				-- ���� ������������ ... \n");
		sqlBuf.append( "				( \n");
		sqlBuf.append( "					ca.role_code is NULL \n");
		
// � ������ ������ BR4J00036917 ������� ����������� ����-������		
		
//		sqlBuf.append( "					OR EXISTS( \n");
//		sqlBuf.append( "							select 1 \n");
//		sqlBuf.append( "							from person_role pr left \n" );
//		sqlBuf.append( "								join person_role_template prt \n"); 
//		sqlBuf.append( "									on pr.prole_id = prt.prole_id \n");
//		sqlBuf.append( "							where  \n");
//		sqlBuf.append( "								COALESCE(prt.template_id, ca.template_id) = ca.template_id \n"); 
//		sqlBuf.append( "								and pr.role_code = ca.role_code \n");
//		sqlBuf.append( "								and pr.person_id in ("+ inlinePersonList+ ") \n");
//		sqlBuf.append( "					) -- OR EXISTS \n");
		sqlBuf.append( "				) --  \n");

		// ������� � �������� ����������� ������ ���� ���� ������� �� ��������...
		if (useCardJoin) {
			sqlBuf.append( "				-- ������������ ������� � �������� ... \n");
			sqlBuf.append( "				AND ( \n");
			sqlBuf.append( "					ca.person_attribute_code IS NULL \n"); 
			sqlBuf.append( "					-- ��� ������������ �������� � ����-�� �������� �������� \n");
			sqlBuf.append( "					OR ( 	(ca.person_attribute_code IS NOT NULL) \n");
			// sqlBuf.append( "							AND (avUsr.attribute_code IS NOT NULL) \n");
			sqlBuf.append( "							AND EXISTS( \n");
			sqlBuf.append( "									SELECT 1 \n");
			sqlBuf.append( "									FROM attribute_value avUsr \n" );
			sqlBuf.append( "									WHERE avUsr.card_id = c.card_id \n"); 
			sqlBuf.append( "										AND avUsr.attribute_code = ca.person_attribute_code \n"); 
			sqlBuf.append( "										AND avUsr.number_value in ("+ inlinePersonList + ") \n" );
			sqlBuf.append( "							) -- AND EXISTS \n" );
			sqlBuf.append( " 					) -- OR \n" );
			sqlBuf.append( "				) -- AND \n");
		}
		sqlBuf.append( "			) -- OR \n");
		sqlBuf.append( "		) -- / AND (*) \n");

		/* (2010/12/07, RuSA) �������� ����
		String sqlText = 
			"\n" +
			"SELECT "+ (inlineResultFields != null ? inlineResultFields : "*") +" \n" +
			"FROM card_access AS ca \n" + 
			"	LEFT JOIN card AS c \n" + 
			"			on c.template_id = ca.template_id \n" + 
			"	LEFT OUTER JOIN permission AS p \n" +
			"			on p.card_access_id = ca.rec_id\n" +
			"	LEFT OUTER JOIN delegation AS dlg \n" +
			"			on dlg.delegation_role_id = p.delegation_role_id \n" +
			"			AND dlg.delegate_person_id in ("+ inlinePersonList + ") \n" +

			// -- ���������� ��� ������ ����������...
			emit( "           ",  inlineJoin ) + // JOIN

			"\n WHERE (1=1) \n" + 

			emit( "       AND", inlinePermissionCond)  + " \n" +

			// AND c.card_id = (:cardId)  			--< ��� �������� 

			// "  AND ca.object_id = c.status_id \n"   ��� CardRW-checker
			// "  AND ca.object_id = (:objectID) \n"   ��� WFM-checker (object checker)
			emit( "       AND", inlineCardCond) + " \n" +

			// AND c.template_id = (:templateId) 	--< ��� ���������-�������
			emit( "       AND", inlineTemplateCond)  + " \n" +

			"  AND (\n" + // >>> AND(1)
			"		(ca.role_code IS NULL AND ca.person_attribute_code IS NULL) \n"+ // all_user_permissions 
			// -- ���� ������ ������������ ���� ...
			"		OR ( (ca.role_code IS NULL) \n" +
			"			OR ( \n" +
			"        (ca.role_code IS NOT NULL) \n" +
			"        AND Exists( \n" + 					// ��� \"role_code in (\"\n" + 
			"          SELECT pr.role_code \n" + 
			"          FROM  person_role AS pr \n" + 
			"                LEFT OUTER JOIN person_role_template AS prt \n" + 
			"                   ON pr.prole_id = prt.prole_id \n" + 
			"          WHERE \n" + 
			"               COALESCE( prt.template_id, ca.template_id) = ca.template_id \n" + 
			"               AND pr.role_code = ca.role_code \n" + 
			"               AND pr.person_id in ("+ inlinePersonList + ") \n" + // (:personId) 
			"        )\n" + // AND EXISTS()
			"      )\n" + // OR()
			"    )\n" +

			// -- ����� ������ � �������� �� ������ �������� (�����, ������������� � ��) ... 
			"\n" + 
			"    AND (\n" + // >>> AND(1b)
			"        ca.person_attribute_code IS NULL \n" + 
			"      OR EXISTS ( \n" + 
			"        SELECT NULL \n" + 
			"        FROM attribute_value AS av \n" + 
			"        WHERE av.attribute_code = ca.person_attribute_code \n" + 
			"              AND av.card_id = c.card_id \n" + 
			"              AND av.number_value in ("+ inlinePersonList + ") \n" + // (:personId)  
			"      )\n" + 
			"    )\n" +// AND(1b) <<<

			"  )\n"  // AND(1) <<< 
			;
		*/
// TODO: (NATIVE_DELEGATE) ������� �������� ������������� 
//			+ "\n OR ( \n" + // >>> OR(1)
//
//			//     -- (!) �����, ���������� ����� ������������� ... 
//			//     -- ������������ ����� ������������ ������ �� ����������, 
//			//     -- ������� ����� ����� (�������� �� ��������� �����)
//
//			//       -- ��� ���������� �� �������������...
//			"        (p.card_access_id IS NOT NULL) \n"+
//			"        AND (dlg.delegation_role_id IS NOT NULL) \n"+
//
//			//       -- ��� �������� �������������...
//			"        AND (dlg.is_active = 1) \n" + 
//
//			//	     -- ���������� �� ������ ������ (!) TIME ZONE problem may occure
//			"        AND ( \n" +
//			"           ( (dlg.start_at is NULL) or dlg.start_at <= now()) \n "+
//			"           AND \n" +
//			"           ( (dlg.end_at is NULL) or dlg.end_at >= now() ) \n" +
//			"        ) \n" +
//
//			"\n" + 
//
//			//       -- ��������� ���� ����� ... 
//			"      AND Exists( \n" + // >>> AND(2) 
//			"        SELECT pr.role_code \n" + 
//			"        FROM  person_role AS pr \n" + 
//			"              LEFT OUTER JOIN person_role_template AS prt \n" + 
//			"                 ON pr.prole_id = prt.prole_id \n" + 
//			"        WHERE pr.role_code = ca.role_code \n" + 
//			"              AND coalesce(prt.template_id, ca.template_id) = ca.template_id \n" + 
//			"              AND pr.person_id = dlg.src_person_id \n" + // (!) BOSS (:personId) 
//			"      )\n" + // AND(2) Exists(...) <<< 
//
//			"\n" +
//
//			//      -- "����" (������ ����������) ��� ����������� � ������ ������...
//			"      AND (\n" + // >>> AND(3)
//			"        ca.person_attribute_code is null \n" + 
//			"        OR EXISTS ( \n" + // >>> AND(4)
//			"          SELECT NULL \n" + 
//			"          FROM attribute_value AS av \n" + 
//			"          WHERE \n" + 
//			"             av.attribute_code = ca.person_attribute_code \n" + 
//			"             AND av.card_id = c.card_id \n" + 
//			"             AND av.number_value = dlg.src_person_id \n" + // (!) BOSS (:personId)  
//			"        )\n" + // AND(4) Exists(...) <<< 
//			"      )\n" +// AND(3) <<<
//			"\n" + 
//			"  )\n" // <<< OR(1)
//
//			// -- � �� ������������� "��������������" ��������������
//			+ "\n" + 
//			"    AND NOT exists ( \n"+ // >>> AND(5)
//			"       SELECT perm.card_access_id \n" + 
//			"       FROM delegation AS dlg \n" +
//			"            JOIN permission AS perm \n" + 
//			"                 ON perm.delegation_role_id = dlg.delegation_role_id \n" + 
//			"       WHERE \n" +
//			"              ca.rec_id = perm.card_access_id \n" +
//			//  --     ��� ����� �������� ����-���� ���...
//			"          AND dlg.src_person_id in ("+ inlinePersonList + ") \n" + // (:personId)
//			//  -- ������ �������� ��������
//			"          AND (dlg.is_active = 1) \n" + 
//			//     -- �������������� �����
//			"          AND (dlg.is_src_has_access_too = 0) \n" + 
//			//	   -- ���������� �� ������ ������
//			"          AND ( \n" +
//			"             ( (dlg.start_at is NULL) or dlg.start_at <= now()) \n "+
//			"             AND \n" +
//			"             ( (dlg.end_at is NULL) or dlg.end_at >= now() ) \n" +
//			"          ) \n" +
//			"    ) \n"// AND(5) <<<
// <<< �������� �������������
			;

		return sqlBuf.toString();
	}


	/**
	 * �������� ���-������� ���� oper(condition), ���� ������� �� ������.
	 * ������: emit( "AND", "ca.permissionType=2");
	 * @param oper
	 * @param condition
	 * @return ���-������� ���� oper(condition), ���� ������� �� ������, ��� 
	 * ����� �����.
	 */
	final static String emit( String oper, String condition)
	{
		if (condition== null || condition.equals(""))
			return "";
		return oper + " "+ condition + " \n";
	}

}
