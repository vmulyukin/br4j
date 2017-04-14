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

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.locks.LockManagement;
import com.aplana.dbmi.service.impl.locks.LockManagement.OperationResult;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.LockInfo;
import com.aplana.dbmi.utils.SimpleDBUtils;
import org.springframework.jdbc.core.RowMapper;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link Card} object from database
 */
public class GetCard extends ObjectQueryBase {
	private static final long serialVersionUID = 6L;
	public final static String LOCK_MANAGEMENT_BEAN = "lockManagement";
	/**
	 * Identifier of 'Get card' action to be used in system log
	 */
	public static final String EVENT_ID = "GET_CARD";

	/**
	 * @return {@link #EVENT_ID}
	 */
	public String getEvent() {
		return EVENT_ID;
	}

	/**
	 * Fetches single {@link Card} object from database
	 * @return fully-initialized {@link Card} instance
	 */
	public Object processQuery() throws DataException
	{
		final HashMap<Object, String> material = new HashMap<Object, String>();
		final Card card = (Card) getJdbcTemplate().queryForObject(
				"SELECT c.card_id, c.template_id, c.is_active, " +
					"c.file_name, c.external_path, c.status_id, " +
					"t.template_name_rus, t.template_name_eng, s.name_rus, s.name_eng " +
				"FROM card c " +
				"INNER JOIN template t ON c.template_id=t.template_id " +
				"INNER JOIN card_status s ON c.status_id=s.status_id " +
				"WHERE c.card_id=?",
				new Object[] { getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Card card = new Card();
						card.setId(rs.getLong(1));
						card.setTemplate(rs.getLong(2));
						card.setActive(rs.getBoolean(3));
						if (rs.getString(4) != null)
							material.put(AttributeUtils.MATERIAL_FILE, rs.getString(4));
						if (rs.getString(5) != null)
							material.put(AttributeUtils.MATERIAL_URL, rs.getString(5));
						card.setState(CardState.getId(rs.getInt(6)));
						card.setTemplateNameRu(rs.getString(7));
						card.setTemplateNameEn(rs.getString(8));
						card.setStateName(new LocalizedString(rs.getString(9), rs.getString(10)));
						return card;
					}
				});
		if (card == null)
			return null;

		final ChildrenQueryBase blockQuery = getQueryFactory().getChildrenQuery(Template.class, TemplateBlock.class);
		blockQuery.setParent(card.getTemplate());
		final Collection<TemplateBlock> blocks = getDatabase().executeQuery(getUser(), blockQuery);
		final Map<Object, Attribute> attrIds = new HashMap<Object, Attribute>();
		final Map<Object, Attribute> oldAttributes = new HashMap<Object, Attribute>();
		boolean hasMaterialAttr = false;
		for (TemplateBlock block : blocks) {
			Collection<Attribute> attributes = block.getAttributes();
			Iterator<Attribute> iAttr = attributes.iterator();
			while (iAttr.hasNext()) {
				Attribute attr = iAttr.next();
				if (attr.isActive())
					attrIds.put(attr.getId().getId(), attr);
				else
					iAttr.remove();
				if (attr instanceof TreeAttribute)
					((TreeAttribute) attr).setValues(new ArrayList<ReferenceValue>());
				else if (attr instanceof PersonAttribute)
					((PersonAttribute) attr).setValues(new ArrayList<Person>());
				else if (Attribute.TYPE_MATERIAL.equals(attr.getType())) {
					AttributeUtils.fillMaterial((MaterialAttribute) attr, material);
					hasMaterialAttr = true;
				}
			}
		}

		if (material.size() > 0 && !hasMaterialAttr) {
			ObjectQueryBase attrQuery = getQueryFactory().getFetchQuery(Attribute.class);
			attrQuery.setId(Attribute.ID_MATERIAL);
			MaterialAttribute attr = getDatabase().executeQuery(getUser(), attrQuery);
			AttributeUtils.fillMaterial(attr, material);
			oldAttributes.put(attr.getId().getId(), attr);
		}
		
		getJdbcTemplate().query(
				"SELECT a.attribute_code, v.number_value, v.string_value, v.date_value, \n" +		//  1-4
				"	v.value_id, v.another_value, v.long_binary_value, \n" +						//  5-7
				"	a.data_type, a.attr_name_rus, a.attr_name_eng, a.is_active, \n" +			//  8-11
				"	r.value_rus, r.value_eng, p.full_name, p.email, \n" +						// 12-15
				"	a.is_mandatory, a.is_hidden, a.is_readonly, \n" +							// 16-18
				"	p.card_id \n" +																// 19
				"	, v.attribute_code \n" +													// 20, ���� null, �� ����� ��� �������� ������� �������� a.attribute_code
				// ��� back-link ����� ��������� id ��������, ����������� �� ������ ...
				"	, ( CASE  \n"+																// 21
				"				WHEN a.data_type='B' and upLink.option_value is null \n"+
				"						THEN avLinkFrom.card_id \n"+
				"				WHEN a.data_type='B' and upLink.option_value is not null \n"+
				"						THEN functionbacklink(c.card_id, upLink.option_value, link.option_value) \n"+
				"				ELSE NULL \n"+
				"	   END ) -- CASE \n"+
				"	, p.person_login \n"+														// 22
				// ����� ��� �������� �� ������� ��������...
				"FROM card c \n" +
				"	INNER JOIN template_block tb on tb.template_id = c.template_id \n"+ 
				"	INNER JOIN attribute a on a.block_code=tb.block_code \n"+
				"	LEFT OUTER JOIN attribute_value v \n" + 
				"			on v.attribute_code=a.attribute_code \n" +
				"			and v.card_id=c.card_id \n" +
				"	LEFT OUTER JOIN values_list r ON v.value_id=r.value_id \n" +
				"	LEFT OUTER JOIN person p ON v.number_value=p.person_id \n" +
				// ���� ������� �������� back-link'�� ������ ������ ��� �������� � ��������, �� ������� �� ���������
				"	LEFT OUTER JOIN attribute_option link on link.attribute_code = a.attribute_code \n" +
				"			and link.option_code='LINK' \n" +
				"	LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = a.attribute_code \n" +
				"			and upLink.option_code='UPLINK' \n" +
				// ������ �� ������ �������� ���� ������� �������� back-link'��
				"	LEFT OUTER JOIN attribute_value avLinkFrom \n" + 
				"		on avLinkFrom.number_value=c.card_id \n" +
				"		and avLinkFrom.attribute_code=( \n" +
				"			select o.option_value \n" +
				"			from attribute_option o \n" +
				"			where o.attribute_code=a.attribute_code \n" +
				"				and o.option_code='LINK' \n" +
				"		) \n" +
				"WHERE c.card_id=? \n" +
				"ORDER BY v.attr_value_id \n",
				new Object[] { card.getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					@SuppressWarnings("unchecked")
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						final String attrCode = rs.getString(1);
						final boolean hasValue = rs.getObject(20) != null; // ���� v.attribute_code == null, �� �������� ���
						Attribute attr = attrIds.get(attrCode);
						if (attr == null) {
							attr = oldAttributes.get(attrCode);
						}
						if (attr == null) {
							attr = newAttribute(rs);
							attr.setBlockId(AttributeBlock.ID_REMOVED);
							oldAttributes.put(attrCode, attr);
						}
						final Class<? extends Attribute> clazz = attr.getClass();
						if (StringAttribute.class.equals(clazz))
							((StringAttribute) attr).setValue(rs.getString(3));
						else if (TextAttribute.class.equals(clazz))
							((TextAttribute) attr).setValue(rs.getString(3));
						else if (IntegerAttribute.class.equals(clazz))
							((IntegerAttribute) attr).setValue(rs.getInt(2));
                        else if (LongAttribute.class.equals(clazz))
							((LongAttribute) attr).setValue(rs.getLong(2));
						else if (DateAttribute.class.equals(clazz)) {
							((DateAttribute) attr).setValueWithTZ(rs.getTimestamp(4));
						} else if (ListAttribute.class.equals(clazz))
							((ListAttribute) attr).setValue( 
									hasValue ? getRefValue(rs) : null
							);
						else if (TreeAttribute.class.equals(clazz)) {
							if (hasValue) {
								Collection<ReferenceValue> values = ((TreeAttribute) attr).getValues();
								if (values == null) {
									values = new ArrayList<ReferenceValue>();
									((TreeAttribute) attr).setValues(values);
								}
								values.add(getRefValue(rs));
							}
						} else if (PersonAttribute.class.equals(clazz)) {
							if (hasValue) {
								Collection<Person> values = ((PersonAttribute) attr).getValues();
								if (values == null) {
									values = new ArrayList<Person>();
									((PersonAttribute) attr).setValues(values);
								}
								values.add(getPerson(rs));
							}
						} else if (CardLinkAttribute.class.equals(clazz)) {
							if (hasValue) {
								((CardLinkAttribute) attr).addLinkedId(new ObjectId(Card.class, rs.getLong(2)));
							}
						} else if (HtmlAttribute.class.equals(clazz)) {
							if (!hasValue) {
								((HtmlAttribute) attr).setValue( null);
							} else {
								try {
									((HtmlAttribute) attr).setValue(SimpleDBUtils.getBlobAsStr(rs, 7, "UTF-8"));
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
						} else if (TypedCardLinkAttribute.class.equals(clazz)) {
							if (hasValue) {
								((TypedCardLinkAttribute) attr).addLinkedId(new ObjectId(Card.class, rs.getLong(2)));
								final Long typeId = rs.getLong(5) == 0 ? null : rs.getLong(5);
								((TypedCardLinkAttribute) attr).addType(rs.getLong(2), typeId);
							}
						} else if (DatedTypedCardLinkAttribute.class.equals(clazz)) {
							if (hasValue) {
								((DatedTypedCardLinkAttribute) attr).addLinkedId(new ObjectId(Card.class, rs.getLong(2)));
								final Date date = DateUtils.setValueWithTZ(rs.getTimestamp(4));
								final Long typeId = rs.getLong(5) == 0 ? null : rs.getLong(5);
								((DatedTypedCardLinkAttribute) attr).addTypeDate(rs.getLong(2), typeId, date);
							}
						} else if (BackLinkAttribute.class.equals(clazz)) {
							// hasValue ����� ������ false, �.�. backlink �� ��������.
							final long link = rs.getLong(21);
							final boolean hasLink = link > 0;
							((BackLinkAttribute) attr).setLinked(hasLink);
							if (hasLink) {
								final Card linkedCard = new Card();
								linkedCard.setId(link);
								((BackLinkAttribute) attr).addLabelLinkedCard(linkedCard);
							}
						}

						attr.setMandatory(rs.getBoolean(16));
						attr.setHidden(rs.getBoolean(17));
						if (!AttributeTypes.isReadOnlyType(attr.getType()))
							attr.setReadOnly(rs.getBoolean(18));
						return attr;
					}

					private Attribute newAttribute(ResultSet rs) throws SQLException {
						Attribute attr = AttributeTypes.createAttribute(rs.getString(8), rs.getString(1));
						attr.setNameRu(rs.getString(9));
						attr.setNameEn(rs.getString(10));
						attr.setActive(rs.getBoolean(11));
						return attr;
					}

					private ReferenceValue getRefValue(ResultSet rs) throws SQLException {
						if (rs.getObject(5)==null)
							return null;
						final ReferenceValue value = new ReferenceValue();
						value.setId(rs.getLong(5));
						if (ReferenceValue.ID_ANOTHER.equals(value.getId())) {
							value.setValueRu(rs.getString(6));
							value.setValueEn(rs.getString(6));
						} else {
							value.setValueRu(rs.getString(12));
							value.setValueEn(rs.getString(13));
						}
						return value;
					}

					private Person getPerson(ResultSet rs) throws SQLException {
						if (rs.getObject(2) == null)
							return null;
						Person user = new Person();
						user.setId(rs.getLong(2));
						user.setFullName(rs.getString(14));
						user.setEmail(rs.getString(15));
						user.setLogin(rs.getString(22));
						if (rs.getObject(19) != null) {
							user.setCardId(new ObjectId(Card.class, rs.getLong(19)));
						}
						return user;
					}
				});

		if (oldAttributes.size() > 0) {
			TemplateBlock block = (TemplateBlock) getJdbcTemplate().queryForObject(
					"SELECT block_code, block_name_rus, block_name_eng FROM attr_block " +
					"WHERE block_code=?",
					new Object[] { AttributeBlock.ID_REMOVED.getId() },
					new int[] { Types.VARCHAR},
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							TemplateBlock block = new TemplateBlock();
							block.setId(rs.getString(1));
							block.setNameRu(rs.getString(2));
							block.setNameEn(rs.getString(3));
							return block;
						}
					});
			block.setTemplate(card.getTemplate());
			block.setAttributes(new ArrayList<Attribute>(oldAttributes.values()));
			blocks.add(block);
		}

		SecurityAttribute acl = (SecurityAttribute) attrIds.get(Attribute.ID_SECURITY.getId());
		acl.setAccessList(getJdbcTemplate().query(
				"SELECT a.acl_id, a.role_code, a.value_id, a.person_id, " +
					"v.value_rus, v.value_eng, p.full_name " +
				"FROM access_control_list a " +
					"LEFT OUTER JOIN values_list v ON a.value_id=v.value_id " +
					"LEFT OUTER JOIN person p ON a.person_id=p.person_id " +
				"WHERE a.card_id=?",
				new Object[] { card.getId().getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						AccessListItem item = new AccessListItem();
						item.setId(rs.getLong(1));
						if (rs.getObject(2) != null) {
							item.setRoleType(rs.getString(2));
						} else if (rs.getObject(3) != null) {
							ReferenceValue dept = new ReferenceValue();
							dept.setId(rs.getLong(3));
							dept.setReference(Reference.ID_DEPARTMENT);
							dept.setValueRu(rs.getString(5));
							dept.setValueEn(rs.getString(6));
							item.setDepartment(dept);
						} else if (rs.getObject(4) != null) {
							Person user = new Person();
							user.setId(rs.getLong(4));
							user.setFullName(rs.getString(7));
							item.setPerson(user);
						}
						return item;
					}
				}));
		card.setAttributes(blocks);
		
		LockManagement storage = (LockManagement) getBeanFactory().getBean(LOCK_MANAGEMENT_BEAN);
		LockInfo li = storage.getLockInfoByObject(getId());
		card.setLocker(li.getLockedBy());
		card.setLockTime(li.getLockDate());
		card.setCanRead(true); // ���� AccessChecker ��������� ����� ���������� �� ����� �������, �� ������ �� ������� ���� ����� �� ������ ����.
		card.setCanWrite(OperationResult.SUCCESS.equals(storage.canLock(getId(), getUser().getPerson(), getSessionId())));
		
		return card;
	}
}