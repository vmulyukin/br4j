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
import java.util.Collection;
import java.util.Iterator;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetCardEventHistory;
import com.aplana.dbmi.model.CardHistoryRecord;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;


public class DoCardEventHistory extends ActionQueryBase {

	public Object processQuery() throws DataException {
		GetCardEventHistory action;
		ObjectId card;
		GetCardEventHistory.FilterEvent filterEvent;
		final boolean isFiltered;
		try {
			action = (GetCardEventHistory)getAction();
			card = action.getCard();
			filterEvent = action.getFilterEvent();
			isFiltered = filterEvent != null && !filterEvent.getEvents().isEmpty();
		} catch (Exception e) {
			return null;
		}
		String sql = 
			"SELECT DISTINCT event.* from \r\n" +
			"(SELECT s.log_date, s.action_code, s.action_name_eng, s.action_name_rus, s.full_name, s.ip_address, \r\n" +
			"s.start_status_id, s.csb_name_rus, s.csb_name_eng,  \r\n" +
			"s.end_status_id, s.csf_name_rus, s.csf_name_eng,  \r\n" +
			"coalesce(s.wfm_name_eng, cs.default_move_name_eng), coalesce(s.wfm_name_rus, cs.default_move_name_rus), s.is_succes, s.version_id, s.action_log_id \r\n" +
			"FROM \r\n" +
			"( \r\n" +
				"SELECT al.log_date, al.action_code, a.action_name_eng, a.action_name_rus, p.full_name, al.ip_address,  \r\n" +
					"cv1.status_id AS start_status_id, csb.name_rus AS csb_name_rus, csb.name_eng AS csb_name_eng,  \r\n" +
					"csf.status_id AS end_status_id,  \r\n" +
							"csf.name_rus AS csf_name_rus, csf.name_eng AS csf_name_eng,  \r\n" +
					"wfm.name_eng AS wfm_name_eng, wfm.name_rus AS wfm_name_rus, al.is_succes, cv1.version_id, al.action_log_id \r\n" +
				"FROM event_log al  \r\n" +
				"JOIN card_version cv1 ON al.card_id=cv1.card_id AND cv1.version_id= \r\n" +
					"(SELECT version_id FROM card_version \r\n" +
						"WHERE card_id=al.card_id AND version_date<al.log_date order by version_date desc limit 1) \r\n" +
				"LEFT JOIN card_version cv2 ON al.card_id=cv2.card_id AND cv2.version_id=cv1.version_id+1 \r\n" +
				"JOIN person p ON p.person_id=al.actor_id \r\n" +
				"JOIN action a ON a.action_code=al.action_code  \r\n" +
				"JOIN card c ON c.card_id=al.card_id \r\n" +
				"LEFT JOIN card_status csb ON csb.status_id=cv1.status_id \r\n" +
				"LEFT JOIN card_status csf ON csf.status_id=coalesce(cv2.status_id, c.status_id) \r\n" +
				"JOIN template t ON t.template_id=c.template_id \r\n" +
				"LEFT JOIN workflow_move wfm ON wfm.workflow_id=t.workflow_id \r\n" +
						"AND wfm.from_status_id=cv1.status_id AND wfm.to_status_id=csf.status_id  \r\n" +
						"AND (coalesce(wfm.name_rus, '')<>'') \r\n" +
				"WHERE al.action_code='CHG_STATUS' AND al.card_id = ? \r\n" +
			") AS s \r\n" +
			"JOIN card_status cs ON cs.status_id=s.end_status_id \r\n" +
			"UNION \r\n" +
				"SELECT al.log_date, al.action_code, a.action_name_eng, a.action_name_rus, p.full_name, al.ip_address, \r\n" +
					"null, null, null, null, null, null, null, null, al.is_succes, cv1.version_id, al.action_log_id \r\n" +
				"FROM event_log al \r\n" +
				"JOIN card_version cv1 ON al.card_id=cv1.card_id AND cv1.version_id= \r\n" +
					"(SELECT version_id FROM card_version \r\n" +
						"WHERE card_id=al.card_id AND version_date<al.log_date order by version_date desc limit 1) \r\n" +
				"JOIN person p ON p.person_id=al.actor_id \r\n" +
				"JOIN action a ON a.action_code=al.action_code  \r\n" +
				"WHERE al.card_id = ? \r\n" +
				"AND al.action_code NOT IN ('CHG_STATUS', 'SIGN_CARD', 'SIGN_ATTACHMENT', 'GET_FILE_WITH_DS') \r\n" +
			"ORDER BY 1)  as event, event_log_detail as eld \r\n" +
			"WHERE event.action_log_id = eld.action_log_id \r\n"; 
			if (isFiltered) {
				sql = sql.concat("AND eld.type_message IN ( " + stringValuesToCommaDelimitedString(filterEvent.getEvents().values()) + " )");
			}
		if(card == null) {
			ArrayList emptyList = new ArrayList(); 
			return emptyList;
		}else 		
			return getJdbcTemplate().query(sql,

				new Object[] {card.getId(), card.getId()},
				new int[] { Types.NUMERIC, Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						CardHistoryRecord rec = new CardHistoryRecord();
						rec.setDate(rs.getTimestamp(1));
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
						rec.isSuccess(rs.getString(15));
						rec.setVersionId(rs.getString(16));
						rec.setRecId(rs.getString(17));
						return rec;
					}
				}
			);
	}
	
	private static String stringValuesToCommaDelimitedString(Collection values) {
		if (values == null || values.isEmpty())
			return "";
		final StringBuffer result = new StringBuffer();
		for ( Iterator i = values.iterator(); i.hasNext(); ) 
		{
			final Object obj = i.next();
			if ((obj == null) || !(obj instanceof String)) continue;
			result.append( "'" + (String)obj + "'");
			if (i.hasNext())
				result.append(',');
		}
		return result.toString();
	}
}
