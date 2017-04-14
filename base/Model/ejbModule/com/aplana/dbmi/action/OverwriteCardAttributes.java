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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used to overwrite given set of attributes for given {@link Card}
  */
public class OverwriteCardAttributes implements ObjectAction<Void> {
	private static final long serialVersionUID = 1L;

	private ObjectId cardId;
	private Collection<? extends Attribute> attributes;
	private boolean insertOnly = false;

	/**
	 * @return true if no deletion of previous values is required, false - otherwise
	 */
	public boolean isInsertOnly() {
		return insertOnly;
	}

	/**
	 * Sets value of insertOnly flag. If value is false then corresponding query will
	 * delete all previous values from DB and inserts new instead. If value of this flag is false
	 * then query will only insert new records and leaves previous (if any exists) untouched.
	 * @param insertOnly desired value of insertOnly flag
	 */
	public void setInsertOnly(boolean insertOnly) {
		this.insertOnly = insertOnly;
	}

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
	public Class<?> getResultType() {
		return null;
	}

	/**
	 * Gets collection of attributes values to be overwritten
	 * @return collection of {@link Attribute} descendants
	 */
	@SuppressWarnings("unchecked")
	public <T extends Attribute> Collection<T> getAttributes() {
		return (Collection<T>)attributes;
	}

	/**
	 * Sets collection of attributes values to be overwritten
	 * @param attributes collection of {@link Attribute} descendants
	 */
	public void setAttributes(Collection<? extends Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public ObjectId getObjectId() {
		return getCardId();
	}
}
