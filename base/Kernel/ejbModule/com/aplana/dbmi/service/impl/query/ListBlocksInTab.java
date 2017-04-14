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

import com.aplana.dbmi.model.TabBlockViewParam;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all children {@link BlockView}
 * objects for given {@link Tab} object.
 */
public class ListBlocksInTab extends ChildrenQueryBase {
	/**
	 * Fetches all children {@link BlockView} objects of given {@link Tab}.
	 * @return collection of {@link BlockView} objects
	 */
	public Object processQuery() throws DataException {
		return getJdbcTemplate().query(
			"SELECT tb.tab_id, tb.block_code, tb.layout " +
			"FROM tab_block tb " +
			"WHERE tb.tab_id=?",
			new Object[] {getParent().getId()},
			new int[] { Types.NUMERIC },
			new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					TabBlockViewParam bv = new TabBlockViewParam();
					bv.setTab(rs.getLong(1));
					bv.setId(rs.getString(2));
					bv.setLayout(String.valueOf(rs.getInt(3)));
					return bv;
				}
			}
		);
	}
}
