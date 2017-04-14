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
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetCardListBySql;
import com.aplana.dbmi.action.UpdateCard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoUpdateCard extends ActionQueryBase implements WriteQuery {

	@Override
	public Object processQuery() throws DataException {
		UpdateCard updateCard = (UpdateCard)getAction();
		final String sql = updateCard.getSql(); 
		final Long cardId = updateCard.getCardId(); 
		if (sql==null||sql.isEmpty()){
			logger.info("SQL is empty. Return 0.");
			return new Long(0);
		}

		int updateCount = getJdbcTemplate().update(sql
			,
			new Object[] {
					cardId,},
			new int[] {
					Types.NUMERIC}
			);
		logger.debug("There are "+updateCount+" attributes in card "+cardId+" by sql:/n"+sql);
		return new Long(updateCount);	
	}

}
