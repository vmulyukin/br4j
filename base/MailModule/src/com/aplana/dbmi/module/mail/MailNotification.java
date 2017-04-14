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
package com.aplana.dbmi.module.mail;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.module.notif.AttachmentsSource;
import com.aplana.dbmi.module.notif.NotificationDevice;
import com.aplana.dbmi.module.notif.NotificationObject;
import com.aplana.dbmi.util.StringsManager;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class MailNotification implements NotificationDevice {
	
	public static final String VAR_CONFIG = "config";
	public static final String VAR_USER = "user";
	public static final String VAR_ADDRESS = "address";

	protected Log logger = LogFactory.getLog(getClass());
	private Configuration configuration;
	private Mailer mailer;
	private StringsManager strings;
	
	//private String sender;
	private String subjectKey;
	private String templateName;
	private String error;
	
	private Collection attachments;
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}
	
	public void setStrings(StringsManager strings) {
		this.strings = strings;
	}

	public void setSubjectKey(String key) {
		this.subjectKey = key;
	}

	/*public void setSender(String sender) {
		this.sender = sender;
	}*/

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public void setAttachments(Collection attachments) {
		this.attachments = attachments;
	}

	public String getError() {
		return error;
	}

	public boolean sendNotification(Person recipient, NotificationObject object) {
		Map model = object.getModel();
		model.put(VAR_USER, recipient);
		model.put(VAR_ADDRESS, recipient.getEmail());
		return sendNotification(model, getUserLocale(recipient), object);
	}

	public boolean sendNotification(String recipient, NotificationObject object) {
		Map model = object.getModel();
		model.put(VAR_ADDRESS, recipient);
		return sendNotification(model, ContextProvider.LOCALE_RUS, object);
	}
	
	private boolean sendNotification(final Map model, Locale locale, final NotificationObject object) {
		String message = null, subject = null;
		try {
			Template bodyTemplate = configuration.getTemplate(templateName, locale, "UTF-8");
			StringWriter writer = new StringWriter();
			bodyTemplate.process(model, writer);
			message = writer.toString();
			
			Template subjectTemplate = new Template("_subject",
					new StringReader(strings.getLocaleString(locale, subjectKey)),
					configuration);
			writer = new StringWriter();
			subjectTemplate.process(model, writer);
			subject = writer.toString();

			final String threadMessage = message;
			final String threadSubject = subject;

			new Thread() {
				public void run() {
					if (attachments == null || attachments.size() == 0) {
						mailer.sendMail(threadMessage, (String) model.get(VAR_ADDRESS), threadSubject);
					} else {
						mailer.sendMail(threadMessage, (String) model.get(VAR_ADDRESS), threadSubject, getAttachmentData(object));
					}
				}
			}.start();

		} catch (Exception e) {
			logger.error("Error creating notification message", e);
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

	private Locale getUserLocale(Person person) {
		//TODO: get locale from user's preferences
		Locale locale = ContextProvider.LOCALE_RUS;
		return locale;
	}
	
	private Collection getAttachmentData(NotificationObject object) {
		ArrayList data = new ArrayList();
		for (Iterator itr = attachments.iterator(); itr.hasNext(); ) {
			AttachmentsSource source = (AttachmentsSource) itr.next();
			data.addAll(source.getAttachments(object));
		}
		return data;
	}

}
