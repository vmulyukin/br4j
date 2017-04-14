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
package com.aplana.dbmi.access.delegate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.CheckDelegationIsEditableAction;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoCheckDelegationIsEditableQuery extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	private static final String SQL = "WITH dlg AS ( SELECT start_at, end_at FROM delegation WHERE delegate_person_id = ? \n"+
													"AND start_at <= now() AND end_at >= now() ORDER BY end_at DESC	limit 1) \n"+
									"SELECT count(1) FROM delegation d WHERE d.delegation_id = ? AND d.creator_person_id = ? \n"+
													"AND d.created_at >= (SELECT start_at FROM dlg) AND d.created_at <= (SELECT end_at FROM dlg)";
	/**
	 * �������� ����������� �������������� ������������� ��������� (�� ���� ���� ��������� �������������)
	 * 
	 * ������� ����� ����� ������������� ������������� ��������� �� � ������ �������� �������������
	 * 
	 */
	@Override
	public Object processQuery() throws DataException {

		Action act = getAction();
		if(act instanceof CheckDelegationIsEditableAction) {
			CheckDelegationIsEditableAction action = (CheckDelegationIsEditableAction)act;
			
			if(action.getDelegationId() == null)
				return false;
			
			long delegationId = (Long) action.getDelegationId().getId();
			
			// ������� ������������ ��� �������� ������������ �������� ����������� �������������� �������������
			long personId = (Long) (getRealUser() != null ? 
											( getRealUser().getPerson() != null ? getRealUser().getPerson().getId().getId() : -1 )
											: -1);
			if(personId == -1)
				return false;
			
			return getJdbcTemplate().queryForInt(SQL, new Object[]{personId, delegationId, personId}) > 0;
		}
		return false;
	}

}
