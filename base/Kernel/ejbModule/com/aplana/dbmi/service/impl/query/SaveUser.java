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
import java.util.Date;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save instances of {@link Person} class.<br>
 * This query could be used for updating already defined users and creating new users.
 * User could change {@link Person#getDepartment() department} property of {@link Person}
 * object only, all other changes will be ignored by this query implementation.<br>
 * Attempt to create user that already exists will result with throwing of {@link DataException}.
 */
public class SaveUser extends SaveQueryBase
{
	/**
	 * Identifier of 'User changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_USER";
	/**
	 * Identifier of 'User created' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_USER";
	
	protected ObjectId processNew() throws DataException
	{
		Person person = (Person) getObject();
		int count = getJdbcTemplate().queryForInt(
				"SELECT count(person_id) FROM person WHERE person_login=? AND is_active=1",
				new Object[] { person.getLogin() });
		if (count > 0){
			throw new DataException("such.user.already.exists", new Object[] { person.getLogin() });
		}
		person.setId(getJdbcTemplate().queryForLong( "SELECT nextval('seq_person_id')"));
		getJdbcTemplate().update(
				"INSERT INTO person (person_id, person_login, full_name, " +
				"email, sync_date, is_active, card_id) " +
				"VALUES (?, ?, ?, ?, ?, 1, ?)",
				new Object[] { person.getId().getId(), person.getLogin(), person.getFullName(),
						person.getEmail(), new Date(), person.getCardId().getId() },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,
						Types.VARCHAR, Types.TIMESTAMP, Types.NUMERIC });
		
		if (logger.isDebugEnabled())
			logger.debug("User " + person.getLogin() + " added");
		return person.getId();
	}

	protected void processUpdate() throws DataException
	{
		checkLock();
		Person user = (Person) getObject();
		getJdbcTemplate().update(
			"UPDATE person SET" +
				" card_id = ?, full_name=?, email=?, is_active=?" +
			" WHERE person_id = ?",
			new Object[] {
				user.getCardId() != null ? user.getCardId().getId() : null,
				user.getFullName(),
				user.getEmail(),
				user.isActive() ? 1 : 0,
				user.getId().getId()
			},
			new int[] {
				Types.NUMERIC,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.NUMERIC,
				Types.NUMERIC
			}
		);
	}

	/**
	 * Validation routine
	 */
	public void validate() throws DataException
	{
		Person user = (Person) getObject();
		if (isNew()){
			if (null == user || user.getLogin() == null || user.getLogin().isEmpty())
				throw new DataException("store.user.new.error");
		}
		else if (user.getId() == null)
			throw new DataException("store.user.update");
		// TODO: �������� �������� �� ��, ��� �������� ������� 
		// ������� �� ������� �������(����������)
		super.validate();
	}

	/**
	 * @return {@link #EVENT_ID}
	 */
	public String getEvent()
	{
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}
}
