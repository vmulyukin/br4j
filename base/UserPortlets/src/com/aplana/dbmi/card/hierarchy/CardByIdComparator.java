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



/**
 * Comparator used to sort collection of cards by cardId
 * @author okravchenko
 */
class CardByIdComparator implements Comparator<HierarchyItem> {
	private boolean descending;
	
	public CardByIdComparator( boolean descending) {
			this.descending = descending;
	}
	
	public boolean isDescending() {
		return descending;
	}

	public void setDescending(boolean descending) {
		this.descending = descending;
	}

	// public int compare(Object obj1, Object obj2)
	public int compare(HierarchyItem obj1, HierarchyItem obj2) {
		
		final Long cardId1 = (Long)((CardHierarchyItem)obj1).getCard().getId().getId();
		final Long cardId2 = (Long)((CardHierarchyItem)obj2).getCard().getId().getId();

		if (cardId1 < cardId2 )
			return descending ? 1 : -1;
		else if (cardId1 > cardId2) 
			return descending ? -1 : 1;
		else return 0;
		
	}

}
