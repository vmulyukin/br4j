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
package com.aplana.dbmi.service;

import java.security.Principal;
import java.util.Collection;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;

/**
 * Local interface of DataService EJB
 * For detailed description see {@link DataService remote interface}
 */
public interface DataServiceLocal extends javax.ejb.EJBLocalObject
{
	/**
	 * local version of {@link DataService#authUser(Principal, String)}
	 */
	public User authUser(Principal user, String address) throws DataException;

	/**
	 * local version of {@link DataService#listAll(User, Class)}
	 */
	public <T extends DataObject> Collection<T> listAll(User user, Boolean isDelegation, User realUser, Class<T> type, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#listChildren(User, ObjectId, Class, Filter)} 
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String session, Filter filter) throws DataException;
	/**
	 * local version of {@link DataService#listChildren(User, ObjectId, Class)} 
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#filter(User, Class, Filter)}
	 */
	public <T extends DataObject> Collection<T> filter(User user, Boolean isDelegation, User realUser, Class<T> type, Filter filter, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#getById(User, ObjectId)}
	 */
	public <T extends DataObject> T getById(User user, Boolean isDelegation, User realUser, ObjectId id, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#saveObject(User, DataObject)} 
	 */
	public ObjectId saveObject(User user, Boolean isDelegation, User realUser, DataObject obj, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#deleteObject(User, ObjectId)}
	 */
	public void deleteObject(User user, Boolean isDelegation, User realUser, ObjectId id, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#doAction(User, Action)} 
	 */
	public <T> T doAction(User user, Boolean isDelegation, User realUser, Action<T> action, String sessionId) throws DataException;
	
	/**
	 * local version of {@link DataService#canCreate(User, Class)}
	 */
	public boolean canCreate(User user, Class<?> type) throws DataException;
	/**
	 * local version of {@link DataService#canChange(User, ObjectId)}
	 */
	public boolean canChange(User user, ObjectId id, String sessionId) throws DataException;
	/**
	 * local version of {@link DataService#canDo(User, Action)}
	 */
	public boolean canDo(User user, Action<?> action) throws DataException;
}
