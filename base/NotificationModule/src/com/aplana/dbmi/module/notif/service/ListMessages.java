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
package com.aplana.dbmi.module.notif.service;

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.filter.MessageFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.locks.LockManagement;
import com.aplana.dbmi.utils.SimpleDBUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ListMessages extends QueryBase {

	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		MessageFilter filter = (MessageFilter) getFilter();
		LockManagement lockManagement = (LockManagement) getBeanFactory().getBean("lockManagement");
		return getJdbcTemplate().query(
				"SELECT m.message_id, m.send_time, m.read_time, \n" + //1-3
					"m.sender_person_id, ps.full_name, ps.email, m.recipient_person_id, m.message_text, \n" + //4-8
					"mg.group_id, mg.group_name_rus, mg.group_name_eng, mg.group_text, m.eventCard_id \n" + //9-12
				"FROM message m \n" +
				"LEFT JOIN person ps ON m.sender_person_id=ps.person_id \n" +
				"LEFT JOIN message_group mg ON m.group_id=mg.group_id \n" +
				"WHERE m.read_time IS NULL " +
					"AND m.send_time > ? " +
					"AND m.recipient_person_id = ? " +
					"AND m.eventcard_id not in (" + getExcludeCardsOnService(lockManagement) + ")",
				new Object[] {
						filter == null ? new Date(0) : filter.getStartAfter(),
						filter == null ? getUser().getPerson().getId().getId() : filter.getPersonId().getId()
				},
				//new int[] { },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Message message = new Message();
						Person sender = new Person();
						sender.setId(rs.getLong(4));
						sender.setFullName(rs.getString(5));
						sender.setEmail(rs.getString(6));						
						
						MessageGroup group = new MessageGroup();
						group.setId(rs.getLong(9))
							.setGroupNameRus(rs.getString(10))
							.setGroupNameEng(rs.getString(11))
							.setGroupText(rs.getString(12));						
						
						
						message.setId(rs.getLong(1))
							.setSendTime(rs.getTimestamp(2))
							.setReadTime(rs.getTimestamp(3))
							.setSender(sender)
							.setRecipient((Person) Person.createFromId(new ObjectId(Person.class, rs.getLong(7))))
							.setText(rs.getString(8))
							.setGroup(group)
							.setMessageEventCardId( rs.getLong(13)!=0 ? new ObjectId(Card.class, rs.getLong(13)): null);
						return message;
					}
		});
	}

	@Override
	protected boolean supportsFilter(Class type) {
		return MessageFilter.class.equals(type);
	}
	
	public String getExcludeCardsOnService(LockManagement lockManagement){
		StringBuilder result = new StringBuilder("-1");
		if (lockManagement != null) {
			List<ObjectId> ids = lockManagement.getCardIdsOnService();
			String idsQuery = SimpleDBUtils.stringIdentifiersToCommaSeparatedSqlString(ids);
			if (!idsQuery.isEmpty()) {
				result.append(",");
				result.append(idsQuery);
			}
		}
		return result.toString();
	}
}
