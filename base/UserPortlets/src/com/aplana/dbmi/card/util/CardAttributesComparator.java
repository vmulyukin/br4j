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
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

public class CardAttributesComparator implements Comparator<Card> {
	private boolean descending;
	private List<ObjectId> sortAttributeIds;
	private CardAttrComparator attributeComparator;
	
	public CardAttributesComparator(List<ObjectId> sortAttributeIds, boolean descending) {
		this.sortAttributeIds = sortAttributeIds;
		this.descending = descending;
		attributeComparator = new CardAttrComparator(null, descending);
	}

	public boolean isDescending() {
		return descending;
	}

	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	public List<ObjectId> getSortAttributes() {
		return sortAttributeIds;
	}

	public void setSortAttributes(List<ObjectId> sortAttributes) {
		this.sortAttributeIds = sortAttributes;
	}
	
	public int compare(Card obj1, Card obj2) {
		if (null == sortAttributeIds) {
			throw new IllegalStateException("Sort attributes are not defined");
		}
		
		Iterator<ObjectId> sortAttributesIterator = sortAttributeIds.iterator();
		
		if(!sortAttributesIterator.hasNext()) {
			throw new IllegalStateException("Sort attributes are not defined");
		}
		
		return compare(obj1, obj2, sortAttributesIterator);
	}
	
	private int compare(Card obj1, Card obj2, Iterator<ObjectId> sortAttributesIterator) {
		if (!sortAttributesIterator.hasNext()) {
			return 0;
		}
		
		ObjectId sortAttributeId = sortAttributesIterator.next();
		attributeComparator.setSortAttribute(sortAttributeId);
		int result = attributeComparator.compare(obj1, obj2);
		
		if(result == 0) {
			result = compare(obj1, obj2, sortAttributesIterator);
		}
		return result;
	}
}