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
package com.aplana.dbmi.replication.query;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.replication.action.GetReplicationHistoryForCard;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Экшен для получения ид карточки истории репликации самой последней для данной карточки репликации.
 * @author ppolushkin
 * @since 25.09.2014
 *
 */
public class DoGetReplicationHistoryForCard extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		GetReplicationHistoryForCard action = getAction();
		
		ObjectId cardId = action.getCardId();
		
		if(cardId == null)
			throw new DataException("Card id cannot be null");
		
		if(!Card.class.isAssignableFrom(cardId.getType()))
			throw new DataException("Replication history exists for card only");
		
		StringBuilder sql = new StringBuilder();
		sql.append("select av_rep_hist_create.card_id \n");
		sql.append("from attribute_value av_rep_hist \n");
		sql.append("join attribute_value av_rep_hist_create \n");
		sql.append("	on av_rep_hist.number_value = av_rep_hist_create.card_id \n");
		sql.append("		and av_rep_hist_create.attribute_code = 'CREATED' \n");
		sql.append("where av_rep_hist.card_id = ? \n");
		sql.append("	and av_rep_hist.attribute_code = 'REPLIC_HIST_ATTR' \n");
		sql.append("order by av_rep_hist_create.date_value desc \n");
		sql.append("limit 1 ");
		
		List<?> historyId 
			= getJdbcTemplate().query(
				sql.toString(), 
				new Object[] {cardId.getId()}, 
				new int[] {Types.NUMERIC},
				new RowMapper() {
					
					@Override
					public Long mapRow(ResultSet rs, int row) throws SQLException {
						return rs.getLong(1);
					}
				});
		
		if(!historyId.isEmpty()) {
			return new ObjectId(Card.class, historyId.get(0));
		}
		return null;
	}
}