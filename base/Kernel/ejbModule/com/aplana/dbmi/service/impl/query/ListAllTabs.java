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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all {@link Tab} objects defined in system.<br>
 */
public class ListAllTabs extends QueryBase {
	public Object processQuery() throws DataException {
		/**
		 * Fetch all {@link Tab} objects defined in system.<br>
		 * NOTE: {@link Tab} instances returned by this query is not fully initialized
		 * and doesn't contain information about blocks included in tabs.
		 * @return list of {@link Tab} objects representing
		 * all tabs defined in system.
		 */
		return getJdbcTemplate().query(
				"SELECT tab_id, name_rus, name_eng " +
				"FROM tab",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Tab tab = new Tab();
						tab.setId(rs.getLong(1));
						tab.setNameRu(rs.getString(2));
						tab.setNameEn(rs.getString(3));
						return tab;
					}
				});
	}
}
