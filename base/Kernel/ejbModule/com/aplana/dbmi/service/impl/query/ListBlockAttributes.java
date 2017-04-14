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
import java.util.HashMap;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;

/**
 * {@link ChildrenQueryBase} descendant used to fetch children
 * {@link Attribute attributes} included in parent {@link AttributeBlock} object.
 */
public class ListBlockAttributes extends ChildrenQueryBase
{
	final HashMap attrMap = new HashMap();
	/**
	 * Fetches {@link Attribute attributes} included in given {@link AttributeBlock}
	 * @return collection of {@link Attribute} descendant instances representing 
	 * attributes included in given {@link AttributeBlock} object. 
	 */
	public Object processQuery() throws DataException {
		Collection attributes = getJdbcTemplate().query(
				"SELECT attribute_code, attr_name_rus, attr_name_eng, data_type, " +
					"block_code, order_in_block, column_width, " +//display_length, rows_number, " +
					"is_mandatory, is_active, is_system," +/*ref_code,*/" locked_by, lock_time, " +
					"is_hidden, is_readonly " +
				"FROM attribute WHERE block_code=? ORDER BY order_in_block",
				new Object[] { getParent().getId() },
				new int[] { Types.VARCHAR},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final Attribute attr = AttributeTypes.createAttribute(rs.getString(4), rs.getString(1));
						attr.setNameRu(rs.getString(2));
						attr.setNameEn(rs.getString(3));
						attr.setBlockId(getParent());
						attr.setBlockOrder(rs.getInt(6));
						attr.setSearchShow(rs.getObject(7) != null && rs.getInt(7) > 0);
						if (attr.isSearchShow())
							attr.setColumnWidth(rs.getInt(7));
						attr.setMandatory(rs.getBoolean(8));
						attr.setActive(rs.getBoolean(9));
						attr.setSystem(rs.getBoolean(10));
						if (rs.getObject(11) != null) {
							attr.setLocker(rs.getLong(11));
							attr.setLockTime(rs.getTimestamp(12));
						}
						attr.setHidden(rs.getBoolean(13));
						if (!AttributeTypes.isReadOnlyType(attr.getType()))
							attr.setReadOnly(rs.getBoolean(14));
						attrMap.put(rs.getString(1), attr);
						return attr;
					}
				});
		getJdbcTemplate().query(
				"SELECT o.attribute_code, o.option_code, o.option_value " +
				"FROM attribute_option o " +
				"INNER JOIN attribute a ON o.attribute_code=a.attribute_code " +
				"WHERE a.block_code=?",
				new Object[] { getParent().getId() },
				new int[] { Types.VARCHAR},
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException
					{
						try {
							String value = rs.getString(3);
							byte[] data = new byte[0];
							if (value != null)
								data = value.getBytes("UTF-8");
							Attribute attr = (Attribute) attrMap.get(rs.getString(1));
							AttributeOptions.extractOption(attr, rs.getString(2),
									new ByteArrayInputStream(data), getJdbcTemplate());
						} catch (DataException e) {
							throw new ExceptionEnvelope(e);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
		return attributes;
	}
}
