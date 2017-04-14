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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.support.SqlLobValue;
//import org.springframework.jdbc.support.lob.OracleLobHandler;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.Notification;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * {@link SaveQueryBase} dfecendant used to save instances of {@link Distribution}
 * and {@link Subscription} classes.
 */
public class SaveNotification extends SaveQueryBase
{
	/**
	 * Identifier of 'New distribution created' log event. Not used now.
	 */
	public static final String EVENT_ID_DISTR_CREATE = "NEW_DISTRIBUTION";
	/**
	 * Identifier of 'Distribution changed' log event. Not used now.
	 */
	public static final String EVENT_ID_DISTR_CHANGE = "CHG_DISTRIBUTION";
	
	/*public String getEvent() {
		if (getObject() instanceof Distribution)
			return isNew() ? EVENT_ID_DISTR_CREATE : EVENT_ID_DISTR_CHANGE;
		return null;
	}*/

	protected ObjectId processNew() throws DataException
	{
		Notification distr = (Notification) getObject();
		boolean isForced = distr instanceof Distribution;
		// (2010/03) POSGRE
		// OLD: long idNew = getJdbcTemplate().queryForLong("SELECT seq_notif_rule_id.nextval FROM dual");
		long idNew = getJdbcTemplate().queryForLong("SELECT nextval('seq_notif_rule_id')");

		if (isForced)
			((Distribution) distr).setId(idNew);
		else
			((Subscription) distr).setId(idNew);
		getJdbcTemplate().update(
				"INSERT INTO notification_rule (notif_rule_id, rule_name, rule_description, search_param, " +
					"creation_date, reoccurrence_interval, person_id, is_forced) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { distr.getId().getId(), distr.getName(), distr.getDescription(),
						new SqlLobValue(distr.getSearchXml()/*, new OracleLobHandler()*/),
						new Date(), distr.getFrequency(), getUser().getPerson().getId().getId(),
						new Integer(isForced ? 1 : 0) },
				new int [] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.CLOB,
						Types.DATE, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC });
		if (isForced)
			storeRecipients();
		return distr.getId();
	}

	protected void processUpdate() throws DataException
	{
		Notification distr = (Notification) getObject();
		getJdbcTemplate().update(
				"UPDATE notification_rule SET rule_name=?, rule_description=?, search_param=?, " +
					"creation_date=?, reoccurrence_interval=? " +
				"WHERE notif_rule_id=?",
				new Object[] { distr.getName(), distr.getDescription(),
						new SqlLobValue(distr.getSearchXml()/*, new OracleLobHandler()*/),
						new Date(), distr.getFrequency(), distr.getId().getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.CLOB,
						Types.DATE, Types.VARCHAR, Types.NUMERIC });
		if (distr instanceof Distribution) {
			getJdbcTemplate().update(
					"DELETE FROM notif_access_control_list WHERE notif_rule_id=?",
					new Object[] { distr.getId().getId() },
					new int[] { Types.NUMERIC }
					);
			storeRecipients();
		}
	}
	
	private void storeRecipients()
	{
		getJdbcTemplate().execute(new ConnectionCallback() {
			public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
				Distribution distr = (Distribution) getObject();
				PreparedStatement stmt = conn.prepareStatement(
						"INSERT INTO notif_access_control_list " +
							"(notif_rule_id, region_value_id, role_code, dept_value_id, person_id) " +
						"VALUES (?, ?, ?, ?, ?)");
				Iterator itr = distr.getRegions().iterator();
				while (itr.hasNext()) {
					ReferenceValue region = (ReferenceValue) itr.next();
					stmt.setObject(1, distr.getId().getId());
					stmt.setObject(2, region.getId().getId());
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.NUMERIC);
					stmt.setNull(5, Types.NUMERIC);
					stmt.execute();
				}
				itr = distr.getAccessList().iterator();
				while (itr.hasNext()) {
					AccessListItem item = (AccessListItem) itr.next();
					stmt.setObject(1, distr.getId().getId());
					stmt.setNull(2, Types.NUMERIC);
					stmt.setNull(3, Types.VARCHAR);
					stmt.setNull(4, Types.NUMERIC);
					stmt.setNull(5, Types.NUMERIC);
					switch(item.getType()) {
					case AccessListItem.TYPE_ROLE:
						stmt.setString(3, item.getRoleType());
						break;
					case AccessListItem.TYPE_DEPARTMENT:
						stmt.setObject(4, item.getDepartment().getId().getId());
						break;
					case AccessListItem.TYPE_PERSON:
						stmt.setObject(5, item.getPerson().getId().getId());
						break;
					}
					stmt.execute();
				}
				return null;
			}
		});
	}

	/**
	 * Checks validity of {@link Notification} object being saved.
	 */
	public void validate() throws DataException
	{
		Notification obj = (Notification) getObject();
		String freq = obj.getFrequency();
		if (!Notification.FREQ_HOURLY.equals(freq) &&
			!Notification.FREQ_DAYLY.equals(freq) &&
			!Notification.FREQ_WEEKLY.equals(freq) &&
			!Notification.FREQ_MONTHLY.equals(freq) &&
			!Notification.FREQ_NONE.equals(freq))
			throw new DataException("store.notification.frequency");
		
		if (obj instanceof Subscription) {
			if (obj.getId() != null) {
				long userId = getJdbcTemplate().queryForLong(
						"SELECT person_id FROM notification_rule WHERE notif_rule_id=?",
						new Object[] { obj.getId().getId() });
				if (!new ObjectId(Person.class, userId).equals(getUser().getPerson().getId()))
					throw new DataException("store.subscription.others");
			}
		} /*else if (obj instanceof Distribution) {		//*****
			Distribution distr = (Distribution) obj;
			if (distr)
		}*/
		super.validate();
	}
}
