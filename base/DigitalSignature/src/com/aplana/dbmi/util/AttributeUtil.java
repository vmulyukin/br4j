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

import java.util.Collection;
import java.util.Date;

import com.aplana.dbmi.model.*;

public class AttributeUtil
{
	public static void setValue (Attribute attr, Object value) {
		if (CardLinkAttribute.class.equals(attr.getId().getType()))
			// (2010/02, RuSA) OLD: ((CardLinkAttribute) attr).setValues((Collection) value);
			((CardLinkAttribute) attr).setIdsLinked((Collection) value);
		else if (StringAttribute.class.equals(attr.getId().getType()))
			((StringAttribute) attr).setValue((String) value);
		else if (attr.getId().getType().isInstance(TextAttribute.class))
			((TextAttribute) attr).setValue((String) value);
		else if (IntegerAttribute.class.equals(attr.getId().getType()))
			((IntegerAttribute) attr).setValue(((Number) value).intValue());
		else if (DateAttribute.class.equals(attr.getId().getType()))
			((DateAttribute) attr).setValue((Date) value);
		else if (ListAttribute.class.equals(attr.getId().getType()))
			((ListAttribute) attr).setValue((ReferenceValue) value);
		else if (TreeAttribute.class.equals(attr.getId().getType()))
			((TreeAttribute) attr).setValues((Collection) value);
		else if (PersonAttribute.class.equals(attr.getId().getType()))
			((PersonAttribute) attr).setValues((Collection) value);
		else
			throw new IllegalArgumentException("Unknown attribute type: " + attr.getId().getType());
	}
	
	public static ObjectId makeAttributeId(Object type, String id) {
		if (Attribute.TYPE_STRING.equals(type))
			return new ObjectId(StringAttribute.class, id);
		if (Attribute.TYPE_TEXT.equals(type))
			return new ObjectId(TextAttribute.class, id);
		if (Attribute.TYPE_HTML.equals(type))
			return new ObjectId(HtmlAttribute.class, id);
		if (Attribute.TYPE_INTEGER.equals(type))
			return new ObjectId(IntegerAttribute.class, id);
        if (Attribute.TYPE_LONG.equals(type))
			return new ObjectId(LongAttribute.class, id);
		if (Attribute.TYPE_DATE.equals(type))
			return new ObjectId(DateAttribute.class, id);
		if (Attribute.TYPE_LIST.equals(type))
			return new ObjectId(ListAttribute.class, id);
		if (Attribute.TYPE_TREE.equals(type))
			return new ObjectId(TreeAttribute.class, id);
		if (Attribute.TYPE_PERSON.equals(type))
			return new ObjectId(PersonAttribute.class, id);
		if (Attribute.TYPE_CARD_LINK.equals(type))
			return new ObjectId(CardLinkAttribute.class, id);
		if (Attribute.TYPE_MATERIAL.equals(type))
			return new ObjectId(MaterialAttribute.class, id);
		if (Attribute.TYPE_TYPED_CARD_LINK.equals(type))
			return new ObjectId(TypedCardLinkAttribute.class, id);
		throw new IllegalArgumentException("Unknown attribute type: " + type);
	}
}
