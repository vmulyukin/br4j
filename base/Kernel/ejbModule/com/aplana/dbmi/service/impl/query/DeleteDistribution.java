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

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * Query used to delete {@link com.aplana.dbmi.model.Distribution} object from database.
 */
public class DeleteDistribution extends ObjectQueryBase
{
	/**
	 * Identifier of action to be written in system log.<br>
	 * TODO: Not used now. Delete? 
	 */
	public static final String EVENT_ID = "DEL_DISTRIBUTION";
	
	/*public String getEvent()
	{
		return EVENT_ID;
	}*/

	/**
	 * Delete one record from NOTIFICATION_RULE table and its children from
	 * NOTIF_ACCESS_CONTROL table.
	 * @return null
	 */
	public Object processQuery() throws DataException
	{
		getJdbcTemplate().update(
				"DELETE FROM notif_access_control_list WHERE notif_rule_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }
				);
		getJdbcTemplate().update(
				"DELETE FROM notification_rule WHERE notif_rule_id=? AND is_forced=1",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }
				);
		return null;
	}
}
