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

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.file.ContinuousAction;
import com.aplana.dbmi.model.AsyncTicket;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

/**
 * Simple wrapper around {@link DataService} ejb reference intended to simplify it's usage.
 * This wrapper object should be initialized with credentials of specific users.<br>
 * Afterwards this object could be used to call {@link DataService} methods without 
 * specifying information about user in every call.<br>
 * Additionally this class wraps all {@link RemoteException} objects thrown while working
 * with EJB into {@link ServiceException}.<br>
 * Also this wrapper allows to perform action implementing {@link ContinuousAction} interface
 */
public class AsyncDataServiceBean extends DataServiceBean
{
	private static final String JNDI_EJB_ASYNC = "ejb/dbmi_async";
	private static final Log logger = LogFactory.getLog(DataServiceBean.class);
	protected AsyncDataService asyncService;

	public enum ExecuteOption {
		SYNC,
		ASYNC,
		UNDEFINED
	}
	
	public AsyncDataServiceBean() {
		super();
	}
	
	public AsyncDataServiceBean(String sessionId) {
		super(sessionId);
	}
	
	/**
	 * Sets reference to EJB object to be used by this AsyncDataServiceBean instance, 
	 * {@link User} object representing user for which this object is created and
	 * reference to EJB object to be used by DataServiceBean (synchronous part).
	 * This method could be called only once for every DataServiceBean instance.
	 * @param service remote interface of {@link AsyncDataService} EJB
	 * @param user user who will use this DataServiceBean instance
	 * @param service remote interface of {@link DataService} EJB
	 */
	public void setService(AsyncDataService asyncService, User user, DataService service) {
		if (this.asyncService != null)
			throw new IllegalStateException("Service can't be changed");
		this.asyncService = asyncService;
		this.user = user;
		if (this.service == null)
			this.service = service;
	}
	
	/**
	 * Calls {@link DataService#saveObject(User, DataObject)} with user argument set to value defined by {@link #setService(DataService, User)} call.
	 * @param obj data object to save
	 * @return identifier of saved object
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException if {@link java.rmi.RemoteException} was caught during EJB call
	 */
	public ObjectId saveObject(DataObject obj, ExecuteOption option) throws DataException, ServiceException
	{
		try {
			Object result = getAsyncService().saveObject(user, isDelegation, realUser, cloneObject(obj), option, sessionId);
			if (result instanceof AsyncTicket) {
				return new ObjectId(AsyncTicket.class, ((AsyncTicket) result).getTicketId());
			}
			return (ObjectId)result;
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public ObjectId saveObject(DataObject obj) throws DataException, ServiceException
	{
		return saveObject(obj, ExecuteOption.UNDEFINED);
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
	public <T> T doAction(Action<T> action, ExecuteOption option) throws DataException, ServiceException
	{
		try {
			if (action instanceof ContinuousAction) {
				ContinuousAction contAction = (ContinuousAction) action;
				contAction.setService(this);
				if (!contAction.beforeMainAction())
					return null;
			}
			T result = getAsyncService().doAction(user, isDelegation, realUser, action, option, sessionId);
			if (action instanceof ContinuousAction)
				((ContinuousAction) action).afterMainAction(result);
			return result;
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public <T> T doAction(Action<T> action) throws DataException, ServiceException
	{
		return doAction(action, ExecuteOption.UNDEFINED);
	}

	protected AsyncDataService getAsyncService() throws ServiceException
	{
		if (asyncService == null)
		{
			if (credentials == null)
				throw new IllegalStateException("User must be set prior to calling service");
			try {
				InitialContext ic = new InitialContext();
				Object objRef = ic.lookup(JNDI_EJB_ASYNC);
				AsyncDataServiceHome home = (AsyncDataServiceHome)PortableRemoteObject.narrow(objRef, AsyncDataServiceHome.class);
				asyncService = home.create();
				if (address == null)
					logger.error("!WARNING! [DataService] User's IP address not set, soon will be an exception here! Use PortletUtil.createService() to correct this");
				user = asyncService.authUser(credentials, address);
				if (getIsDelegation()) {
				    realUser = asyncService.authUser(realCredentials, address);
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
		return asyncService;
	}
}
