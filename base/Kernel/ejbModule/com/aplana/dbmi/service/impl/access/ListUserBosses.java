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
package com.aplana.dbmi.service.impl.access;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

public class ListUserBosses extends ChildrenQueryBase {

	public static final ObjectId ATTR_MANAGER =
		ObjectId.predefined(PersonAttribute.class, "jbr.arm.manager");
	public static final ObjectId ATTR_ASSISTANT =
		ObjectId.predefined(PersonAttribute.class, "boss.assistant");
	
	public Object processQuery() throws DataException {
		return getJdbcTemplate().query(
				"SELECT v_ast.number_value FROM attribute_value v_ast " +
				"JOIN attribute_value v_usr " +
					"ON v_ast.card_id=v_usr.card_id AND v_usr.attribute_code=? " +
				"WHERE v_ast.attribute_code=? AND v_usr.number_value=?",
				new Object[] { ATTR_MANAGER.getId(), ATTR_ASSISTANT.getId(), getParent().getId() },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new ObjectId(Person.class, rs.getLong(1));
					}
				});
	}
}
