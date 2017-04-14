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
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;
import com.aplana.dbmi.model.TabViewParam;
import com.aplana.dbmi.model.filter.CardViewModeFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all children {@link TabViewParam}
 * objects for given {@link Template} object.
 */
public class ListTabViewParams extends ChildrenQueryBase {

	private static final long serialVersionUID = 1L;

	/**
	 * Fetches all children {@link TabViewParam} objects of given {@link Template}.
	 * @return collection of {@link TabViewParam} objects
	 */
	public Object processQuery() throws DataException {
		CardViewModeFilter filter = (CardViewModeFilter) getFilter();
		
		String defaultSql = "SELECT tt.tab_id, tt.template_id, t.name_rus, t.name_eng, tt.order_lr, tt.is_hidden " +
			"FROM tab_template tt, tab t " +
			"WHERE t.tab_id=tt.tab_id AND tt.template_id=?";
		
		Object[] defaultParams = new Object[] { getParent().getId() };
		int[] defaultTypes = new int[] { Types.NUMERIC };

		final Map<Long, TabViewParam> tabMap = new HashMap<Long, TabViewParam>();
		RowCallbackHandler rh = new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				boolean isHidden = rs.getBoolean(6);
				TabViewParam tvp = new TabViewParam();
				tvp.setId(rs.getLong(1));
				if (!isHidden) {
					tvp.setTemplate(rs.getLong(2));
					tvp.setNameRu(rs.getString(3));
					tvp.setNameEn(rs.getString(4));
					tvp.setOrder(rs.getInt(5));
					tabMap.put((Long)tvp.getId().getId(), tvp);
				} else {
					tabMap.remove((Long)tvp.getId().getId());
				}
			}
		};
		
		getJdbcTemplate().query(defaultSql, defaultParams, defaultTypes, rh);

		if (filter != null && filter.getViewMode() != null) {
			String viewModeSql = "SELECT tt.tab_id, tt.template_id, t.name_rus, t.name_eng, tvp.order_lr, tvp.is_hidden " +
			"FROM tab_view_param tvp " +
			"INNER JOIN tab_template tt on tvp.tab_template_id = tt.tab_template_id and tt.template_id = ? " +
			"INNER JOIN tab t on t.tab_id = tt.tab_id " +
			"WHERE tvp.view_code=?";
			
			Object[] params = new Object[] { getParent().getId(), filter.getViewMode().getId() };
			int[] types = new int[] { Types.NUMERIC, Types.VARCHAR };
			
			getJdbcTemplate().query(viewModeSql, params, types, rh);
		}

		return new ArrayList<TabViewParam>(tabMap.values());
	}
	
	@Override
	protected boolean supportsFilter(Class<?> type) {
		return CardViewModeFilter.class.equals(type);
	}
}
