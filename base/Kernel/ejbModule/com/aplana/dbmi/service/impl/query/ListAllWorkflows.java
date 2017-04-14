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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all {@link Workflow} objects defined in system 
 */
public class ListAllWorkflows extends QueryBase {

	/**
	 * Fetches all {@link Workflow} objects defined in system
	 * @return list of {@link Workflow} objects representing all {@link Workflow workflows}
	 * defined in system
	 */
	public Object processQuery() throws DataException {
		return getJdbcTemplate().query(
				"select workflow_id, initial_status_id, name_rus, name_eng, is_active from workflow",
				new RowMapper() {
					public Object mapRow(ResultSet rs, int index) throws SQLException {
						ObjectId workflowId = new ObjectId(Workflow.class, new Long(rs.getLong(1)));
						Workflow res = (Workflow)DataObject.createFromId(workflowId);
						res.setInitialState(CardState.getId(rs.getInt(2)));
						res.getName().setValueRu(rs.getString(3));
						res.getName().setValueEn(rs.getString(4));
						res.setActive(rs.getLong(5) != 0);
						return res;
					}
				}
			);
	}

}
