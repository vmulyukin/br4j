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
package com.aplana.dbmi.search;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Attribute;

/**
 * Represents visitor for all searched attributes
 * 
 * @author skashanski
 *
 */
public abstract class SearchAttributeInitializer<T extends Attribute> {

	protected T attribute = null;

	public T getAttribute() {
		return attribute;
	}

	public void setAttribute(T attribute) {
		this.attribute = attribute;
	}

	/**
	 * Returns value from attribute
	 */
	protected abstract Object getValue();
	
	/**
	 * Initializes search values using passed value 
	 */
	protected abstract void setValue(Object attrValue, Search search);
	
	/**
	 * Checks if attribute is empty
	 */
	protected abstract boolean isEmpty();

	public void initialize(Search search, boolean saveMode) {
		Object attrValue = getValue();

		if (!isEmpty()) {
			setValue(attrValue, search);
		}
	}
}
