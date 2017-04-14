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
* ������:
* ���������� ������ ������������� �� ������������ �������� ������������ ��� 
* ���� �������������, ���� ������� ������������ - �������������
*/
package com.aplana.dbmi.service.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonView;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;


public class GetUserViewsFromSameDepartament extends ActionQueryBase {

    @Override
    public Object processQuery() throws DataException {
        Long userId = (Long) getUser().getPerson().getId().getId();
        String sql = getQuery(userId);

        return getJdbcTemplate().query(
                sql, new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
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
                        if( !person.isActive() )
                        {
                        	logger.warn( "including inactive person : " + person.getFullName() );
                        }
                        return person;
                    }
                }
            );
    }

    public static String getPersonIdsQuery(Long userId) {
        return "SELECT  p.person_id " + getFromClause(userId);
    }

    private static String getQuery(Long userId) {
        return "SELECT  p.person_id, p.person_login,p.full_name, p.email, p.sync_date, " +
                "p.is_active, p.locked_by, p.lock_time, p.card_id, " +
                "av_fname.string_value, av_mname.string_value, av_lname.string_value " + getFromClause(userId);
    }


    private static String getFromClause(Long userId) {
        return "FROM person p " +
            "JOIN attribute_value av_fname ON " +
                "(av_fname.card_id = p.card_id AND av_fname.attribute_code = 'JBR_PERS_NAME') " +
            "JOIN attribute_value av_mname ON " +
                "(av_mname.card_id = p.card_id AND av_mname.attribute_code = 'JBR_PERS_MNAME') " +
            "JOIN attribute_value av_lname ON " +
                "(av_lname.card_id = p.card_id AND av_lname.attribute_code = 'JBR_PERS_SNAME') " +
            "JOIN card p_card ON p.card_id = p_card.card_id " +
            "WHERE " +
             "(" +
                "( " +
                        "EXISTS " +
                        "( " +
                            "SELECT 1 " +
                            "FROM person p_o " +
                            "JOIN person_role pr ON (pr.person_id = p_o.person_id AND pr.role_code IN ('A', 'JBR_DELEGATE_MGR', 'A_USERS','A_DICTIONARIES','A_TEMPLATES','A_JOURNAL','A_STATISTIC','A_TASKS','A_PROCESSES') ) " +
                            "WHERE p_o.person_id = " + userId + " " +
                        ") " +
//                    "AND " +
//                        "p.person_id IN " +
//                        "( " +
//                            "SELECT p_do.person_id " +
//                            "FROM person p_do " +
//                        ") " +
                ") OR ( " +
                        "NOT EXISTS " +
                        "( " +
                            "SELECT 1 " +
                            "FROM person p_o " +
                            "JOIN person_role pr ON (pr.person_id = p_o.person_id AND pr.role_code IN ('A', 'JBR_DELEGATE_MGR', 'A_USERS','A_DICTIONARIES','A_TEMPLATES','A_JOURNAL','A_STATISTIC','A_TASKS','A_PROCESSES') ) " +
                            "WHERE p_o.person_id = " + userId + " " +
                        ") " +
                    "AND " +
                         "p.person_id IN " +
                         "( " +
                            "SELECT p.person_id " +
                            "FROM person p " +
                            "JOIN attribute_value p_av ON (p_av.card_id = p.card_id) " +
                            "WHERE p_av.number_value IN ( " +
                                "WITH RECURSIVE departs AS " +
                                "( " +
                                    "SELECT " +
                                    "( " +
                                        "SELECT av.number_value " +
                                        "FROM person p " +
                                        "JOIN attribute_value av ON (av.card_id = p.card_id AND av.attribute_code = 'JBR_PERS_DEPT_LINK') " +
                                        "WHERE p.person_id = " + userId + " " +
                                    ") AS card_id " +

                                    "UNION " +

                                    "SELECT av.card_id " +
                                    "FROM attribute_value av " +
                                    "JOIN departs dep ON (dep.card_id = av.number_value) " +
                                    "WHERE av.attribute_code = 'JBR_DEPT_PARENT_LINK' " +
                                ") SELECT card_id FROM departs " +
                            ") " +
                        ") " +
                ")" +
              ") AND p.is_active=1 AND p_card.status_id=20"; // select only active users to add in list
    }

}
