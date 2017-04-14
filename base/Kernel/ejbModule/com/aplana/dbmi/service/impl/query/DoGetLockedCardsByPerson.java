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
package com.aplana.dbmi.service.impl.query;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.GetLockedCardsByPerson;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.LockInfo;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;

/**
 * @see GetLockedCardsByPerson
 * @author desu
 *
 */
public class DoGetLockedCardsByPerson extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	public final static String LOCK_MANAGEMENT_BEAN = "lockManagement";

	public ObjectId getEventObject() {
		return ((GetLockedCardsByPerson) getAction()).getId();
	}
	
	public Object processQuery() throws DataException {
		ObjectId id = ((GetLockedCardsByPerson) getAction()).getId();
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		//���������� ��������� � ������ ���������� �������������� ���������
		Integer sessionId = storage.getLockInfoByObject(id).getSessionId();
		ObjectId customer = storage.getLockInfoByObject(id).getCustomer();
		List<ObjectId> result = new ArrayList<ObjectId>();
		//������� ��� ���������� ������� ������������ � ������ ������
		for (LockInfo lock : storage.getLockInfosByCustomer((Person)Person.createFromId(customer), sessionId)) {
			result.add(lock.getObjectId());
		}
		return result;
	}

}
