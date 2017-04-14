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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.util.Date;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * {@link SaveQueryBase} descendant used to save 
 * instances of {@link Attribute} class descendants.<br>
 * Saves 'declaration' of attribute, not its value belonging to one of cards.<br>
 * <ul>Could be used for following {@link Attribute} descendants:
 * <li>StringAttribute</li>
 * <li>TextAttribute</li>
 * <li>HtmlAttribute</li>
 * <li>IntegerAttribute</li>
 * <li>DateAttribute</li>
 * <li>CardLinkAttribute</li>
 * </ul>
 * Attempt to use it for any other type will case IllegalArgumentException
 */
public class SavePlainAttribute extends SaveQueryBase
{
	/**
	 * Identifier of 'New attribute created' log event
	 */
	public static final String EVENT_ID_CREATE = "NEW_ATTR";
	/**
	 * Identifier of 'Attribute changed' log event
	 */
	public static final String EVENT_ID_CHANGE = "CHG_ATTR";
	
	/**
	 * Checks validity of {@link Attribute} object being saved
	 */
	public void validate() throws DataException
	{
		Attribute attr = (Attribute) getObject();
		if ((attr.getNameRu() == null || attr.getNameRu().length() == 0) &&
			(attr.getNameEn() == null || attr.getNameEn().length() == 0))
			throw new DataException("store.attribute.noname");
		if (attr.getId() != null) {
			ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Attribute.class);
			subQuery.setId(attr.getId());
			Attribute attrCurrent = (Attribute) getDatabase().executeQuery(getUser(), subQuery);
			if (!attrCurrent.getType().equals(attr.getType()))
				throw new DataException("store.attribute.type");
			if (attrCurrent.isSystem()) {
				if (!attr.isActive())
					throw new DataException("store.attribute.systemdelete");
				String block = (String) getJdbcTemplate().queryForObject(
						"SELECT block_code FROM attribute WHERE attribute_code=?",
						new Object[] { attr.getId().getId() },
						String.class);
				if (!new ObjectId(AttributeBlock.class, block).equals(attr.getBlockId()))
				//if (!AttributeBlock.ID_COMMON.equals(attr.getBlockId()))
					throw new DataException("store.attrubute.systemblock");
			}
		}
		super.validate();
	}

	/**
	 * @return {@link #EVENT_ID_CREATE} if new attribute is saving for the first time,
	 * {@link #EVENT_ID_CHANGE} if already existed attribute was updated.
	 */
	public String getEvent() {
		return isNew() ? EVENT_ID_CREATE : EVENT_ID_CHANGE;
	}

	protected ObjectId processNew() throws DataException
	{
		Attribute attr = (Attribute) getObject();
		//if (attr.getId() == null)
			attr.setId(generateStringId("attribute"));
		if (attr.getBlockId() == null)
			attr.setBlockId(AttributeBlock.ID_REST);
		getJdbcTemplate().update(
				"INSERT INTO attribute (attribute_code, attr_name_rus, attr_name_eng, data_type, " +
					"block_code, order_in_block, column_width, " +
					"is_mandatory, locked_by, lock_time) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				new Object[] { attr.getId().getId(), attr.getNameRu(), attr.getNameEn(), getDataType(attr),
					attr.getBlockId() == null ? AttributeBlock.ID_REST.getId() : attr.getBlockId().getId(),
					new Integer(attr.getBlockOrder()), new Integer(attr.getColumnWidth()),
					new Boolean(attr.isMandatory()),
					getUser().getPerson().getId().getId(), new Date() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.CHAR,
					Types.VARCHAR, Types.NUMERIC, Types.NUMERIC,
					Types.NUMERIC, Types.NUMERIC, Types.TIMESTAMP });
		updateOptions();
		return attr.getId();
	}

	protected void processUpdate() throws DataException
	{
		checkLock();
		Attribute attr = (Attribute) getObject();
		int rows = getJdbcTemplate().update(
				"UPDATE attribute SET attr_name_rus=?, attr_name_eng=?, block_code=?, order_in_block=?, " +
					"column_width=?, is_mandatory=?, is_active=? " +
				"WHERE attribute_code=?",
				new Object[] { attr.getNameRu(), attr.getNameEn(),
					attr.getBlockId() == null ? AttributeBlock.ID_REST.getId() : attr.getBlockId().getId(),
					new Integer(attr.getBlockOrder()), new Integer(attr.getColumnWidth()),
					new Boolean(attr.isMandatory()), new Boolean(attr.isActive()), attr.getId().getId() },
				new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC,
					Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR });
		if (rows != 1)
			throw new DataException("store.attribute.notexists", new Object[] { attr.getId().getId() });
		updateOptions();
	}
	
	private void updateOptions() throws DataException
	{
		//***** clean up referred tables (e.g. xml_data for filter attributes)
		getJdbcTemplate().update(
				"DELETE FROM attribute_option WHERE attribute_code=?",
				new Object[] { getObject().getId().getId() });
		Attribute attr = (Attribute) getObject();
		String[] options = AttributeOptions.getAttributeOptions(attr.getClass());
		for (int i = 0; i < options.length; i++) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			if (!AttributeOptions.packOption(attr, options[i], buffer, getJdbcTemplate()))
				continue;
			try {
				getJdbcTemplate().update(
						"INSERT INTO attribute_option (attribute_code, option_code, option_value) " +
							"VALUES (?, ?, ?)",
						new Object[] { attr.getId().getId(), options[i], buffer.toString("UTF-8") });
								//new SqlLobValue(buffer.toByteArray(), new OracleLobHandler()) },
						//new int[] { Types.VARCHAR, Types.VARCHAR, Types.BLOB });
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected String getDataType(Attribute attr)
	{
		if (StringAttribute.class.equals(attr.getClass()))
			return AttributeTypes.STRING;
		if (TextAttribute.class.equals(attr.getClass()))
			return AttributeTypes.TEXT;
		if (HtmlAttribute.class.equals(attr.getClass()))
			return AttributeTypes.HTML;
		if (IntegerAttribute.class.equals(attr.getClass()))
			return AttributeTypes.INTEGER;
		if (DateAttribute.class.equals(attr.getClass()))
			return AttributeTypes.DATE;
		if (CardLinkAttribute.class.equals(attr.getClass()))
			return AttributeTypes.CARD_LINK;
		if (PersonAttribute.class.equals(attr.getClass()))
			return AttributeTypes.PERSON;
		throw new IllegalArgumentException("Can't store object of type " + attr.getClass().getName());
	}
}
