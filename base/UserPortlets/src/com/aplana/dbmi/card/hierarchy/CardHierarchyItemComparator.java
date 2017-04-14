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
package com.aplana.dbmi.card.hierarchy;

import java.util.Comparator;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;

/**
 * Comparator used to sort collection of cards by value of specified attribute
 * Sorting is allowed by attributes of following types: {@link IntegerAttribute}, {@link ListAttribute}
 * and by all descendants of {@link StringAttribute}
 * @author dsultanbekov
 */
class CardHierarchyItemComparator implements Comparator {
	private boolean descending;
	private ObjectId sortAttributeId;	

	public CardHierarchyItemComparator(ObjectId sortAttributeId, boolean descending) {
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

	public int compare(Object obj1, Object obj2) {
		if (sortAttributeId.getClass() == null) {
			throw new IllegalStateException("Sort attribute is not defined");
		}
		Attribute a1 = ((CardHierarchyItem)obj1).getCard().getAttributeById(sortAttributeId),
			a2 = ((CardHierarchyItem)obj2).getCard().getAttributeById(sortAttributeId);

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
		} else if (StringAttribute.class.isAssignableFrom(attrType)) {
			StringAttribute attr1 = (StringAttribute)a1,
				attr2 = (StringAttribute)a2;
			if (attr1.getValue() == null) {
				result = -1;
			} else if (attr2.getValue() == null) {
				result = 1;
			} else {
				result = attr1.getValue().compareToIgnoreCase(attr2.getValue());
			}
		} else if (attrType.equals(ListAttribute.class)) {
			ListAttribute attr1 = (ListAttribute)a1,
				attr2 = (ListAttribute)a2;
			if (attr1.getValue() == null) {
				result = -1;
			} else if (attr2.getValue() == null){
				result = 1;
			} else {
				result = attr1.getValue().getValue().compareToIgnoreCase(attr2.getValue().getValue());
			}
		} else {
			throw new IllegalStateException("Sorting by '" + attrType.getName() + "' is not allowed");
		}
		if (descending) {
			result *= -1;
		}
		return result;
	}
}
