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
package com.aplana.ireferent.util;

import java.security.Principal;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataService;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.ServiceException;

public class HelperServiceBean extends AsyncDataServiceBean {

	private DataService helperService;
	private AsyncDataService helperAsyncService;

	public HelperServiceBean(String id, long time) {
		this.sessionId = id;
	}

	public void setHelperAddress(String address) {
		this.setAddress(address);
	}

	public DataService getHelperService() throws ServiceException {
		// this.credentials = user;
		this.helperService = this.getService();
		return this.helperService;
	}

	public void setHelperService(DataService service) {
		this.service = service;
	}

	public AsyncDataService getHelperAsyncService() throws ServiceException {
		// this.credentials = user;
		this.helperAsyncService = this.getAsyncService();
		return this.helperAsyncService;
	}

	public void setHelperAsyncService(AsyncDataService asyncService) {
		this.asyncService = asyncService;
	}

	public void setHelperUser(Principal user) {
		super.setUser(user);
	}

	public void setHelperIsDelegation(boolean isDelegation) {
		this.setIsDelegation(isDelegation);
	}

	public void setHelperRealUser(Principal realUser) {
		this.setRealUser(realUser);
	}

	@Override
	public ObjectId saveObject(DataObject obj) throws DataException,
			ServiceException {
		return saveObject(obj, ExecuteOption.SYNC);
	}

	@Override
	public <T> T doAction(Action<T> action) throws DataException,
			ServiceException {
		return doAction(action, ExecuteOption.SYNC);
	}
}
