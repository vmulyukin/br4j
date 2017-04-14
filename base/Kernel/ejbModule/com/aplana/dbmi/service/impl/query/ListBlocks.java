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

import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all {@link AttributeBlock} objects defined in system.<br>
 */
public class ListBlocks extends QueryBase
{
	public Object processQuery() throws DataException {
		/**
		 * Fetch all {@link AttributeBlock} objects defined in system.<br>
		 * NOTE: {@link AttributeBlock} instances returned by this query is not fully initialized
		 * and doesn't contain information about attributes included in blocks.
		 * @return list of {@link AttributeBlock} objects representing
		 * all attribute blocks defined in system.
		 */
		return getJdbcTemplate().query(
				"SELECT block_code, block_name_rus, block_name_eng, is_active, is_system, locked_by, lock_time " +
				"FROM attr_block",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						AttributeBlock block = new AttributeBlock();
						block.setId(rs.getString(1));
						block.setNameRu(rs.getString(2));
						block.setNameEn(rs.getString(3));
						block.setActive(rs.getBoolean(4));
						block.setSystem(rs.getBoolean(5));
						if (rs.getObject(6) != null) {
							block.setLocker(rs.getLong(6));
							block.setLockTime(rs.getTimestamp(7));
						}
						return block;
					}
				});
	}
}
