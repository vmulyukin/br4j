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

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonProfileAccessRule;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;

public class SaveProfileAccessRule extends SaveAccessRule {

	protected AccessRuleManager manager;

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		super.setJdbcTemplate(jdbc);
		manager = new AccessRuleManager(jdbc);
	}

	protected ObjectId processNew() throws DataException {
		PersonProfileAccessRule rule = (PersonProfileAccessRule) getObject();
		rule.setId(insertRuleAndOperation(rule));
		getJdbcTemplate().update(
				"INSERT INTO profile_access_rule " +
					"(rule_id, profile_attr_code, target_attr_code, link_attr_code, intermed_attr_code, " +
					"linked_status_id, role_code) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)",
				new Object[] {
						rule.getId().getId(),
						safeGetId(rule.getProfileAttribute()),
						safeGetId(rule.getTargetAttribute()),
						safeGetId(rule.getLinkAttribute()),
						safeGetId(rule.getIntermediateLinkAttribute()),
						safeGetId(rule.getLinkedStateId()),
						safeGetId(rule.getRoleId()) },
				new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
						Types.NUMERIC, Types.VARCHAR });
		manager.applyNewRule(rule);
		return rule.getId();
	}

	public void validate() throws DataException {
		super.validate();
		PersonProfileAccessRule rule = (PersonProfileAccessRule) getObject();
		if (!rule.getProfileAttribute().getType().equals(rule.getTargetAttribute().getType()))
			throw new DataException("store.access.profile.types");
		// TODO Auto-generated method stub
	}

}
