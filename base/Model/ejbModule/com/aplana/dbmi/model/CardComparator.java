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
package com.aplana.dbmi.model;

import java.util.Comparator;

import com.aplana.dbmi.action.SearchResult;

public class CardComparator implements Comparator<Card> {

	private ObjectId byAttrId = null;
	private int direction = SearchResult.Column.SORT_ASCENDING;
	
	public CardComparator(ObjectId byAttrId) {
		this.byAttrId = byAttrId;
	}
	
	public CardComparator(ObjectId byAttrId, int direction) {
		this.byAttrId = byAttrId;
		this.direction = direction;
	}

	public int compare(Card card1, Card card2) {
		Attribute attr1 = card1.getAttributeById(byAttrId);
		Attribute attr2 = card2.getAttributeById(byAttrId);
		if (direction == SearchResult.Column.SORT_DESCENGING) {
			return (0 - attr1.compareTo(attr2));
		}
		else {
			return attr1.compareTo(attr2);
		}
	}
	
}
