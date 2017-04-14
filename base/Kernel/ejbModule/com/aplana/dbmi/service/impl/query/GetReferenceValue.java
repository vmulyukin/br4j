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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link ReferenceValue} instance from database
 */
public class GetReferenceValue extends ObjectQueryBase {

	/**
	 * Fetches single {@link ReferenceValue} instance from database
	 * @return {@link ReferenceValue} instance
	 */
    public Object processQuery() throws DataException {
        ReferenceValue referenceValue = (ReferenceValue) getJdbcTemplate().queryForObject(
                "SELECT VALUE_ID, REF_CODE, VALUE_RUS, VALUE_ENG, ORDER_IN_LEVEL, IS_ACTIVE, PARENT_VALUE_ID FROM values_list WHERE value_id=?",
                new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
                new RowMapper() {
                        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                            ReferenceValue referenceValue = new ReferenceValue();
                            referenceValue.setId(rs.getLong(1));
                            referenceValue.setReference(new ObjectId(Reference.class, rs.getString(2)));
                            referenceValue.setValueRu(rs.getString(3));
                            referenceValue.setValueEn(rs.getString(4));
                            referenceValue.setOrder(rs.getInt(5));
                            referenceValue.setActive(rs.getBoolean(6));
                            if (rs.getObject(7) != null)
                                referenceValue.setParent(new ObjectId(ReferenceValue.class, rs.getLong(7)));
                            return referenceValue;
                        }
                }

        );
        return referenceValue;
    }

}
