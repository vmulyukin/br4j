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

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;
import com.aplana.dbmi.service.impl.QueryBase;

/**
 * Query used to fetch all {@link TreeAttribute} instances defined in database 
 */
public class ListTreeAttributes extends QueryBase
{
	/**
	 * Fetches all {@link TreeAttribute} instances defined in database
	 * @return list containing all {@link TreeAttribute} instances defined in database
	 */	
	public Object processQuery() throws DataException
	{
		return getJdbcTemplate().query(
				"SELECT a.attribute_code, a.attr_name_rus, a.attr_name_eng, o.option_value " +
				"FROM attribute a " +
				"INNER JOIN attribute_option o ON a.attribute_code=o.attribute_code AND o.option_code=? " +
				"WHERE data_type=?",
				new Object[] { AttributeOptions.REFERENCE, AttributeTypes.TREE },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						TreeAttribute attr = new TreeAttribute();
						attr.setId(rs.getString(1));
						attr.setNameRu(rs.getString(2));
						attr.setNameEn(rs.getString(3));
						//attr.setReference(new ObjectId(Reference.class, rs.getString(4)));
						try {
							AttributeOptions.extractOption(attr, AttributeOptions.REFERENCE,
									new ByteArrayInputStream(rs.getString(4).getBytes("UTF-8")), getJdbcTemplate());
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DataException e) {
							throw new ExceptionEnvelope(e);
						}
						return attr;
					}
				});
	}
}
