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

import com.aplana.dbmi.model.AttributeViewParamDetail;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

public class SaveAttributeViewParamDetail extends SaveQueryBase {

	protected ObjectId processNew() throws DataException {
		long recId = generateId("seq_attribute_view_param");		
		AttributeViewParamDetail avp = (AttributeViewParamDetail)getObject();
		String sql = "insert into attribute_view_param (rec_id, template_attr_id," +
			" status_id, role_code, is_mandatory, is_hidden, is_readonly)" +
			" values (?, ?, ?, ?, ?, ?, ?)";
		
		getJdbcTemplate().update(sql,
			new Object[] {
				new Long(recId),
				new Long(avp.getTemplateAttributeId()),
				new Long(avp.getStateId()),
				avp.getRoleCode(),
				new Integer(avp.isMandatory() ? 1 : 0),
				new Integer(avp.isHidden() ? 1 : 0),
				new Integer(avp.isReadOnly() ? 1 : 0)
			},
			new int[] {
				Types.DECIMAL,
				Types.DECIMAL,
				Types.DECIMAL,
				Types.VARCHAR,
				Types.DECIMAL,
				Types.DECIMAL,
				Types.DECIMAL
			}
		);
		return new ObjectId(AttributeViewParamDetail.class, new Long(recId));
	}

	protected void processUpdate() throws DataException {
		throw new RuntimeException("Not implemented yet");
	}

	public void validate() throws DataException {
		super.validate();
	}
}
