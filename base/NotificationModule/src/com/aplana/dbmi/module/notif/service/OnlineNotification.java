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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.MessageGroup;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.module.notif.NotificationDevice;
import com.aplana.dbmi.module.notif.NotificationObject;
import com.aplana.dbmi.module.notif.SingleCardNotification;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.util.StringsManager;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class OnlineNotification implements NotificationDevice {

	protected Log logger = LogFactory.getLog(getClass());
	private Configuration configuration;
	private MessageManager manager;
	private Person sender;
	private String templateName;
	private int groupId;
	
	public static final String VAR_USER = "user";
	
	public void setManager(MessageManager manager) {
		this.manager = manager;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}


	public boolean sendNotification(Person recipient, NotificationObject object) {
		if(sender == null){
			sender = (Person) Person.createFromId(new ObjectId(Person.class, 0));
		}
		Message message = new Message();
		message.setRecipient(recipient);
		message.setSender(sender);
		message.setSendTime(new Date());
		message.setGroup(new MessageGroup().setId(groupId));
		if(object instanceof SingleCardNotification){
			SingleCardNotification singleCardNotification = (SingleCardNotification) object;
			Card eventCard = (Card) singleCardNotification.getModel().get(SingleCardNotification.VAR_CARD);
			message.setMessageEventCardId(eventCard.getId());
		}
		try {
			Template bodyTemplate = configuration.getTemplate(templateName, ContextProvider.LOCALE_RUS, "UTF-8");
			StringWriter writer = new StringWriter();
			bodyTemplate.process(object.getModel(), writer);
			message.setText(writer.toString().replace("\n", "").replace("\r", "").replace("\t", ""));
		} catch (Exception e) {
			logger.error("Error creating notification message for " + recipient.getFullName(), e);
			return false;
		}
		try {
			manager.putMessage(message);
		} catch (DataException e) {
			logger.error("Error sending notification message to " + recipient.getFullName(), e);
			return false;
		}
		return true;
	}
	
	public int sendNotification(Collection<Person> recipients, NotificationObject object) {
		int send = 0;
		for(Person p: recipients){
			if(sendNotification(p,object)){
				send ++;
			} else {
				logger.error("Unable to sent message throw " + this.getClass() + " to " + p.getId() );
			}
		}
		return send;
	}

	public boolean sendNotification(String recipient, NotificationObject object) {
		throw new NotImplementedException("Online notifications can be sent to internal persons only");
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
}
