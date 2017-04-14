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

import com.aplana.dbmi.model.WorkflowMoveRequiredField;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

public class ListWorkflowMoveRequiredFields extends ChildrenQueryBase {

	public Object processQuery() throws DataException {

		final StringBuffer sql = new StringBuffer();
		sql.append("SELECT wmr.wfm_id \n");
		sql.append("       , wmr.template_attr_id \n");
		sql.append("       , ta.attribute_code \n");
		sql.append("       , wmr.must_be_set \n");
		sql.append("FROM   workflow_move_required_field wmr \n");
		sql.append("       , template_attribute ta \n");
		sql.append("WHERE  wmr.template_attr_id = ta.template_attr_id \n");
		sql.append("   AND ta.template_id = (?) AND wmr.is_simple = true \n");

		RowMapper rowMapper = new RowMapper() {
			public Object mapRow(ResultSet rs, int index) throws SQLException {
				
				final WorkflowMoveRequiredField w 
					= new WorkflowMoveRequiredField();
				
				// w.setRequired(true);
				w.setWorkflowMoveId(Long.valueOf(rs.getLong(1)));
				w.setTemplateAttributeId(Long.valueOf(rs.getLong(2)));
				w.setAttributeCode(rs.getString(3));
				
				try {
					w.setMustBeSetCode(rs.getInt(4));
				} catch (DataException ex) {
					ex.printStackTrace();
					final SQLException sqlEx 
						= new SQLException("Invalid value at column ' workflow_move_required_field::must_be_set'");
					sqlEx.initCause(ex);
					throw sqlEx;
				}
				
				return w;
			}
		};

		return getJdbcTemplate().query(sql.toString(), 
				new Object[] { getParent().getId() }, 
				new int[] { Types.NUMERIC }, 
				rowMapper);

	}

}
