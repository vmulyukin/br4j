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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.async.QueriesQueue;
import com.aplana.dbmi.service.impl.async.QueryContainer;
import com.aplana.dbmi.service.impl.locks.LockManagement;

/**
 * Decorator for DatabaseBean which adds additional behavior to {@link Database}
 * implementations.
 * 
 */
public abstract class DatabaseBeanDecorator implements BeanFactoryAware, DatabaseEx {
	protected static BeanFactory factory;
	protected static Log logger;
	protected Database db;
	protected QueriesQueue<QueryContainer> queue;
	protected LockManagement lockManagement;
	protected ActiveQueryBases activeQueryBases;

	DatabaseBeanDecorator(Database db) {
		this.db = db;
		logger = LogFactory.getLog(this.getClass());
	}

	@Override
	public Person resolveUser(String name) throws DataException {
		return db.resolveUser(name);
	}

	@Override
	public boolean checkAccess(UserData user, AccessCheckerBase accessChecker)
			throws DataException {
		return db.checkAccess(user, accessChecker);
	}

	@Override
	public void syncUser(PortalUser person) throws DataException {
		db.syncUser(person);
	}

	@Override
	public void clearUsers(Date threshold) throws DataException {
		db.clearUsers(threshold);
	}

	@Override
	public void validate(UserData user, QueryBase query) throws DataException {
		db.validate(user, query);
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (factory == null)
			factory = beanFactory;
	}

	/**
	 * Spring IoC setter
	 * 
	 * @param queue for async work
	 */
	public void setQueue(QueriesQueue<QueryContainer> queue) {
		if (this.queue == null)
			this.queue = queue;
	}
	
	/**
	 * SPring IoC setter for container of currently running query bases
	 * @param aqb
	 */
	public void setActiveQueryBases(ActiveQueryBases aqb) {
		this.activeQueryBases = aqb;
	}

	/**
	 * Sets used {@link LockManagement}
	 * 
	 * @param lockManagement
	 */
	public void setLockManagement(LockManagement lockManagement) {
		this.lockManagement = lockManagement;
	}
	
	@Override
	public Future<Object> getResult(long ticketId) {
		return queue.getResultStorage().pull(ticketId);
	}
	
	/**
	 * Sets BeanFactory for restored processors
	 * 
	 * @param processors
	 */
	protected void setBeanFactoryForBackup(List<ProcessorBase> processors) {
		for (ProcessorBase pr : processors) {
			if (pr.getBeanFactory() == null)
				pr.setBeanFactory(factory);
		}
	}
}
