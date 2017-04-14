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
import java.security.Principal;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.MessageFilter;

public class MessageServiceBean {

	private static final String JNDI_EJB = "ejb/br4j-message";
	//private static final String RMI_URL = "iiop://localhost:10031";
	
	private MessageService service;
	private Principal credentials;
	private String address;
	private User user;
	
	public void setUser(Principal credentials) {
		this.credentials = credentials;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	protected MessageService getService() throws ServiceException {
		if (service == null) {
			if (credentials == null)
				throw new IllegalStateException("User must be set before calling the service");
			
			try {
				InitialContext ic = new InitialContext();
				Object objRef = ic.lookup(JNDI_EJB);
				MessageServiceHome home = (MessageServiceHome) PortableRemoteObject.narrow(objRef, MessageServiceHome.class);
				service = home.create();
				
				user = service.authUser(credentials, address);
			} catch (NamingException e) {
				throw new ServiceException("Can't find data service provider", e);
			} catch (ClassCastException e) {
				throw new ServiceException("Error creating data service", e);
			} catch (CreateException e) {
				throw new ServiceException("Error creating data service", e);
			} catch (DataException e) {
				throw new ServiceException("Error authenticating user", e);
			} catch (RemoteException e) {
				throw new ServiceException("Error on message service provider", e);
			}
		}
		return service;
	}

	public Collection<Message> listMessages(MessageFilter filter) throws DataException, ServiceException {
		try {
			return getService().listMessages(user, filter);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
	
	public void sendMessage(Message message) throws DataException, ServiceException {
		try {
			getService().sendMessage(user, message);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
	
	public void markRead(ObjectId messageId) throws DataException, ServiceException {
		try {
			getService().markRead(user, messageId);
		} catch (RemoteException e) {
			throw new ServiceException(e);
		}
	}
}
