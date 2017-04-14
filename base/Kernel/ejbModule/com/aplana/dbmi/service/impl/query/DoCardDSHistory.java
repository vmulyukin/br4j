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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetCardDSHistory;
import com.aplana.dbmi.action.GetCardHistory;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardHistoryRecord;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoCardDSHistory extends ActionQueryBase {
	
	private static final ObjectId ATTR_DOC_ATTACH = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final String QUERY = 
		"SELECT al.log_date, al.action_code, a.action_name_eng, a.action_name_rus, p.full_name, al.ip_address, \n" + 
				"null, null, null, null, null, null, null, null \n" + 
		"FROM action_log al \n" + 
		"JOIN person p ON p.person_id = al.actor_id \n" + 
		"JOIN action a ON a.action_code = al.action_code \n" + 
		"WHERE (al.action_code = 'SIGN_CARD' AND al.card_id = ?) OR \n" + 
				"(al.action_code = 'SIGN_ATTACHMENT' AND al.card_id IN (SELECT card_id FROM card WHERE parent_card_id = ?)) OR \n" +
				"(al.action_code = 'GET_FILE_WITH_DS' AND al.card_id = ?)" + 
		"ORDER BY al.log_date";
	
	public Object processQuery() throws DataException {
		GetCardDSHistory action;
		ObjectId card;
		try {
			action = (GetCardDSHistory) getAction();
			card = action.getCard();
		} catch (Exception e) {
			return null;
		}
		
		if(card == null) {
			ArrayList emptyList = new ArrayList(); 
			return emptyList;
		}
		
			
			
		List result = getJdbcTemplate().query(QUERY, 
				new Object[] {card.getId(), card.getId(), card.getId()},
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						CardHistoryRecord rec = new CardHistoryRecord();
						// (YNikitin, 2013/05/16) �.�. ���� ������ ������������ � �� � UTC-�������, �� ��� ��������� � �� �� ���� ����������� � ������� ������� ����
						final Date date = rs.getTimestamp(1);
						if (date!=null){
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							final int millisTZO = calendar.get( Calendar.ZONE_OFFSET) + calendar.get( Calendar.DST_OFFSET);
							rec.setDate(new java.sql.Date(calendar.getTimeInMillis() + millisTZO));
						} else 
							rec.setDate(null);
						rec.setActionId(rs.getString(2));
						rec.setActionNameEn(rs.getString(3));
						rec.setActionNameRu(rs.getString(4));
						rec.setActorFullName(rs.getString(5));
						rec.setIpAddress(rs.getString(6));
						rec.setStartStatusId(rs.getObject(7)==null ? null :
											new ObjectId(CardState.class, rs.getLong(7)));
						rec.setStartStatusNameRu(rs.getString(8));
						rec.setStartStatusNameEn(rs.getString(9));
						rec.setEndStatusId(rs.getObject(10)==null ? null :
											new ObjectId(CardState.class, rs.getLong(10)));
						rec.setEndStatusNameRu(rs.getString(11));
						rec.setEndStatusNameEn(rs.getString(12));
						rec.setWfmNameEn(rs.getString(13));
						rec.setWfmNameRu(rs.getString(14));
						return rec;
					}
				}
		);
		
		return result;
	}

}
