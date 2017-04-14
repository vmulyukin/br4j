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

//import java.io.IOException;
//import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link PersonalSearch} instance from database 
 */
public class GetStoredSearch extends ObjectQueryBase
{
	/**
	 * Fetches single {@link PersonalSearch} instance from database
	 * @return fetched {@link PersonalSearch} instance
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().queryForObject(
				"SELECT person_search_id, person_id, search_name, search_description, search_param, search_area " +
				"FROM person_search WHERE person_search_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						PersonalSearch search = new PersonalSearch();
						search.setId(rs.getLong(1));
						search.setPerson(rs.getLong(2));
						search.setName(rs.getString(3));
						search.setDescription(rs.getString(4));
						search.setArea(rs.getString(6));	
						/*try {
							Clob data = rs.getClob(5);
							char[] xml = new char[(int) data.length()];
							data.getCharacterStream().read(xml);
							search.setSearchXml(new String(xml));
						} catch (IOException e) {
							throw new ExceptionEnvelope(new DataException("fetch.search.search", e));
						}*/
						search.setSearchXml(SimpleDBUtils.getClobAsStr(rs, 5));
						
						return search;
					}
				});
	}
}
