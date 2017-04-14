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
 *  This action class is used to add given card object to the 'Favorites' page.<br>
 *  In database-level it is implemented as adding new record to PERSON_CARD table.
 *  <br>
 *  Returns null as result. 
 */
public class AddToFavorites implements ObjectAction<Void> {
	private static final long serialVersionUID = 1L;
	private ObjectId card;

	/**
	 * Gets identifier of the card being added to the 'Favorites' page
	 * @return identifier of the card being added to the 'Favorites' page
	 */
	public ObjectId getCard() {
		return card;
	}

	/**
	 * Sets identifier of the {@link Card} object to be added to the 'Favorites' page
	 * @param card identifier of the {@link Card} object to be added to the 'Favorites' page
	 * @throws IllegalArgumentException if given identifier is not a {@link Card} identifier.
	 */
	public void setCard(ObjectId card) {
		if (card == null || !Card.class.equals(card.getType()))
			throw new IllegalArgumentException("Not a card id");
		this.card = card;
	}

	/**
	 * Sets identifier of the {@link Card} object to be added to the 'Favorites' page
	 * @param cardId numeric identifier of the {@link Card} object to 
	 * be added to the 'Favorites' page
	 */
	public void setCard(long cardId) {
		this.card = new ObjectId(Card.class, cardId);
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getCard();
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return null;
	}
}
