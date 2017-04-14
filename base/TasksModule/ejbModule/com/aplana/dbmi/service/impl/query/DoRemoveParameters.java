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
import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.action.RemoveParameters;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoRemoveParameters extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		RemoveParameters removeParameters = (RemoveParameters)getAction();
		final String task_id = removeParameters.getTask_id(); 
		final List<Long> paramIds = removeParameters.getParamIds();
		
		final String sql = "delete from scheduler_parameter where task_id = ? and param_id in ("+IdUtils.makeLongStringLine(paramIds, ",")+")";

		int deleteCount = getJdbcTemplate().update(sql, new Object[]{task_id},
				new int[] { Types.VARCHAR });
		logger.debug("There are "+deleteCount+" params delete for sql:/n"+sql);
		return new Long(deleteCount);	
	}
}