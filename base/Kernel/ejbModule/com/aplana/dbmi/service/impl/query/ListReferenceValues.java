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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

/**
 * {@link ChildrenQueryBase} used to fetch all {@link ReferenceValue dictionary entries} of given
 * parent {@link Reference dictionary}
 */
public class ListReferenceValues extends ChildrenQueryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Fetches all children {@link ReferenceValue} objects of given {@link Reference} object.
	 * NOTE: if given dictionary is hierarchical then resulting list will contain only
	 * top-level records. All nested values will be added to
	 * {@link ReferenceValue#getChildren() children} property of top-level entries.
	 * @return list containing {@link ReferenceValue} objects associated 
	 * with given {@link Reference} object.
	 */
	@Override
	public Object processQuery() throws DataException
	{
		if (getParent() == null || getParent().getId() == null)
			return null;
		final HashMap<ObjectId,ReferenceValue> parentMap = new HashMap<ObjectId,ReferenceValue>();
		@SuppressWarnings("unchecked")
		final List<ReferenceValue> values = getJdbcTemplate().query(
				"SELECT value_id, ref_code, value_rus, value_eng, order_in_level, is_active, parent_value_id " +
				"FROM values_list WHERE ref_code=? ORDER BY order_in_level",
				new Object[] { getParent().getId() },
				new int[] { Types.VARCHAR},
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final ReferenceValue value = new ReferenceValue();
						value.setId(rs.getLong(1));
						value.setReference(getParent());
						value.setValueRu(rs.getString(3));
						value.setValueEn(rs.getString(4));
						value.setOrder(rs.getInt(5));
						value.setActive(rs.getBoolean(6));
						if (rs.getObject(7) != null)
							value.setParent(new ObjectId(ReferenceValue.class, rs.getLong(7)));
						parentMap.put(value.getId(), value);
						return value;
					}
				});
		if (values != null) {
			final Iterator<ReferenceValue> itr = values.iterator();
			while (itr.hasNext()) {
				final ReferenceValue value = itr.next();
				if (value.getParent() == null)
					continue;
				if (!parentMap.containsKey(value.getParent()))
					throw new DataException("children.reference.orphan", new Object[] {
						value.getId().getId(),
						value.getValueRu(),
						value.getValueEn(),
						getParent().getId() });
				itr.remove();
				final ReferenceValue parent = parentMap.get(value.getParent());
				Collection<ReferenceValue> children = parent.getChildren();
				if (children == null) {
					children = new ArrayList<ReferenceValue>();
					parent.setChildren(children);
				}
				children.add(value);
			}
		}
		return values;
	}
}
