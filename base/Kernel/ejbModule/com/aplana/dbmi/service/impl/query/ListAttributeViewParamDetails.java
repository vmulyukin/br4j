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
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * Query used to fetch all children {@link AttributeViewParamDetail} objects
 * of given {@link Template}  
 */
public class ListAttributeViewParamDetails extends ChildrenQueryBase {

	/**
	 * Fetches all children {@link AttributeViewParamDetail} objects
	 * of given {@link Template}
	 * @return list of {@link AttributeViewParamDetail} objects 
	 */
	public Object processQuery() throws DataException {
		//Long templateId = (Long)getParent().getId(); 
		return getJdbcTemplate().query(
			"select avp.rec_id, avp.template_attr_id, avp.status_id," +				// 1 - 3
			" avp.role_code, avp.is_mandatory, avp.is_hidden, avp.is_readonly, " +	// 4 - 7
			" ta.attribute_code, avp.person_attribute_code " +						// 8 - 9
			"from attribute_view_param avp, template_attribute ta where" +
			" ta.template_id = ?" +
			" and ta.template_attr_id = avp.template_attr_id",
			
			new Object[] {getParent().getId()},
			new int[] { Types.NUMERIC }, // (2010/03) POSTGRE OLD: DECIMAL

			new RowMapper() {
				public Object mapRow(ResultSet rs, int index) throws SQLException {
					ObjectId id = new ObjectId(AttributeViewParamDetail.class, new Long(rs.getLong(1)));
					AttributeViewParamDetail rec = (AttributeViewParamDetail)DataObject.createFromId(id);
					rec.setTemplateAttributeId(rs.getLong(2));
					rec.setStateId(rs.getLong(3));
					rec.setRoleCode(rs.getString(4));
					rec.setMandatory(rs.getInt(5) != 0);
					rec.setHidden(rs.getInt(6) != 0);
					rec.setReadOnly(rs.getInt(7) != 0);
					rec.setAttributeCode(rs.getString(8));
					if (rs.getObject(9) != null) {
						rec.setPersonAttributeId(new ObjectId(PersonAttribute.class, rs.getString(9)));
					}
					return rec;
				}
			}
		);
	}
}
