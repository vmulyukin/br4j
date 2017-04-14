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
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Subscription} instance from database 
 */
public class GetSubscription extends ObjectQueryBase
{
	/**
	 * Fetches single {@link Subscription} instance from database
	 * @return fetched {@link Subscription} instance
	 */
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().queryForObject(
				"SELECT notif_rule_id, rule_name, rule_description, search_param, " +
					"creation_date, reoccurrence_interval, person_id, last_send_date " +
				"FROM notification_rule WHERE notif_rule_id=? AND is_forced=0",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Subscription subscr = new Subscription();
						subscr.setId(rs.getLong(1));
						subscr.setName(rs.getString(2));
						subscr.setDescription(rs.getString(3));
						subscr.setSearchXml(rs.getString(4));
						subscr.setCreationDate(rs.getDate(5));
						subscr.setFrequency(rs.getString(6));
						subscr.setPersonId(new ObjectId(Person.class, rs.getLong(7)));
						if (rs.getObject(8) != null)
							subscr.setLastSentDate(new Date(rs.getDate(8).getTime()));
						/*try {
							Clob data = rs.getClob(4);
							char[] xml = new char[(int) data.length()];
							data.getCharacterStream().read(xml);
							subscr.setSearchXml(new String(xml));
						} catch (IOException e) {
							throw new ExceptionEnvelope(new DataException("fetch.notification.search", e));
						}*/
						subscr.setSearchXml(SimpleDBUtils.getClobAsStr(rs, 4));
						
						return subscr;
					}
				});
	}
}
