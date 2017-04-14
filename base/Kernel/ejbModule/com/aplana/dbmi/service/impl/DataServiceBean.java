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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.service.AuthenticationCache;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.User;
import com.aplana.dbmi.service.impl.locks.LockManagement;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import java.security.Principal;
import java.util.Collection;

/**
 * Bean implementation class for Enterprise Bean: DataService
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
public class DataServiceBean extends AbstractStatelessSessionBean implements javax.ejb.SessionBean
{
	private final Log logger = LogFactory.getLog(getClass());

	private static final long serialVersionUID = 1L;
	public static final String CONFIG_FILE = "beans.xml";
	public static final String BEAN_DATABASE = "asyncDatabase";
	public static final String BEAN_QUERY_FACTORY = "queryFactory";
	public static final String TRANSACTION_MANAGER = "transactionManager";
	public static final String BEAN_SESSION_MANAGER = "sessionManager";
	public final static String LOCK_MANAGEMENT_BEAN = "lockManagement";
	
	@SuppressWarnings("unused")
	private static Boolean loaded = false;
	
	public DataServiceBean()
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
	 * @param credentials user principal object
	 * @param address string representation of user's IP-address (for example '127.0.0.1')
	 * @return {@link User} object representing authenticated user 
	 * @throws DataException if authentication failed
	 */
	public User authUser(Principal credentials, String address) throws DataException
	{
        AuthenticationCache authenticationCache = AuthenticationCache.instance();
        User cachedUser = authenticationCache.getUser( credentials, address );
		if ( cachedUser != null ) {
			return cachedUser;
		}
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		UserData data = new UserData();
		Person person = db.resolveUser(credentials.getName());
		if (person == null) {
			try {
				PortalUser user = Portal.getFactory().getUserService().getByLogin(credentials.getName());
				if (null != user){
					db.syncUser(user);
				}else {
					logger.error("Can't get portal user " + credentials.getName());
					throw new DataException("portal.user.fetch",
							new Object[] { credentials.getName()});
				}
			} catch (PortalException e) {
				logger.error("Can't synchronize portal user " + e.getUserId(), e);
				throw new DataException("synch.user.fetch",
						new Object[] { e.getMessage(), e.getUserId() }, e);
			}
			person = db.resolveUser(credentials.getName());
		}
		if (person == null)
			throw new DataException("session.user", new Object[] { credentials.getName() });
		data.setPerson(person);
		data.setAddress(address);
		logger.info("[DEBUG] User found: " + data.getPerson().getFullName());
		//return data.write();

/*		UserData systemUser = new UserData();
		systemUser.setPerson(db.resolveUser(Database.SYSTEM_USER));
		systemUser.setAddress("internal");
		ListUserBosses query = new ListUserBosses();
		query.setParent(person.getId());
		data.setBosses((Collection) db.executeQuery(systemUser, query));

		SessionManager sessionMgr = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		SessionUID uid = sessionMgr.addSession(data);
		User user = new User();
		user.setPerson(person);
		user.setUserData(uid.getUid());*/
		final User resultUser = data.write();
		authenticationCache.setUser( credentials, address, resultUser );
		return resultUser;
	}
	
	/**
	 * Returns all objects of given type, available to user
	 * @param user user, who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant 
	 * @return Collection containing all objects of given type, available to user
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject> Collection<T> listAll(User user, Boolean isDelegation, User realUser, Class<T> type, String sessionId) throws DataException
	{
		if (!DataObject.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Type should be descendant of DataObject");
		logger.info(getUserData(user) + ": listing all " + type.getName());
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		QueryBase listQuery = factory.getListQuery(type);
		listQuery.setSessionId(session);
		if (isDelegation) {
		    listQuery.setRealUser(getUserData(realUser));
		}
		return db.executeQuery(getUserData(user), listQuery);
	}

	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @return collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String sessionId, Filter filter) throws DataException
	{
		if (id == null)
			throw new IllegalArgumentException("Id can't be null");
		if (!DataObject.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Type should be descendant of DataObject");
		logger.info(getUserData(user) + ": listing " + type.getName() + " descendants of " + id);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		ChildrenQueryBase listQuery = factory.getChildrenQuery(id.getType(), type);
		listQuery.setParent(id);
		listQuery.setSessionId(session);
		listQuery.setFilter(filter);
		if (isDelegation) {
            listQuery.setRealUser(getUserData(realUser));
        }
		return db.executeQuery(getUserData(user), listQuery);
	}
	
	/**
	 * Returns all available to user child objects of given type for given database object
	 * @param user user who perform action
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @return collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(User user, Boolean isDelegation, User realUser, ObjectId id, Class<R> type, String sessionId) throws DataException
	{
		return this.<T,R>listChildren(user, isDelegation, realUser, id, type, sessionId, null);
	}

	/**
	 * Returns all objects of given type which satisfies given {@link Filter} and is available to user 
	 * @param user user who perform action
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant
	 * @param filter filter object
	 * @return collection containing all objects of given type which satisfies given {@link Filter} and is available to user
	 * @throws DataException in case of business-logic error
	 */
	public <T extends DataObject> Collection<T> filter(User user, Boolean isDelegation, User realUser, Class<T> type, Filter filter, String sessionId) throws DataException
	{
		if (!DataObject.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Type should be descendant of DataObject");
		logger.info(getUserData(user) + ": listing all " + type.getName() + " filtered by " + filter);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		QueryBase listQuery = factory.getListQuery(type);
		listQuery.setFilter(filter);
		listQuery.setSessionId(session);
		if (isDelegation) {
            listQuery.setRealUser(getUserData(realUser));
        }
		return db.executeQuery(getUserData(user), listQuery);
	}
	
	/**
	 * Fetches object with given identifier from database and returns it to user
	 * @param user user who perform action
	 * @param id identifier of object to fetch
	 * @return object fetched from database
	 * @throws DataException in case of business-logic error
	 */	
	public <T extends DataObject> T getById(User user, Boolean isDelegation, User realUser, ObjectId id, String sessionId) throws DataException
	{
		if (id == null)
			throw new IllegalArgumentException("Can't get object with null id");
		logger.info(getUserData(user) + ": fetching " + id);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		ObjectQueryBase objQuery = factory.getFetchQuery(id.getType());
		objQuery.setId(id);
		objQuery.setSessionId(session);
		if (isDelegation) {
		    objQuery.setRealUser(getUserData(realUser));
        }
		return db.executeQuery(getUserData(user), objQuery);
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
	public ObjectId saveObject(User user, Boolean isDelegation, User realUser, DataObject obj, String sessionId) throws DataException
	{
		if (obj == null)
			throw new IllegalArgumentException("Object can't be null");
		logger.info(getUserData(user) + ": saving " + obj.getClass().getName() + " [" + obj.getId() + "]");
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		SaveQueryBase saveQuery = factory.getSaveQuery(obj);
		saveQuery.setObject(obj);
		if (isDelegation) {
		    saveQuery.setRealUser(getUserData(realUser));
        }
		saveQuery.setSessionId(session);
		ObjectId result = db.executeQuery(getUserData(user), saveQuery); 
		return result;
	}
	
	/**
	 * Deletes object with given identifier from database
	 * @param user user who perform action
	 * @param id identifier of object to delete
	 * @throws DataException in case of business-logic error
	 */	
	public void deleteObject(User user, Boolean isDelegation, User realUser, ObjectId id, String sessionId) throws DataException
	{
		if (id == null)
			throw new IllegalArgumentException("Can't delete object with null id");
		logger.info(getUserData(user) + ": deleting " + id);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		ObjectQueryBase deleteQuery = factory.getDeleteQuery(id);
		deleteQuery.setId(id);
		deleteQuery.setSessionId(session);
		if (isDelegation) {
		    deleteQuery.setRealUser(getUserData(realUser));
        }
		db.executeQuery(getUserData(user), deleteQuery);
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
	public <T> T doAction(User user, Boolean isDelegation, User realUser, Action<T> action, String sessionId) throws DataException
	{
		if (action == null)
			throw new IllegalArgumentException("Action can't be null");
		logger.info(getUserData(user) + ": processing " + action.getClass().getName() +
				(action instanceof ObjectAction ? " on " + ((ObjectAction<?>) action).getObjectId() : ""));
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		ActionQueryBase actionQuery = factory.getActionQuery(action);
		actionQuery.setAction(action);
		actionQuery.setUser(getUserData(user));
		if (isDelegation) {
		    actionQuery.setRealUser(getUserData(realUser));
        }
		actionQuery.setSessionId(session);
		return db.executeQuery(getUserData(user), actionQuery);
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
		if (!DataObject.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Type should be descendant of DataObject");
		logger.info(getUserData(user) + ": probing creation of " + type);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		AccessCheckerBase accessChecker = factory.getObjectAccessChecker(type);
		if (accessChecker == null)
			return true;
		return db.checkAccess(getUserData(user), accessChecker);
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
		if (id == null)
			throw new IllegalArgumentException("Id can't be null");
		logger.info(getUserData(user) + ": probing a change of " + id);
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		SessionManager manager = (SessionManager) getBeanFactory().getBean(BEAN_SESSION_MANAGER);
		Integer session = manager.getSessionHash(sessionId);
		AccessCheckerBase accessChecker = factory.getObjectAccessChecker(id.getType());
		if (accessChecker == null)
			return true;
		accessChecker.setObject(id);
		boolean canChange = db.checkAccess(getUserData(user), accessChecker);

		LockManagement storage = (LockManagement) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		return canChange && OperationResult.SUCCESS.equals(storage.canLock(id, user.getPerson(), session));
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
		if (action == null)
			throw new IllegalArgumentException("Action can't be null");
		logger.info(getUserData(user) + ": probing possibility of " + action.getClass());
		QueryFactory factory = (QueryFactory) getBeanFactory().getBean(BEAN_QUERY_FACTORY);
		Database db = (Database) getBeanFactory().getBean(BEAN_DATABASE);
		AccessCheckerBase accessChecker = factory.getActionAccessChecker(action.getClass());
		if (accessChecker == null)
			return true;
		if (action instanceof ObjectAction){
			accessChecker.setObject(((ObjectAction<?>) action).getObjectId());
			accessChecker.setAction(action);
		}
		return db.checkAccess(getUserData(user), accessChecker);
	}
	
    private UserData getUserData(User user) throws DataException {
        return UserData.read(user);
    }
}
