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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Attribute} descendant.<br>
 * This query fetches abstract {@link Attribute} definition only, without any value.
 */
public class GetAttribute extends ObjectQueryBase
{
	/**
	 * Fetches {@link Attribute} descendant definition
	 * @return fully initialized {@link Attribute} descendant instance
	 */
	public Object processQuery() throws DataException
	{
		final Attribute attr = (Attribute) getJdbcTemplate().queryForObject(
				"SELECT attribute_code, attr_name_rus, attr_name_eng, data_type, " +
					"block_code, order_in_block, column_width, " +//display_length, rows_number, " +
					"is_mandatory, is_active, is_system," +/*ref_code,*/" locked_by, lock_time, " +
					"is_hidden, is_readonly " +
				"FROM attribute WHERE attribute_code=?",
				new Object[] { getId().getId() },
				new int[] { Types.VARCHAR },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Attribute attrItem = AttributeTypes.createAttribute(rs.getString(4), rs.getString(1));
						attrItem.setNameRu(rs.getString(2));
						attrItem.setNameEn(rs.getString(3));
						attrItem.setBlockId(new ObjectId(AttributeBlock.class, rs.getString(5)));
						attrItem.setBlockOrder(rs.getInt(6));
						attrItem.setSearchShow(rs.getObject(7) != null && rs.getInt(7) > 0);
						if (attrItem.isSearchShow())
							attrItem.setColumnWidth(rs.getInt(7));
						attrItem.setMandatory(rs.getBoolean(8));
						attrItem.setActive(rs.getBoolean(9));
						attrItem.setSystem(rs.getBoolean(10));
						if (rs.getObject(11) != null) {
							attrItem.setLocker(rs.getLong(11));
							attrItem.setLockTime(rs.getTimestamp(12));
						}
						attrItem.setHidden(rs.getBoolean(13));
						if (!AttributeTypes.isReadOnlyType(attrItem.getType()))
							attrItem.setReadOnly(rs.getBoolean(14));
						return attrItem;
					}
				});
		getJdbcTemplate().query(
				"SELECT option_code, option_value FROM attribute_option WHERE attribute_code=?",
				new Object[] { getId().getId() },
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException
					{
						try {
							String value = rs.getString(2);
							byte[] data = new byte[0];
							if (value != null)
								data = value.getBytes("UTF-8");
							AttributeOptions.extractOption(attr, rs.getString(1),
									new ByteArrayInputStream(data), getJdbcTemplate());
						} catch (DataException e) {
							throw new ExceptionEnvelope(e);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		if (attr instanceof ReferenceAttribute) {
			ReferenceAttribute refAttr = (ReferenceAttribute) attr;
			ChildrenQueryBase subQuery = getQueryFactory().getChildrenQuery(Reference.class, ReferenceValue.class);
			if (refAttr.getReference() == null || refAttr.getReference().getId() == null) {
				throw new DataException("constraint.ATTRIBUTE_NO_DICT_1", 
							new Object[]{refAttr.getId()} );
			}
			subQuery.setParent(refAttr.getReference());
			refAttr.setReferenceValues((Collection) getDatabase().executeQuery(getUser(), subQuery));
		}
		return attr;
	}
}
