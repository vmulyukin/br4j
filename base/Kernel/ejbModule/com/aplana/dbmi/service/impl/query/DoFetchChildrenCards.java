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

import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.action.FetchChildrenCards;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * {@link ActionQueryBase Query} used to perform {@link FetchChildrenCards} action
 * @author DSultanbekov
 */
public class DoFetchChildrenCards extends ActionQueryBase {

	public Object processQuery() throws DataException {
		FetchChildrenCards action = (FetchChildrenCards)getAction();
		
		String sql;
		if (action.isReverseLink()) {
			sql = "select distinct av.card_id from attribute_value av where" +
				" av.attribute_code = ?" +
				" and av.number_value = ?";
		} else {
			sql = "select distinct av.number_value from attribute_value av where" +
				" av.attribute_code = ?" +
				" and av.card_id = ?";
		}
		if(action.getLinkAttrTypes()!=null && !action.getLinkAttrTypes().isEmpty()){
			sql = sql + " and av.value_id in (" + ObjectIdUtils.numericIdsToCommaDelimitedString(action.getLinkAttrTypes()) + ")";
		}
		List childrenIds = getJdbcTemplate().queryForList(
			sql, 
			new Object[] {action.getLinkAttributeId().getId(), action.getCardId().getId()},
			new int[] {Types.VARCHAR, Types.NUMERIC},
			Long.class
		);
		
		StringBuffer words = new StringBuffer();
		Iterator i = childrenIds.iterator();
		while (i.hasNext()) {
			words.append(i.next().toString());
			if (i.hasNext()) {
				words.append(',');
			}
		}
		Search search = new Search();
		search.setByCode(true);
		search.setWords(words.toString());
		search.setColumns(action.getColumns());
		
		ActionQueryBase q = getQueryFactory().getActionQuery(search);
		q.setAction(search);
		
		return (SearchResult)getDatabase().executeQuery(getUser(), q);
	}
}
