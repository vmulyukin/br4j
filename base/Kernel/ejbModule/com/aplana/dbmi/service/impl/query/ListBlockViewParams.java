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
import java.sql.Types;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all children {@link BlockViewParam}
 * objects for given {@link Template} object.
 */
public class ListBlockViewParams extends ChildrenQueryBase {
	/**
	 * Fetches all children {@link BlockViewParam} objects of given {@link Template}.
	 * @return collection of {@link BlockViewParam} objects
	 */
	public Object processQuery() throws DataException {
		return getJdbcTemplate().query(
			"SELECT template_id, block_code, status_id, state_block, rec_id " +
			"FROM block_view_param " +
			"WHERE template_id=?",
			new Object[] {getParent().getId()},
			new int[] { Types.NUMERIC }, // (2010/03) POSTGRE
			new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					BlockViewParam bvp = new BlockViewParam();
					bvp.setTemplate(rs.getLong(1));
					bvp.setBlock(rs.getString(2));
					bvp.setCardStatus(rs.getLong(3));
					bvp.setStateBlock(rs.getInt(4));
					bvp.setId(rs.getLong(5));
					return bvp;
				}
			}
		);
		
	}
}
