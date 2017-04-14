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
/**
 * 
 */
package com.aplana.dbmi.jbr.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * @author RAbdullin
 *	�������� ��� �������� ���������� ��������� � ����������� ������ - 
 * ������ �� ����� ��� ����� �� objectIds, ������ �� �������� �������� ��� 
 * �������� �� � ����, �� � ����������.
 */
public class IdUtils {

	final static Log logger = LogFactory.getLog(IdUtils.class);

	final static String[] ATTR_TYPES = {
		AttrUtils.ATTR_TYPE_STRING,
		AttrUtils.ATTR_TYPE_TEXT,
		AttrUtils.ATTR_TYPE_INTEGER, 

		AttrUtils.ATTR_TYPE_DATE,
		AttrUtils.ATTR_TYPE_PERSON, 
		AttrUtils.ATTR_TYPE_LIST,

		AttrUtils.ATTR_TYPE_LINK,
		AttrUtils.ATTR_TYPE_TYPED_LINK,
		AttrUtils.ATTR_DATED_TYPE_TYPED_LINK,
		AttrUtils.ATTR_TYPE_BACKLINK,

		AttrUtils.ATTR_TYPE_TREE,
		
		AttrUtils.ATTR_TYPE_HTML
	};

	// static class only
	private IdUtils() {}

	/**
	 * @param attrKeyOrCode: ������� ������� ���������������� id �� ���������� 
	 * �� ����� .\{Portal}\conf\dbmi\objectids.properties, ������ ��������� ���� 
	 * ���������, �� ������� ���������� (@SEE ATTR_TYPES).
	 * @param defaultAttrClass: ��� �������� ��� ������, ���� �� ����� ������ 
	 * ����������������� ����� � objectIds (@SEE AttrUtils.ATTR_XXX).
	 * @param defaultIsNumeric: ������������ ������ � defaultAttrClass.
	 * @return ������������������ ������ ����� �������.
	 */
	public static ObjectId tryFindPredefinedObjectId(String attrKeyOrCode,
			Class<?>/*<Attribute>*/ defaultAttrClass, boolean defaultIsNumeric) {
		if (attrKeyOrCode == null || attrKeyOrCode.length() < 1)
			return null;
		ObjectId result = null;
		for(int i = 0; i < ATTR_TYPES.length; i++) {
			result = ObjectId.predefined( 
					AttrUtils.getAttrClass(ATTR_TYPES [i]), attrKeyOrCode);
			if (result != null) // FOUND predefined 
				return result;
		}

		result = ObjectId.predefined( Template.class, attrKeyOrCode);
		if (result != null) return result;

		result = ObjectId.predefined( CardState.class, attrKeyOrCode);
		if (result != null) return result;

		// if not found any predefined - create string's typed one
		return (defaultAttrClass == null) 
				? null
				: ObjectIdUtils.getObjectId( defaultAttrClass, attrKeyOrCode, defaultIsNumeric);
	}

	public static ObjectId tryFindPredefinedObjectId(String attrKeyOrCode,
			Class<?>/*<Attribute>*/ defaultAttrClass) {
		return tryFindPredefinedObjectId(attrKeyOrCode, defaultAttrClass, false);
	}


	public static ObjectId tryFindPredefinedObjectId(String attrKeyOrCode,
			String defaultTypeTag) {
		return tryFindPredefinedObjectId(attrKeyOrCode, AttrUtils.getAttrClass(defaultTypeTag));
	}

	/**
	 * @param attrKeyOrCode: ������� ������� ���������������� id �� ���������� 
	 * �� ����� .\{Portal}\conf\dbmi\objectids.properties, ������ ��������� ���� 
	 * ���������, �� ������� ���������� (@SEE ATTR_TYPES).
	 * @return ������������������ ������ ����� ������� (���� ��� �����������������
	 * �������� � ����� ��������� (AttrKeyOrCode), �� ����������� ��� String).
	 */
	public static ObjectId tryFindPredefinedObjectId(String attrKeyOrCode) {
		return tryFindPredefinedObjectId(attrKeyOrCode, StringAttribute.class);
	}


	/**
	 * ������� �������������� �������� �� �����������:
	 * 	(�) ���� ����� ��� (�� ���������) - �� ������ � ���� �����;
	 * 	(�) ���� �� ����� - �� ��������� ������ ���� �� objectids (�� ������ ATTR_TYPES);
	 * 	(�) ���� ��� �� ������ �������� �� objectsIds, ��: 
	 * 		���� defaultAttrClass=null, �� ������������ null,
	 * 		����� ��������� id � ����� attrCodeOrObjpropId � ����� defaultAttrClass. 
	 * @param attrCodeOrObjpropId: ������ ����: 
	 * 		"���: ����_objectIds_���_���",
	 * 	��� ��� ����:
	 * 		"����_objectIds_���_���".
	 * @param defaultAttrClass: ������������, ���� �������� ������� (�).
	 * @param isNumeric: �������� ������������� (������������ ���� ����� ���).
	 * @return ������������� ��������.
	 */
	public static ObjectId smartMakeAttrId(String attrCodeOrObjPropId, 
			Class<?> defaultAttrClass, boolean isNumeric) 
	{
		if (attrCodeOrObjPropId == null || attrCodeOrObjPropId.length() < 1)
			return null;

		if (attrCodeOrObjPropId.indexOf(':') >=0 ) // ��� ���� ������: "��������:���"
			return AttrUtils.getAttributeId(attrCodeOrObjPropId);

		// ������� ������ �������� �����, �� ������ ���� value �������� � ����� 
		// �� ������� (���������) �� objectids.properties...
		return tryFindPredefinedObjectId(attrCodeOrObjPropId, defaultAttrClass, isNumeric);
	}

	/**
	 * ������� ���������� �������������.
	 * @param attrCodeOrObjPropId
	 * @param defaultAttrClass (������������, ���� ��������� ������� ����� �����, � �� �������).
	 * @return
	 */
	public static ObjectId smartMakeAttrId(String attrCodeOrObjPropId, 
				Class<?> defaultAttrClass)
	{
		return smartMakeAttrId(attrCodeOrObjPropId, defaultAttrClass, false);
	}


	/**
	 * ������� id �������� �� ���� � �������� ��� ����.
	 * @param code	��� ��������
	 * @param type	���� ��������, ���� Null, �� ����� ����������� � ��������
	 * ���� defaultType. 
	 * @param defaultType ��� ��-��������, ������������ ���� ���� �� ������ type.
	 * @return AttributeId
	 */
	public static ObjectId makeAttrId( final String code, final String type, 
			final Class<? extends Attribute> defaultType) 
	{
		ObjectId result = null;

		if (code != null && code.length() > 0) 
		{
			if (type == null) {
				// �� ����� ��� ...
				result = smartMakeAttrId(code, defaultType);
			} else {
				final Class<? extends Attribute> t = AttrUtils.getAttrClass(type);
				try {
					result = ObjectIdUtils.getObjectId( t, code, false);
				} catch (IllegalArgumentException e) {}
			}
		}
		if (result == null)
			logger.warn("Could not create attribute ObjectId for code/alias= "+ 
					code+ ", type="+ type+", numeric=false, defaultType="+ defaultType);

		return result;
	}



	/**
	 * �������� ������ ObjectId �� ������ �� ������� ���������� ���������, 
	 * ������������� ����� ������� ��� ����� � �������,
	 * ������ ��������� ���������� ��. {@link smartMakeAttrId}. 
	 * @param idList
	 * @param defaultAttrClass (������������, ���� ��������� ������� ����� �����, � �� �������).
	 * @param isNumeric: �������� �������������� (������������ ��������� � defaultAttrClass).
	 * @param addNulls: true, ���� ���� ��������� ���������� ������ id.
	 * @return ������ ��������� id, null-�������� ��������� � ������ ���� 
	 * ��������� addNulls==true.
	 */
	public static List<ObjectId> stringToAttrIds( String idList, 
				Class<?> defaultAttrType,
				boolean isNumeric,
				String delimiters,
				boolean addNulls) 
	{
		if (idList == null) return null;
		if (delimiters == null || delimiters.length()<1)
			delimiters = ",";
		final String[] ids = idList.split("\\s*["+ delimiters+ "]\\s*");
		final List<ObjectId> result = new ArrayList<ObjectId>(ids.length);
		for (int i = 0; i < ids.length; ++i)
		{
			final String sId = ids[i].trim();
			final ObjectId id = ("".equals(sId))
						? null
						: smartMakeAttrId(sId, defaultAttrType, isNumeric);
			if (addNulls || id != null)
				result.add(id);
		}
		return result;
	}

	public static List<ObjectId> stringToAttrIds( String idList, 
				Class<?> defaultAttrType,
				boolean isNumeric,
				boolean addNulls) 
	{
		return stringToAttrIds( idList, defaultAttrType, isNumeric, ",;", addNulls);
	}

	/**
	 * ������� 2 {@see com.aplana.dbmi.jbr.processors.AbstractCopyPersonProcessor}
	 */
	public static List<ObjectId> stringToAttrIds(Class<?> attrType, String str) {
		return stringToAttrIds( str, attrType, false, ",", false);
	}

	/**
	 * �������� ������ ObjectId �� ������ �� ������� ���������� ��������� 
	 * (���������� ����� ��� �������), ������������� ����� ������� ��� ����� 
	 * � �������, ������ ��������� ���������� ��. {@link smartMakeAttrId}.
	 */ 
	public static List<ObjectId> stringToAttrIds( String idList, Class<?> defaultAttrType)
	{
		return stringToAttrIds(idList, defaultAttrType, false, false); 
	}

	/**
	 * �������� ���� ��������� � �������� ����� �����������. ������ ������������
	 * ��� ������� ������ ����� � ����� SQL.
	 * @param ids ������ id-������ ��� DataObject.
	 * @param delimiter ����������� ��������� � ������.
	 * @param quoteOpen ����������� �������.
	 * @param quoteClose ����������� �������.
	 * @return
	 */
	public static String makeIdCodesQuotedEnum( final Collection<?> ids, 
			final String delimiter, String quoteOpen, String quoteClose)
	{
		if (ids == null || ids.isEmpty())
			return null;
		if (quoteOpen == null) quoteOpen = "";
		if (quoteClose == null) quoteClose = "";
		final StringBuffer result = new StringBuffer(ids.size());
		for( Iterator<?> iter = ids.iterator(); iter.hasNext(); ) 
		{
			final ObjectId id = ObjectIdUtils.getIdFrom(iter.next());
			final String strItem = (id == null) ? "\'\'" : id.getId().toString();
			result.append(quoteOpen).append(strItem).append(quoteClose);
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}

	/**
	 * �������� id ������ ����� �����������. ������ ������������
	 * ��� ������� ������ ������ � ����� SQL.
	 * @param ids ������ Person.
	 * @param delimiter ����������� ��������� � ������.
	 * @return
	 */
	public static String makeIdPersonList( final Collection<?> persons, 
			final String delimiter)
	{
		if (persons == null || persons.isEmpty())
			return null;
		final StringBuffer result = new StringBuffer(persons.size());
		for( Iterator<?> iter = persons.iterator(); iter.hasNext(); ) 
		{
			final Person id = (Person)iter.next();
			final String strItem = (id == null) ? "-1" : id.getId().getId().toString();
			result.append(strItem);
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}
	
	/**
	 * �������� id �������� ����� �����������. ������ ������������
	 * ��� ������� ������ �������� � ����� SQL.
	 * @param ids ������ Person.
	 * @param delimiter ����������� ��������� � ������.
	 * @return
	 */
	public static String makeObjectIdCardList( final Collection<?> cardIds, 
			final String delimiter)
	{
		if (cardIds == null || cardIds.isEmpty())
			return null;
		final StringBuffer result = new StringBuffer(cardIds.size());
		for( Iterator<?> iter = cardIds.iterator(); iter.hasNext(); ) 
		{
			final ObjectId id = (ObjectId)iter.next();
			final String strItem = (id == null) ? "-1" : id.getId().toString();
			result.append(strItem);
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}
	
	/**
	 * �������� id �� ObjectId ����� �����������.
	 * @param ids ������ ObjectId.
	 * @param delimiter ����������� ��������� � ������.
	 * @return
	 */
	public static String makeObjectIdStringLine(final Collection<?> cardIds, 
			final String delimiter)
	{
		if (cardIds == null || cardIds.isEmpty())
			return null;
		final StringBuilder result = new StringBuilder(cardIds.size());
		for(Iterator<?> iter = cardIds.iterator(); iter.hasNext();) 
		{
			final ObjectId id = (ObjectId)iter.next();
			if(id != null && id.getId() != null) {
				result.append(id.getId().toString());
			}
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.length() > 0 ? result.toString() : null;
	}
	
	/**
	 * �������� ������ ������� Long-�������� ����� �����������.
	 * @param ids ������ ObjectId.
	 * @param delimiter ����������� ��������� � ������.
	 * @return
	 */
	public static String makeLongStringLine(final Collection<?> ids, 
			final String delimiter)
	{
		if (ids == null || ids.isEmpty())
			return null;
		final StringBuilder result = new StringBuilder(ids.size());
		for(Iterator<?> iter = ids.iterator(); iter.hasNext();) 
		{
			final Long id = (Long)iter.next();
			result.append(id.toString());
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.length() > 0 ? result.toString() : null;
	}
	
	/**
	 * �������� id �� ObjectId ����� �����������.
	 * @param ids ������ ObjectId.
	 * @return
	 */
	public static String makeObjectIdStringLine(final Collection<?> cardIds)
	{
		return makeObjectIdStringLine(cardIds, ",");
	}
	
	/**
	 * �������� ���� ��������� � �������� ����� �����������. ������ ������������
	 * ��� ������� ������ ����� � ����� SQL.
	 * @param ids ������ id-������ ��� DataObject.
	 * @param delimiter ����������� ��������� � ������.
	 * @param quote ������������-������� ��������� ���������.
	 * @return
	 */
	public static String makeIdCodesQuotedEnum( final Collection<?> ids, 
			final String delimiter, final String quote)
	{
		return makeIdCodesQuotedEnum( ids, delimiter, quote, quote);
	}

	/**
	 * ������� ������ � ������������ ��� �������������-�������.
	 * @param ids ������ id-������ ��� DataObject.
	 * @param delimiter ����������� ��������� � ������.
	 * @return
	 */
	public static String makeIdCodesEnum(final Collection<?> ids, String delimiter)
	{
		return makeIdCodesQuotedEnum( ids, delimiter, null);
	}

	/**
	 * ������� ������ � ��������� �������� � ������������ �������.
	 * @param ids ������ id-������ ��� DataObject.
	 * @return
	 */
	public static String makeIdCodesQuotedEnum(final Collection<?> ids)
	{
		return makeIdCodesQuotedEnum( ids, ", ", "'");
	}

	/**
	 * �������� ���� ��������� � �������� ����� �����������. ������ ������������
	 * ��� ������� ������ ����� � ����� SQL.
	 * @param ids ������ id-������ ��� DataObject.
	 * @param delimiter ����������� ��������� � ������.
	 * @param quoteOpen ����������� �������.
	 * @param quoteClose ����������� �������.
	 * @return
	 */
	public static String makeCardIdEnum( final Collection<?> ids, 
			final String delimiter)
	{
		if (ids == null || ids.isEmpty())
			return null;
		final StringBuffer result = new StringBuffer(ids.size());
		for( Iterator<?> iter = ids.iterator(); iter.hasNext(); ) 
		{
			final Card card = (Card)(iter.next());
			final String strItem = (card == null) ? "0" : card.getId().getId().toString();
			result.append(strItem);
			if (delimiter != null && iter.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}
	
	public static ObjectId makeStateId(String value)
	{
		if (value == null || value.trim().length() < 1) return null;
		ObjectId result = ObjectId.predefined(CardState.class, value);
		if (result == null)
			try {
				result = new ObjectId(CardState.class, Long.parseLong(value));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(value +
						" is neither predefined nor physical card state id");
			}
		return result;
	}

	public static Set<ObjectId> makeStateIdsList(String value)
	{
		final Set<ObjectId> result = new HashSet<ObjectId>();
		if (value != null) {
			final String[] states = value.split("\\s*[;,]\\s*");
			for (int i = 0; i < states.length; i++) {
				if (states[i].trim().length() < 1) continue;
				final ObjectId stateId = makeStateId(states[i]);
				if (stateId != null)
					result.add(stateId);
			} // for i
		}
		return result;
	}
	
	public static ObjectId makeCardId(Long id) {
		return new ObjectId(Card.class, id);
	}

	public static ObjectId getWfmId (String wfmAlias){
		return ObjectId.predefined(WorkflowMove.class, wfmAlias);
	}

	/**
	 * ���� ���������� ��������-�������.
	 * @author rabdullin
	 */
	public static class IdPair 
	{
		public final IdUtils.DataIdDescriptor source;
		public final IdUtils.DataIdDescriptor dest;

		public IdPair() { 
			source = new IdUtils.DataIdDescriptor();
			dest = new IdUtils.DataIdDescriptor();
		}

		public IdPair(IdUtils.DataIdDescriptor src, IdUtils.DataIdDescriptor dst) {
			super();
			this.source = (src != null) ? src : new IdUtils.DataIdDescriptor();
			this.dest = (dst != null) ? dst : new IdUtils.DataIdDescriptor();
		}

		public ObjectId sourceId() {
			return source.getId();
		}

		public ObjectId destId() {
			return dest.getId();
		}

		public String sourceFilterName() {
			return source.getFilterName();
		}

		public int sourceCardIndex() {
			return source.getCardIndex();
		}

		public void setSourceCardIndex(int index) {
			source.setCardIndex( index);
		}

		public static final String RULE_PARTS_SEPARATOR = "->";

		/**
		 * ��������� ������ ��������� ����:
		 * 		"source -> dest"
		 * ��� source � dest � ����� ����� ����� ���:
		 * 		{#int@} {type:} codeOrAlias {&filterXXX}
		 * ��� {...} ���� �������,
		 * 		������� #, @, :, & ���� �������������, 
		 * 		int					����� �����,
		 * 		type				��������� �������� ����,
		 * 		codeOrAlias		��� �������� ��� ����� ��������.
		 * @param ruleStr
		 * @return ��������������� �������
		 */
		@SuppressWarnings("unchecked")
		public static IdPair parseRule(String ruleStr) {
			/*  ���� � ������� �� ������ ������ �������� ������ ����: 
				<%REG_INDEX_SEPARATOR%>�����<%REG_NUMBER_SYMBOL%>
				, �� ����� �������� � ���� IdPair.sourceCardIndex 
			*/
			if (ruleStr == null)
				return null;

			Class<? extends Attribute> type = CardLinkAttribute.class;	// default attribute type

			// ������� ����� ����: ���������-�������� -> �������
			final String[] parts = ruleStr.split(RULE_PARTS_SEPARATOR);
			final DataIdDescriptor srcDesc = DataIdDescriptor.parseObjectId(parts[0], type); 

			if (srcDesc.getId() != null)
				type = srcDesc.getId().getType(); 	// set default equals to source attribute

			final String destPart = (parts.length > 1) ? parts[1] : parts[0];
			final DataIdDescriptor dstDesc = DataIdDescriptor.parseObjectId( destPart, type);

			final IdPair pair = new IdPair( srcDesc, dstDesc);

			/*
			String[] rules = rule.split(REG_INDEX_SEPARATOR);
			rule = rule.substring(rule.indexOf(REG_INDEX_SEPARATOR));
			int sourceCardIndex = -1;	
			try{
				if (rules.length>1&&rules[0].substring(0, 1).equals(REG_NUMBER_SYMBOL)){
					sourceCardIndex = Integer.parseInt(rules[0].substring(1, 0));
				}
			} catch (Exception e){
				sourceCardIndex = -1;
			}
			*/
	/*
			int sourceCardIndex = -1;	
			String sCardIndex = getStartIndex(rule);
			if (sCardIndex!=null&&sCardIndex.length()>0){
				rule = clearStartIndex(rule, sCardIndex);
				try{
					sourceCardIndex = Integer.parseInt(sCardIndex);
				} catch (Exception e){
					sourceCardIndex = -1;
				}
			}
			final Matcher m = REG_FORMAT.matcher(rule);
			if (!m.matches())
				throw new IllegalArgumentException("Invalid rule format for string " + rule);

			final IdPair pair = new IdPair();

			pair.source.setCardIndex( sourceCardIndex);

			Class<? extends Attribute> type = CardLinkAttribute.class;	// default attribute type
			pair.source.setId( makeAttrId( m.group(GROUP_SRC_ID), m.group(GROUP_SRC_TYPE), type));
			if (pair.sourceId() != null) // set default equals to source attribute
				type = pair.sourceId().getType();

			final String destCodeOrAlias = m.group(GROUP_DEST_ID);
			pair.dest = (destCodeOrAlias == null)
						? pair.source
						: makeAttrId( destCodeOrAlias, m.group(GROUP_DEST_TYPE), type);
	*/
			return pair;
		}

		@Override
		public String toString() {
			return MessageFormat.format("(source={0}, dest={1})",this.source, this.dest);
		}

	}

	/**
	 * ��������� ������ �� ������ � id, ��������, �� �������.
	 * @author rabdullin
	 */
	public static class DataIdDescriptor 
	{
		private int cardIndex = -1;
		private ObjectId id;

		// �������� ������� �� ����������� �������� ������
		private String filterName;

		public DataIdDescriptor() {
		}
	
		public ObjectId getId() {
			return this.id;
		}
		public void setId(ObjectId ID) {
			this.id = ID;
		}
	
		public int getCardIndex() {
			return this.cardIndex;
		}
		public void setCardIndex(int index) {
			this.cardIndex = index;
		}
	
		public String getFilterName() {
			return this.filterName;
		}
		public void setFilterName(String name) {
			this.filterName = name;
		}

		/* 
		 * ��������� ������ ����: "{#�����@} {���:} �������� {!������}"
		 * (��� "{#int@} aaa : bbb {!ccc}")
		 *
		 * ������ �������:
		 *			"\\s*+(#\\s*([0-9]+)\\s*@)?\\s*+(([^:&\\s]*+)\\s*+:)?\\s*+([^:&\\s]*+)\\s*+(!\\s*+(.*))?"
		 *			"(\\s*+#\\s*([0-9]+)\\s*@)?\\s*+(([^:!\\s]*+)\\s*+:)?\\s*+([^:!\\s]*+)\\s*+(!\\s*+(.*))?"
		 *
		 * ����� ������ � ��������� [0..7],
		 * ������: "#01@ link: jbr.resolution.FioSign  ! filter xyz  " 
		 * 	 matches is true, has 7 matching groups:
		 *      [0] 	'#01@ link : jbr.resolution.FioSign  ! filter xyz  '
		 *      [1] 	'#01@'
		 *      [*2] 	'01'
		 *      [3] 	'link :'
		 *      [*4] 	'link'
		 *      [*5] 	'jbr.resolution.FioSign'
		 *      [6] 	'! filter xyz  '
		 *      [*7] 	''filter xyz  '
		 */
		static final Pattern REG_ID = Pattern.compile(
				//    source attribute                                destination attribute
				//      [#      int            @]        [type                  :]          id                   [&filter_name]
				"(\\s*+#\\s*([0-9]+)\\s*@)?\\s*+(([^:!\\s]*+)\\s*+:)?\\s*+([^:!\\s]*+)\\s*+(!\\s*+(.*))?"
			);
		static final int GROUP_REG_ID_NUM = 2;
		static final int GROUP_REG_ID_TYPE = 4;
		static final int GROUP_REG_ID_ALIAS = 5;
		static final int GROUP_REG_ID_FILTER = 7;

		public static DataIdDescriptor parseObjectId(String configStr, 
				final Class<? extends Attribute> defaultType) 
		{
			final Matcher m = REG_ID.matcher(configStr);
			if (!m.matches())
				throw new IllegalArgumentException("Invalid objectId format at string '" + configStr+ "'");
			final String number = m.group(GROUP_REG_ID_NUM);
			final String type = m.group(GROUP_REG_ID_TYPE);
			final String alias = m.group(GROUP_REG_ID_ALIAS);
			final String filter = m.group(GROUP_REG_ID_FILTER);

			final IdUtils.DataIdDescriptor result = new IdUtils.DataIdDescriptor();
			if (number != null)
				result.setCardIndex( Integer.parseInt(number));
			result.setId( makeAttrId(alias, type, defaultType));
			result.setFilterName( (filter == null) ? filter : filter.trim());
			return result;
		}

		@Override
		public String toString() {
			return MessageFormat.format("(id={0}, index={1}, filter={2})", id, cardIndex, filterName);
		}

	}

}
