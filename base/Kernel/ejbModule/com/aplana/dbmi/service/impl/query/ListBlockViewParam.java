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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.AttributeViewParamDetail;
import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

public class ListBlockViewParam extends ChildrenQueryBase {

	/**
	 * Fetches all children {@link BlockViewParam} objects
	 * of given {@link Template}
	 * @return list of {@link BlockViewParam} objects 
	 */
	public Object processQuery() throws DataException {
		return getJdbcTemplate().query(
				"select rec_id, template_id, block_code, status_id, state_block "+
				"from block_view_param "+
				"where template_id = ?",
				
				new Object[] {getParent().getId()},
				new int[] { Types.NUMERIC },

				new RowMapper() {
					public Object mapRow(ResultSet rs, int index) throws SQLException {
						ObjectId id = new ObjectId(AttributeViewParamDetail.class, new Long(rs.getLong(1)));
						BlockViewParam rec = (BlockViewParam)DataObject.createFromId(id);
						rec.setTemplate(rs.getLong(2));
						rec.setBlock(rs.getString(3));
						rec.setCardStatus(rs.getLong(4));
						rec.setStateBlock((int)rs.getLong(5));
						return rec;
					}
				}
			);
	}

}
