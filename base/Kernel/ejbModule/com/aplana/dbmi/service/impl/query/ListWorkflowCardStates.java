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

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all {@link CardState} instances
 * related to given parent {@link Workflow} object.
 * @author dsultanbekov
 */
public class ListWorkflowCardStates extends ChildrenQueryBase {

	/**
	 * Returns all {@link CardState} instances which are referenced by {@link WorkflowMove workflow moves}
	 * included in given parent {@link Workflow} object.
	 * @return List of {@link CardState} instances related to given parent {@link Workflow}. 
	 */
	public Object processQuery() {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				ObjectId recId = new ObjectId(CardState.class, rs.getLong(1));
				CardState result = (CardState)DataObject.createFromId(recId);
				result.getName().setValueRu(rs.getString(2));
				result.getName().setValueEn(rs.getString(3));
				result.getDefaultMoveName().setValueRu(rs.getString(4));
				result.getDefaultMoveName().setValueEn(rs.getString(5));
				return result;
			}
		};
		String sql = "select status_id, name_rus, name_eng" +
				", default_move_name_rus, default_move_name_eng" +
				" from card_status cs where exists (" +
				"select 1 from workflow w left outer join workflow_move wm " +
				"on (w.workflow_id = wm.workflow_id) where" +
				" w.workflow_id = ? " +
				" and cs.status_id in (w.initial_status_id, wm.from_status_id, wm.to_status_id))" +
				" order by cs.status_id";
		
		return getJdbcTemplate().query(
			sql, 
			new Object[] {getParent().getId()}, 
			new int[] {Types.NUMERIC}, 
			rowMapper
		);
	}
}
