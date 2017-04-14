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
import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.GetPersonByCardId;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetPersonByCardId extends ActionQueryBase {

    private static final long serialVersionUID = 1L;

	protected String getSqlQuery() {
        String sql = "SELECT " +
        "p.person_id, p.person_login, p.full_name, p.email, p.sync_date, p.is_active, " +   // 1-6
        "p.locked_by, p.lock_time, p.card_id " +                                            // 7-9
        "FROM person p " +
        "WHERE p.is_active=1 AND p.card_id in ({0})";
        
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
    	GetPersonByCardId action = (GetPersonByCardId) getAction();
    	if(action == null || CollectionUtils.isEmpty(action.getIds())) {
    		return null;
    	}
    	
    	List<?> result = getJdbcTemplate().query(
    			MessageFormat.format(getSqlQuery(),
    					IdUtils.makeIdCodesEnum(action.getIds(),",")),
				getRowMapper());
		if (result==null){
			return null;
		} else {
			return result;
		}
		
	}
	
}
