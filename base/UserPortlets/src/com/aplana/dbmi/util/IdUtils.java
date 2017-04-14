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
package com.aplana.dbmi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * @author RAbdullin
 *	�������� ��� �������� ���������� ��������� � ����������� ������ - 
 * ������ �� ����� ��� ����� �� objectIds, ������ �� �������� �������� ��� 
 * �������� �� � ����, �� � ����������.
 */
public class IdUtils {

	final static String[] ATTR_TYPES = {
		AttrUtils.ATTR_TYPE_STRING,
		AttrUtils.ATTR_TYPE_TEXT,
		AttrUtils.ATTR_TYPE_INTEGER, 

		AttrUtils.ATTR_TYPE_DATE,
		AttrUtils.ATTR_TYPE_PERSON, 
		AttrUtils.ATTR_TYPE_LIST,

		AttrUtils.ATTR_TYPE_LINK,
		AttrUtils.ATTR_TYPE_TYPED_LINK,
		AttrUtils.ATTR_TYPE_BACKLINK,

		AttrUtils.ATTR_TYPE_TREE,
		
		AttrUtils.ATTR_TYPE_HTML,
		
		AttrUtils.ATTR_TYPE_PORTAL_USER_LOGIN,
		AttrUtils.ATTR_TYPE_USER_ROLES_AND_GROUPS
	};

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
	public static List<ObjectId> stringToAttrIds(Class attrType, String str) {
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

}
