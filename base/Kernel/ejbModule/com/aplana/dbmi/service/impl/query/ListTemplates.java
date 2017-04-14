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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.model.filter.TemplateForCreateNewCard;
import com.aplana.dbmi.model.filter.TemplateForSearchFilter;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all {@link Template} objects defined in DBMI database
 */
public class ListTemplates extends QueryBase
{
	/**
	 * Fetches all {@link Template} objects defined in DBMI database.<br>
	 * NOTE: {@link Template} objects returned by this methods are not fully initialized
	 * and misses information about {@link TemplateBlock blocks} and {@link Attribute attributes}
	 * comprising template.
	 * @return list containing all {@link Template} objects defined in DBMI database.
	 */
	public Object processQuery() throws DataException {
		
		String sql = "SELECT template_id, template_name_rus, template_name_eng, is_active," + // 1-4
					"locked_by, lock_time, show_in_createcard, show_in_search FROM template t"; // 5-8
		
		sql = sql + getFilterClause();  
		
		return getJdbcTemplate().query(
				sql,
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
						template.setShowInCreateCard(rs.getLong(7) != 0);
						template.setShowInSearch(rs.getLong(8) != 0);
						return template;
					}
		});
	}

	private String getFilterClause() {

		String sql = "";

		Filter filter = getFilter();

		if (filter != null) {
			if (filter instanceof TemplateForCreateNewCard) {
				sql += " where t.show_in_createcard = 1 and t.is_active = 1";
			} else if (filter instanceof TemplateForSearchFilter) {
				sql += " where t.show_in_search = 1";
			} else if (filter instanceof TemplateIdListFilter) {

				TemplateIdListFilter idListfilter = (TemplateIdListFilter) filter;
				Collection filterTemplateIds = idListfilter.getTemplateIds();

				if (!filterTemplateIds.isEmpty()) 
					sql += " where  (t.template_id in ("
							+ ObjectIdUtils.numericIdsToCommaDelimitedString(filterTemplateIds) + "))";

			}
		}

		return sql;
	}

	protected boolean supportsFilter(Class type) {
		return TemplateForSearchFilter.class.equals(type)
			|| TemplateForCreateNewCard.class.equals(type)
			|| TemplateIdListFilter.class.equals(type);
	}
}