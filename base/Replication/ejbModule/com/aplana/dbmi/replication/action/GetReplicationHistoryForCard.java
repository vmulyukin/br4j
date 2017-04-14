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
package com.aplana.dbmi.replication.action;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ObjectId;

/**
 * Экшен для получения ид карточки истории репликации самой последней для данной карточки репликации.
 * @author ppolushkin
 * @since 25.09.2014
 *
 */
public class GetReplicationHistoryForCard implements Action<ObjectId> {
	private static final long serialVersionUID = 1L;
	
	private ObjectId cardId;

	@Override
	public Class<?> getResultType() {
		return ObjectId.class;
	}

	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * @param cardId - ид карточки репликации для которой нужно получить последнюю карточку истории
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}
}