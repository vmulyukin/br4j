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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.CardAccess;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * @deprecated CardAccess objects replaced with {@link com.aplana.dbmi.model.AccessRule}.
 */
public class ListTemplateCardAccess extends ChildrenQueryBase {

	public Object processQuery() throws DataException {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				ObjectId recId = new ObjectId(CardAccess.class, rs.getLong(1));
				CardAccess result = (CardAccess)DataObject.createFromId(recId);
				result.setTemplateId(new ObjectId(Template.class, rs.getLong(2)));
				Long permissionType = new Long(rs.getLong(3));
				result.setPermissionType(permissionType);
				if (CardAccess.WORKFLOW_MOVE.equals(permissionType)) {
					result.setObjectId(new ObjectId(WorkflowMove.class, rs.getLong(4)));
				} else if (CardAccess.READ_CARD.equals(permissionType) || CardAccess.EDIT_CARD.equals(permissionType)) {
					result.setObjectId(new ObjectId(CardState.class, rs.getLong(4)));					
				}
				if (rs.getObject(5) != null) {
					result.setRoleId(new ObjectId(SystemRole.class, rs.getString(5)));
				}
				if (rs.getObject(6) != null) {
					result.setPersonAttributeId(new ObjectId(PersonAttribute.class, rs.getString(6)));	
				}
				return result;
			}
		};
		return getJdbcTemplate().query(
			"select rec_id, template_id, permission_type, object_id, role_code, " +
			"person_attribute_code from card_access where template_id = ?",
			new Object[] {getParent().getId()},
			new int[] {Types.NUMERIC},
			rowMapper
		);
	}

}
