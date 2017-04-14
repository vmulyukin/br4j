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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ExceptionEnvelope;

/**
 * {@link ChildrenQueryBase} descendant used to fetch all {@link TemplateBlock} objects
 * associated with given parent {@link Template} object 
 */
public class ListTemplateBlocks extends ChildrenQueryBase
{
	/**
	 * Fetches all {@link TemplateBlock} objects associated with given 
	 * parent {@link Template} object.
	 * @return list of {@link TemplateBlock} instances representing template blocks
	 * included in given {@link Template}. 
	 */
	public Object processQuery() throws DataException
	{
		final Map attributesByBlock = getAttributesByBlockMap();
		
		return getJdbcTemplate().query(
				"SELECT tb.template_id, tb.block_code, " +
					"ab.block_name_rus, ab.block_name_eng, ab.is_active, ab.is_system, " +
					"ab.locked_by, ab.lock_time " +
				"FROM template_block tb " +
				"INNER JOIN attr_block ab ON tb.block_code=ab.block_code " +
				"WHERE tb.template_id=?",
				new Object[] { getParent().getId() },
				new int[] { Types.NUMERIC }, // (2010/03) POSTGRE
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						TemplateBlock block = new TemplateBlock();
						block.setTemplate(rs.getLong(1));
						block.setId(rs.getString(2));
//TODO �������� �������� Ace						
/*						int layout = rs.getInt(3);
						block.setColumn(layout / 100 - 1);
						block.setOrder(layout % 100);
*/						
						block.setNameRu(rs.getString(3));
						block.setNameEn(rs.getString(4));
						block.setActive(rs.getBoolean(5));
						block.setSystem(rs.getBoolean(6));
						if (rs.getObject(7) != null) {
							block.setLocker(rs.getLong(7));
							block.setLockTime(rs.getTimestamp(8));
						}
						List attributes = (List)attributesByBlock.get((String)block.getId().getId());
						if (attributes == null) {
							logger.warn("No attributes found for block with id = " + block.getId().getId());
							attributes = new ArrayList(0);
						}
						block.setAttributes(attributes);
						return block;
					}
		});
	}
	
	private Map getAttributesByBlockMap() {
		final Map result = new HashMap();
		final Map allAttributes = new HashMap();
		
		String sql = "SELECT " +
			"a.attribute_code, coalesce(ta.block_code, a.block_code), a.attr_name_rus, a.attr_name_eng, a.data_type, " +	// 1 - 5
			"a.is_active, a.is_system, a.is_mandatory, a.column_width, ta.is_mandatory, " +		// 6 - 10
			"ta.order_in_list, ta.column_width, coalesce(ta.is_hidden, a.is_hidden), coalesce(ta.is_readonly, a.is_readonly) " + // 11 - 14
			"FROM attribute a "+
			"JOIN template_block tb ON (a.block_code = tb.block_code and tb.template_id = ?) " +
			"LEFT OUTER JOIN template_attribute ta ON a.attribute_code=ta.attribute_code AND ta.template_id = tb.template_id " +
			"ORDER BY coalesce(ta.block_code, a.block_code), coalesce(ta.order_in_list, a.order_in_block)"; 
		
		getJdbcTemplate().query(
			sql,
			new Object[] { getParent().getId() },
			new int[] { Types.NUMERIC },
			new RowCallbackHandler() {
				public void processRow(ResultSet rs) throws SQLException {
					Attribute attr = AttributeTypes.createAttribute(rs.getString(5), rs.getString(1));
					ObjectId attrId = attr.getId();
					ObjectId blockId = new ObjectId(TemplateBlock.class, rs.getString(2)); 
					attr.setBlockId(blockId);
					attr.setNameRu(rs.getString(3));
					attr.setNameEn(rs.getString(4));
					attr.setActive(rs.getBoolean(6));
					attr.setSystem(rs.getBoolean(7));
					attr.setMandatory(rs.getBoolean(rs.getObject(10) == null ? 8 : 10));
					attr.setColumnWidth(rs.getInt(rs.getObject(12) == null ? 9 : 12));
					attr.setSearchShow(rs.getObject(11) != null);
					attr.setHidden(rs.getBoolean(13));
					if (!AttributeTypes.isReadOnlyType(attr.getType()))
						attr.setReadOnly(rs.getBoolean(14));
					if (attr.isSearchShow())
						attr.setSearchOrder(rs.getInt(11));
					
					List attributes = (List)result.get((String)blockId.getId());
					if (attributes == null) {
						attributes = new ArrayList();
						result.put((String)blockId.getId(), attributes);
					}
					attributes.add(attr);
					allAttributes.put((String)attrId.getId(), attr);
				}
			}
		);
		
		getJdbcTemplate().query(
				"SELECT o.attribute_code, o.option_code, o.option_value " +
				"FROM attribute_option o " +
				"WHERE exists (" +
				"select 1 from attribute a, template_block tb " +
				"where a.attribute_code = o.attribute_code and a.block_code = tb.block_code and tb.template_id = ?)",
				new Object[] { getParent().getId() },
				new int[] { Types.NUMERIC },
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException
					{
						try {
							String value = rs.getString(3);
							byte[] data = new byte[0];
							if (value != null)
								data = value.getBytes("UTF-8");
							Attribute attr = (Attribute) allAttributes.get(rs.getString(1));
							AttributeOptions.extractOption(attr, rs.getString(2),
									new ByteArrayInputStream(data), getJdbcTemplate());
						} catch (DataException e) {
							throw new ExceptionEnvelope(e);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				});
		
		
		return result;
	}
}
