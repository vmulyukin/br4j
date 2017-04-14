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

import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save single {@link LogAction} instance in database
 * @author dsultanbekov
 */
public class SaveLogAction extends SaveQueryBase {

	protected ObjectId processNew() throws DataException {
		LogAction action = (LogAction)getObject();
		String id = generateStringId("action");
		getJdbcTemplate().update(
			"insert into action (action_code, action_name_rus, action_name_eng)" +
			" values (?, ?, ?)",
			new Object[] {id, action.getName().getValueRu(), action.getName().getValueEn()},
			new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}
		);
		return new ObjectId(LogAction.class, id);
	}

	protected void processUpdate() throws DataException {
		checkLock();
		LogAction action = (LogAction)getObject();
		getJdbcTemplate().update(
			"update action set action_name_rus = ?, action_name_eng = ?" +
			" where action_code = ?",
			new Object[] {action.getName().getValueRu(), action.getName().getValueEn(), action.getId().getId()},
			new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR}
		);
	}

	public void validate() throws DataException {
		LogAction action = (LogAction)getObject();
		if (action.getName() == null || action.getName().hasEmptyValues()) {
			throw new DataException("store.logaction.empty.name");
		}
		super.validate();
	}	
}
