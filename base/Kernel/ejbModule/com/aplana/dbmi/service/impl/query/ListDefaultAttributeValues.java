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

import java.io.UnsupportedEncodingException;
//import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DefaultAttributeValue;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.utils.SimpleDBUtils;

/**
 * {@link ChildrenQueryBase} descendant used to fetch default attribute values
 * defined for given parent {@link Template} object.<br>
 * Used to initialize newly created card with set of predefined attribute values.
 */
public class ListDefaultAttributeValues extends ChildrenQueryBase {
	/**
	 * Fetches all default attribute values defined for given {@link Template}
	 * @return list of {@link DefaultAttributeValue} instances representing default attribute values
	 * defined for given template. 
	 */
	public Object processQuery() throws DataException
	{
		final Map attributes = new HashMap();
		getJdbcTemplate().query(
			"select " +
			"ta.TEMPLATE_ATTR_ID" +	// 1
			", ta.ATTRIBUTE_CODE" + // 2
			", a.DATA_TYPE" +		// 3
			", dav.NUMBER_VALUE" +	// 4
			", dav.STRING_VALUE" +	// 5
			", dav.VALUE_ID" +		// 6
			", dav.DATE_VALUE" +	// 7
			", dav.LONG_BINARY_VALUE" +	// 8
			", r.value_rus" +	// 9
			", r.value_eng" +	// 10
			", p.full_name" +	// 11
			", p.email " +		// 12
			", p.card_id " +	// 13 ��� ����������� ��������� ��� id �������� ������������
			" from attribute a, template_attribute ta, default_attribute_value dav" +
			" left outer join values_list r on dav.value_id = r.value_id" +
			" left outer join person p ON dav.number_value = p.person_id" +
			" where ta.TEMPLATE_ATTR_ID = dav.TEMPLATE_ATTR_ID" +
			" and ta.ATTRIBUTE_CODE = a.ATTRIBUTE_CODE and ta.TEMPLATE_ID = ?",
			new Object[] { getParent().getId() },
			new int[] { Types.NUMERIC }, // (2010/03) POSTGRE OLD: INTEGER
			new RowCallbackHandler() {
				private ReferenceValue getReferenceValue(ResultSet rs) throws SQLException {
					ReferenceValue val = new ReferenceValue();
					val.setId(rs.getLong(6));
					val.setValueRu(rs.getString(9));
					val.setValueEn(rs.getString(10));
					return val;
				}
				
				private Person getPerson(ResultSet rs) throws SQLException {
					Person user = new Person();
					user.setId(rs.getLong(4));
					user.setFullName(rs.getString(11));
					user.setEmail(rs.getString(12));
					user.setCardId(new ObjectId(Card.class, rs.getLong(13)));	// ����� ������-������� �� ���������� ������� ������������	
					return user;
				}
				
				public void processRow(ResultSet rs) throws SQLException {
					ObjectId resId = new ObjectId(DefaultAttributeValue.class, new Long(rs.getLong(1)));
					String attributeCode = rs.getString(2);
					DefaultAttributeValue rec = (DefaultAttributeValue)attributes.get(attributeCode);
					if (rec == null) {
						rec = (DefaultAttributeValue)DataObject.createFromId(resId);
						attributes.put(attributeCode, rec);
					}
					String type = rs.getString(3);
					
					rec.setAttributeId(AttributeTypes.createAttributeId(type, attributeCode));
					if (Attribute.TYPE_DATE.equals(type)) {
						rec.setValue(rs.getDate(7));
					} else if (Attribute.TYPE_INTEGER.equals(type)) {
						rec.setValue(new Integer(rs.getInt(4)));
					} else if (Attribute.TYPE_LONG.equals(type)) {
						rec.setValue(new Long(rs.getLong(4)));
					} else if (Attribute.TYPE_STRING.equals(type)) {
						rec.setValue(rs.getString(5));
					} else if (Attribute.TYPE_TEXT.equals(type)) { 
						rec.setValue(rs.getString(5));
					} else if (Attribute.TYPE_LIST.equals(type)) {
						rec.setValue(getReferenceValue(rs));
					} else if (Attribute.TYPE_PERSON.equals(type)) {
						List values = (List)rec.getValue();
						if (values == null) {
							values = new ArrayList();
							rec.setValue(values);
						}
						if (rs.getObject(4) != null) {
							values.add(getPerson(rs));
						}
					} else if (Attribute.TYPE_TREE.equals(type)) {
						List values = (List)rec.getValue();
						if (values == null) {
							values = new ArrayList();
							rec.setValue(values);
						}
						values.add(getReferenceValue(rs));
					} else if (Attribute.TYPE_HTML.equals(type)) {
						//Blob html = rs.getBlob(8);
						//if (html != null) {
						if (rs.getObject(8) != null) {
							try {
								//rec.setValue(new String(html.getBytes(1, (int) html.length()), "UTF-8"));
								// rec.setValue(new String(rs.getBytes(8), "UTF-8"));
								rec.setValue( SimpleDBUtils.getBlobAsStr(rs, 8, "UTF-8"));
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						} else rec.setValue(null);
					} else if (Attribute.TYPE_CARD_LINK.equals(type)) {
						List values = (List)rec.getValue();
						if (values == null) 
							rec.setValue(values = new ArrayList());
						final ObjectId cardId = new ObjectId(Card.class, rs.getLong(4));
						/* (2010/02, RuSA) OLD: values.add(DataObject.createFromId(cardId));
						 * ������ ���� ������� id,�� �� ���� ��������.
						 */
						values.add( cardId );
					} else if (Attribute.TYPE_SECURITY.equals(type)) {
						List values = (List)rec.getValue();
						if (values == null) {
							values = new ArrayList();
							rec.setValue(values);
						}
						AccessListItem item = new AccessListItem();
						if (rs.getObject(5) != null) {
							item.setRoleType(rs.getString(5));
						} else if (rs.getObject(6) != null) {
							ReferenceValue dept = new ReferenceValue();
							dept.setId(rs.getLong(6));
							dept.setReference(Reference.ID_DEPARTMENT);
							dept.setValueRu(rs.getString(9));
							dept.setValueEn(rs.getString(10));
							item.setDepartment(dept);
						} else if (rs.getObject(4) != null) {
							item.setPerson(getPerson(rs));
						}
						values.add(item);
					}
				}
			}
		);		
		
		List result = new ArrayList();
		Iterator i = attributes.values().iterator();
		while (i.hasNext()) {
			DefaultAttributeValue dav = (DefaultAttributeValue)i.next();
			if (dav.getValue() != null) {
				result.add(dav);
			}
		}
		return result;
	}
}
