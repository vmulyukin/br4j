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
package com.aplana.dbmi.access.delegate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PermissionDelegate;
import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;


/**
 * Query used to save {@link PermissionDelegate} instances.
 * @author RAbdullin
 */
public class SavePermissionDelegate extends SaveQueryBase {

	Delegation getSavingDelegate()
	{
		return (Delegation) super.getObject();
	}


	public void validate() throws DataException 
	{
		final Delegation theDelegate = getSavingDelegate();
		if (theDelegate == null)
			throw new DataException("factory.store", 
					new Object[] { PermissionDelegate.class.getName() + "(NULL)"} );

		// ���� theDelegate.getId() == null, �� ��� �����,
		// (!) ����� id ������ �������. 
		if (theDelegate.getId() != null && theDelegate.getId().getId() == null)
			throw new DataException( "delegate.permissions.id.notset1", new Object[] {"Delegate"});

		/*final ObjectId permId = theDelegate.getPermissonSetId();
		if ( permId== null || permId.getId() == null)
			throw new DataException( "delegate.permissions.id.notset1", new Object[] {"SetId"});
        */
		//if (theDelegate.getPermissions() == null) ...

		// ���� �������� ������������ = null -> ��������� ��� ������� ��������
		if (theDelegate.getFromPersonId() == null) {
			if (		this.getUser() != null 
					&& 	this.getUser().getPerson() != null
					)
				theDelegate.setFromPersonId( this.getUser().getPerson().getId());
		}
		if (theDelegate.getFromPersonId() == null)
			throw new DataException( "delegate.permissions.id.notset1", new Object[] {"UserFromId"});

		// ������� ������������ ������ ���� ���� �����...
		if (theDelegate.getToPersonId() == null)
			throw new DataException( "delegate.permissions.id.notset1", new Object[] {"UserToId"});

		// ��������� ������������ ������������� ����...
		// 
		/*if (!this.canActivateDelegate(theDelegate))
			throw new DataException("delegate.already.delegated");
			*/
		super.validate();
		// ... any other data is acceptable
	}


	void savePermissionSet( final PermissionDelegate delegate) 
		throws DataException
	{
		if (delegate == null) 
			return;

		// ���������� ������ ���������� delegate.getPermissions() ...
		final SavePermissionSet saveSet = new SavePermissionSet();
		saveSet.setJdbcTemplate( this.getJdbcTemplate());
		saveSet.setObject( delegate.getPermissions());
		final ObjectId permSetId = (ObjectId) getDatabase().executeQuery( this.getUser(), saveSet);

		// ������ ����� ������...
		final QueryPermissionSet readQuery = new QueryPermissionSet();
		readQuery.setId(permSetId);
		final PermissionSet readSet = (PermissionSet) getDatabase().executeQuery( this.getUser(), readQuery);

		delegate.setPermissions(readSet);
	}

	protected ObjectId processNew() throws DataException 
	{
		logger.debug("Saving new delegate ...");
		final Delegation theDelegate = getSavingDelegate();

		// TODO: (?) ���������� ������ ����������...
		// savePermissionSet( theDelegate);

		// generation new id...
		final long newId = generateId("seq_delegation_id");
		theDelegate.setId( newId);


		// (!) Adding...
		final String sqlText =
				"INSERT INTO delegation \n"+
				"	( \n"+ 
				"		  delegation_id \n"+ 		// 1 
				"		, src_person_id \n"+ 		// 2 
				"		, delegate_person_id \n"+ 	// 3"

				"		, start_at \n"+ 			// 4
				"		, end_at \n"+ 				// 5
				
				"		,created_at \n"+			// 6
				"		,creator_person_id \n"+		// 7
				//"		, delegation_role_id \n"+ 	// 

				//"		, is_src_has_access_too \n"+// 
				//"		, is_active \n"+			// 

				"	) VALUES ( ?, ?, ?, ?, ? ,? ,? ) \n"
				;
		final Object[] args= 
				new Object[] { 
						safeGetId( theDelegate.getId()),					// 1
						safeGetId( theDelegate.getFromPersonId()), 			// 2
						safeGetId( theDelegate.getToPersonId()), 			// 3

						theDelegate.getStartAt(),							// 4
						theDelegate.getEndAt(),								// 5
						
						theDelegate.getCreatedAt(),							// 6
						safeGetId( theDelegate.getCreatorId())							// 7
						//safeGetId( theDelegate.getPermissonSetId()),		// 

						//bool2sql( theDelegate.isFromPersonHasAccessToo()),	// 
						//bool2sql( theDelegate.isActive()) 					// 
					};
		final int[] types = 
				new int[] { 
						Types.NUMERIC, 		// 1
						Types.NUMERIC, 		// 2 
						Types.NUMERIC, 		// 3

						Types.TIMESTAMP, 	// 4
						Types.TIMESTAMP, 	// 5
						
						Types.TIMESTAMP,	// 6
						Types.NUMERIC		// 7
						//Types.NUMERIC, 		// 

						//Types.NUMERIC,		// 
						//Types.NUMERIC		// 
					}
				;
		logger.debug( "Add delegate SQL is " 
				+ SimpleDBUtils.getSqlQueryInfo( sqlText, args, types)
		);
		getJdbcTemplate().update( sqlText, args, types);

		logger.info( MessageFormat.format(
				"SUCCESSFULLY added delegate {0} \n", 
				new Object[] { theDelegate}
			));

		return theDelegate.getId();
	}


	/**
	 * �������� ������� �� ����, ������� null �� ������� �������.
	 * @param fromPersonId
	 * @return
	 */
//	private Long getFromPersonId( ObjectId fromPersonId)
//	{
//		if (fromPersonId == null || fromPersonId.getId() == null)
//		{	// �� ������� �� ���� -> ������ �� �������� ������������...
//			if (		this.getUser() != null 
//					&& 	this.getUser().getPerson() != null
//					)
//				return safeGetId( this.getUser().getPerson().getId());
//			return null;
//		}
//		return (Long) fromPersonId.getId();
//	}

	protected void processUpdate() throws DataException {

		final Delegation theDelegate = getSavingDelegate();

		logger.debug( "Updating delegate id="+ theDelegate.getId() );

		// TODO: (?) ���������� ������ ����������...
		// savePermissionSet( theDelegate);

		// ���������� ...
		final String sqlText =
			"UPDATE delegation \n"+
			"SET \n"+ 
			"	  src_person_id = ? \n"+ 		// 1 
			"	, delegate_person_id = ? \n"+ 	// 2"

			"	, start_at = ? \n"+ 			// 3
			"	, end_at = ? \n"+ 				// 4
			
			"	, creator_person_id = ? \n"+			// 5
			//"	, delegation_role_id = ? \n"+ 	//
			//"	, is_src_has_access_too = ? \n"+//
			//"	, is_active = ? \n"+			//

			"WHERE delegation_id = ? \n"		// 6
			;
		final Object[] args= 
				new Object[] { 
						safeGetId( theDelegate.getFromPersonId()),			// 1
						safeGetId( theDelegate.getToPersonId()),			// 2

						theDelegate.getStartAt(),							// 3
						theDelegate.getEndAt(),								// 4
						
						safeGetId( theDelegate.getCreatorId()),							// 5

						//safeGetId( theDelegate.getPermissonSetId()),		//
						//bool2sql( theDelegate.isFromPersonHasAccessToo()),	// 
						//bool2sql( theDelegate.isActive()), 					// 

						safeGetId(theDelegate.getId()),						// 6
					};
		final int[] types = 
			new int[] { 
					Types.NUMERIC, 		// 1 
					Types.NUMERIC, 		// 2

					Types.TIMESTAMP, 	// 3
					Types.TIMESTAMP, 	// 4
					
					Types.NUMERIC,		// 5

					//Types.NUMERIC, 		// 
					//Types.INTEGER,		// 
					//Types.INTEGER,		// 

					Types.NUMERIC  		// 6
				}
			;
		logger.debug( "Update delegate SQL is " 
				+ SimpleDBUtils.getSqlQueryInfo( sqlText, args, types)
		);
		getJdbcTemplate().update( sqlText, args, types);

		logger.info( MessageFormat.format(
				"SUCCESSFULLY updated delegate {0} \n", 
				new Object[] { theDelegate}
			));

	}


	/* *
	 * ������� �������� �������� ������������ �� ��������� ����.
	 * @param bossId
	 * @param psetId
	 * @param startAt
	 * @param endAt
	 * @return ������ �� ObjectIDs.
	 */
	/*public List ObjectId loadActiveDelegates( ObjectId bossId, 
			ObjectId psetId, Date startAt, Date endAt)
	{
		final String SQL_QUERY_GetDelegateByBoss =
				"SELECT dlg.delegation_id, dlg.start_at, dlg.end_at \n" +
				"FROM delegation dlg \n"+ 
				"WHERE \n" +
				"\t\t	    (dlg.is_active = 1) \n" +
				// "\t\t	--	AND (dlg.is_src_has_access_too = 0) \n" +
				"\t\t	AND dlg.src_person_id = (?) \n" + 		// (:sourceUserID)
				"\t\t	AND dlg.delegation_role_id = (?) \n" 	// (:delegateRoleId)
				
				"\t\t	AND ( \n" +
				"\t\t\t\t  ( (dlg.start_at is NULL) or dlg.start_at <= now()) \n" +
				"\t\t\t\t  AND \n" +
				"\t\t\t\t  ( (dlg.end_at is NULL) or dlg.end_at >= now()) \n" +
				"\t\t	) \n"
				 
				;
		final Object[] args = new Object[] { 
					SavePermissionDelegate.safeGetId(bossId),
					SavePermissionDelegate.safeGetId(psetId)
				}; 
		final int[] types = new int[] { Types.NUMERIC, Types.NUMERIC, };

		final long normNewStartAt = makeNormTime(startAt);
		final long normNewEndAt = makeBigNormTime(endAt);
		final List ObjectId result = new ArrayList(10);
		getJdbcTemplate().query( 
					SQL_QUERY_GetDelegateByBoss, args, types,
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException
						{
							final long iStartAt = makeNormTime(rs.getDate(2));
							final long iEndAt = makeBigNormTime(rs.getDate(3));

							// ���� ���� �������� ���������� ������ � �������� 
							// ���������� - ���� ��� ����������
							final boolean isFar = 
									(iStartAt >= normNewEndAt || iEndAt < normNewStartAt);

							if (!isFar)	// ���� ���������� -> ���������� ���������� �������...
								result.add( new ObjectId( PermissionDelegate.class, rs.getLong(1)));

							return null;
						}
					}
				);
		return (result != null && !result.isEmpty()) ? result : null;
	}*/


	static final long makeNormTime( Date dt )
	{
		return (dt != null) ? dt.getTime() : Long.MIN_VALUE;
	}


	static final long makeBigNormTime( Date dt )
	{
		return (dt != null) ? dt.getTime() : Long.MAX_VALUE;
	}


	/* *
	 * ��������� ����� �� ������� ���������� �������� ��������.
	 * ���������� ����������� �������� - ���� �� ��� � ������������ �������� 
	 * �������� ������� � ����� �� ������������ �����. ���� ���� - �� ������ 
	 * ������� ��� ������ �������� � ����� �� �����.
	 * @param delegate
	 * @return true, ���� �����.
	 */
	/*public boolean canActivateDelegate( PermissionDelegate delegate)
	{
		if (delegate == null) return false;

		if (!delegate.isActive())
			// ���������� ������� ������ ����� ���������...
			return true;

		// �������� ������ �������� ������ ������� ���������...
		final List /*ObjectId* / listExists = loadActiveDelegates( 
				delegate.getFromPersonId(),	delegate.getPermissonSetId(),
				delegate.getStartAt(), 		delegate.getEndAt());

		boolean ok = true;
		if (listExists != null && !listExists.isEmpty()) {
			final boolean isnew = (delegate.getId() == null)
					|| (delegate.getId().getId() == null);
			if (isnew) // ��� ������ - ��� ������ ���� ���������...
				ok = false;
			else // ��� ������� - ��������� ������� ������ ������ ��������
				ok = (listExists.size() == 1) 
					&& listExists.contains( delegate.getId());
		}
		return ok;
	}*/


	public static final Long safeGetId(ObjectId id) {
		return (id == null) ? null : (Long) id.getId();
	}


	public static final Integer bool2sql( boolean flag ) {
		return new Integer( (flag ? 1 : 0) );
	}

//	public static final boolean sql2bool( int flagAsInt ) {
//		return (flagAsInt == 1);
//	}
}
