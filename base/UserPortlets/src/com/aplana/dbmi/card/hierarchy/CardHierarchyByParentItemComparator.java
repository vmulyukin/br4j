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

import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Person;

import com.aplana.dbmi.model.ObjectId;

public class CardHierarchyByParentItemComparator extends CardHierarchyItemComparator {
	private List<Attribute> sortOrderByParentAttrMap;
	
	public CardHierarchyByParentItemComparator(ObjectId sortAttributeId, List<Attribute> sortOrderByParentAttrMap, boolean descending) {
		super(sortAttributeId , descending);
		this.sortOrderByParentAttrMap = sortOrderByParentAttrMap;
		
	}
	public List<Attribute> getSortOrderByParentAttrMap() {
		return sortOrderByParentAttrMap;
	}

	public void setSortOrderByParentAttrMap(List<Attribute> sortOrderByParentAttrMap) {
		this.sortOrderByParentAttrMap = sortOrderByParentAttrMap;
	}

	public int compare(Object obj1, Object obj2) {
		ObjectId sortAttributeId = getSortAttribute();
		boolean descending = isDescending();
		
		if (sortAttributeId.getClass() == null) {
			throw new IllegalStateException("Sort attribute is not defined");
		}
		Attribute a1 = ((CardHierarchyItem)obj1).getCard().getAttributeById(sortAttributeId),
				a2 = ((CardHierarchyItem)obj2).getCard().getAttributeById(sortAttributeId);

		if (a1 == null && a2 != null) {
			return descending ? -1 : 1;
		} else if (a1 != null && a2 == null) {
			return descending ? 1 : -1;
		} else if (a1 == null && a2 == null) {
			return 0;
		}
		
		Class attrType = sortAttributeId.getType();		
		int result = 0;		
		if (attrType.equals(PersonAttribute.class)) {
			PersonAttribute attr1 = (PersonAttribute)a1,
				attr2 = (PersonAttribute)a2;
			if ((null == attr1.getValues()) ||  (attr1.getValues().size()== 0)) {
				result = 1;
			} else if ((null == attr2.getValues()) ||  (attr2.getValues().size()== 0)) {
				result = -1;
			} else if (attr1.getPerson().equals(attr2.getPerson())){
				final Long cardId1 = (Long)((CardHierarchyItem)obj1).getCard().getId().getId();
				final Long cardId2 = (Long)((CardHierarchyItem)obj2).getCard().getId().getId();
				if (cardId1 < cardId2 )
					return descending ? 1 : -1;
				else if (cardId1 > cardId2) 
					return descending ? -1 : 1;
				else return 0;
			} else {
				for (Attribute sortOrderByParentAttr : sortOrderByParentAttrMap) {
					Collection<Person> parentPersons = ((PersonAttribute)sortOrderByParentAttr).getValues();
					if (null != parentPersons) {
						for (Person p : parentPersons) {
							if (p.equals(attr1.getPerson()))
								return descending ? 1 : -1;
							else if (p.equals(attr2.getPerson()))
								return descending ? -1 : 1;
						}
					}
				}
			}
		}else {
			throw new IllegalStateException("Sorting by '" + attrType.getName() + "' is not allowed");
		}
		return result;
	}
}
