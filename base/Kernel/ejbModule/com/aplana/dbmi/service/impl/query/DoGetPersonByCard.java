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
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Query used to get {@link Person} by card ID
 */
public class DoGetPersonByCard extends ActionQueryBase {

    protected String getSqlQuery(boolean isIncludeInactive) {

        String sql = "SELECT \n" +
        "p.person_id, p.person_login, p.full_name, p.email, p.sync_date, p.is_active, \n" +   // 1-6
        "p.locked_by, p.lock_time, p.card_id \n" +                                            // 7-9
        "FROM person p \n" +
        "WHERE p.card_id = ?" + (isIncludeInactive ? "" : " AND p.is_active = 1");
        
        return sql;
    }

    protected RowMapper getRowMapper() {
        return 
            new RowMapper() {
                public Object mapRow(ResultSet rs, int rowNum) throws SQLException
                {
                    Person person = new Person();
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
                    return person;
                }
            };
    }
    
    public Object processQuery() throws DataException
	{
		int count = getJdbcTemplate().queryForInt(
				"SELECT count(1) from person WHERE is_active=1 and card_id = ?",
				getParams()
		);

		boolean isIncludeInactive = count == 0;
 
		List result = getJdbcTemplate().query(
				getSqlQuery(isIncludeInactive),
				getParams(),
				getRowMapper());
		if (null == result || result.isEmpty()){
			return null;
		} else {
			return result.get(0);
		}
		
	}
	
	private Object[] getParams()
	{
		return new Object[] {
			((GetPersonByCard)getAction()).getCardId().getId()
		};
	}
	
}
