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

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save single {@link CardState} instances in database
 * @author dsultanbekov
 */
public class SaveCardState extends SaveQueryBase {	
	protected ObjectId processNew() throws DataException {
		CardState cs = (CardState)getObject();
		Long statusId = new Long(generateId("seq_system_id"));
		getJdbcTemplate().update(
				"insert into card_status (status_id, name_rus, name_eng" +
				", default_move_name_rus, default_move_name_eng)" +
				" values (?, ?, ?, ?, ?)",
				new Object[] {
					statusId,
					cs.getName().getValueRu(),
					cs.getName().getValueEn(),
					cs.getDefaultMoveName().getValueRu(),
					cs.getDefaultMoveName().getValueEn()
				},
				new int[] {
					Types.NUMERIC,
					Types.VARCHAR,
					Types.VARCHAR,
					Types.VARCHAR,
					Types.VARCHAR					
				}
			);
		return new ObjectId(CardState.class, statusId);
	}

	protected void processUpdate() throws DataException {
		CardState cs = (CardState)getObject();
		checkLock();
		getJdbcTemplate().update(
			"update card_status set" +
			" name_rus = ?" +
			", name_eng = ?" +
			", default_move_name_rus = ?" +
			", default_move_name_eng = ?" +
			" where status_id = ?",
			new Object[] {
				cs.getName().getValueRu(),
				cs.getName().getValueEn(),
				cs.getDefaultMoveName().getValueRu(),
				cs.getDefaultMoveName().getValueEn(),
				cs.getId().getId()
			},
			new int[] {
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.VARCHAR,
				Types.NUMERIC
			}
		);
	}

	public void validate() throws DataException {
		CardState cs = (CardState)getObject();
		if (cs.getName() == null || cs.getName().hasEmptyValues()) {
			throw new DataException("store.cardstate.empty.name");
		}
		super.validate();
	}
}
