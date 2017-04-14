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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link com.aplana.dbmi.model.CardState}
 * instance from database.
 */
public class GetCardState extends ObjectQueryBase {
	/**
	 * Fetches {@link com.aplana.dbmi.model.CardState} instance from database
	 * @return {@link com.aplana.dbmi.model.CardState} instance 
	 */
	public Object processQuery() throws DataException {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				CardState res = (CardState)DataObject.createFromId(getId());
				res.getName().setValueRu(rs.getString(1));
				res.getName().setValueEn(rs.getString(2));
				res.getDefaultMoveName().setValueRu(rs.getString(3));
				res.getDefaultMoveName().setValueEn(rs.getString(4));
				return res;
			}
		};
 
		try {
			return getJdbcTemplate().queryForObject(
				"select name_rus, name_eng, default_move_name_rus, default_move_name_eng from card_status cs where cs.status_id = ?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				rowMapper
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
		

}
