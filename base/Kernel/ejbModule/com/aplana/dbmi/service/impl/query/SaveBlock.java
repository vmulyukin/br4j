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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Query used to save {@link AttributeBlock} instance in database.<br>
 * This query stores information about abstract attribute block declaration with no
 * references to any template or card.<br>
 * Updates records in ATTRIBUTE and ATTR_BLOCK tables.
 */
public class SaveBlock extends SaveQueryBase
{
	/**
	 * Identifier of 'New attribute block created' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_BLOCK";
	/**
	 * Identifier of 'Attribute block changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_BLOCK";
	/**
	 * Identifier of 'Attribute block deleted' log event
	 */
	public static final String EVENT_ID_DELETE = "DEL_BLOCK";

	/**
	 * Checks validity of {@link AttributeBlock} object being saved.
	 */
	public void validate() throws DataException
	{
		AttributeBlock block = (AttributeBlock) getObject();
		
		// Check for duplicate attributes
		HashSet attributes = new HashSet();
		Iterator itr = block.getAttributes().iterator();
		while (itr.hasNext()) {
			Attribute attr = (Attribute) itr.next();
			if (attributes.contains(attr.getId().getId()))
				throw new DataException("store.block.attrduplicate");
			attributes.add(attr.getId().getId());
		}
		
		// Check if only free attributes or attributes from this block are saved 
		HashSet available = new HashSet();
		List names = getJdbcTemplate().queryForList(
				"SELECT attribute_code FROM attribute WHERE block_code=?",
				new Object[] { AttributeBlock.ID_REST.getId() },
				String.class);
		itr = names.iterator();
		while (itr.hasNext()) {
			available.add(itr.next());
		}
		if (block.getId() != null) {
			names = getJdbcTemplate().queryForList(
					"SELECT attribute_code FROM attribute WHERE block_code=?",
					new Object[] { block.getId().getId() },
					String.class);
			itr = names.iterator();
			while (itr.hasNext()) {
				available.add(itr.next());
			}
		}
		itr = block.getAttributes().iterator();
		while (itr.hasNext()) {
			Attribute attr = (Attribute) itr.next();
			if (attr.getId() != null && !available.contains(attr.getId().getId()))
				throw new DataException("store.block.attribute",
						new Object[] { attr.getId().getId(), attr.getNameRu(), attr.getNameEn() });
		}
		
		// Check for special (system) blocks
		if (AttributeBlock.ID_REMOVED.equals(block.getId()) ||
			AttributeBlock.ID_REST.equals(block.getId()))
			throw new DataException("store.block.system",
					new Object[] { DataException.RESOURCE_PREFIX + "block." + block.getId().getId() });
		if (AttributeBlock.ID_COMMON.equals(block.getId())) {
			if (!block.isActive())
				throw new DataException("store.block.systemdelete",
						new Object[] { DataException.RESOURCE_PREFIX + "block." + block.getId().getId() });
			names = getJdbcTemplate().queryForList(
					"SELECT attribute_code FROM attribute WHERE is_system=1 AND block_code=?",
					new Object[] { AttributeBlock.ID_COMMON.getId() },
					String.class);
			itr = names.iterator();
			while (itr.hasNext()) {
				String name = (String) itr.next();
				if (!block.hasAttribute(name))
					throw new DataException("store.block.systemattr", new Object[] { name });
			}
		}
		super.validate();
	}

	/**
	 * @return {@link #EVENT_ID_CREATE} if new block is saved for the first time,
	 * {@link #EVENT_ID_CHANGE} if exisiting block is updated and
	 * {@link #EVENT_ID_DELETE} if existing block is marked as inactive.
	 */
	public String getEvent() {
		if (isNew())
			return EVENT_ID_CREATE;
		if (!((AttributeBlock) getObject()).isActive())
			return EVENT_ID_DELETE;
		return EVENT_ID_CHANGE;
	}

	protected ObjectId processNew() throws DataException
	{
		AttributeBlock block = (AttributeBlock) getObject();
		block.setId(generateStringId("attr_block"));
		getJdbcTemplate().update(
				"INSERT INTO attr_block (block_code, block_name_rus, block_name_eng, locked_by, lock_time) " +
				"VALUES (?, ?, ?, ?, ?)",
				new Object[] { block.getId().getId(), block.getNameRu(), block.getNameEn(),
					getUser().getPerson().getId().getId(), new Date() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP });
		updateAttributes();
		return block.getId();
	}

	protected void processUpdate() throws DataException
	{
		checkLock();
		AttributeBlock block = (AttributeBlock) getObject();
		getJdbcTemplate().update(
				"UPDATE attr_block SET block_name_rus=?, block_name_eng=?, is_active=? WHERE block_code=?",
				new Object[] { block.getNameRu(), block.getNameEn(), new Integer(block.isActive() ? 1 : 0), block.getId().getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.VARCHAR}
				);
		updateAttributes();
	}
	
	private void updateAttributes()
	{
		getJdbcTemplate().update(
				"UPDATE attribute SET block_code=? WHERE block_code=?",
				new Object[] { AttributeBlock.ID_REST.getId(), getObject().getId().getId() });
		getJdbcTemplate().execute(new ConnectionCallback() {
			public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
				AttributeBlock block = (AttributeBlock) getObject();
				PreparedStatement stmt = conn.prepareStatement(
						"UPDATE attribute SET block_code=?, order_in_block=? WHERE attribute_code=?");
				Iterator itr = block.getAttributes().iterator();
				int order = 0;
				while (itr.hasNext()) {
					Attribute attr = (Attribute) itr.next();
					stmt.setObject(1, block.getId().getId());
					stmt.setInt(2, order++);
					stmt.setObject(3, attr.getId().getId());
					stmt.execute();
				}
				return null;
			}
		});
	}
}
