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

import java.util.HashSet;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ActiveQueryBases;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.async.QueryContainer;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.LockInfo;

public abstract class LockQueryBase extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	public static final String LOCK_MANAGEMENT_BEAN = "lockManagement".intern();
	
	protected HashSet<ObjectId> getLinkedQueriesIds() {
		//������� ��������� ��� �������� Uid query, ���������� � �������
		HashSet<ObjectId> linkedQueries = new HashSet<ObjectId>();
		linkedQueries.add(getPrimaryQuery().getUid()); //��������� ���� ����� ���� query (����� ������ �������, primaryQuery)
		QueryContainer qc = getPrimaryQuery().getQueryContainer(); //���������� ������� queryContainer, � ������ �������� �������� ���� query

		if (qc != null) {
			// ����������� ��������� queryContainer'� �����
			while (qc.getPrev() != null) {
				linkedQueries.add(qc.getPrev().getQuery().getUid()); //������� Uid �� ������� query � ���������
				qc = qc.getPrev();
			}
			// ������������ � �������� queryContainer
			qc = getPrimaryQuery().getQueryContainer();
			// � ������ ����������� ������ ��� ��������� queryContainer'�
			while (qc.getNext() != null) {
				linkedQueries.add(qc.getNext().getQuery().getUid()); //��� �� ������� � ��������
				qc = qc.getNext();
			}
		}
		return linkedQueries;
	}
	
	protected String getLockInfoFromQuery(ObjectId id) {
		LockManagementSPI storage = (LockManagementSPI) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		LockInfo lockInfo = storage.getLockInfoByObject(id);
		if (lockInfo != null && lockInfo.getObjectId() != null && lockInfo.getQueryId() != null && lockInfo.isInService()) {
			ActiveQueryBases activeQueryBases = (ActiveQueryBases) getBeanFactory().getBean(ActiveQueryBases.BEAN_NAME);
			QueryBase query = activeQueryBases.get(lockInfo.getQueryId());
			if (query == null) {
				return null;
			}
			String infoFromQuery = "������ ����� ��������� " + query.toString();
			infoFromQuery += ", ��� ����� �������� ��������� � ������ �����������.";
			if (query.isAsync())
				infoFromQuery += "\n���������� ������� �������� � ������ ����������� ��������� �����";
			else
				infoFromQuery += "\n��������� �������� �����";
			return infoFromQuery;
		}
		return null;
	}
}
