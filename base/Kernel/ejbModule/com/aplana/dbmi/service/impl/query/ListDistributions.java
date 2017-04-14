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
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * Query used to fetch all {@link Distribution} objects stored in database.
 */
public class ListDistributions extends QueryBase
{
	/**
	 * Fetches all {@link Distribution} objects stored in database.<br>
	 * NOTE: returned {@link Distribution} will be initialized only partially.<br>
	 * @return list containing all {@link Distribution} objects stored in database 
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().query(
				"SELECT n.notif_rule_id, n.rule_name, n.rule_description, n.search_param, " +
					"n.creation_date, n.reoccurrence_interval, n.person_id, n.last_send_date, p.full_name " +
				"FROM notification_rule n INNER JOIN person p ON n.person_id=p.person_id " +
				"WHERE n.is_forced=1",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Distribution distr = new Distribution();
						distr.setId(rs.getLong(1));
						distr.setName(rs.getString(2));
						distr.setDescription(rs.getString(3));
						// distr.setSearchXml(rs.getString(4)); @see below
						distr.setCreationDate(rs.getDate(5));
						distr.setFrequency(rs.getString(6));
						distr.setCreator(new ObjectId(Person.class, rs.getLong(7)));
						if (rs.getObject(8) != null)
							distr.setLastSentDate(new Date(rs.getDate(8).getTime()));
						distr.setCreatorName(rs.getString(9));
						/*try {
							Clob data = rs.getClob(4);
							char[] xml = new char[(int) data.length()];
							data.getCharacterStream().read(xml);
							distr.setSearchXml(new String(xml));
						} catch (IOException e) {
							throw new ExceptionEnvelope(new DataException("fetch.notification.search", e));
						}*/
						distr.setSearchXml(SimpleDBUtils.getClobAsStr(rs, 4));

						BuildSearchSummary summarizer = new BuildSearchSummary();
						summarizer.setJdbcTemplate(getJdbcTemplate());
						try {
							summarizer.setSearch(distr.getSearch());
						} catch (DataException e) {
							throw new ExceptionEnvelope(e);
						}
						summarizer.buildSummary();
						return distr;
					}
				});
	}
}
