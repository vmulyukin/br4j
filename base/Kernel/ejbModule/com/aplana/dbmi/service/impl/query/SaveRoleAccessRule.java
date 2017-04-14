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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.RoleAccessRule;
import com.aplana.dbmi.service.DataException;

public class SaveRoleAccessRule extends SaveAccessRule {
	
	protected ObjectId processNew() throws DataException {
		RoleAccessRule rule = (RoleAccessRule) getObject();
		long id = insertRuleAndOperation(rule);
		getJdbcTemplate().update(
				"INSERT INTO role_access_rule (rule_id, role_code) " +
				"VALUES (?, ?)",
				new Object[] { new Long(id), safeGetId(rule.getRole()) },
				new int[] { Types.NUMERIC, Types.VARCHAR });
		return new ObjectId(RoleAccessRule.class, id);
	}

	public void validate() throws DataException {
		super.validate();
		//RoleAccessRule rule = (RoleAccessRule) getObject();
		// TODO Auto-generated method stub
	}

}
