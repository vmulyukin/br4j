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
package com.aplana.dbmi.service.client;

import java.util.Collection;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.action.file.ContinuousAction;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * Helper class that allows to perform most used operations with database. User,
 * database and query factory should be set before using
 */
public class DataServiceFacade implements ActionPerformer {

	private Database database;
	private UserData user;
	private QueryFactory queryFactory;

	public DataServiceFacade(){
	}

	public DataServiceFacade(Database database, UserData user, QueryFactory queryFactory) {
		this.database = database;
		this.user = user;
		this.queryFactory = queryFactory;
	}

	public void setUser(String name, String address) throws DataException {
		Person person = database.resolveUser(name);
		this.user = new UserData();
		this.user.setAddress(address);
		this.user.setPerson(person);
	}

	public void setSystemUser() throws DataException {
		setUser(Database.SYSTEM_USER, "internal");
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public UserData getUser() {
		return this.user;
	}

	public Database getDatabase() {
		return this.database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public QueryFactory getQueryFactory() {
		return this.queryFactory;
	}

	public void setQueryFactory(QueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}
	
	public <T> T doAction(Action<T> action) throws DataException {
		if (action == null)
			throw new IllegalArgumentException("Action can't be null");

		if (action instanceof ContinuousAction) {
			try {
				ContinuousAction contAction = (ContinuousAction) action;
				contAction.setService(this);
				if (!contAction.beforeMainAction()) {
					return null;
				}
			} catch (ServiceException ex) {
				throw new IllegalStateException(ex);
			}
		}

		ActionQueryBase actionQuery = getQueryFactory().getActionQuery(action);
		actionQuery.setAction(action);
		actionQuery.setDatabase(getDatabase());
		T result = getDatabase().executeQuery(getUser(), actionQuery);

		if (action instanceof ContinuousAction) {
			try {
				((ContinuousAction) action).afterMainAction(result);
			} catch (ServiceException ex) {
				throw new IllegalStateException(ex);
			}
		}
		return result;
	}
	
	public <T extends DataObject> T getById(ObjectId id) throws DataException {
		if (id == null)
			throw new IllegalArgumentException("Can't get object with null id");

		ObjectQueryBase objQuery = getQueryFactory().getFetchQuery(id.getType());
		objQuery.setId(id);
		objQuery.setDatabase(getDatabase());
		return getDatabase().executeQuery(getUser(), objQuery);
	}
	
	public <T extends DataObject> Collection<T> filter(Class<T> type, Filter filter) throws DataException {
		if (!DataObject.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Type should be descendant of DataObject");
		QueryBase listQuery = getQueryFactory().getListQuery(type);
		listQuery.setFilter(filter);
		listQuery.setDatabase(getDatabase());
		return getDatabase().executeQuery(getUser(), listQuery);
	}

	public ObjectId saveObject(DataObject obj) throws DataException {
		if (obj == null)
			throw new IllegalArgumentException("Object can't be null");
		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(obj);
		saveQuery.setObject(obj);
		saveQuery.setDatabase(getDatabase());
		return getDatabase().executeQuery(getUser(), saveQuery);
	}

}
