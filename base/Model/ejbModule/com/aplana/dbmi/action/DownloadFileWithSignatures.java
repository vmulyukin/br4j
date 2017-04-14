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

public class DownloadFileWithSignatures implements ObjectAction {

	private static final long serialVersionUID = 3L;

	private ObjectId cardId;

	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of card to download material from 
	 * @param cardId identifier of card to download material from
	 */
	public void setCardId(ObjectId cardId) {
		if (cardId == null || !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		this.cardId = cardId;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return Material.class;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getCardId();
	}
}
