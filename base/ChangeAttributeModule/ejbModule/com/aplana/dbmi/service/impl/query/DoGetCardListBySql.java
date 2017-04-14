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
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetCardListBySql;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetCardListBySql extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		GetCardListBySql getCardListBySql = (GetCardListBySql)getAction();
		final String sql = getCardListBySql.getSql(); 
		if (sql==null||sql.isEmpty()){
			logger.info("SQL is empty. Return 0.");
			return null;
		}

		List cardList = getJdbcTemplate().query(sql, new RowMapper() {
			
			public Object mapRow(final ResultSet rs, final int arg1) throws SQLException {
				return rs.getLong(1);
			}
		});
		logger.debug("There are "+cardList.size()+" cars for sql:/n"+sql);
		return cardList;	
	}

}
