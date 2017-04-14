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

import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.aplana.dbmi.action.MarkMessageRead;
import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.MessageFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

public class MessageManager implements InitializingBean {

	protected Log logger = LogFactory.getLog(getClass());
	private Database database;
	private QueryFactory queryFactory;
	private UserData user;
	
	protected Database getDatabase() {
		return database;
	}
	
	public void setDatabase(Database database) {
		this.database = database;
	}
	
	protected QueryFactory getQueryFactory() {
		return queryFactory;
	}
	
	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public void afterPropertiesSet() {
		try {
			syncCache();
			new WatchThread().start();
		} catch (DataException e) {
			logger.error("Error starting online messaging service", e);
		}
	}
	
	private void syncCache() throws DataException {
		/*QueryBase query = getQueryFactory().getListQuery(Message.class);
		query.setFilter(AllUnreadMessagesFilter.INSTANCE);
		Collection<Message> messages = (Collection<Message>) getDatabase().executeQuery(getSystemUser(), query);
		*/
	}

	protected UserData getSystemUser() throws DataException {
		if (user == null) {
			user = new UserData();
			user.setPerson(database.resolveUser(Database.SYSTEM_USER));
			user.setAddress("internal");
		}
		return user;
	}
	
	public void putMessage(Message message) throws DataException {
		SaveQueryBase query = getQueryFactory().getSaveQuery(message);
		query.setObject(message);
		ObjectId id = (ObjectId) getDatabase().executeQuery(getSystemUser(), query);
		message.setId(id);
		
		MessageCache cache = MessageCache.getInstance();
		if (cache.isUserRegistered(message.getRecipient().getId())) {
			cache.putMessage(message);
		}
	}
	
	public List<Message> getMessages(ObjectId user, Date startAfter) throws DataException {
		MessageCache cache = MessageCache.getInstance();
		//if (!cache.isUserRegistered(user)) {
			QueryBase query = getQueryFactory().getListQuery(Message.class);
			query.setFilter(new MessageFilter(user,startAfter));
			Collection<Message> messages = (Collection<Message>) getDatabase().
					executeQuery(getSystemUser(), query);
			
			cache.registerUser(user);
			for (Iterator<Message> itr = messages.iterator(); itr.hasNext(); ) {
				Message msg = itr.next();
				cache.putMessage(msg);
			}
		//}
		
		return cache.getMessages(user, startAfter);
	}
	
	public void markMessageRead(Message message) throws DataException {
		MessageCache cache = MessageCache.getInstance();
		if (cache.isUserRegistered(message.getRecipient().getId())) {
			cache.removeMessage(message);
		}
		
		MarkMessageRead action = new MarkMessageRead();
		action.setMessageId(message.getId());
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		getDatabase().executeQuery(getSystemUser(), query);
	}
	
	private class WatchThread extends Thread {
		
		public static final long WATCH_PERIOD = 60L * 1000;
		public static final long DATABASE_PURGE_PERIOD = 60L * 60 * 1000;
		
		private long lastPurged = 0;

		public WatchThread() {
			super("MessageCacheWatchThread");
		}
		
		@Override
		public void run() {
			while (true) {
				MessageCache cache = MessageCache.getInstance();
				cache.purgeInactiveUsers();
				if (System.currentTimeMillis() > lastPurged + DATABASE_PURGE_PERIOD) {
					lastPurged = System.currentTimeMillis();
				}
				try {
					sleep(WATCH_PERIOD);
				} catch (InterruptedException e) {
					LogFactory.getLog(getClass()).error("Message cache watch thread interrupted unexpectedly", e);
				}
			}
		}
	}
	
}
