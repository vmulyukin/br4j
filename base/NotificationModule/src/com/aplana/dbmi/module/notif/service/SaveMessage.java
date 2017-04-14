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

import java.util.Date;

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

public class SaveMessage extends SaveQueryBase {

	@Override
	public void validate() throws DataException {
		Message message = (Message) getObject();
		if (message.getRecipient() == null || message.getRecipient().getId() == null)
			throw new DataException("store.message.norecipient");
	}

	@Override
	protected ObjectId processNew() throws DataException {
		Message message = (Message) getObject();
		if (message.getSender() == null || !Person.ID_SYSTEM.equals(message.getSender().getId())) {
			message.setSender(getUser().getPerson());
		}
		if (message.getSendTime() == null || !Person.ID_SYSTEM.equals(message.getSender().getId())) {
			message.setSendTime(new Date());
		}
		
		long id = generateId("seq_message_id");
		getJdbcTemplate().update(
				"INSERT INTO message " +
					"(message_id, send_time, read_time, " +
					"sender_person_id, recipient_person_id, message_text, group_id, eventCard_id) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { id, message.getSendTime(), null,
						message.getSender().getId().getId(), message.getRecipient().getId().getId(),
						message.getText(), message.getGroup().getId().getId(),
						message.getMessageEventCardId() != null ? message.getMessageEventCardId().getId() : null});
		return new ObjectId(Message.class, id);
	}

	@Override
	protected void processUpdate() throws DataException {
		throw new DataException("store.message.update");
	}

}
