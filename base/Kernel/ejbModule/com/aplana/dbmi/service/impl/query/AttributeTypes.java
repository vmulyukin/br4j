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

/**
 * Utility class used to work with attributes
 */
public class AttributeTypes
{
	/**
	 * Represents {@link DateAttribute}
	 * @see Attribute#TYPE_DATE
	 */
	public static final String DATE = "D";
	/**
	 * Represents {@link IntegerAttribute}
	 * @see Attribute#TYPE_INTEGER
	 */
	public static final String INTEGER = "I";
    /**
	 * Represents {@link LongAttribute}
	 * @see Attribute#TYPE_LONG
	 */
	public static final String LONG = "N";
	/**
	 * Represents {@link ListAttribute}
	 * @see Attribute#TYPE_LIST
	 */
	public static final String LIST = "L";
	/**
	 * Represents {@link PersonAttribute}
	 * @see Attribute#TYPE_PERSON
	 */
	public static final String PERSON = "U";
	/**
	 * Represents {@link StringAttribute}
	 * @see Attribute#TYPE_STRING
	 */
	public static final String STRING = "S";
	/**
	 * Represents {@link TextAttribute}
	 * @see Attribute#TYPE_TEXT
	 */
	public static final String TEXT = "T";
	/**
	 * Represents {@link TreeAttribute}
	 * @see Attribute#TYPE_TREE
	 */
	public static final String TREE = "H";
	/**
	 * Represents {@link SecurityAttribute}
	 * @see Attribute#TYPE_SECURITY
	 */
	public static final String SECURITY = "A";
	/**
	 * Represents {@link CardLinkAttribute}
	 * @see Attribute#TYPE_CARD_LINK 
	 */
	public static final String CARD_LINK = "C";
	/**
	 * Represents {@link HtmlAttribute}
	 * @see Attribute#TYPE_HTML
	 */
	public static final String HTML = "W";
	/**
	 * Represents {@link MaterialAttribute}
	 * @see Attribute#TYPE_MATERIAL
	 */
	public static final String MATERIAL = "M";
	/**
	 * Represents {@link BackLinkAttribute}
	 * @see Attribute#TYPE_BACK_LINK
	 */
	public static final String BACK_LINK = "B";
	/**
	 * Represent {@link TypedCardLink}
	 * @see Attribute#TYPE_CARD_LINK
	 */
	public static final String TYPED_CLINK = "E";
	
	/**
	 * Represent {@link DatedTypedCardLink}
	 * @see Attribute#DATE_TYPE_CARD_LINK
	 */
	public static final String DATED_TYPED_CLINK = "F";

	/**
	 * Represent {@link CardHistoryAttribute}
	 * @see Attribute#CARD_HISTORY
	 */
	public static final String CARD_HISTORY = "Y";
	
	/**
	 * Represent {@link PortalUserLoginAttribute}
	 * @see Attribute#TYPE_USER_LOGIN
	 */
	public static final String TYPE_PORTAL_USER_LOGIN = "Z";
	
	public static final String TYPE_PORTAL_USER_ROLES = "V";
	/**
	 * Returns Class instance representing {@link Attribute} descendant
	 * which is associated with given string type code.
	 * @param type attribute type code as it is sored in DATA_TYPE column of ATTRIBUTE_TABLE.
	 * @return Class instance representing {@link Attribute} descendant
	 * which is associated with given string type code.
	 */
	public static Class getAttributeClass(String type) {
		if (STRING.equals(type))
			return StringAttribute.class;
		if (TEXT.equals(type))
			return TextAttribute.class;
		if (INTEGER.equals(type))
			return IntegerAttribute.class;
        if (LONG.equals(type))
			return LongAttribute.class;
		if (DATE.equals(type))
			return DateAttribute.class;
		if (LIST.equals(type))
			return ListAttribute.class;
		if (TREE.equals(type))
			return TreeAttribute.class;
		if (PERSON.equals(type))
			return PersonAttribute.class;
		if (SECURITY.equals(type))
			return SecurityAttribute.class;
		if (CARD_LINK.equals(type))
			return CardLinkAttribute.class;
		if (HTML.equals(type))
			return HtmlAttribute.class;
		if (MATERIAL.equals(type))
			return MaterialAttribute.class;
		if (BACK_LINK.equals(type))
			return BackLinkAttribute.class;
		if (TYPED_CLINK.equals(type))
			return TypedCardLinkAttribute.class;
		if (DATED_TYPED_CLINK.equals(type))
			return DatedTypedCardLinkAttribute.class;
		if (CARD_HISTORY.equals(type))
			return CardHistoryAttribute.class;
		if (TYPE_PORTAL_USER_LOGIN.equals(type))
			return PortalUserLoginAttribute.class;
		if (TYPE_PORTAL_USER_ROLES.equals(type))
			return UserRolesAndGroupsAttribute.class;
		throw new IllegalArgumentException("Unknown attribute type: " + type);
	}
	
	/**
	 * Creates new {@link ObjectId} identifier of {@link Attribute} descendant from
	 * its code and string constant defining type in database.
	 * @param type string code of attribute type (see {@link #STRING}, {@link #INTEGER}, etc.).
	 * @param id string identifier of {@link Attribute} descendant (ATTRIBUTE_CODE).
	 * @return newly created {@link ObjectId} identifier of {@link Attribute} descendant.
	 */
	public static ObjectId createAttributeId(String type, String id) {
		return new ObjectId(getAttributeClass(type), id);
	}
	
	/**
	 * Creates new instance of one of {@link Attribute} descendant classes
	 * @param type string type code of attribute to create 
	 * @param id string identifier of attribute
	 * @return newly created instance of one of {@link Attribute} descendant classes
	 */
	public static Attribute createAttribute(String type, String id) {
		return (Attribute) DataObject.createFromId(createAttributeId(type, id));
	}

	/**
	 * Checks if given attribute type couldn't be modified
	 * @param type string code of attribute type
	 * @return true if attributes of given type is read-only, false otherwise 
	 */
	public static boolean isReadOnlyType(Object type) {
		return BACK_LINK.equals(type);
	}
}
