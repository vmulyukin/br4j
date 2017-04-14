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
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all children {@link CardVersion}
 * objects for given {@link Card} object.
 */
public class ListCardVersions extends ChildrenQueryBase
{
	/**
	 * Fetches all children {@link CardVersion} objects of given {@link Card} ordered by version number.
	 * <br>
	 * NOTE: {@link CardVersion} objects returned by this method is not fully initialized and
	 * have no information about attributes included in {@link CardVersion}.
	 * @return collection of {@link CardVersion} objects representing previous versions
	 * of given {@link Card}.
	 */
	public Object processQuery() throws DataException
	{		
		return getJdbcTemplate().query(
				"SELECT card_id, version_id, version_date, status_id, parent_card_id " +//, file_name, external_path " +
				"FROM card_version WHERE card_id=? ORDER BY version_id",
				new Object[] { getParent().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						CardVersion version = new CardVersion();
						version.setId(rs.getLong(1), rs.getInt(2));
						version.setVersionDate(new Date(rs.getDate(3).getTime()));
						version.setState(CardState.getId(rs.getInt(4)));
						//version.setFileName(rs.getString(6));
						//version.setUrl(rs.getString(7));
						return version;
					}
				});
	}
}
