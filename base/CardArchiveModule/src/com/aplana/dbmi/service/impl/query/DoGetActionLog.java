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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.action.GetActionLog;
import com.aplana.dbmi.archive.export.ActionLogHist;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ��������� �������� ������ �� ������� action_log
 * ��� ���������� ��������
 * @author ppolushkin
 * 
 * ���� ignoreGetGard ��������� ������������ �������� GET_CARD
 * � ������� action_log. �� ��������� true, �.�. � GET_CARD
 * ������ ������������ ����������� �����
 *
 */
public class DoGetActionLog extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	
	protected static Log logger = LogFactory.getLog(DoGetActionLog.class);

	@Override
	public Object processQuery() throws DataException {
		
		final GetActionLog action = (GetActionLog) getAction();
		
		final List<ActionLogHist> result = new ArrayList<ActionLogHist>();
		
		if(action != null && action.isCardId()) {
			
			StringBuilder sql = new StringBuilder();
			sql.append("select action_code, log_date, actor_id, ip_address, card_id, \n");
			sql.append("	   template_id, block_code, attribute_code, person_id, delegate_user_id, action_log_id \n");
			sql.append("from action_log \n");
			sql.append("where card_id = {0} \n");
			if(action.isIgnoreGetGard()) {
				logger.info("fetch by GET_CARD is disable");
				sql.append("and action_code in (select action_code from action where action_code <> ''GET_CARD'') \n");
			} else {
				logger.info("fetch by GET_CARD is enable");
			}
			sql.append("order by card_id, log_date ");
			
			getJdbcTemplate().query(
					MessageFormat.format(sql.toString(),
							String.valueOf(action.getCardId().getId())),
					new RowCallbackHandler() {
						public void processRow(ResultSet rs) throws SQLException {
							String actionCode = rs.getString(1);
							if(actionCode == null) {
								logger.warn("ACTION_CODE is null");
								return;
							}
							ActionLogHist al = new ActionLogHist();
							al.setActionCode(actionCode);
							Date d = rs.getTimestamp(2);
							al.setLogDate(d != null ? d : null);
							Long l = rs.getLong(3);
							al.setActorId(!rs.wasNull() ? new ObjectId(Person.class, l) : null);
							String str = rs.getString(4);
							al.setIpAddress(str != null ? str : null);
							l = rs.getLong(5);
							al.setCardId(!rs.wasNull() ? new ObjectId(Card.class, l) : null);
							l = rs.getLong(6);
							al.setTemplateId(!rs.wasNull() ? new ObjectId(Template.class, l) : null);
							str = rs.getString(7);
							al.setBlockCode(str != null ? new ObjectId(AttributeBlock.class, str) : null);
							str = rs.getString(8);
							al.setAttributeCode(str != null ? new ObjectId(Attribute.class, str) : null);
							l = rs.getLong(9);
							al.setPersonId(!rs.wasNull() ? new ObjectId(Person.class, l) : null);
							l = rs.getLong(10);
							al.setDelegateUserId(!rs.wasNull() ? new ObjectId(Person.class, l) : null);
							l = rs.getLong(11);
							al.setActionLogId(l);
							result.add(al);
						}
					}
				);
			
		} else {
			logger.warn("Action is not usable");
		}
		
		return result;
	}

}
