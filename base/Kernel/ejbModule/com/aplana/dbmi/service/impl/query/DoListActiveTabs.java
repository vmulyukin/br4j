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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetActiveTab;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoListActiveTabs extends ActionQueryBase {

	public Object processQuery() throws DataException {
		GetActiveTab action;
		ObjectId template;
		ObjectId status;
		try {
			action = (GetActiveTab)getAction();
			template = action.getTemplate();
			status = action.getStatus();
		} catch (Exception e) {
			return null;
		}

		return getJdbcTemplate().query(
				"SELECT t.tab_id, t.name_rus, t.name_eng " +
				"FROM card_status cs, tab t, tab_template_state tts " +
				"WHERE t.tab_id=tts.tab_id AND tts.status_id=cs.status_id AND cs.status_id=? AND "+
				"tts.template_id =?",
				new Object[] {status.getId(), template.getId()},
				new int[] { Types.NUMERIC, Types.NUMERIC }, // (2010/03) POSTGRE
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Tab tab = new Tab();
						tab.setId(rs.getLong(1));
						tab.setNameRu(rs.getString(2));
						tab.setNameEn(rs.getString(3));
						return tab;
					}
				}
			);
	}
}
