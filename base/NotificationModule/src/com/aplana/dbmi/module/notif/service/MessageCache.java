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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;

public class MessageCache {
	private static MessageCache singleton;
	private MessageCache() { };
	
	synchronized public static MessageCache getInstance() {
		/*if (singleton == null)
			singleton = new MessageCache();
		return singleton;*/
		return new MessageCache();
	}
	
	public static final long INACTIVITY_TIMEOUT = 10L * 60 * 1000;
	
	private class UserMessages {
		Date lastAccessed = new Date();
		Date lastUpdated = new Date();
		ArrayList<Message> queue = new ArrayList<Message>();
		
		void addMessage(Message message) {
			queue.add(message);
			lastUpdated = new Date();
		}
		
		List<Message> getMessages(Date startAfter) {
			ArrayList<Message> result = new ArrayList<Message>(queue.size());
			for (Iterator<Message> itr = queue.iterator(); itr.hasNext(); ) {
				Message msg = itr.next();
				if (startAfter == null || msg.getSendTime().after(startAfter))
					result.add(msg);
			}
			lastAccessed = new Date();
			return result;
		}
		
		void removeMessage(Message message) {
			for (Iterator<Message> itr = queue.iterator(); itr.hasNext(); ) {
				Message msg = itr.next();
				if (msg.getId().equals(message.getId())) {
					itr.remove();
					lastUpdated = new Date();
					return;
				}
			}
		}
	}
	
	private HashMap<ObjectId, UserMessages> queues = new HashMap<ObjectId, UserMessages>();
	
	synchronized public void registerUser(ObjectId user) {
		if (!isUserRegistered(user))
			queues.put(user, new UserMessages());
	}
	
	synchronized public void unregisterUser(ObjectId user) {
		queues.remove(user);
	}
	
	public boolean isUserRegistered(ObjectId user) {
		return queues.containsKey(user);
	}
	
	synchronized public int purgeInactiveUsers() {
		int count = 0;
		Date threshold = new Date(System.currentTimeMillis() - INACTIVITY_TIMEOUT);
		for (Iterator<UserMessages> itr = queues.values().iterator(); itr.hasNext(); ) {
			UserMessages queue = itr.next();
			if (queue.lastAccessed.before(threshold)) {
				itr.remove();
				count++;
			}
		}
		return count;
	}
	
	synchronized public void putMessage(Message message) {
		ObjectId user = message.getRecipient().getId();
		if (!isUserRegistered(user))
			throw new IllegalStateException("Can't hold message for unregistered user");
		queues.get(user).addMessage(message);
	}
	
	public List<Message> getMessages(ObjectId user, Date startAfter) {
		if (!isUserRegistered(user))
			throw new IllegalArgumentException("Can't list messages for unregistered user");
		return queues.get(user).getMessages(startAfter);
	}
	
	synchronized public void removeMessage(Message message) {
		ObjectId user = message.getRecipient().getId();
		if (!isUserRegistered(user))
			throw new IllegalStateException("Can't remove message for unregistered user");
		queues.get(user).removeMessage(message);
	}
}
