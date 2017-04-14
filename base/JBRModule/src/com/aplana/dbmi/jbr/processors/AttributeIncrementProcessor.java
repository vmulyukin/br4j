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
package com.aplana.dbmi.jbr.processors;

import java.sql.Types;
import java.util.List;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

public class AttributeIncrementProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	// (ID) ������� id-��������
	public static final String PARAM_ATTR_ID = "AttrId";
	// (integer) ������������ �������� 
	public static final String PARAM_INCREMENT = "increment";
	public static final int DEFAULT_INCREMENT = 1;

	// (boolean) true = ���� ��������� ������������ �������� (�� "initValue", 
	// false = ������ ���������);
	public static final String PARAM_INIT = "init";
	// (integer) ������������� �������� ��� init == true, 
	// ��� �������� �������� ������ null.
	public static final String PARAM_INIT_VALUE = "initValue";
	public static final int DEFAULT_INIT_VALUE = 1;

	private ObjectId attrId;
	private long increment = 1;
	boolean init = false;

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException 
	{
		this.attrId = super.getAttrIdParameter( PARAM_ATTR_ID, IntegerAttribute.class, false );
		logger.debug("attrId = "+attrId);
		if (attrId == null){
			logger.error("Can't find attribute for parameter: "+PARAM_ATTR_ID);
			return null;
		}
		increment = super.getLongParameter( PARAM_INCREMENT, DEFAULT_INCREMENT);
		init = Boolean.parseBoolean(super.getParameter( PARAM_INIT, "false"));

		final ObjectId cardId = super.getCardId();
		logger.debug("increment = "+increment+"; init = " +Boolean.toString(init)+"; cardId = "+cardId);
		if (cardId == null){
			logger.error("Card is not set");
			return null;
		}
		final List<Long> attrValues = super.getJdbcTemplate().queryForList(
				"select number_value from attribute_value " +
				"where card_id=? and attribute_code=?",
				new Object[]{cardId.getId(), attrId.getId()},
				new int[]{Types.NUMERIC, Types.VARCHAR},
				Long.class);
		if (attrValues.size()>1){
			logger.warn("Attribute " + attrId.getId()+ " in card #" + cardId.getId()+" is multivalued. Exiting.");
			return null;
		}
		Long attrValue = new Long(0);
		String sql;
		if (!attrValues.isEmpty()){
			attrValue = attrValues.get(0);
			sql = "update attribute_value set number_value=? " +
					"where card_id=? and attribute_code=?"; 
		} else {
			logger.warn("Attribute " + attrId.getId()+ " in card #" + cardId.getId()+" is not set. I suppose that it has value 0.");
			sql = "insert into attribute_value (number_value, card_id, attribute_code) " +
					"values (?, ?, ?)";
		}
		logger.debug("sql = "+sql);
		final long initVal = super.getLongParameter( PARAM_INIT_VALUE, DEFAULT_INIT_VALUE);
		if (init) {
			attrValue = initVal;
		} else {
			attrValue = increment + ((attrValue == null) ? initVal : attrValue.longValue());
		}
		logger.debug("attrValue = "+attrValue);

		execAction(new LockObject(cardId));
		try {
			final int count = getJdbcTemplate().update(sql,				new Object[]{attrValue, cardId.getId(), attrId.getId()},
				new int[]{Types.NUMERIC, Types.NUMERIC, Types.VARCHAR});
			logger.debug("count = "+count);
			if (count == 1){
				logger.info("Attribute " + attrId+ " in card #" + cardId.getId()+" was successfully changed to " + attrValue);
				// ����� ���������� � ��, ��������� ������� � ������, ����� ����� ���������� �������� ����� ������� ���������� �������� ��������� ����������� ��������
				Card card = this.getCard();
				Attribute attr = card.getAttributeById(attrId);
				if (attr != null && attr instanceof IntegerAttribute){
					((IntegerAttribute)attr).setValue(attrValue.intValue());
				}
			}
			else
				logger.error("Attribute " + attrId+ " in card #" + cardId.getId()+" was not changed to "+ attrValue);
		} finally {
			execAction(new UnlockObject(cardId));
		}
		return null;
	}
}