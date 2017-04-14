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

import java.util.Collection;
import java.util.Date;

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.MessageFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.User;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.UserData;

public class MessageServiceBean extends DataServiceBean {
		//extends AbstractStatelessSessionBean implements SessionBean {
	
	private static final long serialVersionUID = 1L;
	public static final String MESSAGE_MANAGER_BEAN = "onlineNotificationManager";
	
	public Collection<Message> listMessages(User user, MessageFilter filter) throws DataException {
		Person person = UserData.read(user).getPerson();
		if (filter != null && filter.getPersonId() != null && !person.getId().equals(filter.getPersonId())) {
			if (Person.ID_SYSTEM.equals(filter.getPersonId()))
				person = (Person) Person.createFromId(filter.getPersonId());
			else
				throw new DataException("list.message.otheruser");
		}
		MessageManager manager = (MessageManager) getBeanFactory().getBean(MESSAGE_MANAGER_BEAN);
		return manager.getMessages(person.getId(), filter == null ? new Date(0) : filter.getStartAfter());
	}
	
	public void sendMessage(User user, Message message) throws DataException {
		if (message.getId() != null)
			throw new DataException("store.message.repeat");
		Person person = UserData.read(user).getPerson();
		if (!Person.ID_SYSTEM.equals(person.getId()) || message.getSender() == null) {
			message.setSender(person);
		}
		if (!Person.ID_SYSTEM.equals(person.getId()) || message.getSendTime() == null) {
			message.setSendTime(new Date());
		}
		MessageManager manager = (MessageManager) getBeanFactory().getBean(MESSAGE_MANAGER_BEAN);
		manager.putMessage(message);
	}
	
	public void markRead(User user, ObjectId messageId) throws DataException {
		Person person = UserData.read(user).getPerson();
		Message msg = (Message) Message.createFromId(messageId);
		msg.setRecipient(person);
		MessageManager manager = (MessageManager) getBeanFactory().getBean(MESSAGE_MANAGER_BEAN);
		manager.markMessageRead(msg);
	}
	
}
