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
package com.aplana.dbmi.card.doclinked;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * @author RAbdullin
 */
public class DoclinkUtils {

	final static String[] ATTR_TYPES = {
		AttrUtils.ATTR_TYPE_BACKLINK,
		AttrUtils.ATTR_TYPE_LINK,
		AttrUtils.ATTR_TYPE_TYPED_LINK,

		AttrUtils.ATTR_TYPE_TEXT,
		AttrUtils.ATTR_TYPE_INTEGER, 
		AttrUtils.ATTR_TYPE_DATE,

		AttrUtils.ATTR_TYPE_PERSON, 
		AttrUtils.ATTR_TYPE_STRING,
		AttrUtils.ATTR_TYPE_LIST,

		AttrUtils.ATTR_TYPE_TREE 
	};

	/**
	 * @param AttrKeyOrCode: ������� ������� ���������������� id �� �����
	 * (Portal).\conf\dbmi\objectids.properties ������ ��������� ���� ���������,
	 * �� ������� ����������.
	 * @param defaultClass: ����� ��� ��������, ������������, ���� ������ 
	 * ����������������� �� ���� �������.
	 * @param defaultIsNumeric: �������� �� ��� �������� ������, ������������ 
	 * ���������� defaultClass. 
	 * @return ������������������ ������ ����� ������� (���� ��� �����������������
	 * �������� � ����� ��������� (AttrKeyOrCode), �� ����������� ��� CardLink).
	 */
	public static ObjectId tryFindPredefinedObjectId(String AttrKeyOrCode,
			Class<?> defaultClass, boolean defaultIsNumeric) {
		if (AttrKeyOrCode == null)
			return null;
		for(int i = 0; i < ATTR_TYPES.length; i++) {
			final ObjectId result = ObjectId.predefined( 
					AttrUtils.getAttrClass(ATTR_TYPES[i]), AttrKeyOrCode);
			if (result != null) // FOUND predefined 
				return result;
		}
		// if not found any predefined - create string's typed one
		return ObjectIdUtils.getObjectId( 
				(defaultClass != null) ? defaultClass : StringAttribute.class,
				AttrKeyOrCode, 
				defaultIsNumeric);
	}

	public static ObjectId tryFindPredefinedObjectId(String AttrKeyOrCode) {
		return tryFindPredefinedObjectId( AttrKeyOrCode, 
						AttrUtils.getAttrClass(AttrUtils.ATTR_TYPE_LINK), 
						false);
	}

	private DoclinkUtils() {
	}
}