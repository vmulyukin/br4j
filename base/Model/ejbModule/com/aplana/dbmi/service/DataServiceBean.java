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
import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.action.file.ContinuousAction;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.filter.Filter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.core.aspects.controller.node.Navigation;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;

/**
 * Simple wrapper around {@link DataService} ejb reference intended to simplify it's usage.
 * This wrapper object should be initialized with credentials of specific users.<br>
 * Afterwards this object could be used to call {@link DataService} methods without
 * specifying information about user in every call.<br>
 * Additionally this class wraps all {@link RemoteException} objects thrown while working
 * with EJB into {@link ServiceException}.<br>
 * Also this wrapper allows to perform action implementing {@link ContinuousAction} interface
 */
public class DataServiceBean implements ActionPerformer
{
	private static final String JNDI_EJB = "ejb/dbmi";
	//private static final String RMI_URL = "iiop://localhost:10031";
	private static final Log logger = LogFactory.getLog(DataServiceBean.class);
	public static final String USER_NAME = "userName";

	protected DataService service;
	protected Principal credentials;
	protected Principal realCredentials;
	protected String address;
	protected User user;
	protected User realUser;
	protected Boolean isDelegation = false;
	protected String sessionId;

	public DataServiceBean() {
		this.sessionId = SessionUtil.initSession(Navigation.getPortalRuntimeContext());
		if (sessionId == null) {
			logger.warn("DataServiceBean has been created without sessionId (it is null)");
		}
	}
	
	public DataServiceBean(String id) {
		this.sessionId = id;
		if (sessionId == null) {
			logger.warn("DataServiceBean has been created without sessionId (it is null)");
		}
	}
	
	/**
	 * Sets Principal object representing user who will
	 * perform calls to {@link DataService} EJB later.
	 * This methods should be performed before first call to EJB methods.
	 * @param credentials Principal object representing user for which this DataServiceBean instance created
	 * @throws IllegalStateException if this instance of DataServiceBean was already used to communicate with EJB in past
	 */
	public void setUser(Principal credentials) {
	    if (credentials != null) {
    		this.credentials = credentials;
    		if (address != null && service != null) {
    		    try {
    		        user = service.authUser(credentials, address);
    		    } catch (Exception e) {
    		    	logger.error("!WARNING! [DataService] cant set new User - " + credentials.getName());
                }
    		}
	    }
	    if (logger.isDebugEnabled()) {
			if (user!=null && user.getPerson()!=null && user.getPerson().getId().getId().equals(0l)){
				StringBuilder s = new StringBuilder("[TRACE_0_USER] stackTrace: \r\n ");
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				for (StackTraceElement stackTraceElement : stack) {
					s.append(stackTraceElement.toString()).append("\r\n ");
				}
				logger.debug(s.toString());
			}
		}

	}

	/**
	 * Sets address of user who will perform calls to {@link DataService} EJB later
	 * This methods should be performed before first call to EJB methods.
	 * @param address String representation of user's address
	 * @throws IllegalStateException if this instance of DataServiceBean was already used to communicate with EJB in past
	 */
	public void setAddress(String address) {
		if (service != null)
			throw new IllegalStateException("Address can only be set before first access to the service");
		this.address = address;
	}

	/**
	 * Sets reference to EJB object to be used by this DataServiceBean instance and
	 * {@link User} object representing user for which this object is created.
	 * This method could be called only once for every DataServiceBean instance.
	 * @param service remote interface of {@link DataService} EJB
	 * @param user user who will use this DataServiceBean instance
	 */
	public void setService(DataService service, User user) {
		if (this.service != null)
			throw new IllegalStateException("Service can't be changed");
		this.service = service;
		this.user = user;
	}

	/**
	 * Returns currently authenticated {@link Person} object
	 * @return Person object
	 */
	public Person getPerson() {
		return getUser() == null ? null : getUser().getPerson();
	}

	/**
	 * Calls {@link DataService#listAll(User, Class)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param type type of objects to be fetched
	 * @return collection containing all objects of given type, available to user
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject> Collection<T> listAll(Class<T> type) throws DataException, ServiceException
	{
		try {
			return getService().listAll(user, isDelegation, realUser, type, sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#listChildren(User, ObjectId, Class)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @param filter filter for child records
	 * @return filtered collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(ObjectId id, Class<R> type, Filter filter) throws DataException, ServiceException
	{
		try {
			return getService().<T,R>listChildren(user, isDelegation, realUser, id, type, sessionId, filter);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Calls {@link DataService#listChildren(User, ObjectId, Class)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param id identifier of parent data object
	 * @param type type of child records to fetch
	 * @return collection containing  all available to user child objects of given type for given database object
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject, R extends T> Collection<T> listChildren(ObjectId id, Class<R> type) throws DataException, ServiceException
	{
		try {
			return getService().<T,R>listChildren(user, isDelegation, realUser, id, type, sessionId, null);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#filter(User, Class, Filter)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param type type of objects to fetch. Should be {@link com.aplana.dbmi.model.DataObject} descendant
	 * @param filter filter object
	 * @return collection containing all objects of given type which satisfies given {@link Filter} and is available to user
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject> Collection<T> filter(Class<T> type, Filter filter) throws DataException, ServiceException
	{
		try {
			return getService().filter(user, isDelegation, realUser, type, filter, sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#getById(User, ObjectId)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param id identifier of object to fetch
	 * @return object fetched from database
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T extends DataObject> T getById(ObjectId id) throws DataException, ServiceException
	{
		try {
			return getService().getById(user, isDelegation, realUser, id, sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#saveObject(User, DataObject)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param obj data object to save
	 * @return identifier of saved object
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public ObjectId saveObject(DataObject obj) throws DataException, ServiceException
	{
		try {
			return getService().saveObject(user, isDelegation, realUser, cloneObject(obj), sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#deleteObject(User, ObjectId)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param id identifier of object to delete
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public void deleteObject(ObjectId id) throws DataException, ServiceException
	{
		try {
			getService().deleteObject(user, isDelegation, realUser, id, sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#doAction(User, Action)} with user argument set to
	 * value defined by {@link #setService(DataService, User)} call.
	 * If given action object implements {@link com.aplana.dbmi.action.file.ContinuousAction}
	 * interface then {@link com.aplana.dbmi.action.file.ContinuousAction#beforeMainAction()}
	 * and {@link com.aplana.dbmi.action.file.ContinuousAction#afterMainAction(Object)}
	 * methods will be called before and after the call of {@link DataService#doAction(User, Action)}
	 * method respectively.
	 * @param action {@link com.aplana.dbmi.action.Action} object containing additional parameters of action to perform
	 * @return result of the action (could be null}
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public <T> T doAction(Action<T> action) throws DataException, ServiceException
	{
		try {
			if (action instanceof ContinuousAction) {
				ContinuousAction contAction = (ContinuousAction) action;
				contAction.setService(this);
				if (!contAction.beforeMainAction())
					return null;
			}
			T result = getService().doAction(user, isDelegation, realUser, action, sessionId);
			if (action instanceof ContinuousAction)
				((ContinuousAction) action).afterMainAction(result);
			return result;
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#canCreate(User, Class)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param type Type of object to create
	 * @return true if user is allowed to create objects of given type, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public boolean canCreate(Class<?> type) throws DataException, ServiceException
	{
		try {
			return getService().canCreate(user, type);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#canChange(User, ObjectId)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param id identifier of existing object in database
	 * @return true if user could modify object with given identifier, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public boolean canChange(ObjectId id) throws DataException, ServiceException
	{
		try {
			return getService().canChange(user, id, sessionId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Calls {@link DataService#canDo(User, Action)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param action Action to perform
	 * @return true if user can perform given action, false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public boolean canDo(Action<?> action) throws DataException, ServiceException
	{
		try {
			return getService().canDo(user, action);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}

	protected DataService getService() throws ServiceException
	{
		if (service == null)
		{
			if (credentials == null)
				throw new IllegalStateException("User must be set prior to calling service");
			try {
/*
				Properties props = new Properties();
				props.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
				props.put(Context.PROVIDER_URL, RMI_URL);
				InitialContext ic = new InitialContext(props);
*/
				InitialContext ic = new InitialContext();
				Object objRef = ic.lookup(JNDI_EJB);
				DataServiceHome home = (DataServiceHome)PortableRemoteObject.narrow(objRef, DataServiceHome.class);
				service = home.create();
				if (address == null && logger.isErrorEnabled())
					logger.error("!WARNING! [DataService] User's IP address not set, soon will be an exception here! Use PortletUtil.createService() to correct this");
				user = service.authUser(credentials, address);
				if (getIsDelegation()) {
				    realUser = service.authUser(realCredentials, address);
				}
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

	protected User getUser() {
		return user;
	}

	public User getRealUser() {
        return realUser;
    }

	public void setRealUser(Principal realCredentials) {
        if (realCredentials != null) {
            this.realCredentials = realCredentials;
            if (address != null && service != null) {
                try {
                    realUser = service.authUser(realCredentials, address);
                } catch (Exception e) {
                    logger.warn("!WARNING! [DataService] cant set new realUser - " + realCredentials.getName());
                }
            }
        }
        if (logger.isDebugEnabled()) {
			if (realUser!=null &&realUser.getPerson()!=null && realUser.getPerson().getId().getId().equals(0l)){
				StringBuilder s = new StringBuilder("[TRACE_0_USER] stackTrace: \r\n ");
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		        for (StackTraceElement stackTraceElement : stack) {
		            s.append(stackTraceElement.toString()).append("\r\n ");
		        }
		        logger.debug(s.toString());
			}
        }
    }

    public Boolean getIsDelegation() {
        return isDelegation != null ? isDelegation : false;
    }

    /** Return user login from credentials or user
     *  @return String object <b>user_login</b> if {@link #credentials} or {@link #user} is not null and <b>null</b> otherwise
     *  	   
     */
    public String getUserName() {
    	StringBuffer userName = new StringBuffer();
    	if (user!=null&&user.getPerson()!=null){
    		userName.append(user.getPerson().getLogin());
    	} else if (credentials!=null) {
    		userName.append(credentials.getName());
    	} else
    		return null;
        return userName.toString();
    }

    public void setIsDelegation(Boolean isDelegation) {
        this.isDelegation = isDelegation;
    }

    protected DataObject cloneObject(DataObject object) throws ServiceException {
		try {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(buf);
			out.writeObject(object);
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
			return (DataObject) in.readObject();
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		if (sessionId != null)
			this.sessionId = sessionId;
		else {
			if (logger.isWarnEnabled())
				logger.warn("Setting sessionId with null value. SessionId has not been set. Old value is: " + this.sessionId);
		}
	}
}
