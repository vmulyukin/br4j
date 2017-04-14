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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;

/**
 * Utility class used to generate human-readable {@link Search#getSummary() summary}
 * for given {@link Search} object.
 */
public class BuildSearchSummary
{
	protected Log logger = LogFactory.getLog(getClass());
		
	private JdbcTemplate jdbc;
	private Search search;

	/**
	 * Gets JdbcTemplate used for summary generation
	 * @return JdbcTemplate instance used for summary generation
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}
	
	/**
	 * Sets JdbcTemplate to be used for summary generation
	 * @param jdbc JdbcTemplate instance pointing to DBMI database
	 */
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	/**
	 * Gets {@link Search} object to generate summary for
	 * @return {@link Search} object to generate summary for
	 */
	public Search getSearch() {
		return search;
	}
	
	/**
	 * Sets {@link Search} object to generate summary for
	 * @param search {@link Search} object to generate summary for
	 */
	public void setSearch(Search search) {
		this.search = search;
	}

	/**
	 * Initializes {@link Search#getSummary() summary-related} properties of 
	 * {@link Search} object associated with this {@link BuildSearchSummary} instance.
	 */
	public void buildSummary()
	{
		try {
			ResourceBundle messagesRu = ResourceBundle.getBundle(ContextProvider.MESSAGES,
					ContextProvider.LOCALE_RUS);
			ResourceBundle messagesEn = ResourceBundle.getBundle(ContextProvider.MESSAGES,
					ContextProvider.LOCALE_ENG);
			String templatesRu = "";
			String templatesEn = "";
			if (search.getTemplates() == null || search.getTemplates().size() == 0) {
				templatesRu = messagesRu.getString("common.all");
				templatesEn = messagesEn.getString("common.all");
			} else {
				Iterator itr = search.getTemplates().iterator();
				while (itr.hasNext()) {
					Object item = itr.next();
					if (!(item instanceof Template))
						continue;
					Template template = (Template) jdbc.queryForObject(
							"SELECT template_id, template_name_rus, template_name_eng \n" +
							"FROM template \n" + 
							"WHERE template_id=? \n",
							new Object[] { ((Template) item).getId().getId() },
							new int[] { Types.NUMERIC }, // (2010/03 POSTGRE)
							new RowMapper() {
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									Template template = new Template();
									template.setId(rs.getLong(1));
									template.setNameRu(rs.getString(2));
									template.setNameEn(rs.getString(3));
									return template;
								}
							});
					templatesRu += template.getNameRu();
					templatesEn += template.getNameEn();
					if (itr.hasNext()) {
						templatesRu += ", ";
						templatesEn += ", ";
					}
				}
			}
			String regionsRu = "";
			String regionsEn = "";
			if (search.getAttribute(Attribute.ID_REGION) == null) {
				regionsRu = messagesRu.getString("common.all");
				regionsEn = messagesEn.getString("common.all");
			} else {
				Iterator itr = ((Collection) search.getAttribute(Attribute.ID_REGION)).iterator();
				while (itr.hasNext()) {
					Object item = itr.next();
					if (!(item instanceof ReferenceValue))
						continue;
					ReferenceValue region = (ReferenceValue) jdbc.queryForObject(
							"SELECT value_id, value_rus, value_eng FROM values_list WHERE value_id=?",
							new Object[] { ((ReferenceValue) item).getId().getId() },
							new int[] { Types.NUMERIC },
							new RowMapper() {
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									ReferenceValue region = new ReferenceValue();
									region.setId(rs.getLong(1));
									region.setValueRu(rs.getString(2));
									region.setValueEn(rs.getString(3));
									return region;
								}
							});
					regionsRu += region.getValueRu();
					regionsEn += region.getValueEn();
					if (itr.hasNext()) {
						regionsRu += ", ";
						regionsEn += ", ";
					}
				}
			}
			search.setSummaryRu(MessageFormat.format(messagesRu.getString("search.summary"),
					new Object[] { search.getWords(), templatesRu, regionsRu }));
			search.setSummaryEn(MessageFormat.format(messagesEn.getString("search.summary"),
					new Object[] { search.getWords(), templatesEn, regionsEn }));
		} catch (DataAccessException e) {
			logger.error("Error building search summary", e);
			//TODO Somehow fill the summary?
		}
	}
}
