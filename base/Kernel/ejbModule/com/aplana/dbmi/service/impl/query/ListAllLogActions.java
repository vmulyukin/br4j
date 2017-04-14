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

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all instances of {@link LogAction} class defined in DBMI system 
 * @author dsultanbekov
 */
public class ListAllLogActions extends QueryBase {

	/**
	 * Fetches all {@link LogAction} instances from database
	 * @return list of {@link LogAction} instances
	 */
	public Object processQuery() throws DataException {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				ObjectId recId = new ObjectId(LogAction.class, rs.getString(1));
				LogAction res = (LogAction)DataObject.createFromId(recId);
				res.getName().setValueRu(rs.getString(2));
				res.getName().setValueEn(rs.getString(3));
				return res;
			}
		};
		return getJdbcTemplate().query(
			"select action_code, action_name_rus, action_name_eng from action",
			rowMapper
		);
	}
}
