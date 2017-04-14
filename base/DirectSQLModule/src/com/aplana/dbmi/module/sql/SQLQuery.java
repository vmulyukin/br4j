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
package com.aplana.dbmi.module.sql;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;

public class SQLQuery extends ActionQueryBase implements WriteQuery {

	private static final long serialVersionUID = 1L;

	public Object processQuery() throws DataException {
        SQLQueryAction sqa = (SQLQueryAction) getAction();
		if(sqa.isOnlyUpdate()){
			getJdbcTemplate().execute(sqa.getSql());
			return null;
		} else {
			final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
			List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sqa.getSql(), sqa.getParams());
			return result;
		}
    }
//�������� ����� ��� ��������������� ����� �� SQLQueryAction � ���� Types
//���������� ���������� �������� � SQLQueryAction � ���� ������� ����������� 
/*    private int[] convertTypes(int[] types) {
    	int[] result = new int[types.length];
    	for (int i = 0; i < types.length; i++) {
    		switch(types[i]) {
    		case SQLQueryAction.PARAM_STRING:
    			result[i] = Types.VARCHAR;
    			break;
                case SQLQueryAction.PARAM_DATE:
                    result[i] = Types.DATE;
                    break;
                case SQLQueryAction.PARAM_NUMBER:
                    result[i] = Types.NUMERIC;
                    break;
    		default:
    			throw new IllegalArgumentException("Unknown parameter type: " + types[i]);
    		}
    	}
    	return result;
    }
*/
}
