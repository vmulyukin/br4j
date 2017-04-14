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
package com.aplana.dbmi.action;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used for creation new {@link Card} instances on base of existsing card example.
 * Card example is a {@link Card} created by special template. Mapping between attributes of
 * example card and newly created card must be specified in separate file.
 * Expected result of this action is {@link ObjectId} identifier of newly created {@link Card}    
 * @author DSultanbekov
 */
public class CreateCardByExample implements Action {
	private static final long serialVersionUID = 1L;
	private ObjectId exampleId;

	/**
	 * @return ObjectId.class as action should return identifier of newly created card
	 */
	public Class getResultType() {
		return ObjectId.class;
	}

	/**
	 * Returns identifier of example {@link Card card}
	 * @return identifier of example {@link Card card}
	 */
	public ObjectId getExampleId() {
		return exampleId;
	}

	/**
	 * Sets identifier of example {@link Card card} 
	 * @param exampleId identifier of example {@link Card card}
	 */
	public void setExampleId(ObjectId exampleId) {
		this.exampleId = exampleId;
	}
}
