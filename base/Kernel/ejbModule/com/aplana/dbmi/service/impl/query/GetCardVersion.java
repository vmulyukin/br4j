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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.model.AccessListItem;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * {@link ObjectQueryBase} descendant used to fetch single {@link CardVersion} instance from database
 */
public class GetCardVersion extends ObjectQueryBase
{
	/**
	 * Fetches single {@link CardVersion} instance from database
	 * @return fully initialized {@link CardVersion} instance
	 */
	public Object processQuery() throws DataException
	{
		CardVersion.CompositeId versionId = (CardVersion.CompositeId) getId().getId();
		final HashMap material = new HashMap();
		CardVersion card = (CardVersion) getJdbcTemplate().queryForObject(
				"SELECT v.card_id, v.version_id, v.version_date, v.status_id, " +//v.parent_card_id, " +
					"c.template_id, t.template_name_rus, t.template_name_eng, v.file_name, v.external_path " +
					"FROM card_version v " +
					"INNER JOIN card c ON v.card_id=c.card_id " +
					"INNER JOIN template t ON c.template_id=t.template_id " +
					"WHERE v.card_id=? AND v.version_id=?",
				new Object[] { new Long(versionId.getCard()), new Integer(versionId.getVersion()) },
				new int[] { Types.NUMERIC, Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						CardVersion card = new CardVersion();
						card.setId(rs.getLong(1), rs.getInt(2));
						card.setVersionDate(new Date(rs.getDate(3).getTime()));
						card.setState(CardState.getId(rs.getInt(4)));
						/*if (rs.getObject(5) != null)
							card.setParent(rs.getLong(5));*/
						card.setTemplate(rs.getLong(5));
						card.setTemplateNameRu(rs.getString(6));
						card.setTemplateNameEn(rs.getString(7));
						/*card.setFileName(rs.getString(8));
						card.setUrl(rs.getString(9));*/
						if (rs.getString(8) != null)
							material.put(AttributeUtils.MATERIAL_FILE, rs.getString(4));
						if (rs.getString(9) != null)
							material.put(AttributeUtils.MATERIAL_URL, rs.getString(5));
						return card;
					}
				});
		if (card == null)
			return card;
		
		/*if (card.getParent() != null) {
			card.setParentName((String) getJdbcTemplate().queryForObject(
					"SELECT string_value FROM attribute_value " +
					"WHERE card_id=? AND attribute_code=?",
					new Object[] { card.getParent().getId(), Attribute.ID_NAME.getId() },
					String.class));
		}*/
		
		ChildrenQueryBase blockQuery = getQueryFactory().getChildrenQuery(Template.class, TemplateBlock.class);
		blockQuery.setParent(card.getTemplate());
		Collection blocks = (Collection) getDatabase().executeQuery(getUser(), blockQuery);
			/*ListTemplateBlocks blockQuery = new ListTemplateBlocks();
			blockQuery.setJdbcTemplate(getJdbcTemplate());
			blockQuery.setParent(card.getTemplate());
			Collection blocks = (Collection) blockQuery.processQuery();*/
		/*ListBlockAttributes attrQuery = new ListBlockAttributes();
		attrQuery.setJdbcTemplate(getJdbcTemplate());*/
		final HashMap attrIds = new HashMap();
		final ArrayList otherAttributes = new ArrayList();

		boolean hasMaterialAttr = false;
		final ArrayList links = new ArrayList();
		Iterator iBlock = blocks.iterator();
		while (iBlock.hasNext()) {
			TemplateBlock block = (TemplateBlock) iBlock.next();
			//attrQuery.setParent(block.getId());
			Collection attributes = block.getAttributes();//(Collection) attrQuery.processQuery();
			//block.setAttributes(attributes);
			Iterator iAttr = attributes.iterator();
			while (iAttr.hasNext()) {
				Attribute attr = (Attribute) iAttr.next();
				attrIds.put(attr.getId().getId(), attr);
				if (attr instanceof TreeAttribute)
					((TreeAttribute) attr).setValues(new ArrayList());
				else if (attr instanceof PersonAttribute)
					((PersonAttribute) attr).setValues(new ArrayList());
				else if (attr instanceof CardLinkAttribute)
					links.add(attr);
				else if (Attribute.TYPE_MATERIAL.equals(attr.getType())) {
					AttributeUtils.fillMaterial((MaterialAttribute) attr, material);
					hasMaterialAttr = true;
				}
			}
		}

		if (material.size() > 0 && !hasMaterialAttr) {
			ObjectQueryBase attrQuery = getQueryFactory().getFetchQuery(Attribute.class);
			attrQuery.setId(Attribute.ID_MATERIAL);
			MaterialAttribute attr = (MaterialAttribute) getDatabase().executeQuery(getUser(), attrQuery);
			AttributeUtils.fillMaterial(attr, material);
			otherAttributes.add(attr);
		}
		
		getJdbcTemplate().query(
				"SELECT v.attribute_code, v.number_value, v.string_value, v.date_value, " +
					"v.value_id, v.another_value, v.long_binary_value, " +
					"a.data_type, a.attr_name_rus, a.attr_name_eng, a.is_active, " +
					"r.value_rus, r.value_eng, p.full_name, p.email " +
				"FROM attribute_value_hist v " +
					"INNER JOIN attribute a ON v.attribute_code=a.attribute_code " +
					"LEFT OUTER JOIN values_list r ON v.value_id=r.value_id " +
					"LEFT OUTER JOIN person p ON v.number_value=p.person_id " +
				"WHERE v.card_id=? AND v.version_id=?",
				new Object[] { new Long(versionId.getCard()), new Integer(versionId.getVersion()) },
				new int[] { Types.NUMERIC, Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Attribute attr = (Attribute) attrIds.get(rs.getString(1));
						if (attr == null) {
							attr = newAttribute(rs);
							otherAttributes.add(attr);
							attr.setBlockId(AttributeBlock.ID_REST);
						}
						Class clazz = attr.getClass();
						if (StringAttribute.class.equals(clazz))
							((StringAttribute) attr).setValue(rs.getString(3));
						else if (TextAttribute.class.equals(clazz))
							((TextAttribute) attr).setValue(rs.getString(3));
						else if (IntegerAttribute.class.equals(clazz))
							((IntegerAttribute) attr).setValue(rs.getInt(2));
						else if (DateAttribute.class.equals(clazz)) {
							if (((DateAttribute) attr).isShowTime())
								//((DateAttribute) attr).setValue(rs.getTimestamp(4));
								((DateAttribute) attr).setValueWithTZ(rs.getTimestamp(4));
							else
								((DateAttribute) attr).setValue(rs.getDate(4));
						} else if (ListAttribute.class.equals(clazz))
							((ListAttribute) attr).setValue(getRefValue(rs));
						else if (TreeAttribute.class.equals(clazz)) {
							ArrayList values = (ArrayList) ((TreeAttribute) attr).getValues();
							if (values == null) {
								values = new ArrayList();
								((TreeAttribute) attr).setValues(values);
							}
							values.add(getRefValue(rs));
						} else if (PersonAttribute.class.equals(clazz)) {
							ArrayList values = (ArrayList) ((PersonAttribute) attr).getValues();
							if (values == null) {
								values = new ArrayList();
								((PersonAttribute) attr).setValues(values);
							}
							values.add(getPerson(rs));
							//((PersonAttribute) attr).setPerson(getPerson(rs));
						} else if (CardLinkAttribute.class.equals(clazz)) {
							// (2010/02, RuSA)
							((CardLinkAttribute) attr).addLinkedId(new ObjectId(Card.class, rs.getLong(2)));
						} else if (HtmlAttribute.class.equals(clazz)) {
							//Blob html = rs.getBlob(7);
							//if (html != null)
							if (rs.getObject(7) != null)
								try {
									((HtmlAttribute) attr).setValue(
											//new String(html.getBytes(1, (int) html.length()), "UTF-8"));
											new String(rs.getBytes(7), "UTF-8"));
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
						return attr;
					}
					
					Attribute newAttribute(ResultSet rs) throws SQLException {
						Attribute attr = AttributeTypes.createAttribute(rs.getString(8), rs.getString(1));
						attr.setNameRu(rs.getString(9));
						attr.setNameEn(rs.getString(10));
						attr.setActive(rs.getBoolean(11));
						return attr;
					}
					
					ReferenceValue getRefValue(ResultSet rs) throws SQLException {
						ReferenceValue value = new ReferenceValue();
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
					
					Person getPerson(ResultSet rs) throws SQLException {
						if (rs.getObject(2) == null)
							return null;
						Person user = new Person();
						user.setId(rs.getLong(2));
						user.setFullName(rs.getString(14));
						user.setEmail(rs.getString(15));
						return user;
					}
				});

		// (2010/02, RuSA) OLD: AttributeUtils.initCardLinkAttributes(links, getQueryFactory(), getDatabase(), getUser());

		if (otherAttributes.size() > 0) {
			TemplateBlock block = (TemplateBlock) getJdbcTemplate().queryForObject(
					"SELECT block_code, block_name_rus, block_name_eng FROM attr_block " +
					"WHERE block_code=?",
					new Object[] { AttributeBlock.ID_REST.getId() },
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
			block.setAttributes(otherAttributes);
			blocks.add(block);
		}
		
		SecurityAttribute acl = (SecurityAttribute) attrIds.get(Attribute.ID_SECURITY.getId());
		acl.setAccessList(getJdbcTemplate().query(
				"SELECT a.role_code, a.value_id, a.person_id, v.value_rus, v.value_eng, p.full_name " +
				"FROM access_control_list_hist a " +
				"LEFT OUTER JOIN values_list v ON a.value_id=v.value_id " +
				"LEFT OUTER JOIN person p ON a.person_id=p.person_id " +
				"WHERE a.card_id=? AND a.version_id=?",
				new Object[] { new Long(versionId.getCard()), new Integer(versionId.getVersion()) },
				new int[] { Types.NUMERIC, Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						AccessListItem item = new AccessListItem();
						//item.setId(rs.getLong(1));
						if (rs.getObject(1) != null) {
							item.setRoleType(rs.getString(1));
						} else if (rs.getObject(2) != null) {
							ReferenceValue dept = new ReferenceValue();
							dept.setId(rs.getLong(2));
							dept.setReference(Reference.ID_DEPARTMENT);
							dept.setValueRu(rs.getString(4));
							dept.setValueEn(rs.getString(5));
							item.setDepartment(dept);
						} else if (rs.getObject(4) != null) {
							Person user = new Person();
							user.setId(rs.getLong(4));
							user.setFullName(rs.getString(6));
							item.setPerson(user);
						}
						return item;
					}
				}));
		card.setAttributes(blocks);
		return card;
	}
}