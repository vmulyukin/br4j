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

import com.aplana.dbmi.model.AccessAttribute;
import com.aplana.dbmi.model.AccessCard;
import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.AccessTemplate;
import com.aplana.dbmi.model.AccessWorkflowMove;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

public abstract class SaveAccessRule extends SaveQueryBase {
	
	protected void processUpdate() throws DataException {
		throw new IllegalStateException("Update operation not supported for access rule objects");
	}

	public void validate() throws DataException {
		AccessRule rule = (AccessRule) getObject();
		if (rule.getAccessOperation() == null)
			throw new DataException("store.accessrule.nooperation");
	}

	public String getEvent() {
		return "NEW_RULE";
	}

	protected long insertRuleAndOperation(AccessRule rule) {

		long id = 0;

		if (rule.getAccessOperation() instanceof AccessCard) {
			AccessCard opCard = (AccessCard) rule.getAccessOperation();
            logger.info("try generate new id for rule: template_id="+opCard.getTemplate()+" and status_id="+opCard.getStatus());
			id = getJdbcTemplate().queryForLong(
					"INSERT INTO access_rule (template_id, status_id) " +
					"VALUES (?, ?) " +
					"RETURNING rule_id",
					new Object[] {
							safeGetId(opCard.getTemplate()),
							safeGetId(opCard.getStatus()) },
					new int[] { Types.NUMERIC, Types.NUMERIC });
			getJdbcTemplate().update(
					"INSERT INTO access_card_rule(rule_id, operation_code) " +
					"VALUES(?, ?)",
					new Object[] { new Long(id), opCard.getOperation() }, 
					new int[] { Types.NUMERIC, Types.VARCHAR });

		} else if (rule.getAccessOperation() instanceof AccessTemplate) {
			AccessTemplate opTemplate = (AccessTemplate) rule.getAccessOperation();
            logger.info("try generate new id for rule: template_id="+opTemplate.getTemplate());
			id = getJdbcTemplate().queryForLong(
					"INSERT INTO access_rule (template_id, status_id) " +
					"VALUES (?, NULL) " +
					"RETURNING rule_id",
					new Object[] {
							safeGetId(opTemplate.getTemplate()) },
					new int[] { Types.NUMERIC });
			getJdbcTemplate().update(
					"INSERT INTO access_template_rule(rule_id, operation_code) " +
					"VALUES(?, ?)",
					new Object[] { new Long(id), opTemplate.getOperation() }, 
					new int[] { Types.NUMERIC, Types.VARCHAR });

		} else if (rule.getAccessOperation() instanceof AccessWorkflowMove) {
			AccessWorkflowMove opMove = (AccessWorkflowMove) rule.getAccessOperation();
            logger.info("try generate new id for rule: template_id="+opMove.getTemplate());
			id = getJdbcTemplate().queryForLong(
					"INSERT INTO access_rule (template_id, status_id) " +
						"SELECT ?, from_status_id FROM workflow_move " +
						"WHERE wfm_id=? " +
						"UNION ALL " +
						"SELECT ?, NULL WHERE ? IS NULL " +
						"RETURNING rule_id",
					new Object[] {
							safeGetId(opMove.getTemplate()),
							safeGetId(opMove.getMove()),
							safeGetId(opMove.getTemplate()),
							safeGetId(opMove.getMove()) },
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC,
							Types.NUMERIC });
			getJdbcTemplate().update(
					"INSERT INTO access_move_rule(rule_id, wfm_id) " +
					"VALUES(?, ?)",
					new Object[] { new Long(id), safeGetId(opMove.getMove()) }, 
					new int[] { Types.NUMERIC, Types.NUMERIC });

		} else if (rule.getAccessOperation() instanceof AccessAttribute) {
			AccessAttribute opAttr = (AccessAttribute) rule.getAccessOperation();
            logger.info("try generate new id for rule: template_id="+opAttr.getTemplate()+" and status_id="+opAttr.getStatus());
			id = getJdbcTemplate().queryForLong(
					"INSERT INTO access_rule (template_id, status_id) " +
					"VALUES (?, ?) " +
					"RETURNING rule_id",
					new Object[] {
							safeGetId(opAttr.getTemplate()),
							safeGetId(opAttr.getStatus()) },
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.NUMERIC });
			getJdbcTemplate().update(
					"INSERT INTO access_move_rule(rule_id, operation_code, attribute_code, wfm_id) " +
					"VALUES(?, ?, ?, ?)",
					new Object[] {
							new Long(id),
							opAttr.getOperation(),
							safeGetId(opAttr.getAttribute()),
							safeGetId(opAttr.getMove()) }, 
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC });
		} else
			throw new IllegalArgumentException("Unknown rule operation type: " +
					rule.getAccessOperation().getClass());
		return id;
	}
	
	protected Object safeGetId(ObjectId id) {
		return id == null ? null : id.getId();
	}
	
	protected Object safeGetId(DataObject obj) {
		return obj == null ? null : safeGetId(obj.getId());
	}
}
