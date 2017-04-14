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

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.concurrent.Future;

/**
 * Asynchronous Bean implementation class for Enterprise Bean: DataService
 * DataService is an EJB performing almost all business-logic processing in DBMI system.<br>
 * Every process performed by DataService could be described as processing of
 * some action. Action is an object representing request and encapsulating all information,
 * required to perform this request. Every action object must implement
 * {@link com.aplana.dbmi.action.Action} interface.<br>
 * So most low-level method of DataService is {@link #doAction(User, Action)} which 
 * simply perform given action.
 * <br>
 * There is also several additional methods added to simplify routine tasks 
 * such as object fetching ({@link #listAll(User, Class)}, 
 * {@link #listChildren(User, ObjectId, Class)}, {@link #getById(User, ObjectId)}),
 * {@link #filter(User, Class, Filter)} and CRUD operations 
 * ({@link #saveObject(User, DataObject)}, {@link #deleteObject(User, ObjectId)})
 * <br>
 * Last group of methods is related to user permissions control and security:
 * {@link #authUser(Principal, String)}, @link {@link #canChange(User, ObjectId)},
 * {@link #canCreate(User, Class)}, {@link #canDo(User, Action)}
 */
public class AsyncDataServiceBean extends AbstractStatelessSessionBean implements javax.ejb.SessionBean
{
	@SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());

	private static final long serialVersionUID = 1L;
	public static final String CONFIG_FILE = "beans.xml";
	public static final String BEAN_DATABASE = "asyncDatabase";
	public static final String BEAN_QUERY_FACTORY = "queryFactory";
	public static final String TRANSACTION_MANAGER = "transactionManager";
	public static final String BEAN_SESSION_MANAGER = "sessionManager";
	
	private DataService service;
	
	public AsyncDataServiceBean()
	{
	}

	@Override
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	@Override
	protected void onEjbCreate() throws CreateException
	{
	}
	
	/**
	 * Authenticate given user
	 * @param user user principal object
	 * @param address string representation of user's IP-address (for example '127.0.0.1')
	 * @return {@link User} object representing authenticated user 
	 * @throws DataException if authentication failed
	 */
	public User authUser(Principal user, String address) throws DataException
	{
		try {
			return getSyncDataService().authUser(user, address);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Returns all objects of given type, available to user
	 * @param user user, who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant 
	 * @return Collection containing all objects of given type, available to user
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject> Collection<T> listAll(User user, Boolean isDelegation, User realUser, Class<T> type, String session) throws DataException
	{
		try {
			return getSyncDataService().listAll(user, isDelegation, realUser, type, session);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}

	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @param filter filter for child records
	 * @return filtered collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String session, Filter filter) throws DataException
	{
		try {
			return getSyncDataService().<T,R>listChildren(user, isDelegation, realUser, id, type, session, filter);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @return collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String session) throws DataException
	{
		try {
			return getSyncDataService().<T,R>listChildren(user, isDelegation, realUser, id, type, session, null);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Returns all objects of given type which satisfies given {@link Filter} and is available to user 
	 * @param user user who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant
	 * @param filter filter object
	 * @return collection containing all objects of given type which satisfies given {@link Filter} and is available to user
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject> Collection<T> filter(User user, Boolean isDelegation, User realUser, Class<T> type, Filter filter, String session) throws DataException
	{
		try {
			return getSyncDataService().filter(user, isDelegation, realUser, type, filter, session);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Fetches object with given identifier from database and returns it to user
	 * @param user user who perform action
	 * @param id identifier of object to fetch
	 * @return object fetched from database
	 * @throws DataException in case of business-logic error
	 */	
	public <T extends DataObject> T getById(User user, Boolean isDelegation, User realUser, ObjectId id, String session) throws DataException
	{
		try {
			return getSyncDataService().getById(user, isDelegation, realUser, id, session);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	/**
	 * Saves given object in database. This method is user for adding new objects
	 * to database and to update already existing objects. Object is considered new
	 * if call for {@link DataObject#getId()} on this object returns null.
	 * @param user user who perform action
	 * @param obj data object to save
	 * @return identifier of saved object
	 * @throws DataException in case of business-logic error
	 */	
	public Object saveObject(User user, Boolean isDelegation, User realUser, DataObject obj, ExecuteOption option, String sessionId) throws DataException
	{
		Object result;
		if (obj == null)
			throw new IllegalArgumentException("Object can't be null");
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		DatabaseEx db = (DatabaseEx) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		SaveQueryBase saveQuery = factory.getSaveQuery(obj);
		saveQuery.setObject(obj);
		switch (option) {
			case ASYNC: saveQuery.setAsync(true); 
						break;
			case SYNC:  saveQuery.setAsync(false); 
		}
		if (isDelegation) {
		    saveQuery.setRealUser(UserData.read(realUser));
        }
		saveQuery.setSessionId(session);
		try {
			result = db.executeQuery(UserData.read(user), saveQuery);
		} catch (PortalUserProcessException ex) {
			throw new DataException(ex.getMessage());
		}
		return result;
	}
	
	/**
	 * Deletes object with given identifier from database
	 * @param user user who perform action
	 * @param id identifier of object to delete
	 * @throws DataException in case of business-logic error
	 */	
	public void deleteObject(User user, Boolean isDelegation, User realUser, ObjectId id, String session) throws DataException
	{
		try {
			getSyncDataService().deleteObject(user, isDelegation, realUser, id, session);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}

	/**
	/**
	 * Performs given action. Should be used for tasks that couldn't be performed
	 * with others methods.
	 * @param user user who perform action
	 * @param action {@link com.aplana.dbmi.action.Action} object containing additional parameters of action to perform
	 * @return result of the action (could be null} 
	 * @throws DataException in case of business-logic error
	 */
	public <T> T doAction(User user, Boolean isDelegation, User realUser, Action<T> action, ExecuteOption option, String sessionId) throws DataException
	{
		if (action == null)
			throw new IllegalArgumentException("Action can't be null");
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		DatabaseEx db = (DatabaseEx) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		ActionQueryBase actionQuery = factory.getActionQuery(action);
		actionQuery.setAction(action);
		actionQuery.setUser(UserData.read(user));
		switch (option) {
			case ASYNC: actionQuery.setAsync(true); 
						break;
			case SYNC:  actionQuery.setAsync(false); 
		}
		if (isDelegation) {
		    actionQuery.setRealUser(UserData.read(realUser));
        }
		actionQuery.setSessionId(session);
		return db.executeQuery(UserData.read(user), actionQuery);
	}
	
	/**
	 * Checks if given user can create objects of given type in database.
	 * If user is not allowed to create objects of given type, then call
	 * to {@link #saveObject(User, DataObject)} with new object of this type
	 * will cause {@link DataException}
	 * @param user user who perform check
	 * @param type Type of object to create
	 * @return true if user is allowed to create objects of given type, false otherwise
	 * @throws DataException in case of business-logic error
	 */
	public boolean canCreate(User user, Class<?> type) throws DataException
	{
		try {
			return getSyncDataService().canCreate(user, type);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Checks if given user can modify object with given identifier in database.
	 * If user is not allowed to change this object, then call to {@link #saveObject(User, DataObject)}
	 * for this object will cause {@link DataException}
	 * @param user user who perform check
	 * @param id identifier of existing object in database
	 * @return true if user could modify object with given identifier, false otherwise
	 * @throws DataException in case of business-logic error
	 */
	public boolean canChange(User user, ObjectId id, String sessionId) throws DataException
	{
		try {
			return getSyncDataService().canChange(user, id, sessionId);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	/**
	 * Checks if given user can perform given action.
	 * If user is not allowed to perform this action then call
	 * to {@link #doAction(User, Action)} with same object
	 * will cause {@link DataException}
	 * @param user user who perform check
	 * @param action Action to perform
	 * @return true if user can perform given action, false otherwise
	 * @throws DataException in case of business-logic error
	 */
	public boolean canDo(User user, Action<?> action) throws DataException
	{
		try {
			return getSyncDataService().canDo(user, action);
		} catch (RemoteException e) {
			throw new DataException(e.getCause());
		} catch (ServiceException e) {
			throw new DataException(e.getCause());
		}
	}
	
	protected DataService getSyncDataService() throws ServiceException
	{
		if (service == null)
		{
			try {
				InitialContext ic = new InitialContext();
				Object objRef = ic.lookup("ejb/dbmi");
				DataServiceHome home = (DataServiceHome)PortableRemoteObject.narrow(objRef, DataServiceHome.class);
				service = home.create();
			} catch (RemoteException e) {
				throw new ServiceException("Error on data service provider", e);
			} catch (NamingException e) {
				throw new ServiceException("Can't find data service provider", e);
			} catch (CreateException e) {
				throw new ServiceException("Error creating data service", e);
			} catch (Exception e) {
				throw new ServiceException("Unexpected exception when looking for data service", e);
			}
		}
		return service;
	}
	
	public Future<Object> getResult(long id) {
		DatabaseEx db = (DatabaseEx) getBeanFactory().getBean(BEAN_DATABASE);
		return db.getResult(id);
	}
}