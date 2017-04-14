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
package com.aplana.dbmi.model.util;

import java.util.Iterator;

import com.aplana.dbmi.model.*;

/**
 * Utility class for working with {@link Card card's} {@link Attribute attributes}
 * @author DSultanbekov
 */
public class AttrUtils {
	public static final String ATTR_TYPE_STRING = "string";
	public static final String ATTR_TYPE_TEXT = "text";
	public static final String ATTR_TYPE_INTEGER = "number";
	public static final String ATTR_TYPE_DATE = "date";
	public static final String ATTR_TYPE_LIST = "list";
	public static final String ATTR_TYPE_TREE = "tree";
	public static final String ATTR_TYPE_PERSON = "user";
	public static final String ATTR_TYPE_LINK = "link";
	public static final String ATTR_TYPE_TYPED_LINK = "typedLink";
	public static final String ATTR_DATED_TYPE_TYPED_LINK = "datedTypedLink";
	public static final String ATTR_TYPE_BACKLINK = "backLink";
	public static final String ATTR_TYPE_MATERIAL = "material";
	public static final String ATTR_TYPE_HTML = "html";
	public static final String ATTR_TYPE_CARDHISTORY = "cardHistory";
	public static final String ATTR_TYPE_PORTAL_USER_LOGIN = "userLogin";
	public static final String ATTR_TYPE_USER_ROLES_AND_GROUPS = "userRoles";
	
	/**
	 * This method is intended for using in configuration files reading/writing operations.
	 * Converts type of card attribute to short string alias.
	 * @param clazz type of attribute
	 * @return string alias of attribute type
	 * @see #getAttrClass(String)
	 */
	public static final String getAttrTypeString(Class clazz) {
		if (!Attribute.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException(clazz.getName() + " is not a " + Attribute.class.getName() + " descendant");
		} else if (StringAttribute.class.equals(clazz)) {
			return ATTR_TYPE_STRING;
		} else if (TextAttribute.class.equals(clazz)) {
			return ATTR_TYPE_TEXT;
		} else if (IntegerAttribute.class.equals(clazz)) {
			return ATTR_TYPE_INTEGER;
		} else if (DateAttribute.class.equals(clazz)) {
			return ATTR_TYPE_DATE;
		} else if (ListAttribute.class.equals(clazz)) {
			return ATTR_TYPE_LIST;
		} else if (TreeAttribute.class.equals(clazz)) {
			return ATTR_TYPE_TREE;
		} else if (PersonAttribute.class.equals(clazz)) {
			return ATTR_TYPE_PERSON;
		} else if (CardLinkAttribute.class.equals(clazz)) {
			return ATTR_TYPE_LINK;
		} else if (TypedCardLinkAttribute.class.equals(clazz)) {
			return ATTR_TYPE_TYPED_LINK;
		} else if (DatedTypedCardLinkAttribute.class.equals(clazz)) {
			return ATTR_DATED_TYPE_TYPED_LINK;
		} else if (BackLinkAttribute.class.equals(clazz)) {
			return ATTR_TYPE_BACKLINK;
		} else if (MaterialAttribute.class.equals(clazz)) {
			return ATTR_TYPE_MATERIAL;
		} else if (HtmlAttribute.class.equals(clazz)) {
			return ATTR_TYPE_HTML;
		} else if (CardHistoryAttribute.class.equals(clazz)) {
			return ATTR_TYPE_CARDHISTORY;
		} else if (PortalUserLoginAttribute.class.equals(clazz)) {
			return ATTR_TYPE_PORTAL_USER_LOGIN;
		} else if (UserRolesAndGroupsAttribute.class.equals(clazz)) {
			return ATTR_TYPE_USER_ROLES_AND_GROUPS;
		} else {
			throw new IllegalArgumentException("Unknown attribute type: " + clazz.getName());
		}
	}
	
	/**
	 * This method is intended for using in configuration files reading/writing operations.
	 * Convert short string alias of attribute type to corresponding java class. 
	 * @param attrType string attribute type alias
	 * @return type of {@link Attribute}
	 * @throws IllegalArgumentException if no match found for alias
	 * @see #getAttrTypeString(Class)
	 */
	public static Class<? extends Attribute> getAttrClass(final String attrType) {
		if (ATTR_TYPE_STRING.equals(attrType)) {
			return StringAttribute.class;
		} else if (ATTR_TYPE_TEXT.equals(attrType)) {
			return TextAttribute.class;
		} else if (ATTR_TYPE_INTEGER.equals(attrType)) {
			return IntegerAttribute.class;
		} else if (ATTR_TYPE_DATE.equals(attrType)) {
			return DateAttribute.class;
		} else if (ATTR_TYPE_LIST.equals(attrType)) {
			return ListAttribute.class;
		} else if (ATTR_TYPE_TREE.equals(attrType)) {
			return TreeAttribute.class;
		} else if (ATTR_TYPE_PERSON.equals(attrType)) {
			return PersonAttribute.class;
		} else if (ATTR_TYPE_LINK.equals(attrType)) {
			return CardLinkAttribute.class;
		} else if (ATTR_TYPE_TYPED_LINK.equals(attrType)){
			return TypedCardLinkAttribute.class;
		} else if (ATTR_DATED_TYPE_TYPED_LINK.equals(attrType)){
			return DatedTypedCardLinkAttribute.class;
		} else if (ATTR_TYPE_BACKLINK.equals(attrType)) {
			return BackLinkAttribute.class;
		} else if (ATTR_TYPE_MATERIAL.equals(attrType)) {
			return MaterialAttribute.class;
		} else if (ATTR_TYPE_HTML.equals(attrType)) {
			return HtmlAttribute.class;
		} else if (ATTR_TYPE_CARDHISTORY.equals(attrType)) {
			return CardHistoryAttribute.class;
		} else if (ATTR_TYPE_PORTAL_USER_LOGIN.equals(attrType)) {
			return PortalUserLoginAttribute.class;
		} else if (ATTR_TYPE_USER_ROLES_AND_GROUPS.equals(attrType)) {
			return UserRolesAndGroupsAttribute.class;
		} else {
			throw new IllegalArgumentException("Unknown alias for attribute type : " + attrType);
		}
	}

	/**
	 * 
	 * @param attrTypeChar {@link Attribute.TYPE_XXX}
	 * @return the attribute class corresponding to attrTypeChar or Attribute
	 * if type char is unknown.
	 */
	public static Class getAttrTypeClass(final Object attrTypeChar) {
		if (Attribute.TYPE_STRING.equals(attrTypeChar)) {
			return StringAttribute.class;
		} else if (Attribute.TYPE_TEXT.equals(attrTypeChar)) {
			return TextAttribute.class;
		} else if (Attribute.TYPE_INTEGER.equals(attrTypeChar)) {
			return IntegerAttribute.class;
		} else if (Attribute.TYPE_LONG.equals(attrTypeChar)) {
			return LongAttribute.class;
		} else if (Attribute.TYPE_DATE.equals(attrTypeChar)) {
			return DateAttribute.class;
		} else if (Attribute.TYPE_LIST.equals(attrTypeChar)) {
			return ListAttribute.class;
		} else if (Attribute.TYPE_TREE.equals(attrTypeChar)) {
			return TreeAttribute.class;
		} else if (Attribute.TYPE_PERSON.equals(attrTypeChar)) {
			return PersonAttribute.class;
		} else if (Attribute.TYPE_CARD_LINK.equals(attrTypeChar)) {
			return CardLinkAttribute.class;
		} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(attrTypeChar)){
			return TypedCardLinkAttribute.class;
		} else if (Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attrTypeChar)){
			return DatedTypedCardLinkAttribute.class;
		} else if (Attribute.TYPE_BACK_LINK.equals(attrTypeChar)) {
			return BackLinkAttribute.class;
		} else if (Attribute.TYPE_MATERIAL.equals(attrTypeChar)) {
			return MaterialAttribute.class;
		} else if (Attribute.TYPE_HTML.equals(attrTypeChar)) {
			return HtmlAttribute.class;
		} else if (Attribute.TYPE_CARD_HISTORY.equals(attrTypeChar)) {
			return CardHistoryAttribute.class;
		} else if (Attribute.TYPE_PORTAL_USER_LOGIN.equals(attrTypeChar)) {
			return PortalUserLoginAttribute.class;
		} else if (Attribute.TYPE_USER_ROLES_AND_GROUPS.equals(attrTypeChar)) {
			return UserRolesAndGroupsAttribute.class;
		} else {
			// throw new IllegalArgumentException("Unknown alias for attribute type : " + attrTypeChar);
		}
		return Attribute.class;
	}
	
	/**
	 * Gets attribute included in card by its code
	 * Main difference to {@link Card#getAttributeById(com.aplana.dbmi.model.ObjectId)} is that 
	 * this method doesn't require information about type of attribute
	 * @param id string code of card's attribute
	 * @return {@link Attribute} object or null if no such attribute exists
	 */	
	public static Attribute getAttributeByCode(String attributeCode, Card card) {
		if (attributeCode == null)
			throw new IllegalArgumentException("Attribute code can't be null");
		if (card.getAttributes() == null)
			return null;
		for(Iterator itr = card.getAttributes().iterator(); itr.hasNext(); ) {
			DataObject block = (DataObject) itr.next();
			if ((block instanceof Attribute) && attributeCode.equals(block.getId().getId()))
				return (Attribute) block;
			if (block instanceof AttributeBlock) {
				AttributeBlock attrBlock = (AttributeBlock)block;
				if (attrBlock.getAttributes() != null) {
					Iterator i = attrBlock.getAttributes().iterator();
					while (i.hasNext()) {
						Attribute attr = (Attribute) i.next();
						if (attributeCode.equals(attr.getId().getId()))
							return attr;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Takes string representing information about attribute code and its type
	 * and converts it to ObjectId.
	 * Identifier must be presented in form of &lt;type&gt;:&lt;id&lt;,
	 * where 'type' is a string alias of attribute type (see {@link AttrUtils#getAttrTypeString(Class)})
	 * and 'id' could be an actual attribute code, or one of predefined aliases (see {@link ObjectId#predefined})
	 * for example: 'string:NAME', 'link:jbr.incoming.recipient', etc.<br/>
	 * WARNING: this method removes any whitespaces, so 'string:NAME' and 'string: NAME' will
	 * produce same results. 
	 * 'string1:NAME' will produce IllegalArgumentException as there is no such type alias as 'string1'.
	 * @param st string representing attribute type and code 
	 * @return identifier of attribute
	 * @throws IllegalArgumentException if some kind of parse error occurs
	 */
	public static ObjectId getAttributeId(String st) {
		if (st == null) {
			throw new IllegalArgumentException("Nulls are not allowed here");
		}
		int index = st.indexOf(':');
		if (index < 0) {
			throw new IllegalArgumentException("Couldn't parse string: '" + st + "'");
		}
		String type = st.substring(0, index).trim(),
			id = st.substring(index + 1).trim();
		
		return ObjectIdUtils.getObjectId(getAttrClass(type), id, false);
	}
	
	
	/**
	 * ������ ������� �� attrId � ���� �������� 
	 * @param attrId - id ��������
	 * @param type - ��� ��������
	 * @return
	 */
	public static Attribute createAttribute(String attrId, Class type){
		return createAttribute(new ObjectId(type, attrId));
	}
	
	/**
	 * ������ ����� ������� �� ObjectId
	 * @param attrId
	 * @return
	 */
	public static Attribute createAttribute(ObjectId attrId){
		Attribute attribute = null;
		if(equalsAttrClass(attrId, PersonAttribute.class)){
			PersonAttribute personAttribute = new PersonAttribute();
			personAttribute.setId(attrId);
			attribute = personAttribute;
		}else if(equalsAttrClass(attrId, TextAttribute.class)){
			TextAttribute textAttribute = new TextAttribute();
			textAttribute.setId(attrId);			
			attribute = textAttribute;
		}else if(equalsAttrClass(attrId, StringAttribute.class)){
			StringAttribute stringAttribute = new StringAttribute();
			stringAttribute.setId(attrId);
			attribute = stringAttribute;
		}else if(equalsAttrClass(attrId, CardLinkAttribute.class)){
			CardLinkAttribute cardLinkAttribute = new CardLinkAttribute();
			cardLinkAttribute.setId(attrId);
		} 
		//TODO �������� ������ ���� ��������� �� �������������
		return attribute;
	}
	
	/**
	 * ���������� ����� �������� � ����� ��������
	 * @param attrId
	 * @param type
	 * @return
	 */
	public static boolean equalsAttrClass(ObjectId attrId, Class type){
		return attrId.getType().equals(type);
	}
}
