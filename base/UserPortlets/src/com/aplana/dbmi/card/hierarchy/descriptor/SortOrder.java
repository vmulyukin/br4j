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
/**
 * 
 */
package com.aplana.dbmi.card.hierarchy.descriptor;

public class SortOrder {
	private String value;
	public static final SortOrder ASCENDING = new SortOrder("asc");
	public static final SortOrder DESCENDING = new SortOrder("desc");
	public static final SortOrder AUTO = new SortOrder("auto");
	public static final SortOrder NONE = new SortOrder("none");
	private static final SortOrder[] constants = {
		ASCENDING,
		DESCENDING,
		AUTO,
		NONE
	};
	private SortOrder(String st) {
		this.value = st;
	}
	public boolean equals(Object obj) {
		SortOrder type = (SortOrder)obj;
		return (obj != null && value.equals(type.value));
	}
	public int hashCode() {
		return value.hashCode();
	}
	public String toString() {
		return value;
	}
	public static SortOrder fromString(String val) {
		for (int i = 0; i < constants.length; ++i) {
			SortOrder type = constants[i];
			if (type.toString().equals(val)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown sort order: " + val);
	}
	
}