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
import java.sql.Types;
import java.util.Collection;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMoveRequiredField;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Template} instance from database
 */
public class GetTemplate extends ObjectQueryBase
{
	/**
	 * Fetches single {@link Template} instance from database
	 * @return fully initialized {@link Template} instance containing information about
	 * attribute blocks and individual attributes comprising {@link Template}
	 */
	public Object processQuery() 
		throws DataException 
	{
		final Template template = (Template) getJdbcTemplate().queryForObject(
				"SELECT " +
				"template_id" +			// 1
				", template_name_rus" +	// 2
				", template_name_eng" +	// 3
				", is_active" +			// 4
				", locked_by" +			// 5
				", lock_time" +			// 6
				", is_system" +			// 7
				", workflow_id" +		// 8
				", show_in_createcard" +	// 9
				", show_in_search" +		// 10				
				" FROM template WHERE template_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC }, // (2010/03) POSTGRE
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Template template = new Template();
						template.setId(rs.getLong(1));
						template.setNameRu(rs.getString(2));
						template.setNameEn(rs.getString(3));
						template.setActive(rs.getBoolean(4));
						if (rs.getObject(5) != null) {
							template.setLocker(rs.getLong(5));
							template.setLockTime(rs.getTimestamp(6));
						}
						//template.setSystem(rs.getBoolean(7)); // DSultanbekov: ������ �� �����������?						
						template.setWorkflow(new ObjectId(Workflow.class, rs.getLong(8)));
						template.setShowInCreateCard(rs.getLong(9) != 0);
						template.setShowInSearch(rs.getLong(10) != 0);
						return template;
					}
				});
		
		ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Template.class, TemplateBlock.class);
		subQuery.setParent(getId());
		template.setBlocks((Collection) getDatabase().executeQuery(getUser(), subQuery));
		
		
		/*subQuery = getQueryFactory().getChildrenQuery(Template.class, CardAccess.class);
		subQuery.setParent(getId());
		template.setCardAccess((Collection) getDatabase().executeQuery(getUser(), subQuery));*/
		
		
		subQuery = getQueryFactory().getChildrenQuery(Template.class, WorkflowMoveRequiredField.class);
		subQuery.setParent(getId());
		template.setWorkflowMoveRequiredFields((Collection) getDatabase().executeQuery(getUser(), subQuery));
		
		
			/*ListTemplateBlocks subQuery = new ListTemplateBlocks();
			subQuery.setJdbcTemplate(getJdbcTemplate());
			subQuery.setParent(getId());
			template.setBlocks((Collection) subQuery.processQuery());*/
		return template;
	}
}
