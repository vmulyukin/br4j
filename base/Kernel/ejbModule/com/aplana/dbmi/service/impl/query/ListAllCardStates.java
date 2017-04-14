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
import java.util.Collection;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.model.filter.StateIdListFilter;
import com.aplana.dbmi.model.filter.TemplateForCreateNewCard;
import com.aplana.dbmi.model.filter.TemplateForSearchFilter;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * {@link QueryBase} descendant used to list all {@link CardState} instances defined in DBMI system
 * @author dsultanbekov
 */
public class ListAllCardStates extends QueryBase {
	/**
	 * Fetches all {@link CardState} instances defined in DBMI system
	 * @return List of all {@link CardState} instances defined in database
	 */
	public Object processQuery() throws DataException {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				ObjectId recId = new ObjectId(CardState.class, rs.getLong(1));
				CardState res = (CardState)DataObject.createFromId(recId);
				res.getName().setValueEn(rs.getString(2));
				res.getName().setValueRu(rs.getString(3));
				res.getDefaultMoveName().setValueRu(rs.getString(4));
				res.getDefaultMoveName().setValueEn(rs.getString(5));
				return res;
			}
		};
		String sql = "select status_id, name_eng, name_rus, " +
				"default_move_name_rus, default_move_name_eng " +
				"from card_status";
		
		sql = sql + getFilterClause(); 
		
		return getJdbcTemplate().query(sql, rowMapper);
	}
	
	private String getFilterClause() {

		String sql = "";

		Filter filter = getFilter();

		if (filter != null) {
			if (filter instanceof StateIdListFilter) {

				StateIdListFilter idListfilter = (StateIdListFilter) filter;
				Collection filterStateIds = idListfilter.getStateIds();

				if (!filterStateIds.isEmpty()) 
					sql += " where card_status.status_id in ("
							+ ObjectIdUtils.numericIdsToCommaDelimitedString(filterStateIds) + ")";

			}
		}

		return sql;
	}
	
	protected boolean supportsFilter(Class type) {
		return StateIdListFilter.class.equals(type);
	}
}
