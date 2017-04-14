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

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.UpdateRulesByRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

public class DoUpdateRulesByRole extends ActionQueryBase {

	protected AccessRuleManager manager;
	
	@Override
	public Object processQuery() throws DataException {
		UpdateRulesByRole updateRulesByRole = getAction();
		Boolean isExists = (Boolean)getJdbcTemplate().
				queryForObject("select count(1)>0 from person_role where prole_id = ?",
						new Object[] {updateRulesByRole.getObjectId().getId()},
						Boolean.class);
		if(isExists){
			manager.cleanAccessListByRole(updateRulesByRole.getObjectId());
			return manager.updateAccessByRole(updateRulesByRole.getObjectId());
		} else {
			return 0L;
		}
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}
}
