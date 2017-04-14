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

import java.sql.Types;
import java.util.Collection;

import com.aplana.dbmi.action.GetAdminEmail;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Query used to perform {@link GetAdminEmail} action
 */
public class DoGetAdminEmail extends ActionQueryBase
{
	/**
	 * Gets email address of DBMI administrator
	 * @return string representing email address of DBMI administrator. If there is several
	 * administrators then returns email of first one 
	 * (order is not defined, so it choose randomly)
	 * @throws DataException if no active administrators exists in system  
	 */
	public Object processQuery() throws DataException
	{
		Collection emails = getJdbcTemplate().queryForList(
				"SELECT DISTINCT p.email FROM person p " +
				"INNER JOIN person_role r ON p.person_id=r.person_id " +
				"WHERE r.role_code=? AND p.is_active=1",
				new Object[] { Role.ADMINISTRATOR },
				new int[] { Types.VARCHAR },
				String.class);
		if (emails.size() == 0)
			throw new DataException("action.mailadmin.noadmin");
		return emails.iterator().next();
	}
}
