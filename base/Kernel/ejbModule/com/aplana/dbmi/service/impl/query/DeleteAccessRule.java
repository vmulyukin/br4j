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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public abstract class DeleteAccessRule extends ObjectQueryBase {

	public String getEvent() {
		return "DEL_RULE";
	}

	protected void deleteOperationAndRule(ObjectId id) {
		int rows = 0;
		rows += getJdbcTemplate().update(
				"DELETE FROM access_card_rule WHERE rule_id=?",
				new Object[] { id.getId() });
		rows += getJdbcTemplate().update(
				"DELETE FROM access_template_rule WHERE rule_id=?",
				new Object[] { id.getId() });
		rows += getJdbcTemplate().update(
				"DELETE FROM access_move_rule WHERE rule_id=?",
				new Object[] { id.getId() });
		rows += getJdbcTemplate().update(
				"DELETE FROM access_attr_rule WHERE rule_id=?",
				new Object[] { id.getId() });
		if (rows != 1)
			logger.warn("Integrity warning: " + rows + " rows deleted totally " +
					"from access_xxx_rule tables for rule " + id.getId());
		getJdbcTemplate().update(
				"DELETE FROM access_rule WHERE rule_id=?",
				new Object[] { id.getId() });
	}
}
