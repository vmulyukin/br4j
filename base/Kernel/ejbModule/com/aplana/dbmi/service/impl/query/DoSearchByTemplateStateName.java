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

import static com.aplana.dbmi.service.impl.workstation.Util.limitAndOffset;
import static com.aplana.dbmi.service.impl.workstation.Util.userPermissionCheck;
import static com.aplana.dbmi.service.impl.workstation.Util.userPermissionCheckWithClause;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.SearchByTemplateStateNameAction;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoSearchByTemplateStateName extends ActionQueryBase{

	@Override
	public Object processQuery() throws DataException {
		SearchByTemplateStateNameAction action = (SearchByTemplateStateNameAction)getAction();
		
		String sql = buildSQL(action);
		List searchResult = new ArrayList();
		if (sql != null && !"".equals(sql)) {
			searchResult =
				this.getJdbcTemplate().query(sql, new RowMapper(){
	
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						return new ObjectId(Card.class, rs.getLong(1));
					}
	
				});
			logger.info( searchResult.size() + " matches found");
		} 	
		return searchResult;
	}
	
private String buildSQL(SearchByTemplateStateNameAction action) {
		
		final StringBuffer sqlBuf = new StringBuffer("");
		if (
			action.getAdditionalFilter() != null && !action.getAdditionalFilter().isEmpty() && 
			action.getTemplates() != null && !action.getTemplates().isEmpty() && 
			action.getStates() != null && !action.getStates().isEmpty()
		) 
		{
			int userId = ((Long)getUser().getPerson().getId().getId()).intValue();
			String templates = ""; 
			sqlBuf.append("SELECT  c.card_id \n");
			sqlBuf.append(	"FROM  card c \n");
			sqlBuf.append(	"JOIN attribute_value AS av \n");
			sqlBuf.append(	" ON av.card_id = c.card_id \n");
			
			templates = ObjectIdUtils.numericIdsToCommaDelimitedString(action.getTemplates());
			sqlBuf.append(MessageFormat.format(
						"\n\t\t AND c.template_id  IN ({0}) \n",
						new Object[] {templates}
					));

			sqlBuf.append(MessageFormat.format(
						"\n\t\t AND c.status_id  IN ({0}) \n",
						new Object[] {ObjectIdUtils.numericIdsToCommaDelimitedString(action.getStates())}
					));

			HashMap<ObjectId, ObjectId> additionalFilter = action.getAdditionalFilter();
			String attr_codes = "";
			String card_ids = "";
			int i = additionalFilter.size();
			for (ObjectId key : additionalFilter.keySet()) {
				attr_codes += "'" + key.getId().toString() + "'";
				ObjectId value = additionalFilter.get(key);
				card_ids += value.getId().toString();
				i--;
				if (i > 0) {
					attr_codes += ", ";
					card_ids += ", ";
				}
			}

			sqlBuf.append(MessageFormat.format(
						"\n\t\t AND av.attribute_code IN ({0}) \n",
						new Object[] {attr_codes}
					));
			sqlBuf.append(MessageFormat.format(
						"\n\t\t AND av.number_value IN ({0}) \n",
						new Object[] {card_ids}
					));
			sqlBuf.append(	" WHERE \n");
			sqlBuf.append( userPermissionCheck( userId, action.getPermissionTypes()) );	
			sqlBuf.insert(0, userPermissionCheckWithClause(userId));
			sqlBuf.append( limitAndOffset( action.getPage(), action.getPageSize() ) );
		}
		return sqlBuf.toString();
	}

}
