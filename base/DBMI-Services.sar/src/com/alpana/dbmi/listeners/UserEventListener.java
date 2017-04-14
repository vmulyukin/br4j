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
package com.alpana.dbmi.listeners;

import com.aplana.dbmi.action.GetPersonByLogin;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.*;
import com.aplana.dbmi.service.impl.SessionManager;
import com.aplana.dbmi.service.impl.locks.LockManagementBean;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.LockInfo;
import org.apache.log4j.Logger;
import org.jboss.portal.api.event.PortalEvent;
import org.jboss.portal.api.event.PortalEventContext;
import org.jboss.portal.api.event.PortalEventListener;
import org.jboss.portal.api.user.event.UserAuthenticationEvent;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ��������� ���������������� ������� �������: 
 * ������������ ��� ��������� ������� ����� � ������ �� ������� �������������, 
 * ����� ����� ��� �� �������� ����������
 * @author ynikitin
 */
public class UserEventListener implements PortalEventListener {
	private static final Logger logger = Logger.getLogger(UserEventListener.class); 
	private Map<String, Long> userSessionCount = new HashMap<String, Long>();
	private LockManagementBean lockManagement = null;
	private SessionManager sessionManager = null;

	public void onEvent(PortalEventContext eventContext, PortalEvent event) {
		if (event instanceof UserAuthenticationEvent) {
			Long sessionCount = 0l;
			Integer sessionHash = SessionUtil.initSessionId(eventContext.getPortalRuntimeContext());
			String sessionId = SessionUtil.initSession(eventContext.getPortalRuntimeContext());
			UserAuthenticationEvent userEvent = (UserAuthenticationEvent)event;
			SessionManager manager = sessionManagerInstance();
			if (userEvent.getType() == UserAuthenticationEvent.SIGN_IN) {
				logger.debug("User "+userEvent.getUserId()+" is connected and new session "+sessionId+" (hashcode: "+sessionHash+") created");
				manager.addSessionId(sessionId, sessionHash, userEvent.getUserId());
				sessionCount = incUserSessionCount(userEvent.getUserId());
			} else if (userEvent.getType() == UserAuthenticationEvent.SIGN_OUT) {
				logger.debug("User "+userEvent.getUserId()+" by session "+sessionId+" (hashcode: "+sessionHash+") disconnected");
				// ������� ��� ���������� �������� ������������ � ������� ������
				dropAllUserLocks(userEvent.getUserId(), sessionId, sessionHash);
				manager.removeSessionId(sessionId);
				sessionCount = decUserSessionCount(userEvent.getUserId());
			}
			logger.debug("There is "+sessionCount+" session(s) for user '"+userEvent.getUserId()+"' now");
		}
	}
	
	// ������� ��� ���������������� ����������
	private void dropAllUserLocks(String userId, String sessionId, Integer sessionHash) {
		try{
			DataServiceBean serviceBean = serviceBeanInstance(sessionId);
			LockManagementBean managementBean = managementBeanInstance();
			Person person = getPersonByLogin(serviceBean, userId);
			removeAllLocksByPerson(managementBean, person, sessionHash);
		} catch (Exception ex) {
			logger.error("Error while drop all locks for user "+userId+": ", ex);
		}
	}
	
	protected Person getPersonByLogin(DataServiceBean serviceBean, String userId) throws Exception {
		Person p;
		GetPersonByLogin personLogin = new GetPersonByLogin(userId);
		p = serviceBean.doAction(personLogin);
		return p; 
	}
	
	// ��������� ������� ������ ����������� ������������ 
	private Long incUserSessionCount(String userId) {
		Long sessionCount;
		synchronized (userSessionCount) {
			sessionCount = userSessionCount.get(userId);
			if (sessionCount == null)
				sessionCount = 0l;
			sessionCount++;
			userSessionCount.put(userId, sessionCount);
		}
		return sessionCount;
	}

	// ��������� ������� ������ ����������� ������������ 
	private Long decUserSessionCount(String userId) {
		Long sessionCount;
		synchronized (userSessionCount) {
			sessionCount = userSessionCount.get(userId);
			if (sessionCount == null)
				return 0l;
			sessionCount--;
			userSessionCount.put(userId, sessionCount);
		}
		return sessionCount;
	}

	/**
	    * Returns DataServiceBean instance
	    * 
	    * @return DataServiceBean instance or null if some error was occurred
	    *         during initialization
	    * @throws ServiceException
	    */
	private DataServiceBean serviceBeanInstance(String sessionId) throws ServiceException {
		DataServiceBean serviceBean;
		InitialContext context;
		DataServiceHome home;
		try {
			context = new InitialContext();
			home = (DataServiceHome) PortableRemoteObject.narrow(context
					.lookup("ejb/dbmi"), DataServiceHome.class);
		} catch (NamingException ex) {
			throw new ServiceException(
			"Error during DataServiceHome context initialization",ex);
		}
		if (home == null)
			return null;
		
		try {
			DataService service = home.create();
			User user = service.authUser(new SystemUser(), "127.0.0.1");
			serviceBean = new DataServiceBean();
			serviceBean.setService(service, user);
			serviceBean.setSessionId(sessionId);
		} catch (RemoteException ex) {
			throw new ServiceException(ex);
		} catch (CreateException ex) {
			throw new ServiceException(ex);
		} catch (DataException ex) {
			throw new ServiceException(ex);
		}
		return serviceBean;
	}
		
	private LockManagementBean managementBeanInstance() {
		if (lockManagement == null) {
			BeanFactoryLocator factoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
			BeanFactory beanFactory= factoryLocator.useBeanFactory("businessBeanFactory").getFactory();
			lockManagement = (LockManagementBean)beanFactory.getBean("lockManagement");
		}
		return lockManagement;
	}
	
	private SessionManager sessionManagerInstance() {
		if (sessionManager == null) {
			BeanFactoryLocator factoryLocator = ContextSingletonBeanFactoryLocator.getInstance();
			BeanFactory beanFactory= factoryLocator.useBeanFactory("businessBeanFactory").getFactory();
			sessionManager = (SessionManager)beanFactory.getBean("sessionManager");
		}
		return sessionManager;
	}
	
	/**
	 * ������� ��� ���������������� ����������
	 * @param managementBean- ��� ��������� ������ ���������� � ������ �������������� ������������� ��������
	 * @param person		- ��� ���������� ������
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	private void removeAllLocksByPerson(LockManagementBean managementBean, Person person, Integer sessionHash) throws DataException, ServiceException {
		List<LockInfo> lockInfosByCustomer = managementBean.getLockInfosByCustomer(person, sessionHash);
		logger.debug("There is "+lockInfosByCustomer.size()+" locks where user '"+person.getLogin()+"' is customer.");
		if (lockInfosByCustomer.size()==0){
			return;
		}
		// ������� ������� ���������������� ���������� (� ��� ����� ��, ��� ������ ��� �� ������������ ��������)
		for(LockInfo li: lockInfosByCustomer) {
			managementBean.releaseLock(li.getObjectId(), person, sessionHash);
			logger.trace("Lock "+li.toString()+" is drop by UnlockObject.");
		}

		lockInfosByCustomer = managementBean.getLockInfosByCustomer(person, sessionHash);
		if (lockInfosByCustomer.size()>0){
			logger.debug("There is "+lockInfosByCustomer.size()+" locks where user '"+person.getLogin()+"' is customer still.");
		}
		// ����� ����� ������� ���������� ��� ��������, ������� ������������ ���� ��������� ���  
		for(LockInfo li: lockInfosByCustomer){
			managementBean.tryRemoveLock(li.getObjectId(), person, sessionHash);
			logger.trace("Lock "+li.toString()+" is drop by TryRemove.");
		}
	}
}