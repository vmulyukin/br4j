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

import com.aplana.dbmi.action.IsUserDepartamentChief;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class IsUserDepartamentChiefActionQuery extends ActionQueryBase {

    /* (non-Javadoc)
     * @see com.aplana.dbmi.service.impl.QueryBase#processQuery()
     */
    @Override
    public Object processQuery() throws DataException {
    	IsUserDepartamentChief action = (IsUserDepartamentChief) getAction();
    	
        Long userId = (Long) getUser().getPerson().getId().getId();
        
        if(!action.isDelegate()
        		&& getRealUser() != null
        		&& getRealUser().getPerson() != null) {
        	
        	userId = (Long) getRealUser().getPerson().getId().getId();
        }
        
        return getJdbcTemplate().query(
                "SELECT 1 FROM attribute_value av " +
                "JOIN person p ON ( av.number_value = p.card_id) " +
                "WHERE av.attribute_code = 'JBR_DEPT_CHIEF' " +
                "AND p.person_id = " + userId +
                " UNION " +
                "SELECT 1 FROM person_role " +
                "WHERE role_code IN ('A', 'JBR_DELEGATE_MGR', 'A_USERS','A_DICTIONARIES','A_TEMPLATES','A_JOURNAL','A_STATISTIC','A_TASKS','A_PROCESSES') " + // �������������� - �������������� � ������������
                "AND person_id = " + userId,
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getBoolean(1);
                    }
                }
            );
    }

}
