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

import com.aplana.dbmi.action.GetCardHistory;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;


public class DoCardHistory extends ActionQueryBase {

	private Log logger = LogFactory.getLog(getClass());
	private Map<String, String> enabledAttrs = new HashMap<String, String>();

	public Object processQuery() throws DataException {
		GetCardHistory action;
		final ObjectId card;
		try {
			action = getAction();
			card = action.getCard();
		} catch (Exception e) {
			return null;
		}

		if (card == null) {
			ArrayList emptyList = new ArrayList();
			return emptyList;
		} else {
			String sql =
					"WITH \n" +
							"h as ( \n" +
								"SELECT row_number() OVER (ORDER BY al.log_date) as key, al.card_id, al.log_date, al.action_code, a.action_name_eng, a.action_name_rus, p.full_name, p.person_id, al.ip_address,  \n" +
													"cv1.status_id AS start_status_id, csb.name_rus AS csb_name_rus, csb.name_eng AS csb_name_eng,  \n" +
													"csf.status_id AS end_status_id, csf.name_rus AS csf_name_rus, csf.name_eng AS csf_name_eng,  \n" +
													"wfm.name_eng AS wfm_name_eng, wfm.name_rus AS wfm_name_rus, p1.full_name as delegate_full_name  \n" +
													", cv1.version_id, cv2.version_id as next_version_id, al.action_log_id as a_log_id  \n" +
												"FROM action_log al  \n" +
												"LEFT JOIN card_version cv1 ON al.card_id=cv1.card_id AND al.action_log_id = cv1.action_log_id  \n" +
												"LEFT JOIN card_version cv2 ON al.card_id=cv2.card_id AND cv2.version_id =  \n" +
														"(  \n" +
															"select version_id \n" +
															"from card_version cv3 \n" +
															// ������� �� action_log, �.�. ������������������ ��������� ����� � card_version �� ������������� ����������������.
															// ������� �� ���� ��������� ����� ����� ������ action_log.
															"left join action_log al_vers_date on cv3.card_id = al_vers_date.card_id and al_vers_date.action_log_id = cv3.action_log_id \n" +
															"where cv1.card_id = cv3.card_id \n" +
																"and al_vers_date.log_date > al.log_date order by al_vers_date.log_date asc limit 1 -- �.�. ������� ������, �� ������ �������  \n" +
														")  \n" +
												"JOIN person p ON p.person_id=al.actor_id \n" +
												"LEFT JOIN person p1 ON p1.person_id=al.delegate_user_id  \n" +
												"JOIN action a ON a.action_code=al.action_code  \n" +
												"JOIN card c ON c.card_id=al.card_id	 \n" +
												"LEFT JOIN card_status csb ON csb.status_id=cv1.status_id  \n" +
												"LEFT JOIN card_status csf ON csf.status_id=coalesce(cv2.status_id, c.status_id)  \n" +
												"JOIN template t ON t.template_id=c.template_id  \n" +
												"LEFT JOIN workflow_move wfm ON wfm.workflow_id=t.workflow_id  \n" +
														"AND wfm.from_status_id=cv1.status_id AND wfm.to_status_id=csf.status_id  \n" +
														"AND (coalesce(wfm.name_rus, '')<>'')  \n" +
												"WHERE al.card_id=? AND al.action_code IN ('NEW_CARD', 'GET_CARD', 'CHG_CARD', 'CHG_STATUS', 'REMOVE_FILE') \n" +
												"order by al.log_date \n" +
							"), \n" +
							// ��������� ������� ��� ������� � ������� GET_CARD
							"history_sys_get as ( \n" +
								"select * from h where h.person_id =0 and h.action_code = 'GET_CARD' \n" +
							"), \n" +
							// �������� �� ����� ������� ������� � ������� GET_CARD
							"history_without_sys_get as ( \n" +
								"select h.* from h \n" +
								"left join history_sys_get hsg on h.key = hsg.key \n" +
								"where hsg.key is null \n" +
							") \n" +
							"SELECT hwsg.key, hwsg.log_date, hwsg.action_code, hwsg.version_id, hwsg.next_version_id, hwsg.action_name_eng, hwsg.action_name_rus, hwsg.full_name, hwsg.person_id, hwsg.ip_address,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.start_status_id  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS start_status_id,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.csb_name_rus  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS csb_name_rus,  \n" +
											"case   \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.csb_name_eng  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS csb_name_eng,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.end_status_id  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS end_status_id,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.csf_name_rus  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS csf_name_rus,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then hwsg.csf_name_eng  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS csf_name_eng,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then coalesce(hwsg.wfm_name_eng, cs.default_move_name_eng)  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS wfm_name_eng,  \n" +
											"case  \n" +
												"when hwsg.action_code = 'CHG_STATUS' then coalesce(hwsg.wfm_name_rus, cs.default_move_name_rus)  \n" +
												"when hwsg.action_code != 'CHG_STATUS' then null  \n" +
											"end AS wfm_name_rus,  \n" +
											"hwsg.delegate_full_name  \n" +
											"FROM history_without_sys_get as hwsg \n" +
											"JOIN card_status cs ON cs.status_id=hwsg.end_status_id  \n" +
											"order by hwsg.log_date desc limit " + action.getLimit() + " offset " + action.getOffset();
			List<?> result = getJdbcTemplate().query(sql,
					new Object[] { card.getId() },
					new int[] { Types.NUMERIC },
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							CardHistoryRecord rec = new CardHistoryRecord();
							// (YNikitin, 2013/05/16) �.�. ���� ������ ������������ � �� � UTC-�������, �� ��� ��������� � �� �� ���� ����������� � ������� ������� ����
							Date date = rs.getTimestamp("log_date");
							if (date!=null){
								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(date.getTime());
								final int millisTZO = calendar.get( Calendar.ZONE_OFFSET) + calendar.get( Calendar.DST_OFFSET);
								date = new java.sql.Date(calendar.getTimeInMillis() + millisTZO);
								rec.setDate(date);
							} else {
								rec.setDate(null);
							}
							rec.setActionId(rs.getString("action_code"));
							rec.setVersionId(rs.getString("version_id"));
							rec.setActionNameEn(rs.getString("action_name_eng"));
							rec.setActionNameRu(rs.getString("action_name_rus"));
							rec.setActorFullName(rs.getString("full_name"));
							rec.setIpAddress(rs.getString("ip_address"));
							rec.setStartStatusId(rs.getObject("start_status_id") == null ? null :
												new ObjectId(CardState.class, rs.getLong("start_status_id")));
							rec.setStartStatusNameRu(rs.getString("csb_name_rus"));
							rec.setStartStatusNameEn(rs.getString("csb_name_eng"));
							rec.setEndStatusId(rs.getObject("end_status_id") == null ? null :
												new ObjectId(CardState.class, rs.getLong("end_status_id")));
							rec.setEndStatusNameRu(rs.getString("csf_name_rus"));
							rec.setEndStatusNameEn(rs.getString("csf_name_eng"));
							rec.setWfmNameEn(rs.getString("wfm_name_eng"));
							rec.setWfmNameRu(rs.getString("wfm_name_rus"));
							String curVers = rs.getString("version_id");
							String nextVers = rs.getString("next_version_id");

							try {
								if (curVers == null || "".equals(curVers)) {
									rec.setVisibleAttr("");
								} else {
									rec.setVisibleAttr(getVisibleAttribute(card, curVers, nextVers));
								}
							} catch (DataException e) {
								logger.error(String.format("Improper formation of the list of visible attributes. CardId: %s, vers: %s, nextVers: %s", card.getId(), curVers, nextVers), e);
							}
							return rec;
						}
					});
			enabledAttrs.clear();
			return result;
		}
	}

	private String setToString(Set<String> set) {
		if (set == null || set.isEmpty()) {
			return "''";
		}
		StringBuilder buffer = new StringBuilder();
		int i = 0;
		for (String value : set) {
			if (i++ == 0) {
				buffer.append("'");
			} else {
				buffer.append("', '");
			}
			buffer.append(value);
		}
		buffer.append("'");
		return buffer.toString();
	}

	private String getVisibleAttribute(ObjectId cardId, String curVersion, String nextVersion) throws DataException {
		HashSet<AttributeViewParam> attributeViewParams = new HashSet<AttributeViewParam>();

		String mapKey = cardId.getId()+curVersion+nextVersion;
		String calculatedAVP = enabledAttrs.get(mapKey);
		if (calculatedAVP != null) {
			return calculatedAVP;
		}

		final ChildrenQueryBase viewQuery = getQueryFactory().getChildrenQuery(Card.class, AttributeViewParam.class);
		viewQuery.setParent(cardId);
		final Collection<AttributeViewParam> attrViewParams = getDatabase().executeQuery(getUser(), viewQuery);
		if (null != attrViewParams && !attrViewParams.isEmpty())
			attributeViewParams.addAll(attrViewParams);

		if (null != curVersion && !curVersion.isEmpty()) {
			final ChildrenQueryBase viewQueryCurVers = getQueryFactory().getChildrenQuery(CardVersion.class, AttributeViewParam.class);
			viewQueryCurVers.setParent(new ObjectId(CardVersion.class, new CardVersion.CompositeId((Long) cardId.getId(), Integer.parseInt(curVersion))));
			final Collection<AttributeViewParam> attrViewParamsCurVers = getDatabase().executeQuery(getUser(), viewQueryCurVers);
			attributeViewParams.addAll(attrViewParamsCurVers);
		}

		if (null != nextVersion && !nextVersion.isEmpty()) {
			final ChildrenQueryBase viewQueryNextVers = getQueryFactory().getChildrenQuery(CardVersion.class, AttributeViewParam.class);
			viewQueryNextVers.setParent(new ObjectId(CardVersion.class, new CardVersion.CompositeId((Long) cardId.getId(), Integer.parseInt(nextVersion))));
			final Collection<AttributeViewParam> attrViewParamsNextVers = getDatabase().executeQuery(getUser(), viewQueryNextVers);
			attributeViewParams.addAll(attrViewParamsNextVers);
		}

		HashSet<String> attrCodeVisible = new HashSet<String>();
		for(AttributeViewParam attributeViewParam : attributeViewParams) {
			if (!attributeViewParam.isHidden())
				attrCodeVisible.add(attributeViewParam.getId().getId().toString());
		}
		String listVisibleAttr = setToString(attrCodeVisible);
		enabledAttrs.put(mapKey, listVisibleAttr);
		return listVisibleAttr;
	}
}