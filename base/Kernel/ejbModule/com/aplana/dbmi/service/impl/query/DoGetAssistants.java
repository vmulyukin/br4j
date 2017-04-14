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
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.jbr.action.GetAssistants;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetAssistants extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		final GetAssistants action = (GetAssistants)getAction();
		if (action == null || action.getChiefIds() == null) 
			return null;
		final List<?> assistants = getAssistant(action.getChiefIds(), action.getChiefRoleIds());
		return assistants;
	}

	private List<?> getAssistant( Collection<?> chiefIds, Collection<?> chiefRoleIds) 
	{
		final String sChiefIds = 
					ObjectIdUtils.numericIdsToCommaDelimitedString(chiefIds);
		final String sChiefRoles =
					(chiefRoleIds == null || chiefRoleIds.isEmpty())
						? null 
						: IdUtils.makeIdCodesQuotedEnum(chiefRoleIds)
					;
		final List<?> assistants = getJdbcTemplate().query(
				"select av_a.number_value \n" +
				"from attribute_value av_m, attribute_value av_a \n" +
				"where \n" +	
				"\t\t av_m.attribute_code = 'JBR_ARM_MANAGER' \n" +
				"\t\t and av_m.number_value in (" + sChiefIds + ") \n" +
				"\t\t and av_m.card_id = av_a.card_id and av_a.attribute_code = 'JBR_ARM_ASSISTANT' \n" +
				( (sChiefRoles == null)
						? "" 
						: "\t\t and exists ( select 1 from person_role prole where prole.person_id=av_m.number_value \n" +
						  "\t\t\t and prole.role_code in ("+ sChiefRoles +")) \n"
				) 
				,
				new RowMapper() {
					public Object mapRow( ResultSet rs, int rowNum) 
						throws SQLException 
					{
						Long person_id = new Long(rs.getLong(1));
						return new ObjectId(Person.class, person_id);
					}
				}
			);
		return assistants;
	}
}
