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
import com.aplana.dbmi.jbr.action.GetBoss;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetBoss extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		final GetBoss action = (GetBoss)getAction();
		if (action == null || action.getAssistantIds() == null) 
			return null;
		final List<?> bosses = getBoss(action.getAssistantIds());
		return bosses;
	}

	private List<?> getBoss( Collection<ObjectId> assistantId) 
	{
		final String sAssistantId = 
					ObjectIdUtils.numericIdsToCommaDelimitedString(assistantId);

		final List<?> bosses = getJdbcTemplate().query(
				"select av_m.number_value \n" +
						"from    attribute_value av_a  \n" +
						"		join attribute_value av_m on av_m.card_id = av_a.card_id \n" +
						"        join  person pr_boss on pr_boss.person_id=av_m.number_value \n" +
						"where   av_m.attribute_code = 'JBR_ARM_MANAGER' \n" +
						"		and av_a.attribute_code = 'JBR_ARM_ASSISTANT' \n" +
						"        and(av_a.number_value in (" + sAssistantId +"));"

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
		return bosses;
	}
}
