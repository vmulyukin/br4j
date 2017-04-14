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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;

/**
 * Remote interface for Enterprise Bean: DataService.<br>
 * DataService is an EJB performing almost all business-logic in DBMI system.<br>
 * Every process performed by DataService could be described as processing of
 * some action. Action is an object representing request and encapsulating all information,
 * required to perform this request. Every action object must implement
 * {@link com.aplana.dbmi.action.Action} interface.<br>
 * So most low-level method of DataService is {@link #doAction(User, Action)} which 
 * simply performs given action.
 * <br>
 * There is also several additional methods added to simplify routine tasks 
 * such as object fetching ({@link #listAll(User, Class)}, 
 * {@link #listChildren(User, ObjectId, Class)}, {@link #getById(User, ObjectId)}),
 * {@link #filter(User, Class, Filter)} and CRUD operations 
 * ({@link #saveObject(User, DataObject)}, {@link #deleteObject(User, ObjectId)})
 * <br>
 * Last group of methods is related to user permissions control and security:
 * {@link #authUser(Principal, String)}, {@link #canChange(User, ObjectId)},
 * {@link #canCreate(User, Class)}, {@link #canDo(User, Action)}
 */
public interface DataService extends javax.ejb.EJBObject
{
	/**
	 * Authenticate given user
	 * @param user user principal object
	 * @param address string representation of user's IP-address (for example '127.0.0.1')
	 * @return {@link User} object representing authenticated user 
	 * @throws DataException if authentication failed
	 * @throws RemoteException in case of communication error
	 */
	public User authUser(Principal user, String address) throws DataException, RemoteException;
	
	/**
	 * Returns all objects of given type, available to user
	 * @param user user, who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant 
	 * @return Collection containing all objects of given type, available to user
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T extends DataObject> Collection<T> listAll(User user, Boolean isDelegation, User realUser, Class<T> type, String session) throws DataException, RemoteException;
	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @param filter filter for child records
	 * @return filtered collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String session, Filter filter) throws DataException, RemoteException;
	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @return collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String session) throws DataException, RemoteException;
	
	/**
	 * Returns all objects of given type which satisfies given {@link Filter} and is available to user 
	 * @param user user who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant
	 * @param filter filter object
	 * @return collection containing all objects of given type which satisfies given {@link Filter} and is available to user
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T extends DataObject> Collection<T> filter(User user, Boolean isDelegation, User realUser, Class<T> type, Filter filter, String session) throws DataException, RemoteException;
	/**
	 * Fetches object with given identifier from database and returns it to user
	 * @param user user who perform action
	 * @param id identifier of object to fetch
	 * @return object fetched from database
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T extends DataObject> T getById(User user, Boolean isDelegation, User realUser, ObjectId id, String session) throws DataException, RemoteException;
	/**
	 * Saves given object in database. This method is user for adding new objects
	 * to database and to update already existing objects. Object is considered new
	 * if call for {@link DataObject#getId()} on this object returns null.
	 * @param user user who perform action
	 * @param obj data object to save
	 * @return identifier of saved object
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public ObjectId saveObject(User user, Boolean isDelegation, User realUser, DataObject obj, String sessionId) throws DataException, RemoteException;
	/**
	 * Deletes object with given identifier from database
	 * @param user user who perform action
	 * @param id identifier of object to delete
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public void deleteObject(User user, Boolean isDelegation, User realUser, ObjectId id, String session) throws DataException, RemoteException;
	/**
	 * Performs given action. Should be used for tasks that couldn't be performed
	 * with others methods.
	 * @param user user who perform action
	 * @param action {@link com.aplana.dbmi.action.Action} object containing additional parameters of action to perform
	 * @return result of the action (could be null} 
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public <T> T doAction(User user, Boolean isDelegation, User realUser, Action<T> action, String sessionId) throws DataException, RemoteException;
	
    /**
     * Checks if given user can create objects of given type in database.
     * If user is not allowed to create objects of given type, then call
     * to {@link #saveObject(User, DataObject)} with new object of this type
     * will cause {@link DataException}
     * @param user user who perform check
     * @param type Type of object to create
     * @return true if user is allowed to create objects of given type, false otherwise
     * @throws DataException in case of business-logic error
     * @throws RemoteException in case of communication error
     */
	public boolean canCreate(User user, Class<?> type) throws DataException, RemoteException;
	/**
	 * Checks if given user can modify object with given identifier in database.
	 * If user is not allowed to change this object, then call to {@link #saveObject(User, DataObject)}
	 * for this object will cause {@link DataException}
	 * @param user user who perform check
	 * @param id identifier of existing object in database
	 * @return true if user could modify object with given identifier, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public boolean canChange(User user, ObjectId id, String sessionId) throws DataException, RemoteException;
	/**
	 * Checks if given user can perform given action.
	 * If user is not allowed to perform this action then call
	 * to {@link #doAction(User, Action)} with same object
	 * will cause {@link DataException}
	 * @param user user who perform check
	 * @param action Action to perform
	 * @return true if user can perform given action, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws RemoteException in case of communication error
	 */
	public boolean canDo(User user, Action<?> action) throws DataException, RemoteException;
	
}
