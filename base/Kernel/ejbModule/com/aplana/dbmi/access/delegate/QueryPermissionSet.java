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

import java.sql.Types;
import java.util.List;

import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * ��������� ��� PermissionSet �� ��� id.
 * @author RAbdullin
 */
public class QueryPermissionSet extends ObjectQueryBase {

	public Object processQuery() throws DataException {
		if (getId() == null)
			return null;
		return loadPermissionSet( ((Long) getId().getId()).longValue() );
	}

	/**
	 * ��������� �� id �� �� ����� ����������.
	 * @param psetId	����������� id.
	 * @return ����� ����������.
	 * @throws DataException
	 */
	public PermissionSet loadPermissionSet( final long psetId ) 
		throws DataException
	{

		final String sqlText = 
			DelegatorBean.PermissionSetMapper.SQL_QUERY_PermissionSet 
			+ "WHERE drole.delegation_role_id=(?) \n";

		// final PermissionSet result = (PermissionSet) getJdbcTemplate().queryForObject(...);
		List /*<PermissionSet>*/ list = getJdbcTemplate().query(
					sqlText,
					new Object[] { new Long(psetId) }, // (:idSet)
					new int[] { Types.NUMERIC },
					new DelegatorBean.PermissionSetMapper()
				);
		if (list == null || list.isEmpty())
			return null;
		final PermissionSet result = (PermissionSet) list.get(0);
		// ���� �� �������� id -> ��� ����� ������
		return (result.getId() != null) ? result : null;
	}

}
