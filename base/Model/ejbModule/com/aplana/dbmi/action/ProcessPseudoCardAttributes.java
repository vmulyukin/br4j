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

import java.util.Collection;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PseudoAttribute;

/**
 * Action used to process given set of pseudo attributes for given {@link Card}
  */
public class ProcessPseudoCardAttributes implements ObjectAction {
	private static final long serialVersionUID = 1L;

	private ObjectId cardId;
	private Collection<PseudoAttribute> attributes;


	/**
	 * Returns identifier of {@link Card} whose attributes are being changed
	 * @return identifier of {@link Card}
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of {@link Card} whose attributes are being changed
	 * @param cardId identifier of {@link Card}
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * always returns null
	 */
	public Class getResultType() {
		return null;
	}

	/**
	 * Gets collection of attributes values to be processed
	 * @return collection of {@link PseudoAttribute} descendants
	 */
	public Collection<PseudoAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Sets collection of attributes values to be processed
	 * @param attributes collection of {@link PseudoAttribute} descendants
	 */
	public void setAttributes(Collection<PseudoAttribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ObjectId getObjectId() {
		return getCardId();
	}
}
