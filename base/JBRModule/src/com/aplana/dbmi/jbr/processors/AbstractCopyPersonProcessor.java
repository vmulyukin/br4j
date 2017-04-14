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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.impl.Parametrized;

@SuppressWarnings("unchecked")
public abstract class AbstractCopyPersonProcessor extends ProcessCard 
	// implements Parametrized, DatabaseClient 
{
	private ObjectId linkAttrId;
	private boolean linkReversed;
	
	/**
	 * Implementation of {@link Parametrized#setParameter(String, String)} method
	 */
	@Override
	public void setParameter(String name, String value) {
		if ("linkAttrId".equalsIgnoreCase(name)) {
			this.linkAttrId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class, false);
		} else if ("linkReversed".equalsIgnoreCase(name)) {
			this.linkReversed = Boolean.parseBoolean(value);
		} else {
			super.setParameter(name, value);
		}
	}
	
	/**
	 * @return identifier of {@link CardLinkAttribute} which defined link between currently changing 
	 * {@link Card} and linked {@link Cards}
	 */
	protected ObjectId getLinkAttrId() {
		return linkAttrId;
	}

	protected void setLinkAttrId(ObjectId value) {
		this.linkAttrId = value;
	}
	
	/**
	 * @return 
	 * <code>false</code> if {@link #getLinkAttrId() linkAttrId} specifies attribute in currently processed {@link Card},<br> 
	 * <code>true</code> if {@link #getLinkAttrId() linkAttrId} specifies attribute in linked card
	 */
	protected boolean isLinkReversed() {
		return linkReversed;
	}

	protected List<ObjectId> loadCardPersonAttributesValues(ObjectId cardId, String cardorpersonAttributeIds){
		// ������� id-������ ��� id-������������ �������� ��� mixed (U,C)-��������� ��� ���������� � ���������� � ������������� �������� ��� ������� �������� 
		String sql = "SELECT DISTINCT \n"
			+"src.dest_attr_type, \n" 
			+"src.dest_attr_code, \n"
			// � ����������� �� ���� �������� �������� �������� ��� ������: 
			// person_id ��� card_id �������...
			+"	(CASE \n"
			+"		WHEN 'U'=(select a.data_type from attribute a where a.attribute_code=src.dest_attr_code) \n" 
			+"			THEN src.person_id \n"
			+"			ELSE src.person_card_id \n"
			+"	END) as dst_number_value \n"
			+"FROM ( \n"
			+"	SELECT \n"	
			+"		a.data_type as dest_attr_type,\n"
			+"		av.attribute_code as dest_attr_code, -- (:dst_attr_code) \n" 
			+"		-- person_id \n"
			+"		( CASE \n" 
			+"			WHEN a.data_type = 'U' THEN av.number_value \n"
			+"			ELSE (select p.person_id from person p where p.card_id = av.number_value) \n"
			+"		  END ) as person_id, \n"
			+ "\n"
			+"		-- card_id \n"
			+"		( CASE \n"
			+"			WHEN a.data_type = 'U' THEN (select p.card_id from person p where p.person_id = av.number_value) \n"
			+"			ELSE av.number_value \n"
			+"		  END ) as person_card_id \n"
			+"	FROM attribute_value av \n"
			+"		JOIN attribute a on a.attribute_code=av.attribute_code \n"
			+"				and a.data_type in ('U', 'C') \n "
			// 		id �������� �������� (:card_ids) 
			+"				and  av.card_id in (?) \n"
			// 		�������� �������� ((:attr_codes))...
			+"				and av.attribute_code in ("+ cardorpersonAttributeIds+") \n" 
			+"		JOIN card c ON c.card_id IN (?) \n"
			+") src \n"
		;
		return getJdbcTemplate().query(sql, 
				new Object[]{ cardId.getId(), cardId.getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC},
				new RowMapper(){
					public Object mapRow(ResultSet rs, int row) throws SQLException {
						ObjectId id = new ObjectId(((rs.getString(1)=="U")?PersonAttribute.class:CardLinkAttribute.class), rs.getLong(3));
						return id;
	} 
				});
	}
	
	protected List<ObjectId> loadCardPersonAttributeValues(ObjectId cardId, 
			ObjectId personAttributeId ) 
	{
		return getJdbcTemplate().query("SELECT number_value FROM attribute_value " +
								"WHERE card_id=? AND attribute_code=?", 
				new Object[]{
						cardId.getId(),
						personAttributeId.getId()
				},
				new int[] {
						Types.NUMERIC,
						Types.VARCHAR
				},
				new RowMapper(){
					public Object mapRow(ResultSet rs, int row) throws SQLException {
						ObjectId id = new ObjectId(PersonAttribute.class, rs.getLong(1));
						return id;
					}
				});
	}

	/**
	 * ���������� ��������� ������ � ��������.
	 * @param destCardId id ������� ��������
	 * @param destPersonOrCardAttrId ������� ������� ��� ������
	 * @param destPersonOrCardIds ������ person id ��� DataObject ��� ����������
	 * @param preclear true, ����� ��������� ������� ������� ��������� (�.�. 
	 * ����� ������� �������, ������� ��� � personIds).
	 * @return ���-�� ����������� ���������� ������� (� ��������� ������������).
	 */
	protected int insertCardPersonAttributeValues(ObjectId destCardId, 
			ObjectId destPersonOrCardAttrId, Collection destPersonOrCardIds, 
			boolean preclear) 
	{
		return insertCardsPersonAttributeValues( Collections.singletonList(destCardId)
					, destPersonOrCardAttrId, destPersonOrCardIds, preclear);
		}

	/**
	 * ���������� ��������� ������ � ��������.
	 * @param destCardIds ������ ������� ��������
	 * @param destPersonOrCardAttrId ������� ������� ��� ������
	 * @param destPersonOrCardIds ������ person id ��� DataObject ��� ����������
	 * (���� destPersonOrCardId ��� 'U', �� ����� person id, 
	 * ���� 'C' �� id ������������ ��������)
	 * @param preclear true, ����� ��������� ������� ������� ��������� (�.�. 
	 * ����� ������� �������, ������� ��� � personIds).
	 * @return ���-�� ����������� ���������� ������� (� ��������� ������������).
					 */ 
	protected int insertCardsPersonAttributeValues( Collection<?> destCardIds,
				ObjectId destPersonOrCardAttrId,
				Collection destPersonOrCardIds,
				boolean preclear)
	{
		return  CardUtils.insertCardsPersonAttributeValues( destCardIds, destPersonOrCardAttrId,
						destPersonOrCardIds, preclear, this.getJdbcTemplate(), this.logger);
	}

}