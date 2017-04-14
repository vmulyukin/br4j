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
/**
 * 
 */
package com.aplana.dbmi.service.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonView;

/**
 * @author iKuznetsov
 *
 */
public class ListUserViews extends ListUsers {

    @Override
    protected String getSqlQuery() {
        String sql = "SELECT /*+INDEX(p)*/ " +
        "p.person_id, p.person_login, p.full_name, p.email, p.sync_date, p.is_active, " +   // 1-6
        "p.locked_by, p.lock_time, p.card_id, " +                                            // 7-9
        "av_fname.string_value, av_mname.string_value, av_lname.string_value " +            // 10-12
        "FROM person p " +
        "JOIN attribute_value av_fname ON " +
            "(av_fname.card_id = p.card_id AND av_fname.attribute_code = 'JBR_PERS_NAME') " +
        "JOIN attribute_value av_mname ON " +
            "(av_mname.card_id = p.card_id AND av_mname.attribute_code = 'JBR_PERS_MNAME') " +
        "JOIN attribute_value av_lname ON " +
            "(av_lname.card_id = p.card_id AND av_lname.attribute_code = 'JBR_PERS_SNAME') "+ 
        "WHERE p.is_active=1 AND " + super.getFilterClause();
        
        return sql;
    }

    @Override
    protected RowMapper getRowMapper() {
        return 
        new RowMapper() {
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                PersonView person = new PersonView();
                person.setId(rs.getLong(1));
                person.setLogin(rs.getString(2));
                person.setFullName(rs.getString(3));
                person.setEmail(rs.getString(4));
                person.setSyncDate(rs.getTimestamp(5));
                person.setActive(rs.getBoolean(6));
                if (rs.getObject(7) != null) {
                    person.setLocker(rs.getLong(7));
                    person.setLockTime(rs.getTimestamp(8));
                }
                if (rs.getObject(9) != null) {
                    person.setCardId(new ObjectId(Card.class, rs.getLong(9)));
                }
                person.setFirstName(rs.getString(10));
                person.setMiddleName(rs.getString(11));
                person.setLastName(rs.getString(12));
                return person;
            }
        };
    }
}
