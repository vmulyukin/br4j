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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetResponsibleDowRecipients;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetResponsibleDowRecipients extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		GetResponsibleDowRecipients action = (GetResponsibleDowRecipients) getAction();
		ObjectId documentId = action.getObjectId();
		return getJdbcTemplate().query(
			"select distinct p.person_id, p.person_login, p.full_name, p.email, p. card_id \n"+ 
				"from attribute_value av_doc \n"+
				"join attribute_value av_zdow on av_doc.number_value = av_zdow.card_id and av_zdow.attribute_code = 'JBR_DOWZONE_DEPLIST' \n"+
				"join attribute_value av_dep on av_zdow.number_value =  av_dep.card_id AND av_dep.attribute_code = 'JBR_DEPT_RESP_DOW' \n"+
				"join person p on p.person_id = av_dep.number_value \n"+
			"where av_doc.attribute_code = 'JBR_ZONE_DOW' \n"+
			"and av_doc.card_id = ?",
				new Object[] {documentId.getId()},
				new RowMapper() {
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
	}

}
