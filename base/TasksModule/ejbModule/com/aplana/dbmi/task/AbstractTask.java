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
package com.aplana.dbmi.task;

import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import com.aplana.dbmi.action.WriteEventLog;
import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.SystemUser;

/**
 * @author ynikitin
 * ����������� ����� ��� ���� ���������� - ����� �������� serviceBean ��� ������������ ��������� � ���� 
 */
public abstract class AbstractTask extends AbstractStatelessSessionBean implements SessionBean {
	private static final long serialVersionUID = 1L;
	protected static final String CONFIG_FOLDER = "dbmi/tasks";
	protected DataServiceBean serviceBean;
	
	@Override
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	@Override
	protected void onEjbCreate() throws CreateException {
		serviceBean = createServiceBean();
	}

	private DataServiceBean createServiceBean() {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setUser(new SystemUser());
		serviceBean.setAddress("localhost");
		serviceBean.setSessionId(Thread.currentThread().getId() + "");
		return serviceBean;
	}
	
	protected void addInfoMessageInEventLog(ObjectId objectId, String eventName, String message, String messageDescription, boolean isSuccess){
		LogEntry entry = new LogEntry();
		entry.setEvent(eventName);
		entry.setObject(objectId);
		entry.setUser(serviceBean.getPerson());
		entry.setAddress("localhost");
		entry.setTimestamp(new Date());
		InfoMessage msg = new InfoMessage(entry);
		msg.setMessage(message);
		msg.setDescriptionMessage(messageDescription);
		msg.isSucces((isSuccess)?new Long(1):new Long(0));	// �������� ������ � ��� - ��� 1, � �� 0
		try {
			WriteEventLog writeEventLog = new WriteEventLog();
			writeEventLog.setEntry(msg);
			serviceBean.doAction(writeEventLog);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}