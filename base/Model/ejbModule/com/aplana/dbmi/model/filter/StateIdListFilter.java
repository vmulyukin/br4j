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
package com.aplana.dbmi.model.filter;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents filter for states by given state Id's.
 * 
 * @author Panichev
 * 
 */

public class StateIdListFilter implements Filter {

	private static final long serialVersionUID = -7156879906388815729L;

	private Collection stateIds = new ArrayList();

	public StateIdListFilter() {
	}

	public StateIdListFilter(Collection stateIds) {
		super();
		this.stateIds = stateIds;
	}

	public Collection getStateIds() {
		return stateIds;
	}

	public void setStateIds(Collection stateIds) {
		this.stateIds = stateIds;
	}
}
