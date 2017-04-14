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
package com.aplana.dbmi.card.util;

import java.util.Comparator;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;

/**
 * TODO: ���������� ��� � CardHierarchyItemComparator
 * @author DSultanbekov
 */
public class CardAttrComparator implements Comparator<Card> {
	protected boolean descending;
	private ObjectId sortAttributeId;
	
	public CardAttrComparator(ObjectId sortAttributeId, boolean descending) {
		this.sortAttributeId = sortAttributeId;
		this.descending = descending;
	}

	public boolean isDescending() {
		return descending;
	}

	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	public ObjectId getSortAttribute() {
		return sortAttributeId;
	}

	public void setSortAttribute(ObjectId sortAttribute) {
		this.sortAttributeId = sortAttribute;
	}
	
	public int compare(Card obj1, Card obj2) {
		if (sortAttributeId.getClass() == null) {
			throw new IllegalStateException("Sort attribute is not defined");
		}
		Attribute a1 = obj1.getAttributeById(sortAttributeId);
		Attribute a2 = obj2.getAttributeById(sortAttributeId);

		if (a1 == null && a2 != null) {
			return descending ? 1 : -1;
		} else if (a1 != null && a2 == null) {
			return descending ? -1 : 1;
		} else if (a1 == null && a2 == null) {
			return 0;
		}
		
		Class attrType = sortAttributeId.getType();		
		int result;		
		if (attrType.equals(IntegerAttribute.class)) {
			IntegerAttribute attr1 = (IntegerAttribute)a1,
				attr2 = (IntegerAttribute)a2;
			if (attr1.getValue() < attr2.getValue()) {
				result = -1;
			} else if (attr1.getValue() == attr2.getValue()){
				result = 0;
			} else {
				result = 1;
			}
		} else if (attrType.equals(DateAttribute.class)) {
			DateAttribute attr1 = (DateAttribute) a1;
			DateAttribute attr2 = (DateAttribute) a2;
			if (attr1.getValue() == null && attr2.getValue() == null) {
				result = 0;
			} else if (attr1.getValue() == null) {
				result = -1;
			} else if (attr2.getValue() == null) {
				result = 1;
			} else {
				result = attr1.getValue().compareTo(attr2.getValue());
			}
		} else if (attrType.equals(PersonAttribute.class)) {
			PersonAttribute attr1 = (PersonAttribute) a1;
			PersonAttribute attr2 = (PersonAttribute) a2;
			if (attr1.getPersonName() == null && attr2.getPersonName() == null) {
				result = 0;
			} else if (attr1.getPersonName() == null) {
				result = -1;
			} else if (attr2.getPersonName() == null) {
				result = 1;
			} else {
				result = attr1.getPersonName().compareToIgnoreCase(attr2.getPersonName());
			}
		} else if (attrType.isAssignableFrom(StringAttribute.class)) {
			StringAttribute attr1 = (StringAttribute)a1,
				attr2 = (StringAttribute)a2;
			if (attr1.getValue() == null && attr2.getValue() == null) {
				result = 0;
			} else if (attr1.getValue() == null) {
				result = -1;
			} else if (attr2.getValue() == null) {
				result = 1;
			} else {
				result = attr1.getValue().compareToIgnoreCase(attr2.getValue());
			}
		} else if (attrType.equals(ListAttribute.class)) {
			ListAttribute attr1 = (ListAttribute)a1,
				attr2 = (ListAttribute)a2;
			if (attr1.getValue() == null && attr2.getValue() == null) {
				result = 0;
			} else if (attr1.getValue() == null) {
				result = -1;
			} else if (attr2.getValue() == null){
				result = 1;
			} else {
				result = attr1.getValue().getValue().compareToIgnoreCase(attr2.getValue().getValue());
			}
		} else {
			throw new IllegalStateException("Sorting by '" + attrType.getCanonicalName() + "' is not allowed");
		}
		if (descending) {
			result *= -1;
		}
		return result;
	}
}
