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

import com.aplana.dbmi.action.LoadParameters;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.model.SchedulerParameter;;

public class DoLoadParameters  extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		LoadParameters loadParameters = (LoadParameters)getAction();
		final String task_id = loadParameters.getTask_id(); 
		
		final String sql = "select param_id, param_data from scheduler_parameter where task_id = ?";

		List paramList = getJdbcTemplate().query(sql, new Object[]{task_id},
				new int[] { Types.VARCHAR },
				new RowMapper() {
			
			public Object mapRow(final ResultSet rs, final int arg1) throws SQLException {
				SchedulerParameter param = new SchedulerParameter();
				param.setParamId(rs.getLong(1));
				param.setParamValue(rs.getBytes(2));
				return param;
			}
		});
		logger.debug("There are "+paramList.size()+" params for sql with task_id "+task_id+":"+sql);
		return paramList;	
	}
}