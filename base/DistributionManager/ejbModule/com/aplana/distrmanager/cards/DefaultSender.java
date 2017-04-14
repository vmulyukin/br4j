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
package com.aplana.distrmanager.cards;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.Configuration;

public class DefaultSender {
	private static String uuid;
	private static String fullName;
	private static String uuidSystem;
	private static String nameSystem;
	
	private DefaultSender() {
	}
	
	public static void init(DataServiceFacade serviceBean) throws Exception {
		Organization defSender = getDefaultOrganization(serviceBean);
		uuid = defSender.getUUID();
		fullName = defSender.getFullName();
	}
	
	public static Organization getDefaultOrganization(DataServiceFacade serviceBean) throws Exception {
		ObjectId defaultOrgId = Configuration.instance().getDefaultOrganizationId();
		Organization sender = new Organization(serviceBean);
		sender.init(defaultOrgId);
		return sender;
    }

	public static String getUuid() {
		return uuid;
	}

	public static String getFullName() {
		return fullName;
	}
	
	
}
