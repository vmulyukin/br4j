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
package com.aplana.dbmi.message;

import com.aplana.dbmi.model.ObjectId;

public class CardChangeEvent implements Event{
	private final ObjectId m_cardId;
	public CardChangeEvent(ObjectId cardId){
		if (cardId == null){
			throw new NullPointerException("Need set card id");
		}
		m_cardId = cardId;
	}
	
	public ObjectId getCardId(){
		return m_cardId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CardChangeEvent other = (CardChangeEvent) obj;
		if (m_cardId == null) {
			if (other.m_cardId != null)
				return false;
		} else if (!m_cardId.equals(other.m_cardId))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_cardId == null) ? 0 : m_cardId.hashCode());
		return result;
	}

}
