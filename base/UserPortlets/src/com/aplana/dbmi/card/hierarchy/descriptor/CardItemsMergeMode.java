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
package com.aplana.dbmi.card.hierarchy.descriptor;

/**
 * Class used to indicated what to do if same card appeared twice (or more times) during
 * fetching of parent hierarchy items.
 * NONE - indicates that no merging is required and two different hierarchyItems will be creates
 * CARDSET - indicates that if both cards belongs to same cardset then these cards should be merged in single hierarchyItem 
 * @author DSultanbekov
 */
public class CardItemsMergeMode {
	private final String value;		
	private CardItemsMergeMode(String value) {
		this.value = value;
	}
	
	public boolean equals(Object obj) {
		CardItemsMergeMode type = (CardItemsMergeMode)obj;
		return (obj != null && value.equals(type.value));
	}

	public int hashCode() {
		return value.hashCode();
	}

	public String toString() {
		return value;
	}
	public static CardItemsMergeMode fromString(String val) {
		for (int i = 0; i < constants.length; ++i) {
			CardItemsMergeMode type = constants[i];
			if (type.toString().equals(val)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown card items merge mode: " + val);
	}
	public static final CardItemsMergeMode NONE = new CardItemsMergeMode("none");
	public static final CardItemsMergeMode CARDSET = new CardItemsMergeMode("cardSet");
	private static final CardItemsMergeMode[] constants = new CardItemsMergeMode[] {
		NONE,
		CARDSET
	};

}
