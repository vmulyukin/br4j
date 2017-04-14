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
import java.text.MessageFormat;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetActionName;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetActionName extends ActionQueryBase {

	private static final long serialVersionUID = -5689549587539197161L;

	@Override
	public Object processQuery() throws DataException {
		
		GetActionName action = (GetActionName) getAction();
		
		if(action.getActionCode() == null || action.getActionCode().length() == 0)
			return null;
		
		StringBuilder sql = new StringBuilder();
		
		sqlFormer(sql, action);
		
		final List<?> result =
				getJdbcTemplate().query(sql.toString(), new RowMapper() {
			
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				ObjectId recId = new ObjectId(LogAction.class, rs.getString(1));
				LogAction res = (LogAction)DataObject.createFromId(recId);
				res.getName().setValueRu(rs.getString(2));
				res.getName().setValueEn(rs.getString(3));
				return res;
			}
		});
		
		return (result != null && !result.isEmpty()) ? result.get(0) : null;		
	}
	
	private void sqlFormer(StringBuilder sql, GetActionName action) {
		
		sql.append("select \n");
		sql.append("action_code, action_name_rus, action_name_eng \n"); //1,2,3
		sql.append("from action \n");
		sql.append("where (1=1) \n");
		sql.append(MessageFormat.format(
				" and action_code = {0} \n",
				new Object[] {"'" + action.getActionCode() + "'"}
			));		
	}
}
