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
package com.aplana.dbmi.service.impl.async;

import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceHome;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.CreateException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * Used to load DataServiceBean on startup by
 * org.jboss.varia.scheduler.Scheduler
 * 
 * @author chepiov
 * 
 */
public class CreateDataServiceOnStartupTaskBean extends StandardMBean implements
		org.jboss.varia.scheduler.Schedulable {

	private Boolean worked = false;
	private Log logger = LogFactory.getLog(getClass());

	public CreateDataServiceOnStartupTaskBean(Class<?> mbeanInterface)
			throws NotCompliantMBeanException {
		super(mbeanInterface);
	}

	public CreateDataServiceOnStartupTaskBean()
			throws NotCompliantMBeanException {
		this(org.jboss.varia.scheduler.Schedulable.class);
	}

	@Override
	synchronized public void perform(Date pTimeOfCall, long pRemainingRepetitions) {
		if (worked)
			return;
		worked = true;

		DataServiceHome home = null;
		try {
			InitialContext context = new InitialContext();
			home = (DataServiceHome) PortableRemoteObject.narrow(
					context.lookup("ejb/dbmi"), DataServiceHome.class);
		} catch (NamingException ex) {
			logger.error("Error during DataServiceHome context initialization", ex);
		}

		if (home == null) {
			logger.error("Error during DataServiceHome context initialization, DataServiceHome is null");
			return;
		}

		try {
			@SuppressWarnings("unused")
			DataService service = home.create();
		} catch (RemoteException ex) {
			logger.error("Error during DataService creating", ex);
		} catch (CreateException ex) {
			logger.error("Error during DataService creating", ex);
		}
	}
}
