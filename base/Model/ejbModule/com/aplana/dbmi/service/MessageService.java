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

import javax.ejb.EJBObject;

import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.MessageFilter;

public interface MessageService extends EJBObject {

	public User authUser(Principal user, String address) throws DataException, RemoteException;
	
	public Collection<Message> listMessages(User user, MessageFilter filter) throws DataException, RemoteException;
	
	public void sendMessage(User user, Message message) throws DataException, RemoteException;
	
	public void markRead(User user, ObjectId messageId) throws DataException, RemoteException;
}
