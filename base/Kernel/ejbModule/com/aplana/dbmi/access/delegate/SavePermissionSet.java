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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Iterator;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save {@link PermissionSet} instances.
 * @author RAbdullin
 */
public class SavePermissionSet extends SaveQueryBase {

	PermissionSet getSavingSet()
	{
		return (PermissionSet) super.getObject();
	}


	public void validate() throws DataException 
	{
		final PermissionSet theSet = getSavingSet();
		if (theSet == null)
			throw new DataException("factory.store", 
					new Object[] { PermissionSet.class.getName() + "(NULL)"} );

		// ���� theSet.getId() == null, �� ��� �����,
		// (!) ����� id ������ �������. 
		if (theSet.getId() != null && theSet.getId().getId() == null)
			throw new DataException( "delegate.permissions.id.notset1", new Object[] {"NULL"});
		super.validate();
		// ... any other data is acceptable
	}


	protected ObjectId processNew() throws DataException 
	{
		logger.debug("Saving new permission set ... ");

		final PermissionSet theSet = getSavingSet();

		// generation new id...
		final long newId = generateId("seq_delegation_role_id");
		theSet.setId( newId);

		// (!) Adding...
		getJdbcTemplate().update(
				"INSERT INTO delegation_role \n"+
				"	( delegation_role_id,	name_rus,	name_eng) \n"+
				"VALUES \n"+
				"	( ?,	?,	?)",
				new Object[] { theSet.getId().getId(), 
						theSet.getName().getValueRu(),
						theSet.getName().getValueEn() 
					},
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR }
				);
		logger.debug( MessageFormat.format(
				"Added delegation role with id={0}: ru=''{1}'', en=''{2}''", 
				new Object[] { 
						theSet.getId(),
						theSet.getName().getValueRu(),
						theSet.getName().getValueEn()
				} ));

		// ���������� ����������...
		insertSetPermissions( theSet);

		logger.info( MessageFormat.format(
				"SUCCESSFULLY added permissions & delegation role ''{0}'' (id={1})", 
				new Object[] { theSet.getName().getValue(), theSet.getId()} 
			));

		return theSet.getId();
	}


	protected void processUpdate() throws DataException {

		final PermissionSet theSet = getSavingSet();

		logger.debug("Updating permission set id="+ theSet.getId() );

		// ���������� ...
		getJdbcTemplate().update(
				"UPDATE delegation_role \n"+
				"SET \n"+
				"	name_rus = ?, \n" +
				"	name_eng = ? \n" +
				"WHERE delegation_role_id = ?",
				new Object[] {  
						theSet.getName().getValueRu(),
						theSet.getName().getValueEn(),
						theSet.getId().getId()
					},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.NUMERIC}
				);
		logger.debug( MessageFormat.format(
				"Updated delegation role with id={0}: ru=''{1}'', en=''{2}''", 
				new Object[] { 
						theSet.getId(),
						theSet.getName().getValueRu(),
						theSet.getName().getValueEn()
				} ));

		// �������� ������� ����������...
		// deleteSetPermisssionsById( theSet.getId());
		final long delCount = getJdbcTemplate().update(
				"DELETE FROM permission WHERE delegation_role_id = ?",
				new Object[] { theSet.getId().getId()},
				new int[] { Types.NUMERIC}
				);
		logger.debug( MessageFormat.format(
				"Removed {0} pred persmissions of delegation role ''{1}'' (id={2})", 
				new Object[] { 
						new Long(delCount),
						theSet.getName().getValue(),
						theSet.getId()
				} ));

		// ���������� ����������...
		insertSetPermissions( theSet);

		logger.info( MessageFormat.format(
				"SUCCESSFULLY saved permissions & delegation role ''{0}'' (id={1})", 
				new Object[] { theSet.getName().getValue(), theSet.getId()} 
			));
	}


	/**
	 * �������� id ���������� � ������� permission ������ �������.
	 * @param permSet: ����� CardAccess, ������� ���� ��������.
	 */
	private void insertSetPermissions(final PermissionSet permSet) {

		if (permSet == null || permSet.isEmpty()) 
			return;

		final long delegate_role_id = ((Long) permSet.getId().getId()).longValue();

		logger.debug( MessageFormat.format(
				"adding {0} permissions of set ''{1}'' (id={2})", 
				new Object[] { 
						new Long(permSet.size()),
						permSet.getName().getValue(), 
						permSet.getId()
				} ));

		/* �������� ���������� */
		final BatchPreparedStatementSetter pss = 
			new BatchPreparedStatementSetter() {
			final Iterator iter = permSet.iterator();

			public int getBatchSize() {
				return permSet.size();
			}

			public void setValues(PreparedStatement stmt, int index) 
			throws SQLException 
			{
				final CardAccess ca = (CardAccess) iter.next();
				stmt.setLong( 1, delegate_role_id);
				if (ca == null || ca.getId() == null)
					stmt.setNull( 2, Types.NUMERIC);
				else
					stmt.setLong( 2, ((Long) ca.getId().getId()).longValue() );
			}
		};		
		getJdbcTemplate().batchUpdate(
				"INSERT INTO permission \n" +
				"	(delegation_role_id, card_access_id) \n" +
				"VALUES \n" +
				"	(?) \n", 
				pss);

		logger.info( MessageFormat.format(
				"Added {0} permissions of set ''{1}'' (id={2})", 
				new Object[] { 
						new Long( permSet.size()),
						permSet.getName().getValue(), 
						permSet.getId()
				} ));
	}

}
