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
package com.aplana.dbmi.ws.impl;

import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * ������� ����� ��� ���-�������� ����������
 * Created by EChirkov on 30.09.2015.
 */
public class ServiceImplBase {

	private Log logger = LogFactory.getLog(getClass());

	protected DataServiceBean serviceBean = null;

	protected DataServiceBean getDataServiceBean() {
		try {
			if (serviceBean == null) {
				InitialContext context = new InitialContext();
				DataServiceHome home = (DataServiceHome) PortableRemoteObject.narrow(context.lookup("ejb/dbmi"), DataServiceHome.class);
				DataService service = home.create();
				serviceBean = new DataServiceBean();
				serviceBean.setService(service, service.authUser(new SystemUser(), "127.0.0.1"));
			}
		} catch (Exception e) {
			logger.error("Error creating service", e);
		}
		return serviceBean;
	}
}
