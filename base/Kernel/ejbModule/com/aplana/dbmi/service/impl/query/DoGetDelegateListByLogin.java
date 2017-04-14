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

import java.util.List;

import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetDelegateListByLogin extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
    public Object processQuery() throws DataException {
        String sql = "SELECT DISTINCT \r\n" +
        		     "  p.person_login \r\n" +
        		     "FROM delegation d \r\n" +
        		     "  JOIN person p ON (d.src_person_id = p.person_id) \r\n" +
        		     "  JOIN person dp ON (d.delegate_person_id = dp.person_id) \r\n" +
        		     "  JOIN attribute_value av ON (av.attribute_code = 'DLGT_ID' and av.template_id = 2290 and av.number_value = d.delegation_id) \r\n" +
        		     "  JOIN card c ON (c.card_id = av.card_id) \r\n" +
        		     "WHERE ((d.start_at <= CURRENT_DATE AND d.end_at >= CURRENT_DATE) \r\n" +
        		     "  OR (d.start_at is null AND d.end_at >= CURRENT_DATE) \r\n" +
        		     "  OR (d.start_at <= CURRENT_DATE AND d.end_at is null) \r\n" +
        		     "  OR (d.start_at is null AND d.end_at is null)) \r\n" +
        		     "  AND c.status_id = 67425 \r\n" +
        		     "  AND dp.person_login = ?" ;
        GetDelegateListByLogin action = (GetDelegateListByLogin)getAction();
        String login = action.getLogin();
        @SuppressWarnings("unchecked")
		List<String> list = getJdbcTemplate().queryForList(sql, new Object[] {login}, String.class);

		if(logger.isTraceEnabled()) {
			// ������� ���� ������ ������� �� �������������
			String traceString = "[TRACE_DELEGATE] stackTrace for " + login + ": \r\n";
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stack) {
				traceString += " " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")\r\n";
			}
			logger.trace(traceString);
		}
        return list;
    }
}
