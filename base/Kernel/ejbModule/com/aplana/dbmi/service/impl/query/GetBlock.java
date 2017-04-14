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
import java.util.Collection;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link AttributeBlock} instance
 */
public class GetBlock extends ObjectQueryBase
{
	/**
	 * Fetches definition of {@link AttributeBlock}
	 * @return fully initialized {@link AttributeBlock} 
	 * instance (with initialized collection of attributes)
	 */
	public Object processQuery() throws DataException
	{
		AttributeBlock block = (AttributeBlock) getJdbcTemplate().queryForObject(
				"SELECT block_code, block_name_rus, block_name_eng, " +
					"is_active, is_system, locked_by, lock_time " +
				"FROM attr_block WHERE block_code=?",
				new Object[] { getId().getId() },
				new int[] { Types.VARCHAR },
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
		ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(block.getClass(), Attribute.class);
		subQuery.setParent(block.getId());
		block.setAttributes((Collection) getDatabase().executeQuery(getUser(), subQuery));
			/*ListBlockAttributes subQuery = new ListBlockAttributes();
			subQuery.setJdbcTemplate(getJdbcTemplate());
			subQuery.setUser(getUser());
			subQuery.setParent(block.getId());
			block.setAttributes((Collection) subQuery.processQuery());*/
		return block;
	}
}
