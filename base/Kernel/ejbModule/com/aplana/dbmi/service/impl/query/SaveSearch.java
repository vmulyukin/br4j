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

import org.springframework.jdbc.core.support.SqlLobValue;
//import org.springframework.jdbc.support.lob.OracleLobHandler;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save {@link PersonalSearch} instances
 */
public class SaveSearch extends SaveQueryBase
{
	protected ObjectId processNew() throws DataException
	{
		PersonalSearch search = (PersonalSearch) getObject();
		// (2010/03) POSGRE
		// OLD: search.setId(getJdbcTemplate().queryForLong("SELECT seq_system_id.nextval FROM dual"));
		search.setId( super.generateId("seq_system_id") );

		getJdbcTemplate().update(
				"INSERT INTO person_search (person_search_id, person_id, search_name, search_description, search_area, search_param) " +
				"VALUES (?, ?, ?, ?, ?,?)",
				new Object[] { search.getId().getId(), getUser().getPerson().getId().getId(),
						search.getName(), search.getDescription(),search.getArea(),
						new SqlLobValue(search.getSearchXml()/*, new OracleLobHandler()*/) },
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR,Types.VARCHAR, Types.CLOB });
		return search.getId();
	}

	protected void processUpdate() throws DataException
	{
		PersonalSearch search = (PersonalSearch) getObject();
		getJdbcTemplate().update(
				"UPDATE person_search " +
				"SET search_name=?, search_description=?, search_area=?, search_param=? " +
				"WHERE person_search_id=?",
				new Object[] { search.getName(), search.getDescription(),search.getArea(),
						new SqlLobValue(search.getSearchXml()/*, new OracleLobHandler()*/),
						search.getId().getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR,Types.VARCHAR, Types.CLOB, Types.NUMERIC });
	}

	/**
	 * Checks that stored {@link PersonalSearch} objects belongs to user who performs save
	 */
	public void validate() throws DataException
	{
		PersonalSearch search = (PersonalSearch) getObject();
		if (search.getSearchXml() == null)
			throw new DataException("store.search.empty");
		if (search.getId() != null) {
			long id = getJdbcTemplate().queryForLong(
					"SELECT person_id FROM person_search WHERE person_search_id=?",
					new Object[] { search.getId().getId() },
					new int[] { Types.NUMERIC }
					);
			if (!getUser().getPerson().getId().equals(new ObjectId(Person.class, id)))
				throw new DataException("store.search.owner");
		}
		super.validate();
	}
}
