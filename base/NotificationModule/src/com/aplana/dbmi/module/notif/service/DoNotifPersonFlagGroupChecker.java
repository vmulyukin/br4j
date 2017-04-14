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
package com.aplana.dbmi.module.notif.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.aplana.dbmi.action.GetResponsibleDowRecipients;
import com.aplana.dbmi.action.MarkMessageRead;
import com.aplana.dbmi.action.NotifPersonFlagGroupChecker;
import com.aplana.dbmi.model.Message;
import com.aplana.dbmi.model.MessageGroup;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoNotifPersonFlagGroupChecker extends ActionQueryBase {

	@Override
	@SuppressWarnings("unchecked")
	public Object processQuery() throws DataException {
		NotifPersonFlagGroupChecker action = (NotifPersonFlagGroupChecker) getAction();
		Set<Long> ids = new HashSet<Long>();
		ids.add(new Long(-1));
		for(Person p: action.getPersons()){
			ids.add((Long)p.getId().getId());
		}
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", ids);
		parameters.addValue("field", action.getFieldId().getId());
		parameters.addValue("flag", action.getFlagId().getId());
		
		final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
		
		
		Collection<Person> filteredPersons = (Collection<Person>) namedParameterJdbcTemplate.query(
			"select distinct p.person_id, p.person_login, p.full_name, p.email, p. card_id \n" +
				"from \n"+ 
				"person p \n"+
				"join attribute_value av_p on p.card_id = av_p.card_id and av_p.attribute_code = 'JBR_SETTINGS_CARD' \n"+
				"join attribute_value av_s on av_p.number_value = av_s.card_id and av_s.attribute_code = :field and av_s.value_id = :flag \n"+
			"where p.person_id in (:ids)",parameters, new RowMapper() {
				
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					Person recipient = new Person();
					recipient.setId(rs.getLong(1));
					recipient.setLogin(rs.getString(2));
					recipient.setFullName(rs.getString(3));
					recipient.setEmail(rs.getString(4));			
					recipient.setCardId(new ObjectId(Person.class, rs.getLong(5)));	
					return recipient;
				}
			});
		return filteredPersons;
	}

}
