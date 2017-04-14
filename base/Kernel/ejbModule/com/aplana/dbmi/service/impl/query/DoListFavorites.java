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
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.entdb.ManagerTempTables;

/**
 * Query used to perform {@link com.aplana.dbmi.action.ListFavorites} action
 */
public class DoListFavorites extends ActionQueryBase
{
//	���������� � MIShowListPortlet � ����� � ��������� �������� � ���������� � Kernel � ������������ ��������� Search ���������� � MIShowListPortlet
//	public static final String ID_NAME = "favorites.name"; 

	private static final long serialVersionUID = 1L;

	/**
	 * Returns all favorite cards' IDs for user who performs query
	 * @return {@link List} object representing all favorite cards' IDs for
	 * user who performs query 
	 */
	@SuppressWarnings("unchecked")
	public Object processQuery() throws DataException
	{
		List<ObjectId> favList = new ArrayList<ObjectId>();
		final ManagerTempTables mantmp = new ManagerTempTables( this.getJdbcTemplate() );
		mantmp.startAll();
		try {
			String sql = "SELECT card_id FROM person_card WHERE person_id=?";
			favList = getJdbcTemplate().query(sql, 
					new Object[] { getUser().getPerson().getId().getId() },
					new int[] { Types.NUMERIC },
					new RowMapper(){
						public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							return new ObjectId(Card.class, rs.getLong(1));
				}});

		} finally {
			mantmp.close();
		}
		return favList;
	}
}
