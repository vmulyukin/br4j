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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;

import java.sql.Types;
import java.util.Calendar;
import java.util.Date;

public class LogEventBeanImpl extends JdbcDaoSupport implements LogEventBean {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	public LogEventBeanImpl() {
		logger.info("Create LogEvent.");
	}
	
	protected void initTemplateConfig()	{
		getJdbcTemplate().setNativeJdbcExtractor(new JBossNativeJdbcExtractor());
	}
	
	public void logEventExt(UserData user, LogEntry event) {
		try {
			if (event == null) return;
			logger.debug("Start LogEvent...");
			if (LogEntry.class.equals(event.getClass())) {
				logAction(user, event);
			} else {
				if (event instanceof MessageEvent) {
					logEvent(user, (MessageEvent)event);
				}
			}	
			logger.debug("End LogEvent.");
		} catch(Exception e) {
			logger.error(getClass().getName() + ":logEventExt" + ": caught exception", e);
		}
	}
	
	/**
	 * Retrieve next value from the database sequence with given name.
	 * @param sequence name of sequence
	 * @return next value of sequence
	 */
	private long generateId(String sequence) throws Exception 
	{
		return getJdbcTemplate().queryForLong( "SELECT nextval('" + sequence + "')");
	}
	
	private void logAction (UserData user, LogEntry event)
	{
		try {
			String user_log = event.getUser() == null ? "null": event.getUser().getFullName();
			logger.info("logAction: [" + event.getAddress() + "] Logging event: " + event.getEvent() +
					"; object " + event.getObject() +
						"; user " + user_log);
			Class<?> objType = getClass();		//***** may be any non-DataObject class
			if (event.getObject() != null)
				objType = event.getObject().getType();
				//getJdbcTemplate().queryForList("select card_id from action_log where action_code=?", new Object[] {"NEW_CARD"}, new int[] { Types.VARCHAR});
			Date dt = event.getTimestamp();
			// (YNikitin, 2013/05/16) ���� ������ ������������ � �� � ������� ������� �����, ������ ���������� � UTC-�������
			if (dt != null) {
				//�������� ��������, ���� ����� �� ����������
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(dt.getTime());
				dt = new java.sql.Date(calendar.getTimeInMillis()-calendar.get( Calendar.ZONE_OFFSET) - calendar.get( Calendar.DST_OFFSET));
			}
			Integer idLogEvent = event.getIdLogAction();
			// ���� ����� ������� �� �������������� id - ������� ���.
			if (null == idLogEvent) {
				int action_log_id = getJdbcTemplate().queryForInt(
						"SELECT nextval('seq_action_log_id')");
				idLogEvent = action_log_id;
			}
			
			getJdbcTemplate().update(
					"INSERT INTO action_log (action_log_id, action_code, log_date, actor_id, ip_address, " +
						"card_id, template_id, block_code, attribute_code, person_id, delegate_user_id, uid, parent_uid) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					new Object[] {
						idLogEvent, event.getEvent(), dt, event.getRealUser() != null ? event.getRealUser().getId().getId() : user.getPerson().getId().getId(), user.getAddress(),
						Card.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Template.class.equals(objType) ? event.getObject().getId() : null,
						AttributeBlock.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Attribute.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Person.class.equals(objType) ? event.getObject().getId() : null,
						event.getRealUser() != null ? user.getPerson().getId().getId() : null,
						event.getUid() != null ? event.getUid().getId().toString().replace("-", "") : null,
						event.getParentUid() != null ? event.getParentUid().getId().toString().replace("-", "") : null},
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.TIMESTAMP, Types.NUMERIC, Types.VARCHAR,
							Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, 
							Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
		} catch(Exception e) {
			logger.error(getClass().getName() + ":logAction" + ": caught exception", e);
		}
	}
	
	private void logEvent (UserData user, MessageEvent event) {
		try {
			String user_log = event.getUser() == null ? "null": event.getUser().getFullName();
			logger.info("logEvent: [" + event.getAddress() + "] Logging event: " + event.getEvent() +
					"; object " + event.getObject() +
					"; user " + user_log);
			Class<?> objType = getClass();		//***** may be any non-DataObject class
			if (event.getObject() != null)
				objType = event.getObject().getType();
			final Long id_record = generateId("dbmi_trunk.event_log_sequence");
			getJdbcTemplate().update(
					"INSERT INTO event_log (action_code, log_date, actor_id, ip_address, " +
						"card_id, template_id, block_code, attribute_code, person_id, action_log_id, is_succes,uid,parent_uid) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					new Object[] {
						event.getEvent(), event.getTimestamp(), user.getPerson().getId().getId(), user.getAddress(),
						Card.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Template.class.equals(objType) ? event.getObject().getId() : null,
						AttributeBlock.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Attribute.class.isAssignableFrom(objType) ? event.getObject().getId() : null,
						Person.class.equals(objType) ? event.getObject().getId() : null,
						id_record, event.getSucces(),
						event.getUid() != null ? event.getUid().getId().toString().replace("-", "") : null,
						event.getParentUid() != null ? event.getParentUid().getId().toString().replace("-", "") : null},
					new int[] { Types.VARCHAR, Types.TIMESTAMP, Types.NUMERIC, Types.VARCHAR,
								Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, 
								Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC });
			final Long id_detail = generateId("dbmi_trunk.event_detail_sequence");
			getJdbcTemplate().update("INSERT INTO event_log_detail (event_detail_id, action_log_id, message, description, code_message, type_message) VALUES (?, ?, ?, ?, ?, ?)",
										new Object[] {
											id_detail,
										id_record,
										event.getMessage(),
										event.getDescriptionMessage().length() > 255 
												? event.getDescriptionMessage().substring(0, 255)
												: event.getDescriptionMessage(),
										event.getCodeMessage(),
										event.getMessageType()
										},
										new int[] {
											Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
										});
		} catch(Exception e) {
			logger.error(getClass().getName() + ":logEvent" + ": caught exception", e);
		}
	}
}
