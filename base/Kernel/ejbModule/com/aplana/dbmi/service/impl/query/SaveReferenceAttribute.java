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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * {@link SaveQueryBase} descendant used to save 
 * instances of {@link ListAttribute} and {@link TreeAttribute} classes.<br>
 * Saves 'declaration' of attribute, not its value belonging to one of cards.<br>
 */
public class SaveReferenceAttribute extends SavePlainAttribute
{
	/**
	 * Checks validity of {@link ReferenceAttribute} descendant.
	 */
	public void validate() throws DataException
	{
		super.validate();
		ReferenceAttribute attr = (ReferenceAttribute) getObject();
		if (attr.getId() != null) {
			ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Attribute.class);
			subQuery.setId(attr.getId());
			ReferenceAttribute attrCurrent = (ReferenceAttribute) getDatabase().executeQuery(getUser(), subQuery);
			if (!attrCurrent.getReference().equals(attr.getReference()))
				throw new DataException("store.attribute.ref.changeexisting");
		}
		if (attr.getReference() != null) {
			List types = getJdbcTemplate().queryForList(
					"SELECT data_type FROM attribute a " +
					"INNER JOIN attribute_option o ON a.attribute_code=o.attribute_code AND o.option_code=? " +
					"WHERE o.option_value=?",
					new Object[] { AttributeOptions.REFERENCE, attr.getReference().getId() },
					String.class);
			if (types.size() == 0)
				throw new IllegalArgumentException("Invalid reference id");
			if (!attr.getClass().equals(AttributeTypes.getAttributeClass((String) types.get(0))))
				throw new DataException("store.attrubute.ref.type");
		}
		if (attr.getReferenceValues() == null)
			throw new DataException("store.attribute.ref.novalues");
		Iterator itr = attr.getReferenceValues().iterator();
		while (itr.hasNext()) {
			ReferenceValue value = (ReferenceValue) itr.next();
			if (Attribute.TYPE_LIST.equals(attr.getType()) &&
					value.getChildren() != null && value.getChildren().size() != 0)
				throw new DataException("store.attribute.ref.hierarchy");
			if (!value.isActive() && value.getChildren() != null)
				checkChildrenInactive(value.getChildren());
		}
	}
	
	private void checkChildrenInactive(Collection children) throws DataException
	{
		Iterator itr = children.iterator();
		while (itr.hasNext())
		{
			ReferenceValue value = (ReferenceValue) itr.next();
			if (value.isActive())
				throw new DataException("store.attribute.ref.childactive",
						new Object[] { value.getValueRu(), value.getValueEn() });
			if (value.getChildren() != null)
				checkChildrenInactive(value.getChildren());
		}
	}

	public String getEvent() {
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	protected ObjectId processNew() throws DataException
	{
		ReferenceAttribute attr = (ReferenceAttribute) getObject();
		if (attr.getReference() == null) {
			attr.setId(generateStringId("attribute"));
			attr.setReference(new ObjectId(Reference.class, attr.getId().getId()));
			getJdbcTemplate().update(
					"INSERT INTO reference_list (ref_code, description) VALUES (?, ?)",
					new Object[] { attr.getId().getId(), "Values of " + attr.getId().getId() + " attribute" });
		}
		super.processNew();
		/*getJdbcTemplate().update(
				"INSERT INTO attribute (attribute_code, attr_name_rus, attr_name_eng, data_type, " +
					"block_code, order_in_block, column_width, is_mandatory, is_active, ref_code, " +
					"locked_by, lock_time) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { attr.getId().getId(), attr.getNameRu(), attr.getNameEn(), getDataType(attr),
					attr.getBlockId().getId(), new Integer(attr.getBlockOrder()), new Integer(attr.getColumnWidth()),
					new Boolean(attr.isMandatory()), new Boolean(attr.isActive()), attr.getReference().getId(),
					getUser().getPerson().getId().getId(), new Date()},
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CHAR,
					Types.VARCHAR, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR,
					Types.NUMERIC, Types.TIMESTAMP});*/
		updateReference();
		return attr.getId();
	}

	protected void processUpdate() throws DataException
	{
		super.processUpdate();
		/*int rows = getJdbcTemplate().update(
				"UPDATE attribute SET attr_name_rus=?, attr_name_eng=?, block_code=?, order_in_block=?, " +
					"column_width=?, is_mandatory=?, is_active=? " +
				"WHERE attribute_code=?",
				new Object[] { attr.getNameRu(), attr.getNameEn(),
					attr.getBlockId().getId(), new Integer(attr.getBlockOrder()), new Integer(attr.getColumnWidth()),
					new Boolean(attr.isMandatory()), new Boolean(attr.isActive()), attr.getId().getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC,
					Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR });*/
		updateReference();
	}
	
	protected String getDataType(Attribute attr)
	{
		if (ListAttribute.class.equals(attr.getClass()))
			return AttributeTypes.LIST;
		if (TreeAttribute.class.equals(attr.getClass()))
			return AttributeTypes.TREE;
		//throw new IllegalArgumentException("Can't store object of type " + attr.getClass().getName());
		return super.getDataType(attr);
	}
	
	private void updateReference() throws DataException
	{
		final ReferenceAttribute attr = (ReferenceAttribute) getObject();
		getJdbcTemplate().update(
				"UPDATE values_list SET is_active=0 WHERE ref_code=?",
				new Object[] { attr.getReference().getId() });
		getJdbcTemplate().execute(new ValuesUpdater());
	}
	
	private class ValuesUpdater implements ConnectionCallback
	{
		private ReferenceAttribute attr;
		private Connection conn;
		private PreparedStatement stmtInsert = null;
		private PreparedStatement stmtUpdate = null;
		
		public ValuesUpdater() {
			this.attr = (ReferenceAttribute) getObject();
		}
		
		public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
			this.conn = conn;
			updateValues(attr.getReferenceValues(), null);
			return null;
		}
		
		private void updateValues(Collection values, ObjectId parent) throws SQLException
		{
			if (values == null)
				return;
			Iterator itr = values.iterator();
			int order = 0;
			while (itr.hasNext()) {
				ReferenceValue value = (ReferenceValue) itr.next();
				value.setOrder(order++);
				value.setParent(parent);
				if (value.getId() == null)
					insertValue(value);
				else
					updateValue(value);
				updateValues(value.getChildren(), value.getId());
			}
		}
		
		private void insertValue(ReferenceValue value) throws SQLException
		{
			// (2010/03) POSGRE
			// OLD: value.setId(getJdbcTemplate().queryForLong("SELECT seq_value_id.nextval FROM DUAL"));
			value.setId(getJdbcTemplate().queryForLong("SELECT nextval('seq_value_id')"));
			if (stmtInsert == null)
				stmtInsert = conn.prepareStatement(
						"INSERT INTO values_list (value_id, ref_code, value_rus, " +
							"value_eng, order_in_level, is_active, parent_value_id) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?)");
			stmtInsert.setObject(1, value.getId().getId());
			stmtInsert.setObject(2, attr.getReference().getId());
			stmtInsert.setString(3, value.getValueRu());
			stmtInsert.setString(4, value.getValueEn());
			stmtInsert.setInt(5, value.getOrder());
			stmtInsert.setInt(6, value.isActive() ? 1 : 0);
			stmtInsert.setObject(7, value.getParent() == null ? null : value.getParent().getId(), Types.NUMERIC);
			stmtInsert.execute();
		}
		
		private void updateValue(ReferenceValue value) throws SQLException
		{
			if (stmtUpdate == null)
				stmtUpdate = conn.prepareStatement(
						"UPDATE values_list " +
						"SET value_rus=?, value_eng=?, order_in_level=?, is_active=?, parent_value_id=? " +
						"WHERE value_id=?");
			stmtUpdate.setString(1, value.getValueRu());
			stmtUpdate.setString(2, value.getValueEn());
			stmtUpdate.setInt(3, value.getOrder());
			stmtUpdate.setInt(4, value.isActive() ? 1 : 0);
			stmtUpdate.setObject(5, value.getParent() == null ? null : value.getParent().getId(), Types.NUMERIC);
			stmtUpdate.setObject(6, value.getId().getId());
			stmtUpdate.execute();
		}
	}
}
