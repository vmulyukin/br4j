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
package com.aplana.dbmi.module.notif;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Person;

public class NotificationBean {
	public static final String VAR_CONFIG = "config";
	public static final String RECIPIENT_KEY = "recipient";

	protected Log logger = LogFactory.getLog(getClass());

	private Collection<NotificationDevice> devices;
	private Collection<? extends RecipientGroup> recipients;
	private NotificationObject object;
	private boolean isEmailUnique;

	private String error;

	public void setDevices(Collection<NotificationDevice> devices) {
		this.devices = devices;
	}
	
	public void setDevice(NotificationDevice device) {
		this.devices = Collections.singleton(device);
	}

	public void setRecipients(Collection<? extends RecipientGroup> recipients) {
		this.recipients = recipients;
	}

	/*public void setAttachments(Collection attachments) {
		this.attachments = attachments;
	}*/

	public void setObject(NotificationObject object) {
		this.object = object;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isEmailUnique() {
		return this.isEmailUnique;
	}

	public void setEmailUnique(boolean isEmailUnique) {
		this.isEmailUnique = isEmailUnique;
	}

	public int sendNotifications() {
		this.error = null;
		int sent = 0;
		logger.debug("There are " + recipients.size() + " recipient groups");
		ResourceBundle bundle = ResourceBundle.getBundle("notification", ContextProvider.getContext().getLocale());
		Set<Person> persons = new HashSet<Person>();
		for (Iterator<? extends RecipientGroup> itr = recipients.iterator(); itr.hasNext(); ) {
			RecipientGroup group = itr.next();
			persons.addAll(group.discloseRecipients(object));
			logger.debug("There are " + recipients.size() + " discloseRecipients in group " + group);
		}
		
		for (Iterator<NotificationDevice> deviceItr = devices.iterator(); deviceItr.hasNext(); ) {
			NotificationDevice device = deviceItr.next();
			sent = device.sendNotification(persons, object);
		}
		if (error == null && sent == 0) {
			this.error = bundle.getString("ERROR_CREATE_NOTIFICATION");
		}
		return sent;
	}
}
