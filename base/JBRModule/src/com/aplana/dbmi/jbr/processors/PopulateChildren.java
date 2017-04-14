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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.IdUtils.DataIdDescriptor;
import com.aplana.dbmi.jbr.util.IdUtils.IdPair;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/*
 * (YNikitin, 2011/01/24) ������� � ��������� ���������� � ������� ����������� ������� � private �� protected
 * ��� ����������� �� ������������� � �������� ������ � ����������� ��������� �� ProcessCard,
 * ����� � �������� ������� ����� ������ � ������� ProcessCard
 * (2011/02/10) ������� ����� ������� ��������: forceSaveMode
 */
public class PopulateChildren extends ProcessCard implements Parametrized
{
	private static final long serialVersionUID = 1L;

	/**
	 * �������� ���������� - ������������� �������, �� �������� ��������� �������� ��������.
	 * <b>������������.</b> ����� ��������� ���������������� ������������� ������� (�� ��),
	 * ���� ��� �������, �������� ����� <code>objectids.properties</code> (�������������).
	 */
	public static final String PARAM_TEMPLATE = "template";
	/**
	 * �������� ���������� - ������������� ��������������, � ������� ����������� ��������
	 * ��������, ���� <code>attach_reverse==false</code>, ��� ������������� ��������������, 
	 * � ������� ����������� �������� ��������, ���� <code>attach_reverse==true</code>. 
	 * <b>������������.</b> ����� ��������� ��� ���������������� �������������
	 * �������������� (�� ��), ��� � ��� ������� �� <code>objectids.properties</code>
	 * (�������������). �������������� ������ ����� ��� <code>link (C)</code>.
	 */
	public static final String PARAM_ATTACH_ATTR = "attach";
	/**
	 * �������� ����������, �������� ������� �������� �������� ��������. <b>�� ������������.</b>
	 * ������ ���������:<br>
	 * <code>[<i>���</i><b>:</b>]<i>��������_��������������</i>[<b>-&gt;</b>[<i>���</i><b>:</b>]<i>��������_��������������</i>]</code>
	 * <br>(���������� ������ ��������� �� �������������� ��������.)
	 * �������� ������������� � ��� ��������������, �� ��������� ������� ���������
	 * �������� ��������, � ����� ������������� � ��� ��������������, � �������
	 * ������������ ��������� �������� �������� ��������������. ��� ���������� ������
	 * �������� ���� ��� �������� �������������� ��������������� <code>link (C)</code>,
	 * ��� �������� - ��� ��, ��� � ��������. ���� �� ������ ������������� ��������
	 * ��������������, ������������ ��� ��, ��� � ��� ��������.
	 * <p>������, �������� ���� �������������, ������� � ...
	 * � �������� �������������� ����� �������������� ��� ��������������� �������� �� ��,
	 * ��� � ��� ������� �� ����� <code>objectids.properties</code> (�������������).
	 * <p>���������� ���� ������������� ��� ������� ��������� ���������� ����,
	 * ��� ����� ��������� ��������� ��������: <code>link (C)</code>, <code>user (U)</code>,
	 * <code>tree (H)</code>, <code>typedLink (E)</code>.
	 * ���� ���� �������� �� �����, �� ����� ������� ���� �������� ��������
	 */
	public static final String PARAM_SPLIT_ATTR = "split";
	/**
	 * �������� ����������, �������� ������� ����������� ������������� �� �������� ��������
	 * � ��������. ������ ��������� ���������� {@link #PARAM_SPLIT_ATTR}, �� ����� ���������
	 * ��������� ����� ������, ����������� ������� ��� ������ � �������. ����� �� ���������
	 * ����� ����� �������������, ���� �� ��� ���� ��� ����� ����� ������������ �����������
	 * ��������������.
	 */
	public static final String PARAM_COPY_LIST = "copy";

	/**
	 * ������ � ����� ���������, ������������ ������ ��� �������� ���������.
	 */
	public static final String PARAM_FILTER_PREFIX = "filter".toLowerCase();

	/**
	 * ����� ������� ������� - ����� ���������� ������� �������� ����� �������� � ��� �������:
	 * 1. CARD - ��� �������� (�� ���������)
	 * 2. ATTRIBUTE - ������ ������� �������
	 * 3. ��� ��������� - �� ��������� ��������
	 */
	public static final String PARAM_FORCE_SAVE_MODE = "forceSaveMode";

	/**
	 * ����� ������� ������� ��� ���������� �������� �������� ����� ����� �� �������� � ������������
	 */
	public static final String PARAM_FORCE_CHILDS = "forceSaveChilds";

	/**
	 * ����� ������� ������� ��� ������� ��� ���������: CARD (�� ���������) - ��� �������� �������, ATTRIBUTE - ������ �������, � �������� �������������
	 */
	// public static final String PARAM_FORCE_SAVE_MODE = "forceSaveMode";
	protected static final String REG_SEPARATOR = "[,;]";
	protected static final String REG_INDEX_SEPARATOR = "@";
	protected static final String REG_NUMBER_SYMBOL = "#";
	protected static final String FILTER_PARTS_SEPARATOR = " in ";

//	protected static final Pattern REG_FORMAT = Pattern.compile(
//			//    source attribute                                destination attribute
//			//    [type            :]     id               [->    [type          :]     id               ]
//			"\\s*(([^:\\-\\s]*)\\s*:)?\\s*([^:\\-\\s]*)\\s*(->\\s*(([^:\\s]*)\\s*:)?\\s*([^:\\-\\s]*)\\s*)?");
//	private static final int GROUP_SRC_TYPE = 2;
//	private static final int GROUP_SRC_ID = 3;
//	private static final int GROUP_DEST_TYPE = 6;
//	private static final int GROUP_DEST_ID = 7;

	protected ObjectId templateId;
	protected ObjectId attachAttrId;
	
	/**
	 * ������������� ���� ����� ������������ �������� � ��������. 
	 * �������������� ����� ��� <code>boolean</code>.
	 * �������� <code>true</code> - <b>Backlink</b>, <code>false</code> - <b>Cardlink</b>.
	 * <b>Default</b> - <code>false</code>.
	 * ��������������� � {@link #setParameter(String, String)}
	 */
	protected boolean attachReverse = false;
	
	protected ObjectId[] pathAttrIds;
	protected IdPair splitAttrIds = null;

	/** ����������� �������
	 * ����������� � ������-����������
	 */
	protected Map<String, AttributeDataFilter> filters = new HashMap<String, AttributeDataFilter>();

	/*
	 * id �������� � �������� �������� -> ��������� �����������
	 */
	protected HashMap<ObjectId, IdPair> copyAttrIds = new HashMap<ObjectId, IdPair>();

	protected boolean forceSaveChilds = false;
	protected String forceSaveMode = "CARD";	// ����� ���������� ������� �������� ("CARD" - ��� ��������, "ATTRIBUTE" - ������ �������, ����� �� ���������)

	@Override
	public Object process() throws DataException
	{
		if (templateId == null || attachAttrId == null)
			throw new IllegalStateException("Not all mandatory parameters were set");
		final Card source = loadCard(getCardId());
		/*	((ChangeState) getAction()).getCard();
		if (parent.getTemplate() == null)
			throw new IllegalStateException("Card isn't loaded");*/
		Card parent = source;
		final List<ObjectId> childs = new ArrayList<ObjectId>();
		if (pathAttrIds != null) {
			for (int i = 0; i < pathAttrIds.length; i++) {
				final CardLinkAttribute link = (CardLinkAttribute) parent.getAttributeById(pathAttrIds[i]);
				if (link == null)
					throw new IllegalStateException("Attribute " + pathAttrIds[i].getId() +
							" not found in card " + parent.getId().getId());
				if (link.getLinkedCount() != 1)
					throw new IllegalStateException("Attribute " + pathAttrIds[i].getId() +
							" has " + link.getLinkedCount() + " links in card " + parent.getId().getId() +
							"; can't choose one");
				parent = loadCard(link.getSingleLinkedId());
			}
		}

		final LinkAttribute attachAttr = (LinkAttribute) parent.getAttributeById(attachAttrId);
		if (attachAttr == null)
			throw new IllegalStateException("Source attach attribute " + attachAttrId +
					" not found at card " + source.getId().getId());
		if (splitAttrIds != null) {
			final Attribute splitAttr = source.getAttributeById(splitAttrIds.sourceId());
			if (splitAttr == null)
				throw new IllegalStateException("Dest split attribute " + splitAttrIds.sourceId() +
						" not found at card " + source.getId().getId());
			for (Iterator<?> itr = splitAttributeValue(splitAttr); itr.hasNext(); ) {
				final ObjectId child = createChild(source, itr.next());
				childs.add(child);
			}
		} else {
			final ObjectId child = createChild(source, null);
			childs.add(child);
		}
		
		if(!attachReverse) {
			if ("CARD".equalsIgnoreCase(forceSaveMode)) {
				saveParent(parent);
			} else if ("ATTRIBUTE".equalsIgnoreCase(forceSaveMode)) {
				updateAttribute(parent.getId(), attachAttr);
			}
		}

		if (forceSaveChilds){
			for(ObjectId c: childs){
				final Card childCard = loadCard(c);
				saveParent(childCard);
			}
		}

		if (getAction()!=null){
			((ChangeState) getAction()).setCard(source);
		}

		return null;
	}

	protected ObjectId createChild(Card parent, Object variant)
		throws DataException
	{
		final LinkAttribute attachAttr = (LinkAttribute) parent.getAttributeById(attachAttrId);
		final CreateCard create = new CreateCard(templateId);
		final Card child = (Card) super.execAction(create, getSystemUser());

		if (splitAttrIds != null) {
			setAttributeValue(child.getAttributeById(splitAttrIds.destId()), variant);
		}

		// ���������� �������������� �������� ...
		fillChildCard(child, parent);
		
		/* ������ ������� ��������� ����� ����� ����������, 
		 * ����������� ����� ����������� �������� ��������,
		 * �.�. �� �������� ������ ���� �������� � ��������,
		 * ���� ����� ���� BackLink
		 */
		if(attachReverse) {
			ObjectId linked = ((BackLinkAttribute) attachAttr).getLinkSource();
			CardLinkAttribute cla = child.getCardLinkAttributeById(linked);
			if (cla == null)
				throw new IllegalStateException("Source attach attribute " + attachAttrId +
						" not found at card " + child.getId().getId());
			cla.addLinkedId(parent.getId());
		}

		// ������ ���������� �������� - �������� ����� ������� ������������� ...
		final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(child);
		saveQuery.setObject(child);
		final ObjectId id = (ObjectId) getDatabase().executeQuery(getSystemUser(), saveQuery);
		logger.info("new child card created: id=" + id+ " by template "+ templateId);

		// ������������� �������� ...
		super.execAction(new UnlockObject(id), getSystemUser());

		logger.debug("new child card unlocked successfully, card id=" + id);

		/* ������ ������� ��������� ����� ����� ����������, 
		 * ����������� ����� ���������� �������� ��������,
		 * ����� ��� �������� �� ��,
		 * �.�. �� �������� ������ ���� �������� � ������������,
		 * ���� ����� ���� CardLink
		 */
		if(!attachReverse) {
			attachAttr.addLinkedId(id);
		}

		return id;
	}


	/**
	 * @param child
	 * @param parent
	 */
	private void fillChildCard(Card child, Card parent)
	{
		for (Map.Entry<ObjectId, IdPair> entry : copyAttrIds.entrySet() ) {
			try {
				final IdPair pair = entry.getValue();
				AttributeDataFilter filter = null;

				// ������ �� �������� ������ ��� �����������
				if (pair.sourceFilterName() != null) {
					filter = filters.get(pair.sourceFilterName().trim().toLowerCase());
					if (filter == null) {
						logger.warn("config problem: data filter not found '"+ pair.sourceFilterName() + "' -> attribute '"+pair.sourceId()+"' copy skipped, no filtering performed");
						continue;
					}
				}

				final Attribute src = parent.getAttributeById( entry.getKey());
				final Attribute dest = child.getAttributeById( pair.dest.getId() );
				copyAttrbiuteValue( src, dest);

				if (filter != null)
					filter.doFilter(dest, parent);

			} catch (Exception e) {
				logger.warn("Error copying attribute " + entry.getKey() +
						" of parent card "+parent.getId() +
						" to new card into attribute " + entry.getValue()+
						" -> skip attribute", e);
				// just skipping this attribute
			}
		}
	}

	protected Card loadCard(ObjectId id) throws DataException
	{
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(id);
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}

	/**
	 * ��������� ��������� �������� �������� �� �� id.
	 * @param ids
	 * @param attributes
	 * @return
	 * @throws DataException
	 */
	protected List<Card> searchCards(Collection<ObjectId> ids, ObjectId[] attributes)
			throws DataException
	{
		final Search search = new Search();

		search.setByCode(true);
		search.setWords(IdUtils.makeIdCodesEnum(ids, ","));

		final List<SearchResult.Column> columns = CardUtils.createColumns( Card.ATTR_STATE, Card.ATTR_TEMPLATE);
		CardUtils.addColumns( columns, attributes);
		search.setColumns(columns);

		final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		return cards;
	}

	protected void saveParent(Card card) throws DataException
	{
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		execAction(new LockObject(card.getId()));
		try {
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(card.getId()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParameter(String name, String value)
	{
		if (name == null) return;

		if (PARAM_TEMPLATE.equalsIgnoreCase(name))
			templateId = ObjectIdUtils.getObjectId(Template.class, value, true);

		else if (PARAM_ATTACH_ATTR.equalsIgnoreCase(name)) {
			final String[] ids = value.split(":");
			if (ids.length > 1) {
				pathAttrIds = new ObjectId[ids.length - 1];
				for (int i = 0; i < ids.length - 1; i++)
					pathAttrIds[i] = ObjectIdUtils.getObjectId(CardLinkAttribute.class, ids[i], false);
			}
			final List<Class<? extends Attribute>> listAttrClasses 
				= new ArrayList<Class<? extends Attribute>>(Arrays.asList(CardLinkAttribute.class, BackLinkAttribute.class));
			attachAttrId = ObjectIdUtils.getObjectId(listAttrClasses, CardLinkAttribute.class, ids[ids.length - 1], false);
			if(BackLinkAttribute.class.isAssignableFrom(attachAttrId.getType())) {
				attachReverse = true;
			} else if(CardLinkAttribute.class.isAssignableFrom(attachAttrId.getType())) { 
				attachReverse = false;
			} else
				throw new UnsupportedOperationException(attachAttrId.getClass() + " attach attr is not suppported");
		}

		else if (PARAM_SPLIT_ATTR.equalsIgnoreCase(name))
			splitAttrIds = IdPair.parseRule(value.trim());

		else if (PARAM_COPY_LIST.equalsIgnoreCase(name)) {
			final String[] list = value.split(REG_SEPARATOR);
			for (int i = 0; i < list.length; i++) {
				final IdPair pair = IdPair.parseRule(list[i]);
				copyAttrIds.put(pair.sourceId(), pair);
			}
		}

		else if (name.toLowerCase().startsWith(PARAM_FILTER_PREFIX)) {
			// <parameter name="filter.report_of_executor" value="user:jbr.report.int.executor in #0@user:jbr.AssignmentExecutor"/>
			final AttributeDataFilter filter = parseFilter(value);
			if (filter != null)
				filters.put( name.toLowerCase(), filter);
		}

		else if (PARAM_FORCE_CHILDS.equalsIgnoreCase(name)) {
			forceSaveChilds = Boolean.parseBoolean(value);
		}

		else if (PARAM_FORCE_SAVE_MODE.equals(name)){
			forceSaveMode = value.toUpperCase();
		}

		else
			// throw new IllegalArgumentException("Unknown parameter: " + name);
			super.setParameter(name, value);
	}

	/**
	 * ������� ������ �� �����-������ ����:
	 * 		"type:attr_src in type:attr_dest"
	 * ��������:
	 * 		"user:jbr.report.int.executor in #0@user:jbr.AssignmentExecutor"
	 * @param configLine
	 * @return
	 */
	protected AttributeDataFilter parseFilter(String filterStr) {

		if (filterStr == null || filterStr.length() == 0 )
				return null;

		Class<? extends Attribute> type = CardLinkAttribute.class;	// default attribute type

		// ������� ����� ����: "���������_������������  in  ���������_�_�������" ...
		final String[] parts = filterStr.split(FILTER_PARTS_SEPARATOR);
		final DataIdDescriptor desc1 = DataIdDescriptor.parseObjectId(parts[0], type);

		if (desc1.getId() != null)
				type = desc1.getId().getType(); 	// set default equals to source attribute

		final String destPart = (parts.length > 1) ? parts[1] : parts[0];
		final DataIdDescriptor desc2 = DataIdDescriptor.parseObjectId( destPart, type);

		final DataFilterEqOrInside filter = new DataFilterEqOrInside( desc1, desc2);
		return filter;
	}


	protected Iterator<?> splitAttributeValue(Attribute attr) {
		if (Attribute.TYPE_TREE.equals(attr.getType()))
			return ((TreeAttribute) attr).getValues().iterator();
		if (Attribute.TYPE_PERSON.equals(attr.getType()))
			return ((PersonAttribute) attr).getValues().iterator();
		if (Attribute.TYPE_CARD_LINK.equals(attr.getType()))
			return ((CardLinkAttribute) attr).getIdsLinked().iterator();
		if (Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType()))
			return ((TypedCardLinkAttribute) attr).getIdsLinked().iterator();
		if (Attribute.TYPE_BACK_LINK.equals(attr.getType()))
			return listLinkedCards(attr.getId()).iterator();
		throw new IllegalArgumentException("Attribute " + attr.getId().getId() +
				"'s type doesn't support splitting (single-valued)");
	}

	protected Collection<?> splitAttributeValue2(Attribute attr) {
		if (Attribute.TYPE_TREE.equals(attr.getType()))
			return ((TreeAttribute) attr).getValues();
		if (Attribute.TYPE_PERSON.equals(attr.getType()))
			return ((PersonAttribute) attr).getValues();
		if (Attribute.TYPE_CARD_LINK.equals(attr.getType()))
			return ((CardLinkAttribute) attr).getIdsLinked();
		if (Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType()))
			return ((TypedCardLinkAttribute) attr).getIdsLinked();
		if (Attribute.TYPE_BACK_LINK.equals(attr.getType()))
			return listLinkedCards(attr.getId());
		throw new IllegalArgumentException("Attribute " + attr.getId().getId() +
				"'s type doesn't support splitting (single-valued)");
	}

	protected void copyAttrbiuteValue(Attribute src, Attribute dest)
	{
		if (src == null) {
			logger.warn("Source attribute not found!");
			return;
		}

		Object value = null;
		if (Attribute.TYPE_STRING.equals(src.getType()))
			value = ((StringAttribute) src).getValue();
		else if (Attribute.TYPE_TEXT.equals(src.getType()))
			value = ((TextAttribute) src).getValue();
		else if (Attribute.TYPE_DATE.equals(src.getType()))
			value = ((DateAttribute) src).getValue();
		else if (Attribute.TYPE_INTEGER.equals(src.getType()))
			value = new Integer(((IntegerAttribute) src).getValue());
        else if (Attribute.TYPE_LONG.equals(src.getType()))
			value = new Long(((LongAttribute) src).getValue());
		else if (Attribute.TYPE_LIST.equals(src.getType()))
			value = ((ListAttribute) src).getValue();
		else if (Attribute.TYPE_TREE.equals(src.getType()))
			value = ((TreeAttribute) src).getValues();
		else if (Attribute.TYPE_PERSON.equals(src.getType()))
			value = ((PersonAttribute) src).getValues();
		else if (Attribute.TYPE_CARD_LINK.equals(src.getType()))
			value = ((CardLinkAttribute) src).getIdsLinked();
		else if (Attribute.TYPE_TYPED_CARD_LINK.equals(src.getType()))
			value = ((TypedCardLinkAttribute) src).getLabelLinkedMap();
		else if (Attribute.TYPE_BACK_LINK.equals(src.getType()))
			value = listLinkedCards(src.getId());
		else
			throw new IllegalArgumentException("Unsupported attribute type: " + src.getType());
		setAttributeValue(dest, value);
	}

	@SuppressWarnings("unchecked")
	protected void setAttributeValue(Attribute attr, Object value) {
		if (attr == null) {
			logger.warn("Target attribute not found!");
			return;
		}

		if (Attribute.TYPE_STRING.equals(attr.getType())) {
			// Accepts: any type (uses toString)
			((StringAttribute) attr).setValue(value == null ? null : value.toString());

		} else if (Attribute.TYPE_TEXT.equals(attr.getType())) {
			// Accepts: any type (uses toString)
			((TextAttribute) attr).setValue(value == null ? null : value.toString());

		} else if (Attribute.TYPE_DATE.equals(attr.getType())) {
			// Accepts: Date, Number (java timestamp)
			if (value instanceof Number) {
				logger.info("Attribute " + attr.getId().getId() + ": converting to date from " +
						value.getClass().getName() + " " + value);
				value = new Date(((Number) value).longValue());
			}
			if (value != null && !(value instanceof Date))
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((DateAttribute) attr).setValue((Date) value);

		} else if (Attribute.TYPE_INTEGER.equals(attr.getType())) {
			// Accepts: Number, String
			if (value instanceof String)
				value = new Integer((String) value);
			if (value != null && !(value instanceof Number))
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((IntegerAttribute) attr).setValue(value == null ? 0 : ((Number) value).intValue());
		} else if (Attribute.TYPE_LONG.equals(attr.getType())) {
			// Accepts: Number, String
			if (value instanceof String)
				value = new Long((String) value);
			if (value != null && !(value instanceof Number))
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((LongAttribute) attr).setValue(value == null ? 0 : ((Number) value).longValue());
		} else if (Attribute.TYPE_LIST.equals(attr.getType())) {
			// Accepts: Long (ref.value id), ObjectId, ReferenceValue; Collection of listed types with 0 or 1 items
			if (value instanceof Collection) {
				Collection<?> coll = (Collection<?>) value;
				if (coll.size() == 0)
					value = null;
				else if (coll.size() == 1)
					value = coll.iterator().next();
				else
					throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
							" to collection of values (size=" + coll.size() + ")");
				logger.info("Attribute " + attr.getId().getId() + ": converted to single value from "
						+ ((value == null)? "classNULL" : value.getClass().getName())
						+ " " + value);
			}
			if (value instanceof Long) {
				logger.info("Making reference value id from long " + value);
				value = new ObjectId(ReferenceValue.class, ((Long) value).longValue() );
			}
			if (value instanceof ObjectId)
				value = DataObject.createFromId((ObjectId) value);
			if (value != null && !(value instanceof ReferenceValue))
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((ListAttribute) attr).setValue((ReferenceValue) value);

		} else if (Attribute.TYPE_TREE.equals(attr.getType())) {
			// Accepts: Long (ref.value id), ObjectId, ReferenceValue; Collection of ReferenceValues
			if (value instanceof Long) {
				logger.info("Making reference value id from long " + value);
				value = new ObjectId(ReferenceValue.class, ((Long) value).longValue() );
			}
			if (value instanceof ObjectId)
				value = DataObject.createFromId((ObjectId) value);
			Collection<ReferenceValue> coll = null;
			if (value == null)
				coll = new ArrayList<ReferenceValue>(1);
			else if (value instanceof Collection)
				coll = (Collection<ReferenceValue>) value;
			else if (value instanceof ReferenceValue) {
				coll = new ArrayList<ReferenceValue>(1);
				coll.add( (ReferenceValue) value);
			} else
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((TreeAttribute) attr).setValues(coll);

		} else if (Attribute.TYPE_PERSON.equals(attr.getType())) {
			// Accepts: Long (person id), ObjectId, Person, Collection of Persons, Cards or ObjectIds
			if (value instanceof Collection) {
				final Collection<?> coll = (Collection<?>) value;
				for (Iterator<?> itr = coll.iterator(); itr.hasNext(); )
					addPerson((PersonAttribute) attr, itr.next());
			} else
				addPerson((PersonAttribute) attr, value);

		} else if (Attribute.TYPE_CARD_LINK.equals(attr.getType())) {
			// Accepts: Long (card id), ObjectId, Card; Collection of ObjectIds or Cards;
			//		String (comma separated card id list)
			if (value instanceof Long) {
				logger.info("Making card id from long " + value);
				value = new ObjectId(Card.class, ((Long) value).longValue());
			}
			if (value instanceof Integer) {
				logger.info("Making card id from integer " + value);
				value = new ObjectId(Card.class, ((Integer) value).longValue());
			}
			if (value instanceof Card) {
				value = ((Card) value).getId();
			}
			Collection coll = null;
			if (value == null)
				coll = new ArrayList<ObjectId>(1);
			else if (value instanceof Collection)
				coll = (Collection) value;
			else if (value instanceof ObjectId) {
				coll = new ArrayList<ObjectId>(1);
				coll.add(value);
			} else if (value instanceof String)
				coll = ObjectIdUtils.commaDelimitedStringToNumericIds((String) value, Card.class);
			else
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((CardLinkAttribute) attr).setIdsLinked(coll);

		} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(attr.getType())) {
			// Accepts: Long (card id), ObjectId, Card; Collection of ObjectIds or Cards;
			//		String (comma separated card id list)
			/*if (value instanceof Long) {
				logger.info("Making card id from long " + value);
				value = new ObjectId(Card.class, (Long) value);
			}
			if (value instanceof Card) {
				value = ((Card) value).getId();
			}
			Collection coll = null;
			if (value == null)
				coll = new ArrayList(1);
			else if (value instanceof Collection)
				coll = (Collection) value;
			else if (value instanceof ObjectId) {
				coll = new ArrayList(1);
				coll.add(value);
			} else if (value instanceof String)
				coll = ObjectIdUtils.commaDelimitedStringToNumericIds((String) value, Card.class);
			else
				throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
						" to value " + value);
			((TypedCardLinkAttribute) attr).setIdsLinked(coll);*/
			throw new UnsupportedOperationException("Typed card link attribute copying not implemented yet");

		} else
			throw new IllegalArgumentException("Unsupported attribute type: " + attr.getType());
	}

	@SuppressWarnings("unchecked")
	private void addPerson(PersonAttribute attr, Object value) {
		if (value instanceof Long) {
			logger.info("Making person id from long " + value);
			value = new ObjectId(Person.class, ((Long) value).longValue() );
		}
		if (value instanceof ObjectId)
			value = DataObject.createFromId((ObjectId) value);
		if (value instanceof Card)
			try {
				value = convertCardToPerson(((Card) value).getId());
			} catch (DataException e) {
				logger.error("Error converting card " + ((Card) value).getId().getId() +
						" to person; skipped", e);
				return;
			}
		if (value == null)
			return;		// Just skip
		if (value instanceof ObjectId)
			value = DataObject.createFromId((ObjectId) value);
		if (!(value instanceof Person))
			throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
					" to value " + value);
		Collection<Person> coll = attr.getValues();
		if (coll == null) {
			coll = new ArrayList<Person>();
			attr.setValues(coll);
		}
		coll.add( (Person) value);
	}

//	private void addCardLink(CardLinkAttribute attr, Object value) {
//		if (value instanceof Long) {
//			logger.info("Making card id from long " + value);
//			value = new ObjectId(Card.class, (Long) value);
//		}
//		if (value instanceof ObjectId)
//			value = DataObject.createFromId((ObjectId) value);
//		if (value instanceof Person)
//			try {
//				value = convertPersonToCard((Person) value);
//			} catch (DataException e) {
//				logger.error("Error converting person " + ((Person) value).getId().getId() +
//						" to card; skipped", e);
//				return;
//			}
//		if (value == null)
//			return;		// Just skip
//		if (!(value instanceof Person))
//			throw new IllegalArgumentException("Can't set attribute " + attr.getId().getId() +
//					" to value " + value);
//		attr.addLinkedId(((Card) value).getId());
//	}

	private ObjectId convertCardToPerson(ObjectId cardId) throws DataException {
		final Set<Person> ids = CardUtils.getPersonsByCards(Collections.singletonList(cardId),
				getQueryFactory(), getDatabase(), getSystemUser());
		if (ids.size() != 1) {
			logger.error("Found " + ids.size() + " persons for card " + cardId.getId());
			return null;
		}
		return ids.iterator().next().getId();
	}

//	private ObjectId convertPersonToCard(Person person) throws DataException {
//		if (person.getCardId() != null)
//			return person.getCardId();
//		ObjectQueryBase query = getQueryFactory().getFetchQuery(Person.class);
//		query.setId(person.getId());
//		person = (Person) getDatabase().executeQuery(getSystemUser(), query);
//		return person.getCardId();
//	}

	@SuppressWarnings("unchecked")
	private Collection<Card> listLinkedCards(ObjectId attrId)
	{
		final ListProject list = new ListProject();
		list.setAttribute(attrId);
		list.setCard(getCardId());
		final List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>(1);
		cols.add( CardUtils.createColumn(Card.ATTR_TEMPLATE));
		list.setColumns( cols);
		try {
			ActionQueryBase query = getQueryFactory().getActionQuery(list);
			query.setAction(list);
			return ((SearchResult) getDatabase().executeQuery(getSystemUser(), query)).getCards();
		} catch (DataException e) {
			logger.error("Error searching back links to card ...", e);
			return null;
		}
	}

	/**
	 * ������� ������, ������� � ������ ������ ��������� ������ ����� �������
	 * @param inputText
	 * @return
	 */
	public static String getStartIndex(String inputText){
		// ����� ������ �� ������� ������ ����������� ��������� # � @, ������� ����� � ������ ������
		// ToDo: �������� �������� �� ���������� ���������, ���-�� �����
//		String pattern = "^(["+REG_NUMBER_SYMBOL+"]\\s*["+REG_INDEX_SEPARATOR+"])";
/*		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(inputText);
		if (m.find()){
			return m.group();
		} else
			return null;
		*/
		final String[] elements = inputText.trim().split(REG_INDEX_SEPARATOR);
		if (elements.length > 0 && elements[0].startsWith(REG_NUMBER_SYMBOL)){
			return elements[0].substring(1);
		}
		return null;
	}

	/**
	 * ������� ������, � ������� ����� ������
	 * @param inputText
	 * @return
	 */
	public static String clearStartIndex(String inputText, String startIndex){
		// ����� ������ �� ������� ������ ����������� ��������� # � @, ������� ����� � ������ ������
		// ToDo: ���������� �� ���������� ���������
/*

		String pattern = "^(["+REG_NUMBER_SYMBOL+"]\\s*["+REG_INDEX_SEPARATOR+"])";
		if (inputText.matches(pattern)){
			return inputText.replaceFirst(pattern, "");
		} else {
			return inputText;
		}
*/
		if (startIndex != null && startIndex.length()>0){
			return inputText.replaceFirst(REG_NUMBER_SYMBOL+startIndex+REG_INDEX_SEPARATOR, "");
		}
		return inputText;
	}
	/**
	 * ��������� ���������� �������� ��� �������� cardId.
	 * @param cardId
	 * @param attr
	 * @throws DataException
	 */
	private void updateAttribute(ObjectId cardId, Attribute attr)
		throws DataException
	{
		final OverwriteCardAttributes action = new OverwriteCardAttributes();
		action.setCardId(cardId);
		action.setAttributes(Collections.singletonList(attr));
		action.setInsertOnly(false);

		super.execAction(new LockObject(cardId));
		try {
			super.execAction(action, getSystemUser());
		} finally {
			super.execAction(new UnlockObject(cardId));
		}
	}

	/**
	 * ����� ��� ����������� ������������ ������.
	 * @author RAbdullin
	 */
	protected abstract class AttributeDataFilter {

		/**
		 * ��������� ������������ ������ � �������� destAttr.
		 * @param destAttr ������� �������, � ������� ��������� ������������
		 * ������.
		 * @param mainCard ������� ��������, �� ������� ����� ������ ���
		 * ��������� � ��������.
		 * ��������, �������� � destAttr ����� ��������, � ������� �����������
		 * ����������� � ������ ����������� mainCard.
		 * @throws DataException
		 */
		public abstract void doFilter( Attribute destAttr, Card mainCard)
				throws DataException;
	}

	/**
	 * ������ ��� �������� ��������� �������� ������� ��������� (string, int)
	 * ��� ��������� ������ ������ �������� ������ �������.
	 * @author RAbdullin
	 */
	protected class DataFilterEqOrInside extends AttributeDataFilter
	{

		/**
		 * ����������� �������
		 */
		public final IdUtils.DataIdDescriptor chkDesc;

		/**
		 * ������� �� ������� ��������.
		 */
		public final IdUtils.DataIdDescriptor dataDesc;

		// enum FilterOperation { ... };
		// public FilterOperation operation;

		public DataFilterEqOrInside() {
			chkDesc = new IdUtils.DataIdDescriptor();
			dataDesc = new IdUtils.DataIdDescriptor();
		}

		public DataFilterEqOrInside(IdUtils.DataIdDescriptor chkDesc,
				IdUtils.DataIdDescriptor dataDesc) {
			super();
			this.chkDesc = (chkDesc != null) ? chkDesc : new IdUtils.DataIdDescriptor();
			this.dataDesc = (dataDesc != null) ? dataDesc : new IdUtils.DataIdDescriptor();;
		}

		public ObjectId chkAttrId() {
			return chkDesc.getId();
		}

		public ObjectId dataAttrId() {
			return dataDesc.getId();
		}

		/**
		 * � �������� destAttr ���� CardLink �������� ������ ��������, �������
		 * ��������� � �������� chkAttrId �� �� �������� ��� � ������� dataAttrId
		 * � �������� mainCard.
		 */
		@SuppressWarnings({ "synthetic-access", "unchecked" })
		@Override
		public void doFilter( Attribute destAttr, Card mainCard) throws DataException
		{
			if (destAttr == null)
				return;

			/* �������� ���� � ������ ������ destAttr ... */
			if ( !(destAttr instanceof CardLinkAttribute) ) {
				logger.error("Cardlink expected but attribute "+destAttr.getId() +" is of class "+ destAttr.getClass());
				throw new DataException("jbr.card.configfail");
			}

			final CardLinkAttribute link = (CardLinkAttribute) destAttr;
			if (link.getIdsLinked() == null || link.getIdsLinked().isEmpty()) {
				logger.debug("nothing to filter from empty attribute "+ destAttr.getId());
				return;
			}

			/* ��������� �������� ������ destAttr ... */
			final List<ObjectId> ids = new ArrayList<ObjectId>( link.getIdsLinked());
			boolean changed = false; // ��������� ��������� ������� ids
			Map<ObjectId, Card> cardsmap = null;
			{
				// ��������� ������ ����������� �������� �������� ...
				final List<Card> partialCards = searchCards(ids, new ObjectId[]{ this.chkAttrId() });
				cardsmap = ObjectIdUtils.collectionToObjectIdMap(partialCards);
			}

			/* �������� �������� ������ �� ����-��� �������� ��������
			 * chkAttrId �������� �������� dataAttr �� �������� mainCard ...
			 * */
			if (cardsmap == null || cardsmap.isEmpty()) {
				logger.warn( "Load zero cards by ids ["+ IdUtils.makeIdCodesEnum(ids, ",") +"] -> clearing all filtered data" );
				changed = true;
				ids.clear();
			} else {
				/* ���������������� �������� ����������� (��������) �������� ������ ... */
				for(Iterator<ObjectId> iter = ids.iterator(); iter.hasNext(); ) {
					final ObjectId cardId = iter.next();
					final Card c = cardsmap.get(cardId);
					if (!checkEqOrInside( c, mainCard)) {
						// �������� �� �������� -> ������� �� ������ ...
						changed = true;
						iter.remove();
					}
				}
			}

			if (changed)
				link.setIdsLinked(ids);
		}

		/**
		 * ��������� ������� �� ��������.
		 * @param chkcard ��������, ������� this.chkAttrId() ������� �����������.
		 * @param datacard ������� ��������, � ������� ������ ���� ������� this.dataAttrId
		 * @return true, ��� ��������� ��������� ��� ���������,
		 * ��� person- � cardlink-��������� ��� ��������� ������ id-�� ��
		 * �������� �������� chkcard � ������ �� �������� datacard.
		 * (!) ���� ��������� ������ ���������.
		 * BackLink �������� �� ��������������.
		 * @throws DataException
		 */
		@SuppressWarnings({ "synthetic-access", "unchecked" })
		protected boolean checkEqOrInside(Card chkcard, Card datacard)
				throws DataException
		{
			if (chkcard == null || datacard == null)
				return false;
			if (chkAttrId() == null || dataAttrId() == null)
				// ������ ���������
				return true;

			final Attribute chkAttr = chkcard.getAttributeById(chkAttrId());
			if (chkAttr == null) {
				logger.warn("no attribute "+ chkAttrId()+ " in checking card "+ chkcard.getId() + "-> filter throw out card "+ chkcard.getId());
				return false;
			}
			final Attribute dataAttr = datacard.getAttributeById(dataAttrId());
			if (dataAttr == null) {
				logger.warn("no attribute "+ dataAttrId()+ " in data card "+ datacard.getId() + "-> filter throw out card "+ chkcard.getId());
				return false;
			}

			if (!chkAttr.getClass().equals(dataAttr.getClass())) {
				final String errInfo = "Impossible to compare attributes of different classes: \n" +
						"\t checked id="+ chkAttrId() +", class "+ chkAttr.getClass() +
						"\n\t data id="+ dataAttrId() +", class "+ dataAttr.getClass();
				logger.error( errInfo);
				// throw new DataException("jbr.card.configfail");
				throw new DataException(errInfo);
			}

			boolean result = false;
			if (		chkAttr instanceof IntegerAttribute
					|| chkAttr instanceof DateAttribute
					|| chkAttr instanceof StringAttribute
					|| chkAttr instanceof ListAttribute
					|| chkAttr instanceof TreeAttribute
				)
			{
				result = ((IntegerAttribute) chkAttr).equalValue(dataAttr);
			} else if (chkAttr instanceof PersonAttribute || chkAttr instanceof CardLinkAttribute)
			{	// ��� ���� ���������� �� ���������, � ��������� chkDesc � dataDesc ...
				// result = ((PersonAttribute) chkDesc).equalValue(dataDesc);
				Set<ObjectId> chkSet = null, dataSet = null;
				if (chkAttr instanceof PersonAttribute) {
					chkSet = ObjectIdUtils.collectionToSetOfIds( ((PersonAttribute) chkAttr).getValues() );
					dataSet = ObjectIdUtils.collectionToSetOfIds( ((PersonAttribute) dataAttr).getValues() );
				} else {
					chkSet = ObjectIdUtils.collectionToSetOfIds( ((CardLinkAttribute) chkAttr).getIdsLinked());
					dataSet = ObjectIdUtils.collectionToSetOfIds( ((CardLinkAttribute) dataAttr).getIdsLinked() );
				}
				if (dataSet == null || dataSet.isEmpty()) {
					result = (chkSet == null || chkSet.isEmpty());
				} else
					// chkSet ��������� ������ � dataSet ...
					result = dataSet.containsAll(chkSet);
			} else {
				logger.error( "Unsupported attribute type while comparing attributes: \n" +
						"\t checked id="+ chkAttrId() +", class "+ chkAttr.getClass() +
						"\n\t data id="+ dataAttrId() +", class "+ dataAttr.getClass()
						);
				// throw new DataException("jbr.card.configfail");
				// IllegalArgumentException
				throw new DataException("Unsupported compare for attribute type: " + chkAttr.getType());
			}
			return result;
		}
	}

}
