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
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Workflow} instance from database
 */
public class GetWorkflow extends ObjectQueryBase {
	/**
	 * Fetches single {@link Workflow} instance from database
	 * @return fetched {@link Workflow} instance
	 */
	public Object processQuery() throws DataException {
		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				Workflow res = (Workflow)DataObject.createFromId(getId());
				res.setInitialState(CardState.getId(rs.getInt(1)));
				res.getName().setValueRu(rs.getString(2));
				res.getName().setValueEn(rs.getString(3));
				res.setActive(rs.getLong(4) != 0);
				return res;
			}
		}; 
		
		Workflow result = (Workflow)getJdbcTemplate().queryForObject(
			"select initial_status_id, name_rus, name_eng, is_active" +
			" from workflow where workflow_id = ?",
			new Object[] {getId().getId()},
			new int[] {Types.NUMERIC },
			rowMapper
		);
		
		ChildrenQueryBase q = getQueryFactory().getChildrenQuery(Workflow.class, WorkflowMove.class);
		q.setParent(getId());
		result.setMoves((List)getDatabase().executeQuery(getUser(), q));
		return result;
	}
}
