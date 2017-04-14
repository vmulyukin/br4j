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
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemGroup;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * 
 * @author vialeksandrov
 *
 */

public class ListGroupsByRole extends ChildrenQueryBase {

	@Override
	public Object processQuery() throws DataException {
		
		final ObjectId roleId = getParent();
		
		if(roleId.getId() == null) return null;
		
		final List<SystemGroup> result = new ArrayList<SystemGroup>();
		getJdbcTemplate().query(
				"SELECT sg.group_code, sg.group_name_rus, sg.group_name_eng \n" +
				"FROM system_group sg join group_role gr on sg.group_code = gr.group_code \n" +
				"WHERE gr.role_code = ?",
				new Object[] { roleId.getId() },
				new int[] { Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						SystemGroup group = new SystemGroup();
						group.setId(new ObjectId(SystemGroup.class, rs.getString(1)));
						group.setNameRu(rs.getString(2));
						group.setNameEn(rs.getString(3));
						result.add(group);
						return null;
					}
				});
		
		return result;
	}

}
